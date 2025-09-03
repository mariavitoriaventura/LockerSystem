package org.project.demo.repository;
import org.project.demo.model.Resident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
public interface ResidentRepository extends JpaRepository<Resident, Long>{
    Optional<Resident> findByApartment(String apartment);
    boolean existsByApartment(String apartment);

}
