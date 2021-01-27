package com.android.commgmt.zrhung;

import android.content.Context;
import com.android.server.wm.IHwDisplayContentEx;
import com.android.server.zrhung.IZRHungService;

public class DefaultZrHungServicesFactory {
    private static final String TAG = "DefaultZrHungServicesFactory";
    private static DefaultZrHungServicesFactory factory = null;

    public static synchronized DefaultZrHungServicesFactory getFactory() {
        DefaultZrHungServicesFactory defaultZrHungServicesFactory;
        synchronized (DefaultZrHungServicesFactory.class) {
            if (factory == null) {
                factory = new DefaultZrHungServicesFactory();
            }
            defaultZrHungServicesFactory = factory;
        }
        return defaultZrHungServicesFactory;
    }

    public IZRHungService getDefaultZRHungService(Context context) {
        return DefaultZrHungService.getDefaultZrHungService(context);
    }

    public IHwDisplayContentEx getDefaultHwDispalyContentEx() {
        return DefaultHwDisplayContentEx.getDefaultIHwDisplayContent();
    }
}
