package com.android.internal.telephony;

import android.content.Context;
import android.util.Log;
import com.huawei.internal.telephony.PhoneExt;

public class InCallDataStateManager {
    private static final String TAG = "InCallDataStateManager";
    private static InCallDataStateManager sInstance;
    private DefaultInCallDataStateManager mInCallDataStateManager = HwPartOptTelephonyFactory.getTelephonyFactory().getFullnetworkFactory().getInCallDataStateManager();

    private InCallDataStateManager() {
        Log.d(TAG, "add " + this.mInCallDataStateManager.getClass().getCanonicalName() + " to memory");
    }

    public static InCallDataStateManager getInstance() {
        if (sInstance == null) {
            sInstance = new InCallDataStateManager();
        }
        return sInstance;
    }

    public void makeInCallDataStateManager(Context context, PhoneExt[] phoneExts) {
        this.mInCallDataStateManager.makeInCallDataStateManager(context, phoneExts);
    }

    public boolean isSlaveActive() {
        return this.mInCallDataStateManager.isSlaveActive();
    }

    public boolean isSwitchingToSlave() {
        return this.mInCallDataStateManager.isSwitchingToSlave();
    }

    public void registerImsCallStates(boolean enable, int phoneId) {
        this.mInCallDataStateManager.registerImsCallStates(enable, phoneId);
    }
}
