package com.zosh.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddItemRequest {

    @NotBlank
    private String size;

    @Min(1)
    private int quantity;

    @NotNull
    private Long productId;

    @Size(max = 64)
    private String affToken; // optional: short token từ /r/{token}
}
