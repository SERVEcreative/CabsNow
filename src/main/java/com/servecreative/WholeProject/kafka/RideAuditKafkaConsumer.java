package com.servecreative.WholeProject.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.servecreative.WholeProject.DTO.RideDispatchMessage;
import com.servecreative.WholeProject.Model.RideEventLog;
import com.servecreative.WholeProject.Repository.RideEventLogRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class RideAuditKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(RideAuditKafkaConsumer.class);

    private final RideEventLogRepository repository;
    private final ObjectMapper objectMapper;
    private final Counter auditCounter;

    public RideAuditKafkaConsumer(
            RideEventLogRepository repository,
            ObjectMapper objectMapper,
            MeterRegistry meterRegistry) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.auditCounter = meterRegistry.counter("cabsnow.kafka.audit.persisted");
    }

    @Transactional
    @KafkaListener(topics = KafkaTopics.RIDE_AUDIT, groupId = "${spring.application.name}-audit")
    public void consume(RideDispatchMessage message) {
        if (repository.existsByEventId(message.getEventId())) {
            return;
        }

        try {
            RideEventLog entry = new RideEventLog();
            entry.setEventId(message.getEventId());
            entry.setDutyId(message.getDutyId());
            entry.setEventType(message.getEventType());
            if (message.getPayload() != null) {
                entry.setStatus(message.getPayload().getStatus());
            }
            entry.setPayloadJson(objectMapper.writeValueAsString(message.getPayload()));
            repository.save(entry);
            auditCounter.increment();
        } catch (Exception ex) {
            log.warn("Failed to persist ride audit event {}: {}", message.getEventId(), ex.getMessage());
        }
    }
}
