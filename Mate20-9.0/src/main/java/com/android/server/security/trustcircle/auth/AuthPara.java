package com.android.server.security.trustcircle.auth;

import huawei.android.security.IAuthCallback;

public class AuthPara {

    public static class InitAuthInfo {
        public static final int AUTH_STATUS_INIT = 0;
        public byte[] mAESTmpKey;
        public long mAuthID;
        public int mAuthType;
        public int mAuthVersion;
        public IAuthCallback mCallback;
        public int mPolicy;
        public long mUserID;

        public InitAuthInfo(IAuthCallback callback, long authID, int authType, int authVersion, int policy, long userID, byte[] AESTmpKey) {
            this.mAuthID = authID;
            this.mAuthType = authType;
            this.mAuthVersion = authVersion;
            this.mPolicy = policy;
            this.mUserID = userID;
            this.mAESTmpKey = AESTmpKey;
            this.mCallback = callback;
        }
    }

    public static class OnAuthAckInfo {
        public long mAuthID;
        public byte[] mMAC;
        public int mResult;
        public byte[] mSessionKey;
        public byte[] mSessionKeyIV;

        public OnAuthAckInfo(long authID, byte[] sessionKeyIV, byte[] sessionKey, byte[] mac, int result) {
            this.mAuthID = authID;
            this.mResult = result;
            this.mSessionKeyIV = sessionKeyIV;
            this.mSessionKey = sessionKey;
            this.mMAC = mac;
        }
    }

    public static class OnAuthSyncAckInfo {
        public long mAuthID;
        public short mAuthKeyAlgoType;
        public byte[] mAuthKeyInfo;
        public byte[] mAuthKeyInfoSign;
        public byte[] mMAC;
        public long mNonceSlave;
        public int mPkVersionSlave;
        public int mResult;
        public byte[] mTcisIdSlave;

        public OnAuthSyncAckInfo(long authID, byte[] tcisIdSlave, int pkVersionSlave, long nonceSlave, byte[] mac, short authKeyAlgoType, byte[] authKeyInfo, byte[] authKeyInfoSign, int result) {
            this.mAuthID = authID;
            this.mTcisIdSlave = tcisIdSlave;
            this.mPkVersionSlave = pkVersionSlave;
            this.mNonceSlave = nonceSlave;
            this.mMAC = mac;
            this.mAuthKeyAlgoType = authKeyAlgoType;
            this.mAuthKeyInfo = authKeyInfo;
            this.mAuthKeyInfoSign = authKeyInfoSign;
            this.mResult = result;
        }
    }

    public static class OnAuthSyncInfo {
        public long mAuthID;
        public short mAuthKeyAlgoType;
        public byte[] mAuthKeyInfo;
        public byte[] mAuthKeyInfoSign;
        public int mIndexVersion;
        public long mNonce;
        public int mResult;
        public short mTAVersion;
        public byte[] mTcisId;

        public OnAuthSyncInfo(long authID, byte[] tcisId, int indexVersion, short taVersion, long nonce, short authKeyAlgoType, byte[] authKeyInfo, byte[] authKeyInfoSign, int result) {
            this.mAuthID = authID;
            this.mTcisId = tcisId;
            this.mIndexVersion = indexVersion;
            this.mTAVersion = taVersion;
            this.mNonce = nonce;
            this.mAuthKeyAlgoType = authKeyAlgoType;
            this.mAuthKeyInfo = authKeyInfo;
            this.mAuthKeyInfoSign = authKeyInfoSign;
            this.mResult = result;
        }
    }

    public static class RecAckInfo {
        public long mAuthID;
        public byte[] mMAC;

        public RecAckInfo(long authID, byte[] mac) {
            this.mAuthID = authID;
            this.mMAC = mac;
        }
    }

    public static class RecAuthAckInfo {
        public long mAuthID;
        public short mAuthKeyAlgoTypeSlave;
        public byte[] mAuthKeyInfoSignSlave;
        public byte[] mAuthKeyInfoSlave;
        public byte[] mMacSlave;
        public long mNonceSlave;
        public int mPkVersionSlave;
        public byte[] mTcisIDSlave;

        public RecAuthAckInfo(long authID, byte[] tcisId, int pkVersion, long nonce, byte[] mac, short authKeyAlgoType, byte[] authKeyInfo, byte[] authKeyInfoSign) {
            this.mAuthID = authID;
            this.mTcisIDSlave = tcisId;
            this.mPkVersionSlave = pkVersion;
            this.mNonceSlave = nonce;
            this.mMacSlave = mac;
            this.mAuthKeyAlgoTypeSlave = authKeyAlgoType;
            this.mAuthKeyInfoSlave = authKeyInfo;
            this.mAuthKeyInfoSignSlave = authKeyInfoSign;
        }
    }

    public static class RecAuthInfo {
        public byte[] mAESTmpKey;
        public long mAuthID;
        public short mAuthKeyAlgoType;
        public byte[] mAuthKeyInfo;
        public byte[] mAuthKeyInfoSign;
        public int mAuthType;
        public int mAuthVersion;
        public IAuthCallback mCallback;
        public long mNonce;
        public int mPkVersion;
        public int mPolicy;
        public short mTAVersion;
        public byte[] mTcisId;
        public long mUserID;

        public RecAuthInfo(long authID, IAuthCallback callback, int authType, int authVersion, short taVersion, int policy, long userID, byte[] AESTmpKey, byte[] tcisId, int pkVersion, long nonce, short authKeyAlgoType, byte[] authKeyInfo, byte[] authKeyInfoSign) {
            this.mAuthID = authID;
            this.mCallback = callback;
            this.mAuthType = authType;
            this.mAuthVersion = authVersion;
            this.mTAVersion = taVersion;
            this.mPolicy = policy;
            this.mUserID = userID;
            this.mAESTmpKey = AESTmpKey;
            this.mTcisId = tcisId;
            this.mPkVersion = pkVersion;
            this.mNonce = nonce;
            this.mAuthKeyAlgoType = authKeyAlgoType;
            this.mAuthKeyInfo = authKeyInfo;
            this.mAuthKeyInfoSign = authKeyInfoSign;
        }
    }

    public static class ReqPkInfo {
        public long mAuthID;
        public long mUserID;

        public ReqPkInfo(long userID, long authID) {
            this.mUserID = userID;
            this.mAuthID = authID;
        }
    }

    public static class RespPkInfo {
        public long mAuthID;
        public short mAuthKeyAlgoType;
        public byte[] mAuthKeyData;
        public byte[] mAuthKeyDataSign;
        public int mResult;

        public RespPkInfo(long authID, short authKeyAlgoType, byte[] authKeyData, byte[] authKeyDataSign, int result) {
            this.mAuthID = authID;
            this.mAuthKeyAlgoType = authKeyAlgoType;
            this.mAuthKeyData = authKeyData;
            this.mAuthKeyDataSign = authKeyDataSign;
            this.mResult = result;
        }
    }
}
