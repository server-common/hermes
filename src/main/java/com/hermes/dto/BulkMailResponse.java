package com.hermes.dto;

import java.time.LocalDateTime;
import java.util.List;

public record BulkMailResponse(
    String batchId,           // 배치 처리 ID
    int totalCount,           // 전체 발송 요청 수
    int successCount,         // 성공적으로 큐에 등록된 수
    int failedCount,          // 실패한 수
    List<BulkMailResult> results,  // 개별 결과
    LocalDateTime requestedAt      // 요청 시간
) {

    public static BulkMailResponse of(String batchId, List<BulkMailResult> results) {
        int successCount = (int) results.stream().filter(BulkMailResult::success).count();
        int failedCount = results.size() - successCount;

        return new BulkMailResponse(
            batchId,
            results.size(),
            successCount,
            failedCount,
            results,
            LocalDateTime.now()
        );
    }
}