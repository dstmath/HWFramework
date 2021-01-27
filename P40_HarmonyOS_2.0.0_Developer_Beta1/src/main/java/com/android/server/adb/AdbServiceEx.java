package com.android.server.adb;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class AdbServiceEx {
    protected static boolean DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    protected static final int MSG_ENABLE_HDB = 101;
    protected static final String TAG = "AdbServiceEx";
    private AdbServiceBridge mBridge = null;

    public AdbServiceEx(Context context) {
        this.mBridge = new AdbServiceBridge(context);
        this.mBridge.setAdbServiceEx(this);
    }

    public AdbService getAdbService() {
        return this.mBridge;
    }

    public void systemReady() {
    }

    public void bootCompleted() {
    }

    /* access modifiers changed from: protected */
    public boolean isAdbDisabled(boolean isEnable) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void handleUserSwtiched(int newUserId) {
    }

    /* access modifiers changed from: protected */
    public void setHdbEnabled(boolean isEnable) {
    }

    /* access modifiers changed from: protected */
    public void onInitHandle() {
    }

    public void setAdbEnabled(boolean isEnable) {
        this.mBridge.mAdbEnabled = isEnable;
    }

    public ContentResolver getContentResolver() {
        return this.mBridge.mContentResolver;
    }

    public Handler getAdbHandler() {
        return this.mBridge.mHandler;
    }

    /* access modifiers changed from: protected */
    public boolean containsFunction(String functions, String function) {
        if (this.mBridge.mHandler != null) {
            return this.mBridge.mHandler.containsFunction(functions, function);
        }
        return false;
    }

    public void setAdbHandlerMessage(int what, boolean isEnable) {
        if (this.mBridge.mHandler != null) {
            this.mBridge.mHandler.sendMessage(what, isEnable);
        }
    }
}
