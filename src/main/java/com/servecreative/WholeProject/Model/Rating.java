package com.servecreative.WholeProject.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ratings")
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int ratingId;

    @Column(nullable = false, unique = true)
    private int dutyId;

    @Column(nullable = false)
    private int riderId;

    @Column(nullable = false)
    private int driverId;

    @Column(nullable = false)
    private int stars;

    @Column(length = 500)
    private String comment;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public int getRatingId() { return ratingId; }
    public void setRatingId(int ratingId) { this.ratingId = ratingId; }
    public int getDutyId() { return dutyId; }
    public void setDutyId(int dutyId) { this.dutyId = dutyId; }
    public int getRiderId() { return riderId; }
    public void setRiderId(int riderId) { this.riderId = riderId; }
    public int getDriverId() { return driverId; }
    public void setDriverId(int driverId) { this.driverId = driverId; }
    public int getStars() { return stars; }
    public void setStars(int stars) { this.stars = stars; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
