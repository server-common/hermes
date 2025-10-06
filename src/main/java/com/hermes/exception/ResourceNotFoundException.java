package com.hermes.exception;

public class ResourceNotFoundException extends HermesException {

    public ResourceNotFoundException(String resourceType, String identifier) {
        super(String.format("%s을(를) 찾을 수 없습니다: %s", resourceType, identifier));
    }
}