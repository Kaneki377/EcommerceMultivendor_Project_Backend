package com.zosh.cart;

import com.zosh.model.*;
import com.zosh.repository.CartItemRepository;
import com.zosh.repository.CartRepository;
import com.zosh.service.impl.CartItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import java.util.Optional;

@SpringBootTest
@Transactional
public class CartItemServiceImplTest {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemServiceImpl cartItemService;

    private CartItem savedCartItem;
    private Customer customer;

    @BeforeEach
    public void setUp() {
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();

        customer = new Customer();
        customer.setId(2L);

        Product product = new Product();
        product.setId(1L);
        product.setMrpPrice(100);
        product.setSellingPrice(80);

        Cart cart = new Cart();
        cart.setCustomer(customer);
        cartRepository.save(cart);

        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setCustomerId(customer.getId());
        cartItem.setQuantity(2);
        cartItem.setMrpPrice(200);
        cartItem.setSellingPrice(160);

        savedCartItem = cartItemRepository.save(cartItem);
    }

    @Test
    public void testUpdateCartItem_Success() throws Exception {
        CartItem newItem = new CartItem();
        newItem.setQuantity(3);

        CartItem updated = cartItemService.updateCartItem(customer.getId(), savedCartItem.getId(), newItem);

        assertThat(updated.getQuantity()).isEqualTo(3);
        assertThat(updated.getSellingPrice()).isEqualTo(240); // 3 * 80
    }

    @Test
    public void testRemoveCartItem_Success() throws Exception {
        cartItemService.removeCartItem(customer.getId(), savedCartItem.getId());

        Optional<CartItem> result = cartItemRepository.findById(savedCartItem.getId());
        assertThat(result).isEmpty();
    }

    @Test
    public void testFindCartItemById_Success() throws Exception {
        CartItem item = cartItemService.findCartItemById(savedCartItem.getId());

        assertThat(item.getId()).isEqualTo(savedCartItem.getId());
    }
}
