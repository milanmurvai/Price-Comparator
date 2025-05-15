package eu.accesa.price_comparator.service;

import eu.accesa.price_comparator.dto.product.SubstituteProductRequest;
import eu.accesa.price_comparator.dto.product.SubstituteProductResponse;
import eu.accesa.price_comparator.model.Discount;
import eu.accesa.price_comparator.model.Price;
import eu.accesa.price_comparator.model.Product;
import eu.accesa.price_comparator.repository.DiscountRepository;
import eu.accesa.price_comparator.repository.PriceRepository;
import eu.accesa.price_comparator.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final ProductRepository productRepo;
    private final PriceRepository priceRepo;
    private final DiscountRepository discountRepo;

    public RecommendationService(ProductRepository productRepo,
                                 PriceRepository priceRepo,
                                 DiscountRepository discountRepo) {
        this.productRepo = productRepo;
        this.priceRepo = priceRepo;
        this.discountRepo = discountRepo;
    }

    public List<SubstituteProductResponse> getRecommendedSubstitutes(SubstituteProductRequest request) {
        String productName = request.productName();
        LocalDate date = request.date();
        List<Product> variants = productRepo.findAllByName(productName);
        return variants.stream()
                .map(p -> {
                    Optional<Price> priceOpt = priceRepo.findFirstByProductAndDateLessThanEqualOrderByDateDesc(p, date);
                    if (priceOpt.isEmpty()) return null;

                    Price price = priceOpt.get();
                    double priceValue = price.getPrice();

                    Optional<Discount> discountOpt = discountRepo.findActiveDiscount(p, price.getStore(), date);
                    double discount = discountOpt.map(Discount::getPercentage).orElse(0);

                    double finalPrice = priceValue - (discount / 100.0) * priceValue;
                    double unitPrice = finalPrice / price.getQuantity();

                    return new SubstituteProductResponse(
                            p.getId().split("_")[0],
                            p.getId().substring(p.getId().indexOf("_") + 1),
                            p.getName(),
                            p.getBrand(),
                            p.getCategory(),
                            unitPrice,
                            finalPrice,
                            price.getQuantity(),
                            p.getUnit()
                    );
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(SubstituteProductResponse::pricePerUnit))
                .collect(Collectors.toList());
    }
}
