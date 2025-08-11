package com.zosh.service;

import com.zosh.model.AffiliateLink;
import com.zosh.request.ClickQueryRequest;
import com.zosh.response.ClickEventResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface ClickEventService {

    void recordClick(AffiliateLink link, HttpServletRequest request);
    List<ClickEventResponse> listClicksOfLink(Long linkId, ClickQueryRequest request);
}
