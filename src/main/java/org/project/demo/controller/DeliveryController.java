package org.project.demo.controller;

import org.project.demo.dto.DeliveryRequest;
import org.project.demo.enums.LockerStatus;
import org.project.demo.model.*;
import org.project.demo.repository.*;
import org.project.demo.service.WhatsAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/deliveries")
public class DeliveryController {

    @Autowired private DeliveryRepository deliveryRepository;
    @Autowired private WhatsAppService whatsAppService;
    @Autowired private ResidentRepository residentRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private LockerRepository lockerRepository;
    @Autowired private DeliveryHistoryRepository deliveryHistoryRepository;
    @Autowired private AuditLogRepository auditLogRepository;

    @GetMapping
    public List<Delivery> getAllDeliveries() {
        return deliveryRepository.findAll();
    }

    @GetMapping("/by-apartment/{apartment}")
    public List<Delivery> getDeliveriesByApartment(@PathVariable String apartment) {
        return deliveryRepository.findByResidentApartment(apartment);
    }

    @PostMapping
    public Delivery createDelivery(@RequestBody DeliveryRequest request) {
        Resident resident = residentRepository.findByApartment(request.getApartment())
                .orElseThrow(() -> new RuntimeException("Resident not found"));

        Locker locker = lockerRepository.findByNumber(request.getLockerNumber())
                .orElseThrow(() -> new RuntimeException("Locker not found"));

        if (locker.getStatus() != LockerStatus.FREE) {
            throw new RuntimeException("Locker is not free");
        }

        User user = userRepository.findById(request.getCreatedById())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Delivery delivery = Delivery.builder()
                .locker(locker)
                .lockerNumber(request.getLockerNumber())
                .receivedAt(LocalDateTime.now())
                .photoUrl(request.getPhotoUrl())
                .resident(resident)
                .observation(request.getObservation())
                .createdBy(user)
                .build();

        Delivery savedDelivery = deliveryRepository.save(delivery);

        locker.setStatus(LockerStatus.OCCUPIED);
        locker.setDelivery(savedDelivery);
        lockerRepository.save(locker);

        String pickupToken = String.format("%06d", new Random().nextInt(999999));
        savedDelivery.setPickupToken(pickupToken);
        deliveryRepository.save(savedDelivery);

        // Envia WhatsApp
        whatsAppService.sendDeliveryNotification(
                resident.getPhone(),
                resident.getName(),
                savedDelivery.getLockerNumber(),
                savedDelivery.getObservation(),
                pickupToken
        );

        // Log de criação
        auditLogRepository.save(
                AuditLog.builder()
                        .entityName("Delivery")
                        .entityId(savedDelivery.getId())
                        .action("CREATE")
                        .userId(user.getId())
                        .username(user.getUsername())
                        .changedFields("Locker " + savedDelivery.getLockerNumber() + " ocupado. Token: " + pickupToken)
                        .timestamp(LocalDateTime.now())
                        .build()
        );

        return savedDelivery;
    }

    @PutMapping("/{id}/collect")
    public ResponseEntity<String> collectDelivery(@PathVariable Long id, @RequestParam Long collectedById) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        User user = userRepository.findById(collectedById)
                .orElseThrow(() -> new RuntimeException("User not found"));

        delivery.setCollectedBy(user);
        deliveryRepository.save(delivery);

        DeliveryHistory history = DeliveryHistory.builder()
                .collectedAt(LocalDateTime.now())
                .resident(delivery.getResident())
                .lockerNumber(delivery.getLockerNumber())
                .photoUrl(delivery.getPhotoUrl())
                .build();
        deliveryHistoryRepository.save(history);

        Locker locker = delivery.getLocker();
        locker.setStatus(LockerStatus.FREE);
        locker.setDelivery(null);
        lockerRepository.save(locker);

        deliveryRepository.delete(delivery);

        // Log de coleta (DELETE)
        auditLogRepository.save(
                AuditLog.builder()
                        .entityName("Delivery")
                        .entityId(id)
                        .action("DELIVERED")
                        .userId(user.getId())
                        .username(user.getUsername())
                        .changedFields("Entrega coletada. Locker " + locker.getNumber() + " liberado.")
                        .timestamp(LocalDateTime.now())
                        .build()
        );

        return ResponseEntity.ok("Delivery collected successfully.");
    }

    @PutMapping("/{id}")
    public ResponseEntity<Delivery> updateDelivery(
            @PathVariable Long id,
            @RequestBody DeliveryRequest request
    ) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        Resident resident = residentRepository.findByApartment(request.getApartment())
                .orElseThrow(() -> new RuntimeException("Resident not found"));

        User user = userRepository.findById(request.getCreatedById())
                .orElseThrow(() -> new RuntimeException("User not found"));

        StringBuilder changes = new StringBuilder();
        if (!delivery.getObservation().equals(request.getObservation())) {
            changes.append("Obs: ").append(delivery.getObservation())
                    .append(" -> ").append(request.getObservation()).append("; ");
        }
        if (!delivery.getResident().getApartment().equals(resident.getApartment())) {
            changes.append("Apt: ").append(delivery.getResident().getApartment())
                    .append(" -> ").append(resident.getApartment()).append("; ");
        }

        delivery.setObservation(request.getObservation());
        delivery.setResident(resident);
        Delivery updated = deliveryRepository.save(delivery);

        if (changes.length() > 0) {
            auditLogRepository.save(
                    AuditLog.builder()
                            .entityName("Delivery")
                            .entityId(updated.getId())
                            .action("UPDATE")
                            .userId(user.getId())
                            .username(user.getUsername())
                            .changedFields(changes.toString())
                            .timestamp(LocalDateTime.now())
                            .build()
            );
        }

        return ResponseEntity.ok(updated);
    }
}
