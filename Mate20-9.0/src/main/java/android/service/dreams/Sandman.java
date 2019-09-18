package android.service.dreams;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.service.dreams.IDreamManager;
import android.util.HwPCUtils;
import android.util.Slog;

public final class Sandman {
    private static final ComponentName SOMNAMBULATOR_COMPONENT = new ComponentName(HwPCUtils.PKG_PHONE_SYSTEMUI, "com.android.systemui.Somnambulator");
    private static final String TAG = "Sandman";

    private Sandman() {
    }

    public static boolean shouldStartDockApp(Context context, Intent intent) {
        ComponentName name = intent.resolveActivity(context.getPackageManager());
        return name != null && !name.equals(SOMNAMBULATOR_COMPONENT);
    }

    public static void startDreamByUserRequest(Context context) {
        startDream(context, false);
    }

    public static void startDreamWhenDockedIfAppropriate(Context context) {
        if (!isScreenSaverEnabled(context) || !isScreenSaverActivatedOnDock(context)) {
            Slog.i(TAG, "Dreams currently disabled for docks.");
        } else {
            startDream(context, true);
        }
    }

    private static void startDream(Context context, boolean docked) {
        try {
            IDreamManager dreamManagerService = IDreamManager.Stub.asInterface(ServiceManager.getService(DreamService.DREAM_SERVICE));
            if (dreamManagerService != null && !dreamManagerService.isDreaming()) {
                if (docked) {
                    Slog.i(TAG, "Activating dream while docked.");
                    ((PowerManager) context.getSystemService("power")).wakeUp(SystemClock.uptimeMillis(), "android.service.dreams:DREAM");
                } else {
                    Slog.i(TAG, "Activating dream by user request.");
                }
                dreamManagerService.dream();
            }
        } catch (RemoteException ex) {
            Slog.e(TAG, "Could not start dream when docked.", ex);
        }
    }

    private static boolean isScreenSaverEnabled(Context context) {
        if (Settings.Secure.getIntForUser(context.getContentResolver(), Settings.Secure.SCREENSAVER_ENABLED, context.getResources().getBoolean(17956941) ? 1 : 0, -2) != 0) {
            return true;
        }
        return false;
    }

    private static boolean isScreenSaverActivatedOnDock(Context context) {
        if (Settings.Secure.getIntForUser(context.getContentResolver(), Settings.Secure.SCREENSAVER_ACTIVATE_ON_DOCK, context.getResources().getBoolean(17956939) ? 1 : 0, -2) != 0) {
            return true;
        }
        return false;
    }
}
