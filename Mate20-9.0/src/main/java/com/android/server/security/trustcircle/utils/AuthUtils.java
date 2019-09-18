package com.android.server.security.trustcircle.utils;

import android.content.Context;
import com.android.server.security.trustcircle.auth.AuthPara;
import com.android.server.security.trustcircle.auth.IOTController;
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
import com.android.server.security.trustcircle.tlv.command.ka.CMD_KA;
import com.android.server.security.trustcircle.tlv.command.ka.RET_KA;
import com.android.server.security.trustcircle.tlv.command.query.CMD_GET_PK;
import com.android.server.security.trustcircle.tlv.command.query.RET_GET_PK;
import com.android.server.security.trustcircle.tlv.core.TLVEngine;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import com.android.server.security.trustcircle.tlv.tree.AuthData;
import com.android.server.security.trustcircle.tlv.tree.AuthInfo;
import com.android.server.security.trustcircle.tlv.tree.AuthPkInfo;
import com.android.server.security.trustcircle.tlv.tree.AuthSyncData;
import com.android.server.security.trustcircle.tlv.tree.Cert;
import com.android.server.security.trustcircle.tlv.tree.KaInfo;
import com.android.server.security.trustcircle.utils.Status;

public class AuthUtils {
    private static final int FATS_AUTH = 1;
    private static final String TAG = "AuthUtils";

    public static AuthPara.OnAuthSyncInfo processAuthSync(AuthPara.InitAuthInfo info) {
        AuthPara.InitAuthInfo initAuthInfo = info;
        AuthInfo authTLV = new AuthInfo();
        authTLV.authType.setTLVStruct(Integer.valueOf(initAuthInfo.mAuthType));
        authTLV.authID.setTLVStruct(Long.valueOf(initAuthInfo.mAuthID));
        authTLV.policy.setTLVStruct(Integer.valueOf(initAuthInfo.mPolicy));
        authTLV.userID.setTLVStruct(Long.valueOf(initAuthInfo.mUserID));
        authTLV.encryptedAESKey.setTLVStruct(ByteUtil.boxbyteArray(initAuthInfo.mAESTmpKey));
        CMD_AUTH_SYNC cmd = new CMD_AUTH_SYNC();
        cmd.authVersion.setTLVStruct(Integer.valueOf(initAuthInfo.mAuthVersion));
        cmd.authInfo.setTLVStruct(authTLV);
        TLVEngine.TLVResult decodeCmdTLV = TLVEngine.decodeCmdTLV(TcisJNI.processCmd(null, TLVEngine.encode2CmdTLV(cmd)));
        int resultCode = decodeCmdTLV.getResultCode();
        LogHelper.d(TAG, "processAuthSync result, resultCode: " + Integer.toHexString(resultCode));
        TLVTree resultTLV = decodeCmdTLV.getResultTLV();
        if (resultCode != 0) {
            TLVTree ret = resultTLV;
            AuthPara.OnAuthSyncInfo onAuthSyncInfo = new AuthPara.OnAuthSyncInfo(initAuthInfo.mAuthID, new byte[0], 1, 1, 1, 1, new byte[0], new byte[0], resultCode);
            return onAuthSyncInfo;
        } else if (resultTLV instanceof RET_AUTH_SYNC) {
            RET_AUTH_SYNC retInstance = (RET_AUTH_SYNC) resultTLV;
            long authID = ((Long) retInstance.authID.getTLVStruct()).longValue();
            short taVersion = ((Short) retInstance.TAVersion.getTLVStruct()).shortValue();
            AuthData authData = (AuthData) retInstance.authData.getTLVStruct();
            Cert cert = (Cert) authData.cert.getTLVStruct();
            short authKeyAlgoEncode = ((Short) cert.authKeyAlgoEncode.getTLVStruct()).shortValue();
            byte[] authPKInfoSign = ByteUtil.unboxByteArray((Byte[]) cert.authPKInfoSign.getTLVStruct());
            byte[] authPkInfoByte = ByteUtil.unboxByteArray(cert.authPkInfo.encapsulate());
            AuthSyncData authSyncData = (AuthSyncData) authData.authSyncData.getTLVStruct();
            AuthSyncData authSyncData2 = authSyncData;
            Cert cert2 = cert;
            RET_AUTH_SYNC ret_auth_sync = retInstance;
            AuthData authData2 = authData;
            TLVTree.TLVRootTree tLVRootTree = resultTLV;
            AuthPara.OnAuthSyncInfo onAuthSyncInfo2 = new AuthPara.OnAuthSyncInfo(authID, ByteUtil.unboxByteArray((Byte[]) authSyncData.tcisID.getTLVStruct()), ((Integer) authSyncData.indexVersion.getTLVStruct()).intValue(), taVersion, ByteUtil.byteArrayToLongDirect((Byte[]) authSyncData.nonce.getTLVStruct()), authKeyAlgoEncode, authPkInfoByte, authPKInfoSign, resultCode);
            return onAuthSyncInfo2;
        } else {
            TLVTree ret2 = resultTLV;
            int i = resultCode;
            AuthPara.OnAuthSyncInfo onAuthSyncInfo3 = new AuthPara.OnAuthSyncInfo(initAuthInfo.mAuthID, new byte[0], 1, 1, 1, 1, new byte[0], new byte[0], 2046820353);
            return onAuthSyncInfo3;
        }
    }

