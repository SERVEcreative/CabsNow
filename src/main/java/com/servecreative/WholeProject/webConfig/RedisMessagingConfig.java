package com.servecreative.WholeProject.webConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.servecreative.WholeProject.DTO.RideDispatchEnvelope;
import com.servecreative.WholeProject.Services.RideMessageBroadcaster;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "true")
public class RedisMessagingConfig {

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }

    @Bean
    public MessageListenerAdapter rideEventListenerAdapter(RedisRideEventSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "onMessage");
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory factory,
            MessageListenerAdapter rideEventListenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.addMessageListener(
                rideEventListenerAdapter,
                new ChannelTopic(RideMessageBroadcaster.REDIS_CHANNEL));
        return container;
    }

    @Bean
    public RedisRideEventSubscriber redisRideEventSubscriber(
            RideMessageBroadcaster broadcaster,
            ObjectMapper objectMapper) {
        return new RedisRideEventSubscriber(broadcaster, objectMapper);
    }
}
