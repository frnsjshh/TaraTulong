package com.francis.taratulong.user.volunteer;


import com.francis.taratulong.exception.UserAlreadyExistsException;
import com.francis.taratulong.exception.UserNotFoundException;
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
        if(volunteerRepository.findByEmail(volunteer.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Cannot create account. Email already in use.", volunteer.getEmail());
        }
        return volunteerRepository.save(volunteer);
    }

    public Volunteer getVolunteer(Long id) {
        return volunteerRepository.findById(id).orElseThrow(() -> new UserNotFoundException("Volunteer not found" + id));
    }

    public Volunteer updateVolunteer(Long id, Volunteer volunteerRequest) {
        Volunteer volunteer = volunteerRepository.findById(id).orElseThrow(() -> new UserNotFoundException("Volunteer not found:" + id));
        volunteer.setFirstName(volunteerRequest.getFirstName());
        volunteer.setLastName(volunteerRequest.getLastName());
        return saveVolunteer(volunteer);
    }

    public Volunteer updateEmail(Long id, String email) {
        Volunteer volunteer = volunteerRepository.findById(id).orElseThrow(()-> new UserNotFoundException("User not found." + id));
        Volunteer userFoundThroughEmail = volunteerRepository.findByEmail(email).orElse(null);
        if(userFoundThroughEmail!=null && !userFoundThroughEmail.getIsDeleted()) {
            throw new UserAlreadyExistsException("Email already in use.", userFoundThroughEmail.getEmail());
        }
        volunteer.setEmail(email);
        return volunteerRepository.save(volunteer);
    }

    public void deleteVolunteer(Long id) {
        Volunteer volunteer = volunteerRepository.findById(id).orElseThrow(()-> new UserNotFoundException("Cannot delete volunteer. Volunteer not found: " + id));
        volunteer.setIsDeleted(true);
        volunteerRepository.save(volunteer);
    }
}
