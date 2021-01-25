package com.huawei.android.app;

import android.content.Context;
import huawei.android.app.HwKeyguardManagerImpl;

public class KeyguardManagerEx {
    private static KeyguardManagerEx mInstance;
    private Context mContext;

    public static synchronized KeyguardManagerEx getDefault(Context context) {
        KeyguardManagerEx keyguardManagerEx;
        synchronized (KeyguardManagerEx.class) {
            if (mInstance == null) {
                mInstance = new KeyguardManagerEx(context);
            }
            keyguardManagerEx = mInstance;
        }
        return keyguardManagerEx;
    }

    private KeyguardManagerEx(Context context) {
        this.mContext = context;
    }

    public boolean isLockScreenDisabled() {
        return HwKeyguardManagerImpl.getDefault().isLockScreenDisabled(this.mContext);
    }
}
