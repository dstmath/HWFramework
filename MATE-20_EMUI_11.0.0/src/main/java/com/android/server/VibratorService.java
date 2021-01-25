package com.android.server;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.IUidObserver;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hardware.input.InputManager;
import android.icu.text.DateFormat;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.ExternalVibration;
import android.os.Handler;
import android.os.IBinder;
import android.os.IExternalVibratorService;
import android.os.IVibratorService;
import android.os.Message;
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
import android.os.UserHandle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.WorkSource;
import android.provider.Settings;
import android.util.DebugUtils;
import android.util.Flog;
import android.util.HwLog;
import android.util.Slog;
import android.util.SparseArray;
import android.util.StatsLog;
import android.view.InputDevice;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.app.IBatteryStats;
import com.android.internal.os.SomeArgs;
import com.android.internal.util.DumpUtils;
import com.android.server.NsdService;
import com.android.server.job.controllers.JobStatus;
import com.android.server.notification.NotificationShellCmd;
import com.android.server.power.IHwShutdownThread;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.android.os.IHwVibrator;
import huawei.android.security.IHwBehaviorCollectManager;
import huawei.cust.HwCustUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;

public class VibratorService extends IVibratorService.Stub implements InputManager.InputDeviceListener {
    private static final String CAMERA_POSITION = "facing";
    private static final String CAMERA_SEND_SYSTEM_EVENTS_PERMISSION = "android.permission.CAMERA_SEND_SYSTEM_EVENTS";
    private static final String CAMERA_STATE = "cameraState";
    private static final String CLIENT_NAME = "clientName";
    private static final String COMMAND_SET_AMPLITUDE_PREFIX = "setamplitude=";
    private static final boolean DEBUG = false;
    private static final long[] DEFAULT_VIBRATE_WAVEFORM = {60};
    private static final long[] DOUBLE_CLICK_EFFECT_FALLBACK_TIMINGS = {0, 30, 100, 30};
    private static final String EXTERNAL_VIBRATOR_SERVICE = "external_vibrator_service";
    private static final int HWVIBRATOR_SUPPORT = 0;
    private static final long MAX_HAPTIC_FEEDBACK_DURATION = 5000;
    private static final int MAX_HWVIBRATE_TIME = 400;
    private static final String MEETIME_PACKAGE = "com.huawei.meetime";
    private static final int MSG_SET_HWVIBRATOR_DELAY = 1;
    private static final Map<String, Integer> PATTERN_TIMELEN = new HashMap<String, Integer>() {
        /* class com.android.server.VibratorService.AnonymousClass2 */

        {
            Integer valueOf = Integer.valueOf((int) IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME);
            put("haptic.pattern.type1", valueOf);
            put("haptic.pattern.type2", valueOf);
            put("haptic.pattern.type3", 1000);
            put("haptic.pattern.type4", 1000);
            put("haptic.pattern.type5", 1590);
            put("haptic.pattern.type6", 1100);
            put("haptic.pattern.type7", 1600);
            put("haptic.pattern.type8", 4620);
            put("haptic.ringtone.Bounce", 6762);
            put("haptic.ringtone.Cartoon", 2118);
            put("haptic.ringtone.Chilled", 11947);
            put("haptic.ringtone.Classic_Bell", 5166);
            put("haptic.ringtone.Concentrate", 15707);
            put("haptic.ringtone.Day_lily", 5695);
            put("haptic.ringtone.Digital_Ringtone", 3756);
            put("haptic.ringtone.Dream", 6044);
            put("haptic.ringtone.Dream_It_Possible", 39710);
            put("haptic.ringtone.Dynamo", 15148);
            put("haptic.ringtone.Flipped", 20477);
            put("haptic.ringtone.Forest_Day", 6112);
            put("haptic.ringtone.Free", 9917);
            put("haptic.ringtone.Halo", 16042);
            put("haptic.ringtone.Huawei_Tune_Clean", 13342);
            put("haptic.ringtone.Huawei_Tune_Living", 17249);
            put("haptic.ringtone.Huawei_Tune_Orchestral", 15815);
            put("haptic.ringtone.Menuet", 8261);
            put("haptic.ringtone.Neon", 23925);
            put("haptic.ringtone.Notes", 9051);
            put("haptic.ringtone.Pulse", 27550);
            put("haptic.ringtone.Sax", 4780);
            put("haptic.ringtone.Spin", 6000);
            put("haptic.ringtone.Whistle", 20276);
            put("haptic.ringtone.Amusement_Park", 9441);
            put("haptic.ringtone.Breathe_Freely", 30887);
            put("haptic.ringtone.Fantasy_World", 15301);
            put("haptic.ringtone.Summer_Afternoon", 31468);
            put("haptic.ringtone.Sunlit_Garden", 38330);
            put("haptic.ringtone.Surging_Power", 17125);
            put("haptic.ringtone.Brave", 20559);
            put("haptic.ringtone.Heart", 27495);
            put("haptic.ringtone.Little_Emotion", 12061);
            put("haptic.ringtone.Psychedelic", 16921);
            put("haptic.ringtone.Ripple", 7641);
            put("haptic.ringtone.We_Are_The_Brave", 45551);
            put("haptic.ringtone.Another_Van", 13879);
            put("haptic.ringtone.Eye", 24514);
            put("haptic.ringtone.Future", 29309);
            put("haptic.ringtone.Nova_Song", 33164);
            put("haptic.ringtone.Smile", 12040);
            put("haptic.ringtone.Swing", 20463);
            put("haptic.ringtone.Wave", 25681);
            put("haptic.ringtone.Harp", 10030);
            put("haptic.ringtone.Hello_Ya", 35051);
            put("haptic.ringtone.Sailing", 19188);
            put("haptic.ringtone.Westlake", 11654);
            put("haptic.ringtone.Porsche_Design_Titanium", 29999);
            put("haptic.ringtone.Ringtone_ATT", 21000);
            put("haptic.ringtone.ATT_FIREFLY_2016_ATT", 23000);
            put("haptic.ringtone.Now_or_Never_ATT", 19000);
            put("haptic.ringtone.Classic", Integer.valueOf((int) EventLogTags.IMF_FORCE_RECONNECT_IME));
            put("haptic.ringtone.Dance", Integer.valueOf((int) EventLogTags.WM_NO_SURFACE_MEMORY));
            put("haptic.ringtone.entel123_tt", 10000);
            put("haptic.ringtone.Gamelan_Music", 52000);
            put("haptic.ringtone.Institucionalmidi", valueOf);
            put("haptic.ringtone.Institucional_Rubrica", 3000);
            put("haptic.ringtone.Jazz", 38000);
            Integer valueOf2 = Integer.valueOf((int) EventLogTags.JOB_DEFERRED_EXECUTION);
            put("haptic.ringtone.Telekom_Ring", valueOf2);
            put("haptic.ringtone.T-Mobile_dzwonek", valueOf2);
            put("haptic.ringtone.T-Mobile_Ring", valueOf2);
            put("haptic.ringtone.Ringtone_UN", 24000);
            put("haptic.ringtone.Kolbi", 3870);
            put("haptic.ringtone.PLAY", 29949);
            put("haptic.ringtone.Proximus_Ringtone_Classic", 32093);
            put("haptic.ringtone.Proximus_Ringtone_Dance", 30881);
            put("haptic.ringtone.Proximus_Ringtone_Jazz", 37907);
            put("haptic.ringtone.Flow", 8141);
            put("haptic.ringtone.Polonaise", 13035);
            put("haptic.ringtone.Suite", 15987);
            put("haptic.ringtone.Wind_Dance", 8034);
        }
    };
    private static final String RAMPING_RINGER_ENABLED = "ramping_ringer_enabled";
    private static final int SCALE_HIGH = 1;
    private static final float SCALE_HIGH_GAMMA = 0.5f;
    private static final int SCALE_LOW = -1;
    private static final float SCALE_LOW_GAMMA = 1.5f;
    private static final int SCALE_LOW_MAX_AMPLITUDE = 192;
    private static final int SCALE_MUTE = -100;
    private static final int SCALE_NONE = 0;
    private static final float SCALE_NONE_GAMMA = 1.0f;
    private static final int SCALE_VERY_HIGH = 2;
    private static final float SCALE_VERY_HIGH_GAMMA = 0.25f;
    private static final int SCALE_VERY_LOW = -2;
    private static final float SCALE_VERY_LOW_GAMMA = 2.0f;
    private static final int SCALE_VERY_LOW_MAX_AMPLITUDE = 168;
    private static final long SEND_AUDIO_DELAY_TIME = 3000;
    private static final String SYSTEM_UI_PACKAGE = "com.android.systemui";
    private static final String TAG = "VibratorService";
    private boolean isVibratorStateOn = false;
    private final boolean mAllowPriorityVibrationsInLowPowerMode;
    private final AppOpsManager mAppOps;
    private AudioManager mAudioManager;
    private final IBatteryStats mBatteryStatsService;
    private final Context mContext;
    private int mCurVibUid = -1;
    private ExternalVibration mCurrentExternalVibration;
    private IBinder mCurrentMaskToken;
    @GuardedBy({"mLock"})
    private Vibration mCurrentVibration;
    private HwCustCbsUtils mCust;
    private final int mDefaultVibrationAmplitude;
    private final SparseArray<VibrationEffect> mFallbackEffects;
    private final Handler mH = new Handler() {
        /* class com.android.server.VibratorService.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what != 1) {
                Slog.w(VibratorService.TAG, "Unknown message:" + msg.what);
            } else if (msg.obj instanceof SomeArgs) {
                SomeArgs args = (SomeArgs) msg.obj;
                if (args.arg2 instanceof IBinder) {
                    VibratorService.this.setHwVibratorMessage(args, (IBinder) args.arg2);
                }
            }
        }
    };
    private int mHapticFeedbackIntensity;
    HwInnerVibratorService mHwInnerService = new HwInnerVibratorService(this);
    private InputManager mIm;
    private boolean mInputDeviceListenerRegistered;
    private final ArrayList<Vibrator> mInputDeviceVibrators = new ArrayList<>();
    BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        /* class com.android.server.VibratorService.AnonymousClass9 */

        @Override // android.content.BroadcastReceiver
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
    private final Object mLock = new Object();
    private boolean mLowPowerMode;
    private int mNotificationIntensity;
    private Bundle mOptions;
    private PowerManagerInternal mPowerManagerInternal;
    private final LinkedList<VibrationInfo> mPreviousAlarmVibrations;
    private final LinkedList<ExternalVibration> mPreviousExternalVibrations;
    private final LinkedList<VibrationInfo> mPreviousNotificationVibrations;
    private final LinkedList<VibrationInfo> mPreviousRingVibrations;
    private final LinkedList<VibrationInfo> mPreviousVibrations;
    private final int mPreviousVibrationsLimit;
    private final SparseArray<Integer> mProcStatesCache = new SparseArray<>();
    private int mRingIntensity;
    private final SparseArray<ScaleLevel> mScaleLevels;
    private final Runnable mSendVibratorStateOffRunnable = new Runnable() {
        /* class com.android.server.VibratorService.AnonymousClass5 */

        @Override // java.lang.Runnable
        public void run() {
            VibratorService.this.setAudioManager();
            if (VibratorService.this.mAudioManager != null && VibratorService.this.isVibratorStateOn) {
                VibratorService.this.isVibratorStateOn = false;
                VibratorService.this.mAudioManager.setParameter("vibrator_state", "off");
                Slog.i(VibratorService.TAG, "mSendVibratorStateOffRunnable vibrator_state off");
            }
        }
    };
    private final Runnable mSendVibratorStateOnRunnable = new Runnable() {
        /* class com.android.server.VibratorService.AnonymousClass4 */

        @Override // java.lang.Runnable
        public void run() {
            VibratorService.this.setAudioManager();
            if (VibratorService.this.mAudioManager != null && !VibratorService.this.isVibratorStateOn) {
                VibratorService.this.isVibratorStateOn = true;
                VibratorService.this.mAudioManager.setParameter("vibrator_state", "on");
                Slog.i(VibratorService.TAG, "mSendVibratorStateOnRunnable vibrator_state on");
            }
        }
    };
    private SettingsObserver mSettingObserver;
    private final boolean mSupportsAmplitudeControl;
    private final boolean mSupportsExternalControl;
    private volatile VibrateThread mThread;
    private final WorkSource mTmpWorkSource = new WorkSource();
    private final IUidObserver mUidObserver = new IUidObserver.Stub() {
        /* class com.android.server.VibratorService.AnonymousClass3 */

        public void onUidStateChanged(int uid, int procState, long procStateSeq) {
            VibratorService.this.mProcStatesCache.put(uid, Integer.valueOf(procState));
        }

        public void onUidGone(int uid, boolean disabled) {
            VibratorService.this.mProcStatesCache.delete(uid);
        }

        public void onUidActive(int uid) {
        }

        public void onUidIdle(int uid, boolean disabled) {
        }

        public void onUidCachedChanged(int uid, boolean cached) {
        }
    };
    private boolean mVibrateInputDevicesSetting;
    private final Runnable mVibrationEndRunnable = new Runnable() {
        /* class com.android.server.VibratorService.AnonymousClass8 */

        @Override // java.lang.Runnable
        public void run() {
            VibratorService.this.onVibrationFinished();
        }
    };
    private Vibrator mVibrator;
    private boolean mVibratorUnderExternalControl;
    private final PowerManager.WakeLock mWakeLock;

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

    static native void vibratorSetExternalControl(boolean z);

    static native boolean vibratorSupportsAmplitudeControl();

    static native boolean vibratorSupportsExternalControl();

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendAudioVibratorStateOn() {
        Handler handler = this.mH;
        if (handler != null) {
            if (!handler.hasCallbacks(this.mSendVibratorStateOnRunnable)) {
                this.mH.post(this.mSendVibratorStateOnRunnable);
            }
            if (this.mH.hasCallbacks(this.mSendVibratorStateOffRunnable)) {
                this.mH.removeCallbacks(this.mSendVibratorStateOffRunnable);
            }
            this.mH.postDelayed(this.mSendVibratorStateOffRunnable, 3000);
        }
    }

    private void sendAudioVibratorStateOff() {
        Handler handler = this.mH;
        if (handler != null && handler.hasCallbacks(this.mSendVibratorStateOffRunnable)) {
            this.mH.removeCallbacks(this.mSendVibratorStateOffRunnable);
            this.mH.postDelayed(this.mSendVibratorStateOffRunnable, 3000);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setAudioManager() {
        Context context;
        if (this.mAudioManager == null && (context = this.mContext) != null) {
            Object audioObject = context.getSystemService("audio");
            if (audioObject instanceof AudioManager) {
                this.mAudioManager = (AudioManager) audioObject;
            }
        }
    }

    /* access modifiers changed from: private */
    public class Vibration implements IBinder.DeathRecipient {
        public VibrationEffect effect;
        private boolean isNeedCancelHwVibrator;
        public final String opPkg;
        public VibrationEffect originalEffect;
        public final String reason;
        public final long startTime;
        public final long startTimeDebug;
        public final IBinder token;
        private String type;
        public final int uid;
        public final int usageHint;

        public void setType(String t) {
            this.type = t;
        }

        public String getType() {
            return this.type;
        }

        private Vibration(IBinder token2, VibrationEffect effect2, int usageHint2, int uid2, String opPkg2, String reason2) {
            this.isNeedCancelHwVibrator = true;
            this.type = null;
            this.token = token2;
            this.effect = effect2;
            this.startTime = SystemClock.elapsedRealtime();
            this.startTimeDebug = System.currentTimeMillis();
            this.usageHint = usageHint2;
            this.uid = uid2;
            this.opPkg = opPkg2;
            this.reason = reason2;
        }

        @Override // android.os.IBinder.DeathRecipient
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
            VibratorService vibratorService = VibratorService.this;
            if (VibratorService.isHapticFeedback(this.usageHint)) {
                return true;
            }
            VibrationEffect.Prebaked prebaked = this.effect;
            if (prebaked instanceof VibrationEffect.Prebaked) {
                int id = prebaked.getId();
                if (id == 0 || id == 1 || id == 2 || id == 3 || id == 4 || id == 5 || id == 21) {
                    return true;
                }
                Slog.w(VibratorService.TAG, "Unknown prebaked vibration effect, assuming it isn't haptic feedback.");
                return false;
            }
            long duration = prebaked.getDuration();
            if (duration < 0 || duration >= VibratorService.MAX_HAPTIC_FEEDBACK_DURATION) {
                return false;
            }
            return true;
        }

        public boolean isNotification() {
            VibratorService vibratorService = VibratorService.this;
            return VibratorService.isNotification(this.usageHint);
        }

        public boolean isRingtone() {
            VibratorService vibratorService = VibratorService.this;
            return VibratorService.isRingtone(this.usageHint);
        }

        public boolean isAlarm() {
            VibratorService vibratorService = VibratorService.this;
            return VibratorService.isAlarm(this.usageHint);
        }

        public boolean isFromSystem() {
            int i = this.uid;
            return i == 1000 || i == 0 || VibratorService.SYSTEM_UI_PACKAGE.equals(this.opPkg);
        }

        public VibrationInfo toInfo() {
            return new VibrationInfo(this.startTimeDebug, this.effect, this.originalEffect, this.usageHint, this.uid, this.opPkg, this.reason, this.type);
        }
    }

    /* access modifiers changed from: private */
    public static class VibrationInfo {
        private final VibrationEffect mEffect;
        private final String mOpPkg;
        private final VibrationEffect mOriginalEffect;
        private final String mReason;
        private final long mStartTimeDebug;
        private final String mType;
        private final int mUid;
        private final int mUsageHint;

        public VibrationInfo(long startTimeDebug, VibrationEffect effect, VibrationEffect originalEffect, int usageHint, int uid, String opPkg, String reason, String type) {
            this.mStartTimeDebug = startTimeDebug;
            this.mEffect = effect;
            this.mOriginalEffect = originalEffect;
            this.mUsageHint = usageHint;
            this.mUid = uid;
            this.mOpPkg = opPkg;
            this.mReason = reason;
            this.mType = type;
        }

        public String toString() {
            return "startTime: " + DateFormat.getDateTimeInstance().format(new Date(this.mStartTimeDebug)) + ", effect: " + this.mEffect + ", originalEffect: " + this.mOriginalEffect + ", usageHint: " + this.mUsageHint + ", uid: " + this.mUid + ", opPkg: " + this.mOpPkg + ", reason: " + this.mReason + ", type: " + this.mType;
        }
    }

    /* access modifiers changed from: private */
    public static final class ScaleLevel {
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

    /* JADX WARN: Type inference failed for: r2v5, types: [com.android.server.VibratorService$ExternalVibratorService, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    VibratorService(Context context) {
        this.mCust = (HwCustCbsUtils) HwCustUtils.createObj(HwCustCbsUtils.class, new Object[]{context});
        vibratorInit();
        vibratorOff();
        this.mSupportsAmplitudeControl = vibratorSupportsAmplitudeControl();
        this.mSupportsExternalControl = vibratorSupportsExternalControl();
        this.mContext = context;
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, "*vibrator*");
        this.mWakeLock.setReferenceCounted(true);
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService(AppOpsManager.class);
        this.mBatteryStatsService = IBatteryStats.Stub.asInterface(ServiceManager.getService("batterystats"));
        this.mPreviousVibrationsLimit = this.mContext.getResources().getInteger(17694876);
        this.mDefaultVibrationAmplitude = this.mContext.getResources().getInteger(17694784);
        this.mAllowPriorityVibrationsInLowPowerMode = this.mContext.getResources().getBoolean(17891345);
        this.mPreviousRingVibrations = new LinkedList<>();
        this.mPreviousNotificationVibrations = new LinkedList<>();
        this.mPreviousAlarmVibrations = new LinkedList<>();
        this.mPreviousVibrations = new LinkedList<>();
        this.mPreviousExternalVibrations = new LinkedList<>();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_OFF");
        context.registerReceiver(this.mIntentReceiver, filter);
        VibrationEffect clickEffect = createHwEffectFromType("haptic.common.click");
        VibrationEffect doubleClickEffect = createHwEffectFromType("haptic.common.double_click");
        VibrationEffect heavyClickEffect = createHwEffectFromType("haptic.common.long_press");
        VibrationEffect tickEffect = createHwEffectFromType("haptic.common.click_up");
        VibrationEffect textureTickEffect = createHwEffectFromType("haptic.common.tick");
        this.mFallbackEffects = new SparseArray<>();
        this.mFallbackEffects.put(0, clickEffect);
        this.mFallbackEffects.put(1, doubleClickEffect);
        this.mFallbackEffects.put(2, tickEffect);
        this.mFallbackEffects.put(5, heavyClickEffect);
        this.mFallbackEffects.put(21, textureTickEffect);
        this.mScaleLevels = new SparseArray<>();
        this.mScaleLevels.put(-2, new ScaleLevel(SCALE_VERY_LOW_GAMMA, SCALE_VERY_LOW_MAX_AMPLITUDE));
        this.mScaleLevels.put(-1, new ScaleLevel(SCALE_LOW_GAMMA, SCALE_LOW_MAX_AMPLITUDE));
        this.mScaleLevels.put(0, new ScaleLevel(1.0f));
        this.mScaleLevels.put(1, new ScaleLevel(SCALE_HIGH_GAMMA));
        this.mScaleLevels.put(2, new ScaleLevel(SCALE_VERY_HIGH_GAMMA));
        ServiceManager.addService(EXTERNAL_VIBRATOR_SERVICE, (IBinder) new ExternalVibratorService());
    }

    private VibrationEffect createEffectFromResource(int resId) {
        return createEffectFromTimings(getLongIntArray(this.mContext.getResources(), resId));
    }

    private VibrationEffect createHwEffectFromType(String type) {
        if (checkHwVibrator(type) == 0) {
            return VibrationEffect.createWaveformType(DEFAULT_VIBRATE_WAVEFORM, type);
        }
        if (type == "haptic.common.long_press") {
            return createEffectFromResource(17236034);
        }
        if (type == "haptic.common.double_click") {
            return VibrationEffect.createWaveform(DOUBLE_CLICK_EFFECT_FALLBACK_TIMINGS, -1);
        }
        if (type == "haptic.common.click_up") {
            return createEffectFromResource(17236000);
        }
        if (type == "haptic.common.click") {
            return createEffectFromResource(17236076);
        }
        if (type == "haptic.common.tick") {
            return VibrationEffect.get(2, false);
        }
        Slog.e(TAG, "Invalid type:" + type);
        return null;
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
                /* class com.android.server.VibratorService.AnonymousClass6 */

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
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("ring_vibration_intensity"), true, this.mSettingObserver, -1);
            this.mContext.registerReceiver(new BroadcastReceiver() {
                /* class com.android.server.VibratorService.AnonymousClass7 */

                @Override // android.content.BroadcastReceiver
                public void onReceive(Context context, Intent intent) {
                    VibratorService.this.updateVibrators();
                }
            }, new IntentFilter("android.intent.action.USER_SWITCHED"), null, this.mH);
            try {
                ActivityManager.getService().registerUidObserver(this.mUidObserver, 3, -1, (String) null);
            } catch (RemoteException e) {
            }
            this.mCurrentMaskToken = null;
            updateVibrators();
        } finally {
            Trace.traceEnd(8388608);
        }
    }

