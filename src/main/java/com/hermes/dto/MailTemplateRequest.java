package com.hermes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MailTemplateRequest(
    @NotBlank(message = "템플릿 이름은 필수입니다")
    @Size(max = 100, message = "템플릿 이름은 100자를 초과할 수 없습니다")
    String name,

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 255, message = "제목은 255자를 초과할 수 없습니다")
    String subject,

    @NotBlank(message = "내용은 필수입니다")
    String content,

    Boolean isHtml,

    @NotBlank(message = "groupKey는 필수입니다")
    String groupKey
) {

    public MailTemplateRequest {
        if (isHtml == null) {
            isHtml = true;
        }
    }
}