package com.android.server.security.trustcircle.lifecycle;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import com.android.internal.util.State;
import com.android.server.security.trustcircle.CallbackManager;
import com.android.server.security.trustcircle.jni.TcisJNI;
import com.android.server.security.trustcircle.tlv.command.login.CMD_LOGIN_CANCEL;
import com.android.server.security.trustcircle.tlv.command.login.CMD_LOGIN_REQ;
import com.android.server.security.trustcircle.tlv.command.login.CMD_LOGIN_RESULT_UPDATE;
import com.android.server.security.trustcircle.tlv.command.login.RET_LOGIN_REQ;
import com.android.server.security.trustcircle.tlv.command.logout.CMD_LOGOUT_REQ;
import com.android.server.security.trustcircle.tlv.command.query.CMD_GET_LOGIN_STATUS;
import com.android.server.security.trustcircle.tlv.command.query.CMD_GET_TCIS_ID;
import com.android.server.security.trustcircle.tlv.command.query.CMD_INIT_REQ;
import com.android.server.security.trustcircle.tlv.command.query.DATA_TCIS_ERROR_STEP;
import com.android.server.security.trustcircle.tlv.command.query.RET_GET_LOGIN_STATUS;
import com.android.server.security.trustcircle.tlv.command.query.RET_GET_TCIS_ID;
import com.android.server.security.trustcircle.tlv.command.query.RET_INIT_REQ;
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
import com.android.server.security.trustcircle.tlv.tree.AuthPkInfo;
import com.android.server.security.trustcircle.tlv.tree.StatesInfo;
import com.android.server.security.trustcircle.tlv.tree.UpdateIndexInfo;
import com.android.server.security.trustcircle.utils.ByteUtil;
import com.android.server.security.trustcircle.utils.LogHelper;
import com.android.server.security.trustcircle.utils.Status.ExceptionStep;
import com.android.server.security.trustcircle.utils.Status.LifeCycleStauts;
import com.android.server.security.trustcircle.utils.Status.TCIS_Result;
import com.android.server.security.trustcircle.utils.Status.UpdateStauts;
import com.android.server.security.trustcircle.utils.Utils;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class LifeCycleProcessor {
    public static final long INVALID_USER_ID = -1;
    private static final String KEY_TCIS_STATUS = "tcis_error_step";
    public static final byte LOGINED = (byte) 1;
    private static final String SERVICE_NAME = "com.huawei.trustcircle.tcis.TCUpdateService";
    public static final String TAG = LifeCycleProcessor.class.getSimpleName();
    private static final String TCIS_PKG_NAME = "com.huawei.trustcircle";
    private static volatile long currentUserId = -1;

    public static class UsersState {
        private int state;
        private long userHandle;

        public UsersState(long userHandle, int state) {
            this.userHandle = userHandle;
            this.state = state;
        }

        public long getUserId() {
            return this.userHandle;
        }

        public void setUserHandle(int userHandle) {
            this.userHandle = (long) userHandle;
        }

        public int getState() {
            return this.state;
        }

        public void setState(int state) {
            this.state = state;
        }

        public String getString() {
            short status = (short) this.state;
            StringBuffer sb = new StringBuffer();
            sb.append(ByteUtil.long2StrictHexString(this.userHandle)).append(ByteUtil.short2HexString(status));
            return sb.toString();
        }
    }

    public static void updateTime(long time) {
        NetworkChangeReceiver.updateTime(time);
    }

    public static long initAndroidUser(int userHandle) {
        boolean z;
        CMD_INIT_REQ reqTLV = new CMD_INIT_REQ();
        reqTLV.userHandle.setTLVStruct(Integer.valueOf(userHandle));
        TLVResult<?> result = sendCmd(reqTLV);
        TLVTree respTree = result.getResultTLV();
        int resultCode = result.getResultCode();
        long userID = -1;
        if (respTree != null && (respTree instanceof RET_INIT_REQ) && resultCode == TCIS_Result.SUCCESS.value()) {
            RET_INIT_REQ respTLV = (RET_INIT_REQ) respTree;
            Byte[] loginState = (Byte[]) respTLV.loginState.getTLVStruct();
            if (loginState != null && loginState.length == 1 && loginState[0].byteValue() == (byte) 1) {
                userID = ((Long) respTLV.userID.getTLVStruct()).longValue();
            }
        }
        if (userID != -1) {
            z = true;
        } else {
            z = false;
        }
        TcisLifeCycleDispatcher.setIotEnable(z);
        updateExceptionStepOfCurrentUser(resultCode, ExceptionStep.SWITCH_USER, false);
        LogHelper.d(TAG, "initAndroidUser-resultCode: " + resultCode);
        return userID;
    }

    public static Bundle getTcisInfo() {
        TLVResult<?> result = sendCmd(new CMD_GET_TCIS_ID());
        TLVTree respTree = result.getResultTLV();
        int resultCode = result.getResultCode();
        Bundle tcisInfo = null;
        if (respTree != null && (respTree instanceof RET_GET_TCIS_ID) && resultCode == TCIS_Result.SUCCESS.value()) {
            RET_GET_TCIS_ID respTLV = (RET_GET_TCIS_ID) respTree;
            String tcisIDString = ByteUtil.byteArray2ServerHexString((Byte[]) respTLV.tcisID.getTLVStruct());
            short taVersion = ((Short) respTLV.TAVersion.getTLVStruct()).shortValue();
            tcisInfo = new Bundle();
            tcisInfo.putString("tcisID", tcisIDString);
            tcisInfo.putShort("TAVersion", taVersion);
            long hwUserId = getLoginedUserID();
            tcisInfo.putString("hwUserId", hwUserId <= 0 ? null : String.valueOf(hwUserId));
        }
        return tcisInfo;
    }

    public static boolean checkNeedRegister(int serverRegisterStatus, long userID) {
        return serverRegisterStatus == LifeCycleStauts.NOT_REGISTER.ordinal() || checkTARegisterStatus(userID) == LifeCycleStauts.NOT_REGISTER.ordinal();
    }

    public static boolean checkUserIDLogined(long userID) {
        if (getLoginedUserID() == userID) {
            return true;
        }
        return false;
    }

    public static long getLoginedUserID() {
        TLVResult<?> result = sendCmd(new CMD_GET_LOGIN_STATUS());
        TLVTree respTree = result.getResultTLV();
        int resultCode = result.getResultCode();
        long userID = -1;
        if (respTree != null && (respTree instanceof RET_GET_LOGIN_STATUS) && resultCode == TCIS_Result.SUCCESS.value()) {
            userID = ((Long) ((RET_GET_LOGIN_STATUS) respTree).userID.getTLVStruct()).longValue();
        }
        LogHelper.d(TAG, "getLoginedUser-resultCode: " + resultCode);
        return userID;
    }

    public static int checkTARegisterStatus(long userID) {
        CMD_CHECK_REG_STATUS reqTLV = new CMD_CHECK_REG_STATUS();
        reqTLV.userID.setTLVStruct(Long.valueOf(userID));
        int resultCode = sendCmd(reqTLV).getResultCode();
        LogHelper.d(TAG, "loginServerRequest-TARegisterStatus: " + resultCode);
        if (resultCode == TCIS_Result.SUCCESS.value()) {
            return LifeCycleStauts.REGISTERED.ordinal();
        }
        return LifeCycleStauts.NOT_REGISTER.ordinal();
    }

    public static boolean register(long userID, String sessionID) {
        TcisLifeCycleDispatcher.setIotEnable(false);
        currentUserId = userID;
        boolean valid = false;
        CMD_REG_REQ reqTLV = new CMD_REG_REQ();
        reqTLV.userID.setTLVStruct(Long.valueOf(userID));
        reqTLV.sessionID.setTLVStruct(ByteUtil.hexString2ByteArray(sessionID));
        TLVResult<?> result = sendCmd(reqTLV);
        TLVTree respTree = result.getResultTLV();
        int resultCode = result.getResultCode();
        LogHelper.d(TAG, "register-resultCode: " + resultCode);
        int globalKeyID = -1;
        short authKeyAlgoEncode = (short) -1;
        String regAuthKeyData = null;
        String regAuthKeyDataSign = null;
        String clientChallenge = null;
        if (respTree != null && (respTree instanceof RET_REG_REQ) && resultCode == TCIS_Result.SUCCESS.value()) {
            RET_REG_REQ respTLV = (RET_REG_REQ) respTree;
            valid = true;
            globalKeyID = ((Integer) respTLV.glogbalKeyID.getTLVStruct()).intValue();
            authKeyAlgoEncode = ((Short) respTLV.authKeyAlgoEncode.getTLVStruct()).shortValue();
            regAuthKeyData = ByteUtil.byteArray2ServerHexString(respTLV.regAuthKeyData.encapsulate());
            regAuthKeyDataSign = respTLV.regAuthKeyDataSign.byteArray2ServerHexString();
            clientChallenge = respTLV.clientChallenge.byteArray2ServerHexString();
        }
        return onRegisterResponse(resultCode, globalKeyID, authKeyAlgoEncode, regAuthKeyData, regAuthKeyDataSign, clientChallenge) ? valid : false;
    }

    public static boolean onRegisterResponse(int resultCode, int globalKeyID, int authKeyAlgoType, String regAuthKeyData, String regAuthKeyDataSign, String clientChallenge) {
        updateExceptionStepOfCurrentUser(resultCode, ExceptionStep.REG, false);
        try {
            if (CallbackManager.getInstance().isILifeCycleCallbackValid()) {
                CallbackManager.getInstance().getILifeCycleCallback().onRegisterResponse(resultCode, globalKeyID, (short) authKeyAlgoType, regAuthKeyData, regAuthKeyDataSign, clientChallenge);
                return true;
            }
            CallbackManager.getInstance().unregisterILifeCycleCallback();
            return false;
        } catch (RemoteException e) {
            LogHelper.e(TAG, "RemoteException in onRegisterResponse: " + e.getMessage());
            CallbackManager.getInstance().unregisterILifeCycleCallback();
            return false;
        }
    }

    public static boolean finalRegister(String authPKInfo, String authPKInfoSign, String updateIndexInfo, String updateIndexSignature) {
        boolean valid = false;
        TLVTree authPkInfoTree = TLVEngine.decodeTLV(ByteUtil.serverHexString2ByteArray(authPKInfo));
        Byte[] authPKInfoSignBytes = ByteUtil.serverHexString2ByteArray(authPKInfoSign);
        TLVTree updateIndexInfoTree = TLVEngine.decodeTLV(ByteUtil.serverHexString2ByteArray(updateIndexInfo));
        Byte[] updateIndexSignBytes = ByteUtil.serverHexString2ByteArray(updateIndexSignature);
        if ((authPkInfoTree instanceof AuthPkInfo) && ((updateIndexInfoTree instanceof UpdateIndexInfo) ^ 1) == 0) {
            CMD_REG_RESULT reqTLV = new CMD_REG_RESULT();
            reqTLV.authPkInfo.setTLVStruct(authPkInfoTree);
            reqTLV.authPKInfoSign.setTLVStruct(authPKInfoSignBytes);
            reqTLV.updateIndexInfo.setTLVStruct(updateIndexInfoTree);
            reqTLV.updateIndexInfoSign.setTLVStruct(updateIndexSignBytes);
            int resultCode = sendCmd(reqTLV).getResultCode();
            if (resultCode == TCIS_Result.SUCCESS.value()) {
                LogHelper.i(TAG, "register tcis to TA successfully");
                valid = true;
            } else {
                LogHelper.w(TAG, "register tcis to TA failed");
            }
            LogHelper.d(TAG, "finalRegister-resultCode: " + resultCode);
            if (!onFinalRegisterResult(resultCode)) {
                valid = false;
            }
            return valid;
        }
        LogHelper.e(TAG, "error decode in finalRegister");
        onFinalRegisterResult(TCIS_Result.UNKNOWN_CMD.value());
        return false;
    }

    public static boolean onFinalRegisterResult(int resultCode) {
        boolean result;
        TcisLifeCycleDispatcher.setIotEnableByResultCode(resultCode);
        updateExceptionStepOfCurrentUser(resultCode, ExceptionStep.REG, false);
        try {
            if (CallbackManager.getInstance().isILifeCycleCallbackValid()) {
                CallbackManager.getInstance().getILifeCycleCallback().onFinalRegisterResult(resultCode);
                result = true;
            } else {
                result = false;
            }
        } catch (RemoteException e) {
            LogHelper.e(TAG, "RemoteException in onFinalRegisterResult: " + e.getMessage());
            result = false;
        }
        CallbackManager.getInstance().unregisterILifeCycleCallback();
        return result;
    }

    public static boolean loginAndUpdate(long userID) {
        TcisLifeCycleDispatcher.setIotEnable(false);
        currentUserId = userID;
        boolean valid = false;
        TLVResult<?> result = updateCmd(userID);
        TLVTree respTree = result.getResultTLV();
        int resultCode = result.getResultCode();
        LogHelper.d(TAG, "loginAndUpdate-resultCode: " + resultCode);
        int indexVersion = -1;
        String clientChallenge = null;
        if (respTree != null && (respTree instanceof RET_LOGIN_REQ) && resultCode == TCIS_Result.SUCCESS.value()) {
            RET_LOGIN_REQ respTLV = (RET_LOGIN_REQ) respTree;
            valid = true;
            indexVersion = ((Integer) respTLV.indexVersion.getTLVStruct()).intValue();
            clientChallenge = respTLV.clientChallenge.byteArray2ServerHexString();
        }
        return onLoginResponse(resultCode, indexVersion, clientChallenge) ? valid : false;
    }

    private static TLVResult<?> updateCmd(long userID) {
        CMD_LOGIN_REQ reqTLV = new CMD_LOGIN_REQ();
        reqTLV.userID.setTLVStruct(Long.valueOf(userID));
        return sendCmd(reqTLV);
    }

    public static boolean onLoginResponse(int resultCode, int indexVersion, String clientChallenge) {
        TcisLifeCycleDispatcher.setIotEnableByResultCode(resultCode);
        updateExceptionStepOfCurrentUser(resultCode, ExceptionStep.LOGIN, false);
        try {
            if (CallbackManager.getInstance().isILifeCycleCallbackValid()) {
                CallbackManager.getInstance().getILifeCycleCallback().onLoginResponse(resultCode, indexVersion, clientChallenge);
                return true;
            }
            CallbackManager.getInstance().unregisterILifeCycleCallback();
            return false;
        } catch (RemoteException e) {
            LogHelper.e(TAG, "RemoteException in onLoginResponse: " + e.getMessage());
            CallbackManager.getInstance().unregisterILifeCycleCallback();
            return false;
        }
    }

    public static boolean updateOnly(long userID) {
        boolean valid = false;
        currentUserId = userID;
        TLVResult<?> result = updateCmd(userID);
        TLVTree respTree = result.getResultTLV();
        int resultCode = result.getResultCode();
        LogHelper.d(TAG, "updateServerRequest-updateOnly-resultCode: " + resultCode);
        int indexVersion = -1;
        String clientChallenge = null;
        if (respTree != null && (respTree instanceof RET_LOGIN_REQ) && resultCode == TCIS_Result.SUCCESS.value()) {
            RET_LOGIN_REQ respTLV = (RET_LOGIN_REQ) respTree;
            valid = true;
            indexVersion = ((Integer) respTLV.indexVersion.getTLVStruct()).intValue();
            clientChallenge = respTLV.clientChallenge.byteArray2ServerHexString();
        }
        return onUpdateResponse(resultCode, indexVersion, clientChallenge) ? valid : false;
    }

    public static boolean onUpdateResponse(int resultCode, int indexVersion, String clientChallenge) {
        updateExceptionStepOfCurrentUser(resultCode, ExceptionStep.UPDATE, false);
        try {
            if (CallbackManager.getInstance().isILifeCycleCallbackValid()) {
                CallbackManager.getInstance().getILifeCycleCallback().onUpdateResponse(resultCode, indexVersion, clientChallenge);
                return true;
            }
            CallbackManager.getInstance().unregisterILifeCycleCallback();
            return false;
        } catch (RemoteException e) {
            LogHelper.e(TAG, "RemoteException in onUpdateResponse: " + e.getMessage());
            CallbackManager.getInstance().unregisterILifeCycleCallback();
            return false;
        }
    }

    public static boolean finalLogin(int updateResult, String updateIndexInfo, String updateIndexSignature) {
        boolean valid = false;
        int resultCode = TCIS_Result.BAD_PARAM.value();
        if (updateResult == UpdateStauts.NO_NEED_UPDATE.ordinal()) {
            LogHelper.i(TAG, "finalLogin-nothing to update");
            resultCode = TCIS_Result.SUCCESS.value();
            valid = true;
        } else if (TextUtils.isEmpty(updateIndexInfo) || TextUtils.isEmpty(updateIndexSignature)) {
            LogHelper.w(TAG, "error:finalLogin-update data is not complete");
            resultCode = TCIS_Result.BAD_PARAM.value();
        } else {
            LogHelper.i(TAG, "finalLogin-need to update tcis data");
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
                LogHelper.e(TAG, "error decode in finalLogin");
                onFinalLoginResult(TCIS_Result.BAD_PARAM.value());
                return false;
            }
        }
        LogHelper.d(TAG, "finalLogin-resultCode: " + resultCode);
        if (!onFinalLoginResult(resultCode)) {
            valid = false;
        }
        return valid;
    }

    public static boolean onFinalLoginResult(int resultCode) {
        boolean result;
        updateExceptionStepOfCurrentUser(resultCode, ExceptionStep.LOGIN, false);
        try {
            if (CallbackManager.getInstance().isILifeCycleCallbackValid()) {
                CallbackManager.getInstance().getILifeCycleCallback().onFinalLoginResult(resultCode);
                result = true;
            } else {
                result = false;
            }
        } catch (RemoteException e) {
            LogHelper.e(TAG, "RemoteException in onFinalLoginResult: " + e.getMessage());
            result = false;
        }
        CallbackManager.getInstance().unregisterILifeCycleCallback();
        return result;
    }

    public static boolean cancelRegOrLogin(State state, long userID) {
        if (state == null) {
            LogHelper.w(TAG, "nothing to cancel");
            return false;
        } else if (RegisteringState.class == state.getClass()) {
            return cancelReg(userID);
        } else {
            if (LoginingState.class == state.getClass()) {
                return cancelLogin(userID);
            }
            LogHelper.w(TAG, "unknown cancel state: " + state.getName());
            return false;
        }
    }

    private static boolean cancelReg(long userID) {
        TcisLifeCycleDispatcher.setIotEnable(false);
        boolean valid = false;
        CMD_REG_CANCEL reqTLV = new CMD_REG_CANCEL();
        reqTLV.userID.setTLVStruct(Long.valueOf(userID));
        int resultCode = sendCmd(reqTLV).getResultCode();
        if (resultCode == TCIS_Result.SUCCESS.value()) {
            currentUserId = -1;
            valid = true;
        }
        LogHelper.d(TAG, "cancelRegister-resultCode: " + resultCode);
        CallbackManager.getInstance().unregisterILifeCycleCallback();
        return valid;
    }

    private static boolean cancelLogin(long userID) {
        TcisLifeCycleDispatcher.setIotEnable(false);
        boolean valid = false;
        CMD_LOGIN_CANCEL reqTLV = new CMD_LOGIN_CANCEL();
        reqTLV.userID.setTLVStruct(Long.valueOf(userID));
        int resultCode = sendCmd(reqTLV).getResultCode();
        if (resultCode == TCIS_Result.SUCCESS.value()) {
            currentUserId = -1;
            valid = true;
        }
        LogHelper.d(TAG, "cancelLogin-resultCode: " + resultCode);
        CallbackManager.getInstance().unregisterILifeCycleCallback();
        return valid;
    }

    public static boolean logout(long userID) {
        boolean valid = false;
        CMD_LOGOUT_REQ reqTLV = new CMD_LOGOUT_REQ();
        reqTLV.userID.setTLVStruct(Long.valueOf(userID));
        int resultCode = sendCmd(reqTLV).getResultCode();
        if (resultCode == TCIS_Result.SUCCESS.value()) {
            currentUserId = -1;
            valid = true;
        }
        LogHelper.d(TAG, "logout-resultCode: " + resultCode);
        return onLogoutResult(resultCode) ? valid : false;
    }

    public static boolean onLogoutResult(int resultCode) {
        TcisLifeCycleDispatcher.setIotEnableByResultCodeReverse(resultCode);
        updateExceptionStepOfCurrentUser(resultCode, ExceptionStep.LOGOUT, false);
        try {
            if (CallbackManager.getInstance().isILifeCycleCallbackValid()) {
                CallbackManager.getInstance().getILifeCycleCallback().onLogoutResult(resultCode);
                CallbackManager.getInstance().unregisterILifeCycleCallback();
                return true;
            }
            CallbackManager.getInstance().unregisterILifeCycleCallback();
            return false;
        } catch (RemoteException e) {
            LogHelper.e(TAG, "RemoteException in onLogoutResult: " + e.getMessage());
            CallbackManager.getInstance().unregisterILifeCycleCallback();
            return false;
        }
    }

    public static boolean unregister(long userID) {
        boolean valid = false;
        currentUserId = userID;
        CMD_UNREG_REQ reqTLV = new CMD_UNREG_REQ();
        reqTLV.userID.setTLVStruct(Long.valueOf(userID));
        int resultCode = sendCmd(reqTLV).getResultCode();
        if (resultCode == TCIS_Result.SUCCESS.value()) {
            valid = true;
        }
        LogHelper.d(TAG, "unregister-resultCode: " + resultCode);
        return onUnregisterResult(resultCode) ? valid : false;
    }

    public static boolean onUnregisterResult(int resultCode) {
        TcisLifeCycleDispatcher.setIotEnableByResultCodeReverse(resultCode);
        updateExceptionStepOfCurrentUser(resultCode, ExceptionStep.UNREG, true);
        if (resultCode == TCIS_Result.SUCCESS.value()) {
            currentUserId = -1;
        }
        try {
            if (CallbackManager.getInstance().isILifeCycleCallbackValid()) {
                CallbackManager.getInstance().getILifeCycleCallback().onUnregisterResult(resultCode);
                CallbackManager.getInstance().unregisterILifeCycleCallback();
                return true;
            }
            CallbackManager.getInstance().unregisterILifeCycleCallback();
            return false;
        } catch (RemoteException e) {
            LogHelper.e(TAG, "RemoteException in onUnregisterResult: " + e.getMessage());
            CallbackManager.getInstance().unregisterILifeCycleCallback();
            return false;
        }
    }

    public static <T extends TLVRootTree> TLVResult<T> sendCmd(T reqTLV) {
        return TLVEngine.decodeCmdTLV(processCmd(TLVEngine.encode2CmdTLV(reqTLV)));
    }

    private static byte[] processCmd(byte[] request) {
        return TcisJNI.processCmd(null, request);
    }

    public static ComponentName startTcisService(Context context) {
        if (context == null) {
            LogHelper.e(TAG, "error- startTcisService: Context is null");
            return null;
        }
        Intent serviceIntent = new Intent();
        serviceIntent.setComponent(new ComponentName(TCIS_PKG_NAME, SERVICE_NAME));
        serviceIntent.putExtra("taLoginedUserId", getLoginedUserID());
        return context.startService(serviceIntent);
    }

    public static void updateExceptionStepOfCurrentUser(int resultCode, ExceptionStep step, boolean delete) {
        if (step != null && currentUserId != -1) {
            UsersState newState;
            StringBuffer sb = new StringBuffer();
            if (resultCode != TCIS_Result.SUCCESS.value()) {
                newState = new UsersState(currentUserId, step.ordinal());
            } else {
                newState = new UsersState(currentUserId, ExceptionStep.NO_EXCEPTION.ordinal());
            }
            UsersState[] states = getUsersState();
            int number = states.length;
            boolean hasData = false;
            for (int j = 0; j < states.length; j++) {
                if (states[j].getUserId() == currentUserId) {
                    hasData = true;
                    if (delete) {
                    } else {
                        states[j] = newState;
                    }
                }
                sb.append(states[j].getString());
            }
            if (!(hasData || (delete ^ 1) == 0)) {
                sb.append(newState.getString());
                number++;
            }
            Utils.setProperty(TcisLifeCycleDispatcher.getInstance().getContext(), KEY_TCIS_STATUS, ByteUtil.byteArrayOri2HexString(TLVEngine.encode2TLV(new DATA_TCIS_ERROR_STEP((short) number, new StatesInfo(ByteUtil.hexString2ByteArray(sb.toString()))))));
        }
    }

    public static String getExceptionSteps() {
        return Utils.getProperty(TcisLifeCycleDispatcher.getInstance().getContext(), KEY_TCIS_STATUS);
    }

    public static UsersState[] getUsersState() {
        UsersState[] userInfos = new UsersState[0];
        TLVTree oriData = TLVEngine.decodeTLV(ByteUtil.hexString2byteArray(getExceptionSteps()));
        if (oriData != null && (oriData instanceof DATA_TCIS_ERROR_STEP)) {
            DATA_TCIS_ERROR_STEP data = (DATA_TCIS_ERROR_STEP) oriData;
            int number = ((Short) data.numbers.getTLVStruct()).shortValue();
            byte[] infos = ByteUtil.unboxByteArray((Byte[]) ((StatesInfo) data.info.getTLVStruct()).infos.getTLVStruct());
            LogHelper.i(TAG, "tcis number: " + number);
            if (number != 0 && infos.length == number * 10) {
                userInfos = new UsersState[number];
                ByteBuffer buffer = ByteBuffer.wrap(infos);
                buffer.order(ByteOrder.BIG_ENDIAN);
                int i = 0;
                while (buffer.remaining() >= 10 && i <= number) {
                    userInfos[i] = new UsersState(buffer.getLong(), buffer.getShort());
                    i++;
                }
            }
        }
        return userInfos;
    }

    public static int getExceptionStepOfCurrentUserId() {
        UsersState[] states = getUsersState();
        for (int j = 0; j < states.length; j++) {
            if (states[j].getUserId() == currentUserId) {
                LogHelper.i(TAG, "tcis user " + currentUserId + " ,exception step: " + states[j].getState());
                return states[j].getState();
            }
        }
        LogHelper.w(TAG, "no exception data in tcis user " + currentUserId);
        return ExceptionStep.NO_EXCEPTION.ordinal();
    }
}
