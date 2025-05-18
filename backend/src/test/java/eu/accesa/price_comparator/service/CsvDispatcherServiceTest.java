package eu.accesa.price_comparator.service;

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
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@DataJpaTest
class CsvDispatcherServiceTest {
    @Autowired
    private ProductRepository productRepo;
    @Autowired
    private StoreRepository storeRepo;
    @Autowired
    private DiscountRepository discountRepo;
    @Autowired
    private PriceRepository priceRepo;
    private CsvDispatcherService dispatcherService;

    @BeforeEach
    void setup() {
        dispatcherService = new CsvDispatcherService(
                new PriceCsvImporter(storeRepo, productRepo, priceRepo),
                new DiscountCsvImporter(storeRepo, productRepo, discountRepo)
        );

        Store store = new Store("Teststore");
        storeRepo.save(store);

        productRepo.save(new Product("Teststore_P001", "lapte", "lactate", "Zuzu", "l"));
    }

    @Test
    void testImportDiscountFile() throws Exception {
        ClassPathResource resource = new ClassPathResource("csv/discounts/test_insert.csv");
        InputStream csv = resource.getInputStream();

        MultipartFile file = new MockMultipartFile(
                "file",
                "teststore_discounts_2025-05-01.csv",
                "text/csv",
                csv
        );
        dispatcherService.importCsv(file);

        assertEquals(1, discountRepo.count());
        assertEquals(0, priceRepo.count());
    }

    @Test
    void testImportPriceFile() throws Exception {
        ClassPathResource resource = new ClassPathResource("csv/prices/teststore_2025-05-01.csv");
        InputStream csv = resource.getInputStream();

        MultipartFile file = new MockMultipartFile(
                "file",
                "teststore_2025-05-01.csv",
                "text/csv",
                csv
        );
        dispatcherService.importCsv(file);

        assertEquals(1, priceRepo.count());
        assertEquals(0, discountRepo.count());
    }

    @Test
    void testInvalidFileTypeThrowsException() {
        MultipartFile file = new MockMultipartFile(
                "file",
                "invalid_file.txt",
                "text/plain",
                new byte[0]
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                dispatcherService.importCsv(file));

        assertEquals("Invalid file type. Expected a .csv file.", ex.getMessage());
    }

    @Test
    void testInvalidFilenameFormatThrowsException() {
        MultipartFile file = new MockMultipartFile(
                "file",
                "badname.csv",
                "text/csv",
                new byte[0]
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                dispatcherService.importCsv(file));

        assertEquals("Invalid filename format. Expected: store_YYYY-MM-DD.csv or store_discounts_YYYY-MM-DD.csv", ex.getMessage());
    }

    @Test
    void testMissingDateInDiscountFilenameThrowsException() {
        MultipartFile file = new MockMultipartFile(
                "file",
                "teststore_discounts.csv",
                "text/csv",
                new byte[0]
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                dispatcherService.importCsv(file));

        assertEquals("Missing date in discount file name.", ex.getMessage());
    }

}
