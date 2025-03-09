package com.servecreative.WholeProject.Repository;

import com.servecreative.WholeProject.Model.Driver;
import com.servecreative.WholeProject.Model.Duty;
import com.servecreative.WholeProject.Model.Rider;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
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


    @Query("SELECT d FROM Duty d WHERE d.rider.riderId = :riderId AND d.status = :status")
     Duty findDutiesByRiderIdAndStatus(@Param("riderId") int riderId, @Param("status") Duty.DutyStatus status);

    @Query("SELECT d FROM Duty d WHERE d.assignedDriver.driverId = :driverId AND d.status = :status")
    List<Duty> findDutiesByDriverIdAndStatus(@Param("driverId") int driverId, @Param("status") Duty.DutyStatus status);

    @Query("SELECT d FROM Duty d WHERE d.status = :status AND (d.rider.riderId = :id OR d.assignedDriver.driverId = :id)")
    List<Duty> findDutiesByStatusAndEitherRiderOrDriver(@Param("status") Duty.DutyStatus status, @Param("id") int id);


}
