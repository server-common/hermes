package com.hermes.repository;

import com.hermes.entity.MailSetting;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MailSettingRepository extends JpaRepository<MailSetting, Long> {

    Optional<MailSetting> findByIdAndGroupKey(Long id, String groupKey);

    Optional<MailSetting> findBySettingKeyAndGroupKey(String settingKey, String groupKey);

    boolean existsBySettingKeyAndGroupKey(String settingKey, String groupKey);

    @Query("SELECT ms.settingValue FROM MailSetting ms WHERE ms.settingKey = :key AND ms.groupKey = :groupKey")
    Optional<String> findValueByKeyAndGroupKey(@Param("key") String key, @Param("groupKey") String groupKey);

    Page<MailSetting> findByGroupKey(String groupKey, Pageable pageable);

    List<MailSetting> findByGroupKey(String groupKey);

    @Query("SELECT DISTINCT ms.groupKey FROM MailSetting ms WHERE ms.groupKey IS NOT NULL AND ms.groupKey <> ''")
    List<String> findDistinctGroupKeys();
}