package com.huawei.android.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.AbsLayoutParams;
import android.view.View;
import android.view.ViewRootImpl;
import android.view.WindowInsets;
import android.view.WindowManager;
import com.huawei.android.app.PackageManagerEx;
import com.huawei.annotation.HwSystemApi;
import com.huawei.chr.RilConstS32Ex;

public class WindowManagerEx {
    @HwSystemApi
    public static final int TRANSIT_ACTIVITY_CLOSE = 7;
    @HwSystemApi
    public static final int TRANSIT_ACTIVITY_OPEN = 6;

    public static class LayoutParamsEx {
        public static final int BLUR_ALPHA_DYNAMIC_CHANGE = 8;
        public static final int BLUR_DYNAMIC_CHANGE = 1;
        public static final boolean BLUR_FEATURE_ENABLED = AbsLayoutParams.BLUR_FEATURE_ENABLED;
        public static final int BLUR_FULL_WINDOW = 4;
        public static final int BLUR_TWICE = 2;
        public static final int FLAG_ALWAYS_ALLOW_SHOW_DOCK_WINDOWS = 1024;
        public static final int FLAG_BLUR_BEHIND = 33554432;
        public static final int FLAG_BLUR_SELF = 67108864;
        public static final int FLAG_DESTORY_SURFACE = 2;
        public static final int FLAG_IGNORE_NAVIGATIONBAR_OR_STATURBAR = 256;
        public static final int FLAG_IGNORE_SNAPSHOT_IF_NOT_ONLY = 2048;
        public static final int FLAG_SEC_IME_RAISE = 32;
        public static final int FLAG_SHOW_HICAR_WINDOW = 128;
        @HwSystemApi
        public static final int INPUT_FEATURE_DISABLE_USER_ACTIVITY = 4;
        public static final int LAYOUT_IN_DISPLAY_SIDE_MODE_ALWAYS = 1;
        public static final int LAYOUT_IN_DISPLAY_SIDE_MODE_DEFAULT = 0;
        public static final int LAYOUT_IN_DISPLAY_SIDE_MODE_NEVER = 2;
        public static final int STYLE_BACKGROUND_LARGE_DARK = 106;
        public static final int STYLE_BACKGROUND_LARGE_LIGHT = 102;
        public static final int STYLE_BACKGROUND_MEDIUM_DARK = 105;
        public static final int STYLE_BACKGROUND_MEDIUM_LIGHT = 101;
        public static final int STYLE_BACKGROUND_SMALL_DARK = 104;
        public static final int STYLE_BACKGROUND_SMALL_LIGHT = 100;
        public static final int STYLE_BACKGROUND_XLARGE_DARK = 107;
        public static final int STYLE_BACKGROUND_XLARGE_LIGHT = 103;
        public static final int STYLE_CARD_DARK = 5;
        public static final int STYLE_CARD_DIM_BACK_DARK = 7;
        public static final int STYLE_CARD_DIM_BACK_LIGHT = 3;
        public static final int STYLE_CARD_LIGHT = 1;
        public static final int STYLE_CARD_THICK_DARK = 6;
        public static final int STYLE_CARD_THICK_LIGHT = 2;
        public static final int STYLE_CARD_THIN_DARK = 4;
        public static final int STYLE_CARD_THIN_LIGHT = 0;
        public static final int SYSTEM_FLAG_HIDE_NON_SYSTEM_OVERLAY_WINDOWS = 524288;
        public static final String TAG = "WindowManagerEx";
        public static final int TOP_ACTIVITY_NOTCH_STATE_UNKNOWN = 0;
        public static final int TOP_ACTIVITY_NOTCH_STATE_UNUSED = 1;
        public static final int TOP_ACTIVITY_NOTCH_STATE_USED = 2;
        @HwSystemApi
        public static final int TYPE_APPLICATION_ABOVE_SUB_PANEL = 1005;
        @HwSystemApi
        public static final int TYPE_APPLICATION_MEDIA_OVERLAY = 1004;
        @HwSystemApi
        public static final int TYPE_DOCK_DIVIDER = 2034;
        @HwSystemApi
        public static final int TYPE_DREAM = 2023;
        @HwSystemApi
        public static final int TYPE_KEYGUARD = 2004;
        public static final int TYPE_SINGLE_HAND_OVERLAY = 2040;
        WindowManager.LayoutParams attrs;

