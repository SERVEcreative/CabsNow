package com.servecreative.WholeProject.Services;

import com.servecreative.WholeProject.DTO.AuthResponse;
import com.servecreative.WholeProject.DTO.DriverLoginRequest;
import com.servecreative.WholeProject.DTO.DriverSignupRequest;
import com.servecreative.WholeProject.Model.Driver;
import com.servecreative.WholeProject.Repository.DriverRepository;
import com.servecreative.WholeProject.Utils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DriverAuthService {

    private final DriverRepository driverRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public DriverAuthService(
            DriverRepository driverRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil) {
        this.driverRepository = driverRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse signup(DriverSignupRequest request) {
        if (driverRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone number already registered");
        }

        Driver driver = new Driver();
        driver.setName(request.getName());
        driver.setPhoneNumber(request.getPhoneNumber());
        driver.setVehicleNumber(request.getVehicleNumber());
        driver.setAadharNumber(request.getAadharNumber());
        driver.setPassword(passwordEncoder.encode(request.getPassword()));
        driver.setStatus(Driver.DriverStatus.AVAILABLE);

        Driver saved = driverRepository.save(driver);
        return buildAuthResponse(saved);
    }

    public AuthResponse login(DriverLoginRequest request) {
        Driver driver = driverRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), driver.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        return buildAuthResponse(driver);
    }

    private AuthResponse buildAuthResponse(Driver driver) {
        String token = jwtUtil.generateToken(driver.getDriverId(), "DRIVER", driver.getPhoneNumber());
        return new AuthResponse(
                token,
                "DRIVER",
                driver.getDriverId(),
                driver.getName(),
                null,
                driver.getPhoneNumber());
    }
}
