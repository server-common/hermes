package com.hermes.service;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataSourceMonitoringService {

    private final DataSource dataSource;

    /**
     * 커넥션 풀 통계 조회
     */
    public Map<String, Object> getConnectionPoolStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            HikariDataSource hikariDataSource = getHikariDataSource();
            if (hikariDataSource != null) {
                HikariPoolMXBean poolBean = hikariDataSource.getHikariPoolMXBean();

                stats.put("activeConnections", poolBean.getActiveConnections());
                stats.put("idleConnections", poolBean.getIdleConnections());
                stats.put("totalConnections", poolBean.getTotalConnections());
                stats.put("threadsAwaitingConnection", poolBean.getThreadsAwaitingConnection());
                stats.put("maxPoolSize", hikariDataSource.getMaximumPoolSize());
                stats.put("minPoolSize", hikariDataSource.getMinimumIdle());
                stats.put("poolName", hikariDataSource.getPoolName());

                // 추가 통계
                stats.put("connectionTimeout", hikariDataSource.getConnectionTimeout());
                stats.put("idleTimeout", hikariDataSource.getIdleTimeout());
                stats.put("maxLifetime", hikariDataSource.getMaxLifetime());
                stats.put("validationTimeout", hikariDataSource.getValidationTimeout());

                // 사용률 계산
                int active = poolBean.getActiveConnections();
                int max = hikariDataSource.getMaximumPoolSize();
                double usageRate = max > 0 ? (double) active / max * 100 : 0;
                stats.put("usageRate", Math.round(usageRate * 10.0) / 10.0);

            } else {
                log.warn("HikariDataSource를 찾을 수 없습니다.");
                stats.put("error", "HikariDataSource not found");
            }
        } catch (Exception e) {
            log.error("커넥션 풀 통계 조회 중 오류: {}", e.getMessage());
            stats.put("error", e.getMessage());
        }

        return stats;
    }

    /**
     * LazyConnectionDataSourceProxy에서 실제 HikariDataSource 추출
     */
    private HikariDataSource getHikariDataSource() {
        if (dataSource instanceof LazyConnectionDataSourceProxy proxy) {
            DataSource targetDataSource = proxy.getTargetDataSource();
            if (targetDataSource instanceof HikariDataSource hikariDataSource) {
                return hikariDataSource;
            }
        } else if (dataSource instanceof HikariDataSource hikariDataSource) {
            return hikariDataSource;
        }
        return null;
    }

    /**
     * 커넥션 풀 상태 요약
     */
    public String getConnectionPoolSummary() {
        try {
            Map<String, Object> stats = getConnectionPoolStats();

            if (stats.containsKey("error")) {
                return "커넥션 풀 상태 조회 실패: " + stats.get("error");
            }

            return String.format(
                "HikariCP 상태 - 활성: %s, 유휴: %s, 총: %d/%d (사용률: %.1f%%)",
                stats.get("activeConnections"),
                stats.get("idleConnections"),
                stats.get("totalConnections"),
                stats.get("maxPoolSize"),
                stats.get("usageRate")
            );

        } catch (Exception e) {
            return "커넥션 풀 상태 조회 중 오류: " + e.getMessage();
        }
    }

    /**
     * 커넥션 풀 헬스 체크
     */
    public boolean isConnectionPoolHealthy() {
        try {
            Map<String, Object> stats = getConnectionPoolStats();

            if (stats.containsKey("error")) {
                return false;
            }

            double usageRate = (Double) stats.get("usageRate");
            int threadsWaiting = (Integer) stats.get("threadsAwaitingConnection");

            // 사용률이 95% 이상이거나 대기 중인 스레드가 있으면 비정상
            return usageRate < 95.0 && threadsWaiting == 0;

        } catch (Exception e) {
            log.error("커넥션 풀 헬스 체크 중 오류: {}", e.getMessage());
            return false;
        }
    }
}