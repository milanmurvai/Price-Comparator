package eu.accesa.price_comparator.service;

import eu.accesa.price_comparator.model.Product;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendNotification(String to, Product product, double price, String storeName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject("🔔 Price Alert: " + product.getName());
            helper.setText(
                    "<h2 style='color:#2c3e50;'>Great News!</h2>" +
                            "<p>The product <strong>" + product.getName() + "</strong> from <strong>" + product.getBrand() + "</strong> is now just " +
                            "<span style='color:green; font-weight:bold;'>" + price + " RON</span> at <strong>" + storeName + "</strong>.</p>" +
                            "<p>Don't miss the deal — available now!</p>" +
                            "<hr><small>This is an automated notification from Price Comparator.</small>",
                    true
            );

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }


}
