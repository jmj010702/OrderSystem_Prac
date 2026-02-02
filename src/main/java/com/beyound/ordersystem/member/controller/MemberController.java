package com.beyound.ordersystem.member.controller;

import com.beyound.ordersystem.common.auth.JwtTokenProvider;
import com.beyound.ordersystem.member.dto.*;
import com.beyound.ordersystem.member.entity.Member;
import com.beyound.ordersystem.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public MemberController(MemberService memberService, JwtTokenProvider jwtTokenProvider) {
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody MemberCreateDto dto) {
        Member member = memberService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(member.getId());
    }


    @PostMapping("/doLogin")
    public ResponseEntity<?> doLogin(@RequestBody MemberLoginDto dto) {
        Member member = memberService.doLogin(dto);
        String accessToken = jwtTokenProvider.createToken(member);
//        refresh토큰 생성 및 저장
        String refreshToken = jwtTokenProvider.createRtToken(member);

        MemberTokenDto mt_dto = MemberTokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(mt_dto);
    }


    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public List<MemberListDto> findAll() {
        return memberService.findAll();
    }

    @GetMapping("/detail/{inputId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MemberListDto> findById(@PathVariable Long inputId) {
        MemberListDto dto = memberService.findById(inputId);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @GetMapping("/myinfo")
    public ResponseEntity<MemberListDto> myInfo(@AuthenticationPrincipal String email) {

        return ResponseEntity.status(HttpStatus.OK).body(memberService.myInfo(email));
    }

    @PostMapping("/refresh-at")
    public ResponseEntity<?> refreshAt(@RequestBody RefreshTokenDto dto) {
//        rt 검증 (1. 토큰 자체 검증, 2. redis조회 검증)
        Member member = jwtTokenProvider.validateRt(dto.getRefreshToken());
//        at신규 생성
        String accessToken = jwtTokenProvider.createToken(member);
        MemberTokenDto mt_dto = MemberTokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(null)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(mt_dto);
    }
}