    public static AuthPara.OnAuthSyncAckInfo processAuthSyncRec(AuthPara.RecAuthInfo info) {
        AuthPara.RecAuthInfo recAuthInfo = info;
        Cert certTLV = new Cert();
        if (recAuthInfo.mPolicy != 1) {
            AuthPkInfo authPkInfo = new AuthPkInfo();
            authPkInfo.indexVersion.setTLVStruct((short) 0);
            authPkInfo.userID.setTLVStruct(0L);
            authPkInfo.tcisID.setTLVStruct(ByteUtil.boxbyteArray(new byte[10]));
            authPkInfo.authPK.setTLVStruct(ByteUtil.boxbyteArray(new byte[64]));
            certTLV.authKeyAlgoEncode.setTLVStruct((short) 0);
            certTLV.authPkInfo.setTLVStruct(authPkInfo);
            certTLV.authPKInfoSign.setTLVStruct(ByteUtil.boxbyteArray(new byte[64]));
        } else {
            certTLV.authKeyAlgoEncode.setTLVStruct(Short.valueOf(recAuthInfo.mAuthKeyAlgoType));
            certTLV.authPkInfo.setTLVStruct((AuthPkInfo) TLVEngine.decodeTLV(recAuthInfo.mAuthKeyInfo));
            certTLV.authPKInfoSign.setTLVStruct(ByteUtil.boxbyteArray(recAuthInfo.mAuthKeyInfoSign));
        }
        AuthSyncData authSyncTLV = new AuthSyncData();
        authSyncTLV.tcisID.setTLVStruct(ByteUtil.boxbyteArray(recAuthInfo.mTcisId));
        authSyncTLV.indexVersion.setTLVStruct(Integer.valueOf(recAuthInfo.mPkVersion));
        authSyncTLV.nonce.setTLVStruct(ByteUtil.longToByteArray(recAuthInfo.mNonce));
        AuthData authDataTLV = new AuthData();
        authDataTLV.cert.setTLVStruct(certTLV);
        authDataTLV.authSyncData.setTLVStruct(authSyncTLV);
        AuthInfo authInfo = new AuthInfo();
        authInfo.authType.setTLVStruct(Integer.valueOf(recAuthInfo.mAuthType));
        authInfo.authID.setTLVStruct(Long.valueOf(recAuthInfo.mAuthID));
        authInfo.policy.setTLVStruct(Integer.valueOf(recAuthInfo.mPolicy));
        authInfo.userID.setTLVStruct(Long.valueOf(recAuthInfo.mUserID));
        authInfo.encryptedAESKey.setTLVStruct(ByteUtil.boxbyteArray(recAuthInfo.mAESTmpKey));
        CMD_AUTH_SYNC_RECV cmd = new CMD_AUTH_SYNC_RECV();
        cmd.authVersion.setTLVStruct(Integer.valueOf(recAuthInfo.mAuthVersion));
        cmd.authInfo.setTLVStruct(authInfo);
        cmd.authData.setTLVStruct(authDataTLV);
        cmd.TAVersion.setTLVStruct(Short.valueOf(recAuthInfo.mTAVersion));
        byte[] result = TcisJNI.processCmd(null, TLVEngine.encode2CmdTLV(cmd));
        TLVEngine.TLVResult decodeCmdTLV = TLVEngine.decodeCmdTLV(result);
        int resultCode = decodeCmdTLV.getResultCode();
        if (isNeedRequestPk(result)) {
            resultCode = 1;
        }
        LogHelper.d(TAG, "processAuthSyncRec result, resultCode: " + Integer.toHexString(resultCode));
        TLVTree resultTLV = decodeCmdTLV.getResultTLV();
        if (resultCode != 0) {
            TLVTree ret = resultTLV;
            AuthPara.OnAuthSyncAckInfo onAuthSyncAckInfo = new AuthPara.OnAuthSyncAckInfo(recAuthInfo.mAuthID, new byte[0], 1, 1, new byte[0], 1, new byte[0], new byte[0], resultCode);
            return onAuthSyncAckInfo;
        } else if (resultTLV instanceof RET_AUTH_SYNC_RECV) {
            RET_AUTH_SYNC_RECV retInstance = (RET_AUTH_SYNC_RECV) resultTLV;
            long authID = ((Long) retInstance.authID.getTLVStruct()).longValue();
            byte[] mac = ByteUtil.unboxByteArray((Byte[]) retInstance.mac.getTLVStruct());
            AuthData authData = (AuthData) retInstance.authData.getTLVStruct();
            Cert certRet = (Cert) authData.cert.getTLVStruct();
            short authKeyAlgoEncode = ((Short) certRet.authKeyAlgoEncode.getTLVStruct()).shortValue();
            byte[] authPKInfoSign = ByteUtil.unboxByteArray((Byte[]) certRet.authPKInfoSign.getTLVStruct());
            byte[] authPkInfoByte = ByteUtil.unboxByteArray(certRet.authPkInfo.encapsulate());
            AuthSyncData authSyncData = (AuthSyncData) authData.authSyncData.getTLVStruct();
            Cert cert = certRet;
            AuthSyncData authSyncData2 = authSyncData;
            AuthData authData2 = authData;
            TLVTree.TLVRootTree tLVRootTree = resultTLV;
            AuthPara.OnAuthSyncAckInfo onAuthSyncAckInfo2 = new AuthPara.OnAuthSyncAckInfo(authID, ByteUtil.unboxByteArray((Byte[]) authSyncData.tcisID.getTLVStruct()), ((Integer) authSyncData.indexVersion.getTLVStruct()).intValue(), ByteUtil.byteArrayToLongDirect((Byte[]) authSyncData.nonce.getTLVStruct()), mac, authKeyAlgoEncode, authPkInfoByte, authPKInfoSign, resultCode);
            return onAuthSyncAckInfo2;
        } else {
            TLVTree ret2 = resultTLV;
            AuthPara.OnAuthSyncAckInfo onAuthSyncAckInfo3 = new AuthPara.OnAuthSyncAckInfo(recAuthInfo.mAuthID, new byte[0], 1, 1, new byte[0], 1, new byte[0], new byte[0], 2046820353);
            return onAuthSyncAckInfo3;
        }
    }

