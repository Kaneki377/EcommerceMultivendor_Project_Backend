package com.zosh.product;


import com.zosh.exceptions.SellerException;
import com.zosh.model.Product;
import com.zosh.model.Seller;

import com.zosh.repository.SellerRepository;
import com.zosh.request.CreateProductRequest;
import com.zosh.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

@SpringBootTest
@Transactional
public class ProductServiceImplTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private SellerRepository sellerRepository;

    @Test
    void testCreateProduct() throws SellerException {
        Seller seller = new Seller();
        seller.setSellerName("Seller Test");
        seller = sellerRepository.save(seller);

        CreateProductRequest req = new CreateProductRequest();
        req.setTitle("Test Product");
        req.setDescription("Great product");
        req.setMrpPrice(1000);
        req.setSellingPrice(900);
        req.setColor("Black");
        req.setSizes(String.valueOf(List.of("M")));
        req.setImages(List.of("image1.jpg"));
        req.setCategory("cat-1");
        req.setCategory2("cat-2");
        req.setCategory3("cat-3");

        Product product = productService.createProduct(req, seller);

        assertNotNull(product.getId());
        assertEquals("Test Product", product.getTitle());
        assertEquals("Black", product.getColor());
    }
}
