package eu.accesa.price_comparator.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record SubstituteProductRequest(
        @Schema(description = "The name of the product to be substituted", example = "lapte zuzu")
        @NotBlank(message = "Product name cannot be blank")
        String productName,
        @Schema(description = "The date for which the substitute product is requested", example = "2025-05-13")
        @NotBlank(message = "Date cannot be blank")
        LocalDate date
) {
}
