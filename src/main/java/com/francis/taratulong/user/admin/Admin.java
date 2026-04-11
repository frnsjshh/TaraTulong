package com.francis.taratulong.user.admin;

import com.francis.taratulong.user.AppUser;
import com.francis.taratulong.user.organization.Org;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter@Setter@NoArgsConstructor
public class Admin extends AppUser {
    private String name;

    @OneToMany(mappedBy = "admin")
    private List<Org> org;
}
