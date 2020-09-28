package com.huawei.internal.policy;

import android.content.Context;
import android.graphics.Point;
import android.os.IBinder;
import android.view.View;
import android.view.WindowManager;
import com.android.internal.policy.PhoneWindow;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class PhoneWindowEx {
    private PhoneWindow mPhoneWindow;

    public void setPhoneWindow(PhoneWindow phoneWindow) {
        this.mPhoneWindow = phoneWindow;
    }

    @HwSystemApi
    public View getDecorView() {
        PhoneWindow phoneWindow = this.mPhoneWindow;
        if (phoneWindow != null) {
            return phoneWindow.getDecorView();
        }
        return null;
    }

    @HwSystemApi
    public IBinder getAppToken() {
        PhoneWindow phoneWindow = this.mPhoneWindow;
        if (phoneWindow != null) {
            return phoneWindow.getAppToken();
        }
        return null;
    }

    @HwSystemApi
    public WindowManager.LayoutParams getAttributes() {
        PhoneWindow phoneWindow = this.mPhoneWindow;
        if (phoneWindow != null) {
            return phoneWindow.getAttributes();
        }
        return null;
    }

    @HwSystemApi
    public Context getContext() {
        PhoneWindow phoneWindow = this.mPhoneWindow;
        if (phoneWindow != null) {
            return phoneWindow.getContext();
        }
        return null;
    }

    @HwSystemApi
    public final void dispatchOnWindowDismissed(boolean finishTask, boolean suppressWindowTransition) {
        PhoneWindow phoneWindow = this.mPhoneWindow;
        if (phoneWindow != null) {
            phoneWindow.dispatchOnWindowDismissed(finishTask, suppressWindowTransition);
        }
    }

    @HwSystemApi
    public void notifyRestrictedCaptionAreaCallback(int left, int top, int right, int bottom) {
        PhoneWindow phoneWindow = this.mPhoneWindow;
        if (phoneWindow != null) {
            phoneWindow.notifyRestrictedCaptionAreaCallback(left, top, right, bottom);
        }
    }

    @HwSystemApi
    public boolean isOverlayWithDecorCaptionEnabled() {
        PhoneWindow phoneWindow = this.mPhoneWindow;
        if (phoneWindow != null) {
            return phoneWindow.isOverlayWithDecorCaptionEnabled();
        }
        return false;
    }

    @HwSystemApi
    public Point getViewDecorViewOffset() {
        PhoneWindow phoneWindow = this.mPhoneWindow;
        if (phoneWindow != null) {
            return phoneWindow.getDecorView().getViewRootImpl().mOffset;
        }
        return null;
    }
}
