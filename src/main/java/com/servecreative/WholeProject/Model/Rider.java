package com.servecreative.WholeProject.Model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "riders")
public class Rider {

    @Id
    private int riderId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @OneToMany(mappedBy = "rider", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference // Prevents infinite recursion by marking this as the managing side
    private List<Duty> duties;  // List of duties created by the rider

    // Getters and Setters

    // Default Constructor
    public Rider() {}

    // Constructor to set values manually
    public Rider(int riderId, String name, String phoneNumber) {
        this.riderId = riderId;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }
    public int getRiderId() {
        return riderId;
    }

    public void setRiderId(int riderId) {
        this.riderId = riderId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public List<Duty> getDuties() {
        return duties;
    }

    public void setDuties(List<Duty> duties) {
        this.duties = duties;
    }
}
