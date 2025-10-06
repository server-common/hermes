package com.hermes.service;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheMaintenanceService {

    private final CacheService cacheService;
    private final CacheManager cacheManager;
    private final AtomicLong maintenanceCount = new AtomicLong(0);

    /**
     * 매일 새벽 3시에 캐시 갱신
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void dailyCacheRefresh() {
        log.info("일일 캐시 갱신 시작 - {}", LocalDateTime.now());

        try {
            // 기존 캐시 삭제
            cacheService.evictAllCache();

            // 잠시 대기 후 워밍업
            Thread.sleep(1000);
            cacheService.warmUpCache();

            long count = maintenanceCount.incrementAndGet();
            log.info("일일 캐시 갱신 완료 - 총 {}번째 갱신", count);

        } catch (Exception e) {
            log.error("일일 캐시 갱신 중 오류 발생: {}", e.getMessage());
        }
    }

    /**
     * 매시간 캐시 상태 체크
     */
    @Scheduled(cron = "0 0 * * * *")
    public void hourlyCacheHealthCheck() {
        try {
            log.debug("캐시 상태 체크 시작");

            boolean allCachesHealthy = true;
            for (String cacheName : cacheManager.getCacheNames()) {
                var cache = cacheManager.getCache(cacheName);
                if (cache == null) {
                    log.warn("캐시가 비활성화됨: {}", cacheName);
                    allCachesHealthy = false;
                }
            }

            if (allCachesHealthy) {
                log.debug("모든 캐시가 정상 상태입니다");
            } else {
                log.warn("일부 캐시에 문제가 있습니다. 캐시 재초기화를 고려하세요.");
            }
        } catch (Exception e) {
            log.error("캐시 상태 체크 중 오류: {}", e.getMessage());
        }
    }

    /**
     * 매주 일요일 새벽 2시에 캐시 통계 리포트
     */
    @Scheduled(cron = "0 0 2 * * SUN")
    public void weeklyCacheReport() {
        log.info("=== 주간 캐시 리포트 ===");
        log.info("총 캐시 갱신 횟수: {}", maintenanceCount.get());

        cacheManager.getCacheNames().forEach(cacheName -> {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                log.info("캐시 활성 상태: {}", cacheName);
            }
        });

        log.info("=== 주간 캐시 리포트 ===");
    }
}