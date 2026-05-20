package org.example.common;

import org.example.service.S3Service;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ImageUrlService {

    private static final Pattern DATA_URL = Pattern.compile("^data:(image/[^;]+);base64,(.+)$", Pattern.DOTALL);

    private final S3Service s3Service;

    public ImageUrlService(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    /**
     * data URL은 S3에 올리고, http(s) 및 상대 경로는 그대로 반환합니다.
     */
    public String normalizeForStorage(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String trimmed = raw.trim();
        Matcher matcher = DATA_URL.matcher(trimmed);
        if (matcher.matches()) {
            try {
                String contentType = matcher.group(1);
                byte[] bytes = Base64.getDecoder().decode(matcher.group(2).replaceAll("\\s", ""));
                String ext = contentType.contains("png") ? ".png" : ".jpg";
                return s3Service.uploadBytes(bytes, contentType, "upload" + ext);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Invalid image data URL.");
            }
        }
        if (trimmed.length() > 1024) {
            throw new IllegalArgumentException("Image URL is too long.");
        }
        return trimmed;
    }
}
