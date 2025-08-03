package com.zosh.request;

import lombok.Data;

@Data
public class PaypalCompletionRequest {

    private String paymentId;
    private String payerId;
    private Long paymentOrderId;
}
