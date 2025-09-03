package com.zosh.model;

import java.security.SecureRandom;

public class OrderIdGenerator {
    private static final SecureRandom random = new SecureRandom();

    public static String generateOrderId() {
        int number = random.nextInt(1_000_0000); // từ 0 -> 9999999
        return String.format("ORD%07d", number); // luôn đủ 7 chữ số
    }
}
