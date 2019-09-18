package com.huawei.android.view;

import android.view.WindowManager;

public class WindowManagerEx {

    public static class LayoutParamsEx {
        public static final int FLAG_DESTORY_SURFACE = 2;
        public static final int FLAG_SEC_IME_RAISE = 32;
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

        public void addPrivateFlags(int privateFlags) {
            setPrivateFlags(privateFlags, privateFlags);
        }

        private void setPrivateFlags(int privateFlags, int mask) {
            this.attrs.privateFlags = (this.attrs.privateFlags & (~mask)) | (privateFlags & mask);
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
            this.attrs.hwFlags = (this.attrs.hwFlags & (~mask)) | (hwFlags & mask);
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
            WindowManager.LayoutParams layoutParams = this.attrs;
            int i = this.attrs.inputFeatures | inputFeatures;
            layoutParams.inputFeatures = i;
            return i;
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
    }

    public static final int getTaskScreenshotFullscreenFlag() {
        return 1;
    }

    public static final int getTaskScreenshotSelectedRegionFlag() {
        return 2;
    }
}
