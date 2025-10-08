package com.hermes.dto;

public record BulkMailResult(
    String to,
    boolean success,
    Long mailLogId,    // 성공 시 메일 로그 ID
    String errorMessage // 실패 시 오류 메시지
) {

    public static BulkMailResult success(String to, Long mailLogId) {
        return new BulkMailResult(to, true, mailLogId, null);
    }

    public static BulkMailResult failure(String to, String errorMessage) {
        return new BulkMailResult(to, false, null, errorMessage);
    }
}