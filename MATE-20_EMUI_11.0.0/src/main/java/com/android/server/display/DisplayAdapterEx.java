package com.android.server.display;

import android.content.Context;
import android.os.Handler;
import android.view.Display;
import com.android.server.display.DisplayAdapter;
import com.android.server.display.DisplayManagerServiceEx;

public class DisplayAdapterEx {
    public static final int DISPLAY_DEVICE_EVENT_ADDED = 1;
    public static final int DISPLAY_DEVICE_EVENT_CHANGED = 2;
    public static final int DISPLAY_DEVICE_EVENT_REMOVED = 3;
    private static final String TAG = "DisplayAdapterEx";
    private DisplayAdapterBridge mDisplayAdapterBridge;
    private DisplayManagerServiceEx.SyncRootEx mSyncRoot;

    public DisplayAdapterEx() {
    }

    public DisplayAdapterEx(DisplayManagerServiceEx.SyncRootEx syncRoot, Context context, Handler handler, ListenerEx listener, String name) {
        this.mDisplayAdapterBridge = new DisplayAdapterBridge(syncRoot.getSyncRoot(), context, handler, listener.getListener(), name);
        this.mDisplayAdapterBridge.setDisplayAdapterEx(this);
        this.mSyncRoot = syncRoot;
    }

    public DisplayAdapterBridge getDisplayAdapterBridge() {
        return this.mDisplayAdapterBridge;
    }

    public void registerLocked() {
        DisplayAdapterBridge displayAdapterBridge = this.mDisplayAdapterBridge;
        if (displayAdapterBridge != null) {
            displayAdapterBridge.registerLocked();
        }
    }

    public Context getContext() {
        DisplayAdapterBridge displayAdapterBridge = this.mDisplayAdapterBridge;
        if (displayAdapterBridge != null) {
            return displayAdapterBridge.getContext();
        }
        return null;
    }

    public DisplayManagerServiceEx.SyncRootEx getSyncRoot() {
        return this.mSyncRoot;
    }

    public boolean createVrDisplay(String displayName, int[] displayParams) {
        return false;
    }

    public boolean destroyVrDisplay(String displayName) {
        return false;
    }

    public boolean destroyAllVrDisplay() {
        return false;
    }

    public Display.Mode createMode(int width, int height, float refreshRate) {
        if (this.mDisplayAdapterBridge != null) {
            return DisplayAdapterBridge.createMode(width, height, refreshRate);
        }
        return null;
    }

    public void sendDisplayDeviceEventLocked(DisplayDeviceExt displayDevice, int event) {
        DisplayAdapterBridge displayAdapterBridge = this.mDisplayAdapterBridge;
        if (displayAdapterBridge != null) {
            displayAdapterBridge.sendDisplayDeviceEventLocked(displayDevice.getDisplayDeviceBridge(), event);
        }
    }

    public static class ListenerEx {
        DisplayAdapter.Listener mListener;

        public ListenerEx() {
        }

        public ListenerEx(DisplayAdapter.Listener listener) {
            this.mListener = listener;
        }

        public DisplayAdapter.Listener getListener() {
            return this.mListener;
        }
    }
}
