package com.huawei.android.os;

import android.content.Context;
import android.os.Vibrator;
import android.util.Log;

public class SystemVibrateEx {
    private static final String TAG = SystemVibrateEx.class.getSimpleName();
    private Context mContext;
    private Vibrator mVibrator;

    public SystemVibrateEx(Context mContext2) {
        this.mVibrator = (Vibrator) mContext2.getSystemService("vibrator");
    }

    public void hwVibrate(int virbateMode) {
        if (this.mVibrator != null) {
            this.mVibrator.hwVibrate(null, virbateMode);
            String str = TAG;
            Log.d(str, "virbateMode is: " + virbateMode);
        }
    }
}
