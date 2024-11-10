package com.servecreative.WholeProject.Repository;

import com.servecreative.WholeProject.Model.Driver;
import com.servecreative.WholeProject.Model.Duty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

}
