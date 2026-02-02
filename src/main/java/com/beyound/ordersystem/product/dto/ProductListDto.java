package com.beyound.ordersystem.product.dto;


import com.beyound.ordersystem.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductListDto {
    private Long id;
    private String name;
    private String category;
    private Long price;
    private Long stockQuantity;
    private String imagePath;

    public static ProductListDto fromEntity(Product product) {
        return ProductListDto.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .imagePath(product.getImagePath())
                .build();
    }
}
