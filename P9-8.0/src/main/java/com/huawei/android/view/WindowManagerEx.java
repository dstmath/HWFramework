package com.huawei.android.view;

import android.view.WindowManager.LayoutParams;
import com.huawei.android.app.AppOpsManagerEx;

public class WindowManagerEx {

    public static class LayoutParamsEx {
        public static final int FLAG_DESTORY_SURFACE = 2;
        LayoutParams attrs;

        public LayoutParamsEx(LayoutParams lp) {
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
            return AppOpsManagerEx.TYPE_NET;
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
    }
}
