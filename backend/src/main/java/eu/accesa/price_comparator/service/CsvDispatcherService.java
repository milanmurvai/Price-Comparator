package eu.accesa.price_comparator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;

@Service
public class CsvDispatcherService {

    private final PriceCsvImporter priceCsvImporter;
    private final DiscountCsvImporter discountCsvImporter;

    @Autowired
    public CsvDispatcherService(PriceCsvImporter priceCsvImporter, DiscountCsvImporter discountCsvImporter) {
        this.priceCsvImporter = priceCsvImporter;
        this.discountCsvImporter = discountCsvImporter;
    }

    public void importCsv(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("Invalid file type. Expected a .csv file.");
        }

        String[] parts = filename.replace(".csv", "").split("_");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid filename format. Expected: store_YYYY-MM-DD.csv or store_discounts_YYYY-MM-DD.csv");
        }
        String store = capitalize(parts[0]);

        if (parts[1].equalsIgnoreCase("discounts")) {
            // store_discounts_YYYY-MM-DD.csv
            if (parts.length < 3) throw new IllegalArgumentException("Missing date in discount file name.");
            String dateStr = parts[2];
            discountCsvImporter.importDiscounts(file.getInputStream(), store, LocalDate.parse(dateStr));
        } else {
            // store_YYYY-MM-DD.csv
            String dateStr = parts[1];
            priceCsvImporter.importPrices(file.getInputStream(), store, LocalDate.parse(dateStr));
        }

    }

    private String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}

