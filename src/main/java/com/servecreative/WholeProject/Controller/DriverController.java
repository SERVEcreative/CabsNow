package com.servecreative.WholeProject.Controller;

import com.servecreative.WholeProject.Model.Driver;
import com.servecreative.WholeProject.Services.DriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    private final DriverService driverService;

    @Autowired
    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @PostMapping("/accept/{dutyId}/{driverId}")
    public ResponseEntity<String> acceptDuty(@PathVariable int dutyId, @PathVariable int driverId) {
        return ResponseEntity.ok(driverService.acceptDuty(driverId, dutyId));
    }

    @PostMapping("/ignore/{dutyId}/{driverId}")
    public ResponseEntity<String> ignoreDuty(@PathVariable int dutyId, @PathVariable int driverId) {
        return ResponseEntity.ok(driverService.ignoreDuty(driverId, dutyId));
    }

    @PostMapping("/complete/{dutyId}/{driverId}")
    public ResponseEntity<String> completeDuty(@PathVariable int dutyId, @PathVariable int driverId) {
        return ResponseEntity.ok(driverService.completeDuty(driverId, dutyId));
    }


}