        public LayoutParamsEx(WindowManager.LayoutParams lp) {
            this.attrs = lp;
        }

        public static int getTypeNavigationBarPanel() {
            return 2024;
        }

        public static int getPrivateFlagShowForAllUsers() {
            return 16;
        }

        @HwSystemApi
        public WindowManager.LayoutParams getLayoutParams() {
            return this.attrs;
        }

        @HwSystemApi
        public void setLayoutParamsPrivateFlags(int newFlags) {
            this.attrs.privateFlags = newFlags;
        }

        public void setPrivateFlags(Context context, int newFlags) {
            if (PackageManagerEx.hasSystemSignaturePermission(context)) {
                this.attrs.privateFlags = newFlags;
                return;
            }
            throw new SecurityException("current application has no permission for operation:setPrivateFlags");
        }

        public int getPrivateFlags(Context context) {
            if (PackageManagerEx.hasSystemSignaturePermission(context)) {
                return this.attrs.privateFlags;
            }
            throw new SecurityException("current application has no permission for operation:getPrivateFlags");
        }

        public void addPrivateFlags(int privateFlags) {
            setPrivateFlags(privateFlags, privateFlags);
        }

        private void setPrivateFlags(int privateFlags, int mask) {
            WindowManager.LayoutParams layoutParams = this.attrs;
            layoutParams.privateFlags = (layoutParams.privateFlags & (~mask)) | (privateFlags & mask);
        }

        public static int getPrivateFlagHideNaviBar() {
            return Integer.MIN_VALUE;
        }

        public void setIsEmuiStyle(int emuiStyle) {
            this.attrs.isEmuiStyle = emuiStyle;
        }

        public void addHwFlags(int hwFlags) {
            setHwFlags(hwFlags, hwFlags);
        }

        private void setHwFlags(int hwFlags, int mask) {
            WindowManager.LayoutParams layoutParams = this.attrs;
            layoutParams.hwFlags = (layoutParams.hwFlags & (~mask)) | (hwFlags & mask);
        }

        public void clearHwFlags(int hwFlags) {
            setHwFlags(0, hwFlags);
        }

        public void setBlurStyle(int style) {
            WindowManager.LayoutParams layoutParams = this.attrs;
            layoutParams.hwBehindLayerBlurStyle = style;
            layoutParams.hwFrontLayerBlurStyle = style;
        }

        public void setBlurStyle(int behindLayerBlurStyle, int frontLayerBlurStyle) {
            WindowManager.LayoutParams layoutParams = this.attrs;
            layoutParams.hwBehindLayerBlurStyle = behindLayerBlurStyle;
            layoutParams.hwFrontLayerBlurStyle = frontLayerBlurStyle;
        }

        public static int getSecureShotFlag() {
            return 4096;
        }

        public static int getSecureCapFlag() {
            return 8192;
        }

        public int addInputFeatures(int inputFeatures) {
            this.attrs.inputFeatures |= inputFeatures;
            return this.attrs.inputFeatures;
        }

        public static int getPrivateFlagForceHardwareAccelerated() {
            return 2;
        }

        public static int getInputFetureNoInputChannel() {
            return 2;
        }

        public static int getLayoutInDisplayCutoutModeAlways() {
            return 1;
        }

        public static int getStatusBarSubPanelType() {
            return RilConstS32Ex.RIL_UNSOL_HW_RIL_CHR_IND;
        }

        public static int getScreenShotType() {
            return 2036;
        }

        public static void setDisplayCutoutModeAlways(WindowManager.LayoutParams lp) {
            lp.layoutInDisplayCutoutMode = 1;
        }

        public static int getNavigationBarType() {
            return 2019;
        }

        public void setDisplaySideMode(int mode) {
            if (mode == 0 || mode == 1 || mode == 2) {
                this.attrs.layoutInDisplaySideMode = mode;
            }
        }

