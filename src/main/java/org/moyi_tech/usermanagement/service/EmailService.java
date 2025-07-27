package org.moyi_tech.usermanagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender emailSender;

    public void sendPasswordResetEmail(String to, String resetToken) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@example.com");
        message.setTo(to);
        message.setSubject("密码重置请求");
        
        String resetUrl = "http://localhost:3000/reset-password?token=" + resetToken;
        message.setText("您好，\n\n" +
                "您请求重置密码。请点击以下链接重置您的密码：\n\n" +
                resetUrl + "\n\n" +
                "此链接将在1小时后过期。\n\n" +
                "如果您没有请求重置密码，请忽略此邮件。\n\n" +
                "谢谢！");

        emailSender.send(message);
    }
}
