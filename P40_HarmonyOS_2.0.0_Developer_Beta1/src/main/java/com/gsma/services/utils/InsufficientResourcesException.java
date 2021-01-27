package com.gsma.services.utils;

public class InsufficientResourcesException extends Exception {
    public InsufficientResourcesException() {
    }

    public InsufficientResourcesException(String detailMessage) {
        super(detailMessage);
    }
}
