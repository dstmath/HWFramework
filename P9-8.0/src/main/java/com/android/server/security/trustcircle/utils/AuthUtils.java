package com.android.server.security.trustcircle.utils;

import com.android.server.security.trustcircle.auth.AuthPara.InitAuthInfo;
import com.android.server.security.trustcircle.auth.AuthPara.OnAuthAckInfo;
import com.android.server.security.trustcircle.auth.AuthPara.OnAuthSyncAckInfo;
import com.android.server.security.trustcircle.auth.AuthPara.OnAuthSyncInfo;
import com.android.server.security.trustcircle.auth.AuthPara.RecAckInfo;
import com.android.server.security.trustcircle.auth.AuthPara.RecAuthAckInfo;
import com.android.server.security.trustcircle.auth.AuthPara.RecAuthInfo;
import com.android.server.security.trustcircle.auth.AuthPara.ReqPkInfo;
import com.android.server.security.trustcircle.auth.AuthPara.RespPkInfo;
import com.android.server.security.trustcircle.jni.TcisJNI;
import com.android.server.security.trustcircle.tlv.command.auth.CMD_AUTH_ACK_RECV;
import com.android.server.security.trustcircle.tlv.command.auth.CMD_AUTH_CANCEL;
import com.android.server.security.trustcircle.tlv.command.auth.CMD_AUTH_MASTER_RECV_KEY;
import com.android.server.security.trustcircle.tlv.command.auth.CMD_AUTH_SLAVE_RECV_KEY;
import com.android.server.security.trustcircle.tlv.command.auth.CMD_AUTH_SYNC;
import com.android.server.security.trustcircle.tlv.command.auth.CMD_AUTH_SYNC_ACK_RECV;
import com.android.server.security.trustcircle.tlv.command.auth.CMD_AUTH_SYNC_RECV;
import com.android.server.security.trustcircle.tlv.command.auth.RET_AUTH_ACK_RECV;
import com.android.server.security.trustcircle.tlv.command.auth.RET_AUTH_SYNC;
import com.android.server.security.trustcircle.tlv.command.auth.RET_AUTH_SYNC_ACK_RECV;
import com.android.server.security.trustcircle.tlv.command.auth.RET_AUTH_SYNC_RECV;
import com.android.server.security.trustcircle.tlv.command.query.CMD_GET_PK;
import com.android.server.security.trustcircle.tlv.command.query.RET_GET_PK;
import com.android.server.security.trustcircle.tlv.core.TLVEngine;
import com.android.server.security.trustcircle.tlv.core.TLVEngine.TLVResult;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import com.android.server.security.trustcircle.tlv.tree.AuthData;
import com.android.server.security.trustcircle.tlv.tree.AuthInfo;
import com.android.server.security.trustcircle.tlv.tree.AuthPkInfo;
import com.android.server.security.trustcircle.tlv.tree.AuthSyncData;
import com.android.server.security.trustcircle.tlv.tree.Cert;

public class AuthUtils {
    private static final int FATS_AUTH = 1;
    private static final String TAG = "AuthUtils";

    public static OnAuthSyncInfo processAuthSync(InitAuthInfo info) {
        AuthInfo authTLV = new AuthInfo();
        authTLV.authType.setTLVStruct(Integer.valueOf(info.mAuthType));
        authTLV.authID.setTLVStruct(Long.valueOf(info.mAuthID));
        authTLV.policy.setTLVStruct(Integer.valueOf(info.mPolicy));
        authTLV.userID.setTLVStruct(Long.valueOf(info.mUserID));
        authTLV.encryptedAESKey.setTLVStruct(ByteUtil.boxbyteArray(info.mAESTmpKey));
        CMD_AUTH_SYNC cmd = new CMD_AUTH_SYNC();
        cmd.authVersion.setTLVStruct(Integer.valueOf(info.mAuthVersion));
        cmd.authInfo.setTLVStruct(authTLV);
        TLVResult<?> tlvResult = TLVEngine.decodeCmdTLV(TcisJNI.processCmd(null, TLVEngine.encode2CmdTLV(cmd)));
        int resultCode = tlvResult.getResultCode();
        LogHelper.d(TAG, "processAuthSync result, resultCode: " + Integer.toHexString(resultCode));
        TLVTree ret = tlvResult.getResultTLV();
        if (resultCode != 0) {
            return new OnAuthSyncInfo(info.mAuthID, new byte[0], 1, (short) 1, 1, (short) 1, new byte[0], new byte[0], resultCode);
        }
        if (!(ret instanceof RET_AUTH_SYNC)) {
            return new OnAuthSyncInfo(info.mAuthID, new byte[0], 1, (short) 1, 1, (short) 1, new byte[0], new byte[0], 2046820353);
        }
        RET_AUTH_SYNC retInstance = (RET_AUTH_SYNC) ret;
        long authID = ((Long) retInstance.authID.getTLVStruct()).longValue();
        short taVersion = ((Short) retInstance.TAVersion.getTLVStruct()).shortValue();
        AuthData authData = (AuthData) retInstance.authData.getTLVStruct();
        Cert cert = (Cert) authData.cert.getTLVStruct();
        short authKeyAlgoEncode = ((Short) cert.authKeyAlgoEncode.getTLVStruct()).shortValue();
        byte[] authPKInfoSign = ByteUtil.unboxByteArray((Byte[]) cert.authPKInfoSign.getTLVStruct());
        AuthSyncData authSyncData = (AuthSyncData) authData.authSyncData.getTLVStruct();
        return new OnAuthSyncInfo(authID, ByteUtil.unboxByteArray((Byte[]) authSyncData.tcisID.getTLVStruct()), ((Integer) authSyncData.indexVersion.getTLVStruct()).intValue(), taVersion, ByteUtil.byteArrayToLongDirect((Byte[]) authSyncData.nonce.getTLVStruct()), authKeyAlgoEncode, ByteUtil.unboxByteArray(cert.authPkInfo.encapsulate()), authPKInfoSign, resultCode);
    }

