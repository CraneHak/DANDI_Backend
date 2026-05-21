package org.example.report;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 프론트 검수 저장 PATCH body (status=pending 유지 + 검수 필드).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UpdateReportDetailsRequest(
        String itemName,
        String name,
        String category,
        String itemType,
        String lostAt,
        String foundAt,
        String location,
        String place,
        String storage,
        String memo,
        String image,
        String imageUrl,
        String photoUrl,
        String mosaicImageUrl,
        String status,
        String reportStatus
) {
    public String resolvedItemName() {
        if (itemName != null && !itemName.isBlank()) {
            return itemName.trim();
        }
        if (name != null && !name.isBlank()) {
            return name.trim();
        }
        return null;
    }

    public String resolvedCategory() {
        if (category != null && !category.isBlank()) {
            return category.trim();
        }
        if (itemType != null && !itemType.isBlank()) {
            return itemType.trim();
        }
        return null;
    }

    public String resolvedLocation() {
        if (location != null && !location.isBlank()) {
            return location.trim();
        }
        if (place != null && !place.isBlank()) {
            return place.trim();
        }
        return null;
    }

    public String resolvedLostAt() {
        if (lostAt != null && !lostAt.isBlank()) {
            return lostAt.trim();
        }
        if (foundAt != null && !foundAt.isBlank()) {
            return foundAt.trim();
        }
        return null;
    }

    public String resolvedImage() {
        if (image != null && !image.isBlank()) {
            return image.trim();
        }
        if (imageUrl != null && !imageUrl.isBlank()) {
            return imageUrl.trim();
        }
        if (photoUrl != null && !photoUrl.isBlank()) {
            return photoUrl.trim();
        }
        if (mosaicImageUrl != null && !mosaicImageUrl.isBlank()) {
            return mosaicImageUrl.trim();
        }
        return null;
    }
}
