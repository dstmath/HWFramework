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
import android.os.ICancellationSignal;
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

    @Override // com.android.uiautomator.core.UiAutomatorBridge
    public Display getDefaultDisplay() {
        return DisplayManagerGlobal.getInstance().getRealDisplay(0);
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x006d  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0072  */
    @Override // com.android.uiautomator.core.UiAutomatorBridge
    public long getSystemLongPressTime() {
        ContentProviderHolder holder;
        long longPressTimeout = 0;
        IContentProvider provider = null;
        Cursor cursor = null;
        try {
            IActivityManager activityManager = ActivityManager.getService();
            String providerName = Settings.Secure.CONTENT_URI.getAuthority();
            IBinder token = new Binder();
            try {
                ContentProviderHolder holder2 = activityManager.getContentProviderExternal(providerName, 0, token, "*uiautomator*");
                if (holder2 != null) {
                    IContentProvider provider2 = holder2.provider;
                    try {
                        Cursor cursor2 = provider2.query((String) null, Settings.Secure.CONTENT_URI, new String[]{"value"}, ContentResolver.createSqlQueryBundle("name=?", new String[]{"long_press_timeout"}, null), (ICancellationSignal) null);
                        if (cursor2.moveToFirst()) {
                            longPressTimeout = (long) cursor2.getInt(0);
                        }
                        cursor2.close();
                        activityManager.removeContentProviderExternalAsUser(providerName, token, 0);
                        return longPressTimeout;
                    } catch (Throwable th) {
                        holder = th;
                        provider = provider2;
                        if (0 != 0) {
                        }
                        if (provider != null) {
                        }
                        throw holder;
                    }
                } else {
                    throw new IllegalStateException("Could not find provider: " + providerName);
                }
            } catch (Throwable th2) {
                holder = th2;
                if (0 != 0) {
                    cursor.close();
                }
                if (provider != null) {
                    activityManager.removeContentProviderExternalAsUser(providerName, token, 0);
                }
                throw holder;
            }
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "Error reading long press timeout setting.", e);
            throw new RuntimeException("Error reading long press timeout setting.", e);
        }
    }

    @Override // com.android.uiautomator.core.UiAutomatorBridge
    public int getRotation() {
        try {
            return IWindowManager.Stub.asInterface(ServiceManager.getService("window")).getDefaultDisplayRotation();
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "Error getting screen rotation", e);
            throw new RuntimeException(e);
        }
    }

    @Override // com.android.uiautomator.core.UiAutomatorBridge
    public boolean isScreenOn() {
        try {
            return IPowerManager.Stub.asInterface(ServiceManager.getService("power")).isInteractive();
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "Error getting screen status", e);
            throw new RuntimeException(e);
        }
    }
}
