package com.francis.taratulong.registration;


import com.francis.taratulong.Status;
import com.francis.taratulong.event.Event;
import com.francis.taratulong.user.volunteer.Volunteer;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter@Setter@NoArgsConstructor
@Table(
        name = "registration",
        indexes = {
                @Index(name = "idx_registration_event", columnList = "event_id"),
                @Index(name = "idx_registration_volunteer", columnList = "volunteer_id")
        }
)
public class Registration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "volunteer_id")
    private Volunteer volunteer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status registrationStatus = Status.PENDING;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus attendanceStatus;

    @Column()
    @Min(1)
    @Max(5)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime appliedAt;
}
