package com.zosh.controller;

import com.zosh.exceptions.ProductException;
import com.zosh.model.Product;
import com.zosh.model.Seller;
import com.zosh.request.CreateProductRequest;
import com.zosh.service.ProductService;
import com.zosh.service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sellers/products")
public class SellerProductController {

    private final ProductService productService;

    private final SellerService sellerService;

    @GetMapping()
    public ResponseEntity<List<Product>> getProductBySellerId(
            @RequestHeader("Authorization") String jwt) throws Exception {
        Seller seller = sellerService.getSellerProfile(jwt);

        List<Product> products = productService.getProductBySellerId(seller.getId());

        return new ResponseEntity<>(products, HttpStatus.OK);

    }

    @PostMapping()
    public ResponseEntity<Product> createProduct(
            @RequestBody CreateProductRequest request,
            @RequestHeader("Authorization") String jwt) throws Exception {
        Seller seller = sellerService.getSellerProfile(jwt);

        Product product = productService.createProduct(request, seller);
        return new ResponseEntity<>(product, HttpStatus.CREATED);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Product> deleteProduct(@PathVariable Long productId) throws ProductException {
        try{
            productService.deleteProduct(productId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch(Exception e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long productId,
                                                 @RequestBody Product product) {
        try {
            Product updatedProduct = productService.updateProduct(productId, product);
            return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
        }catch(ProductException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


}
