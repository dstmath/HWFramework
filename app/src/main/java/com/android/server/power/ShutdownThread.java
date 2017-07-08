package com.android.server.power;

import android.app.ActivityManagerNative;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.IActivityManager;
import android.app.ProgressDialog;
import android.bluetooth.IBluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.nfc.INfcAdapter;
import android.nfc.INfcAdapter.Stub;
import android.os.Binder;
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
import android.os.storage.IMountService;
import android.os.storage.IMountShutdownObserver;
import android.util.Flog;
import android.util.Log;
import com.android.internal.os.HwBootAnimationOeminfo;
import com.android.internal.telephony.ITelephony;
import com.android.server.HwServiceFactory;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.job.controllers.JobStatus;
import com.android.server.pm.PackageManagerService;
import com.android.server.policy.HwPolicyFactory;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

public final class ShutdownThread extends Thread {
    private static final String ACTION_ACTURAL_SHUTDOWN = "com.android.internal.app.SHUTDOWNBROADCAST";
    private static final int ACTIVITY_MANAGER_STOP_PERCENT = 4;
    public static final String AUDIT_SAFEMODE_PROPERTY = "persist.sys.audit_safemode";
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
    private static final AudioAttributes VIBRATION_ATTRIBUTES = null;
    private static IHwShutdownThread iHwShutdownThread;
    private static String mReason;
    private static boolean mReboot;
    private static boolean mRebootHasProgressBar;
    private static boolean mRebootSafeMode;
    private static AlertDialog sConfirmDialog;
    private static final ShutdownThread sInstance = null;
    private static boolean sIsStarted;
    private static Object sIsStartedGuard;
    private boolean mActionDone;
    private final Object mActionDoneSync;
    private Context mContext;
    private WakeLock mCpuWakeLock;
    private Handler mHandler;
    private PowerManager mPowerManager;
    private ProgressDialog mProgressDialog;
    private WakeLock mScreenWakeLock;

    /* renamed from: com.android.server.power.ShutdownThread.1 */
    static class AnonymousClass1 implements OnClickListener {
        final /* synthetic */ Context val$context;

        AnonymousClass1(Context val$context) {
            this.val$context = val$context;
        }

        public void onClick(DialogInterface dialog, int which) {
            ShutdownThread.beginShutdownSequence(this.val$context);
        }
    }

    /* renamed from: com.android.server.power.ShutdownThread.5 */
    class AnonymousClass5 implements Runnable {
        final /* synthetic */ CharSequence val$message;
        final /* synthetic */ int val$progress;

        AnonymousClass5(int val$progress, CharSequence val$message) {
            this.val$progress = val$progress;
            this.val$message = val$message;
        }

        public void run() {
            if (ShutdownThread.this.mProgressDialog != null) {
                ShutdownThread.this.mProgressDialog.setProgress(this.val$progress);
                if (this.val$message != null) {
                    ShutdownThread.this.mProgressDialog.setMessage(this.val$message);
                }
            }
        }
    }

    /* renamed from: com.android.server.power.ShutdownThread.6 */
    class AnonymousClass6 extends Thread {
        final /* synthetic */ boolean[] val$done;
        final /* synthetic */ long val$endTime;
        final /* synthetic */ int val$timeout;

        AnonymousClass6(long val$endTime, int val$timeout, boolean[] val$done) {
            this.val$endTime = val$endTime;
            this.val$timeout = val$timeout;
            this.val$done = val$done;
        }

