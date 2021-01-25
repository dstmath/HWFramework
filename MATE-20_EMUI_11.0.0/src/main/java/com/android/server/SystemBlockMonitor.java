package com.android.server;

import android.content.Context;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.HwServiceFactory;
import com.android.server.Watchdog;
import com.android.server.am.ActivityManagerService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SystemBlockMonitor extends Thread implements HwServiceFactory.ISystemBlockMonitor {
    static final long CHECK_INTERVAL = 6000;
    static final int COMPLETED = 0;
    static final long DEFAULT_TIMEOUT = 6000;
    static final long DUMP_INTERVAL = 60000;
    public static final List<String> HAL_INTERFACES_OF_INTEREST = Arrays.asList("android.hardware.audio@2.0::IDevicesFactory", "android.hardware.bluetooth@1.0::IBluetoothHci", "android.hardware.camera.provider@2.4::ICameraProvider", "android.hardware.vr@1.0::IVr", "android.hardware.media.omx@1.0::IOmx");
    private static final boolean IS_DEBUG = SystemProperties.getBoolean("ro.debuggable", false);
    static final int JLOG_DEFAULT_TIMEOUT = 6;
    public static final String[] NATIVE_STACKS_OF_INTEREST = {"/system/bin/netd", "/system/bin/audioserver", "/system/bin/cameraserver", "/system/bin/drmserver", "/system/bin/mediadrmserver", "/system/bin/mediaserver", "/system/bin/installd", "/system/bin/sdcard", "/system/bin/surfaceflinger", "media.log", "/system/bin/keystore", "media.codec", "media.extractor", HwBluetoothBigDataService.BIGDATA_RECEIVER_PACKAGENAME};
    static final int OVERDUE = 2;
    static final String TAG = "SystemBlockMonitor";
    static final int WAITING = 1;
    static SystemBlockMonitor systemBlockMonitor;
    private long mDumpStarTtime = 0;
    final List<HandlerChecker> mHandlerCheckers = new ArrayList(16);
    HandlerChecker mMonitorChecker = null;
    HandlerThread mMonitorThread = null;

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

    public final class HandlerChecker implements Runnable {
        private Watchdog.Monitor mCurrentMonitor;
        private final Handler mHandler;
        private boolean mIsCompleted;
        private final ArrayList<Watchdog.Monitor> mMonitors = new ArrayList<>(16);
        private final String mName;
        private long mStartTime;
        private final long mWaitMax;

        HandlerChecker(Handler handler, String name, long waitMaxMillis) {
            this.mHandler = handler;
            this.mName = name;
            this.mWaitMax = waitMaxMillis;
            this.mIsCompleted = true;
        }

        public void addMonitor(Watchdog.Monitor monitor) {
            if (monitor != null) {
                this.mMonitors.add(monitor);
            }
        }

        public void scheduleCheckLocked() {
            if (this.mMonitors.size() == 0 && this.mHandler.getLooper().getQueue().isPolling()) {
                this.mIsCompleted = true;
            } else if (this.mIsCompleted) {
                this.mIsCompleted = false;
                this.mCurrentMonitor = null;
                this.mStartTime = SystemClock.uptimeMillis();
                this.mHandler.postAtFrontOfQueue(this);
            }
        }

        public int getCompletionStateLocked() {
            if (this.mIsCompleted) {
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

        @Override // java.lang.Runnable
        public void run() {
            int size = this.mMonitors.size();
            for (int i = 0; i < size; i++) {
                synchronized (SystemBlockMonitor.this) {
                    this.mCurrentMonitor = this.mMonitors.get(i);
                }
                this.mCurrentMonitor.monitor();
            }
            synchronized (SystemBlockMonitor.this) {
                this.mIsCompleted = true;
                this.mCurrentMonitor = null;
            }
        }
    }

    private static final class BinderThreadMonitor implements Watchdog.Monitor {
        private BinderThreadMonitor() {
        }

        public void monitor() {
            Binder.blockUntilThreadAvailable();
        }
    }

    public static synchronized SystemBlockMonitor getInstance() {
        SystemBlockMonitor systemBlockMonitor2;
        synchronized (SystemBlockMonitor.class) {
            if (IS_DEBUG) {
                if (systemBlockMonitor == null) {
                    systemBlockMonitor = new SystemBlockMonitor();
                }
                Slog.i(TAG, "getInstance SystemBlockMonitor");
            }
            systemBlockMonitor2 = systemBlockMonitor;
        }
        return systemBlockMonitor2;
    }

    public void init(Context context, ActivityManagerService activity) {
        Slog.i(TAG, "SystemBlockMonitor Init");
    }

    public void addMonitor(Watchdog.Monitor monitor) {
        synchronized (this) {
            if (isAlive()) {
                Slog.i(TAG, "SystemBlockMonitor isAlive");
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
        if (thread != null) {
            synchronized (this) {
                if (isAlive()) {
                    Slog.i(TAG, "SystemBlockMonitor isAlive");
                }
                this.mHandlerCheckers.add(new HandlerChecker(thread, thread.getLooper().getThread().getName(), timeoutMillis));
            }
        }
    }

    private int evaluateCheckerCompletionLocked() {
        int state = 0;
        for (HandlerChecker hc : this.mHandlerCheckers) {
            state = state > hc.getCompletionStateLocked() ? state : hc.getCompletionStateLocked();
        }
        return state;
    }

    private void printLockedThread() {
        for (HandlerChecker hc : this.mHandlerCheckers) {
            if (hc.getCompletionStateLocked() == 2) {
                String lockedThread = hc.getName();
                Slog.i(TAG, lockedThread + " is blocked for over 6 seconds");
            }
        }
    }

    public void startRun() {
        SystemBlockMonitor systemBlockMonitor2 = systemBlockMonitor;
        if (systemBlockMonitor2 != null) {
            systemBlockMonitor2.start();
        }
    }

    public int checkRecentLockedState() {
        if (this.mDumpStarTtime == 0 || SystemClock.uptimeMillis() - this.mDumpStarTtime >= DUMP_INTERVAL) {
            return 0;
        }
        return Process.myPid();
    }

    @Override // java.lang.Thread, java.lang.Runnable
    public void run() {
        while (true) {
            synchronized (this) {
                for (HandlerChecker hc : this.mHandlerCheckers) {
                    hc.scheduleCheckLocked();
                }
                try {
                    wait(6000);
                } catch (InterruptedException e) {
                    Slog.w(TAG, "error msg :" + e.getMessage());
                }
                int waitState = evaluateCheckerCompletionLocked();
                if (waitState != 0) {
                    if (waitState != 1) {
                        if (waitState == 2) {
                            Slog.i(TAG, "OVERDUE");
                            printLockedThread();
                            if (this.mDumpStarTtime == 0 || SystemClock.uptimeMillis() - this.mDumpStarTtime >= DUMP_INTERVAL) {
                                this.mDumpStarTtime = SystemClock.uptimeMillis();
                            }
                        }
                    }
                }
            }
        }
    }
}
