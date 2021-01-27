package com.android.internal.telephony.vsim;

import android.content.Context;
import com.huawei.internal.telephony.PhoneExt;

public class DefaultHwPartTelephonyVSimFactory {
    private static final String TAG = "DefaultHwPartTelephonyVSimFactory";
    private static DefaultHwPartTelephonyVSimFactory sInstance = null;

    public static synchronized DefaultHwPartTelephonyVSimFactory getInstance() {
        DefaultHwPartTelephonyVSimFactory defaultHwPartTelephonyVSimFactory;
        synchronized (DefaultHwPartTelephonyVSimFactory.class) {
            if (sInstance == null) {
                sInstance = new DefaultHwPartTelephonyVSimFactory();
            }
            defaultHwPartTelephonyVSimFactory = sInstance;
        }
        return defaultHwPartTelephonyVSimFactory;
    }

    public DefaultHwVSimUtils getHwVSimUtils() {
        return DefaultHwVSimUtils.getInstance();
    }

    public DefaultHwPhoneServiceVsimEx getHwPhoneServiceVsimEx(Context context, PhoneExt[] phones) {
        return new DefaultHwPhoneServiceVsimEx();
    }
}
