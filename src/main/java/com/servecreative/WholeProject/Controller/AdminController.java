package com.servecreative.WholeProject.Controller;

import com.servecreative.WholeProject.DTO.AdminLoginRequest;
import com.servecreative.WholeProject.DTO.AdminStatsResponse;
import com.servecreative.WholeProject.DTO.AuthResponse;
import com.servecreative.WholeProject.Services.AdminService;
import com.servecreative.WholeProject.securityConfig.SecurityHelper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final SecurityHelper securityHelper;

    public AdminController(AdminService adminService, SecurityHelper securityHelper) {
        this.adminService = adminService;
        this.securityHelper = securityHelper;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return ResponseEntity.ok(adminService.login(request));
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> stats() {
        securityHelper.requireAdmin();
        return ResponseEntity.ok(adminService.getStats());
    }
}
