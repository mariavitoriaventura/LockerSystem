package org.project.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime collectedAt;

    @ManyToOne
    private Resident resident;

    private Integer lockerNumber;

    private String photoUrl;
    private LocalDateTime receivedAt;

}
