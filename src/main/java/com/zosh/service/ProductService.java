package com.zosh.service;

import com.zosh.exceptions.ProductException;
import com.zosh.model.Product;
import com.zosh.model.Seller;
import com.zosh.request.CreateProductRequest;
import org.springframework.data.domain.Page;


import java.util.List;

public interface ProductService {

    public Product createProduct(CreateProductRequest request, Seller seller);

    public void deleteProduct(Long productId) throws ProductException;

    public Product updateProduct(Long productId, Product product) throws ProductException;

    public Product updateProductStock(Long productId)throws ProductException;

    public Product findProductById(Long productId) throws ProductException;

    List<Product> searchProducts(String query);

    public Page<Product> getAllProducts(
            String category,
            String brand,
            String colors,
            String sizes,
            Integer minPrice,
            Integer maxPrice,
            Integer minDiscount,
            String sort,
            String stock,
            Integer pageNumber
    );

    List<Product> getProductBySellerId(Long sellerId);

}
