package com.huawei.android.view;

import android.graphics.Rect;
import android.util.Log;
import android.view.WindowInsets;
import android.view.WindowManager;
import com.huawei.annotation.HwSystemApi;

public class WindowManagerEx {

    public static class LayoutParamsEx {
        public static final int FLAG_DESTORY_SURFACE = 2;
        public static final int FLAG_IGNORE_NAVIGATIONBAR_OR_STATURBAR = 256;
        public static final int FLAG_SEC_IME_RAISE = 32;
        public static final int FLAG_SHOW_HICAR_WINDOW = 128;
        public static final int LAYOUT_IN_DISPLAY_SIDE_MODE_ALWAYS = 1;
        public static final int LAYOUT_IN_DISPLAY_SIDE_MODE_DEFAULT = 0;
        public static final int LAYOUT_IN_DISPLAY_SIDE_MODE_NEVER = 2;
        public static final String TAG = "WindowManagerEx";
        public static final int TOP_ACTIVITY_NOTCH_STATE_UNKNOWN = 0;
        public static final int TOP_ACTIVITY_NOTCH_STATE_UNUSED = 1;
        public static final int TOP_ACTIVITY_NOTCH_STATE_USED = 2;
        @HwSystemApi
        public static final int TYPE_APPLICATION_ABOVE_SUB_PANEL = 1005;
        @HwSystemApi
        public static final int TYPE_APPLICATION_MEDIA_OVERLAY = 1004;
        @HwSystemApi
        public static final int TYPE_KEYGUARD = 2004;
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
            return 2017;
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
}
