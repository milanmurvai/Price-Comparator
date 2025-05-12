package eu.accesa.price_comparator.service;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import eu.accesa.price_comparator.model.Price;
import eu.accesa.price_comparator.model.Product;
import eu.accesa.price_comparator.model.Store;
import eu.accesa.price_comparator.repository.PriceRepository;
import eu.accesa.price_comparator.repository.ProductRepository;
import eu.accesa.price_comparator.repository.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDate;

@Service
public class PriceCsvImporter {

    private final StoreRepository storeRepo;
    private final ProductRepository productRepo;
    private final PriceRepository priceRepo;

    @Autowired
    public PriceCsvImporter(StoreRepository storeRepo, ProductRepository productRepo, PriceRepository priceRepo) {
        this.storeRepo = storeRepo;
        this.productRepo = productRepo;
        this.priceRepo = priceRepo;
    }

    public void importPrices(InputStream inputStream, String storeName, LocalDate date) throws IOException {
        Store store = storeRepo.findByName(storeName)
                .orElseGet(() -> storeRepo.save(new Store(null, storeName)));

        CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
        try (Reader reader = new InputStreamReader(inputStream);
             CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(parser).withSkipLines(1).build()) {

            String[] row;
            while ((row = csvReader.readNext()) != null) {
                String productId = storeName + "_" + row[0].trim();
                String name = row[1].trim();
                String category = row[2].trim();
                String brand = row[3].trim();
                double quantity = Double.parseDouble(row[4].trim());
                String unit = row[5].trim();
                double price = Double.parseDouble(row[6].trim());
                String currency = row[7].trim();

                Product product = productRepo.findById(productId).orElseGet(() -> {
                    Product p = new Product(productId, name, category, brand, unit);
                    return productRepo.save(p);
                });

                priceRepo.save(new Price(store, product, quantity, price, currency, date));
            }
        } catch (CsvValidationException e) {
            throw new IllegalArgumentException("Invalid CSV format: " + e.getMessage());
        }
    }
}
