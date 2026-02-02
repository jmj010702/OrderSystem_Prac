package com.beyound.ordersystem.common.init;

import com.beyound.ordersystem.member.entity.Member;
import com.beyound.ordersystem.member.entity.Role;
import com.beyound.ordersystem.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class initialDataLoad implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public initialDataLoad(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (memberRepository.findByEmail("admin@naver.com").isPresent()) {
            return;
        }
        memberRepository.save(Member.builder()
                .name("admin")
                .email("admin@naver.com")
                .role(Role.ADMIN)
                .password(passwordEncoder.encode("admin1234"))
                .build());
    }
}