    public static OnAuthSyncAckInfo processAuthSyncRec(RecAuthInfo info) {
        Cert certTLV = new Cert();
        AuthPkInfo authPkInfo;
        if (info.mPolicy != 1) {
            authPkInfo = new AuthPkInfo();
            authPkInfo.indexVersion.setTLVStruct(Short.valueOf((short) 0));
            authPkInfo.userID.setTLVStruct(Long.valueOf(0));
            authPkInfo.tcisID.setTLVStruct(ByteUtil.boxbyteArray(new byte[10]));
            authPkInfo.authPK.setTLVStruct(ByteUtil.boxbyteArray(new byte[64]));
            certTLV.authKeyAlgoEncode.setTLVStruct(Short.valueOf((short) 0));
            certTLV.authPkInfo.setTLVStruct(authPkInfo);
            certTLV.authPKInfoSign.setTLVStruct(ByteUtil.boxbyteArray(new byte[64]));
        } else {
            authPkInfo = (AuthPkInfo) TLVEngine.decodeTLV(info.mAuthKeyInfo);
            certTLV.authKeyAlgoEncode.setTLVStruct(Short.valueOf(info.mAuthKeyAlgoType));
            certTLV.authPkInfo.setTLVStruct(authPkInfo);
            certTLV.authPKInfoSign.setTLVStruct(ByteUtil.boxbyteArray(info.mAuthKeyInfoSign));
        }
        AuthSyncData authSyncTLV = new AuthSyncData();
        authSyncTLV.tcisID.setTLVStruct(ByteUtil.boxbyteArray(info.mTcisId));
        authSyncTLV.indexVersion.setTLVStruct(Integer.valueOf(info.mPkVersion));
        authSyncTLV.nonce.setTLVStruct(ByteUtil.longToByteArray(info.mNonce));
        AuthData authDataTLV = new AuthData();
        authDataTLV.cert.setTLVStruct(certTLV);
        authDataTLV.authSyncData.setTLVStruct(authSyncTLV);
        AuthInfo authInfo = new AuthInfo();
        authInfo.authType.setTLVStruct(Integer.valueOf(info.mAuthType));
        authInfo.authID.setTLVStruct(Long.valueOf(info.mAuthID));
        authInfo.policy.setTLVStruct(Integer.valueOf(info.mPolicy));
        authInfo.userID.setTLVStruct(Long.valueOf(info.mUserID));
        authInfo.encryptedAESKey.setTLVStruct(ByteUtil.boxbyteArray(info.mAESTmpKey));
        CMD_AUTH_SYNC_RECV cmd = new CMD_AUTH_SYNC_RECV();
        cmd.authVersion.setTLVStruct(Integer.valueOf(info.mAuthVersion));
        cmd.authInfo.setTLVStruct(authInfo);
        cmd.authData.setTLVStruct(authDataTLV);
        cmd.TAVersion.setTLVStruct(Short.valueOf(info.mTAVersion));
        byte[] result = TcisJNI.processCmd(null, TLVEngine.encode2CmdTLV(cmd));
        TLVResult<?> tlvResult = TLVEngine.decodeCmdTLV(result);
        int resultCode = tlvResult.getResultCode();
        if (isNeedRequestPk(result)) {
            resultCode = 1;
        }
        LogHelper.d(TAG, "processAuthSyncRec result, resultCode: " + Integer.toHexString(resultCode));
        TLVTree ret = tlvResult.getResultTLV();
        if (resultCode != 0) {
            return new OnAuthSyncAckInfo(info.mAuthID, new byte[0], 1, 1, new byte[0], (short) 1, new byte[0], new byte[0], resultCode);
        }
        if (!(ret instanceof RET_AUTH_SYNC_RECV)) {
            return new OnAuthSyncAckInfo(info.mAuthID, new byte[0], 1, 1, new byte[0], (short) 1, new byte[0], new byte[0], 2046820353);
        }
        RET_AUTH_SYNC_RECV retInstance = (RET_AUTH_SYNC_RECV) ret;
        long authID = ((Long) retInstance.authID.getTLVStruct()).longValue();
        byte[] mac = ByteUtil.unboxByteArray((Byte[]) retInstance.mac.getTLVStruct());
        AuthData authData = (AuthData) retInstance.authData.getTLVStruct();
        Cert certRet = (Cert) authData.cert.getTLVStruct();
        short authKeyAlgoEncode = ((Short) certRet.authKeyAlgoEncode.getTLVStruct()).shortValue();
        byte[] authPKInfoSign = ByteUtil.unboxByteArray((Byte[]) certRet.authPKInfoSign.getTLVStruct());
        AuthSyncData authSyncData = (AuthSyncData) authData.authSyncData.getTLVStruct();
        return new OnAuthSyncAckInfo(authID, ByteUtil.unboxByteArray((Byte[]) authSyncData.tcisID.getTLVStruct()), ((Integer) authSyncData.indexVersion.getTLVStruct()).intValue(), ByteUtil.byteArrayToLongDirect((Byte[]) authSyncData.nonce.getTLVStruct()), mac, authKeyAlgoEncode, ByteUtil.unboxByteArray(certRet.authPkInfo.encapsulate()), authPKInfoSign, resultCode);
    }

