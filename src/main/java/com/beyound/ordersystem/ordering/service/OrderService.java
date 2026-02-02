package com.beyound.ordersystem.ordering.service;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Autowired
    public OrderService(OrderingDetailRepository orderingDetailRepository, OrderRepository orderRepository, MemberRepository memberRepository, ProductRepository productRepository, SseAlarmService sseAlarmService) {
        this.orderingDetailRepository = orderingDetailRepository;
        this.orderRepository = orderRepository;
        this.memberRepository = memberRepository;
        this.productRepository = productRepository;
        this.sseAlarmService = sseAlarmService;
    }

    public Ordering create(List<OrderingCreateDto> dtoList) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("order_ser_create"));
        Ordering ordering = Ordering.builder()
                .member(member)
                .build();

        for (OrderingCreateDto dto : dtoList) {
            Product product = productRepository.findById(dto.getProductId()).orElseThrow(() -> new EntityNotFoundException("order_ser_create_for"));


            if (product.getStockQuantity() < dto.getProductCount()) {
                throw new IllegalArgumentException("재고 부족 ord_ser_create");
            }
            product.updateStockQuantity(dto.getProductCount());
            OrderingDetail orderingDetail = OrderingDetail.builder()
                    .ordering(ordering)
                    .product(product)
                    .quantity(dto.getProductCount())
                    .build();
            ordering.getOrderingDetailList().add(orderingDetail);

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
