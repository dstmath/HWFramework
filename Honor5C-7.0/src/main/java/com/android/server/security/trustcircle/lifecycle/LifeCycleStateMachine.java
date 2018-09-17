package com.android.server.security.trustcircle.lifecycle;

import android.os.Message;
import com.android.internal.util.IState;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.security.trustcircle.lifecycle.TcisLifeCycleDispatcher.LoginServerRequestData;
import com.android.server.security.trustcircle.lifecycle.TcisLifeCycleDispatcher.RegisterData;
import com.android.server.security.trustcircle.lifecycle.TcisLifeCycleDispatcher.UpdateData;
import com.android.server.security.trustcircle.tlv.core.TLVTreeInvokerWrapper.TCIS_Result;
import com.android.server.security.trustcircle.utils.LogHelper;
import huawei.android.security.ILifeCycleCallback;

public class LifeCycleStateMachine extends StateMachine {
    public static final int CANCEL = 4;
    public static final int CHECK_STATUS = 1;
    public static final int DELETE_ACCOUNT = 6;
    public static final int FINAL_REG = 2;
    public static final int LOGOUT = 5;
    private static final String TAG = null;
    public static final int TIME_OUT = 7;
    public static final int TIME_OUT_TIME = 10000;
    public static final int UPDATE = 3;
    private ILifeCycleCallback callback;
    private State mIDLEState;
    private State mLoginedState;
    private State mLoginingState;
    private State mRegisteringState;

    class IDLEState extends State {
        IDLEState() {
        }

        public void enter() {
            LogHelper.d(LifeCycleStateMachine.TAG, "enter " + getName());
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case LifeCycleStateMachine.CHECK_STATUS /*1*/:
                    LoginServerRequestData data = msg.obj;
                    if (LifeCycleProcessor.checkNeedRegister(data.serverRegisterStatus, data.userID)) {
                        LogHelper.d(LifeCycleStateMachine.TAG, "userID: " + data.userID + " needs to register");
                        if (LifeCycleProcessor.register(LifeCycleStateMachine.this.callback, data.userID, data.sessionID)) {
                            LifeCycleStateMachine.this.sendMessageDelayed(LifeCycleStateMachine.TIME_OUT, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
                            LifeCycleStateMachine.this.transitionTo(LifeCycleStateMachine.this.mRegisteringState);
                        }
                    } else {
                        LogHelper.d(LifeCycleStateMachine.TAG, "userID: " + data.userID + " already registered, will login");
                        if (LifeCycleProcessor.loginAndUpdate(LifeCycleStateMachine.this.callback, data.userID)) {
                            LifeCycleStateMachine.this.sendMessageDelayed(LifeCycleStateMachine.TIME_OUT, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
                            LifeCycleStateMachine.this.transitionTo(LifeCycleStateMachine.this.mLoginingState);
                        }
                    }
                    return true;
                case LifeCycleStateMachine.LOGOUT /*5*/:
                    if (LifeCycleProcessor.logout(LifeCycleStateMachine.this.callback, msg.obj.userID)) {
                        LogHelper.d(LifeCycleStateMachine.TAG, "logout successfully");
                    } else {
                        LogHelper.d(LifeCycleStateMachine.TAG, "logout failed");
                    }
                    return true;
                case LifeCycleStateMachine.DELETE_ACCOUNT /*6*/:
                    if (LifeCycleProcessor.unregister(LifeCycleStateMachine.this.callback, msg.obj.userID)) {
                        LogHelper.d(LifeCycleStateMachine.TAG, "unregister successfully");
                    } else {
                        LogHelper.d(LifeCycleStateMachine.TAG, "unregister failed");
                    }
                    return true;
                default:
                    LogHelper.w(LifeCycleStateMachine.TAG, "error:unexpceted action in " + getName() + " : " + msg.what);
                    return true;
            }
        }

        public void exit() {
            LogHelper.d(LifeCycleStateMachine.TAG, "exit " + getName());
        }
    }

    class LoginedState extends State {
        LoginedState() {
        }

