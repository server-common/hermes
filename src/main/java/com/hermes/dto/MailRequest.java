package com.hermes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MailRequest(
    @NotBlank(message = "수신자 이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    String to,

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다")
    String subject,

    @NotBlank(message = "내용은 필수입니다")
    String content,

    boolean isHtml,

    @NotBlank(message = "groupKey는 필수입니다")
    String groupKey
) {

}