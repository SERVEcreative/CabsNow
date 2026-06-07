package com.servecreative.WholeProject.Services;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.servecreative.WholeProject.Model.Duty;
import com.servecreative.WholeProject.Model.Rider;
import com.servecreative.WholeProject.Repository.DutyRepository;
import com.servecreative.WholeProject.Repository.RiderRepository;
import com.servecreative.WholeProject.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class RiderService {

    private final DutyRepository dutyRepository;
    private final RiderRepository riderRepository;
    private final UserRepository userRepository;
    private final RideEventPublisher rideEventPublisher;

    public RiderService(DutyRepository dutyRepository, RiderRepository riderRepository,
                        UserRepository userRepository, RideEventPublisher rideEventPublisher) {
        this.dutyRepository = dutyRepository;
        this.riderRepository = riderRepository;
        this.userRepository = userRepository;
        this.rideEventPublisher = rideEventPublisher;
    }

    public Rider saveRider(Rider rider) {
        return riderRepository.save(rider);
    }

    public Duty bookRide(int riderId, String pickupLocation, String dropLocation, String vehicleType,
                         double fare, Double pickupLat, Double pickupLng, Double dropLat, Double dropLng) {
        Rider rider = riderRepository.findById(riderId)
                .orElseThrow(() -> new RuntimeException("Rider not found"));

        Duty existingDuty = dutyRepository.findDutiesByRiderIdAndStatus(riderId, Duty.DutyStatus.PENDING);
        if (existingDuty != null) {
            throw new IllegalStateException("You already have a pending ride. Complete or cancel it before booking a new one.");
        }

        Duty.VehicleType parsedVehicleType;
        try {
            parsedVehicleType = Duty.VehicleType.valueOf(vehicleType.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Invalid vehicle type: " + vehicleType);
        }

        Duty duty = new Duty();
        duty.setStatus(Duty.DutyStatus.PENDING);
        duty.setRider(rider);
        duty.setPickupLocation(pickupLocation);
        duty.setDropLocation(dropLocation);
        duty.setVehicleType(parsedVehicleType);
        duty.setFare(fare);
        duty.setPickupLat(pickupLat);
        duty.setPickupLng(pickupLng);
        duty.setDropLat(dropLat);
        duty.setDropLng(dropLng);

        Duty saved = dutyRepository.save(duty);
        rideEventPublisher.publish(saved, "Finding nearby drivers…", pickupLat, pickupLng);
        return saved;
    }

    public Duty cancelRide(int riderId) {
        riderRepository.findById(riderId).orElseThrow(() -> new RuntimeException("Rider not available"));
        Duty existingDuty = dutyRepository.findDutiesByRiderIdAndStatus(riderId, Duty.DutyStatus.PENDING);

        if (existingDuty == null) {
            throw new RuntimeException("No pending ride to cancel");
        }

        existingDuty.setStatus(Duty.DutyStatus.REJECTED);
        Duty saved = dutyRepository.save(existingDuty);
        rideEventPublisher.publish(saved, "Ride cancelled");
        return saved;
    }

    public byte[] generateDutyHistoryPdf(int riderId) {
        Rider rider = riderRepository.findById(riderId)
                .orElseThrow(() -> new RuntimeException("Rider not found"));

        List<Duty> duties = dutyRepository.findDutiesByRider(riderId);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("CabsNow — Ride History"));
            document.add(new Paragraph("Rider: " + rider.getName()));
            document.add(new Paragraph("Phone: " + rider.getPhoneNumber()));
            document.add(new Paragraph(" "));

            if (duties.isEmpty()) {
                document.add(new Paragraph("No rides found."));
            } else {
                for (Duty duty : duties) {
                    document.add(new Paragraph(
                            "Duty #" + duty.getDutyId()
                                    + " | " + duty.getStatus()
                                    + " | " + duty.getPickupLocation() + " -> " + duty.getDropLocation()
                                    + " | Fare: " + duty.getFare()
                                    + " | Vehicle: " + duty.getVehicleType()));
                }
            }

            document.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate ride history PDF", e);
        }
    }
}
