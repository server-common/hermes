package com.hermes.controller;

import com.hermes.common.dto.HermesPageRequest;
import com.hermes.common.dto.HermesPageResponse;
import com.hermes.dto.BulkMailRequest;
import com.hermes.dto.BulkMailResponse;
import com.hermes.dto.BulkMailStatusResponse;
import com.hermes.dto.BulkTemplateMailRequest;
import com.hermes.dto.MailRequest;
import com.hermes.dto.MailResponse;
import com.hermes.dto.TemplateMailRequest;
import com.hermes.entity.MailLog;
import com.hermes.service.MailQueueService;
import com.hermes.service.MailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mail")
public class MailController {

    private final MailService mailService;

    @PostMapping("/send")
    public ResponseEntity<MailResponse> sendMail(@Valid @RequestBody MailRequest request) {
        MailResponse response = mailService.sendMail(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send/template")
    public ResponseEntity<MailResponse> sendTemplatedMail(@Valid @RequestBody TemplateMailRequest request) {
        MailResponse response = mailService.sendTemplatedMail(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/logs")
    public ResponseEntity<HermesPageResponse<MailResponse>> getMailLogs(@ModelAttribute @Valid HermesPageRequest hermesPageRequest,
        @org.springframework.web.bind.annotation.RequestParam String groupKey) {
        HermesPageResponse<MailResponse> logs = mailService.getMailLogs(hermesPageRequest, groupKey);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/logs/{id}")
    public ResponseEntity<MailResponse> getMailLog(@PathVariable Long id,
        @org.springframework.web.bind.annotation.RequestParam String groupKey) {
        MailResponse response = mailService.getMailLog(id, groupKey);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/logs/status/{status}")
    public ResponseEntity<HermesPageResponse<MailResponse>> getMailLogsByStatus(@PathVariable MailLog.MailStatus status,
        @ModelAttribute @Valid HermesPageRequest hermesPageRequest,
        @org.springframework.web.bind.annotation.RequestParam String groupKey) {

        HermesPageResponse<MailResponse> logs = mailService.getMailLogsByStatus(status, hermesPageRequest, groupKey);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/queue/status")
    public ResponseEntity<MailQueueService.QueueStatus> getQueueStatus() {
        MailQueueService.QueueStatus status = mailService.getQueueStatus();
        return ResponseEntity.ok(status);
    }

    @PostMapping("/send/bulk")
    public ResponseEntity<BulkMailResponse> sendBulkMail(@Valid @RequestBody BulkMailRequest request) {
        BulkMailResponse response = mailService.sendBulkMail(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send/bulk/template")
    public ResponseEntity<BulkMailResponse> sendBulkTemplatedMail(@Valid @RequestBody BulkTemplateMailRequest request) {
        BulkMailResponse response = mailService.sendBulkTemplatedMail(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/bulk/status/{batchId}")
    public ResponseEntity<BulkMailStatusResponse> getBulkMailStatus(@PathVariable String batchId,
        @org.springframework.web.bind.annotation.RequestParam String groupKey) {
        BulkMailStatusResponse response = mailService.getBulkMailBatchStatus(batchId, groupKey);
        return ResponseEntity.ok(response);
    }
}