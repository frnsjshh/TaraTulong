package com.francis.taratulong.registration;

import com.francis.taratulong.event.Event;
import com.francis.taratulong.event.EventService;
import com.francis.taratulong.exception.EventRegistrationClosed;
import com.francis.taratulong.exception.RegistrationNotFoundException;
import com.francis.taratulong.exception.VolunteerAlreadyRegisteredException;
import com.francis.taratulong.user.volunteer.VolunteerService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Transactional
public class RegistrationService {
    private RegistrationRepository registrationRepository;
    private VolunteerService volunteerService;
    private EventService eventService;


    public RegistrationService(RegistrationRepository registrationRepository, VolunteerService volunteerService, EventService eventService) {
        this.registrationRepository = registrationRepository;
        this.volunteerService = volunteerService;
        this.eventService = eventService;
    }

    public Registration saveRegistration(Long volunteerId, Long eventId) {
        Registration userRegistration = new Registration();
        Event event = eventService.getEvent(eventId);
        if(LocalDateTime.now().isAfter(event.getCutOffTime())){
            throw new EventRegistrationClosed("Registration for this event has closed.");
        }
        if(registrationRepository.existsByVolunteerIdAndEventId(volunteerId,eventId)) {
            throw new VolunteerAlreadyRegisteredException("Error! User already registered for this event.");
        }

        userRegistration.setVolunteer(volunteerService.getVolunteer(volunteerId));
        userRegistration.setEvent(event);
        return registrationRepository.save(userRegistration);
    }

    public Registration getRegistration(Long id) {
        return registrationRepository.findById(id).orElseThrow(()-> new RegistrationNotFoundException("Registration not found."));
    }

    public Registration updateRegistration(Long id, Registration registration){

    }
}
