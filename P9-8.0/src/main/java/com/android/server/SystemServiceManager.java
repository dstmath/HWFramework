package com.android.server;

import android.content.Context;
import android.os.Binder;
import android.os.Trace;
import android.util.Slog;
import com.android.server.os.HwBootFail;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class SystemServiceManager {
    private static final int SERVICE_CALL_WARN_TIME_MS = 50;
    private static final String TAG = "SystemServiceManager";
    private final Context mContext;
    private int mCurrentPhase = -1;
    private boolean mRuntimeRestarted;
    private boolean mSafeMode;
    private final ArrayList<SystemService> mServices = new ArrayList();

    SystemServiceManager(Context context) {
        this.mContext = context;
    }

    public SystemService startService(String className) {
        try {
            return startService(Class.forName(className));
        } catch (ClassNotFoundException ex) {
            Slog.i(TAG, "Starting " + className);
            throw new RuntimeException("Failed to create service " + className + ": service class not found, usually indicates that the caller should " + "have called PackageManager.hasSystemFeature() to check whether the " + "feature is available on this device before trying to start the " + "services that implement it", ex);
        }
    }

    public <T extends SystemService> T startService(Class<T> serviceClass) {
        String name;
        try {
            name = serviceClass.getName();
            Trace.traceBegin(524288, "StartService " + name);
            if (SystemService.class.isAssignableFrom(serviceClass)) {
                SystemService service = (SystemService) serviceClass.getConstructor(new Class[]{Context.class}).newInstance(new Object[]{this.mContext});
                startService(service);
                Trace.traceEnd(524288);
                return service;
            }
            throw new RuntimeException("Failed to create " + name + ": service must extend " + SystemService.class.getName());
        } catch (InstantiationException ex) {
            throw new RuntimeException("Failed to create service " + name + ": service could not be instantiated", ex);
        } catch (IllegalAccessException ex2) {
            throw new RuntimeException("Failed to create service " + name + ": service must have a public constructor with a Context argument", ex2);
        } catch (NoSuchMethodException ex3) {
            throw new RuntimeException("Failed to create service " + name + ": service must have a public constructor with a Context argument", ex3);
        } catch (InvocationTargetException ex4) {
            throw new RuntimeException("Failed to create service " + name + ": service constructor threw an exception", ex4);
        } catch (Throwable th) {
            Trace.traceEnd(524288);
        }
    }

    public void startService(SystemService service) {
        synchronized (this.mServices) {
            this.mServices.add(service);
        }
        long time = System.currentTimeMillis();
        try {
            service.onStart();
            warnIfTooLong(System.currentTimeMillis() - time, service, "onStart");
        } catch (RuntimeException ex) {
            throw new RuntimeException("Failed to start service " + service.getClass().getName() + ": onStart threw an exception", ex);
        }
    }

    public void startBootPhase(int phase) {
        if (phase <= this.mCurrentPhase) {
            throw new IllegalArgumentException("Next phase must be larger than previous");
        }
        this.mCurrentPhase = phase;
        Slog.i(TAG, "Starting phase " + this.mCurrentPhase);
        int i;
        SystemService service;
        try {
            Trace.traceBegin(524288, "OnBootPhase " + phase);
            HwBootFail.setBootStage(HwBootFail.changeBootStage(phase));
            int serviceLen = this.mServices.size();
            i = 0;
            while (i < serviceLen) {
                service = (SystemService) this.mServices.get(i);
                long time = System.currentTimeMillis();
                Trace.traceBegin(524288, service.getClass().getName());
                service.onBootPhase(this.mCurrentPhase);
                warnIfTooLong(System.currentTimeMillis() - time, service, "onBootPhase");
                Trace.traceEnd(524288);
                i++;
            }
            Trace.traceEnd(524288);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to boot service " + (service == null ? "null at index " + i : service.getClass().getName()) + ": onBootPhase threw an exception during phase " + this.mCurrentPhase, ex);
        } catch (Throwable th) {
            Trace.traceEnd(524288);
        }
    }

    public boolean isBootCompleted() {
        return this.mCurrentPhase >= 1000;
    }

    public void startUser(int userHandle) {
        Slog.i(TAG, "Calling onStartUser u" + userHandle);
        int serviceLen = this.mServices.size();
        for (int i = 0; i < serviceLen; i++) {
            SystemService service = (SystemService) this.mServices.get(i);
            Trace.traceBegin(524288, "onStartUser " + service.getClass().getName());
            long time = System.currentTimeMillis();
            try {
                service.onStartUser(userHandle);
            } catch (Exception ex) {
                Slog.wtf(TAG, "Failure reporting start of user " + userHandle + " to service " + service.getClass().getName(), ex);
            }
            warnIfTooLong(System.currentTimeMillis() - time, service, "onStartUser ");
            Trace.traceEnd(524288);
        }
    }

    public void unlockUser(int userHandle) {
        Slog.i(TAG, "Calling onUnlockUser u" + userHandle);
        int serviceLen = this.mServices.size();
        for (int i = 0; i < serviceLen; i++) {
            SystemService service = (SystemService) this.mServices.get(i);
            Trace.traceBegin(524288, "onUnlockUser " + service.getClass().getName());
            long time = System.currentTimeMillis();
            try {
                service.onUnlockUser(userHandle);
            } catch (Exception ex) {
                Slog.wtf(TAG, "Failure reporting unlock of user " + userHandle + " to service " + service.getClass().getName(), ex);
            }
            warnIfTooLong(System.currentTimeMillis() - time, service, "onUnlockUser ");
            Trace.traceEnd(524288);
        }
    }

    public void switchUser(int userHandle) {
        Slog.i(TAG, "Calling switchUser u" + userHandle);
        int serviceLen = this.mServices.size();
        for (int i = 0; i < serviceLen; i++) {
            SystemService service = (SystemService) this.mServices.get(i);
            Trace.traceBegin(524288, "onSwitchUser " + service.getClass().getName());
            long time = System.currentTimeMillis();
            try {
                service.onSwitchUser(userHandle);
            } catch (Exception ex) {
                Slog.wtf(TAG, "Failure reporting switch of user " + userHandle + " to service " + service.getClass().getName(), ex);
            }
            warnIfTooLong(System.currentTimeMillis() - time, service, "onSwitchUser");
            Trace.traceEnd(524288);
        }
    }

    public void stopUser(int userHandle) {
        Slog.i(TAG, "Calling onStopUser u" + userHandle);
        int serviceLen = this.mServices.size();
        for (int i = 0; i < serviceLen; i++) {
            SystemService service = (SystemService) this.mServices.get(i);
            Trace.traceBegin(524288, "onStopUser " + service.getClass().getName());
            long time = System.currentTimeMillis();
            try {
                service.onStopUser(userHandle);
            } catch (Exception ex) {
                Slog.wtf(TAG, "Failure reporting stop of user " + userHandle + " to service " + service.getClass().getName(), ex);
            }
            warnIfTooLong(System.currentTimeMillis() - time, service, "onStopUser");
            Trace.traceEnd(524288);
        }
    }

    public void cleanupUser(int userHandle) {
        Slog.i(TAG, "Calling onCleanupUser u" + userHandle);
        int serviceLen = this.mServices.size();
        for (int i = 0; i < serviceLen; i++) {
            SystemService service = (SystemService) this.mServices.get(i);
            Trace.traceBegin(524288, "onCleanupUser " + service.getClass().getName());
            long time = System.currentTimeMillis();
            try {
                service.onCleanupUser(userHandle);
            } catch (Exception ex) {
                Slog.wtf(TAG, "Failure reporting cleanup of user " + userHandle + " to service " + service.getClass().getName(), ex);
            }
            warnIfTooLong(System.currentTimeMillis() - time, service, "onCleanupUser");
            Trace.traceEnd(524288);
        }
    }

    void setSafeMode(boolean safeMode) {
        this.mSafeMode = safeMode;
    }

    public boolean isSafeMode() {
        return this.mSafeMode;
    }

    public boolean isRuntimeRestarted() {
        return this.mRuntimeRestarted;
    }

    void setRuntimeRestarted(boolean runtimeRestarted) {
        this.mRuntimeRestarted = runtimeRestarted;
    }

    private void warnIfTooLong(long duration, SystemService service, String operation) {
        if (duration > 50) {
            Slog.w(TAG, "Service " + service.getClass().getName() + " took " + duration + " ms in " + operation);
        }
    }

    public void dump() {
        StringBuilder builder = new StringBuilder();
        builder.append("Current phase: ").append(this.mCurrentPhase).append("\n");
        builder.append("Services:\n");
        int startedLen = this.mServices.size();
        for (int i = 0; i < startedLen; i++) {
            builder.append("\t").append(((SystemService) this.mServices.get(i)).getClass().getSimpleName()).append("\n");
        }
        Slog.e(TAG, builder.toString());
    }

    public String dumpInfo() {
        if (1000 != Binder.getCallingUid()) {
            Slog.e(TAG, "permission not allowed. uid = " + Binder.getCallingUid());
            return null;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("Current phase: ").append(this.mCurrentPhase).append("\n");
        builder.append("Services:\n");
        int startedLen = this.mServices.size();
        for (int i = 0; i < startedLen; i++) {
            builder.append("\t").append(((SystemService) this.mServices.get(i)).getClass().getSimpleName()).append("\n");
        }
        return builder.toString();
    }
}
