package com.francis.taratulong.user.volunteer;

import com.francis.taratulong.user.AppUserRequestEmailDTO;
import com.francis.taratulong.user.AppUserRequestPasswordDTO;
import com.francis.taratulong.user.AppUserService;
import com.francis.taratulong.user.volunteer.dto.VolunteerMapper;
import com.francis.taratulong.user.volunteer.dto.VolunteerRequestDTO;
import com.francis.taratulong.user.volunteer.dto.VolunteerRequestProfileDTO;
import com.francis.taratulong.user.volunteer.dto.VolunteerResponseDTO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/volunteers")
public class VolunteerController {
    private final VolunteerService volunteerService;
    private final AppUserService appUserService;
    public VolunteerController(VolunteerService volunteerService, AppUserService appUserService) {
        this.volunteerService = volunteerService;
        this.appUserService = appUserService;
    }

    @PostMapping
    public VolunteerResponseDTO saveUser(@Valid @RequestBody VolunteerRequestDTO volunteerRequestDTO) {
        return VolunteerMapper.toResponseDTO(
                volunteerService.saveVolunteer(
                        VolunteerMapper.toEntity(volunteerRequestDTO)));
    }

    @GetMapping("/{id}")
    public VolunteerResponseDTO getUser(@PathVariable Long id) {
        return VolunteerMapper.toResponseDTO(volunteerService.getVolunteer(id));
    }

    @PutMapping("/{id}")
    public VolunteerResponseDTO updateUser(@PathVariable Long id, @Valid@RequestBody VolunteerRequestProfileDTO volunteerRequestProfileDTO){
        return VolunteerMapper.toResponseDTO(
                volunteerService.updateVolunteer(id, VolunteerMapper.toEntity(volunteerRequestProfileDTO))
        );
    }
    @PatchMapping("/{id}/email")
    public void updateEmail(@PathVariable Long id, @Valid@RequestBody AppUserRequestEmailDTO emailRequestDTO) {
         appUserService.updateEmail(id, emailRequestDTO.email());
    }
    @PatchMapping("/{id}/password")
    public void updatePassword(@PathVariable Long id, @Valid@RequestBody AppUserRequestPasswordDTO passwordRequestDTO) {
        appUserService.updatePassword(id, passwordRequestDTO.currentPassword(), passwordRequestDTO.password());
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        volunteerService.deleteVolunteer(id);
    }


}
