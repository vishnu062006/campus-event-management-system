package com.example.campus_events.controller;

import com.example.campus_events.model.WalletTransaction;
import com.example.campus_events.repository.PaymentRepository;
import com.example.campus_events.repository.UserRepository;
import com.example.campus_events.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private static final String ADMIN_EMAIL = "vmh@gmail.com";
    // Effectively infinite balance for admin — 10 crore rupees
    private static final double ADMIN_INFINITE_BALANCE = 10000.0;

    @Autowired private WalletService walletService;
    @Autowired private UserRepository userRepository;
    @Autowired private PaymentRepository paymentRepository;

    @GetMapping("/{userId}/balance")
    public ResponseEntity<?> getBalance(@PathVariable Integer userId) {
        return userRepository.findById(userId)
                .map(u -> {
                    // Admin always sees infinite balance
                    double balance = u.getEmail().equals(ADMIN_EMAIL)
                            ? ADMIN_INFINITE_BALANCE
                            : u.getWalletBalance();
                    return ResponseEntity.ok(Map.of("balance", balance));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{userId}/transactions")
    public ResponseEntity<List<WalletTransaction>> getTransactions(@PathVariable Integer userId) {
        return ResponseEntity.ok(walletService.getTransactionHistory(userId));
    }

    @GetMapping("/{userId}/payments")
    public ResponseEntity<?> getPayments(@PathVariable Integer userId) {
        return userRepository.findById(userId)
                .map(u -> ResponseEntity.ok(paymentRepository.findByUserOrderByCreatedAtDesc(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Mock top-up endpoint — used until Razorpay keys are wired in.
     * When Razorpay is ready, this gets replaced by:
     *   POST /api/wallet/create-order  → creates Razorpay order, returns orderId
     *   POST /api/wallet/verify-payment → verifies signature, credits wallet
     */
    @PostMapping("/{userId}/topup")
    public ResponseEntity<?> topup(@PathVariable Integer userId,
                                   @RequestBody Map<String, Object> body) {
        try {
            double amount = ((Number) body.get("amount")).doubleValue();

            if (amount < 10) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Minimum top-up amount is ₹10"));
            }
            if (amount > 50000) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Maximum top-up amount is ₹50,000"));
            }

            return userRepository.findById(userId)
                    .map(u -> {
                        // Admin doesn't need top-up — balance is always infinite
                        if (u.getEmail().equals(ADMIN_EMAIL)) {
                            return ResponseEntity.ok(Map.of(
                                    "message", "Admin account has unlimited balance",
                                    "balance", ADMIN_INFINITE_BALANCE
                            ));
                        }
                        walletService.credit(u, amount, "Wallet top-up via Razorpay");
                        return ResponseEntity.ok(Map.of(
                                "message", "₹" + amount + " added to wallet",
                                "balance", u.getWalletBalance()
                        ));
                    })
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Top-up failed: " + e.getMessage()));
        }
    }

    /* ══════════════════════════════════════════════════════════
       RAZORPAY ENDPOINTS — uncomment when keys are ready
       ══════════════════════════════════════════════════════════

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> body) {
        try {
            double amount = ((Number) body.get("amount")).doubleValue();
            // amount in paise for Razorpay
            long amountPaise = (long)(amount * 100);

            RazorpayClient client = new RazorpayClient(
                System.getenv("RAZORPAY_KEY_ID"),
                System.getenv("RAZORPAY_KEY_SECRET")
            );

            JSONObject options = new JSONObject();
            options.put("amount", amountPaise);
            options.put("currency", "INR");
            options.put("receipt", "wallet_topup_" + System.currentTimeMillis());
            options.put("payment_capture", 1);

            Order order = client.orders.create(options);

            return ResponseEntity.ok(Map.of(
                "orderId", order.get("id"),
                "amount", amountPaise,
                "currency", "INR"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Order creation failed: " + e.getMessage()));
        }
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, Object> body) {
        try {
            String orderId    = (String) body.get("razorpay_order_id");
            String paymentId  = (String) body.get("razorpay_payment_id");
            String signature  = (String) body.get("razorpay_signature");
            Integer userId    = (Integer) body.get("userId");
            double amount     = ((Number) body.get("amount")).doubleValue();

            // Verify HMAC-SHA256 signature
            String payload = orderId + "|" + paymentId;
            String secret  = System.getenv("RAZORPAY_KEY_SECRET");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(), "HmacSHA256"));
            String generatedSig = bytesToHex(mac.doFinal(payload.getBytes()));

            if (!generatedSig.equals(signature)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Payment verification failed"));
            }

            return userRepository.findById(userId)
                    .map(u -> {
                        walletService.credit(u, amount, "Wallet top-up · " + paymentId);
                        return ResponseEntity.ok(Map.of(
                            "message", "Payment verified. ₹" + amount + " added.",
                            "balance", u.getWalletBalance()
                        ));
                    })
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Verification error: " + e.getMessage()));
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
    ══════════════════════════════════════════════════════════ */
}