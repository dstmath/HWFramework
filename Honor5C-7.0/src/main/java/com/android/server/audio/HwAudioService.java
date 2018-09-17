package com.android.server.audio;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityManagerNative;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.soundtrigger.SoundTrigger.GenericSoundModel;
import android.media.AudioRoutesInfo;
import android.media.AudioSystem;
import android.media.IAudioRoutesObserver;
import android.media.SoundPool;
import android.media.soundtrigger.SoundTriggerDetector;
import android.media.soundtrigger.SoundTriggerDetector.Callback;
import android.media.soundtrigger.SoundTriggerDetector.EventPayload;
import android.media.soundtrigger.SoundTriggerManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.provider.SettingsEx.Systemex;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.IMonitor;
import android.util.IMonitor.EventStream;
import android.util.Slog;
import android.view.ContextThemeWrapper;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;
import com.android.internal.app.ISoundTriggerService;
import com.android.internal.app.ISoundTriggerService.Stub;
import com.android.server.HwServiceFactory;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.HwActivityManagerService;
import com.android.server.audio.AbsAudioService.DeviceVolumeState;
import com.android.server.audio.AudioService.SetModeDeathHandler;
import com.android.server.audio.report.AudioConst;
import com.android.server.audio.report.AudioExceptionMsg;
import com.android.server.pfw.autostartup.comm.XmlConst.PreciseIgnore;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.memory.utils.EventTracker;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.wifipro.WifiProCommonDefs;
import com.android.server.wifipro.WifiProCommonUtils;
import com.dts.hpx.lite.sdk.thin.HpxLiteSdkThin;
import com.huawei.pgmng.IPGPlugCallbacks;
import com.huawei.pgmng.PGAction;
import com.huawei.pgmng.PGPlug;
import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;
import java.util.UUID;

public class HwAudioService extends AudioService {
    private static final String ACTION_COMMFORCE = "android.intent.action.COMMFORCE";
    private static final String ACTION_INCALL_EXTRA = "IsForegroundActivity";
    private static final String ACTION_INCALL_SCREEN = "InCallScreenIsForegroundActivity";
    private static final String ACTION_START_SOUNDTRIGGER_DETECTOR = "com.huawei.vassistant.intent.action.START_SOUNDTRIGGER";
    private static final String ACTION_WAKEUP_SERVICE = "com.huawei.wakeup.services.WakeupService";
    private static final String BLUETOOTH_PKG_NAME = "com.android.bluetooth";
    private static final boolean DEBUG;
    private static final boolean DEBUG_THEME_SOUND = false;
    private static final String DTS_MODE_OFF_PARA = "srs_cfg:trumedia_enable=0";
    private static final String DTS_MODE_ON_PARA = "srs_cfg:trumedia_enable=1";
    private static final String DTS_MODE_PARA = "srs_cfg:trumedia_enable";
    private static final String DTS_MODE_PRESTATE = "persist.sys.dts.prestate";
    private static final int DTS_OFF = 0;
    private static final int DTS_ON = 3;
    private static final int DUALPA_RCV_DEALY_DURATION = 5000;
    private static final String DUALPA_RCV_DEALY_OFF = "dualpa_security_delay=0";
    private static final String DUALPA_RCV_DEALY_ON = "dualpa_security_delay=1";
    private static final String EXTRA_SOUNDTRIGGER_STATE = "soundtrigger_state";
    private static final String[] FORBIDDEN_SOUND_EFFECT_WHITE_LIST;
    private static final String HPX_MODE_PRESTATE = "persist.sys.hpx.prestate";
    private static final int HPX_STATE_OFF = 0;
    private static final int HPX_STATE_ON = 1;
    private static final String HS_NO_CHARGE_OFF = "hs_no_charge=off";
    private static final String HS_NO_CHARGE_ON = "hs_no_charge=on";
    public static final int HW_AUDIO_EXCEPTION_OCCOUR = -1000;
    public static final int HW_AUDIO_MODE_TYPE_EXCEPTION_OCCOUR = -1001;
    public static final int HW_AUDIO_TRACK_OVERFLOW_TYPE_EXCEPTION_OCCOUR = -1002;
    public static final int HW_MIC_MUTE_EXCEPTION_OCCOUR = -2002;
    public static final int HW_RING_MODE_TYPE_EXCEPTION_OCCOUR = -2001;
    public static final int HW_SCO_CONNECT_EXCEPTION_OCCOUR = -2003;
    private static final UUID MODEL_UUID;
    private static final int MSG_AUDIO_EXCEPTION_OCCUR = 10000;
    private static final int MSG_DUALPA_RCV_DEALY = 71;
    private static final int MSG_RECORD_ACTIVE = 61;
    private static final int MSG_RELOAD_SNDEFFS = 99;
    private static final int MSG_RINGER_MODE_CHANGE = 80;
    private static final String PERMISSION_COMMFORCE = "android.permission.COMM_FORCE";
    public static final int PID_BIT_WIDE = 21;
    private static final int RADIO_UID = 1001;
    private static final String[] RECORD_ACTIVE_APP_LIST;
    private static final String[] RECORD_REQUEST_APP_LIST;
    private static final String SESSION_ID = "session_id";
    private static final String SOUNDTRIGGER_MAD_OFF = "mad=off";
    private static final String SOUNDTRIGGER_MAD_ON = "mad=on";
    private static final String SOUNDTRIGGER_START = "start";
    private static final int SOUNDTRIGGER_STATUS_OFF = 0;
    private static final int SOUNDTRIGGER_STATUS_ON = 1;
    private static final String SOUNDTRIGGER_STOP = "stop";
    private static final String SOUNDTRIGGER_WAKUP_OFF = "wakeup=off";
    private static final String SOUNDTRIGGER_WAKUP_ON = "wakeup=on";
    private static final int SOUND_EFFECT_CLOSE = 1;
    private static final int SOUND_EFFECT_OPEN = 2;
    private static final int SPK_RCV_STEREO_OFF = 0;
    private static final String SPK_RCV_STEREO_OFF_PARA = "stereo_landscape_portrait_enable=0";
    private static final int SPK_RCV_STEREO_ON = 1;
    private static final String SPK_RCV_STEREO_ON_PARA = "stereo_landscape_portrait_enable=1";
    private static final String SWS_MODE_OFF_PARA = "HIFIPARA=STEREOWIDEN_Enable=false";
    private static final String SWS_MODE_ON_PARA = "HIFIPARA=STEREOWIDEN_Enable=true";
    private static final int SWS_OFF = 0;
    private static final int SWS_ON = 3;
    private static final int SWS_SCENE_AUDIO = 0;
    private static final int SWS_SCENE_VIDEO = 1;
    private static final String SYSKEY_SOUND_HWT = "syskey_sound_hwt";
    private static final int SYSTEM_UID = 1000;
    private static final String TAG = "HwAudioService";
    private static final long THREE_SEC_LIMITED = 3000;
    private static final String VASSISTANT_PACKAGE_NAME = "com.huawei.vassistant";
    private static boolean isCallForeground;
    private static HpxLiteSdkThin mHpxLiteSdkThin;
    private static final boolean mIsHuaweiSWS3Config;
    private static final boolean mIsHuaweiSafeMediaConfig;
    private static Handler mMyHandler;
    private static final int mSecurityVolumeIndex;
    private static int mSwsScene;
    private long dialogShowTime;
    private boolean isShowingDialog;
    private AudioExceptionRecord mAudioExceptionRecord;
    private IBinder mBinder;
    private Callback mCallBack;
    private ContentResolver mContentResolver;
    private Context mContext;
    DeathRecipient mDeathRecipient;
    private int mDtsStatus;
    private boolean mEnableAdjustVolume;
    private EnableVolumeClient mEnableVolumeClient;
    private final Object mEnableVolumeLock;
    private int mHeadsetSwitchState;
    private HwThemeHandler mHwThemeHandler;
    private boolean mIsFmConnected;
    private boolean mIsLinkDeathRecipient;
    private Toast mLVMToast;
    private final BroadcastReceiver mReceiver;
    private Toast mRingerModeToast;
    private SoundTriggerDetector mSoundTriggerDetector;
    private String mSoundTriggerGram;
    private Handler mSoundTriggerHandler;
    private SoundTriggerManager mSoundTriggerManager;
    private String mSoundTriggerRes;
    private ISoundTriggerService mSoundTriggerService;
    private int mSoundTriggerStatus;
    private int mSpkRcvStereoStatus;
    private int mSwsStatus;
    private String mSysKeyEffectFile;
    private UserManager mUserManager;
    private int oldInCallDevice;
    private int oldLVMDevice;

    /* renamed from: com.android.server.audio.HwAudioService.5 */
    class AnonymousClass5 implements Runnable {
        final /* synthetic */ boolean val$start;

        AnonymousClass5(boolean val$start) {
            this.val$start = val$start;
        }

        public void run() {
            Slog.i(HwAudioService.TAG, "createAndStartSoundTrigger startsoundtrigger");
            HwAudioService.this.startSoundTriggerV2(this.val$start);
        }
    }

    private class EnableVolumeClient implements DeathRecipient {
        private String mCallerPkg;
        private IBinder mCb;

        public String getCallerPkg() {
            return this.mCallerPkg;
        }

        EnableVolumeClient(IBinder cb) {
            if (cb != null) {
                try {
                    cb.linkToDeath(this, HwAudioService.SWS_SCENE_AUDIO);
                } catch (RemoteException e) {
                    Slog.w(HwAudioService.TAG, "EnableVolumeClient() could not link to " + cb + " binder death");
                    cb = null;
                }
            }
            this.mCb = cb;
            this.mCallerPkg = HwAudioService.this.getPackageNameByPid(Binder.getCallingPid());
        }

        public void binderDied() {
            synchronized (HwAudioService.this.mEnableVolumeLock) {
                Slog.i(HwAudioService.TAG, " EnableVolumeClient died.pkgname:" + this.mCallerPkg);
                HwAudioService.this.mEnableAdjustVolume = true;
                release();
            }
        }

        public void release() {
            if (this.mCb != null) {
                this.mCb.unlinkToDeath(this, HwAudioService.SWS_SCENE_AUDIO);
                this.mCb = null;
            }
            HwAudioService.this.mEnableVolumeClient = null;
        }
    }

    private class HwThemeHandler extends Handler {
        private HwThemeHandler() {
        }

        public void handleMessage(Message msg) {
            if (HwAudioService.MSG_RELOAD_SNDEFFS == msg.what) {
                HwAudioService.this.reloadSoundEffects();
            }
        }
    }

    private static class IPGPClient implements IPGPlugCallbacks {
        private PGPlug mPGPlug;

