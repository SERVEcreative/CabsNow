package com.servecreative.WholeProject.Services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.servecreative.WholeProject.DTO.RideDispatchEnvelope;
import com.servecreative.WholeProject.DTO.RideStatusEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class RideMessageBroadcaster {

    public static final String REDIS_CHANNEL = "cabsnow:ride-events";

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;

    public RideMessageBroadcaster(
            SimpMessagingTemplate messagingTemplate,
            ObjectMapper objectMapper,
            @Autowired(required = false) StringRedisTemplate redisTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }

    public void dispatch(String destination, RideStatusEvent event) {
        if (redisTemplate != null) {
            try {
                RideDispatchEnvelope envelope = new RideDispatchEnvelope(destination, event);
                redisTemplate.convertAndSend(REDIS_CHANNEL, objectMapper.writeValueAsString(envelope));
            } catch (Exception ex) {
                messagingTemplate.convertAndSend(destination, event);
            }
        } else {
            messagingTemplate.convertAndSend(destination, event);
        }
    }

    public void dispatchLocal(String destination, RideStatusEvent event) {
        messagingTemplate.convertAndSend(destination, event);
    }
}
