package com.zosh.domain;

public enum PayoutStatus {
    CREATED,
    APPROVED,
    PROCESSING,
    PAID,
    FAILED,
    REVERSED   // chi trả thất bại
}
