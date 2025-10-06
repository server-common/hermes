package com.hermes.common.controller;

import com.hermes.service.CacheService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cache")
@RequiredArgsConstructor
public class CacheController {

    private final CacheService cacheService;

    /**
     * 모든 캐시 삭제
     */
    @DeleteMapping("/all")
    public ResponseEntity<Map<String, String>> evictAllCache() {
        cacheService.evictAllCache();
        return ResponseEntity.ok(Map.of("message", "모든 캐시가 삭제되었습니다."));
    }

    /**
     * 메일 설정 캐시 삭제
     */
    @DeleteMapping("/mail-setting")
    public ResponseEntity<Map<String, String>> evictMailSettingCache() {
        cacheService.evictMailSettingCache();
        return ResponseEntity.ok(Map.of("message", "메일 설정 캐시가 삭제되었습니다."));
    }

    /**
     * 메일 템플릿 캐시 삭제
     */
    @DeleteMapping("/mail-template")
    public ResponseEntity<Map<String, String>> evictMailTemplateCache() {
        cacheService.evictMailTemplateCache();
        return ResponseEntity.ok(Map.of("message", "메일 템플릿 캐시가 삭제되었습니다."));
    }

    /**
     * 특정 캐시 키 삭제
     */
    @DeleteMapping("/{cacheName}/{key}")
    public ResponseEntity<Map<String, String>> evictCacheByKey(@PathVariable String cacheName, @PathVariable String key) {
        cacheService.evictCacheByKey(cacheName, key);
        return ResponseEntity.ok(Map.of("message", String.format("캐시가 삭제되었습니다: %s - %s", cacheName, key)));
    }

    /**
     * 캐시 워밍업
     */
    @PostMapping("/warmup")
    public ResponseEntity<Map<String, String>> warmUpCache() {
        cacheService.warmUpCache();
        return ResponseEntity.ok(Map.of("message", "캐시 워밍업이 완료되었습니다."));
    }

    /**
     * 캐시 강제 갱신
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> clearCorruptedCache() {
        cacheService.clearCorruptedCache();
        return ResponseEntity.ok(Map.of("message", "캐시 강제 갱신이 완료되었습니다."));
    }
}