package org.example.keyword;

import org.example.entity.ItemStatus;
import org.example.entity.LostItem;
import org.example.notice.NoticeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class KeywordAlertService {

    private final KeywordRepository keywordRepository;
    private final NoticeService noticeService;

    public KeywordAlertService(KeywordRepository keywordRepository, NoticeService noticeService) {
        this.keywordRepository = keywordRepository;
        this.noticeService = noticeService;
    }

    @Transactional
    public void notifyMatchingUsers(LostItem item) {
        if (item == null || item.getStatus() != ItemStatus.ACQUIRED) {
            return;
        }
        String haystack = buildHaystack(item);
        if (haystack.isBlank()) {
            return;
        }

        List<Keyword> keywords = keywordRepository.findAll();
        for (Keyword keyword : keywords) {
            String term = keyword.getKeyword();
            if (term == null || term.isBlank()) {
                continue;
            }
            if (!containsIgnoreCase(haystack, term)) {
                continue;
            }
            String uid = keyword.getRequesterUid();
            if (uid == null || uid.isBlank()) {
                continue;
            }
            noticeService.create(
                    uid,
                    "관심 키워드 알림",
                    "설정하신 관심 키워드와 일치하는 습득물이 등록되었습니다.\n\n"
                            + "키워드: " + term.trim() + "\n"
                            + "물품: " + safe(item.getItemName()) + "\n"
                            + "위치: " + safe(item.getFoundLocation())
            );
        }
    }

    private String buildHaystack(LostItem item) {
        return String.join(
                " ",
                safe(item.getItemName()),
                safe(item.getItemType()),
                safe(item.getFoundLocation()),
                safe(item.getLostLocation()),
                safe(item.getStoredLocation()),
                safe(item.getContact())
        ).toLowerCase(Locale.ROOT);
    }

    private boolean containsIgnoreCase(String haystack, String needle) {
        return haystack.contains(needle.trim().toLowerCase(Locale.ROOT));
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value.trim();
    }
}
