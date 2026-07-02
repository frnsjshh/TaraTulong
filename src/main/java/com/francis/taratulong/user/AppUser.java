package com.francis.taratulong.user;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter@Setter@NoArgsConstructor
@Table(name="app_users")
@Inheritance(strategy = InheritanceType.JOINED)
@SQLRestriction("deleted=false")
public class AppUser {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @UuidGenerator
    @Column(nullable = false, updatable = false, unique = true)
    private UUID uuid;

    @Column (nullable = false, unique = true)
    @Email
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime joinDate;

    @Column(nullable = false)
    private boolean deleted = false;
}
