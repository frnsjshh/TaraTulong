package com.francis.taratulong.user.organization;

import com.francis.taratulong.Status;
import com.francis.taratulong.event.Event;
import com.francis.taratulong.user.AppUser;
import com.francis.taratulong.user.admin.Admin;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter@Setter
@NoArgsConstructor
public class Org extends AppUser {

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private Admin approvedBy;

    @OneToMany(mappedBy = "organizer", fetch = FetchType.LAZY)
    private List<Event> events;

}
