package com.zosh.service;

import com.zosh.model.Koc;
import com.zosh.request.CreateKocRequest;

public interface KocService {
    Koc createKoc(CreateKocRequest request) ;
}