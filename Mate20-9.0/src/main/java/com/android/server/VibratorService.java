package com.android.server;

import android.app.AppOpsManager;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hardware.input.InputManager;
import android.icu.text.DateFormat;
import android.media.AudioAttributes;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.IVibratorService;
import android.os.PowerManager;
import android.os.PowerManagerInternal;
import android.os.PowerSaveState;
import android.os.Process;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.ShellCallback;
import android.os.ShellCommand;
import android.os.SystemClock;
import android.os.Trace;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.WorkSource;
import android.provider.Settings;
import android.util.DebugUtils;
import android.util.Flog;
import android.util.Slog;
import android.util.SparseArray;
import android.view.InputDevice;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.app.IBatteryStats;
import com.android.internal.util.DumpUtils;
import com.android.server.NsdService;
import com.android.server.job.controllers.JobStatus;
import com.huawei.android.os.IHwVibrator;
import huawei.android.security.IHwBehaviorCollectManager;
import huawei.cust.HwCustUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

public class VibratorService extends IVibratorService.Stub implements InputManager.InputDeviceListener {
    private static final boolean DEBUG = false;
    private static final int DEFAULT_CODE = -2;
    private static final long DEFAULT_VIBRATE_TIME = 60;
    /* access modifiers changed from: private */
    public static final long[] DEFAULT_VIBRATE_WAVEFORM = {60};
    private static final long[] DOUBLE_CLICK_EFFECT_FALLBACK_TIMINGS = {0, 30, 100, 30};
    private static final int EXCEPTION_CODE = -1;
    private static final int HWVibrator_SUPPORT = 0;
    private static final long MAX_HAPTIC_FEEDBACK_DURATION = 5000;
    private static final int MAX_HWVIBRATE_TIME = 400;
    private static final int SCALE_HIGH = 1;
    private static final float SCALE_HIGH_GAMMA = 0.5f;
    private static final int SCALE_LOW = -1;
    private static final float SCALE_LOW_GAMMA = 1.5f;
    private static final int SCALE_LOW_MAX_AMPLITUDE = 192;
    private static final int SCALE_NONE = 0;
    private static final float SCALE_NONE_GAMMA = 1.0f;
    private static final int SCALE_VERY_HIGH = 2;
    private static final float SCALE_VERY_HIGH_GAMMA = 0.25f;
    private static final int SCALE_VERY_LOW = -2;
    private static final float SCALE_VERY_LOW_GAMMA = 2.0f;
    private static final int SCALE_VERY_LOW_MAX_AMPLITUDE = 168;
    private static final String SYSTEM_UI_PACKAGE = "com.android.systemui";
    private static final String TAG = "VibratorService";
    private final boolean mAllowPriorityVibrationsInLowPowerMode;
    private final AppOpsManager mAppOps;
    private final IBatteryStats mBatteryStatsService;
    /* access modifiers changed from: private */
    public final Context mContext;
    private int mCurVibUid = -1;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public Vibration mCurrentVibration;
    /* access modifiers changed from: private */
    public HwCustCbsUtils mCust;
    private final int mDefaultVibrationAmplitude;
    private final SparseArray<VibrationEffect> mFallbackEffects;
    private final Handler mH = new Handler();
    private int mHapticFeedbackIntensity;
    HwInnerVibratorService mHwInnerService = new HwInnerVibratorService(this);
    private InputManager mIm;
    private boolean mInputDeviceListenerRegistered;
    private final ArrayList<Vibrator> mInputDeviceVibrators = new ArrayList<>();
    BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
                synchronized (VibratorService.this.mLock) {
                    if (VibratorService.this.mCurrentVibration != null && ((!VibratorService.this.mCurrentVibration.isHapticFeedback() || !VibratorService.this.mCurrentVibration.isFromSystem()) && (VibratorService.this.mCust == null || !VibratorService.this.mCust.isNotAllowPkg(VibratorService.this.mCurrentVibration.opPkg)))) {
                        VibratorService.this.doCancelVibrateLocked();
                    }
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private boolean mLowPowerMode;
    private int mNotificationIntensity;
    private PowerManagerInternal mPowerManagerInternal;
    private final LinkedList<VibrationInfo> mPreviousVibrations;
    private final int mPreviousVibrationsLimit;
    private final SparseArray<ScaleLevel> mScaleLevels;
    private SettingsObserver mSettingObserver;
    private final boolean mSupportsAmplitudeControl;
    /* access modifiers changed from: private */
    public volatile VibrateThread mThread;
    /* access modifiers changed from: private */
    public final WorkSource mTmpWorkSource = new WorkSource();
    private boolean mVibrateInputDevicesSetting;
    private final Runnable mVibrationEndRunnable = new Runnable() {
        public void run() {
            VibratorService.this.onVibrationFinished();
        }
    };
    private Vibrator mVibrator;
    /* access modifiers changed from: private */
    public final PowerManager.WakeLock mWakeLock;

    public class HwInnerVibratorService extends IHwVibrator.Stub {
        private static final String TAG = "HwInnerVibratorService";
        VibratorService service;

        HwInnerVibratorService(VibratorService vs) {
            this.service = vs;
        }

        public boolean isSupportHwVibrator(String type) {
            VibratorService vibratorService = this.service;
            int isSupport = VibratorService.checkHwVibrator(type);
            Slog.i(TAG, "isSupportHwVibrator type:" + type + ", isSupport:" + isSupport);
            if (isSupport == 0) {
                return true;
            }
            return false;
        }

        public void setHwVibrator(int uid, String opPkg, IBinder token, String type) {
            int i = uid;
            String str = type;
            Trace.traceBegin(8388608, "setHwVibrator");
            if (str == null) {
                try {
                    Slog.i(TAG, "setHwVibrator type is null");
                } finally {
                    Trace.traceEnd(8388608);
                }
            } else if (token == null) {
                Slog.e(TAG, "token must not be null");
                Trace.traceEnd(8388608);
            } else {
                if (!str.startsWith("haptic.control.")) {
                    VibratorService.this.mContext.enforceCallingOrSelfPermission("android.permission.VIBRATE", "setHwVibrator");
                }
                VibratorService.this.verifyIncomingUid(i);
                synchronized (VibratorService.this.mLock) {
                    if (VibratorService.this.mCurrentVibration != null && (VibratorService.this.mCurrentVibration.effect instanceof VibrationEffect.OneShot) && VibratorService.this.mCurrentVibration.hasTimeoutLongerThan(400)) {
                        Slog.d(TAG, "Ignoring incoming vibration in favor of current vibration");
                        Trace.traceEnd(8388608);
                    } else if (VibratorService.this.mCurrentVibration == null || !VibratorService.isRepeatingVibration(VibratorService.this.mCurrentVibration.effect)) {
                        int pid = Binder.getCallingPid();
                        Slog.i(TAG, "setHwVibrator on: uid is " + i + ",pid is " + pid + ",packagename = " + opPkg + ",type is " + str);
                        int i2 = pid;
                        Vibration vibration = new Vibration(token, VibrationEffect.createWaveform(VibratorService.DEFAULT_VIBRATE_WAVEFORM, -1), 0, i, opPkg);
                        Vibration vib = vibration;
                        vib.setType(str);
                        VibratorService.this.linkVibration(vib);
                        long ident = Binder.clearCallingIdentity();
                        try {
                            if (VibratorService.this.mCurrentVibration != null) {
                                boolean unused = VibratorService.this.mCurrentVibration.needCancelHwVibrator = false;
                            }
                            VibratorService.this.doCancelVibrateLocked();
                            Vibration unused2 = VibratorService.this.mCurrentVibration = vib;
                            int unused3 = VibratorService.this.startVibrationLocked(vib);
                            VibratorService.this.addToPreviousVibrationsLocked(vib);
                            Trace.traceEnd(8388608);
                        } finally {
                            Binder.restoreCallingIdentity(ident);
                        }
                    } else {
                        Slog.d(TAG, "Ignoring incoming vibration in favor of alarm vibration");
                        Trace.traceEnd(8388608);
                    }
                }
            }
        }

        /* JADX INFO: finally extract failed */
        public void stopHwVibrator(int uid, String opPkg, IBinder token, String type) {
            if (type == null) {
                Slog.i(TAG, "stopHwVibrator type is null");
                return;
            }
            if (!type.startsWith("haptic.control.")) {
                VibratorService.this.mContext.enforceCallingOrSelfPermission("android.permission.VIBRATE", "stopHwVibrator");
            }
            if (type == null) {
                Slog.i(TAG, "stopHwVibrator type is null");
                return;
            }
            synchronized (VibratorService.this.mLock) {
                if (VibratorService.this.mCurrentVibration != null && VibratorService.this.mCurrentVibration.token == token && VibratorService.this.mCurrentVibration.getType() != null && VibratorService.this.mCurrentVibration.getType().equals(type)) {
                    int pid = Binder.getCallingPid();
                    long ident = Binder.clearCallingIdentity();
                    try {
                        Slog.i(TAG, "stopHwVibrator on: uid is " + uid + ",pid is " + pid + ",packagename = " + opPkg + ",type is " + type);
                        boolean unused = VibratorService.this.mCurrentVibration.needCancelHwVibrator = true;
                        VibratorService.this.doCancelVibrateLocked();
                        Binder.restoreCallingIdentity(ident);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(ident);
                        throw th;
                    }
                }
            }
        }

        public void setHwParameter(String command) {
            VibratorService vibratorService = this.service;
            VibratorService.setParameter(command);
        }

        public String getHwParameter(String command) {
            VibratorService vibratorService = this.service;
            return VibratorService.getParameter(command);
        }
    }

    private static final class ScaleLevel {
        public final float gamma;
        public final int maxAmplitude;

        public ScaleLevel(float gamma2) {
            this(gamma2, 255);
        }

        public ScaleLevel(float gamma2, int maxAmplitude2) {
            this.gamma = gamma2;
            this.maxAmplitude = maxAmplitude2;
        }

        public String toString() {
            return "ScaleLevel{gamma=" + this.gamma + ", maxAmplitude=" + this.maxAmplitude + "}";
        }
    }

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
        private final VibrationEffect.Waveform mWaveform;

        VibrateThread(VibrationEffect.Waveform waveform, int uid, int usageHint) {
            this.mWaveform = waveform;
            this.mUid = uid;
            this.mUsageHint = usageHint;
            VibratorService.this.mTmpWorkSource.set(uid);
            VibratorService.this.mWakeLock.setWorkSource(VibratorService.this.mTmpWorkSource);
        }

        private long delayLocked(long duration) {
            Trace.traceBegin(8388608, "delayLocked");
            long durationRemaining = duration;
            if (duration > 0) {
                try {
                    long bedtime = SystemClock.uptimeMillis() + duration;
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
                } finally {
                    Trace.traceEnd(8388608);
                }
            } else {
                Trace.traceEnd(8388608);
                return 0;
            }
        }

        public void run() {
            Process.setThreadPriority(-8);
            VibratorService.this.mWakeLock.acquire();
            try {
                if (playWaveform()) {
                    VibratorService.this.onVibrationFinished();
                }
            } finally {
                VibratorService.this.mWakeLock.release();
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:20:0x0064 A[Catch:{ all -> 0x007c }] */
        public boolean playWaveform() {
            boolean z;
            long onDuration;
            long duration;
            Trace.traceBegin(8388608, "playWaveform");
            try {
                synchronized (this) {
                    long[] timings = this.mWaveform.getTimings();
                    int[] amplitudes = this.mWaveform.getAmplitudes();
                    int len = timings.length;
                    int repeat = this.mWaveform.getRepeatIndex();
                    int index = 0;
                    long j = 0;
                    long onDuration2 = 0;
                    while (true) {
                        if (this.mForceStop) {
                            break;
                        }
                        if (index < len) {
                            int amplitude = amplitudes[index];
                            int index2 = index + 1;
                            long duration2 = timings[index];
                            if (duration2 <= j) {
                                index = index2;
                            } else {
                                if (amplitude == 0) {
                                    duration = duration2;
                                } else if (onDuration2 <= j) {
                                    duration = duration2;
                                    onDuration = getTotalOnDuration(timings, amplitudes, index2 - 1, repeat);
                                    VibratorService.this.doVibratorOn(onDuration, amplitude, this.mUid, this.mUsageHint);
                                    long waitTime = delayLocked(duration);
                                    if (amplitude != 0) {
                                        onDuration -= waitTime;
                                    }
                                    onDuration2 = onDuration;
                                    index = index2;
                                } else {
                                    duration = duration2;
                                    VibratorService.this.doVibratorSetAmplitude(amplitude);
                                }
                                onDuration = onDuration2;
                                long waitTime2 = delayLocked(duration);
                                if (amplitude != 0) {
                                }
                                onDuration2 = onDuration;
                                index = index2;
                            }
                        } else if (repeat < 0) {
                            break;
                        } else {
                            index = repeat;
                        }
                        j = 0;
                    }
                    z = !this.mForceStop;
                }
                Trace.traceEnd(8388608);
                return z;
            } catch (Throwable th) {
                Trace.traceEnd(8388608);
                throw th;
            }
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
            do {
                if (amplitudes[i] != 0) {
                    int i2 = i + 1;
                    timing += timings[i];
                    if (i2 < timings.length) {
                        i = i2;
                        continue;
                    } else if (repeatIndex >= 0) {
                        i = repeatIndex;
                        continue;
                    }
                }
                return timing;
            } while (i != startIndex);
            return 1000;
        }
    }

    private class Vibration implements IBinder.DeathRecipient {
        public VibrationEffect effect;
        private int mode;
        /* access modifiers changed from: private */
        public boolean needCancelHwVibrator;
        public final String opPkg;
        public VibrationEffect originalEffect;
        public final long startTime;
        public final long startTimeDebug;
        public final IBinder token;
        private String type;
        public final int uid;
        public final int usageHint;

        public void setMode(int m) {
            this.mode = m;
        }

        public int getMode() {
            return this.mode;
        }

        public void setType(String t) {
            this.type = t;
        }

        public String getType() {
            return this.type;
        }

        private Vibration(IBinder token2, VibrationEffect effect2, int usageHint2, int uid2, String opPkg2) {
            this.mode = 0;
            this.needCancelHwVibrator = true;
            this.type = null;
            this.token = token2;
            this.effect = effect2;
            this.startTime = SystemClock.elapsedRealtime();
            this.startTimeDebug = System.currentTimeMillis();
            this.usageHint = usageHint2;
            this.uid = uid2;
            this.opPkg = opPkg2;
        }

        public void binderDied() {
            synchronized (VibratorService.this.mLock) {
                if (this == VibratorService.this.mCurrentVibration) {
                    VibratorService.this.doCancelVibrateLocked();
                }
            }
        }

        public boolean hasTimeoutLongerThan(long millis) {
            long duration = this.effect.getDuration();
            return duration >= 0 && duration > millis;
        }

        public boolean isHapticFeedback() {
            boolean z = true;
            if (this.effect instanceof VibrationEffect.Prebaked) {
                switch (this.effect.getId()) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                        return true;
                    default:
                        Slog.w(VibratorService.TAG, "Unknown prebaked vibration effect, assuming it isn't haptic feedback.");
                        return false;
                }
            } else {
                long duration = this.effect.getDuration();
                if (duration < 0 || duration >= VibratorService.MAX_HAPTIC_FEEDBACK_DURATION) {
                    z = false;
                }
                return z;
            }
        }

        public boolean isNotification() {
            int i = this.usageHint;
            if (i != 5) {
                switch (i) {
                    case 7:
                    case 8:
                    case 9:
                        break;
                    default:
                        return false;
                }
            }
            return true;
        }

        public boolean isRingtone() {
            return this.usageHint == 6;
        }

        public boolean isFromSystem() {
            return this.uid == 1000 || this.uid == 0 || VibratorService.SYSTEM_UI_PACKAGE.equals(this.opPkg);
        }

        public VibrationInfo toInfo() {
            VibrationInfo vibrationInfo = new VibrationInfo(this.startTimeDebug, this.effect, this.originalEffect, this.usageHint, this.uid, this.opPkg, this.type);
            return vibrationInfo;
        }
    }

    private static class VibrationInfo {
        private final VibrationEffect mEffect;
        private final String mOpPkg;
        private final VibrationEffect mOriginalEffect;
        private final long mStartTimeDebug;
        private final String mType;
        private final int mUid;
        private final int mUsageHint;

        public VibrationInfo(long startTimeDebug, VibrationEffect effect, VibrationEffect originalEffect, int usageHint, int uid, String opPkg, String type) {
            this.mStartTimeDebug = startTimeDebug;
            this.mEffect = effect;
            this.mOriginalEffect = originalEffect;
            this.mUsageHint = usageHint;
            this.mUid = uid;
            this.mOpPkg = opPkg;
            this.mType = type;
        }

        public String toString() {
            return "startTime: " + DateFormat.getDateTimeInstance().format(new Date(this.mStartTimeDebug)) + ", effect: " + this.mEffect + ", originalEffect: " + this.mOriginalEffect + ", usageHint: " + this.mUsageHint + ", uid: " + this.mUid + ", opPkg: " + this.mOpPkg + ", type: " + this.mType;
        }
    }

    private final class VibratorShellCommand extends ShellCommand {
        private static final long MAX_VIBRATION_MS = 200;
        private final IBinder mToken;

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
            PrintWriter pw;
            Trace.traceBegin(8388608, "runVibrate");
            try {
                int zenMode = Settings.Global.getInt(VibratorService.this.mContext.getContentResolver(), "zen_mode");
                if (zenMode != 0) {
                    pw = getOutPrintWriter();
                    pw.print("Ignoring because device is on DND mode ");
                    pw.println(DebugUtils.flagsToString(Settings.Global.class, "ZEN_MODE_", zenMode));
                    if (pw != null) {
                        $closeResource(null, pw);
                    }
                    Trace.traceEnd(8388608);
                    return 0;
                }
            } catch (Settings.SettingNotFoundException e) {
            } catch (Throwable th) {
                if (pw != null) {
                    $closeResource(r5, pw);
                }
                throw th;
            }
            try {
                long duration = Long.parseLong(getNextArgRequired());
                if (duration <= MAX_VIBRATION_MS) {
                    String description = getNextArg();
                    if (description == null) {
                        description = "Shell command";
                    }
                    String str = description;
                    VibratorService.this.vibrate(Binder.getCallingUid(), str, VibrationEffect.createOneShot(duration, -1), 0, this.mToken);
                    return 0;
                }
                throw new IllegalArgumentException("maximum duration is 200");
            } finally {
                Trace.traceEnd(8388608);
            }
        }

        private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
            if (x0 != null) {
                try {
                    x1.close();
                } catch (Throwable th) {
                    x0.addSuppressed(th);
                }
            } else {
                x1.close();
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:10:0x0039, code lost:
            $closeResource(r1, r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x003c, code lost:
            throw r2;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:5:0x0033, code lost:
            r2 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x0037, code lost:
            if (r0 != null) goto L_0x0039;
         */
        public void onHelp() {
            PrintWriter pw = getOutPrintWriter();
            pw.println("Vibrator commands:");
            pw.println("  help");
            pw.println("    Prints this help text.");
            pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
            pw.println("  vibrate duration [description]");
            pw.println("    Vibrates for duration milliseconds; ignored when device is on DND ");
            pw.println("    (Do Not Disturb) mode.");
            pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
            if (pw != null) {
                $closeResource(null, pw);
            }
        }
    }

    static native int checkHwVibrator(String str);

    static native String getParameter(String str);

    static native int setHwVibrator(String str);

    static native int setParameter(String str);

    static native int stopHwVibrator(String str);

    static native boolean vibratorExists();

    static native void vibratorInit();

    static native void vibratorOff();

    static native void vibratorOn(long j);

    static native long vibratorPerformEffect(long j, long j2);

    static native void vibratorSetAmplitude(int i);

    static native boolean vibratorSupportsAmplitudeControl();

    static native int vibratorWrite(int i);

    VibratorService(Context context) {
        this.mCust = (HwCustCbsUtils) HwCustUtils.createObj(HwCustCbsUtils.class, new Object[]{context});
        vibratorInit();
        vibratorOff();
        this.mSupportsAmplitudeControl = vibratorSupportsAmplitudeControl();
        this.mContext = context;
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, "*vibrator*");
        this.mWakeLock.setReferenceCounted(true);
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService(AppOpsManager.class);
        this.mBatteryStatsService = IBatteryStats.Stub.asInterface(ServiceManager.getService("batterystats"));
        this.mPreviousVibrationsLimit = this.mContext.getResources().getInteger(17694850);
        this.mDefaultVibrationAmplitude = this.mContext.getResources().getInteger(17694774);
        this.mAllowPriorityVibrationsInLowPowerMode = this.mContext.getResources().getBoolean(17956875);
        this.mPreviousVibrations = new LinkedList<>();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_OFF");
        context.registerReceiver(this.mIntentReceiver, filter);
        VibrationEffect clickEffect = createEffectFromResource(17236051);
        VibrationEffect doubleClickEffect = VibrationEffect.createWaveform(DOUBLE_CLICK_EFFECT_FALLBACK_TIMINGS, -1);
        VibrationEffect heavyClickEffect = createEffectFromResource(17236015);
        VibrationEffect tickEffect = createEffectFromResource(17235996);
        this.mFallbackEffects = new SparseArray<>();
        this.mFallbackEffects.put(0, clickEffect);
        this.mFallbackEffects.put(1, doubleClickEffect);
        this.mFallbackEffects.put(2, tickEffect);
        this.mFallbackEffects.put(5, heavyClickEffect);
        this.mScaleLevels = new SparseArray<>();
        this.mScaleLevels.put(-2, new ScaleLevel(SCALE_VERY_LOW_GAMMA, SCALE_VERY_LOW_MAX_AMPLITUDE));
        this.mScaleLevels.put(-1, new ScaleLevel(SCALE_LOW_GAMMA, SCALE_LOW_MAX_AMPLITUDE));
        this.mScaleLevels.put(0, new ScaleLevel(1.0f));
        this.mScaleLevels.put(1, new ScaleLevel(0.5f));
        this.mScaleLevels.put(2, new ScaleLevel(SCALE_VERY_HIGH_GAMMA));
    }

    private VibrationEffect createEffectFromResource(int resId) {
        return createEffectFromTimings(getLongIntArray(this.mContext.getResources(), resId));
    }

    private static VibrationEffect createEffectFromTimings(long[] timings) {
        if (timings == null || timings.length == 0) {
            return null;
        }
        if (timings.length == 1) {
            return VibrationEffect.createOneShot(timings[0], -1);
        }
        return VibrationEffect.createWaveform(timings, -1);
    }

    public void systemReady() {
        Trace.traceBegin(8388608, "VibratorService#systemReady");
        try {
            this.mIm = (InputManager) this.mContext.getSystemService(InputManager.class);
            this.mVibrator = (Vibrator) this.mContext.getSystemService(Vibrator.class);
            this.mSettingObserver = new SettingsObserver(this.mH);
            this.mPowerManagerInternal = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
            this.mPowerManagerInternal.registerLowPowerModeObserver(new PowerManagerInternal.LowPowerModeListener() {
                public int getServiceType() {
                    return 2;
                }

                public void onLowPowerModeChanged(PowerSaveState result) {
                    VibratorService.this.updateVibrators();
                }
            });
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("vibrate_input_devices"), true, this.mSettingObserver, -1);
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("haptic_feedback_intensity"), true, this.mSettingObserver, -1);
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("notification_vibration_intensity"), true, this.mSettingObserver, -1);
            this.mContext.registerReceiver(new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    VibratorService.this.updateVibrators();
                }
            }, new IntentFilter("android.intent.action.USER_SWITCHED"), null, this.mH);
            updateVibrators();
        } finally {
            Trace.traceEnd(8388608);
        }
    }

    public boolean hasVibrator() {
        return doVibratorExists();
    }

    public boolean hasAmplitudeControl() {
        boolean z;
        synchronized (this.mInputDeviceVibrators) {
            z = this.mSupportsAmplitudeControl && this.mInputDeviceVibrators.isEmpty();
        }
        return z;
    }

    /* access modifiers changed from: private */
    public void verifyIncomingUid(int uid) {
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
        String str = opPkg;
        Trace.traceBegin(8388608, "hwVibrate");
        try {
            if ("com.huawei.android.launcher".equals(str)) {
                if (this.mContext.checkCallingOrSelfPermission("android.permission.VIBRATE") != 0) {
                    throw new SecurityException("Requires VIBRATE permission");
                }
            }
            verifyIncomingUid(uid);
            Slog.d(TAG, "hw Vibrating for  opPkg " + str);
            Vibration vibration = new Vibration(token, VibrationEffect.createOneShot(DEFAULT_VIBRATE_TIME, -1), usageHint, uid, str);
            Vibration vib = vibration;
            try {
                vib.setMode(mode);
                linkVibration(vib);
                long ident = Binder.clearCallingIdentity();
                try {
                    synchronized (this.mLock) {
                        doCancelVibrateLocked();
                        this.mCurrentVibration = vib;
                        if (-1 == startVibrationLocked(vib)) {
                            vib.setMode(0);
                            startVibrationLocked(vib);
                        }
                        addToPreviousVibrationsLocked(vib);
                    }
                    Binder.restoreCallingIdentity(ident);
                    Trace.traceEnd(8388608);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(ident);
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                Trace.traceEnd(8388608);
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            int i = mode;
            Trace.traceEnd(8388608);
            throw th;
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
        long ident;
        VibrationEffect vibrationEffect = effect;
        Trace.traceBegin(8388608, "vibrate");
        HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.VIBRATOR_VIBRATE);
        try {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.VIBRATE") != 0) {
                int i = uid;
                String str = opPkg;
                throw new SecurityException("Requires VIBRATE permission");
            } else if (token == null) {
                Slog.e(TAG, "token must not be null");
                Trace.traceEnd(8388608);
            } else {
                verifyIncomingUid(uid);
                if (!verifyVibrationEffect(effect)) {
                    Trace.traceEnd(8388608);
                    return;
                }
                synchronized (this.mLock) {
                    if ((vibrationEffect instanceof VibrationEffect.OneShot) && this.mCurrentVibration != null && (this.mCurrentVibration.effect instanceof VibrationEffect.OneShot)) {
                        VibrationEffect.OneShot newOneShot = (VibrationEffect.OneShot) vibrationEffect;
                        VibrationEffect.OneShot currentOneShot = this.mCurrentVibration.effect;
                        if (this.mCurrentVibration.hasTimeoutLongerThan(newOneShot.getDuration()) && newOneShot.getAmplitude() == currentOneShot.getAmplitude()) {
                            Trace.traceEnd(8388608);
                            return;
                        }
                    }
                    try {
                        if (isRepeatingVibration(effect) || this.mCurrentVibration == null || !isRepeatingVibration(this.mCurrentVibration.effect)) {
                            Vibration vibration = new Vibration(token, vibrationEffect, usageHint, uid, opPkg);
                            Vibration vib = vibration;
                            int pid = Binder.getCallingPid();
                            StringBuilder sb = new StringBuilder();
                            sb.append("vibrate on: uid is ");
                            try {
                                sb.append(uid);
                                sb.append(",pid is ");
                                sb.append(pid);
                                sb.append(",packagename = ");
                            } catch (Throwable th) {
                                th = th;
                                String str2 = opPkg;
                                try {
                                    throw th;
                                } catch (Throwable th2) {
                                    th = th2;
                                    Trace.traceEnd(8388608);
                                    throw th;
                                }
                            }
                            try {
                                sb.append(opPkg);
                                Flog.i(NsdService.NativeResponseCode.SERVICE_REGISTERED, sb.toString());
                                linkVibration(vib);
                                ident = Binder.clearCallingIdentity();
                                doCancelVibrateLocked();
                                startVibrationLocked(vib);
                                addToPreviousVibrationsLocked(vib);
                                Binder.restoreCallingIdentity(ident);
                                Trace.traceEnd(8388608);
                            } catch (Throwable th3) {
                                th = th3;
                                throw th;
                            }
                        } else {
                            Trace.traceEnd(8388608);
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        int i2 = uid;
                        String str22 = opPkg;
                        throw th;
                    }
                }
            }
        } catch (Throwable th5) {
            th = th5;
            int i3 = uid;
            String str3 = opPkg;
            Trace.traceEnd(8388608);
            throw th;
        }
    }

    /* access modifiers changed from: private */
    public static boolean isRepeatingVibration(VibrationEffect effect) {
        return effect.getDuration() == JobStatus.NO_LATEST_RUNTIME;
    }

    /* access modifiers changed from: private */
    public void addToPreviousVibrationsLocked(Vibration vib) {
        if (this.mPreviousVibrations.size() > this.mPreviousVibrationsLimit) {
            this.mPreviousVibrations.removeFirst();
        }
        this.mPreviousVibrations.addLast(vib.toInfo());
    }

    /* JADX INFO: finally extract failed */
    public void cancelVibrate(IBinder token) {
        HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.VIBRATOR_CANCELVIBRATE);
        this.mContext.enforceCallingOrSelfPermission("android.permission.VIBRATE", "cancelVibrate");
        synchronized (this.mLock) {
            if (this.mCurrentVibration != null && this.mCurrentVibration.token == token) {
                int uid = Binder.getCallingUid();
                int pid = Binder.getCallingPid();
                long ident = Binder.clearCallingIdentity();
                try {
                    Flog.i(NsdService.NativeResponseCode.SERVICE_REGISTERED, "Canceling vibration. UID:" + uid + ", PID:" + pid);
                    doCancelVibrateLocked();
                    Binder.restoreCallingIdentity(ident);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(ident);
                    throw th;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public void doCancelVibrateLocked() {
        Trace.asyncTraceEnd(8388608, "vibration", 0);
        Trace.traceBegin(8388608, "doCancelVibrateLocked");
        try {
            this.mH.removeCallbacks(this.mVibrationEndRunnable);
            if (this.mThread != null) {
                this.mThread.cancel();
                this.mThread = null;
            }
            if (!cancelHwVibrate()) {
                doVibratorOff();
            }
            reportFinishVibrationLocked();
        } finally {
            Trace.traceEnd(8388608);
        }
    }

    public void onVibrationFinished() {
        synchronized (this.mLock) {
            doCancelVibrateLocked();
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public int startVibrationLocked(Vibration vib) {
        Trace.traceBegin(8388608, "startVibrationLocked");
        try {
            Flog.i(NsdService.NativeResponseCode.SERVICE_REGISTERED, "startVibrationLocked vib.getMode:" + vib.getMode());
            if (!isAllowedToVibrateLocked(vib)) {
                return -2;
            }
            int intensity = getCurrentIntensityLocked(vib);
            if (intensity == 0) {
                Trace.traceEnd(8388608);
                return -2;
            } else if (this.mCust == null || this.mCust.allowVibrateWhenSlient(this.mContext, vib.opPkg)) {
                int mode = getAppOpMode(vib);
                Flog.i(NsdService.NativeResponseCode.SERVICE_REGISTERED, "startVibrationLocked mode:" + mode);
                if (mode != 0) {
                    if (mode == 2) {
                        Slog.w(TAG, "Would be an error: vibrate from uid " + vib.uid);
                    }
                    Trace.traceEnd(8388608);
                    return -2;
                } else if (vib.getMode() != 0) {
                    int doVibratorHwOn = doVibratorHwOn(vib.getMode());
                    Trace.traceEnd(8388608);
                    return doVibratorHwOn;
                } else if (vib.getType() != null) {
                    int hwVibrator = setHwVibrator(vib.getType());
                    Trace.traceEnd(8388608);
                    return hwVibrator;
                } else {
                    applyVibrationIntensityScalingLocked(vib, intensity);
                    startVibrationInnerLocked(vib);
                    Trace.traceEnd(8388608);
                    return -2;
                }
            } else {
                Trace.traceEnd(8388608);
                return -2;
            }
        } finally {
            Trace.traceEnd(8388608);
        }
    }

    @GuardedBy("mLock")
    private void startVibrationInnerLocked(Vibration vib) {
        Trace.traceBegin(8388608, "startVibrationInnerLocked");
        try {
            this.mCurrentVibration = vib;
            if (vib.effect instanceof VibrationEffect.OneShot) {
                Trace.asyncTraceBegin(8388608, "vibration", 0);
                VibrationEffect.OneShot oneShot = vib.effect;
                doVibratorOn(oneShot.getDuration(), oneShot.getAmplitude(), vib.uid, vib.usageHint);
                this.mH.postDelayed(this.mVibrationEndRunnable, oneShot.getDuration());
            } else if (vib.effect instanceof VibrationEffect.Waveform) {
                Trace.asyncTraceBegin(8388608, "vibration", 0);
                this.mThread = new VibrateThread(vib.effect, vib.uid, vib.usageHint);
                this.mThread.start();
            } else if (vib.effect instanceof VibrationEffect.Prebaked) {
                Trace.asyncTraceBegin(8388608, "vibration", 0);
                long timeout = doVibratorPrebakedEffectLocked(vib);
                if (timeout > 0) {
                    this.mH.postDelayed(this.mVibrationEndRunnable, timeout);
                }
            } else {
                Slog.e(TAG, "Unknown vibration type, ignoring");
            }
        } finally {
            Trace.traceEnd(8388608);
        }
    }

    private boolean isAllowedToVibrateLocked(Vibration vib) {
        if (!this.mLowPowerMode) {
            return true;
        }
        if ((this.mCust != null && this.mCust.isAllowLowPowerPkg(vib.opPkg)) || vib.usageHint == 6 || vib.usageHint == 4 || vib.usageHint == 11 || vib.usageHint == 7) {
            return true;
        }
        return false;
    }

    private int getCurrentIntensityLocked(Vibration vib) {
        if (vib.isNotification() || vib.isRingtone()) {
            return this.mNotificationIntensity;
        }
        if (vib.isHapticFeedback()) {
            return this.mHapticFeedbackIntensity;
        }
        return 2;
    }

    private void applyVibrationIntensityScalingLocked(Vibration vib, int intensity) {
        int defaultIntensity;
        if (vib.effect instanceof VibrationEffect.Prebaked) {
            vib.effect.setEffectStrength(intensityToEffectStrength(intensity));
            return;
        }
        if (vib.isNotification() || vib.isRingtone()) {
            defaultIntensity = this.mVibrator.getDefaultNotificationVibrationIntensity();
        } else if (vib.isHapticFeedback()) {
            defaultIntensity = this.mVibrator.getDefaultHapticFeedbackIntensity();
        } else {
            return;
        }
        ScaleLevel scale = this.mScaleLevels.get(intensity - defaultIntensity);
        if (scale == null) {
            Slog.e(TAG, "No configured scaling level! (current=" + intensity + ", default= " + defaultIntensity + ")");
            return;
        }
        VibrationEffect scaledEffect = null;
        if (vib.effect instanceof VibrationEffect.OneShot) {
            scaledEffect = vib.effect.resolve(this.mDefaultVibrationAmplitude).scale(scale.gamma, scale.maxAmplitude);
        } else if (vib.effect instanceof VibrationEffect.Waveform) {
            scaledEffect = vib.effect.resolve(this.mDefaultVibrationAmplitude).scale(scale.gamma, scale.maxAmplitude);
        } else {
            Slog.w(TAG, "Unable to apply intensity scaling, unknown VibrationEffect type");
        }
        if (scaledEffect != null) {
            vib.originalEffect = vib.effect;
            vib.effect = scaledEffect;
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
        int mode = this.mAppOps.checkAudioOpNoThrow(3, vib.usageHint, vib.uid, vib.opPkg);
        if (mode == 0) {
            return this.mAppOps.startOpNoThrow(3, vib.uid, vib.opPkg);
        }
        return mode;
    }

    @GuardedBy("mLock")
    private void reportFinishVibrationLocked() {
        Trace.traceBegin(8388608, "reportFinishVibrationLocked");
        try {
            if (this.mCurrentVibration != null) {
                this.mAppOps.finishOp(3, this.mCurrentVibration.uid, this.mCurrentVibration.opPkg);
                unlinkVibration(this.mCurrentVibration);
                this.mCurrentVibration = null;
            }
        } finally {
            Trace.traceEnd(8388608);
        }
    }

    /* access modifiers changed from: private */
    public void linkVibration(Vibration vib) {
        if (vib.effect instanceof VibrationEffect.Waveform) {
            try {
                vib.token.linkToDeath(vib, 0);
            } catch (RemoteException e) {
            }
        }
    }

    private void unlinkVibration(Vibration vib) {
        if (vib.effect instanceof VibrationEffect.Waveform) {
            vib.token.unlinkToDeath(vib, 0);
        }
    }

    /* access modifiers changed from: private */
    public void updateVibrators() {
        synchronized (this.mLock) {
            boolean devicesUpdated = updateInputDeviceVibratorsLocked();
            boolean lowPowerModeUpdated = updateLowPowerModeLocked();
            updateVibrationIntensityLocked();
            if (devicesUpdated || lowPowerModeUpdated) {
                doCancelVibrateLocked();
            }
        }
    }

    private boolean updateInputDeviceVibratorsLocked() {
        boolean changed = false;
        boolean vibrateInputDevices = false;
        try {
            vibrateInputDevices = Settings.System.getIntForUser(this.mContext.getContentResolver(), "vibrate_input_devices", -2) > 0;
        } catch (Settings.SettingNotFoundException e) {
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

    private void updateVibrationIntensityLocked() {
        this.mHapticFeedbackIntensity = Settings.System.getIntForUser(this.mContext.getContentResolver(), "haptic_feedback_intensity", this.mVibrator.getDefaultHapticFeedbackIntensity(), -2);
        this.mNotificationIntensity = Settings.System.getIntForUser(this.mContext.getContentResolver(), "notification_vibration_intensity", this.mVibrator.getDefaultNotificationVibrationIntensity(), -2);
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

    /* access modifiers changed from: private */
    public void doVibratorOn(long millis, int amplitude, int uid, int usageHint) {
        Trace.traceBegin(8388608, "doVibratorOn");
        try {
            synchronized (this.mInputDeviceVibrators) {
                Flog.i(NsdService.NativeResponseCode.SERVICE_REGISTERED, "Turning vibrator on for " + millis + " ms.");
                if (amplitude == -1) {
                    amplitude = this.mDefaultVibrationAmplitude;
                }
                noteVibratorOnLocked(uid, millis);
                int vibratorCount = this.mInputDeviceVibrators.size();
                if (vibratorCount != 0) {
                    AudioAttributes attributes = new AudioAttributes.Builder().setUsage(usageHint).build();
                    for (int i = 0; i < vibratorCount; i++) {
                        this.mInputDeviceVibrators.get(i).vibrate(millis, attributes);
                    }
                } else {
                    vibratorOn(millis);
                    doVibratorSetAmplitude(amplitude);
                }
            }
            Trace.traceEnd(8388608);
        } catch (Throwable th) {
            Trace.traceEnd(8388608);
            throw th;
        }
    }

    /* access modifiers changed from: private */
    public void doVibratorSetAmplitude(int amplitude) {
        if (this.mSupportsAmplitudeControl) {
            vibratorSetAmplitude(amplitude);
        }
    }

    private void doVibratorOff() {
        Trace.traceBegin(8388608, "doVibratorOff");
        try {
            synchronized (this.mInputDeviceVibrators) {
                noteVibratorOffLocked();
                int vibratorCount = this.mInputDeviceVibrators.size();
                if (vibratorCount != 0) {
                    for (int i = 0; i < vibratorCount; i++) {
                        this.mInputDeviceVibrators.get(i).cancel();
                    }
                } else {
                    vibratorOff();
                }
            }
            Trace.traceEnd(8388608);
        } catch (Throwable th) {
            Trace.traceEnd(8388608);
            throw th;
        }
    }

    @GuardedBy("mLock")
    private long doVibratorPrebakedEffectLocked(Vibration vib) {
        boolean usingInputDeviceVibrators;
        Vibration vibration = vib;
        Trace.traceBegin(8388608, "doVibratorPrebakedEffectLocked");
        try {
            VibrationEffect.Prebaked prebaked = vibration.effect;
            synchronized (this.mInputDeviceVibrators) {
                usingInputDeviceVibrators = !this.mInputDeviceVibrators.isEmpty();
            }
            if (!usingInputDeviceVibrators) {
                long timeout = vibratorPerformEffect((long) prebaked.getId(), (long) prebaked.getEffectStrength());
                if (timeout > 0) {
                    noteVibratorOnLocked(vibration.uid, timeout);
                    Trace.traceEnd(8388608);
                    return timeout;
                }
            }
            if (!prebaked.shouldFallback()) {
                Trace.traceEnd(8388608);
                return 0;
            }
            VibrationEffect effect = getFallbackEffect(prebaked.getId());
            if (effect == null) {
                Slog.w(TAG, "Failed to play prebaked effect, no fallback");
                Trace.traceEnd(8388608);
                return 0;
            }
            Vibration vibration2 = new Vibration(vibration.token, effect, vibration.usageHint, vibration.uid, vibration.opPkg);
            Vibration fallbackVib = vibration2;
            int intensity = getCurrentIntensityLocked(fallbackVib);
            linkVibration(fallbackVib);
            applyVibrationIntensityScalingLocked(fallbackVib, intensity);
            startVibrationInnerLocked(fallbackVib);
            Trace.traceEnd(8388608);
            return 0;
        } catch (Throwable th) {
            Trace.traceEnd(8388608);
            throw th;
        }
    }

    private VibrationEffect getFallbackEffect(int effectId) {
        return this.mFallbackEffects.get(effectId);
    }

    private static int intensityToEffectStrength(int intensity) {
        switch (intensity) {
            case 1:
                return 0;
            case 2:
                return 1;
            case 3:
                return 2;
            default:
                Slog.w(TAG, "Got unexpected vibration intensity: " + intensity);
                return 2;
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

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, pw)) {
            pw.println("Vibrator Service:");
            synchronized (this.mLock) {
                pw.print("  mCurrentVibration=");
                if (this.mCurrentVibration != null) {
                    pw.println(this.mCurrentVibration.toInfo().toString());
                } else {
                    pw.println("null");
                }
                pw.println("  mLowPowerMode=" + this.mLowPowerMode);
                pw.println("  mHapticFeedbackIntensity=" + this.mHapticFeedbackIntensity);
                pw.println("  mNotificationIntensity=" + this.mNotificationIntensity);
                pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                pw.println("  Previous vibrations:");
                Iterator it = this.mPreviousVibrations.iterator();
                while (it.hasNext()) {
                    pw.print("    ");
                    pw.println(((VibrationInfo) it.next()).toString());
                }
            }
        }
    }

    /* JADX WARNING: type inference failed for: r8v0, types: [android.os.IBinder, com.android.server.VibratorService] */
    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) throws RemoteException {
        new VibratorShellCommand(this).exec(this, in, out, err, args, callback, resultReceiver);
    }

    /* JADX WARNING: type inference failed for: r0v0, types: [com.android.server.VibratorService$HwInnerVibratorService, android.os.IBinder] */
    public IBinder getHwInnerService() {
        return this.mHwInnerService;
    }

    private boolean cancelHwVibrate() {
        boolean canceled = false;
        if (!(this.mCurrentVibration == null || this.mCurrentVibration.getType() == null)) {
            canceled = true;
            if (this.mCurrentVibration.needCancelHwVibrator) {
                stopHwVibrator(this.mCurrentVibration.getType());
            }
        }
        return canceled;
    }
}
