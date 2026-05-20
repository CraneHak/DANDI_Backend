package org.example.repository;

import org.example.entity.LostItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LostItemRepository extends JpaRepository<LostItem, Integer> {
    Optional<LostItem> findByReportId(Long reportId);
}
