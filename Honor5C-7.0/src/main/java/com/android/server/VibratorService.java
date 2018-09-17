package com.android.server;

import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.hardware.input.InputManager;
import android.hardware.input.InputManager.InputDeviceListener;
import android.media.AudioAttributes;
import android.media.AudioAttributes.Builder;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.IVibratorService.Stub;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.PowerManagerInternal;
import android.os.PowerManagerInternal.LowPowerModeListener;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.os.WorkSource;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.util.Flog;
import android.util.Slog;
import com.android.internal.app.IAppOpsService;
import com.android.internal.app.IBatteryStats;
import com.android.server.am.ProcessList;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

public class VibratorService extends Stub implements InputDeviceListener {
    private static final boolean DEBUG = false;
    private static final int DEFAULT_CODE = -2;
    private static final long DEFAULT_VIBRATE_TIME = 60;
    private static final int EXCEPTION_CODE = -1;
    private static final String SYSTEM_UI_PACKAGE = "com.android.systemui";
    private static final String TAG = "VibratorService";
    private final IAppOpsService mAppOpsService;
    private final IBatteryStats mBatteryStatsService;
    private final Context mContext;
    private int mCurVibUid;
    private Vibration mCurrentVibration;
    private final Handler mH;
    private InputManager mIm;
    private boolean mInputDeviceListenerRegistered;
    private final ArrayList<Vibrator> mInputDeviceVibrators;
    BroadcastReceiver mIntentReceiver;
    private boolean mLowPowerMode;
    private PowerManagerInternal mPowerManagerInternal;
    private final LinkedList<VibrationInfo> mPreviousVibrations;
    private final int mPreviousVibrationsLimit;
    private SettingsObserver mSettingObserver;
    volatile VibrateThread mThread;
    private final WorkSource mTmpWorkSource;
    private boolean mVibrateInputDevicesSetting;
    private final Runnable mVibrationRunnable;
    private final LinkedList<Vibration> mVibrations;
    private final WakeLock mWakeLock;

