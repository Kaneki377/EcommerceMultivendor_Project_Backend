package com.zosh.service.impl;

import com.zosh.model.Customer;
import com.zosh.model.Product;
import com.zosh.model.Review;
import com.zosh.repository.ReviewRepository;
import com.zosh.request.CreateReviewRequest;
import com.zosh.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;

    @Override
    public Review createReview(CreateReviewRequest request, Customer customer, Product product) {
       Review review = new Review();
       review.setCustomer(customer);
       review.setProduct(product);
       review.setReviewText(request.getReviewText());
       review.setRating(review.getRating());
       review.setProductImages(request.getProductImages());

       product.getReviews().add(review);

        return reviewRepository.save(review);
    }

    @Override
    public List<Review> getReviewByProductId(Long productId) {
        return reviewRepository.findByProductId(productId);
    }

    @Override
    public Review updatedReview(Long reviewId, String reviewText, double rating, Long customerId) throws Exception {
        Review review = getReviewById(reviewId);

        if(review.getCustomer().getId().equals(customerId)) {
            review.setReviewText(reviewText);
            review.setRating(rating);
            return reviewRepository.save(review);
        }
        throw new Exception("You can't update this review");
    }

    @Override
    public void deleteReview(Long reviewId, Long customerId) throws Exception {
        Review review = getReviewById(reviewId);
        if(review.getCustomer().getId().equals(customerId)) {
            throw new Exception("You can't delete this review");
        }
        reviewRepository.delete(review);
    }

    @Override
    public Review getReviewById(Long reviewId) throws Exception {
        return reviewRepository.findById(reviewId).orElseThrow(()->
                new Exception("Review not found..."));
    }
}
