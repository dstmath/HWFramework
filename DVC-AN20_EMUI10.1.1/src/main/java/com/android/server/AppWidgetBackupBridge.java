package com.android.server;

import java.util.List;

public class AppWidgetBackupBridge {
    private static WidgetBackupProvider sAppWidgetService;

    public static void register(WidgetBackupProvider instance) {
        sAppWidgetService = instance;
    }

    public static List<String> getWidgetParticipants(int userId) {
        WidgetBackupProvider widgetBackupProvider = sAppWidgetService;
        if (widgetBackupProvider != null) {
            return widgetBackupProvider.getWidgetParticipants(userId);
        }
        return null;
    }

    public static byte[] getWidgetState(String packageName, int userId) {
        WidgetBackupProvider widgetBackupProvider = sAppWidgetService;
        if (widgetBackupProvider != null) {
            return widgetBackupProvider.getWidgetState(packageName, userId);
        }
        return null;
    }

    public static void restoreStarting(int userId) {
        WidgetBackupProvider widgetBackupProvider = sAppWidgetService;
        if (widgetBackupProvider != null) {
            widgetBackupProvider.restoreStarting(userId);
        }
    }

    public static void restoreWidgetState(String packageName, byte[] restoredState, int userId) {
        WidgetBackupProvider widgetBackupProvider = sAppWidgetService;
        if (widgetBackupProvider != null) {
            widgetBackupProvider.restoreWidgetState(packageName, restoredState, userId);
        }
    }

    public static void restoreFinished(int userId) {
        WidgetBackupProvider widgetBackupProvider = sAppWidgetService;
        if (widgetBackupProvider != null) {
            widgetBackupProvider.restoreFinished(userId);
        }
    }
}
