package com.francis.taratulong.registration;

import com.francis.taratulong.Status;
import com.francis.taratulong.event.Event;
import com.francis.taratulong.event.EventService;
import com.francis.taratulong.exception.*;
import com.francis.taratulong.user.volunteer.VolunteerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RegistrationService {
    private final RegistrationRepository registrationRepository;
    private final VolunteerService volunteerService;
    private final EventService eventService;


    public Registration saveRegistration(Long volunteerId, Long eventId) {
        log.debug("Attempting registration: volunteerId={}, eventId={}", volunteerId, eventId);
        Registration registration = new Registration();
        Event event = eventService.getEvent(eventId);
        if(LocalDateTime.now().isAfter(event.getCutOffTime())){
            log.warn("Registration denied: event {} cutoff has passed", eventId);
            throw new EventRegistrationClosed("Registration for this event has closed.");
        }
        if(event.getSlotsAvailable()<=0){
            log.warn("Registration denied: event {} has no slots available", eventId);
            throw new EventRegistrationClosed("No slots available for this event.");
        }
        if(registrationRepository.existsByVolunteerIdAndEventId(volunteerId,eventId)) {
            log.warn("Registration denied: volunteer {} already registered for event {}", volunteerId, eventId);
            throw new VolunteerAlreadyRegisteredException("Error! User already registered for this event.");
        }
        registration.setVolunteer(volunteerService.getVolunteer(volunteerId));
        registration.setEvent(event);
        Registration saved = registrationRepository.save(registration);
        log.info("Registration created: id={}, volunteerId={}, eventId={}", saved.getId(), volunteerId, eventId);
        return saved;
    }

    public Registration getRegistration(Long id) {
        return registrationRepository.findById(id).orElseThrow(()-> new RegistrationNotFoundException("Registration not found."));
    }

    public Page<Registration> getRegistrationsForEvent(Long eventId, Long orgId, Pageable pageable) {
        verifyOrgOwnershipToEvent(orgId, eventId);
        return registrationRepository.findAllByEventIdWithDetails(eventId, pageable);
    }



    public Page<Registration> getRegistrationsForVolunteer(Long volunteerId, Status status, Pageable pageable) {
        return registrationRepository.findAllByVolunteerIdWithDetails(volunteerId, status, pageable);
    }

    //UPDATE ATTENDANCE AS ORG
    // will not accept PENDING as a new status
    public void updateAttendanceStatus(Long eventId, Long org, AttendanceStatus newStatus) {
        log.debug("Updating attendance: registrationId={}, newStatus={}", eventId, newStatus);
        Registration registration = getRegistration(eventId);
        verifyOrgOwnershipToEvent(org, eventId);
        if(!isApproved(registration)) throw new RegistrationConflictException("Cannot update registration status. Registration not approved.");
        AttendanceStatus currentStatus = registration.getAttendanceStatus();
        if(currentStatus.equals(newStatus)) return;

        pointShiftAndRegistrationCounter(newStatus, currentStatus, registration);
        registration.setAttendanceStatus(newStatus);
        log.info("Attendance updated: registrationId={}, {} -> {}", eventId, currentStatus, newStatus);
    }

    public AttendanceStatus cancelRegistration(Long id, Long volunteerId) {
        log.debug("Attempting cancellation: registrationId={}, volunteerId={}", id, volunteerId);
        Registration registrationDb = getRegistration(id);
        if(!registrationDb.getVolunteer().getId().equals(volunteerId)) throw new UnauthorizedAccessException("Cannot cancel someone else's registration");
        if(!isApproved(registrationDb)) throw new RegistrationConflictException("Cannot cancel registration. Registration not approved.");

        LocalDateTime eventStartDateTime = registrationDb.getEvent().getStartDateTime();
        LocalDateTime now = LocalDateTime.now();
        AttendanceStatus cancelStatus = now.plusHours(48).isBefore(eventStartDateTime)
                ? AttendanceStatus.CANCELLED_EARLY
                : AttendanceStatus.CANCELLED_LATE;

        pointShiftAndRegistrationCounter(cancelStatus, registrationDb.getAttendanceStatus(), registrationDb);
        registrationDb.setAttendanceStatus(cancelStatus);
        log.info("Registration cancelled: id={}, status={}", id, cancelStatus);
        return cancelStatus;
    }

    public void pointShiftAndRegistrationCounter(AttendanceStatus newStatus, AttendanceStatus currentStatus, Registration registration) {
        Long volunteerId = registration.getVolunteer().getId();
        //point delta algorithm
        int pointShift = newStatus.getPointValue() - currentStatus.getPointValue();
        volunteerService.updateTrustScore(registration.getVolunteer().getId(), pointShift);

        //not yet updated
        if(currentStatus.equals(AttendanceStatus.PENDING)) volunteerService.updateTotalRegistrations(registration.getVolunteer().getId(), 1);
        boolean wasPresent = currentStatus.equals(AttendanceStatus.PRESENT);
        boolean isPresent = newStatus.equals(AttendanceStatus.PRESENT);

        //wasn't present, is now present
        if(!wasPresent && isPresent) volunteerService.updateEventsAttended(volunteerId, 1);
        else if(wasPresent && !isPresent) volunteerService.updateEventsAttended(volunteerId, -1);
        // If they go from NO_SHOW to CANCELLED_LATE, neither boolean is true. It safely does nothing!
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
        log.debug("Attempting to approve registration: id={}, orgId={}", id, orgId);
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
        log.info("Registration approved: id={}, remainingSlots={}", id, event.getSlotsAvailable());
    }

    public void setRejected(Long id, Long orgId){
        log.debug("Attempting to reject registration: id={}, orgId={}", id, orgId);
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
        log.info("Registration rejected: id={}", id);
    }

    public void deleteRegistration(Long id, Long volunteerId) {
        log.debug("Attempting to delete registration: id={}, volunteerId={}", id, volunteerId);
        Registration registrationDb = registrationRepository.findById(id).orElseThrow(()->new RegistrationNotFoundException("Cannot delete registration. Registration not found."));
        if(!Objects.equals(volunteerId, registrationDb.getVolunteer().getId())) throw new UnauthorizedAccessException ("Cannot delete registration. Unauthorized");
        if(isApproved(registrationDb)){
            Event event = registrationDb.getEvent();
            event.setSlotsAvailable(event.getSlotsAvailable()+1);
        }
        registrationRepository.deleteById(id);
        log.info("Registration deleted: id={}", id);
    }


    private void verifyOrgOwnershipToRegistration(Registration registration, Long orgId) {
        if (!registration.getEvent().getOrganizer().getId().equals(orgId)) {
            log.warn("Unauthorized access: orgId={} tried to act on registration for event owned by orgId={}", orgId, registration.getEvent().getOrganizer().getId());
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
