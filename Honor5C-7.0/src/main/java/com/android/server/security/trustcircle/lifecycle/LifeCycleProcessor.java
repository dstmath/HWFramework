package com.android.server.security.trustcircle.lifecycle;

import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import com.android.internal.util.State;
import com.android.server.security.trustcircle.IOTController;
import com.android.server.security.trustcircle.jni.TcisJNI;
import com.android.server.security.trustcircle.tlv.command.login.CMD_LOGIN_CANCEL;
import com.android.server.security.trustcircle.tlv.command.login.CMD_LOGIN_REQ;
import com.android.server.security.trustcircle.tlv.command.login.CMD_LOGIN_RESULT_UPDATE;
import com.android.server.security.trustcircle.tlv.command.login.RET_LOGIN_REQ;
import com.android.server.security.trustcircle.tlv.command.logout.CMD_LOGOUT_REQ;
import com.android.server.security.trustcircle.tlv.command.query.CMD_GET_LOGIN_STATUS;
import com.android.server.security.trustcircle.tlv.command.query.CMD_GET_TCIS_ID;
import com.android.server.security.trustcircle.tlv.command.query.RET_GET_LOGIN_STATUS;
import com.android.server.security.trustcircle.tlv.command.query.RET_GET_TCIS_ID;
import com.android.server.security.trustcircle.tlv.command.register.CMD_CHECK_REG_STATUS;
import com.android.server.security.trustcircle.tlv.command.register.CMD_REG_CANCEL;
import com.android.server.security.trustcircle.tlv.command.register.CMD_REG_REQ;
import com.android.server.security.trustcircle.tlv.command.register.CMD_REG_RESULT;
import com.android.server.security.trustcircle.tlv.command.register.CMD_UNREG_REQ;
import com.android.server.security.trustcircle.tlv.command.register.RET_REG_REQ;
import com.android.server.security.trustcircle.tlv.core.TLVEngine;
import com.android.server.security.trustcircle.tlv.core.TLVEngine.TLVResult;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvokerWrapper.TCIS_Result;
import com.android.server.security.trustcircle.tlv.tree.AuthPkInfo;
import com.android.server.security.trustcircle.tlv.tree.UpdateIndexInfo;
import com.android.server.security.trustcircle.utils.ByteUtil;
import com.android.server.security.trustcircle.utils.LogHelper;
import huawei.android.security.ILifeCycleCallback;

public class LifeCycleProcessor {
    public static final String TAG = null;

