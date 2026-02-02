package com.beyound.ordersystem.ordering.dto;

import com.beyound.ordersystem.ordering.entity.Ordering;
import com.beyound.ordersystem.ordering.entity.OrderingDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderListDetailDto {
    private Long DetailId;
    private String productName;
    private Long productCount;

    public static OrderListDetailDto fromEntity(OrderingDetail orderingDetail){
        return OrderListDetailDto.builder()
                .DetailId(orderingDetail.getId())
                .productName(orderingDetail.getProduct().getName())
                .productCount(orderingDetail.getQuantity())
                .build();
    }


}
