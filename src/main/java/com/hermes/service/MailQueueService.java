package com.hermes.service;

import com.hermes.entity.MailLog;
import com.hermes.repository.MailLogRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailQueueService {

    private static final String MAIL_QUEUE_KEY = "mail:queue";
    private static final String MAIL_PROCESSING_KEY = "mail:processing";
    private static final String MAIL_RETRY_KEY = "mail:retry";

    private final RedisTemplate<String, Object> redisTemplate;
    private final JavaMailSender mailSender;
    private final MailLogRepository mailLogRepository;
    private final MailSettingService mailSettingService;
    private final MailLogService mailLogService;

    /**
     * 메일을 큐에 추가
     */
    public void enqueueMailForSending(Long mailLogId) {
        redisTemplate.opsForList().rightPush(MAIL_QUEUE_KEY, mailLogId);
        log.debug("메일 큐에 추가: ID = {}", mailLogId);
    }

    /**
     * 큐에서 메일을 꺼내서 처리 (스케줄러로 주기적 실행) 대량 발송을 위해 배치 처리 지원
     */
//    @Scheduled(cron = "0/2 * * * * *") // 2초
    @Scheduled(fixedDelay = 2, initialDelay = 3, timeUnit = TimeUnit.SECONDS) // 2초
    public void processMailQueue() {
        try {
            // 배치 크기 설정에서 가져오기 (기본값: 10)
            int batchSize = mailSettingService.getSettingValueAsInt("batch_size", 10);

            // 배치 단위로 메일 처리
            for (int i = 0; i < batchSize; i++) {
                Object mailLogId = redisTemplate.opsForList().leftPop(MAIL_QUEUE_KEY);

                if (mailLogId == null) {
                    break; // 큐가 비어있으면 종료
                }

                Long id = parseLongSafely(mailLogId.toString());
                if (id == null) {
                    log.warn("잘못된 메일 ID 형식: {}", mailLogId);
                    continue;
                }

                // 처리 중 큐에 추가 (중복 처리 방지)
                redisTemplate.opsForSet().add(MAIL_PROCESSING_KEY, id);
                redisTemplate.expire(MAIL_PROCESSING_KEY, 10, TimeUnit.MINUTES);

                log.debug("메일 전송 처리 시작: ID = {}", id);
                processMailSending(id);

                // 처리 완료 후 처리 중 큐에서 제거
                redisTemplate.opsForSet().remove(MAIL_PROCESSING_KEY, id);
            }
        } catch (Exception e) {
            log.error("메일 큐 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 실제 메일 전송 처리
     */
    private void processMailSending(Long mailLogId) {
        try {
            MailLog mailLog = mailLogRepository.findById(mailLogId).orElseThrow(() -> new RuntimeException("메일 로그를 찾을 수 없습니다: " + mailLogId));

            // 이미 전송된 메일은 스킵
            if (mailLog.getStatus() == MailLog.MailStatus.SENT) {
                log.info("이미 전송된 메일입니다: ID = {}", mailLogId);
                return;
            }

            // 메일 전송
            sendMail(mailLog);

            // 성공 시 상태 업데이트
            mailLogService.updateMailLogStatus(mailLog, MailLog.MailStatus.SENT, null);
            log.info("메일 전송 성공: {} -> {}", mailLog.getSubject(), mailLog.getRecipient());

        } catch (Exception e) {
            log.error("메일 전송 실패: ID = {}, 오류 = {}", mailLogId, e.getMessage());
            handleMailError(mailLogId, e.getMessage());
        }
    }

    /**
     * 실제 메일 전송
     */
    private void sendMail(MailLog mailLog) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();

        String sender = mailSettingService.getSettingValue("from_address");
        String senderName = mailSettingService.getSettingValue("from_name");
        message.setFrom(new InternetAddress(sender, senderName, "UTF-8"));
        message.setRecipients(MimeMessage.RecipientType.TO, mailLog.getRecipient());
        message.setSubject(mailLog.getSubject());
        message.setText(mailLog.getContent(), "UTF-8", "html");

        mailSender.send(message);
    }

    /**
     * 메일 전송 실패 처리
     */
    private void handleMailError(Long mailLogId, String errorMessage) {
        try {
            MailLog mailLog = mailLogRepository.findById(mailLogId).orElse(null);
            if (mailLog == null) {
                return;
            }

            // 재시도 횟수 확인
            int maxRetryCount = mailSettingService.getSettingValueAsInt("max_retry_count", 3);
            int currentRetryCount = getCurrentRetryCount(mailLogId);

            if (currentRetryCount < maxRetryCount) {
                // 재시도 큐에 추가
                scheduleRetry(mailLogId, currentRetryCount + 1);
                log.info("메일 재시도 예약: ID = {}, 시도 횟수 = {}", mailLogId, currentRetryCount + 1);
            } else {
                // 최대 재시도 횟수 초과 시 실패 처리
                mailLogService.updateMailLogStatus(mailLog, MailLog.MailStatus.FAILED, errorMessage);
                log.error("메일 전송 최종 실패: ID = {}, 오류 = {}", mailLogId, errorMessage);
            }
        } catch (Exception e) {
            log.error("메일 오류 처리 중 예외 발생: {}", e.getMessage());
        }
    }

    /**
     * 재시도 스케줄링
     */
    private void scheduleRetry(Long mailLogId, int retryCount) {
        int retryDelayMinutes = mailSettingService.getSettingValueAsInt("retry_delay_minutes", 5);
        long delaySeconds = retryDelayMinutes * 60L;

        // 재시도 정보를 Redis에 저장
        String retryKey = MAIL_RETRY_KEY + ":" + mailLogId;
        redisTemplate.opsForValue().set(retryKey, retryCount, delaySeconds, TimeUnit.SECONDS);

        // 지연 후 다시 큐에 추가하는 스케줄 등록
        redisTemplate.opsForZSet().add(MAIL_RETRY_KEY + ":scheduled", mailLogId, System.currentTimeMillis() + (delaySeconds * 1000));
    }

    /**
     * 재시도 큐 처리
     */
    @Scheduled(cron = "0/10 0 * * * *") // 10초
    public void processRetryQueue() {
        try {
            long currentTime = System.currentTimeMillis();

            // 재시도 시간이 된 메일들을 조회
            var retryMails = redisTemplate.opsForZSet().rangeByScore(MAIL_RETRY_KEY + ":scheduled", 0, currentTime);

            if (retryMails != null && !retryMails.isEmpty()) {
                for (Object mailLogId : retryMails) {
                    Long id = parseLongSafely(mailLogId.toString());
                    if (id == null) {
                        log.warn("재시도 큐에서 잘못된 메일 ID 형식: {}", mailLogId);
                        continue;
                    }

                    // 다시 메인 큐에 추가
                    enqueueMailForSending(id);

                    // 재시도 스케줄에서 제거
                    redisTemplate.opsForZSet().remove(MAIL_RETRY_KEY + ":scheduled", id);

                    log.info("메일 재시도 큐에서 메인 큐로 이동: ID = {}", id);
                }
            }
        } catch (Exception e) {
            log.error("재시도 큐 처리 중 오류 발생: {}", e.getMessage());
        }
    }

    /**
     * 현재 재시도 횟수 조회
     */
    private int getCurrentRetryCount(Long mailLogId) {
        try {
            String retryKey = MAIL_RETRY_KEY + ":" + mailLogId;
            Object retryCount = redisTemplate.opsForValue().get(retryKey);
            if (retryCount != null) {
                Integer count = parseIntSafely(retryCount.toString());
                return count != null ? count : 0;
            }

            return 0;
        } catch (Exception e) {
            log.warn("재시도 횟수 조회 중 오류 발생: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 큐 상태 조회
     */
    public QueueStatus getQueueStatus() {
        long pendingCount = getSafeSize(() -> redisTemplate.opsForList().size(MAIL_QUEUE_KEY));
        long processingCount = getSafeSize(() -> redisTemplate.opsForSet().size(MAIL_PROCESSING_KEY));
        long retryCount = getSafeSize(() -> redisTemplate.opsForZSet().size(MAIL_RETRY_KEY + ":scheduled"));

        return new QueueStatus(pendingCount, processingCount, retryCount);
    }

    /**
     * Redis 연산에서 null 안전하게 처리
     */
    private long getSafeSize(java.util.function.Supplier<Long> sizeSupplier) {
        try {
            Long size = sizeSupplier.get();
            return size != null ? size : 0L;
        } catch (Exception e) {
            log.warn("Redis 크기 조회 중 오류 발생: {}", e.getMessage());
            return 0L;
        }
    }

    /**
     * 문자열을 Long으로 안전하게 파싱
     */
    private Long parseLongSafely(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            log.warn("Long 파싱 실패: {}", value);
            return null;
        }
    }

    /**
     * 문자열을 Integer로 안전하게 파싱
     */
    private Integer parseIntSafely(String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            log.warn("Integer 파싱 실패: {}", value);
            return null;
        }
    }

    /**
     * 큐 상태 정보 클래스
     */
    public record QueueStatus(long pendingCount, long processingCount, long retryCount) {

    }
}