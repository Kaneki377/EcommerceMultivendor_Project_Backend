package com.zosh.product;


import com.zosh.model.Product;
import com.zosh.model.Seller;
import com.zosh.repository.CategoryRepository;
import com.zosh.repository.ProductRepository;
import com.zosh.request.CreateProductRequest;
import com.zosh.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Rollback
public class ProductServiceImplTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    public void testCreateProduct_WithDB() {
        CreateProductRequest request = new CreateProductRequest();
        request.setTitle("Integration Test Product");
        request.setDescription("Integration test desc");
        request.setMrpPrice(1000);
        request.setSellingPrice(800);
        request.setCategory("C01");
        request.setCategory2("C02");
        request.setCategory3("C03");
        request.setImages(List.of("img.jpg"));
        request.setSizes(String.valueOf(List.of("M")));
        request.setColor("Red");

        Seller seller = new Seller();
        seller.setId(1L); // Bạn cần chắc chắn ID 1L có trong DB
        Product product = productService.createProduct(request, seller);

        assertThat(product.getId()).isNotNull();
        assertThat(product.getTitle()).isEqualTo("Integration Test Product");
        System.out.println("Created product ID: " + product.getId());
    }

    @Test
    public void testFindProductById_WithDB() {
        Product p = productService.findProductById(1L);
        assertThat(p).isNotNull();
        System.out.println(p.getTitle());
    }

    @Test
    public void testGetProductBySellerId_WithDB() {
        List<Product> list = productService.getProductBySellerId(1L);
        assertThat(list).isNotNull();
        System.out.println("Product count: " + list.size());
    }
}
