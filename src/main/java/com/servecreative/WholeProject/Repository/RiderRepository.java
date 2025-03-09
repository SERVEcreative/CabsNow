package com.servecreative.WholeProject.Repository;


import com.servecreative.WholeProject.Model.Rider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RiderRepository extends JpaRepository<Rider,Integer> {

    Optional<Rider> findById(Integer riderId);
}
