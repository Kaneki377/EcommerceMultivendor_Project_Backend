package com.zosh.model;

import lombok.Data;

import java.util.List;

@Data
public class Home {

    private List<HomeCategory> grid;

    private List<HomeCategory> shopByCategories;

    private List<HomeCategory> eletricCategories;

    private List<HomeCategory> dealCategories;

    private List<Deal> deals;
}
