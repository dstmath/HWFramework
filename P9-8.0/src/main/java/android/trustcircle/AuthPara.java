package android.trustcircle;

public class AuthPara {

    public static class InitAuthInfo {
        public byte[] mAESTmpKey;
        public int mAuthType;
        public int mAuthVersion;
        public int mPolicy;
        public long mUserID;

        public InitAuthInfo(int authType, int authVersion, int policy, long userID, byte[] AESTmpKey) {
            this.mAuthType = authType;
            this.mAuthVersion = authVersion;
            this.mPolicy = policy;
            this.mUserID = userID;
            this.mAESTmpKey = AESTmpKey;
        }
    }

    public static class OnAuthAckInfo {
        public byte[] mMAC;
        public int mResult;
        public byte[] mSessionKey;
        public byte[] mSessionKeyIV;

        public OnAuthAckInfo(int result, byte[] sessionKeyIV, byte[] sessionKey, byte[] mac) {
            this.mResult = result;
            this.mSessionKeyIV = sessionKeyIV;
            this.mSessionKey = sessionKey;
            this.mMAC = mac;
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
            this.mTcisIdSlave = tcisIdSlave;
            this.mPkVersionSlave = pkVersionSlave;
            this.mNonceSlave = nonceSlave;
            this.mMAC = mac;
            this.mAuthKeyAlgoType = authKeyAlgoType;
            this.mAuthKeyInfo = authKeyInfo;
            this.mAuthKeyInfoSign = authKeyInfoSign;
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
            this.mTcisId = tcisId;
            this.mPkVersion = pkVersion;
            this.mTAVersion = taVersion;
            this.mNonce = nonce;
            this.mAuthKeyAlgoType = authKeyAlgoType;
            this.mAuthKeyInfo = authKeyInfo;
            this.mAuthKeyInfoSign = authKeyInfoSign;
        }
    }

    public static class RecAckInfo {
        public byte[] mMAC;

        public RecAckInfo(byte[] mac) {
            this.mMAC = mac;
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

        public RecAuthInfo(int authType, int authVersion, short taVersion, int policy, long userID, byte[] AESTmpKey, byte[] tcisId, int pkVersion, long nonce, short authKeyAlgoType, byte[] authKeyInfo, byte[] authKeyInfoSign) {
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
        public long mUserID;

        public ReqPkInfo(long userID) {
            this.mUserID = userID;
        }
    }

    public static class RespPkInfo {
        public short mAuthKeyAlgoType;
        public byte[] mAuthKeyData;
        public byte[] mAuthKeyDataSign;

        public RespPkInfo(short authKeyAlgoType, byte[] authKeyData, byte[] authKeyDataSign) {
            this.mAuthKeyAlgoType = authKeyAlgoType;
            this.mAuthKeyData = authKeyData;
            this.mAuthKeyDataSign = authKeyDataSign;
        }
    }

    public enum Type {
        REC_AUTH_SYNC_ACK,
        REC_ACK,
        REQ_PK,
        REC_PK
    }
}
