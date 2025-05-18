package eu.accesa.price_comparator.service;

import eu.accesa.price_comparator.dto.product.SubstituteProductRequest;
import eu.accesa.price_comparator.dto.product.SubstituteProductResponse;
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
@Import(RecommendationService.class)
class RecommendationServiceTest {

    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private PriceRepository priceRepo;

    @Autowired
    private DiscountRepository discountRepo;

    @Autowired
    private StoreRepository storeRepo;

    @Autowired
    private RecommendationService recommendationService;

    @BeforeEach
    void setup() {
        Store carrefour = new Store("Carrefour");
        Product p1 = new Product("Carrefour_P001", "lapte", "lactate", "Zuzu", "l");
        Product p2 = new Product("Carrefour_P002", "lapte", "lactate", "Lacta", "l");
        Product p3 = new Product("Carrefour_P003", "lapte", "lactate", "NoBrand", "l");

        carrefour = storeRepo.save(carrefour);
        p1 = productRepo.save(p1);
        p2 = productRepo.save(p2);
        p3 = productRepo.save(p3);

        priceRepo.save(new Price(carrefour, p1, 1.0, 10.0, "RON", LocalDate.of(2025, 5, 1)));

        priceRepo.save(new Price(carrefour, p2, 1.0, 12.0, "RON", LocalDate.of(2025, 5, 1)));
        discountRepo.save(new Discount(carrefour, p2, LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 5), 50));

        priceRepo.save(new Price(carrefour, p3, 0.2, 2.0, "RON", LocalDate.of(2025, 5, 1)));
    }

    @Test
    void testGetRecommendedSubstitutes() {
        SubstituteProductRequest request = new SubstituteProductRequest("lapte", LocalDate.of(2025, 5, 1));
        List<SubstituteProductResponse> responses = recommendationService.getRecommendedSubstitutes(request);

        assertEquals(3, responses.size());

        SubstituteProductResponse best = responses.get(0);
        assertEquals("P002", best.productId());
        assertEquals(6.0, best.totalPrice());
        assertEquals(6.0, best.pricePerUnit());

        SubstituteProductResponse middle = responses.get(1);
        assertEquals("P001", middle.productId());
        assertEquals(10.0, middle.totalPrice());
        assertEquals(10.0, middle.pricePerUnit());

        SubstituteProductResponse last = responses.get(2);
        assertEquals("P003", last.productId());
        assertEquals(2.0, last.totalPrice());
        assertEquals(10.0, last.pricePerUnit());
    }

    @Test
    void testEmptyListIfNoPriceFound() {
        SubstituteProductRequest request = new SubstituteProductRequest("lapte", LocalDate.of(2020, 1, 1));
        List<SubstituteProductResponse> responses = recommendationService.getRecommendedSubstitutes(request);

        assertTrue(responses.isEmpty());
    }

    @Test
    void testReturnsOnlyMatchingProductName() {
        productRepo.save(new Product("Carrefour_X001", "apa", "bauturi", "Dorna", "l"));

        SubstituteProductRequest request = new SubstituteProductRequest("lapte", LocalDate.of(2025, 5, 1));
        List<SubstituteProductResponse> responses = recommendationService.getRecommendedSubstitutes(request);

        assertEquals(3, responses.size());
    }
}
