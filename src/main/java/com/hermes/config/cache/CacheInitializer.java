package com.hermes.config.cache;

import com.hermes.config.properties.CacheWarmupProperties;
import com.hermes.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(1000) // 다른 초기화 작업 후에 실행
@RequiredArgsConstructor
public class CacheInitializer implements ApplicationRunner {

    private final CacheService cacheService;
    private final CacheWarmupProperties warmupProperties;

    @Override
    public void run(ApplicationArguments args) {
        log.info("애플리케이션 시작 - 캐시 초기화 시작");
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        // 손상된 캐시 정리
        try {
            cacheService.evictAllCache();
            log.info("캐시 초기화 완료");
        } catch (Exception e) {
            log.error("캐시 초기화 중 오류 발생: {}", e.getMessage());
        }

        // 워밍업
        if (!warmupProperties.isEnabled()) {
            log.info("캐시 워밍업이 비활성화되어 있습니다.");
            return;
        }

        // 애플리케이션이 완전히 준비된 후 캐시 워밍업 실행
        log.info("애플리케이션 준비 완료 - 캐시 워밍업 시작");

        try {
            // 설정된 지연 시간 후 워밍업 실행
            int delayMs = warmupProperties.getDelaySeconds() * 1000;
            Thread.sleep(delayMs);
            cacheService.warmUpCache();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("캐시 워밍업 지연 중 인터럽트 발생");
        } catch (Exception e) {
            log.error("캐시 워밍업 실행 중 오류: {}", e.getMessage());
        }
    }
}