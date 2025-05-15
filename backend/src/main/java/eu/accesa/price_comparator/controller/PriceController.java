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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
    @GetMapping("/history")
    public ResponseEntity<List<PriceHistoryResponse>> getPriceHistory(
            @RequestParam String productName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String store,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String category
    ) {
        PriceHistoryRequest request = new PriceHistoryRequest(
                productName,
                startDate,
                endDate,
                Optional.ofNullable(store),
                Optional.ofNullable(brand),
                Optional.ofNullable(category)
        );
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
