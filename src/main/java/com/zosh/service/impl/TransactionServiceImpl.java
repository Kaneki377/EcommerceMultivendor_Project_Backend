package com.zosh.service.impl;

import com.zosh.model.Order;
import com.zosh.model.Seller;
import com.zosh.model.Transaction;
import com.zosh.repository.PaymentOrderRepository;
import com.zosh.repository.SellerRepository;
import com.zosh.repository.TransactionRepository;
import com.zosh.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    private final SellerRepository sellerRepository;

    @Override
    public Transaction createTransaction(Order order) {
        // Kiểm tra xem đã có transaction cho order này chưa
        List<Transaction> existingTransactions = transactionRepository.findByOrder_Id(order.getId());
        if (!existingTransactions.isEmpty()) {
            return existingTransactions.get(0); // Trả về transaction đã tồn tại
        }

        Seller seller = sellerRepository.findById(order.getSellerId()).get();

        Transaction transaction = new Transaction();
        transaction.setSeller(seller);
        transaction.setCustomer(order.getCustomer());
        transaction.setOrder(order);

        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionBySellerId(Seller seller) {
        return transactionRepository.findBySellerId(seller.getId());
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }
}
