package com.servecreative.WholeProject.Repository;

import com.servecreative.WholeProject.Model.Driver;
import com.servecreative.WholeProject.Model.Duty;
import com.servecreative.WholeProject.Model.Rider;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DutyRepository extends JpaRepository<Duty, Integer> {
    List<Duty> findByStatus(Duty.DutyStatus status);

    @Query("SELECT d FROM Duty d WHERE d.assignedDriver.driverId = :driverId")
    List<Duty> findDutiesByDriverId(int driverId);

    Optional<Duty> findById(int integer);

    List<Duty> findByAssignedDriverAndStatus(Driver driver, Duty.DutyStatus status);

    //-------------Rider related task-----------------//

    @Query("SELECT d FROM Duty d WHERE d.rider.riderId = :riderId AND d.status = :status")
     Duty findDutiesByRiderIdAndStatus(@Param("riderId") int riderId, @Param("status") Duty.DutyStatus status);

    @Query("SELECT d FROM Duty d WHERE d.assignedDriver.driverId = :driverId AND d.status = :status")
    List<Duty> findDutiesByDriverIdAndStatus(@Param("driverId") int driverId, @Param("status") Duty.DutyStatus status);

    @Query("SELECT d FROM Duty d WHERE d.status = :status AND (d.rider.riderId = :id OR d.assignedDriver.driverId = :id)")
    List<Duty> findDutiesByStatusAndEitherRiderOrDriver(@Param("status") Duty.DutyStatus status, @Param("id") int id);

    @Query("SELECT d FROM Duty d WHERE d.rider.riderId = :riderId ORDER BY d.createdAt DESC")
    List<Duty> findDutiesByRider(@Param("riderId") int riderId);

    //-------------Driver related task-----------------//

    @Query("SELECT d FROM Duty d WHERE d.status = 'PENDING' AND d.assignedDriver IS NULL")
    List<Duty> findAvailableDuties();

    // Accept Ride
    @Modifying
    @Transactional
    @Query("UPDATE Duty d SET d.status = 'ACCEPTED', d.assignedDriver = (SELECT dr FROM Driver dr WHERE dr.driverId = :driverId) WHERE d.dutyId = :dutyId AND d.status = 'PENDING'")
    int acceptDuty(@Param("dutyId") int dutyId, @Param("driverId") int driverId);

    // Ignore Ride (Keep it pending for other drivers)
    @Modifying
    @Transactional
    @Query("UPDATE Duty d SET d.status = 'PENDING', d.assignedDriver = NULL " +
            "WHERE d.dutyId = :dutyId AND d.status = 'ACCEPTED' AND d.assignedDriver IS NOT NULL " +
            "AND d.assignedDriver.driverId = :driverId")
    int ignoreDuty(@Param("dutyId") int dutyId, @Param("driverId") int driverId);

    // Complete Ride
    @Modifying
    @Transactional
    @Query("UPDATE Duty d SET d.status = 'COMPLETED' WHERE d.dutyId = :dutyId AND d.assignedDriver.driverId = :driverId AND d.status = 'ACCEPTED'")
    int completeDuty(@Param("dutyId") int dutyId, @Param("driverId") int driverId);


}
