package org.project.demo.repository;

import org.project.demo.model.Locker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LockerRepository extends JpaRepository<Locker, Long> {
    Optional<Locker> findByNumber(Integer number);
}
