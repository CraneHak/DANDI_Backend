package org.example.controller;

import org.example.entity.LostItem;
import org.example.service.LostItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/lost-items")
public class LostItemController {

    private final LostItemService lostItemService;

    public LostItemController(LostItemService lostItemService) {
        this.lostItemService = lostItemService;
    }

    @GetMapping
    public List<LostItemResponse> getAll() {
        return lostItemService.findAll().stream()
                .map(LostItemResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public LostItemResponse getById(@PathVariable Integer id) {
        return LostItemResponse.from(lostItemService.findById(id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LostItemResponse> createJson(@RequestBody CreateLostItemRequest body) {
        try {
            LostItem saved = lostItemService.createFromRequest(body);
            return ResponseEntity.status(HttpStatus.CREATED).body(LostItemResponse.from(saved));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LostItemResponse> createMultipart(
            @RequestParam(required = false) String itemName,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String itemType,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String place,
            @RequestParam(required = false) String foundLocation,
            @RequestParam(required = false) String storage,
            @RequestParam(required = false) String storedLocation,
            @RequestParam(required = false) String memo,
            @RequestParam(required = false) String contact,
            @RequestParam(required = false) String lostAt,
            @RequestParam(required = false) String foundAt,
            @RequestParam(required = false) String acquiredAt,
            @RequestParam(required = false) String createdAt,
            @RequestParam(required = false) String registeredAt,
            @RequestParam(required = false) String storedDate,
            @RequestParam(required = false) String reportId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String imageUrl,
            @RequestParam(required = false) String photoUrl,
            @RequestParam(required = false) String mosaicImageUrl,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) throws IOException {
        try {
            LostItem saved = lostItemService.createFromMultipart(
                    itemName,
                    name,
                    category,
                    itemType,
                    location,
                    place,
                    foundLocation,
                    storage,
                    storedLocation,
                    memo,
                    contact,
                    lostAt,
                    foundAt,
                    acquiredAt,
                    createdAt,
                    registeredAt,
                    storedDate,
                    reportId,
                    status,
                    color,
                    imageUrl,
                    null,
                    photoUrl,
                    mosaicImageUrl,
                    image,
                    file
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(LostItemResponse.from(saved));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{id}")
    public LostItemResponse patch(@PathVariable Integer id, @RequestBody UpdateLostItemRequest body) {
        return LostItemResponse.from(lostItemService.update(id, body));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            lostItemService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }
}
