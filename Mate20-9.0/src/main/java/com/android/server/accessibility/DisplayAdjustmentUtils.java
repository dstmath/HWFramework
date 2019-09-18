package com.android.server.accessibility;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Binder;
import android.provider.Settings;
import com.android.server.LocalServices;
import com.android.server.display.DisplayTransformManager;

class DisplayAdjustmentUtils {
    private static final int DEFAULT_DISPLAY_DALTONIZER = 12;
    private static final float[] MATRIX_GRAYSCALE = {0.2126f, 0.2126f, 0.2126f, 0.0f, 0.7152f, 0.7152f, 0.7152f, 0.0f, 0.0722f, 0.0722f, 0.0722f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f};
    private static final float[] MATRIX_INVERT_COLOR = {0.402f, -0.598f, -0.599f, 0.0f, -1.174f, -0.174f, -1.175f, 0.0f, -0.228f, -0.228f, 0.772f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f};

    DisplayAdjustmentUtils() {
    }

    /* JADX INFO: finally extract failed */
    public static void applyDaltonizerSetting(Context context, int userId) {
        ContentResolver cr = context.getContentResolver();
        DisplayTransformManager dtm = (DisplayTransformManager) LocalServices.getService(DisplayTransformManager.class);
        int daltonizerMode = -1;
        long identity = Binder.clearCallingIdentity();
        try {
            if (Settings.Secure.getIntForUser(cr, "accessibility_display_daltonizer_enabled", 0, userId) != 0) {
                daltonizerMode = Settings.Secure.getIntForUser(cr, "accessibility_display_daltonizer", 12, userId);
            }
            Binder.restoreCallingIdentity(identity);
            float[] grayscaleMatrix = null;
            if (daltonizerMode == 0) {
                grayscaleMatrix = MATRIX_GRAYSCALE;
                daltonizerMode = -1;
            }
            dtm.setColorMatrix(DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE, grayscaleMatrix);
            dtm.setDaltonizerMode(daltonizerMode);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    public static void applyInversionSetting(Context context, int userId) {
        ContentResolver cr = context.getContentResolver();
        DisplayTransformManager dtm = (DisplayTransformManager) LocalServices.getService(DisplayTransformManager.class);
        long identity = Binder.clearCallingIdentity();
        boolean invertColors = false;
        try {
            if (Settings.Secure.getIntForUser(cr, "accessibility_display_inversion_enabled", 0, userId) != 0) {
                invertColors = true;
            }
            dtm.setColorMatrix(DisplayTransformManager.LEVEL_COLOR_MATRIX_INVERT_COLOR, invertColors ? MATRIX_INVERT_COLOR : null);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }
}
