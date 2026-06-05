package com.servecreative.WholeProject;

import com.servecreative.WholeProject.Services.FareService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class CabsNowApplicationTests {

    @Autowired
    private FareService fareService;

    @Test
    void contextLoads() {
    }

    @Test
    void fareCalculationUsesFallbackWithoutApiKey() {
        double fare = fareService.calculateFare("28.6139", "77.2090", "28.5355", "77.3910", "SEDAN");
        assertTrue(fare > 0);
    }
}
