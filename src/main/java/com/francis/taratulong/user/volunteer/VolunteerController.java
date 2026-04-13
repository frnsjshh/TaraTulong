package com.francis.taratulong.user.volunteer;

import com.francis.taratulong.user.volunteer.dto.EmailUpdateRequestDTO;
import com.francis.taratulong.user.volunteer.dto.VolunteerMapper;
import com.francis.taratulong.user.volunteer.dto.VolunteerRequestDTO;
import com.francis.taratulong.user.volunteer.dto.VolunteerResponseDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/volunteers")
public class VolunteerController {
    private final VolunteerService volunteerService;
    public VolunteerController(VolunteerService volunteerService) {
        this.volunteerService = volunteerService;
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
    public VolunteerResponseDTO updateUser(@PathVariable Long id, @Valid@RequestBody VolunteerRequestDTO volunteerRequestDTO){
        return VolunteerMapper.toResponseDTO(
                volunteerService.updateVolunteer(id, VolunteerMapper.toEntity(volunteerRequestDTO))
        );
    }
    @PatchMapping("/{id}/email")
    public VolunteerResponseDTO updateEmail(@PathVariable Long id, @Valid@RequestBody EmailUpdateRequestDTO emailRequestDTO) {
        return VolunteerMapper.toResponseDTO(volunteerService.updateEmail(id, emailRequestDTO.email()));
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        volunteerService.deleteVolunteer(id);
    }


}
