package com.francis.taratulong.registration.v1;

import com.francis.taratulong.registration.RegistrationService;
import com.francis.taratulong.registration.v1.dto.RatingAndFeedbackRequestAndResponseDTO;
import com.francis.taratulong.registration.v1.dto.RegistrationMapper;
import com.francis.taratulong.registration.v1.dto.RegistrationRequestDTO;
import com.francis.taratulong.registration.v1.dto.RegistrationResponseDTO;
import com.francis.taratulong.user.AppUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/registrations")
@RequiredArgsConstructor
public class RegistrationController {
    private final RegistrationService registrationService;

    @PostMapping
    public ResponseEntity<RegistrationResponseDTO> createRegistration(
            @Valid @RequestBody RegistrationRequestDTO requestDTO,
            @AuthenticationPrincipal AppUser currentVolunteer){
        RegistrationResponseDTO response = RegistrationMapper.toResponseDTO(registrationService.saveRegistration(currentVolunteer.getId(), requestDTO.eventId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegistrationResponseDTO> getRegistration(@PathVariable Long id){
        RegistrationResponseDTO response = RegistrationMapper.toResponseDTO(registrationService.getRegistration(id));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/{id}/present")
    public ResponseEntity<Void> setRegistrationToPresent(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUser currentOrg
    ) {
        registrationService.setPresent(id, currentOrg.getId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/absent")
    public ResponseEntity<Void> setRegistrationToAbsent(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUser currentOrg
    ) {
        registrationService.setNotPresent(id, currentOrg.getId());
        return ResponseEntity.noContent().build();
    }
    @PatchMapping("/{id}/feedback")
    public ResponseEntity<RatingAndFeedbackRequestAndResponseDTO> setRatingAndFeedback(
            @PathVariable Long id,
            @RequestBody RatingAndFeedbackRequestAndResponseDTO feedbackRequestDTO,
            @AuthenticationPrincipal AppUser currentOrg
    ) {
        RatingAndFeedbackRequestAndResponseDTO response = RegistrationMapper.toRatingAndFeedbackDTO(registrationService.setRatingAndFeedback(id, feedbackRequestDTO.rating(), feedbackRequestDTO.feedback(), currentOrg.getId()));
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status/approved")
    public ResponseEntity<Void> setRegistrationApproved(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUser currentOrg
    ) {
        registrationService.setApproved(id, currentOrg.getId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status/rejected")
    public ResponseEntity<Void> setRegistrationRejected(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUser currentOrg
    ) {
        registrationService.setRejected(id, currentOrg.getId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRegistration(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUser currentVolunteer
    ){
        registrationService.deleteRegistration(id, currentVolunteer.getId());
        return ResponseEntity.noContent().build();
    }
}
