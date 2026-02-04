package com.beyound.ordersystem.product.dto;


import com.beyound.ordersystem.member.entity.Member;
import com.beyound.ordersystem.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUpdateDto {
    private String name;
    private Long price;
    private String category;
    private Long stockQuantity;
//    이미지 수정은 일반적으로 별도의 api로 처리한다
    private MultipartFile productImage;

}
