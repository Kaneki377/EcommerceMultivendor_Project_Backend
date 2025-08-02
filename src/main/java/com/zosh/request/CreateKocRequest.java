package com.zosh.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateKocRequest {

    @NotNull
    private Long customerId;

    @Size(max = 255)
    private String facebookLink;

    @Size(max = 255)
    private String instagramLink;

    @Size(max = 255)
    private String tiktokLink;

    @Size(max = 255)
    private String youtubeLink;

    public boolean hasAtLeastOneLink() {
        return (facebookLink != null && !facebookLink.isBlank()) ||
                (instagramLink != null && !instagramLink.isBlank()) ||
                (tiktokLink != null && !tiktokLink.isBlank()) ||
                (youtubeLink != null && !youtubeLink.isBlank());
    }
}
