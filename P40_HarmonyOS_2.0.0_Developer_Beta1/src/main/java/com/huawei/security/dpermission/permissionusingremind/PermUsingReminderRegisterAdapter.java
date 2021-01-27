package com.huawei.security.dpermission.permissionusingremind;

import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.ArrayMap;
import com.huawei.security.dpermission.DPermissionInitializer;
import java.lang.Thread;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.security.permission.OnUsingPermissionReminder;
import ohos.security.permission.PermissionInner;
import ohos.security.permission.PermissionReminderInfo;

public class PermUsingReminderRegisterAdapter {
    private static final int DEFAULT_SIZE = 16;
    private static final int FAILURE = -1;
    private static final Object INSTANCE_LOCK = new Object();
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) DPermissionInitializer.DPERMISSION_LOG_ID, "PermUsingReminderRegisterAdapter");
    private static final int MAX_NEXT_RETRY_TIME_INDEX = 4;
    private static final int SUCCESS = 0;
    private static final int[] TIME_INTERVAL = {1, 1, 2, 3, 5};
    private static volatile PermUsingReminderRegisterAdapter sInstance;
    private final Map<IBinder, IBinder.DeathRecipient> mDeathEaters = new ArrayMap(16);
    private ReminderDelegate mReminderDelegate = new ReminderDelegate(new HarmonyOnUsingPermissionReminder());
    private final Object reminderLock = new Object();

    private PermUsingReminderRegisterAdapter() {
    }

    public static PermUsingReminderRegisterAdapter getInstance() {
        if (sInstance == null) {
            synchronized (INSTANCE_LOCK) {
                if (sInstance == null) {
                    sInstance = new PermUsingReminderRegisterAdapter();
                }
            }
        }
        return sInstance;
    }

    public int registerOnUsingPermissionReminder(IOnUsingPermissionReminder iOnUsingPermissionReminder) {
        if (iOnUsingPermissionReminder == null) {
            HiLog.warn(LABEL, "registerOnUsingPermissionReminder::callback should not be null", new Object[0]);
            return -1;
        }
        AndroidBinderDeathRecipient androidBinderDeathRecipient = new AndroidBinderDeathRecipient(iOnUsingPermissionReminder);
        synchronized (this.reminderLock) {
            this.mReminderDelegate.add(iOnUsingPermissionReminder);
            linkDeathRecipientLocked(iOnUsingPermissionReminder, androidBinderDeathRecipient);
        }
        return 0;
    }

    public int unregisterOnUsingPermissionReminder(IOnUsingPermissionReminder iOnUsingPermissionReminder) {
        if (iOnUsingPermissionReminder == null) {
            HiLog.warn(LABEL, "unregisterOnUsingPermissionReminder::callback should not be null", new Object[0]);
            return -1;
        }
        synchronized (this.reminderLock) {
            unlinkDeathRecipientLocked(iOnUsingPermissionReminder);
            this.mReminderDelegate.remove(iOnUsingPermissionReminder);
        }
        return 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchStartUsingPermission(PermissionReminderInfo permissionReminderInfo) {
        this.mReminderDelegate.dispatchStartUsingPermission(assembleInfoParcel(permissionReminderInfo));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchStopUsingPermission(PermissionReminderInfo permissionReminderInfo) {
        this.mReminderDelegate.dispatchStopUsingPermission(assembleInfoParcel(permissionReminderInfo));
    }

    private PermissionReminderInfoParcel assembleInfoParcel(PermissionReminderInfo permissionReminderInfo) {
        PermissionReminderInfoParcel permissionReminderInfoParcel = new PermissionReminderInfoParcel();
        permissionReminderInfoParcel.setDeviceId(permissionReminderInfo.getDeviceId());
        permissionReminderInfoParcel.setDeviceLabel(permissionReminderInfo.getDeviceLabel());
        permissionReminderInfoParcel.setBundleName(permissionReminderInfo.getBundleName());
        permissionReminderInfoParcel.setBundleLabel(permissionReminderInfo.getBundleLabel());
        permissionReminderInfoParcel.setPermName(permissionReminderInfo.getPermName());
        return permissionReminderInfoParcel;
    }

    private void linkDeathRecipientLocked(IOnUsingPermissionReminder iOnUsingPermissionReminder, IBinder.DeathRecipient deathRecipient) {
        try {
            IBinder asBinder = iOnUsingPermissionReminder.asBinder();
            asBinder.linkToDeath(deathRecipient, 0);
            this.mDeathEaters.put(asBinder, deathRecipient);
        } catch (RemoteException unused) {
            HiLog.error(LABEL, "Unable to link to death for using permission reminder callback", new Object[0]);
        }
    }

    private void unlinkDeathRecipientLocked(IOnUsingPermissionReminder iOnUsingPermissionReminder) {
        IBinder asBinder = iOnUsingPermissionReminder.asBinder();
        IBinder.DeathRecipient remove = this.mDeathEaters.remove(asBinder);
        if (remove != null) {
            asBinder.unlinkToDeath(remove, 0);
        }
    }

    /* access modifiers changed from: private */
    public static void registerHarmonyReminderWithThread(OnUsingPermissionReminder onUsingPermissionReminder) {
        Thread thread = new Thread(new RegisterReminderTask(onUsingPermissionReminder));
        thread.setName("RegisterReminderThread");
        thread.setUncaughtExceptionHandler(new RegisterUncaughtExceptionHandler());
        thread.start();
    }

    /* access modifiers changed from: private */
    public static final class ReminderDelegate {
        private Handler mHandler = new Handler(Looper.getMainLooper());
        private Object mLock = new Object();
        private Map<IBinder, IOnUsingPermissionReminder> mReminderCallbacks = new ArrayMap(16);
        private OnUsingPermissionReminder reminder;

        ReminderDelegate(OnUsingPermissionReminder onUsingPermissionReminder) {
            this.reminder = onUsingPermissionReminder;
        }

        public void add(IOnUsingPermissionReminder iOnUsingPermissionReminder) {
            synchronized (this.mLock) {
                if (this.mReminderCallbacks.isEmpty()) {
                    PermUsingReminderRegisterAdapter.registerHarmonyReminderWithThread(this.reminder);
                }
                this.mReminderCallbacks.put(iOnUsingPermissionReminder.asBinder(), iOnUsingPermissionReminder);
                HiLog.debug(PermUsingReminderRegisterAdapter.LABEL, "After add, mReminderCallbacks size: %{public}d", new Object[]{Integer.valueOf(this.mReminderCallbacks.size())});
            }
        }

        public void remove(IOnUsingPermissionReminder iOnUsingPermissionReminder) {
            synchronized (this.mLock) {
                this.mReminderCallbacks.remove(iOnUsingPermissionReminder.asBinder());
                HiLog.debug(PermUsingReminderRegisterAdapter.LABEL, "After remove, mReminderCallbacks size: %{public}d", new Object[]{Integer.valueOf(this.mReminderCallbacks.size())});
                if (this.mReminderCallbacks.isEmpty()) {
                    HiLog.debug(PermUsingReminderRegisterAdapter.LABEL, "No subscribers on the Android side, unregister reminder at Harmony side", new Object[0]);
                    PermissionInner.unregisterUsingPermissionReminder(this.reminder);
                }
            }
        }

        public void dispatchStartUsingPermission(PermissionReminderInfoParcel permissionReminderInfoParcel) {
            synchronized (this.mLock) {
                for (IOnUsingPermissionReminder iOnUsingPermissionReminder : this.mReminderCallbacks.values()) {
                    this.mHandler.post(new StartUsingPermissionCallback(iOnUsingPermissionReminder, permissionReminderInfoParcel));
                }
            }
        }

        public void dispatchStopUsingPermission(PermissionReminderInfoParcel permissionReminderInfoParcel) {
            synchronized (this.mLock) {
                for (IOnUsingPermissionReminder iOnUsingPermissionReminder : this.mReminderCallbacks.values()) {
                    this.mHandler.post(new StopUsingPermissionCallback(iOnUsingPermissionReminder, permissionReminderInfoParcel));
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class StartUsingPermissionCallback implements Runnable {
        private IOnUsingPermissionReminder mCallback;
        private PermissionReminderInfoParcel mInfoParcel;

        StartUsingPermissionCallback(IOnUsingPermissionReminder iOnUsingPermissionReminder, PermissionReminderInfoParcel permissionReminderInfoParcel) {
            this.mCallback = iOnUsingPermissionReminder;
            this.mInfoParcel = permissionReminderInfoParcel;
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                this.mCallback.startUsingPermission(this.mInfoParcel);
            } catch (RemoteException unused) {
                HiLog.error(PermUsingReminderRegisterAdapter.LABEL, "Failed to notify permission start using", new Object[0]);
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class StopUsingPermissionCallback implements Runnable {
        private IOnUsingPermissionReminder mCallback;
        private PermissionReminderInfoParcel mInfoParcel;

        StopUsingPermissionCallback(IOnUsingPermissionReminder iOnUsingPermissionReminder, PermissionReminderInfoParcel permissionReminderInfoParcel) {
            this.mCallback = iOnUsingPermissionReminder;
            this.mInfoParcel = permissionReminderInfoParcel;
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                this.mCallback.stopUsingPermission(this.mInfoParcel);
            } catch (RemoteException unused) {
                HiLog.error(PermUsingReminderRegisterAdapter.LABEL, "Failed to notify permission stop using", new Object[0]);
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class RegisterReminderTask implements Runnable {
        private int nextInterval;
        private OnUsingPermissionReminder reminder;

        RegisterReminderTask(OnUsingPermissionReminder onUsingPermissionReminder) {
            this.reminder = onUsingPermissionReminder;
        }

        @Override // java.lang.Runnable
        public void run() {
            HiLog.info(PermUsingReminderRegisterAdapter.LABEL, "begin register reminder to dpms", new Object[0]);
            int registerUsingPermissionReminder = PermissionInner.registerUsingPermissionReminder(this.reminder);
            while (registerUsingPermissionReminder != 0 && this.nextInterval <= 4) {
                try {
                    TimeUnit.SECONDS.sleep((long) PermUsingReminderRegisterAdapter.TIME_INTERVAL[this.nextInterval]);
                } catch (InterruptedException unused) {
                    HiLog.error(PermUsingReminderRegisterAdapter.LABEL, "RegisterReminderTask encounter InterruptedException", new Object[0]);
                }
                this.nextInterval++;
                registerUsingPermissionReminder = PermissionInner.registerUsingPermissionReminder(this.reminder);
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class RegisterUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
        private RegisterUncaughtExceptionHandler() {
        }

        @Override // java.lang.Thread.UncaughtExceptionHandler
        public void uncaughtException(Thread thread, Throwable th) {
            HiLog.error(PermUsingReminderRegisterAdapter.LABEL, "RegisterReminderThread: unknown exception", new Object[0]);
        }
    }

    /* access modifiers changed from: private */
    public final class AndroidBinderDeathRecipient implements IBinder.DeathRecipient {
        private IOnUsingPermissionReminder callback;

        AndroidBinderDeathRecipient(IOnUsingPermissionReminder iOnUsingPermissionReminder) {
            this.callback = iOnUsingPermissionReminder;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            HiLog.warn(PermUsingReminderRegisterAdapter.LABEL, "binder died: %{public}s", new Object[]{this.callback.toString()});
            PermUsingReminderRegisterAdapter.this.unregisterOnUsingPermissionReminder(this.callback);
        }
    }

    private final class HarmonyOnUsingPermissionReminder extends OnUsingPermissionReminder {
        private HarmonyOnUsingPermissionReminder() {
        }

        public void startUsingPermission(PermissionReminderInfo permissionReminderInfo) {
            HiLog.debug(PermUsingReminderRegisterAdapter.LABEL, "startUsingPermission called, info:%{private}s", new Object[]{permissionReminderInfo.toString()});
            PermUsingReminderRegisterAdapter.this.dispatchStartUsingPermission(permissionReminderInfo);
        }

        public void stopUsingPermission(PermissionReminderInfo permissionReminderInfo) {
            HiLog.debug(PermUsingReminderRegisterAdapter.LABEL, "stopUsingPermission called, info:%{private}s", new Object[]{permissionReminderInfo.toString()});
            PermUsingReminderRegisterAdapter.this.dispatchStopUsingPermission(permissionReminderInfo);
        }
    }
}
