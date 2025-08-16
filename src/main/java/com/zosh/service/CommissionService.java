package com.zosh.service;

import com.zosh.model.PaymentOrder;

public interface CommissionService {

    void snapshotForPaymentOrder(PaymentOrder paymentOrder);
}
