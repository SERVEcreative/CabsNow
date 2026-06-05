package com.servecreative.WholeProject.securityConfig;

public record AuthenticatedUser(int id, String role) {

    public boolean isRider() {
        return "RIDER".equals(role);
    }

    public boolean isDriver() {
        return "DRIVER".equals(role);
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
