package com.huawei.security.deviceauth;

import android.os.IBinder;

public class OperationParameter {
    private HwDevAuthCallback mCallbackHandler;
    private byte[] mPeerId;
    private int mPeerType;
    private byte[] mSelfId;
    private int mSelfType;
    private String mServiceType;
    private String mSessionId;

    public OperationParameter(SessionInfo sessionInfo) {
        this.mSessionId = sessionInfo.getSessionId();
        this.mServiceType = sessionInfo.getServiceType();
        this.mSelfId = sessionInfo.getSelfId();
        this.mSelfType = sessionInfo.getSelfType();
        this.mPeerId = sessionInfo.getPeerId();
        this.mPeerType = sessionInfo.getPeerType();
    }

    public OperationParameter(String sessionId) {
        this.mPeerType = -1;
        this.mSessionId = sessionId;
    }

    public SessionInfo getSessionInfo(IBinder token) {
        return new SessionInfo(this.mSessionId, this.mServiceType, this.mSelfId, this.mSelfType, this.mPeerId, this.mPeerType, token);
    }

    public String getSessionId() {
        return this.mSessionId;
    }

    public void setSessionId(String sessionId) {
        this.mSessionId = sessionId;
    }

    public String getServiceType() {
        return this.mServiceType;
    }

    public void setServiceType(String serviceType) {
        this.mServiceType = serviceType;
    }

    public byte[] getSelfId() {
        byte[] bArr = this.mSelfId;
        if (bArr == null) {
            return null;
        }
        return (byte[]) bArr.clone();
    }

    public void setSelfId(byte[] selfId) {
        if (selfId == null) {
            this.mSelfId = null;
        } else {
            this.mSelfId = (byte[]) selfId.clone();
        }
    }

    public int getSelfType() {
        return this.mSelfType;
    }

    public void setSelfType(int selfType) {
        this.mSelfType = selfType;
    }

    public byte[] getPeerId() {
        byte[] bArr = this.mPeerId;
        if (bArr == null) {
            return null;
        }
        return (byte[]) bArr.clone();
    }

    public void setPeerId(byte[] peerId) {
        if (peerId == null) {
            this.mPeerId = null;
        } else {
            this.mPeerId = (byte[]) peerId.clone();
        }
    }

    public HwDevAuthCallback getCallbackHandler() {
        return this.mCallbackHandler;
    }

    public void setCallbackHandler(HwDevAuthCallback callbackHandler) {
        this.mCallbackHandler = callbackHandler;
    }

    public int getPeerType() {
        return this.mPeerType;
    }

    public void setPeerType(int peerType) {
        this.mPeerType = peerType;
    }
}
