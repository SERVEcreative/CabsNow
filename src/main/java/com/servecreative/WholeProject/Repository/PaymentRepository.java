package com.servecreative.WholeProject.Repository;

import com.servecreative.WholeProject.Model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    Optional<Payment> findByDuty_DutyId(int dutyId);
}
