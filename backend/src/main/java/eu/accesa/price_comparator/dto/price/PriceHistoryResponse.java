package eu.accesa.price_comparator.dto.price;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Price history of a product (filtered by store, brand or category if applicable)")
public record PriceHistoryResponse(

        @Schema(description = "Store where the prices were recorded", example = "Lidl")
        String store,

        @Schema(description = "Brand of the product", example = "Lidl")
        String brand,

        @Schema(description = "Product category", example = "ouă")
        String category,

        @Schema(description = "List of historical price points for the product")
        List<PriceHistoryPoint> points
) {
}
