package eu.accesa.price_comparator.service;

import eu.accesa.price_comparator.model.Product;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.Mockito.*;

@SpringBootTest
class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    @MockBean
    private JavaMailSender mailSender;

    @Test
    void testSendNotification() {
        Product product = new Product("Store_P123", "Lapte", "Lactate", "Zuzu", "l");
        String recipient = "test@example.com";
        double price = 4.99;
        String store = "Carrefour";

        MimeMessage mockMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mockMessage);

        emailService.sendNotification(recipient, product, price, store);

        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mockMessage);
    }

}
