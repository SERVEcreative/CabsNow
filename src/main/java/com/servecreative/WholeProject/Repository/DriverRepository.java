package com.servecreative.WholeProject.Repository;

import com.servecreative.WholeProject.Model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Integer> {
    List<Driver> findByStatus(String status);
}
