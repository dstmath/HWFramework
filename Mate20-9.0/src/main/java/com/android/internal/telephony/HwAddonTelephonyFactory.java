package com.android.internal.telephony;

import android.util.Log;

public class HwAddonTelephonyFactory {
    private static final String TAG = "HwAddonTelephonyFactory";
    private static final Object mLock = new Object();
    private static volatile HwAddonTelephonyInterface obj = null;

    private static class HwAddonTelephonyDefaultImpl implements HwAddonTelephonyInterface {
        private HwAddonTelephonyDefaultImpl() {
        }

        public int getDefault4GSlotId() {
            return 0;
        }
    }

    public interface HwAddonTelephonyInterface {
        int getDefault4GSlotId();
    }

    private static HwAddonTelephonyInterface getImplObject() {
        if (obj != null) {
            return obj;
        }
        synchronized (mLock) {
            try {
                obj = (HwAddonTelephonyInterface) Class.forName("com.huawei.android.telephony.HwAddonTelephonyImpl").newInstance();
            } catch (Exception e) {
                Log.e(TAG, ": reflection exception is " + e);
            }
        }
        if (obj != null) {
            Log.v(TAG, ": successes to get AllImpl object and return....");
            return obj;
        }
        Log.e(TAG, ": failes to get AllImpl object");
        return new HwAddonTelephonyDefaultImpl();
    }

    public static HwAddonTelephonyInterface getTelephony() {
        return getImplObject();
    }
}
