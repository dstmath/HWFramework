package com.huawei.android.os;

import android.content.Context;
import android.os.SystemVibrator;
import android.os.Vibrator;
import android.util.Log;

public class SystemVibrateEx {
    private static final String TAG = SystemVibrateEx.class.getSimpleName();
    private Context mContext;
    private Vibrator mVibrator;

    public SystemVibrateEx(Context mContext) {
        this.mVibrator = (SystemVibrator) ((Vibrator) mContext.getSystemService("vibrator"));
    }

    public void hwVibrate(int virbateMode) {
        if (this.mVibrator != null) {
            this.mVibrator.hwVibrate(null, virbateMode);
            Log.d(TAG, "virbateMode is: " + virbateMode);
        }
    }
}
