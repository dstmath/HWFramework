package com.android.server.security.trustcircle.lifecycle;

import android.os.Message;
import com.android.internal.util.IState;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.security.trustcircle.lifecycle.TcisLifeCycleDispatcher;
import com.android.server.security.trustcircle.utils.LogHelper;
import com.android.server.security.trustcircle.utils.Status;

public class LifeCycleStateMachine extends StateMachine {
    public static final int MSG_CANCEL = 5;
    public static final int MSG_CHECK_LOGIN_STATUS = 2;
    public static final int MSG_CHECK_REG_STATUS = 1;
    public static final int MSG_DELETE_ACCOUNT = 7;
    public static final int MSG_FINAL_REG = 3;
    public static final int MSG_LOGOUT = 6;
    public static final int MSG_SWITCH_USER = 9;
    public static final int MSG_TIME_OUT = 8;
    public static final int MSG_UPDATE = 4;
    /* access modifiers changed from: private */
    public static final String TAG = LifeCycleStateMachine.class.getSimpleName();
    public static final int TIME_OUT_TIME = 10000;
    /* access modifiers changed from: private */
    public State mIDLEState = new IDLEState();
    private State mLoginedState = new LoginedState();
    private State mLoginingState = new LoginingState();
    private State mRegisteringState = new RegisteringState();
    private State mSameUserState = new SameUserState();
    /* access modifiers changed from: private */
    public State mSwitchingUserState = new SwitchingUserState();

    class IDLEState extends State {
        IDLEState() {
        }

        public void enter() {
            String access$000 = LifeCycleStateMachine.TAG;
            LogHelper.d(access$000, "enter " + getName());
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    LifeCycleStateMachine.this.checkRegStatus(msg);
                    return true;
                case 2:
                    LifeCycleStateMachine.this.checkLoginStatus(msg);
                    return true;
                case 6:
                    LifeCycleStateMachine.this.logout(msg);
                    return true;
                case 7:
                    LifeCycleStateMachine.this.unregister(msg);
                    return true;
                case 9:
                    return false;
                default:
                    String access$000 = LifeCycleStateMachine.TAG;
                    LogHelper.w(access$000, "error:unexpceted action in " + getName() + " : " + msg.what);
                    return true;
            }
        }

