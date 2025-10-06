package com.hermes.controller;

import com.hermes.common.dto.HermesPageRequest;
import com.hermes.common.dto.HermesPageResponse;
import com.hermes.dto.MailSettingRequest;
import com.hermes.dto.MailSettingResponse;
import com.hermes.service.MailSettingService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
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
@RequestMapping("/mail/setting")
public class MailSettingController {

    private final MailSettingService mailSettingService;

    @PostMapping
    public ResponseEntity<MailSettingResponse> createSetting(@Valid @RequestBody MailSettingRequest request) {
        MailSettingResponse response = mailSettingService.createSetting(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MailSettingResponse> updateSetting(@PathVariable Long id, @Valid @RequestBody MailSettingRequest request) {
        MailSettingResponse response = mailSettingService.updateSetting(id, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/key/{key}")
    public ResponseEntity<MailSettingResponse> updateSettingByKey(@PathVariable String key, @RequestBody Map<String, String> request) {
        String value = request.get("value");
        if (value == null) {
            return ResponseEntity.badRequest().build();
        }

        MailSettingResponse response = mailSettingService.updateSettingByKey(key, value);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSetting(@PathVariable Long id) {
        mailSettingService.deleteSetting(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MailSettingResponse> getSetting(@PathVariable Long id) {
        MailSettingResponse response = mailSettingService.getSetting(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/key/{key}")
    public ResponseEntity<MailSettingResponse> getSettingByKey(@PathVariable String key) {
        MailSettingResponse response = mailSettingService.getSettingByKeyResponse(key);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/key/{key}/value")
    public ResponseEntity<Map<String, String>> getSettingValue(@PathVariable String key) {
        String value = mailSettingService.getSettingValue(key);
        return ResponseEntity.ok(Map.of("key", key, "value", value));
    }

    @GetMapping
    public ResponseEntity<HermesPageResponse<MailSettingResponse>> getSettings(@ModelAttribute @Valid HermesPageRequest hermesPageRequest) {
        HermesPageResponse<MailSettingResponse> settings = mailSettingService.getSettings(hermesPageRequest);
        return ResponseEntity.ok(settings);
    }

    @GetMapping("/all")
    public ResponseEntity<List<MailSettingResponse>> getAllSettings() {
        List<MailSettingResponse> settings = mailSettingService.getAllSettings();
        return ResponseEntity.ok(settings);
    }
}