package com.servecreative.WholeProject.Controller;

import com.servecreative.WholeProject.DTO.BookRideRequest;
import com.servecreative.WholeProject.Model.Duty;
import com.servecreative.WholeProject.Services.RiderService;
import com.servecreative.WholeProject.securityConfig.AuthenticatedUser;
import com.servecreative.WholeProject.securityConfig.SecurityHelper;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/riders")
public class RiderController {

    private final RiderService riderService;
    private final SecurityHelper securityHelper;

    public RiderController(RiderService riderService, SecurityHelper securityHelper) {
        this.riderService = riderService;
        this.securityHelper = securityHelper;
    }

    @PostMapping("/book")
    public Duty bookRide(@Valid @RequestBody BookRideRequest request) {
        AuthenticatedUser rider = securityHelper.requireRider();
        return riderService.bookRide(
                rider.id(),
                request.getPickupLocation(),
                request.getDropLocation(),
                request.getVehicleType(),
                request.getFare(),
                request.getPickupLat(),
                request.getPickupLng(),
                request.getDropLat(),
                request.getDropLng());
    }

    @PutMapping("/cancel")
    public ResponseEntity<Duty> cancelRide() {
        AuthenticatedUser rider = securityHelper.requireRider();
        Duty updatedDuty = riderService.cancelRide(rider.id());
        return ResponseEntity.ok(updatedDuty);
    }

    @GetMapping("/history/pdf")
    public ResponseEntity<byte[]> downloadDutyHistoryPdf() {
        AuthenticatedUser rider = securityHelper.requireRider();
        byte[] pdfBytes = riderService.generateDutyHistoryPdf(rider.id());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Duty_History.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
