package com.francis.taratulong.user.volunteer.v1;

import com.francis.taratulong.user.AppUser;
import com.francis.taratulong.user.volunteer.VolunteerService;
import com.francis.taratulong.user.volunteer.v1.dto.VolunteerMapper;
import com.francis.taratulong.user.volunteer.v1.dto.VolunteerRequestDTO;
import com.francis.taratulong.user.volunteer.v1.dto.VolunteerRequestProfileDTO;
import com.francis.taratulong.user.volunteer.v1.dto.VolunteerResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/v1/volunteers")
@RequiredArgsConstructor
public class VolunteerController {
    private final VolunteerService volunteerService;
    private final VolunteerMapper volunteerMapper;

    @PostMapping
    public ResponseEntity<VolunteerResponseDTO> saveUser(@Valid @RequestBody VolunteerRequestDTO volunteerRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                volunteerMapper.toResponseDTO(volunteerService.saveVolunteer(volunteerMapper.toEntity(volunteerRequestDTO)))
        );
    }

    @GetMapping("/me")
    public ResponseEntity<VolunteerResponseDTO> getUser(@AuthenticationPrincipal AppUser appUser) {
        return ResponseEntity.ok(
                volunteerMapper.toResponseDTO(volunteerService.getVolunteer(appUser.getId()))
        );
    }

    @PutMapping("/me")
    public ResponseEntity<VolunteerResponseDTO> updateUser(
            @AuthenticationPrincipal AppUser appUser,
            @Valid@RequestBody VolunteerRequestProfileDTO volunteerRequestProfileDTO
    ){
        return ResponseEntity.ok(
                volunteerMapper.toResponseDTO(volunteerService.updateVolunteer(appUser.getId(), volunteerMapper.toEntity(volunteerRequestProfileDTO)))
        );
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal AppUser appUser) {
        volunteerService.deleteVolunteer(appUser.getId());
        return ResponseEntity.noContent().build();
    }

}
