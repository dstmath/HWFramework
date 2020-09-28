package com.android.internal.policy;

import android.content.Context;
import android.view.View;
import android.view.ViewRootImpl;
import android.view.Window;
import com.android.internal.policy.HwPolicyFactory;

public class HwPolicyFactoryImpl implements HwPolicyFactory.Factory {
    private static final String TAG = "HwPolicyFactoryImpl";

    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.internal.policy.HwPhoneWindow, android.view.Window] */
    public Window getHwPhoneWindow(Context context) {
        return new HwPhoneWindow(context);
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.internal.policy.HwPhoneWindow, android.view.Window] */
    public Window getHwPhoneWindow(Context context, Window win, ViewRootImpl.ActivityConfigCallback activityConfigCallback) {
        return new HwPhoneWindow(context, win, activityConfigCallback);
    }

    public PhoneLayoutInflater getHwPhoneLayoutInflater(Context context) {
        return new HwPhoneLayoutInflater(context);
    }

    public View getHwNavigationBarColorView(Context context) {
        return new HwNavigationBarColorView(context);
    }
}
