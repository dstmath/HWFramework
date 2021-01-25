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
    }

    public InCallDataStateManagerImpl(Context context, PhoneExt[] phoneExts) {
        RlogEx.i(TAG, "InCallDataStateManagerImpl constructor init");
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
            sInstance = new InCallDataStateManagerImpl(context, phoneExts);
        }
    }

    public boolean isSlaveActive() {
        if (this.mInCallDataStateMachine == null) {
            return false;
        }
        return this.mInCallDataStateMachine.isSlaveActive();
    }

    public boolean isSwitchingToSlave() {
        if (this.mInCallDataStateMachine == null) {
            return false;
        }
        return this.mInCallDataStateMachine.isSwitchingToSlave();
    }

    public void registerImsCallStates(boolean enable, int phoneId) {
        if (this.mInCallDataStateMachine != null) {
            this.mInCallDataStateMachine.registerImsCallStates(enable, phoneId);
        }
    }
}
