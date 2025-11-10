package com.hermes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record BulkTemplateMailRequest(
    @NotEmpty(message = "수신자 목록은 필수입니다")
    @Size(max = 1000, message = "한 번에 최대 1000명까지 발송 가능합니다")
    @Valid
    List<BulkTemplateMailRecipient> recipients,

    @NotBlank(message = "템플릿 이름은 필수입니다")
    String templateName,

    @NotBlank(message = "groupKey는 필수입니다")
    String groupKey
) {

}