package org.example.report;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.auth.FirebaseAuthenticationToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public List<ReportResponse> getAll() {
        return reportService.findAll().stream()
                .map(ReportResponse::from)
                .toList();
    }

    @PostMapping
    public ResponseEntity<ReportResponse> create(
            Authentication authentication,
            @Valid @RequestBody CreateReportRequest body
    ) {
        try {
            Report report = new Report();
            report.setItemName(body.itemName());
            report.setCategory(body.category());
            report.setLostAt(body.lostAt());
            report.setLocation(body.location());
            report.setStorage(body.storage());
            report.setMemo(mergeMemo(body.memo(), body.ownerName()));
            report.setImage(body.resolvedImage());
            if (body.ownerEmail() != null && !body.ownerEmail().isBlank()) {
                report.setReporterEmail(body.ownerEmail().trim());
            }

            ReportActor actor = toActor(authentication);
            if (actor != null && report.getReporterEmail() == null) {
                report.setReporterEmail(actor.email());
            }

            Report saved = reportService.create(report, actor);
            return ResponseEntity.status(HttpStatus.CREATED).body(ReportResponse.from(saved));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest body
    ) {
        return applyStatusChange(authentication, id, body.status());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateReport(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest body
    ) {
        return applyStatusChange(authentication, id, body.status());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            reportService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage());
        } catch (ReportNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    private ResponseEntity<Map<String, Object>> applyStatusChange(
            Authentication authentication,
            Long id,
            ReportStatus status
    ) {
        try {
            Report saved = reportService.updateStatus(id, status, toActor(authentication));
            return ResponseEntity.ok(Map.of(
                    "message", "상태 변경이 완료되었습니다.",
                    "id", saved.getId(),
                    "status", saved.getStatus().getValue()
            ));
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage());
        } catch (ReportNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    private ReportActor toActor(Authentication authentication) {
        if (!(authentication instanceof FirebaseAuthenticationToken token)) {
            return null;
        }
        return new ReportActor(token.getUid(), token.getEmail(), token.isAdmin());
    }

    private String mergeMemo(String memo, String ownerName) {
        if (ownerName == null || ownerName.isBlank()) {
            return memo;
        }
        if (memo == null || memo.isBlank()) {
            return "신고자: " + ownerName.trim();
        }
        return memo.trim() + " (신고자: " + ownerName.trim() + ")";
    }

    public record CreateReportRequest(
            @NotBlank String itemName,
            String category,
            String lostAt,
            @NotBlank String location,
            String storage,
            String memo,
            String image,
            String imageUrl,
            String photoUrl,
            String ownerEmail,
            String ownerName
    ) {
        public String resolvedImage() {
            if (image != null && !image.isBlank()) return image.trim();
            if (imageUrl != null && !imageUrl.isBlank()) return imageUrl.trim();
            if (photoUrl != null && !photoUrl.isBlank()) return photoUrl.trim();
            return null;
        }
    }

    public record UpdateStatusRequest(
            @NotNull ReportStatus status
    ) {}

    public record ReportResponse(
            String id,
            String itemName,
            String name,
            String category,
            String lostAt,
            String foundAt,
            String location,
            String place,
            String storage,
            String memo,
            String imageUrl,
            String image,
            String photoUrl,
            ReportStatus status,
            String createdAt,
            String pickedUpAt,
            String ownerEmail,
            String ownerName
    ) {
        private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        static ReportResponse from(Report r) {
            String image = r.getImage();
            return new ReportResponse(
                    String.valueOf(r.getId()),
                    r.getItemName(),
                    r.getItemName(),
                    r.getCategory(),
                    r.getLostAt(),
                    r.getLostAt(),
                    r.getLocation(),
                    r.getLocation(),
                    r.getStorage(),
                    r.getMemo(),
                    image,
                    image,
                    image,
                    r.getStatus(),
                    r.getCreatedAt() != null ? r.getCreatedAt().format(FMT) : null,
                    r.getPickedUpAt() != null ? r.getPickedUpAt().format(FMT) : null,
                    r.getReporterEmail(),
                    null
            );
        }
    }
}
