package com.hermes.common.controller;

import com.hermes.entity.MailLog;
import com.hermes.repository.MailLogRepository;
import com.hermes.service.DataSourceMonitoringService;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/health")
public class HealthController {

    private final MailLogRepository mailLogRepository;
    private final DataSourceMonitoringService dataSourceMonitoringService;
    private final RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getMailStats() {
        Map<String, Object> response = new HashMap<>();

        try {
            long totalMails = mailLogRepository.count();
            long pendingMails = mailLogRepository.countByStatus(MailLog.MailStatus.PENDING);
            long sentMails = mailLogRepository.countByStatus(MailLog.MailStatus.SENT);
            long failedMails = mailLogRepository.countByStatus(MailLog.MailStatus.FAILED);

            response.put("totalMails", totalMails);
            response.put("pendingMails", pendingMails);
            response.put("sentMails", sentMails);
            response.put("failedMails", failedMails);
            response.put("successRate", totalMails > 0 ? (double) sentMails / totalMails * 100 : 0);
            response.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now().toString());

            log.error("메일 통계 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(503).body(response);
        }
    }

    @GetMapping("/database")
    public ResponseEntity<Map<String, Object>> checkDatabase() {
        Map<String, Object> response = new HashMap<>();

        try {
            long count = mailLogRepository.count();
            Map<String, Object> poolStats = dataSourceMonitoringService.getConnectionPoolStats();
            boolean isHealthy = dataSourceMonitoringService.isConnectionPoolHealthy();

            response.put("status", isHealthy ? "UP" : "DEGRADED");
            response.put("database", "PostgreSQL");
            response.put("schema", "hermes");
            response.put("totalMailLogs", count);
            response.put("connectionPool", poolStats);
            response.put("poolSummary", dataSourceMonitoringService.getConnectionPoolSummary());
            response.put("timestamp", LocalDateTime.now().toString());

            log.info("데이터베이스 연결 확인 성공 - 총 메일 로그: {}", count);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now().toString());

            log.error("데이터베이스 연결 확인 실패: {}", e.getMessage());
            return ResponseEntity.status(503).body(response);
        }
    }

    @GetMapping("/datasource")
    public ResponseEntity<Map<String, Object>> getDataSourceInfo() {
        Map<String, Object> response = dataSourceMonitoringService.getConnectionPoolStats();
        response.put("timestamp", LocalDateTime.now().toString());

        boolean isHealthy = dataSourceMonitoringService.isConnectionPoolHealthy();
        response.put("status", isHealthy ? "HEALTHY" : "DEGRADED");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/redis")
    public ResponseEntity<Map<String, Object>> checkRedis() {
        Map<String, Object> response = new HashMap<>();

        try {
            redisTemplate.opsForValue().set("health:check", "OK");
            String result = (String) redisTemplate.opsForValue().get("health:check");

            response.put("status", "UP");
            response.put("redis", "Connected");
            response.put("testResult", result);
            response.put("timestamp", LocalDateTime.now().toString());

            log.info("Redis 연결 확인 성공");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now().toString());

            log.error("Redis 연결 확인 실패: {}", e.getMessage());
            return ResponseEntity.status(503).body(response);
        }
    }
}