        public void enter() {
            LogHelper.d(LifeCycleStateMachine.TAG, "enter " + getName());
            LifeCycleStateMachine.this.removeMessages(LifeCycleStateMachine.TIME_OUT);
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case LifeCycleStateMachine.CHECK_STATUS /*1*/:
                    LoginServerRequestData data = msg.obj;
                    if (LifeCycleProcessor.checkNeedRegister(data.serverRegisterStatus, data.userID)) {
                        LogHelper.d(LifeCycleStateMachine.TAG, "userID: " + data.userID + " needs to register");
                        if (LifeCycleProcessor.register(LifeCycleStateMachine.this.callback, data.userID, data.sessionID)) {
                            LifeCycleStateMachine.this.sendMessageDelayed(LifeCycleStateMachine.TIME_OUT, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
                            LifeCycleStateMachine.this.transitionTo(LifeCycleStateMachine.this.mRegisteringState);
                        }
                    } else {
                        LogHelper.d(LifeCycleStateMachine.TAG, "userID: " + data.userID + " already registered, will login");
                        if (LifeCycleProcessor.loginAndUpdate(LifeCycleStateMachine.this.callback, data.userID)) {
                            LifeCycleStateMachine.this.sendMessageDelayed(LifeCycleStateMachine.TIME_OUT, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
                            LifeCycleStateMachine.this.transitionTo(LifeCycleStateMachine.this.mLoginingState);
                        }
                    }
                    return true;
                case LifeCycleStateMachine.LOGOUT /*5*/:
                    if (LifeCycleProcessor.logout(LifeCycleStateMachine.this.callback, msg.obj.userID)) {
                        LogHelper.d(LifeCycleStateMachine.TAG, "logout successfully");
                        LifeCycleStateMachine.this.transitionTo(LifeCycleStateMachine.this.mIDLEState);
                    } else {
                        LogHelper.d(LifeCycleStateMachine.TAG, "logout failed");
                    }
                    return true;
                case LifeCycleStateMachine.DELETE_ACCOUNT /*6*/:
                    if (LifeCycleProcessor.unregister(LifeCycleStateMachine.this.callback, msg.obj.userID)) {
                        LogHelper.d(LifeCycleStateMachine.TAG, "unregister successfully");
                        LifeCycleStateMachine.this.transitionTo(LifeCycleStateMachine.this.mIDLEState);
                    } else {
                        LogHelper.d(LifeCycleStateMachine.TAG, "unregister failed");
                    }
                    return true;
                default:
                    LogHelper.w(LifeCycleStateMachine.TAG, "error:unexpceted action in LoginedState : " + msg.what);
                    return true;
            }
        }

        public void exit() {
            LogHelper.d(LifeCycleStateMachine.TAG, "exit " + getName());
        }
    }

    class LoginingState extends State {
        LoginingState() {
        }

        public void enter() {
            LogHelper.d(LifeCycleStateMachine.TAG, "enter " + getName());
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case LifeCycleStateMachine.UPDATE /*3*/:
                    LifeCycleStateMachine.this.removeMessages(LifeCycleStateMachine.TIME_OUT);
                    UpdateData updateDate = msg.obj;
                    if (LifeCycleProcessor.finalLogin(LifeCycleStateMachine.this.callback, updateDate.updateResult, updateDate.updateIndexInfo, updateDate.updateIndexSignature)) {
                        LifeCycleStateMachine.this.transitionTo(LifeCycleStateMachine.this.mLoginedState);
                    }
                    LifeCycleStateMachine.this.sendMessageDelayed(LifeCycleStateMachine.TIME_OUT, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
                    return true;
                case LifeCycleStateMachine.CANCEL /*4*/:
                    LifeCycleStateMachine.this.removeMessages(LifeCycleStateMachine.TIME_OUT);
                    if (LifeCycleProcessor.cancelRegOrLogin(this, LifeCycleStateMachine.this.callback, msg.obj.userID)) {
                        LogHelper.d(LifeCycleStateMachine.TAG, "cancel login successfully");
                    } else {
                        LogHelper.d(LifeCycleStateMachine.TAG, "cancel login failed");
                    }
                    LifeCycleStateMachine.this.transitionTo(LifeCycleStateMachine.this.mIDLEState);
                    return true;
                case LifeCycleStateMachine.LOGOUT /*5*/:
                    LifeCycleStateMachine.this.removeMessages(LifeCycleStateMachine.TIME_OUT);
                    if (LifeCycleProcessor.logout(LifeCycleStateMachine.this.callback, msg.obj.userID)) {
                        LogHelper.d(LifeCycleStateMachine.TAG, "logout successfully");
                        LifeCycleStateMachine.this.transitionTo(LifeCycleStateMachine.this.mIDLEState);
                    } else {
                        LogHelper.d(LifeCycleStateMachine.TAG, "logout failed");
                    }
                    return true;
                case LifeCycleStateMachine.DELETE_ACCOUNT /*6*/:
                    LifeCycleStateMachine.this.removeMessages(LifeCycleStateMachine.TIME_OUT);
                    if (LifeCycleProcessor.unregister(LifeCycleStateMachine.this.callback, msg.obj.userID)) {
                        LogHelper.d(LifeCycleStateMachine.TAG, "unregister successfully");
                        LifeCycleStateMachine.this.transitionTo(LifeCycleStateMachine.this.mIDLEState);
                    } else {
                        LogHelper.d(LifeCycleStateMachine.TAG, "unregister failed");
                    }
                    return true;
                case LifeCycleStateMachine.TIME_OUT /*7*/:
                    LogHelper.w(LifeCycleStateMachine.TAG, "timeout in " + getName());
                    LifeCycleProcessor.onFinalLoginResult(LifeCycleStateMachine.this.callback, TCIS_Result.TIMEOUT.value());
                    LifeCycleStateMachine.this.transitionTo(LifeCycleStateMachine.this.mIDLEState);
                    return true;
                default:
                    LogHelper.w(LifeCycleStateMachine.TAG, "error:LoginingState");
                    LifeCycleProcessor.onFinalLoginResult(LifeCycleStateMachine.this.callback, TCIS_Result.BAD_ACCESS.value());
                    return true;
            }
        }

