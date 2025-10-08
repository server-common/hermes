package com.hermes.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hermes.entity.MailSetting;
import java.time.LocalDateTime;

public record MailSettingResponse(
    Long id,
    String settingKey,
    String settingValue,
    String description,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    LocalDateTime createdAt,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    LocalDateTime updatedAt
) {

    public static MailSettingResponse from(MailSetting setting) {
        return new MailSettingResponse(
            setting.getId(),
            setting.getSettingKey(),
            setting.getSettingValue(),
            setting.getDescription(),
            setting.getCreatedAt(),
            setting.getUpdatedAt()
        );
    }
}