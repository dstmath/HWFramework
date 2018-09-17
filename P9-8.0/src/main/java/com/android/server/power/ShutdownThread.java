package com.android.server.power;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.IActivityManager;
import android.app.IActivityManager.Stub;
import android.app.ProgressDialog;
import android.bluetooth.IBluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioAttributes.Builder;
import android.nfc.INfcAdapter;
import android.os.FileUtils;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RecoverySystem;
import android.os.RecoverySystem.ProgressListener;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.SystemVibrator;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.IStorageManager;
import android.os.storage.IStorageShutdownObserver;
import android.util.Log;
import com.android.internal.os.HwBootAnimationOeminfo;
import com.android.internal.telephony.ITelephony;
import com.android.server.HwServiceFactory;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.job.controllers.JobStatus;
import com.android.server.pm.PackageManagerService;
import java.io.File;
import java.io.IOException;

public final class ShutdownThread extends Thread {
    private static final String ACTION_ACTURAL_SHUTDOWN = "com.android.internal.app.SHUTDOWNBROADCAST";
    private static final int ACTIVITY_MANAGER_STOP_PERCENT = 4;
    private static final int BROADCAST_STOP_PERCENT = 2;
    private static final int MAX_BROADCAST_TIME = 10000;
    private static final int MAX_RADIO_WAIT_TIME = 12000;
    private static final int MAX_SHUTDOWN_WAIT_TIME = 20000;
    private static final int MAX_UNCRYPT_WAIT_TIME = 900000;
    private static final int MOUNT_SERVICE_STOP_PERCENT = 20;
    private static final int PACKAGE_MANAGER_STOP_PERCENT = 6;
    private static final int PHONE_STATE_POLL_SLEEP_MSEC = 100;
    private static final int RADIO_STOP_PERCENT = 18;
    public static final String REBOOT_SAFEMODE_PROPERTY = "persist.sys.safemode";
    public static final String RO_SAFEMODE_PROPERTY = "ro.sys.safemode";
    public static final String SHUTDOWN_ACTION_PROPERTY = "sys.shutdown.requested";
    private static final int SHUTDOWN_VIBRATE_MS = 500;
    private static final String TAG = "ShutdownThread";
    private static final AudioAttributes VIBRATION_ATTRIBUTES = new Builder().setContentType(4).setUsage(13).build();
    private static IHwShutdownThread iHwShutdownThread = HwServiceFactory.getHwShutdownThread();
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
    private WakeLock mCpuWakeLock;
    private Handler mHandler;
    private PowerManager mPowerManager;
    private ProgressDialog mProgressDialog;
    private WakeLock mScreenWakeLock;

    public static class CloseDialogReceiver extends BroadcastReceiver implements OnDismissListener {
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

