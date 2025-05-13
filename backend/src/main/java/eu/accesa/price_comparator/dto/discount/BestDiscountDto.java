package eu.accesa.price_comparator.dto.discount;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record BestDiscountDto(
        @Schema(description = "Product ID", example = "P001")
        String productId,

        @Schema(description = "Store name offering the discount")
        String store,

        @Schema(description = "Name of the product")
        String productName,

        @Schema(description = "Product category")
        String category,

        @Schema(description = "Brand of the product")
        String brand,

        @Schema(description = "Percentage of discount")
        int discount,

        @Schema(description = "Start date of the discount validity period", example = "2025-05-01")
        LocalDate validFrom,

        @Schema(description = "End date of the discount validity period", example = "2025-05-07")
        LocalDate validTo
) {
}


