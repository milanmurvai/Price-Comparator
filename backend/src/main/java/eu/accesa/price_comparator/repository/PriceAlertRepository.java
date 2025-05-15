package eu.accesa.price_comparator.repository;

import eu.accesa.price_comparator.model.PriceAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PriceAlertRepository extends JpaRepository<PriceAlert, Long> {
    List<PriceAlert> findAllByTriggeredFalse();
}