    public static AuthPara.OnAuthAckInfo processRecAuthSyncAck(AuthPara.RecAuthAckInfo info) {
        AuthPara.RecAuthAckInfo recAuthAckInfo = info;
        Cert cert = new Cert();
        cert.authPkInfo.setTLVStruct((AuthPkInfo) TLVEngine.decodeTLV(recAuthAckInfo.mAuthKeyInfoSlave));
        cert.authKeyAlgoEncode.setTLVStruct(Short.valueOf(recAuthAckInfo.mAuthKeyAlgoTypeSlave));
        cert.authPKInfoSign.setTLVStruct(ByteUtil.boxbyteArray(recAuthAckInfo.mAuthKeyInfoSignSlave));
        AuthSyncData authSyncData = new AuthSyncData();
        authSyncData.tcisID.setTLVStruct(ByteUtil.boxbyteArray(recAuthAckInfo.mTcisIDSlave));
        authSyncData.nonce.setTLVStruct(ByteUtil.longToByteArray(recAuthAckInfo.mNonceSlave));
        authSyncData.indexVersion.setTLVStruct(Integer.valueOf(recAuthAckInfo.mPkVersionSlave));
        AuthData authData = new AuthData();
        authData.cert.setTLVStruct(cert);
        authData.authSyncData.setTLVStruct(authSyncData);
        CMD_AUTH_SYNC_ACK_RECV cmd = new CMD_AUTH_SYNC_ACK_RECV();
        cmd.authID.setTLVStruct(Long.valueOf(recAuthAckInfo.mAuthID));
        cmd.mac.setTLVStruct(ByteUtil.boxbyteArray(recAuthAckInfo.mMacSlave));
        cmd.authData.setTLVStruct(authData);
        byte[] result = TcisJNI.processCmd(null, TLVEngine.encode2CmdTLV(cmd));
        TLVEngine.TLVResult decodeCmdTLV = TLVEngine.decodeCmdTLV(result);
        int resultCode = decodeCmdTLV.getResultCode();
        LogHelper.d(TAG, "processRecAuthSyncAck result, resultCode: " + Integer.toHexString(resultCode));
        if (isNeedRequestPk(result)) {
            resultCode = 1;
        }
        TLVTree resultTLV = decodeCmdTLV.getResultTLV();
        if (resultCode != 0) {
            TLVTree ret = resultTLV;
            AuthPara.OnAuthAckInfo onAuthAckInfo = new AuthPara.OnAuthAckInfo(recAuthAckInfo.mAuthID, new byte[0], new byte[0], new byte[0], resultCode);
            return onAuthAckInfo;
        } else if (resultTLV instanceof RET_AUTH_SYNC_ACK_RECV) {
            RET_AUTH_SYNC_ACK_RECV retInstance = (RET_AUTH_SYNC_ACK_RECV) resultTLV;
            long authID = ((Long) retInstance.authID.getTLVStruct()).longValue();
            byte[] mac = ByteUtil.unboxByteArray((Byte[]) retInstance.mac.getTLVStruct());
            RET_AUTH_SYNC_ACK_RECV ret_auth_sync_ack_recv = retInstance;
            TLVTree.TLVRootTree tLVRootTree = resultTLV;
            AuthPara.OnAuthAckInfo onAuthAckInfo2 = new AuthPara.OnAuthAckInfo(authID, ByteUtil.unboxByteArray((Byte[]) retInstance.iv.getTLVStruct()), ByteUtil.unboxByteArray((Byte[]) retInstance.sessionKey.getTLVStruct()), mac, resultCode);
            return onAuthAckInfo2;
        } else {
            TLVTree ret2 = resultTLV;
            AuthPara.OnAuthAckInfo onAuthAckInfo3 = new AuthPara.OnAuthAckInfo(recAuthAckInfo.mAuthID, new byte[0], new byte[0], new byte[0], 2046820353);
            return onAuthAckInfo3;
        }
    }

