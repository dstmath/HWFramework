package com.android.server.display;

import android.content.Context;
import android.os.Handler;
import android.view.Display.Mode;
import com.android.server.display.DisplayManagerService.SyncRoot;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;

abstract class DisplayAdapter {
    public static final int DISPLAY_DEVICE_EVENT_ADDED = 1;
    public static final int DISPLAY_DEVICE_EVENT_CHANGED = 2;
    public static final int DISPLAY_DEVICE_EVENT_REMOVED = 3;
    private static final AtomicInteger NEXT_DISPLAY_MODE_ID = new AtomicInteger(1);
    private final Context mContext;
    private final Handler mHandler;
    private final Listener mListener;
    private final String mName;
    private final SyncRoot mSyncRoot;

    public interface Listener {
        void onDisplayDeviceEvent(DisplayDevice displayDevice, int i);

        void onTraversalRequested();
    }

    public DisplayAdapter(SyncRoot syncRoot, Context context, Handler handler, Listener listener, String name) {
        this.mSyncRoot = syncRoot;
        this.mContext = context;
        this.mHandler = handler;
        this.mListener = listener;
        this.mName = name;
    }

    public final SyncRoot getSyncRoot() {
        return this.mSyncRoot;
    }

    public final Context getContext() {
        return this.mContext;
    }

    public final Handler getHandler() {
        return this.mHandler;
    }

    public final String getName() {
        return this.mName;
    }

    public void registerLocked() {
    }

    public void dumpLocked(PrintWriter pw) {
    }

    protected final void sendDisplayDeviceEventLocked(final DisplayDevice device, final int event) {
        this.mHandler.post(new Runnable() {
            public void run() {
                DisplayAdapter.this.mListener.onDisplayDeviceEvent(device, event);
            }
        });
    }

    protected final void sendTraversalRequestLocked() {
        this.mHandler.post(new Runnable() {
            public void run() {
                DisplayAdapter.this.mListener.onTraversalRequested();
            }
        });
    }

    public static Mode createMode(int width, int height, float refreshRate) {
        return new Mode(NEXT_DISPLAY_MODE_ID.getAndIncrement(), width, height, refreshRate);
    }
}
