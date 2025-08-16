package com.zosh.dto;

import com.zosh.model.OrderItem;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionDTO {

    private Long orderItemId;
    private Long orderId;
    private Long productId;
    private Long campaignId;
    private Long kocId;
    private BigDecimal commissionAmount;
    private OrderItem.CommissionStatus status;
    private LocalDateTime attributedAt;
}
