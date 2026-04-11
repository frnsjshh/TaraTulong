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
@Getter@Setter@NoArgsConstructor
public class Org extends AppUser {

    @Column(columnDefinition = "TEXT")
    private String description;

    private String location;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private Admin admin;

    @OneToMany(mappedBy = "organizer")
    private List<Event> events;

}
