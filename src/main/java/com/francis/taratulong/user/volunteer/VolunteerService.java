package com.francis.taratulong.user.volunteer;


import com.francis.taratulong.exception.UserAlreadyExistsException;
import com.francis.taratulong.exception.UserNotFoundException;
import com.francis.taratulong.user.Role;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class VolunteerService {
    private final VolunteerRepository volunteerRepository;
    private final PasswordEncoder passwordEncoder;

    public Volunteer saveVolunteer(Volunteer volunteer) {
        log.debug("Attempting to save volunteer with email={}", volunteer.getEmail());
        Volunteer existingUser = volunteerRepository.findByEmail(volunteer.getEmail()).orElse(null);
        if(existingUser!=null) {
            log.warn("Volunteer registration denied: email {} already exists", volunteer.getEmail());
            throw new UserAlreadyExistsException("User already exist", existingUser.getEmail());
        }
        volunteer.setPassword(passwordEncoder.encode(volunteer.getPassword()));
        volunteer.setRole(Role.VOLUNTEER);
        Volunteer saved = volunteerRepository.save(volunteer);
        log.info("Volunteer created: id={}, email={}", saved.getId(), saved.getEmail());
        return saved;
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

}
