package eu.accesa.price_comparator.repository;

import eu.accesa.price_comparator.model.Discount;
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
class DiscountRepositoryTest {

    @Autowired
    private DiscountRepository discountRepo;

    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private StoreRepository storeRepo;

    private Product product;
    private Store store;

    @BeforeEach
    void setup() {
        store = storeRepo.save(new Store("Lidl"));
        product = productRepo.save(new Product("Lidl_P001", "lapte", "lactate", "Zuzu", "l"));

        discountRepo.save(new Discount(store, product, LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 5, 10), LocalDate.of(2025, 5, 20), 15));
    }

    @Test
    void testExistsByStoreAndProductAndImportedDate() {
        assertTrue(discountRepo.existsByStoreAndProductAndImportedDate(
                store, product, LocalDate.of(2025, 5, 1)));
    }

    @Test
    void testFindByStoreAndProductAndFromDateBeforeAndToDateAfter() {
        Optional<Discount> discount = discountRepo
                .findByStoreAndProductAndFromDateBeforeAndToDateAfter(
                        store, product, LocalDate.of(2025, 5, 11), LocalDate.of(2025, 5, 19));
        assertTrue(discount.isPresent());
    }

    @Test
    void testFindFirstByStoreAndProductAndFromDateBeforeAndToDateAfter() {
        Optional<Discount> discount = discountRepo
                .findFirstByStoreAndProductAndFromDateBeforeAndToDateAfter(
                        store, product, LocalDate.of(2025, 5, 15), LocalDate.of(2025, 5, 15));
        assertTrue(discount.isPresent());
    }

    @Test
    void testFindAllByStoreAndProductAndFromDateAfterAndToDateBefore() {
        List<Discount> discounts = discountRepo
                .findAllByStoreAndProductAndFromDateAfterAndToDateBefore(
                        store, product, LocalDate.of(2025, 4, 30), LocalDate.of(2025, 5, 21));
        assertEquals(1, discounts.size());
    }

    @Test
    void testFindActiveDiscount() {
        Optional<Discount> discount = discountRepo.findActiveDiscount(
                product, store, LocalDate.of(2025, 5, 15));
        assertTrue(discount.isPresent());
    }

    @Test
    void testFindAllByImportedDate() {
        List<Discount> discounts = discountRepo.findAllByImportedDate(LocalDate.of(2025, 5, 1));
        assertEquals(1, discounts.size());
    }

    @Test
    void testFindBestActiveDiscountPerProduct() {
        List<Discount> best = discountRepo.findBestActiveDiscountPerProduct(LocalDate.of(2025, 5, 15));
        assertEquals(1, best.size());
        assertEquals(15, best.get(0).getPercentage());
    }
}

