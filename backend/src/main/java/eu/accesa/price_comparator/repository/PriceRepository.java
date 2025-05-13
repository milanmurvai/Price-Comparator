package eu.accesa.price_comparator.repository;

import eu.accesa.price_comparator.model.Price;
import eu.accesa.price_comparator.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceRepository extends JpaRepository<Price, Long> {
    Optional<Price> findFirstByProductAndDateLessThanEqualOrderByDateDesc(Product product, LocalDate date);

    List<Price> findAllByProductAndDateBetweenOrderByDate(Product product, LocalDate startDate, LocalDate endDate);
}