        public static DisplaySideRegionEx getDisplaySideRegion(WindowInsets windowInsets) {
            if (windowInsets != null && windowInsets.getDisplaySideRegion() != null) {
                return new DisplaySideRegionEx(windowInsets.getDisplaySideRegion());
            }
            Log.e(TAG, "getDisplaySideRegion: windowInsets is null please check!");
            return null;
        }

        public static Rect getDisplaySafeInsets(WindowInsets windowInsets) {
            if (windowInsets == null) {
                Log.e(TAG, "getDisplaySafeInsets: windowInsets is null please check!");
                return null;
            }
            Rect side = new Rect();
            Rect cutout = new Rect();
            if (windowInsets.getDisplaySideRegion() != null) {
                side = windowInsets.getDisplaySideRegion().getSafeInsets();
            }
            if (windowInsets.getDisplayCutout() != null) {
                cutout = windowInsets.getDisplayCutout().getSafeInsets();
            }
            return new Rect(side.left > cutout.left ? side.left : cutout.left, side.top > cutout.top ? side.top : cutout.top, side.right > cutout.right ? side.right : cutout.right, side.bottom > cutout.bottom ? side.bottom : cutout.bottom);
        }

        @HwSystemApi
        public static int getTypeKeyguard() {
            return TYPE_KEYGUARD;
        }

        @HwSystemApi
        public static int getPrivateFlagKeyguard() {
            return 1024;
        }

        @HwSystemApi
        public static int getHwFlags(WindowManager.LayoutParams attr) {
            return attr.hwFlags;
        }

        @HwSystemApi
        public static int getPrivateFlags(WindowManager.LayoutParams attr) {
            return attr.privateFlags;
        }

        @HwSystemApi
        public static void setUserActivityTimeout(WindowManager.LayoutParams attr, int activTime) {
            attr.userActivityTimeout = (long) activTime;
        }
    }

    public static final int getTaskScreenshotFullscreenFlag() {
        return 1;
    }

    public static final int getTaskScreenshotSelectedRegionFlag() {
        return 2;
    }

    public static int getTopActivityAdaptNotchState(String packageName) {
        return HwWindowManager.getTopActivityAdaptNotchState(packageName);
    }

    public static void setBlurMode(View view, int blurMode) {
        if (view == null) {
            throw new IllegalArgumentException("view must not be null");
        } else if (getBlurFeatureEnabled()) {
            ViewRootImpl root = view.getViewRootImpl();
            if (root == null) {
                Log.e(LayoutParamsEx.TAG, "root view must not be null when setting blur mode");
            } else {
                root.setBlurMode(blurMode);
            }
        }
    }

    public static void setBlurEnabled(View view, boolean isEnabled) {
        if (view == null) {
            throw new IllegalArgumentException("view must not be null");
        } else if (getBlurFeatureEnabled()) {
            ViewRootImpl root = view.getViewRootImpl();
            if (root == null) {
                Log.e(LayoutParamsEx.TAG, "root view must not be null when setting blur enable");
            } else {
                root.setBlurEnabled(isEnabled);
            }
        }
    }

    public static void setBlurCacheEnabled(View view, boolean isEnabled) {
        if (view == null) {
            throw new IllegalArgumentException("view must not be null");
        } else if (getBlurFeatureEnabled()) {
            ViewRootImpl root = view.getViewRootImpl();
            if (root == null) {
                Log.e(LayoutParamsEx.TAG, "root view must not be null when setting blur cache enable");
            } else {
                root.setBlurCacheEnabled(isEnabled);
            }
        }
    }

    public static void setBlurProgress(View view, float progress) {
        if (view == null) {
            throw new IllegalArgumentException("view must not be null");
        } else if (getBlurFeatureEnabled()) {
            ViewRootImpl root = view.getViewRootImpl();
            if (root == null) {
                Log.e(LayoutParamsEx.TAG, "root view must not be null when setting blur progress");
            } else {
                root.setBlurProgress(progress);
            }
        }
    }

    public static boolean getBlurFeatureEnabled() {
        return LayoutParamsEx.BLUR_FEATURE_ENABLED;
    }
}
