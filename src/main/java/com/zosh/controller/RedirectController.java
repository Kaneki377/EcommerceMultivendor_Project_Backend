package com.zosh.controller;

import com.zosh.model.AffiliateLink;
import com.zosh.service.AffiliateLinkService;
import com.zosh.service.ClickEventService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RedirectController {

    private final AffiliateLinkService affiliateLinkService;
    private final ClickEventService clickEventService;

    // VD: /r/AB12CD34
    @GetMapping("/r/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code, HttpServletRequest request) {
        AffiliateLink link = affiliateLinkService.getByCode(code);

        // Ghi sự kiện click
        clickEventService.recordClick(link, request);

        // Redirect 302
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", link.getTargetUrl());
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}
