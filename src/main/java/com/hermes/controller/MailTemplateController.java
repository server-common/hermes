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
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        mailTemplateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MailTemplateResponse> getTemplate(@PathVariable Long id) {
        MailTemplateResponse response = mailTemplateService.getTemplate(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<MailTemplateResponse> getTemplateByName(@PathVariable String name) {
        MailTemplateResponse response = mailTemplateService.getTemplateByName(name);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<HermesPageResponse<MailTemplateResponse>> getTemplates(@ModelAttribute @Valid HermesPageRequest hermesPageRequest) {
        HermesPageResponse<MailTemplateResponse> templates = mailTemplateService.getTemplates(hermesPageRequest);
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/search")
    public ResponseEntity<HermesPageResponse<MailTemplateResponse>> searchTemplates(@ModelAttribute @Valid HermesSearchRequest hermesSearchRequest) {
        HermesPageResponse<MailTemplateResponse> templates = mailTemplateService.searchTemplates(hermesSearchRequest);
        return ResponseEntity.ok(templates);
    }
}