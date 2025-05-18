package eu.accesa.price_comparator.controller;

import eu.accesa.price_comparator.dto.price.PriceAlertRequest;
import eu.accesa.price_comparator.dto.price.PriceHistoryRequest;
import eu.accesa.price_comparator.dto.price.PriceHistoryResponse;
import eu.accesa.price_comparator.service.PriceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/prices")
public class PriceController {
    private final PriceService priceService;

    public PriceController(PriceService priceService) {
        this.priceService = priceService;
    }

    @Operation(
            summary = "Get price history for a product",
            description = "Returns the discounted price history of a product (identified by name), filtered optionally by store, brand, and category, for a given date range."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved price history",
                    content = @Content(schema = @Schema(implementation = PriceHistoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/history")
    public ResponseEntity<List<PriceHistoryResponse>> getPriceHistory(@Valid @RequestBody PriceHistoryRequest request) {
        return ResponseEntity.ok(priceService.getPriceHistory(request));
    }

    @Operation(
            summary = "Create a price alert",
            description = "Creates a price alert for a product, notifying the user when the price drops below a specified target."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Alert created successfully",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/alerts")
    public ResponseEntity<String> createAlert(@Valid @RequestBody PriceAlertRequest alert) {
        return ResponseEntity.ok(priceService.createAlert(alert));
    }

}
