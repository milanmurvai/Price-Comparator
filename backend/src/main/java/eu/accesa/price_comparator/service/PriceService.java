package eu.accesa.price_comparator.service;

import eu.accesa.price_comparator.dto.price.PriceAlertRequest;
import eu.accesa.price_comparator.dto.price.PriceHistoryPoint;
import eu.accesa.price_comparator.dto.price.PriceHistoryRequest;
import eu.accesa.price_comparator.dto.price.PriceHistoryResponse;
import eu.accesa.price_comparator.model.Discount;
import eu.accesa.price_comparator.model.Price;
import eu.accesa.price_comparator.model.PriceAlert;
import eu.accesa.price_comparator.model.Product;
import eu.accesa.price_comparator.repository.DiscountRepository;
import eu.accesa.price_comparator.repository.PriceAlertRepository;
import eu.accesa.price_comparator.repository.PriceRepository;
import eu.accesa.price_comparator.repository.ProductRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PriceService {

    private final ProductRepository productRepo;
    private final PriceRepository priceRepo;
    private final DiscountRepository discountRepo;
    private final PriceAlertRepository priceAlertRepo;
    private final EmailService emailService;

    public PriceService(ProductRepository productRepo, PriceRepository priceRepo, DiscountRepository discountRepo,
                        PriceAlertRepository priceAlertRepo, EmailService emailService) {
        this.productRepo = productRepo;
        this.priceRepo = priceRepo;
        this.discountRepo = discountRepo;
        this.priceAlertRepo = priceAlertRepo;
        this.emailService = emailService;
    }

    public List<PriceHistoryResponse> getPriceHistory(PriceHistoryRequest request) {
        List<Product> products = productRepo.findAllByName(request.productName());

        if (request.store().isPresent()) {
            products = products.stream()
                    .filter(p -> p.getId().startsWith(request.store().get() + "_"))
                    .toList();
        }

        if (request.brand().isPresent()) {
            products = products.stream()
                    .filter(p -> p.getBrand().equalsIgnoreCase(request.brand().get()))
                    .toList();
        }

        if (request.category().isPresent()) {
            products = products.stream()
                    .filter(p -> p.getCategory().equalsIgnoreCase(request.category().get()))
                    .toList();
        }

        List<PriceHistoryResponse> responses = new ArrayList<>();

        for (Product product : products) {
            List<Price> prices = priceRepo.findAllByProductAndDateBetweenOrderByDate(product,
                    request.startDate(), request.endDate());

            List<PriceHistoryPoint> points = new ArrayList<>();

            for (Price price : prices) {
                Optional<Discount> discountOpt = discountRepo.findActiveDiscount(product, price.getStore(), price.getDate());
                double discount = discountOpt.map(Discount::getPercentage).orElse(0);
                double finalPrice = Math.round((price.getPrice() - (discount / 100.0 * price.getPrice())) * 100.0) / 100.0;
                points.add(new PriceHistoryPoint(price.getDate(), finalPrice));
            }

            if (!points.isEmpty()) {
                responses.add(new PriceHistoryResponse(
                        product.getId().split("_")[0],
                        product.getBrand(),
                        product.getCategory(),
                        points
                ));
            }
        }

        return responses;
    }

    public String createAlert(PriceAlertRequest alertRequest) {

        PriceAlert alert = new PriceAlert(
                alertRequest.userEmail(),
                alertRequest.productName(),
                alertRequest.targetPrice());

        priceAlertRepo.save(alert);

        return "Alert created successfully for product: " + alertRequest.productName();
    }

    // This method is scheduled to run every day at 9 AM, in order to check for price alerts changes
    @Scheduled(cron = "0 0 9 * * *", zone = "Europe/Bucharest")
    public void checkPriceAlerts() {
        List<PriceAlert> alerts = priceAlertRepo.findAllByTriggeredFalse();

        for (PriceAlert alert : alerts) {
            List<Product> products = productRepo.findAllByName(alert.getProductName());

            for (Product product : products) {
                Optional<Price> priceOpt = priceRepo.findFirstByProductAndDateLessThanEqualOrderByDateDesc(product, LocalDate.now());
                if (priceOpt.isEmpty()) continue;

                double price = priceOpt.get().getPrice();

                Optional<Discount> discountOpt = discountRepo.findActiveDiscount(product, priceOpt.get().getStore(), LocalDate.now());
                double discount = discountOpt.map(Discount::getPercentage).orElse(0);

                double finalPrice = price - (discount / 100.0 * price);
                double finalPriceRounded = Math.round(finalPrice * 100.0) / 100.0;

                if (finalPriceRounded <= alert.getTargetPrice()) {
                    emailService.sendNotification(alert.getUserEmail(), product, finalPrice, priceOpt.get().getStore().getName());
                    alert.setTriggered(true);
                    priceAlertRepo.save(alert);
                    break;
                }
            }
        }
    }

}
