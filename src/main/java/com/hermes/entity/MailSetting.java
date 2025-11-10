package com.hermes.entity;

import com.hermes.dto.MailSettingRequest;
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
@Table(name = "mail_setting")
@Comment("시스템 설정 테이블")
public class MailSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("시스템 설정 고유 ID")
    private Long id;

    @Comment("시스템 설정 고유 KEY")
    private String groupKey;

    @Column(name = "setting_key")
    @Comment("시스템 설정 키")
    private String settingKey;

    @Column(name = "setting_value")
    @Comment("시스템 설정 값")
    private String settingValue;

    @Column(name = "description")
    @Comment("시스템 설정 설명")
    private String description;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    @Comment("시스템 설정 생성 시간")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    @Comment("시스템 설정 수정 시간")
    private LocalDateTime updatedAt;

    public void updateSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }

    public void updateSettingValue(String settingValue) {
        this.settingValue = settingValue;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void update(MailSettingRequest request) {
        this.updateSettingKey(request.settingKey());
        this.updateSettingValue(request.settingValue());
        this.updateDescription(request.description());
    }
}