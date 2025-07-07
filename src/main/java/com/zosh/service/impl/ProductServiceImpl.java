package com.zosh.service.impl;

import com.zosh.model.Category;
import com.zosh.model.Product;
import com.zosh.model.Seller;
import com.zosh.repository.CategoryRepository;
import com.zosh.repository.ProductRepository;
import com.zosh.request.CreateProductRequest;
import com.zosh.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    private final CategoryRepository categoryRepository;

    @Override
    public Product createProduct(CreateProductRequest request, Seller seller) {
        Category category1 = categoryRepository.findByCategoryId(request.getCategory());

        if (category1 == null) {
            Category category = new Category();
            category.setCategoryId(request.getCategory());
            category.setLevel(1);
            category1 = categoryRepository.save(category);
        }

        Category category2 = categoryRepository.findByCategoryId(request.getCategory2());

        if (category2 == null) {
            Category category = new Category();
            category.setCategoryId(request.getCategory());
            category.setLevel(2);
            category.setParentCategory(category1);
            category2 = categoryRepository.save(category);
        }

        Category category3 = categoryRepository.findByCategoryId(request.getCategory3());

        if (category3 == null) {
            Category category = new Category();
            category.setCategoryId(request.getCategory());
            category.setLevel(3);
            category.setParentCategory(category2);
            category3 = categoryRepository.save(category);
        }

        int discountPercentage = calculateDiscountPercentage(request.getMrpPrice(), request.getSellingPrice());

        Product product = new Product();
        product.setSeller(seller);
        product.setCategory(category3);
        product.setDescription(request.getDescription());
        product.setCreatedAt(LocalDateTime.now());
        product.setTitle(request.getTitle());
        product.setColor(request.getColor());
        product.setMrpPrice(request.getMrpPrice());
        product.setSellingPrice(request.getSellingPrice());
        product.setImages(request.getImages());
        product.setSizes(request.getSizes());
        product.setDiscountPercent(discountPercentage);

        return productRepository.save(product);
    }

    private int calculateDiscountPercentage(int mrpPrice, int sellingPrice) {
        if(mrpPrice <= 0) {
            throw new IllegalArgumentException("Actual price must be greater than zero");
        }
        double discount = mrpPrice - sellingPrice;
        double discountPercentage = (discount / mrpPrice) * 100;
        return (int) discountPercentage;
    }

    @Override
    public void deleteProduct(Long productId) {
        Product product = findProductById(productId);
        productRepository.delete(product);
    }

    @Override
    public Product updateProduct(Long productId, Product product) {
        findProductById(productId);
        product.setId(productId);

        return productRepository.save(product);
    }

    @Override
    public Product findProductById(Long productId) {
        return null;
    }

    @Override
    public List<Product> searchProducts() {
        return List.of();
    }

    @Override
    public Page<Product> getAllProducts(String category, String brand, String colors, String sizes, String minPrice, String maxPrice, Integer minDiscount, String sort, String stock, Integer pageNumber) {
        return null;
    }

    @Override
    public List<Product> getProductBySellerId(Long sellerId) {
        return List.of();
    }
}
