package com.zosh.repositoryTest;

import com.zosh.model.Role;
import com.zosh.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

//DataJpaTest Tự động rollback dữ liệu sau mỗi test, nên không ảnh hưởng DB thật
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(false)
public class RoleRepositoryTest {
    @Autowired
    private RoleRepository repo;

    @Test
    public void testCreateFirstRole() {
        Role roleCustomer = new Role("Customer"); //Kiểm tra xem việc tạo một Role có lưu thành công vào DB hay không
        Role saveRole = repo.save(roleCustomer);

        assertThat(saveRole.getId()).isGreaterThan(0);
    }

    @Test
    public void testCreateRoles() {
        Role roleSeller = new Role("Seller");

        Role roleShipper = new Role("Shipper");

        Role roleManager = new Role("Manager");

        Role roleKOC = new Role("KOC");

        repo.saveAll(List.of(roleSeller,roleShipper,roleManager,roleKOC));
    }
}
