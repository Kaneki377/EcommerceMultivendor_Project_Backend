package com.zosh.service.impl;

import com.zosh.exceptions.ReviewNotFoundException;
import com.zosh.model.Customer;
import com.zosh.model.OrderItem;
import com.zosh.model.Product;
import com.zosh.model.Review;
import com.zosh.repository.OrderItemRepository;
import com.zosh.repository.OrderRepository;
import com.zosh.repository.ReviewRepository;
import com.zosh.request.CreateReviewRequest;
import com.zosh.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private  final OrderItemRepository orderItemRepository;
    @Override
    public Review createReview(CreateReviewRequest request, Customer customer, Product product) throws ReviewNotFoundException {
        if (!orderItemRepository.customerArrivedOrderForProduct(customer.getId(), product.getId())) {
            throw new ReviewNotFoundException("You can only review after delivery");
        }
        if (reviewRepository.existsByCustomer_IdAndProduct_Id(customer.getId(), product.getId())) {
            throw new ReviewNotFoundException("You already reviewed this product");
        }

        Review newReview = new Review();

        newReview.setReviewText(request.getReviewText());
        newReview.setRating(request.getReviewRating());
        newReview.setProductImages(request.getProductImages());
        newReview.setCustomer(customer);
        newReview.setProduct(product);

        product.getReviews().add(newReview);

        return reviewRepository.save(newReview);
    }

    @Override
    public List<Review> getReviewByProductId(Long productId) {

        return reviewRepository.findReviewsByProductId(productId);
    }

    @Override
    public Review updatedReview(Long reviewId, String reviewText, double rating, Long customerId) throws ReviewNotFoundException, AuthenticationException {
        Review review=reviewRepository.findById(reviewId)
                .orElseThrow(()-> new ReviewNotFoundException("Review Not found"));

        if(review.getCustomer().getId().equals(customerId)) {
            review.setReviewText(reviewText);
            review.setRating(rating);
            return reviewRepository.save(review);
        }
        throw new AuthenticationException("You do not have permission to delete this review");
    }

    @Override
    public void deleteReview(Long reviewId, Long customerId) throws ReviewNotFoundException,
            AuthenticationException {
        Review review=reviewRepository.findById(reviewId)
                .orElseThrow(()-> new ReviewNotFoundException("Review Not found"));
        if(!review.getCustomer().getId().equals(customerId)) {
            throw new AuthenticationException("You do not have permission to delete this review");
        }
        reviewRepository.delete(review);
    }

    @Override
    public Review getReviewById(Long reviewId) throws Exception {
        return reviewRepository.findById(reviewId).orElseThrow(()->
                new Exception("Review not found..."));
    }
}
