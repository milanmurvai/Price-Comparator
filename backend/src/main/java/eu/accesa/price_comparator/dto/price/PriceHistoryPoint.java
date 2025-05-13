package eu.accesa.price_comparator.dto.price;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record PriceHistoryPoint(

        @Schema(description = "Date for the recorded price", example = "2025-03-10")
        LocalDate date,

        @Schema(description = "Final price after applying discounts", example = "8.49")
        double price
) {
}

