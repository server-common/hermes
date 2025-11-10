package com.hermes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MailSettingRequest(
    @NotBlank(message = "설정 키는 필수입니다")
    @Size(max = 100, message = "설정 키는 100자를 초과할 수 없습니다")
    String settingKey,

    @NotBlank(message = "설정 값은 필수입니다")
    String settingValue,

    String description,

    @NotBlank(message = "groupKey는 필수입니다")
    String groupKey
) {

}