package com.zosh.service;

import com.zosh.exceptions.SellerException;
import com.zosh.model.Seller;
import com.zosh.model.SellerReport;

public interface SellerReportService {
    SellerReport getSellerReport(Seller seller) throws SellerException;
    SellerReport updateSellerReport(SellerReport sellerReport) throws SellerException;
}