    /* access modifiers changed from: private */
    public final class SettingsObserver extends ContentObserver {
        public SettingsObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean SelfChange) {
            VibratorService.this.updateVibrators();
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
    /* access modifiers changed from: public */
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

    public void vibrate(int uid, String opPkg, VibrationEffect effect, int usageHint, String reason, IBinder token) {
        Throwable th;
        Object obj;
        Throwable th2;
        Trace.traceBegin(8388608, "vibrate, reason = " + reason);
        HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.VIBRATOR_VIBRATE);
        try {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.VIBRATE") != 0) {
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
                Object obj2 = this.mLock;
                synchronized (obj2) {
                    try {
                        if ((effect instanceof VibrationEffect.OneShot) && this.mCurrentVibration != null && (this.mCurrentVibration.effect instanceof VibrationEffect.OneShot)) {
                            VibrationEffect.OneShot newOneShot = (VibrationEffect.OneShot) effect;
                            VibrationEffect.OneShot currentOneShot = this.mCurrentVibration.effect;
                            if (this.mCurrentVibration.hasTimeoutLongerThan(newOneShot.getDuration()) && newOneShot.getAmplitude() == currentOneShot.getAmplitude()) {
                                Trace.traceEnd(8388608);
                                return;
                            }
                        }
                        if (this.mCurrentExternalVibration != null) {
                            Trace.traceEnd(8388608);
                        } else if (isRepeatingVibration(effect) || this.mCurrentVibration == null || !isRepeatingVibration(this.mCurrentVibration)) {
                            obj = obj2;
                            try {
                                Vibration vib = new Vibration(token, effect, usageHint, uid, opPkg, reason);
                                int pid = Binder.getCallingPid();
                                StringBuilder sb = new StringBuilder();
                                sb.append("vibrate by uid:");
                                sb.append(uid);
                                sb.append(", pid:");
                                sb.append(pid);
                                sb.append(", pkg:");
                                try {
                                    sb.append(opPkg);
                                    sb.append(", reason:");
                                    sb.append(reason);
                                    Flog.i((int) NsdService.NativeResponseCode.SERVICE_REGISTERED, sb.toString());
                                    if (this.mProcStatesCache.get(uid, 7).intValue() <= 7 || vib.isNotification() || vib.isRingtone() || vib.isAlarm()) {
                                        linkVibration(vib);
                                        long ident = Binder.clearCallingIdentity();
                                        try {
                                            doCancelVibrateLocked();
                                            startVibrationLocked(vib);
                                            addToPreviousVibrationsLocked(vib);
                                            Trace.traceEnd(8388608);
                                        } finally {
                                            Binder.restoreCallingIdentity(ident);
                                        }
                                    } else {
                                        Slog.e(TAG, "Ignoring incoming vibration as process with uid = " + uid + " is background, usage = " + AudioAttributes.usageToString(vib.usageHint));
                                        Trace.traceEnd(8388608);
                                    }
                                } catch (Throwable th3) {
                                    th = th3;
                                    Trace.traceEnd(8388608);
                                    throw th;
                                }
                            } catch (Throwable th4) {
                                th2 = th4;
                                throw th2;
                            }
                        } else {
                            Trace.traceEnd(8388608);
                        }
                    } catch (Throwable th5) {
                        th2 = th5;
                        obj = obj2;
                        throw th2;
                    }
                }
            }
        } catch (Throwable th6) {
            th = th6;
            Trace.traceEnd(8388608);
            throw th;
        }
    }

    private static boolean isRepeatingVibration(VibrationEffect effect) {
        return effect.getDuration() == JobStatus.NO_LATEST_RUNTIME;
    }

    /* access modifiers changed from: private */
    public static boolean isRepeatingVibration(Vibration vib) {
        return vib.effect.getDuration() == JobStatus.NO_LATEST_RUNTIME || isRingtone(vib.getType()) || isAlarm(vib.getType());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void addToPreviousVibrationsLocked(Vibration vib) {
        LinkedList<VibrationInfo> previousVibrations;
        if (vib.isRingtone()) {
            previousVibrations = this.mPreviousRingVibrations;
        } else if (vib.isNotification()) {
            previousVibrations = this.mPreviousNotificationVibrations;
        } else if (vib.isAlarm()) {
            previousVibrations = this.mPreviousAlarmVibrations;
        } else {
            previousVibrations = this.mPreviousVibrations;
        }
        if (previousVibrations.size() > this.mPreviousVibrationsLimit) {
            previousVibrations.removeFirst();
        }
        previousVibrations.addLast(vib.toInfo());
    }

    public void cancelVibrate(IBinder token) {
        HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.VIBRATOR_CANCELVIBRATE);
        this.mContext.enforceCallingOrSelfPermission("android.permission.VIBRATE", "cancelVibrate");
        synchronized (this.mLock) {
            this.mCurrentMaskToken = null;
            if (this.mCurrentVibration != null && this.mCurrentVibration.token == token) {
                int uid = Binder.getCallingUid();
                int pid = Binder.getCallingPid();
                long ident = Binder.clearCallingIdentity();
                try {
                    Flog.i((int) NsdService.NativeResponseCode.SERVICE_REGISTERED, "Canceling vibration. UID:" + uid + ", PID:" + pid);
                    doCancelVibrateLocked();
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mLock"})
    private void doCancelVibrateLocked() {
        Trace.asyncTraceEnd(8388608, "vibration", 0);
        Trace.traceBegin(8388608, "doCancelVibrateLocked");
        try {
            this.mH.removeCallbacks(this.mVibrationEndRunnable);
            if (this.mThread != null) {
                this.mThread.cancel();
                this.mThread = null;
            }
            if (this.mCurrentExternalVibration != null) {
                this.mCurrentExternalVibration.mute();
                this.mCurrentExternalVibration = null;
                setVibratorUnderExternalControl(false);
            }
            if (!isCancelHwVibrate()) {
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

    private boolean isCurrentMaskToken(Vibration vib) {
        Slog.w(TAG, "setHwVibrator type=" + vib.getType());
        if (this.mCurrentMaskToken == null || vib.getType() == null) {
            return false;
        }
        if ((!isRingtone(vib.getType()) && !isAlarm(vib.getType()) && !isPattern(vib.getType())) || this.mCurrentMaskToken != vib.token) {
            return false;
        }
        Slog.w(TAG, "setHwVibrator token is same type=" + vib.getType());
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mLock"})
    private void startVibrationLocked(Vibration vib) {
        Trace.traceBegin(8388608, "startVibrationLocked");
        try {
            if (!isAllowedToVibrateLocked(vib)) {
                Slog.e(TAG, "is not allowed to vibrate locked");
                return;
            }
            int intensity = getCurrentIntensityLocked(vib);
            if (intensity == 0) {
                Slog.e(TAG, "current intensity locked is VIBRATION_INTENSITY_OFF");
                Trace.traceEnd(8388608);
            } else if (this.mCust == null || this.mCust.allowVibrateWhenSlient(this.mContext, vib.opPkg)) {
                int mode = getAppOpMode(vib);
                Flog.i((int) NsdService.NativeResponseCode.SERVICE_REGISTERED, "startVibrationLocked mode:" + mode);
                if (mode != 0) {
                    if (mode == 2) {
                        Slog.w(TAG, "Would be an error: vibrate from uid " + vib.uid);
                    }
                    Trace.traceEnd(8388608);
                } else if (vib.getType() == null || vib.effect.getRepeatIndex() >= 0) {
                    this.mCurrentMaskToken = null;
                    applyVibrationIntensityScalingLocked(vib, intensity);
                    startVibrationInnerLocked(vib);
                    Trace.traceEnd(8388608);
                } else if (isCurrentMaskToken(vib)) {
                    Trace.traceEnd(8388608);
                } else if ((isAlarm(vib.getType()) || isRingtone(vib.getType()) || isPattern(vib.getType()) || isNotification(vib.getType())) && isNeedDisableVibratorOn(vib.uid)) {
                    if (!isNotification(vib.getType())) {
                        this.mCurrentMaskToken = vib.token;
                    }
                    Slog.i(TAG, "vibrator is disabled!");
                    Trace.traceEnd(8388608);
                } else {
                    this.mCurrentVibration = vib;
                    setHwVibrator(vib.getType());
                    sendAudioVibratorStateOn();
                    Trace.traceEnd(8388608);
                }
            } else {
                Trace.traceEnd(8388608);
            }
        } finally {
            Trace.traceEnd(8388608);
        }
    }

    @GuardedBy({"mLock"})
    private void startVibrationInnerLocked(Vibration vib) {
        Trace.traceBegin(8388608, "startVibrationInnerLocked");
        try {
            this.mCurrentVibration = vib;
            if (vib.effect instanceof VibrationEffect.OneShot) {
                Trace.asyncTraceBegin(8388608, "vibration", 0);
                VibrationEffect.OneShot oneShot = vib.effect;
                doVibratorOn(oneShot.getDuration(), oneShot.getAmplitude(), vib.uid, vib.usageHint);
                HwLog.dubaie("DUBAI_TAG_VIBRATOR_ON", "uid=" + vib.uid + " name=" + vib.opPkg + " duration=" + oneShot.getDuration() + " amplitude=" + oneShot.getAmplitude());
                this.mH.postDelayed(this.mVibrationEndRunnable, oneShot.getDuration());
            } else if (vib.effect instanceof VibrationEffect.Waveform) {
                Trace.asyncTraceBegin(8388608, "vibration", 0);
                VibrationEffect.Waveform waveform = vib.effect;
                if (waveform.getType() == null || waveform.getRepeatIndex() >= 0) {
                    if (vib.getType() != null) {
                        this.mThread = new VibrateThread(this, waveform, vib.uid, vib.usageHint, vib.getType());
                    } else {
                        this.mThread = new VibrateThread(waveform, vib.uid, vib.usageHint);
                    }
                    this.mThread.setOpPkg(vib.opPkg);
                    this.mThread.start();
                } else {
                    Slog.i(TAG, "Oneshot Waveform Vibrator, type:" + waveform.getType());
                    setHwVibrator(waveform.getType());
                    sendAudioVibratorStateOn();
                    Trace.traceEnd(8388608);
                    return;
                }
            } else if (vib.effect instanceof VibrationEffect.Prebaked) {
                Trace.asyncTraceBegin(8388608, "vibration", 0);
                long timeout = doVibratorPrebakedEffectLocked(vib);
                if (timeout > 0) {
                    this.mH.postDelayed(this.mVibrationEndRunnable, timeout);
                }
            } else {
                Slog.e(TAG, "Unknown vibration type, ignoring");
            }
            sendAudioVibratorStateOn();
        } finally {
            Trace.traceEnd(8388608);
        }
    }

    private boolean isAllowedToVibrateLocked(Vibration vib) {
        if (!this.mLowPowerMode) {
            return true;
        }
        HwCustCbsUtils hwCustCbsUtils = this.mCust;
        if ((hwCustCbsUtils != null && hwCustCbsUtils.isAllowLowPowerPkg(vib.opPkg)) || vib.usageHint == 6 || vib.usageHint == 4 || vib.usageHint == 11 || vib.usageHint == 7) {
            return true;
        }
        return false;
    }

    private int getCurrentIntensityLocked(Vibration vib) {
        if (vib.isRingtone()) {
            return this.mRingIntensity;
        }
        if (vib.isNotification()) {
            return this.mNotificationIntensity;
        }
        if (vib.isHapticFeedback()) {
            return this.mHapticFeedbackIntensity;
        }
        if (vib.isAlarm()) {
            return 3;
        }
        return 2;
    }

    private void applyVibrationIntensityScalingLocked(Vibration vib, int intensity) {
        int defaultIntensity;
        if (vib.effect instanceof VibrationEffect.Prebaked) {
            vib.effect.setEffectStrength(intensityToEffectStrength(intensity));
            return;
        }
        if (vib.isRingtone()) {
            defaultIntensity = this.mVibrator.getDefaultRingVibrationIntensity();
        } else if (vib.isNotification()) {
            defaultIntensity = this.mVibrator.getDefaultNotificationVibrationIntensity();
        } else if (vib.isHapticFeedback()) {
            defaultIntensity = this.mVibrator.getDefaultHapticFeedbackIntensity();
        } else if (vib.isAlarm()) {
            defaultIntensity = 3;
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
            vib.effect.setType(vib.originalEffect.getType());
        }
    }

    private int getAppOpMode(Vibration vib) {
        int mode = this.mAppOps.checkAudioOpNoThrow(3, vib.usageHint, vib.uid, vib.opPkg);
        if (mode == 0) {
            return this.mAppOps.startOpNoThrow(3, vib.uid, vib.opPkg);
        }
        return mode;
    }

    @GuardedBy({"mLock"})
    private void reportFinishVibrationLocked() {
        Trace.traceBegin(8388608, "reportFinishVibrationLocked");
        try {
            if (this.mCurrentVibration != null) {
                this.mAppOps.finishOp(3, this.mCurrentVibration.uid, this.mCurrentVibration.opPkg);
                unlinkVibration(this.mCurrentVibration);
                this.mCurrentVibration = null;
                sendAudioVibratorStateOff();
            }
        } finally {
            Trace.traceEnd(8388608);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void linkVibration(Vibration vib) {
        if (vib.effect instanceof VibrationEffect.Waveform) {
            try {
                vib.token.linkToDeath(vib, 0);
            } catch (RemoteException e) {
            }
        }
    }

    private void unlinkVibration(Vibration vib) {
        if (vib.effect instanceof VibrationEffect.Waveform) {
            try {
                vib.token.unlinkToDeath(vib, 0);
            } catch (NoSuchElementException e) {
                Slog.w(TAG, "unlinkVibration NoSuchElementException");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateVibrators() {
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
        int[] ids;
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
        for (int i : this.mIm.getInputDeviceIds()) {
            InputDevice device = this.mIm.getInputDevice(i);
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
        this.mRingIntensity = Settings.System.getIntForUser(this.mContext.getContentResolver(), "ring_vibration_intensity", this.mVibrator.getDefaultRingVibrationIntensity(), -2);
    }

    @Override // android.hardware.input.InputManager.InputDeviceListener
    public void onInputDeviceAdded(int deviceId) {
        updateVibrators();
    }

    @Override // android.hardware.input.InputManager.InputDeviceListener
    public void onInputDeviceChanged(int deviceId) {
        updateVibrators();
    }

    @Override // android.hardware.input.InputManager.InputDeviceListener
    public void onInputDeviceRemoved(int deviceId) {
        updateVibrators();
    }

    private boolean doVibratorExists() {
        return vibratorExists();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doVibratorOn(long millis, int amplitude, int uid, int usageHint) {
        Trace.traceBegin(8388608, "doVibratorOn");
        try {
            synchronized (this.mInputDeviceVibrators) {
                if (isNeedDisableVibratorOn(uid)) {
                    Slog.i(TAG, "vibrator is disabled!");
                    return;
                }
                Flog.i((int) NsdService.NativeResponseCode.SERVICE_REGISTERED, "Turning vibrator on for " + millis + " ms.");
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
                Trace.traceEnd(8388608);
            }
        } finally {
            Trace.traceEnd(8388608);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isNeedDisableVibratorOn(int uid) {
        Bundle bundle = this.mOptions;
        if (bundle == null) {
            Slog.i(TAG, "mOptions is null.");
            return false;
        } else if (bundle.getInt(CAMERA_STATE, 2) != 1) {
            Slog.i(TAG, "isNeedDisableVibratorOn camera is not active");
            return false;
        } else {
            String clientName = this.mOptions.getString(CLIENT_NAME);
            if (clientName == null) {
                Slog.i(TAG, "clientName is null.");
                return false;
            } else if (clientName.equals(MEETIME_PACKAGE)) {
                Slog.i(TAG, "clientName = com.huawei.meetime");
                return false;
            } else {
                ActivityInfo lastResumeActivity = HwActivityTaskManager.getLastResumedActivity();
                if (lastResumeActivity == null) {
                    Slog.i(TAG, "lastResumeActivity is null. clientName = " + clientName);
                    return false;
                } else if (!clientName.equals(lastResumeActivity.packageName)) {
                    return false;
                } else {
                    if (this.mOptions.getInt(CAMERA_POSITION) == 1 && SYSTEM_UI_PACKAGE.equals(clientName)) {
                        return false;
                    }
                    Slog.i(TAG, "isNeedDisableVibratorOn uid = " + uid + ", clientName = " + clientName);
                    PackageManager pm = this.mContext.getPackageManager();
                    if (pm == null) {
                        Slog.i(TAG, "PackageManager is null. clientName = " + clientName);
                        return false;
                    }
                    ApplicationInfo appInfo = null;
                    try {
                        appInfo = pm.getApplicationInfoAsUser(clientName, 786432, UserHandle.getCallingUserId());
                    } catch (PackageManager.NameNotFoundException e) {
                        Slog.w(TAG, "could not get appinfo by clientName: " + clientName);
                    }
                    if (appInfo != null) {
                        return !UserHandle.isSameApp(appInfo.uid, uid);
                    }
                    Slog.i(TAG, "appInfo is null. clientName = " + clientName);
                    return false;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getActualUid(int uid, String clientName) {
        if (clientName == null) {
            Slog.i(TAG, "clientName is null");
            return uid;
        }
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null) {
            Slog.i(TAG, "PackageManager is null. clientName = " + clientName);
            return uid;
        }
        ApplicationInfo appInfo = null;
        try {
            appInfo = pm.getApplicationInfoAsUser(clientName, 786432, UserHandle.getCallingUserId());
        } catch (PackageManager.NameNotFoundException e) {
            Slog.w(TAG, "could not get appinfo by clientName: " + clientName);
        }
        if (appInfo != null) {
            return appInfo.uid;
        }
        Slog.i(TAG, "appInfo is null. clientName = " + clientName);
        return uid;
    }

    /* access modifiers changed from: private */
    public static boolean isRingtone(String type) {
        return type != null && type.startsWith("haptic.ringtone.");
    }

    /* access modifiers changed from: private */
    public static boolean isAlarm(String type) {
        return type != null && type.startsWith("haptic.alarm.");
    }

    /* access modifiers changed from: private */
    public static boolean isNotification(String type) {
        return type != null && type.startsWith("haptic.notice.");
    }

    /* access modifiers changed from: private */
    public static boolean isPattern(String type) {
        return type != null && type.startsWith("haptic.pattern.");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doVibratorSetAmplitude(int amplitude) {
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
        } finally {
            Trace.traceEnd(8388608);
        }
    }

    @GuardedBy({"mLock"})
    private long doVibratorPrebakedEffectLocked(Vibration vib) {
        boolean usingInputDeviceVibrators;
        Trace.traceBegin(8388608, "doVibratorPrebakedEffectLocked");
        try {
            VibrationEffect.Prebaked prebaked = vib.effect;
            synchronized (this.mInputDeviceVibrators) {
                usingInputDeviceVibrators = !this.mInputDeviceVibrators.isEmpty();
            }
            if (!usingInputDeviceVibrators) {
                long timeout = vibratorPerformEffect((long) prebaked.getId(), (long) prebaked.getEffectStrength());
                if (timeout > 0) {
                    noteVibratorOnLocked(vib.uid, timeout);
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
            IBinder iBinder = vib.token;
            int i = vib.usageHint;
            int i2 = vib.uid;
            String str = vib.opPkg;
            Vibration fallbackVib = new Vibration(iBinder, effect, i, i2, str, vib.reason + " (fallback)");
            int intensity = getCurrentIntensityLocked(fallbackVib);
            linkVibration(fallbackVib);
            applyVibrationIntensityScalingLocked(fallbackVib, intensity);
            startVibrationInnerLocked(fallbackVib);
            Trace.traceEnd(8388608);
            return 0;
        } finally {
            Trace.traceEnd(8388608);
        }
    }

    private VibrationEffect getFallbackEffect(int effectId) {
        return this.mFallbackEffects.get(effectId);
    }

    private static int intensityToEffectStrength(int intensity) {
        if (intensity == 1) {
            return 0;
        }
        if (intensity == 2) {
            return 1;
        }
        if (intensity == 3) {
            return 2;
        }
        Slog.w(TAG, "Got unexpected vibration intensity: " + intensity);
        return 2;
    }

    /* access modifiers changed from: private */
    public static boolean isNotification(int usageHint) {
        if (usageHint == 5 || usageHint == 7 || usageHint == 8 || usageHint == 9) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public static boolean isRingtone(int usageHint) {
        return usageHint == 6;
    }

    /* access modifiers changed from: private */
    public static boolean isHapticFeedback(int usageHint) {
        return usageHint == 13;
    }

    /* access modifiers changed from: private */
    public static boolean isAlarm(int usageHint) {
        return usageHint == 4;
    }

    private void noteVibratorOnLocked(int uid, long millis) {
        try {
            this.mBatteryStatsService.noteVibratorOn(uid, millis);
            StatsLog.write_non_chained(84, uid, null, 1, millis);
            this.mCurVibUid = uid;
        } catch (RemoteException e) {
        }
    }

    private void noteVibratorOffLocked() {
        int i = this.mCurVibUid;
        if (i >= 0) {
            try {
                this.mBatteryStatsService.noteVibratorOff(i);
                StatsLog.write_non_chained(84, this.mCurVibUid, null, 0, 0);
            } catch (RemoteException e) {
            }
            this.mCurVibUid = -1;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setVibratorUnderExternalControl(boolean externalControl) {
        this.mVibratorUnderExternalControl = externalControl;
        vibratorSetExternalControl(externalControl);
    }

    /* access modifiers changed from: private */
    public class VibrateThread extends Thread {
        private int mAmplitude;
        private boolean mForceStop;
        private String mHwVibratorType;
        private String mOpPkg;
        private final int mUid;
        private final int mUsageHint;
        private final VibrationEffect.Waveform mWaveform;

        VibrateThread(VibrationEffect.Waveform waveform, int uid, int usageHint) {
            this.mOpPkg = null;
            this.mAmplitude = 0;
            this.mWaveform = waveform;
            this.mUid = uid;
            this.mUsageHint = usageHint;
            VibratorService.this.mTmpWorkSource.set(uid);
            VibratorService.this.mWakeLock.setWorkSource(VibratorService.this.mTmpWorkSource);
        }

        VibrateThread(VibratorService vibratorService, VibrationEffect.Waveform waveform, int uid, int usageHint, String type) {
            this(waveform, uid, usageHint);
            this.mHwVibratorType = type;
        }

        public void setOpPkg(String pkg) {
            this.mOpPkg = pkg;
        }

        private long delayLocked(long duration) {
            Trace.traceBegin(8388608, "delayLocked");
            long durationRemaining = duration;
            if (duration > 0) {
                try {
                    long bedtime = SystemClock.uptimeMillis() + duration;
                    do {
                        try {
                            wait(durationRemaining);
                        } catch (InterruptedException e) {
                        }
                        if (this.mForceStop) {
                            break;
                        }
                        durationRemaining = bedtime - SystemClock.uptimeMillis();
                    } while (durationRemaining > 0);
                    return duration - durationRemaining;
                } finally {
                    Trace.traceEnd(8388608);
                }
            } else {
                Trace.traceEnd(8388608);
                return 0;
            }
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            boolean finished;
            Process.setThreadPriority(-8);
            VibratorService.this.mWakeLock.acquire();
            try {
                if (this.mHwVibratorType != null) {
                    finished = playHwWaveform();
                } else {
                    finished = playWaveform();
                }
                VibratorService.setParameter("setamplitude=0");
                if (finished) {
                    VibratorService.this.onVibrationFinished();
                }
            } finally {
                VibratorService.this.mWakeLock.release();
            }
        }

        public boolean playHwWaveform() {
            Trace.traceBegin(8388608, "playHwWaveform");
            try {
                synchronized (this) {
                    if (VibratorService.PATTERN_TIMELEN.containsKey(this.mHwVibratorType)) {
                        int timeLen = ((Integer) VibratorService.PATTERN_TIMELEN.get(this.mHwVibratorType)).intValue();
                        if ((VibratorService.isAlarm(this.mHwVibratorType) || VibratorService.isRingtone(this.mHwVibratorType) || VibratorService.isPattern(this.mHwVibratorType) || VibratorService.isNotification(this.mHwVibratorType)) && VibratorService.this.isNeedDisableVibratorOn(this.mUid)) {
                            Slog.i(VibratorService.TAG, "vibrator is disabled!");
                            return false;
                        }
                        Slog.i(VibratorService.TAG, "Play continuous Waveform vibrator, Type: " + this.mHwVibratorType);
                        while (!this.mForceStop) {
                            VibratorService.setHwVibrator(this.mHwVibratorType);
                            VibratorService.setParameter(VibratorService.COMMAND_SET_AMPLITUDE_PREFIX + this.mAmplitude);
                            delayLocked((long) timeLen);
                            VibratorService.this.sendAudioVibratorStateOn();
                        }
                        Trace.traceEnd(8388608);
                        return !this.mForceStop;
                    }
                    Slog.e(VibratorService.TAG, "Invalid type:" + this.mHwVibratorType);
                    Trace.traceEnd(8388608);
                    return false;
                }
            } finally {
                Trace.traceEnd(8388608);
            }
        }

        public boolean playWaveform() {
            boolean z;
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
                    long onDuration = 0;
                    while (true) {
                        if (this.mForceStop) {
                            break;
                        } else if (index < len) {
                            int amplitude = amplitudes[index];
                            int index2 = index + 1;
                            long duration2 = timings[index];
                            if (duration2 <= j) {
                                index = index2;
                            } else {
                                if (amplitude != 0) {
                                    if (onDuration <= j) {
                                        onDuration = getTotalOnDuration(timings, amplitudes, index2 - 1, repeat);
                                        duration = duration2;
                                        VibratorService.this.doVibratorOn(onDuration, amplitude, this.mUid, this.mUsageHint);
                                        HwLog.dubaie("DUBAI_TAG_VIBRATOR_ON", "uid=" + this.mUid + " name=" + this.mOpPkg + " duration=" + onDuration + " amplitude=" + amplitude);
                                    } else {
                                        duration = duration2;
                                        VibratorService.this.doVibratorSetAmplitude(amplitude);
                                    }
                                    VibratorService.setParameter(VibratorService.COMMAND_SET_AMPLITUDE_PREFIX + this.mAmplitude);
                                } else {
                                    duration = duration2;
                                }
                                long waitTime = delayLocked(duration);
                                if (amplitude != 0) {
                                    onDuration -= waitTime;
                                }
                                index = index2;
                                j = 0;
                            }
                        } else if (repeat < 0) {
                            break;
                        } else {
                            index = repeat;
                            j = 0;
                        }
                    }
                    z = !this.mForceStop;
                }
                return z;
            } finally {
                Trace.traceEnd(8388608);
            }
        }

        public void cancel() {
            synchronized (this) {
                VibratorService.this.mThread.mForceStop = true;
                VibratorService.this.mThread.notify();
            }
        }

        public void setHwAmplitude(String type, int amplitude) {
            String str;
            if ((this.mHwVibratorType == null && type.equals("haptic.pattern.type1")) || ((str = this.mHwVibratorType) != null && str.equals(type))) {
                this.mAmplitude = amplitude;
                VibratorService.setParameter(VibratorService.COMMAND_SET_AMPLITUDE_PREFIX + amplitude);
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
                        repeatIndex = -1;
                        continue;
                    }
                }
                return timing;
            } while (i != startIndex);
            return 1000;
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
                pw.print("  mCurrentExternalVibration=");
                if (this.mCurrentExternalVibration != null) {
                    pw.println(this.mCurrentExternalVibration.toString());
                } else {
                    pw.println("null");
                }
                pw.println("  mVibratorUnderExternalControl=" + this.mVibratorUnderExternalControl);
                pw.println("  mLowPowerMode=" + this.mLowPowerMode);
                pw.println("  mHapticFeedbackIntensity=" + this.mHapticFeedbackIntensity);
                pw.println("  mNotificationIntensity=" + this.mNotificationIntensity);
                pw.println("  mRingIntensity=" + this.mRingIntensity);
                pw.println("");
                pw.println("  Previous ring vibrations:");
                Iterator<VibrationInfo> it = this.mPreviousRingVibrations.iterator();
                while (it.hasNext()) {
                    pw.print("    ");
                    pw.println(it.next().toString());
                }
                pw.println("  Previous notification vibrations:");
                Iterator<VibrationInfo> it2 = this.mPreviousNotificationVibrations.iterator();
                while (it2.hasNext()) {
                    pw.print("    ");
                    pw.println(it2.next().toString());
                }
                pw.println("  Previous alarm vibrations:");
                Iterator<VibrationInfo> it3 = this.mPreviousAlarmVibrations.iterator();
                while (it3.hasNext()) {
                    pw.print("    ");
                    pw.println(it3.next().toString());
                }
                pw.println("  Previous vibrations:");
                Iterator<VibrationInfo> it4 = this.mPreviousVibrations.iterator();
                while (it4.hasNext()) {
                    pw.print("    ");
                    pw.println(it4.next().toString());
                }
                pw.println("  Previous external vibrations:");
                Iterator<ExternalVibration> it5 = this.mPreviousExternalVibrations.iterator();
                while (it5.hasNext()) {
                    pw.print("    ");
                    pw.println(it5.next().toString());
                }
            }
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r8v0, resolved type: com.android.server.VibratorService */
    /* JADX WARN: Multi-variable type inference failed */
    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) throws RemoteException {
        new VibratorShellCommand(this).exec(this, in, out, err, args, callback, resultReceiver);
    }

    final class ExternalVibratorService extends IExternalVibratorService.Stub {
        ExternalVibrationDeathRecipient mCurrentExternalDeathRecipient;

        ExternalVibratorService() {
        }

        /* JADX INFO: Multiple debug info for r3v2 int: [D('scaleLevel' int), D('currentIntensity' int)] */
        public int onExternalVibrationStart(ExternalVibration vib) {
            int currentIntensity;
            int defaultIntensity;
            int currentIntensity2;
            if (!VibratorService.this.mSupportsExternalControl) {
                return VibratorService.SCALE_MUTE;
            }
            if (ActivityManager.checkComponentPermission("android.permission.VIBRATE", vib.getUid(), -1, true) != 0) {
                Slog.w(VibratorService.TAG, "pkg=" + vib.getPackage() + ", uid=" + vib.getUid() + " tried to play externally controlled vibration without VIBRATE permission, ignoring.");
                return VibratorService.SCALE_MUTE;
            }
            synchronized (VibratorService.this.mLock) {
                if (!vib.equals(VibratorService.this.mCurrentExternalVibration)) {
                    if (VibratorService.this.mCurrentExternalVibration == null) {
                        VibratorService.this.doCancelVibrateLocked();
                        VibratorService.this.setVibratorUnderExternalControl(true);
                    }
                    VibratorService.this.mCurrentExternalVibration = vib;
                    this.mCurrentExternalDeathRecipient = new ExternalVibrationDeathRecipient();
                    VibratorService.this.mCurrentExternalVibration.linkToDeath(this.mCurrentExternalDeathRecipient);
                    if (VibratorService.this.mPreviousExternalVibrations.size() > VibratorService.this.mPreviousVibrationsLimit) {
                        VibratorService.this.mPreviousExternalVibrations.removeFirst();
                    }
                    VibratorService.this.mPreviousExternalVibrations.addLast(vib);
                }
                int usage = vib.getAudioAttributes().getUsage();
                if (VibratorService.isRingtone(usage)) {
                    defaultIntensity = VibratorService.this.mVibrator.getDefaultRingVibrationIntensity();
                    currentIntensity = VibratorService.this.mRingIntensity;
                } else if (VibratorService.isNotification(usage)) {
                    defaultIntensity = VibratorService.this.mVibrator.getDefaultNotificationVibrationIntensity();
                    currentIntensity = VibratorService.this.mNotificationIntensity;
                } else if (VibratorService.isHapticFeedback(usage)) {
                    defaultIntensity = VibratorService.this.mVibrator.getDefaultHapticFeedbackIntensity();
                    currentIntensity = VibratorService.this.mHapticFeedbackIntensity;
                } else if (VibratorService.isAlarm(usage)) {
                    defaultIntensity = 3;
                    currentIntensity = 3;
                } else {
                    defaultIntensity = 0;
                    currentIntensity = 0;
                }
                currentIntensity2 = currentIntensity - defaultIntensity;
            }
            if (currentIntensity2 >= -2 && currentIntensity2 <= 2) {
                return currentIntensity2;
            }
            Slog.w(VibratorService.TAG, "Error in scaling calculations, ended up with invalid scale level " + currentIntensity2 + " for vibration " + vib);
            return 0;
        }

        public void onExternalVibrationStop(ExternalVibration vib) {
            synchronized (VibratorService.this.mLock) {
                if (vib.equals(VibratorService.this.mCurrentExternalVibration)) {
                    VibratorService.this.mCurrentExternalVibration.unlinkToDeath(this.mCurrentExternalDeathRecipient);
                    this.mCurrentExternalDeathRecipient = null;
                    VibratorService.this.mCurrentExternalVibration = null;
                    VibratorService.this.setVibratorUnderExternalControl(false);
                }
            }
        }

        /* access modifiers changed from: private */
        public class ExternalVibrationDeathRecipient implements IBinder.DeathRecipient {
            private ExternalVibrationDeathRecipient() {
            }

            @Override // android.os.IBinder.DeathRecipient
            public void binderDied() {
                synchronized (VibratorService.this.mLock) {
                    ExternalVibratorService.this.onExternalVibrationStop(VibratorService.this.mCurrentExternalVibration);
                }
            }
        }
    }

    private final class VibratorShellCommand extends ShellCommand {
        private final IBinder mToken;

        /* access modifiers changed from: private */
        public final class CommonOptions {
            public boolean force;

            private CommonOptions() {
                this.force = false;
            }

            public void check(String opt) {
                if (((opt.hashCode() == 1497 && opt.equals("-f")) ? (char) 0 : 65535) == 0) {
                    this.force = true;
                }
            }
        }

        private VibratorShellCommand(IBinder token) {
            this.mToken = token;
        }

        public int onCommand(String cmd) {
            if ("vibrate".equals(cmd)) {
                return runVibrate();
            }
            if ("waveform".equals(cmd)) {
                return runWaveform();
            }
            if ("prebaked".equals(cmd)) {
                return runPrebaked();
            }
            if (!"cancel".equals(cmd)) {
                return handleDefaultCommands(cmd);
            }
            VibratorService.this.cancelVibrate(this.mToken);
            return 0;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:14:0x0033, code lost:
            r3 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:15:0x0034, code lost:
            if (r1 != null) goto L_0x0036;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0036, code lost:
            $closeResource(r2, r1);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x0039, code lost:
            throw r3;
         */
        private boolean checkDoNotDisturb(CommonOptions opts) {
            try {
                int zenMode = Settings.Global.getInt(VibratorService.this.mContext.getContentResolver(), "zen_mode");
                if (zenMode == 0 || opts.force) {
                    return false;
                }
                PrintWriter pw = getOutPrintWriter();
                pw.print("Ignoring because device is on DND mode ");
                pw.println(DebugUtils.flagsToString(Settings.Global.class, "ZEN_MODE_", zenMode));
                $closeResource(null, pw);
                return true;
            } catch (Settings.SettingNotFoundException e) {
                return false;
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

        private int runVibrate() {
            Trace.traceBegin(8388608, "runVibrate");
            try {
                CommonOptions commonOptions = new CommonOptions();
                while (true) {
                    String opt = getNextOption();
                    if (opt == null) {
                        break;
                    }
                    commonOptions.check(opt);
                }
                if (checkDoNotDisturb(commonOptions)) {
                    return 0;
                }
                long duration = Long.parseLong(getNextArgRequired());
                String description = getNextArg();
                if (description == null) {
                    description = NotificationShellCmd.CHANNEL_NAME;
                }
                VibratorService.this.vibrate(Binder.getCallingUid(), description, VibrationEffect.createOneShot(duration, -1), 0, "Shell Command", this.mToken);
                Trace.traceEnd(8388608);
                return 0;
            } finally {
                Trace.traceEnd(8388608);
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:23:0x0051 A[Catch:{ all -> 0x00fe }] */
        /* JADX WARNING: Removed duplicated region for block: B:29:0x006c A[Catch:{ all -> 0x00fe }] */
        private int runWaveform() {
            VibrationEffect effect;
            Trace.traceBegin(8388608, "runWaveform");
            String description = NotificationShellCmd.CHANNEL_NAME;
            int repeat = -1;
            ArrayList<Integer> amplitudesList = null;
            try {
                CommonOptions commonOptions = new CommonOptions();
                while (true) {
                    String opt = getNextOption();
                    boolean z = false;
                    if (opt == null) {
                        break;
                    }
                    int hashCode = opt.hashCode();
                    if (hashCode != 1492) {
                        if (hashCode != 1495) {
                            if (hashCode == 1509 && opt.equals("-r")) {
                                z = true;
                                if (z) {
                                    description = getNextArgRequired();
                                } else if (z) {
                                    repeat = Integer.parseInt(getNextArgRequired());
                                } else if (!z) {
                                    commonOptions.check(opt);
                                } else if (amplitudesList == null) {
                                    amplitudesList = new ArrayList<>();
                                }
                            }
                        } else if (opt.equals("-d")) {
                            if (z) {
                            }
                        }
                    } else if (opt.equals("-a")) {
                        z = true;
                        if (z) {
                        }
                    }
                    z = true;
                    if (z) {
                    }
                }
                if (checkDoNotDisturb(commonOptions)) {
                    return 0;
                }
                ArrayList<Long> timingsList = new ArrayList<>();
                while (true) {
                    String arg = getNextArg();
                    if (arg == null) {
                        break;
                    } else if (amplitudesList == null || amplitudesList.size() >= timingsList.size()) {
                        timingsList.add(Long.valueOf(Long.parseLong(arg)));
                    } else {
                        amplitudesList.add(Integer.valueOf(Integer.parseInt(arg)));
                    }
                }
                long[] timings = timingsList.stream().mapToLong($$Lambda$ELHKvd8JMVRD8rbALqYPKbDX2mM.INSTANCE).toArray();
                if (amplitudesList == null) {
                    effect = VibrationEffect.createWaveform(timings, repeat);
                } else {
                    effect = VibrationEffect.createWaveform(timings, amplitudesList.stream().mapToInt($$Lambda$UV1wDVoVlbcxpr8zevj_aMFtUGw.INSTANCE).toArray(), repeat);
                }
                VibratorService.this.vibrate(Binder.getCallingUid(), description, effect, 0, "Shell Command", this.mToken);
                Trace.traceEnd(8388608);
                return 0;
            } finally {
                Trace.traceEnd(8388608);
            }
        }

        private int runPrebaked() {
            Trace.traceBegin(8388608, "runPrebaked");
            try {
                CommonOptions commonOptions = new CommonOptions();
                while (true) {
                    String opt = getNextOption();
                    if (opt == null) {
                        break;
                    }
                    commonOptions.check(opt);
                }
                if (checkDoNotDisturb(commonOptions)) {
                    return 0;
                }
                int id = Integer.parseInt(getNextArgRequired());
                String description = getNextArg();
                VibratorService.this.vibrate(Binder.getCallingUid(), description == null ? NotificationShellCmd.CHANNEL_NAME : description, VibrationEffect.get(id, false), 0, "Shell Command", this.mToken);
                Trace.traceEnd(8388608);
                return 0;
            } finally {
                Trace.traceEnd(8388608);
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:10:0x007d, code lost:
            $closeResource(r0, r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x0080, code lost:
            throw r1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:8:0x007a, code lost:
            r1 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x007b, code lost:
            if (r2 != null) goto L_0x007d;
         */
        public void onHelp() {
            PrintWriter pw = getOutPrintWriter();
            pw.println("Vibrator commands:");
            pw.println("  help");
            pw.println("    Prints this help text.");
            pw.println("");
            pw.println("  vibrate duration [description]");
            pw.println("    Vibrates for duration milliseconds; ignored when device is on DND ");
            pw.println("    (Do Not Disturb) mode.");
            pw.println("  waveform [-d description] [-r index] [-a] duration [amplitude] ...");
            pw.println("    Vibrates for durations and amplitudes in list;");
            pw.println("    ignored when device is on DND (Do Not Disturb) mode.");
            pw.println("    If -r is provided, the waveform loops back to the specified");
            pw.println("    index (e.g. 0 loops from the beginning)");
            pw.println("    If -a is provided, the command accepts duration-amplitude pairs;");
            pw.println("    otherwise, it accepts durations only and alternates off/on");
            pw.println("    Duration is in milliseconds; amplitude is a scale of 1-255.");
            pw.println("  prebaked effect-id [description]");
            pw.println("    Vibrates with prebaked effect; ignored when device is on DND ");
            pw.println("    (Do Not Disturb) mode.");
            pw.println("  cancel");
            pw.println("    Cancels any active vibration");
            pw.println("Common Options:");
            pw.println("  -f - Force. Ignore Do Not Disturb setting.");
            pw.println("");
            $closeResource(null, pw);
        }
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.VibratorService$HwInnerVibratorService, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public IBinder getHwInnerService() {
        return this.mHwInnerService;
    }

    private boolean isCancelHwVibrate() {
        boolean isCanceled = false;
        Vibration vibration = this.mCurrentVibration;
        if (!(vibration == null || vibration.getType() == null)) {
            isCanceled = true;
            if (this.mCurrentVibration.isNeedCancelHwVibrator) {
                stopHwVibrator(this.mCurrentVibration.getType());
                this.mCurrentVibration = null;
            }
        }
        return isCanceled;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getUsageHintFromType(String vibType) {
        if (vibType == null) {
            return 0;
        }
        if (isAlarm(vibType)) {
            return 4;
        }
        if (isNotification(vibType)) {
            return 10;
        }
        if (isRingtone(vibType) || isPattern(vibType)) {
            return 6;
        }
        return 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setHwVibratorMessage(SomeArgs args, IBinder localToken) {
        if (args == null) {
            Slog.w(TAG, "setHwVibratorDelay msg is null");
        } else if (this.mHwInnerService == null) {
            Slog.w(TAG, "setHwVibratorDelay mHwInnerService is null");
        } else {
            int processId = args.argi1;
            String packageName = "";
            String vibrateType = "";
            if (args.arg1 instanceof String) {
                packageName = (String) args.arg1;
            }
            if (args.arg3 instanceof String) {
                vibrateType = (String) args.arg3;
            }
            this.mHwInnerService.setHwVibrator(processId, packageName, localToken, vibrateType);
            Slog.i(TAG, "setHwVibratorDelay pkg delay: " + packageName + " type: " + vibrateType);
        }
    }

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
            setHwVibrator(uid, opPkg, token, type, -1);
        }

        public void setHwVibrator(int uid, String opPkg, IBinder token, String type, int repeat) {
            long j;
            long j2;
            Throwable th;
            Throwable th2;
            int opUid;
            Trace.traceBegin(8388608, "setHwVibrator");
            if (type == null) {
                j = 8388608;
            } else if (token == null) {
                j = 8388608;
            } else {
                try {
                    if (!type.startsWith("haptic.control.")) {
                        VibratorService.this.mContext.enforceCallingOrSelfPermission("android.permission.VIBRATE", "setHwVibrator");
                    }
                    VibratorService.this.verifyIncomingUid(uid);
                    synchronized (VibratorService.this.mLock) {
                        try {
                            int pid = Binder.getCallingPid();
                            opUid = VibratorService.this.getActualUid(uid, opPkg);
                            Slog.i(TAG, "setHwVibrator on: uid is " + opUid + ", pid is " + pid + ", packagename = " + opPkg + ", type is " + type + " repeat:" + repeat);
                            j2 = 8388608;
                        } catch (Throwable th3) {
                            th2 = th3;
                            j2 = 8388608;
                            throw th2;
                        }
                        try {
                            Vibration vib = new Vibration(token, VibrationEffect.createWaveform(VibratorService.DEFAULT_VIBRATE_WAVEFORM, repeat), VibratorService.this.getUsageHintFromType(type), opUid, opPkg, "Shell Command");
                            vib.setType(type);
                            if (VibratorService.isRepeatingVibration(vib) || VibratorService.this.mCurrentVibration == null || !VibratorService.isRepeatingVibration(VibratorService.this.mCurrentVibration)) {
                                VibratorService.this.linkVibration(vib);
                                long ident = Binder.clearCallingIdentity();
                                try {
                                    if (VibratorService.this.mCurrentVibration != null) {
                                        VibratorService.this.mCurrentVibration.isNeedCancelHwVibrator = false;
                                    }
                                    VibratorService.this.doCancelVibrateLocked();
                                    VibratorService.this.startVibrationLocked(vib);
                                    VibratorService.this.addToPreviousVibrationsLocked(vib);
                                    Trace.traceEnd(8388608);
                                    return;
                                } finally {
                                    Binder.restoreCallingIdentity(ident);
                                }
                            } else {
                                Slog.d(TAG, "Ignoring incoming vibration : " + type);
                                Trace.traceEnd(8388608);
                                return;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            Trace.traceEnd(j2);
                            throw th;
                        }
                    }
                } catch (Throwable th5) {
                    th = th5;
                    j2 = 8388608;
                    Trace.traceEnd(j2);
                    throw th;
                }
            }
            Slog.i(TAG, "setHwVibrator type or token is null ");
            Trace.traceEnd(j);
        }

        public void setHwVibratorDelay(int uid, String opPkg, IBinder token, String type, int delay) {
            VibratorService.this.mContext.enforceCallingOrSelfPermission("android.permission.VIBRATE", "setHwVibratorDelay");
            if (delay == 0) {
                setHwVibrator(uid, opPkg, token, type, -1);
                return;
            }
            SomeArgs args = SomeArgs.obtain();
            args.argi1 = uid;
            args.arg1 = opPkg;
            args.arg2 = token;
            args.arg3 = type;
            VibratorService.this.mH.sendMessageDelayed(VibratorService.this.mH.obtainMessage(1, args), (long) delay);
            Slog.i(TAG, "setHwVibratorDelay pkg: " + opPkg + " type: " + type + " delay: " + delay);
        }

        public void setHwVibratorRepeat(int uid, String opPkg, IBinder token, String type, int repeat) {
            VibratorService.this.mContext.enforceCallingOrSelfPermission("android.permission.VIBRATE", "setHwVibratorRepeat");
            if (repeat >= VibratorService.DEFAULT_VIBRATE_WAVEFORM.length) {
                repeat = VibratorService.DEFAULT_VIBRATE_WAVEFORM.length - 1;
            }
            if (repeat < 0 || VibratorService.isRingtone(type) || VibratorService.isPattern(type)) {
                setHwVibrator(uid, opPkg, token, type, repeat);
            } else {
                Slog.e(TAG, "repeat mode only support for pattern type!");
            }
        }

        public void setHwAmplitude(int uid, String opPkg, IBinder token, String type, int amplitude) {
            VibratorService.this.mContext.enforceCallingOrSelfPermission("android.permission.VIBRATE", "setHwAmplitude");
            if (type == null) {
                Slog.i(TAG, "setHwAmplitude type is null");
                return;
            }
            synchronized (VibratorService.this.mLock) {
                if (VibratorService.this.mThread != null) {
                    VibratorService.this.mThread.setHwAmplitude(type, amplitude);
                } else if (VibratorService.this.mCurrentVibration == null || !type.equals(VibratorService.this.mCurrentVibration.getType())) {
                    VibratorService.setParameter("setamplitude=0");
                } else {
                    VibratorService.setParameter(VibratorService.COMMAND_SET_AMPLITUDE_PREFIX + amplitude);
                }
            }
        }

        public void stopHwVibrator(int uid, String opPkg, IBinder token, String type) {
            if (type == null) {
                Slog.i(TAG, "stopHwVibrator type is null");
                return;
            }
            if (!type.startsWith("haptic.control.")) {
                VibratorService.this.mContext.enforceCallingOrSelfPermission("android.permission.VIBRATE", "stopHwVibrator");
            }
            synchronized (VibratorService.this.mLock) {
                VibratorService.this.mCurrentMaskToken = null;
                if (!(VibratorService.this.mCurrentVibration == null || VibratorService.this.mCurrentVibration.token != token || VibratorService.this.mCurrentVibration.getType() == null)) {
                    int pid = Binder.getCallingPid();
                    long ident = Binder.clearCallingIdentity();
                    try {
                        Slog.i(TAG, "stopHwVibrator on: uid is " + uid + ", pid is " + pid + ", packagename = " + opPkg + ", type is " + type);
                        VibratorService.this.mCurrentVibration.isNeedCancelHwVibrator = true;
                        VibratorService.this.doCancelVibrateLocked();
                    } finally {
                        Binder.restoreCallingIdentity(ident);
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

        public void notifyVibrateOptions(IBinder token, Bundle options) {
            VibratorService.this.mContext.enforcePermission(VibratorService.CAMERA_SEND_SYSTEM_EVENTS_PERMISSION, Binder.getCallingPid(), Binder.getCallingUid(), null);
            VibratorService.this.mOptions = options;
        }
    }
}
