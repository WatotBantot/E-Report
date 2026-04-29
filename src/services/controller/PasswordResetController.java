package services.controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import config.database.DBConnection;
import daos.PasswordResetDao;
import models.UserInfo;
import services.EmailService;

/**
 * Controller for OTP-based password reset flow.
 *
 * Flow:
 * 1. User enters username → system looks up email → sends OTP
 * 2. User enters OTP → system verifies against stored code + expiry
 * 3. User enters new password → system updates in DB
 */
public class PasswordResetController {

    private final PasswordResetDao resetDao;
    private final EmailService emailService;

    // In-memory OTP store: username → OtpEntry
    // In production, use Redis or a database table with TTL
    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    // OTP expiry: 10 minutes
    private static final int OTP_EXPIRY_MINUTES = 10;
    // Max attempts before OTP is invalidated
    private static final int MAX_ATTEMPTS = 3;

    public PasswordResetController() {
        this.resetDao = new PasswordResetDao();
        this.emailService = new EmailService();
    }

    // ==================== STEP 1: SEND OTP ====================

    /**
     * Initiates password reset by sending OTP to user's registered email.
     *
     * @param username Username entered by user
     * @return Result with status and message
     */
    public OtpResult sendOtp(String username) {
        if (username == null || username.trim().isEmpty()) {
            return new OtpResult(false, "Username is required.");
        }

        try (Connection con = DBConnection.connect()) {
            UserInfo ui = resetDao.findByUsername(con, username.trim());

            if (ui == null) {
                // Don't reveal whether username exists — generic message
                return new OtpResult(false, "If this account exists, an OTP has been sent.");
            }

            String email = ui.getEmail();
            if (email == null || email.isBlank()) {
                return new OtpResult(false, "No email address on file for this account.");
            }

            String otp = emailService.generateOtp();
            boolean sent = emailService.sendOtpEmail(email, otp);

            if (!sent) {
                return new OtpResult(false, "Failed to send OTP. Please try again later.");
            }

            // Store OTP with expiry
            OtpEntry entry = new OtpEntry(otp, ui.getUI_ID(), email, Instant.now());
            otpStore.put(username, entry);

            // Mask email for display: j***@gmail.com
            String maskedEmail = maskEmail(email);

            return new OtpResult(true,
                    "OTP sent to " + maskedEmail + ". Valid for " + OTP_EXPIRY_MINUTES + " minutes.");

        } catch (SQLException e) {
            e.printStackTrace();
            return new OtpResult(false, "Database error. Please try again.");
        }
    }

    // ==================== STEP 2: VERIFY OTP ====================

    /**
     * Verifies the OTP entered by the user.
     *
     * @param username Username
     * @param otp      OTP entered by user
     * @return Result with status
     */
    public OtpResult verifyOtp(String username, String otp) {
        if (username == null || otp == null) {
            return new OtpResult(false, "Username and OTP are required.");
        }

        OtpEntry entry = otpStore.get(username);
        if (entry == null) {
            return new OtpResult(false, "No active OTP found. Please request a new one.");
        }

        // Check expiry
        Instant expiryTime = entry.createdAt.plus(OTP_EXPIRY_MINUTES, ChronoUnit.MINUTES);
        if (Instant.now().isAfter(expiryTime)) {
            otpStore.remove(username);
            return new OtpResult(false, "OTP has expired. Please request a new one.");
        }

        // Check max attempts
        if (entry.attempts >= MAX_ATTEMPTS) {
            otpStore.remove(username);
            return new OtpResult(false, "Too many failed attempts. Please request a new OTP.");
        }

        entry.attempts++;

        if (!entry.otpCode.equals(otp.trim())) {
            int remaining = MAX_ATTEMPTS - entry.attempts;
            if (remaining <= 0) {
                otpStore.remove(username);
                return new OtpResult(false, "Too many failed attempts. Please request a new OTP.");
            }
            return new OtpResult(false, "Invalid OTP. " + remaining + " attempt(s) remaining.");
        }

        // OTP verified — mark as verified
        entry.verified = true;
        return new OtpResult(true, "OTP verified successfully.");
    }

    // ==================== STEP 3: RESET PASSWORD ====================

    /**
     * Resets the password after OTP verification.
     *
     * @param username    Username
     * @param newPassword New password
     * @return Result with status
     */
    public OtpResult resetPassword(String username, String newPassword) {
        if (username == null || newPassword == null || newPassword.isEmpty()) {
            return new OtpResult(false, "Username and new password are required.");
        }

        OtpEntry entry = otpStore.get(username);
        if (entry == null || !entry.verified) {
            return new OtpResult(false, "OTP verification required before resetting password.");
        }

        try (Connection con = DBConnection.connect()) {
            con.setAutoCommit(false);
            boolean success = resetDao.updatePassword(con, entry.userId, newPassword);

            if (success) {
                con.commit();
                otpStore.remove(username);
                return new OtpResult(true, "Password reset successfully.");
            } else {
                con.rollback();
                return new OtpResult(false, "Failed to update password.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new OtpResult(false, "Database error: " + e.getMessage());
        }
    }

    /**
     * Clears any stored OTP for a user (e.g., on cancel).
     */
    public void clearSession(String username) {
        if (username != null) {
            otpStore.remove(username);
        }
    }

    // ==================== HELPERS ====================

    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 1)
            return email;

        String local = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        if (local.length() <= 2) {
            return local.charAt(0) + "***" + domain;
        }

        return local.charAt(0) + "***" + local.charAt(local.length() - 1) + domain;
    }

    // ==================== INNER CLASSES ====================

    private static class OtpEntry {
        final String otpCode;
        final int userId;
        final String email;
        final Instant createdAt;
        int attempts;
        boolean verified;

        OtpEntry(String otpCode, int userId, String email, Instant createdAt) {
            this.otpCode = otpCode;
            this.userId = userId;
            this.email = email;
            this.createdAt = createdAt;
            this.attempts = 0;
            this.verified = false;
        }
    }

    /**
     * Result object for OTP operations.
     */
    public static class OtpResult {
        public final boolean success;
        public final String message;

        public OtpResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}