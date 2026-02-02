package com.beyound.ordersystem.common.service;

import com.beyound.ordersystem.common.dto.SseMessageDto;
import com.beyound.ordersystem.common.repository.SseEmitterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
public class SseAlarmService {

    private final SseEmitterRegistry sseEmitterRegistry;
    private final ObjectMapper objectMapper;

    @Autowired
    public SseAlarmService(SseEmitterRegistry sseEmitterRegistry, ObjectMapper objectMapper) {
        this.sseEmitterRegistry = sseEmitterRegistry;
        this.objectMapper = objectMapper;
    }

    public void sendMessage(String receiver, String sender, String message) {
        SseEmitter sseEmitter = sseEmitterRegistry.getEmitter(receiver);

        SseMessageDto dto = SseMessageDto.builder()
                .receiver(receiver)
                .sender(sender)
                .message(message)
                .build();
        try {
            String data = objectMapper.writeValueAsString(dto);
            sseEmitter.send(SseEmitter.event().name("ordered").data(data));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
