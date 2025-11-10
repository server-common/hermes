package com.hermes.service;

import com.hermes.common.dto.HermesPageRequest;
import com.hermes.common.dto.HermesPageResponse;
import com.hermes.dto.BulkMailRequest;
import com.hermes.dto.BulkMailResponse;
import com.hermes.dto.BulkMailResult;
import com.hermes.dto.BulkMailStatusResponse;
import com.hermes.dto.BulkTemplateMailRequest;
import com.hermes.dto.MailRequest;
import com.hermes.dto.MailResponse;
import com.hermes.dto.MailTemplateResponse;
import com.hermes.dto.TemplateMailRequest;
import com.hermes.entity.BulkMailBatch;
import com.hermes.entity.MailLog;
import com.hermes.exception.HermesException;
import com.hermes.exception.ResourceNotFoundException;
import com.hermes.repository.BulkMailBatchRepository;
import com.hermes.repository.MailLogRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final MailLogRepository mailLogRepository;
    private final BulkMailBatchRepository bulkMailBatchRepository;
    private final MailTemplateService mailTemplateService;
    private final MailSettingService mailSettingService;
    private final MailQueueService mailQueueService;

    @Transactional
    public MailResponse sendMail(MailRequest request) {
        checkDailyLimit(request.groupKey());
        return processAndSendMail(request.groupKey(), request.to(), request.subject(), request.content());
    }

    @Transactional
    public MailResponse sendTemplatedMail(TemplateMailRequest request) {
        checkDailyLimit(request.groupKey());

        // 템플릿 조회 및 변수 치환
        MailTemplateResponse template = mailTemplateService.getTemplateByName(request.templateName(), request.groupKey());
        String processedSubject = mailTemplateService.processTemplate(template.subject(), request.variables());
        String processedContent = mailTemplateService.processTemplate(template.content(), request.variables());

        log.info("템플릿 메일 전송 요청: {} -> {} (템플릿: {})", processedSubject, request.to(), request.templateName());

        return processAndSendMail(request.groupKey(), request.to(), processedSubject, processedContent);
    }

    private MailResponse processAndSendMail(String groupKey, String to, String subject, String content) {
        // 메일 로그 생성
        MailLog savedMailLog = mailLogRepository.save(MailLog.builder()
            .groupKey(groupKey)
            .recipient(to)
            .subject(subject)
            .content(content)
            .build());

        // 메일 큐에 추가 (실제 전송은 MailQueueService에서 처리)
        mailQueueService.enqueueMailForSending(savedMailLog.getId());

        return MailResponse.from(savedMailLog);
    }

    private void checkDailyLimit(String groupKey) {
        try {
            int dailyLimit = mailSettingService.getSettingValueAsInt(groupKey, "daily_limit", 10000);
            LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
            long todayCount = mailLogRepository.countByStatusAndGroupKeyAndSentAtAfter(MailLog.MailStatus.SENT, groupKey, startOfDay);

            if (todayCount >= dailyLimit) {
                throw new HermesException("일일 메일 전송 제한에 도달했습니다: " + dailyLimit);
            }
        } catch (HermesException e) {
            // HermesException은 다시 던져서 호출자가 처리하도록 함
            throw e;
        } catch (Exception e) {
            log.warn("일일 전송 제한 확인 중 오류 발생: {}", e.getMessage());
        }
    }

    /**
     * 큐 상태 조회
     */
    public MailQueueService.QueueStatus getQueueStatus() {
        return mailQueueService.getQueueStatus();
    }

    @Transactional(readOnly = true)
    public HermesPageResponse<MailResponse> getMailLogs(HermesPageRequest hermesPageRequest, String groupKey) {
        Page<MailLog> page = mailLogRepository.findByGroupKey(groupKey, hermesPageRequest.toPageable());
        return HermesPageResponse.from(page.map(MailResponse::from));
    }

    @Transactional(readOnly = true)
    public MailResponse getMailLog(Long id, String groupKey) {
        MailLog mailLog = mailLogRepository.findByIdAndGroupKey(id, groupKey)
            .orElseThrow(() -> new ResourceNotFoundException("메일 로그", id.toString()));
        return MailResponse.from(mailLog);
    }

    @Transactional(readOnly = true)
    public HermesPageResponse<MailResponse> getMailLogsByStatus(MailLog.MailStatus status, HermesPageRequest hermesPageRequest, String groupKey) {
        Page<MailLog> page = mailLogRepository.findByStatusAndGroupKey(status, groupKey, hermesPageRequest.toPageable());
        return HermesPageResponse.from(page.map(MailResponse::from));
    }

    /**
     * 대량 메일 발송
     */
    @Transactional
    public BulkMailResponse sendBulkMail(BulkMailRequest request) {
        String batchId = generateBatchId();
        log.info("대량 메일 발송 시작: batchId={}, 수신자 수={}, groupKey={}", batchId, request.recipients().size(), request.groupKey());

        // 일일 제한 체크 (대량 발송 고려)
        checkBulkDailyLimit(request.groupKey(), request.recipients().size());

        List<BulkMailResult> results = new ArrayList<>();

        for (var recipient : request.recipients()) {
            try {
                // 개인화된 제목과 내용 생성 (이름이 있는 경우)
                String personalizedSubject = personalize(request.subject(), recipient.name());
                String personalizedContent = personalize(request.content(), recipient.name());

                // 메일 로그 생성 및 큐에 추가
                MailLog savedMailLog = mailLogRepository.save(
                    MailLog.builder()
                        .groupKey(request.groupKey())
                        .recipient(recipient.to())
                        .subject(personalizedSubject)
                        .content(personalizedContent)
                        .build()
                );

                mailQueueService.enqueueMailForSending(savedMailLog.getId());

                results.add(BulkMailResult.success(recipient.to(), savedMailLog.getId()));

            } catch (Exception e) {
                log.error("대량 메일 발송 중 개별 실패: to={}, error={}", recipient.to(), e.getMessage());
                results.add(BulkMailResult.failure(recipient.to(), e.getMessage()));
            }
        }

        // 배치 정보 저장
        int successCount = results.stream().mapToInt(r -> r.success() ? 1 : 0).sum();
        int failedCount = results.size() - successCount;

        saveBulkMailBatch(request.groupKey(), batchId, request.recipients().size(), successCount, failedCount, null);

        log.info("대량 메일 발송 완료: batchId={}, 성공={}, 실패={}, groupKey={}", batchId, successCount, failedCount, request.groupKey());

        return BulkMailResponse.of(batchId, results);
    }

    /**
     * 대량 템플릿 메일 발송
     */
    @Transactional
    public BulkMailResponse sendBulkTemplatedMail(BulkTemplateMailRequest request) {
        String batchId = generateBatchId();
        log.info("대량 템플릿 메일 발송 시작: batchId={}, 템플릿={}, 수신자 수={}, groupKey={}", batchId, request.templateName(), request.recipients().size(), request.groupKey());

        // 일일 제한 체크
        checkBulkDailyLimit(request.groupKey(), request.recipients().size());

        // 템플릿 조회 (한 번만)
        MailTemplateResponse template;
        try {
            template = mailTemplateService.getTemplateByName(request.templateName(), request.groupKey());
        } catch (Exception e) {
            log.error("템플릿 조회 실패: {}", e.getMessage());
            throw new HermesException("템플릿을 찾을 수 없습니다: " + request.templateName());
        }

        List<BulkMailResult> results = new ArrayList<>();

        for (var recipient : request.recipients()) {
            try {
                // 개별 변수 치환
                String processedSubject = mailTemplateService.processTemplate(template.subject(), recipient.variables());
                String processedContent = mailTemplateService.processTemplate(template.content(), recipient.variables());

                // 메일 로그 생성 및 큐에 추가
                MailLog savedMailLog = mailLogRepository.save(
                    MailLog.builder()
                        .groupKey(request.groupKey())
                        .recipient(recipient.to())
                        .subject(processedSubject)
                        .content(processedContent)
                        .build()
                );

                mailQueueService.enqueueMailForSending(savedMailLog.getId());

                results.add(BulkMailResult.success(recipient.to(), savedMailLog.getId()));

            } catch (Exception e) {
                log.error("대량 템플릿 메일 발송 중 개별 실패: to={}, error={}", recipient.to(), e.getMessage());
                results.add(BulkMailResult.failure(recipient.to(), e.getMessage()));
            }
        }

        // 배치 정보 저장
        int successCount = results.stream().mapToInt(r -> r.success() ? 1 : 0).sum();
        int failedCount = results.size() - successCount;

        saveBulkMailBatch(request.groupKey(), batchId, request.recipients().size(), successCount, failedCount, request.templateName());

        log.info("대량 템플릿 메일 발송 완료: batchId={}, 성공={}, 실패={}, groupKey={}", batchId, successCount, failedCount, request.groupKey());

        return BulkMailResponse.of(batchId, results);
    }

    /**
     * 대량 발송을 위한 일일 제한 체크
     */
    private void checkBulkDailyLimit(String groupKey, int requestCount) {
        try {
            int dailyLimit = mailSettingService.getSettingValueAsInt(groupKey, "daily_limit", 10000);
            LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
            long todayCount = mailLogRepository.countByStatusAndGroupKeyAndSentAtAfter(MailLog.MailStatus.SENT, groupKey, startOfDay);

            if (todayCount + requestCount > dailyLimit) {
                throw new HermesException(String.format(
                    "일일 메일 전송 제한 초과: 현재 %d건, 요청 %d건, 제한 %d건",
                    todayCount, requestCount, dailyLimit));
            }
        } catch (HermesException e) {
            throw e;
        } catch (Exception e) {
            log.warn("일일 전송 제한 확인 중 오류 발생: {}", e.getMessage());
        }
    }

    /**
     * 배치 ID 생성
     */
    private String generateBatchId() {
        return "BULK_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * 내용 개인화 (이름이 있는 경우)
     */
    private String personalize(String text, String name) {
        if (name != null && !name.trim().isEmpty()) {
            // 간단한 개인화: {{name}} 치환
            return text.replace("{{name}}", name);
        }
        return text;
    }

    /**
     * 배치 정보 저장
     */
    private void saveBulkMailBatch(String groupKey, String batchId, int totalCount, int successCount, int failedCount, String templateName) {
        try {
            BulkMailBatch batch = BulkMailBatch.builder()
                .groupKey(groupKey)
                .batchId(batchId)
                .totalCount(totalCount)
                .successCount(successCount)
                .failedCount(failedCount)
                .templateName(templateName)
                .build();

            if (failedCount == 0) {
                batch.complete();
            } else if (successCount == 0) {
                batch.fail();
            } else {
                batch.complete(); // 부분 성공도 완료로 처리
            }

            bulkMailBatchRepository.save(batch);
        } catch (Exception e) {
            log.error("배치 정보 저장 실패: batchId={}, error={}", batchId, e.getMessage());
        }
    }

    /**
     * 배치 상태 조회
     */
    @Transactional(readOnly = true)
    public BulkMailStatusResponse getBulkMailBatchStatus(String batchId, String groupKey) {
        BulkMailBatch batch = bulkMailBatchRepository.findByBatchIdAndGroupKey(batchId, groupKey)
            .orElseThrow(() -> new ResourceNotFoundException("배치", batchId));

        return BulkMailStatusResponse.from(batch);
    }
}