    /* JADX WARNING: Missing block: B:9:0x0017, code:
            r1 = r7.getResources().getInteger(17694802);
     */
    /* JADX WARNING: Missing block: B:10:0x0024, code:
            if (mRebootSafeMode == false) goto L_0x00ac;
     */
    /* JADX WARNING: Missing block: B:11:0x0026, code:
            r2 = 17040856;
     */
    /* JADX WARNING: Missing block: B:12:0x0029, code:
            android.util.Log.d(TAG, "Notifying thread to start shutdown longPressBehavior=" + r1);
     */
    /* JADX WARNING: Missing block: B:13:0x0043, code:
            if (r8 == false) goto L_0x00bd;
     */
    /* JADX WARNING: Missing block: B:15:0x004f, code:
            if (com.android.server.HwServiceFactory.getHwShutdownThread().needRebootDialog(mReason, r7) != false) goto L_0x00a8;
     */
    /* JADX WARNING: Missing block: B:16:0x0051, code:
            r0 = new com.android.server.power.ShutdownThread.CloseDialogReceiver(r7);
     */
    /* JADX WARNING: Missing block: B:17:0x0058, code:
            if (sConfirmDialog == null) goto L_0x005f;
     */
    /* JADX WARNING: Missing block: B:18:0x005a, code:
            sConfirmDialog.dismiss();
     */
    /* JADX WARNING: Missing block: B:19:0x005f, code:
            r4 = new android.app.AlertDialog.Builder(r7, 33947691);
     */
    /* JADX WARNING: Missing block: B:20:0x0069, code:
            if (mRebootSafeMode == false) goto L_0x00b9;
     */
    /* JADX WARNING: Missing block: B:21:0x006b, code:
            r3 = 17040857;
     */
    /* JADX WARNING: Missing block: B:22:0x006e, code:
            sConfirmDialog = r4.setTitle(r3).setMessage(r2).setPositiveButton(17039379, new com.android.server.power.ShutdownThread.AnonymousClass1()).setNegativeButton(17039369, null).create();
            r0.dialog = sConfirmDialog;
            sConfirmDialog.setOnDismissListener(r0);
            sConfirmDialog.getWindow().setType(2009);
            sConfirmDialog.show();
     */
    /* JADX WARNING: Missing block: B:23:0x00a8, code:
            return;
     */
    /* JADX WARNING: Missing block: B:28:0x00ad, code:
            if (r1 != 2) goto L_0x00b4;
     */
    /* JADX WARNING: Missing block: B:29:0x00af, code:
            r2 = 17040984;
     */
    /* JADX WARNING: Missing block: B:30:0x00b4, code:
            r2 = 17040983;
     */
    /* JADX WARNING: Missing block: B:31:0x00b9, code:
            r3 = 17040834;
     */
    /* JADX WARNING: Missing block: B:32:0x00bd, code:
            beginShutdownSequence(r7);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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

    /* JADX WARNING: Missing block: B:12:0x0024, code:
            r4 = android.os.Binder.getCallingUid();
            android.util.Flog.e(1600, "ShutdownThread beginShutdownSequence uid=" + r4 + ", pid=" + android.os.Binder.getCallingPid());
     */
    /* JADX WARNING: Missing block: B:13:0x0056, code:
            if (iHwShutdownThread.isDoShutdownAnimation() != false) goto L_0x00d1;
     */
    /* JADX WARNING: Missing block: B:15:0x0062, code:
            if ((iHwShutdownThread.needRebootProgressDialog(mReboot, r10) ^ 1) == 0) goto L_0x00d1;
     */
    /* JADX WARNING: Missing block: B:17:0x006a, code:
            if ((com.android.server.policy.HwPolicyFactory.isHwGlobalActionsShowing() ^ 1) == 0) goto L_0x00d1;
     */
    /* JADX WARNING: Missing block: B:18:0x006c, code:
            r1 = new android.app.ProgressDialog(r10, 33947691);
     */
    /* JADX WARNING: Missing block: B:19:0x0076, code:
            if (mReason == null) goto L_0x0157;
     */
    /* JADX WARNING: Missing block: B:21:0x0081, code:
            if (mReason.startsWith("recovery-update") == false) goto L_0x0157;
     */
    /* JADX WARNING: Missing block: B:23:0x0089, code:
            if (android.os.RecoverySystem.UNCRYPT_PACKAGE_FILE.exists() == false) goto L_0x0145;
     */
    /* JADX WARNING: Missing block: B:24:0x008b, code:
            r5 = android.os.RecoverySystem.BLOCK_MAP_FILE.exists() ^ 1;
     */
    /* JADX WARNING: Missing block: B:25:0x0093, code:
            mRebootHasProgressBar = r5;
            r1.setTitle(r10.getText(17040863));
     */
    /* JADX WARNING: Missing block: B:26:0x00a1, code:
            if (mRebootHasProgressBar == false) goto L_0x0148;
     */
    /* JADX WARNING: Missing block: B:27:0x00a3, code:
            r1.setMax(100);
            r1.setProgress(0);
            r1.setIndeterminate(false);
            r1.setProgressNumberFormat(null);
            r1.setProgressStyle(1);
            r1.setMessage(r10.getText(17040861));
     */
    /* JADX WARNING: Missing block: B:28:0x00be, code:
            r1.setCancelable(false);
            r1.getWindow().setType(2009);
            r1.show();
            sInstance.mProgressDialog = r1;
     */
    /* JADX WARNING: Missing block: B:29:0x00d1, code:
            sInstance.mContext = r10;
            sInstance.mPowerManager = (android.os.PowerManager) r10.getSystemService("power");
            sInstance.mCpuWakeLock = null;
     */
    /* JADX WARNING: Missing block: B:31:?, code:
            sInstance.mCpuWakeLock = sInstance.mPowerManager.newWakeLock(1, "ShutdownThread-cpu");
            sInstance.mCpuWakeLock.setReferenceCounted(false);
            sInstance.mCpuWakeLock.acquire();
     */
    /* JADX WARNING: Missing block: B:41:0x0145, code:
            r5 = false;
     */
    /* JADX WARNING: Missing block: B:42:0x0148, code:
            r1.setIndeterminate(true);
            r1.setMessage(r10.getText(17040862));
     */
    /* JADX WARNING: Missing block: B:44:0x0159, code:
            if (mReason == null) goto L_0x017f;
     */
    /* JADX WARNING: Missing block: B:46:0x0164, code:
            if (mReason.equals("recovery") == false) goto L_0x017f;
     */
    /* JADX WARNING: Missing block: B:47:0x0166, code:
            r1.setTitle(r10.getText(17040859));
            r1.setMessage(r10.getText(17040858));
            r1.setIndeterminate(true);
     */
    /* JADX WARNING: Missing block: B:48:0x017f, code:
            r1.setTitle(r10.getText(17040834));
            r1.setMessage(r10.getText(17040985));
            r1.setIndeterminate(true);
     */
    /* JADX WARNING: Missing block: B:49:0x0198, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:50:0x0199, code:
            android.util.Log.w(TAG, "No permission to acquire wake lock", r0);
            sInstance.mCpuWakeLock = null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void beginShutdownSequence(Context context) {
        context.sendBroadcast(new Intent(ACTION_ACTURAL_SHUTDOWN));
        synchronized (sIsStartedGuard) {
            if (sIsStarted) {
                Log.d(TAG, "Shutdown sequence already running, returning.");
                return;
            }
            sIsStarted = true;
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
        sInstance.mHandler = /* anonymous class already generated */;
        sInstance.start();
    }

