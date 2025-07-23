package com.zosh.service;

import com.zosh.model.Seller;
import com.zosh.model.SellerReport;

public interface SellerReportService {
    SellerReport getSellerReport(Seller seller) throws Exception;
    SellerReport updateSellerReport(SellerReport sellerReport) throws Exception;
}
