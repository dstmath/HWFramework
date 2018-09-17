package com.android.server;

import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
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
import android.os.PowerSaveState;
import android.os.Process;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.ShellCallback;
import android.os.ShellCommand;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.VibrationEffect.OneShot;
import android.os.VibrationEffect.Prebaked;
import android.os.VibrationEffect.Waveform;
import android.os.Vibrator;
import android.os.WorkSource;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.util.Flog;
import android.util.Slog;
import android.view.InputDevice;
import com.android.internal.app.IAppOpsService;
import com.android.internal.app.IBatteryStats;
import com.android.internal.util.DumpUtils;
import huawei.cust.HwCustUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;

public class VibratorService extends Stub implements InputDeviceListener {
    private static final boolean DEBUG = false;
    private static final int DEFAULT_CODE = -2;
    private static final long DEFAULT_VIBRATE_TIME = 60;
    private static final int EXCEPTION_CODE = -1;
    private static final int MAX_BINDER_NUM = 1000;
    private static final String SYSTEM_UI_PACKAGE = "com.android.systemui";
    private static final String TAG = "VibratorService";
    private final boolean mAllowPriorityVibrationsInLowPowerMode;
    private final IAppOpsService mAppOpsService;
    private final IBatteryStats mBatteryStatsService;
    private final ArrayList<IBinder> mBinderList = new ArrayList();
    private final Context mContext;
    private int mCurVibUid = -1;
    private Vibration mCurrentVibration;
    private HwCustCbsUtils mCust;
    private final int mDefaultVibrationAmplitude;
    private final VibrationEffect[] mFallbackEffects;
    private final Handler mH = new Handler();
    private InputManager mIm;
    private boolean mInputDeviceListenerRegistered;
    private final ArrayList<Vibrator> mInputDeviceVibrators = new ArrayList();
    BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
                synchronized (VibratorService.this.mLock) {
                    if (!(VibratorService.this.mCurrentVibration == null || (VibratorService.this.mCurrentVibration.isSystemHapticFeedback() ^ 1) == 0)) {
                        if (((VibratorService.this.mCust != null ? VibratorService.this.mCust.isNotAllowPkg(VibratorService.this.mCurrentVibration.mOpPkg) : 0) ^ 1) != 0) {
                            VibratorService.this.doCancelVibrateLocked();
                        }
                    }
                }
            }
        }
    };
    private final Object mLock = new Object();
    private boolean mLowPowerMode;
    private PowerManagerInternal mPowerManagerInternal;
    private final LinkedList<VibrationInfo> mPreviousVibrations;
    private final int mPreviousVibrationsLimit;
    private SettingsObserver mSettingObserver;
    private final boolean mSupportsAmplitudeControl;
    private volatile VibrateThread mThread;
    private final WorkSource mTmpWorkSource = new WorkSource();
    private boolean mVibrateInputDevicesSetting;
    private final Runnable mVibrationEndRunnable = new Runnable() {
        public void run() {
            VibratorService.this.onVibrationFinished();
        }
    };
    private final WakeLock mWakeLock;

    private final class SettingsObserver extends ContentObserver {
        public SettingsObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean SelfChange) {
            VibratorService.this.updateVibrators();
        }
    }

    private class VibrateThread extends Thread {
        private boolean mForceStop;
        private final int mUid;
        private final int mUsageHint;
        private final Waveform mWaveform;

        VibrateThread(Waveform waveform, int uid, int usageHint) {
            this.mWaveform = waveform;
            this.mUid = uid;
            this.mUsageHint = usageHint;
            VibratorService.this.mTmpWorkSource.set(uid);
            VibratorService.this.mWakeLock.setWorkSource(VibratorService.this.mTmpWorkSource);
        }

        private long delayLocked(long duration) {
            long durationRemaining = duration;
            if (duration <= 0) {
                return 0;
            }
            long bedtime = duration + SystemClock.uptimeMillis();
            while (true) {
                try {
                    wait(durationRemaining);
                } catch (InterruptedException e) {
                }
                if (!this.mForceStop) {
                    durationRemaining = bedtime - SystemClock.uptimeMillis();
                    if (durationRemaining <= 0) {
                        break;
                    }
                } else {
                    break;
                }
            }
            return duration - durationRemaining;
        }

        public void run() {
            Process.setThreadPriority(-8);
            VibratorService.this.mWakeLock.acquire();
            try {
                if (playWaveform()) {
                    VibratorService.this.onVibrationFinished();
                }
                VibratorService.this.mWakeLock.release();
            } catch (Throwable th) {
                VibratorService.this.mWakeLock.release();
            }
        }

        /* JADX WARNING: Missing block: B:24:0x0070, code:
            r3 = r20.mForceStop ^ 1;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean playWaveform() {
            synchronized (this) {
                long[] timings = this.mWaveform.getTimings();
                int[] amplitudes = this.mWaveform.getAmplitudes();
                int len = timings.length;
                int repeat = this.mWaveform.getRepeatIndex();
                int index = 0;
                long onDuration = 0;
                while (true) {
                    int index2 = index;
                    if (!this.mForceStop) {
                        if (index2 < len) {
                            int amplitude = amplitudes[index2];
                            index = index2 + 1;
                            long duration = timings[index2];
                            if (duration > 0) {
                                if (amplitude != 0) {
                                    if (onDuration <= 0) {
                                        onDuration = getTotalOnDuration(timings, amplitudes, index - 1, repeat);
                                        VibratorService.this.doVibratorOn(onDuration, amplitude, this.mUid, this.mUsageHint);
                                    } else {
                                        VibratorService.this.doVibratorSetAmplitude(amplitude);
                                    }
                                }
                                long waitTime = delayLocked(duration);
                                if (amplitude != 0) {
                                    onDuration -= waitTime;
                                }
                            }
                        } else if (repeat >= 0) {
                            index = repeat;
                        }
                    }
                    break;
                }
            }
            return r3;
        }

        public void cancel() {
            synchronized (this) {
                VibratorService.this.mThread.mForceStop = true;
                VibratorService.this.mThread.notify();
            }
        }

        private long getTotalOnDuration(long[] timings, int[] amplitudes, int startIndex, int repeatIndex) {
            int i = startIndex;
            long timing = 0;
            while (amplitudes[i] != 0) {
                int i2 = i + 1;
                timing += timings[i];
                if (i2 >= timings.length) {
                    if (repeatIndex < 0) {
                        break;
                    }
                    i = repeatIndex;
                    continue;
                } else {
                    i = i2;
                    continue;
                }
                if (i == startIndex) {
                    return 1000;
                }
            }
            return timing;
        }
    }

    private class Vibration implements DeathRecipient {
        private final VibrationEffect mEffect;
        private int mMode;
        private final String mOpPkg;
        private final long mStartTime;
        private final IBinder mToken;
        private final int mUid;
        private final int mUsageHint;

        /* synthetic */ Vibration(VibratorService this$0, IBinder token, VibrationEffect effect, int usageHint, int uid, String opPkg, Vibration -this6) {
            this(token, effect, usageHint, uid, opPkg);
        }

        public void setMode(int mode) {
            this.mMode = mode;
        }

        public int getMode() {
            return this.mMode;
        }

        private Vibration(IBinder token, VibrationEffect effect, int usageHint, int uid, String opPkg) {
            this.mMode = 0;
            this.mToken = token;
            this.mEffect = effect;
            this.mStartTime = SystemClock.uptimeMillis();
            this.mUsageHint = usageHint;
            this.mUid = uid;
            this.mOpPkg = opPkg;
        }

        public void binderDied() {
            synchronized (VibratorService.this.mLock) {
                if (this == VibratorService.this.mCurrentVibration) {
                    VibratorService.this.doCancelVibrateLocked();
                }
            }
        }

        public boolean hasLongerTimeout(long millis) {
            boolean z = false;
            if (!(this.mEffect instanceof OneShot)) {
                return false;
            }
            if (this.mStartTime + this.mEffect.getTiming() > SystemClock.uptimeMillis() + millis) {
                z = true;
            }
            return z;
        }

        public boolean isSystemHapticFeedback() {
            boolean repeating = false;
            if (this.mEffect instanceof Waveform) {
                repeating = ((Waveform) this.mEffect).getRepeatIndex() < 0;
            }
            if (this.mUid == 1000 || this.mUid == 0 || VibratorService.SYSTEM_UI_PACKAGE.equals(this.mOpPkg)) {
                return repeating ^ 1;
            }
            return false;
        }
    }

    private static class VibrationInfo {
        private final VibrationEffect mEffect;
        private final String mOpPkg;
        private final long mStartTime;
        private final int mUid;
        private final int mUsageHint;

        public VibrationInfo(long startTime, VibrationEffect effect, int usageHint, int uid, String opPkg) {
            this.mStartTime = startTime;
            this.mEffect = effect;
            this.mUsageHint = usageHint;
            this.mUid = uid;
            this.mOpPkg = opPkg;
        }

        public String toString() {
            return ", startTime: " + this.mStartTime + ", effect: " + this.mEffect + ", usageHint: " + this.mUsageHint + ", uid: " + this.mUid + ", opPkg: " + this.mOpPkg;
        }
    }

    private final class VibratorShellCommand extends ShellCommand {
        private static final long MAX_VIBRATION_MS = 200;
        private final IBinder mToken;

        /* synthetic */ VibratorShellCommand(VibratorService this$0, IBinder token, VibratorShellCommand -this2) {
            this(token);
        }

        private VibratorShellCommand(IBinder token) {
            this.mToken = token;
        }

        public int onCommand(String cmd) {
            if ("vibrate".equals(cmd)) {
                return runVibrate();
            }
            return handleDefaultCommands(cmd);
        }

        private int runVibrate() {
            long duration = Long.parseLong(getNextArgRequired());
            if (duration > MAX_VIBRATION_MS) {
                throw new IllegalArgumentException("maximum duration is 200");
            }
            String description = getNextArg();
            if (description == null) {
                description = "Shell command";
            }
            VibratorService.this.vibrate(Binder.getCallingUid(), description, VibrationEffect.createOneShot(duration, -1), 0, this.mToken);
            return 0;
        }

        public void onHelp() {
            Throwable th;
            Throwable th2 = null;
            PrintWriter printWriter = null;
            try {
                printWriter = getOutPrintWriter();
                printWriter.println("Vibrator commands:");
                printWriter.println("  help");
                printWriter.println("    Prints this help text.");
                printWriter.println("");
                printWriter.println("  vibrate duration [description]");
                printWriter.println("    Vibrates for duration milliseconds.");
                printWriter.println("");
                if (printWriter != null) {
                    try {
                        printWriter.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 != null) {
                    throw th2;
                }
                return;
            } catch (Throwable th22) {
                Throwable th4 = th22;
                th22 = th;
                th = th4;
            }
            if (printWriter != null) {
                try {
                    printWriter.close();
                } catch (Throwable th5) {
                    if (th22 == null) {
                        th22 = th5;
                    } else if (th22 != th5) {
                        th22.addSuppressed(th5);
                    }
                }
            }
            if (th22 != null) {
                throw th22;
            }
            throw th;
        }
    }

    static native boolean vibratorExists();

    static native void vibratorInit();

    static native void vibratorOff();

    static native void vibratorOn(long j);

    static native long vibratorPerformEffect(long j, long j2);

    static native void vibratorSetAmplitude(int i);

    static native boolean vibratorSupportsAmplitudeControl();

    static native int vibratorWrite(int i);

    VibratorService(Context context) {
        VibrationEffect clickEffect;
        this.mCust = (HwCustCbsUtils) HwCustUtils.createObj(HwCustCbsUtils.class, new Object[]{context});
        vibratorInit();
        vibratorOff();
        this.mSupportsAmplitudeControl = vibratorSupportsAmplitudeControl();
        this.mContext = context;
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, "*vibrator*");
        this.mWakeLock.setReferenceCounted(true);
        this.mAppOpsService = IAppOpsService.Stub.asInterface(ServiceManager.getService("appops"));
        this.mBatteryStatsService = IBatteryStats.Stub.asInterface(ServiceManager.getService("batterystats"));
        this.mPreviousVibrationsLimit = this.mContext.getResources().getInteger(17694838);
        this.mDefaultVibrationAmplitude = this.mContext.getResources().getInteger(17694773);
        this.mAllowPriorityVibrationsInLowPowerMode = this.mContext.getResources().getBoolean(17956875);
        this.mPreviousVibrations = new LinkedList();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_OFF");
        context.registerReceiver(this.mIntentReceiver, filter);
        long[] clickEffectTimings = getLongIntArray(context.getResources(), 17236046);
        if (clickEffectTimings.length == 0) {
            clickEffect = null;
        } else if (clickEffectTimings.length == 1) {
            clickEffect = VibrationEffect.createOneShot(clickEffectTimings[0], -1);
        } else {
            clickEffect = VibrationEffect.createWaveform(clickEffectTimings, -1);
        }
        VibrationEffect doubleClickEffect = VibrationEffect.createWaveform(new long[]{0, 30, 100, 30}, -1);
        this.mFallbackEffects = new VibrationEffect[]{clickEffect, doubleClickEffect};
    }

    public void systemReady() {
        this.mIm = (InputManager) this.mContext.getSystemService(InputManager.class);
        this.mSettingObserver = new SettingsObserver(this.mH);
        this.mPowerManagerInternal = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
        this.mPowerManagerInternal.registerLowPowerModeObserver(new LowPowerModeListener() {
            public int getServiceType() {
                return 2;
            }

            public void onLowPowerModeChanged(PowerSaveState result) {
                VibratorService.this.updateVibrators();
            }
        });
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor("vibrate_input_devices"), true, this.mSettingObserver, -1);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                VibratorService.this.updateVibrators();
            }
        }, new IntentFilter("android.intent.action.USER_SWITCHED"), null, this.mH);
        updateVibrators();
    }

    public boolean hasVibrator() {
        return doVibratorExists();
    }

    public boolean hasAmplitudeControl() {
        boolean isEmpty;
        synchronized (this.mInputDeviceVibrators) {
            isEmpty = this.mSupportsAmplitudeControl ? this.mInputDeviceVibrators.isEmpty() : false;
        }
        return isEmpty;
    }

    private void verifyIncomingUid(int uid) {
        if (uid != Binder.getCallingUid() && Binder.getCallingPid() != Process.myPid()) {
            this.mContext.enforcePermission("android.permission.UPDATE_APP_OPS_STATS", Binder.getCallingPid(), Binder.getCallingUid(), null);
        }
    }

    private static boolean verifyVibrationEffect(VibrationEffect effect) {
        if (effect == null) {
            Slog.wtf(TAG, "effect must not be null");
            return false;
        }
        try {
            effect.validate();
            return true;
        } catch (Exception e) {
            Slog.wtf(TAG, "Encountered issue when verifying VibrationEffect.", e);
            return false;
        }
    }

    public void hwVibrate(int uid, String opPkg, int usageHint, IBinder token, int mode) {
        Throwable th;
        if (!"com.huawei.android.launcher".equals(opPkg) || this.mContext.checkCallingOrSelfPermission("android.permission.VIBRATE") == 0) {
            verifyIncomingUid(uid);
            Slog.d(TAG, "hw Vibrating for  opPkg " + opPkg);
            Vibration vib = new Vibration(this, token, null, usageHint, uid, opPkg, null);
            vib.setMode(mode);
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (this.mLock) {
                    try {
                        doCancelVibrateLocked();
                        this.mCurrentVibration = vib;
                        if (-1 == startVibrationLocked(vib)) {
                            Vibration vib2 = new Vibration(this, token, VibrationEffect.createOneShot(DEFAULT_VIBRATE_TIME, -1), usageHint, uid, opPkg, null);
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

    private static long[] getLongIntArray(Resources r, int resid) {
        int[] ar = r.getIntArray(resid);
        if (ar == null) {
            return null;
        }
        long[] out = new long[ar.length];
        for (int i = 0; i < ar.length; i++) {
            out[i] = (long) ar[i];
        }
        return out;
    }

    public void vibrate(int uid, String opPkg, VibrationEffect effect, int usageHint, IBinder token) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.VIBRATE") != 0) {
            throw new SecurityException("Requires VIBRATE permission");
        } else if (token == null) {
            Slog.e(TAG, "token must not be null");
        } else {
            verifyIncomingUid(uid);
            if (verifyVibrationEffect(effect)) {
                if ((effect instanceof OneShot) && this.mCurrentVibration != null && (this.mCurrentVibration.mEffect instanceof OneShot)) {
                    OneShot newOneShot = (OneShot) effect;
                    OneShot currentOneShot = (OneShot) this.mCurrentVibration.mEffect;
                    if (this.mCurrentVibration.hasLongerTimeout(newOneShot.getTiming()) && newOneShot.getAmplitude() == currentOneShot.getAmplitude()) {
                        return;
                    }
                }
                Vibration vib = new Vibration(this, token, effect, usageHint, uid, opPkg, null);
                Flog.i(NativeResponseCode.SERVICE_REGISTERED, "vibrate on: uid is " + uid + ",pid is " + Binder.getCallingPid() + ",packagename = " + opPkg);
                if (effect instanceof Waveform) {
                    IBinder binder = token;
                    try {
                        if (this.mBinderList.size() > 1000) {
                            this.mBinderList.clear();
                            this.mBinderList.add(token);
                            token.linkToDeath(vib, 0);
                        } else if (this.mBinderList.contains(token)) {
                            Flog.i(NativeResponseCode.SERVICE_REGISTERED, " the token need be ignored");
                        } else {
                            token.linkToDeath(vib, 0);
                            this.mBinderList.add(token);
                        }
                    } catch (RemoteException e) {
                        return;
                    }
                }
                long ident = Binder.clearCallingIdentity();
                try {
                    synchronized (this.mLock) {
                        doCancelVibrateLocked();
                        startVibrationLocked(vib);
                        addToPreviousVibrationsLocked(vib);
                    }
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }
    }

    private void addToPreviousVibrationsLocked(Vibration vib) {
        if (this.mPreviousVibrations.size() > this.mPreviousVibrationsLimit) {
            this.mPreviousVibrations.removeFirst();
        }
        this.mPreviousVibrations.addLast(new VibrationInfo(vib.mStartTime, vib.mEffect, vib.mUsageHint, vib.mUid, vib.mOpPkg));
    }

    public void cancelVibrate(IBinder token) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.VIBRATE", "cancelVibrate");
        synchronized (this.mLock) {
            if (this.mCurrentVibration != null && this.mCurrentVibration.mToken == token) {
                long ident = Binder.clearCallingIdentity();
                try {
                    Flog.i(NativeResponseCode.SERVICE_REGISTERED, "Canceling vibration.");
                    doCancelVibrateLocked();
                    Binder.restoreCallingIdentity(ident);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }
    }

    private void doCancelVibrateLocked() {
        this.mH.removeCallbacks(this.mVibrationEndRunnable);
        if (this.mThread != null) {
            this.mThread.cancel();
            this.mThread = null;
        }
        doVibratorOff();
        reportFinishVibrationLocked();
    }

    public void onVibrationFinished() {
        synchronized (this.mLock) {
            doCancelVibrateLocked();
        }
    }

    private int startVibrationLocked(Vibration vib) {
        Flog.i(NativeResponseCode.SERVICE_REGISTERED, "startVibrationLocked vib.getMode:" + vib.getMode());
        if (!isAllowedToVibrate(vib)) {
            return -2;
        }
        if (this.mCust != null && (this.mCust.allowVibrateWhenSlient(this.mContext, vib.mOpPkg) ^ 1) != 0) {
            return -2;
        }
        int mode = getAppOpMode(vib);
        Flog.i(NativeResponseCode.SERVICE_REGISTERED, "startVibrationLocked mode:" + mode);
        if (mode != 0) {
            if (mode == 2) {
                Slog.w(TAG, "Would be an error: vibrate from uid " + vib.mUid);
            }
            return -2;
        } else if (vib.getMode() != 0) {
            return doVibratorHwOn(vib.getMode());
        } else {
            startVibrationInnerLocked(vib);
            return -2;
        }
    }

    private void startVibrationInnerLocked(Vibration vib) {
        this.mCurrentVibration = vib;
        if (vib.mEffect instanceof OneShot) {
            OneShot oneShot = (OneShot) vib.mEffect;
            doVibratorOn(oneShot.getTiming(), oneShot.getAmplitude(), vib.mUid, vib.mUsageHint);
            this.mH.postDelayed(this.mVibrationEndRunnable, oneShot.getTiming());
        } else if (vib.mEffect instanceof Waveform) {
            this.mThread = new VibrateThread((Waveform) vib.mEffect, vib.mUid, vib.mUsageHint);
            this.mThread.start();
        } else if (vib.mEffect instanceof Prebaked) {
            long timeout = doVibratorPrebakedEffectLocked(vib);
            if (timeout > 0) {
                this.mH.postDelayed(this.mVibrationEndRunnable, timeout);
            }
        } else {
            Slog.e(TAG, "Unknown vibration type, ignoring");
        }
    }

    private boolean isAllowedToVibrate(Vibration vib) {
        if (!this.mLowPowerMode) {
            return true;
        }
        if ((this.mCust != null && this.mCust.isAllowLowPowerPkg(vib.mOpPkg)) || vib.mUsageHint == 6) {
            return true;
        }
        if (this.mAllowPriorityVibrationsInLowPowerMode) {
            return vib.mUsageHint == 4 || vib.mUsageHint == 11 || vib.mUsageHint == 7;
        } else {
            return false;
        }
    }

    private int doVibratorHwOn(int mode) {
        int vibratorWrite;
        synchronized (this.mInputDeviceVibrators) {
            vibratorWrite = vibratorWrite(mode);
        }
        return vibratorWrite;
    }

    private int getAppOpMode(Vibration vib) {
        try {
            int mode = this.mAppOpsService.checkAudioOperation(3, vib.mUsageHint, vib.mUid, vib.mOpPkg);
            if (mode == 0) {
                return this.mAppOpsService.startOperation(AppOpsManager.getToken(this.mAppOpsService), 3, vib.mUid, vib.mOpPkg);
            }
            return mode;
        } catch (RemoteException e) {
            Slog.e(TAG, "Failed to get appop mode for vibration!", e);
            return 1;
        }
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

    private void unlinkVibration(Vibration vib) {
        if (vib.mEffect instanceof Waveform) {
            vib.mToken.unlinkToDeath(vib, 0);
        }
    }

    private void updateVibrators() {
        synchronized (this.mLock) {
            boolean devicesUpdated = updateInputDeviceVibratorsLocked();
            boolean lowPowerModeUpdated = updateLowPowerModeLocked();
            if (devicesUpdated || lowPowerModeUpdated) {
                doCancelVibrateLocked();
            }
        }
    }

    private boolean updateInputDeviceVibratorsLocked() {
        boolean changed = false;
        boolean vibrateInputDevices = false;
        try {
            vibrateInputDevices = System.getIntForUser(this.mContext.getContentResolver(), "vibrate_input_devices", -2) > 0;
        } catch (SettingNotFoundException e) {
        }
        if (vibrateInputDevices != this.mVibrateInputDevicesSetting) {
            changed = true;
            this.mVibrateInputDevicesSetting = vibrateInputDevices;
        }
        if (this.mVibrateInputDevicesSetting) {
            if (!this.mInputDeviceListenerRegistered) {
                this.mInputDeviceListenerRegistered = true;
                this.mIm.registerInputDeviceListener(this, this.mH);
            }
        } else if (this.mInputDeviceListenerRegistered) {
            this.mInputDeviceListenerRegistered = false;
            this.mIm.unregisterInputDeviceListener(this);
        }
        this.mInputDeviceVibrators.clear();
        if (!this.mVibrateInputDevicesSetting) {
            return changed;
        }
        int[] ids = this.mIm.getInputDeviceIds();
        for (int inputDevice : ids) {
            InputDevice device = this.mIm.getInputDevice(inputDevice);
            if (device != null) {
                Vibrator vibrator = device.getVibrator();
                if (vibrator.hasVibrator()) {
                    this.mInputDeviceVibrators.add(vibrator);
                }
            }
        }
        return true;
    }

    private boolean updateLowPowerModeLocked() {
        boolean lowPowerMode = this.mPowerManagerInternal.getLowPowerState(2).batterySaverEnabled;
        if (lowPowerMode == this.mLowPowerMode) {
            return false;
        }
        this.mLowPowerMode = lowPowerMode;
        return true;
    }

    public void onInputDeviceAdded(int deviceId) {
        updateVibrators();
    }

    public void onInputDeviceChanged(int deviceId) {
        updateVibrators();
    }

    public void onInputDeviceRemoved(int deviceId) {
        updateVibrators();
    }

    private boolean doVibratorExists() {
        return vibratorExists();
    }

    private void doVibratorOn(long millis, int amplitude, int uid, int usageHint) {
        synchronized (this.mInputDeviceVibrators) {
            Flog.i(NativeResponseCode.SERVICE_REGISTERED, "Turning vibrator on for " + millis + " ms.");
            if (amplitude == -1) {
                amplitude = this.mDefaultVibrationAmplitude;
            }
            noteVibratorOnLocked(uid, millis);
            int vibratorCount = this.mInputDeviceVibrators.size();
            if (vibratorCount != 0) {
                AudioAttributes attributes = new Builder().setUsage(usageHint).build();
                for (int i = 0; i < vibratorCount; i++) {
                    ((Vibrator) this.mInputDeviceVibrators.get(i)).vibrate(millis, attributes);
                }
            } else {
                vibratorOn(millis);
                doVibratorSetAmplitude(amplitude);
            }
        }
    }

    private void doVibratorSetAmplitude(int amplitude) {
        if (this.mSupportsAmplitudeControl) {
            vibratorSetAmplitude(amplitude);
        }
    }

    private void doVibratorOff() {
        synchronized (this.mInputDeviceVibrators) {
            noteVibratorOffLocked();
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

    private long doVibratorPrebakedEffectLocked(Vibration vib) {
        synchronized (this.mInputDeviceVibrators) {
            Prebaked prebaked = (Prebaked) vib.mEffect;
            if (this.mInputDeviceVibrators.size() == 0) {
                long timeout = vibratorPerformEffect((long) prebaked.getId(), 1);
                if (timeout > 0) {
                    noteVibratorOnLocked(vib.mUid, timeout);
                    return timeout;
                }
            }
            int id = prebaked.getId();
            if (id >= 0 && id < this.mFallbackEffects.length) {
                if (this.mFallbackEffects[id] != null) {
                    startVibrationInnerLocked(new Vibration(this, vib.mToken, this.mFallbackEffects[id], vib.mUsageHint, vib.mUid, vib.mOpPkg, null));
                    return 0;
                }
            }
            Slog.w(TAG, "Failed to play prebaked effect, no fallback");
            return 0;
        }
    }

    private void noteVibratorOnLocked(int uid, long millis) {
        try {
            this.mBatteryStatsService.noteVibratorOn(uid, millis);
            this.mCurVibUid = uid;
        } catch (RemoteException e) {
        }
    }

    private void noteVibratorOffLocked() {
        if (this.mCurVibUid >= 0) {
            try {
                this.mBatteryStatsService.noteVibratorOff(this.mCurVibUid);
            } catch (RemoteException e) {
            }
            this.mCurVibUid = -1;
        }
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, pw)) {
            pw.println("Previous vibrations:");
            synchronized (this.mLock) {
                for (VibrationInfo info : this.mPreviousVibrations) {
                    pw.print("  ");
                    pw.println(info.toString());
                }
            }
        }
    }

    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) throws RemoteException {
        new VibratorShellCommand(this, this, null).exec(this, in, out, err, args, callback, resultReceiver);
    }
}
