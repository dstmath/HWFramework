package com.huawei.security.keystore;

public class HwDeviceIdAttestationException extends Exception {
    public HwDeviceIdAttestationException(String detailMessage) {
        super(detailMessage);
    }

    public HwDeviceIdAttestationException(String detailMessage, Throwable cause) {
        super(detailMessage, cause);
    }
}
