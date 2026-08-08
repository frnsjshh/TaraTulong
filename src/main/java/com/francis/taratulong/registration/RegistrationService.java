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

    public Page<Registration> getRegistrationsForEvent(Long eventId, Long orgId, Pageable pageable) {
        verifyOrgOwnershipToEvent(orgId, eventId);
        return registrationRepository.findAllByEventIdWithDetails(eventId, pageable);
    }

    //UPDATE ATTENDANCE AS ORG
    // will not accept PENDING as a new status
    public void updateAttendanceStatus(Long eventId, Long org, AttendanceStatus newStatus) {
        Registration registration = getRegistration(eventId);
        verifyOrgOwnershipToEvent(org, eventId);
        if(!isApproved(registration)) throw new RegistrationConflictException("Cannot update registration status. Registration not approved.");
        AttendanceStatus currentStatus = registration.getAttendanceStatus();
        if(currentStatus.equals(newStatus)) return;

        pointShiftAndRegistrationCounter(newStatus, currentStatus, registration);
        registration.setAttendanceStatus(newStatus);
    }

    public AttendanceStatus cancelRegistration(Long id, Long volunteerId) {
        Registration registrationDb = getRegistration(id);
        if(!registrationDb.getVolunteer().getId().equals(volunteerId)) throw new UnauthorizedAccessException("Cannot cancel someone else's registration");
        if(!isApproved(registrationDb)) throw new RegistrationConflictException("Cannot cancel registration. Registration not approved.");

        LocalDateTime eventStartDateTime = registrationDb.getEvent().getStartDateTime();
        LocalDateTime now = LocalDateTime.now();
        AttendanceStatus cancelStatus = now.plusHours(48).isBefore(eventStartDateTime)
                ? AttendanceStatus.CANCELLED_EARLY
                : AttendanceStatus.CANCELLED_LATE;

        pointShiftAndRegistrationCounter(cancelStatus, registrationDb.getAttendanceStatus(), registrationDb);
        return cancelStatus;
    }

    public void pointShiftAndRegistrationCounter(AttendanceStatus newStatus, AttendanceStatus currentStatus, Registration registration) {
        //point delta algorithm -2 - -25
        int pointShift = newStatus.getPointValue() - currentStatus.getPointValue();
        volunteerService.updateTrustScore(registration.getVolunteer().getId(), pointShift);

        //not yet updated
        if(currentStatus.equals(AttendanceStatus.PENDING)) {
            //increment total registrations
            volunteerService.updateTotalRegistrations(registration.getVolunteer().getId(), 1);
            if(newStatus.equals(AttendanceStatus.PRESENT)) volunteerService.updateEventsAttended(registration.getVolunteer().getId(), newStatus); //increments on if present, no decrement needed since it started as pending
        }
        //updating existing status
        //if both values are negative (e.g., NO_SHOW * CANCELLED_EARLY = -25 * -2), no change in events attended
        else if((currentStatus.getPointValue()*newStatus.getPointValue()) < 0) {
            //updates events attended depending on the status
            //if present increment, if anything else decrement
            volunteerService.updateEventsAttended(registration.getVolunteer().getId(), newStatus);
        }
        registrationRepository.save(registration);
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