        public IPGPClient() {
            this.mPGPlug = new PGPlug(this, HwAudioService.TAG);
            new Thread(this.mPGPlug, HwAudioService.TAG).start();
        }

        public void onDaemonConnected() {
            Slog.i(HwAudioService.TAG, "HwAudioService:IPGPClient connected success!");
        }

        public boolean onEvent(int actionID, String value) {
            if (HwAudioService.SWS_SCENE_VIDEO != PGAction.checkActionType(actionID)) {
                if (HwAudioService.DEBUG) {
                    Slog.i(HwAudioService.TAG, "HwAudioService:Filter application event id : " + actionID);
                }
                return true;
            }
            int subFlag = PGAction.checkActionFlag(actionID);
            if (HwAudioService.DEBUG) {
                Slog.i(HwAudioService.TAG, "HwAudioService:IPGP onEvent actionID=" + actionID + ", value=" + value + ",  subFlag=" + subFlag);
            }
            if (subFlag != HwAudioService.SWS_ON) {
                if (HwAudioService.DEBUG) {
                    Slog.i(HwAudioService.TAG, "Not used non-parent scene , ignore it");
                }
                return true;
            }
            if (actionID == 10009) {
                if (HwAudioService.mSwsScene == 0) {
                    HwAudioService.mSwsScene = HwAudioService.SWS_SCENE_VIDEO;
                    Slog.i(HwAudioService.TAG, "HwAudioService:Video_Front");
                    AudioSystem.setParameters("IPGPMode=video");
                }
            } else if (HwAudioService.mSwsScene != 0) {
                HwAudioService.mSwsScene = HwAudioService.SWS_SCENE_AUDIO;
                Slog.i(HwAudioService.TAG, "HwAudioService:Video_Not_Front");
                AudioSystem.setParameters("IPGPMode=audio");
            }
            return true;
        }

        public void onConnectedTimeout() {
            Slog.e(HwAudioService.TAG, "HwAudioService:Client connect timeout!");
        }
    }

