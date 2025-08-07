package com.zosh.service.impl;

import com.zosh.exceptions.SellerException;
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
    public SellerReport getSellerReport(Seller seller) throws SellerException {
        SellerReport report = sellerReportRepository.findBySellerId(seller.getId());
        if(report == null){
            SellerReport newReport = new SellerReport();
            newReport.setSeller(seller);
            return sellerReportRepository.save(newReport);
        }
        return report;
    }

    @Override
    public SellerReport updateSellerReport(SellerReport sellerReport) throws SellerException {
        return sellerReportRepository.save(sellerReport);
    }
}
