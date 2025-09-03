package com.zosh.controller;

import com.zosh.domain.AccountStatus;
import com.zosh.domain.ProductStatus;
import com.zosh.dto.KocDto;
import com.zosh.exceptions.SellerException;
import com.zosh.exceptions.UserException;
import com.zosh.model.HomeCategory;
import com.zosh.model.Koc;
import com.zosh.model.Seller;
import com.zosh.model.User;
import com.zosh.repository.ProductRepository;
import com.zosh.service.HomeCategoryService;
import com.zosh.service.KocService;
import com.zosh.service.SellerService;
import com.zosh.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class ManagerController {

    private final SellerService sellerService;
    private final UserService userService;
    private final HomeCategoryService homeCategoryService;
    private final KocService kocService;
    private final ProductRepository productRepository;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<User> getUserProfile(@RequestHeader("Authorization") String jwt) throws UserException {
        System.out.println("getUserProfile");
        User user = userService.findUserProfileByJwt(jwt);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PatchMapping("/seller/{id}/status/{status}")
    public ResponseEntity<Seller> updateSellerStatus(
            @PathVariable Long id,
            @PathVariable AccountStatus status,
            @RequestParam(name = "restoreProducts", defaultValue = "false") boolean restoreProducts)
            throws SellerException {

        Seller updatedSeller = sellerService.updateSellerAccountStatus(id, status, restoreProducts);
        long activeCount = productRepository.countBySellerIdAndStatus(id, ProductStatus.ACTIVE);
        return ResponseEntity.ok(updatedSeller);

    }

    @GetMapping("/home-category")
    public ResponseEntity<List<HomeCategory>> getHomeCategory() throws Exception {

        List<HomeCategory> categories = homeCategoryService.getAllHomeCategories();
        return ResponseEntity.ok(categories);

    }

    @PatchMapping("/home-category/{id}")
    public ResponseEntity<HomeCategory> updateHomeCategory(
            @PathVariable Long id,
            @RequestBody HomeCategory homeCategory) throws Exception {

        HomeCategory updatedCategory = homeCategoryService.updateHomeCategory(homeCategory, id);
        return ResponseEntity.ok(updatedCategory);

    }

    @GetMapping("/koc")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> getAllKoc(
            @RequestParam(required = false) AccountStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        var pageable = org.springframework.data.domain.PageRequest.of(page, size);
        var result = kocService.getAll(status, pageable);

        return ResponseEntity.ok(result); // trả Page<Koc>
    }

    @GetMapping("/koc/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> getKocById(@PathVariable Long id) {
        return ResponseEntity.ok(kocService.getById(id));
    }

    @PatchMapping("/koc/{id}/status/{status}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<KocDto> updateKocStatus(
            @PathVariable Long id,
            @PathVariable AccountStatus status) {

        Koc koc = kocService.updateStatus(id, status);

        // map entity -> dto
        KocDto dto = new KocDto();
        dto.setId(koc.getId());
        dto.setName(koc.getCustomer().getFullName());
        dto.setAccountStatus(koc.getAccountStatus());
        dto.setKocCode(koc.getKocCode());
        dto.setCustomerId(koc.getCustomer() != null ? koc.getCustomer().getId() : null);
        dto.setEmail(
                (koc.getCustomer() != null && koc.getCustomer().getAccount() != null)
                        ? koc.getCustomer().getAccount().getEmail()
                        : null);

        return ResponseEntity.ok(dto);
    }

}
