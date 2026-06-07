package com.servecreative.WholeProject.Services;

import com.servecreative.WholeProject.DTO.RideDispatchMessage;
import com.servecreative.WholeProject.Repository.UserRepository;
import com.servecreative.WholeProject.kafka.KafkaTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class RideEventBus {

    private static final Logger log = LoggerFactory.getLogger(RideEventBus.class);

    private final RideMessageBroadcaster broadcaster;
    private final RideEventLogService eventLogService;
    private final EmailNotificationService emailNotificationService;
    private final UserRepository userRepository;
    private final KafkaTemplate<String, RideDispatchMessage> kafkaTemplate;
    private final boolean kafkaEnabled;

    public RideEventBus(
            RideMessageBroadcaster broadcaster,
            RideEventLogService eventLogService,
            EmailNotificationService emailNotificationService,
            UserRepository userRepository,
            @org.springframework.beans.factory.annotation.Autowired(required = false)
            KafkaTemplate<String, RideDispatchMessage> kafkaTemplate,
            @Value("${app.kafka.enabled:false}") boolean kafkaEnabled) {
        this.broadcaster = broadcaster;
        this.eventLogService = eventLogService;
        this.emailNotificationService = emailNotificationService;
        this.userRepository = userRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaEnabled = kafkaEnabled;
    }

    public void publish(RideDispatchMessage message) {
        // Live WebSocket updates always go out immediately (don't wait for Kafka).
        message.getDestinations().forEach(destination ->
                broadcaster.dispatch(destination, message.getPayload()));

        if (kafkaEnabled && kafkaTemplate != null) {
            kafkaTemplate.send(KafkaTopics.RIDE_AUDIT, message.getEventId(), message);
            kafkaTemplate.send(KafkaTopics.RIDE_NOTIFICATIONS, String.valueOf(message.getDutyId()), message);
            log.debug("Kafka audit/notification for duty {}", message.getDutyId());
            return;
        }

        eventLogService.record(message);
        sendBookingNotification(message);
    }

    private void sendBookingNotification(RideDispatchMessage message) {
        if (message.getPayload() == null || !"PENDING".equals(message.getPayload().getStatus())) {
            return;
        }
        userRepository.findById((long) message.getPayload().getRiderId()).ifPresent(user ->
                emailNotificationService.sendRideConfirmation(
                        user.getEmail(),
                        message.getDutyId(),
                        message.getPayload().getPickupLocation(),
                        message.getPayload().getDropLocation(),
                        message.getPayload().getFare()));
    }
}
