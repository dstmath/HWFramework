package android.appwidget;

import android.content.pm.ParceledListSlice;

public class HwCustAppWidgetManager {
    protected AppWidgetManager mAppWidgetManager;

    public HwCustAppWidgetManager(AppWidgetManager appWidgetManager) {
        this.mAppWidgetManager = appWidgetManager;
    }

    public void hideTotemweatherWidgets(String pkgName, ParceledListSlice providers) {
    }
}
