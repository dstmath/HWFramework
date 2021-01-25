package com.android.server.wifi.util;

import java.io.Serializable;

public class EncryptedData implements Serializable {
    private static final long serialVersionUID = 1337;
    private byte[] mEncryptedData;
    private byte[] mIv;
    private String mKeyAlias;

    public EncryptedData(byte[] encryptedData, byte[] iv, String keyAlias) {
        this.mEncryptedData = encryptedData;
        this.mIv = iv;
        this.mKeyAlias = keyAlias;
    }

    public byte[] getEncryptedData() {
        return this.mEncryptedData;
    }

    public byte[] getIv() {
        return this.mIv;
    }

    public String getKeyAlias() {
        return this.mKeyAlias;
    }
}
