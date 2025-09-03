package org.project.demo.service;

import org.project.demo.enums.LockerStatus;
import org.project.demo.model.Delivery;
import org.project.demo.model.Locker;
import org.project.demo.repository.DeliveryRepository;
import org.project.demo.repository.LockerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LockerService {

    @Autowired
    private LockerRepository lockerRepository;

    @Autowired
    private DeliveryRepository deliveryRepository;

    public List<Locker> getAllLockers() {
        return lockerRepository.findAll();
    }

    public void markLockerOccupied(Long lockerId, Delivery delivery) {
        Locker locker = lockerRepository.findById(lockerId)
                .orElseThrow(() -> new RuntimeException("Locker not found"));
        locker.setStatus(LockerStatus.OCCUPIED);
        locker.setDelivery(delivery);
        lockerRepository.save(locker);
    }

    public void markLockerFree(Long lockerId) {
        Locker locker = lockerRepository.findById(lockerId)
                .orElseThrow(() -> new RuntimeException("Locker not found"));
        locker.setStatus(LockerStatus.FREE);
        locker.setDelivery(null);
        lockerRepository.save(locker);
    }
}
