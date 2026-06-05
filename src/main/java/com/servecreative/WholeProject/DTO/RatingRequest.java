package com.servecreative.WholeProject.DTO;

import jakarta.validation.constraints.*;

public class RatingRequest {
    @NotNull
    private Integer dutyId;

    @Min(1) @Max(5)
    private int stars;

    @Size(max = 500)
    private String comment;

    public Integer getDutyId() { return dutyId; }
    public void setDutyId(Integer dutyId) { this.dutyId = dutyId; }
    public int getStars() { return stars; }
    public void setStars(int stars) { this.stars = stars; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
