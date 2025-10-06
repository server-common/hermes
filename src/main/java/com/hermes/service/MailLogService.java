package com.hermes.service;

import com.hermes.entity.MailLog;
import com.hermes.repository.MailLogRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailLogService {

    private final MailLogRepository mailLogRepository;

    @Transactional
    public void updateMailLogStatus(Long mailLogId, MailLog.MailStatus status, String errorMessage) {
        mailLogRepository.findById(mailLogId).ifPresent(mailLog -> {
            mailLog.updateStatus(status);

            if (status == MailLog.MailStatus.SENT) {
                mailLog.updateSentAt(LocalDateTime.now());
            }

            if (errorMessage != null) {
                mailLog.updateErrorMessage(errorMessage);
            }

            mailLogRepository.save(mailLog);
        });
    }

    @Transactional
    public void updateMailLogStatus(MailLog mailLog, MailLog.MailStatus status, String errorMessage) {
        mailLog.updateStatus(status);

        if (status == MailLog.MailStatus.SENT) {
            mailLog.updateSentAt(LocalDateTime.now());
        }

        if (errorMessage != null) {
            mailLog.updateErrorMessage(errorMessage);
        }

        mailLogRepository.save(mailLog);
    }
}