        public void exit() {
            LogHelper.d(LifeCycleStateMachine.TAG, "exit " + getName());
        }
    }

    class RegisteringState extends State {
        RegisteringState() {
        }

        public void enter() {
            LogHelper.d(LifeCycleStateMachine.TAG, "enter " + getName());
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case LifeCycleStateMachine.FINAL_REG /*2*/:
                    LifeCycleStateMachine.this.removeMessages(LifeCycleStateMachine.TIME_OUT);
                    RegisterData data = msg.obj;
                    if (LifeCycleProcessor.finalRegister(LifeCycleStateMachine.this.callback, data.authPKInfo, data.authPKInfoSign, data.updateIndexInfo, data.updateIndexSignature)) {
                        LifeCycleStateMachine.this.transitionTo(LifeCycleStateMachine.this.mLoginedState);
                    } else {
                        LifeCycleStateMachine.this.transitionTo(LifeCycleStateMachine.this.mIDLEState);
                    }
                    return true;
                case LifeCycleStateMachine.CANCEL /*4*/:
                    LifeCycleStateMachine.this.removeMessages(LifeCycleStateMachine.TIME_OUT);
                    if (LifeCycleProcessor.cancelRegOrLogin(this, LifeCycleStateMachine.this.callback, msg.obj.userID)) {
                        LogHelper.d(LifeCycleStateMachine.TAG, "cancel register successfully");
                    } else {
                        LogHelper.d(LifeCycleStateMachine.TAG, "cancel register failed");
                    }
                    LifeCycleStateMachine.this.transitionTo(LifeCycleStateMachine.this.mIDLEState);
                    return true;
                case LifeCycleStateMachine.TIME_OUT /*7*/:
                    LogHelper.w(LifeCycleStateMachine.TAG, "timeout in " + getName());
                    LifeCycleProcessor.onFinalRegisterResult(LifeCycleStateMachine.this.callback, TCIS_Result.TIMEOUT.value());
                    LifeCycleStateMachine.this.transitionTo(LifeCycleStateMachine.this.mIDLEState);
                    return true;
                default:
                    LifeCycleProcessor.onFinalRegisterResult(LifeCycleStateMachine.this.callback, TCIS_Result.BAD_ACCESS.value());
                    LogHelper.w(LifeCycleStateMachine.TAG, "error:unexpceted action in " + getName() + " : " + msg.what);
                    return true;
            }
        }

        public void exit() {
            LogHelper.d(LifeCycleStateMachine.TAG, "exit " + getName());
        }
    }

    public enum TcisState {
        ;
        
        private Class clazz;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.security.trustcircle.lifecycle.LifeCycleStateMachine.TcisState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.security.trustcircle.lifecycle.LifeCycleStateMachine.TcisState.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.security.trustcircle.lifecycle.LifeCycleStateMachine.TcisState.<clinit>():void");
        }

        private TcisState(Class clazz) {
            this.clazz = clazz;
        }

        public Class getStateClass() {
            return this.clazz;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.security.trustcircle.lifecycle.LifeCycleStateMachine.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.security.trustcircle.lifecycle.LifeCycleStateMachine.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.security.trustcircle.lifecycle.LifeCycleStateMachine.<clinit>():void");
    }

    public LifeCycleStateMachine() {
        super(TAG);
        this.callback = null;
        this.mIDLEState = new IDLEState();
        this.mRegisteringState = new RegisteringState();
        this.mLoginingState = new LoginingState();
        this.mLoginedState = new LoginedState();
        addState(this.mIDLEState);
        addState(this.mRegisteringState);
        addState(this.mLoginingState);
        addState(this.mLoginedState);
        setInitialState(this.mIDLEState);
        start();
    }

    public void setCallback(ILifeCycleCallback cb) {
        this.callback = cb;
    }

    public int getCurrentLifeState() {
        IState state = super.getCurrentState();
        if (state == null) {
            return TcisState.IDLE.ordinal();
        }
        TcisState[] values = TcisState.values();
        int length = values.length;
        for (int i = 0; i < length; i += CHECK_STATUS) {
            TcisState statz = values[i];
            if (statz.getStateClass() == state.getClass()) {
                return statz.ordinal();
            }
        }
        LogHelper.e(TAG, "unknown current state : " + state.getName());
        return TcisState.IDLE.ordinal();
    }

    public void checkStatus(Object obj) {
        sendMessage(CHECK_STATUS, obj);
    }

    public void getRegData(Object obj) {
        sendMessage(FINAL_REG, obj);
    }

    public void cancelRegOrLogin(Object obj) {
        sendMessage(CANCEL, obj);
    }

    public void getUpdateData(Object obj) {
        sendMessage(UPDATE, obj);
    }

    public void logout(Object obj) {
        sendMessage(LOGOUT, obj);
    }

    public void delAccount(Object obj) {
        sendMessage(DELETE_ACCOUNT, obj);
    }

    public void timeout(Object obj) {
        sendMessage(TIME_OUT, obj);
    }
}
