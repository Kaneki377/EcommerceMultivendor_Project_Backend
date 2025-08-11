package com.zosh.repository;

import com.zosh.domain.ProductStatus;
import com.zosh.model.Product;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;


public interface ProductRepository extends JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {

    List<Product> findBySellerId(Long sellerId);

    //tìm kiếm sản phẩm (search) theo từ khóa (gửi từ frontend)
    @Query("""
      select p from Product p 
      join p.seller s
      where ( :query is null 
              or lower(p.title) like lower(concat('%', :query, '%'))
              or lower(p.category.name) like lower(concat('%', :query, '%'))
              or lower(p.category.categoryId) like lower(concat('%', :query, '%'))
            )
        and p.status = com.zosh.domain.ProductStatus.ACTIVE
        and s.accountStatus = com.zosh.domain.AccountStatus.ACTIVE
        and p.in_stock = true 
    """)
    List<Product> searchProduct(@Param("query") String query);


//Ẩn toàn bộ sản phẩm của 1 seller, trừ những sản phẩm đã bị DELETED
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
  update Product p
     set p.status = com.zosh.domain.ProductStatus.HIDDEN
   where p.seller.id = :sellerId
     and p.status <> com.zosh.domain.ProductStatus.DELETED
""")
    int bulkHideBySeller(@Param("sellerId") Long sellerId);

    // Kích hoạt lại toàn bộ sản phẩm đang ở trạng thái HIDDEN của seller.
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
  update Product p
     set p.status = com.zosh.domain.ProductStatus.ACTIVE
   where p.seller.id = :sellerId
     and p.status = com.zosh.domain.ProductStatus.HIDDEN
""")
    int bulkActivateHiddenBySeller(@Param("sellerId") Long sellerId);

    long countBySellerIdAndStatus(Long sellerId, ProductStatus status);

    //khóa product cho đến khi transaction commit/rollback
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> lockById(@Param("id") Long id);

}

