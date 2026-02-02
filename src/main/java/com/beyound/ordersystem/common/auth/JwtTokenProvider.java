package com.beyound.ordersystem.common.auth;

import com.beyound.ordersystem.member.entity.Member;
import com.beyound.ordersystem.member.repository.MemberRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class JwtTokenProvider {
    private final RedisTemplate<String, String> redisTemplate;
    private final MemberRepository memberRepository;

    @Value("${jwt.secretKey}")
    private String secret_key;

    @Value("${jwt.expiration}")
    private int expiration;

    private Key secretkey;


    @Value("${jwt.secretKeyRt}")
    private String secret_key_rt;

    @Value("${jwt.expirationRt}")
    private int expirationRt;

    private Key secretkeyRt;

    public JwtTokenProvider(@Qualifier("rtInventory") RedisTemplate<String, String> redisTemplate, MemberRepository memberRepository) {
        this.redisTemplate = redisTemplate;
        this.memberRepository = memberRepository;
    }

    @PostConstruct
    public void init() {
        secretkey = new SecretKeySpec(Base64.getDecoder().decode(secret_key), SignatureAlgorithm.HS512.getJcaName());
        secretkeyRt = new SecretKeySpec(Base64.getDecoder().decode(secret_key_rt), SignatureAlgorithm.HS512.getJcaName());
    }

    public String createToken(Member member) {
        Claims claims = Jwts.claims().setSubject(member.getEmail());
        claims.put("role", member.getRole().toString());

        Date now = new Date();

        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiration * 60 * 1000L))
                .signWith(secretkey)
                .compact();

        return token;
    }


    public String createRtToken(Member member) {
//        유효기간이 긴 rt 토큰 생성
        Claims claims = Jwts.claims().setSubject(member.getEmail());
        claims.put("role", member.getRole().toString());
        Date now = new Date();
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationRt * 60 * 1000L))
                .signWith(secretkeyRt)
                .compact();
//     rt토크을 redis에 저장
//        opsForValue : 일반 스트링 자료구조 /  opsForSet또는 opsForZset, opsForList 등 존재
//        redisTemplate.opsForValue().set(member.getEmail(), token);
        redisTemplate.opsForValue().set(member.getEmail(), token, expirationRt, TimeUnit.MINUTES); //3000분 ttl(ttl : 유효시간)
        return token;
    }


    public Member validateRt(String refreshToken) {
        Claims claims = null;
//        rt토큰 그 자체를 검증
        try {
            claims = Jwts.parserBuilder()
                    .setSigningKey(secretkeyRt)
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("잘못된 토큰입니다");
        }

        String email = claims.getSubject().toString();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("없는 emiail"));
//        redis rt와 비교 검증
        String redisRt = redisTemplate.opsForValue().get(email);

        if (!redisRt.equals(refreshToken)) {
            throw new IllegalArgumentException("잘못된 토큰입니다");
        }
        return member;

    }
}
