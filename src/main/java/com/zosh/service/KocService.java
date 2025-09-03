package com.zosh.service;

import com.zosh.dto.KocDto;
import com.zosh.model.Koc;
import com.zosh.request.CreateKocRequest;
import com.zosh.domain.AccountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
public interface KocService {
    Koc createKoc(CreateKocRequest request) ;
    Koc getById(Long id);
    Page<KocDto> getAll(AccountStatus status, Pageable pageable);
    Koc updateStatus(Long id, AccountStatus status);
    // ➕ thêm
    Koc getByCustomerId(Long customerId);
}