package eu.accesa.price_comparator.dto.price;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.Optional;

public record PriceHistoryRequest(

        @Schema(description = "Product name to retrieve history for", example = "lapte zuzu")
        String productName,

        @Schema(description = "Start date of the time range", example = "2025-01-01")
        LocalDate startDate,

        @Schema(description = "End date of the time range", example = "2025-05-13")
        LocalDate endDate,

        @Schema(description = "Optional filter: store name", example = "Lidl")
        Optional<String> store,

        @Schema(description = "Optional filter: product brand", example = "Zuzu")
        Optional<String> brand,

        @Schema(description = "Optional filter: product category", example = "lactate")
        Optional<String> category
) {
}
