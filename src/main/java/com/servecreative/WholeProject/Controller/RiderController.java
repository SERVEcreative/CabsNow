package com.servecreative.WholeProject.Controller;

import com.servecreative.WholeProject.Model.Duty;
import com.servecreative.WholeProject.Services.RiderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
                         @RequestParam String dropLocation,
                         @RequestParam String vehicleType) {
        return riderService.bookRide(riderId, pickupLocation, dropLocation,vehicleType);
    }
    //cancel a ride
    @PutMapping("/cancelDuties/{riderId}")
    public ResponseEntity<Duty> cancelRide(@PathVariable int riderId) {
        Duty updatedDuty = riderService.cancelRide(riderId);
        return ResponseEntity.ok(updatedDuty);
    }
    @GetMapping("/history/pdf/{riderId}")
    public ResponseEntity<byte[]> downloadDutyHistoryPdf(@PathVariable int riderId) {
        byte[] pdfBytes = riderService.generateDutyHistoryPdf(riderId);

        if (pdfBytes == null) {
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Duty_History.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

}
