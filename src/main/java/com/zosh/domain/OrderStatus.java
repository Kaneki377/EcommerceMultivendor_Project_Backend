package com.zosh.domain;

public enum OrderStatus {
    PENDING,
    PLACED,
    CONFIRMED,
    SHIPPED,
    DELIVERED, //Trang thai don hang được giao nhưng còn có thể b hoàn trả
    CANCELLED,
    COMPLETETED //Trang thai don hang hoan toan duoc thanh toan
}
