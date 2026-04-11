package com.francis.taratulong.user.volunteer;

import com.francis.taratulong.user.AppUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter@Setter@NoArgsConstructor
public class Volunteer extends AppUser {

    @Column(length = 100)
    private String firstName;

    @Column(length = 100)
    private String lastName;


}
