package com.francis.taratulong.registration;

import com.francis.taratulong.registration.dto.RatingAndFeedbackRequestDTO;
import com.francis.taratulong.registration.dto.RegistrationMapper;
import com.francis.taratulong.registration.dto.RegistrationRequestDTO;
import com.francis.taratulong.registration.dto.RegistrationResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/registrations")
@RequiredArgsConstructor
public class RegistrationController {
    private final RegistrationService registrationService;

    @PostMapping
    public RegistrationResponseDTO createRegistration(@Valid @RequestBody RegistrationRequestDTO requestDTO){
        return RegistrationMapper.toResponseDTO(registrationService.saveRegistration(requestDTO.volunteerId(), requestDTO.eventId()));
    }

    @GetMapping("/{id}")
    public RegistrationResponseDTO getRegistration(@PathVariable Long id){
        return RegistrationMapper.toResponseDTO(registrationService.getRegistration(id));
    }

    @PatchMapping("/{id}/present")
    public void setRegistrationToPresent(@PathVariable Long id) {
        registrationService.setPresent(id);
    }

    @PatchMapping("/{id}/absent")
    public void setRegistrationToAbsent(@PathVariable Long id) {
        registrationService.setNotPresent(id);
    }
    @PatchMapping("/{id}/feedback")
    public void setRegistrationFeedback(@PathVariable Long id, @RequestBody RatingAndFeedbackRequestDTO feedbackRequestDTO) {
        registrationService.setRatingAndFeedback(id, feedbackRequestDTO.rating(), feedbackRequestDTO.feedback());
    }
    @PatchMapping("/{id}/status/approved")
    public void setRegistrationApproved(@PathVariable Long id) {
        registrationService.setApproved(id);
    }

    @PatchMapping("/{id}/status/rejected")
    public void setRegistrationRejected(@PathVariable Long id) {
        registrationService.setRejected(id);
    }

    @DeleteMapping("/{id}")
    public void deleteRegistration(@PathVariable Long id){
        registrationService.deleteRegistration(id);
    }
}
