package com.hermes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record BulkMailRequest(
    @NotEmpty(message = "수신자 목록은 필수입니다")
    @Size(max = 1000, message = "한 번에 최대 1000명까지 발송 가능합니다")
    @Valid
    List<BulkMailRecipient> recipients,

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다")
    String subject,

    @NotBlank(message = "내용은 필수입니다")
    String content,

    boolean isHtml
) {

}