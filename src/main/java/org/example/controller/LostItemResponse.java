package org.example.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.example.entity.ItemStatus;
import org.example.entity.LostItem;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LostItemResponse(
        Integer id,
        Long reportId,
        String name,
        String itemName,
        String category,
        String place,
        String location,
        String foundAt,
        String lostAt,
        String storage,
        String imageUrl,
        String image,
        String photoUrl,
        String memo,
        ItemStatus status
) {
    public static LostItemResponse from(LostItem item) {
        String category = item.getItemType();
        if (category == null && item.getCategory() != null) {
            category = item.getCategory().getName();
        }
        String place = item.getFoundLocation() != null ? item.getFoundLocation() : item.getLostLocation();
        String foundAt = item.getStoredDate() != null ? item.getStoredDate().toString() : null;
        String imageUrl = item.getImageUrl();
        return new LostItemResponse(
                item.getId(),
                item.getReportId(),
                item.getItemName(),
                item.getItemName(),
                category,
                place,
                place,
                foundAt,
                foundAt,
                item.getStoredLocation(),
                imageUrl,
                imageUrl,
                imageUrl,
                item.getContact() != null && !item.getContact().isBlank() ? item.getContact() : null,
                item.getStatus()
        );
    }
}
