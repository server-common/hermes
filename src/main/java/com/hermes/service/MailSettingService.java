package com.hermes.service;

import com.hermes.common.dto.HermesPageRequest;
import com.hermes.common.dto.HermesPageResponse;
import com.hermes.dto.MailSettingRequest;
import com.hermes.dto.MailSettingResponse;
import com.hermes.entity.MailSetting;
import com.hermes.exception.DuplicateResourceException;
import com.hermes.exception.HermesException;
import com.hermes.exception.ResourceNotFoundException;
import com.hermes.repository.MailSettingRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailSettingService {

    private final MailSettingRepository mailSettingRepository;

    @Transactional(readOnly = true)
    public List<String> getAllGroupKeysForSettings() {
        return mailSettingRepository.findDistinctGroupKeys();
    }

    @Transactional
    @CacheEvict(value = {"mailSetting", "mailSettingValue"}, allEntries = true)
    public MailSettingResponse createSetting(MailSettingRequest request) {
        validateSettingKeyUniqueness(request.settingKey(), null, request.groupKey());

        MailSetting setting = buildSetting(request);
        MailSetting savedSetting = mailSettingRepository.save(setting);

        log.info("메일 설정 생성: {} = {}", savedSetting.getSettingKey(), savedSetting.getSettingValue());
        return MailSettingResponse.from(savedSetting);
    }

    @Transactional
    @CacheEvict(value = {"mailSetting", "mailSettingValue"}, allEntries = true)
    public MailSettingResponse updateSetting(Long id, MailSettingRequest request) {
        MailSetting setting = getSettingById(id, request.groupKey());
        validateSettingKeyUniqueness(request.settingKey(), setting.getSettingKey(), request.groupKey());

        updateSettingFields(setting, request);
        MailSetting updatedSetting = mailSettingRepository.save(setting);

        log.info("메일 설정 수정: {} = {}", updatedSetting.getSettingKey(), updatedSetting.getSettingValue());
        return MailSettingResponse.from(updatedSetting);
    }

    @Transactional
    @CacheEvict(value = {"mailSetting", "mailSettingValue"}, allEntries = true)
    public MailSettingResponse updateSettingByKey(String key, String value, String groupKey) {
        MailSetting setting = getSettingByKey(key, groupKey);
        setting.updateSettingValue(value);
        MailSetting updatedSetting = mailSettingRepository.save(setting);

        log.info("메일 설정 값 변경: {} = {} (group={})", key, value, groupKey);
        return MailSettingResponse.from(updatedSetting);
    }

    @Transactional
    @CacheEvict(value = {"mailSetting", "mailSettingValue"}, allEntries = true)
    public void deleteSetting(Long id, String groupKey) {
        MailSetting setting = getSettingById(id, groupKey);
        mailSettingRepository.delete(setting);
        log.info("메일 설정 삭제: {} (group={})", setting.getSettingKey(), groupKey);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "mailSetting", key = "#id + ':' + #groupKey")
    public MailSettingResponse getSetting(Long id, String groupKey) {
        return MailSettingResponse.from(getSettingById(id, groupKey));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "mailSetting", key = "#key + ':' + #groupKey")
    public MailSettingResponse getSettingByKeyResponse(String key, String groupKey) {
        return MailSettingResponse.from(getSettingByKey(key, groupKey));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "mailSettingValue", key = "#groupKey + ':' + #key")
    public String getSettingValue(String groupKey, String key) {
        return mailSettingRepository.findValueByKeyAndGroupKey(key, groupKey)
            .orElseThrow(() -> new ResourceNotFoundException("설정", key));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "mailSettingValue", key = "#groupKey + ':' + #key + ':' + #defaultValue", condition = "#defaultValue != null")
    public String getSettingValue(String groupKey, String key, String defaultValue) {
        return mailSettingRepository.findValueByKeyAndGroupKey(key, groupKey).orElse(defaultValue);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "mailSettingValue", key = "'int:' + #groupKey + ':' + #key")
    public int getSettingValueAsInt(String groupKey, String key) {
        return parseIntValue(getSettingValue(groupKey, key), key);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "mailSettingValue", key = "'int:' + #groupKey + ':' + #key + ':' + #defaultValue")
    public int getSettingValueAsInt(String groupKey, String key, int defaultValue) {
        try {
            String value = getSettingValue(groupKey, key, String.valueOf(defaultValue));
            return parseIntValue(value, key);
        } catch (NumberFormatException e) {
            log.warn("설정 값 파싱 실패, 기본값 사용: {} = {}", key, defaultValue);
            return defaultValue;
        }
    }

    @Transactional(readOnly = true)
    public HermesPageResponse<MailSettingResponse> getSettings(HermesPageRequest hermesPageRequest, String groupKey) {
        Page<MailSetting> page = mailSettingRepository.findByGroupKey(groupKey, hermesPageRequest.toPageable());
        return HermesPageResponse.from(page.map(MailSettingResponse::from));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "mailSetting", key = "'all:' + #groupKey")
    public List<MailSettingResponse> getAllSettings(String groupKey) {
        return mailSettingRepository.findByGroupKey(groupKey).stream().map(MailSettingResponse::from).toList();
    }

    // Private helper methods
    private MailSetting getSettingById(Long id, String groupKey) {
        return mailSettingRepository.findByIdAndGroupKey(id, groupKey)
            .orElseThrow(() -> new ResourceNotFoundException("설정", id.toString()));
    }

    private MailSetting getSettingByKey(String key, String groupKey) {
        return mailSettingRepository.findBySettingKeyAndGroupKey(key, groupKey)
            .orElseThrow(() -> new ResourceNotFoundException("설정", key));
    }

    private void validateSettingKeyUniqueness(String newKey, String currentKey, String groupKey) {
        if (currentKey == null || !currentKey.equals(newKey)) {
            if (mailSettingRepository.existsBySettingKeyAndGroupKey(newKey, groupKey)) {
                throw new DuplicateResourceException("설정 키", newKey);
            }
        }
    }

    private MailSetting buildSetting(MailSettingRequest request) {
        return MailSetting.builder()
            .groupKey(request.groupKey())
            .settingKey(request.settingKey())
            .settingValue(request.settingValue())
            .description(request.description())
            .build();
    }

    private void updateSettingFields(MailSetting setting, MailSettingRequest request) {
        setting.update(request);
    }

    private int parseIntValue(String value, String key) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new HermesException("설정 값이 숫자가 아닙니다: " + key + " = " + value);
        }
    }
}