package com.beyound.ordersystem.ordering.dto;


import com.beyound.ordersystem.ordering.entity.Ordering;
import com.beyound.ordersystem.ordering.entity.OrderingDetail;
import com.beyound.ordersystem.ordering.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderListDto {
    private Long Id;
    private String memberEmail;
    private Status orderStatus;

    private List<OrderListDetailDto> orderDetails;

    public static OrderListDto fromEntity(Ordering ordering) {
        List<OrderListDetailDto> orderListDetailDtoList = new ArrayList<>();
        for (OrderingDetail orderingDetail : ordering.getOrderingDetailList()) {
            orderListDetailDtoList.add(OrderListDetailDto.fromEntity(orderingDetail));
        }

        OrderListDto orderListDto = OrderListDto.builder()
                .Id(ordering.getId())
                .orderStatus(ordering.getStatus())
                .memberEmail(ordering.getMember().getName())
                .orderDetails(orderListDetailDtoList)
                .build();
        return orderListDto;
    }

}
