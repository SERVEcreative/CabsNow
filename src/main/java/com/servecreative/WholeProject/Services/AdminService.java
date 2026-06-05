package com.servecreative.WholeProject.Services;

import com.servecreative.WholeProject.DTO.AdminLoginRequest;
import com.servecreative.WholeProject.DTO.AdminStatsResponse;
import com.servecreative.WholeProject.DTO.AuthResponse;
import com.servecreative.WholeProject.Model.Admin;
import com.servecreative.WholeProject.Model.Duty;
import com.servecreative.WholeProject.Model.Payment;
import com.servecreative.WholeProject.Repository.*;
import com.servecreative.WholeProject.Utils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final DutyRepository dutyRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AdminService(AdminRepository adminRepository, UserRepository userRepository,
                        DriverRepository driverRepository, DutyRepository dutyRepository,
                        PaymentRepository paymentRepository, PasswordEncoder passwordEncoder,
                        JwtUtil jwtUtil) {
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
        this.dutyRepository = dutyRepository;
        this.paymentRepository = paymentRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse login(AdminLoginRequest request) {
        Admin admin = adminRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtUtil.generateToken(admin.getAdminId(), "ADMIN", admin.getEmail());
        return new AuthResponse(token, "ADMIN", admin.getAdminId(), "Admin", admin.getEmail(), null);
    }

    public AdminStatsResponse getStats() {
        long users = userRepository.count();
        long drivers = driverRepository.count();
        long duties = dutyRepository.count();
        long completed = dutyRepository.findByStatus(Duty.DutyStatus.COMPLETED).size();
        double revenue = paymentRepository.findAll().stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.PAID)
                .mapToDouble(Payment::getAmount)
                .sum();
        long pendingPayments = paymentRepository.findAll().stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.PENDING)
                .count();
        return new AdminStatsResponse(users, drivers, duties, completed, revenue, pendingPayments);
    }
}
