package com.fmcg.ecommerce.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl {

    private final JavaMailSender mailSender;

    @Async
    public void sendOtpEmail(String to, String otp, String name) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("Your FMCG eCommerce OTP Code");
            helper.setText(buildOtpHtml(name, otp), true);
            mailSender.send(message);
            log.info("OTP email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send OTP email to {}: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendOrderConfirmationEmail(String to, String name, String orderNumber, String total) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("Order Confirmed - " + orderNumber);
            helper.setText(buildOrderConfirmHtml(name, orderNumber, total), true);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send order confirmation to {}: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendOrderStatusEmail(String to, String name, String orderNumber, String status) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("Order Update - " + orderNumber);
            helper.setText(buildStatusUpdateHtml(name, orderNumber, status), true);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send status email to {}: {}", to, e.getMessage());
        }
    }

    private String buildOtpHtml(String name, String otp) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background: #f9f9f9; padding: 20px; border-radius: 12px;">
              <div style="background: linear-gradient(135deg, #667eea, #764ba2); padding: 30px; border-radius: 12px; text-align: center;">
                <h1 style="color: white; margin: 0; font-size: 28px;">🛒 FMCG eCommerce</h1>
              </div>
              <div style="background: white; padding: 30px; border-radius: 12px; margin-top: 20px;">
                <h2 style="color: #333;">Hello, %s! 👋</h2>
                <p style="color: #555; font-size: 16px;">Your One-Time Password (OTP) for login is:</p>
                <div style="background: #f0f0ff; border: 2px dashed #667eea; border-radius: 12px; padding: 20px; text-align: center; margin: 20px 0;">
                  <span style="font-size: 42px; font-weight: bold; color: #667eea; letter-spacing: 8px;">%s</span>
                </div>
                <p style="color: #888; font-size: 14px;">⏰ This OTP expires in <strong>10 minutes</strong>.</p>
                <p style="color: #888; font-size: 14px;">🔒 Do not share this OTP with anyone.</p>
              </div>
              <p style="text-align: center; color: #aaa; font-size: 12px; margin-top: 20px;">© 2025 FMCG eCommerce. All rights reserved.</p>
            </div>
            """.formatted(name, otp);
    }

    private String buildOrderConfirmHtml(String name, String orderNumber, String total) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background: #f9f9f9; padding: 20px; border-radius: 12px;">
              <div style="background: linear-gradient(135deg, #11998e, #38ef7d); padding: 30px; border-radius: 12px; text-align: center;">
                <h1 style="color: white; margin: 0;">✅ Order Confirmed!</h1>
              </div>
              <div style="background: white; padding: 30px; border-radius: 12px; margin-top: 20px;">
                <h2 style="color: #333;">Thank you, %s!</h2>
                <p>Your order <strong>%s</strong> has been confirmed.</p>
                <p>Order Total: <strong style="color: #11998e;">₹%s</strong></p>
                <p>We'll notify you when your order is shipped. 🚚</p>
              </div>
            </div>
            """.formatted(name, orderNumber, total);
    }

    private String buildStatusUpdateHtml(String name, String orderNumber, String status) {
        String emoji = switch (status.toUpperCase()) {
            case "CONFIRMED" -> "✅";
            case "PREPARING" -> "👨‍🍳";
            case "OUT_FOR_DELIVERY" -> "🚴";
            case "DELIVERED" -> "📦";
            case "CANCELLED" -> "❌";
            default -> "📋";
        };
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
              <div style="background: #667eea; padding: 30px; border-radius: 12px; text-align: center;">
                <h1 style="color: white; margin: 0;">%s Order Update</h1>
              </div>
              <div style="background: white; padding: 30px; border-radius: 12px; margin-top: 20px;">
                <h2>Hi %s,</h2>
                <p>Your order <strong>%s</strong> status has been updated to:</p>
                <div style="background: #f0f0ff; padding: 15px; border-radius: 8px; text-align: center;">
                  <strong style="font-size: 20px; color: #667eea;">%s</strong>
                </div>
              </div>
            </div>
            """.formatted(emoji, name, orderNumber, status.replace("_", " "));
    }
}
