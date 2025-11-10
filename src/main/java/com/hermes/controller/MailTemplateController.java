package com.hermes.controller;

import com.hermes.common.dto.HermesPageRequest;
import com.hermes.common.dto.HermesPageResponse;
import com.hermes.common.dto.HermesSearchRequest;
import com.hermes.dto.MailTemplateRequest;
import com.hermes.dto.MailTemplateResponse;
import com.hermes.service.MailTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mail/template")
public class MailTemplateController {

    private final MailTemplateService mailTemplateService;

    @PostMapping
    public ResponseEntity<MailTemplateResponse> createTemplate(@Valid @RequestBody MailTemplateRequest request) {
        MailTemplateResponse response = mailTemplateService.createTemplate(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MailTemplateResponse> updateTemplate(@PathVariable Long id, @Valid @RequestBody MailTemplateRequest request) {
        MailTemplateResponse response = mailTemplateService.updateTemplate(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id,
        @org.springframework.web.bind.annotation.RequestParam String groupKey) {
        mailTemplateService.deleteTemplate(id, groupKey);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MailTemplateResponse> getTemplate(@PathVariable Long id,
        @org.springframework.web.bind.annotation.RequestParam String groupKey) {
        MailTemplateResponse response = mailTemplateService.getTemplate(id, groupKey);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<MailTemplateResponse> getTemplateByName(@PathVariable String name,
        @org.springframework.web.bind.annotation.RequestParam String groupKey) {
        MailTemplateResponse response = mailTemplateService.getTemplateByName(name, groupKey);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<HermesPageResponse<MailTemplateResponse>> getTemplates(@ModelAttribute @Valid HermesPageRequest hermesPageRequest,
        @org.springframework.web.bind.annotation.RequestParam String groupKey) {
        HermesPageResponse<MailTemplateResponse> templates = mailTemplateService.getTemplates(hermesPageRequest, groupKey);
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/search")
    public ResponseEntity<HermesPageResponse<MailTemplateResponse>> searchTemplates(@ModelAttribute @Valid HermesSearchRequest hermesSearchRequest,
        @org.springframework.web.bind.annotation.RequestParam String groupKey) {
        HermesPageResponse<MailTemplateResponse> templates = mailTemplateService.searchTemplates(hermesSearchRequest, groupKey);
        return ResponseEntity.ok(templates);
    }
}