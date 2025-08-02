package com.zosh.controller;

import com.zosh.model.Koc;
import com.zosh.service.KocService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/koc")
@RequiredArgsConstructor
public class KocController {

    private final KocService kocService;

    @PostMapping("/create")
    public ResponseEntity<Koc> createKoc(@RequestParam Long customerId, @RequestParam String socialLink) {
        Koc koc = kocService.createKoc(customerId, socialLink);
        return ResponseEntity.ok(koc);
    }

    @GetMapping("/test")
    @PreAuthorize("hasRole('ROLE_KOC')")
    public ResponseEntity<String> testKocRole() {
        return ResponseEntity.ok("Access granted for ROLE_KOC");
    }
}