    static {
        DEBUG = SystemProperties.getBoolean("ro.media.debuggable", DEBUG_THEME_SOUND);
        mIsHuaweiSafeMediaConfig = SystemProperties.getBoolean("ro.config.huawei_safe_media", true);
        mSecurityVolumeIndex = SystemProperties.getInt("ro.config.hw.security_volume", SWS_SCENE_AUDIO);
        isCallForeground = DEBUG_THEME_SOUND;
        FORBIDDEN_SOUND_EFFECT_WHITE_LIST = new String[]{"com.jawbone.up", "com.codoon.gps", "com.lakala.android", "com.hoolai.magic", "com.android.bankabc", "com.icbc"};
        mHpxLiteSdkThin = SystemProperties.getBoolean("ro.config.hpx_support", DEBUG_THEME_SOUND) ? new HpxLiteSdkThin() : null;
        RECORD_REQUEST_APP_LIST = new String[]{"com.google.android.googlequicksearchbox", "com.google.android.googlequicksearchbox:search", "com.google.android.googlequicksearchbox:interactor", "ru.yandex.searchplugin"};
        String[] strArr = new String[SWS_SCENE_VIDEO];
        strArr[SWS_SCENE_AUDIO] = "com.realvnc.android.remote";
        RECORD_ACTIVE_APP_LIST = strArr;
        mIsHuaweiSWS3Config = "sws3".equalsIgnoreCase(SystemProperties.get("ro.config.sws_version", "sws1"));
        MODEL_UUID = UUID.fromString("7dc67ab3-eab6-4e34-b62a-f4fa3788092a");
        mSwsScene = SWS_SCENE_AUDIO;
        mMyHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case HwAudioService.SWS_SCENE_VIDEO /*1*/:
                        if (AudioSystem.getDeviceConnectionState(8, AppHibernateCst.INVALID_PKG) == HwAudioService.SWS_SCENE_VIDEO || AudioSystem.getDeviceConnectionState(4, AppHibernateCst.INVALID_PKG) == HwAudioService.SWS_SCENE_VIDEO) {
                            if (HwAudioService.DTS_SOUND_EFFECTS_SUPPORT) {
                                String curDTSState = AudioSystem.getParameters(HwAudioService.DTS_MODE_PARA);
                                if (curDTSState != null && curDTSState.contains(HwAudioService.DTS_MODE_ON_PARA)) {
                                    AudioSystem.setParameters(HwAudioService.DTS_MODE_OFF_PARA);
                                    SystemProperties.set(HwAudioService.DTS_MODE_PRESTATE, PreciseIgnore.COMP_SCREEN_ON_VALUE_);
                                    if (HwAudioService.DEBUG) {
                                        Slog.i(HwAudioService.TAG, "Cur DTS mode = " + curDTSState + " force set DTS off");
                                    }
                                }
                            }
                            if (HwAudioService.HPX_EFFECTS_SUPPORT && HwAudioService.mHpxLiteSdkThin != null) {
                                int curHpxState = HwAudioService.mHpxLiteSdkThin.getHpxEnabled();
                                if (curHpxState == HwAudioService.SWS_SCENE_VIDEO) {
                                    HwAudioService.mHpxLiteSdkThin.setHpxEnabled(HwAudioService.SWS_SCENE_AUDIO);
                                    SystemProperties.set(HwAudioService.HPX_MODE_PRESTATE, PreciseIgnore.COMP_SCREEN_ON_VALUE_);
                                    if (HwAudioService.DEBUG) {
                                        Slog.i(HwAudioService.TAG, "Cur hpx state = " + curHpxState + " force set HPX off");
                                    }
                                }
                            }
                        }
                    case HwAudioService.SOUND_EFFECT_OPEN /*2*/:
                        if (HwAudioService.DTS_SOUND_EFFECTS_SUPPORT) {
                            String preDTSState = SystemProperties.get(HwAudioService.DTS_MODE_PRESTATE, "unknown");
                            if (preDTSState != null && preDTSState.equals(PreciseIgnore.COMP_SCREEN_ON_VALUE_)) {
                                AudioSystem.setParameters(HwAudioService.DTS_MODE_ON_PARA);
                                SystemProperties.set(HwAudioService.DTS_MODE_PRESTATE, "unknown");
                                if (HwAudioService.DEBUG) {
                                    Slog.i(HwAudioService.TAG, "set DTS on");
                                }
                            }
                        }
                        if (HwAudioService.HPX_EFFECTS_SUPPORT && HwAudioService.mHpxLiteSdkThin != null) {
                            String preHPXState = SystemProperties.get(HwAudioService.HPX_MODE_PRESTATE, "unknown");
                            if (preHPXState != null && preHPXState.equals(PreciseIgnore.COMP_SCREEN_ON_VALUE_)) {
                                HwAudioService.mHpxLiteSdkThin.setHpxEnabled(HwAudioService.SWS_SCENE_VIDEO);
                                SystemProperties.set(HwAudioService.HPX_MODE_PRESTATE, "unknown");
                                if (HwAudioService.DEBUG) {
                                    Slog.i(HwAudioService.TAG, "set HPX on");
                                }
                            }
                        }
                    default:
                }
            }
        };
    }

    protected boolean usingHwSafeMediaConfig() {
        return mIsHuaweiSafeMediaConfig;
    }

    protected int getHwSafeMediaVolumeIndex() {
        return mSecurityVolumeIndex * 10;
    }

    protected boolean isHwSafeMediaVolumeEnabled() {
        return mSecurityVolumeIndex > 0 ? true : DEBUG_THEME_SOUND;
    }

    protected boolean checkMMIRunning() {
        return "true".equals(SystemProperties.get("runtime.mmitest.isrunning", "false"));
    }

    public HwAudioService(Context context) {
        super(context);
        this.oldLVMDevice = SWS_SCENE_AUDIO;
        this.oldInCallDevice = SWS_SCENE_AUDIO;
        this.mLVMToast = null;
        this.mRingerModeToast = null;
        this.mSoundTriggerStatus = SWS_SCENE_AUDIO;
        this.mSoundTriggerRes = null;
        this.mSoundTriggerGram = null;
        this.mDtsStatus = SWS_SCENE_AUDIO;
        this.mSwsStatus = SWS_SCENE_AUDIO;
        this.isShowingDialog = DEBUG_THEME_SOUND;
        this.mEnableVolumeClient = null;
        this.mEnableVolumeLock = new Object();
        this.mEnableAdjustVolume = true;
        this.mAudioExceptionRecord = null;
        this.mSpkRcvStereoStatus = -1;
        this.dialogShowTime = 0;
        this.mIsLinkDeathRecipient = DEBUG_THEME_SOUND;
        this.mIsFmConnected = DEBUG_THEME_SOUND;
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null && action.equals("android.intent.action.FM")) {
                    int state = intent.getIntExtra("state", HwAudioService.SWS_SCENE_AUDIO);
                    if (HwAudioService.DEBUG) {
                        Slog.i(HwAudioService.TAG, "Broadcast Receiver: Got ACTION_FMRx_PLUG, state =" + state);
                    }
                    boolean isConnected = HwAudioService.this.mIsFmConnected;
                    if (state == 0 && isConnected) {
                        AudioSystem.setDeviceConnectionState(1048576, HwAudioService.SWS_SCENE_AUDIO, AppHibernateCst.INVALID_PKG, AppHibernateCst.INVALID_PKG);
                        HwAudioService.this.mIsFmConnected = HwAudioService.DEBUG_THEME_SOUND;
                    } else if (state == HwAudioService.SWS_SCENE_VIDEO && !isConnected) {
                        AudioSystem.setDeviceConnectionState(1048576, HwAudioService.SWS_SCENE_VIDEO, AppHibernateCst.INVALID_PKG, AppHibernateCst.INVALID_PKG);
                        HwAudioService.this.mIsFmConnected = true;
                    }
                } else if (action != null && action.equals("android.intent.action.RINGTONE_CHANGE")) {
                    HwAudioService.this.mSysKeyEffectFile = intent.getStringExtra("KEYTOUCH_AUDIOEFFECT_PATH");
                    if (HwAudioService.this.mSysKeyEffectFile != null) {
                        HwAudioService.this.mHwThemeHandler.sendMessage(HwAudioService.this.mHwThemeHandler.obtainMessage(HwAudioService.MSG_RELOAD_SNDEFFS, HwAudioService.SWS_SCENE_AUDIO, HwAudioService.SWS_SCENE_AUDIO, null));
                    }
                } else if (action != null && HwAudioService.ACTION_INCALL_SCREEN.equals(action)) {
                    HwAudioService.isCallForeground = intent.getBooleanExtra(HwAudioService.ACTION_INCALL_EXTRA, true);
                    if (HwAudioService.DEBUG) {
                        Slog.v(HwAudioService.TAG, "isCallForeground= " + HwAudioService.isCallForeground);
                    }
                } else if (action != null && "android.intent.action.PHONE_STATE".equals(action)) {
                    String state2 = intent.getStringExtra("state");
                    if (HwAudioService.DEBUG) {
                        Slog.v(HwAudioService.TAG, "phone state=" + state2);
                    }
                    if (TelephonyManager.EXTRA_STATE_IDLE.equals(state2)) {
                        HwAudioService.this.force_exitLVMMode();
                    }
                } else if (action != null && "android.intent.action.USER_UNLOCKED".equals(action)) {
                    HwAudioService.this.onUserUnlocked(intent.getIntExtra("android.intent.extra.user_handle", -10000));
                } else if (action != null && "android.intent.action.USER_SWITCHED".equals(action)) {
                    HwAudioService.this.onUserSwitched();
                }
            }
        };
        this.mCallBack = new Callback() {
            public void onAvailabilityChanged(int var1) {
            }

            public void onDetected(EventPayload var1) {
                Slog.i(HwAudioService.TAG, "onDetected() called with: eventPayload = [" + var1 + "]");
                if (var1.getCaptureSession() != null) {
                    HwAudioService.this.startWakeupService(var1.getCaptureSession().intValue());
                    return;
                }
                Slog.e(HwAudioService.TAG, "session invalid!");
            }

            public void onError() {
                Slog.e(HwAudioService.TAG, "onError()");
            }

            public void onRecognitionPaused() {
            }

            public void onRecognitionResumed() {
            }
        };
        this.mDeathRecipient = new DeathRecipient() {
            public void binderDied() {
                Slog.e(HwAudioService.TAG, "binderDied");
                HwAudioService.this.startSoundTriggerV2(true);
            }
        };
        this.mHeadsetSwitchState = SWS_SCENE_AUDIO;
        Slog.i(TAG, TAG);
        this.mContext = context;
        IntentFilter intentFilter = new IntentFilter("android.intent.action.FM");
        if (intentFilter != null) {
            intentFilter.addAction("android.intent.action.RINGTONE_CHANGE");
            intentFilter.addAction(ACTION_INCALL_SCREEN);
            intentFilter.addAction("android.intent.action.PHONE_STATE");
            intentFilter.addAction("android.intent.action.USER_UNLOCKED");
            intentFilter.addAction("android.intent.action.USER_SWITCHED");
            context.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, intentFilter, null, null);
        }
        this.mContentResolver = this.mContext.getContentResolver();
        this.mSysKeyEffectFile = Systemex.getString(this.mContentResolver, SYSKEY_SOUND_HWT);
        this.mAudioExceptionRecord = new AudioExceptionRecord();
        readPersistedSettingsEx(this.mContentResolver);
        setAudioSystemParameters();
        HwServiceFactory.getHwDrmDialogService().startDrmDialogService(context);
        if (SWS_SOUND_EFFECTS_SUPPORT && mIsHuaweiSWS3Config) {
            Slog.i(TAG, "HwAudioService: Start SWS3.0 IPGPClient.");
            IPGPClient iPGPClient = new IPGPClient();
        }
    }

    public void initHwThemeHandler() {
        this.mHwThemeHandler = new HwThemeHandler();
    }

    public void reloadSoundEffects() {
        unloadSoundEffects();
        loadSoundEffects();
    }

    private void onUserUnlocked(int userId) {
        if (userId != -10000) {
            createManagers();
            boolean isPrimary = this.mUserManager.getUserInfo(userId).isPrimary();
            Slog.i(TAG, "user unlocked ,start soundtrigger! isPrimary : " + isPrimary);
            if (isPrimary) {
                createAndStartSoundTrigger(true);
            }
        }
    }

    private void onUserSwitched() {
        int userId = ActivityManager.getCurrentUser();
        if (userId != -10000) {
            createManagers();
            boolean isPrimary = this.mUserManager.getUserInfo(userId).isPrimary();
            Slog.i(TAG, "user switched ,isPrimary : " + isPrimary);
            if (isPrimary) {
                createAndStartSoundTrigger(true);
            } else {
                createAndStartSoundTrigger(DEBUG_THEME_SOUND);
            }
        }
    }

    private boolean isSoundTriggerOn() {
        if (Secure.getInt(this.mContentResolver, "hw_soundtrigger_enabled", SWS_SCENE_AUDIO) == SWS_SCENE_VIDEO) {
            return true;
        }
        return DEBUG_THEME_SOUND;
    }

    private void createAndStartSoundTrigger(boolean start) {
        createSoundTriggerHandler();
        this.mSoundTriggerHandler.post(new AnonymousClass5(start));
    }

    private boolean startSoundTriggerDetector() {
        createManagers();
        createSoundTriggerDetector();
        GenericSoundModel model = getCurrentModel();
        Slog.i(TAG, "startSoundTriggerDetector model : " + model);
        if (model == null || !this.mSoundTriggerDetector.startRecognition(SWS_SCENE_VIDEO)) {
            Slog.e(TAG, "start recognition failed!");
            return DEBUG_THEME_SOUND;
        }
        Slog.i(TAG, "start recognition successfully!");
        return true;
    }

    private boolean stopSoundTriggerDetector() {
        createManagers();
        createSoundTriggerDetector();
        GenericSoundModel model = getCurrentModel();
        Slog.i(TAG, "stopSoundTriggerDetector model : " + model);
        if (model == null || !this.mSoundTriggerDetector.stopRecognition()) {
            Slog.e(TAG, "stop recognition failed!");
            return DEBUG_THEME_SOUND;
        }
        Slog.i(TAG, "stop recognition successfully!");
        return true;
    }

    private void createSoundTriggerHandler() {
        if (this.mSoundTriggerHandler == null) {
            this.mSoundTriggerHandler = new Handler(Looper.getMainLooper());
        }
    }

    private void createManagers() {
        if (this.mSoundTriggerManager == null) {
            this.mSoundTriggerManager = (SoundTriggerManager) this.mContext.getSystemService("soundtrigger");
            this.mUserManager = (UserManager) this.mContext.getSystemService("user");
            this.mSoundTriggerService = Stub.asInterface(ServiceManager.getService("soundtrigger"));
        }
    }

    private void createSoundTriggerDetector() {
        if (this.mSoundTriggerDetector == null) {
            createSoundTriggerHandler();
            this.mSoundTriggerDetector = this.mSoundTriggerManager.createSoundTriggerDetector(MODEL_UUID, this.mCallBack, this.mSoundTriggerHandler);
        }
    }

    private static boolean isSupportWakeUpV2() {
        String wakeupV2 = AudioSystem.getParameters("audio_capability=soundtrigger_version");
        Slog.i(TAG, "wakeupV2 : " + wakeupV2);
        if (AppHibernateCst.INVALID_PKG.equals(wakeupV2)) {
            return DEBUG_THEME_SOUND;
        }
        return true;
    }

    private void startSoundTriggerV2(boolean start) {
        if (isSupportWakeUpV2()) {
            createManagers();
            GenericSoundModel model = getCurrentModel();
            boolean isUserUnlocked = this.mUserManager.isUserUnlocked(UserHandle.SYSTEM);
            boolean isSoundTriggerOn = isSoundTriggerOn();
            Slog.i(TAG, "startSoundTriggerV2 model : " + model + " isSoundTriggerOn : " + isSoundTriggerOn + " isUserUnlocked : " + isUserUnlocked + " start : " + start);
            if (isUserUnlocked && model != null && isSoundTriggerOn) {
                if (start) {
                    startSoundTriggerDetector();
                } else {
                    stopSoundTriggerDetector();
                }
            }
            return;
        }
        Slog.i(TAG, "startSoundTriggerV2 not support wakeup v2");
    }

    private GenericSoundModel getCurrentModel() {
        try {
            if (this.mSoundTriggerService != null) {
                return this.mSoundTriggerService.getSoundModel(new ParcelUuid(MODEL_UUID));
            }
            Slog.e(TAG, "getCurrentModel mSoundTriggerService is null!");
            return null;
        } catch (Exception e) {
            Slog.e(TAG, "getCurrentModel e : " + e);
        }
    }

    private void startWakeupService(int session) {
        Intent intent = new Intent(ACTION_WAKEUP_SERVICE);
        intent.setPackage(VASSISTANT_PACKAGE_NAME);
        intent.putExtra(SESSION_ID, session);
        this.mContext.startService(intent);
    }

    public AudioRoutesInfo startWatchingRoutes(IAudioRoutesObserver observer) {
        Slog.d(TAG, "startWatchingRoutes");
        if (linkVAtoDeathRe(observer)) {
            return null;
        }
        return super.startWatchingRoutes(observer);
    }

    private boolean linkVAtoDeathRe(IAudioRoutesObserver observer) {
        Slog.d(TAG, "linkVAtoDeathRe for vassistant");
        int uid = Binder.getCallingUid();
        if (this.mContext == null) {
            return DEBUG_THEME_SOUND;
        }
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null) {
            return DEBUG_THEME_SOUND;
        }
        String[] packages = pm.getPackagesForUid(uid);
        if (packages == null) {
            return DEBUG_THEME_SOUND;
        }
        int i = SWS_SCENE_AUDIO;
        while (i < packages.length) {
            String packageName = packages[i];
            Slog.d(TAG, "packageName:" + packageName);
            if (VASSISTANT_PACKAGE_NAME.equals(packageName)) {
                this.mBinder = observer.asBinder();
                try {
                    this.mBinder.linkToDeath(this.mDeathRecipient, SWS_SCENE_AUDIO);
                    this.mIsLinkDeathRecipient = true;
                    return true;
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
                i += SWS_SCENE_VIDEO;
            }
        }
        return DEBUG_THEME_SOUND;
    }

    public boolean isFileReady(String filename) {
        return new File(filename).canRead();
    }

    public void unloadHwThemeSoundEffects() {
        this.mHwThemeHandler.removeMessages(MSG_RELOAD_SNDEFFS);
    }

    public int getSampleId(SoundPool soundpool, int effect, String defFilePath, int index) {
        int sampleId = SWS_SCENE_AUDIO;
        if (effect == 0) {
            String themeFilePath = this.mSysKeyEffectFile;
            if (themeFilePath == null) {
                themeFilePath = defFilePath;
            }
            if (themeFilePath == null) {
                return -1;
            }
            if (isFileReady(themeFilePath)) {
                sampleId = soundpool.load(themeFilePath, index);
                if (sampleId > 0) {
                    Systemex.putString(this.mContentResolver, SYSKEY_SOUND_HWT, themeFilePath);
                }
            }
        } else {
            sampleId = soundpool.load(defFilePath, index);
        }
        return sampleId;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        int _result;
        switch (code) {
            case WifiProCommonDefs.TYEP_HAS_INTERNET /*101*/:
                int event = data.readInt();
                if (DEBUG) {
                    Slog.v(TAG, "HwAudioService.onTransact: got event " + event);
                }
                int mForcedUseForMedia = event == SWS_SCENE_VIDEO ? SWS_SCENE_VIDEO : SWS_SCENE_AUDIO;
                sendMsg(this.mAudioHandler, 8, SOUND_EFFECT_OPEN, SWS_SCENE_VIDEO, mForcedUseForMedia, null, SWS_SCENE_AUDIO);
                if (DEBUG) {
                    Slog.v(TAG, "setSpeakermediaOn " + mForcedUseForMedia);
                }
                reply.writeNoException();
                return true;
            case WifiProCommonUtils.HISTORY_TYPE_PORTAL /*102*/:
                reply.writeNoException();
                return true;
            case EventTracker.TRACK_TYPE_KILL /*1002*/:
                data.enforceInterface("android.media.IAudioService");
                _result = setSoundEffectState(data.readInt() != 0 ? true : DEBUG_THEME_SOUND, data.readString(), data.readInt() != 0 ? true : DEBUG_THEME_SOUND, data.readString());
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            case EventTracker.TRACK_TYPE_TRIG /*1003*/:
                data.enforceInterface("android.media.IAudioService");
                sendAudioRecordStateChangedIntent(data.readString(), data.readInt(), data.readInt(), data.readString());
                reply.writeNoException();
                return true;
            case EventTracker.TRACK_TYPE_END /*1004*/:
                data.enforceInterface("android.media.IAudioService");
                if (!checkRecordActive(Binder.getCallingPid())) {
                    Slog.i(TAG, "AudioException record is not occpied.check mic mute.");
                    String recordPkg = getPackageNameByPid(Binder.getCallingPid());
                    printCtaifsLog(getRecordAppName(this.mContext, recordPkg), recordPkg, "onTransact", "\u672c\u5730\u5f55\u97f3");
                    checkMicMute();
                }
                reply.writeNoException();
                return true;
            case 1101:
                _result = isAdjustVolumeEnable() ? SWS_SCENE_VIDEO : SWS_SCENE_AUDIO;
                Slog.i(TAG, "isAdjustVolumeEnable transaction called. result:" + _result);
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            case 1102:
                IBinder callerToken = data.readStrongBinder();
                boolean _arg0 = data.readInt() != 0 ? true : DEBUG_THEME_SOUND;
                Slog.i(TAG, "enableVolumeAdjust  transaction called.enable:" + _arg0);
                enableVolumeAdjust(_arg0, Binder.getCallingUid(), callerToken);
                reply.writeNoException();
                return true;
            case 1103:
                boolean _result2;
                data.enforceInterface("android.media.IAudioService");
                if (data.readInt() != 0 ? true : DEBUG_THEME_SOUND) {
                    _result2 = startSoundTriggerDetector();
                } else {
                    _result2 = stopSoundTriggerDetector();
                }
                reply.writeNoException();
                reply.writeInt(_result2 ? SWS_SCENE_VIDEO : SWS_SCENE_AUDIO);
                if (this.mBinder != null && this.mIsLinkDeathRecipient) {
                    this.mBinder.unlinkToDeath(this.mDeathRecipient, SWS_SCENE_AUDIO);
                    this.mIsLinkDeathRecipient = DEBUG_THEME_SOUND;
                }
                return true;
            default:
                return super.onTransact(code, data, reply, flags);
        }
    }

    public static void printCtaifsLog(String applicationName, String packageName, String callingMethod, String desciption) {
        Slog.i("ctaifs<" + applicationName + ">[" + applicationName + "][" + packageName + "]", "[" + callingMethod + "] " + desciption);
    }

    public static String getRecordAppName(Context context, String packageName) {
        if (context == null) {
            return null;
        }
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            return null;
        }
        ApplicationInfo appInfo = null;
        long ident = Binder.clearCallingIdentity();
        try {
            appInfo = pm.getApplicationInfoAsUser(packageName, SWS_SCENE_AUDIO, getCurrentUserId());
        } catch (NameNotFoundException e) {
            Slog.e(TAG, "App Name Not Found");
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
        if (appInfo == null) {
            return null;
        }
        CharSequence displayName = pm.getApplicationLabel(appInfo);
        if (TextUtils.isEmpty(displayName)) {
            return null;
        }
        return String.valueOf(displayName);
    }

    protected void appendExtraInfo(Intent intent) {
        if (DEBUG) {
            Slog.d(TAG, "appendExtraInfo mHeadsetSwitchState: " + this.mHeadsetSwitchState);
        }
        if (intent != null) {
            intent.putExtra("switch_state", this.mHeadsetSwitchState);
        }
    }

    protected void sendDeviceConnectionIntentForImcs(int device, int state, String name) {
        Intent intent = new Intent();
        intent.putExtra("state", state);
        intent.putExtra(MemoryConstant.MEM_POLICY_ACTIONNAME, name);
        if (DEBUG) {
            Slog.d(TAG, "sendDeviceConnectionIntentForImcs mHeadsetSwitchState: " + this.mHeadsetSwitchState);
        }
        intent.putExtra("switch_state", this.mHeadsetSwitchState);
        if (device == 4) {
            intent.setAction("imcs.action.HEADSET_PLUG");
            intent.putExtra("microphone", SWS_SCENE_VIDEO);
        } else if (device == 8) {
            intent.setAction("imcs.action.HEADSET_PLUG");
            intent.putExtra("microphone", SWS_SCENE_AUDIO);
        } else {
            return;
        }
        long ident = Binder.clearCallingIdentity();
        try {
            ActivityManagerNative.broadcastStickyIntent(intent, "imcs.permission.HEADSET_PLUG", -1);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private boolean isAdjustVolumeEnable() {
        Slog.i(TAG, "isAdjustVolumeEnable,mEnableAdjustVolume:" + this.mEnableAdjustVolume);
        return this.mEnableAdjustVolume;
    }

    private void enableVolumeAdjust(boolean enable, int callerUid, IBinder cb) {
        if (hasSystemPriv(callerUid)) {
            synchronized (this.mEnableVolumeLock) {
                String callerPkg = getPackageNameByPid(Binder.getCallingPid());
                if (this.mEnableVolumeClient == null) {
                    this.mEnableVolumeClient = new EnableVolumeClient(cb);
                    this.mEnableAdjustVolume = enable;
                } else {
                    String clientPkg = this.mEnableVolumeClient.getCallerPkg();
                    if (callerPkg.equals(clientPkg)) {
                        this.mEnableAdjustVolume = enable;
                    } else {
                        Slog.w(TAG, "Just allowed one caller use the interface until older caller die.older. caller:" + callerPkg + " old callser:" + clientPkg);
                    }
                }
            }
            return;
        }
        Slog.i(TAG, "caller is not system app.Can not set volumeAdjust to enable or disable.");
    }

    protected void readPersistedSettingsEx(ContentResolver cr) {
        if (SOUND_EFFECTS_SUPPORT) {
            getEffectsState(cr);
        }
        if (HW_SOUND_TRIGGER_SUPPORT) {
            getSoundTriggerSettings(cr);
        }
        if (SPK_RCV_STEREO_SUPPORT) {
            getSpkRcvStereoState(cr);
        }
    }

    protected void onErrorCallBackEx(int exceptionId) {
        Slog.i(TAG, "AudioException onErrorCallBackEx exceptionId:" + exceptionId);
        if (exceptionId <= HW_AUDIO_EXCEPTION_OCCOUR) {
            sendMsg(this.mAudioHandler, MSG_AUDIO_EXCEPTION_OCCUR, SWS_SCENE_VIDEO, exceptionId, SWS_SCENE_AUDIO, null, SWS_SCENE_AUDIO);
        }
    }

    protected void onScoExceptionOccur(int clientPid) {
        Slog.w(TAG, "AudioException ScoExceptionOccur,clientpid:" + clientPid + " have more than one sco connected!");
        sendMsg(this.mAudioHandler, MSG_AUDIO_EXCEPTION_OCCUR, SWS_SCENE_VIDEO, HW_SCO_CONNECT_EXCEPTION_OCCOUR, SWS_SCENE_AUDIO, new AudioExceptionMsg(HW_SCO_CONNECT_EXCEPTION_OCCOUR, getPackageNameByPid(clientPid)), SWS_SCENE_AUDIO);
    }

    public void setMicrophoneMute(boolean on, String callingPackage, int userId) {
        boolean firstState = AudioSystem.isMicrophoneMuted();
        super.setMicrophoneMute(on, callingPackage, userId);
        if (!firstState && AudioSystem.isMicrophoneMuted()) {
            this.mAudioExceptionRecord.updateMuteMsg(callingPackage);
        }
    }

    protected void handleMessageEx(Message msg) {
        AudioExceptionMsg audioExceptionMsg = null;
        switch (msg.what) {
            case MSG_RECORD_ACTIVE /*61*/:
                if (!this.isShowingDialog) {
                    showRecordWarnDialog(msg.arg1, msg.arg2);
                    break;
                } else {
                    Slog.i(TAG, "MSG_RECORD_ACTIVE should not show record warn dialog");
                }
            case MSG_DUALPA_RCV_DEALY /*71*/:
                AudioSystem.setParameters(DUALPA_RCV_DEALY_OFF);
                break;
            case MSG_RINGER_MODE_CHANGE /*80*/:
                if (msg.obj != null) {
                    AudioExceptionMsg tempMsg = msg.obj;
                    String caller = tempMsg.msgPackagename;
                    if (caller == null) {
                        caller = AppHibernateCst.INVALID_PKG;
                    }
                    int fromRingerMode = msg.arg1;
                    int ringerMode = msg.arg2;
                    if (isFirstTimeShow(caller)) {
                        Slog.i(TAG, "AudioException showRingerModeDialog");
                        showRingerModeDialog(caller, fromRingerMode, ringerMode);
                        savePkgNameToDB(caller);
                    } else {
                        Slog.i(TAG, "AudioException showRingerModeToast");
                        showRingerModeToast(caller, fromRingerMode, ringerMode);
                    }
                    onAudioException(HW_RING_MODE_TYPE_EXCEPTION_OCCOUR, tempMsg);
                    break;
                }
                Slog.e(TAG, "MSG_RINGER_MODE_CHANGE msg obj is null!");
            case MSG_AUDIO_EXCEPTION_OCCUR /*10000*/:
                int i = msg.arg1;
                if (msg.obj != null) {
                    audioExceptionMsg = (AudioExceptionMsg) msg.obj;
                }
                onAudioException(i, audioExceptionMsg);
                break;
            case 10001:
                if (LOUD_VOICE_MODE_SUPPORT) {
                    handleLVMModeChangeProcess(msg.arg1, msg.obj);
                    break;
                }
                break;
        }
    }

    public void onAudioException(int exceptionId, AudioExceptionMsg exceptionMsg) {
        if (!this.mSystemReady) {
            Slog.e(TAG, "AudioException,but system is not ready! ");
        }
        switch (exceptionId) {
            case HW_SCO_CONNECT_EXCEPTION_OCCOUR /*-2003*/:
                Slog.w(TAG, "AudioException HW_SCO_CONNECT_EXCEPTION_OCCOUR");
                sendAudioException(exceptionMsg);
            case HW_MIC_MUTE_EXCEPTION_OCCOUR /*-2002*/:
                Slog.w(TAG, "AudioException HW_MIC_MUTE_EXCEPTION_OCCOUR");
                sendAudioException(exceptionMsg);
                AudioSystem.muteMicrophone(DEBUG_THEME_SOUND);
            case HW_RING_MODE_TYPE_EXCEPTION_OCCOUR /*-2001*/:
                Slog.w(TAG, "AudioException HW_RING_MODE_TYPE_EXCEPTION_OCCOUR");
                sendAudioException(exceptionMsg);
            case HW_AUDIO_MODE_TYPE_EXCEPTION_OCCOUR /*-1001*/:
                Slog.w(TAG, "AudioException HW_AUDIO_MODE_TYPE_EXCEPTION_OCCOUR, setModeInt AudioSystem.MODE_NORMAL");
                int pid = -1;
                if (getMode() == SWS_ON) {
                    if (!this.mSetModeDeathHandlers.isEmpty()) {
                        SetModeDeathHandler hdlr = (SetModeDeathHandler) this.mSetModeDeathHandlers.get(SWS_SCENE_AUDIO);
                        if (hdlr.getMode() == SWS_ON) {
                            pid = hdlr.getPid();
                        }
                    }
                    if (pid > 0) {
                        sendAudioException(new AudioExceptionMsg(HW_AUDIO_MODE_TYPE_EXCEPTION_OCCOUR, getPackageNameByPid(pid)));
                        return;
                    } else {
                        Slog.e(TAG, "AudioException top sedModeDeathHander's mode is not COMMUNICATION");
                        return;
                    }
                }
                Slog.e(TAG, "AudioException catched HW_AUDIO_MODE_TYPE_EXCEPTION_OCCOUR,but state is not MODE_IN_COMMUNICATION.");
            default:
                int tmp = -exceptionId;
                if ((-(tmp >> PID_BIT_WIDE)) == HW_AUDIO_TRACK_OVERFLOW_TYPE_EXCEPTION_OCCOUR) {
                    int exceptionPid = tmp - 2101346304;
                    if (exceptionPid > 0) {
                        String packageName = getPackageNameByPid(exceptionPid);
                        if (packageName != null) {
                            Slog.e(TAG, "AudioTrack_Overflow packageName = " + packageName + " pid = " + exceptionPid);
                            sendAudioException(new AudioExceptionMsg(HW_AUDIO_TRACK_OVERFLOW_TYPE_EXCEPTION_OCCOUR, packageName));
                            return;
                        }
                        Slog.w(TAG, "AudioTrack_Overflow getPackageNameByPid failed");
                        return;
                    }
                    Slog.w(TAG, "AudioTrack_Overflow pid error");
                    return;
                }
                Slog.w(TAG, "No such AudioException exceptionId:" + exceptionId);
        }
    }

    private void sendAudioException(AudioExceptionMsg exceptionMsg) {
        if (exceptionMsg == null) {
            try {
                Slog.e(TAG, "exceptionMsg is null.");
            } catch (Exception e) {
                Slog.e(TAG, "sendAudioException error!");
                e.printStackTrace();
            }
        } else {
            EventStream eventAudioException = IMonitor.openEventStream(AudioConst.EID_AUDIOEXCEPTION);
            if (eventAudioException == null) {
                Slog.e(TAG, "eventAudioException is null.");
                return;
            }
            eventAudioException.setParam((short) 0, exceptionMsg.msgType).setParam((short) 1, exceptionMsg.msgPackagename == null ? AppHibernateCst.INVALID_PKG : exceptionMsg.msgPackagename).setParam((short) 2, AppHibernateCst.INVALID_PKG);
            IMonitor.sendEvent(eventAudioException);
            IMonitor.closeEventStream(eventAudioException);
            Slog.d(TAG, "IMonitor send AudioException success in " + exceptionMsg.msgType);
        }
    }

    protected void handleLVMModeChangeProcess(int state, Object object) {
        if (state == SWS_SCENE_VIDEO) {
            if (object != null) {
                DeviceVolumeState myObj = (DeviceVolumeState) object;
                setLVMMode(myObj.mDirection, myObj.mDevice, myObj.mOldIndex, myObj.mstreamType);
            }
        } else if (isInCall()) {
            int device = getInCallDevice();
            setLVMMode(SWS_SCENE_AUDIO, device, -1, SWS_SCENE_AUDIO);
            setVoiceALGODeviceChange(device);
        }
    }

    private boolean isInCall() {
        if (getMode() != SOUND_EFFECT_OPEN) {
            return DEBUG_THEME_SOUND;
        }
        TelecomManager telecomManager = (TelecomManager) this.mContext.getSystemService("telecom");
        if (telecomManager == null) {
            return DEBUG_THEME_SOUND;
        }
        return telecomManager.isInCall();
    }

    protected int getOldInCallDevice(int mode) {
        if (mode == SOUND_EFFECT_OPEN) {
            this.oldInCallDevice = getInCallDevice();
        } else if (mode == 0) {
            this.oldInCallDevice = SWS_SCENE_AUDIO;
        }
        return this.oldInCallDevice;
    }

    private int getInCallDevice() {
        int activeStreamType;
        if (AudioSystem.getForceUse(SWS_SCENE_AUDIO) == SWS_ON) {
            activeStreamType = 6;
        } else {
            activeStreamType = SWS_SCENE_AUDIO;
        }
        return hwGetDeviceForStream(activeStreamType);
    }

    private void setVoiceALGODeviceChange(int device) {
        if (this.oldInCallDevice != device) {
            AudioSystem.setParameters("VOICE_ALGO_DeviceChange=true");
            this.oldInCallDevice = device;
        }
    }

    private void force_exitLVMMode() {
        if ("true".equals(AudioSystem.getParameters("VOICE_LVM_Enable"))) {
            AudioSystem.setParameters("VOICE_LVM_Enable=false");
            this.oldLVMDevice = SWS_SCENE_AUDIO;
            if (DEBUG) {
                Slog.i(TAG, "force disable LVM");
            }
        }
    }

    protected void setLVMMode(int direction, int device, int oldIndex, int streamType) {
        if (getMode() == SOUND_EFFECT_OPEN && this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE") != 0) {
            Slog.e(TAG, "MODIFY_PHONE_STATE Permission Denial: setLVMMode");
        } else if (LOUD_VOICE_MODE_SUPPORT && isInCall() && streamType == 0) {
            boolean isMaxVolume;
            boolean isLVMMode = "true".equals(AudioSystem.getParameters("VOICE_LVM_Enable"));
            boolean isLVMDevice = (SOUND_EFFECT_OPEN == device || SWS_SCENE_VIDEO == device) ? true : DEBUG_THEME_SOUND;
            if (getStreamMaxIndex(SWS_SCENE_AUDIO) == oldIndex) {
                isMaxVolume = true;
            } else {
                isMaxVolume = DEBUG_THEME_SOUND;
            }
            boolean isChangeVolume = DEBUG_THEME_SOUND;
            if (isMaxVolume && isLVMDevice && this.oldLVMDevice == device) {
                isChangeVolume = getStreamIndex(SWS_SCENE_AUDIO, device) < oldIndex ? true : DEBUG_THEME_SOUND;
            }
            if (DEBUG) {
                Slog.i(TAG, "direction:" + direction + " device:" + device + " oldIndex:" + oldIndex + " isLVMMode:" + isLVMMode + " isChangeVolume:" + isChangeVolume);
            }
            if (!isLVMMode && isMaxVolume && SWS_SCENE_VIDEO == direction && isLVMDevice) {
                AudioSystem.setParameters("VOICE_LVM_Enable=true");
                showLVMToast(33685769);
                this.oldLVMDevice = device;
                if (DEBUG) {
                    Slog.i(TAG, "enable LVM after  = " + this.oldLVMDevice);
                }
            } else if (isLVMMode) {
                if (-1 != direction && device == this.oldLVMDevice) {
                    if (isChangeVolume) {
                    }
                }
                AudioSystem.setParameters("VOICE_LVM_Enable=false");
                showLVMToast(33685770);
                this.oldLVMDevice = SWS_SCENE_AUDIO;
                if (DEBUG) {
                    Slog.i(TAG, "disable LVM after oldLVMDevice= " + this.oldLVMDevice);
                }
            }
        } else {
            Slog.e(TAG, "setLVMMode abort");
        }
    }

    private void showLVMToast(int message) {
        if (this.mLVMToast == null) {
            this.mLVMToast = Toast.makeText(new ContextThemeWrapper(this.mContext, this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Toast", null, null)), "Unknown State", SWS_SCENE_AUDIO);
            LayoutParams windowParams = this.mLVMToast.getWindowParams();
            windowParams.privateFlags |= 16;
        }
        try {
            if (DEBUG) {
                Slog.i(TAG, "showLVMToast ");
            }
            this.mLVMToast.setText(message);
            this.mLVMToast.show();
        } catch (Exception e) {
            Slog.e(TAG, "showLVMToast exception: " + e);
        }
    }

    protected void getSoundTriggerSettings(ContentResolver cr) {
        String KEY_SOUNDTRIGGER_TYPE = "hw_soundtrigger_type";
        this.mSoundTriggerStatus = Secure.getInt(cr, "hw_soundtrigger_enabled", SWS_SCENE_AUDIO);
        this.mSoundTriggerRes = Secure.getString(cr, "hw_soundtrigger_resource");
        this.mSoundTriggerGram = Secure.getString(cr, "hw_soundtrigger_grammar");
        if (DEBUG) {
            Slog.i(TAG, "mSoundTriggerStatus = " + this.mSoundTriggerStatus + " mSoundTriggerRes = " + this.mSoundTriggerRes + " mSoundTriggerGram = " + this.mSoundTriggerGram);
        }
    }

    private void setSoundTrigger() {
        if (this.mSoundTriggerStatus == SWS_SCENE_VIDEO) {
            AudioSystem.setParameters(SOUNDTRIGGER_MAD_ON);
            AudioSystem.setParameters(SOUNDTRIGGER_WAKUP_ON);
            Slog.i(TAG, "setSoundTrigger = on");
        } else if (this.mSoundTriggerStatus == 0) {
            AudioSystem.setParameters(SOUNDTRIGGER_MAD_OFF);
            AudioSystem.setParameters(SOUNDTRIGGER_WAKUP_OFF);
            Slog.i(TAG, "setSoundTrigger = off");
            return;
        }
        if (this.mSoundTriggerRes != null && !this.mSoundTriggerRes.isEmpty() && this.mSoundTriggerGram != null && !this.mSoundTriggerGram.isEmpty()) {
            AudioSystem.setParameters("mad=tslice_model=" + this.mSoundTriggerRes);
            AudioSystem.setParameters("mad=tslice_gram=" + this.mSoundTriggerGram);
        }
    }

    private boolean isInCallingState() {
        TelephonyManager telephonyManager = null;
        if (this.mContext != null) {
            telephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        }
        if (telephonyManager == null) {
            return DEBUG_THEME_SOUND;
        }
        if (telephonyManager.getCallState() != 0) {
            if (DEBUG) {
                Slog.v(TAG, "phone is in call state");
            }
            return true;
        }
        if (DEBUG) {
            Slog.v(TAG, "phone is NOT in call state");
        }
        return DEBUG_THEME_SOUND;
    }

    private boolean hasSystemPriv(int uid) {
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null) {
            return DEBUG_THEME_SOUND;
        }
        String[] pkgs = pm.getPackagesForUid(uid);
        int length = pkgs.length;
        for (int i = SWS_SCENE_AUDIO; i < length; i += SWS_SCENE_VIDEO) {
            String pkg = pkgs[i];
            try {
                ApplicationInfo info = pm.getApplicationInfo(pkg, SWS_SCENE_AUDIO);
                if (!(info == null || (info.flags & SWS_SCENE_VIDEO) == 0)) {
                    Slog.i(TAG, "system app " + pkg);
                    return true;
                }
            } catch (Exception e) {
                Slog.i(TAG, "not found app " + pkg);
            }
        }
        return DEBUG_THEME_SOUND;
    }

    protected boolean checkAudioSettingAllowed(String msg) {
        if (isInCallingState()) {
            int uid = Binder.getCallingUid();
            if (RADIO_UID == uid || SYSTEM_UID == uid || hasSystemPriv(uid)) {
                if (DEBUG) {
                    Slog.v(TAG, "Audio Settings ALLOW from func=" + msg + ", from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                }
                return true;
            }
            if (DEBUG) {
                Slog.v(TAG, "Audio Settings NOT allow from func=" + msg + ", from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            }
            return DEBUG_THEME_SOUND;
        }
        Slog.w(TAG, "Audio Settings ALLOW from func=" + msg + ", from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
        return true;
    }

    protected String getPackageNameByPid(int pid) {
        if (pid <= 0) {
            return null;
        }
        ActivityManager activityManager = (ActivityManager) this.mContext.getSystemService("activity");
        if (activityManager == null) {
            return null;
        }
        List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return null;
        }
        String packageName = null;
        for (RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.pid == pid) {
                packageName = appProcess.processName;
                break;
            }
        }
        return packageName;
    }

    public void sendAudioRecordStateChangedIntent(String sender, int state, int pid, String packageName) {
        if (!HW_KARAOKE_EFFECT_ENABLED) {
            return;
        }
        if (this.mContext.checkCallingPermission("android.permission.RECORD_AUDIO") == -1) {
            Slog.e(TAG, "sendAudioRecordStateChangedIntent dennied from " + packageName);
            return;
        }
        if (DEBUG) {
            Slog.i(TAG, "sendAudioRecordStateChangedIntent=" + sender + " " + state + " " + pid);
        }
        Intent intent = new Intent("android.media.AUDIO_RECORD_STATE_CHANGED_ACTION");
        intent.putExtra("sender", sender);
        intent.putExtra("state", state);
        intent.putExtra("packagename", getPackageNameByPid(pid));
        long ident = Binder.clearCallingIdentity();
        try {
            ActivityManagerNative.getDefault().broadcastIntent(null, intent, null, null, -1, null, null, null, -1, null, DEBUG_THEME_SOUND, DEBUG_THEME_SOUND, -1);
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private String getPackageNameByPidEx(int pid) {
        String str = null;
        try {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("android.app.IActivityManager");
            data.writeInt(pid);
            ActivityManagerNative.getDefault().asBinder().transact(504, data, reply, SWS_SCENE_AUDIO);
            reply.readException();
            str = reply.readString();
            data.recycle();
            reply.recycle();
            return str;
        } catch (Exception e) {
            Slog.e(TAG, "getPackageNameForPid " + pid, e);
            return str;
        }
    }

    private boolean checkRecordActive(int requestRecordPid) {
        if (TextUtils.isEmpty(AudioSystem.getParameters("active_record_pid"))) {
            return DEBUG_THEME_SOUND;
        }
        boolean isRecordOccupied;
        int activeRecordPid = Integer.parseInt(AudioSystem.getParameters("active_record_pid"));
        boolean isSourceActive = !AudioSystem.isSourceActive(1999) ? AudioSystem.isSourceActive(8) : true;
        Slog.i(TAG, "AudioException checkRecordActive requestRecordPid = " + requestRecordPid + ", activeRecordPid = " + activeRecordPid);
        if (activeRecordPid == -1 || requestRecordPid == activeRecordPid || isSourceActive) {
            isRecordOccupied = DEBUG_THEME_SOUND;
        } else {
            isRecordOccupied = true;
        }
        String requestPkgName = getPackageNameByPid(requestRecordPid);
        if (isRecordOccupied) {
            if (isInRequestAppList(requestPkgName) || isInActiveAppList(getPackageNameByPidEx(activeRecordPid))) {
                return isRecordOccupied;
            }
            sendMsg(this.mAudioHandler, MSG_RECORD_ACTIVE, SOUND_EFFECT_OPEN, requestRecordPid, activeRecordPid, null, SWS_SCENE_AUDIO);
        } else if (!(requestPkgName == null || requestPkgName.equals(AppHibernateCst.INVALID_PKG))) {
            AudioSystem.setParameters("RecordCallingAppName=" + requestPkgName);
        }
        return isRecordOccupied;
    }

    private void checkMicMute() {
        if (AudioSystem.isMicrophoneMuted()) {
            Slog.i(TAG, "AudioException mic is muted when record! set it not mute!");
            sendMsg(this.mAudioHandler, MSG_AUDIO_EXCEPTION_OCCUR, SWS_SCENE_VIDEO, HW_MIC_MUTE_EXCEPTION_OCCOUR, SWS_SCENE_AUDIO, new AudioExceptionMsg(HW_MIC_MUTE_EXCEPTION_OCCOUR, this.mAudioExceptionRecord.mLastMutePackageName), HwActivityManagerService.SERVICE_ADJ);
        }
    }

    private boolean isInRequestAppList(String pkgName) {
        int i = SWS_SCENE_AUDIO;
        while (i < RECORD_REQUEST_APP_LIST.length) {
            if (pkgName != null && pkgName.contains(RECORD_REQUEST_APP_LIST[i])) {
                return true;
            }
            i += SWS_SCENE_VIDEO;
        }
        return DEBUG_THEME_SOUND;
    }

    private boolean isInActiveAppList(String pkgName) {
        for (int i = SWS_SCENE_AUDIO; i < RECORD_ACTIVE_APP_LIST.length; i += SWS_SCENE_VIDEO) {
            if (RECORD_ACTIVE_APP_LIST[i].equals(pkgName)) {
                Slog.i(TAG, "isInActiveAppList " + pkgName);
                return true;
            }
        }
        return DEBUG_THEME_SOUND;
    }

    private String getApplicationLabel(String pkgName) {
        String label = null;
        try {
            PackageManager packageManager = this.mContext.getPackageManager();
            label = packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkgName, SWS_SCENE_AUDIO)).toString();
        } catch (NameNotFoundException e) {
            Slog.e(TAG, "getApplicationLabel exception", e);
        }
        return label;
    }

    private void showRecordWarnDialog(int requestRecordPid, int activeRecordPid) {
        String requestPkgName = getPackageNameByPid(requestRecordPid);
        String requestPkgLabel = getApplicationLabel(requestPkgName);
        String activePkgName = getPackageNameByPid(activeRecordPid);
        String activePkgLabel = getApplicationLabel(activePkgName);
        if (activePkgName == null) {
            activePkgName = this.mContext.getString(17040608);
        }
        Builder builder = new Builder(this.mContext, this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null));
        String string = this.mContext.getString(33685811);
        Object[] objArr = new Object[SOUND_EFFECT_OPEN];
        if (activePkgLabel == null) {
            activePkgLabel = activePkgName;
        }
        objArr[SWS_SCENE_AUDIO] = activePkgLabel;
        if (requestPkgLabel == null) {
            requestPkgLabel = requestPkgName;
        }
        objArr[SWS_SCENE_VIDEO] = requestPkgLabel;
        builder.setMessage(String.format(string, objArr));
        builder.setCancelable(DEBUG_THEME_SOUND);
        builder.setPositiveButton(33685810, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
                HwAudioService.this.isShowingDialog = HwAudioService.DEBUG_THEME_SOUND;
            }
        });
        Dialog dialog = builder.create();
        dialog.getWindow().setType(2003);
        if (SystemClock.elapsedRealtime() - this.dialogShowTime > THREE_SEC_LIMITED || SystemClock.elapsedRealtime() - this.dialogShowTime < 0) {
            dialog.show();
            this.dialogShowTime = SystemClock.elapsedRealtime();
            this.isShowingDialog = true;
        }
    }

    protected void getEffectsState(ContentResolver contentResolver) {
        if (DTS_SOUND_EFFECTS_SUPPORT) {
            getDtsstatus(contentResolver);
        }
        if (SWS_SOUND_EFFECTS_SUPPORT) {
            getSwsstatus(contentResolver);
        }
    }

    private void setEffectsState() {
        if (DTS_SOUND_EFFECTS_SUPPORT) {
            setDtsStatus();
        }
        if (SWS_SOUND_EFFECTS_SUPPORT) {
            setSwsStatus();
        }
    }

    private void getDtsstatus(ContentResolver contentResolver) {
        this.mDtsStatus = Secure.getInt(contentResolver, "dts_mode", SWS_SCENE_AUDIO);
    }

    private void getSwsstatus(ContentResolver contentResolver) {
        this.mSwsStatus = System.getInt(contentResolver, "sws_mode", SWS_SCENE_AUDIO);
    }

    private void setDtsStatus() {
        if (this.mDtsStatus == SWS_ON) {
            AudioSystem.setParameters(DTS_MODE_ON_PARA);
            Slog.i(TAG, "setDtsStatus = on");
        } else if (this.mDtsStatus == 0) {
            AudioSystem.setParameters(DTS_MODE_OFF_PARA);
            Slog.i(TAG, "setDtsStatus = off");
        }
    }

    private void setSwsStatus() {
        if (this.mSwsStatus == SWS_ON) {
            AudioSystem.setParameters(SWS_MODE_ON_PARA);
        } else if (this.mSwsStatus == 0) {
            AudioSystem.setParameters(SWS_MODE_OFF_PARA);
        }
    }

    protected void onSetSoundEffectState(int device, int state) {
        if (!checkAudioSettingsPermission("onSetSoundEffectState()") || !SOUND_EFFECTS_SUPPORT || isScreenOff() || isKeyguardLocked()) {
            return;
        }
        if ((device == 4 || device == 8) && isTopActivity(FORBIDDEN_SOUND_EFFECT_WHITE_LIST)) {
            if (DTS_SOUND_EFFECTS_SUPPORT) {
                onSetDtsState(state);
            }
            if (HPX_EFFECTS_SUPPORT) {
                onSetHpxState(state);
            }
        }
    }

    protected boolean checkEnbaleVolumeAdjust() {
        return isAdjustVolumeEnable();
    }

    private boolean isTopActivity(String[] appnames) {
        try {
            List<RunningTaskInfo> tasks = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningTasks(SWS_SCENE_VIDEO);
            if (tasks == null || tasks.isEmpty()) {
                return DEBUG_THEME_SOUND;
            }
            ComponentName topActivity = ((RunningTaskInfo) tasks.get(SWS_SCENE_AUDIO)).topActivity;
            if (topActivity == null || topActivity.getPackageName() == null) {
                return DEBUG_THEME_SOUND;
            }
            if (DEBUG) {
                Slog.i(TAG, "isTOPActivity topActivity.getPackageName()" + topActivity.getPackageName());
            }
            for (int i = SWS_SCENE_AUDIO; i < appnames.length; i += SWS_SCENE_VIDEO) {
                if (appnames[i].equals(topActivity.getPackageName())) {
                    return true;
                }
            }
            return DEBUG_THEME_SOUND;
        } catch (Exception e) {
            Slog.e(TAG, " Failure to get topActivity PackageName " + e);
        }
    }

    private void onSetDtsState(int state) {
        String preDTSState = SystemProperties.get(DTS_MODE_PRESTATE, "unknown");
        if (state == SWS_SCENE_VIDEO) {
            String curDTSState = AudioSystem.getParameters(DTS_MODE_PARA);
            if (curDTSState != null && curDTSState.contains(DTS_MODE_ON_PARA)) {
                AudioSystem.setParameters(DTS_MODE_OFF_PARA);
                SystemProperties.set(DTS_MODE_PRESTATE, PreciseIgnore.COMP_SCREEN_ON_VALUE_);
                if (DEBUG) {
                    Slog.i(TAG, "onSetDTSState cur DTS mode = " + curDTSState + " force set DTS off");
                }
            }
        } else if (state == 0 && preDTSState != null && preDTSState.equals(PreciseIgnore.COMP_SCREEN_ON_VALUE_)) {
            AudioSystem.setParameters(DTS_MODE_ON_PARA);
            SystemProperties.set(DTS_MODE_PRESTATE, "unknown");
            if (DEBUG) {
                Slog.i(TAG, "onSetDTSState set DTS on");
            }
        }
    }

    private void onSetHpxState(int state) {
        String preHPXState = SystemProperties.get(HPX_MODE_PRESTATE, "unknown");
        if (mHpxLiteSdkThin == null) {
            return;
        }
        if (state == SWS_SCENE_VIDEO) {
            int curHpxState = mHpxLiteSdkThin.getHpxEnabled();
            if (curHpxState == SWS_SCENE_VIDEO) {
                mHpxLiteSdkThin.setHpxEnabled(SWS_SCENE_AUDIO);
                SystemProperties.set(HPX_MODE_PRESTATE, PreciseIgnore.COMP_SCREEN_ON_VALUE_);
                if (DEBUG) {
                    Slog.i(TAG, "onSetHpxState cur HPX state = " + curHpxState + " force set HPX off");
                }
            }
        } else if (state == 0 && preHPXState != null && preHPXState.equals(PreciseIgnore.COMP_SCREEN_ON_VALUE_)) {
            mHpxLiteSdkThin.setHpxEnabled(SWS_SCENE_VIDEO);
            SystemProperties.set(HPX_MODE_PRESTATE, "unknown");
            if (DEBUG) {
                Slog.i(TAG, "onSetHpxState set HPX on");
            }
        }
    }

    public int setSoundEffectState(boolean restore, String packageName, boolean isOnTop, String reserved) {
        if (!SOUND_EFFECTS_SUPPORT || !checkAudioSettingsPermission("setSoundEffectState()") || isScreenOff() || isKeyguardLocked()) {
            return -1;
        }
        if (DEBUG) {
            Slog.i(TAG, "restore:" + restore + " packageName:" + packageName + " Top:" + isOnTop);
        }
        if (restore) {
            restoreSoundEffectAndHSState(packageName);
        } else {
            onSetSoundEffectAndHSState(packageName, isOnTop);
        }
        return SWS_SCENE_AUDIO;
    }

    static void onSetSoundEffectAndHSState(String packageName, boolean isOnTOP) {
        for (int i = SWS_SCENE_AUDIO; i < FORBIDDEN_SOUND_EFFECT_WHITE_LIST.length; i += SWS_SCENE_VIDEO) {
            if (FORBIDDEN_SOUND_EFFECT_WHITE_LIST[i].equals(packageName)) {
                AudioSystem.setParameters(isOnTOP ? HS_NO_CHARGE_ON : HS_NO_CHARGE_OFF);
                mMyHandler.sendEmptyMessage(isOnTOP ? SWS_SCENE_VIDEO : SOUND_EFFECT_OPEN);
                if (DEBUG) {
                    Slog.i(TAG, "onSetSoundEffectAndHSState message: " + (isOnTOP ? "HS_NO_CHARGE_ON + SOUND_EFFECT_CLOSE" : "HS_NO_CHARGE_OFF + SOUND_EFFECT_OPEN"));
                }
                return;
            }
        }
    }

    static void restoreSoundEffectAndHSState(String processName) {
        String preDTSState = null;
        String preHPXState = null;
        if (DTS_SOUND_EFFECTS_SUPPORT) {
            preDTSState = SystemProperties.get(DTS_MODE_PRESTATE, "unknown");
        }
        if (HPX_EFFECTS_SUPPORT && mHpxLiteSdkThin != null) {
            preHPXState = SystemProperties.get(HPX_MODE_PRESTATE, "unknown");
        }
        for (int i = SWS_SCENE_AUDIO; i < FORBIDDEN_SOUND_EFFECT_WHITE_LIST.length; i += SWS_SCENE_VIDEO) {
            if (FORBIDDEN_SOUND_EFFECT_WHITE_LIST[i].equals(processName)) {
                AudioSystem.setParameters(HS_NO_CHARGE_OFF);
                if (DTS_SOUND_EFFECTS_SUPPORT && r1 != null && r1.equals(PreciseIgnore.COMP_SCREEN_ON_VALUE_)) {
                    AudioSystem.setParameters(DTS_MODE_ON_PARA);
                    SystemProperties.set(DTS_MODE_PRESTATE, "unknown");
                    if (DEBUG) {
                        Slog.i(TAG, "restoreDTSAndHSState success!");
                    }
                }
                if (HPX_EFFECTS_SUPPORT && mHpxLiteSdkThin != null && r2 != null && r2.equals(PreciseIgnore.COMP_SCREEN_ON_VALUE_)) {
                    mHpxLiteSdkThin.setHpxEnabled(SWS_SCENE_VIDEO);
                    SystemProperties.set(HPX_MODE_PRESTATE, "unknown");
                    if (DEBUG) {
                        Slog.i(TAG, "restoreHPXState success!");
                    }
                }
                return;
            }
        }
    }

    protected void sendCommForceBroadcast() {
        if (getMode() == SOUND_EFFECT_OPEN) {
            int uid = Binder.getCallingUid();
            if (SYSTEM_UID == uid || BLUETOOTH_PKG_NAME.equals(getPackageNameByPid(Binder.getCallingPid()))) {
                try {
                    this.mContext.sendBroadcastAsUser(new Intent(ACTION_COMMFORCE), UserHandle.getUserHandleForUid(UserHandle.getCallingUserId()), PERMISSION_COMMFORCE);
                } catch (Exception e) {
                    Slog.e(TAG, "sendCommForceBroadcast exception: " + e);
                }
                return;
            }
            if (DEBUG) {
                Slog.v(TAG, "Ignore sendCommForceBroadcast from uid = " + uid);
            }
        }
    }

    protected void checkMuteRcvDelay(int curMode, int mode) {
        if (mode == 0 && (curMode == SOUND_EFFECT_OPEN || curMode == SWS_ON)) {
            AudioSystem.setParameters(DUALPA_RCV_DEALY_ON);
            sendMsg(this.mAudioHandler, MSG_DUALPA_RCV_DEALY, SWS_SCENE_AUDIO, SWS_SCENE_AUDIO, SWS_SCENE_AUDIO, null, DUALPA_RCV_DEALY_DURATION);
        } else if (curMode != mode && (mode == SWS_SCENE_VIDEO || mode == SOUND_EFFECT_OPEN || mode == SWS_ON)) {
            AudioSystem.setParameters(DUALPA_RCV_DEALY_OFF);
        } else if (DEBUG) {
            Slog.i(TAG, "checkMuteRcvDelay Do Nothing");
        }
    }

    private void setAudioSystemParameters() {
        if (HW_SOUND_TRIGGER_SUPPORT) {
            setSoundTrigger();
        }
        if (SOUND_EFFECTS_SUPPORT) {
            setEffectsState();
        }
        if (SPK_RCV_STEREO_SUPPORT) {
            setSpkRcvStereoStatus();
        }
    }

    protected void processMediaServerRestart() {
        readPersistedSettingsEx(this.mContentResolver);
        setAudioSystemParameters();
        Slog.i(TAG, "mediaserver restart ,start soundtrigger!");
        createAndStartSoundTrigger(true);
    }

    private void getSpkRcvStereoState(ContentResolver contentResolver) {
        this.mSpkRcvStereoStatus = Secure.getInt(contentResolver, "stereo_landscape_portrait", SWS_SCENE_VIDEO);
    }

    private void setSpkRcvStereoStatus() {
        if (this.mSpkRcvStereoStatus == SWS_SCENE_VIDEO) {
            AudioSystem.setParameters(SPK_RCV_STEREO_ON_PARA);
            AudioSystem.setParameters("rotation=0");
        } else if (this.mSpkRcvStereoStatus == 0) {
            AudioSystem.setParameters(SPK_RCV_STEREO_OFF_PARA);
        } else {
            Slog.e(TAG, "setSpkRcvStereoStatus Fail " + this.mSpkRcvStereoStatus);
        }
    }

    public void setRingerModeExternal(int toRingerMode, String caller) {
        boolean shouldControll = isSystemApp(caller) ? DEBUG_THEME_SOUND : true;
        if (shouldControll) {
            Slog.i(TAG, "AudioException setRingerModeExternal shouldReport=" + checkShouldAbortRingerModeChange(caller) + " uid:" + Binder.getCallingUid() + " caller:" + caller);
        }
        int fromRingerMode = getRingerModeExternal();
        super.setRingerModeExternal(toRingerMode, caller);
        if (fromRingerMode == getRingerModeInternal()) {
            Slog.d(TAG, "AudioException setRingerModeExternal ,but not change");
            return;
        }
        if (shouldControll) {
            alertUserRingerModeChange(caller, fromRingerMode, toRingerMode);
        }
    }

    private void alertUserRingerModeChange(String caller, int fromRingerMode, int ringerMode) {
        sendMsg(this.mAudioHandler, MSG_RINGER_MODE_CHANGE, SOUND_EFFECT_OPEN, fromRingerMode, ringerMode, new AudioExceptionMsg(HW_RING_MODE_TYPE_EXCEPTION_OCCOUR, caller), SWS_SCENE_AUDIO);
    }

    private boolean isFirstTimeShow(String caller) {
        boolean z = true;
        String pkgs = Secure.getStringForUser(this.mContentResolver, "change_ringer_mode_pkgs", -2);
        if (TextUtils.isEmpty(pkgs)) {
            return true;
        }
        if (pkgs.contains(caller)) {
            z = DEBUG_THEME_SOUND;
        }
        return z;
    }

    private void savePkgNameToDB(String caller) {
        if (!TextUtils.isEmpty(caller)) {
            String pkgs = Secure.getStringForUser(this.mContentResolver, "change_ringer_mode_pkgs", -2);
            if (TextUtils.isEmpty(pkgs)) {
                pkgs = caller + "\\";
            } else {
                pkgs = pkgs + caller + "\\";
            }
            Secure.putStringForUser(this.mContentResolver, "change_ringer_mode_pkgs", pkgs, -2);
        }
    }

    private boolean checkShouldAbortRingerModeChange(String caller) {
        return (isScreenOff() || isKeyguardLocked() || !isCallerShowing(caller)) ? true : DEBUG_THEME_SOUND;
    }

    private boolean isKeyguardLocked() {
        KeyguardManager keyguard = (KeyguardManager) this.mContext.getSystemService("keyguard");
        if (keyguard != null) {
            return keyguard.isKeyguardLocked();
        }
        return DEBUG_THEME_SOUND;
    }

    private boolean isCallerShowing(String caller) {
        String pkgName = getTopApp();
        if (pkgName != null) {
            return pkgName.startsWith(caller);
        }
        Slog.e(TAG, "AudioException getTopApp ,but pkgname is null.");
        return DEBUG_THEME_SOUND;
    }

    private String getTopApp() {
        return ((ActivityManagerService) ServiceManager.getService("activity")).topAppName();
    }

    private boolean isScreenOff() {
        boolean z = DEBUG_THEME_SOUND;
        PowerManager power = (PowerManager) this.mContext.getSystemService("power");
        if (power == null) {
            return DEBUG_THEME_SOUND;
        }
        if (!power.isScreenOn()) {
            z = true;
        }
        return z;
    }

    private boolean isSystemApp(String caller) {
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null) {
            return DEBUG_THEME_SOUND;
        }
        try {
            ApplicationInfo info = pm.getApplicationInfo(caller, SWS_SCENE_AUDIO);
            if (!(info == null || (info.flags & SWS_SCENE_VIDEO) == 0)) {
                return true;
            }
        } catch (Exception e) {
            Slog.i(TAG, "AudioException not found app:" + caller);
        }
        return DEBUG_THEME_SOUND;
    }

    private void showRingerModeToast(String caller, int fromRingerMode, int toRingerMode) {
        int fromResId = getRingerModeStrResId(fromRingerMode);
        int toResId = getRingerModeStrResId(toRingerMode);
        if (fromResId <= 0 || toResId <= 0) {
            Slog.e(TAG, "AudioException showRingerModeToast resid not found ");
            return;
        }
        String callerAppName = getApplicationLabel(caller);
        if (this.mRingerModeToast == null) {
            this.mRingerModeToast = Toast.makeText(new ContextThemeWrapper(this.mContext, this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Toast", null, null)), "Unknown State", SWS_SCENE_AUDIO);
        }
        try {
            Context context = this.mContext;
            Object[] objArr = new Object[SWS_ON];
            objArr[SWS_SCENE_AUDIO] = callerAppName;
            objArr[SWS_SCENE_VIDEO] = this.mContext.getString(fromResId);
            objArr[SOUND_EFFECT_OPEN] = this.mContext.getString(toResId);
            String messageStr = context.getString(33685812, objArr);
            Slog.i(TAG, "AudioException showRingerModeToast ");
            this.mRingerModeToast.setText(messageStr);
            this.mRingerModeToast.show();
        } catch (Exception e) {
            Slog.e(TAG, "AudioException showRingerModeToast exception: " + e);
        }
    }

    private void showRingerModeDialog(String caller, int fromRingerMode, int toRingerMode) {
        int fromResId = getRingerModeStrResId(fromRingerMode);
        int toResId = getRingerModeStrResId(toRingerMode);
        if (fromResId <= 0 || toResId <= 0) {
            Slog.e(TAG, "AudioException showRingerModeDialog resid not found ");
            return;
        }
        String callerAppName = getApplicationLabel(caller);
        Context context = this.mContext;
        Object[] objArr = new Object[SWS_ON];
        objArr[SWS_SCENE_AUDIO] = callerAppName;
        objArr[SWS_SCENE_VIDEO] = this.mContext.getString(fromResId);
        objArr[SOUND_EFFECT_OPEN] = this.mContext.getString(toResId);
        String messageStr = context.getString(33685812, objArr);
        Builder builder = new Builder(this.mContext, this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null));
        builder.setMessage(messageStr);
        builder.setCancelable(DEBUG_THEME_SOUND);
        builder.setPositiveButton(33685814, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        Dialog dialog = builder.create();
        dialog.getWindow().setType(2003);
        dialog.show();
        Slog.i(TAG, "AudioException showRingerModeDialog show ");
        this.isShowingDialog = true;
    }

    private int getRingerModeStrResId(int RingerMode) {
        switch (RingerMode) {
            case SWS_SCENE_AUDIO /*0*/:
                return 33685816;
            case SWS_SCENE_VIDEO /*1*/:
                return 33685813;
            case SOUND_EFFECT_OPEN /*2*/:
                return 33685815;
            default:
                Slog.e(TAG, "getRingerModeStrResId RingerMode is error.RingerMode =" + RingerMode);
                return SWS_SCENE_AUDIO;
        }
    }

    private static int getCurrentUserId() {
        int i;
        long ident = Binder.clearCallingIdentity();
        try {
            i = ActivityManagerNative.getDefault().getCurrentUser().id;
            return i;
        } catch (RemoteException e) {
            i = TAG;
            Slog.w(i, "Activity manager not running, nothing we can do assume user 0.");
            return SWS_SCENE_AUDIO;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        super.dump(fd, pw, args);
        this.mContext.enforceCallingOrSelfPermission("android.permission.DUMP", TAG);
        pw.print("HwAudioService:");
        pw.print("DEBUG =");
        pw.println(DEBUG);
        pw.print("mIsHuaweiSafeMediaConfig =");
        pw.println(mIsHuaweiSafeMediaConfig);
        pw.print("mSecurityVolumeIndex =");
        pw.println(mSecurityVolumeIndex);
        pw.print("LOUD_VOICE_MODE_SUPPORT=");
        pw.println(LOUD_VOICE_MODE_SUPPORT);
        pw.print("Lound Voice State:");
        pw.println(AudioSystem.getParameters("VOICE_LVM_Enable"));
        pw.print("HW_SOUND_TRIGGER_SUPPORT=");
        pw.println(HW_SOUND_TRIGGER_SUPPORT);
        pw.print("mSoundTriggerStatus=");
        pw.println(this.mSoundTriggerStatus);
        pw.print("mad=on:");
        pw.println(AudioSystem.getParameters(SOUNDTRIGGER_MAD_ON));
        pw.print("wakeup=on:");
        pw.println(AudioSystem.getParameters(SOUNDTRIGGER_WAKUP_ON));
        pw.print("HW_KARAOKE_EFFECT_ENABLED=");
        pw.println(HW_KARAOKE_EFFECT_ENABLED);
        pw.print("DTS_SOUND_EFFECTS_SUPPORT=");
        pw.println(DTS_SOUND_EFFECTS_SUPPORT);
        pw.print("mDtsStatus=");
        pw.println(this.mDtsStatus);
        pw.print("Dts State:");
        pw.println(AudioSystem.getParameters(DTS_MODE_PARA));
        pw.print("DTS_MODE_PRESTATE:");
        pw.println(SystemProperties.get(DTS_MODE_PRESTATE, "unknown"));
        pw.print("HS_NO_CHARGE_ON:");
        pw.println(AudioSystem.getParameters(HS_NO_CHARGE_ON));
        pw.print("isCallForeground=");
        pw.println(isCallForeground);
        pw.print("DUAL_SMARTPA_SUPPORT=");
        pw.println(DUAL_SMARTPA_SUPPORT);
        pw.print("SPK_RCV_STEREO_SUPPORT=");
        pw.println(SPK_RCV_STEREO_SUPPORT);
        pw.print("mSpkRcvStereoStatus=");
        pw.println(this.mSpkRcvStereoStatus);
    }
}
