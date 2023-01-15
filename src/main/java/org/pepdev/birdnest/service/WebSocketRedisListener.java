package org.pepdev.birdnest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Listens to redis event and sends the drone's data to the clients
 */
@Component
public class WebSocketRedisListener implements MessageListener {
    private final ObjectMapper mapper;

    private final SimpMessagingTemplate simpMessagingTemplate;

    private final DroneGuardianService droneGuardianService;

    @Autowired
    public WebSocketRedisListener(SimpMessagingTemplate simpMessagingTemplate, DroneGuardianService droneGuardianService) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.droneGuardianService = droneGuardianService;
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            this.simpMessagingTemplate.convertAndSend("/drones", mapper.writeValueAsString(droneGuardianService.getPilotInfos()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
