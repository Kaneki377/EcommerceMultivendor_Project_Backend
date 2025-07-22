package com.zosh.cart;

import com.zosh.model.*;

import com.zosh.repository.CartRepository;
import com.zosh.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Transactional
public class CartServiceImplTest {


    @Autowired
    private CartRepository cartRepository;


    private Customer customer;
    private Product product;
    @Autowired
    private CartService cartService;

    @BeforeEach
    public void setUp() {
        customer = new Customer();
        customer.setId(1L);

        product = new Product();
        product.setId(1L);
        product.setMrpPrice(100);
        product.setSellingPrice(80);

        Cart cart = new Cart();
        cart.setCustomer(customer);
        cart.setCartItems(new HashSet<>());
        cartRepository.save(cart);
    }

    @Test
    public void testAddCartItem_NewItem() {
        CartItem savedItem = cartService.addCartItem(customer, product, "M", 2);

        assertThat(savedItem.getProduct()).isEqualTo(product);
        assertThat(savedItem.getSize()).isEqualTo("M");
        assertThat(savedItem.getQuantity()).isEqualTo(2);
        assertThat(savedItem.getSellingPrice()).isEqualTo(160);
    }

    @Test
    public void testFindCustomerCart_Empty() {
        Cart cart = cartService.findCustomerCart(customer);
        assertThat(cart).isNotNull();
        assertThat(cart.getTotalItem()).isEqualTo(0);
        assertThat(cart.getTotalMrpPrice()).isEqualTo(0);
        assertThat(cart.getTotalSellingPrice()).isEqualTo(0);
    }
}
