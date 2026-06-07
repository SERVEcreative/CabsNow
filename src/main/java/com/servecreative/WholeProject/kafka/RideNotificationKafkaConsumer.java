package com.servecreative.WholeProject.kafka;

import com.servecreative.WholeProject.DTO.RideDispatchMessage;
import com.servecreative.WholeProject.Repository.UserRepository;
import com.servecreative.WholeProject.Services.EmailNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class RideNotificationKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(RideNotificationKafkaConsumer.class);

    private final EmailNotificationService emailNotificationService;
    private final UserRepository userRepository;

    public RideNotificationKafkaConsumer(
            EmailNotificationService emailNotificationService,
            UserRepository userRepository) {
        this.emailNotificationService = emailNotificationService;
        this.userRepository = userRepository;
    }

    @KafkaListener(topics = KafkaTopics.RIDE_NOTIFICATIONS, groupId = "${spring.application.name}-notifications")
    public void consume(RideDispatchMessage message) {
        if (message.getPayload() == null) {
            return;
        }

        String status = message.getPayload().getStatus();
        if (!"PENDING".equals(status) && !"COMPLETED".equals(status)) {
            return;
        }

        userRepository.findById((long) message.getPayload().getRiderId()).ifPresent(user -> {
            if ("PENDING".equals(status)) {
                emailNotificationService.sendRideConfirmation(
                        user.getEmail(),
                        message.getDutyId(),
                        message.getPayload().getPickupLocation(),
                        message.getPayload().getDropLocation(),
                        message.getPayload().getFare());
                log.info("Async notification queued for duty {} rider {}", message.getDutyId(), user.getEmail());
            }
        });
    }
}
