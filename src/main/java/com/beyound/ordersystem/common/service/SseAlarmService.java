package com.beyound.ordersystem.common.service;

import com.beyound.ordersystem.common.dto.SseMessageDto;
import com.beyound.ordersystem.common.repository.SseEmitterRegistry;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
public class SseAlarmService implements MessageListener {

    private final SseEmitterRegistry sseEmitterRegistry;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public SseAlarmService(SseEmitterRegistry sseEmitterRegistry, ObjectMapper objectMapper, @Qualifier("ssePubSub") RedisTemplate<String, String> redisTemplate) {
        this.sseEmitterRegistry = sseEmitterRegistry;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
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
//            만약에 emitter객체가 현재 서버에 있으면, 바로 알림 발송, 그렇지 않으면, redis pub/sub활용
            if (sseEmitter != null) {
                sseEmitter.send(SseEmitter.event().name("ordered").data(data));
//                사용자가 새로고침 후에 알림 메세지를 조회하려면 DB에 추가적으로 저장 필요.
            } else {
                redisTemplate.convertAndSend("order-channel", data);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    아래 코드에서 분기처리 해줘야 함
    @Override
    public void onMessage(Message message, byte @Nullable [] pattern) { //위에 코드에서 보낸 메세지를 여기 매개변수 message에서 받아옴
//        message : 실질적으로 메시지가 담겨 있는 객체
//        pattern : 채널명
//        추후 여러개의 채널에 각기 메시지를 publish하고 subscribe할 경우 채널명으로 분기처리 필요(가능)
//        ex) 예를 들어 회원가입 -> 회원축하 알림 주문 -> 주문처리 완료 알림 , 상품 등록 -> 상품 등록 알림이면 그대로 사용 가능

        /*
        주문했ㅇ을 때 메시지가 복잡한 경우 주문 메세지 +회원 메세지 따로
        if(channelName.eqauls("order-channel)) {

        }else  if (channelName.eqauls("member-channel) {

        }
         */
        String channelName = new String(pattern);
        try {
            SseMessageDto dto = objectMapper.readValue(message.getBody(), SseMessageDto.class);
            SseEmitter sseEmitter = sseEmitterRegistry.getEmitter(dto.getReceiver());
            String data = objectMapper.writeValueAsString(dto);
//        해당 서버에 receiver의 emitter객체가 있으면 send
            if (sseEmitter != null) {
                sseEmitter.send(SseEmitter.event().name("ordered").data(data));
            }
            System.out.println("message : " + dto);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