        public void run() {
            boolean nfcOff;
            boolean bluetoothOff;
            boolean radioOff;
            long delay;
            INfcAdapter nfc = Stub.asInterface(ServiceManager.checkService("nfc"));
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
                    if (bluetooth.isEnabled()) {
                        bluetoothOff = false;
                        if (!bluetoothOff) {
                            Log.w(ShutdownThread.TAG, "Disabling Bluetooth...");
                            bluetooth.disable(false);
                        }
                        if (phone != null) {
                            try {
                                if (phone.needMobileRadioShutdown()) {
                                    radioOff = false;
                                    if (!radioOff) {
                                        Log.w(ShutdownThread.TAG, "Turning off cellular radios...");
                                        phone.shutdownMobileRadios();
                                    }
                                    Log.i(ShutdownThread.TAG, "Waiting for NFC, Bluetooth and Radio...");
                                    delay = this.val$endTime - SystemClock.elapsedRealtime();
                                    while (delay > 0) {
                                        if (ShutdownThread.mRebootHasProgressBar) {
                                            ShutdownThread.sInstance.setRebootProgress(((int) (((((double) (((long) this.val$timeout) - delay)) * 1.0d) * 12.0d) / ((double) this.val$timeout))) + ShutdownThread.PACKAGE_MANAGER_STOP_PERCENT, null);
                                        }
                                        if (!bluetoothOff) {
                                            try {
                                                bluetoothOff = bluetooth.isEnabled();
                                            } catch (RemoteException ex2) {
                                                Log.e(ShutdownThread.TAG, "RemoteException during bluetooth shutdown", ex2);
                                                bluetoothOff = true;
                                            }
                                            if (bluetoothOff) {
                                                Log.i(ShutdownThread.TAG, "Bluetooth turned off.");
                                            }
                                        }
                                        if (!radioOff) {
                                            try {
                                                radioOff = phone.needMobileRadioShutdown();
                                            } catch (RemoteException ex22) {
                                                Log.e(ShutdownThread.TAG, "RemoteException during radio shutdown", ex22);
                                                radioOff = true;
                                            }
                                            if (radioOff) {
                                                Log.i(ShutdownThread.TAG, "Radio turned off.");
                                            }
                                        }
                                        if (!nfcOff) {
                                            try {
                                                nfcOff = nfc.getState() != 1;
                                            } catch (RemoteException ex222) {
                                                Log.e(ShutdownThread.TAG, "RemoteException during NFC shutdown", ex222);
                                                nfcOff = true;
                                            }
                                            if (nfcOff) {
                                                Log.i(ShutdownThread.TAG, "NFC turned off.");
                                            }
                                        }
                                        if (!radioOff && bluetoothOff && nfcOff) {
                                            Log.i(ShutdownThread.TAG, "NFC, Radio and Bluetooth shutdown complete.");
                                            this.val$done[0] = true;
                                            return;
                                        }
                                        SystemClock.sleep(100);
                                        delay = this.val$endTime - SystemClock.elapsedRealtime();
                                    }
                                }
                            } catch (RemoteException ex2222) {
                                Log.e(ShutdownThread.TAG, "RemoteException during radio shutdown", ex2222);
                                radioOff = true;
                            }
                        }
                        radioOff = true;
                        if (radioOff) {
                            Log.w(ShutdownThread.TAG, "Turning off cellular radios...");
                            phone.shutdownMobileRadios();
                        }
                        Log.i(ShutdownThread.TAG, "Waiting for NFC, Bluetooth and Radio...");
                        delay = this.val$endTime - SystemClock.elapsedRealtime();
                        while (delay > 0) {
                            if (ShutdownThread.mRebootHasProgressBar) {
                                ShutdownThread.sInstance.setRebootProgress(((int) (((((double) (((long) this.val$timeout) - delay)) * 1.0d) * 12.0d) / ((double) this.val$timeout))) + ShutdownThread.PACKAGE_MANAGER_STOP_PERCENT, null);
                            }
                            if (bluetoothOff) {
                                if (bluetooth.isEnabled()) {
                                }
                                if (bluetoothOff) {
                                    Log.i(ShutdownThread.TAG, "Bluetooth turned off.");
                                }
                            }
                            if (radioOff) {
                                if (phone.needMobileRadioShutdown()) {
                                }
                                if (radioOff) {
                                    Log.i(ShutdownThread.TAG, "Radio turned off.");
                                }
                            }
                            if (nfcOff) {
                                if (nfc.getState() != 1) {
                                }
                                if (nfcOff) {
                                    Log.i(ShutdownThread.TAG, "NFC turned off.");
                                }
                            }
                            if (!radioOff) {
                            }
                            SystemClock.sleep(100);
                            delay = this.val$endTime - SystemClock.elapsedRealtime();
                        }
                    }
                } catch (RemoteException ex22222) {
                    Log.e(ShutdownThread.TAG, "RemoteException during bluetooth shutdown", ex22222);
                    bluetoothOff = true;
                }
            }
            bluetoothOff = true;
            if (bluetoothOff) {
                Log.w(ShutdownThread.TAG, "Disabling Bluetooth...");
                bluetooth.disable(false);
            }
            if (phone != null) {
                if (phone.needMobileRadioShutdown()) {
                    radioOff = false;
                    if (radioOff) {
                        Log.w(ShutdownThread.TAG, "Turning off cellular radios...");
                        phone.shutdownMobileRadios();
                    }
                    Log.i(ShutdownThread.TAG, "Waiting for NFC, Bluetooth and Radio...");
                    delay = this.val$endTime - SystemClock.elapsedRealtime();
                    while (delay > 0) {
                        if (ShutdownThread.mRebootHasProgressBar) {
                            ShutdownThread.sInstance.setRebootProgress(((int) (((((double) (((long) this.val$timeout) - delay)) * 1.0d) * 12.0d) / ((double) this.val$timeout))) + ShutdownThread.PACKAGE_MANAGER_STOP_PERCENT, null);
                        }
                        if (bluetoothOff) {
                            if (bluetooth.isEnabled()) {
                            }
                            if (bluetoothOff) {
                                Log.i(ShutdownThread.TAG, "Bluetooth turned off.");
                            }
                        }
                        if (radioOff) {
                            if (phone.needMobileRadioShutdown()) {
                            }
                            if (radioOff) {
                                Log.i(ShutdownThread.TAG, "Radio turned off.");
                            }
                        }
                        if (nfcOff) {
                            if (nfc.getState() != 1) {
                            }
                            if (nfcOff) {
                                Log.i(ShutdownThread.TAG, "NFC turned off.");
                            }
                        }
                        if (!radioOff) {
                        }
                        SystemClock.sleep(100);
                        delay = this.val$endTime - SystemClock.elapsedRealtime();
                    }
                }
            }
            radioOff = true;
            if (radioOff) {
                Log.w(ShutdownThread.TAG, "Turning off cellular radios...");
                phone.shutdownMobileRadios();
            }
            Log.i(ShutdownThread.TAG, "Waiting for NFC, Bluetooth and Radio...");
            delay = this.val$endTime - SystemClock.elapsedRealtime();
            while (delay > 0) {
                if (ShutdownThread.mRebootHasProgressBar) {
                    ShutdownThread.sInstance.setRebootProgress(((int) (((((double) (((long) this.val$timeout) - delay)) * 1.0d) * 12.0d) / ((double) this.val$timeout))) + ShutdownThread.PACKAGE_MANAGER_STOP_PERCENT, null);
                }
                if (bluetoothOff) {
                    if (bluetooth.isEnabled()) {
                    }
                    if (bluetoothOff) {
                        Log.i(ShutdownThread.TAG, "Bluetooth turned off.");
                    }
                }
                if (radioOff) {
                    if (phone.needMobileRadioShutdown()) {
                    }
                    if (radioOff) {
                        Log.i(ShutdownThread.TAG, "Radio turned off.");
                    }
                }
                if (nfcOff) {
                    if (nfc.getState() != 1) {
                    }
                    if (nfcOff) {
                        Log.i(ShutdownThread.TAG, "NFC turned off.");
                    }
                }
                if (!radioOff) {
                }
                SystemClock.sleep(100);
                delay = this.val$endTime - SystemClock.elapsedRealtime();
            }
        }
    }

    /* renamed from: com.android.server.power.ShutdownThread.8 */
    class AnonymousClass8 extends Thread {
        final /* synthetic */ boolean[] val$done;
        final /* synthetic */ ProgressListener val$progressListener;

        AnonymousClass8(ProgressListener val$progressListener, boolean[] val$done) {
            this.val$progressListener = val$progressListener;
            this.val$done = val$done;
        }

        public void run() {
            RecoverySystem rs = (RecoverySystem) ShutdownThread.this.mContext.getSystemService("recovery");
            try {
                RecoverySystem.processPackage(ShutdownThread.this.mContext, new File(FileUtils.readTextFile(RecoverySystem.UNCRYPT_PACKAGE_FILE, 0, null)), this.val$progressListener);
            } catch (IOException e) {
                Log.e(ShutdownThread.TAG, "Error uncrypting file", e);
            }
            this.val$done[0] = true;
        }
    }

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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.power.ShutdownThread.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.power.ShutdownThread.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.power.ShutdownThread.<clinit>():void");
    }

    private ShutdownThread() {
        this.mActionDoneSync = new Object();
    }

    public static void shutdown(Context context, String reason, boolean confirm) {
        mReboot = false;
        mRebootSafeMode = false;
        mReason = reason;
        iHwShutdownThread.resetValues();
        shutdownInner(context, confirm);
    }

    static void shutdownInner(Context context, boolean confirm) {
        synchronized (sIsStartedGuard) {
            if (sIsStarted) {
                Log.d(TAG, "Request to shutdown already running, returning.");
                return;
            }
            int resourceId;
            int longPressBehavior = context.getResources().getInteger(17694799);
            if (mRebootSafeMode) {
                resourceId = 17039653;
            } else if (longPressBehavior == BROADCAST_STOP_PERCENT) {
                resourceId = 17039651;
            } else {
                resourceId = 17039650;
            }
            Log.d(TAG, "Notifying thread to start shutdown longPressBehavior=" + longPressBehavior);
            if (!confirm) {
                beginShutdownSequence(context);
            } else if (!HwServiceFactory.getHwShutdownThread().needRebootDialog(mReason, context)) {
                int i;
                CloseDialogReceiver closer = new CloseDialogReceiver(context);
                if (sConfirmDialog != null) {
                    sConfirmDialog.dismiss();
                }
                Builder builder = new Builder(context, 33947691);
                if (mRebootSafeMode) {
                    i = 17039652;
                } else {
                    i = 17039639;
                }
                sConfirmDialog = builder.setTitle(i).setMessage(resourceId).setPositiveButton(17039379, new AnonymousClass1(context)).setNegativeButton(17039369, null).create();
                closer.dialog = sConfirmDialog;
                sConfirmDialog.setOnDismissListener(closer);
                sConfirmDialog.getWindow().setType(2009);
                sConfirmDialog.show();
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

    public static void beginShutdownSequence(Context context) {
        context.sendBroadcast(new Intent(ACTION_ACTURAL_SHUTDOWN));
        synchronized (sIsStartedGuard) {
            if (sIsStarted) {
                Log.d(TAG, "Shutdown sequence already running, returning.");
                return;
            }
            sIsStarted = true;
            Flog.e(1600, "ShutdownThread beginShutdownSequence uid=" + Binder.getCallingUid() + ", pid=" + Binder.getCallingPid());
            if (!(iHwShutdownThread.isDoShutdownAnimation() || iHwShutdownThread.needRebootProgressDialog(mReboot, context) || HwPolicyFactory.isHwGlobalActionsShowing())) {
                ProgressDialog pd = new ProgressDialog(context, 33947691);
                if ("recovery-update".equals(mReason)) {
                    boolean z = RecoverySystem.UNCRYPT_PACKAGE_FILE.exists() ? !RecoverySystem.BLOCK_MAP_FILE.exists() : false;
                    mRebootHasProgressBar = z;
                    pd.setTitle(context.getText(17039643));
                    if (mRebootHasProgressBar) {
                        pd.setMax(PHONE_STATE_POLL_SLEEP_MSEC);
                        pd.setProgress(0);
                        pd.setIndeterminate(false);
                        pd.setProgressNumberFormat(null);
                        pd.setProgressStyle(1);
                        pd.setMessage(context.getText(17039644));
                    } else {
                        pd.setIndeterminate(true);
                        pd.setMessage(context.getText(17039646));
                    }
                } else if ("recovery".equals(mReason)) {
                    pd.setTitle(context.getText(17039647));
                    pd.setMessage(context.getText(17039648));
                    pd.setIndeterminate(true);
                } else {
                    pd.setTitle(context.getText(17039639));
                    pd.setMessage(context.getText(17039649));
                    pd.setIndeterminate(true);
                }
                pd.setCancelable(false);
                pd.getWindow().setType(2009);
                pd.show();
                sInstance.mProgressDialog = pd;
            }
            sInstance.mContext = context;
            sInstance.mPowerManager = (PowerManager) context.getSystemService("power");
            sInstance.mCpuWakeLock = null;
            try {
                sInstance.mCpuWakeLock = sInstance.mPowerManager.newWakeLock(1, "ShutdownThread-cpu");
                sInstance.mCpuWakeLock.setReferenceCounted(false);
                sInstance.mCpuWakeLock.acquire();
            } catch (SecurityException e) {
                Log.w(TAG, "No permission to acquire wake lock", e);
                sInstance.mCpuWakeLock = null;
            }
            sInstance.mScreenWakeLock = null;
            if (sInstance.mPowerManager.isScreenOn()) {
                try {
                    sInstance.mScreenWakeLock = sInstance.mPowerManager.newWakeLock(26, "ShutdownThread-screen");
                    sInstance.mScreenWakeLock.setReferenceCounted(false);
                    sInstance.mScreenWakeLock.acquire();
                } catch (SecurityException e2) {
                    Log.w(TAG, "No permission to acquire wake lock", e2);
                    sInstance.mScreenWakeLock = null;
                }
            }
            sInstance.mHandler = new Handler() {
            };
            sInstance.start();
        }
    }

    void actionDone() {
        synchronized (this.mActionDoneSync) {
            this.mActionDone = true;
            this.mActionDoneSync.notifyAll();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void run() {
        String str;
        BroadcastReceiver br = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                ShutdownThread.this.actionDone();
            }
        };
        AlarmManager alarmManager = (AlarmManager) sInstance.mContext.getSystemService("alarm");
        if (alarmManager != null) {
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
        intent.addFlags(268435456);
        this.mContext.sendOrderedBroadcastAsUser(intent, UserHandle.ALL, null, br, this.mHandler, 0, null, null);
        long endTime = SystemClock.elapsedRealtime() + JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY;
        synchronized (this.mActionDoneSync) {
            while (true) {
                if (this.mActionDone) {
                    break;
                }
                long delay = endTime - SystemClock.elapsedRealtime();
                if (delay <= 0) {
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
            sInstance.setRebootProgress(BROADCAST_STOP_PERCENT, null);
        }
        Log.i(TAG, "Shutting down activity manager...");
        IActivityManager am = ActivityManagerNative.asInterface(ServiceManager.checkService("activity"));
        if (am != null) {
            try {
                am.shutdown(MAX_BROADCAST_TIME);
            } catch (RemoteException e2) {
            }
        }
        if (mRebootHasProgressBar) {
            sInstance.setRebootProgress(ACTIVITY_MANAGER_STOP_PERCENT, null);
        }
        Log.i(TAG, "Shutting down package manager...");
        PackageManagerService pm = (PackageManagerService) ServiceManager.getService(HwBroadcastRadarUtil.KEY_PACKAGE);
        if (pm != null) {
            pm.shutdown();
        }
        if (mRebootHasProgressBar) {
            sInstance.setRebootProgress(PACKAGE_MANAGER_STOP_PERCENT, null);
        }
        shutdownRadios(MAX_RADIO_WAIT_TIME);
        if (mRebootHasProgressBar) {
            sInstance.setRebootProgress(RADIO_STOP_PERCENT, null);
        }
        IMountShutdownObserver anonymousClass4 = new IMountShutdownObserver.Stub() {
            public void onShutDownComplete(int statusCode) throws RemoteException {
                Log.w(ShutdownThread.TAG, "Result code " + statusCode + " from MountService.shutdown");
                ShutdownThread.this.actionDone();
            }
        };
        Log.i(TAG, "Shutting down MountService");
        this.mActionDone = false;
        long endShutTime = SystemClock.elapsedRealtime() + 20000;
        synchronized (this.mActionDoneSync) {
            try {
                IMountService mount = IMountService.Stub.asInterface(ServiceManager.checkService("mount"));
                if (mount != null) {
                    mount.shutdown(anonymousClass4);
                } else {
                    Log.w(TAG, "MountService unavailable for shutdown");
                }
            } catch (Throwable e3) {
                Log.e(TAG, "Exception during MountService shutdown", e3);
            }
            while (true) {
                if (this.mActionDone) {
                    break;
                }
                delay = endShutTime - SystemClock.elapsedRealtime();
                if (delay <= 0) {
                    break;
                }
                if (mRebootHasProgressBar) {
                    sInstance.setRebootProgress(((int) (((((double) (20000 - delay)) * 1.0d) * 2.0d) / 20000.0d)) + RADIO_STOP_PERCENT, null);
                }
                try {
                    this.mActionDoneSync.wait(Math.min(delay, 100));
                } catch (InterruptedException e4) {
                }
            }
            Log.w(TAG, "Shutdown wait timed out");
        }
        if (mRebootHasProgressBar) {
            sInstance.setRebootProgress(MOUNT_SERVICE_STOP_PERCENT, null);
            uncrypt();
        }
        rebootOrShutdown(this.mContext, mReboot, mReason, shutDownBegin);
    }

    private void setRebootProgress(int progress, CharSequence message) {
        this.mHandler.post(new AnonymousClass5(progress, message));
    }

    private void shutdownRadios(int timeout) {
        boolean[] done = new boolean[1];
        Thread t = new AnonymousClass6(SystemClock.elapsedRealtime() + ((long) timeout), timeout, done);
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
        boolean[] done = new boolean[]{false};
        Thread t = new AnonymousClass8(new ProgressListener() {
            public void onProgress(int status) {
                if (status >= 0 && status < ShutdownThread.PHONE_STATE_POLL_SLEEP_MSEC) {
                    ShutdownThread.sInstance.setRebootProgress(((int) ((((double) status) * 80.0d) / 100.0d)) + ShutdownThread.MOUNT_SERVICE_STOP_PERCENT, ShutdownThread.this.mContext.getText(17039645));
                } else if (status == ShutdownThread.PHONE_STATE_POLL_SLEEP_MSEC) {
                    ShutdownThread.sInstance.setRebootProgress(status, ShutdownThread.this.mContext.getText(17039646));
                }
            }
        }, done);
        t.start();
        try {
            t.join(900000);
        } catch (InterruptedException e) {
        }
        if (!done[0]) {
            Log.w(TAG, "Timed out waiting for uncrypt.");
        }
    }

    private static void deviceRebootOrShutdown(boolean reboot, String reason) {
        String deviceShutdownClassName = "com.qti.server.power.ShutdownOem";
        try {
            Class<?> cl = Class.forName(deviceShutdownClassName);
            try {
                Class[] clsArr = new Class[BROADCAST_STOP_PERCENT];
                clsArr[0] = Boolean.TYPE;
                clsArr[1] = String.class;
                Method m = cl.getMethod("rebootOrShutdown", clsArr);
                Object newInstance = cl.newInstance();
                Object[] objArr = new Object[BROADCAST_STOP_PERCENT];
                objArr[0] = Boolean.valueOf(reboot);
                objArr[1] = reason;
                m.invoke(newInstance, objArr);
            } catch (NoSuchMethodException e) {
                Log.e(TAG, "rebootOrShutdown method not found in class " + deviceShutdownClassName);
            } catch (Exception e2) {
                Log.e(TAG, "Unknown exception hit while trying to invode rebootOrShutdown");
            }
        } catch (ClassNotFoundException e3) {
            Log.e(TAG, "Unable to find class " + deviceShutdownClassName);
        }
    }

    public static AlertDialog getsConfirmDialog() {
        return sConfirmDialog;
    }
}
