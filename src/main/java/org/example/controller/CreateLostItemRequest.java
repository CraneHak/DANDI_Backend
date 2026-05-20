package org.example.controller;

import com.fasterxml.jackson.annotation.JsonAlias;

public record CreateLostItemRequest(
        @JsonAlias({"reportId", "report_id"})
        Long reportId,
        @JsonAlias({"name", "itemName"})
        String name,
        String itemName,
        String category,
        @JsonAlias({"place", "location", "foundLocation"})
        String place,
        String location,
        @JsonAlias({"foundAt", "lostAt"})
        String foundAt,
        String lostAt,
        String storage,
        String memo,
        @JsonAlias({"image", "imageUrl", "photoUrl"})
        String image,
        String imageUrl,
        String photoUrl,
        String itemType,
        String contact
) {
    public String resolvedName() {
        if (name != null && !name.isBlank()) return name.trim();
        if (itemName != null && !itemName.isBlank()) return itemName.trim();
        return null;
    }

    public String resolvedPlace() {
        if (place != null && !place.isBlank()) return place.trim();
        if (location != null && !location.isBlank()) return location.trim();
        return null;
    }

    public String resolvedFoundAt() {
        if (foundAt != null && !foundAt.isBlank()) return foundAt.trim();
        if (lostAt != null && !lostAt.isBlank()) return lostAt.trim();
        return null;
    }

    public String resolvedImage() {
        if (image != null && !image.isBlank()) return image.trim();
        if (imageUrl != null && !imageUrl.isBlank()) return imageUrl.trim();
        if (photoUrl != null && !photoUrl.isBlank()) return photoUrl.trim();
        return null;
    }
}
