package com.android.ims;

import android.content.Context;
import android.util.Log;

public class HwImsFactory {
    private static final String TAG = "HwImsFactory";
    private static final Object mLock = new Object();
    private static volatile HwImsFactoryInterface sObj = null;

    /* access modifiers changed from: package-private */
    public interface HwImsFactoryInterface {
        IHwImsCallEx getHwImsCallEx(ImsCall imsCall, Context context);

        IHwImsUtEx getHwImsUtEx(IHwImsUtManager iHwImsUtManager, int i);
    }

    private static HwImsFactoryInterface getImplObject() {
        if (sObj != null) {
            return sObj;
        }
        synchronized (mLock) {
            try {
                sObj = (HwImsFactoryInterface) Class.forName("com.android.ims.HwImsFactoryImpl").newInstance();
            } catch (Exception e) {
                Log.e(TAG, ": reflection exception ");
            }
        }
        if (sObj != null) {
            Log.v(TAG, ": successes to get AllImpl object and return....");
            return sObj;
        }
        Log.e(TAG, ": failes to get AllImpl object");
        return null;
    }

    public static IHwImsUtEx getHwImsUtEx(IHwImsUtManager imsUtManager, int phoneId) {
        HwImsFactoryInterface obj = getImplObject();
        if (obj != null) {
            return obj.getHwImsUtEx(imsUtManager, phoneId);
        }
        Log.e(TAG, "the HwImsFactoryImpl get by reflect is null");
        return null;
    }

    public static IHwImsCallEx getHwImsCallEx(ImsCall imsCall, Context context) {
        HwImsFactoryInterface obj = getImplObject();
        if (obj != null) {
            return obj.getHwImsCallEx(imsCall, context);
        }
        Log.e(TAG, "the HwImsFactoryImpl get by reflect is null");
        return null;
    }
}