    public static OnAuthAckInfo processRecAuthSyncAck(RecAuthAckInfo info) {
        AuthPkInfo authPkInfo = (AuthPkInfo) TLVEngine.decodeTLV(info.mAuthKeyInfoSlave);
        Cert cert = new Cert();
        cert.authPkInfo.setTLVStruct(authPkInfo);
        cert.authKeyAlgoEncode.setTLVStruct(Short.valueOf(info.mAuthKeyAlgoTypeSlave));
        cert.authPKInfoSign.setTLVStruct(ByteUtil.boxbyteArray(info.mAuthKeyInfoSignSlave));
        AuthSyncData authSyncData = new AuthSyncData();
        authSyncData.tcisID.setTLVStruct(ByteUtil.boxbyteArray(info.mTcisIDSlave));
        authSyncData.nonce.setTLVStruct(ByteUtil.longToByteArray(info.mNonceSlave));
        authSyncData.indexVersion.setTLVStruct(Integer.valueOf(info.mPkVersionSlave));
        AuthData authData = new AuthData();
        authData.cert.setTLVStruct(cert);
        authData.authSyncData.setTLVStruct(authSyncData);
        CMD_AUTH_SYNC_ACK_RECV cmd = new CMD_AUTH_SYNC_ACK_RECV();
        cmd.authID.setTLVStruct(Long.valueOf(info.mAuthID));
        cmd.mac.setTLVStruct(ByteUtil.boxbyteArray(info.mMacSlave));
        cmd.authData.setTLVStruct(authData);
        byte[] result = TcisJNI.processCmd(null, TLVEngine.encode2CmdTLV(cmd));
        TLVResult<?> tlvResult = TLVEngine.decodeCmdTLV(result);
        int resultCode = tlvResult.getResultCode();
        LogHelper.d(TAG, "processRecAuthSyncAck result, resultCode: " + Integer.toHexString(resultCode));
        if (isNeedRequestPk(result)) {
            resultCode = 1;
        }
        TLVTree ret = tlvResult.getResultTLV();
        if (resultCode != 0) {
            return new OnAuthAckInfo(info.mAuthID, new byte[0], new byte[0], new byte[0], resultCode);
        }
        if (!(ret instanceof RET_AUTH_SYNC_ACK_RECV)) {
            return new OnAuthAckInfo(info.mAuthID, new byte[0], new byte[0], new byte[0], 2046820353);
        }
        RET_AUTH_SYNC_ACK_RECV retInstance = (RET_AUTH_SYNC_ACK_RECV) ret;
        return new OnAuthAckInfo(((Long) retInstance.authID.getTLVStruct()).longValue(), ByteUtil.unboxByteArray((Byte[]) retInstance.iv.getTLVStruct()), ByteUtil.unboxByteArray((Byte[]) retInstance.sessionKey.getTLVStruct()), ByteUtil.unboxByteArray((Byte[]) retInstance.mac.getTLVStruct()), resultCode);
    }

