package com.huawei.security.keystore;

public class HwAssetStoreManagerException extends Exception {
    public HwAssetStoreManagerException(String detailMessage) {
        super(detailMessage);
    }

    public HwAssetStoreManagerException(String detailMessage, Throwable cause) {
        super(detailMessage, cause);
    }
}
