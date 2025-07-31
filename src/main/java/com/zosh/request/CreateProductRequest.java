package com.zosh.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateProductRequest {

    @NotBlank(message = "Title must not be empty")
    @Size(max = 128, message = "Title must not exceed 128 characters")
    private String title;

    @NotBlank(message = "Description must not be empty")
    @Size(max = 1024, message = "Description must not exceed 1024 characters")
    private String description;

    @Min(value = 0, message = "MRP must be greater than or equal to 0")
    private int mrpPrice;

    @Min(value = 0, message = "Selling price must be greater than or equal to 0")
    private int sellingPrice;

    @NotBlank(message = "Color must not be empty")
    private String color;

    private int quantity;

    @NotEmpty(message = "Images list must not be empty")
    private List<@NotBlank(message = "Image URL must not be blank") String> images;

    @NotBlank(message = "Category must not be empty")
    private String category;
    private String category2;
    private String category3;

    @NotBlank(message = "Sizes must not be empty")
    private String sizes;
}
