package eu.accesa.price_comparator.service;

import eu.accesa.price_comparator.dto.price.PriceAlertRequest;
import eu.accesa.price_comparator.dto.price.PriceHistoryPoint;
import eu.accesa.price_comparator.dto.price.PriceHistoryRequest;
import eu.accesa.price_comparator.dto.price.PriceHistoryResponse;
import eu.accesa.price_comparator.model.*;
import eu.accesa.price_comparator.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DataJpaTest
@Import(PriceService.class)
class PriceServiceTest {

    @MockBean
    private EmailService emailService;

    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private PriceRepository priceRepo;

    @Autowired
    private DiscountRepository discountRepo;

    @Autowired
    private StoreRepository storeRepo;

    @Autowired
    private PriceAlertRepository priceAlertRepo;

    @Autowired
    private PriceService priceService;

    @BeforeEach
    void setup() {
        Store store = new Store("Carrefour");
        Product product = new Product("Carrefour_P001", "lapte", "lactate", "Zuzu", "l");
        store = storeRepo.save(store);
        product = productRepo.save(product);

        priceRepo.save(new Price(store, product, 1.0, 10.0, "RON", LocalDate.of(2025, 5, 1)));
        priceRepo.save(new Price(store, product, 1.0, 8.0, "RON", LocalDate.of(2025, 5, 2)));

        discountRepo.save(new Discount(store, product, LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 5, 2), LocalDate.of(2025, 7, 20), 20));
    }

    @Test
    void testGetPriceHistory_WithDiscount() {
        PriceHistoryRequest request = new PriceHistoryRequest(
                "lapte",
                LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 5, 3),
                Optional.of("Carrefour"),
                Optional.of("Zuzu"),
                Optional.of("lactate")
        );

        List<PriceHistoryResponse> responses = priceService.getPriceHistory(request);
        assertEquals(1, responses.size());

        PriceHistoryResponse response = responses.get(0);
        assertEquals("Carrefour", response.store());
        assertEquals("Zuzu", response.brand());
        assertEquals("lactate", response.category());

        List<PriceHistoryPoint> points = response.points();
        assertEquals(2, points.size());

        assertEquals(LocalDate.of(2025, 5, 1), points.get(0).date());
        assertEquals(10.0, points.get(0).price());

        assertEquals(LocalDate.of(2025, 5, 2), points.get(1).date());
        assertEquals(6.4, points.get(1).price());
    }

    @Test
    void testCreateAlert() {
        PriceAlertRequest request = new PriceAlertRequest("test@example.com", "lapte", 5.0);
        String result = priceService.createAlert(request);

        assertTrue(result.contains("Alert created successfully"));
        assertEquals(1, priceAlertRepo.findAll().size());
    }

    @Test
    void testCheckPriceAlerts_SendsEmail() {
        priceAlertRepo.save(new PriceAlert("test@example.com", "lapte", 7.0));

        priceService.checkPriceAlerts();

        List<PriceAlert> alerts = priceAlertRepo.findAll();
        assertEquals(1, alerts.size());
        assertTrue(alerts.get(0).isTriggered());

        verify(emailService, times(1)).sendNotification(any(), any(), anyDouble(), any());
    }

    @Test
    void testCheckPriceAlerts_TooExpensive_NoEmail() {
        priceAlertRepo.save(new PriceAlert("test@example.com", "lapte", 5.0));

        priceService.checkPriceAlerts();

        List<PriceAlert> alerts = priceAlertRepo.findAll();
        assertFalse(alerts.get(0).isTriggered());

        verify(emailService, never()).sendNotification(any(), any(), anyDouble(), any());
    }
}
