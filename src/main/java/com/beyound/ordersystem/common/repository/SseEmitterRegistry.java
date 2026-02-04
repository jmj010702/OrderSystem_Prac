package com.beyound.ordersystem.common.repository;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterRegistry {
    //    SseEmitter 객체는 사용자의 연결정보(ip, macaddress 등)을 의미
//    ConcurrentHashMap은 thred-safe한 map(동시성 이슈 발생 X)
    private Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();


    public void addSseEmitter(String email, SseEmitter sseEmitter) {
        this.emitterMap.put(email, sseEmitter);
    }

    public SseEmitter getEmitter(String email) {
        return this.emitterMap.get(email);
    }

    public void removeEmitter(String email) {
        this.emitterMap.remove(email);
    }
}
