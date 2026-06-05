package com.servecreative.WholeProject.Services;

import com.servecreative.WholeProject.DTO.AuthResponse;
import com.servecreative.WholeProject.DTO.UserLoginRequest;
import com.servecreative.WholeProject.DTO.UserResponse;
import com.servecreative.WholeProject.DTO.UserSignupRequest;
import com.servecreative.WholeProject.Model.Rider;
import com.servecreative.WholeProject.Model.User;
import com.servecreative.WholeProject.Repository.UserRepository;
import com.servecreative.WholeProject.Utils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RiderService riderService;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            RiderService riderService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.riderService = riderService;
    }

    public AuthResponse signup(UserSignupRequest request) {
        if (userRepository.findByEmail(request.getEmail()) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        User user = new User(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPhone(),
                passwordEncoder.encode(request.getPassword()));
        User savedUser = userRepository.save(user);

        Rider rider = new Rider(
                savedUser.getId(),
                savedUser.getFirstName() + " " + savedUser.getLastName(),
                savedUser.getPhone());
        riderService.saveRider(rider);

        return buildAuthResponse(savedUser);
    }

    public AuthResponse login(UserLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return buildAuthResponse(user);
    }

    public UserResponse getUserById(int id) {
        User user = userRepository.findById((long) id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return toResponse(user);
    }

    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return toResponse(user);
    }

    public void deleteUser(int id) {
        if (!userRepository.existsById((long) id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        userRepository.deleteById((long) id);
    }

    private AuthResponse buildAuthResponse(User user) {
        String fullName = user.getFirstName() + " " + user.getLastName();
        String token = jwtUtil.generateToken(user.getId(), "RIDER", user.getEmail());
        return new AuthResponse(token, "RIDER", user.getId(), fullName, user.getEmail(), user.getPhone());
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone());
    }
}
