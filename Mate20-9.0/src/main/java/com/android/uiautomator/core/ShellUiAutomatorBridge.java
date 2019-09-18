package com.android.uiautomator.core;

import android.app.ActivityManager;
import android.app.ContentProviderHolder;
import android.app.IActivityManager;
import android.app.UiAutomation;
import android.content.ContentResolver;
import android.content.IContentProvider;
import android.database.Cursor;
import android.hardware.display.DisplayManagerGlobal;
import android.os.Binder;
import android.os.IBinder;
import android.os.IPowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.IWindowManager;

public class ShellUiAutomatorBridge extends UiAutomatorBridge {
    private static final String LOG_TAG = ShellUiAutomatorBridge.class.getSimpleName();

    public ShellUiAutomatorBridge(UiAutomation uiAutomation) {
        super(uiAutomation);
    }

    public Display getDefaultDisplay() {
        return DisplayManagerGlobal.getInstance().getRealDisplay(0);
    }

    public long getSystemLongPressTime() {
        IActivityManager activityManager;
        String providerName;
        IBinder token;
        long longPressTimeout = 0;
        Cursor cursor = null;
        try {
            activityManager = ActivityManager.getService();
            providerName = Settings.Secure.CONTENT_URI.getAuthority();
            token = new Binder();
            ContentProviderHolder holder = activityManager.getContentProviderExternal(providerName, 0, token);
            if (holder != null) {
                IContentProvider provider = holder.provider;
                Cursor cursor2 = provider.query(null, Settings.Secure.CONTENT_URI, new String[]{"value"}, ContentResolver.createSqlQueryBundle("name=?", new String[]{"long_press_timeout"}, null), null);
                if (cursor2.moveToFirst()) {
                    longPressTimeout = (long) cursor2.getInt(0);
                }
                if (cursor2 != null) {
                    cursor2.close();
                }
                if (provider != null) {
                    activityManager.removeContentProviderExternal(providerName, token);
                }
                return longPressTimeout;
            }
            throw new IllegalStateException("Could not find provider: " + providerName);
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "Error reading long press timeout setting.", e);
            throw new RuntimeException("Error reading long press timeout setting.", e);
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            if (0 != 0) {
                activityManager.removeContentProviderExternal(providerName, token);
            }
            throw th;
        }
    }

    public int getRotation() {
        try {
            return IWindowManager.Stub.asInterface(ServiceManager.getService("window")).getDefaultDisplayRotation();
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "Error getting screen rotation", e);
            throw new RuntimeException(e);
        }
    }

    public boolean isScreenOn() {
        try {
            return IPowerManager.Stub.asInterface(ServiceManager.getService("power")).isInteractive();
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "Error getting screen status", e);
            throw new RuntimeException(e);
        }
    }
}
