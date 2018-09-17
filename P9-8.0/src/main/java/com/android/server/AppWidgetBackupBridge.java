package com.android.server;

import java.util.List;

public class AppWidgetBackupBridge {
    private static WidgetBackupProvider sAppWidgetService;

    public static void register(WidgetBackupProvider instance) {
        sAppWidgetService = instance;
    }

    public static List<String> getWidgetParticipants(int userId) {
        if (sAppWidgetService != null) {
            return sAppWidgetService.getWidgetParticipants(userId);
        }
        return null;
    }

    public static byte[] getWidgetState(String packageName, int userId) {
        if (sAppWidgetService != null) {
            return sAppWidgetService.getWidgetState(packageName, userId);
        }
        return null;
    }

    public static void restoreStarting(int userId) {
        if (sAppWidgetService != null) {
            sAppWidgetService.restoreStarting(userId);
        }
    }

    public static void restoreWidgetState(String packageName, byte[] restoredState, int userId) {
        if (sAppWidgetService != null) {
            sAppWidgetService.restoreWidgetState(packageName, restoredState, userId);
        }
    }

    public static void restoreFinished(int userId) {
        if (sAppWidgetService != null) {
            sAppWidgetService.restoreFinished(userId);
        }
    }
}
