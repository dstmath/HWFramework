package com.android.server.power;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.IActivityManager;
import android.app.ProgressDialog;
import android.app.admin.SecurityLog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.os.Binder;
import android.os.FileUtils;
import android.os.Handler;
import android.os.PowerManager;
import android.os.RecoverySystem;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.SystemVibrator;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.ArrayMap;
import android.util.Flog;
import android.util.Log;
import android.util.TimingsTraceLog;
import com.android.internal.os.HwBootAnimationOeminfo;
import com.android.internal.telephony.ITelephony;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.RescueParty;
import com.android.server.job.controllers.JobStatus;
import com.android.server.pm.PackageManagerService;
import com.android.server.policy.HwPolicyFactory;
import com.android.server.statusbar.StatusBarManagerInternal;
import com.huawei.android.app.HwAlarmManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class ShutdownThread extends Thread {
    private static final String ACTION_ACTURAL_SHUTDOWN = "com.android.internal.app.SHUTDOWNBROADCAST";
    private static final int ACTION_DONE_POLL_WAIT_MS = 500;
    private static final String ACTION_HWSYSTEMMANAGER_SHUTDOWN_LIMIT_POWERMODE = "huawei.intent.action.HWSYSTEMMANAGER_SHUTDOWN_LIMIT_POWERMODE";
    private static final int ACTIVITY_MANAGER_STOP_PERCENT = 4;
    private static final int BROADCAST_STOP_PERCENT = 2;
    private static final int MAX_BROADCAST_TIME = 10000;
    private static final int MAX_RADIO_WAIT_TIME = 12000;
    private static final int MAX_SHUTDOWN_WAIT_TIME = 20000;
    private static final int MAX_UNCRYPT_WAIT_TIME = 900000;
    private static final String MDPP_TAG = "MDPPWriteEvent";
    private static final String METRICS_FILE_BASENAME = "/data/system/shutdown-metrics";
    private static String METRIC_AM = "shutdown_activity_manager";
    private static String METRIC_PM = "shutdown_package_manager";
    private static String METRIC_RADIO = "shutdown_radio";
    private static String METRIC_RADIOS = "shutdown_radios";
    private static String METRIC_SEND_BROADCAST = "shutdown_send_shutdown_broadcast";
    private static String METRIC_SHUTDOWN_TIME_START = "begin_shutdown";
    private static String METRIC_SYSTEM_SERVER = "shutdown_system_server";
    private static final int MOUNT_SERVICE_STOP_PERCENT = 20;
    private static final int PACKAGE_MANAGER_STOP_PERCENT = 6;
    private static final int RADIOS_STATE_POLL_SLEEP_MS = 100;
    private static final int RADIO_STOP_PERCENT = 18;
    public static final String REBOOT_SAFEMODE_PROPERTY = "persist.sys.safemode";
    public static final String RO_SAFEMODE_PROPERTY = "ro.sys.safemode";
    public static final String SHUTDOWN_ACTION_PROPERTY = "sys.shutdown.requested";
    private static final int SHUTDOWN_VIBRATE_MS = 500;
    private static final String TAG = "ShutdownThread";
    private static final ArrayMap<String, Long> TRON_METRICS = new ArrayMap<>();
    private static final AudioAttributes VIBRATION_ATTRIBUTES = new AudioAttributes.Builder().setContentType(4).setUsage(13).build();
    private static IHwShutdownThread iHwShutdownThread = HwServiceFactory.getHwShutdownThread();
    private static boolean mFastShutdownEnable = false;
    private static String mReason;
    private static boolean mReboot;
    private static boolean mRebootHasProgressBar;
    private static boolean mRebootSafeMode;
    private static AlertDialog sConfirmDialog;
    private static final ShutdownThread sInstance = new ShutdownThread();
    private static boolean sIsStarted = false;
    private static final Object sIsStartedGuard = new Object();
    private boolean mActionDone;
    private final Object mActionDoneSync = new Object();
    private Context mContext;
    private PowerManager.WakeLock mCpuWakeLock;
    private Handler mHandler;
    private PowerManager mPowerManager;
    private ProgressDialog mProgressDialog;
    private PowerManager.WakeLock mScreenWakeLock;

    private ShutdownThread() {
    }

    public static void shutdown(Context context, String reason, boolean confirm) {
        mReboot = false;
        mRebootSafeMode = false;
        mReason = reason;
        iHwShutdownThread.resetValues();
        shutdownInner(context, confirm);
    }

    private static void shutdownInner(final Context context, boolean confirm) {
        int resourceId;
        int i;
        context.assertRuntimeOverlayThemable();
        synchronized (sIsStartedGuard) {
            if (sIsStarted) {
                Log.d(TAG, "Request to shutdown already running, returning.");
                return;
            }
        }
        int longPressBehavior = context.getResources().getInteger(17694826);
        if (mRebootSafeMode) {
            resourceId = 17041097;
        } else if (longPressBehavior == 2) {
            resourceId = 17041229;
        } else {
            resourceId = 17041228;
        }
        Log.d(TAG, "Notifying thread to start shutdown longPressBehavior=" + longPressBehavior);
        if (!confirm) {
            beginShutdownSequence(context);
        } else if (!HwServiceFactory.getHwShutdownThread().needRebootDialog(mReason, context)) {
            CloseDialogReceiver closer = new CloseDialogReceiver(context);
            AlertDialog alertDialog = sConfirmDialog;
            if (alertDialog != null) {
                alertDialog.dismiss();
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(context, 33947691);
            if (mRebootSafeMode) {
                i = 17041098;
            } else {
                i = 17041073;
            }
            sConfirmDialog = builder.setTitle(i).setMessage(resourceId).setPositiveButton(17039379, new DialogInterface.OnClickListener() {
                /* class com.android.server.power.ShutdownThread.AnonymousClass1 */

                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialog, int which) {
                    ShutdownThread.beginShutdownSequence(context);
                }
            }).setNegativeButton(17039369, (DialogInterface.OnClickListener) null).create();
            AlertDialog alertDialog2 = sConfirmDialog;
            closer.dialog = alertDialog2;
            alertDialog2.setOnDismissListener(closer);
            sConfirmDialog.getWindow().setType(2009);
            sConfirmDialog.show();
        }
    }

    public static class CloseDialogReceiver extends BroadcastReceiver implements DialogInterface.OnDismissListener {
        public Dialog dialog;
        private Context mContext;

        public CloseDialogReceiver(Context context) {
            this.mContext = context;
            context.registerReceiver(this, new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Dialog dialog2 = this.dialog;
            if (dialog2 != null) {
                dialog2.cancel();
            }
        }

        @Override // android.content.DialogInterface.OnDismissListener
        public void onDismiss(DialogInterface unused) {
            this.mContext.unregisterReceiver(this);
        }
    }

    public static void reboot(Context context, String reason, boolean confirm) {
        mReboot = true;
        mRebootSafeMode = false;
        mRebootHasProgressBar = false;
        mReason = reason;
        shutdownInner(context, confirm);
    }

    public static void rebootSafeMode(Context context, boolean confirm) {
        if (!((UserManager) context.getSystemService("user")).hasUserRestriction("no_safe_boot")) {
            mReboot = true;
            mRebootSafeMode = true;
            mRebootHasProgressBar = false;
            mReason = null;
            shutdownInner(context, confirm);
        }
    }

    private static ProgressDialog showShutdownDialog(Context context) {
        ProgressDialog pd = new ProgressDialog(context);
        String str = mReason;
        if (str == null || !str.startsWith("recovery-update")) {
            String str2 = mReason;
            if (str2 == null || !str2.equals("recovery")) {
                if (showSysuiReboot()) {
                    return null;
                }
                pd.setTitle(context.getText(17041073));
                pd.setMessage(context.getText(17041230));
                pd.setIndeterminate(true);
            } else if (RescueParty.isAttemptingFactoryReset()) {
                pd.setTitle(context.getText(17041073));
                pd.setMessage(context.getText(17041230));
                pd.setIndeterminate(true);
            } else {
                pd.setTitle(context.getText(17041100));
                pd.setMessage(context.getText(17041099));
                pd.setIndeterminate(true);
            }
        } else {
            mRebootHasProgressBar = RecoverySystem.UNCRYPT_PACKAGE_FILE.exists() && !RecoverySystem.BLOCK_MAP_FILE.exists();
            pd.setTitle(context.getText(17041104));
            if (mRebootHasProgressBar) {
                pd.setMax(100);
                pd.setProgress(0);
                pd.setIndeterminate(false);
                pd.setProgressNumberFormat(null);
                pd.setProgressStyle(1);
                pd.setMessage(context.getText(17041102));
            } else if (showSysuiReboot()) {
                return null;
            } else {
                pd.setIndeterminate(true);
                pd.setMessage(context.getText(17041103));
            }
        }
        pd.setCancelable(false);
        pd.getWindow().setType(2009);
        pd.show();
        return pd;
    }

    private static boolean showSysuiReboot() {
        Log.d(TAG, "Attempting to use SysUI shutdown UI");
        try {
            if (((StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class)).showShutdownUi(mReboot, mReason)) {
                Log.d(TAG, "SysUI handling shutdown UI");
                return true;
            }
        } catch (Exception e) {
        }
        Log.d(TAG, "SysUI is unavailable");
        return false;
    }

    public static void beginShutdownSequence(Context context) {
        iHwShutdownThread.onEarlyShutdownBegin(mReboot, mRebootSafeMode, mReason);
        if (SystemProperties.getBoolean("sys.super_power_save", false)) {
            Flog.d(1600, "send ACTION_HWSYSTEMMANAGER_SHUTDOWN_LIMIT_POWERMODE");
            Intent intentPowerMode = new Intent(ACTION_HWSYSTEMMANAGER_SHUTDOWN_LIMIT_POWERMODE);
            intentPowerMode.putExtra("shutdomn_limit_powermode", 0);
            intentPowerMode.addFlags(268435456);
            context.sendBroadcast(intentPowerMode);
        }
        context.sendBroadcast(new Intent(ACTION_ACTURAL_SHUTDOWN));
        synchronized (sIsStartedGuard) {
            if (sIsStarted) {
                Log.d(TAG, "Shutdown sequence already running, returning.");
                return;
            }
            sIsStarted = true;
        }
        int uid = Binder.getCallingUid();
        int pid = Binder.getCallingPid();
        Flog.e(1600, "ShutdownThread beginShutdownSequence uid=" + uid + ", pid=" + pid);
        if (!iHwShutdownThread.isDoShutdownAnimation() && !iHwShutdownThread.needRebootProgressDialog(mReboot, context) && !HwPolicyFactory.isHwGlobalActionsShowing()) {
            sInstance.mProgressDialog = showShutdownDialog(context);
        }
        mFastShutdownEnable = HwPolicyFactory.isHwFastShutdownEnable();
        if (mFastShutdownEnable) {
            try {
                new SystemVibrator(context).vibrate(500, VIBRATION_ATTRIBUTES);
            } catch (Exception e) {
                Log.w(TAG, "Failed to vibrate during shutdown under condition of fastshutdown mode.", e);
            }
        }
        ShutdownThread shutdownThread = sInstance;
        shutdownThread.mContext = context;
        shutdownThread.mPowerManager = (PowerManager) context.getSystemService("power");
        ShutdownThread shutdownThread2 = sInstance;
        shutdownThread2.mCpuWakeLock = null;
        try {
            shutdownThread2.mCpuWakeLock = shutdownThread2.mPowerManager.newWakeLock(1, "ShutdownThread-cpu");
            sInstance.mCpuWakeLock.setReferenceCounted(false);
            sInstance.mCpuWakeLock.acquire();
        } catch (SecurityException e2) {
            Log.w(TAG, "No permission to acquire wake lock", e2);
            sInstance.mCpuWakeLock = null;
        }
        ShutdownThread shutdownThread3 = sInstance;
        shutdownThread3.mScreenWakeLock = null;
        if (shutdownThread3.mPowerManager.isScreenOn()) {
            try {
                sInstance.mScreenWakeLock = sInstance.mPowerManager.newWakeLock(26, "ShutdownThread-screen");
                sInstance.mScreenWakeLock.setReferenceCounted(false);
                sInstance.mScreenWakeLock.acquire();
            } catch (SecurityException e3) {
                Log.w(TAG, "No permission to acquire wake lock", e3);
                sInstance.mScreenWakeLock = null;
            }
        }
        if (SecurityLog.isLoggingEnabled()) {
            SecurityLog.writeEvent(210010, new Object[0]);
            Log.i(MDPP_TAG, "TAG 210010");
        }
        sInstance.mHandler = new Handler() {
            /* class com.android.server.power.ShutdownThread.AnonymousClass2 */
        };
        sInstance.start();
    }

    /* access modifiers changed from: package-private */
    public void actionDone() {
        synchronized (this.mActionDoneSync) {
            this.mActionDone = true;
            this.mActionDoneSync.notifyAll();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
        android.util.Log.w(com.android.server.power.ShutdownThread.TAG, "Shutdown broadcast timed out");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x00ab, code lost:
        r0 = th;
     */
    @Override // java.lang.Thread, java.lang.Runnable
    public void run() {
        Throwable th;
        long endTime;
        TimingsTraceLog shutdownTimingLog = newTimingsLog();
        shutdownTimingLog.traceBegin("SystemServerShutdown");
        metricShutdownStart();
        metricStarted(METRIC_SYSTEM_SERVER);
        BroadcastReceiver br = new BroadcastReceiver() {
            /* class com.android.server.power.ShutdownThread.AnonymousClass3 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                ShutdownThread.this.actionDone();
            }
        };
        Log.i(TAG, "shutdownThread setHwRTCAlarm");
        HwAlarmManager.setHwRTCAlarm();
        StringBuilder sb = new StringBuilder();
        sb.append(mReboot ? "1" : "0");
        String str = mReason;
        if (str == null) {
            str = "";
        }
        sb.append(str);
        SystemProperties.set(SHUTDOWN_ACTION_PROPERTY, sb.toString());
        if (mRebootSafeMode) {
            SystemProperties.set(REBOOT_SAFEMODE_PROPERTY, "1");
        }
        metricStarted(METRIC_SEND_BROADCAST);
        shutdownTimingLog.traceBegin("SendShutdownBroadcast");
        Log.i(TAG, "Sending shutdown broadcast...");
        long shutDownBegin = SystemClock.elapsedRealtime();
        this.mActionDone = false;
        Intent intent = new Intent("android.intent.action.ACTION_SHUTDOWN");
        intent.addFlags(1342177280);
        this.mContext.sendOrderedBroadcastAsUser(intent, UserHandle.ALL, null, br, this.mHandler, 0, null, null);
        long endTime2 = SystemClock.elapsedRealtime() + JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY;
        synchronized (this.mActionDoneSync) {
            while (true) {
                try {
                    if (this.mActionDone) {
                        break;
                    }
                    long delay = endTime2 - SystemClock.elapsedRealtime();
                    if (delay <= 0) {
                        break;
                    }
                    if (mRebootHasProgressBar) {
                        endTime = endTime2;
                        try {
                            sInstance.setRebootProgress((int) (((((double) (JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY - delay)) * 1.0d) * 2.0d) / 10000.0d), null);
                        } catch (Throwable th2) {
                            th = th2;
                        }
                    } else {
                        endTime = endTime2;
                    }
                    try {
                        this.mActionDoneSync.wait(Math.min(delay, 500L));
                    } catch (InterruptedException e) {
                    }
                    endTime2 = endTime;
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
        }
        if (mRebootHasProgressBar) {
            sInstance.setRebootProgress(2, null);
        }
        shutdownTimingLog.traceEnd();
        metricEnded(METRIC_SEND_BROADCAST);
        Log.i(TAG, "Shutting down activity manager...");
        shutdownTimingLog.traceBegin("ShutdownActivityManager");
        metricStarted(METRIC_AM);
        IActivityManager am = IActivityManager.Stub.asInterface(ServiceManager.checkService("activity"));
        if (am != null) {
            try {
                am.shutdown(10000);
            } catch (RemoteException e2) {
            }
        }
        if (mRebootHasProgressBar) {
            sInstance.setRebootProgress(4, null);
        }
        shutdownTimingLog.traceEnd();
        metricEnded(METRIC_AM);
        Log.i(TAG, "Shutting down package manager...");
        shutdownTimingLog.traceBegin("ShutdownPackageManager");
        metricStarted(METRIC_PM);
        PackageManagerService pm = (PackageManagerService) ServiceManager.getService("package");
        if (pm != null) {
            pm.shutdown();
        }
        if (mRebootHasProgressBar) {
            sInstance.setRebootProgress(6, null);
        }
        shutdownTimingLog.traceEnd();
        metricEnded(METRIC_PM);
        shutdownTimingLog.traceBegin("ShutdownRadios");
        metricStarted(METRIC_RADIOS);
        shutdownRadios(MAX_RADIO_WAIT_TIME);
        if (mRebootHasProgressBar) {
            sInstance.setRebootProgress(18, null);
        }
        shutdownTimingLog.traceEnd();
        metricEnded(METRIC_RADIOS);
        if (mRebootHasProgressBar) {
            sInstance.setRebootProgress(20, null);
            uncrypt();
        }
        shutdownTimingLog.traceEnd();
        metricEnded(METRIC_SYSTEM_SERVER);
        saveMetrics(mReboot, mReason);
        rebootOrShutdown(this.mContext, mReboot, mReason, shutDownBegin);
    }

    /* access modifiers changed from: private */
    public static TimingsTraceLog newTimingsLog() {
        return new TimingsTraceLog("ShutdownTiming", 524288);
    }

    /* access modifiers changed from: private */
    public static void metricStarted(String metricKey) {
        synchronized (TRON_METRICS) {
            TRON_METRICS.put(metricKey, Long.valueOf(SystemClock.elapsedRealtime() * -1));
        }
    }

    /* access modifiers changed from: private */
    public static void metricEnded(String metricKey) {
        synchronized (TRON_METRICS) {
            TRON_METRICS.put(metricKey, Long.valueOf(SystemClock.elapsedRealtime() + TRON_METRICS.get(metricKey).longValue()));
        }
    }

    private static void metricShutdownStart() {
        synchronized (TRON_METRICS) {
            TRON_METRICS.put(METRIC_SHUTDOWN_TIME_START, Long.valueOf(System.currentTimeMillis()));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setRebootProgress(final int progress, final CharSequence message) {
        this.mHandler.post(new Runnable() {
            /* class com.android.server.power.ShutdownThread.AnonymousClass4 */

            @Override // java.lang.Runnable
            public void run() {
                if (ShutdownThread.this.mProgressDialog != null) {
                    ShutdownThread.this.mProgressDialog.setProgress(progress);
                    if (message != null) {
                        ShutdownThread.this.mProgressDialog.setMessage(message);
                    }
                }
            }
        });
    }

    private void shutdownRadios(final int timeout) {
        final long endTime = SystemClock.elapsedRealtime() + ((long) timeout);
        final boolean[] done = new boolean[1];
        Thread t = new Thread() {
            /* class com.android.server.power.ShutdownThread.AnonymousClass5 */

            /* JADX WARNING: Removed duplicated region for block: B:11:0x0026 A[Catch:{ RemoteException -> 0x0021 }] */
            /* JADX WARNING: Removed duplicated region for block: B:16:0x004e  */
            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                boolean radioOff;
                long delay;
                TimingsTraceLog shutdownTimingsTraceLog = ShutdownThread.newTimingsLog();
                ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
                if (phone != null) {
                    try {
                        if (phone.needMobileRadioShutdown()) {
                            radioOff = false;
                            if (!radioOff) {
                                Log.w(ShutdownThread.TAG, "Turning off cellular radios...");
                                ShutdownThread.metricStarted(ShutdownThread.METRIC_RADIO);
                                phone.shutdownMobileRadios();
                            }
                            Log.i(ShutdownThread.TAG, "Waiting for Radio...");
                            delay = endTime - SystemClock.elapsedRealtime();
                            while (delay > 0) {
                                if (ShutdownThread.mRebootHasProgressBar) {
                                    int i = timeout;
                                    ShutdownThread.sInstance.setRebootProgress(((int) (((((double) (((long) i) - delay)) * 1.0d) * 12.0d) / ((double) i))) + 6, null);
                                }
                                if (!radioOff) {
                                    try {
                                        radioOff = !phone.needMobileRadioShutdown();
                                    } catch (RemoteException ex) {
                                        Log.e(ShutdownThread.TAG, "RemoteException during radio shutdown", ex);
                                        radioOff = true;
                                    }
                                    if (radioOff) {
                                        Log.i(ShutdownThread.TAG, "Radio turned off.");
                                        ShutdownThread.metricEnded(ShutdownThread.METRIC_RADIO);
                                        shutdownTimingsTraceLog.logDuration("ShutdownRadio", ((Long) ShutdownThread.TRON_METRICS.get(ShutdownThread.METRIC_RADIO)).longValue());
                                    }
                                }
                                if (radioOff) {
                                    Log.i(ShutdownThread.TAG, "Radio shutdown complete.");
                                    done[0] = true;
                                    return;
                                }
                                SystemClock.sleep(100);
                                delay = endTime - SystemClock.elapsedRealtime();
                            }
                        }
                    } catch (RemoteException ex2) {
                        Log.e(ShutdownThread.TAG, "RemoteException during radio shutdown", ex2);
                        radioOff = true;
                    }
                }
                radioOff = true;
                if (!radioOff) {
                }
                Log.i(ShutdownThread.TAG, "Waiting for Radio...");
                delay = endTime - SystemClock.elapsedRealtime();
                while (delay > 0) {
                }
            }
        };
        t.start();
        try {
            t.join((long) timeout);
        } catch (InterruptedException e) {
        }
        if (!done[0]) {
            Log.w(TAG, "Timed out waiting for Radio shutdown.");
        }
    }

    public static void rebootOrShutdown(Context context, boolean reboot, String reason) {
        rebootOrShutdown(context, reboot, reason, -1);
    }

    private static void rebootOrShutdown(Context context, boolean reboot, String reason, long shutDownBegin) {
        deviceRebootOrShutdown(reboot, reason);
        int shutdownFlag = HwBootAnimationOeminfo.getBootAnimShutFlag();
        if (-1 == shutdownFlag) {
            Log.e(TAG, "shutdownThread: getBootAnimShutFlag error");
        }
        if (shutdownFlag == 0) {
            Log.d(TAG, "rebootOrShutdown: " + reboot);
            try {
                if (HwBootAnimationOeminfo.setBootAnimShutFlag(1) != 0) {
                    Log.e(TAG, "shutdownThread: writeBootAnimShutFlag error");
                }
            } catch (Exception ex) {
                Log.e(TAG, ex.toString());
            }
        }
        iHwShutdownThread.waitShutdownAnimationComplete(context, shutDownBegin);
        if (reboot) {
            Log.i(TAG, "Rebooting, reason: " + reason);
            PowerManagerService.lowLevelReboot(reason);
            Log.e(TAG, "Reboot failed, will attempt shutdown instead");
            reason = null;
        } else if (context != null) {
            if (!mFastShutdownEnable) {
                try {
                    new SystemVibrator(context).vibrate(500, VIBRATION_ATTRIBUTES);
                } catch (Exception e) {
                    Log.w(TAG, "Failed to vibrate during shutdown.", e);
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e2) {
            }
        }
        Log.i(TAG, "Performing low-level shutdown...");
        PowerManagerService.lowLevelShutdown(reason);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0094, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:?, code lost:
        r5.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0099, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x009a, code lost:
        r6.addSuppressed(r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x009d, code lost:
        throw r7;
     */
    private static void saveMetrics(boolean reboot, String reason) {
        StringBuilder metricValue = new StringBuilder();
        metricValue.append("reboot:");
        metricValue.append(reboot ? "y" : "n");
        metricValue.append(",");
        metricValue.append("reason:");
        metricValue.append(reason);
        int metricsSize = TRON_METRICS.size();
        for (int i = 0; i < metricsSize; i++) {
            String name = TRON_METRICS.keyAt(i);
            long value = TRON_METRICS.valueAt(i).longValue();
            if (value < 0) {
                Log.e(TAG, "metricEnded wasn't called for " + name);
            } else {
                metricValue.append(',');
                metricValue.append(name);
                metricValue.append(':');
                metricValue.append(value);
            }
        }
        File tmp = new File("/data/system/shutdown-metrics.tmp");
        boolean saved = false;
        try {
            FileOutputStream fos = new FileOutputStream(tmp);
            fos.write(metricValue.toString().getBytes(StandardCharsets.UTF_8));
            saved = true;
            fos.close();
        } catch (IOException e) {
            Log.e(TAG, "Cannot save shutdown metrics", e);
        }
        if (saved) {
            tmp.renameTo(new File("/data/system/shutdown-metrics.txt"));
        }
    }

    private void uncrypt() {
        Log.i(TAG, "Calling uncrypt and monitoring the progress...");
        final RecoverySystem.ProgressListener progressListener = new RecoverySystem.ProgressListener() {
            /* class com.android.server.power.ShutdownThread.AnonymousClass6 */

            @Override // android.os.RecoverySystem.ProgressListener
            public void onProgress(int status) {
                if (status >= 0 && status < 100) {
                    CharSequence msg = ShutdownThread.this.mContext.getText(17041101);
                    ShutdownThread.sInstance.setRebootProgress(((int) ((((double) status) * 80.0d) / 100.0d)) + 20, msg);
                } else if (status == 100) {
                    ShutdownThread.sInstance.setRebootProgress(status, ShutdownThread.this.mContext.getText(17041103));
                }
            }
        };
        final boolean[] done = {false};
        Thread t = new Thread() {
            /* class com.android.server.power.ShutdownThread.AnonymousClass7 */

            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                RecoverySystem recoverySystem = (RecoverySystem) ShutdownThread.this.mContext.getSystemService("recovery");
                try {
                    RecoverySystem.processPackage(ShutdownThread.this.mContext, new File(FileUtils.readTextFile(RecoverySystem.UNCRYPT_PACKAGE_FILE, 0, null)), progressListener);
                } catch (IOException e) {
                    Log.e(ShutdownThread.TAG, "Error uncrypting file", e);
                }
                done[0] = true;
            }
        };
        t.start();
        try {
            t.join(900000);
        } catch (InterruptedException e) {
        }
        if (!done[0]) {
            Log.w(TAG, "Timed out waiting for uncrypt.");
            try {
                FileUtils.stringToFile(RecoverySystem.UNCRYPT_STATUS_FILE, String.format("uncrypt_time: %d\nuncrypt_error: %d\n", 900, 100));
            } catch (IOException e2) {
                Log.e(TAG, "Failed to write timeout message to uncrypt status", e2);
            }
        }
    }

    private static void deviceRebootOrShutdown(boolean reboot, String reason) {
        try {
            Class<?> cl = Class.forName("com.qti.server.power.ShutdownOem");
            try {
                cl.getMethod("rebootOrShutdown", Boolean.TYPE, String.class).invoke(cl.newInstance(), Boolean.valueOf(reboot), reason);
            } catch (NoSuchMethodException e) {
                Log.e(TAG, "rebootOrShutdown method not found in class com.qti.server.power.ShutdownOem");
            } catch (Exception e2) {
                Log.e(TAG, "Unknown exception hit while trying to invode rebootOrShutdown");
            }
        } catch (ClassNotFoundException e3) {
            Log.e(TAG, "Unable to find class com.qti.server.power.ShutdownOem");
        }
    }

    public static AlertDialog getsConfirmDialog() {
        return sConfirmDialog;
    }
}
