package eu.accesa.price_comparator.service;

import eu.accesa.price_comparator.dto.discount.BestDiscountDto;
import eu.accesa.price_comparator.repository.DiscountRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DiscountService {

    private final DiscountRepository discountRepo;

    public DiscountService(DiscountRepository discountRepo) {
        this.discountRepo = discountRepo;
    }

    public List<BestDiscountDto> getBestDiscounts(LocalDate date) {
        return discountRepo.findActiveDiscountsOrderedByPercentageDesc(date).stream()
                .map(d -> new BestDiscountDto(
                        d.getProduct().getId().substring(d.getProduct().getId().indexOf("_") + 1),
                        d.getStore().getName(),
                        d.getProduct().getName(),
                        d.getProduct().getCategory(),
                        d.getProduct().getBrand(),
                        d.getPercentage(),
                        d.getFromDate(),
                        d.getToDate()
                )).collect(Collectors.toList());
    }
}
