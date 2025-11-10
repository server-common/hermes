package com.hermes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record TemplateMailRequest(
    @NotBlank(message = "수신자 이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    String to,

    @NotBlank(message = "템플릿 이름은 필수입니다")
    String templateName,

    Map<String, String> variables,

    @NotBlank(message = "groupKey는 필수입니다")
    String groupKey
) {

}