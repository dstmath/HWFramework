package huawei.android.utils;

import android.content.Context;

public class HwEyeProtectionSpline {
    private static final float DEFAULT_BRIGHTNESS = 100.0f;
    private static final String TAG = "HwEyeProtectionSpline";
    protected Context mContext;
    protected boolean mIsEyeProtectionControlFlag = false;

    public HwEyeProtectionSpline(Context context) {
        this.mContext = context;
    }

    public boolean isEyeProtectionMode() {
        return false;
    }

    public float getEyeProtectionBrightnessLevel(float lux) {
        return DEFAULT_BRIGHTNESS;
    }

    public void setEyeProtectionControlFlag(boolean isEyeProtectionControlFlag) {
        this.mIsEyeProtectionControlFlag = isEyeProtectionControlFlag;
    }

    public boolean getEyeProtectionControlFlag() {
        return this.mIsEyeProtectionControlFlag;
    }
}
