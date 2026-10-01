package com.francis.taratulong.location;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {

    List<Location> findByType(LocationType type);

    List<Location> findByParentId(UUID parentId);

    List<Location> findByParentIdAndType(UUID parentId, LocationType type);

    Optional<Location> findByCode(String code);

    List<Location> findByCodeIn(Collection<String> codes);
}
