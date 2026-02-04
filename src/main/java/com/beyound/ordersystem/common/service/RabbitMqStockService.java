package com.beyound.ordersystem.common.service;

import com.beyound.ordersystem.common.dto.RabbitMqStockDto;
import com.beyound.ordersystem.product.entity.Product;
import com.beyound.ordersystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Component
public class RabbitMqStockService {

    private final RabbitTemplate rabbitTemplate;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public RabbitMqStockService(RabbitTemplate rabbitTemplate, ProductRepository productRepository, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.productRepository = productRepository;
        this.objectMapper = objectMapper;
    }

    public void publish(Long productId, Long productCount) {
        RabbitMqStockDto dto = RabbitMqStockDto.builder()
                .productId(productId)
                .productCount(productCount)
                .build();
        rabbitTemplate.convertAndSend("stockQueue", dto);
    }


    //   @RabbitListener :  rabbitmq에 특정 큐에 대해 subscribe하는 어노테이션
//     @RabbitListener는 단일 스레드로 메시지를 처리하므로, 동시성 이슈 발생X 다만 멀티버서 환경에서는 문제발생할 수 있음
    @RabbitListener(queues = "stockQueue")
    @Transactional
    public void subscribe(Message message) {
        String messageBody = new String(message.getBody());
//        System.out.println("메세지 : " + messageBody);
        RabbitMqStockDto dto = objectMapper.readValue(messageBody, RabbitMqStockDto.class);
        Product product = productRepository.findById(dto.getProductId()).orElseThrow(() -> new EntityNotFoundException("없는 Id, com_ser_Rabbit_subs"));
        product.updateStockQuantity(dto.getProductCount());
    }
}
