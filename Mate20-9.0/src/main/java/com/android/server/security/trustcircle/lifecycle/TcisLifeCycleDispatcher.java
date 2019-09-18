package com.android.server.security.trustcircle.lifecycle;

import android.content.Context;
import android.os.Bundle;
import com.android.server.security.trustcircle.utils.LogHelper;
import com.android.server.security.trustcircle.utils.Status;
import com.android.server.security.trustcircle.utils.Utils;

public class TcisLifeCycleDispatcher {
    private static final String TAG = TcisLifeCycleDispatcher.class.getSimpleName();
    private static volatile boolean isReadyForIot;
    private static volatile TcisLifeCycleDispatcher sInstance;
    private Context mContext;
    private volatile LifeCycleStateMachine stateMachine = new LifeCycleStateMachine();

    public static class CancelData {
        long userID;

        public CancelData(long userID2) {
            this.userID = userID2;
        }
    }

    public static class LoginServerRequestData {
        int serverRegisterStatus;
        String sessionID;
        long userID;

        public LoginServerRequestData(long userID2, int serverRegisterStatus2, String sessionID2) {
            this.userID = userID2;
            this.serverRegisterStatus = serverRegisterStatus2;
            this.sessionID = sessionID2;
        }
    }

    public static class LogoutData {
        long userID;

        public LogoutData(long userID2) {
            this.userID = userID2;
        }
    }

    public static class RegisterData {
        String authPKInfo;
        String authPKInfoSign;
        String updateIndexInfo;
        String updateIndexSignature;

        public RegisterData(String authPKInfo2, String authPKInfoSign2, String updateIndexInfo2, String updateIndexSignature2) {
            this.authPKInfo = authPKInfo2;
            this.authPKInfoSign = authPKInfoSign2;
            this.updateIndexInfo = updateIndexInfo2;
            this.updateIndexSignature = updateIndexSignature2;
        }
    }

    public static class UnregData {
        long userID;

        public UnregData(long userID2) {
            this.userID = userID2;
        }
    }

    public static class UpdateData {
        String updateIndexInfo;
        String updateIndexSignature;
        int updateResult;

        public UpdateData(int updateResult2, String updateIndexInfo2, String updateIndexSignature2) {
            this.updateResult = updateResult2;
            this.updateIndexInfo = updateIndexInfo2;
            this.updateIndexSignature = updateIndexSignature2;
        }
    }

    public static class UpdateRequestData {
        long userID;

        public UpdateRequestData(long userID2) {
            this.userID = userID2;
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
        return checkUserIDLogined(targetUserId);
    }

    public static void setIotEnable(boolean ready) {
        isReadyForIot = ready;
    }

    public static void setIotEnableByResultCode(int errorCode) {
        if (errorCode == Status.TCIS_Result.SUCCESS.value()) {
            setIotEnable(true);
        } else {
            setIotEnable(false);
        }
    }

    public static void setIotEnableByResultCodeReverse(int errorCode) {
        if (errorCode == Status.TCIS_Result.SUCCESS.value()) {
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
        String str = TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("serverRegisterStatus: ");
        sb.append(serverRegisterStatus == 0 ? "not registered" : "registered");
        LogHelper.i(str, sb.toString());
        this.stateMachine.checkRegisterStatus(new LoginServerRequestData(userID, serverRegisterStatus, sessionID));
    }

    public void finalRegister(String authPKInfo, String authPKInfoSign, String updateIndexInfo, String updateIndexSignature) {
        this.stateMachine.getRegData(new RegisterData(authPKInfo, authPKInfoSign, updateIndexInfo, updateIndexSignature));
    }

    public void updateServerRequest(long userID) {
        this.stateMachine.checkLoginStatus((Object) new UpdateRequestData(userID));
    }

    public void finalLogin(int updateResult, String updateIndexInfo, String updateIndexSignature) {
        String str = TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("updateResult: ");
        sb.append(updateResult == 0 ? "no need update" : "need update");
        LogHelper.d(str, sb.toString());
        this.stateMachine.getUpdateData(new UpdateData(updateResult, updateIndexInfo, updateIndexSignature));
    }

    public void cancelRegOrLogin(long userID) {
        this.stateMachine.cancelRegOrLogin(new CancelData(userID));
    }

    public void logout(long userID) {
        this.stateMachine.logout((Object) new LogoutData(userID));
    }

    public void unregister(long userID) {
        this.stateMachine.delAccount(new UnregData(userID));
    }
}
