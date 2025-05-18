package eu.accesa.price_comparator.repository;

import eu.accesa.price_comparator.model.PriceAlert;
import eu.accesa.price_comparator.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class PriceAlertRepositoryTest {

    @Autowired
    private PriceAlertRepository repo;

    @Autowired
    private ProductRepository productRepo;

    @BeforeEach
    void setup() {
        productRepo.save(new Product("Lidl_P001", "lapte zuzu", "lactate", "Zuzu", "l"));
        repo.save(new PriceAlert("user@example.com", "lapte zuzu", 9.99));
    }

    @Test
    void testFindAllByTriggeredFalse() {
        List<PriceAlert> alerts = repo.findAllByTriggeredFalse();
        assertEquals(1, alerts.size());
    }
}
