package com.hermes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record BulkMailRecipient(
    @NotBlank(message = "수신자 이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    String to,

    String name  // 선택적 필드 (개인화를 위해)
) {

}