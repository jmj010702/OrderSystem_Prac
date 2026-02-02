package com.beyound.ordersystem.common.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor

public class CommonErrorDto {
    private int status_code;
    private String error_message;
}
