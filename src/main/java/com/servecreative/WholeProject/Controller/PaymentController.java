package com.servecreative.WholeProject.Controller;

import com.servecreative.WholeProject.DTO.PaymentConfirmRequest;
import com.servecreative.WholeProject.Model.Payment;
import com.servecreative.WholeProject.Services.PaymentService;
import com.servecreative.WholeProject.securityConfig.AuthenticatedUser;
import com.servecreative.WholeProject.securityConfig.SecurityHelper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final SecurityHelper securityHelper;

    public PaymentController(PaymentService paymentService, SecurityHelper securityHelper) {
        this.paymentService = paymentService;
        this.securityHelper = securityHelper;
    }

    @PostMapping("/create/{dutyId}")
    public ResponseEntity<Payment> create(@PathVariable int dutyId) {
        AuthenticatedUser rider = securityHelper.requireRider();
        return ResponseEntity.ok(paymentService.createPayment(dutyId, rider.id()));
    }

    @PostMapping("/confirm")
    public ResponseEntity<Payment> confirm(@Valid @RequestBody PaymentConfirmRequest request) {
        AuthenticatedUser rider = securityHelper.requireRider();
        return ResponseEntity.ok(paymentService.confirmPayment(request.getDutyId(), rider.id()));
    }

    @GetMapping("/{dutyId}")
    public ResponseEntity<Payment> get(@PathVariable int dutyId) {
        securityHelper.requireRider();
        return ResponseEntity.ok(paymentService.getPayment(dutyId));
    }
}
