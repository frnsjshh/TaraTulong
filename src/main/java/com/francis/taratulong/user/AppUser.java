package com.francis.taratulong.user;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jdk.jfr.Timestamp;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter@Setter@NoArgsConstructor
@Table(name="app_users")
public class AppUser {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String uuid;

    @Column (nullable = false)
    @Email
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    @Timestamp
    private LocalDateTime joinDate;

    @Column(nullable = false)
    private Boolean isDeleted = false;
}
