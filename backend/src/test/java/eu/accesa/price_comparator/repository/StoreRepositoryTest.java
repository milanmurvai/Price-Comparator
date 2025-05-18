package eu.accesa.price_comparator.repository;

import eu.accesa.price_comparator.model.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class StoreRepositoryTest {

    @Autowired
    private StoreRepository repo;

    @BeforeEach
    void setup() {
        repo.save(new Store("Lidl"));
    }

    @Test
    void testFindByName() {
        Optional<Store> store = repo.findByName("Lidl");
        assertTrue(store.isPresent());
        assertEquals("Lidl", store.get().getName());
    }
}
