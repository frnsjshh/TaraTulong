package com.francis.taratulong.user.admin;

import com.francis.taratulong.user.AppUser;
import com.francis.taratulong.user.organization.Org;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter@Setter@NoArgsConstructor
public class Admin extends AppUser {
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @OneToMany(mappedBy = "admin")
    private List<Org> org;
}
