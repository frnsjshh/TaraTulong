package com.francis.taratulong.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    Page<Event> findByOrganizerId(Long organizerId, Pageable pageable);

    /**
     * Searches events with optional filters for location hierarchy and categories.
     * <p>
     * Location filtering is hierarchical:
     * - locationId: matches exact city/municipality
     * - provinceId: matches events in that province or any city/municipality under it
     * - regionId: matches events in that region, or any province under it, or any city under those provinces
     * <p>
     * Category filtering uses OR logic: event must have at least one of the specified categories.
     * All filters are optional (null = not applied) and combined with AND.
     */
    @Query("""
        SELECT DISTINCT e FROM Event e
        LEFT JOIN e.categories c
        LEFT JOIN e.location loc
        LEFT JOIN loc.parent locParent
        LEFT JOIN locParent.parent locGrandParent
        WHERE (:locationId IS NULL OR loc.id = :locationId)
        AND (:provinceId IS NULL OR loc.id = :provinceId OR locParent.id = :provinceId)
        AND (:regionId IS NULL OR loc.id = :regionId OR locParent.id = :regionId OR locGrandParent.id = :regionId)
        AND (:categoryNames IS NULL OR c.name IN :categoryNames)
    """)
    Page<Event> searchEvents(
            @Param("locationId") UUID locationId,
            @Param("provinceId") UUID provinceId,
            @Param("regionId") UUID regionId,
            @Param("categoryNames") List<String> categoryNames,
            Pageable pageable
    );
}
