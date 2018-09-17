package com.android.server.security.trustcircle.lifecycle;

import android.content.Context;
import android.os.Bundle;
import com.android.server.security.trustcircle.utils.LogHelper;
import com.android.server.security.trustcircle.utils.Status.TCIS_Result;
import com.android.server.security.trustcircle.utils.Utils;

public class TcisLifeCycleDispatcher {
    private static final String TAG = TcisLifeCycleDispatcher.class.getSimpleName();
    private static volatile boolean isReadyForIot;
    private static volatile TcisLifeCycleDispatcher sInstance;
    private Context mContext;
    private volatile LifeCycleStateMachine stateMachine = new LifeCycleStateMachine();

    public static class CancelData {
        long userID;

        public CancelData(long userID) {
            this.userID = userID;
        }
    }

    public static class LoginServerRequestData {
        int serverRegisterStatus;
        String sessionID;
        long userID;

        public LoginServerRequestData(long userID, int serverRegisterStatus, String sessionID) {
            this.userID = userID;
            this.serverRegisterStatus = serverRegisterStatus;
            this.sessionID = sessionID;
        }
    }

    public static class LogoutData {
        long userID;

        public LogoutData(long userID) {
            this.userID = userID;
        }
    }

    public static class RegisterData {
        String authPKInfo;
        String authPKInfoSign;
        String updateIndexInfo;
        String updateIndexSignature;

        public RegisterData(String authPKInfo, String authPKInfoSign, String updateIndexInfo, String updateIndexSignature) {
            this.authPKInfo = authPKInfo;
            this.authPKInfoSign = authPKInfoSign;
            this.updateIndexInfo = updateIndexInfo;
            this.updateIndexSignature = updateIndexSignature;
        }
    }

    public static class UnregData {
        long userID;

        public UnregData(long userID) {
            this.userID = userID;
        }
    }

    public static class UpdateData {
        String updateIndexInfo;
        String updateIndexSignature;
        int updateResult;

        public UpdateData(int updateResult, String updateIndexInfo, String updateIndexSignature) {
            this.updateResult = updateResult;
            this.updateIndexInfo = updateIndexInfo;
            this.updateIndexSignature = updateIndexSignature;
        }
    }

    public static class UpdateRequestData {
        long userID;

        public UpdateRequestData(long userID) {
            this.userID = userID;
        }
    }

    private TcisLifeCycleDispatcher() {
    }

    public static TcisLifeCycleDispatcher getInstance() {
        if (sInstance == null) {
            synchronized (TcisLifeCycleDispatcher.class) {
                if (sInstance == null) {
                    sInstance = new TcisLifeCycleDispatcher();
                }
            }
        }
        return sInstance;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    public Context getContext() {
        return this.mContext;
    }

    public static boolean isReadyForIot(long targetUserId) {
        return isReadyForIot ? checkUserIDLogined(targetUserId) : false;
    }

    public static void setIotEnable(boolean ready) {
        isReadyForIot = ready;
    }

    public static void setIotEnableByResultCode(int errorCode) {
        if (errorCode == TCIS_Result.SUCCESS.value()) {
            setIotEnable(true);
        } else {
            setIotEnable(false);
        }
    }

    public static void setIotEnableByResultCodeReverse(int errorCode) {
        if (errorCode == TCIS_Result.SUCCESS.value()) {
            setIotEnable(false);
        } else {
            setIotEnable(true);
        }
    }

    public Bundle getTcisInfo() {
        return LifeCycleProcessor.getTcisInfo();
    }

    public int getCurrentState() {
        return this.stateMachine.getCurrentLifeState();
    }

    public void switchUser(int newUserHandle) {
        this.stateMachine.switchUser(newUserHandle);
    }

    public void initTcisOfCurrentUser() {
        initTcisByUserHandle(Utils.getCurrentUserId());
    }

    public void initTcisByUserHandle(int userHandle) {
        switchUser(userHandle);
    }

    public static boolean checkUserIDLogined(long userID) {
        return LifeCycleProcessor.getLoginedUserID() == userID;
    }

    public static long getLoginedUserID() {
        return LifeCycleProcessor.getLoginedUserID();
    }

    public void loginServerRequest(long userID, int serverRegisterStatus, String sessionID) {
        LogHelper.i(TAG, "serverRegisterStatus: " + (serverRegisterStatus == 0 ? "not registered" : "registered"));
        this.stateMachine.checkRegisterStatus(new LoginServerRequestData(userID, serverRegisterStatus, sessionID));
    }

    public void finalRegister(String authPKInfo, String authPKInfoSign, String updateIndexInfo, String updateIndexSignature) {
        this.stateMachine.getRegData(new RegisterData(authPKInfo, authPKInfoSign, updateIndexInfo, updateIndexSignature));
    }

    public void updateServerRequest(long userID) {
        this.stateMachine.checkLoginStatus(new UpdateRequestData(userID));
    }

    public void finalLogin(int updateResult, String updateIndexInfo, String updateIndexSignature) {
        LogHelper.d(TAG, "updateResult: " + (updateResult == 0 ? "no need update" : "need update"));
        this.stateMachine.getUpdateData(new UpdateData(updateResult, updateIndexInfo, updateIndexSignature));
    }

    public void cancelRegOrLogin(long userID) {
        this.stateMachine.cancelRegOrLogin(new CancelData(userID));
    }

    public void logout(long userID) {
        this.stateMachine.logout(new LogoutData(userID));
    }

    public void unregister(long userID) {
        this.stateMachine.delAccount(new UnregData(userID));
    }
}
