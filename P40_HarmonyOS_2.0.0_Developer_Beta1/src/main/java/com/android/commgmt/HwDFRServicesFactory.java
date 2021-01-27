package com.android.commgmt;

import com.android.commgmt.zrhung.DefaultZrHungServicesFactory;
import com.huawei.android.util.SlogEx;

public class HwDFRServicesFactory {
    private static final String TAG = "HwDFRServicesFactory";

    private static Object loadFactory(String factoryName) {
        if (factoryName == null) {
            return null;
        }
        try {
            return Class.forName(factoryName).newInstance();
        } catch (ClassNotFoundException e) {
            SlogEx.e(TAG, "Class Not found Exception");
            return null;
        } catch (IllegalAccessException e2) {
            SlogEx.e(TAG, "Illegal access exception");
            return null;
        } catch (InstantiationException e3) {
            SlogEx.e(TAG, "Instantiation exception");
            return null;
        }
    }

    public static DefaultZrHungServicesFactory getZrHungServicesFactory() {
        SlogEx.i(TAG, "getZrHungServicesFactory");
        DefaultZrHungServicesFactory factory = (DefaultZrHungServicesFactory) loadFactory("com.android.server.zrhung.ZrHungServicesFactoryImpl");
        if (factory != null) {
            return factory;
        }
        SlogEx.i(TAG, "No ZrHung services implements exists.");
        return DefaultZrHungServicesFactory.getFactory();
    }
}
