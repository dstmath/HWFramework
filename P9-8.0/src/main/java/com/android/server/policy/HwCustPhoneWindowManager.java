package com.android.server.policy;

import android.content.Context;
import android.os.PowerManager;
import android.view.KeyEvent;
import android.view.WindowManagerPolicy.WindowState;

public class HwCustPhoneWindowManager {
    static final String TAG = "HwCustPhoneWindowManager";

    public void dueKEYCODE_SOUNDTRIGGER_EVENT(int keyCode, Context context, boolean isMusicOrFMActive, boolean down, boolean keyguardShow) {
    }

    public int selectAnimationLw(int transit) {
        return 0;
    }

    public boolean isChargingAlbumSupported() {
        return false;
    }

    public void processCustInterceptKey(int keyCode, boolean down, Context context) {
    }

    public boolean isSosAllowed() {
        return true;
    }

    public void volumnkeyWakeup(Context mContext, boolean isScreenOn, PowerManager mPowerManager) {
    }

    public boolean isVolumnkeyWakeup() {
        return false;
    }

    public boolean interceptVolumeUpKey(KeyEvent event, Context context, boolean isScreenOn, boolean keyguardActive, boolean isMusicOrFMOrVoiceCallActive, boolean isInjected, boolean down) {
        return false;
    }

    public boolean disableHomeKey(Context context) {
        return false;
    }

    public int updateSystemBarsLw(Context context, WindowState focusedWindow, int vis) {
        return vis;
    }
}
