package com.forkmyfolio.repository;

import com.forkmyfolio.model.VisitorStats;
import com.forkmyfolio.model.enums.VisitorStatType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VisitorStatsRepository extends JpaRepository<VisitorStats, Long> {
    Optional<VisitorStats> findByTypeAndRefId(VisitorStatType type, String refId);
}