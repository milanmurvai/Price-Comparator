package eu.accesa.price_comparator.dto.discount;

import java.time.LocalDate;

public record BestDiscountDto(
        String productId,
        String store,
        String productName,
        String category,
        String brand,
        int discount,
        LocalDate validFrom,
        LocalDate validTo
) {
}

