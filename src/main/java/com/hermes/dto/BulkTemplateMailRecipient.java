package com.hermes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record BulkTemplateMailRecipient(
    @NotBlank(message = "수신자 이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    String to,

    Map<String, String> variables  // 각 수신자별 개별 변수
) {

}