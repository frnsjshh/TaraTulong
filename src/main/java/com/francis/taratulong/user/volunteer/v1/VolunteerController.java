package com.francis.taratulong.user.volunteer.v1;

import com.francis.taratulong.user.AppUser;
import com.francis.taratulong.user.volunteer.VolunteerService;
import com.francis.taratulong.user.volunteer.v1.dto.VolunteerMapper;
import com.francis.taratulong.user.volunteer.v1.dto.VolunteerRequestDTO;
import com.francis.taratulong.user.volunteer.v1.dto.VolunteerRequestProfileDTO;
import com.francis.taratulong.user.volunteer.v1.dto.VolunteerResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/v1/volunteers")
public class VolunteerController {
    private final VolunteerService volunteerService;
    public VolunteerController(VolunteerService volunteerService) {
        this.volunteerService = volunteerService;
    }

    @PostMapping
    public ResponseEntity<VolunteerResponseDTO> saveUser(@Valid @RequestBody VolunteerRequestDTO volunteerRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                VolunteerMapper.toResponseDTO(volunteerService.saveVolunteer(VolunteerMapper.toEntity(volunteerRequestDTO)))
        );
    }

    @GetMapping("/me")
    public ResponseEntity<VolunteerResponseDTO> getUser(@AuthenticationPrincipal AppUser appUser) {
        return ResponseEntity.ok(
                VolunteerMapper.toResponseDTO(volunteerService.getVolunteer(appUser.getId()))
        );
    }

    @PutMapping("/me")
    public ResponseEntity<VolunteerResponseDTO> updateUser(
            @AuthenticationPrincipal AppUser appUser,
            @Valid@RequestBody VolunteerRequestProfileDTO volunteerRequestProfileDTO
    ){
        return ResponseEntity.ok(
                VolunteerMapper.toResponseDTO(volunteerService.updateVolunteer(appUser.getId(), VolunteerMapper.toEntity(volunteerRequestProfileDTO)))
        );
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal AppUser appUser) {
        volunteerService.deleteVolunteer(appUser.getId());
        return ResponseEntity.noContent().build();
    }

}
