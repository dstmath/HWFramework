package com.android.internal.telephony;

import android.content.Context;
import com.huawei.internal.telephony.PhoneExt;

public class DefaultInCallDataStateManager {
    private static DefaultInCallDataStateManager sInstance = new DefaultInCallDataStateManager();

    public static DefaultInCallDataStateManager getInstance() {
        return sInstance;
    }

    public void makeInCallDataStateManager(Context context, PhoneExt[] phoneExts) {
    }

    public boolean isSlaveActive() {
        return false;
    }

    public boolean isSwitchingToSlave() {
        return false;
    }

    public void registerImsCallStates(boolean enable, int phoneId) {
    }
}
