package com.beyound.ordersystem.ordering.service;

import com.beyound.ordersystem.common.service.RabbitMqStockService;
import com.beyound.ordersystem.common.service.SseAlarmService;
import com.beyound.ordersystem.member.entity.Member;
import com.beyound.ordersystem.member.repository.MemberRepository;
import com.beyound.ordersystem.ordering.dto.OrderListDto;
import com.beyound.ordersystem.ordering.dto.OrderingCreateDto;
import com.beyound.ordersystem.ordering.entity.Ordering;
import com.beyound.ordersystem.ordering.entity.OrderingDetail;
import com.beyound.ordersystem.ordering.repository.OrderRepository;
import com.beyound.ordersystem.ordering.repository.OrderingDetailRepository;
import com.beyound.ordersystem.product.entity.Product;
import com.beyound.ordersystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    private final OrderingDetailRepository orderingDetailRepository;
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final SseAlarmService sseAlarmService;
    private final RedisTemplate<String, String> redisTemplate;
    private final RabbitMqStockService rabbitMqStockService;

    @Autowired
    public OrderService(OrderingDetailRepository orderingDetailRepository, OrderRepository orderRepository, MemberRepository memberRepository, ProductRepository productRepository, SseAlarmService sseAlarmService, @Qualifier("stockInventory") RedisTemplate<String, String> redisTemplate, RabbitMqStockService rabbitMqStockService) {
        this.orderingDetailRepository = orderingDetailRepository;
        this.orderRepository = orderRepository;
        this.memberRepository = memberRepository;
        this.productRepository = productRepository;
        this.sseAlarmService = sseAlarmService;
        this.redisTemplate = redisTemplate;
        this.rabbitMqStockService = rabbitMqStockService;
    }

//    동시성 제어 방법 1: 특정 메서드에 한해 격리 수준 올리기
//    @Transactional(isolation = Isolation.SERIALIZABLE)

    public Ordering create(List<OrderingCreateDto> dtoList) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("order_ser_create"));
        Ordering ordering = Ordering.builder()
                .member(member)
                .build();

        for (OrderingCreateDto dto : dtoList) {
            Product product = productRepository.findById(dto.getProductId()).orElseThrow(() -> new EntityNotFoundException("order_ser_create_for"));
//            동시성 제어방법 2: select for update를 통한 락 설정 이후 조회
//            Product product = productRepository.findByIdForUpdate(dto.getProductId()).orElseThrow(() -> new EntityNotFoundException("order_ser_create_for"));

//            동시성 제어 방법 3 : Redis에서 재고 수량 확인 및 재고 수량 감소 처리
//            단점 : 조회와 감소 요청이 분리되어 동시성 문제 발생 ->해결책 : 루아(lua)스크립트를 통해 여러 작업을 단일 요청으로 묶어 처리 가능

            String remain = redisTemplate.opsForValue().get(String.valueOf(dto.getProductId()));
            int remainQuantity = Integer.parseInt(remain);
            if (remainQuantity < dto.getProductCount()) {
                throw new IllegalArgumentException("재고 부족 ord_ser_create");
            } else {
                redisTemplate.opsForValue().decrement(String.valueOf(dto.getProductId()), dto.getProductCount());
            }
//          기존 코드
//            if (product.getStockQuantity() < dto.getProductCount()) {
//                throw new IllegalArgumentException("재고 부족 ord_ser_create");
//            }
//            product.updateStockQuantity(dto.getProductCount());

            OrderingDetail orderingDetail = OrderingDetail.builder()
                    .ordering(ordering)
                    .product(product)
                    .quantity(dto.getProductCount())
                    .build();
            ordering.getOrderingDetailList().add(orderingDetail);

//            rdb동기화를 위한 작업 1: 스케쥴러 활용 ㄴ
//            rdb동기화를 위한 작업 2: rabbitMq에 rdb재고감소 메시지 발송
            rabbitMqStockService.publish(dto.getProductId(), dto.getProductCount());
        }
        orderRepository.save(ordering);

//        주문 성공시 admin 유저에게 알림 메시지 전송
        String message = ordering.getId() + "번 주문이 들어왔습니다";
        sseAlarmService.sendMessage("admin@naver.com", email, message);

        return ordering;
    }

    @Transactional(readOnly = true)
    public List<OrderListDto> findAll() {
        return orderRepository.findAll().stream().map(OrderListDto::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderListDto> myOrders(String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("order_ser_myorders"));
        return orderRepository.findAllByMember(member).stream().map(OrderListDto::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<OrderListDto> findAllPage(Pageable pageable) {
        Page<Ordering> ordering = orderRepository.findAll(pageable);
        return ordering.map(OrderListDto::fromEntity);
    }
}
