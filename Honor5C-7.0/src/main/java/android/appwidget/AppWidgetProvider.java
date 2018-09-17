package android.appwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class AppWidgetProvider extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle extras;
        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) {
            extras = intent.getExtras();
            if (extras != null) {
                int[] appWidgetIds = extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                if (appWidgetIds != null && appWidgetIds.length > 0) {
                    onUpdate(context, AppWidgetManager.getInstance(context), appWidgetIds);
                }
            }
        } else if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
            extras = intent.getExtras();
            if (extras != null && extras.containsKey(AppWidgetManager.EXTRA_APPWIDGET_ID)) {
                int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
                onDeleted(context, new int[]{appWidgetId});
            }
        } else if (AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED.equals(action)) {
            extras = intent.getExtras();
            if (extras != null && extras.containsKey(AppWidgetManager.EXTRA_APPWIDGET_ID) && extras.containsKey(AppWidgetManager.EXTRA_APPWIDGET_OPTIONS)) {
                onAppWidgetOptionsChanged(context, AppWidgetManager.getInstance(context), extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID), extras.getBundle(AppWidgetManager.EXTRA_APPWIDGET_OPTIONS));
            }
        } else if (AppWidgetManager.ACTION_APPWIDGET_ENABLED.equals(action)) {
            onEnabled(context);
        } else if (AppWidgetManager.ACTION_APPWIDGET_DISABLED.equals(action)) {
            onDisabled(context);
        } else if (AppWidgetManager.ACTION_APPWIDGET_RESTORED.equals(action)) {
            extras = intent.getExtras();
            if (extras != null) {
                int[] oldIds = extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_OLD_IDS);
                int[] newIds = extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                if (oldIds != null && oldIds.length > 0) {
                    onRestored(context, oldIds, newIds);
                    onUpdate(context, AppWidgetManager.getInstance(context), newIds);
                }
            }
        }
    }

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    }

    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
    }

    public void onDeleted(Context context, int[] appWidgetIds) {
    }

    public void onEnabled(Context context) {
    }

    public void onDisabled(Context context) {
    }

    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
    }
}
