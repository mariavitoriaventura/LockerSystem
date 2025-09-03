package org.project.demo.repository;
import org.project.demo.model.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface DeliveryRepository extends JpaRepository<Delivery, Long>{
    List<Delivery> findByResidentApartment(String apartment);
}
