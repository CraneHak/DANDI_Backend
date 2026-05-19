package org.example.pickup;

import org.example.auth.FirebaseAuthenticationToken;
import org.example.entity.ItemStatus;
import org.example.entity.LostItem;
import org.example.repository.LostItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
public class PickupPassService {
    private static final long DEFAULT_TTL_MINUTES = 10;
    private final LostItemRepository lostItemRepository;
    private final PickupPassRepository pickupPassRepository;
    private final CollectionLogRepository collectionLogRepository;

    public PickupPassService(
            LostItemRepository lostItemRepository,
            PickupPassRepository pickupPassRepository,
            CollectionLogRepository collectionLogRepository
    ) {
        this.lostItemRepository = lostItemRepository;
        this.pickupPassRepository = pickupPassRepository;
        this.collectionLogRepository = collectionLogRepository;
    }

    @Transactional
    public IssuePickupPassResponse issuePass(FirebaseAuthenticationToken requester, IssuePickupPassRequest request) {
        Integer lostItemId = parseLostItemId(request);
        LostItem lostItem = lostItemRepository.findById(lostItemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lost item not found."));

        OffsetDateTime now = OffsetDateTime.now();
        PickupPass activePass = pickupPassRepository.findLatestActivePass(lostItemId, requester.getUid(), now)
                .orElse(null);
        if (activePass != null) {
            return toIssueResponse(activePass, "이미 발급된 유효한 수령 QR이 있습니다.");
        }

        PickupPass pass = new PickupPass();
        pass.setToken(newToken());
        pass.setLostItem(lostItem);
        pass.setRequesterUid(requester.getUid());
        pass.setRequesterEmail(requester.getEmail());
        pass.setIssuedAt(now);
        pass.setExpiresAt(now.plusMinutes(DEFAULT_TTL_MINUTES));

        PickupPass saved = pickupPassRepository.save(pass);
        lostItem.setStatus(ItemStatus.PENDING_RECEIPT);

        CollectionLog issuedLog = new CollectionLog();
        issuedLog.setLostItem(lostItem);
        issuedLog.setPickupPassId(saved.getId());
        issuedLog.setRequesterUid(saved.getRequesterUid());
        issuedLog.setRequesterEmail(saved.getRequesterEmail());
        issuedLog.setOtpToken(saved.getToken());
        issuedLog.setOtpExpiresAt(saved.getExpiresAt());
        issuedLog.setAction(CollectionAction.QR_ISSUED);
        collectionLogRepository.save(issuedLog);

        return toIssueResponse(saved, "수령 QR이 발급되었습니다.");
    }

    @Transactional
    public VerifyPickupPassResponse verifyPass(FirebaseAuthenticationToken verifier, VerifyPickupPassRequest request) {
        requireAdmin(verifier);
        String token = normalizeToken(request.token());
        PickupPass pass = pickupPassRepository.findByTokenForUpdate(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유효하지 않은 QR 토큰입니다."));

        OffsetDateTime now = OffsetDateTime.now();
        if (pass.getUsedAt() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용된 QR 토큰입니다.");
        }
        if (pass.getExpiresAt().isBefore(now)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "만료된 QR 토큰입니다.");
        }

        pass.setUsedAt(now);
        pass.setVerifiedByUid(verifier.getUid());
        pass.setVerifiedByEmail(verifier.getEmail());
        pass.getLostItem().setStatus(ItemStatus.RECEIVED);

        PickupPass saved = pickupPassRepository.save(pass);

        CollectionLog verifyLog = new CollectionLog();
        verifyLog.setLostItem(saved.getLostItem());
        verifyLog.setPickupPassId(saved.getId());
        verifyLog.setRequesterUid(saved.getRequesterUid());
        verifyLog.setRequesterEmail(saved.getRequesterEmail());
        verifyLog.setManagerUid(saved.getVerifiedByUid());
        verifyLog.setManagerEmail(saved.getVerifiedByEmail());
        verifyLog.setOtpToken(saved.getToken());
        verifyLog.setOtpExpiresAt(saved.getExpiresAt());
        verifyLog.setCollectedAt(saved.getUsedAt());
        verifyLog.setAction(CollectionAction.QR_VERIFIED);
        collectionLogRepository.save(verifyLog);

        return new VerifyPickupPassResponse(
                true,
                "수령 인증이 완료되었습니다.",
                String.valueOf(saved.getLostItem().getId()),
                saved.getRequesterEmail(),
                saved.getUsedAt().toString()
        );
    }

    private Integer parseLostItemId(IssuePickupPassRequest request) {
        String rawId = request.lostItemId();
        if (rawId == null || rawId.isBlank()) {
            // Backward compatibility: accept legacy field name.
            rawId = request.reportId();
        }
        if (rawId == null || rawId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "lostItemId is required.");
        }
        try {
            return Integer.valueOf(rawId.trim());
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "lostItemId must be numeric.");
        }
    }

    private String newToken() {
        return "DKU-" + UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT);
    }

    private String normalizeToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "token is required.");
        }
        return rawToken.trim();
    }

    private void requireAdmin(FirebaseAuthenticationToken auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
        if (!isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin role is required.");
        }
    }

    private IssuePickupPassResponse toIssueResponse(PickupPass pass, String message) {
        String lostItemId = String.valueOf(pass.getLostItem().getId());
        return new IssuePickupPassResponse(
                String.valueOf(pass.getId()),
                lostItemId,
                lostItemId,
                pass.getToken(),
                pass.getIssuedAt().toString(),
                pass.getExpiresAt().toString(),
                pass.getUsedAt() == null ? null : pass.getUsedAt().toString(),
                message
        );
    }

    public record IssuePickupPassRequest(String lostItemId, String reportId) {
    }

    public record IssuePickupPassResponse(
            String id,
            String lostItemId,
            String reportId,
            String token,
            String issuedAt,
            String expiresAt,
            String usedAt,
            String message
    ) {
    }

    public record VerifyPickupPassRequest(String token) {
    }

    public record VerifyPickupPassResponse(
            boolean ok,
            String message,
            String reportId,
            String requesterEmail,
            String usedAt
    ) {
    }
}
