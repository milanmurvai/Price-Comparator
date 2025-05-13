package eu.accesa.price_comparator.repository;

import eu.accesa.price_comparator.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    List<Product> findByIdEndingWith(String suffix);

    List<Product> findAllByName(String name);

}
