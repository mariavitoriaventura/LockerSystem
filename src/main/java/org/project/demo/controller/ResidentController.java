package org.project.demo.controller;

import org.project.demo.model.AuditLog;
import org.project.demo.model.Resident;
import org.project.demo.model.User;
import org.project.demo.repository.AuditLogRepository;
import org.project.demo.repository.ResidentRepository;
import org.project.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/residents")
public class ResidentController {

    @Autowired
    private ResidentRepository residentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @GetMapping
    public List<Resident> getAllResidents() {
        return residentRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> createResident(
            @RequestBody Resident resident,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado.");
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Resident saved = residentRepository.save(resident);

        AuditLog log = new AuditLog();
        log.setEntityName("Resident");
        log.setEntityId(saved.getId());
        log.setAction("CREATE");
        log.setUserId(user.getId());
        log.setUsername(user.getUsername());
        log.setChangedFields("Criado: id=" + saved.getId() + ", name=" + saved.getName());
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);

        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateResident(
            @PathVariable Long id,
            @RequestBody Resident resident,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado.");
        }
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Resident existingResident = residentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Morador não encontrado"));

        StringBuilder changes = new StringBuilder();
        if (!existingResident.getName().equals(resident.getName())) {
            changes.append("Nome: ").append(existingResident.getName())
                    .append(" -> ").append(resident.getName()).append("; ");
            existingResident.setName(resident.getName());
        }
        if (!existingResident.getApartment().equals(resident.getApartment())) {
            changes.append("Apartamento: ").append(existingResident.getApartment())
                    .append(" -> ").append(resident.getApartment()).append("; ");
            existingResident.setApartment(resident.getApartment());
        }
        if (!existingResident.getEmail().equals(resident.getEmail())) {
            changes.append("Email: ").append(existingResident.getEmail())
                    .append(" -> ").append(resident.getEmail()).append("; ");
            existingResident.setEmail(resident.getEmail());
        }
        if (!existingResident.getPhone().equals(resident.getPhone())) {
            changes.append("Telefone: ").append(existingResident.getPhone())
                    .append(" -> ").append(resident.getPhone()).append("; ");
            existingResident.setPhone(resident.getPhone());
        }

        Resident updatedResident = residentRepository.save(existingResident);

        if (changes.length() > 0) {
            AuditLog log = new AuditLog();
            log.setEntityName("Resident");
            log.setEntityId(updatedResident.getId());
            log.setAction("UPDATE");
            log.setUserId(user.getId());
            log.setUsername(user.getUsername());
            log.setChangedFields(changes.toString());
            log.setTimestamp(LocalDateTime.now());
            auditLogRepository.save(log);
        }

        return ResponseEntity.ok(updatedResident);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteResident(
            @PathVariable Long id,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado.");
        }
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Resident existingResident = residentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Morador não encontrado"));

        // capture os dados antes do delete (evita LAZY no toString)
        Long deletedId = existingResident.getId();
        String deletedName = existingResident.getName();

        residentRepository.delete(existingResident);

        AuditLog log = new AuditLog();
        log.setEntityName("Resident");
        log.setEntityId(deletedId);
        log.setAction("DELETE");
        log.setUserId(user.getId());
        log.setUsername(user.getUsername());
        log.setChangedFields("Morador deletado: id=" + deletedId + ", name=" + deletedName);
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);

        return ResponseEntity.noContent().build();
    }
    @GetMapping("/exists/{apartment}")
    public ResponseEntity<Boolean> checkIfApartmentExists(@PathVariable String apartment) {
        boolean exists = residentRepository.existsByApartment(apartment);
        return ResponseEntity.ok(exists);
    }
}
