package com.zosh.service.impl;

import com.zosh.domain.HomeCategorySection;
import com.zosh.model.Deal;
import com.zosh.model.Home;
import com.zosh.model.HomeCategory;
import com.zosh.repository.DealRepository;
import com.zosh.repository.HomeCategoryRepository;
import com.zosh.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private final DealRepository dealRepository;
    private final HomeCategoryRepository homeCategoryRepository;

    @Override
    public Home creatHomePageData(List<HomeCategory> allCategories) {

        List<HomeCategory> gridCategories = allCategories.stream()
                .filter(category ->
                        category.getSection() == HomeCategorySection.GRID)
                .collect(Collectors.toList());

        List<HomeCategory> shopByCategories = allCategories.stream()
                .filter(category ->
                        category.getSection() == HomeCategorySection.SHOP_BY_CATEGORIES)
                .collect(Collectors.toList());

        List<HomeCategory> electricCategories = allCategories.stream()
                .filter(category ->
                        category.getSection() == HomeCategorySection.ELECTRIC_CATEGORIES)
                .collect(Collectors.toList());

        List<HomeCategory> dealCategories = allCategories.stream()
                .filter(category -> category.getSection() == HomeCategorySection.DEALS)
                .toList();

        List<Deal> createdDeals = new ArrayList<>();

        if (dealRepository.findAll().isEmpty()) {
            List<Deal> deals = allCategories.stream()
                    .filter(category -> category.getSection() == HomeCategorySection.DEALS)
                    .map(category -> new Deal(null, 10, category))  // Assuming a discount of 10 for each deal
                    .collect(Collectors.toList());
            createdDeals = dealRepository.saveAll(deals);
        } else createdDeals = dealRepository.findAll();


        Home home = new Home();
        home.setGrid(gridCategories);
        home.setShopByCategories(shopByCategories);
        home.setElectricCategories(electricCategories);
        home.setDeals(createdDeals);
        home.setDealCategories(dealCategories);

        return home;
    }

    @Override
    public Home getHomePageData() {
        // Lấy tất cả category từ DB rồi group theo section
        List<HomeCategory> all = homeCategoryRepository.findAll();

        var bySection = all.stream()
                .collect(Collectors.groupingBy(HomeCategory::getSection));

        List<HomeCategory> grid =
                bySection.getOrDefault(HomeCategorySection.GRID, List.of());
        List<HomeCategory> shopByCategories =
                bySection.getOrDefault(HomeCategorySection.SHOP_BY_CATEGORIES, List.of());
        List<HomeCategory> electricCategories =
                bySection.getOrDefault(HomeCategorySection.ELECTRIC_CATEGORIES, List.of());
        List<HomeCategory> dealCategories =
                bySection.getOrDefault(HomeCategorySection.DEALS, List.of());

        // Deals: chỉ đọc, không tạo mới ở GET
        List<Deal> deals = dealRepository.findAll();

        Home home = new Home();
        home.setGrid(grid);
        home.setShopByCategories(shopByCategories);
        home.setElectricCategories(electricCategories);
        home.setDealCategories(dealCategories);
        home.setDeals(deals);
        return home;
    }
}
