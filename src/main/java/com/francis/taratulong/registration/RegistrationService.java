package com.francis.taratulong.registration;

import com.francis.taratulong.Status;
import com.francis.taratulong.event.Event;
import com.francis.taratulong.event.EventService;
import com.francis.taratulong.exception.*;
import com.francis.taratulong.user.volunteer.VolunteerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class RegistrationService {
    private final RegistrationRepository registrationRepository;
    private final VolunteerService volunteerService;
    private final EventService eventService;


    public Registration saveRegistration(Long volunteerId, Long eventId) {
        Registration registration = new Registration();
        Event event = eventService.getEvent(eventId);
        if(LocalDateTime.now().isAfter(event.getCutOffTime())){
            throw new EventRegistrationClosed("Registration for this event has closed.");
        }
        if(event.getSlotsAvailable()<=0){
            throw new EventRegistrationClosed("No slots available for this event.");
        }
        if(registrationRepository.existsByVolunteerIdAndEventId(volunteerId,eventId)) {
            throw new VolunteerAlreadyRegisteredException("Error! User already registered for this event.");
        }
        registration.setVolunteer(volunteerService.getVolunteer(volunteerId));
        registration.setEvent(event);
        return registrationRepository.save(registration);
    }

    public Registration getRegistration(Long id) {
        return registrationRepository.findById(id).orElseThrow(()-> new RegistrationNotFoundException("Registration not found."));
    }

    public Page<Registration> getRegistrationsForEvent(Long eventId, Long ordId, Pageable pageable) {
        verifyOrgOwnershipToEvent(ordId, eventId);
        return registrationRepository.findAllByEventIdWithDetails(eventId, pageable);
    }


    public void setPresent(Long id, Long orgId) {
        Registration registrationDb = registrationRepository.findById(id).orElseThrow(()-> new RegistrationNotFoundException("Cannot set registration as present. Registration not found."));
        verifyOrgOwnershipToRegistration(registrationDb, orgId);
        if(!isApproved(registrationDb)) throw new RegistrationConflictException("Action denied: Attendance requires an approved registration status.");

        //check if attendance is already marked as present
        if(Boolean.TRUE.equals(registrationDb.getParticipated())) throw new RegistrationConflictException("Action denied: Registration already marked as present.");


        if(registrationDb.getParticipated()==null) {
            volunteerService.updateEventsAttended(registrationDb.getVolunteer().getId(), 1); //adding 1 to volunteer's total events attended'
            volunteerService.updateTotalRegistrations(registrationDb.getVolunteer().getId(), 1); //adding 1 to volunteer's total registrations'
        } else {
            volunteerService.updateEventsAttended(registrationDb.getVolunteer().getId(), 1);
        }

        registrationDb.setParticipated(true);
    }

    public void setNotPresent(Long id, Long orgId) {
        Registration registrationDb = registrationRepository.findById(id).orElseThrow(()-> new RegistrationNotFoundException("Cannot set registration as present. Registration not found."));
        verifyOrgOwnershipToRegistration(registrationDb, orgId);

        if(!isApproved(registrationDb)) throw new RegistrationConflictException("Action denied: Attendance requires an approved registration status.");

        //check if attendance is already marked as not present
        if(Boolean.FALSE.equals(registrationDb.getParticipated())) throw new RegistrationConflictException("Action denied: Registration already marked as not present.");

        if(registrationDb.getParticipated()==null) {
            volunteerService.updateTotalRegistrations(registrationDb.getVolunteer().getId(), 1);
            //not adding 1 to volunteer's total events attended, volunteers absent
        } else {
            volunteerService.updateEventsAttended(registrationDb.getVolunteer().getId(), -1);
        }
        registrationDb.setParticipated(false);
    }

    public Registration setRatingAndFeedback(Long id, int rating, String feedback, Long orgId) {
        if(rating<1 || rating>5) {
            throw new RegistrationConflictException("Rating must be between 1 and 5.");
        }
        Registration registrationDb = registrationRepository.findById(id).orElseThrow(()-> new RegistrationNotFoundException("Cannot set rating. Registration not found."));
        verifyOrgOwnershipToRegistration(registrationDb, orgId);
        if(!isApproved(registrationDb)) throw new RegistrationConflictException("Cannot rate registration. Registration not approved.");
        registrationDb.setRating(rating);
        registrationDb.setFeedback(feedback==null || feedback.isBlank() ? "No feedback" : feedback);
        return registrationDb;
    }

    public void setApproved(Long id, Long orgId){
        Registration registrationDb = registrationRepository.findById(id).orElseThrow(()-> new RegistrationNotFoundException("Cannot approve registration. Registration not found."));
        Event event = registrationDb.getEvent();
        verifyOrgOwnershipToRegistration(registrationDb, orgId);

        if(isApproved(registrationDb)){
            throw new RegistrationConflictException("Registration already approved.");
        }
        if(event.getSlotsAvailable()<=0){
            throw new EventRegistrationClosed("No slots available for this event.");
        }
        registrationDb.setRegistrationStatus(Status.APPROVED);
        event.setSlotsAvailable(event.getSlotsAvailable()-1);
    }

    public void setRejected(Long id, Long orgId){
        Registration registrationDb = registrationRepository.findById(id).orElseThrow(()-> new RegistrationNotFoundException("Cannot set registration as present. Registration not found."));
        verifyOrgOwnershipToRegistration(registrationDb, orgId);

        if(isRejected(registrationDb)){
            throw new RegistrationConflictException("Registration already rejected.");
        }

        if(isApproved(registrationDb)){
            Event event = registrationDb.getEvent();
            event.setSlotsAvailable(event.getSlotsAvailable()+1);
        }
        registrationDb.setRegistrationStatus(Status.REJECTED);


    }

    public void deleteRegistration(Long id, Long volunteerId) {
        Registration registrationDb = registrationRepository.findById(id).orElseThrow(()->new RegistrationNotFoundException("Cannot delete registration. Registration not found."));
        if(!Objects.equals(volunteerId, registrationDb.getVolunteer().getId())) throw new UnauthorizedAccessException ("Cannot delete registration. Unauthorized");
        if(isApproved(registrationDb)){
            Event event = registrationDb.getEvent();
            event.setSlotsAvailable(event.getSlotsAvailable()+1);
        }
        registrationRepository.deleteById(id);
    }


    private void verifyOrgOwnershipToRegistration(Registration registration, Long orgId) {
        if (!registration.getEvent().getOrganizer().getId().equals(orgId)) {
            throw new UnauthorizedAccessException("Unauthorized action for this event.");
        }
    }

    private void verifyOrgOwnershipToEvent(Long orgId, Long eventId) {
        if(!eventService.getOrganizer(eventId).equals(orgId))
            throw new UnauthorizedAccessException("Unauthorized");
    }

    private boolean isApproved(Registration registration) {
        return registration.getRegistrationStatus()==Status.APPROVED;
    }
    private boolean isRejected(Registration registration) {
        return registration.getRegistrationStatus()==Status.REJECTED;
    }
}
