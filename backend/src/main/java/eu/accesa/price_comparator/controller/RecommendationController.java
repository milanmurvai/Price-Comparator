package eu.accesa.price_comparator.controller;

import eu.accesa.price_comparator.dto.product.SubstituteProductRequest;
import eu.accesa.price_comparator.dto.product.SubstituteProductResponse;
import eu.accesa.price_comparator.service.RecommendationService;
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
@RequestMapping("/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @Operation(
            summary = "Get best substitutes based on price per unit",
            description = "Finds the best-value alternatives for a product based on price per unit (e.g., RON/kg)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of substitute products",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = SubstituteProductResponse.class))))
    })
    @PostMapping()
    public ResponseEntity<List<SubstituteProductResponse>> getSubstitutes(
            @Valid @RequestBody SubstituteProductRequest request) {

        return ResponseEntity.ok(recommendationService.getRecommendedSubstitutes(request));
    }
}
