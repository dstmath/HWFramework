package huawei.android.util;

import android.content.Context;

public class HwCustEyeProtectionSpline {
    private static final float DEFAULT_BRIGHTNESS = 100.0f;
    private static final String TAG = "HwCustEyeProtectionSpline";
    protected Context mContext;

    public HwCustEyeProtectionSpline(Context context) {
        this.mContext = context;
    }

    public boolean IsEyeProtectionMode() {
        return false;
    }

    public float getEyeProtectionBrightnessLevel(float lux) {
        return DEFAULT_BRIGHTNESS;
    }
}
