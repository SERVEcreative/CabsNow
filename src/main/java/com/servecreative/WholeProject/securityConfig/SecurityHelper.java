package com.servecreative.WholeProject.securityConfig;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class SecurityHelper {

    public AuthenticatedUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return user;
    }

    public void assertRider(int riderId) {
        AuthenticatedUser user = currentUser();
        if (!user.isRider() || user.id() != riderId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to access this rider resource");
        }
    }

    public void assertDriver(int driverId) {
        AuthenticatedUser user = currentUser();
        if (!user.isDriver() || user.id() != driverId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to access this driver resource");
        }
    }

    public AuthenticatedUser requireRider() {
        AuthenticatedUser user = currentUser();
        if (!user.isRider()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Rider role required");
        }
        return user;
    }

    public AuthenticatedUser requireDriver() {
        AuthenticatedUser user = currentUser();
        if (!user.isDriver()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Driver role required");
        }
        return user;
    }

    public AuthenticatedUser requireAdmin() {
        AuthenticatedUser user = currentUser();
        if (!user.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin role required");
        }
        return user;
    }
}
