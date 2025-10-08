package com.hermes.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hermes.entity.MailTemplate;
import java.time.LocalDateTime;

public record MailTemplateResponse(
    Long id,
    String name,
    String subject,
    String content,
    Boolean isHtml,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    LocalDateTime createdAt,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    LocalDateTime updatedAt
) {

    public static MailTemplateResponse from(MailTemplate template) {
        return new MailTemplateResponse(
            template.getId(),
            template.getName(),
            template.getSubject(),
            template.getContent(),
            template.getIsHtml(),
            template.getCreatedAt(),
            template.getUpdatedAt()
        );
    }
}