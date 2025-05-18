package eu.accesa.price_comparator.repository;

import eu.accesa.price_comparator.model.Product;
import eu.accesa.price_comparator.model.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository repo;

    @Autowired
    private StoreRepository storeRepo;

    @BeforeEach
    void setup() {
        storeRepo.save(new Store("Lidl"));
        repo.save(new Product("Lidl_P001", "lapte zuzu", "lactate", "Zuzu", "l"));
    }

    @Test
    void testFindByIdEndingWith() {
        List<Product> products = repo.findByIdEndingWith("P001");
        assertEquals(1, products.size());
    }

    @Test
    void testFindAllByName() {
        List<Product> products = repo.findAllByName("lapte zuzu");
        assertEquals(1, products.size());
    }
}
