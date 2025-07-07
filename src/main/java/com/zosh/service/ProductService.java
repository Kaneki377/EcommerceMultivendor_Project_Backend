package com.zosh.service;

import com.zosh.model.Product;
import com.zosh.model.Seller;
import com.zosh.request.CreateProductRequest;
import org.springframework.data.domain.Page;


import java.util.List;

public interface ProductService {

    public Product createProduct(CreateProductRequest request, Seller seller);

    public void deleteProduct(Long productId);

    public Product updateProduct(Long productId, Product product);

    public Product findProductById(Long productId);

    List<Product> searchProducts();

    public Page<Product> getAllProducts(
            String category,
            String brand,
            String colors,
            String sizes,
            String minPrice,
            String maxPrice,
            Integer minDiscount,
            String sort,
            String stock,
            Integer pageNumber
    );

    List<Product> getProductBySellerId(Long sellerId);

}
