package com.hermes.dto;

import com.hermes.entity.BulkMailBatch;
import java.time.LocalDateTime;

public record BulkMailStatusResponse(
    Long id,
    String batchId,
    Integer totalCount,
    Integer successCount,
    Integer failedCount,
    Double successRate,
    BulkMailBatch.BatchStatus status,
    String templateName,
    LocalDateTime createdAt,
    LocalDateTime completedAt,
    Long processingTimeSeconds  // 처리 시간 (초)
) {

    public static BulkMailStatusResponse from(BulkMailBatch batch) {
        // 성공률 계산
        Double successRate = null;
        if (batch.getTotalCount() != null && batch.getTotalCount() > 0 && batch.getSuccessCount() != null) {
            successRate = Math.round((batch.getSuccessCount().doubleValue() / batch.getTotalCount()) * 100 * 100.0) / 100.0;
        }

        // 처리 시간 계산
        Long processingTimeSeconds = null;
        if (batch.getCreatedAt() != null && batch.getCompletedAt() != null) {
            processingTimeSeconds = java.time.Duration.between(batch.getCreatedAt(), batch.getCompletedAt()).getSeconds();
        }

        return new BulkMailStatusResponse(
            batch.getId(),
            batch.getBatchId(),
            batch.getTotalCount(),
            batch.getSuccessCount(),
            batch.getFailedCount(),
            successRate,
            batch.getStatus(),
            batch.getTemplateName(),
            batch.getCreatedAt(),
            batch.getCompletedAt(),
            processingTimeSeconds
        );
    }
}