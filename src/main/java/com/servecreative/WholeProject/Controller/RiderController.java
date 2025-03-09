package com.servecreative.WholeProject.Controller;

import com.servecreative.WholeProject.Model.Duty;
import com.servecreative.WholeProject.Services.RiderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/riders")
public class RiderController {

    @Autowired
    private RiderService riderService;

    // Book a ride
    @PostMapping("/book")
    public Duty bookRide(@RequestParam int riderId,
                         @RequestParam String pickupLocation,
                         @RequestParam String dropLocation) {
        return riderService.bookRide(riderId, pickupLocation, dropLocation);
    }
    //cancel a ride
    @PutMapping("/cancelduties/{riderId}")
    public ResponseEntity<Duty> cancelRide(@PathVariable int riderId) {
        Duty updatedDuty = riderService.cancelRide(riderId);
        return ResponseEntity.ok(updatedDuty);
    }
}
