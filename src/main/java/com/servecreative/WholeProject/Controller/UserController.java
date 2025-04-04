package com.servecreative.WholeProject.Controller;

import com.servecreative.WholeProject.Model.*;
import com.servecreative.WholeProject.Services.RiderService;
import com.servecreative.WholeProject.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RiderService riderService;

    @PostMapping("/signup")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        try {
            User savedUser = userService.saveUser(user);
            Rider rider = new Rider(savedUser.getId(), savedUser.getFirstName() + " " + savedUser.getLastName(), savedUser.getPhone());
            riderService.saveRider(rider);
            return ResponseEntity.ok(savedUser); // Return saved user
        } catch (Exception e) {
            // Log the exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Return null on error
        }
    }

    @GetMapping("/login")
    public ResponseEntity<User> getUserByEmailPassword(@RequestParam String email,
                                                       @RequestParam String password) {
        User user = userService.getUserByEmailAndPassword(email, password);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }


    @GetMapping("/{email}")
    public ResponseEntity<Object> getUserByEmail(@PathVariable String email) {
        User user = userService.getUserByEmail(email);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
}