    public static AuthPara.OnAuthAckInfo processRecPkMaster(AuthPara.RespPkInfo info) {
        AuthPara.RespPkInfo respPkInfo = info;
        Cert cert = new Cert();
        cert.authPkInfo.setTLVStruct((AuthPkInfo) TLVEngine.decodeTLV(respPkInfo.mAuthKeyData));
        cert.authKeyAlgoEncode.setTLVStruct(Short.valueOf(respPkInfo.mAuthKeyAlgoType));
        cert.authPKInfoSign.setTLVStruct(ByteUtil.boxbyteArray(respPkInfo.mAuthKeyDataSign));
        CMD_AUTH_MASTER_RECV_KEY cmd = new CMD_AUTH_MASTER_RECV_KEY();
        cmd.authID.setTLVStruct(Long.valueOf(respPkInfo.mAuthID));
        cmd.cert.setTLVStruct(cert);
        TLVEngine.TLVResult decodeCmdTLV = TLVEngine.decodeCmdTLV(TcisJNI.processCmd(null, TLVEngine.encode2CmdTLV(cmd)));
        int resultCode = decodeCmdTLV.getResultCode();
        LogHelper.d(TAG, "processRecPkMaster result, resultCode: " + Integer.toHexString(resultCode));
        TLVTree.TLVRootTree resultTLV = decodeCmdTLV.getResultTLV();
        if (resultCode != 0) {
            AuthPara.OnAuthAckInfo onAuthAckInfo = new AuthPara.OnAuthAckInfo(respPkInfo.mAuthID, new byte[0], new byte[0], new byte[0], resultCode);
            return onAuthAckInfo;
        } else if (resultTLV instanceof RET_AUTH_SYNC_ACK_RECV) {
            RET_AUTH_SYNC_ACK_RECV retInstance = (RET_AUTH_SYNC_ACK_RECV) resultTLV;
            byte[] sessionKey = ByteUtil.unboxByteArray((Byte[]) retInstance.sessionKey.getTLVStruct());
            RET_AUTH_SYNC_ACK_RECV ret_auth_sync_ack_recv = retInstance;
            AuthPara.OnAuthAckInfo onAuthAckInfo2 = new AuthPara.OnAuthAckInfo(respPkInfo.mAuthID, ByteUtil.unboxByteArray((Byte[]) retInstance.iv.getTLVStruct()), sessionKey, ByteUtil.unboxByteArray((Byte[]) retInstance.mac.getTLVStruct()), resultCode);
            return onAuthAckInfo2;
        } else {
            AuthPara.OnAuthAckInfo onAuthAckInfo3 = new AuthPara.OnAuthAckInfo(respPkInfo.mAuthID, new byte[0], new byte[0], new byte[0], 2046820353);
            return onAuthAckInfo3;
        }
    }

