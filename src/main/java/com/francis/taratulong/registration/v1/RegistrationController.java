package com.francis.taratulong.registration.v1;

import com.francis.taratulong.Status;
import com.francis.taratulong.registration.AttendanceStatus;
import com.francis.taratulong.registration.Registration;
import com.francis.taratulong.registration.RegistrationService;
import com.francis.taratulong.registration.v1.dto.RatingAndFeedbackRequestAndResponseDTO;
import com.francis.taratulong.registration.v1.dto.RegistrationMapper;
import com.francis.taratulong.registration.v1.dto.RegistrationRequestDTO;
import com.francis.taratulong.registration.v1.dto.RegistrationResponseDTO;
import com.francis.taratulong.user.AppUser;
import com.francis.taratulong.user.volunteer.VolunteerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Registration")
@RestController
@RequestMapping("api/v1/registrations")
@RequiredArgsConstructor
public class RegistrationController {
    private final RegistrationService registrationService;
    private final RegistrationMapper registrationMapper;

    @PostMapping
    public ResponseEntity<RegistrationResponseDTO> createRegistration(
            @Valid @RequestBody RegistrationRequestDTO requestDTO,
            @AuthenticationPrincipal AppUser currentVolunteer){
        RegistrationResponseDTO response = registrationMapper.toResponseDTO(registrationService.saveRegistration(currentVolunteer.getId(), requestDTO.eventId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegistrationResponseDTO> getRegistration(@PathVariable Long id){
        RegistrationResponseDTO response = registrationMapper.toResponseDTO(registrationService.getRegistration(id));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/events/{eventId}")
    public ResponseEntity<Page<RegistrationResponseDTO>> getRegistrationsForEvent(
            @PathVariable Long eventId,
            @AuthenticationPrincipal AppUser appUser,
            @PageableDefault(size = 20, sort = "appliedAt", direction = Sort.Direction.DESC)Pageable pageable
    ) {
        Page<Registration> registrationPage = registrationService.getRegistrationsForEvent(eventId, appUser.getId(),pageable);
        return ResponseEntity.ok(registrationPage.map(registrationMapper::toResponseDTO));
    }

    @GetMapping("/volunteer")
    public ResponseEntity<Page<RegistrationResponseDTO>> getRegistrationsForVolunteer(
            @AuthenticationPrincipal AppUser appUser,
            @PageableDefault(size = 20, sort = "appliedAt", direction = Sort.Direction.DESC)Pageable pageable,
            @RequestParam(required = false ) Status status
    ) {
        Page<Registration> registrationPage = registrationService.getRegistrationsForVolunteer(appUser.getId(), status , pageable);
        return ResponseEntity.ok(registrationPage.map(registrationMapper::toResponseDTO));
    }

    @PatchMapping("/{id}/present")
    public ResponseEntity<Void> setRegistrationToPresent(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUser currentOrg
    ) {
        registrationService.updateAttendanceStatus(id, currentOrg.getId(), AttendanceStatus.PRESENT);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/absent")
    public ResponseEntity<Void> setRegistrationToAbsent(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUser currentOrg
    ) {
        registrationService.updateAttendanceStatus(id, currentOrg.getId(), AttendanceStatus.NO_SHOW);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> setRegistrationToCancelled(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        registrationService.cancelRegistration(currentUser.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/feedback")
    public ResponseEntity<RatingAndFeedbackRequestAndResponseDTO> setRatingAndFeedback(
            @PathVariable Long id,
            @RequestBody RatingAndFeedbackRequestAndResponseDTO feedbackRequestDTO,
            @AuthenticationPrincipal AppUser currentOrg
    ) {
        RatingAndFeedbackRequestAndResponseDTO response = registrationMapper.toRatingAndFeedbackDTO(registrationService.setRatingAndFeedback(id, feedbackRequestDTO.rating(), feedbackRequestDTO.feedback(), currentOrg.getId()));
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
