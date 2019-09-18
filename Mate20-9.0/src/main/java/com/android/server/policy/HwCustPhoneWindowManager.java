package com.android.server.policy;

import android.content.Context;
import android.os.PowerManager;
import android.view.KeyEvent;
import com.android.server.policy.WindowManagerPolicy;

public class HwCustPhoneWindowManager {
    static final String TAG = "HwCustPhoneWindowManager";

    public void dueKEYCODE_SOUNDTRIGGER_EVENT(int keyCode, Context context, boolean isMusicOrFMActive, boolean down, boolean keyguardShow) {
    }

    public void processCustInterceptKey(int keyCode, boolean down, Context context) {
    }

    public boolean isSosAllowed() {
        return true;
    }

    public void volumnkeyWakeup(Context mContext, boolean isScreenOn, PowerManager mPowerManager) {
    }

    public boolean isVolumnkeyWakeup(Context mContext) {
        return false;
    }

    public boolean interceptVolumeUpKey(KeyEvent event, Context context, boolean isScreenOn, boolean keyguardActive, boolean isMusicOrFMOrVoiceCallActive, boolean isInjected, boolean down) {
        return false;
    }

    public boolean disableHomeKey(Context context) {
        return false;
    }

    public int updateSystemBarsLw(Context context, WindowManagerPolicy.WindowState focusedWindow, int vis) {
        return vis;
    }
}
