package com.example.campus_events.service;

import com.example.campus_events.model.*;
import com.example.campus_events.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RegistrationService {

    private static final Logger log = LoggerFactory.getLogger(RegistrationService.class);
    private static final double PLATFORM_FEE_PERCENT = 0.05;

    @Autowired private RegistrationRepository registrationRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private WaitingListRepository waitingListRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private WalletService walletService;
    @Autowired private EmailService emailService;

    @Transactional
    public Registration registerForEvent(Integer userId, Integer eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        if (registrationRepository.existsByUserAndEvent(user, event)) {
            throw new IllegalStateException("You are already registered for this event");
        }
        if (waitingListRepository.existsByUserAndEvent(user, event)) {
            throw new IllegalStateException("You are already on the waitlist for this event");
        }

        // Wallet deduction for paid events
        double fee = event.getEntryFee() != null ? event.getEntryFee() : 0.0;
        if (fee > 0) {
            walletService.debit(user, fee, "Registration fee for: " + event.getTitle());
            paymentRepository.save(new Payment(user, event, fee, 0.0, "PAYMENT"));
        }

        long currentCount = registrationRepository.countByEvent(event);
        if (currentCount >= event.getMaxParticipants()) {
            // If paid, refund immediately since going to waitlist
            if (fee > 0) {
                walletService.credit(user, fee, "Refund — waitlisted for: " + event.getTitle());
                paymentRepository.save(new Payment(user, event, fee, 0.0, "REFUND"));
            }
            long waitlistPos = waitingListRepository.countByEvent(event) + 1;
            waitingListRepository.save(new WaitingList(user, event, (int) waitlistPos));
            emailService.sendWaitlistJoined(user, event, (int) waitlistPos);
            throw new IllegalStateException(
                    "Event is full. You've been added to the waitlist at position #" + waitlistPos
            );
        }

        Registration registration = new Registration(user, event);
        Registration saved = registrationRepository.save(registration);
        emailService.sendRegistrationConfirmation(user, event);
        log.info("Registered User {} for Event {}", userId, eventId);
        return saved;
    }

    @Transactional
    public boolean cancelRegistration(Integer userId, Integer eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        return registrationRepository.findByUserAndEvent(user, event)
                .map(registration -> {
                    registrationRepository.delete(registration);

                    // 95% refund (5% platform fee)
                    double fee = event.getEntryFee() != null ? event.getEntryFee() : 0.0;
                    boolean refunded = false;
                    if (fee > 0) {
                        double platformFee = fee * PLATFORM_FEE_PERCENT;
                        double refundAmount = fee - platformFee;
                        walletService.credit(user, refundAmount,
                                "Refund (95%) for cancellation: " + event.getTitle());
                        paymentRepository.save(
                                new Payment(user, event, refundAmount, platformFee, "REFUND"));
                        refunded = true;
                        log.info("Refunded ₹{} (platform fee ₹{}) to User {}",
                                refundAmount, platformFee, userId);
                    }

                    emailService.sendCancellationConfirmation(user, event, refunded);
                    promoteFromWaitlist(event);
                    return true;
                })
                .orElse(false);
    }

    private void promoteFromWaitlist(Event event) {
        List<WaitingList> waitlist = waitingListRepository.findByEventOrderByPositionAsc(event);
        if (waitlist.isEmpty()) return;

        WaitingList first = waitlist.get(0);
        User promotedUser = first.getUser();
        double fee = event.getEntryFee() != null ? event.getEntryFee() : 0.0;

        if (fee > 0) {
            if (promotedUser.getWalletBalance() < fee) {
                waitingListRepository.delete(first);
                reorderWaitlist(event);
                promoteFromWaitlist(event);
                return;
            }
            walletService.debit(promotedUser, fee, "Promoted from waitlist: " + event.getTitle());
            paymentRepository.save(new Payment(promotedUser, event, fee, 0.0, "PAYMENT"));
        }

        registrationRepository.save(new Registration(promotedUser, event));
        waitingListRepository.delete(first);
        reorderWaitlist(event);
        emailService.sendWaitlistPromotion(promotedUser, event);
        log.info("Promoted User {} from waitlist to Event {}", promotedUser.getId(), event.getId());
    }

    private void reorderWaitlist(Event event) {
        List<WaitingList> remaining = waitingListRepository.findByEventOrderByPositionAsc(event);
        for (int i = 0; i < remaining.size(); i++) remaining.get(i).setPosition(i + 1);
        waitingListRepository.saveAll(remaining);
    }

    public List<Registration> getParticipants(Integer eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        return registrationRepository.findByEvent(event);
    }

    public List<WaitingList> getWaitlist(Integer eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        return waitingListRepository.findByEventOrderByPositionAsc(event);
    }

    public boolean isRegistered(Integer userId, Integer eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        return registrationRepository.existsByUserAndEvent(user, event);
    }
}