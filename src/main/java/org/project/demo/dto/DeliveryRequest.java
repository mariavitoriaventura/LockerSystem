package org.project.demo.dto;

import lombok.Data;

@Data
public class DeliveryRequest {
    private String apartment;
    private Integer lockerNumber;
    private String photoUrl;
    private String observation;
    private Long createdById;
}
