package com.huawei.server.fingerprint;

import android.content.Context;
import android.util.Log;
import com.huawei.server.FactoryLoader;

public class HwPartFingerprintFactory {
    private static final String FACTORY_NAME = "com.huawei.server.fingerprint.HwPartFingerprintFactoryImpl";
    private static final String TAG = "HwPartFingerprintFactory";
    private static HwPartFingerprintFactory sFactory = null;

    public static synchronized HwPartFingerprintFactory loadFactory() {
        synchronized (HwPartFingerprintFactory.class) {
            if (sFactory != null) {
                return sFactory;
            }
            Object object = FactoryLoader.loadFactory(FACTORY_NAME);
            if (object == null || !(object instanceof HwPartFingerprintFactory)) {
                sFactory = new HwPartFingerprintFactory();
            } else {
                sFactory = (HwPartFingerprintFactory) object;
            }
            Log.i(TAG, "load HwPartFingerprintFactory");
            return sFactory;
        }
    }

    public DefaultFingerViewController getFingerViewController(Context context) {
        return new DefaultFingerViewController(context);
    }
}
