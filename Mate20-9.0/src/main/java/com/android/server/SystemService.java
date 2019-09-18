package com.android.server;

import android.app.ActivityThread;
import android.content.Context;
import android.os.IBinder;
import android.os.ServiceManager;

public abstract class SystemService {
    public static final int PHASE_ACTIVITY_MANAGER_READY = 550;
    public static final int PHASE_BOOT_COMPLETED = 1000;
    public static final int PHASE_DEVICE_SPECIFIC_SERVICES_READY = 520;
    public static final int PHASE_LOCK_SETTINGS_READY = 480;
    public static final int PHASE_SYSTEM_SERVICES_READY = 500;
    public static final int PHASE_THIRD_PARTY_APPS_CAN_START = 600;
    public static final int PHASE_WAIT_FOR_DEFAULT_DISPLAY = 100;
    private final Context mContext;

    public abstract void onStart();

    public SystemService(Context context) {
        this.mContext = context;
    }

    public final Context getContext() {
        return this.mContext;
    }

    public final Context getUiContext() {
        return ActivityThread.currentActivityThread().getSystemUiContext();
    }

    public final boolean isSafeMode() {
        return getManager().isSafeMode();
    }

    public void onBootPhase(int phase) {
    }

    public void onStartUser(int userHandle) {
    }

    public void onUnlockUser(int userHandle) {
    }

    public void onSwitchUser(int userHandle) {
    }

    public void onStopUser(int userHandle) {
    }

    public void onCleanupUser(int userHandle) {
    }

    /* access modifiers changed from: protected */
    public final void publishBinderService(String name, IBinder service) {
        publishBinderService(name, service, false);
    }

    /* access modifiers changed from: protected */
    public final void publishBinderService(String name, IBinder service, boolean allowIsolated) {
        publishBinderService(name, service, allowIsolated, 8);
    }

    /* access modifiers changed from: protected */
    public final void publishBinderService(String name, IBinder service, boolean allowIsolated, int dumpPriority) {
        ServiceManager.addService(name, service, allowIsolated, dumpPriority);
    }

    /* access modifiers changed from: protected */
    public final IBinder getBinderService(String name) {
        return ServiceManager.getService(name);
    }

    /* access modifiers changed from: protected */
    public final <T> void publishLocalService(Class<T> type, T service) {
        LocalServices.addService(type, service);
    }

    /* access modifiers changed from: protected */
    public final <T> T getLocalService(Class<T> type) {
        return LocalServices.getService(type);
    }

    private SystemServiceManager getManager() {
        return (SystemServiceManager) LocalServices.getService(SystemServiceManager.class);
    }
}
