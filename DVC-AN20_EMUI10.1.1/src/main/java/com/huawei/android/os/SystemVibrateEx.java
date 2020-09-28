package com.huawei.android.os;

import android.content.Context;
import android.os.SystemVibrator;
import android.os.Vibrator;

public class SystemVibrateEx {
    private static final String TAG = SystemVibrateEx.class.getSimpleName();
    private Context mContext;
    private Vibrator mVibrator;

    public SystemVibrateEx(Context mContext2) {
        this.mVibrator = (SystemVibrator) ((Vibrator) mContext2.getSystemService("vibrator"));
    }

    public void hwVibrate(int virbateMode) {
    }
}
