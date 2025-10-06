package com.hermes.entity;

import com.hermes.dto.MailTemplateRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import org.springframework.data.annotation.LastModifiedDate;

@Entity
@Getter
@Builder
@ToString
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mail_template")
@Comment("메일 템플릿 관리 테이블")
public class MailTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("메일 템플릿 고유 ID")
    private Long id;

    @Column(name = "name", unique = true)
    @Comment("메일 템플릿 이름")
    private String name;

    @Column(name = "subject")
    @Comment("메일 템플릿 제목")
    private String subject;

    @Column(name = "content")
    @Comment("메일 템플릿 내용")
    private String content;

    @Column(name = "is_html")
    @Builder.Default
    @Comment("메일 템플릿 html 여부")
    private Boolean isHtml = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    @Comment("메일 템플릿 생성 시간")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    @Comment("메일 템플릿 수정 시간")
    private LocalDateTime updatedAt;

    public void update(MailTemplateRequest request) {
        this.name = request.name();
        this.subject = request.subject();
        this.content = request.content();
        this.isHtml = request.isHtml();
    }
}