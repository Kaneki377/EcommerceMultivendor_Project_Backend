package com.zosh.controller;

import com.zosh.exceptions.CustomerException;
import com.zosh.exceptions.ProductException;
import com.zosh.model.Customer;
import com.zosh.model.Product;
import com.zosh.model.Review;
import com.zosh.model.User;
import com.zosh.request.CreateReviewRequest;
import com.zosh.response.ApiResponse;
import com.zosh.service.CustomerService;
import com.zosh.service.ProductService;
import com.zosh.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;

    private final CustomerService customerService;

    private final ProductService productService;

    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<List<Review>> getReviewsByProductId(
            @PathVariable Long productId) {
        List<Review> reviews = reviewService.getReviewByProductId(productId);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping("/products/{productId}/reviews")
    public ResponseEntity<Review> writeReview(
            @RequestBody CreateReviewRequest req,
            @PathVariable Long productId,
            @RequestHeader("Authorization") String jwt) throws CustomerException, ProductException {

        Customer customer = customerService.findCustomerByJwtToken(jwt);
        Product product = productService.findProductById(productId);

        Review review = reviewService.createReview(
                req, customer, product
        );
        return ResponseEntity.ok(review);

    }

    @PatchMapping("/reviews/{reviewId}")
    public ResponseEntity<Review> updateReview(
            @RequestBody CreateReviewRequest req,
            @PathVariable Long reviewId,
            @RequestHeader("Authorization") String jwt)
            throws Exception {

        Customer customer = customerService.findCustomerByJwtToken(jwt);

        Review review = reviewService.updatedReview(
                reviewId,
                req.getReviewText(),
                req.getReviewRating(),
                customer.getId()
        );
        return ResponseEntity.ok(review);

    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse> deleteReview(
            @PathVariable Long reviewId,
            @RequestHeader("Authorization") String jwt) throws
            Exception {

        Customer customer = customerService.findCustomerByJwtToken(jwt);

        reviewService.deleteReview(reviewId, customer.getId());
        ApiResponse res = new ApiResponse();
        res.setMessage("Review deleted successfully");

        return ResponseEntity.ok(res);

    }
}
