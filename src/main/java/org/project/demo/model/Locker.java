package org.project.demo.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.project.demo.enums.LockerStatus;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Locker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private Integer number; // Número do locker, 1 a 30

    @Enumerated(EnumType.STRING)
    private LockerStatus status; // EX: FREE, OCCUPIED, MAINTENANCE

    @OneToOne(mappedBy = "locker", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Delivery delivery; // entrega que está neste locker, pode ser null se livre

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public LockerStatus getStatus() {
        return status;
    }

    public void setStatus(LockerStatus status) {
        this.status = status;
    }

    public Delivery getDelivery() {
        return delivery;
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
    }
}
