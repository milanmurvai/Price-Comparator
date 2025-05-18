package eu.accesa.price_comparator.service;

import eu.accesa.price_comparator.dto.basket.BasketRequest;
import eu.accesa.price_comparator.dto.basket.BasketResponse;
import eu.accesa.price_comparator.model.Discount;
import eu.accesa.price_comparator.model.Price;
import eu.accesa.price_comparator.model.Product;
import eu.accesa.price_comparator.model.Store;
import eu.accesa.price_comparator.repository.DiscountRepository;
import eu.accesa.price_comparator.repository.PriceRepository;
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
@Import(BasketService.class)
class BasketServiceTest {

    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private PriceRepository priceRepo;

    @Autowired
    private DiscountRepository discountRepo;

    @Autowired
    private StoreRepository storeRepo;

    @Autowired
    private BasketService basketService;

    private Store carrefour;

    @BeforeEach
    void setup() {
        discountRepo.deleteAll();
        priceRepo.deleteAll();
        productRepo.deleteAll();
        storeRepo.deleteAll();

        carrefour = storeRepo.save(new Store("Carrefour"));
        Store lidl = storeRepo.save(new Store("Lidl"));

        Product product1 = productRepo.save(new Product("Carrefour_P001", "lapte", "lactate", "Zuzu", "l"));
        Product product2 = productRepo.save(new Product("Lidl_P001", "lapte", "lactate", "Zuzu", "l"));

        priceRepo.save(new Price(carrefour, product1, 1.0, 10.0, "RON", LocalDate.of(2025, 5, 1)));
        priceRepo.save(new Price(lidl, product2, 1.0, 9.0, "RON", LocalDate.of(2025, 5, 1)));

        discountRepo.save(new Discount(
                carrefour, product1,
                LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 5, 7),
                20));
    }

    @Test
    void testOptimizeBasket_PicksCheapestStoreWithDiscount() {
        BasketRequest request = new BasketRequest(List.of("P001"), LocalDate.of(2025, 5, 1));
        List<BasketResponse> responses = basketService.optimizeBasket(request);

        assertEquals(1, responses.size());

        BasketResponse response = responses.get(0);
        assertEquals("Carrefour", response.store());
        assertEquals(1, response.items().size());
        assertEquals(8.0, response.total());
    }

    @Test
    void testOptimizeBasket_PicksLidlIfNoDiscount() {
        discountRepo.deleteAll();

        BasketRequest request = new BasketRequest(List.of("P001"), LocalDate.of(2025, 5, 1));
        List<BasketResponse> responses = basketService.optimizeBasket(request);

        assertEquals(1, responses.size());
        BasketResponse response = responses.get(0);
        assertEquals("Lidl", response.store());
        assertEquals(9.0, response.total());
    }

    @Test
    void testOptimizeBasket_ProductNotFound() {
        BasketRequest request = new BasketRequest(List.of("X999"), LocalDate.of(2025, 5, 1));
        List<BasketResponse> responses = basketService.optimizeBasket(request);

        assertTrue(responses.isEmpty());
    }

    @Test
    void testOptimizeBasket_MultipleProductsSameStore() {
        Product product2 = productRepo.save(new Product("Carrefour_P002", "paine", "panificatie", "Panif", "buc"));

        priceRepo.save(new Price(carrefour, product2, 1.0, 5.0, "RON", LocalDate.of(2025, 5, 1)));
        discountRepo.save(new Discount(carrefour, product2, LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 7), 10));

        BasketRequest request = new BasketRequest(List.of("P001", "P002"), LocalDate.of(2025, 5, 1));
        List<BasketResponse> responses = basketService.optimizeBasket(request);

        assertEquals(1, responses.size());
        BasketResponse response = responses.get(0);
        assertEquals("Carrefour", response.store());
        assertEquals(2, response.items().size());
        assertEquals(12.5, response.total());
    }
}
