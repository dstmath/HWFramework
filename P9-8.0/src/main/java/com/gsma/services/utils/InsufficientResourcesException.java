package com.gsma.services.utils;

public class InsufficientResourcesException extends Exception {
    public InsufficientResourcesException(String detailMessage) {
        super(detailMessage);
    }
}