    void actionDone() {
        synchronized (this.mActionDoneSync) {
            this.mActionDone = true;
            this.mActionDoneSync.notifyAll();
        }
    }

    public void run() {
        String str;
        long delay;
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
        StringBuilder append = new StringBuilder().append(mReboot ? "1" : "0");
        if (mReason != null) {
            str = mReason;
        } else {
            str = "";
        }
        SystemProperties.set(SHUTDOWN_ACTION_PROPERTY, append.append(str).toString());
        if (mRebootSafeMode) {
            SystemProperties.set(REBOOT_SAFEMODE_PROPERTY, "1");
        }
        Log.i(TAG, "Sending shutdown broadcast...");
        long shutDownBegin = SystemClock.elapsedRealtime();
        this.mActionDone = false;
        Intent intent = new Intent("android.intent.action.ACTION_SHUTDOWN");
        intent.addFlags(285212672);
        this.mContext.sendOrderedBroadcastAsUser(intent, UserHandle.ALL, null, br, this.mHandler, 0, null, null);
        long endTime = SystemClock.elapsedRealtime() + JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY;
        synchronized (this.mActionDoneSync) {
            while (!this.mActionDone) {
                delay = endTime - SystemClock.elapsedRealtime();
                if (delay <= 0) {
                    Log.w(TAG, "Shutdown broadcast timed out");
                    break;
                }
                if (mRebootHasProgressBar) {
                    sInstance.setRebootProgress((int) (((((double) (JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY - delay)) * 1.0d) * 2.0d) / 10000.0d), null);
                }
                try {
                    this.mActionDoneSync.wait(Math.min(delay, 100));
                } catch (InterruptedException e) {
                }
            }
        }
        if (mRebootHasProgressBar) {
            sInstance.setRebootProgress(2, null);
        }
        Log.i(TAG, "Shutting down activity manager...");
        IActivityManager am = Stub.asInterface(ServiceManager.checkService("activity"));
        if (am != null) {
            try {
                am.shutdown(10000);
            } catch (RemoteException e2) {
            }
        }
        if (mRebootHasProgressBar) {
            sInstance.setRebootProgress(4, null);
        }
        Log.i(TAG, "Shutting down package manager...");
        PackageManagerService pm = (PackageManagerService) ServiceManager.getService(HwBroadcastRadarUtil.KEY_PACKAGE);
        if (pm != null) {
            pm.shutdown();
        }
        if (mRebootHasProgressBar) {
            sInstance.setRebootProgress(6, null);
        }
        shutdownRadios(MAX_RADIO_WAIT_TIME);
        if (mRebootHasProgressBar) {
            sInstance.setRebootProgress(18, null);
        }
        AnonymousClass4 anonymousClass4 = new IStorageShutdownObserver.Stub() {
            public void onShutDownComplete(int statusCode) throws RemoteException {
                Log.w(ShutdownThread.TAG, "Result code " + statusCode + " from StorageManagerService.shutdown");
                ShutdownThread.this.actionDone();
            }
        };
        Log.i(TAG, "Shutting down StorageManagerService");
        this.mActionDone = false;
        long endShutTime = SystemClock.elapsedRealtime() + 20000;
        synchronized (this.mActionDoneSync) {
            try {
                IStorageManager storageManager = IStorageManager.Stub.asInterface(ServiceManager.checkService("mount"));
                if (storageManager != null) {
                    storageManager.shutdown(anonymousClass4);
                } else {
                    Log.w(TAG, "StorageManagerService unavailable for shutdown");
                }
            } catch (Throwable e3) {
                Log.e(TAG, "Exception during StorageManagerService shutdown", e3);
            }
            while (!this.mActionDone) {
                delay = endShutTime - SystemClock.elapsedRealtime();
                if (delay <= 0) {
                    Log.w(TAG, "Shutdown wait timed out");
                    break;
                }
                if (mRebootHasProgressBar) {
                    sInstance.setRebootProgress(((int) (((((double) (20000 - delay)) * 1.0d) * 2.0d) / 20000.0d)) + 18, null);
                }
                try {
                    this.mActionDoneSync.wait(Math.min(delay, 100));
                } catch (InterruptedException e4) {
                }
            }
        }
        if (mRebootHasProgressBar) {
            sInstance.setRebootProgress(20, null);
            uncrypt();
        }
        rebootOrShutdown(this.mContext, mReboot, mReason, shutDownBegin);
        return;
    }

