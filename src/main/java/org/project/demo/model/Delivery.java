package org.project.demo.model;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer lockerNumber;

    private LocalDateTime receivedAt;

    private String photoUrl;

    @Column(columnDefinition = "TEXT")
    private String observation;

    @Column(length = 6)
    private String pickupToken;


    @ManyToOne
    @JoinColumn(name = "resident_id", nullable = false)
    private Resident resident;

    @OneToOne
    @JoinColumn(name = "locker_id")
    @JsonBackReference
    private Locker locker;

    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "collected_by_id")
    private User collectedBy;


    public Integer getLockerNumber() {
        return lockerNumber;
    }

    public void setLockerNumber(Integer lockerNumber) {
        this.lockerNumber = lockerNumber;
    }

    public String getPickupToken() {
        return pickupToken;
    }

    public void setPickupToken(String pickupToken) {
        this.pickupToken = pickupToken;
    }
}
