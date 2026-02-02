package com.beyound.ordersystem.product.entity;

import com.beyound.ordersystem.common.entity.BaseTimeEntity;
import com.beyound.ordersystem.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@Entity
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private Long price;
    private String category;
    @Column(nullable = false)
    private Long stockQuantity;
    private String imagePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT), nullable = false)
    private Member member;

    public void updateProfileImageUrl(String url) {
        this.imagePath = url;
    }

    public void updateStockQuantity(Long orderQuantity) {
        this.stockQuantity = this.stockQuantity - orderQuantity;
    }
}

