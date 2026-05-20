package org.example.controller;

import com.fasterxml.jackson.annotation.JsonAlias;

public record UpdateLostItemRequest(
        @JsonAlias({"name", "itemName"})
        String name,
        String itemName,
        String category,
        @JsonAlias({"place", "location"})
        String place,
        String location,
        @JsonAlias({"foundAt", "lostAt", "time"})
        String foundAt,
        String storage,
        String memo,
        @JsonAlias({"image", "imageUrl", "photoUrl"})
        String image,
        String itemType
) {
}
