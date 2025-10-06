package com.hermes.repository;

import com.hermes.entity.MailSetting;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MailSettingRepository extends JpaRepository<MailSetting, Long> {

    Optional<MailSetting> findBySettingKey(String settingKey);

    boolean existsBySettingKey(String settingKey);

    @Query("SELECT ms.settingValue FROM MailSetting ms WHERE ms.settingKey = :key")
    Optional<String> findValueByKey(@Param("key") String key);
}