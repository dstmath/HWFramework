package com.huawei.android.hwaps;

import android.util.Log;

public class HwapsWrapper {
    private static final String TAG = "Hwaps";
    private static IHwapsFactory mFactory = null;

    private static synchronized IHwapsFactory getHwapsFactoryImpl() {
        synchronized (HwapsWrapper.class) {
            IHwapsFactory iHwapsFactory;
            if (mFactory != null) {
                iHwapsFactory = mFactory;
                return iHwapsFactory;
            }
            try {
                mFactory = (IHwapsFactory) Class.forName("com.huawei.android.hwaps.HwapsFactoryImpl").newInstance();
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "reflection exception is ClassNotFoundException");
            } catch (InstantiationException e2) {
                Log.e(TAG, "reflection exception is InstantiationException");
            } catch (IllegalAccessException e3) {
                Log.e(TAG, "reflection exception is IllegalAccessException");
            }
            if (mFactory == null) {
                Log.e(TAG, "failes to get HwapsFactoryImpl");
            }
            iHwapsFactory = mFactory;
            return iHwapsFactory;
        }
    }

    public static IFpsRequest getFpsRequest() {
        IHwapsFactory factory = getHwapsFactoryImpl();
        if (factory != null) {
            return factory.getFpsRequest();
        }
        return null;
    }

    public static IFpsController getFpsController() {
        IHwapsFactory factory = getHwapsFactoryImpl();
        if (factory != null) {
            return factory.getFpsController();
        }
        return null;
    }

    public static IEventAnalyzed getEventAnalyzed() {
        IHwapsFactory factory = getHwapsFactoryImpl();
        if (factory != null) {
            return factory.getEventAnalyzed();
        }
        return null;
    }

    public static ISmartLowpowerBrowser getSmartLowpowerBrowser() {
        IHwapsFactory factory = getHwapsFactoryImpl();
        if (factory != null) {
            return factory.getSmartLowpowerBrowser();
        }
        return null;
    }
}
