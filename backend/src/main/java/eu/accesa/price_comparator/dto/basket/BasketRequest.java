package eu.accesa.price_comparator.dto.basket;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDate;
import java.util.List;

public record BasketRequest(
        @Schema(description = "List of product IDs included in the shopping basket", example = "[\"P001\", \"P020\"]")
        @NotEmpty(message = "Product IDs cannot be empty")
        List<String> productIds,

        @Schema(description = "Target date for basket optimization", example = "2025-05-13")
        @NotEmpty(message = "Date cannot be empty")
        LocalDate date
) {
}
