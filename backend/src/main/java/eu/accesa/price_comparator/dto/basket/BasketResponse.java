package eu.accesa.price_comparator.dto.basket;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record BasketResponse(
        @Schema(description = "Store name where these items should be bought")
        String store,

        @Schema(description = "List of items to buy from this store")
        List<BasketItem> items,

        @Schema(description = "Total cost of the items in this store")
        double total
) {
}

