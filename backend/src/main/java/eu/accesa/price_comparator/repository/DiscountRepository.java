package eu.accesa.price_comparator.repository;

import eu.accesa.price_comparator.model.Discount;
import eu.accesa.price_comparator.model.Product;
import eu.accesa.price_comparator.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT d FROM Discount d WHERE d.product = :product AND d.store = :store " +
            "AND :date BETWEEN d.fromDate AND d.toDate")
    Optional<Discount> findActiveDiscount(Product product, Store store, LocalDate date);

    List<Discount> findAllByImportedDate(LocalDate today);

    @Query("""
            SELECT d FROM Discount d
            WHERE :date BETWEEN d.fromDate AND d.toDate
            AND d.percentage = (
                SELECT MAX(d2.percentage) FROM Discount d2
                WHERE d2.product.id = d.product.id
                AND :date BETWEEN d2.fromDate AND d2.toDate
            )
            """)
    List<Discount> findBestActiveDiscountPerProduct(@Param("date") LocalDate date);

}
