package com.android.server.appwidget;

import android.content.Context;
import com.android.server.appwidget.HwAppWidgetServiceFactory;

public class HwAppWidgetServiceImplFactory implements HwAppWidgetServiceFactory.Factory {
    public HwAppWidgetServiceFactory.IHwAppWidgetService getHwAppWidgetService() {
        return new HwAppWidgetServiceFactoryImpl();
    }

    public static class HwAppWidgetServiceFactoryImpl implements HwAppWidgetServiceFactory.IHwAppWidgetService {
        public AppWidgetServiceImpl getAppWidgetImpl(Context context) {
            return new HwAppWidgetServiceImpl(context);
        }
    }
}
