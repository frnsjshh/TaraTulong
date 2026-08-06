package com.francis.taratulong.user.volunteer;

import com.francis.taratulong.user.AppUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Getter@Setter@NoArgsConstructor
public class Volunteer extends AppUser {

    @Column(length = 50, nullable = false)
    private String firstName;

    @Column(length = 50, nullable = false)
    private String lastName;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal attendanceRate = BigDecimal.ZERO; //eg. 86.00

    @Column(nullable = false)
    private int totalEventsAttended = 0;

    @Column(nullable = false)
    private int totalApprovedRegistrations = 0;


    private static final int ATTENDANCE_RATE_SCALE = 2;

    @PreUpdate
    private void updateAttendanceRate() {
        if (totalEventsAttended == 0) return;
        //calculate attendance rate
        attendanceRate = BigDecimal.valueOf(totalApprovedRegistrations)
                        .divide(BigDecimal.valueOf(totalEventsAttended), ATTENDANCE_RATE_SCALE, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));//to get percentage
    }
}
