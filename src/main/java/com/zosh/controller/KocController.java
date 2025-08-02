package com.zosh.controller;

import com.zosh.model.Koc;
import com.zosh.request.CreateKocRequest;
import com.zosh.service.KocService;
import jakarta.validation.Valid;
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
    public ResponseEntity<?> createKoc(@Valid @RequestBody CreateKocRequest request) {
        if (!request.hasAtLeastOneLink()) {
            return ResponseEntity.badRequest().body("Phải cung cấp ít nhất một link mạng xã hội.");
        }

        Koc koc = kocService.createKoc(request);
        return ResponseEntity.ok(koc);
    }


    @GetMapping("/test")
    @PreAuthorize("hasRole('ROLE_KOC')")
    public ResponseEntity<String> testKocRole() {
        return ResponseEntity.ok("Access granted for ROLE_KOC");
    }
}