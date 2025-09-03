package org.project.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryRecordDTO {
    private String residentName;
    private String apartment;
    private String lockerNumber;
    private String photoUrl;
    private LocalDateTime receivedAt;
    private LocalDateTime collectedAt;
    private String status;
}
