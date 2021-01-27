package com.huawei.android.os;

import android.content.Context;
import android.os.SystemVibrator;
import android.os.Vibrator;

public class SystemVibrateEx {
    private static final String TAG = SystemVibrateEx.class.getSimpleName();
    private Vibrator mVibrator;

    public SystemVibrateEx(Context context) {
        this.mVibrator = (SystemVibrator) ((Vibrator) context.getSystemService("vibrator"));
    }

    public void hwVibrate(int virbateMode) {
    }
}
