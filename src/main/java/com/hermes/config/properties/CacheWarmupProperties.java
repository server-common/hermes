package com.hermes.config.properties;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "hermes.cache.warmup")
public class CacheWarmupProperties {

    /**
     * 캐시 워밍업 활성화 여부
     */
    private boolean enabled;

    /**
     * 애플리케이션 시작 후 워밍업 지연 시간 (초)
     */
    private int delaySeconds;

    /**
     * 자주 사용되는 설정 키 목록
     */
    private List<String> frequentSettings;

    /**
     * 자주 사용되는 템플릿 이름 목록
     */
    private List<String> frequentTemplates;
}