package com.servecreative.WholeProject.Controller;

import com.servecreative.WholeProject.Model.Duty;
import com.servecreative.WholeProject.Services.DutyService;
import com.servecreative.WholeProject.securityConfig.AuthenticatedUser;
import com.servecreative.WholeProject.securityConfig.SecurityHelper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/duties")
public class DutyController {

    private final DutyService dutyService;
    private final SecurityHelper securityHelper;

    public DutyController(DutyService dutyService, SecurityHelper securityHelper) {
        this.dutyService = dutyService;
        this.securityHelper = securityHelper;
    }

    @PostMapping("/createDuty")
    public Duty createDuty(@RequestBody Duty duty) {
        AuthenticatedUser rider = securityHelper.requireRider();
        return dutyService.createDuty(duty, rider.id());
    }

    @PostMapping("/{id}/reject")
    public Duty rejectDuty(@PathVariable int id) {
        AuthenticatedUser rider = securityHelper.requireRider();
        return dutyService.rejectDutyForRider(id, rider.id());
    }

    @GetMapping("/{dutyId}")
    public ResponseEntity<Duty> getDutyById(@PathVariable int dutyId) {
        Optional<Duty> dutyOptional = dutyService.getDutyById(dutyId);
        if (dutyOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Duty duty = dutyOptional.get();
        AuthenticatedUser user = securityHelper.currentUser();
        if (user.isRider() && duty.getRider().getRiderId() != user.id()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (user.isDriver()) {
            boolean canView = duty.getStatus() == Duty.DutyStatus.PENDING
                    || (duty.getAssignedDriver() != null
                    && duty.getAssignedDriver().getDriverId() == user.id());
            if (!canView) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        return ResponseEntity.ok(duty);
    }

    @PostMapping("/{dutyId}/accept")
    public ResponseEntity<Duty> acceptDuty(@PathVariable int dutyId) {
        AuthenticatedUser driver = securityHelper.requireDriver();
        try {
            Duty acceptedDuty = dutyService.acceptDuty(dutyId, driver.id());
            return ResponseEntity.ok(acceptedDuty);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/getAllDutyDyStatusPending")
    public ResponseEntity<List<Duty>> getDutyByStatus() {
        securityHelper.requireDriver();
        List<Duty> dutyList = dutyService.getDutyByStatusPending();
        if (dutyList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(dutyList);
    }

    @PostMapping("/complete")
    public ResponseEntity<Duty> completeDuty(@RequestBody Map<String, Integer> requestBody) {
        AuthenticatedUser driver = securityHelper.requireDriver();
        Integer dutyId = requestBody.get("dutyId");
        if (dutyId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        try {
            Duty completedDuty = dutyService.completeDuty(dutyId, driver.id());
            return ResponseEntity.ok(completedDuty);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/getCompletedDutiesByDriver/{driverId}")
    public ResponseEntity<List<Duty>> getCompletedDutiesByDriver(@PathVariable int driverId) {
        securityHelper.assertDriver(driverId);
        List<Duty> completedDuties = dutyService.getCompletedDutiesByDriver(driverId);
        if (completedDuties.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(completedDuties);
    }
}
