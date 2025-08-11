package com.zosh.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClickStatsResponse {

    private Long linkId;
    private Long totalClicks;
    private Long uniqueSessions;
    private Map<LocalDate, Long> clicksByDate; // key: yyyy-MM-dd
}
