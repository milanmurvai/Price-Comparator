package eu.accesa.price_comparator.repository;

import eu.accesa.price_comparator.model.Discount;
import eu.accesa.price_comparator.model.Product;
import eu.accesa.price_comparator.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {
    boolean existsByStoreAndProductAndImportedDate(Store store, Product product, LocalDate importedDate);

    Optional<Discount> findByStoreAndProductAndFromDateBeforeAndToDateAfter(
            Store store, Product product, LocalDate newFromDate, LocalDate newToDate);

    Optional<Discount> findFirstByStoreAndProductAndFromDateBeforeAndToDateAfter(
            Store store, Product product, LocalDate fromDate, LocalDate fromDateAgain);

    List<Discount> findAllByStoreAndProductAndFromDateAfterAndToDateBefore(
            Store store, Product product, LocalDate fromExclusive, LocalDate toExclusive);
}
