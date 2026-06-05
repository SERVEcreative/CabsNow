package com.servecreative.WholeProject.DTO;

import jakarta.validation.constraints.NotBlank;

public class DriverLoginRequest {
    @NotBlank
    private String phoneNumber;

    @NotBlank
    private String password;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
