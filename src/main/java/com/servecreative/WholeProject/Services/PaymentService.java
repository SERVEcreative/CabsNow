package com.servecreative.WholeProject.Services;

import com.servecreative.WholeProject.Model.Duty;
import com.servecreative.WholeProject.Model.Payment;
import com.servecreative.WholeProject.Repository.DutyRepository;
import com.servecreative.WholeProject.Repository.PaymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final DutyRepository dutyRepository;

    public PaymentService(PaymentRepository paymentRepository, DutyRepository dutyRepository) {
        this.paymentRepository = paymentRepository;
        this.dutyRepository = dutyRepository;
    }

    public Payment createPayment(int dutyId, int riderId) {
        Duty duty = dutyRepository.findById(dutyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Duty not found"));

        if (duty.getRider().getRiderId() != riderId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your ride");
        }

        paymentRepository.findByDuty_DutyId(dutyId).ifPresent(p -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Payment already exists");
        });

        Payment payment = new Payment();
        payment.setDuty(duty);
        payment.setAmount(duty.getFare());
        payment.setStatus(Payment.PaymentStatus.PENDING);
        return paymentRepository.save(payment);
    }

    public Payment confirmPayment(int dutyId, int riderId) {
        Duty duty = dutyRepository.findById(dutyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Duty not found"));

        if (duty.getRider().getRiderId() != riderId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your ride");
        }

        Payment payment = paymentRepository.findByDuty_DutyId(dutyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));

        payment.setStatus(Payment.PaymentStatus.PAID);
        payment.setTransactionRef("MOCK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return paymentRepository.save(payment);
    }

    public Payment getPayment(int dutyId) {
        return paymentRepository.findByDuty_DutyId(dutyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
    }
}
