package com.beyound.ordersystem.ordering.controller;

import com.beyound.ordersystem.common.auth.JwtTokenProvider;
import com.beyound.ordersystem.member.dto.RefreshTokenDto;
import com.beyound.ordersystem.member.entity.Member;
import com.beyound.ordersystem.ordering.dto.OrderListDto;
import com.beyound.ordersystem.ordering.dto.OrderingCreateDto;
import com.beyound.ordersystem.ordering.entity.Ordering;
import com.beyound.ordersystem.ordering.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.awt.print.Pageable;
import java.util.List;

@RestController
@RequestMapping("/ordering")
public class OrderController {

    private final OrderService orderService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public OrderController(OrderService orderService, JwtTokenProvider jwtTokenProvider) {
        this.orderService = orderService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody List<OrderingCreateDto> dtoList) {
        Ordering ordering = orderService.create(dtoList);
        return ResponseEntity.status(HttpStatus.CREATED).body(ordering.getId());
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> findAll() {
        List<OrderListDto> orderListDtos = orderService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(orderListDtos);
    }

    @GetMapping("/listPage")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> findAllPage(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(orderService.findAll());
    }

    @GetMapping("/myorders")
    public ResponseEntity<?> myOrders(@AuthenticationPrincipal String email) {
        return ResponseEntity.status(HttpStatus.OK).body(orderService.myOrders(email));
    }

}
