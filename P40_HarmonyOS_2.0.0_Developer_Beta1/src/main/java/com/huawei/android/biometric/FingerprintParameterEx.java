package com.huawei.android.biometric;

import android.os.IBinder;

public class FingerprintParameterEx {
    private byte[] mCryptoTokens;
    private int mFlags;
    private int mGroupId;
    private long mOpId;
    private String mOpPackageName;
    private BiometricServiceReceiverListenerEx mReceiver;
    private IBinder mToken;
    private int mUserId;

    public IBinder getToken() {
        return this.mToken;
    }

    public void setToken(IBinder token) {
        this.mToken = token;
    }

    public byte[] getCryptoToken() {
        return this.mCryptoTokens;
    }

    public void setCryptoToken(byte[] cryptoToken) {
        this.mCryptoTokens = cryptoToken;
    }

    public int getUserId() {
        return this.mUserId;
    }

    public void setUserId(int userId) {
        this.mUserId = userId;
    }

    public BiometricServiceReceiverListenerEx getReceiver() {
        return this.mReceiver;
    }

    public void setReceiver(BiometricServiceReceiverListenerEx receiver) {
        this.mReceiver = receiver;
    }

    public int getFlags() {
        return this.mFlags;
    }

    public void setFlags(int flags) {
        this.mFlags = flags;
    }

    public String getOpPackageName() {
        return this.mOpPackageName;
    }

    public void setOpPackageName(String opPackageName) {
        this.mOpPackageName = opPackageName;
    }

    public long getOpId() {
        return this.mOpId;
    }

    public void setOpId(long opId) {
        this.mOpId = opId;
    }

    public int getGroupId() {
        return this.mGroupId;
    }

    public void setGroupId(int groupId) {
        this.mGroupId = groupId;
    }
}
