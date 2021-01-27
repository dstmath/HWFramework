package com.android.server.policy;

import android.content.Context;
import android.os.PowerManager;
import android.view.KeyEvent;
import com.android.server.policy.WindowManagerPolicy;

public class HwCustPhoneWindowManager {
    static final String TAG = "HwCustPhoneWindowManager";

    public void dueKEYCODE_SOUNDTRIGGER_EVENT(int keyCode, Context context, boolean isMusicOrFmActive, boolean isDown, boolean isKeyguardShow) {
    }

    public void processCustInterceptKey(int keyCode, boolean isDown, Context context) {
    }

    public boolean isSosAllowed() {
        return true;
    }

    public void volumnkeyWakeup(Context context, boolean isScreenOn, PowerManager powerManager) {
    }

    public boolean isVolumnkeyWakeup(Context context) {
        return false;
    }

    public boolean interceptVolumeUpKey(KeyEvent event, Context context, boolean isScreenOn, boolean isKeyguardActive, boolean isMusicOrFmOrVoiceCallActive, boolean isInjected, boolean isDown) {
        return false;
    }

    public boolean disableHomeKey(Context context) {
        return false;
    }

    public int updateSystemBarsLw(Context context, WindowManagerPolicy.WindowState focusedWindow, int vis) {
        return vis;
    }
}
