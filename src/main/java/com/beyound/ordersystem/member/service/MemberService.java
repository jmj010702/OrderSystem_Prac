package com.beyound.ordersystem.member.service;

import com.beyound.ordersystem.member.dto.MemberCreateDto;
import com.beyound.ordersystem.member.dto.MemberListDto;
import com.beyound.ordersystem.member.dto.MemberLoginDto;
import com.beyound.ordersystem.member.entity.Member;
import com.beyound.ordersystem.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public Member create(MemberCreateDto dto) {
        if (memberRepository.findByEmail(dto.getEmail()).isPresent())
            throw new IllegalArgumentException("service-membercreate부분");

        Member member = Member.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .build();
        memberRepository.save(member);
        return member;
    }

    public Member doLogin(MemberLoginDto dto) {
        Optional<Member> member = memberRepository.findByEmail(dto.getEmail());
        boolean check = true;
        if (member.isEmpty()) {
            check = false;
        } else {
            if (!passwordEncoder.matches(dto.getPassword(), member.get().getPassword())) {
                check = false;
            }
        }
        if (check == false) {
            throw new BadCredentialsException("ser_doLogin");
        }
        return member.get();
    }


    public List<MemberListDto> findAll() {
        List<Member> member = memberRepository.findAll();
        List<MemberListDto> memberListDtoList = new ArrayList<>();
        for (Member m : member) {
            MemberListDto dto = MemberListDto.builder()
                    .id(m.getId())
                    .name(m.getName())
                    .email(m.getEmail())
                    .build();

            memberListDtoList.add(dto);
        }
        return memberListDtoList;
    }

    public MemberListDto findById(Long id) {

        Member member = memberRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("ser_memfindById"));
        return MemberListDto.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .build();
    }

    public MemberListDto myInfo(String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("ser_mem_myinfo"));
        return MemberListDto.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .build();
    }


}
