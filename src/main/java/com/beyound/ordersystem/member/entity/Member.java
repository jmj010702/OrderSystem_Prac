package com.beyound.ordersystem.member.entity;


import com.beyound.ordersystem.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String name;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;


}
