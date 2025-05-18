package eu.accesa.price_comparator.service;

import eu.accesa.price_comparator.dto.discount.BestDiscountDto;
import eu.accesa.price_comparator.model.Discount;
import eu.accesa.price_comparator.model.Product;
import eu.accesa.price_comparator.model.Store;
import eu.accesa.price_comparator.repository.DiscountRepository;
import eu.accesa.price_comparator.repository.ProductRepository;
import eu.accesa.price_comparator.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(DiscountService.class)
class DiscountServiceTest {

    @Autowired
    private DiscountRepository discountRepo;

    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private StoreRepository storeRepo;

    @Autowired
    private DiscountService discountService;

    private Store carrefour;
    private Store lidl;
    private Product p1;
    private Product p3;

    @BeforeEach
    void setup() {
        discountRepo.deleteAll();
        productRepo.deleteAll();
        storeRepo.deleteAll();

        carrefour = storeRepo.save(new Store("Carrefour"));
        lidl = storeRepo.save(new Store("Lidl"));

        p1 = productRepo.save(new Product("Carrefour_P001", "Lapte", "Lactate", "Zuzu", "l"));
        Product p2 = productRepo.save(new Product("Lidl_P001", "Lapte", "Lactate", "Zuzu", "l"));
        p3 = productRepo.save(new Product("Lidl_P002", "Paine", "Panificatie", "Barilla", "buc"));

        discountRepo.save(new Discount(carrefour, p1,
                LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 5, 10),
                10));

        discountRepo.save(new Discount(lidl, p2,
                LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 5, 10),
                30));

        discountRepo.save(new Discount(lidl, p3,
                LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 5, 7),
                15));
    }

    @Test
    void testGetBestDiscounts() {
        List<BestDiscountDto> result = discountService.getBestDiscounts(LocalDate.of(2025, 5, 1));

        assertEquals(2, result.size());

        BestDiscountDto milk = result.stream().filter(d -> d.productId().equals("P001")).findFirst().orElseThrow();
        assertEquals("Lidl", milk.store());
        assertEquals(30, milk.discount());

        BestDiscountDto bread = result.stream().filter(d -> d.productId().equals("P002")).findFirst().orElseThrow();
        assertEquals("Lidl", bread.store());
        assertEquals(15, bread.discount());
    }

    @Test
    void testGetTodayDiscounts() {
        LocalDate today = LocalDate.now();
        discountRepo.save(new Discount(carrefour, p1, today, today, today.plusDays(3), 25));
        discountRepo.save(new Discount(lidl, p3, today, today, today.plusDays(3), 5));

        List<BestDiscountDto> result = discountService.getTodayDiscounts();

        assertEquals(2, result.size());

        assertTrue(result.stream().anyMatch(d -> d.productId().equals("P001") && d.discount() == 25));
        assertTrue(result.stream().anyMatch(d -> d.productId().equals("P002") && d.discount() == 5));
    }

    @Test
    void testEmptyDiscounts() {
        discountRepo.deleteAll();

        List<BestDiscountDto> best = discountService.getBestDiscounts(LocalDate.of(2025, 5, 1));
        List<BestDiscountDto> today = discountService.getTodayDiscounts();

        assertTrue(best.isEmpty());
        assertTrue(today.isEmpty());
    }
}