    public static OnAuthAckInfo processRecPkMaster(RespPkInfo info) {
        AuthPkInfo authPkInfo = (AuthPkInfo) TLVEngine.decodeTLV(info.mAuthKeyData);
        Cert cert = new Cert();
        cert.authPkInfo.setTLVStruct(authPkInfo);
        cert.authKeyAlgoEncode.setTLVStruct(Short.valueOf(info.mAuthKeyAlgoType));
        cert.authPKInfoSign.setTLVStruct(ByteUtil.boxbyteArray(info.mAuthKeyDataSign));
        CMD_AUTH_MASTER_RECV_KEY cmd = new CMD_AUTH_MASTER_RECV_KEY();
        cmd.authID.setTLVStruct(Long.valueOf(info.mAuthID));
        cmd.cert.setTLVStruct(cert);
        TLVResult<?> tlvResult = TLVEngine.decodeCmdTLV(TcisJNI.processCmd(null, TLVEngine.encode2CmdTLV(cmd)));
        int resultCode = tlvResult.getResultCode();
        LogHelper.d(TAG, "processRecPkMaster result, resultCode: " + Integer.toHexString(resultCode));
        TLVTree ret = tlvResult.getResultTLV();
        if (resultCode != 0) {
            return new OnAuthAckInfo(info.mAuthID, new byte[0], new byte[0], new byte[0], resultCode);
        }
        if (!(ret instanceof RET_AUTH_SYNC_ACK_RECV)) {
            return new OnAuthAckInfo(info.mAuthID, new byte[0], new byte[0], new byte[0], 2046820353);
        }
        RET_AUTH_SYNC_ACK_RECV retInstance = (RET_AUTH_SYNC_ACK_RECV) ret;
        byte[] sessionKey = ByteUtil.unboxByteArray((Byte[]) retInstance.sessionKey.getTLVStruct());
        return new OnAuthAckInfo(info.mAuthID, ByteUtil.unboxByteArray((Byte[]) retInstance.iv.getTLVStruct()), sessionKey, ByteUtil.unboxByteArray((Byte[]) retInstance.mac.getTLVStruct()), resultCode);
    }

    public static OnAuthSyncAckInfo processRecPkSlave(RespPkInfo info) {
        AuthPkInfo authPkInfo = (AuthPkInfo) TLVEngine.decodeTLV(info.mAuthKeyData);
        Cert cert = new Cert();
        cert.authPkInfo.setTLVStruct(authPkInfo);
        cert.authKeyAlgoEncode.setTLVStruct(Short.valueOf(info.mAuthKeyAlgoType));
        cert.authPKInfoSign.setTLVStruct(ByteUtil.boxbyteArray(info.mAuthKeyDataSign));
        CMD_AUTH_SLAVE_RECV_KEY cmd = new CMD_AUTH_SLAVE_RECV_KEY();
        cmd.authID.setTLVStruct(Long.valueOf(info.mAuthID));
        cmd.cert.setTLVStruct(cert);
        TLVResult<?> tlvResult = TLVEngine.decodeCmdTLV(TcisJNI.processCmd(null, TLVEngine.encode2CmdTLV(cmd)));
        int resultCode = tlvResult.getResultCode();
        LogHelper.d(TAG, "processRecPkSlave result, resultCode: " + Integer.toHexString(resultCode));
        TLVTree ret = tlvResult.getResultTLV();
        if (resultCode != 0) {
            return new OnAuthSyncAckInfo(info.mAuthID, new byte[0], 1, 1, new byte[0], (short) 1, new byte[0], new byte[0], resultCode);
        }
        if (!(ret instanceof RET_AUTH_SYNC_RECV)) {
            return new OnAuthSyncAckInfo(info.mAuthID, new byte[0], 1, 1, new byte[0], (short) 1, new byte[0], new byte[0], 2046820353);
        }
        RET_AUTH_SYNC_RECV retInstance = (RET_AUTH_SYNC_RECV) ret;
        long authID = ((Long) retInstance.authID.getTLVStruct()).longValue();
        byte[] mac = ByteUtil.unboxByteArray((Byte[]) retInstance.mac.getTLVStruct());
        AuthData authData = (AuthData) retInstance.authData.getTLVStruct();
        Cert certRet = (Cert) authData.cert.getTLVStruct();
        short authKeyAlgoEncode = ((Short) certRet.authKeyAlgoEncode.getTLVStruct()).shortValue();
        byte[] authPKInfoSign = ByteUtil.unboxByteArray((Byte[]) certRet.authPKInfoSign.getTLVStruct());
        AuthSyncData authSyncData = (AuthSyncData) authData.authSyncData.getTLVStruct();
        return new OnAuthSyncAckInfo(authID, ByteUtil.unboxByteArray((Byte[]) authSyncData.tcisID.getTLVStruct()), ((Integer) authSyncData.indexVersion.getTLVStruct()).intValue(), ByteUtil.byteArrayToLongDirect((Byte[]) authSyncData.nonce.getTLVStruct()), mac, authKeyAlgoEncode, ByteUtil.unboxByteArray(certRet.authPkInfo.encapsulate()), authPKInfoSign, resultCode);
    }

