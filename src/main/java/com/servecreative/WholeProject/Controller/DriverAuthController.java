package com.servecreative.WholeProject.Controller;

import com.servecreative.WholeProject.DTO.DriverLoginRequest;
import com.servecreative.WholeProject.DTO.DriverSignupRequest;
import com.servecreative.WholeProject.Services.DriverAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/driver/auth")
public class DriverAuthController {

    private final DriverAuthService driverAuthService;

    public DriverAuthController(DriverAuthService driverAuthService) {
        this.driverAuthService = driverAuthService;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody DriverSignupRequest request) {
        return ResponseEntity.ok(driverAuthService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody DriverLoginRequest request) {
        return ResponseEntity.ok(driverAuthService.login(request));
    }
}
