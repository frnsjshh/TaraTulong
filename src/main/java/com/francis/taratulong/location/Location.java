package com.francis.taratulong.location;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter@Setter@NoArgsConstructor
public class Location {
    @Id
    private Long id;
    @Column(nullable = false, unique = true)
    private String code;
    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LocationType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Location parent;

}