        public void exit() {
            String access$000 = LifeCycleStateMachine.TAG;
            LogHelper.d(access$000, "exit " + getName());
        }
    }

    class LoginedState extends State {
        LoginedState() {
        }

        public void enter() {
            String access$000 = LifeCycleStateMachine.TAG;
            LogHelper.d(access$000, "enter " + getName());
            LifeCycleStateMachine.this.removeMessages(8);
            LifeCycleProcessor.updateTime(System.currentTimeMillis());
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    LifeCycleStateMachine.this.checkRegStatus(msg);
                    return true;
                case 2:
                    LifeCycleStateMachine.this.checkLoginStatus(msg);
                    return true;
                case 6:
                    LifeCycleStateMachine.this.logout(msg);
                    return true;
                case 7:
                    LifeCycleStateMachine.this.unregister(msg);
                    return true;
                case 9:
                    return false;
                default:
                    String access$000 = LifeCycleStateMachine.TAG;
                    LogHelper.w(access$000, "error:unexpceted action in " + getName() + " : " + msg.what);
                    return true;
            }
        }

        public void exit() {
            String access$000 = LifeCycleStateMachine.TAG;
            LogHelper.d(access$000, "exit " + getName());
        }
    }

    class LoginingState extends State {
        LoginingState() {
        }

        public void enter() {
            String access$000 = LifeCycleStateMachine.TAG;
            LogHelper.d(access$000, "enter " + getName());
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 4:
                    LifeCycleStateMachine.this.removeMessages(8);
                    LifeCycleStateMachine.this.finalLogin(msg);
                    LifeCycleStateMachine.this.sendMessageDelayed(8, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
                    return true;
                case 5:
                    LifeCycleStateMachine.this.removeMessages(8);
                    LifeCycleStateMachine.this.cancel(msg, this);
                    return true;
                case 6:
                    LifeCycleStateMachine.this.removeMessages(8);
                    LifeCycleStateMachine.this.logout(msg);
                    return true;
                case 7:
                    LifeCycleStateMachine.this.removeMessages(8);
                    LifeCycleStateMachine.this.unregister(msg);
                    return true;
                case 8:
                    LifeCycleStateMachine.this.processTimeout();
                    LifeCycleProcessor.onFinalLoginResult(Status.TCIS_Result.TIMEOUT.value());
                    return true;
                case 9:
                    return false;
                default:
                    LogHelper.w(LifeCycleStateMachine.TAG, "error:LoginingState");
                    return true;
            }
        }

        public void exit() {
            String access$000 = LifeCycleStateMachine.TAG;
            LogHelper.d(access$000, "exit " + getName());
        }
    }

    class RegisteringState extends State {
        RegisteringState() {
        }

        public void enter() {
            String access$000 = LifeCycleStateMachine.TAG;
            LogHelper.d(access$000, "enter " + getName());
        }

        public boolean processMessage(Message msg) {
            int i = msg.what;
            if (i == 3) {
                LifeCycleStateMachine.this.removeMessages(8);
                LifeCycleStateMachine.this.finalRegister(msg);
                return true;
            } else if (i != 5) {
                switch (i) {
                    case 8:
                        LifeCycleStateMachine.this.processTimeout();
                        LifeCycleProcessor.onFinalRegisterResult(Status.TCIS_Result.TIMEOUT.value());
                        return true;
                    case 9:
                        return false;
                    default:
                        String access$000 = LifeCycleStateMachine.TAG;
                        LogHelper.w(access$000, "error:unexpceted action in " + getName() + " : " + msg.what);
                        return true;
                }
            } else {
                LifeCycleStateMachine.this.removeMessages(8);
                LifeCycleStateMachine.this.cancel(msg, this);
                return true;
            }
        }

        public void exit() {
            String access$000 = LifeCycleStateMachine.TAG;
            LogHelper.d(access$000, "exit " + getName());
        }
    }

    class SameUserState extends State {
        SameUserState() {
        }

        public void enter() {
            String access$000 = LifeCycleStateMachine.TAG;
            LogHelper.d(access$000, "enter " + getName());
        }

        public boolean processMessage(Message msg) {
            if (msg.what != 9) {
                String access$000 = LifeCycleStateMachine.TAG;
                LogHelper.w(access$000, "error:unexpceted action in " + getName() + " : " + msg.what);
                return true;
            }
            LifeCycleStateMachine.this.transitionTo(LifeCycleStateMachine.this.mSwitchingUserState);
            LifeCycleStateMachine.this.sendMessage(Message.obtain(msg));
            return true;
        }

        public void exit() {
            String access$000 = LifeCycleStateMachine.TAG;
            LogHelper.d(access$000, "exit " + getName());
        }
    }

    class SwitchingUserState extends State {
        SwitchingUserState() {
        }

        public void enter() {
            String access$000 = LifeCycleStateMachine.TAG;
            LogHelper.d(access$000, "enter " + getName());
            LifeCycleStateMachine.this.removeMessages(8);
            LifeCycleProcessor.updateTime(System.currentTimeMillis());
        }

        public boolean processMessage(Message msg) {
            if (msg.what != 9) {
                String access$000 = LifeCycleStateMachine.TAG;
                LogHelper.w(access$000, "error:unexpceted action in " + getName() + " : " + msg.what);
                LifeCycleStateMachine.this.transitionTo(LifeCycleStateMachine.this.mIDLEState);
                return true;
            }
            LifeCycleStateMachine.this.switchUser(msg);
            return true;
        }

        public void exit() {
            String access$000 = LifeCycleStateMachine.TAG;
            LogHelper.d(access$000, "exit " + getName());
        }
    }

    public enum TcisState {
        IDLE(IDLEState.class),
        REG_ING(RegisteringState.class),
        LOGIN_ING(LoginingState.class),
        LOGIN_ED(LoginedState.class);
        
        private Class clazz;

        private TcisState(Class clazz2) {
            this.clazz = clazz2;
        }

        public Class getStateClass() {
            return this.clazz;
        }
    }

    public LifeCycleStateMachine() {
        super(TAG);
        addState(this.mSwitchingUserState);
        addState(this.mSameUserState);
        addState(this.mIDLEState, this.mSameUserState);
        addState(this.mRegisteringState, this.mSameUserState);
        addState(this.mLoginingState, this.mSameUserState);
        addState(this.mLoginedState, this.mSameUserState);
        setInitialState(this.mSwitchingUserState);
        start();
    }

    public int getCurrentLifeState() {
        IState currentState = LifeCycleStateMachine.super.getCurrentState();
        if (currentState == null) {
            return TcisState.IDLE.ordinal();
        }
        for (TcisState state : TcisState.values()) {
            if (state.getStateClass() == currentState.getClass()) {
                return state.ordinal();
            }
        }
        LogHelper.e(TAG, "unknown current state : " + currentState.getName());
        return TcisState.IDLE.ordinal();
    }

    public void checkRegisterStatus(Object obj) {
        sendMessage(1, obj);
    }

    public void getRegData(Object obj) {
        sendMessage(3, obj);
    }

    public void cancelRegOrLogin(Object obj) {
        sendMessage(5, obj);
    }

    public void checkLoginStatus(Object obj) {
        sendMessage(2, obj);
    }

    public void getUpdateData(Object obj) {
        sendMessage(4, obj);
    }

    public void logout(Object obj) {
        sendMessage(6, obj);
    }

    public void delAccount(Object obj) {
        sendMessage(7, obj);
    }

    public void timeout(Object obj) {
        sendMessage(8, obj);
    }

    public void switchUser(int newUserHandle) {
        sendMessage(9, newUserHandle);
    }

    /* access modifiers changed from: private */
    public void checkRegStatus(Message msg) {
        if (msg == null || !(msg.obj instanceof TcisLifeCycleDispatcher.LoginServerRequestData)) {
            LogHelper.e(TAG, "checkRegStatus - message is invalid");
            return;
        }
        TcisLifeCycleDispatcher.LoginServerRequestData data = (TcisLifeCycleDispatcher.LoginServerRequestData) msg.obj;
        if (LifeCycleProcessor.checkNeedRegister(data.serverRegisterStatus, data.userID)) {
            LogHelper.i(TAG, "needs to register");
            if (LifeCycleProcessor.register(data.userID, data.sessionID)) {
                sendMessageDelayed(8, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
                transitionTo(this.mRegisteringState);
            }
        } else {
            LogHelper.i(TAG, "already registered, will login");
            if (LifeCycleProcessor.loginAndUpdate(data.userID)) {
                LogHelper.i(TAG, "login successfully");
                sendMessageDelayed(8, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
                transitionTo(this.mLoginingState);
            } else {
                LogHelper.w(TAG, "login failed");
                transitionTo(this.mIDLEState);
            }
        }
    }

    /* access modifiers changed from: private */
    public void finalRegister(Message msg) {
        if (msg == null || !(msg.obj instanceof TcisLifeCycleDispatcher.RegisterData)) {
            LogHelper.e(TAG, "finalRegister - message is invalid");
            return;
        }
        TcisLifeCycleDispatcher.RegisterData data = (TcisLifeCycleDispatcher.RegisterData) msg.obj;
        if (LifeCycleProcessor.finalRegister(data.authPKInfo, data.authPKInfoSign, data.updateIndexInfo, data.updateIndexSignature)) {
            LogHelper.i(TAG, "register totally successfully");
            transitionTo(this.mLoginedState);
        } else {
            LogHelper.w(TAG, "register totally failed");
            transitionTo(this.mIDLEState);
        }
    }

    /* access modifiers changed from: private */
    public void checkLoginStatus(Message msg) {
        if (msg == null || !(msg.obj instanceof TcisLifeCycleDispatcher.UpdateRequestData)) {
            LogHelper.e(TAG, "checkLoginStatus - message is invalid");
            return;
        }
        TcisLifeCycleDispatcher.UpdateRequestData updateReqData = (TcisLifeCycleDispatcher.UpdateRequestData) msg.obj;
        if (LifeCycleProcessor.checkUserIDLogined(updateReqData.userID)) {
            LogHelper.i(TAG, "already login, will update");
            if (LifeCycleProcessor.updateOnly(updateReqData.userID)) {
                LogHelper.i(TAG, "login successfully");
                sendMessageDelayed(8, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
                transitionTo(this.mLoginingState);
            } else {
                LogHelper.w(TAG, "login failed");
                transitionTo(this.mIDLEState);
            }
        } else {
            if (LifeCycleProcessor.checkTARegisterStatus(updateReqData.userID) == Status.LifeCycleStauts.NOT_REGISTER.ordinal()) {
                LifeCycleProcessor.onUpdateResponse(Status.TCIS_Result.NOT_REG.value(), -1, null);
                LogHelper.i(TAG, "needs to register");
            } else {
                LifeCycleProcessor.onUpdateResponse(Status.TCIS_Result.NOT_LOGIN.value(), -1, null);
                LogHelper.i(TAG, "needs to login");
            }
            transitionTo(this.mIDLEState);
        }
    }

    /* access modifiers changed from: private */
    public void finalLogin(Message msg) {
        if (msg == null || !(msg.obj instanceof TcisLifeCycleDispatcher.UpdateData)) {
            LogHelper.e(TAG, "finalLogin - message is invalid");
            return;
        }
        TcisLifeCycleDispatcher.UpdateData updateDate = (TcisLifeCycleDispatcher.UpdateData) msg.obj;
        if (LifeCycleProcessor.finalLogin(updateDate.updateResult, updateDate.updateIndexInfo, updateDate.updateIndexSignature)) {
            LogHelper.i(TAG, "finalLogin successfully");
            transitionTo(this.mLoginedState);
        } else {
            LogHelper.w(TAG, "finalLogin failed");
        }
    }

    /* access modifiers changed from: private */
    public void logout(Message msg) {
        if (msg == null || !(msg.obj instanceof TcisLifeCycleDispatcher.LogoutData)) {
            LogHelper.e(TAG, "logout - message is invalid");
            return;
        }
        if (LifeCycleProcessor.logout(((TcisLifeCycleDispatcher.LogoutData) msg.obj).userID)) {
            LifeCycleProcessor.updateTime(System.currentTimeMillis());
            LogHelper.i(TAG, "logout successfully");
            transitionTo(this.mIDLEState);
        } else {
            LogHelper.w(TAG, "logout failed");
        }
    }

    /* access modifiers changed from: private */
    public void unregister(Message msg) {
        if (msg == null || !(msg.obj instanceof TcisLifeCycleDispatcher.UnregData)) {
            LogHelper.e(TAG, "unregister - message is invalid");
            return;
        }
        if (LifeCycleProcessor.unregister(((TcisLifeCycleDispatcher.UnregData) msg.obj).userID)) {
            LogHelper.i(TAG, "unregister successfully");
            transitionTo(this.mIDLEState);
        } else {
            LogHelper.i(TAG, "unregister failed");
        }
    }

    /* access modifiers changed from: private */
    public void cancel(Message msg, State state) {
        if (msg == null || !(msg.obj instanceof TcisLifeCycleDispatcher.CancelData)) {
            LogHelper.e(TAG, "cancel - message is invalid");
            return;
        }
        if (LifeCycleProcessor.cancelRegOrLogin(state, ((TcisLifeCycleDispatcher.CancelData) msg.obj).userID)) {
            LogHelper.i(TAG, "cancel login successfully");
        } else {
            LogHelper.w(TAG, "cancel login failed");
        }
        transitionTo(this.mIDLEState);
    }

    /* access modifiers changed from: private */
    public void processTimeout() {
        String str = TAG;
        LogHelper.w(str, "timeout in " + getName());
        transitionTo(this.mIDLEState);
    }

    /* access modifiers changed from: private */
    public void switchUser(Message msg) {
        if (msg == null) {
            LogHelper.e(TAG, "switchUser - message is null");
            return;
        }
        int newUserHandle = msg.arg1;
        if (LifeCycleProcessor.initAndroidUser(newUserHandle) != -1) {
            String str = TAG;
            LogHelper.i(str, "load new tics user from user " + newUserHandle);
            transitionTo(this.mLoginedState);
        } else {
            String str2 = TAG;
            LogHelper.w(str2, "no tcis user logined in user " + newUserHandle);
            transitionTo(this.mIDLEState);
        }
    }
}
