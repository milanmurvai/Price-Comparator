package eu.accesa.price_comparator.service;

import eu.accesa.price_comparator.dto.discount.BestDiscountDto;
import eu.accesa.price_comparator.model.Discount;
import eu.accesa.price_comparator.repository.DiscountRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DiscountService {

    private final DiscountRepository discountRepo;

    public DiscountService(DiscountRepository discountRepo) {
        this.discountRepo = discountRepo;
    }

    public List<BestDiscountDto> getBestDiscounts(LocalDate date) {
        List<Discount> result = discountRepo.findBestActiveDiscountPerProduct(date);

        Map<String, Discount> bestPerProduct = new HashMap<>();

        for (Discount d : result) {
            String fullId = d.getProduct().getId();
            String shortId = fullId.substring(fullId.indexOf("_") + 1);

            Discount existing = bestPerProduct.get(shortId);
            if (existing == null || d.getPercentage() > existing.getPercentage()) {
                bestPerProduct.put(shortId, d);
            }
        }

        return bestPerProduct.entrySet().stream()
                .map(entry -> {
                    Discount d = entry.getValue();
                    String shortId = entry.getKey();
                    return new BestDiscountDto(
                            shortId,
                            d.getStore().getName(),
                            d.getProduct().getName(),
                            d.getProduct().getCategory(),
                            d.getProduct().getBrand(),
                            d.getPercentage(),
                            d.getFromDate(),
                            d.getToDate()
                    );
                })
                .sorted(Comparator.comparing(BestDiscountDto::productId))
                .toList();
    }


    public List<BestDiscountDto> getTodayDiscounts() {
        LocalDate today = LocalDate.now();
        return discountRepo.findAllByImportedDate(today).stream()
                .map(d -> new BestDiscountDto(
                        d.getProduct().getId().substring(d.getProduct().getId().indexOf("_") + 1),
                        d.getStore().getName(),
                        d.getProduct().getName(),
                        d.getProduct().getCategory(),
                        d.getProduct().getBrand(),
                        d.getPercentage(),
                        d.getFromDate(),
                        d.getToDate()
                ))
                .toList();
    }
}