    private void setRebootProgress(final int progress, final CharSequence message) {
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
        final long endTime = SystemClock.elapsedRealtime() + ((long) timeout);
        final boolean[] done = new boolean[1];
        final int i = timeout;
        Thread t = new Thread() {
            public void run() {
                boolean nfcOff;
                boolean bluetoothOff;
                int radioOff;
                INfcAdapter nfc = INfcAdapter.Stub.asInterface(ServiceManager.checkService("nfc"));
                ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
                IBluetoothManager bluetooth = IBluetoothManager.Stub.asInterface(ServiceManager.checkService("bluetooth_manager"));
                if (nfc != null) {
                    try {
                        nfcOff = nfc.getState() == 1;
                    } catch (RemoteException ex) {
                        Log.e(ShutdownThread.TAG, "RemoteException during NFC shutdown", ex);
                        nfcOff = true;
                    }
                } else {
                    nfcOff = true;
                }
                if (!nfcOff) {
                    Log.w(ShutdownThread.TAG, "Turning off NFC...");
                    nfc.disable(false);
                }
                if (bluetooth != null) {
                    try {
                        bluetoothOff = bluetooth.getState() == 10;
                    } catch (RemoteException ex2) {
                        Log.e(ShutdownThread.TAG, "RemoteException during bluetooth shutdown", ex2);
                        bluetoothOff = true;
                    }
                } else {
                    bluetoothOff = true;
                }
                if (!bluetoothOff) {
                    Log.w(ShutdownThread.TAG, "Disabling Bluetooth...");
                    bluetooth.disable(ShutdownThread.this.mContext.getPackageName(), false);
                }
                if (phone != null) {
                    try {
                        radioOff = phone.needMobileRadioShutdown() ^ 1;
                    } catch (RemoteException ex22) {
                        Log.e(ShutdownThread.TAG, "RemoteException during radio shutdown", ex22);
                        radioOff = 1;
                    }
                } else {
                    radioOff = 1;
                }
                if (radioOff == 0) {
                    Log.w(ShutdownThread.TAG, "Turning off cellular radios...");
                    phone.shutdownMobileRadios();
                }
                Log.i(ShutdownThread.TAG, "Waiting for NFC, Bluetooth and Radio...");
                long j = endTime;
                long elapsedRealtime = SystemClock.elapsedRealtime();
                while (true) {
                    long delay = j - elapsedRealtime;
                    if (delay > 0) {
                        if (ShutdownThread.mRebootHasProgressBar) {
                            ShutdownThread.sInstance.setRebootProgress(((int) (((((double) (((long) i) - delay)) * 1.0d) * 12.0d) / ((double) i))) + 6, null);
                        }
                        if (!bluetoothOff) {
                            try {
                                bluetoothOff = bluetooth.getState() == 10;
                            } catch (RemoteException ex222) {
                                Log.e(ShutdownThread.TAG, "RemoteException during bluetooth shutdown", ex222);
                                bluetoothOff = true;
                            }
                            if (bluetoothOff) {
                                Log.i(ShutdownThread.TAG, "Bluetooth turned off.");
                            }
                        }
                        if (radioOff == 0) {
                            try {
                                radioOff = phone.needMobileRadioShutdown() ^ 1;
                            } catch (RemoteException ex2222) {
                                Log.e(ShutdownThread.TAG, "RemoteException during radio shutdown", ex2222);
                                radioOff = 1;
                            }
                            if (radioOff != 0) {
                                Log.i(ShutdownThread.TAG, "Radio turned off.");
                            }
                        }
                        if (!nfcOff) {
                            try {
                                nfcOff = nfc.getState() == 1;
                            } catch (RemoteException ex22222) {
                                Log.e(ShutdownThread.TAG, "RemoteException during NFC shutdown", ex22222);
                                nfcOff = true;
                            }
                            if (nfcOff) {
                                Log.i(ShutdownThread.TAG, "NFC turned off.");
                            }
                        }
                        if (radioOff != 0 && bluetoothOff && nfcOff) {
                            Log.i(ShutdownThread.TAG, "NFC, Radio and Bluetooth shutdown complete.");
                            done[0] = true;
                            return;
                        }
                        SystemClock.sleep(100);
                        j = endTime;
                        elapsedRealtime = SystemClock.elapsedRealtime();
                    } else {
                        return;
                    }
                }
            }
        };
        t.start();
        try {
            t.join((long) timeout);
        } catch (InterruptedException e) {
        }
        if (!done[0]) {
            Log.w(TAG, "Timed out waiting for NFC, Radio and Bluetooth shutdown.");
        }
    }

