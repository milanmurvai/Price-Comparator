package eu.accesa.price_comparator.service;

import eu.accesa.price_comparator.model.Price;
import eu.accesa.price_comparator.model.Product;
import eu.accesa.price_comparator.model.Store;
import eu.accesa.price_comparator.repository.PriceRepository;
import eu.accesa.price_comparator.repository.ProductRepository;
import eu.accesa.price_comparator.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PriceCsvImporterTest {

    @Autowired
    private StoreRepository storeRepo;

    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private PriceRepository priceRepo;

    private PriceCsvImporter importer;

    @BeforeEach
    void setup() {
        importer = new PriceCsvImporter(storeRepo, productRepo, priceRepo);
    }

    @Test
    @DisplayName("Insert new store and new product from CSV")
    void testInsertNewStoreAndProduct() throws Exception {
        InputStream csv = new ClassPathResource("csv/prices/test_insert.csv").getInputStream();
        importer.importPrices(csv, "teststore", LocalDate.of(2025, 5, 1));

        List<Product> products = productRepo.findAll();
        assertEquals(1, products.size());
        Product p = products.get(0);
        assertEquals("teststore_P001", p.getId());
        assertEquals("lapte", p.getName());

        List<Price> prices = priceRepo.findAll();
        assertEquals(1, prices.size());
        Price price = prices.get(0);
        assertEquals(9.9, price.getPrice());
        assertEquals(1.0, price.getQuantity());
        assertEquals("RON", price.getCurrency());
        assertEquals(LocalDate.of(2025, 5, 1), price.getDate());
    }

    @Test
    @DisplayName("Reuses existing store and product")
    void testReusesExistingEntities() throws Exception {
        storeRepo.save(new Store("teststore"));
        productRepo.save(new Product("teststore_P001", "lapte", "lactate", "Zuzu", "l"));

        InputStream csv = new ClassPathResource("csv/prices/test_insert.csv").getInputStream();
        importer.importPrices(csv, "teststore", LocalDate.of(2025, 5, 1));

        assertEquals(1, productRepo.count());
        assertEquals(1, storeRepo.count());

        List<Price> prices = priceRepo.findAll();
        assertEquals(1, prices.size());
    }

    @Test
    @DisplayName("Multiple products in one CSV")
    void testMultipleRows() throws Exception {
        InputStream csv = new ClassPathResource("csv/prices/test_multiple.csv").getInputStream();
        importer.importPrices(csv, "teststore", LocalDate.of(2025, 5, 1));

        assertEquals(2, productRepo.count());
        assertEquals(2, priceRepo.count());
    }

    @Test
    @DisplayName("Invalid CSV format in price file → throws exception")
    void testInvalidPriceCsvThrowsException() throws Exception {
        InputStream csv = new ClassPathResource("csv/prices/invalid_price.csv").getInputStream();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                importer.importPrices(csv, "teststore", LocalDate.of(2025, 5, 1))
        );

        assertTrue(ex.getMessage().startsWith("Invalid CSV format"));
    }

}
