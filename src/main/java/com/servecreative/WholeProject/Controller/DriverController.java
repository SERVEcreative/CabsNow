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

    @GetMapping
    public ResponseEntity<List<Driver>> getAllDrivers() {
        List<Driver> drivers = driverService.getAllDrivers();
        return new ResponseEntity<>(drivers, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Driver> getDriverById(@PathVariable("id") int driverId) {
        Optional<Driver> driver = driverService.getDriverById(driverId);
        return driver.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/createDriver")
    public ResponseEntity<Driver> createDriver(@RequestBody Driver driver) {
        Driver savedDriver = driverService.saveDriver(driver);
        return new ResponseEntity<>(savedDriver, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDriver(@PathVariable("id") int driverId) {
        driverService.deleteDriver(driverId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Driver>> getDriversByStatus(@PathVariable String status) {
        List<Driver> drivers = driverService.getDriversByStatus(status);
        return new ResponseEntity<>(drivers, HttpStatus.OK);
    }
}
