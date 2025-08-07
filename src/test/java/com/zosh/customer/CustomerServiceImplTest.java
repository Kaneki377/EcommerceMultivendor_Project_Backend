package com.zosh.customer;

import com.zosh.config.JwtProvider;
import com.zosh.model.Account;
import com.zosh.model.Customer;
import com.zosh.repository.CustomerRepository;
import com.zosh.service.CustomerService;
import com.zosh.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class CustomerServiceImplTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Mock
    private JwtProvider jwtProvider;

    private CustomerService customerService;

    private final String TEST_USERNAME = "johndoe";
    private final String TEST_EMAIL = "john@example.com";

    @BeforeEach
    void setup() {
        customerService = new CustomerServiceImpl(customerRepository, jwtProvider);

        Account acc = new Account();
        acc.setUsername(TEST_USERNAME);
        acc.setEmail(TEST_EMAIL);
        acc.setIsEnabled(true);

        Customer customer = new Customer();
        customer.setFullName("John Doe");
        customer.setAccount(acc);

        customerRepository.save(customer);
    }

    @Test
    void testFindCustomerByEmail() throws Exception {
        Customer customer = customerService.findCustomerByEmail(TEST_EMAIL);
        assertThat(customer).isNotNull();
        assertThat(customer.getAccount().getUsername()).isEqualTo(TEST_USERNAME);
    }

    @Test
    void testFindCustomerByUsername() throws Exception {
        Customer customer = customerService.findCustomerByUsername(TEST_USERNAME);
        assertThat(customer).isNotNull();
        assertThat(customer.getAccount().getEmail()).isEqualTo(TEST_EMAIL);
    }

    @Test
    void testFindCustomerProfileByJwt() throws Exception {
        String fakeJwt = "fake.jwt.token";

        // Giả lập jwtProvider trả về username
        Mockito.when(jwtProvider.getUsernameFromJwtToken(fakeJwt)).thenReturn(TEST_USERNAME);

        Customer customer = customerService.findCustomerProfileByJwt(fakeJwt);
        assertThat(customer).isNotNull();
        assertThat(customer.getAccount().getUsername()).isEqualTo(TEST_USERNAME);
    }
}
