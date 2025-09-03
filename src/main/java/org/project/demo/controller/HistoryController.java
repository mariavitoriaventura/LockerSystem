package org.project.demo.controller;

import org.project.demo.dto.DeliveryRecordDTO;
import org.project.demo.model.*;
import org.project.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
@RestController
@RequestMapping("/history")
public class HistoryController {

    @Autowired private DeliveryRepository deliveryRepository;
    @Autowired private DeliveryHistoryRepository deliveryHistoryRepository;
    @Autowired private AuditLogRepository auditLogRepository;
    @Autowired private UserRepository userRepository;

    @GetMapping
    public List<DeliveryRecordDTO> getFullDeliveryHistory() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        org.project.demo.model.User userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<DeliveryRecordDTO> result = new ArrayList<>();

        List<Delivery> activeDeliveries = deliveryRepository.findAll();
        for (Delivery d : activeDeliveries) {
            result.add(new DeliveryRecordDTO(
                    d.getResident().getName(),
                    d.getResident().getApartment(),
                    String.valueOf(d.getLockerNumber()),
                    d.getPhotoUrl(),
                    d.getReceivedAt(),
                    null,
                    "ACTIVE"
            ));
        }

        List<DeliveryHistory> collected = deliveryHistoryRepository.findAll();
        for (DeliveryHistory h : collected) {
            result.add(new DeliveryRecordDTO(
                    h.getResident().getName(),
                    h.getResident().getApartment(),
                    String.valueOf(h.getLockerNumber()),
                    h.getPhotoUrl(),
                    h.getReceivedAt(),
                    h.getCollectedAt(),
                    "COLLECTED"
            ));
        }

        auditLogRepository.save(
                AuditLog.builder()
                        .entityName("DeliveryHistory")
                        .entityId(null)
                        .action("ACCESS")
                        .userId(userEntity.getId())
                        .username(userEntity.getUsername())
                        .changedFields("Acesso ao histórico completo de entregas.")
                        .timestamp(LocalDateTime.now())
                        .build()
        );

        return result;
    }
}
