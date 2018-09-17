package com.android.internal.policy;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewRootImpl.ActivityConfigCallback;
import android.view.Window;

public class HwPolicyFactory {
    private static final String TAG = "HwPolicyFactory";
    private static final Object mLock = new Object();
    private static volatile Factory obj = null;

    public interface Factory {
        View getHwNavigationBarColorView(Context context);

        PhoneLayoutInflater getHwPhoneLayoutInflater(Context context);

        Window getHwPhoneWindow(Context context);

        Window getHwPhoneWindow(Context context, Window window, ActivityConfigCallback activityConfigCallback);
    }

    public static Window getHwPhoneWindow(Context context) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwPhoneWindow(context);
        }
        return new PhoneWindow(context);
    }

    public static Window getHwPhoneWindow(Context context, Window win, ActivityConfigCallback activityConfigCallback) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwPhoneWindow(context, win, activityConfigCallback);
        }
        return new PhoneWindow(context, win, activityConfigCallback);
    }

    public static PhoneLayoutInflater getHwPhoneLayoutInflater(Context context) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwPhoneLayoutInflater(context);
        }
        return new PhoneLayoutInflater(context);
    }

    public static View getHwNavigationBarColorView(Context context) {
        Factory obj = getImplObject();
        return obj != null ? obj.getHwNavigationBarColorView(context) : new View(context);
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
            Log.v(TAG, ": success to get AllImpl object and return....");
            return obj;
        }
        Log.e(TAG, ": fail to get AllImpl object");
        return null;
    }
}
