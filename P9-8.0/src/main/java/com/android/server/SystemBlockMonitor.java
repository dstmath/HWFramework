package com.android.server;

import android.content.Context;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Jlog;
import android.util.Slog;
import com.android.server.HwServiceFactory.ISystemBlockMonitor;
import com.android.server.Watchdog.Monitor;
import com.android.server.am.ActivityManagerService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SystemBlockMonitor extends Thread implements ISystemBlockMonitor {
    static final long CHECK_INTERVAL = 6000;
    static final int COMPLETED = 0;
    private static final boolean DEBUG = SystemProperties.getBoolean("ro.debuggable", false);
    static final long DEFAULT_TIMEOUT = 6000;
    static final long DUMP_INTERVAL = 60000;
    public static final List<String> HAL_INTERFACES_OF_INTEREST = Arrays.asList(new String[]{"android.hardware.audio@2.0::IDevicesFactory", "android.hardware.bluetooth@1.0::IBluetoothHci", "android.hardware.camera.provider@2.4::ICameraProvider", "android.hardware.vr@1.0::IVr", "android.hardware.media.omx@1.0::IOmx"});
    static final int JLOG_DEFAULT_TIMEOUT = 6;
    public static final String[] NATIVE_STACKS_OF_INTEREST = new String[]{"/system/bin/netd", "/system/bin/audioserver", "/system/bin/cameraserver", "/system/bin/drmserver", "/system/bin/mediadrmserver", "/system/bin/mediaserver", "/system/bin/installd", "/system/bin/sdcard", "/system/bin/surfaceflinger", "media.log", "/system/bin/keystore", "media.codec", "media.extractor", HwBluetoothBigDataService.BIGDATA_RECEIVER_PACKAGENAME};
    static final int OVERDUE = 2;
    static final String TAG = "SystemBlockMonitor";
    static final int WAITING = 1;
    static SystemBlockMonitor sSystemBlockMonitor;
    private long dumpStarTtime = 0;
    final ArrayList<HandlerChecker> mHandlerCheckers = new ArrayList();
    HandlerChecker mMonitorChecker = null;
    HandlerThread mMonitorThread = null;

    private static final class BinderThreadMonitor implements Monitor {
        /* synthetic */ BinderThreadMonitor(BinderThreadMonitor -this0) {
            this();
        }

        private BinderThreadMonitor() {
        }

        public void monitor() {
            Binder.blockUntilThreadAvailable();
        }
    }

    public final class HandlerChecker implements Runnable {
        private boolean mCompleted;
        private Monitor mCurrentMonitor;
        private final Handler mHandler;
        private final ArrayList<Monitor> mMonitors = new ArrayList();
        private final String mName;
        private long mStartTime;
        private final long mWaitMax;

        HandlerChecker(Handler handler, String name, long waitMaxMillis) {
            this.mHandler = handler;
            this.mName = name;
            this.mWaitMax = waitMaxMillis;
            this.mCompleted = true;
        }

        public void addMonitor(Monitor monitor) {
            this.mMonitors.add(monitor);
        }

        public void scheduleCheckLocked() {
            if (this.mMonitors.size() == 0 && this.mHandler.getLooper().getQueue().isPolling()) {
                this.mCompleted = true;
            } else if (this.mCompleted) {
                this.mCompleted = false;
                this.mCurrentMonitor = null;
                this.mStartTime = SystemClock.uptimeMillis();
                this.mHandler.postAtFrontOfQueue(this);
            }
        }

        public int getCompletionStateLocked() {
            if (this.mCompleted) {
                return 0;
            }
            if (SystemClock.uptimeMillis() - this.mStartTime < this.mWaitMax) {
                return 1;
            }
            return 2;
        }

        public Thread getThread() {
            return this.mHandler.getLooper().getThread();
        }

        public String getName() {
            return this.mName;
        }

        public void run() {
            int size = this.mMonitors.size();
            for (int i = 0; i < size; i++) {
                synchronized (SystemBlockMonitor.this) {
                    this.mCurrentMonitor = (Monitor) this.mMonitors.get(i);
                }
                this.mCurrentMonitor.monitor();
            }
            synchronized (SystemBlockMonitor.this) {
                this.mCompleted = true;
                this.mCurrentMonitor = null;
            }
        }
    }

    public static synchronized SystemBlockMonitor getInstance() {
        synchronized (SystemBlockMonitor.class) {
            if (DEBUG) {
                if (sSystemBlockMonitor == null) {
                    sSystemBlockMonitor = new SystemBlockMonitor();
                }
                Slog.i(TAG, "getInstance SystemBlockMonitor");
                SystemBlockMonitor systemBlockMonitor = sSystemBlockMonitor;
                return systemBlockMonitor;
            }
            return null;
        }
    }

    protected SystemBlockMonitor() {
        super(TAG);
        Slog.i(TAG, "Create SystemBlockMonitor");
        this.mMonitorThread = new HandlerThread("monitor thread");
        this.mMonitorThread.start();
        this.mMonitorChecker = new HandlerChecker(this.mMonitorThread.getThreadHandler(), "monitor thread", 6000);
        this.mHandlerCheckers.add(this.mMonitorChecker);
        this.mHandlerCheckers.add(new HandlerChecker(FgThread.getHandler(), "foreground thread", 6000));
        this.mHandlerCheckers.add(new HandlerChecker(new Handler(Looper.getMainLooper()), "main thread", 6000));
        this.mHandlerCheckers.add(new HandlerChecker(UiThread.getHandler(), "ui thread", 6000));
        this.mHandlerCheckers.add(new HandlerChecker(IoThread.getHandler(), "i/o thread", 6000));
        this.mHandlerCheckers.add(new HandlerChecker(DisplayThread.getHandler(), "display thread", 6000));
        addMonitor(new BinderThreadMonitor());
    }

    public void init(Context context, ActivityManagerService activity) {
        Slog.i(TAG, "SystemBlockMonitor Init");
    }

    public void addMonitor(Monitor monitor) {
        synchronized (this) {
            if (isAlive()) {
                throw new RuntimeException("Monitors can't be added once the SystemBlockMonitor is running");
            }
            if (monitor != null) {
                this.mMonitorChecker.addMonitor(monitor);
            }
        }
    }

    public void addThread(Handler thread) {
        if (thread != null) {
            addThread(thread, 6000);
        }
    }

    public void addThread(Handler thread, long timeoutMillis) {
        synchronized (this) {
            if (isAlive()) {
                throw new RuntimeException("Threads can't be added once the SystemBlockMonitor is running");
            }
            this.mHandlerCheckers.add(new HandlerChecker(thread, thread.getLooper().getThread().getName(), timeoutMillis));
        }
    }

    private int evaluateCheckerCompletionLocked() {
        int state = 0;
        int checkerSize = this.mHandlerCheckers.size();
        for (int i = 0; i < checkerSize; i++) {
            HandlerChecker hc = (HandlerChecker) this.mHandlerCheckers.get(i);
            if (state <= hc.getCompletionStateLocked()) {
                state = hc.getCompletionStateLocked();
            }
        }
        return state;
    }

    private void printLockedThread() {
        int checkerSize = this.mHandlerCheckers.size();
        for (int i = 0; i < checkerSize; i++) {
            HandlerChecker hc = (HandlerChecker) this.mHandlerCheckers.get(i);
            if (hc.getCompletionStateLocked() == 2) {
                String lockedThread = hc.getName();
                Slog.i(TAG, lockedThread + " is blocked for over 6 seconds");
                Jlog.d(353, lockedThread, 6, lockedThread + " is blocked for over 6 seconds");
            }
        }
    }

    public void startRun() {
        if (sSystemBlockMonitor != null) {
            sSystemBlockMonitor.start();
        }
    }

    public int checkRecentLockedState() {
        if (this.dumpStarTtime == 0 || SystemClock.uptimeMillis() - this.dumpStarTtime >= 60000) {
            return 0;
        }
        return Process.myPid();
    }

    public void run() {
        while (true) {
            synchronized (this) {
                int checkerSize = this.mHandlerCheckers.size();
                for (int i = 0; i < checkerSize; i++) {
                    ((HandlerChecker) this.mHandlerCheckers.get(i)).scheduleCheckLocked();
                }
                try {
                    wait(6000);
                } catch (InterruptedException e) {
                    Slog.w(TAG, "error msg :" + e.getMessage());
                }
                int waitState = evaluateCheckerCompletionLocked();
                if (!(waitState == 0 || waitState == 1 || waitState != 2)) {
                    Slog.i(TAG, "OVERDUE");
                    printLockedThread();
                    if (this.dumpStarTtime == 0 || SystemClock.uptimeMillis() - this.dumpStarTtime >= 60000) {
                        this.dumpStarTtime = SystemClock.uptimeMillis();
                    }
                }
            }
        }
    }
}
