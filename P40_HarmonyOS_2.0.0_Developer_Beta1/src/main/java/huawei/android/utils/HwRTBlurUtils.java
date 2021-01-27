package huawei.android.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import androidhwext.R;

public class HwRTBlurUtils {
    public static final boolean DEBUG = false;
    public static final String EMUI_LITE = "ro.build.hw_emui_lite.enable";
    public static final String TAG = "HwRTBlurUtils";

    public static BlurParams obtainBlurStyle(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, String name) {
        if (context == null) {
            Log.e(TAG, "check blur style for " + name + ", but context is null");
            return null;
        }
        BlurParams params = new BlurParams();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RealTimeBlur, defStyleAttr, defStyleRes);
        int indexCount = typedArray.getIndexCount();
        for (int i = 0; i < indexCount; i++) {
            int attr = typedArray.getIndex(i);
            switch (attr) {
                case 0:
                    params.setEnable(typedArray.getBoolean(attr, false));
                    break;
                case 1:
                    params.setRadius(typedArray.getInt(attr, 100));
                    break;
                case 2:
                    setRealTimeBlurRoundX(attr, params, typedArray);
                    break;
                case 3:
                    setRealTimeBlurRoundY(attr, params, typedArray);
                    break;
                case 4:
                    params.setAlpha(typedArray.getFloat(attr, 1.0f));
                    break;
                case 5:
                    setRealTimeBlurBlankLeft(attr, params, typedArray);
                    break;
                case 6:
                    setRealTimeBlurBlankTop(attr, params, typedArray);
                    break;
                case 7:
                    setRealTimeBlurBlankRight(attr, params, typedArray);
                    break;
                case 8:
                    setRealTimeBlurBlankBottom(attr, params, typedArray);
                    break;
                case 9:
                    if (typedArray.peekValue(attr).type != 16) {
                        params.setEnabledWindowBg(typedArray.getDrawable(attr));
                        break;
                    } else {
                        break;
                    }
                case 10:
                    if (typedArray.peekValue(attr).type != 16) {
                        params.setDisabledWindowBg(typedArray.getDrawable(attr));
                        break;
                    } else {
                        break;
                    }
            }
        }
        typedArray.recycle();
        if (SystemProperties.getBoolean(EMUI_LITE, false)) {
            params.enable = false;
        }
        return params;
    }

    private static void setRealTimeBlurRoundX(int attr, BlurParams params, TypedArray typedArray) {
        TypedValue value = typedArray.peekValue(attr);
        if (value.type == 5) {
            params.setRoundx(typedArray.getDimensionPixelSize(attr, 0));
        }
        if (value.type == 16) {
            params.setRoundx(typedArray.getInt(attr, 0));
        }
    }

    private static void setRealTimeBlurRoundY(int attr, BlurParams params, TypedArray typedArray) {
        TypedValue value = typedArray.peekValue(attr);
        if (value.type == 5) {
            params.setRoundy(typedArray.getDimensionPixelSize(attr, 0));
        }
        if (value.type == 16) {
            params.setRoundy(typedArray.getInt(attr, 0));
        }
    }

    private static void setRealTimeBlurBlankLeft(int attr, BlurParams params, TypedArray typedArray) {
        TypedValue value = typedArray.peekValue(attr);
        if (value.type == 5) {
            params.setBlankLeft(typedArray.getDimensionPixelSize(attr, 0));
        }
        if (value.type == 16) {
            params.setBlankLeft(typedArray.getInt(attr, 0));
        }
    }

    private static void setRealTimeBlurBlankTop(int attr, BlurParams params, TypedArray typedArray) {
        TypedValue value = typedArray.peekValue(attr);
        if (value.type == 5) {
            params.setBlankTop(typedArray.getDimensionPixelSize(attr, 0));
        }
        if (value.type == 16) {
            params.setBlankTop(typedArray.getInt(attr, 0));
        }
    }

    private static void setRealTimeBlurBlankRight(int attr, BlurParams params, TypedArray typedArray) {
        TypedValue value = typedArray.peekValue(attr);
        if (value.type == 5) {
            params.setBlankRight(typedArray.getDimensionPixelSize(attr, 0));
        }
        if (value.type == 16) {
            params.setBlankRight(typedArray.getInt(attr, 0));
        }
    }

    private static void setRealTimeBlurBlankBottom(int attr, BlurParams params, TypedArray typedArray) {
        TypedValue value = typedArray.peekValue(attr);
        if (value.type == 5) {
            params.setBlankBottom(typedArray.getDimensionPixelSize(attr, 0));
        }
        if (value.type == 16) {
            params.setBlankBottom(typedArray.getInt(attr, 0));
        }
    }

    public static void updateBlurStatus(WindowManager.LayoutParams layoutParams, BlurParams blurParams) {
    }

    public static void updateWindowBgForBlur(BlurParams blurParams, View decorView) {
        if (blurParams != null && decorView != null) {
            Drawable bg = blurParams.enable ? blurParams.enabledWindowBg : blurParams.disabledWindowBg;
            if (bg != null) {
                decorView.setBackground(bg);
            }
        }
    }

    public static class BlurParams {
        private static final float DEFAULT_ALPHA = 1.0f;
        private static final int DEFAULT_BLANKB = 0;
        private static final int DEFAULT_BLANKL = 0;
        private static final int DEFAULT_BLANKR = 0;
        private static final int DEFAULT_BLANKT = 0;
        private static final int DEFAULT_RADIUS = 100;
        private static final int DEFAULT_ROUNDX = 0;
        private static final int DEFAULT_ROUNDY = 0;
        public float alpha = 1.0f;
        public int blankB = 0;
        public int blankL = 0;
        public int blankR = 0;
        public int blankT = 0;
        public Drawable disabledWindowBg;
        public boolean enable;
        public Drawable enabledWindowBg;
        public int radius = 100;
        public int rx = 0;
        public int ry = 0;

        public BlurParams() {
        }

        public BlurParams(boolean isEnable, int radius2, int rx2, int ry2, float alpha2, int blankL2, int blankT2, int blankR2, int blankB2, Drawable enabledWindowBg2, Drawable disabledWindowBg2) {
            this.enable = isEnable;
            this.radius = radius2;
            this.rx = rx2;
            this.ry = ry2;
            this.alpha = alpha2;
            this.blankL = blankL2;
            this.blankT = blankT2;
            this.blankR = blankR2;
            this.blankB = blankB2;
            this.enabledWindowBg = enabledWindowBg2;
            this.disabledWindowBg = disabledWindowBg2;
        }

        public void setEnable(boolean isEnable) {
            this.enable = isEnable;
        }

        public void setRadius(int radius2) {
            this.radius = radius2;
        }

        public void setRoundx(int rx2) {
            this.rx = rx2;
        }

        public void setRoundy(int ry2) {
            this.ry = ry2;
        }

        public void setAlpha(float alpha2) {
            this.alpha = alpha2;
        }

        public void setBlankLeft(int left) {
            this.blankL = left;
        }

        public void setBlankTop(int top) {
            this.blankT = top;
        }

        public void setBlankRight(int right) {
            this.blankR = right;
        }

        public void setBlankBottom(int bottom) {
            this.blankB = bottom;
        }

        public void setEnabledWindowBg(Drawable drawable) {
            this.enabledWindowBg = drawable;
        }

        public void setDisabledWindowBg(Drawable drawable) {
            this.disabledWindowBg = drawable;
        }

        public boolean isEnable() {
            return this.enable;
        }

        public int getRadius() {
            return this.radius;
        }

        public int getRoundx() {
            return this.rx;
        }

        public int getRoundy() {
            return this.ry;
        }

        public float getAlpha() {
            return this.alpha;
        }

        public int getBlankLeft() {
            return this.blankL;
        }

        public int getBlankTop() {
            return this.blankT;
        }

        public int getBlankRight() {
            return this.blankR;
        }

        public int getBlankBottom() {
            return this.blankB;
        }

        public Drawable getEnabledWindowBg() {
            return this.enabledWindowBg;
        }

        public Drawable getDisabledWindowBg() {
            return this.disabledWindowBg;
        }

        public String toString() {
            if (this.enable) {
                return "enable with :" + this.radius + ", [" + this.blankL + ", " + this.blankT + ", " + this.blankR + ", " + this.blankB + "], [" + this.rx + ", " + this.ry + "], " + this.alpha + "f, " + this.enabledWindowBg;
            }
            return "disable with , " + this.disabledWindowBg;
        }
    }
}