    public static AuthPara.OnAuthSyncAckInfo processRecPkSlave(AuthPara.RespPkInfo info) {
        AuthPara.RespPkInfo respPkInfo = info;
        Cert cert = new Cert();
        cert.authPkInfo.setTLVStruct((AuthPkInfo) TLVEngine.decodeTLV(respPkInfo.mAuthKeyData));
        cert.authKeyAlgoEncode.setTLVStruct(Short.valueOf(respPkInfo.mAuthKeyAlgoType));
        cert.authPKInfoSign.setTLVStruct(ByteUtil.boxbyteArray(respPkInfo.mAuthKeyDataSign));
        CMD_AUTH_SLAVE_RECV_KEY cmd = new CMD_AUTH_SLAVE_RECV_KEY();
        cmd.authID.setTLVStruct(Long.valueOf(respPkInfo.mAuthID));
        cmd.cert.setTLVStruct(cert);
        TLVEngine.TLVResult decodeCmdTLV = TLVEngine.decodeCmdTLV(TcisJNI.processCmd(null, TLVEngine.encode2CmdTLV(cmd)));
        int resultCode = decodeCmdTLV.getResultCode();
        LogHelper.d(TAG, "processRecPkSlave result, resultCode: " + Integer.toHexString(resultCode));
        TLVTree resultTLV = decodeCmdTLV.getResultTLV();
        if (resultCode != 0) {
            TLVTree ret = resultTLV;
            byte[] bArr = new byte[0];
            AuthPara.OnAuthSyncAckInfo onAuthSyncAckInfo = new AuthPara.OnAuthSyncAckInfo(respPkInfo.mAuthID, new byte[0], 1, 1, new byte[0], 1, bArr, new byte[0], resultCode);
            return onAuthSyncAckInfo;
        } else if (resultTLV instanceof RET_AUTH_SYNC_RECV) {
            RET_AUTH_SYNC_RECV retInstance = (RET_AUTH_SYNC_RECV) resultTLV;
            long authID = ((Long) retInstance.authID.getTLVStruct()).longValue();
            byte[] mac = ByteUtil.unboxByteArray((Byte[]) retInstance.mac.getTLVStruct());
            AuthData authData = (AuthData) retInstance.authData.getTLVStruct();
            Cert certRet = (Cert) authData.cert.getTLVStruct();
            short authKeyAlgoEncode = ((Short) certRet.authKeyAlgoEncode.getTLVStruct()).shortValue();
            byte[] authPKInfoSign = ByteUtil.unboxByteArray((Byte[]) certRet.authPKInfoSign.getTLVStruct());
            byte[] authPkInfoByte = ByteUtil.unboxByteArray(certRet.authPkInfo.encapsulate());
            AuthSyncData authSyncData = (AuthSyncData) authData.authSyncData.getTLVStruct();
            AuthSyncData authSyncData2 = authSyncData;
            Cert cert2 = certRet;
            RET_AUTH_SYNC_RECV ret_auth_sync_recv = retInstance;
            AuthData authData2 = authData;
            TLVTree.TLVRootTree tLVRootTree = resultTLV;
            AuthPara.OnAuthSyncAckInfo onAuthSyncAckInfo2 = new AuthPara.OnAuthSyncAckInfo(authID, ByteUtil.unboxByteArray((Byte[]) authSyncData.tcisID.getTLVStruct()), ((Integer) authSyncData.indexVersion.getTLVStruct()).intValue(), ByteUtil.byteArrayToLongDirect((Byte[]) authSyncData.nonce.getTLVStruct()), mac, authKeyAlgoEncode, authPkInfoByte, authPKInfoSign, resultCode);
            return onAuthSyncAckInfo2;
        } else {
            TLVTree ret2 = resultTLV;
            int i = resultCode;
            AuthPara.OnAuthSyncAckInfo onAuthSyncAckInfo3 = new AuthPara.OnAuthSyncAckInfo(respPkInfo.mAuthID, new byte[0], 1, 1, new byte[0], 1, new byte[0], new byte[0], 2046820353);
            return onAuthSyncAckInfo3;
        }
    }

