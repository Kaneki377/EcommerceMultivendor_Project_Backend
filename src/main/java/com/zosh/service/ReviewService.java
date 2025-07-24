package com.zosh.service;

import com.zosh.model.Customer;
import com.zosh.model.Product;
import com.zosh.model.Review;
import com.zosh.request.CreateReviewRequest;

import java.util.List;

public interface ReviewService {

    Review createReview(CreateReviewRequest request,
                        Customer customer,
                        Product product);

    List<Review> getReviewByProductId(Long productId);

    Review updatedReview(Long reviewId, String reviewText,
                         double rating, Long customerId) throws Exception;

    void deleteReview(Long reviewId, Long customerId) throws Exception;

    Review getReviewById(Long reviewId) throws Exception;
}
