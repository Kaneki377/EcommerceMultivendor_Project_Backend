package com.zosh.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
public class ClickQueryRequest {

    private LocalDateTime from; // nullable
    private LocalDateTime to;   // nullable
    private int page = 0;
    private int size = 20;
}
