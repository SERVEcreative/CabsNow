package com.servecreative.WholeProject.Repository;

import com.servecreative.WholeProject.Model.RideEventLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RideEventLogRepository extends JpaRepository<RideEventLog, Long> {

    List<RideEventLog> findByDutyIdOrderByCreatedAtAsc(int dutyId);

    Optional<RideEventLog> findByEventId(String eventId);

    boolean existsByEventId(String eventId);
}
