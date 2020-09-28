package com.huawei.security.keystore;

public class HwDeviceIdAttestationException extends Exception {
    private static final long serialVersionUID = 7817090988476150120L;

    public HwDeviceIdAttestationException(String detailMessage) {
        super(detailMessage);
    }

    public HwDeviceIdAttestationException(String detailMessage, Throwable cause) {
        super(detailMessage, cause);
    }
}
