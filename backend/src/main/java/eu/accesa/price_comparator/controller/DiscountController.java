package eu.accesa.price_comparator.controller;

import eu.accesa.price_comparator.dto.discount.BestDiscountDto;
import eu.accesa.price_comparator.service.DiscountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/discounts")
public class DiscountController {

    private final DiscountService discountService;

    public DiscountController(DiscountService discountService) {
        this.discountService = discountService;
    }

    @Operation(
            summary = "Get best current discounts",
            description = "Returns a list of products with the highest active discounts across all stores for a given date (default is today)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of best discounts returned successfully",
                    content = @Content(schema = @Schema(implementation = BestDiscountDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid date format",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = String.class))),
    })
    @GetMapping("/best")
    public ResponseEntity<List<BestDiscountDto>> getBestDiscounts(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Schema(description = "Optional date to check discounts for. If not provided, defaults to today.", example = "2025-05-13")
            LocalDate date
    ) {
        List<BestDiscountDto> result = discountService.getBestDiscounts(date != null ? date : LocalDate.now());
        return ResponseEntity.ok(result);
    }
}
