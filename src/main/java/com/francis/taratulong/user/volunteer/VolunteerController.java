package com.francis.taratulong.user.volunteer;

import com.francis.taratulong.user.AppUser;
import com.francis.taratulong.user.dto.AppUserRequestEmailDTO;
import com.francis.taratulong.user.dto.AppUserRequestPasswordDTO;
import com.francis.taratulong.user.AppUserService;
import com.francis.taratulong.user.volunteer.dto.VolunteerMapper;
import com.francis.taratulong.user.volunteer.dto.VolunteerRequestDTO;
import com.francis.taratulong.user.volunteer.dto.VolunteerRequestProfileDTO;
import com.francis.taratulong.user.volunteer.dto.VolunteerResponseDTO;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
                volunteerService.saveVolunteer(VolunteerMapper.toEntity(volunteerRequestDTO)));
    }

    @GetMapping("/me")
    public VolunteerResponseDTO getUser(
            @AuthenticationPrincipal AppUser appUser
            ) {
        return VolunteerMapper.toResponseDTO(volunteerService.getVolunteer(appUser.getId()));
    }

    @PutMapping("/me")
    public VolunteerResponseDTO updateUser(
            @AuthenticationPrincipal AppUser appUser,
            @Valid@RequestBody VolunteerRequestProfileDTO volunteerRequestProfileDTO
    ){
        return VolunteerMapper.toResponseDTO(
                volunteerService.updateVolunteer(appUser.getId(), VolunteerMapper.toEntity(volunteerRequestProfileDTO))
        );
    }
    @PatchMapping("/me/email")
    public void updateEmail(@AuthenticationPrincipal AppUser appUser, @Valid@RequestBody AppUserRequestEmailDTO emailRequestDTO) {
         appUserService.updateEmail(appUser.getId(), emailRequestDTO.email());
    }
    @PatchMapping("/me/password")
    public void updatePassword(@AuthenticationPrincipal AppUser appUser, @Valid@RequestBody AppUserRequestPasswordDTO passwordRequestDTO) {
        appUserService.updatePassword(appUser.getId(), passwordRequestDTO.currentPassword(), passwordRequestDTO.password());
    }

    @DeleteMapping("/me")
    public void deleteUser(@AuthenticationPrincipal AppUser appUser) {
        volunteerService.deleteVolunteer(appUser.getId());
    }


}
