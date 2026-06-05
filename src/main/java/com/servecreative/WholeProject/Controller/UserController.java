package com.servecreative.WholeProject.Controller;

import com.servecreative.WholeProject.DTO.AuthResponse;
import com.servecreative.WholeProject.DTO.UserLoginRequest;
import com.servecreative.WholeProject.DTO.UserResponse;
import com.servecreative.WholeProject.DTO.UserSignupRequest;
import com.servecreative.WholeProject.Services.UserService;
import com.servecreative.WholeProject.securityConfig.AuthenticatedUser;
import com.servecreative.WholeProject.securityConfig.SecurityHelper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final SecurityHelper securityHelper;

    public UserController(UserService userService, SecurityHelper securityHelper) {
        this.userService = userService;
        this.securityHelper = securityHelper;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody UserSignupRequest request) {
        return ResponseEntity.ok(userService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody UserLoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        AuthenticatedUser user = securityHelper.requireRider();
        return ResponseEntity.ok(userService.getUserById(user.id()));
    }

    @GetMapping("/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        AuthenticatedUser user = securityHelper.requireRider();
        UserResponse profile = userService.getUserByEmail(email);
        if (user.id() != profile.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to view this profile");
        }
        return ResponseEntity.ok(profile);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable int id) {
        securityHelper.assertRider(id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