    public static AuthPara.OnAuthAckInfo processAckRec(AuthPara.RecAckInfo info) {
        AuthPara.RecAckInfo recAckInfo = info;
        CMD_AUTH_ACK_RECV cmd = new CMD_AUTH_ACK_RECV();
        cmd.authID.setTLVStruct(Long.valueOf(recAckInfo.mAuthID));
        cmd.mac.setTLVStruct(ByteUtil.boxbyteArray(recAckInfo.mMAC));
        TLVEngine.TLVResult decodeCmdTLV = TLVEngine.decodeCmdTLV(TcisJNI.processCmd(null, TLVEngine.encode2CmdTLV(cmd)));
        int resultCode = decodeCmdTLV.getResultCode();
        LogHelper.d(TAG, "processAckRec result, resultCode: " + Integer.toHexString(resultCode));
        TLVTree.TLVRootTree resultTLV = decodeCmdTLV.getResultTLV();
        if (resultCode != 0) {
            AuthPara.OnAuthAckInfo onAuthAckInfo = new AuthPara.OnAuthAckInfo(recAckInfo.mAuthID, new byte[0], new byte[0], new byte[0], resultCode);
            return onAuthAckInfo;
        } else if (resultTLV instanceof RET_AUTH_ACK_RECV) {
            RET_AUTH_ACK_RECV retInstance = (RET_AUTH_ACK_RECV) resultTLV;
            byte[] sessionKey = ByteUtil.unboxByteArray((Byte[]) retInstance.sessionKey.getTLVStruct());
            AuthPara.OnAuthAckInfo onAuthAckInfo2 = new AuthPara.OnAuthAckInfo(recAckInfo.mAuthID, ByteUtil.unboxByteArray((Byte[]) retInstance.iv.getTLVStruct()), sessionKey, new byte[0], resultCode);
            return onAuthAckInfo2;
        } else {
            AuthPara.OnAuthAckInfo onAuthAckInfo3 = new AuthPara.OnAuthAckInfo(recAckInfo.mAuthID, new byte[0], new byte[0], new byte[0], 2046820353);
            return onAuthAckInfo3;
        }
    }

