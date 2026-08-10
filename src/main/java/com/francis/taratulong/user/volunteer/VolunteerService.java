package com.francis.taratulong.user.volunteer;


import com.francis.taratulong.exception.UserAlreadyExistsException;
import com.francis.taratulong.exception.UserNotFoundException;
import com.francis.taratulong.registration.AttendanceStatus;
import com.francis.taratulong.registration.RegistrationService;
import com.francis.taratulong.user.Role;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class VolunteerService {
    private final VolunteerRepository volunteerRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegistrationService registrationService;

    public Volunteer saveVolunteer(Volunteer volunteer) {
        Volunteer existingUser = volunteerRepository.findByEmail(volunteer.getEmail()).orElse(null);
        if(existingUser!=null) throw new UserAlreadyExistsException("User already exist", existingUser.getEmail());
        volunteer.setPassword(passwordEncoder.encode(volunteer.getPassword()));
        volunteer.setRole(Role.VOLUNTEER);
        return volunteerRepository.save(volunteer);
    }

    public Volunteer getVolunteer(Long id) {
        return volunteerRepository.findById(id).orElseThrow(() -> new UserNotFoundException("Volunteer not found"));
    }


    public Volunteer updateVolunteer(Long id, Volunteer volunteerRequest) {
        Volunteer volunteer = volunteerRepository.findById(id).orElseThrow(() -> new UserNotFoundException("Volunteer not found:" + id));
        volunteer.setFirstName(volunteerRequest.getFirstName());
        volunteer.setLastName(volunteerRequest.getLastName());
        return volunteer;
    }


    public void updateEventsAttended(Long id, int amountToShift) {
        Volunteer volunteer = getVolunteer(id);
        volunteer.setTotalEventsAttended(volunteer.getTotalEventsAttended() + amountToShift);
    }

    public void updateTotalRegistrations(Long id, int number) {
        Volunteer volunteer = getVolunteer(id);
        volunteer.setTotalApprovedRegistrations(volunteer.getTotalApprovedRegistrations() + number);
    }

    public void updateTrustScore(Long id, int score) {
        Volunteer volunteer = getVolunteer(id);
        volunteer.setTrustScore(volunteer.getTrustScore() + score);
    }

    public AttendanceStatus cancelRegistration(Long registrationId, Long volunteerId) {
        return registrationService.cancelRegistration(registrationId, volunteerId);
    }

    public void deleteVolunteer(Long id) {
        Volunteer volunteer = volunteerRepository.findById(id).orElseThrow(()-> new UserNotFoundException("Cannot delete volunteer. Volunteer not found: " + id));
        volunteer.setDeleted(true);
        volunteer.setEmail("DELETED_"+volunteer.getEmail() + "_"+ LocalDateTime.now());
    }
}
