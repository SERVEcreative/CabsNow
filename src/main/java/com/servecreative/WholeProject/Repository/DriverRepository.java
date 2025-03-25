package com.servecreative.WholeProject.Repository;

import com.servecreative.WholeProject.Model.Driver;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Integer> {
    List<Driver> findByStatus(String status);

    @Modifying
    @Transactional
    @Query("UPDATE Driver dr SET dr.status = 'ON_DUTY' WHERE dr.driverId = :driverId")
    int updateDriverToOnduty(@Param("driverId") int driverId);

    @Modifying
    @Transactional
    @Query("UPDATE Driver dr SET dr.status = 'AVAILABLE' WHERE dr.driverId = :driverId")
    int updateDriverToAvailable(@Param("driverId") int driverId);

    Optional<Driver> findByPhoneNumber(String phoneNumber);
    boolean existsByPhoneNumber(String phoneNumber);
}
