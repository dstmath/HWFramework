package com.android.server;

import android.content.Context;
import android.os.Binder;
import android.os.Environment;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.Slog;
import com.android.internal.logging.MetricsLogger;
import com.android.server.os.HwBootFail;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class SystemServiceManager {
    private static final int SERVICE_CALL_WARN_TIME_MS = 50;
    private static final String TAG = "SystemServiceManager";
    private static File sSystemDir;
    private final Context mContext;
    private int mCurrentPhase = -1;
    private boolean mRuntimeRestarted;
    private long mRuntimeStartElapsedTime;
    private long mRuntimeStartUptime;
    private boolean mSafeMode;
    private final ArrayList<SystemService> mServices = new ArrayList<>();

    SystemServiceManager(Context context) {
        this.mContext = context;
    }

    public SystemService startService(String className) {
        try {
            return startService((Class<SystemService>) Class.forName(className));
        } catch (ClassNotFoundException ex) {
            Slog.i(TAG, "Starting " + className);
            throw new RuntimeException("Failed to create service " + className + ": service class not found, usually indicates that the caller should have called PackageManager.hasSystemFeature() to check whether the feature is available on this device before trying to start the services that implement it", ex);
        }
    }

    public <T extends SystemService> T startService(Class<T> serviceClass) {
        try {
            String name = serviceClass.getName();
            Trace.traceBegin(524288, "StartService " + name);
            if (SystemService.class.isAssignableFrom(serviceClass)) {
                try {
                    T service = serviceClass.getConstructor(Context.class).newInstance(this.mContext);
                    startService(service);
                    return service;
                } catch (InstantiationException ex) {
                    throw new RuntimeException("Failed to create service " + name + ": service could not be instantiated", ex);
                } catch (IllegalAccessException ex2) {
                    throw new RuntimeException("Failed to create service " + name + ": service must have a public constructor with a Context argument", ex2);
                } catch (NoSuchMethodException ex3) {
                    throw new RuntimeException("Failed to create service " + name + ": service must have a public constructor with a Context argument", ex3);
                } catch (InvocationTargetException ex4) {
                    throw new RuntimeException("Failed to create service " + name + ": service constructor threw an exception", ex4);
                }
            } else {
                throw new RuntimeException("Failed to create " + name + ": service must extend " + SystemService.class.getName());
            }
        } finally {
            Trace.traceEnd(524288);
        }
    }

    public void startService(SystemService service) {
        synchronized (this.mServices) {
            this.mServices.add(service);
            StringBuilder sb = new StringBuilder();
            sb.append("Add starting ");
            sb.append(service.getClass().getName());
            sb.append(" at index ");
            sb.append(this.mServices.size() - 1);
            Slog.i(TAG, sb.toString());
        }
        long time = SystemClock.elapsedRealtime();
        try {
            service.onStart();
            warnIfTooLong(SystemClock.elapsedRealtime() - time, service, "onStart");
        } catch (RuntimeException ex) {
            throw new RuntimeException("Failed to start service " + service.getClass().getName() + ": onStart threw an exception", ex);
        }
    }

    public void stopService(SystemService service) {
        if (!HwBootFail.isBootSuccess() || !"1".equals(SystemProperties.get("sys.boot_completed"))) {
            Slog.i(TAG, "boot is not ready " + service.getClass().getName());
            return;
        }
        synchronized (this.mServices) {
            int index = this.mServices.indexOf(service);
            if (index >= 0) {
                this.mServices.remove(index);
                service.onStop();
                Slog.i(TAG, "stoping " + service.getClass().getName());
            }
        }
    }

    /* JADX INFO: finally extract failed */
    public void startBootPhase(int phase) {
        if (phase > this.mCurrentPhase) {
            this.mCurrentPhase = phase;
            Slog.i(TAG, "Starting phase " + this.mCurrentPhase);
            MetricsLogger.histogram((Context) null, "boot_starting_phase_" + this.mCurrentPhase, (int) SystemClock.elapsedRealtime());
            try {
                Trace.traceBegin(524288, "OnBootPhase " + phase);
                if (phase == 100) {
                    HwBootFail.setFrameworkBootStage(83886081);
                }
                int serviceLen = this.mServices.size();
                for (int i = 0; i < serviceLen; i++) {
                    SystemService service = this.mServices.get(i);
                    long time = SystemClock.elapsedRealtime();
                    Trace.traceBegin(524288, service.getClass().getName());
                    try {
                        service.onBootPhase(this.mCurrentPhase);
                        warnIfTooLong(SystemClock.elapsedRealtime() - time, service, "onBootPhase");
                        Trace.traceEnd(524288);
                    } catch (Exception ex) {
                        throw new RuntimeException("Failed to boot service " + service.getClass().getName() + ": onBootPhase threw an exception during phase " + this.mCurrentPhase, ex);
                    }
                }
                Trace.traceEnd(524288);
            } catch (Throwable th) {
                Trace.traceEnd(524288);
                throw th;
            }
        } else {
            throw new IllegalArgumentException("Next phase must be larger than previous");
        }
    }

    public boolean isBootCompleted() {
        return this.mCurrentPhase >= 1000;
    }

    public void startUser(int userHandle) {
        Slog.i(TAG, "Calling onStartUser u" + userHandle);
        int serviceLen = this.mServices.size();
        for (int i = 0; i < serviceLen; i++) {
            SystemService service = this.mServices.get(i);
            Trace.traceBegin(524288, "onStartUser " + service.getClass().getName());
            long time = SystemClock.elapsedRealtime();
            try {
                service.onStartUser(userHandle);
            } catch (Exception ex) {
                Slog.wtf(TAG, "Failure reporting start of user " + userHandle + " to service " + service.getClass().getName(), ex);
            }
            warnIfTooLong(SystemClock.elapsedRealtime() - time, service, "onStartUser ");
            Trace.traceEnd(524288);
        }
    }

    public void unlockUser(int userHandle) {
        Slog.i(TAG, "Calling onUnlockUser u" + userHandle);
        int serviceLen = this.mServices.size();
        for (int i = 0; i < serviceLen; i++) {
            SystemService service = this.mServices.get(i);
            Trace.traceBegin(524288, "onUnlockUser " + service.getClass().getName());
            long time = SystemClock.elapsedRealtime();
            try {
                service.onUnlockUser(userHandle);
            } catch (Exception ex) {
                Slog.wtf(TAG, "Failure reporting unlock of user " + userHandle + " to service " + service.getClass().getName(), ex);
            }
            warnIfTooLong(SystemClock.elapsedRealtime() - time, service, "onUnlockUser ");
            Trace.traceEnd(524288);
        }
    }

    public void switchUser(int userHandle) {
        Slog.i(TAG, "Calling switchUser u" + userHandle);
        int serviceLen = this.mServices.size();
        for (int i = 0; i < serviceLen; i++) {
            SystemService service = this.mServices.get(i);
            Trace.traceBegin(524288, "onSwitchUser " + service.getClass().getName());
            long time = SystemClock.elapsedRealtime();
            try {
                service.onSwitchUser(userHandle);
            } catch (Exception ex) {
                Slog.wtf(TAG, "Failure reporting switch of user " + userHandle + " to service " + service.getClass().getName(), ex);
            }
            warnIfTooLong(SystemClock.elapsedRealtime() - time, service, "onSwitchUser");
            Trace.traceEnd(524288);
        }
    }

    public void stopUser(int userHandle) {
        Slog.i(TAG, "Calling onStopUser u" + userHandle);
        int serviceLen = this.mServices.size();
        for (int i = 0; i < serviceLen; i++) {
            SystemService service = this.mServices.get(i);
            Trace.traceBegin(524288, "onStopUser " + service.getClass().getName());
            long time = SystemClock.elapsedRealtime();
            try {
                service.onStopUser(userHandle);
            } catch (Exception ex) {
                Slog.wtf(TAG, "Failure reporting stop of user " + userHandle + " to service " + service.getClass().getName(), ex);
            }
            warnIfTooLong(SystemClock.elapsedRealtime() - time, service, "onStopUser");
            Trace.traceEnd(524288);
        }
    }

    public void cleanupUser(int userHandle) {
        Slog.i(TAG, "Calling onCleanupUser u" + userHandle);
        int serviceLen = this.mServices.size();
        for (int i = 0; i < serviceLen; i++) {
            SystemService service = this.mServices.get(i);
            Trace.traceBegin(524288, "onCleanupUser " + service.getClass().getName());
            long time = SystemClock.elapsedRealtime();
            try {
                service.onCleanupUser(userHandle);
            } catch (Exception ex) {
                Slog.wtf(TAG, "Failure reporting cleanup of user " + userHandle + " to service " + service.getClass().getName(), ex);
            }
            warnIfTooLong(SystemClock.elapsedRealtime() - time, service, "onCleanupUser");
            Trace.traceEnd(524288);
        }
    }

    /* access modifiers changed from: package-private */
    public void setSafeMode(boolean safeMode) {
        this.mSafeMode = safeMode;
    }

    public boolean isSafeMode() {
        return this.mSafeMode;
    }

    public boolean isRuntimeRestarted() {
        return this.mRuntimeRestarted;
    }

    public long getRuntimeStartElapsedTime() {
        return this.mRuntimeStartElapsedTime;
    }

    public long getRuntimeStartUptime() {
        return this.mRuntimeStartUptime;
    }

    /* access modifiers changed from: package-private */
    public void setStartInfo(boolean runtimeRestarted, long runtimeStartElapsedTime, long runtimeStartUptime) {
        this.mRuntimeRestarted = runtimeRestarted;
        this.mRuntimeStartElapsedTime = runtimeStartElapsedTime;
        this.mRuntimeStartUptime = runtimeStartUptime;
    }

    private void warnIfTooLong(long duration, SystemService service, String operation) {
        if (duration > 50) {
            Slog.w(TAG, "Service " + service.getClass().getName() + " took " + duration + " ms in " + operation);
        }
    }

    @Deprecated
    public static File ensureSystemDir() {
        if (sSystemDir == null) {
            sSystemDir = new File(Environment.getDataDirectory(), "system");
            sSystemDir.mkdirs();
        }
        return sSystemDir;
    }

    public void dump() {
        StringBuilder builder = new StringBuilder();
        builder.append("Current phase: ");
        builder.append(this.mCurrentPhase);
        builder.append("\n");
        builder.append("Services:\n");
        int startedLen = this.mServices.size();
        for (int i = 0; i < startedLen; i++) {
            builder.append("\t");
            builder.append(this.mServices.get(i).getClass().getSimpleName());
            builder.append("\n");
        }
        Slog.e(TAG, builder.toString());
    }

    public String dumpInfo() {
        if (1000 != Binder.getCallingUid()) {
            Slog.e(TAG, "permission not allowed. uid = " + Binder.getCallingUid());
            return null;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("Current phase: ");
        builder.append(this.mCurrentPhase);
        builder.append("\n");
        builder.append("Services:\n");
        for (int i = this.mServices.size() - 1; i >= 0; i--) {
            builder.append("\t");
            builder.append(this.mServices.get(i).getClass().getSimpleName());
            builder.append("\n");
        }
        return builder.toString();
    }
}
