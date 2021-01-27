package com.huawei.android.biometric;

import android.content.Context;
import android.os.IBinder;
import com.huawei.android.biometric.FingerprintServiceEx;

public class ClientMonitorParameterEx {
    private boolean isRequireConfirmation;
    private boolean isRestricted;
    private String mAaid;
    private int mCallingUserId;
    private FingerprintServiceEx.ConstantsEx mConstants;
    private Context mContext;
    private int mCookie;
    private FingerprintServiceEx.DaemonWrapperEx mDaemon;
    private int mFlags;
    private int mGroupId;
    private long mHalDeviceId;
    private BiometricServiceReceiverListenerEx mListener;
    private byte[] mNonces;
    private long mOpId;
    private String mOwner;
    private int mTargetUserId;
    private IBinder mToken;

    public Context getContext() {
        return this.mContext;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    public FingerprintServiceEx.ConstantsEx getConstants() {
        return this.mConstants;
    }

    public void setConstants(FingerprintServiceEx.ConstantsEx constants) {
        this.mConstants = constants;
    }

    public FingerprintServiceEx.DaemonWrapperEx getDaemon() {
        return this.mDaemon;
    }

    public void setDaemon(FingerprintServiceEx.DaemonWrapperEx daemon) {
        this.mDaemon = daemon;
    }

    public long getHalDeviceId() {
        return this.mHalDeviceId;
    }

    public void setHalDeviceId(long halDeviceId) {
        this.mHalDeviceId = halDeviceId;
    }

    public IBinder getToken() {
        return this.mToken;
    }

    public void setToken(IBinder token) {
        this.mToken = token;
    }

    public BiometricServiceReceiverListenerEx getListener() {
        return this.mListener;
    }

    public void setListener(BiometricServiceReceiverListenerEx listener) {
        this.mListener = listener;
    }

    public int getTargetUserId() {
        return this.mTargetUserId;
    }

    public void setTargetUserId(int targetUserId) {
        this.mTargetUserId = targetUserId;
    }

    public int getGroupId() {
        return this.mGroupId;
    }

    public void setGroupId(int groupId) {
        this.mGroupId = groupId;
    }

    public long getOpId() {
        return this.mOpId;
    }

    public void setOpId(long opId) {
        this.mOpId = opId;
    }

    public boolean isRestricted() {
        return this.isRestricted;
    }

    public void setRestricted(boolean isRequireRestricted) {
        this.isRestricted = isRequireRestricted;
    }

    public String getOwner() {
        return this.mOwner;
    }

    public void setOwner(String owner) {
        this.mOwner = owner;
    }

    public int getCookie() {
        return this.mCookie;
    }

    public void setCookie(int cookie) {
        this.mCookie = cookie;
    }

    public boolean isRequireConfirmation() {
        return this.isRequireConfirmation;
    }

    public void setRequireConfirmation(boolean isConfirmation) {
        this.isRequireConfirmation = isConfirmation;
    }

    public int getCallingUserId() {
        return this.mCallingUserId;
    }

    public void setCallingUserId(int callingUserId) {
        this.mCallingUserId = callingUserId;
    }

    public String getAaid() {
        return this.mAaid;
    }

    public void setAaid(String aaid) {
        this.mAaid = aaid;
    }

    public byte[] getNonce() {
        return this.mNonces;
    }

    public void setNonce(byte[] nonce) {
        this.mNonces = nonce;
    }

    public int getFlags() {
        return this.mFlags;
    }

    public void setFlags(int flags) {
        this.mFlags = flags;
    }
}
