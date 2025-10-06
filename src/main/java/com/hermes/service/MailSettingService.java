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

    @Transactional
    @CacheEvict(value = {"mailSetting", "mailSettingValue"}, allEntries = true)
    public MailSettingResponse createSetting(MailSettingRequest request) {
        validateSettingKeyUniqueness(request.settingKey(), null);

        MailSetting setting = buildSetting(request);
        MailSetting savedSetting = mailSettingRepository.save(setting);

        log.info("메일 설정 생성: {} = {}", savedSetting.getSettingKey(), savedSetting.getSettingValue());
        return MailSettingResponse.from(savedSetting);
    }

    @Transactional
    @CacheEvict(value = {"mailSetting", "mailSettingValue"}, allEntries = true)
    public MailSettingResponse updateSetting(Long id, MailSettingRequest request) {
        MailSetting setting = getSettingById(id);
        validateSettingKeyUniqueness(request.settingKey(), setting.getSettingKey());

        updateSettingFields(setting, request);
        MailSetting updatedSetting = mailSettingRepository.save(setting);

        log.info("메일 설정 수정: {} = {}", updatedSetting.getSettingKey(), updatedSetting.getSettingValue());
        return MailSettingResponse.from(updatedSetting);
    }

    @Transactional
    @CacheEvict(value = {"mailSetting", "mailSettingValue"}, allEntries = true)
    public MailSettingResponse updateSettingByKey(String key, String value) {
        MailSetting setting = getSettingByKey(key);
        setting.updateSettingValue(value);
        MailSetting updatedSetting = mailSettingRepository.save(setting);

        log.info("메일 설정 값 변경: {} = {}", key, value);
        return MailSettingResponse.from(updatedSetting);
    }

    @Transactional
    @CacheEvict(value = {"mailSetting", "mailSettingValue"}, allEntries = true)
    public void deleteSetting(Long id) {
        MailSetting setting = getSettingById(id);
        mailSettingRepository.delete(setting);
        log.info("메일 설정 삭제: {}", setting.getSettingKey());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "mailSetting", key = "#id")
    public MailSettingResponse getSetting(Long id) {
        return MailSettingResponse.from(getSettingById(id));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "mailSetting", key = "#key")
    public MailSettingResponse getSettingByKeyResponse(String key) {
        return MailSettingResponse.from(getSettingByKey(key));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "mailSettingValue", key = "#key")
    public String getSettingValue(String key) {
        return mailSettingRepository.findValueByKey(key).orElseThrow(() -> new ResourceNotFoundException("설정", key));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "mailSettingValue", key = "#key", condition = "#defaultValue != null")
    public String getSettingValue(String key, String defaultValue) {
        return mailSettingRepository.findValueByKey(key).orElse(defaultValue);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "mailSettingValue", key = "'int:' + #key")
    public int getSettingValueAsInt(String key) {
        return parseIntValue(getSettingValue(key), key);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "mailSettingValue", key = "'int:' + #key + ':' + #defaultValue")
    public int getSettingValueAsInt(String key, int defaultValue) {
        try {
            String value = getSettingValue(key, String.valueOf(defaultValue));
            return parseIntValue(value, key);
        } catch (NumberFormatException e) {
            log.warn("설정 값 파싱 실패, 기본값 사용: {} = {}", key, defaultValue);
            return defaultValue;
        }
    }

    @Transactional(readOnly = true)
    public HermesPageResponse<MailSettingResponse> getSettings(HermesPageRequest hermesPageRequest) {
        Page<MailSetting> page = mailSettingRepository.findAll(hermesPageRequest.toPageable());
        return HermesPageResponse.from(page.map(MailSettingResponse::from));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "mailSetting", key = "'all'")
    public List<MailSettingResponse> getAllSettings() {
        return mailSettingRepository.findAll().stream().map(MailSettingResponse::from).toList();
    }

    // Private helper methods
    private MailSetting getSettingById(Long id) {
        return mailSettingRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("설정", id.toString()));
    }

    private MailSetting getSettingByKey(String key) {
        return mailSettingRepository.findBySettingKey(key).orElseThrow(() -> new ResourceNotFoundException("설정", key));
    }

    private void validateSettingKeyUniqueness(String newKey, String currentKey) {
        if (currentKey == null || !currentKey.equals(newKey)) {
            if (mailSettingRepository.existsBySettingKey(newKey)) {
                throw new DuplicateResourceException("설정 키", newKey);
            }
        }
    }

    private MailSetting buildSetting(MailSettingRequest request) {
        return MailSetting.builder().settingKey(request.settingKey()).settingValue(request.settingValue()).description(request.description()).build();
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