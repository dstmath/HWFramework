package com.android.server.security.trustcircle.lifecycle;

import android.os.Bundle;
import com.android.server.security.trustcircle.utils.LogHelper;
import huawei.android.security.ILifeCycleCallback;

public class TcisLifeCycleDispatcher {
    private static final String TAG = null;
    private static volatile TcisLifeCycleDispatcher sInstance;
    private static volatile LifeCycleStateMachine stateMachine;

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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.security.trustcircle.lifecycle.TcisLifeCycleDispatcher.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.security.trustcircle.lifecycle.TcisLifeCycleDispatcher.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.security.trustcircle.lifecycle.TcisLifeCycleDispatcher.<clinit>():void");
    }

    private TcisLifeCycleDispatcher() {
        stateMachine = new LifeCycleStateMachine();
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

    public Bundle getTcisInfo() {
        return LifeCycleProcessor.getTcisInfo();
    }

    public int getCurrentState() {
        return stateMachine.getCurrentLifeState();
    }

    public static boolean checkUserIDLogined(long userID) {
        return LifeCycleProcessor.getLoginedUserID() == userID;
    }

    public static long getLoginedUserID() {
        return LifeCycleProcessor.getLoginedUserID();
    }

    public void loginServerRequest(ILifeCycleCallback callback, long userID, int serverRegisterStatus, String sessionID) {
        stateMachine.setCallback(callback);
        LogHelper.d(TAG, "serverRegisterStatus: " + (serverRegisterStatus == 0 ? "not registered" : "registered"));
        stateMachine.checkStatus(new LoginServerRequestData(userID, serverRegisterStatus, sessionID));
    }

    public void finalRegister(ILifeCycleCallback callback, String authPKInfo, String authPKInfoSign, String updateIndexInfo, String updateIndexSignature) {
        stateMachine.setCallback(callback);
        stateMachine.getRegData(new RegisterData(authPKInfo, authPKInfoSign, updateIndexInfo, updateIndexSignature));
    }

    public void finalLogin(ILifeCycleCallback callback, int updateResult, String updateIndexInfo, String updateIndexSignature) {
        LogHelper.d(TAG, "updateResult: " + (updateResult == 0 ? "no need update" : "need update"));
        stateMachine.setCallback(callback);
        stateMachine.getUpdateData(new UpdateData(updateResult, updateIndexInfo, updateIndexSignature));
    }

    public void cancelRegOrLogin(ILifeCycleCallback callback, long userID) {
        stateMachine.setCallback(callback);
        stateMachine.cancelRegOrLogin(new CancelData(userID));
    }

    public void logout(ILifeCycleCallback callback, long userID) {
        stateMachine.setCallback(callback);
        stateMachine.logout(new LogoutData(userID));
    }

    public void unregister(ILifeCycleCallback callback, long userID) {
        stateMachine.setCallback(callback);
        stateMachine.delAccount(new UnregData(userID));
    }
}
