package eu.accesa.price_comparator.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PriceAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userEmail;

    private String productName;

    private double targetPrice;

    private boolean triggered = false;

    private LocalDate createdAt;

    public PriceAlert(String userEmail, String productName, double targetPrice) {
        this.userEmail = userEmail;
        this.productName = productName;
        this.targetPrice = targetPrice;
        this.createdAt = LocalDate.now();
    }
}
