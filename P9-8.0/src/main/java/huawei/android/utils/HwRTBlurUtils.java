package huawei.android.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import androidhwext.R;

public class HwRTBlurUtils {
    public static final boolean DEBUG = true;
    public static final String EMUI_LITE = "ro.build.hw_emui_lite.enable";
    public static final String TAG = "HwRTBlurUtils";

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

        public BlurParams(boolean enable, int radius, int rx, int ry, float alpha, int blankL, int blankT, int blankR, int blankB, Drawable enabledWindowBg, Drawable disabledWindowBg) {
            this.enable = enable;
            this.radius = radius;
            this.rx = rx;
            this.ry = ry;
            this.alpha = alpha;
            this.blankL = blankL;
            this.blankT = blankT;
            this.blankR = blankR;
            this.blankB = blankB;
            this.enabledWindowBg = enabledWindowBg;
            this.disabledWindowBg = disabledWindowBg;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        public void setRadius(int radius) {
            this.radius = radius;
        }

        public void setRoundx(int rx) {
            this.rx = rx;
        }

        public void setRoundy(int ry) {
            this.ry = ry;
        }

        public void setAlpha(float alpha) {
            this.alpha = alpha;
        }

        public void setBlankLeft(int l) {
            this.blankL = l;
        }

        public void setBlankTop(int t) {
            this.blankT = t;
        }

        public void setBlankRight(int r) {
            this.blankR = r;
        }

        public void setBlankBottom(int b) {
            this.blankB = b;
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
                return "enable with :" + this.radius + ", [" + this.blankL + ", " + this.blankT + ", " + this.blankR + ", " + this.blankB + "], [" + this.rx + ", " + this.ry + "], " + this.alpha + "f" + ", " + this.enabledWindowBg;
            }
            return "disable with , " + this.disabledWindowBg;
        }
    }

    public static BlurParams obtainBlurStyle(Context context, AttributeSet set, int defStyleAttr, int defStyleRes, String name) {
        if (context == null) {
            Log.e(TAG, "check blur style for " + name + ", but context is null");
            return null;
        }
        BlurParams params = new BlurParams();
        TypedArray ahwext = context.obtainStyledAttributes(set, R.styleable.RealTimeBlur, defStyleAttr, defStyleRes);
        int Nhwext = ahwext.getIndexCount();
        for (int i = 0; i < Nhwext; i++) {
            int attr = ahwext.getIndex(i);
            TypedValue value;
            switch (attr) {
                case 0:
                    params.setEnable(ahwext.getBoolean(attr, false));
                    break;
                case 1:
                    params.setRadius(ahwext.getInt(attr, 100));
                    break;
                case 2:
                    value = ahwext.peekValue(attr);
                    if (value.type != 5) {
                        if (value.type != 16) {
                            break;
                        }
                        params.setRoundx(ahwext.getInt(attr, 0));
                        break;
                    }
                    params.setRoundx(ahwext.getDimensionPixelSize(attr, 0));
                    break;
                case 3:
                    value = ahwext.peekValue(attr);
                    if (value.type != 5) {
                        if (value.type != 16) {
                            break;
                        }
                        params.setRoundy(ahwext.getInt(attr, 0));
                        break;
                    }
                    params.setRoundy(ahwext.getDimensionPixelSize(attr, 0));
                    break;
                case 4:
                    params.setAlpha(ahwext.getFloat(attr, 1.0f));
                    break;
                case 5:
                    value = ahwext.peekValue(attr);
                    if (value.type != 5) {
                        if (value.type != 16) {
                            break;
                        }
                        params.setBlankLeft(ahwext.getInt(attr, 0));
                        break;
                    }
                    params.setBlankLeft(ahwext.getDimensionPixelSize(attr, 0));
                    break;
                case 6:
                    value = ahwext.peekValue(attr);
                    if (value.type != 5) {
                        if (value.type != 16) {
                            break;
                        }
                        params.setBlankTop(ahwext.getInt(attr, 0));
                        break;
                    }
                    params.setBlankTop(ahwext.getDimensionPixelSize(attr, 0));
                    break;
                case 7:
                    value = ahwext.peekValue(attr);
                    if (value.type != 5) {
                        if (value.type != 16) {
                            break;
                        }
                        params.setBlankRight(ahwext.getInt(attr, 0));
                        break;
                    }
                    params.setBlankRight(ahwext.getDimensionPixelSize(attr, 0));
                    break;
                case 8:
                    value = ahwext.peekValue(attr);
                    if (value.type != 5) {
                        if (value.type != 16) {
                            break;
                        }
                        params.setBlankBottom(ahwext.getInt(attr, 0));
                        break;
                    }
                    params.setBlankBottom(ahwext.getDimensionPixelSize(attr, 0));
                    break;
                case 9:
                    if (ahwext.peekValue(attr).type == 16) {
                        break;
                    }
                    params.setEnabledWindowBg(ahwext.getDrawable(attr));
                    break;
                case 10:
                    if (ahwext.peekValue(attr).type == 16) {
                        break;
                    }
                    params.setDisabledWindowBg(ahwext.getDrawable(attr));
                    break;
                default:
                    break;
            }
        }
        ahwext.recycle();
        if (SystemProperties.getBoolean(EMUI_LITE, false)) {
            params.enable = false;
        }
        Log.d(TAG, "check blur style for " + name + ", themeResId : " + String.format("0x%08x", new Object[]{Integer.valueOf(context.getThemeResId())}) + ", context : " + context + ", Nhwext : " + Nhwext + ", get Blur : " + params);
        return params;
    }

    public static void updateBlurStatus(LayoutParams lp, BlurParams blurParams) {
        if (lp != null && blurParams != null && (blurParams.enable ^ 1) == 0) {
            lp.flags |= 4;
            lp.blurRadius = blurParams.radius;
            lp.blurRoundx = blurParams.rx;
            lp.blurRoundy = blurParams.ry;
            lp.blurAlpha = blurParams.alpha;
            lp.blurBlankLeft = blurParams.blankL;
            lp.blurBlankTop = blurParams.blankT;
            lp.blurBlankRight = blurParams.blankR;
            lp.blurBlankBottom = blurParams.blankB;
        }
    }

    public static void updateWindowBgForBlur(BlurParams blurParams, View decorView) {
        if (blurParams != null && decorView != null) {
            Drawable bg = blurParams.enable ? blurParams.enabledWindowBg : blurParams.disabledWindowBg;
            if (bg != null) {
                decorView.setBackground(bg);
            }
        }
    }
}
