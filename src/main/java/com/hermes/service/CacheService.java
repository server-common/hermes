package com.hermes.service;

import com.hermes.config.properties.CacheWarmupProperties;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final CacheManager cacheManager;
    private final MailSettingService mailSettingService;
    private final MailTemplateService mailTemplateService;
    private final CacheWarmupProperties warmupProperties;

    // 워밍업에 사용할 스레드 풀
    private final ExecutorService warmupExecutor = Executors.newFixedThreadPool(3);

    /**
     * 모든 캐시 삭제
     */
    @CacheEvict(value = {"mailSetting", "mailSettingValue"}, allEntries = true)
    public void evictAllCache() {
        log.info("모든 캐시를 삭제했습니다.");

        // 추가적으로 캐시 매니저를 통한 직접 삭제
        try {
            for (String cacheName : cacheManager.getCacheNames()) {
                var cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                    log.debug("캐시 직접 삭제: {}", cacheName);
                }
            }
        } catch (Exception e) {
            log.warn("캐시 직접 삭제 중 오류: {}", e.getMessage());
        }
    }

    /**
     * 메일 설정 캐시만 삭제
     */
    @CacheEvict(value = {"mailSetting", "mailSettingValue"}, allEntries = true)
    public void evictMailSettingCache() {
        log.info("메일 설정 캐시를 삭제했습니다.");
    }

    /**
     * 특정 키의 캐시 삭제
     */
    public void evictCacheByKey(String cacheName, String key) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
            log.info("캐시 삭제: {} - {}", cacheName, key);
        }
    }

    /**
     * 손상된 캐시 데이터 정리 (JSON 역직렬화 오류 해결)
     */
    public void clearCorruptedCache() {
        log.info("손상된 캐시 데이터 정리를 시작합니다.");

        try {
            // 모든 캐시 삭제
            evictAllCache();

            // 잠시 대기 후 캐시 워밍업
            Thread.sleep(1000);

            // 캐시 워밍업
            warmUpCache();

            log.info("손상된 캐시 데이터 정리 및 재구성 완료");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("캐시 정리 중 인터럽트 발생");
        } catch (Exception e) {
            log.error("캐시 정리 중 오류 발생: {}", e.getMessage());
        }
    }

    /**
     * 캐시 워밍업 - 자주 사용되는 설정값들을 미리 로드
     */
    public void warmUpCache() {
        log.info("캐시 워밍업을 시작합니다.");

        long startTime = System.currentTimeMillis();

        // 비동기로 각각의 캐시 워밍업 실행
        CompletableFuture<Void> settingsWarmup = CompletableFuture.runAsync(this::warmUpMailSettings, warmupExecutor);
        CompletableFuture<Void> templatesWarmup = CompletableFuture.runAsync(this::warmUpMailTemplates, warmupExecutor);

        // 모든 워밍업 작업 완료 대기
        CompletableFuture.allOf(settingsWarmup, templatesWarmup)
            .thenRun(() -> {
                long endTime = System.currentTimeMillis();
                log.info("캐시 워밍업 완료 - 소요시간: {}ms", endTime - startTime);

                log.info("=== 캐시 통계 ===");
                cacheManager.getCacheNames().forEach(cacheName -> {
                    var cache = cacheManager.getCache(cacheName);
                    if (cache != null) {
                        log.info("캐시: {} - 활성화됨", cacheName);
                    }
                });
                log.info("=== 캐시 통계 ===");
            })
            .exceptionally(throwable -> {
                log.error("캐시 워밍업 중 오류 발생: {}", throwable.getMessage());
                return null;
            });
    }

    /**
     * 메일 설정 캐시 워밍업
     */
    private void warmUpMailSettings() {
        try {
            log.info("메일 설정 캐시 워밍업 시작");

            // 설정에서 자주 사용되는 설정값들을 가져옴
            List<String> frequentSettings = warmupProperties.getFrequentSettings();

            int loadedCount = 0;
            for (String settingKey : frequentSettings) {
                try {
                    // 설정값 조회 (캐시에 저장됨)
                    mailSettingService.getSettingValue(settingKey, "default");

                    // 정수형 설정값도 미리 로드
                    if (isNumericSetting(settingKey)) {
                        mailSettingService.getSettingValueAsInt(settingKey, 0);
                    }

                    loadedCount++;
                    log.debug("설정 캐시 로드: {}", settingKey);
                } catch (Exception e) {
                    log.warn("설정 캐시 로드 실패: {} - {}", settingKey, e.getMessage());
                }
            }

            // 모든 설정 목록도 캐시에 로드
            try {
                mailSettingService.getAllSettings();
                loadedCount++;
            } catch (Exception e) {
                log.warn("전체 설정 목록 캐시 로드 실패: {}", e.getMessage());
            }

            log.info("메일 설정 캐시 워밍업 완료 - {}개 항목 로드", loadedCount);

        } catch (Exception e) {
            log.error("메일 설정 캐시 워밍업 중 오류: {}", e.getMessage());
        }
    }

    /**
     * 메일 템플릿 캐시 워밍업
     */
    private void warmUpMailTemplates() {
        try {
            log.info("메일 템플릿 캐시 워밍업 시작");

            // 설정에서 자주 사용되는 템플릿들을 가져옴
            List<String> frequentTemplates = warmupProperties.getFrequentTemplates();

            int loadedCount = 0;
            for (String templateName : frequentTemplates) {
                try {
                    mailTemplateService.getTemplateByName(templateName);
                    loadedCount++;
                    log.debug("템플릿 캐시 로드: {}", templateName);
                } catch (Exception e) {
                    log.debug("템플릿 없음 또는 로드 실패: {} - {}", templateName, e.getMessage());
                }
            }

            log.info("메일 템플릿 캐시 워밍업 완료 - {}개 항목 로드", loadedCount);

        } catch (Exception e) {
            log.error("메일 템플릿 캐시 워밍업 중 오류: {}", e.getMessage());
        }
    }

    /**
     * 숫자형 설정인지 확인
     */
    private boolean isNumericSetting(String settingKey) {
        return settingKey.contains("limit") ||
            settingKey.contains("count") ||
            settingKey.contains("timeout") ||
            settingKey.contains("delay") ||
            settingKey.contains("size") ||
            settingKey.contains("port");
    }
}