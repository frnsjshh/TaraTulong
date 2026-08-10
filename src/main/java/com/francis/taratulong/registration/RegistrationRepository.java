package com.francis.taratulong.registration;

import com.francis.taratulong.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    boolean existsByVolunteerIdAndEventId(Long volunteerId, Long eventId);

    @Query(
            value = "SELECT r FROM Registration r " +
                    "JOIN FETCH r.volunteer " +
                    "JOIN FETCH r.event " +
                    "WHERE r.event.id = :eventId",
            countQuery = "SELECT count(r) FROM Registration r WHERE r.event.id = :eventId"
    )
    Page<Registration> findAllByEventIdWithDetails(
            @Param("eventId") Long eventId,
            Pageable pageable
    );


    @Query(
            value = "SELECT r FROM Registration r " +
                    "JOIN FETCH r.volunteer " +
                    "JOIN FETCH r.event " +
                    "WHERE r.volunteer.id = :volunteerId " +
                    "AND (:status IS NULL OR r.registrationStatus = :status)",
            countQuery = "SELECT count(r) FROM Registration r " +
                    "WHERE r.volunteer.id = :volunteerId " +
                    "AND (:status IS NULL OR r.registrationStatus = :status)"
    )
    //the status param dictates whether to return approved or pending etc. registrations
    Page<Registration> findAllByVolunteerIdWithDetails(
            @Param("volunteerId") Long volunteerId,
            @Param("status") Status status,
            Pageable pageable
    );


}