    public static AuthPara.RespPkInfo processGetPk(AuthPara.ReqPkInfo info) {
        AuthPara.ReqPkInfo reqPkInfo = info;
        CMD_GET_PK cmd = new CMD_GET_PK();
        cmd.authID.setTLVStruct(Long.valueOf(reqPkInfo.mAuthID));
        cmd.userID.setTLVStruct(Long.valueOf(reqPkInfo.mUserID));
        TLVEngine.TLVResult decodeCmdTLV = TLVEngine.decodeCmdTLV(TcisJNI.processCmd(null, TLVEngine.encode2CmdTLV(cmd)));
        int resultCode = decodeCmdTLV.getResultCode();
        LogHelper.d(TAG, "processGetPk result, resultCode: " + Integer.toHexString(resultCode));
        TLVTree.TLVRootTree resultTLV = decodeCmdTLV.getResultTLV();
        if (resultCode != 0) {
            AuthPara.RespPkInfo respPkInfo = new AuthPara.RespPkInfo(reqPkInfo.mAuthID, 1, new byte[0], new byte[0], resultCode);
            return respPkInfo;
        } else if (resultTLV instanceof RET_GET_PK) {
            Cert cert = (Cert) ((RET_GET_PK) resultTLV).cert.getTLVStruct();
            byte[] authPkInfoByte = ByteUtil.unboxByteArray(cert.authPkInfo.encapsulate());
            AuthPara.RespPkInfo respPkInfo2 = new AuthPara.RespPkInfo(reqPkInfo.mAuthID, ((Short) cert.authKeyAlgoEncode.getTLVStruct()).shortValue(), authPkInfoByte, ByteUtil.unboxByteArray((Byte[]) cert.authPKInfoSign.getTLVStruct()), resultCode);
            return respPkInfo2;
        } else {
            AuthPara.RespPkInfo respPkInfo3 = new AuthPara.RespPkInfo(reqPkInfo.mAuthID, 1, new byte[0], new byte[0], 2046820353);
            return respPkInfo3;
        }
    }

    public static int processCancelAuth(long authID) {
        CMD_AUTH_CANCEL cmd = new CMD_AUTH_CANCEL();
        cmd.authID.setTLVStruct(Long.valueOf(authID));
        return TLVEngine.decodeCmdTLV(TcisJNI.processCmd(null, TLVEngine.encode2CmdTLV(cmd))).getResultCode();
    }

    private static boolean isNeedRequestPk(byte[] result) {
        if (result != null && result.length >= 8 && result[4] == 0 && result[5] == 0 && result[6] == 0 && result[7] == 1) {
            return true;
        }
        return false;
    }

    public static IOTController.KaInfoResponse processKaAuth(Context context, IOTController.KaInfoRequest info) {
        CMD_KA cmdKa = new CMD_KA();
        cmdKa.userId.setTLVStruct(Long.valueOf(info.userId));
        cmdKa.kaVersion.setTLVStruct(Integer.valueOf(info.kaVersion));
        cmdKa.eeAesTmpKey.setTLVStruct(ByteUtil.boxbyteArray(info.aesTmpKey));
        TLVTree kaInfoTree = TLVEngine.decodeTLV(ByteUtil.serverHexString2ByteArray(info.kaInfo));
        if (kaInfoTree == null || !(kaInfoTree instanceof KaInfo)) {
            LogHelper.e(TAG, "ka info decode fail");
            return new IOTController.KaInfoResponse(2046820355, new byte[0], new byte[0]);
        }
        cmdKa.kaInfo.setTLVStruct(kaInfoTree);
        TLVEngine.TLVResult decodeCmdTLV = TLVEngine.decodeCmdTLV(TcisJNI.processCmd(null, TLVEngine.encode2CmdTLV(cmdKa)));
        int resultCode = decodeCmdTLV.getResultCode();
        LogHelper.d(TAG, "processKaAuth result, resultCode: " + Integer.toHexString(resultCode));
        TLVTree.TLVRootTree resultTLV = decodeCmdTLV.getResultTLV();
        if (resultCode != Status.TCIS_Result.SUCCESS.value()) {
            return new IOTController.KaInfoResponse(resultCode, new byte[0], new byte[0]);
        }
        if (!(resultTLV instanceof RET_KA)) {
            return new IOTController.KaInfoResponse(2046820353, new byte[0], new byte[0]);
        }
        RET_KA retKa = (RET_KA) resultTLV;
        return new IOTController.KaInfoResponse(0, ByteUtil.unboxByteArray((Byte[]) retKa.iv.getTLVStruct()), ByteUtil.unboxByteArray((Byte[]) retKa.payload.getTLVStruct()));
    }
}
