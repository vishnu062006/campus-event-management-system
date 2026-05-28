package com.example.campus_events.service;

import com.example.campus_events.model.Event;
import com.example.campus_events.model.User;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    private double safeEntryFee(Event event) {
        return event.getEntryFee() != null ? event.getEntryFee() : 0.0;
    }

    // This method handles the actual sending and catching of the timeout error.
    // This method handles the actual sending and catching of the timeout error.
    private void sendHtml(String to, String subject, String html) {
        try {
            // ---------------------------------------------------------
            // TEMPORARILY DISABLED TO PREVENT GMAIL SMTP TIMEOUTS
            // ---------------------------------------------------------
            /*
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            helper.setFrom("vishnumashalkar.cs24@bmsce.ac.in", "Eventara");
            mailSender.send(message);
            */

            // SIMULATE SUCCESSFUL EMAIL SENDING IN LOGS
            log.info("✅ [MOCK EMAIL] Successfully 'sent' to: {} | Subject: {}", to, subject);

        } catch (Exception e) {
            log.error("Failed to process mock email to {}. Reason: {}", to, e.getMessage());
        }
    }

    private String baseTemplate(String content) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="UTF-8"/>
              <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
            </head>
            <body style="margin:0;padding:0;background:#060910;font-family:'Segoe UI',Arial,sans-serif;">
              <div style="max-width:560px;margin:40px auto;background:#0d1117;border:1px solid #21262d;border-radius:16px;overflow:hidden;">
                
                <div style="background:linear-gradient(135deg,#0d1f35,#1a0d35);padding:32px 32px 24px;border-bottom:1px solid #21262d;">
                  <div style="font-size:22px;font-weight:800;color:#fff;letter-spacing:-0.04em;">
                    Event<span style="background:linear-gradient(135deg,#58a6ff,#a78bfa);-webkit-background-clip:text;-webkit-text-fill-color:transparent;">ara</span>
                  </div>
                  <div style="font-size:11px;color:#8b949e;margin-top:4px;letter-spacing:0.08em;text-transform:uppercase;">
                    Campus Event Management
                  </div>
                </div>

                <div style="padding:32px;">
                  """ + content + """
                </div>

                <div style="padding:20px 32px;border-top:1px solid #21262d;background:#0a0f16;">
                  <p style="margin:0;font-size:11px;color:#8b949e;text-align:center;">
                    BMS College of Engineering · Bengaluru<br/>
                    <span style="color:#30363d;">© 2026 Eventara</span>
                  </p>
                </div>
              </div>
            </body>
            </html>
            """;
    }

    @Async
    public void sendRegistrationConfirmation(User user, Event event) {
        String firstName = user.getName().split(" ")[0];
        double entryFee = safeEntryFee(event);

        String feeHtml = entryFee > 0
                ? "<div style='display:flex;justify-content:space-between;padding:10px 0;border-bottom:1px solid #21262d;'><span style='color:#8b949e;font-size:13px;'>Entry Fee Paid</span><span style='color:#3fb950;font-size:13px;font-weight:700;'>₹" + entryFee + "</span></div>"
                : "<div style='display:flex;justify-content:space-between;padding:10px 0;border-bottom:1px solid #21262d;'><span style='color:#8b949e;font-size:13px;'>Entry Fee</span><span style='color:#3fb950;font-size:13px;font-weight:700;'>Free</span></div>";

        String content = """
            <div style="display:inline-flex;align-items:center;gap:8px;background:rgba(63,185,80,0.1);border:1px solid rgba(63,185,80,0.25);border-radius:20px;padding:6px 14px;margin-bottom:20px;">
              <span style="width:8px;height:8px;border-radius:50%;background:#3fb950;display:inline-block;"></span>
              <span style="font-size:12px;font-weight:700;color:#3fb950;letter-spacing:0.06em;text-transform:uppercase;">Registration Confirmed</span>
            </div>

            <h1 style="margin:0 0 8px;font-size:26px;font-weight:800;color:#fff;letter-spacing:-0.03em;">
              You're in, %s! 🎉
            </h1>
            <p style="margin:0 0 28px;font-size:14px;color:#8b949e;line-height:1.6;">
              Your spot at <strong style="color:#c9d1d9;">%s</strong> is confirmed.
            </p>

            <div style="background:#060910;border:1px solid #21262d;border-radius:12px;overflow:hidden;margin-bottom:24px;">
              <div style="padding:16px 20px;border-bottom:1px solid #21262d;">
                <div style="font-size:11px;font-weight:700;color:#8b949e;letter-spacing:0.08em;text-transform:uppercase;margin-bottom:6px;">Event Details</div>
                <div style="font-size:18px;font-weight:800;color:#fff;">%s</div>
              </div>
              <div style="padding:4px 20px 8px;">
                <div style="display:flex;justify-content:space-between;padding:10px 0;border-bottom:1px solid #21262d;">
                  <span style="color:#8b949e;font-size:13px;">📅 Date</span>
                  <span style="color:#c9d1d9;font-size:13px;font-weight:600;">%s</span>
                </div>
                <div style="display:flex;justify-content:space-between;padding:10px 0;border-bottom:1px solid #21262d;">
                  <span style="color:#8b949e;font-size:13px;">📍 Location</span>
                  <span style="color:#c9d1d9;font-size:13px;font-weight:600;">%s</span>
                </div>
                %s
              </div>
            </div>
            """.formatted(
                firstName,
                event.getTitle(),
                event.getTitle(),
                event.getEventDate().toLocalDate().toString(),
                event.getLocation(),
                feeHtml
        );

        sendHtml(user.getEmail(), "✅ You're registered for " + event.getTitle(), baseTemplate(content));
    }

    @Async
    public void sendCancellationConfirmation(User user, Event event, boolean refunded) {
        String firstName = user.getName().split(" ")[0];
        double entryFee = safeEntryFee(event);

        String refundHtml = refunded && entryFee > 0
                ? "<div style='background:rgba(63,185,80,0.08);border:1px solid rgba(63,185,80,0.2);border-radius:10px;padding:14px 16px;margin-top:20px;'><p style='margin:0;font-size:13px;color:#3fb950;'>💸 ₹" + String.format("%.0f", entryFee * 0.95) + " refunded to your Eventara wallet.</p></div>"
                : "";

        String content = """
            <h1 style="color:#fff;">Cancelled, %s</h1>
            <p style="color:#8b949e;">Your registration for %s has been cancelled.</p>
            %s
            """.formatted(firstName, event.getTitle(), refundHtml);

        sendHtml(user.getEmail(), "❌ Registration Cancelled — " + event.getTitle(), baseTemplate(content));
    }

    @Async
    public void sendWaitlistPromotion(User user, Event event) {
        String firstName = user.getName().split(" ")[0];
        double entryFee = safeEntryFee(event);

        String paymentHtml = entryFee > 0
                ? "<div style='background:rgba(248,81,73,0.08);border:1px solid rgba(248,81,73,0.2);border-radius:10px;padding:14px;'><p style='margin:0;font-size:13px;color:#f85149;'>💳 ₹" + entryFee + " automatically deducted from your wallet.</p></div>"
                : "";

        String content = """
            <h1 style="color:#fff;">Great news, %s! 🎊</h1>
            <p style="color:#8b949e;">You've been promoted from the waitlist for %s.</p>
            %s
            """.formatted(firstName, event.getTitle(), paymentHtml);

        sendHtml(user.getEmail(), "🎉 You're in! " + event.getTitle(), baseTemplate(content));
    }

    @Async
    public void sendWaitlistJoined(User user, Event event, int waitlistPos) {
        String firstName = user.getName().split(" ")[0];

        String content = """
        <div style="display:inline-flex;align-items:center;gap:8px;background:rgba(240,136,62,0.1);border:1px solid rgba(240,136,62,0.25);border-radius:20px;padding:6px 14px;margin-bottom:20px;">
          <span style="font-size:12px;font-weight:700;color:#f0883e;letter-spacing:0.06em;text-transform:uppercase;">Added to Waitlist</span>
        </div>

        <h1 style="margin:0 0 8px;font-size:26px;font-weight:800;color:#fff;letter-spacing:-0.03em;">
          You're #%d on the waitlist
        </h1>

        <p style="margin:0 0 24px;font-size:14px;color:#8b949e;line-height:1.6;">
          Hey <strong style="color:#c9d1d9;">%s</strong>, 
          <strong style="color:#c9d1d9;">%s</strong> is currently full, but we've added you to the waitlist.
          We'll automatically notify you if a spot opens.
        </p>

        <div style="background:#060910;border:1px solid #21262d;border-radius:12px;padding:20px;text-align:center;margin-bottom:20px;">
          <div style="font-size:48px;font-weight:800;color:#f0883e;letter-spacing:-0.04em;">#%d</div>
          <div style="font-size:12px;color:#8b949e;margin-top:4px;">Your waitlist position</div>
        </div>
        """.formatted(
                waitlistPos,
                firstName,
                event.getTitle(),
                waitlistPos
        );

        sendHtml(
                user.getEmail(),
                "⏳ You're on the waitlist — " + event.getTitle(),
                baseTemplate(content)
        );
    }
}