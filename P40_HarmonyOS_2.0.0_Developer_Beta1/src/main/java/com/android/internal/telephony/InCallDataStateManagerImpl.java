package com.android.internal.telephony;

import android.content.Context;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.PhoneExt;

public class InCallDataStateManagerImpl extends DefaultInCallDataStateManager {
    private static final String TAG = "InCallDataStateManager";
    private static InCallDataStateManagerImpl sInstance;
    private static final Object sLock = new Object();
    private InCallDataStateMachine mInCallDataStateMachine;

    private InCallDataStateManagerImpl() {
        RlogEx.i(TAG, "InCallDataStateManagerImpl default init");
    }

    private InCallDataStateManagerImpl(Context context, PhoneExt[] phoneExts) {
        RlogEx.i(TAG, "InCallDataStateManagerImpl constructor init");
        initInCallDataStateMachine(context, phoneExts);
    }

    private void initInCallDataStateMachine(Context context, PhoneExt[] phoneExts) {
        this.mInCallDataStateMachine = new InCallDataStateMachine(context, phoneExts);
        this.mInCallDataStateMachine.start();
    }

    public static InCallDataStateManagerImpl getInstance() {
        InCallDataStateManagerImpl inCallDataStateManagerImpl;
        synchronized (sLock) {
            if (sInstance == null) {
                sInstance = new InCallDataStateManagerImpl();
            }
            inCallDataStateManagerImpl = sInstance;
        }
        return inCallDataStateManagerImpl;
    }

    public void makeInCallDataStateManager(Context context, PhoneExt[] phoneExts) {
        synchronized (sLock) {
            if (sInstance == null) {
                sInstance = new InCallDataStateManagerImpl(context, phoneExts);
            } else {
                initInCallDataStateMachine(context, phoneExts);
            }
        }
    }

    public boolean isSlaveActive() {
        InCallDataStateMachine inCallDataStateMachine = this.mInCallDataStateMachine;
        if (inCallDataStateMachine == null) {
            return false;
        }
        return inCallDataStateMachine.isSlaveActive();
    }

    public boolean isSwitchingToSlave() {
        InCallDataStateMachine inCallDataStateMachine = this.mInCallDataStateMachine;
        if (inCallDataStateMachine == null) {
            return false;
        }
        return inCallDataStateMachine.isSwitchingToSlave();
    }

    public void registerImsCallStates(boolean enable, int phoneId) {
        InCallDataStateMachine inCallDataStateMachine = this.mInCallDataStateMachine;
        if (inCallDataStateMachine != null) {
            inCallDataStateMachine.registerImsCallStates(enable, phoneId);
        }
    }
}
