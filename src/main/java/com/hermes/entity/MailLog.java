package com.hermes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
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
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "content")
@Table(name = "mail_log")
@Comment("메일 전송 로그 테이블")
public class MailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("메일 로그 고유 ID")
    private Long id;

    @Comment("메일 로그 고유 KEY")
    private String groupKey;

    @Column(name = "recipient")
    @Comment("수신자 이메일 주소")
    private String recipient;

    @Column(name = "subject")
    @Comment("메일 제목")
    private String subject;

    @Column(name = "content")
    @Comment("메일 내용")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    @Comment("전송 상태 (PENDING, SENT, FAILED)")
    private MailStatus status = MailStatus.PENDING;

    @Column(name = "sent_at")
    @Comment("실제 전송 완료 시간")
    private LocalDateTime sentAt;

    @Column(name = "error_message")
    @Comment("메일 요청 생성 시간")
    private String errorMessage;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    @Comment("전송 실패 시 오류 메시지")
    private LocalDateTime createdAt;

    public enum MailStatus {
        PENDING, SENT, FAILED
    }

    // 편의 생성자
    public MailLog(String recipient, String subject, String content) {
        this.recipient = recipient;
        this.subject = subject;
        this.content = content;
        this.status = MailStatus.PENDING;
    }

    public void updateStatus(MailStatus status) {
        this.status = status;
    }

    public void updateSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public void updateErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = MailStatus.PENDING;
        }
    }
}