package com.servecreative.WholeProject.Controller;

import com.servecreative.WholeProject.Model.Duty;
import com.servecreative.WholeProject.Model.Driver;
import com.servecreative.WholeProject.Repository.DutyRepository;
import com.servecreative.WholeProject.Services.DutyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PrimitiveIterator;

@RestController
@RequestMapping("/api/duties")
public class DutyController {

    @Autowired
    private DutyService dutyService;


    @PostMapping("/createDuty")
    public Duty createDuty(@RequestBody Duty duty) {
        return dutyService.createDuty(duty);
    }

    @PostMapping("/{id}/reject")
    public Duty rejectDuty(@PathVariable int id) {
        return dutyService.rejectDuty(id);
    }



    // Endpoint to get a duty by ID
    @GetMapping("/{dutyId}")
    public ResponseEntity<Duty> getDutyById(@PathVariable int dutyId) {
        Optional<Duty> dutyOptional = dutyService.getDutyById(dutyId);
        return dutyOptional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{dutyId}/accept")
    public ResponseEntity<Duty> acceptDuty(@PathVariable int dutyId, @RequestBody Map<String, Integer> requestBody) {
        Integer driverId = requestBody.get("driverId");
        // Check if driverId is missing in the request body
        if (driverId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        try {
            Duty acceptedDuty = dutyService.acceptDuty(dutyId, driverId);
            return ResponseEntity.ok(acceptedDuty);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/getAllDutyDyStatusPending")
    public ResponseEntity<List<Duty>> getDutyByStatus() {
        List<Duty> dutyList = dutyService.getDutyByStatusPending();

        if (dutyList.isEmpty()) {
            return ResponseEntity.noContent().build(); // Returns 204 No Content if the list is empty
        }

        return ResponseEntity.ok(dutyList); // Returns 200 OK with the duty list
    }

    @PostMapping("/complete")
    public ResponseEntity<Duty> completeDuty(@RequestBody Map<String, Integer> requestBody) {
        try {
            // Extract dutyId and driverId from the request body
            Integer dutyId = requestBody.get("dutyId");
            Integer driverId = requestBody.get("driverId");

            // Check if both dutyId and driverId are provided
            if (dutyId == null || driverId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            // Call the service to complete the duty
            Duty completedDuty = dutyService.completeDuty(dutyId, driverId);

            return ResponseEntity.ok(completedDuty);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @GetMapping("/getCompletedDutiesByDriver/{driverId}")
    public ResponseEntity<List<Duty>> getCompletedDutiesByDriver(@PathVariable int driverId) {
        List<Duty> completedDuties = dutyService.getCompletedDutiesByDriver(driverId);

        if (completedDuties.isEmpty()) {
            return ResponseEntity.noContent().build(); // Returns 204 No Content if the list is empty
        }

        return ResponseEntity.ok(completedDuties); // Returns 200 OK with the completed duties list
    }


}
