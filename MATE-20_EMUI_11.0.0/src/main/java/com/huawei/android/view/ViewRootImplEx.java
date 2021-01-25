package com.huawei.android.view;

import android.view.View;
import android.view.ViewParent;
import android.view.ViewRootImpl;
import android.view.WindowManager;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class ViewRootImplEx {
    private ViewRootImpl viewRoot;

    public void setViewRootImpl(ViewRootImpl viewRootImpl) {
        this.viewRoot = viewRootImpl;
    }

    public boolean peekEvent() {
        return this.viewRoot.peekEvent();
    }

    @HwSystemApi
    public static boolean isViewRootImplNull(View view) {
        return view.getViewRootImpl() == null;
    }

    @HwSystemApi
    public static int getDetectedFlag(View view) {
        return view.getViewRootImpl().mDetectedFlag;
    }

    @HwSystemApi
    public static void setDetectedFlag(View view, int flag) {
        view.getViewRootImpl().mDetectedFlag = flag;
    }

    @HwSystemApi
    public static int getViewCount(View view) {
        return view.getViewRootImpl().mViewCount;
    }

    @HwSystemApi
    public static void increaseViewCount(View view) {
        view.getViewRootImpl().mViewCount++;
    }

    @HwSystemApi
    public static WindowManager.LayoutParams getWindowAttributes(View view) {
        return view.getViewRootImpl().mWindowAttributes;
    }

    public static boolean isViewRootImpl(ViewParent vp) {
        if (vp instanceof ViewRootImpl) {
            return true;
        }
        return false;
    }
}
