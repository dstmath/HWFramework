package com.android.server.power;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.admin.SecurityLog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
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
import com.android.server.statusbar.StatusBarManagerInternal;
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
    private static final String METRICS_FILE_BASENAME = "/data/system/shutdown-metrics";
    private static String METRIC_AM = "shutdown_activity_manager";
    private static String METRIC_PM = "shutdown_package_manager";
    /* access modifiers changed from: private */
    public static String METRIC_RADIO = "shutdown_radio";
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
    /* access modifiers changed from: private */
    public static final ArrayMap<String, Long> TRON_METRICS = new ArrayMap<>();
    private static final AudioAttributes VIBRATION_ATTRIBUTES = new AudioAttributes.Builder().setContentType(4).setUsage(13).build();
    private static IHwShutdownThread iHwShutdownThread = HwServiceFactory.getHwShutdownThread();
    private static boolean mFastShutdownEnable = false;
    private static String mReason;
    private static boolean mReboot;
    /* access modifiers changed from: private */
    public static boolean mRebootHasProgressBar;
    private static boolean mRebootSafeMode;
    private static AlertDialog sConfirmDialog;
    /* access modifiers changed from: private */
    public static final ShutdownThread sInstance = new ShutdownThread();
    private static boolean sIsStarted = false;
    private static final Object sIsStartedGuard = new Object();
    private boolean mActionDone;
    private final Object mActionDoneSync = new Object();
    /* access modifiers changed from: private */
    public Context mContext;
    private PowerManager.WakeLock mCpuWakeLock;
    private Handler mHandler;
    private PowerManager mPowerManager;
    /* access modifiers changed from: private */
    public ProgressDialog mProgressDialog;
    private PowerManager.WakeLock mScreenWakeLock;

    public static class CloseDialogReceiver extends BroadcastReceiver implements DialogInterface.OnDismissListener {
        public Dialog dialog;
        private Context mContext;

        public CloseDialogReceiver(Context context) {
            this.mContext = context;
            context.registerReceiver(this, new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
        }

        public void onReceive(Context context, Intent intent) {
            if (this.dialog != null) {
                this.dialog.cancel();
            }
        }

        public void onDismiss(DialogInterface unused) {
            this.mContext.unregisterReceiver(this);
        }
    }

    private ShutdownThread() {
    }

    public static void shutdown(Context context, String reason, boolean confirm) {
        mReboot = false;
        mRebootSafeMode = false;
        mReason = reason;
        iHwShutdownThread.resetValues();
        shutdownInner(context, confirm);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0021, code lost:
        if (mRebootSafeMode == false) goto L_0x0027;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0023, code lost:
        r1 = 17040972;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0028, code lost:
        if (r0 != 2) goto L_0x002e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002a, code lost:
        r1 = 17041106;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x002e, code lost:
        r1 = 17041105;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0031, code lost:
        android.util.Log.d(TAG, "Notifying thread to start shutdown longPressBehavior=" + r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0047, code lost:
        if (r7 == false) goto L_0x00b2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0053, code lost:
        if (com.android.server.HwServiceFactory.getHwShutdownThread().needRebootDialog(mReason, r6) != false) goto L_0x00b5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0055, code lost:
        r2 = new com.android.server.power.ShutdownThread.CloseDialogReceiver(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x005c, code lost:
        if (sConfirmDialog == null) goto L_0x0063;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x005e, code lost:
        sConfirmDialog.dismiss();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0063, code lost:
        r3 = new android.app.AlertDialog.Builder(r6, 33947691);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x006d, code lost:
        if (mRebootSafeMode == false) goto L_0x0073;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x006f, code lost:
        r4 = 17040973;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0073, code lost:
        r4 = 17040949;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0076, code lost:
        sConfirmDialog = r3.setTitle(r4).setMessage(r1).setPositiveButton(17039379, new com.android.server.power.ShutdownThread.AnonymousClass1()).setNegativeButton(17039369, null).create();
        r2.dialog = sConfirmDialog;
        sConfirmDialog.setOnDismissListener(r2);
        sConfirmDialog.getWindow().setType(2009);
        sConfirmDialog.show();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x00b2, code lost:
        beginShutdownSequence(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00b5, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0014, code lost:
        r0 = r6.getResources().getInteger(17694802);
     */
    private static void shutdownInner(final Context context, boolean confirm) {
        context.assertRuntimeOverlayThemable();
        synchronized (sIsStartedGuard) {
            if (sIsStarted) {
                Log.d(TAG, "Request to shutdown already running, returning.");
            }
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
        if (mReason != null && mReason.startsWith("recovery-update")) {
            mRebootHasProgressBar = RecoverySystem.UNCRYPT_PACKAGE_FILE.exists() && !RecoverySystem.BLOCK_MAP_FILE.exists();
            pd.setTitle(context.getText(17040979));
            if (mRebootHasProgressBar) {
                pd.setMax(100);
                pd.setProgress(0);
                pd.setIndeterminate(false);
                pd.setProgressNumberFormat(null);
                pd.setProgressStyle(1);
                pd.setMessage(context.getText(17040977));
            } else if (showSysuiReboot()) {
                return null;
            } else {
                pd.setIndeterminate(true);
                pd.setMessage(context.getText(17040978));
            }
        } else if (mReason == null || !mReason.equals("recovery")) {
            if (showSysuiReboot()) {
                return null;
            }
            pd.setTitle(context.getText(17040949));
            pd.setMessage(context.getText(17041107));
            pd.setIndeterminate(true);
        } else if (RescueParty.isAttemptingFactoryReset()) {
            pd.setTitle(context.getText(17040949));
            pd.setMessage(context.getText(17041107));
            pd.setIndeterminate(true);
        } else {
            pd.setTitle(context.getText(17040975));
            pd.setMessage(context.getText(17040974));
            pd.setIndeterminate(true);
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

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0045, code lost:
        r3 = android.os.Binder.getCallingUid();
        r5 = android.os.Binder.getCallingPid();
        android.util.Flog.e(1600, "ShutdownThread beginShutdownSequence uid=" + r3 + ", pid=" + r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x006f, code lost:
        if (iHwShutdownThread.isDoShutdownAnimation() != false) goto L_0x0089;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0079, code lost:
        if (iHwShutdownThread.needRebootProgressDialog(mReboot, r9) != false) goto L_0x0089;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x007f, code lost:
        if (com.android.server.policy.HwPolicyFactory.isHwGlobalActionsShowing() != false) goto L_0x0089;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0081, code lost:
        sInstance.mProgressDialog = showShutdownDialog(r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0089, code lost:
        mFastShutdownEnable = com.android.server.policy.HwPolicyFactory.isHwFastShutdownEnable();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0091, code lost:
        if (mFastShutdownEnable == false) goto L_0x00a8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:?, code lost:
        new android.os.SystemVibrator(r9).vibrate(500, VIBRATION_ATTRIBUTES);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x00a0, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00a1, code lost:
        android.util.Log.w(TAG, "Failed to vibrate during shutdown under condition of fastshutdown mode.", r6);
     */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00f5 A[SYNTHETIC, Splitter:B:34:0x00f5] */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0126  */
    public static void beginShutdownSequence(Context context) {
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
        if (SecurityLog.isLoggingEnabled()) {
            SecurityLog.writeEvent(210010, new Object[0]);
        }
        sInstance.mHandler = new Handler() {
        };
        sInstance.start();
        sInstance.mScreenWakeLock = null;
        if (sInstance.mPowerManager.isScreenOn()) {
            try {
                sInstance.mScreenWakeLock = sInstance.mPowerManager.newWakeLock(26, "ShutdownThread-screen");
                sInstance.mScreenWakeLock.setReferenceCounted(false);
                sInstance.mScreenWakeLock.acquire();
            } catch (SecurityException e) {
                Log.w(TAG, "No permission to acquire wake lock", e);
                sInstance.mScreenWakeLock = null;
            }
        }
        if (SecurityLog.isLoggingEnabled()) {
        }
        sInstance.mHandler = new Handler() {
        };
        sInstance.start();
        sInstance.mContext = context;
        sInstance.mPowerManager = (PowerManager) context.getSystemService("power");
        sInstance.mCpuWakeLock = null;
        try {
            sInstance.mCpuWakeLock = sInstance.mPowerManager.newWakeLock(1, "ShutdownThread-cpu");
            sInstance.mCpuWakeLock.setReferenceCounted(false);
            sInstance.mCpuWakeLock.acquire();
        } catch (SecurityException e2) {
            Log.w(TAG, "No permission to acquire wake lock", e2);
            sInstance.mCpuWakeLock = null;
        }
        sInstance.mScreenWakeLock = null;
        if (sInstance.mPowerManager.isScreenOn()) {
        }
        if (SecurityLog.isLoggingEnabled()) {
        }
        sInstance.mHandler = new Handler() {
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

    /* JADX WARNING: Code restructure failed: missing block: B:24:?, code lost:
        android.util.Log.w(TAG, "Shutdown broadcast timed out");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x00c5, code lost:
        r18 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00c9, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00ca, code lost:
        r18 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0105, code lost:
        if (mRebootHasProgressBar == false) goto L_0x010d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0107, code lost:
        sInstance.setRebootProgress(2, null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x010d, code lost:
        r2.traceEnd();
        metricEnded(METRIC_SEND_BROADCAST);
        android.util.Log.i(TAG, "Shutting down activity manager...");
        r2.traceBegin("ShutdownActivityManager");
        metricStarted(METRIC_AM);
        r3 = android.app.IActivityManager.Stub.asInterface(android.os.ServiceManager.checkService("activity"));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0130, code lost:
        if (r3 == null) goto L_0x0139;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:?, code lost:
        r3.shutdown(10000);
     */
    public void run() {
        long endTime;
        TimingsTraceLog shutdownTimingLog = newTimingsLog();
        shutdownTimingLog.traceBegin("SystemServerShutdown");
        metricShutdownStart();
        metricStarted(METRIC_SYSTEM_SERVER);
        BroadcastReceiver br = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                ShutdownThread.this.actionDone();
            }
        };
        AlarmManager alarmManager = (AlarmManager) sInstance.mContext.getSystemService("alarm");
        if (alarmManager != null) {
            Log.i(TAG, "shutdownThread setHwRTCAlarm");
            alarmManager.setHwRTCAlarm();
            Log.i(TAG, "shutdownThread setHwairPlaneStateProp");
            alarmManager.setHwAirPlaneStateProp();
        }
        StringBuilder sb = new StringBuilder();
        sb.append(mReboot ? "1" : "0");
        sb.append(mReason != null ? mReason : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
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
                        sInstance.setRebootProgress((int) (((((double) (JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY - delay)) * 1.0d) * 2.0d) / 10000.0d), null);
                    } else {
                        endTime = endTime2;
                    }
                    try {
                        this.mActionDoneSync.wait(Math.min(delay, 500));
                    } catch (InterruptedException e) {
                    }
                    endTime2 = endTime;
                } catch (Throwable th) {
                    th = th;
                    long j = endTime2;
                    throw th;
                }
            }
            try {
            } catch (Throwable th2) {
                th = th2;
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
            TRON_METRICS.put(metricKey, Long.valueOf(-1 * SystemClock.elapsedRealtime()));
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
    public void setRebootProgress(final int progress, final CharSequence message) {
        this.mHandler.post(new Runnable() {
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

    private void shutdownRadios(int timeout) {
        boolean[] done = new boolean[1];
        final long elapsedRealtime = SystemClock.elapsedRealtime() + ((long) timeout);
        final int i = timeout;
        final boolean[] zArr = done;
        AnonymousClass5 r4 = new Thread() {
            /* JADX WARNING: Removed duplicated region for block: B:11:0x0021 A[Catch:{ RemoteException -> 0x001c }] */
            /* JADX WARNING: Removed duplicated region for block: B:16:0x0054  */
            public void run() {
                boolean radioOff;
                long delay;
                boolean radioOff2;
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
                            delay = elapsedRealtime - SystemClock.elapsedRealtime();
                            while (delay > 0) {
                                if (ShutdownThread.mRebootHasProgressBar) {
                                    ShutdownThread.sInstance.setRebootProgress(((int) (((((double) (((long) i) - delay)) * 1.0d) * 12.0d) / ((double) i))) + 6, null);
                                }
                                if (!radioOff) {
                                    try {
                                        radioOff2 = !phone.needMobileRadioShutdown();
                                    } catch (RemoteException ex) {
                                        Log.e(ShutdownThread.TAG, "RemoteException during radio shutdown", ex);
                                        radioOff2 = true;
                                    }
                                    if (radioOff) {
                                        Log.i(ShutdownThread.TAG, "Radio turned off.");
                                        ShutdownThread.metricEnded(ShutdownThread.METRIC_RADIO);
                                        shutdownTimingsTraceLog.logDuration("ShutdownRadio", ((Long) ShutdownThread.TRON_METRICS.get(ShutdownThread.METRIC_RADIO)).longValue());
                                    }
                                }
                                if (radioOff) {
                                    Log.i(ShutdownThread.TAG, "Radio shutdown complete.");
                                    zArr[0] = true;
                                    return;
                                }
                                SystemClock.sleep(100);
                                delay = elapsedRealtime - SystemClock.elapsedRealtime();
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
                delay = elapsedRealtime - SystemClock.elapsedRealtime();
                while (delay > 0) {
                }
            }
        };
        r4.start();
        try {
            r4.join((long) timeout);
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

    private static void saveMetrics(boolean reboot, String reason) {
        FileOutputStream fos;
        StringBuilder metricValue = new StringBuilder();
        metricValue.append("reboot:");
        metricValue.append(reboot ? "y" : "n");
        metricValue.append(",");
        metricValue.append("reason:");
        metricValue.append(reason);
        int metricsSize = TRON_METRICS.size();
        boolean saved = false;
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
        try {
            fos = new FileOutputStream(tmp);
            fos.write(metricValue.toString().getBytes(StandardCharsets.UTF_8));
            saved = true;
            fos.close();
        } catch (IOException e) {
            Log.e(TAG, "Cannot save shutdown metrics", e);
        } catch (Throwable th) {
            r5.addSuppressed(th);
        }
        if (saved) {
            tmp.renameTo(new File("/data/system/shutdown-metrics.txt"));
            return;
        }
        return;
        throw th;
    }

    private void uncrypt() {
        Log.i(TAG, "Calling uncrypt and monitoring the progress...");
        final RecoverySystem.ProgressListener progressListener = new RecoverySystem.ProgressListener() {
            public void onProgress(int status) {
                if (status >= 0 && status < 100) {
                    CharSequence msg = ShutdownThread.this.mContext.getText(17040976);
                    ShutdownThread.sInstance.setRebootProgress(((int) ((((double) status) * 80.0d) / 100.0d)) + 20, msg);
                } else if (status == 100) {
                    ShutdownThread.sInstance.setRebootProgress(status, ShutdownThread.this.mContext.getText(17040978));
                }
            }
        };
        final boolean[] done = {false};
        Thread t = new Thread() {
            public void run() {
                ShutdownThread.this.mContext.getSystemService("recovery");
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
                FileUtils.stringToFile(RecoverySystem.UNCRYPT_STATUS_FILE, String.format("uncrypt_time: %d\nuncrypt_error: %d\n", new Object[]{900, 100}));
            } catch (IOException e2) {
                Log.e(TAG, "Failed to write timeout message to uncrypt status", e2);
            }
        }
    }

    private static void deviceRebootOrShutdown(boolean reboot, String reason) {
        try {
            Class<?> cl = Class.forName("com.qti.server.power.ShutdownOem");
            try {
                cl.getMethod("rebootOrShutdown", new Class[]{Boolean.TYPE, String.class}).invoke(cl.newInstance(), new Object[]{Boolean.valueOf(reboot), reason});
            } catch (NoSuchMethodException e) {
                Log.e(TAG, "rebootOrShutdown method not found in class " + "com.qti.server.power.ShutdownOem");
            } catch (Exception e2) {
                Log.e(TAG, "Unknown exception hit while trying to invode rebootOrShutdown");
            }
        } catch (ClassNotFoundException e3) {
            Log.e(TAG, "Unable to find class " + "com.qti.server.power.ShutdownOem");
        }
    }

    public static AlertDialog getsConfirmDialog() {
        return sConfirmDialog;
    }
}
