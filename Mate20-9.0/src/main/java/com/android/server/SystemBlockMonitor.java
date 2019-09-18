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
import com.android.server.HwServiceFactory;
import com.android.server.Watchdog;
import com.android.server.am.ActivityManagerService;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SystemBlockMonitor extends Thread implements HwServiceFactory.ISystemBlockMonitor {
    static final long CHECK_INTERVAL = 6000;
    static final int COMPLETED = 0;
    private static final boolean DEBUG = SystemProperties.getBoolean("ro.debuggable", false);
    static final long DEFAULT_TIMEOUT = 6000;
    static final long DUMP_INTERVAL = 60000;
    public static final List<String> HAL_INTERFACES_OF_INTEREST = Arrays.asList(new String[]{"android.hardware.audio@2.0::IDevicesFactory", "android.hardware.bluetooth@1.0::IBluetoothHci", "android.hardware.camera.provider@2.4::ICameraProvider", "android.hardware.vr@1.0::IVr", "android.hardware.media.omx@1.0::IOmx"});
    static final int JLOG_DEFAULT_TIMEOUT = 6;
    public static final String[] NATIVE_STACKS_OF_INTEREST = {"/system/bin/netd", "/system/bin/audioserver", "/system/bin/cameraserver", "/system/bin/drmserver", "/system/bin/mediadrmserver", "/system/bin/mediaserver", "/system/bin/installd", "/system/bin/sdcard", "/system/bin/surfaceflinger", "media.log", "/system/bin/keystore", "media.codec", "media.extractor", HwBluetoothBigDataService.BIGDATA_RECEIVER_PACKAGENAME};
    static final int OVERDUE = 2;
    static final String TAG = "SystemBlockMonitor";
    static final int WAITING = 1;
    static SystemBlockMonitor sSystemBlockMonitor;
    private long dumpStarTtime = 0;
    final ArrayList<HandlerChecker> mHandlerCheckers = new ArrayList<>();
    HandlerChecker mMonitorChecker = null;
    HandlerThread mMonitorThread = null;

    private static final class BinderThreadMonitor implements Watchdog.Monitor {
        private BinderThreadMonitor() {
        }

        public void monitor() {
            Binder.blockUntilThreadAvailable();
        }
    }

    public final class HandlerChecker implements Runnable {
        private boolean mCompleted;
        private Watchdog.Monitor mCurrentMonitor;
        private final Handler mHandler;
        private final ArrayList<Watchdog.Monitor> mMonitors = new ArrayList<>();
        private final String mName;
        private long mStartTime;
        private final long mWaitMax;

        HandlerChecker(Handler handler, String name, long waitMaxMillis) {
            this.mHandler = handler;
            this.mName = name;
            this.mWaitMax = waitMaxMillis;
            this.mCompleted = true;
        }

        public void addMonitor(Watchdog.Monitor monitor) {
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
                    this.mCurrentMonitor = this.mMonitors.get(i);
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
            if (!DEBUG) {
                return null;
            }
            if (sSystemBlockMonitor == null) {
                sSystemBlockMonitor = new SystemBlockMonitor();
            }
            Slog.i(TAG, "getInstance SystemBlockMonitor");
            SystemBlockMonitor systemBlockMonitor = sSystemBlockMonitor;
            return systemBlockMonitor;
        }
    }

    protected SystemBlockMonitor() {
        super(TAG);
        Slog.i(TAG, "Create SystemBlockMonitor");
        this.mMonitorThread = new HandlerThread("monitor thread");
        this.mMonitorThread.start();
        HandlerChecker handlerChecker = new HandlerChecker(this.mMonitorThread.getThreadHandler(), "monitor thread", 6000);
        this.mMonitorChecker = handlerChecker;
        this.mHandlerCheckers.add(this.mMonitorChecker);
        ArrayList<HandlerChecker> arrayList = this.mHandlerCheckers;
        HandlerChecker handlerChecker2 = new HandlerChecker(FgThread.getHandler(), "foreground thread", 6000);
        arrayList.add(handlerChecker2);
        ArrayList<HandlerChecker> arrayList2 = this.mHandlerCheckers;
        HandlerChecker handlerChecker3 = new HandlerChecker(new Handler(Looper.getMainLooper()), "main thread", 6000);
        arrayList2.add(handlerChecker3);
        ArrayList<HandlerChecker> arrayList3 = this.mHandlerCheckers;
        HandlerChecker handlerChecker4 = new HandlerChecker(UiThread.getHandler(), "ui thread", 6000);
        arrayList3.add(handlerChecker4);
        ArrayList<HandlerChecker> arrayList4 = this.mHandlerCheckers;
        HandlerChecker handlerChecker5 = new HandlerChecker(IoThread.getHandler(), "i/o thread", 6000);
        arrayList4.add(handlerChecker5);
        ArrayList<HandlerChecker> arrayList5 = this.mHandlerCheckers;
        HandlerChecker handlerChecker6 = new HandlerChecker(DisplayThread.getHandler(), "display thread", 6000);
        arrayList5.add(handlerChecker6);
        addMonitor(new BinderThreadMonitor());
    }

    public void init(Context context, ActivityManagerService activity) {
        Slog.i(TAG, "SystemBlockMonitor Init");
    }

    public void addMonitor(Watchdog.Monitor monitor) {
        synchronized (this) {
            if (isAlive()) {
                throw new RuntimeException("Monitors can't be added once the SystemBlockMonitor is running");
            } else if (monitor != null) {
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
            if (!isAlive()) {
                String name = thread.getLooper().getThread().getName();
                ArrayList<HandlerChecker> arrayList = this.mHandlerCheckers;
                HandlerChecker handlerChecker = new HandlerChecker(thread, name, timeoutMillis);
                arrayList.add(handlerChecker);
            } else {
                throw new RuntimeException("Threads can't be added once the SystemBlockMonitor is running");
            }
        }
    }

    private int evaluateCheckerCompletionLocked() {
        int state = 0;
        int checkerSize = this.mHandlerCheckers.size();
        for (int i = 0; i < checkerSize; i++) {
            HandlerChecker hc = this.mHandlerCheckers.get(i);
            state = state > hc.getCompletionStateLocked() ? state : hc.getCompletionStateLocked();
        }
        return state;
    }

    private void printLockedThread() {
        int checkerSize = this.mHandlerCheckers.size();
        for (int i = 0; i < checkerSize; i++) {
            HandlerChecker hc = this.mHandlerCheckers.get(i);
            if (hc.getCompletionStateLocked() == 2) {
                String lockedThread = hc.getName();
                Slog.i(TAG, lockedThread + " is blocked for over 6 seconds");
                Jlog.d(MemoryConstant.MSG_WORKINGSET_TOUCHEDFILES_ABORT, lockedThread, 6, lockedThread + " is blocked for over 6 seconds");
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
                    this.mHandlerCheckers.get(i).scheduleCheckLocked();
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
                            if (this.dumpStarTtime == 0 || SystemClock.uptimeMillis() - this.dumpStarTtime >= 60000) {
                                this.dumpStarTtime = SystemClock.uptimeMillis();
                            }
                        }
                    }
                }
            }
        }
    }
}
