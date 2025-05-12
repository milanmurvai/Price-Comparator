package eu.accesa.price_comparator.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Price {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Store store;

    @ManyToOne
    private Product product;

    private double quantity;
    private double price;
    private String currency;
    private LocalDate date;

    public Price(Store store, Product product, double quantity, double price, String currency, LocalDate date) {
        this.store = store;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
        this.currency = currency;
        this.date = date;
    }
}

