package com.zosh.service.impl;

import com.zosh.model.Seller;
import com.zosh.model.SellerReport;
import com.zosh.repository.SellerReportRepository;
import com.zosh.service.SellerReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SellerReportServiceImpl implements SellerReportService {

    private final SellerReportRepository sellerReportRepository;

    @Override
    public SellerReport getSellerReport(Seller seller) throws Exception {
        SellerReport sellerReport = sellerReportRepository.findBySellerId(seller.getId());

        if(sellerReport == null) {
            SellerReport newSellerReport = new SellerReport();
            newSellerReport.setId(seller.getId());
            return sellerReportRepository.save(newSellerReport);
        }

        return sellerReport;
    }

    @Override
    public SellerReport updateSellerReport(SellerReport sellerReport) throws Exception {
        return sellerReportRepository.save(sellerReport);
    }
}
