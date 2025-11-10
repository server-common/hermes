package com.hermes.service;

import com.hermes.common.dto.HermesPageRequest;
import com.hermes.common.dto.HermesPageResponse;
import com.hermes.common.dto.HermesSearchRequest;
import com.hermes.dto.MailTemplateRequest;
import com.hermes.dto.MailTemplateResponse;
import com.hermes.entity.MailTemplate;
import com.hermes.exception.DuplicateResourceException;
import com.hermes.exception.ResourceNotFoundException;
import com.hermes.repository.MailTemplateRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailTemplateService {

    private final MailTemplateRepository mailTemplateRepository;

    @Transactional(readOnly = true)
    public java.util.List<String> getAllGroupKeysForTemplates() {
        return mailTemplateRepository.findDistinctGroupKeys();
    }

    @Transactional
    public MailTemplateResponse createTemplate(MailTemplateRequest request) {
        validateTemplateNameUniqueness(request.name(), null, request.groupKey());

        MailTemplate template = buildTemplate(request);
        MailTemplate savedTemplate = mailTemplateRepository.save(template);

        log.info("메일 템플릿 생성: {}", savedTemplate.getName());
        return MailTemplateResponse.from(savedTemplate);
    }

    @Transactional
    public MailTemplateResponse updateTemplate(Long id, MailTemplateRequest request) {
        MailTemplate template = getTemplateById(id, request.groupKey());
        validateTemplateNameUniqueness(request.name(), template.getName(), request.groupKey());

        updateTemplateFields(template, request);
        MailTemplate updatedTemplate = mailTemplateRepository.save(template);

        log.info("메일 템플릿 수정: {}", updatedTemplate.getName());
        return MailTemplateResponse.from(updatedTemplate);
    }

    @Transactional
    public void deleteTemplate(Long id, String groupKey) {
        MailTemplate template = getTemplateById(id, groupKey);
        mailTemplateRepository.delete(template);
        log.info("메일 템플릿 삭제: {}", template.getName());
    }

    @Transactional(readOnly = true)
    public MailTemplateResponse getTemplate(Long id, String groupKey) {
        return MailTemplateResponse.from(getTemplateById(id, groupKey));
    }

    @Transactional(readOnly = true)
    public MailTemplateResponse getTemplateByName(String name) {
        throw new UnsupportedOperationException("Use getTemplateByName(name, groupKey)");
    }

    @Transactional(readOnly = true)
    public MailTemplateResponse getTemplateByName(String name, String groupKey) {
        MailTemplate template = mailTemplateRepository.findByNameAndGroupKey(name, groupKey)
            .orElseThrow(() -> new ResourceNotFoundException("템플릿", name));
        return MailTemplateResponse.from(template);
    }

    @Transactional(readOnly = true)
    public HermesPageResponse<MailTemplateResponse> getTemplates(HermesPageRequest hermesPageRequest, String groupKey) {
        Page<MailTemplate> page = mailTemplateRepository.findByGroupKey(groupKey, hermesPageRequest.toPageable());
        return HermesPageResponse.from(page.map(MailTemplateResponse::from));
    }

    @Transactional(readOnly = true)
    public HermesPageResponse<MailTemplateResponse> searchTemplates(HermesSearchRequest hermesSearchRequest, String groupKey) {
        Page<MailTemplate> page = mailTemplateRepository.findByKeywordAndGroupKey(
            hermesSearchRequest.keyword(), groupKey, hermesSearchRequest.hermesPageRequest().toPageable());
        return HermesPageResponse.from(page.map(MailTemplateResponse::from));
    }

    /**
     * 템플릿 변수를 치환하여 실제 메일 내용을 생성
     */
    public String processTemplate(String templateContent, Map<String, String> variables) {
        if (variables == null || variables.isEmpty()) {
            return templateContent;
        }

        String processedContent = templateContent;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            processedContent = processedContent.replace(placeholder, entry.getValue());
        }

        return processedContent;
    }

    // Private helper methods
    private MailTemplate getTemplateById(Long id, String groupKey) {
        return mailTemplateRepository.findByIdAndGroupKey(id, groupKey)
            .orElseThrow(() -> new ResourceNotFoundException("템플릿", id.toString()));
    }

    private void validateTemplateNameUniqueness(String newName, String currentName, String groupKey) {
        if (currentName == null || !currentName.equals(newName)) {
            if (mailTemplateRepository.existsByNameAndGroupKey(newName, groupKey)) {
                throw new DuplicateResourceException("템플릿 이름", newName);
            }
        }
    }

    private MailTemplate buildTemplate(MailTemplateRequest request) {
        return MailTemplate.builder()
            .groupKey(request.groupKey())
            .name(request.name())
            .subject(request.subject())
            .content(request.content())
            .isHtml(request.isHtml())
            .build();
    }

    private void updateTemplateFields(MailTemplate template, MailTemplateRequest request) {
        template.update(request);
    }
}