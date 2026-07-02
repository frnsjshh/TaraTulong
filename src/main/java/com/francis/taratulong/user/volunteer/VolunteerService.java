package com.francis.taratulong.user.volunteer;


import com.francis.taratulong.exception.UserAlreadyExistsException;
import com.francis.taratulong.exception.UserNotFoundException;
import com.francis.taratulong.user.Role;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
public class VolunteerService {
    private final VolunteerRepository volunteerRepository;
    private final PasswordEncoder passwordEncoder;

    public VolunteerService(VolunteerRepository volunteerRepository, PasswordEncoder passwordEncoder) {
        this.volunteerRepository = volunteerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Volunteer saveVolunteer(Volunteer volunteer) {
        Optional<Volunteer> existingUser = volunteerRepository.findByEmail(volunteer.getEmail());
        if(existingUser.isPresent()) {
            Volunteer dbFound = existingUser.get();
            if(!dbFound.isDeleted()) throw new UserAlreadyExistsException("User already exist", dbFound.getEmail());
            dbFound.setPassword(passwordEncoder.encode(volunteer.getPassword()));
            dbFound.setFirstName(volunteer.getFirstName());
            dbFound.setLastName(volunteer.getLastName());
            dbFound.setRole(Role.VOLUNTEER);
            dbFound.setDeleted(false);
            return volunteerRepository.save(dbFound);
        }
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

    public Volunteer updateEmail(Long id, String email) {
        Volunteer volunteer = volunteerRepository.findById(id).orElseThrow(()-> new UserNotFoundException("User not found." + id));
        Volunteer userFoundThroughEmail = volunteerRepository.findByEmail(email).orElse(null);
        if(userFoundThroughEmail!=null && !userFoundThroughEmail.isDeleted()) {
            throw new UserAlreadyExistsException("Email already in use.", userFoundThroughEmail.getEmail());
        }
        volunteer.setEmail(email);
        return volunteer;
    }

    public void deleteVolunteer(Long id) {
        Volunteer volunteer = volunteerRepository.findById(id).orElseThrow(()-> new UserNotFoundException("Cannot delete volunteer. Volunteer not found: " + id));
        volunteer.setDeleted(true);
        volunteer.setEmail(volunteer.getEmail()+ id);
    }
}
