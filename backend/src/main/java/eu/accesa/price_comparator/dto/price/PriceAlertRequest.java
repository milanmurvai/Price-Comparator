package eu.accesa.price_comparator.dto.price;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record PriceAlertRequest(
        @Schema(description = "Product name to retrieve history for", example = "lapte zuzu")
        @NotBlank(message = "Product name must not be blank")
        String productName,

        @Schema(description = "The email address where the notification needs to be sent", example = "example@gmail.com")
        @Email(message = "Invalid email format")
        String userEmail,

        @Schema(description = "The target price for the alert", example = "8.50")
        @Positive(message = "Target price must be greater than 0")
        double targetPrice
) {
}
