package com.servecreative.WholeProject.webConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.servecreative.WholeProject.DTO.RideDispatchEnvelope;
import com.servecreative.WholeProject.Services.RideMessageBroadcaster;

public class RedisRideEventSubscriber {

    private final RideMessageBroadcaster broadcaster;
    private final ObjectMapper objectMapper;

    public RedisRideEventSubscriber(RideMessageBroadcaster broadcaster, ObjectMapper objectMapper) {
        this.broadcaster = broadcaster;
        this.objectMapper = objectMapper;
    }

    public void onMessage(String message) {
        try {
            RideDispatchEnvelope envelope = objectMapper.readValue(message, RideDispatchEnvelope.class);
            if (envelope.getDestination() != null && envelope.getPayload() != null) {
                broadcaster.dispatchLocal(envelope.getDestination(), envelope.getPayload());
            }
        } catch (Exception ignored) {
            /* malformed redis payload */
        }
    }
}
