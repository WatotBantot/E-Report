package services;

import java.util.Properties;
import java.util.Random;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import config.AppConfig;

/**
 * Service for sending emails via SMTP using Jakarta Mail.
 * Currently supports OTP delivery for password reset.
 */
public class EmailService {

    private final Session session;
    private final Random random;

    public EmailService() {
        this.random = new Random();
        this.session = createSession();
    }

    /**
     * Sends an OTP code to the specified email address.
     *
     * @param toEmail Recipient email address
     * @param otp     The 6-digit OTP code
     * @return true if sent successfully
     */
    public boolean sendOtpEmail(String toEmail, String otp) {
        if (session == null) {
            System.err.println("Email session not initialized. Check SMTP configuration.");
            return false;
        }

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(AppConfig.SMTP_USERNAME, AppConfig.SMTP_FROM_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Password Reset OTP - Barangay E-Reporting System");

            String htmlContent = buildOtpEmailBody(otp);
            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("OTP email sent successfully to: " + toEmail);
            return true;

        } catch (Exception e) {
            System.err.println("Failed to send OTP email to: " + toEmail);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Generates a random 6-digit OTP code.
     */
    public String generateOtp() {
        int otp = 100000 + random.nextInt(900000); // 100000 to 999999
        return String.valueOf(otp);
    }

    // ==================== PRIVATE ====================

    private Session createSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", AppConfig.SMTP_USE_TLS);
        props.put("mail.smtp.host", AppConfig.SMTP_HOST);
        props.put("mail.smtp.port", AppConfig.SMTP_PORT);
        props.put("mail.smtp.ssl.trust", AppConfig.SMTP_HOST);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(AppConfig.SMTP_USERNAME, AppConfig.SMTP_PASSWORD);
            }
        });
    }

    private String buildOtpEmailBody(String otp) {
        String template = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: 'Segoe UI', Arial, sans-serif; background: #f5f5f5; padding: 20px; }
                        .container { max-width: 480px; margin: 0 auto; background: white; border-radius: 12px; padding: 32px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
                        .logo { text-align: center; margin-bottom: 24px; }
                        h2 { color: #333; text-align: center; margin-bottom: 8px; }
                        p { color: #666; line-height: 1.6; font-size: 14px; }
                        .otp-box { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; font-size: 32px; font-weight: bold; text-align: center; padding: 20px; border-radius: 8px; margin: 24px 0; letter-spacing: 8px; }
                        .warning { background: #fff3cd; border-left: 4px solid #ffc107; padding: 12px; margin-top: 20px; font-size: 13px; color: #856404; }
                        .footer { text-align: center; margin-top: 24px; font-size: 12px; color: #999; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h2>🔒 Password Reset Request</h2>
                        <p>Hello,</p>
                        <p>We received a request to reset your password for the <strong>Barangay Malacañang E-Reporting System</strong>.</p>
                        <p>Use the following One-Time Password (OTP) to proceed:</p>
                        <div class="otp-box">%s</div>
                        <p>This code will expire in <strong>10 minutes</strong>. Do not share this code with anyone.</p>
                        <div class="warning">
                            ⚠️ If you did not request this password reset, please ignore this email and ensure your account is secure.
                        </div>
                        <div class="footer">
                            Barangay Malacañang E-Reporting System<br>
                            This is an automated message — please do not reply.
                        </div>
                    </div>
                </body>
                </html>
                """;
        return template.replace("%s", otp);
    }

    public static void main(String[] args) {
        EmailService svc = new EmailService();
        boolean ok = svc.sendOtpEmail("renjo.barrientos@gmail.com", "123456");
        System.out.println("Sent: " + ok);
    }
}