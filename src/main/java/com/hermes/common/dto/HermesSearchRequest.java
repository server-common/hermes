package com.hermes.common.dto;

import jakarta.validation.Valid;

public record HermesSearchRequest(
    String keyword,

    @Valid
    HermesPageRequest hermesPageRequest
) {

    public HermesSearchRequest {
        if (hermesPageRequest == null) {
            hermesPageRequest = new HermesPageRequest(null, null, null, null);
        }
    }
}