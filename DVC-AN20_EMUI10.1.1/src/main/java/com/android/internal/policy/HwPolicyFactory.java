package com.android.internal.policy;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewRootImpl;
import android.view.Window;

public class HwPolicyFactory {
    private static final String TAG = "HwPolicyFactory";
    private static final Object mLock = new Object();
    private static volatile Factory obj = null;

    public interface Factory {
        View getHwNavigationBarColorView(Context context);

        PhoneLayoutInflater getHwPhoneLayoutInflater(Context context);

        Window getHwPhoneWindow(Context context);

        Window getHwPhoneWindow(Context context, Window window, ViewRootImpl.ActivityConfigCallback activityConfigCallback);
    }

    public static Window getHwPhoneWindow(Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwPhoneWindow(context);
        }
        return new PhoneWindow(context);
    }

    public static Window getHwPhoneWindow(Context context, Window win, ViewRootImpl.ActivityConfigCallback activityConfigCallback) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwPhoneWindow(context, win, activityConfigCallback);
        }
        return new PhoneWindow(context, win, activityConfigCallback);
    }

    public static PhoneLayoutInflater getHwPhoneLayoutInflater(Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwPhoneLayoutInflater(context);
        }
        return new PhoneLayoutInflater(context);
    }

    public static View getHwNavigationBarColorView(Context context) {
        Factory obj2 = getImplObject();
        return obj2 != null ? obj2.getHwNavigationBarColorView(context) : new View(context);
    }

    private static Factory getImplObject() {
        if (obj != null) {
            return obj;
        }
        synchronized (mLock) {
            try {
                obj = (Factory) Class.forName("com.android.internal.policy.HwPolicyFactoryImpl").newInstance();
            } catch (Exception e) {
                Log.e(TAG, ": reflection exception occurs");
            }
        }
        if (obj != null) {
            return obj;
        }
        Log.e(TAG, ": fail to get AllImpl object");
        return null;
    }
}
