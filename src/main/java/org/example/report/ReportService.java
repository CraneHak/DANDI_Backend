package org.example.report;

import org.example.entity.ItemStatus;
import org.example.entity.LostItem;
import org.example.notice.NoticeService;
import org.example.service.LostItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final NoticeService noticeService;
    private final LostItemService lostItemService;

    public ReportService(
            ReportRepository reportRepository,
            NoticeService noticeService,
            LostItemService lostItemService
    ) {
        this.reportRepository = reportRepository;
        this.noticeService = noticeService;
        this.lostItemService = lostItemService;
    }

    public List<Report> findAll() {
        return reportRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public Report create(Report report, ReportActor actor) {
        normalizeReportFields(report);
        report.setStatus(ReportStatus.PENDING);
        if (actor != null) {
            report.setReporterUid(actor.uid());
            if (actor.email() != null) {
                report.setReporterEmail(actor.email());
            }
        }
        Report saved = reportRepository.save(report);
        if (actor != null) {
            noticeService.create(
                    actor.uid(),
                    "분실물 신고 접수",
                    "분실물 신고가 접수되었습니다: " + safe(saved.getItemName())
            );
        }
        return saved;
    }

    @Transactional
    public Report updateStatus(Long id, ReportStatus newStatus, ReportActor actor) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ReportNotFoundException(id));
        validateStatusTransition(report.getStatus(), newStatus);
        report.setStatus(newStatus);
        if (newStatus == ReportStatus.PICKED_UP) {
            report.setPickedUpAt(LocalDateTime.now());
        } else if (newStatus != ReportStatus.PICKED_UP) {
            report.setPickedUpAt(null);
        }
        Report saved = reportRepository.save(report);

        String requesterUid = actor != null ? actor.uid() : saved.getReporterUid();
        if (requesterUid != null && !requesterUid.isBlank()) {
            String title = switch (newStatus) {
                case RESOLVED -> "습득 완료";
                case PICKED_UP -> "최종 수령 완료";
                case UNAVAILABLE -> "습득 불가";
                default -> "신고 상태 변경";
            };
            String message = switch (newStatus) {
                case RESOLVED -> "습득이 확인되어 홈에 공개되었습니다: " + safe(saved.getItemName());
                case UNAVAILABLE -> "아직 습득되지 않았습니다: " + safe(saved.getItemName());
                case PICKED_UP -> "물품 수령이 완료되었습니다: " + safe(saved.getItemName());
                default -> "신고 상태가 변경되었습니다: " + safe(saved.getItemName());
            };
            noticeService.create(requesterUid, title, message);
        }

        if (newStatus == ReportStatus.RESOLVED) {
            lostItemService.upsertFromReport(saved);
        }

        return saved;
    }

    @Transactional
    public void delete(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ReportNotFoundException(id));
        if (report.getStatus() == ReportStatus.PICKED_UP) {
            throw new IllegalStateException("최종 수령 완료된 신고는 삭제할 수 없습니다.");
        }
        lostItemService.deleteByReportId(id);
        reportRepository.deleteById(id);
    }

    private void normalizeReportFields(Report report) {
        report.setItemName(trimToNull(report.getItemName()));
        report.setCategory(trimToNull(report.getCategory()));
        report.setLostAt(trimToNull(report.getLostAt()));
        report.setLocation(trimToNull(report.getLocation()));
        report.setStorage(trimToNull(report.getStorage()));
        report.setMemo(trimToNull(report.getMemo()));
        if (report.getItemName() == null) {
            throw new IllegalArgumentException("itemName is required.");
        }
        if (report.getLocation() == null) {
            throw new IllegalArgumentException("location is required.");
        }
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void validateStatusTransition(ReportStatus from, ReportStatus to) {
        if (from == to) return;
        if (from == ReportStatus.PICKED_UP && to != ReportStatus.PICKED_UP) {
            throw new IllegalStateException("최종 수령 완료 상태에서는 되돌릴 수 없습니다.");
        }
        if (from == ReportStatus.UNAVAILABLE && to == ReportStatus.PICKED_UP) {
            throw new IllegalStateException("습득 불가 상태에서는 바로 수령 완료로 변경할 수 없습니다.");
        }
    }

    private String safe(String value) {
        return value == null ? "미상 물품" : value;
    }
}
