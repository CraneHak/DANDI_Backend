package org.example.notice;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public NoticeService(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    public List<Notice> findAll(String requesterUid) {
        return noticeRepository.findAllByRequesterUidOrderByCreatedAtDesc(requesterUid);
    }

    @Transactional
    public Notice create(String requesterUid, String title, String message) {
        if (requesterUid == null || requesterUid.isBlank()) {
            throw new IllegalArgumentException("requesterUid is required.");
        }
        Notice notice = new Notice();
        notice.setRequesterUid(requesterUid);
        notice.setTitle(title);
        notice.setMessage(message);
        notice.setRead(false);
        return noticeRepository.save(notice);
    }

    @Transactional
    public Notice markRead(String requesterUid, Long id) {
        Notice notice = noticeRepository.findByIdAndRequesterUid(id, requesterUid)
                .orElseThrow(() -> new NoticeNotFoundException(id));
        notice.setRead(true);
        return noticeRepository.save(notice);
    }

    @Transactional
    public void delete(String requesterUid, Long id) {
        Notice notice = noticeRepository.findByIdAndRequesterUid(id, requesterUid)
                .orElseThrow(() -> new NoticeNotFoundException(id));
        noticeRepository.delete(notice);
    }

    @Transactional
    public void deleteAll(String requesterUid) {
        noticeRepository.deleteAllByRequesterUid(requesterUid);
    }
}
