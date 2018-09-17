package com.android.server.appwidget;

import android.content.Context;
import com.android.server.appwidget.HwAppWidgetServiceFactory.Factory;
import com.android.server.appwidget.HwAppWidgetServiceFactory.IHwAppWidgetService;

public class HwAppWidgetServiceImplFactory implements Factory {

    public static class HwAppWidgetServiceFactoryImpl implements IHwAppWidgetService {
        public AppWidgetServiceImpl getAppWidgetImpl(Context context) {
            return new HwAppWidgetServiceImpl(context);
        }
    }

    public IHwAppWidgetService getHwAppWidgetService() {
        return new HwAppWidgetServiceFactoryImpl();
    }
}
