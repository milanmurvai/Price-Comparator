package eu.accesa.price_comparator.dto.basket;

import io.swagger.v3.oas.annotations.media.Schema;

public record BasketItem(
        @Schema(description = "ID of the product", example = "P001")
        String productId,

        @Schema(description = "Final price after applying any discounts")
        double finalPrice
) {
}