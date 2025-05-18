package eu.accesa.price_comparator.controller;

import eu.accesa.price_comparator.dto.basket.BasketRequest;
import eu.accesa.price_comparator.dto.basket.BasketResponse;
import eu.accesa.price_comparator.service.BasketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
@RequestMapping("/basket")
public class BasketController {

    private final BasketService basketService;

    public BasketController(BasketService basketService) {
        this.basketService = basketService;
    }

    @Operation(
            summary = "Optimize shopping basket",
            description = "Splits the user basket into store-specific shopping lists to minimize total cost"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Basket optimization completed successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = BasketResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid basket data",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/optimize")
    public ResponseEntity<List<BasketResponse>> optimizeBasket(@Valid @RequestBody BasketRequest request) {
        List<BasketResponse> optimizedBasket = basketService.optimizeBasket(request);
        return ResponseEntity.ok(optimizedBasket);
    }
}
