package eu.accesa.price_comparator.service;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import eu.accesa.price_comparator.model.Discount;
import eu.accesa.price_comparator.model.Product;
import eu.accesa.price_comparator.model.Store;
import eu.accesa.price_comparator.repository.DiscountRepository;
import eu.accesa.price_comparator.repository.ProductRepository;
import eu.accesa.price_comparator.repository.StoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class DiscountCsvImporter {

    private final StoreRepository storeRepo;
    private final ProductRepository productRepo;
    private final DiscountRepository discountRepo;
    private static final Logger log = LoggerFactory.getLogger(DiscountCsvImporter.class);


    public DiscountCsvImporter(StoreRepository storeRepo, ProductRepository productRepo, DiscountRepository discountRepo) {
        this.storeRepo = storeRepo;
        this.productRepo = productRepo;
        this.discountRepo = discountRepo;
    }

    public void importDiscounts(InputStream inputStream, String storeName, LocalDate date) throws IOException {
        Store store = storeRepo.findByName(storeName)
                .orElseThrow(() -> new IllegalArgumentException("Store not found: " + storeName));

        CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
        try (Reader reader = new InputStreamReader(inputStream);
             CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(parser).withSkipLines(1).build()) {

            String[] row;
            while ((row = csvReader.readNext()) != null) {
                String productId = storeName + "_" + row[0].trim();
                LocalDate fromDate = LocalDate.parse(row[6].trim());
                LocalDate toDate = LocalDate.parse(row[7].trim());
                int percentage = Integer.parseInt(row[8].trim());

                // daca nu gaseste produsul, ignoram discountul
                Optional<Product> productOpt = productRepo.findById(productId);
                if (productOpt.isEmpty()) {
                    log.info("Ignoring discount for non-existing product " + productId);
                    continue;
                }
                Product product = productOpt.get();

                // daca discountul a fost importat deja pentru aceeasi data, ignoram
                if (discountRepo.existsByStoreAndProductAndImportedDate(store, product, date)) {
                    log.info("Ignoring duplicate discount for " + productId + " on " + date);
                    continue;
                }

                // daca discountul vechi contine complet discountul nou
                discountRepo.findByStoreAndProductAndFromDateBeforeAndToDateAfter(store, product, fromDate, toDate)
                        .ifPresent(old -> {
                            log.info("Splitting old discount: " + old.getFromDate() + " to " + old.getToDate() + " due to new range " + fromDate + " to " + toDate);
                            discountRepo.delete(old);
                            discountRepo.save(new Discount(store, product, old.getImportedDate(), old.getFromDate(), fromDate.minusDays(1), old.getPercentage()));
                            discountRepo.save(new Discount(store, product, old.getImportedDate(), toDate.plusDays(1), old.getToDate(), old.getPercentage()));
                        });

                // daca discountul vechi se suprapune partial la inceput
                discountRepo.findFirstByStoreAndProductAndFromDateBeforeAndToDateAfter(store, product, fromDate, fromDate)
                        .ifPresent(overlap -> {
                            log.info("Truncating end of existing discount " + overlap.getFromDate() + " to " + overlap.getToDate() + " at " + fromDate);
                            overlap.setToDate(fromDate.minusDays(1));
                            discountRepo.save(overlap);
                        });

                // daca discountul vechi se suprapune partial la final
                discountRepo.findFirstByStoreAndProductAndFromDateBeforeAndToDateAfter(store, product, toDate, toDate)
                        .ifPresent(overlap -> {
                            log.info("Truncating start of existing discount " + overlap.getFromDate() + " to " + overlap.getToDate() + " at " + toDate);
                            overlap.setFromDate(toDate.plusDays(1));
                            discountRepo.save(overlap);
                        });

                // daca discountul vechi este complet inclus in cel nou
                List<Discount> inside = discountRepo
                        .findAllByStoreAndProductAndFromDateAfterAndToDateBefore(store, product, fromDate.minusDays(1), toDate.plusDays(1));
                if (!inside.isEmpty()) {
                    log.info("Removing " + inside.size() + " fully contained old discounts for " + productId);
                    discountRepo.deleteAll(inside);
                }

                discountRepo.save(new Discount(store, product, date, fromDate, toDate, percentage));
            }
        } catch (CsvValidationException e) {
            throw new IllegalArgumentException("Invalid CSV format: " + e.getMessage());
        }
    }
}
