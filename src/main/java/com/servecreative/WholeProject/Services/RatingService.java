package com.servecreative.WholeProject.Services;

import com.servecreative.WholeProject.DTO.RatingRequest;
import com.servecreative.WholeProject.Model.Duty;
import com.servecreative.WholeProject.Model.Rating;
import com.servecreative.WholeProject.Repository.DutyRepository;
import com.servecreative.WholeProject.Repository.RatingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final DutyRepository dutyRepository;

    public RatingService(RatingRepository ratingRepository, DutyRepository dutyRepository) {
        this.ratingRepository = ratingRepository;
        this.dutyRepository = dutyRepository;
    }

    public Rating rateDriver(RatingRequest request, int riderId) {
        Duty duty = dutyRepository.findById(request.getDutyId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Duty not found"));

        if (duty.getRider().getRiderId() != riderId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your ride");
        }
        if (duty.getStatus() != Duty.DutyStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ride must be completed before rating");
        }
        if (duty.getAssignedDriver() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No driver assigned");
        }

        ratingRepository.findByDutyId(duty.getDutyId()).ifPresent(r -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already rated");
        });

        Rating rating = new Rating();
        rating.setDutyId(duty.getDutyId());
        rating.setRiderId(riderId);
        rating.setDriverId(duty.getAssignedDriver().getDriverId());
        rating.setStars(request.getStars());
        rating.setComment(request.getComment());
        return ratingRepository.save(rating);
    }

    public Map<String, Object> getDriverRatingSummary(int driverId) {
        List<Rating> ratings = ratingRepository.findByDriverId(driverId);
        Double avg = ratingRepository.averageStarsByDriverId(driverId);
        Map<String, Object> summary = new HashMap<>();
        summary.put("driverId", driverId);
        summary.put("averageStars", avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0);
        summary.put("totalRatings", ratings.size());
        summary.put("ratings", ratings);
        return summary;
    }
}
