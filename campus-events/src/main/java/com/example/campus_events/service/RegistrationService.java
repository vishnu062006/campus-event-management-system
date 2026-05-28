package com.example.campus_events.service;

import com.example.campus_events.model.*;
import com.example.campus_events.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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

    @PersistenceContext
    private EntityManager entityManager;

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

        double fee = event.getEntryFee() != null ? event.getEntryFee() : 0.0;

        long currentCount = registrationRepository.countByEvent(event);
        if (currentCount >= event.getMaxParticipants()) {
            long waitlistPos = waitingListRepository.countByEvent(event) + 1;
            waitingListRepository.save(new WaitingList(user, event, (int) waitlistPos));
            //emailService.sendWaitlistJoined(user, event, (int) waitlistPos);
            throw new IllegalStateException(
                    "Event is full. You've been added to the waitlist at position #" + waitlistPos
            );
        }

        if (fee > 0) {
            walletService.debit(user, fee, "Registration fee for: " + event.getTitle());
            paymentRepository.save(new Payment(user, event, fee, 0.0, "PAYMENT"));
        }

        Registration registration = new Registration(user, event);
        Registration saved = registrationRepository.save(registration);
        //emailService.sendRegistrationConfirmation(user, event);
        log.info("Registered User {} for Event {}", userId, eventId);
        return saved;
    }

    @Transactional
    public boolean cancelRegistration(Integer userId, Integer eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        Optional<Registration> regOpt = registrationRepository.findByUserAndEvent(user, event);
        if (regOpt.isEmpty()) return false;

        registrationRepository.delete(regOpt.get());
        registrationRepository.flush();
        entityManager.clear();

        double fee = event.getEntryFee() != null ? event.getEntryFee() : 0.0;
        boolean refunded = false;
        if (fee > 0) {
            double platformFee = fee * PLATFORM_FEE_PERCENT;
            double refundAmount = fee - platformFee;
            walletService.credit(user, refundAmount,
                    "Refund (95%) for cancellation: " + event.getTitle());
            paymentRepository.save(new Payment(user, event, refundAmount, platformFee, "REFUND"));
            refunded = true;
            log.info("Refunded ₹{} (platform fee ₹{}) to User {}", refundAmount, platformFee, userId);
        }

        //emailService.sendCancellationConfirmation(user, event, refunded);
        promoteFromWaitlist(event);
        return true;
    }
    @Transactional
    public void leaveWaitlist(Integer userId, Integer eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        WaitingList entry = waitingListRepository.findByUserAndEvent(user, event)
                .orElseThrow(() -> new IllegalArgumentException("Not on waitlist"));
        waitingListRepository.delete(entry);
        waitingListRepository.flush();
        entityManager.clear();
        reorderWaitlist(event);
    }
    @Transactional
    public void promoteFromWaitlist(Event event) {
        List<WaitingList> waitlist = waitingListRepository.findByEventOrderByPositionAsc(event);
        if (waitlist.isEmpty()) return;

        WaitingList first = waitlist.get(0);
        User promotedUser = first.getUser();
        double fee = event.getEntryFee() != null ? event.getEntryFee() : 0.0;

        if (fee > 0 && promotedUser.getWalletBalance() < fee) {
            log.info("User {} has insufficient balance, skipping promotion", promotedUser.getId());
            waitingListRepository.delete(first);
            waitingListRepository.flush();
            entityManager.clear();
            reorderWaitlist(event);
            promoteFromWaitlist(event);
            return;
        }

        if (fee > 0) {
            walletService.debit(promotedUser, fee, "Promoted from waitlist: " + event.getTitle());
            paymentRepository.save(new Payment(promotedUser, event, fee, 0.0, "PAYMENT"));
        }

        registrationRepository.save(new Registration(promotedUser, event));
        waitingListRepository.delete(first);
        waitingListRepository.flush();
        entityManager.clear();
        reorderWaitlist(event);
        //emailService.sendWaitlistPromotion(promotedUser, event);
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

    public Optional<Integer> getWaitlistPosition(Integer userId, Integer eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        return waitingListRepository.findByUserAndEvent(user, event)
                .map(WaitingList::getPosition);
    }
}