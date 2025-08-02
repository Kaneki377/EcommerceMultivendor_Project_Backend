package com.zosh.service;

import com.zosh.model.Koc;

public interface KocService {
    Koc createKoc(Long customerId, String socialLink);
}