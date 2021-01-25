package com.huawei.hwaps;

import android.util.Log;

public class HwapsWrapper {
    private static final String TAG = "HwapsWrapper";
    private static volatile IHwapsFactory mFactory = null;

    private HwapsWrapper() {
    }

    private static IHwapsFactory getHwapsFactoryImpl() {
        if (mFactory == null) {
            synchronized (HwapsWrapper.class) {
                if (mFactory == null) {
                    try {
                        mFactory = (IHwapsFactory) Class.forName("com.huawei.hwaps.HwapsFactoryImpl").newInstance();
                    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                        Log.e(TAG, "reflection exception is " + e.getMessage());
                    }
                }
            }
        }
        if (mFactory == null) {
            Log.e(TAG, "failes to get HwapsFactoryImpl");
        }
        return mFactory;
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
}