    private final class SettingsObserver extends ContentObserver {
        public SettingsObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean SelfChange) {
            VibratorService.this.updateInputDeviceVibrators();
        }
    }

    private class VibrateThread extends Thread {
        boolean mDone;
        final Vibration mVibration;

        VibrateThread(Vibration vib) {
            this.mVibration = vib;
            VibratorService.this.mTmpWorkSource.set(vib.mUid);
            VibratorService.this.mWakeLock.setWorkSource(VibratorService.this.mTmpWorkSource);
            VibratorService.this.mWakeLock.acquire();
        }

        private void delay(long duration) {
            if (duration > 0) {
                long bedtime = duration + SystemClock.uptimeMillis();
                while (true) {
                    try {
                        wait(duration);
                    } catch (InterruptedException e) {
                    }
                    if (!this.mDone) {
                        duration = bedtime - SystemClock.uptimeMillis();
                        if (duration <= 0) {
                            return;
                        }
                    } else {
                        return;
                    }
                }
            }
        }

        public void run() {
            Process.setThreadPriority(-8);
            synchronized (this) {
                long[] pattern = this.mVibration.mPattern;
                int len = pattern.length;
                int repeat = this.mVibration.mRepeat;
                int uid = this.mVibration.mUid;
                int usageHint = this.mVibration.mUsageHint;
                long duration = 0;
                int index = 0;
                while (!this.mDone) {
                    if (index < len) {
                        duration += pattern[index];
                        index++;
                    }
                    delay(duration);
                    if (this.mDone) {
                        int i = index;
                        break;
                    }
                    if (index < len) {
                        i = index + 1;
                        duration = pattern[index];
                        if (duration > 0) {
                            VibratorService.this.doVibratorOn(duration, uid, usageHint);
                        }
                    } else if (repeat < 0) {
                        i = index;
                        break;
                    } else {
                        i = repeat;
                        duration = 0;
                    }
                    index = i;
                }
                VibratorService.this.mWakeLock.release();
            }
            synchronized (VibratorService.this.mVibrations) {
                if (VibratorService.this.mThread == this) {
                    VibratorService.this.mThread = null;
                }
                if (!this.mDone) {
                    VibratorService.this.unlinkVibration(this.mVibration);
                    VibratorService.this.startNextVibrationLocked();
                }
            }
        }
    }

    private class Vibration implements DeathRecipient {
        private int mMode;
        private final String mOpPkg;
        private final long[] mPattern;
        private final int mRepeat;
        private final long mStartTime;
        private final long mTimeout;
        private final IBinder mToken;
        private final int mUid;
        private final int mUsageHint;

        public void setMode(int mode) {
            this.mMode = mode;
        }

        public int getMode() {
            return this.mMode;
        }

        Vibration(VibratorService this$0, IBinder token, long millis, int usageHint, int uid, String opPkg) {
            this(token, millis, null, 0, usageHint, uid, opPkg);
        }

        Vibration(VibratorService this$0, IBinder token, long[] pattern, int repeat, int usageHint, int uid, String opPkg) {
            this(token, 0, pattern, repeat, usageHint, uid, opPkg);
        }

        private Vibration(IBinder token, long millis, long[] pattern, int repeat, int usageHint, int uid, String opPkg) {
            this.mMode = 0;
            this.mToken = token;
            this.mTimeout = millis;
            this.mStartTime = SystemClock.uptimeMillis();
            this.mPattern = pattern;
            this.mRepeat = repeat;
            this.mUsageHint = usageHint;
            this.mUid = uid;
            this.mOpPkg = opPkg;
        }

        public void binderDied() {
            synchronized (VibratorService.this.mVibrations) {
                VibratorService.this.mVibrations.remove(this);
                if (this == VibratorService.this.mCurrentVibration) {
                    VibratorService.this.doCancelVibrateLocked();
                    VibratorService.this.startNextVibrationLocked();
                }
            }
        }

        public boolean hasLongerTimeout(long millis) {
            if (this.mTimeout != 0 && this.mStartTime + this.mTimeout >= SystemClock.uptimeMillis() + millis) {
                return true;
            }
            return VibratorService.DEBUG;
        }

        public boolean isSystemHapticFeedback() {
            if (!(this.mUid == ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE || this.mUid == 0)) {
                if (!VibratorService.SYSTEM_UI_PACKAGE.equals(this.mOpPkg)) {
                    return VibratorService.DEBUG;
                }
            }
            if (this.mRepeat < 0) {
                return true;
            }
            return VibratorService.DEBUG;
        }
    }

    private static class VibrationInfo {
        String opPkg;
        long[] pattern;
        int repeat;
        long startTime;
        long timeout;
        int uid;
        int usageHint;

        public VibrationInfo(long timeout, long startTime, long[] pattern, int repeat, int usageHint, int uid, String opPkg) {
            this.timeout = timeout;
            this.startTime = startTime;
            this.pattern = pattern;
            this.repeat = repeat;
            this.usageHint = usageHint;
            this.uid = uid;
            this.opPkg = opPkg;
        }

        public String toString() {
            return "timeout: " + this.timeout + ", startTime: " + this.startTime + ", pattern: " + Arrays.toString(this.pattern) + ", repeat: " + this.repeat + ", usageHint: " + this.usageHint + ", uid: " + this.uid + ", opPkg: " + this.opPkg;
        }
    }

    static native boolean vibratorExists();

    static native void vibratorInit();

    static native void vibratorOff();

    static native void vibratorOn(long j);

    static native int vibratorWrite(int i);

    VibratorService(Context context) {
        this.mTmpWorkSource = new WorkSource();
        this.mH = new Handler();
        this.mInputDeviceVibrators = new ArrayList();
        this.mCurVibUid = EXCEPTION_CODE;
        this.mVibrationRunnable = new Runnable() {
            public void run() {
                synchronized (VibratorService.this.mVibrations) {
                    VibratorService.this.doCancelVibrateLocked();
                    VibratorService.this.startNextVibrationLocked();
                }
            }
        };
        this.mIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
                    synchronized (VibratorService.this.mVibrations) {
                        if (!(VibratorService.this.mCurrentVibration == null || VibratorService.this.mCurrentVibration.isSystemHapticFeedback())) {
                            VibratorService.this.doCancelVibrateLocked();
                        }
                        Iterator<Vibration> it = VibratorService.this.mVibrations.iterator();
                        while (it.hasNext()) {
                            Vibration vibration = (Vibration) it.next();
                            if (vibration != VibratorService.this.mCurrentVibration) {
                                VibratorService.this.unlinkVibration(vibration);
                                it.remove();
                            }
                        }
                    }
                }
            }
        };
        vibratorInit();
        vibratorOff();
        this.mContext = context;
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, "*vibrator*");
        this.mWakeLock.setReferenceCounted(true);
        this.mAppOpsService = IAppOpsService.Stub.asInterface(ServiceManager.getService("appops"));
        this.mBatteryStatsService = IBatteryStats.Stub.asInterface(ServiceManager.getService("batterystats"));
        this.mPreviousVibrationsLimit = this.mContext.getResources().getInteger(17694881);
        this.mVibrations = new LinkedList();
        this.mPreviousVibrations = new LinkedList();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_OFF");
        context.registerReceiver(this.mIntentReceiver, filter);
    }

    public void systemReady() {
        this.mIm = (InputManager) this.mContext.getSystemService(InputManager.class);
        this.mSettingObserver = new SettingsObserver(this.mH);
        this.mPowerManagerInternal = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
        this.mPowerManagerInternal.registerLowPowerModeObserver(new LowPowerModeListener() {
            public void onLowPowerModeChanged(boolean enabled) {
                VibratorService.this.updateInputDeviceVibrators();
            }
        });
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor("vibrate_input_devices"), true, this.mSettingObserver, EXCEPTION_CODE);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                VibratorService.this.updateInputDeviceVibrators();
            }
        }, new IntentFilter("android.intent.action.USER_SWITCHED"), null, this.mH);
        updateInputDeviceVibrators();
    }

    public boolean hasVibrator() {
        return doVibratorExists();
    }

    private void verifyIncomingUid(int uid) {
        if (uid != Binder.getCallingUid() && Binder.getCallingPid() != Process.myPid()) {
            this.mContext.enforcePermission("android.permission.UPDATE_APP_OPS_STATS", Binder.getCallingPid(), Binder.getCallingUid(), null);
        }
    }

    public void vibrate(int uid, String opPkg, long milliseconds, int usageHint, IBinder token) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.VIBRATE") != 0) {
            throw new SecurityException("Requires VIBRATE permission");
        }
        verifyIncomingUid(uid);
        if (milliseconds > 0 && (this.mCurrentVibration == null || !this.mCurrentVibration.hasLongerTimeout(milliseconds))) {
            Flog.i(NativeResponseCode.SERVICE_REGISTERED, "Vibrating for " + milliseconds + " ms.");
            Vibration vib = new Vibration(this, token, milliseconds, usageHint, uid, opPkg);
            Flog.i(NativeResponseCode.SERVICE_REGISTERED, "vibrate on: uid is " + uid + ",pid is " + Binder.getCallingPid() + ",packagename = " + opPkg);
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (this.mVibrations) {
                    removeVibrationLocked(token);
                    doCancelVibrateLocked();
                    addToPreviousVibrationsLocked(vib);
                    startVibrationLocked(vib);
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public void hwVibrate(int uid, String opPkg, int usageHint, IBinder token, int mode) {
        Throwable th;
        if (!"com.huawei.android.launcher".equals(opPkg) || this.mContext.checkCallingOrSelfPermission("android.permission.VIBRATE") == 0) {
            verifyIncomingUid(uid);
            Slog.d(TAG, "hw Vibrating for  opPkg " + opPkg);
            Vibration vib = new Vibration(this, token, 0, usageHint, uid, opPkg);
            vib.setMode(mode);
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (this.mVibrations) {
                    try {
                        removeVibrationLocked(token);
                        doCancelVibrateLocked();
                        this.mCurrentVibration = vib;
                        if (EXCEPTION_CODE == startVibrationLocked(vib)) {
                            Vibration vib2 = new Vibration(this, token, DEFAULT_VIBRATE_TIME, usageHint, uid, opPkg);
                            try {
                                startVibrationLocked(vib2);
                                vib = vib2;
                            } catch (Throwable th2) {
                                th = th2;
                                vib = vib2;
                                throw th;
                            }
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        throw th;
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        } else {
            throw new SecurityException("Requires VIBRATE permission");
        }
    }

    private boolean isAll0(long[] pattern) {
        for (long j : pattern) {
            if (j != 0) {
                return DEBUG;
            }
        }
        return true;
    }

    public void vibratePattern(int uid, String packageName, long[] pattern, int repeat, int usageHint, IBinder token) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.VIBRATE") != 0) {
            throw new SecurityException("Requires VIBRATE permission");
        }
        verifyIncomingUid(uid);
        long identity = Binder.clearCallingIdentity();
        if (pattern != null) {
            try {
                if (pattern.length != 0) {
                    if (!(isAll0(pattern) || repeat >= pattern.length || token == null)) {
                        Vibration vib = new Vibration(this, token, pattern, repeat, usageHint, uid, packageName);
                        Flog.i(NativeResponseCode.SERVICE_REGISTERED, "vibratePattern on: uid is " + uid + ",pid is " + Binder.getCallingPid() + ",packagename = " + packageName);
                        try {
                            token.linkToDeath(vib, 0);
                            synchronized (this.mVibrations) {
                                removeVibrationLocked(token);
                                doCancelVibrateLocked();
                                if (repeat >= 0) {
                                    this.mVibrations.addFirst(vib);
                                    startNextVibrationLocked();
                                } else {
                                    startVibrationLocked(vib);
                                }
                                addToPreviousVibrationsLocked(vib);
                            }
                            Binder.restoreCallingIdentity(identity);
                            return;
                        } catch (RemoteException e) {
                            Binder.restoreCallingIdentity(identity);
                            return;
                        }
                    }
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
            }
        }
        Binder.restoreCallingIdentity(identity);
    }

    private void addToPreviousVibrationsLocked(Vibration vib) {
        if (this.mPreviousVibrations.size() > this.mPreviousVibrationsLimit) {
            this.mPreviousVibrations.removeFirst();
        }
        this.mPreviousVibrations.addLast(new VibrationInfo(vib.mTimeout, vib.mStartTime, vib.mPattern, vib.mRepeat, vib.mUsageHint, vib.mUid, vib.mOpPkg));
    }

    public void cancelVibrate(IBinder token) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.VIBRATE", "cancelVibrate");
        long identity = Binder.clearCallingIdentity();
        try {
            synchronized (this.mVibrations) {
                if (removeVibrationLocked(token) == this.mCurrentVibration) {
                    Flog.i(NativeResponseCode.SERVICE_REGISTERED, "Canceling vibration.");
                    doCancelVibrateLocked();
                    startNextVibrationLocked();
                }
            }
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private void doCancelVibrateLocked() {
        if (this.mThread != null) {
            synchronized (this.mThread) {
                this.mThread.mDone = true;
                this.mThread.notify();
            }
            this.mThread = null;
        }
        doVibratorOff();
        this.mH.removeCallbacks(this.mVibrationRunnable);
        reportFinishVibrationLocked();
    }

    private void startNextVibrationLocked() {
        if (this.mVibrations.size() <= 0) {
            reportFinishVibrationLocked();
            this.mCurrentVibration = null;
            return;
        }
        startVibrationLocked((Vibration) this.mVibrations.getFirst());
    }

    private int startVibrationLocked(Vibration vib) {
        try {
            if (this.mLowPowerMode && vib.mUsageHint != 6) {
                return DEFAULT_CODE;
            }
            int mode = this.mAppOpsService.checkAudioOperation(3, vib.mUsageHint, vib.mUid, vib.mOpPkg);
            if (mode == 0) {
                mode = this.mAppOpsService.startOperation(AppOpsManager.getToken(this.mAppOpsService), 3, vib.mUid, vib.mOpPkg);
            }
            if (mode == 0) {
                this.mCurrentVibration = vib;
                if (vib.getMode() != 0) {
                    return doVibratorHwOn(vib.getMode());
                }
                if (vib.mTimeout != 0) {
                    doVibratorOn(vib.mTimeout, vib.mUid, vib.mUsageHint);
                    this.mH.postDelayed(this.mVibrationRunnable, vib.mTimeout);
                } else if (vib.mPattern == null || vib.mPattern.length == 0) {
                    Flog.e(NativeResponseCode.SERVICE_REGISTERED, "pattern is null or pattern's length is 0");
                } else {
                    this.mThread = new VibrateThread(vib);
                    this.mThread.start();
                }
                return DEFAULT_CODE;
            }
            if (mode == 2) {
                Flog.w(NativeResponseCode.SERVICE_REGISTERED, "Would be an error: vibrate from uid " + vib.mUid);
            }
            this.mH.post(this.mVibrationRunnable);
            return DEFAULT_CODE;
        } catch (RemoteException e) {
        }
    }

    private int doVibratorHwOn(int mode) {
        int vibratorWrite;
        synchronized (this.mInputDeviceVibrators) {
            vibratorWrite = vibratorWrite(mode);
        }
        return vibratorWrite;
    }

    private void reportFinishVibrationLocked() {
        if (this.mCurrentVibration != null) {
            try {
                this.mAppOpsService.finishOperation(AppOpsManager.getToken(this.mAppOpsService), 3, this.mCurrentVibration.mUid, this.mCurrentVibration.mOpPkg);
            } catch (RemoteException e) {
            }
            this.mCurrentVibration = null;
        }
    }

    private Vibration removeVibrationLocked(IBinder token) {
        ListIterator<Vibration> iter = this.mVibrations.listIterator(0);
        while (iter.hasNext()) {
            Vibration vib = (Vibration) iter.next();
            if (vib.mToken == token) {
                iter.remove();
                unlinkVibration(vib);
                return vib;
            }
        }
        if (this.mCurrentVibration == null || this.mCurrentVibration.mToken != token) {
            return null;
        }
        unlinkVibration(this.mCurrentVibration);
        return this.mCurrentVibration;
    }

    private void unlinkVibration(Vibration vib) {
        if (vib.mPattern != null) {
            vib.mToken.unlinkToDeath(vib, 0);
        }
    }

    private void updateInputDeviceVibrators() {
        boolean z = true;
        synchronized (this.mVibrations) {
            doCancelVibrateLocked();
            synchronized (this.mInputDeviceVibrators) {
                this.mVibrateInputDevicesSetting = DEBUG;
                try {
                    if (System.getIntForUser(this.mContext.getContentResolver(), "vibrate_input_devices", DEFAULT_CODE) <= 0) {
                        z = DEBUG;
                    }
                    this.mVibrateInputDevicesSetting = z;
                } catch (SettingNotFoundException e) {
                }
                this.mLowPowerMode = this.mPowerManagerInternal.getLowPowerModeEnabled();
                if (this.mVibrateInputDevicesSetting) {
                    if (!this.mInputDeviceListenerRegistered) {
                        this.mInputDeviceListenerRegistered = true;
                        this.mIm.registerInputDeviceListener(this, this.mH);
                    }
                } else if (this.mInputDeviceListenerRegistered) {
                    this.mInputDeviceListenerRegistered = DEBUG;
                    this.mIm.unregisterInputDeviceListener(this);
                }
                this.mInputDeviceVibrators.clear();
                if (this.mVibrateInputDevicesSetting) {
                    int[] ids = this.mIm.getInputDeviceIds();
                    for (int inputDevice : ids) {
                        Vibrator vibrator = this.mIm.getInputDevice(inputDevice).getVibrator();
                        if (vibrator.hasVibrator()) {
                            this.mInputDeviceVibrators.add(vibrator);
                        }
                    }
                }
            }
            startNextVibrationLocked();
        }
    }

    public void onInputDeviceAdded(int deviceId) {
        updateInputDeviceVibrators();
    }

    public void onInputDeviceChanged(int deviceId) {
        updateInputDeviceVibrators();
    }

    public void onInputDeviceRemoved(int deviceId) {
        updateInputDeviceVibrators();
    }

    private boolean doVibratorExists() {
        return vibratorExists();
    }

    private void doVibratorOn(long millis, int uid, int usageHint) {
        synchronized (this.mInputDeviceVibrators) {
            Flog.i(NativeResponseCode.SERVICE_REGISTERED, "Turning vibrator on for " + millis + " ms.");
            try {
                this.mBatteryStatsService.noteVibratorOn(uid, millis);
                this.mCurVibUid = uid;
            } catch (RemoteException e) {
            }
            int vibratorCount = this.mInputDeviceVibrators.size();
            if (vibratorCount != 0) {
                AudioAttributes attributes = new Builder().setUsage(usageHint).build();
                for (int i = 0; i < vibratorCount; i++) {
                    ((Vibrator) this.mInputDeviceVibrators.get(i)).vibrate(millis, attributes);
                }
            } else {
                vibratorOn(millis);
            }
        }
    }

    private void doVibratorOff() {
        synchronized (this.mInputDeviceVibrators) {
            if (this.mCurVibUid >= 0) {
                try {
                    this.mBatteryStatsService.noteVibratorOff(this.mCurVibUid);
                } catch (RemoteException e) {
                }
                this.mCurVibUid = EXCEPTION_CODE;
            }
            int vibratorCount = this.mInputDeviceVibrators.size();
            if (vibratorCount != 0) {
                for (int i = 0; i < vibratorCount; i++) {
                    ((Vibrator) this.mInputDeviceVibrators.get(i)).cancel();
                }
            } else {
                vibratorOff();
            }
        }
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump vibrator service from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            return;
        }
        pw.println("Previous vibrations:");
        synchronized (this.mVibrations) {
            for (VibrationInfo info : this.mPreviousVibrations) {
                pw.print("  ");
                pw.println(info.toString());
            }
        }
    }
}
