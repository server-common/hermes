package com.hermes.repository;

import com.hermes.entity.MailLog;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MailLogRepository extends JpaRepository<MailLog, Long> {

    Page<MailLog> findByStatus(MailLog.MailStatus status, Pageable pageable);

    long countByStatus(MailLog.MailStatus status);

    long countByStatusAndSentAtAfter(MailLog.MailStatus status, LocalDateTime sentAt);
}