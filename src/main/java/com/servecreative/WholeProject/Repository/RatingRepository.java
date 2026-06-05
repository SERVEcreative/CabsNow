package com.servecreative.WholeProject.Repository;

import com.servecreative.WholeProject.Model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Integer> {
    Optional<Rating> findByDutyId(int dutyId);
    List<Rating> findByDriverId(int driverId);

    @Query("SELECT AVG(r.stars) FROM Rating r WHERE r.driverId = :driverId")
    Double averageStarsByDriverId(int driverId);
}