    public enum LifeCycleStauts {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.security.trustcircle.lifecycle.LifeCycleProcessor.LifeCycleStauts.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.security.trustcircle.lifecycle.LifeCycleProcessor.LifeCycleStauts.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.security.trustcircle.lifecycle.LifeCycleProcessor.LifeCycleStauts.<clinit>():void");
        }
    }

    public enum UpdateStauts {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.security.trustcircle.lifecycle.LifeCycleProcessor.UpdateStauts.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.security.trustcircle.lifecycle.LifeCycleProcessor.UpdateStauts.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.security.trustcircle.lifecycle.LifeCycleProcessor.UpdateStauts.<clinit>():void");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.security.trustcircle.lifecycle.LifeCycleProcessor.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.security.trustcircle.lifecycle.LifeCycleProcessor.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.security.trustcircle.lifecycle.LifeCycleProcessor.<clinit>():void");
    }

    public LifeCycleProcessor() {
    }

    public static Bundle getTcisInfo() {
        TLVResult<?> result = sendCmd(new CMD_GET_TCIS_ID());
        TLVTree respTree = result.getResultTLV();
        int resultCode = result.getResultCode();
        if (respTree == null || !(respTree instanceof RET_GET_TCIS_ID) || resultCode != TCIS_Result.SUCCESS.value()) {
            return null;
        }
        RET_GET_TCIS_ID respTLV = (RET_GET_TCIS_ID) respTree;
        String tcisIDString = ByteUtil.byteArray2ServerHexString((Byte[]) respTLV.tcisID.getTLVStruct());
        short taVersion = ((Short) respTLV.TAVersion.getTLVStruct()).shortValue();
        Bundle tcisInfo = new Bundle();
        tcisInfo.putString("tcisID", tcisIDString);
        tcisInfo.putShort("TAVersion", taVersion);
        return tcisInfo;
    }

    public static boolean checkNeedRegister(int serverRegisterStatus, long userID) {
        return serverRegisterStatus == LifeCycleStauts.NOT_REGISTER.ordinal() || checkTARegisterStatus(userID) == LifeCycleStauts.NOT_REGISTER.ordinal();
    }

    public static boolean checkUserIDLogined(long userID) {
        return getLoginedUserID() == userID;
    }

    public static long getLoginedUserID() {
        TLVResult<?> result = sendCmd(new CMD_GET_LOGIN_STATUS());
        TLVTree respTree = result.getResultTLV();
        int resultCode = result.getResultCode();
        if (respTree != null && (respTree instanceof RET_GET_LOGIN_STATUS) && resultCode == TCIS_Result.SUCCESS.value()) {
            return ((Long) ((RET_GET_LOGIN_STATUS) respTree).userID.getTLVStruct()).longValue();
        }
        return 0;
    }

    private static int checkTARegisterStatus(long userID) {
        CMD_CHECK_REG_STATUS reqTLV = new CMD_CHECK_REG_STATUS();
        reqTLV.userID.setTLVStruct(Long.valueOf(userID));
        int resultCode = sendCmd(reqTLV).getResultCode();
        LogHelper.d(TAG, "loginServerRequest-TARegisterStatus: " + resultCode);
        if (resultCode == TCIS_Result.SUCCESS.value()) {
            return LifeCycleStauts.REGISTERED.ordinal();
        }
        return LifeCycleStauts.NOT_REGISTER.ordinal();
    }

    public static boolean register(ILifeCycleCallback callback, long userID, String sessionID) {
        boolean valid = false;
        CMD_REG_REQ reqTLV = new CMD_REG_REQ();
        reqTLV.userID.setTLVStruct(Long.valueOf(userID));
        reqTLV.sessionID.setTLVStruct(ByteUtil.hexString2ByteArray(sessionID));
        TLVResult<?> result = sendCmd(reqTLV);
        TLVTree respTree = result.getResultTLV();
        int resultCode = result.getResultCode();
        LogHelper.d(TAG, "register-errorCode: " + resultCode);
        int globalKeyID = -1;
        int i = -1;
        String str = null;
        String str2 = null;
        String clientChallenge = null;
        if (respTree != null && (respTree instanceof RET_REG_REQ) && resultCode == TCIS_Result.SUCCESS.value()) {
            RET_REG_REQ respTLV = (RET_REG_REQ) respTree;
            valid = true;
            globalKeyID = ((Integer) respTLV.glogbalKeyID.getTLVStruct()).intValue();
            i = ((Short) respTLV.authKeyAlgoEncode.getTLVStruct()).shortValue();
            str = ByteUtil.byteArray2ServerHexString(respTLV.regAuthKeyData.encapsulate());
            str2 = respTLV.regAuthKeyDataSign.byteArray2ServerHexString();
            clientChallenge = respTLV.clientChallenge.byteArray2ServerHexString();
        }
        onRegisterResponse(callback, resultCode, globalKeyID, i, str, str2, clientChallenge);
        return valid;
    }

    public static void onRegisterResponse(ILifeCycleCallback callback, int errorCode, int globalKeyID, int authKeyAlgoType, String regAuthKeyData, String regAuthKeyDataSign, String clientChallenge) {
        if (callback != null) {
            try {
                callback.onRegisterResponse(errorCode, globalKeyID, (short) authKeyAlgoType, regAuthKeyData, regAuthKeyDataSign, clientChallenge);
            } catch (RemoteException e) {
                LogHelper.e(TAG, e.getMessage());
            }
        }
    }

    public static boolean finalRegister(ILifeCycleCallback callback, String authPKInfo, String authPKInfoSign, String updateIndexInfo, String updateIndexSignature) {
        boolean valid = false;
        TLVTree authPkInfoTree = TLVEngine.decodeTLV(ByteUtil.serverHexString2ByteArray(authPKInfo));
        Byte[] authPKInfoSignBytes = ByteUtil.serverHexString2ByteArray(authPKInfoSign);
        TLVTree updateIndexInfoTree = TLVEngine.decodeTLV(ByteUtil.serverHexString2ByteArray(updateIndexInfo));
        Byte[] updateIndexSignBytes = ByteUtil.serverHexString2ByteArray(updateIndexSignature);
        if ((authPkInfoTree instanceof AuthPkInfo) && (updateIndexInfoTree instanceof UpdateIndexInfo)) {
            CMD_REG_RESULT reqTLV = new CMD_REG_RESULT();
            reqTLV.authPkInfo.setTLVStruct(authPkInfoTree);
            reqTLV.authPKInfoSign.setTLVStruct(authPKInfoSignBytes);
            reqTLV.updateIndexInfo.setTLVStruct(updateIndexInfoTree);
            reqTLV.updateIndexInfoSign.setTLVStruct(updateIndexSignBytes);
            int resultCode = sendCmd(reqTLV).getResultCode();
            if (resultCode == TCIS_Result.SUCCESS.value()) {
                valid = true;
            }
            LogHelper.d(TAG, "finalRegister-errorCode: " + resultCode);
            onFinalRegisterResult(callback, resultCode);
            return valid;
        }
        LogHelper.d(TAG, "error decode in finalRegister");
        onFinalRegisterResult(callback, TCIS_Result.UNKNOWN_CMD.value());
        return false;
    }

    public static void onFinalRegisterResult(ILifeCycleCallback callback, int resultCode) {
        if (callback != null) {
            try {
                callback.onFinalRegisterResult(resultCode);
            } catch (RemoteException e) {
                LogHelper.e(TAG, e.getMessage());
            }
        }
    }

    public static boolean loginAndUpdate(ILifeCycleCallback callback, long userID) {
        boolean valid = false;
        CMD_LOGIN_REQ reqTLV = new CMD_LOGIN_REQ();
        reqTLV.userID.setTLVStruct(Long.valueOf(userID));
        TLVResult<?> result = sendCmd(reqTLV);
        TLVTree respTree = result.getResultTLV();
        int resultCode = result.getResultCode();
        LogHelper.d(TAG, "loginServerRequest-loginAndUpdate-errorCode: " + resultCode);
        int indexVersion = -1;
        String clientChallenge = null;
        if (respTree != null && (respTree instanceof RET_LOGIN_REQ) && resultCode == TCIS_Result.SUCCESS.value()) {
            RET_LOGIN_REQ respTLV = (RET_LOGIN_REQ) respTree;
            valid = true;
            indexVersion = ((Integer) respTLV.indexVersion.getTLVStruct()).intValue();
            clientChallenge = respTLV.clientChallenge.byteArray2ServerHexString();
        }
        onLoginResponse(callback, resultCode, indexVersion, clientChallenge);
        return valid;
    }

    public static void onLoginResponse(ILifeCycleCallback callback, int errorCode, int indexVersion, String clientChallenge) {
        if (callback != null) {
            try {
                callback.onLoginResponse(errorCode, indexVersion, clientChallenge);
            } catch (RemoteException e) {
                LogHelper.e(TAG, e.getMessage());
            }
        }
    }

    public static boolean finalLogin(ILifeCycleCallback callback, int updateResult, String updateIndexInfo, String updateIndexSignature) {
        boolean valid = false;
        int resultCode = TCIS_Result.BAD_PARAM.value();
        if (updateResult == UpdateStauts.NO_NEED_UPDATE.ordinal()) {
            LogHelper.d(TAG, "finalLogin-nothing to update");
            resultCode = TCIS_Result.SUCCESS.value();
            valid = true;
        } else if (TextUtils.isEmpty(updateIndexInfo) || TextUtils.isEmpty(updateIndexSignature)) {
            LogHelper.w(TAG, "error:finalLogin-update date is not complete");
            resultCode = TCIS_Result.BAD_PARAM.value();
        } else {
            LogHelper.d(TAG, "finalLogin-update tcis data");
            TLVTree updateIndexInfoTree = TLVEngine.decodeTLV(ByteUtil.serverHexString2ByteArray(updateIndexInfo));
            if (updateIndexInfoTree instanceof UpdateIndexInfo) {
                Byte[] updateIndexSignBytes = ByteUtil.serverHexString2ByteArray(updateIndexSignature);
                CMD_LOGIN_RESULT_UPDATE reqTLV = new CMD_LOGIN_RESULT_UPDATE();
                reqTLV.updateIndexInfo.setTLVStruct(updateIndexInfoTree);
                reqTLV.updateIndexInfoSign.setTLVStruct(updateIndexSignBytes);
                resultCode = sendCmd(reqTLV).getResultCode();
                if (resultCode == TCIS_Result.SUCCESS.value()) {
                    valid = true;
                }
            } else {
                LogHelper.d(TAG, "error decode in finalLogin");
                onFinalLoginResult(callback, TCIS_Result.BAD_PARAM.value());
                return false;
            }
        }
        LogHelper.d(TAG, "finalLogin-errorCode: " + resultCode);
        onFinalLoginResult(callback, resultCode);
        return valid;
    }

    public static void onFinalLoginResult(ILifeCycleCallback callback, int errorCode) {
        if (callback != null) {
            try {
                callback.onFinalLoginResult(errorCode);
            } catch (RemoteException e) {
                LogHelper.e(TAG, e.getMessage());
            }
        }
    }

    public static boolean cancelRegOrLogin(State state, ILifeCycleCallback callback, long userID) {
        if (state == null) {
            LogHelper.w(TAG, "nothing to cancel");
            return false;
        } else if (RegisteringState.class == state.getClass()) {
            return cancelReg(callback, userID);
        } else {
            if (LoginingState.class == state.getClass()) {
                return cancelLogin(callback, userID);
            }
            LogHelper.w(TAG, "unknown cancel state: " + state.getName());
            return false;
        }
    }

    private static boolean cancelReg(ILifeCycleCallback callback, long userID) {
        boolean valid = false;
        CMD_REG_CANCEL reqTLV = new CMD_REG_CANCEL();
        reqTLV.userID.setTLVStruct(Long.valueOf(userID));
        int resultCode = sendCmd(reqTLV).getResultCode();
        if (resultCode == TCIS_Result.SUCCESS.value()) {
            valid = true;
        }
        LogHelper.d(TAG, "cancelRegister-errorCode: " + resultCode);
        return valid;
    }

    private static boolean cancelLogin(ILifeCycleCallback callback, long userID) {
        boolean valid = false;
        CMD_LOGIN_CANCEL reqTLV = new CMD_LOGIN_CANCEL();
        reqTLV.userID.setTLVStruct(Long.valueOf(userID));
        int resultCode = sendCmd(reqTLV).getResultCode();
        if (resultCode == TCIS_Result.SUCCESS.value()) {
            valid = true;
        }
        LogHelper.d(TAG, "cancelLogin-errorCode: " + resultCode);
        return valid;
    }

    public static boolean logout(ILifeCycleCallback callback, long userID) {
        IOTController.getInstance().cancelAuth(-2);
        boolean valid = false;
        CMD_LOGOUT_REQ reqTLV = new CMD_LOGOUT_REQ();
        reqTLV.userID.setTLVStruct(Long.valueOf(userID));
        int resultCode = sendCmd(reqTLV).getResultCode();
        if (resultCode == TCIS_Result.SUCCESS.value()) {
            valid = true;
        }
        LogHelper.d(TAG, "logout-errorCode: " + resultCode);
        try {
            callback.onLogoutResult(resultCode);
        } catch (RemoteException e) {
            LogHelper.e(TAG, e.getMessage());
        }
        return valid;
    }

    public static boolean unregister(ILifeCycleCallback callback, long userID) {
        IOTController.getInstance().cancelAuth(-2);
        boolean valid = false;
        CMD_UNREG_REQ reqTLV = new CMD_UNREG_REQ();
        reqTLV.userID.setTLVStruct(Long.valueOf(userID));
        int resultCode = sendCmd(reqTLV).getResultCode();
        if (resultCode == TCIS_Result.SUCCESS.value()) {
            valid = true;
        }
        LogHelper.d(TAG, "unregister-errorCode: " + resultCode);
        try {
            callback.onUnregisterResult(resultCode);
        } catch (RemoteException e) {
            LogHelper.e(TAG, e.getMessage());
        }
        return valid;
    }

    public static <T extends TLVRootTree> TLVResult<T> sendCmd(T reqTLV) {
        return TLVEngine.decodeCmdTLV(processCmd(TLVEngine.encode2CmdTLV(reqTLV)));
    }

    private static byte[] processCmd(byte[] request) {
        return TcisJNI.processCmd(null, request);
    }
}
