package com.android.server.rms.iaware.memory.policy;

import android.os.Bundle;
import android.os.HandlerThread;
import android.rms.iaware.AwareLog;
import com.android.server.rms.iaware.memory.utils.EventTracker;
import java.util.concurrent.atomic.AtomicBoolean;

public class DMEServer {
    private static final String TAG = "AwareMem_DMEServer";
    private static final Object mLock = new Object();
    private static DMEServer sDMEServer;
    private final AtomicBoolean mRunning = new AtomicBoolean(false);

    public static DMEServer getInstance() {
        DMEServer dMEServer;
        synchronized (mLock) {
            if (sDMEServer == null) {
                sDMEServer = new DMEServer();
            }
            dMEServer = sDMEServer;
        }
        return dMEServer;
    }

    public void setHandler(HandlerThread handlerThread) {
        AwareLog.d(TAG, "setHandler: object=" + handlerThread);
        if (handlerThread != null) {
            MemoryExecutorServer.getInstance().setMemHandlerThread(handlerThread);
        } else {
            AwareLog.e(TAG, "setHandler: why handlerThread is null!!");
        }
    }

    public void enable() {
        if (!this.mRunning.get()) {
            this.mRunning.set(true);
            MemoryExecutorServer.getInstance().enable();
            AwareLog.i(TAG, "start");
        }
    }

    public void disable() {
        if (this.mRunning.get()) {
            this.mRunning.set(false);
            MemoryExecutorServer.getInstance().disable();
            AwareLog.i(TAG, "stop");
        }
    }

    public void stopExecute(long timestamp, int event) {
        if (this.mRunning.get()) {
            MemoryExecutorServer.getInstance().stopMemoryRecover();
            AwareLog.d(TAG, "stopExecuteMemoryRecover event=" + event);
            EventTracker.getInstance().trackEvent(1005, event, timestamp, null);
            return;
        }
        AwareLog.i(TAG, "stopMemoryRecover iaware not running");
    }

    public void execute(String scene, Bundle extras, int event, long timeStamp) {
        if (this.mRunning.get()) {
            MemoryExecutorServer.getInstance().executeMemoryRecover(scene, extras, event, timeStamp);
        } else {
            AwareLog.i(TAG, "executeMemoryRecover iaware not running");
        }
    }

    public void notifyProtectLruState(int state) {
        MemoryExecutorServer.getInstance().notifyProtectLruState(state);
    }
}
