package com.francis.taratulong.user.organization;

import com.francis.taratulong.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrgRepository extends JpaRepository<Org, Long> {

    Optional<Org> findByEmail(String email);

    @Query(
            value = "SELECT o FROM Org o LEFT JOIN FETCH o.approvedBy WHERE (:status IS NULL OR o.status = :status)",
            countQuery = "SELECT count(o) FROM Org o WHERE (:status IS NULL OR o.status = :status)"
    )
    Page<Org> findAllByStatus(
            @Param("status") Status status,
            Pageable pageable
    );
}
