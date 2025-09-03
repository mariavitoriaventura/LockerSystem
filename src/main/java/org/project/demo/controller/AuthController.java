package org.project.demo.controller;

import org.project.demo.dto.LoginRequest;
import org.project.demo.dto.LoginResponse;
import org.project.demo.dto.RegisterRequest;
import org.project.demo.enums.Role;
import org.project.demo.model.AuditLog;
import org.project.demo.model.User;
import org.project.demo.repository.AuditLogRepository;
import org.project.demo.repository.UserRepository;
import org.project.demo.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String token = jwtUtil.generateToken(request.getUsername());

        // Log de login
        userRepository.findByUsername(request.getUsername()).ifPresent(user -> {
            AuditLog log = new AuditLog();
            log.setEntityName("User");
            log.setEntityId(user.getId());
            log.setAction("LOGIN");
            log.setChangedFields("Login realizado com sucesso.");
            log.setUserId(user.getId());
            log.setUsername(user.getUsername());
            log.setTimestamp(LocalDateTime.now());
            auditLogRepository.save(log);
        });

        return ResponseEntity.ok(new LoginResponse(token));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Usuário já existe.");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(Role.valueOf(request.getRole().toUpperCase()));

        User savedUser = userRepository.save(user);

        // Log de registro
        AuditLog log = new AuditLog();
        log.setEntityName("User");
        log.setEntityId(savedUser.getId());
        log.setAction("REGISTER");
        log.setChangedFields("Usuário registrado: " + savedUser.getUsername());
        log.setUserId(savedUser.getId());
        log.setUsername(savedUser.getUsername());
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);

        return ResponseEntity.ok("Usuário registrado com sucesso!");
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return ResponseEntity.ok(user);
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@RequestBody Map<String, String> updates, Authentication auth) {
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        StringBuilder changes = new StringBuilder();

        updates.forEach((key, value) -> {
            switch (key) {
                case "email":
                    if (!user.getEmail().equals(value)) {
                        changes.append("Email: ").append(user.getEmail()).append(" -> ").append(value).append("; ");
                        user.setEmail(value);
                    }
                    break;
                case "phone":
                    if (user.getPhone() == null || !user.getPhone().equals(value)) {
                        changes.append("Telefone: ").append(user.getPhone()).append(" -> ").append(value).append("; ");
                        user.setPhone(value);
                    }
                    break;
                case "password":
                    if (!value.isBlank()) {
                        changes.append("Senha: atualizada; ");
                        user.setPassword(passwordEncoder.encode(value));
                    }
                    break;
            }
        });

        userRepository.save(user);

        if (changes.length() > 0) {
            AuditLog log = new AuditLog();
            log.setEntityName("User");
            log.setEntityId(user.getId());
            log.setAction("UPDATE");
            log.setChangedFields(changes.toString());
            log.setUserId(user.getId());
            log.setUsername(user.getUsername());
            log.setTimestamp(LocalDateTime.now());
            auditLogRepository.save(log);
        }

        return ResponseEntity.ok("Dados atualizados");
    }
}