    public static void rebootOrShutdown(Context context, boolean reboot, String reason) {
        rebootOrShutdown(context, reboot, reason, -1);
    }

    private static void rebootOrShutdown(Context context, boolean reboot, String reason, long shutDownBegin) {
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
            try {
                new SystemVibrator(context).vibrate(500, VIBRATION_ATTRIBUTES);
            } catch (Exception e) {
                Log.w(TAG, "Failed to vibrate during shutdown.", e);
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e2) {
            }
        }
        Log.i(TAG, "Performing low-level shutdown...");
        PowerManagerService.lowLevelShutdown(reason);
    }

    private void uncrypt() {
        Log.i(TAG, "Calling uncrypt and monitoring the progress...");
        final ProgressListener progressListener = new ProgressListener() {
            public void onProgress(int status) {
                if (status >= 0 && status < 100) {
                    ShutdownThread.sInstance.setRebootProgress(((int) ((((double) status) * 80.0d) / 100.0d)) + 20, ShutdownThread.this.mContext.getText(17040860));
                } else if (status == 100) {
                    ShutdownThread.sInstance.setRebootProgress(status, ShutdownThread.this.mContext.getText(17040862));
                }
            }
        };
        final boolean[] done = new boolean[]{false};
        Thread t = new Thread() {
            public void run() {
                RecoverySystem rs = (RecoverySystem) ShutdownThread.this.mContext.getSystemService("recovery");
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
                FileUtils.stringToFile(RecoverySystem.UNCRYPT_STATUS_FILE, String.format("uncrypt_time: %d\nuncrypt_error: %d\n", new Object[]{Integer.valueOf(900), Integer.valueOf(100)}));
            } catch (IOException e2) {
                Log.e(TAG, "Failed to write timeout message to uncrypt status", e2);
            }
        }
    }

    public static AlertDialog getsConfirmDialog() {
        return sConfirmDialog;
    }
}
