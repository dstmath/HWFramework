package com.android.server.appwidget;

import android.content.Context;
import android.util.Log;

public class HwAppWidgetServiceFactory {
    private static final String TAG = "HwAppWidgetServiceFactory";
    private static volatile Factory obj = null;

    public interface Factory {
        IHwAppWidgetService getHwAppWidgetService();
    }

    public interface IHwAppWidgetService {
        AppWidgetServiceImpl getAppWidgetImpl(Context context);
    }

    private static synchronized Factory getImplObject() {
        synchronized (HwAppWidgetServiceFactory.class) {
            if (obj != null) {
                Factory factory = obj;
                return factory;
            }
            try {
                obj = (Factory) Class.forName("com.android.server.appwidget.HwAppWidgetServiceImplFactory").newInstance();
            } catch (Exception e) {
                Log.e(TAG, ": reflection exception is " + e);
            }
            Factory factory2 = obj;
            return factory2;
        }
    }

    public static IHwAppWidgetService getHwAppWidgetService() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwAppWidgetService();
        }
        return null;
    }
}
