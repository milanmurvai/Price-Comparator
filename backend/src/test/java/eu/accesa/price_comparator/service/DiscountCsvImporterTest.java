package eu.accesa.price_comparator.service;

import eu.accesa.price_comparator.model.Discount;
import eu.accesa.price_comparator.model.Product;
import eu.accesa.price_comparator.model.Store;
import eu.accesa.price_comparator.repository.DiscountRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class DiscountCsvImporterTest {

    @Autowired
    private ProductRepository productRepo;
    @Autowired
    private StoreRepository storeRepo;
    @Autowired
    private DiscountRepository discountRepo;

    private DiscountCsvImporter importer;
    private Store store;
    private Product product;
    private Product product2;

    @BeforeEach
    void setup() {
        importer = new DiscountCsvImporter(storeRepo, productRepo, discountRepo);

        store = new Store("teststore");
        store = storeRepo.save(store);

        product = productRepo.save(new Product("teststore_P001", "lapte", "lactate", "Zuzu", "l"));
        product2 = productRepo.save(new Product("teststore_P002", "paine", "panificatie", "Panificatie", "buc"));
    }

    @Test
    @DisplayName("No existing discount → inserts new")
    void testInsertNew() throws Exception {
        InputStream csv = new ClassPathResource("csv/test_insert.csv").getInputStream();
        importer.importDiscounts(csv, "teststore", LocalDate.of(2025, 5, 1));

        List<Discount> discounts = discountRepo.findAll();
        assertEquals(1, discounts.size());
        Discount d = discounts.get(0);
        assertEquals(10, d.getPercentage());
        assertEquals(LocalDate.of(2025, 5, 1), d.getFromDate());
        assertEquals(LocalDate.of(2025, 5, 7), d.getToDate());
    }

    @Test
    @DisplayName("Existing discount for different product → inserts new")
    void testInsertNewDiscountDiffProduct() throws Exception {
        Discount d = new Discount(store, product, LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 3), 10);
        discountRepo.save(d);

        InputStream csv = new ClassPathResource("csv/test_insert_new_discount_for_diff_product.csv").getInputStream();
        importer.importDiscounts(csv, "teststore", LocalDate.of(2025, 5, 1));

        List<Discount> discounts = discountRepo.findAll();
        assertEquals(2, discounts.size());
        Discount newDiscount = discounts.get(1);
        assertEquals(LocalDate.of(2025, 5, 1), newDiscount.getFromDate());
        assertEquals(LocalDate.of(2025, 5, 7), newDiscount.getToDate());
        assertEquals(10, newDiscount.getPercentage());
    }


    @Test
    @DisplayName("Discount for unknown product -> ignored")
    void testUnknownProductIgnored() throws Exception {
        InputStream csv = new ClassPathResource("csv/test_unknown_product.csv").getInputStream();
        importer.importDiscounts(csv, "teststore", LocalDate.of(2025, 5, 1));

        assertEquals(0, discountRepo.findAll().size());
    }

    @Test
    @DisplayName("Existing discount for same product but different period and importedDate → inserts new")
    void testInsertNewDiscountSameProduct() throws Exception {
        Discount d = new Discount(store, product, LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 3), 10);
        discountRepo.save(d);

        InputStream csv = new ClassPathResource("csv/test_insert_new_discount_for_same_product.csv").getInputStream();
        importer.importDiscounts(csv, "teststore", LocalDate.of(2025, 5, 8));

        List<Discount> discounts = discountRepo.findAll();
        assertEquals(2, discounts.size());
        Discount newDiscount = discounts.get(1);
        assertEquals(LocalDate.of(2025, 5, 7), newDiscount.getFromDate());
        assertEquals(LocalDate.of(2025, 5, 12), newDiscount.getToDate());
        assertEquals(10, newDiscount.getPercentage());

    }

    @Test
    @DisplayName("Duplicate discount for same product/store/date -> ignored")
    void testDuplicateDiscountIgnored() throws Exception {
        Discount d = new Discount(store, product, LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 3), 10);
        discountRepo.save(d);

        InputStream csv = new ClassPathResource("csv/test_insert.csv").getInputStream();
        importer.importDiscounts(csv, "teststore", LocalDate.of(2025, 5, 1));

        assertEquals(1, discountRepo.findAll().size());
        Discount d2 = discountRepo.findAll().get(0);
        assertEquals(d.getId(), d2.getId());
        assertEquals(d.getFromDate(), d2.getFromDate());
        assertEquals(d.getToDate(), d2.getToDate());
        assertEquals(d.getPercentage(), d2.getPercentage());
    }

    @Test
    @DisplayName("New discount, same as old one, but different import date → delete the old one")
        // OLD: 2025-05-01 - 2025-05-10
        // NEW: 2025-05-01 - 2025-05-10
    void testNewDiscountSameAsOld() throws Exception {
        discountRepo.save(new Discount(store, product, LocalDate.of(2025, 4, 25),
                LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 10), 5));

        InputStream csv = new ClassPathResource("csv/test_new_discount_same_as_old.csv").getInputStream();
        importer.importDiscounts(csv, "teststore", LocalDate.of(2025, 5, 1));

        List<Discount> all = discountRepo.findAll();
        assertEquals(1, all.size());
        Discount newDiscount = all.get(0);
        assertEquals(LocalDate.of(2025, 5, 1), newDiscount.getFromDate());
        assertEquals(LocalDate.of(2025, 5, 10), newDiscount.getToDate());
        assertEquals(10, newDiscount.getPercentage());
    }


    @Test
    @DisplayName("Old discount includes the new one → split")
        // OLD: 2025-05-01 - 2025-05-10
        // NEW: 2025-05-04 - 2025-05-06
    void testSplitDiscount() throws Exception {
        discountRepo.save(new Discount(store, product, LocalDate.of(2025, 4, 25),
                LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 10), 5));

        InputStream csv = new ClassPathResource("csv/test_inside_split.csv").getInputStream();
        importer.importDiscounts(csv, "teststore", LocalDate.of(2025, 5, 2));

        List<Discount> all = discountRepo.findAll();
        assertEquals(3, all.size());
        Discount left = all.get(0);
        assertEquals(LocalDate.of(2025, 5, 1), left.getFromDate());
        assertEquals(LocalDate.of(2025, 5, 3), left.getToDate());
        assertEquals(5, left.getPercentage());
        Discount right = all.get(1);
        assertEquals(LocalDate.of(2025, 5, 7), right.getFromDate());
        assertEquals(LocalDate.of(2025, 5, 10), right.getToDate());
        assertEquals(5, right.getPercentage());
        Discount newDiscount = all.get(2);
        assertEquals(LocalDate.of(2025, 5, 4), newDiscount.getFromDate());
        assertEquals(LocalDate.of(2025, 5, 6), newDiscount.getToDate());
        assertEquals(15, newDiscount.getPercentage());
    }

    @Test
    @DisplayName("Old discount is included in the new one → deleted")
        // OLD: 2025-05-02 - 2025-05-03
        // NEW: 2025-05-01 - 2025-05-10
    void testIncludedDiscountDeleted() throws Exception {
        discountRepo.save(new Discount(store, product, LocalDate.of(2025, 4, 30),
                LocalDate.of(2025, 5, 2), LocalDate.of(2025, 5, 3), 5));

        InputStream csv = new ClassPathResource("csv/test_included.csv").getInputStream();
        importer.importDiscounts(csv, "teststore", LocalDate.of(2025, 5, 1));

        List<Discount> all = discountRepo.findAll();
        assertEquals(1, all.size());
        Discount newDiscount = all.get(0);
        assertEquals(LocalDate.of(2025, 5, 1), newDiscount.getFromDate());
        assertEquals(LocalDate.of(2025, 5, 10), newDiscount.getToDate());
        assertEquals(10, newDiscount.getPercentage());
    }

    @Test
    @DisplayName("Partial overlap at beginning → truncate left")
        // OLD: 2025-05-01 - 2025-05-05
        // NEW: 2025-05-04 - 2025-05-07
    void testPartialOverlapTruncateLeft() throws Exception {
        discountRepo.save(new Discount(store, product, LocalDate.of(2025, 4, 25),
                LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 5), 5));

        InputStream csv = new ClassPathResource("csv/test_overlap_start.csv").getInputStream();
        importer.importDiscounts(csv, "teststore", LocalDate.of(2025, 5, 1));

        List<Discount> all = discountRepo.findAll();
        assertEquals(2, all.size());
        Discount old = all.get(0);
        assertEquals(LocalDate.of(2025, 5, 1), old.getFromDate());
        assertEquals(LocalDate.of(2025, 5, 3), old.getToDate());
        assertEquals(5, old.getPercentage());
        Discount newDiscount = all.get(1);
        assertEquals(LocalDate.of(2025, 5, 4), newDiscount.getFromDate());
        assertEquals(LocalDate.of(2025, 5, 7), newDiscount.getToDate());
        assertEquals(12, newDiscount.getPercentage());
    }

    @Test
    @DisplayName("Partial overlap at ending → truncate right")
        // OLD: 2025-05-04 - 2025-05-07
        // NEW: 2025-05-01 - 2025-05-05
    void testPartialOverlapTruncateRight() throws Exception {
        discountRepo.save(new Discount(store, product, LocalDate.of(2025, 4, 25),
                LocalDate.of(2025, 5, 4), LocalDate.of(2025, 5, 7), 5));

        InputStream csv = new ClassPathResource("csv/test_overlap_end.csv").getInputStream();
        importer.importDiscounts(csv, "teststore", LocalDate.of(2025, 5, 1));

        List<Discount> all = discountRepo.findAll();
        assertEquals(2, all.size());
        Discount old = all.get(0);
        assertEquals(LocalDate.of(2025, 5, 6), old.getFromDate());
        assertEquals(LocalDate.of(2025, 5, 7), old.getToDate());
        assertEquals(5, old.getPercentage());
        Discount newDiscount = all.get(1);
        assertEquals(LocalDate.of(2025, 5, 1), newDiscount.getFromDate());
        assertEquals(LocalDate.of(2025, 5, 5), newDiscount.getToDate());
        assertEquals(12, newDiscount.getPercentage());
    }

}