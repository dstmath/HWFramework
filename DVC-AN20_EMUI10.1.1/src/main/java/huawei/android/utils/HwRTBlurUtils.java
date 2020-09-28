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
            switch (attr) {
                case 0:
                    params.setEnable(ahwext.getBoolean(attr, false));
                    break;
                case 1:
                    params.setRadius(ahwext.getInt(attr, 100));
                    break;
                case 2:
                    TypedValue value = ahwext.peekValue(attr);
                    if (value.type != 5) {
                        if (value.type == 16) {
                            params.setRoundx(ahwext.getInt(attr, 0));
                            break;
                        } else {
                            break;
                        }
                    } else {
                        params.setRoundx(ahwext.getDimensionPixelSize(attr, 0));
                        break;
                    }
                case 3:
                    TypedValue value2 = ahwext.peekValue(attr);
                    if (value2.type != 5) {
                        if (value2.type == 16) {
                            params.setRoundy(ahwext.getInt(attr, 0));
                            break;
                        } else {
                            break;
                        }
                    } else {
                        params.setRoundy(ahwext.getDimensionPixelSize(attr, 0));
                        break;
                    }
                case 4:
                    params.setAlpha(ahwext.getFloat(attr, 1.0f));
                    break;
                case 5:
                    TypedValue value3 = ahwext.peekValue(attr);
                    if (value3.type != 5) {
                        if (value3.type == 16) {
                            params.setBlankLeft(ahwext.getInt(attr, 0));
                            break;
                        } else {
                            break;
                        }
                    } else {
                        params.setBlankLeft(ahwext.getDimensionPixelSize(attr, 0));
                        break;
                    }
                case 6:
                    TypedValue value4 = ahwext.peekValue(attr);
                    if (value4.type != 5) {
                        if (value4.type == 16) {
                            params.setBlankTop(ahwext.getInt(attr, 0));
                            break;
                        } else {
                            break;
                        }
                    } else {
                        params.setBlankTop(ahwext.getDimensionPixelSize(attr, 0));
                        break;
                    }
                case 7:
                    TypedValue value5 = ahwext.peekValue(attr);
                    if (value5.type != 5) {
                        if (value5.type == 16) {
                            params.setBlankRight(ahwext.getInt(attr, 0));
                            break;
                        } else {
                            break;
                        }
                    } else {
                        params.setBlankRight(ahwext.getDimensionPixelSize(attr, 0));
                        break;
                    }
                case 8:
                    TypedValue value6 = ahwext.peekValue(attr);
                    if (value6.type != 5) {
                        if (value6.type == 16) {
                            params.setBlankBottom(ahwext.getInt(attr, 0));
                            break;
                        } else {
                            break;
                        }
                    } else {
                        params.setBlankBottom(ahwext.getDimensionPixelSize(attr, 0));
                        break;
                    }
                case 9:
                    if (ahwext.peekValue(attr).type != 16) {
                        params.setEnabledWindowBg(ahwext.getDrawable(attr));
                        break;
                    } else {
                        break;
                    }
                case 10:
                    if (ahwext.peekValue(attr).type != 16) {
                        params.setDisabledWindowBg(ahwext.getDrawable(attr));
                        break;
                    } else {
                        break;
                    }
            }
        }
        ahwext.recycle();
        if (SystemProperties.getBoolean(EMUI_LITE, false)) {
            params.enable = false;
        }
        return params;
    }

    public static void updateBlurStatus(WindowManager.LayoutParams lp, BlurParams blurParams) {
        if (lp != null && blurParams != null && blurParams.enable) {
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

        public BlurParams(boolean enable2, int radius2, int rx2, int ry2, float alpha2, int blankL2, int blankT2, int blankR2, int blankB2, Drawable enabledWindowBg2, Drawable disabledWindowBg2) {
            this.enable = enable2;
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

        public void setEnable(boolean enable2) {
            this.enable = enable2;
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
                return "enable with :" + this.radius + ", [" + this.blankL + ", " + this.blankT + ", " + this.blankR + ", " + this.blankB + "], [" + this.rx + ", " + this.ry + "], " + this.alpha + "f, " + this.enabledWindowBg;
            }
            return "disable with , " + this.disabledWindowBg;
        }
    }
}
