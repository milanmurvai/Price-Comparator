package eu.accesa.price_comparator.service;

import eu.accesa.price_comparator.dto.basket.BasketItem;
import eu.accesa.price_comparator.dto.basket.BasketRequest;
import eu.accesa.price_comparator.dto.basket.BasketResponse;
import eu.accesa.price_comparator.model.Discount;
import eu.accesa.price_comparator.model.Price;
import eu.accesa.price_comparator.model.Product;
import eu.accesa.price_comparator.model.Store;
import eu.accesa.price_comparator.repository.DiscountRepository;
import eu.accesa.price_comparator.repository.PriceRepository;
import eu.accesa.price_comparator.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BasketService {

    private final ProductRepository productRepo;
    private final PriceRepository priceRepo;
    private final DiscountRepository discountRepo;

    public BasketService(ProductRepository productRepo, PriceRepository priceRepo,
                         DiscountRepository discountRepo) {
        this.productRepo = productRepo;
        this.priceRepo = priceRepo;
        this.discountRepo = discountRepo;
    }

    public List<BasketResponse> optimizeBasket(BasketRequest request) {
        Map<String, List<BasketItem>> storeToItems = new HashMap<>();
        Map<String, Double> storeToTotal = new HashMap<>();

        for (String productId : request.productIds()) {
            List<Product> candidates = productRepo.findByIdEndingWith(productId);

            double minPrice = Double.MAX_VALUE;
            Product bestProduct = null;
            Store bestStore = null;
            double finalPrice = 0;

            for (Product candidate : candidates) {
                Optional<Price> priceOpt = priceRepo.findFirstByProductAndDateLessThanEqualOrderByDateDesc(candidate, request.date());
                if (priceOpt.isEmpty()) continue;

                Price price = priceOpt.get();

                Optional<Discount> discountOpt = discountRepo.findActiveDiscount(candidate, price.getStore(), request.date());
                double discount = discountOpt.map(Discount::getPercentage).orElse(0);

                double discountedPrice = price.getPrice() - (discount / 100.0) * price.getPrice();
                discountedPrice = Math.round(discountedPrice * 100.0) / 100.0;
                if (discountedPrice < minPrice) {
                    minPrice = discountedPrice;
                    finalPrice = discountedPrice;
                    bestProduct = candidate;
                    bestStore = price.getStore();
                }
            }

            if (bestStore != null && bestProduct != null) {
                storeToItems
                        .computeIfAbsent(bestStore.getName(), s -> new ArrayList<>())
                        .add(new BasketItem(productId, finalPrice));

                storeToTotal.merge(bestStore.getName(), finalPrice, Double::sum);
            }
        }

        return storeToItems.entrySet().stream()
                .map(entry -> {
                    double totalRounded = Math.round(storeToTotal.get(entry.getKey()) * 100.0) / 100.0;
                    return new BasketResponse(
                            entry.getKey(),
                            entry.getValue(),
                            totalRounded
                    );
                })
                .collect(Collectors.toList());
    }
}
