package com.example.nursery.service;

import com.example.nursery.model.Order;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

import javax.mail.internet.MimeMessage;

@Service
public class NotificationService {

    // make this optional so app won't fail to start if no mail bean is present
    private final JavaMailSender mailSender;

    @Autowired
    public NotificationService(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ... rest of class unchanged, but check mailSender != null before sending email
    public void sendEmailWithAttachment(String to, String subject, String body, File attachment) {
        if (mailSender == null || to == null || to.isBlank()) return;
        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);
            helper.addAttachment(attachment.getName(), new FileSystemResource(attachment));
            mailSender.send(mime);
        } catch (Exception ex) {
            System.err.println("Failed to send email with attachment: " + ex.getMessage());
        }
    }
    // similarly guard other email calls

	public void sendSms(Object object, String string) {
		// TODO Auto-generated method stub
		
	}

	public void sendBillNotification(Order saved) {
		// TODO Auto-generated method stub
		
	}
}