package com.android.server.appwidget;

import android.content.Context;
import com.android.server.appwidget.HwAppWidgetServiceFactory;

public class HwAppWidgetServiceImplFactory implements HwAppWidgetServiceFactory.Factory {

    public static class HwAppWidgetServiceFactoryImpl implements HwAppWidgetServiceFactory.IHwAppWidgetService {
        public AppWidgetServiceImpl getAppWidgetImpl(Context context) {
            return new HwAppWidgetServiceImpl(context);
        }
    }

    public HwAppWidgetServiceFactory.IHwAppWidgetService getHwAppWidgetService() {
        return new HwAppWidgetServiceFactoryImpl();
    }
}
