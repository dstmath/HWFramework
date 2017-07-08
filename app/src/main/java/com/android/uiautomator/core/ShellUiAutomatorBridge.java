package com.android.uiautomator.core;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.IActivityManager.ContentProviderHolder;
import android.app.UiAutomation;
import android.content.IContentProvider;
import android.database.Cursor;
import android.hardware.display.DisplayManagerGlobal;
import android.os.Binder;
import android.os.IBinder;
import android.os.IPowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.Display;
import android.view.IWindowManager.Stub;

public class ShellUiAutomatorBridge extends UiAutomatorBridge {
    private static final String LOG_TAG = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.uiautomator.core.ShellUiAutomatorBridge.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.uiautomator.core.ShellUiAutomatorBridge.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.uiautomator.core.ShellUiAutomatorBridge.<clinit>():void");
    }

    public ShellUiAutomatorBridge(UiAutomation uiAutomation) {
        super(uiAutomation);
    }

    public Display getDefaultDisplay() {
        return DisplayManagerGlobal.getInstance().getRealDisplay(0);
    }

    public long getSystemLongPressTime() {
        long longPressTimeout = 0;
        Cursor cursor = null;
        IActivityManager activityManager;
        String providerName;
        IBinder token;
        try {
            activityManager = ActivityManagerNative.getDefault();
            providerName = Secure.CONTENT_URI.getAuthority();
            token = new Binder();
            ContentProviderHolder holder = activityManager.getContentProviderExternal(providerName, 0, token);
            if (holder == null) {
                throw new IllegalStateException("Could not find provider: " + providerName);
            }
            IContentProvider provider = holder.provider;
            cursor = provider.query(null, Secure.CONTENT_URI, new String[]{"value"}, "name=?", new String[]{"long_press_timeout"}, null, null);
            if (cursor.moveToFirst()) {
                longPressTimeout = (long) cursor.getInt(0);
            }
            if (cursor != null) {
                cursor.close();
            }
            if (provider != null) {
                activityManager.removeContentProviderExternal(providerName, token);
            }
            return longPressTimeout;
        } catch (RemoteException e) {
            String message = "Error reading long press timeout setting.";
            Log.e(LOG_TAG, message, e);
            throw new RuntimeException(message, e);
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            if (null != null) {
                activityManager.removeContentProviderExternal(providerName, token);
            }
        }
    }

    public int getRotation() {
        try {
            return Stub.asInterface(ServiceManager.getService("window")).getRotation();
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
