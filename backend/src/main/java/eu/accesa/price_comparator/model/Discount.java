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
public class Discount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Store store;

    @ManyToOne
    private Product product;

    private LocalDate importedDate;
    private LocalDate fromDate;
    private LocalDate toDate;
    private int percentage;

    public Discount(Store store, Product product, LocalDate importedDate, LocalDate fromDate, LocalDate toDate, int percentage) {
        this.store = store;
        this.product = product;
        this.importedDate = importedDate;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.percentage = percentage;
    }
}

