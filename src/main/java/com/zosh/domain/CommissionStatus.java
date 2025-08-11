package com.zosh.domain;


public enum CommissionStatus {

    PENDING,   // tạo khi order được đặt nhưng chưa đủ điều kiện nhận
    EARNED,    // đủ điều kiện (ví dụ sau COMPLETED + hết thời gian hold)
    ADJUSTED,  // điều chỉnh do hoàn tiền một phần
    CANCELED   // huỷ do hoàn tiền toàn phần/hệ thống thu hồi
}
