package eu.accesa.price_comparator.repository;

import eu.accesa.price_comparator.model.Price;
import eu.accesa.price_comparator.model.Product;
import eu.accesa.price_comparator.model.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class PriceRepositoryTest {

    @Autowired
    private PriceRepository priceRepo;

    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private StoreRepository storeRepo;

    private Product product;

    @BeforeEach
    void setup() {
        Store store = storeRepo.save(new Store("Lidl"));
        product = productRepo.save(new Product("Lidl_P001", "lapte", "lactate", "Zuzu", "l"));

        priceRepo.save(new Price(store, product, 1.0, 10.0, "RON", LocalDate.of(2025, 5, 1)));
        priceRepo.save(new Price(store, product, 1.0, 9.0, "RON", LocalDate.of(2025, 4, 1)));
    }

    @Test
    void testFindFirstByProductAndDateLessThanEqualOrderByDateDesc() {
        Optional<Price> price = priceRepo.findFirstByProductAndDateLessThanEqualOrderByDateDesc(
                product, LocalDate.of(2025, 5, 2));
        assertTrue(price.isPresent());
        assertEquals(10.0, price.get().getPrice());
    }

    @Test
    void testFindAllByProductAndDateBetweenOrderByDate() {
        List<Price> prices = priceRepo.findAllByProductAndDateBetweenOrderByDate(
                product, LocalDate.of(2025, 3, 1), LocalDate.of(2025, 5, 5));
        assertEquals(2, prices.size());
        assertEquals(9.0, prices.get(0).getPrice());
        assertEquals(10.0, prices.get(1).getPrice());
    }
}
