package com.android.server.rms.iaware.memory.policy;

import android.os.Bundle;
import android.os.HandlerThread;
import android.rms.iaware.AwareLog;
import com.android.server.rms.iaware.memory.utils.EventTracker;
import java.util.concurrent.atomic.AtomicBoolean;

public class DmeServer {
    private static final Object LOCK = new Object();
    private static final String TAG = "AwareMem_DmeServer";
    private static DmeServer sDmeServer;
    private final AtomicBoolean mRunning = new AtomicBoolean(false);

    public static DmeServer getInstance() {
        DmeServer dmeServer;
        synchronized (LOCK) {
            if (sDmeServer == null) {
                sDmeServer = new DmeServer();
            }
            dmeServer = sDmeServer;
        }
        return dmeServer;
    }

    public void setHandler(HandlerThread handlerThread) {
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

    public void stopExecute(long timeStamp, int event) {
        if (this.mRunning.get()) {
            MemoryExecutorServer.getInstance().stopMemoryRecover();
            if (AwareLog.getDebugLogSwitch()) {
                AwareLog.d(TAG, "stopExecuteMemoryRecover event=" + event);
            }
            EventTracker.getInstance().trackEvent(EventTracker.TRACK_TYPE_STOP, event, timeStamp, null);
            return;
        }
        AwareLog.w(TAG, "stopMemoryRecover iaware not running");
    }

    public void execute(String scene, Bundle extras, int event, long timeStamp) {
        if (!this.mRunning.get()) {
            AwareLog.w(TAG, "executeMemoryRecover iaware not running");
        } else {
            MemoryExecutorServer.getInstance().executeMemoryRecover(scene, extras, event, timeStamp);
        }
    }

    public void notifyProtectLruState(int state) {
        MemoryExecutorServer.getInstance().notifyProtectLruState(state);
    }

    public int getProtectLruState() {
        return MemoryExecutorServer.getInstance().getProtectLruState();
    }

    public boolean isFirstBooting() {
        return MemoryExecutorServer.getInstance().isFirstBooting();
    }

    public void firstBootingFinish() {
        MemoryExecutorServer.getInstance().firstBootingFinish();
    }
}
