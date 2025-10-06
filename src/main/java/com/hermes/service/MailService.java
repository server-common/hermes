package com.hermes.service;

import com.hermes.common.dto.HermesPageRequest;
import com.hermes.common.dto.HermesPageResponse;
import com.hermes.dto.MailRequest;
import com.hermes.dto.MailResponse;
import com.hermes.dto.MailTemplateResponse;
import com.hermes.dto.TemplateMailRequest;
import com.hermes.entity.MailLog;
import com.hermes.exception.HermesException;
import com.hermes.exception.ResourceNotFoundException;
import com.hermes.repository.MailLogRepository;
import java.time.LocalDateTime;
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
    private final MailTemplateService mailTemplateService;
    private final MailSettingService mailSettingService;
    private final MailQueueService mailQueueService;

    @Transactional
    public MailResponse sendMail(MailRequest request) {
        checkDailyLimit();
        return processAndSendMail(request.to(), request.subject(), request.content());
    }

    @Transactional
    public MailResponse sendTemplatedMail(TemplateMailRequest request) {
        checkDailyLimit();

        // 템플릿 조회 및 변수 치환
        MailTemplateResponse template = mailTemplateService.getTemplateByName(request.templateName());
        String processedSubject = mailTemplateService.processTemplate(template.subject(), request.variables());
        String processedContent = mailTemplateService.processTemplate(template.content(), request.variables());

        log.info("템플릿 메일 전송 요청: {} -> {} (템플릿: {})", processedSubject, request.to(), request.templateName());

        return processAndSendMail(request.to(), processedSubject, processedContent);
    }

    private MailResponse processAndSendMail(String to, String subject, String content) {
        // 메일 로그 생성
        MailLog savedMailLog = mailLogRepository.save(new MailLog(to, subject, content));

        // 메일 큐에 추가 (실제 전송은 MailQueueService에서 처리)
        mailQueueService.enqueueMailForSending(savedMailLog.getId());

        return MailResponse.from(savedMailLog);
    }

    private void checkDailyLimit() {
        try {
            int dailyLimit = mailSettingService.getSettingValueAsInt("daily_limit", 10000);
            LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
            long todayCount = mailLogRepository.countByStatusAndSentAtAfter(MailLog.MailStatus.SENT, startOfDay);

            if (todayCount >= dailyLimit) {
                throw new HermesException("일일 메일 전송 제한에 도달했습니다: " + dailyLimit);
            }
        } catch (HermesException e) {
            // HermesException은 다시 던져서 호출자가 처리하도록 함
            throw e;
        } catch (Exception e) {
            log.warn("일일 전송 제한 확인 중 오류 발생: {}", e.getMessage());
            // 설정 조회 실패 시에는 제한 없이 진행
        }
    }

    /**
     * 큐 상태 조회
     */
    public MailQueueService.QueueStatus getQueueStatus() {
        return mailQueueService.getQueueStatus();
    }

    @Transactional(readOnly = true)
    public HermesPageResponse<MailResponse> getMailLogs(HermesPageRequest hermesPageRequest) {
        Page<MailLog> page = mailLogRepository.findAll(hermesPageRequest.toPageable());
        return HermesPageResponse.from(page.map(MailResponse::from));
    }

    @Transactional(readOnly = true)
    public MailResponse getMailLog(Long id) {
        MailLog mailLog = mailLogRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("메일 로그", id.toString()));
        return MailResponse.from(mailLog);
    }

    @Transactional(readOnly = true)
    public HermesPageResponse<MailResponse> getMailLogsByStatus(MailLog.MailStatus status, HermesPageRequest hermesPageRequest) {
        Page<MailLog> page = mailLogRepository.findByStatus(status, hermesPageRequest.toPageable());
        return HermesPageResponse.from(page.map(MailResponse::from));
    }
}