package com.android.server.rms.ipcchecker;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.rms.utils.Utils;
import android.util.Log;
import com.android.server.rms.IHwIpcChecker;
import com.android.server.rms.IHwIpcMonitor;
import java.util.ArrayList;

public class HwIpcChecker implements IHwIpcChecker, Runnable {
    private static final int COMPLETED = 0;
    private static final int OVERDUE = 3;
    private static final String TAG = "RMS.HwIpcChecker";
    private static final int WAITED_HALF = 2;
    private static final int WAITING = 1;
    private boolean mCompleted;
    private IHwIpcMonitor mCurrentMonitor;
    private Handler mHandler;
    private final ArrayList<IHwIpcMonitor> mHwIpcMonitors = new ArrayList<>();
    private final Object mLock;
    private long mStartTime;
    private final long mWaitMax;

    public HwIpcChecker(Object object, Handler handler, long waitMaxMillis) {
        this.mWaitMax = waitMaxMillis;
        this.mCompleted = true;
        this.mHandler = getAvailableHandler(handler);
        this.mLock = object;
    }

    private Handler getAvailableHandler(Handler handler) {
        if (handler != null) {
            return new Handler(handler.getLooper());
        }
        HandlerThread handlerThread = new HandlerThread(TAG, 10);
        handlerThread.start();
        return new Handler(handlerThread.getLooper());
    }

    public void addMonitor(IHwIpcMonitor monitor) {
        if (monitor != null) {
            this.mHwIpcMonitors.add(monitor);
        }
    }

    public void scheduleCheckLocked() {
        if (this.mHwIpcMonitors.size() == 0) {
            this.mCompleted = true;
            if (Utils.DEBUG || Utils.HWFLOW) {
                Log.i(TAG, "No Ipc Monitor");
            }
        } else if (this.mCompleted) {
            this.mCompleted = false;
            this.mCurrentMonitor = null;
            this.mStartTime = SystemClock.uptimeMillis();
            this.mHandler.postAtFrontOfQueue(this);
        }
    }

    private boolean isOverdueLocked() {
        boolean flag = !this.mCompleted && SystemClock.uptimeMillis() > this.mStartTime + this.mWaitMax;
        if (Utils.DEBUG || Utils.HWFLOW) {
            Log.i(TAG, "isOverdueLocked " + flag + " waittime" + this.mWaitMax);
        }
        return flag;
    }

    public IHwIpcMonitor getCurrentIpcMonitor() {
        if (isOverdueLocked()) {
            Log.i(TAG, "IPC is overdue");
            if (this.mCurrentMonitor != null) {
                String monitor = describeBlockedStateLocked();
                Log.i(TAG, "recovery for ipc monitor [" + monitor + "]");
                return this.mCurrentMonitor;
            }
        }
        return GeneralIpcMonitor.getInstance();
    }

    private String describeBlockedStateLocked() {
        if (this.mCurrentMonitor == null) {
            return "No Blocked monitor";
        }
        return "Blocked in monitor " + this.mCurrentMonitor.getClass().getName();
    }

    public void run() {
        int size = this.mHwIpcMonitors.size();
        if (Utils.DEBUG || Utils.HWFLOW) {
            Log.i(TAG, "Ipc Monitor size " + size);
        }
        for (int i = 0; i < size; i++) {
            synchronized (this.mLock) {
                this.mCurrentMonitor = this.mHwIpcMonitors.get(i);
            }
            if (Utils.DEBUG || Utils.HWFLOW) {
                Log.i(TAG, "ipc monitor enter");
            }
            this.mCurrentMonitor.doMonitor();
            if (Utils.DEBUG || Utils.HWFLOW) {
                Log.i(TAG, "ipc monitor exit");
            }
        }
        synchronized (this.mLock) {
            if (Utils.DEBUG || Utils.HWFLOW) {
                Log.i(TAG, "ipc monitor check finish");
            }
            this.mCompleted = true;
            this.mCurrentMonitor = null;
        }
    }
}
