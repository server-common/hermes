package com.hermes.exception;

public class DuplicateResourceException extends HermesException {

    public DuplicateResourceException(String resourceType, String identifier) {
        super(String.format("이미 존재하는 %s입니다: %s", resourceType, identifier));
    }
}