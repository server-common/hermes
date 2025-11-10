package com.hermes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;

@Entity
@Getter
@Builder
@ToString
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bulk_mail_batch")
@Comment("대량 메일 발송 배치 관리 테이블")
public class BulkMailBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("배치 고유 ID")
    private Long id;

    @Column(name = "batch_id")
    @Comment("배치 식별자")
    private String batchId;

    @Comment("배치 메일 고유 KEY")
    private String groupKey;

    @Column(name = "total_count")
    @Comment("전체 발송 대상 수")
    private Integer totalCount;

    @Column(name = "success_count")
    @Comment("성공 수")
    private Integer successCount;

    @Column(name = "failed_count")
    @Comment("실패 수")
    private Integer failedCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    @Comment("배치 상태")
    private BatchStatus status = BatchStatus.PROCESSING;

    @Column(name = "template_name")
    @Comment("사용된 템플릿 이름 (템플릿 발송인 경우)")
    private String templateName;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    @Comment("배치 생성 시간")
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    @Comment("배치 완료 시간")
    private LocalDateTime completedAt;

    public enum BatchStatus {
        PROCESSING, COMPLETED, FAILED
    }

    public void updateCounts(int successCount, int failedCount) {
        this.successCount = successCount;
        this.failedCount = failedCount;
    }

    public void complete() {
        this.status = BatchStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void fail() {
        this.status = BatchStatus.FAILED;
        this.completedAt = LocalDateTime.now();
    }
}