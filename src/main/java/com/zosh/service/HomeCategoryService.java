package com.zosh.service;

import com.zosh.model.HomeCategory;

import java.util.List;

public interface HomeCategoryService {
    HomeCategory createHomeCategory(HomeCategory homeCategory);

    List<HomeCategory> createCategories(List<HomeCategory> homeCategories);

    HomeCategory updateHomeCategory(HomeCategory homeCategory, Long id) throws Exception;

    List<HomeCategory> getAllHomeCategories();
}