    public static OnAuthAckInfo processAckRec(RecAckInfo info) {
        CMD_AUTH_ACK_RECV cmd = new CMD_AUTH_ACK_RECV();
        cmd.authID.setTLVStruct(Long.valueOf(info.mAuthID));
        cmd.mac.setTLVStruct(ByteUtil.boxbyteArray(info.mMAC));
        TLVResult<?> tlvResult = TLVEngine.decodeCmdTLV(TcisJNI.processCmd(null, TLVEngine.encode2CmdTLV(cmd)));
        int resultCode = tlvResult.getResultCode();
        LogHelper.d(TAG, "processAckRec result, resultCode: " + Integer.toHexString(resultCode));
        TLVTree ret = tlvResult.getResultTLV();
        if (resultCode != 0) {
            return new OnAuthAckInfo(info.mAuthID, new byte[0], new byte[0], new byte[0], resultCode);
        }
        if (!(ret instanceof RET_AUTH_ACK_RECV)) {
            return new OnAuthAckInfo(info.mAuthID, new byte[0], new byte[0], new byte[0], 2046820353);
        }
        RET_AUTH_ACK_RECV retInstance = (RET_AUTH_ACK_RECV) ret;
        byte[] sessionKey = ByteUtil.unboxByteArray((Byte[]) retInstance.sessionKey.getTLVStruct());
        return new OnAuthAckInfo(info.mAuthID, ByteUtil.unboxByteArray((Byte[]) retInstance.iv.getTLVStruct()), sessionKey, new byte[0], resultCode);
    }

    public static RespPkInfo processGetPk(ReqPkInfo info) {
        CMD_GET_PK cmd = new CMD_GET_PK();
        cmd.authID.setTLVStruct(Long.valueOf(info.mAuthID));
        cmd.userID.setTLVStruct(Long.valueOf(info.mUserID));
        TLVResult<?> tlvResult = TLVEngine.decodeCmdTLV(TcisJNI.processCmd(null, TLVEngine.encode2CmdTLV(cmd)));
        int resultCode = tlvResult.getResultCode();
        LogHelper.d(TAG, "processGetPk result, resultCode: " + Integer.toHexString(resultCode));
        TLVTree ret = tlvResult.getResultTLV();
        if (resultCode != 0) {
            return new RespPkInfo(info.mAuthID, (short) 1, new byte[0], new byte[0], resultCode);
        }
        if (!(ret instanceof RET_GET_PK)) {
            return new RespPkInfo(info.mAuthID, (short) 1, new byte[0], new byte[0], 2046820353);
        }
        Cert cert = (Cert) ((RET_GET_PK) ret).cert.getTLVStruct();
        byte[] authPkInfoByte = ByteUtil.unboxByteArray(cert.authPkInfo.encapsulate());
        return new RespPkInfo(info.mAuthID, ((Short) cert.authKeyAlgoEncode.getTLVStruct()).shortValue(), authPkInfoByte, ByteUtil.unboxByteArray((Byte[]) cert.authPKInfoSign.getTLVStruct()), resultCode);
    }

    public static int processCancelAuth(long authID) {
        CMD_AUTH_CANCEL cmd = new CMD_AUTH_CANCEL();
        cmd.authID.setTLVStruct(Long.valueOf(authID));
        return TLVEngine.decodeCmdTLV(TcisJNI.processCmd(null, TLVEngine.encode2CmdTLV(cmd))).getResultCode();
    }

    private static boolean isNeedRequestPk(byte[] result) {
        return result != null && result.length >= 8 && result[4] == (byte) 0 && result[5] == (byte) 0 && result[6] == (byte) 0 && result[7] == (byte) 1;
    }
}
