package com.android.internal.view;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;

public final class RotationPolicy {
    private static final int CURRENT_ROTATION = -1;
    private static final boolean IS_lOCK_UNNATURAL_ORIENTATION = SystemProperties.getBoolean("ro.config.lock_land_screen", false);
    public static final int NATURAL_ROTATION = (SystemProperties.getInt("ro.panel.hw_orientation", 0) / 90);
    private static final String TAG = "RotationPolicy";

    public static abstract class RotationPolicyListener {
        final ContentObserver mObserver = new ContentObserver(new Handler()) {
            public void onChange(boolean selfChange, Uri uri) {
                RotationPolicyListener.this.onChange();
            }
        };

        public abstract void onChange();
    }

    private RotationPolicy() {
    }

    public static boolean isRotationSupported(Context context) {
        PackageManager pm = context.getPackageManager();
        return pm.hasSystemFeature("android.hardware.sensor.accelerometer") && pm.hasSystemFeature("android.hardware.screen.portrait") && pm.hasSystemFeature("android.hardware.screen.landscape") && context.getResources().getBoolean(17957034);
    }

    public static int getRotationLockOrientation(Context context) {
        if (!IS_lOCK_UNNATURAL_ORIENTATION) {
            Point size = new Point();
            try {
                WindowManagerGlobal.getWindowManagerService().getInitialDisplaySize(0, size);
                return size.x < size.y ? 1 : 2;
            } catch (RemoteException e) {
                Log.w(TAG, "Unable to get the display size");
            }
        }
        return 0;
    }

    public static boolean isRotationLockToggleVisible(Context context) {
        if (!isRotationSupported(context) || Settings.System.getIntForUser(context.getContentResolver(), "hide_rotation_lock_toggle_for_accessibility", 0, -2) != 0) {
            return false;
        }
        return true;
    }

    public static boolean isRotationLocked(Context context) {
        return Settings.System.getIntForUser(context.getContentResolver(), "accelerometer_rotation", 0, -2) == 0;
    }

    public static void setRotationLock(Context context, boolean enabled) {
        setRotationLockAtAngle(context, enabled, IS_lOCK_UNNATURAL_ORIENTATION ? -1 : NATURAL_ROTATION);
    }

    public static void setRotationLockAtAngle(Context context, boolean enabled, int rotation) {
        Settings.System.putIntForUser(context.getContentResolver(), "hide_rotation_lock_toggle_for_accessibility", 0, -2);
        setRotationLock(enabled, rotation);
    }

    public static void setRotationLockForAccessibility(Context context, boolean enabled) {
        Settings.System.putIntForUser(context.getContentResolver(), "hide_rotation_lock_toggle_for_accessibility", enabled, -2);
        setRotationLock(enabled, IS_lOCK_UNNATURAL_ORIENTATION ? -1 : NATURAL_ROTATION);
    }

    private static boolean areAllRotationsAllowed(Context context) {
        return context.getResources().getBoolean(17956870);
    }

    private static void setRotationLock(final boolean enabled, final int rotation) {
        AsyncTask.execute(new Runnable() {
            public void run() {
                try {
                    IWindowManager wm = WindowManagerGlobal.getWindowManagerService();
                    if (enabled) {
                        wm.freezeRotation(rotation);
                    } else {
                        wm.thawRotation();
                    }
                } catch (RemoteException e) {
                    Log.w(RotationPolicy.TAG, "Unable to save auto-rotate setting");
                }
            }
        });
    }

    public static void registerRotationPolicyListener(Context context, RotationPolicyListener listener) {
        registerRotationPolicyListener(context, listener, UserHandle.getCallingUserId());
    }

    public static void registerRotationPolicyListener(Context context, RotationPolicyListener listener, int userHandle) {
        context.getContentResolver().registerContentObserver(Settings.System.getUriFor("accelerometer_rotation"), false, listener.mObserver, userHandle);
        context.getContentResolver().registerContentObserver(Settings.System.getUriFor("hide_rotation_lock_toggle_for_accessibility"), false, listener.mObserver, userHandle);
    }

    public static void unregisterRotationPolicyListener(Context context, RotationPolicyListener listener) {
        context.getContentResolver().unregisterContentObserver(listener.mObserver);
    }
}
