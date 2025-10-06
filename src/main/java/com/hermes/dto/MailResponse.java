package com.hermes.dto;

import com.hermes.entity.MailLog;
import java.time.LocalDateTime;

public record MailResponse(
    Long id,
    String recipient,
    String subject,
    MailLog.MailStatus status,
    String errorMessage,
    LocalDateTime sentAt,
    LocalDateTime createdAt
) {

    public static MailResponse from(MailLog mailLog) {
        return new MailResponse(
            mailLog.getId(),
            mailLog.getRecipient(),
            mailLog.getSubject(),
            mailLog.getStatus(),
            mailLog.getErrorMessage(),
            mailLog.getSentAt(),
            mailLog.getCreatedAt()
        );
    }
}