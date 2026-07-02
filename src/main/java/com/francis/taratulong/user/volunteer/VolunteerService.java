package com.francis.taratulong.user.volunteer;


import com.francis.taratulong.exception.UserAlreadyExistsException;
import com.francis.taratulong.exception.UserNotFoundException;
import com.francis.taratulong.user.Role;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class VolunteerService {
    private final VolunteerRepository volunteerRepository;

    public VolunteerService(VolunteerRepository volunteerRepository) {
        this.volunteerRepository = volunteerRepository;
    }

    public Volunteer saveVolunteer(Volunteer volunteer) {
        Volunteer dbFound = volunteerRepository.findByEmail(volunteer.getEmail()).orElse(null);
        if(dbFound != null && !dbFound.isDeleted()) {
            throw new UserAlreadyExistsException("Cannot create account. Email already in use.", volunteer.getEmail());
        }
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
