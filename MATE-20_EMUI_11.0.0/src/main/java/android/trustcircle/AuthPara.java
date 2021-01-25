package android.trustcircle;

public class AuthPara {

    public enum Type {
        REC_AUTH_SYNC_ACK,
        REC_ACK,
        REQ_PK,
        REC_PK
    }

    public static class InitAuthInfo {
        public byte[] mAESTmpKey;
        public int mAuthType;
        public int mAuthVersion;
        public int mPolicy;
        public long mUserID;

        public InitAuthInfo(int authType, int authVersion, int policy, long userId, byte[] aesTmpKey) {
            this.mAuthType = authType;
            this.mAuthVersion = authVersion;
            this.mPolicy = policy;
            this.mUserID = userId;
            this.mAESTmpKey = (byte[]) aesTmpKey.clone();
        }
    }

    public static class OnAuthSyncInfo {
        public short mAuthKeyAlgoType;
        public byte[] mAuthKeyInfo;
        public byte[] mAuthKeyInfoSign;
        public long mNonce;
        public int mPkVersion;
        public short mTAVersion;
        public byte[] mTcisId;

        public OnAuthSyncInfo(byte[] tcisId, int pkVersion, short taVersion, long nonce, short authKeyAlgoType, byte[] authKeyInfo, byte[] authKeyInfoSign) {
            this.mTcisId = (byte[]) tcisId.clone();
            this.mPkVersion = pkVersion;
            this.mTAVersion = taVersion;
            this.mNonce = nonce;
            this.mAuthKeyAlgoType = authKeyAlgoType;
            this.mAuthKeyInfo = (byte[]) authKeyInfo.clone();
            this.mAuthKeyInfoSign = (byte[]) authKeyInfoSign.clone();
        }
    }

    public static class RecAuthAckInfo {
        public short mAuthKeyAlgoTypeSlave;
        public byte[] mAuthKeyInfoSignSlave;
        public byte[] mAuthKeyInfoSlave;
        public byte[] mMacSlave;
        public long mNonceSlave;
        public int mPkVersionSlave;
        public byte[] mTcisIDSlave;

        public RecAuthAckInfo(byte[] tcisId, int pkVersion, long nonce, byte[] mac, short authKeyAlgoType, byte[] authKeyInfo, byte[] authKeyInfoSign) {
            this.mTcisIDSlave = (byte[]) tcisId.clone();
            this.mPkVersionSlave = pkVersion;
            this.mNonceSlave = nonce;
            this.mMacSlave = (byte[]) mac.clone();
            this.mAuthKeyAlgoTypeSlave = authKeyAlgoType;
            this.mAuthKeyInfoSlave = (byte[]) authKeyInfo.clone();
            this.mAuthKeyInfoSignSlave = (byte[]) authKeyInfoSign.clone();
        }
    }

    public static class ReqPkInfo {
        public long mUserID;

        public ReqPkInfo(long userId) {
            this.mUserID = userId;
        }
    }

    public static class RespPkInfo {
        public short mAuthKeyAlgoType;
        public byte[] mAuthKeyData;
        public byte[] mAuthKeyDataSign;

        public RespPkInfo(short authKeyAlgoType, byte[] authKeyData, byte[] authKeyDataSign) {
            this.mAuthKeyAlgoType = authKeyAlgoType;
            this.mAuthKeyData = (byte[]) authKeyData.clone();
            this.mAuthKeyDataSign = (byte[]) authKeyDataSign.clone();
        }
    }

    public static class OnAuthAckInfo {
        public byte[] mMAC;
        public int mResult;
        public byte[] mSessionKey;
        public byte[] mSessionKeyIV;

        public OnAuthAckInfo(int result, byte[] sessionKeyIv, byte[] sessionKey, byte[] mac) {
            this.mResult = result;
            this.mSessionKeyIV = (byte[]) sessionKeyIv.clone();
            this.mSessionKey = (byte[]) sessionKey.clone();
            this.mMAC = (byte[]) mac.clone();
        }
    }

    public static class RecAuthInfo {
        public byte[] mAESTmpKey;
        public short mAuthKeyAlgoType;
        public byte[] mAuthKeyInfo;
        public byte[] mAuthKeyInfoSign;
        public int mAuthType;
        public int mAuthVersion;
        public long mNonce;
        public int mPkVersion;
        public int mPolicy;
        public short mTAVersion;
        public byte[] mTcisId;
        public long mUserID;

        public RecAuthInfo(int authType, int authVersion, short taVersion, int policy, long userId, byte[] aesTmpKey, byte[] tcisId, int pkVersion, long nonce, short authKeyAlgoType, byte[] authKeyInfo, byte[] authKeyInfoSign) {
            this.mAuthType = authType;
            this.mAuthVersion = authVersion;
            this.mTAVersion = taVersion;
            this.mPolicy = policy;
            this.mUserID = userId;
            this.mAESTmpKey = (byte[]) aesTmpKey.clone();
            this.mTcisId = (byte[]) tcisId.clone();
            this.mPkVersion = pkVersion;
            this.mNonce = nonce;
            this.mAuthKeyAlgoType = authKeyAlgoType;
            this.mAuthKeyInfo = (byte[]) authKeyInfo.clone();
            this.mAuthKeyInfoSign = (byte[]) authKeyInfoSign.clone();
        }
    }

    public static class OnAuthSyncAckInfo {
        public short mAuthKeyAlgoType;
        public byte[] mAuthKeyInfo;
        public byte[] mAuthKeyInfoSign;
        public byte[] mMAC;
        public long mNonceSlave;
        public int mPkVersionSlave;
        public byte[] mTcisIdSlave;

        public OnAuthSyncAckInfo(byte[] tcisIdSlave, int pkVersionSlave, long nonceSlave, byte[] mac, short authKeyAlgoType, byte[] authKeyInfo, byte[] authKeyInfoSign) {
            this.mTcisIdSlave = (byte[]) tcisIdSlave.clone();
            this.mPkVersionSlave = pkVersionSlave;
            this.mNonceSlave = nonceSlave;
            this.mMAC = (byte[]) mac.clone();
            this.mAuthKeyAlgoType = authKeyAlgoType;
            this.mAuthKeyInfo = (byte[]) authKeyInfo.clone();
            this.mAuthKeyInfoSign = (byte[]) authKeyInfoSign.clone();
        }
    }

    public static class RecAckInfo {
        public byte[] mMAC;

        public RecAckInfo(byte[] mac) {
            this.mMAC = (byte[]) mac.clone();
        }
    }
}
