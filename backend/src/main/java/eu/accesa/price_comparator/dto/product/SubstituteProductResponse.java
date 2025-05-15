package eu.accesa.price_comparator.dto.product;

public record SubstituteProductResponse(
        String store,
        String productId,
        String name,
        String brand,
        String category,
        double pricePerUnit,
        double totalPrice,
        double quantity,
        String unit
) {
}