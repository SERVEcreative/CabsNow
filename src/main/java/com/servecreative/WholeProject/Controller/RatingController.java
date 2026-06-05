package com.servecreative.WholeProject.Controller;

import com.servecreative.WholeProject.DTO.RatingRequest;
import com.servecreative.WholeProject.Model.Rating;
import com.servecreative.WholeProject.Services.RatingService;
import com.servecreative.WholeProject.securityConfig.SecurityHelper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    private final RatingService ratingService;
    private final SecurityHelper securityHelper;

    public RatingController(RatingService ratingService, SecurityHelper securityHelper) {
        this.ratingService = ratingService;
        this.securityHelper = securityHelper;
    }

    @PostMapping
    public ResponseEntity<Rating> rate(@Valid @RequestBody RatingRequest request) {
        return ResponseEntity.ok(ratingService.rateDriver(request, securityHelper.requireRider().id()));
    }
}
