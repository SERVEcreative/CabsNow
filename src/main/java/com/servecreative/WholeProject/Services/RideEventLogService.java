package com.servecreative.WholeProject.Services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.servecreative.WholeProject.DTO.RideDispatchMessage;
import com.servecreative.WholeProject.Model.RideEventLog;
import com.servecreative.WholeProject.Repository.RideEventLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RideEventLogService {

    private static final Logger log = LoggerFactory.getLogger(RideEventLogService.class);

    private final RideEventLogRepository repository;
    private final ObjectMapper objectMapper;

    public RideEventLogService(RideEventLogRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void record(RideDispatchMessage message) {
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
        } catch (Exception ex) {
            log.warn("Failed to record ride event {}: {}", message.getEventId(), ex.getMessage());
        }
    }

    public List<RideEventLog> getTimeline(int dutyId) {
        return repository.findByDutyIdOrderByCreatedAtAsc(dutyId);
    }
}
