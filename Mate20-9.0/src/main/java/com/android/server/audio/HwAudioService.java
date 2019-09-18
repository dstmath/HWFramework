package com.android.server.audio;

import android.aft.HwAftPolicyManager;
import android.aft.IHwAftPolicyService;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.soundtrigger.SoundTrigger;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.hdm.HwDeviceManager;
import android.media.AudioPlaybackConfiguration;
import android.media.AudioRoutesInfo;
import android.media.AudioSystem;
import android.media.HwMediaMonitorManager;
import android.media.IAudioRoutesObserver;
import android.media.PlayerBase;
import android.media.SoundPool;
import android.media.soundtrigger.SoundTriggerDetector;
import android.media.soundtrigger.SoundTriggerManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.UserManagerInternal;
import android.provider.Settings;
import android.provider.SettingsEx;
import android.rms.iaware.LogIAware;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Slog;
import android.util.Xml;
import android.view.ContextThemeWrapper;
import android.widget.Toast;
import com.android.internal.app.ISoundTriggerService;
import com.android.internal.content.PackageMonitor;
import com.android.server.DeviceIdleController;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.audio.AbsAudioService;
import com.android.server.audio.AudioService;
import com.android.server.audio.report.AudioExceptionMsg;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.notification.HwCustZenModeHelper;
import com.android.server.pm.UserManagerService;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.IGameObserver;
import com.huawei.displayengine.IDisplayEngineService;
import huawei.cust.HwCustUtils;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import vendor.huawei.hardware.hwdisplay.displayengine.V1_0.HighBitsCompModeID;

public class HwAudioService extends AudioService {
    private static final String ACTION_COMMFORCE = "huawei.intent.action.COMMFORCE";
    private static final String ACTION_INCALL_EXTRA = "IsForegroundActivity";
    private static final String ACTION_INCALL_SCREEN = "InCallScreenIsForegroundActivity";
    private static final String ACTION_START_SOUNDTRIGGER_DETECTOR = "com.huawei.vassistant.intent.action.START_SOUNDTRIGGER";
    private static final String ACTION_WAKEUP_SERVICE = "com.huawei.wakeup.services.WakeupService";
    private static final String BLUETOOTH_PKG_NAME = "com.android.bluetooth";
    private static final String CURRENT_PRODUCT_NAME = SystemProperties.get("ro.product.device", null);
    /* access modifiers changed from: private */
    public static final boolean DEBUG = SystemProperties.getBoolean("ro.media.debuggable", false);
    private static final boolean DEBUG_THEME_SOUND = false;
    private static final int DEFAULT_STREAM_TYPE = 1;
    private static final int DEVICE_IN_HEADPHONE = -1979705328;
    private static final int DEVICE_OUT_HEADPHONE = 604004364;
    private static final int DUALPA_RCV_DEALY_DURATION = 5000;
    private static final String DUALPA_RCV_DEALY_OFF = "dualpa_security_delay=0";
    private static final String DUALPA_RCV_DEALY_ON = "dualpa_security_delay=1";
    private static final String EXCLUSIVE_APP_NAME = "com.rohdeschwarz.sit.topsecapp";
    private static final String EXCLUSIVE_PRODUCT_NAME = "HWH1711-Q";
    private static final String EXTRA_SOUNDTRIGGER_STATE = "soundtrigger_state";
    private static final int GAMEMODE_BACKGROUND = 2;
    private static final int GAMEMODE_FOREGROUND = 9;
    public static final int HW_AUDIO_EXCEPTION_OCCOUR = -1000;
    public static final int HW_AUDIO_MODE_TYPE_EXCEPTION_OCCOUR = -1001;
    public static final int HW_AUDIO_TRACK_OVERFLOW_TYPE_EXCEPTION_OCCOUR = -1002;
    public static final int HW_MIC_MUTE_EXCEPTION_OCCOUR = -2002;
    public static final int HW_RING_MODE_TYPE_EXCEPTION_OCCOUR = -2001;
    public static final int HW_SCO_CONNECT_EXCEPTION_OCCOUR = -2003;
    private static final int JUDGE_MILLISECONDS = 5000;
    private static final String KEY_SOUNDTRIGGER_SWITCH = "hw_soundtrigger_enabled";
    private static final boolean LOW_LATENCY_SUPPORT = SystemProperties.getBoolean("persist.media.lowlatency.enable", false);
    private static final String LOW_LATENCY_WHITE_LIST = "/odm/etc/audio/low_latency/white_list.xml";
    private static final UUID MODEL_UUID = UUID.fromString("7dc67ab3-eab6-4e34-b62a-f4fa3788092a");
    private static final int MSG_AUDIO_EXCEPTION_OCCUR = 10000;
    private static final int MSG_DISABLE_HEADPHONE = 91;
    private static final int MSG_DUALPA_RCV_DEALY = 71;
    private static final int MSG_RELOAD_SNDEFFS = 99;
    private static final int MSG_RINGER_MODE_CHANGE = 80;
    private static final int MSG_SET_TYPEC_PARAM_TO_SWS = 93;
    private static final int MSG_SHOW_DISABLE_HEADPHONE_TOAST = 92;
    private static final int MSG_SHOW_DISABLE_MICROPHONE_TOAST = 90;
    private static final String NODE_ATTR_PACKAGE = "package";
    private static final String NODE_WHITEAPP = "whiteapp";
    private static final int PACKAGE_ADDED = 1;
    private static final int PACKAGE_REMOVED = 2;
    private static final String PERMISSION_COMMFORCE = "android.permission.COMM_FORCE";
    public static final int PID_BIT_WIDE = 21;
    private static final String PROP_DESKTOP_MODE = "sys.desktop.mode";
    private static final int RADIO_UID = 1001;
    private static final int RECORDSTATE_STOPPED = 1;
    private static final String SESSION_ID = "session_id";
    private static final int SETTINGS_RING_TYPE = 2;
    private static final String SOUNDTRIGGER_MAD_OFF = "mad=off";
    private static final String SOUNDTRIGGER_MAD_ON = "mad=on";
    private static final String SOUNDTRIGGER_START = "start";
    private static final int SOUNDTRIGGER_STATUS_OFF = 0;
    private static final int SOUNDTRIGGER_STATUS_ON = 1;
    private static final String SOUNDTRIGGER_STOP = "stop";
    private static final String SOUNDTRIGGER_WAKUP_OFF = "wakeup=off";
    private static final String SOUNDTRIGGER_WAKUP_ON = "wakeup=on";
    private static final int SOUNDVIBRATE_AS_GAME = 4;
    private static final int SOUNDVIBRATE_BACKGROUND = 2;
    private static final int SOUNDVIBRATE_FOREGROUND = 1;
    private static final int SOUNDVIBRATE_PROP_OFF = 0;
    private static final int SOUNDVIBRATE_PROP_ON = 1;
    private static final int SOUNDVIBRATE_SETTINGS_OFF = 0;
    private static final int SOUNDVIBRATE_SETTINGS_ON = 1;
    private static final int SPK_RCV_STEREO_OFF = 0;
    private static final String SPK_RCV_STEREO_OFF_PARA = "stereo_landscape_portrait_enable=0";
    private static final int SPK_RCV_STEREO_ON = 1;
    private static final String SPK_RCV_STEREO_ON_PARA = "stereo_landscape_portrait_enable=1";
    /* access modifiers changed from: private */
    public static final boolean SUPPORT_GAME_MODE = SystemProperties.getBoolean("ro.config.dolby_game_mode", false);
    private static final String SWS_TYPEC_MANUFACTURER = "SWS_TYPEC_MANUFACTURER";
    private static final String SWS_TYPEC_PRODUCT_NAME = "SWS_TYPEC_PRODUCT_NAME";
    private static final String SWS_TYPEC_SERIALNO = "SWS_TYPEC_SERIALNO";
    private static final String SWS_VERSION = SystemProperties.get("ro.config.sws_version", "0500");
    private static final String SYSKEY_SOUND_HWT = "syskey_sound_hwt";
    private static final int SYSTEM_UID = 1000;
    private static final String TAG = "HwAudioService";
    private static final String TAG_CTAIFS = "ctaifs";
    private static final long THREE_SEC_LIMITED = 3000;
    private static final String VASSISTANT_PACKAGE_NAME = "com.huawei.vassistant";
    /* access modifiers changed from: private */
    public static boolean isCallForeground = false;
    private static final boolean mIsHuaweiSafeMediaConfig = SystemProperties.getBoolean("ro.config.huawei_safe_media", true);
    private static PhoneStateListener mPhoneListener = new PhoneStateListener() {
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case 0:
                    SystemProperties.set("persist.sys.audio_mode", "0");
                    return;
                case 1:
                    SystemProperties.set("persist.sys.audio_mode", "1");
                    return;
                case 2:
                    SystemProperties.set("persist.sys.audio_mode", "2");
                    return;
                default:
                    return;
            }
        }
    };
    private static final int mSecurityVolumeIndex = SystemProperties.getInt("ro.config.hw.security_volume", 0);
    private static final boolean mSws6Capability;
    private static final boolean mSwsSupported = SystemProperties.getBoolean("ro.config.hw_sws", false);
    /* access modifiers changed from: private */
    public static final boolean mVoipOptimizeInGameMode = SystemProperties.getBoolean("ro.config.gameassist_voipopt", false);
    /* access modifiers changed from: private */
    public boolean IS_IN_GAMEMODE = false;
    private long dialogShowTime = 0;
    private boolean isShowingDialog = false;
    private AudioExceptionRecord mAudioExceptionRecord = null;
    private IBinder mBinder;
    private SoundTriggerDetector.Callback mCallBack = new SoundTriggerDetector.Callback() {
        public void onAvailabilityChanged(int var1) {
        }

        public void onDetected(SoundTriggerDetector.EventPayload var1) {
            Slog.i(HwAudioService.TAG, "onDetected() called with: eventPayload = [" + var1 + "]");
            if (var1.getCaptureSession() != null) {
                DeviceIdleController.LocalService idleController = (DeviceIdleController.LocalService) LocalServices.getService(DeviceIdleController.LocalService.class);
                if (idleController != null) {
                    Slog.i(HwAudioService.TAG, "addPowerSaveTempWhitelistApp#va");
                    idleController.addPowerSaveTempWhitelistApp(Process.myUid(), HwAudioService.VASSISTANT_PACKAGE_NAME, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT, HwAudioService.getCurrentUserId(), false, "hivoice");
                }
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
    private ContentResolver mContentResolver;
    /* access modifiers changed from: private */
    public Context mContext;
    private HwCustZenModeHelper mCustZenModeHelper;
    IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        public void binderDied() {
            Slog.e(HwAudioService.TAG, "binderDied");
            HwAudioService.this.startSoundTriggerV2(true);
        }
    };
    protected volatile int mDesktopMode = -1;
    /* access modifiers changed from: private */
    public boolean mEnableAdjustVolume = true;
    /* access modifiers changed from: private */
    public EnableVolumeClient mEnableVolumeClient = null;
    /* access modifiers changed from: private */
    public final Object mEnableVolumeLock = new Object();
    private ArrayList<HeadphoneInfo> mHeadphones = new ArrayList<>();
    private int mHeadsetSwitchState = 0;
    /* access modifiers changed from: private */
    public HwThemeHandler mHwThemeHandler;
    /* access modifiers changed from: private */
    public boolean mIsBindIAware = false;
    /* access modifiers changed from: private */
    public boolean mIsFmConnected = false;
    private boolean mIsLinkDeathRecipient = false;
    private Toast mLVMToast = null;
    private long mLastSetMode3Time = -1;
    private long mLastStopAudioRecordTime = -1;
    private long mLastStopPlayBackTime = -1;
    private final BroadcastReceiver mLowlatencyReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
                String packageName = intent.getData().getSchemeSpecificPart();
                if (HwAudioService.this.isLowlatencyPkg(packageName)) {
                    HwAudioService.this.updateLowlatencyUidsMap(packageName, 2);
                }
            } else if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
                String packageName2 = intent.getData().getSchemeSpecificPart();
                if (HwAudioService.this.isLowlatencyPkg(packageName2)) {
                    HwAudioService.this.updateLowlatencyUidsMap(packageName2, 1);
                }
                if (HwAudioService.this.mHwAudioServiceEx.isKaraokeWhiteListApp(packageName2)) {
                    HwAudioService.this.mHwAudioServiceEx.setKaraokeWhiteAppUIDByPkgName(packageName2);
                }
            }
        }
    };
    private Map<String, Integer> mLowlatencyUidsMap = new ArrayMap();
    private NotificationManager mNm;
    private final PackageMonitor mPackageMonitor = new PackageMonitor() {
        public void onPackageRemoved(String packageName, int uid) {
            if (HwAudioService.VASSISTANT_PACKAGE_NAME.equals(packageName)) {
                Slog.i(HwAudioService.TAG, "onPackageRemoved uid :" + uid);
                HwAudioService.this.updateVAVersionCode();
            }
        }

        public void onPackageDataCleared(String packageName, int uid) {
            if (HwAudioService.VASSISTANT_PACKAGE_NAME.equals(packageName)) {
                Slog.i(HwAudioService.TAG, "onPackageDataCleared uid : " + uid);
                HwAudioService.this.resetVASoundTrigger();
            }
        }

        public void onPackageAppeared(String packageName, int reason) {
            if (HwAudioService.VASSISTANT_PACKAGE_NAME.equals(packageName)) {
                Slog.i(HwAudioService.TAG, "onPackageUpdateStarted reason : " + reason);
                HwAudioService.this.updateVAVersionCode();
            }
        }
    };
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals("android.intent.action.FM")) {
                int state = intent.getIntExtra("state", 0);
                if (HwAudioService.DEBUG) {
                    Slog.i(HwAudioService.TAG, "Broadcast Receiver: Got ACTION_FMRx_PLUG, state =" + state);
                }
                boolean isConnected = HwAudioService.this.mIsFmConnected;
                if (state == 0 && isConnected) {
                    AudioSystem.setDeviceConnectionState(HighBitsCompModeID.MODE_COLOR_ENHANCE, 0, "", "");
                    boolean unused = HwAudioService.this.mIsFmConnected = false;
                } else if (state == 1 && !isConnected) {
                    AudioSystem.setDeviceConnectionState(HighBitsCompModeID.MODE_COLOR_ENHANCE, 1, "", "");
                    boolean unused2 = HwAudioService.this.mIsFmConnected = true;
                }
            } else if (action != null && action.equals("huawei.intent.action.RINGTONE_CHANGE")) {
                String unused3 = HwAudioService.this.mSysKeyEffectFile = intent.getStringExtra("KEYTOUCH_AUDIOEFFECT_PATH");
                if (HwAudioService.this.mSysKeyEffectFile != null) {
                    HwAudioService.this.mHwThemeHandler.sendMessage(HwAudioService.this.mHwThemeHandler.obtainMessage(99, 0, 0, null));
                }
            } else if (action != null && HwAudioService.ACTION_INCALL_SCREEN.equals(action)) {
                boolean unused4 = HwAudioService.isCallForeground = intent.getBooleanExtra(HwAudioService.ACTION_INCALL_EXTRA, true);
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
            } else if (action != null && "android.intent.action.BOOT_COMPLETED".equals(action) && !HwAudioService.this.mIsBindIAware) {
                boolean unused5 = HwAudioService.this.mIsBindIAware = true;
                HwAudioService.this.bindIAwareSdk();
            }
        }
    };
    private Toast mRingerModeToast = null;
    /* access modifiers changed from: private */
    public SetICallBackForKaraokeHandler mSetICallBackForKaraokeHandler = null;
    /* access modifiers changed from: private */
    public boolean mSetVbrlibFlag = false;
    private SoundTriggerDetector mSoundTriggerDetector;
    private String mSoundTriggerGram = null;
    private Handler mSoundTriggerHandler;
    private SoundTriggerManager mSoundTriggerManager;
    private String mSoundTriggerRes = null;
    private ISoundTriggerService mSoundTriggerService;
    private int mSoundTriggerStatus = 0;
    private int mSpkRcvStereoStatus = -1;
    /* access modifiers changed from: private */
    public String mSysKeyEffectFile;
    private UserManager mUserManager;
    private final UserManagerInternal mUserManagerInternal;
    private int mVAVersinCode;
    private List<String> mWhiteAppName = new ArrayList();
    private String[] mZenModeWhiteList;
    private int oldInCallDevice = 0;
    private int oldLVMDevice = 0;

    private class EnableVolumeClient implements IBinder.DeathRecipient {
        private String mCallerPkg;
        private IBinder mCb;

        public String getCallerPkg() {
            return this.mCallerPkg;
        }

        EnableVolumeClient(IBinder cb) {
            if (cb != null) {
                try {
                    cb.linkToDeath(this, 0);
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
                boolean unused = HwAudioService.this.mEnableAdjustVolume = true;
                release();
            }
        }

        public void release() {
            if (this.mCb != null) {
                this.mCb.unlinkToDeath(this, 0);
                this.mCb = null;
            }
            EnableVolumeClient unused = HwAudioService.this.mEnableVolumeClient = null;
        }
    }

    private class ForeAppTypeListener implements AwareIntelligentRecg.IAwareFgChangedCallback {
        private static final int UNKNOWN_TYPE = -1;
        private String mPackageName = null;
        private int mType = -1;

        public ForeAppTypeListener() {
        }

        public void onForegroundActivitiesChanged(ArrayList<AwareIntelligentRecg.PidInfo> pidInfos, boolean isAdd) {
            if (pidInfos == null || pidInfos.size() == 0) {
                Slog.e(HwAudioService.TAG, "IAware onForegroundActivitiesChanged return data is null");
                return;
            }
            int listSize = pidInfos.size();
            if (listSize > 50) {
                Slog.e(HwAudioService.TAG, "IAware return ListSize is " + listSize + ", >" + 50);
                listSize = 50;
            }
            for (int i = 0; i < listSize; i++) {
                ArrayMap<String, Integer> maps = pidInfos.get(i).getPkgAndType();
                if (maps != null) {
                    int mapSize = maps.size();
                    if (mapSize > 50) {
                        Slog.e(HwAudioService.TAG, "list index is " + i + ",mapSize is " + mapSize + ", >" + 50);
                        mapSize = 50;
                    }
                    for (int j = 0; j < mapSize; j++) {
                        if (maps.valueAt(j).intValue() == 9) {
                            reportForegroundAppType(9, maps.keyAt(j));
                            return;
                        }
                    }
                    continue;
                }
            }
            if (pidInfos.get(0).getPkgAndType().size() > 0) {
                reportForegroundAppType(pidInfos.get(0).getPkgAndType().valueAt(0).intValue(), null);
            } else {
                reportForegroundAppType(-1, null);
            }
        }

        private void reportForegroundAppType(int type, String pkg) {
            Slog.i(HwAudioService.TAG, "now type = " + this.mType + ",reportForegroundAppType = " + type + ",pkg = " + pkg + ",mVoipOptimizeInGameMode" + HwAudioService.mVoipOptimizeInGameMode);
            if (this.mType != type || (pkg != null && !pkg.equals(this.mPackageName))) {
                if (this.mType != type) {
                    this.mType = type;
                    SystemProperties.set("sys.iaware.type", type + "");
                }
                this.mPackageName = pkg;
                if (type != 9) {
                    if (HwAudioService.SUPPORT_GAME_MODE && HwAudioService.this.IS_IN_GAMEMODE) {
                        HwAudioService.this.mHwAudioServiceEx.setDolbyEffect(0);
                        boolean unused = HwAudioService.this.IS_IN_GAMEMODE = false;
                        AudioSystem.setParameters("dolby_game_mode=off");
                    }
                    if (HwAudioService.mVoipOptimizeInGameMode != 0) {
                        AudioSystem.setParameters("game_mode=off");
                    }
                } else {
                    if (HwAudioService.SUPPORT_GAME_MODE && !HwAudioService.this.IS_IN_GAMEMODE && !AudioSystem.isStreamActive(3, 0)) {
                        Slog.i(HwAudioService.TAG, "Music steam not start");
                        if (HwAudioService.this.mHwAudioServiceEx.setDolbyEffect(3)) {
                            boolean unused2 = HwAudioService.this.IS_IN_GAMEMODE = true;
                        } else {
                            Slog.d(HwAudioService.TAG, "set game mode fail.");
                        }
                    }
                    if (HwAudioService.mVoipOptimizeInGameMode != 0) {
                        Slog.d(HwAudioService.TAG, "support voip optinization during game mode.");
                        AudioSystem.setParameters("game_mode=on");
                    }
                }
            }
        }
    }

    private static class HeadphoneInfo {
        public String mCaller;
        public String mDeviceAddress;
        public String mDeviceName;
        public int mDeviceType;

        public HeadphoneInfo(int deviceType, String deviceAddress, String deviceName, String caller) {
            this.mDeviceType = deviceType;
            this.mDeviceName = deviceName;
            this.mDeviceAddress = deviceAddress;
            this.mCaller = caller;
        }
    }

    private class HwAudioGameObserver extends IGameObserver.Stub {
        private HwAudioGameObserver() {
        }

        public void onGameListChanged() {
            Slog.v(HwAudioService.TAG, "onGameListChanged !");
        }

        public void onGameStatusChanged(String packageName, int event) {
            if (packageName != null) {
                Slog.d(HwAudioService.TAG, "onGameStatusChanged packageName = " + packageName + ", event = " + event);
                if (HwAudioService.this.isSupportSoundToVibrateMode() && Settings.Secure.getInt(HwAudioService.this.mContext.getContentResolver(), "sound_to_vibrate_effect", 1) == 1) {
                    if (event != 4) {
                        switch (event) {
                            case 1:
                                break;
                            case 2:
                                AudioSystem.setParameters("vbrmode=background");
                                break;
                            default:
                                Slog.w(HwAudioService.TAG, "onGameStatusChanged event not in foreground or background!");
                                break;
                        }
                    }
                    if (!HwAudioService.this.mSetVbrlibFlag) {
                        Slog.d(HwAudioService.TAG, "HwAudioGameObserver set mSetVbrlibFalg = true means firstly load the vbr lib!");
                        AudioSystem.setParameters("vbrEnter=on");
                        boolean unused = HwAudioService.this.mSetVbrlibFlag = true;
                    }
                    AudioSystem.setParameters("vbrmode=front;gamepackagename=" + packageName);
                }
            }
        }
    }

    private class HwThemeHandler extends Handler {
        private HwThemeHandler() {
        }

        public void handleMessage(Message msg) {
            if (99 == msg.what) {
                HwAudioService.this.reloadSoundEffects();
            }
        }
    }

    private class SetICallBackForKaraokeHandler implements IBinder.DeathRecipient {
        private String mCallerPkg;
        private IBinder mCb;

        SetICallBackForKaraokeHandler(IBinder cb) {
            if (cb != null) {
                try {
                    cb.linkToDeath(this, 0);
                } catch (RemoteException e) {
                    Slog.w(HwAudioService.TAG, "SetICallBackForKaraokeHandler() could not link to " + cb + " binder death");
                    cb = null;
                }
            }
            this.mCb = cb;
            this.mCallerPkg = HwAudioService.this.getPackageNameByPid(Binder.getCallingPid());
        }

        public void binderDied() {
            Slog.w(HwAudioService.TAG, "Karaoke client died,pkgname = " + this.mCallerPkg);
            AudioSystem.setParameters("Karaoke_enable=disable");
            release();
        }

        public void release() {
            if (this.mCb != null) {
                this.mCb.unlinkToDeath(this, 0);
                this.mCb = null;
            }
            SetICallBackForKaraokeHandler unused = HwAudioService.this.mSetICallBackForKaraokeHandler = null;
        }

        public IBinder getBinder() {
            return this.mCb;
        }
    }

    static {
        boolean z = false;
        if (mSwsSupported && "0600".equals(SWS_VERSION)) {
            z = true;
        }
        mSws6Capability = z;
    }

    /* access modifiers changed from: protected */
    public boolean usingHwSafeMediaConfig() {
        return mIsHuaweiSafeMediaConfig;
    }

    /* access modifiers changed from: protected */
    public int getHwSafeMediaVolumeIndex() {
        return mSecurityVolumeIndex * 10;
    }

    /* access modifiers changed from: protected */
    public boolean isHwSafeMediaVolumeEnabled() {
        return mSecurityVolumeIndex > 0;
    }

    /* access modifiers changed from: protected */
    public boolean checkMMIRunning() {
        return "true".equals(SystemProperties.get("runtime.mmitest.isrunning", "false"));
    }

    public HwAudioService(Context context) {
        super(context);
        Slog.i(TAG, TAG);
        this.mContext = context;
        this.mNm = (NotificationManager) this.mContext.getSystemService("notification");
        IntentFilter intentFilter = new IntentFilter("android.intent.action.FM");
        intentFilter.addAction("huawei.intent.action.RINGTONE_CHANGE");
        intentFilter.addAction(ACTION_INCALL_SCREEN);
        intentFilter.addAction("android.intent.action.PHONE_STATE");
        intentFilter.addAction("android.intent.action.USER_UNLOCKED");
        intentFilter.addAction("android.intent.action.REBOOT");
        intentFilter.addAction("android.intent.action.ACTION_SHUTDOWN");
        intentFilter.addAction("android.intent.action.BOOT_COMPLETED");
        context.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, intentFilter, null, null);
        this.mPackageMonitor.register(this.mContext, this.mContext.getMainLooper(), UserHandle.SYSTEM, false);
        this.mContentResolver = this.mContext.getContentResolver();
        this.mAudioExceptionRecord = new AudioExceptionRecord();
        readPersistedSettingsEx(this.mContentResolver);
        setAudioSystemParameters();
        HwServiceFactory.getHwDrmDialogService().startDrmDialogService(context);
        this.mUserManagerInternal = (UserManagerInternal) LocalServices.getService(UserManagerInternal.class);
        if (LOW_LATENCY_SUPPORT) {
            IntentFilter lowlatencyIntentFilter = new IntentFilter();
            lowlatencyIntentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
            lowlatencyIntentFilter.addAction("android.intent.action.PACKAGE_ADDED");
            lowlatencyIntentFilter.addDataScheme("package");
            this.mContext.registerReceiverAsUser(this.mLowlatencyReceiver, UserHandle.ALL, lowlatencyIntentFilter, null, null);
            initLowlatencyUidsMap();
        }
        this.mCustZenModeHelper = (HwCustZenModeHelper) HwCustUtils.createObj(HwCustZenModeHelper.class, new Object[0]);
        if (this.mCustZenModeHelper != null) {
            this.mZenModeWhiteList = this.mCustZenModeHelper.getWhiteAppsInZenMode();
        }
        ((TelephonyManager) context.getSystemService("phone")).listen(mPhoneListener, 32);
        ActivityManagerEx.registerGameObserver(new HwAudioGameObserver());
    }

    /* access modifiers changed from: private */
    public boolean isSupportSoundToVibrateMode() {
        return SystemProperties.getInt("ro.config.gameassist_soundtovibrate", 0) == 1;
    }

    public void initHwThemeHandler() {
        this.mHwThemeHandler = new HwThemeHandler();
    }

    public void reloadSoundEffects() {
        unloadSoundEffects();
        loadSoundEffects();
    }

    /* access modifiers changed from: private */
    public void onUserUnlocked(int userId) {
        if (userId != -10000) {
            createManagers();
            boolean isPrimary = this.mUserManager.getUserInfo(userId).isPrimary();
            Slog.i(TAG, "user unlocked ,start soundtrigger! isPrimary : " + isPrimary);
            if (isPrimary) {
                updateVAVersionCode();
                createAndStartSoundTrigger(true);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onUserBackground(int userId) {
        Slog.i(TAG, "onUserBackground userId : " + userId);
        checkWithUserSwith(userId, true);
    }

    /* access modifiers changed from: protected */
    public void onUserForeground(int userId) {
        Slog.i(TAG, "onUserForeground userId : " + userId);
        checkWithUserSwith(userId, false);
    }

    /* access modifiers changed from: protected */
    public void updateDefaultStream() {
        if (this.mContentResolver == null) {
            Slog.i(TAG, "mContentResolver is null.");
            return;
        }
        int streamType = Settings.System.getIntForUser(this.mContentResolver, "default_volume_key_control", 1, -2);
        if (streamType == 2) {
            Slog.i(TAG, "get default stream : " + streamType);
            this.mDefaultVolStream = 2;
        } else {
            this.mDefaultVolStream = 3;
        }
    }

    private void checkWithUserSwith(int userId, boolean background) {
        createManagers();
        if (userId >= 0) {
            boolean isPrimary = UserManagerService.getInstance().getUserInfo(userId).isPrimary();
            Slog.i(TAG, "onUserBackground isPrimary : " + isPrimary);
            if (isPrimary) {
                createAndStartSoundTrigger(!background);
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateVAVersionCode() {
        int versionCode = 0;
        try {
            versionCode = this.mContext.getPackageManager().getPackageInfo(VASSISTANT_PACKAGE_NAME, 128).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Slog.e(TAG, "updateVAVersionCode vassistant not found!");
        }
        Slog.i(TAG, "updateVAVersionCode versionCode : " + versionCode + " mVAVersinCode = " + this.mVAVersinCode);
        if (versionCode < this.mVAVersinCode) {
            resetVASoundTrigger();
        }
        this.mVAVersinCode = versionCode;
    }

    /* access modifiers changed from: private */
    public void resetVASoundTrigger() {
        startSoundTriggerV2(false);
        Settings.Secure.putInt(this.mContentResolver, KEY_SOUNDTRIGGER_SWITCH, 0);
    }

    private boolean isSoundTriggerOn() {
        return Settings.Secure.getInt(this.mContentResolver, KEY_SOUNDTRIGGER_SWITCH, 0) == 1;
    }

    private void createAndStartSoundTrigger(boolean start) {
        startSoundTriggerV2(start);
    }

    private boolean startSoundTriggerDetector() {
        createManagers();
        createSoundTriggerDetector();
        SoundTrigger.GenericSoundModel model = getCurrentModel();
        Slog.i(TAG, "startSoundTriggerDetector model : " + model);
        long ident = Binder.clearCallingIdentity();
        if (model != null) {
            try {
                if (this.mSoundTriggerDetector.startRecognition(1)) {
                    Slog.i(TAG, "start recognition successfully!");
                    return true;
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
        Slog.e(TAG, "start recognition failed!");
        Binder.restoreCallingIdentity(ident);
        return false;
    }

    private boolean stopSoundTriggerDetector() {
        createManagers();
        createSoundTriggerDetector();
        SoundTrigger.GenericSoundModel model = getCurrentModel();
        Slog.i(TAG, "stopSoundTriggerDetector model : " + model);
        long ident = Binder.clearCallingIdentity();
        if (model != null) {
            try {
                if (this.mSoundTriggerDetector.stopRecognition()) {
                    Slog.i(TAG, "stop recognition successfully!");
                    return true;
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
        Slog.e(TAG, "stop recognition failed!");
        Binder.restoreCallingIdentity(ident);
        return false;
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
            this.mSoundTriggerService = ISoundTriggerService.Stub.asInterface(ServiceManager.getService("soundtrigger"));
        }
    }

    private void createSoundTriggerDetector() {
        if (this.mSoundTriggerDetector == null) {
            createSoundTriggerHandler();
            this.mSoundTriggerDetector = this.mSoundTriggerManager.createSoundTriggerDetector(MODEL_UUID, this.mCallBack, this.mSoundTriggerHandler);
        }
    }

    private static boolean isSupportWakeUpV2() {
        String wakeupV2 = AudioSystem.getParameters("audio_capability#soundtrigger_version");
        Slog.i(TAG, "wakeupV2 : " + wakeupV2);
        if ("".equals(wakeupV2)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void startSoundTriggerV2(boolean start) {
        if (!isSupportWakeUpV2()) {
            Slog.i(TAG, "startSoundTriggerV2 not support wakeup v2");
            return;
        }
        createManagers();
        SoundTrigger.GenericSoundModel model = getCurrentModel();
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
    }

    private SoundTrigger.GenericSoundModel getCurrentModel() {
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

    /* access modifiers changed from: private */
    public void startWakeupService(int session) {
        Intent intent = new Intent(ACTION_WAKEUP_SERVICE);
        intent.setPackage(VASSISTANT_PACKAGE_NAME);
        intent.putExtra(SESSION_ID, session);
        this.mContext.startService(intent);
    }

    public AudioRoutesInfo startWatchingRoutes(IAudioRoutesObserver observer) {
        Slog.d(TAG, "startWatchingRoutes");
        if (!linkVAtoDeathRe(observer)) {
            return HwAudioService.super.startWatchingRoutes(observer);
        }
        return null;
    }

    private boolean linkVAtoDeathRe(IAudioRoutesObserver observer) {
        String packageName;
        Slog.d(TAG, "linkVAtoDeathRe for vassistant");
        int uid = Binder.getCallingUid();
        if (this.mContext == null) {
            return false;
        }
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null) {
            return false;
        }
        String[] packages = pm.getPackagesForUid(uid);
        if (packages == null) {
            return false;
        }
        int i = 0;
        while (i < packages.length) {
            Slog.d(TAG, "packageName:" + packageName);
            if (VASSISTANT_PACKAGE_NAME.equals(packageName)) {
                this.mBinder = observer.asBinder();
                try {
                    this.mBinder.linkToDeath(this.mDeathRecipient, 0);
                    this.mIsLinkDeathRecipient = true;
                    return true;
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
                i++;
            }
        }
        return false;
    }

    public boolean isFileReady(String filename) {
        return new File(filename).canRead();
    }

    public void unloadHwThemeSoundEffects() {
        this.mHwThemeHandler.removeMessages(99);
    }

    public int getSampleId(SoundPool soundpool, int effect, String defFilePath, int index) {
        int sampleId = 0;
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
                    SettingsEx.Systemex.putString(this.mContentResolver, SYSKEY_SOUND_HWT, themeFilePath);
                }
            }
        } else {
            sampleId = soundpool.load(defFilePath, index);
        }
        return sampleId;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        int _result;
        boolean _result2;
        int _arg0;
        if (code != 1003) {
            boolean _arg02 = false;
            switch (code) {
                case 101:
                    int event = data.readInt();
                    if (DEBUG) {
                        Slog.v(TAG, "HwAudioService.onTransact: got event " + event);
                    }
                    if (event == 1) {
                        _arg02 = true;
                    }
                    sendMsg(this.mAudioHandler, 8, 2, 1, _arg02 ? 1 : 0, null, 0);
                    if (DEBUG) {
                        Slog.v(TAG, "setSpeakermediaOn " + (_arg02 ? 1 : 0));
                    }
                    reply.writeNoException();
                    return true;
                case 102:
                    reply.writeNoException();
                    return true;
                default:
                    switch (code) {
                        case 1005:
                            data.enforceInterface("android.media.IAudioService");
                            sendMsg(this.mAudioHandler, MSG_SHOW_DISABLE_MICROPHONE_TOAST, 2, 0, 0, null, 20);
                            reply.writeNoException();
                            return true;
                        case HwArbitrationDEFS.MSG_WIFI_STATE_DISCONNECT /*1006*/:
                            data.enforceInterface("android.media.IAudioService");
                            sendMsg(this.mAudioHandler, 91, 2, 0, 0, null, 0);
                            reply.writeNoException();
                            return true;
                        default:
                            switch (code) {
                                case 1101:
                                    Slog.i(TAG, "isAdjustVolumeEnable transaction called. result:" + ((int) isAdjustVolumeEnable()));
                                    reply.writeNoException();
                                    reply.writeInt(_result);
                                    return true;
                                case 1102:
                                    IBinder callerToken = data.readStrongBinder();
                                    if (data.readInt() != 0) {
                                        _arg02 = true;
                                    }
                                    Slog.i(TAG, "enableVolumeAdjust  transaction called.enable:" + _arg02);
                                    enableVolumeAdjust(_arg02, Binder.getCallingUid(), callerToken);
                                    reply.writeNoException();
                                    return true;
                                case 1103:
                                    data.enforceInterface("android.media.IAudioService");
                                    if (data.readInt() != 0) {
                                        _result2 = startSoundTriggerDetector();
                                    } else {
                                        _result2 = stopSoundTriggerDetector();
                                    }
                                    reply.writeNoException();
                                    reply.writeInt(_result2 ? 1 : 0);
                                    if (this.mBinder != null && this.mIsLinkDeathRecipient) {
                                        this.mBinder.unlinkToDeath(this.mDeathRecipient, 0);
                                        this.mIsLinkDeathRecipient = false;
                                    }
                                    return true;
                                case 1104:
                                    Slog.i(TAG, "desktopModeChanged transaction mode:" + _arg0);
                                    desktopModeChanged(_arg0);
                                    reply.writeNoException();
                                    return true;
                                case 1105:
                                    data.enforceInterface("android.media.IAudioService");
                                    reply.writeNoException();
                                    reply.writeBoolean(isScoAvailableOffCall());
                                    return true;
                                case HwArbitrationDEFS.MSG_STREAMING_VIDEO_BAD /*1106*/:
                                    data.enforceInterface("android.media.IAudioService");
                                    reply.writeNoException();
                                    reply.writeBoolean(isHwKaraokeEffectEnable());
                                    return true;
                                case HwArbitrationDEFS.MSG_GAME_WAR_STATE_BAD /*1107*/:
                                    data.enforceInterface("android.media.IAudioService");
                                    setICallBackForKaraoke(data.readStrongBinder());
                                    reply.writeNoException();
                                    return true;
                                default:
                                    return HwAudioService.super.onTransact(code, data, reply, flags);
                            }
                    }
            }
        } else {
            data.enforceInterface("android.media.IAudioService");
            String readString = data.readString();
            recordLastRecordTime(data.readInt(), data.readInt());
            reply.writeNoException();
            return true;
        }
    }

    public boolean isHwKaraokeEffectEnable() {
        boolean enable = this.mHwAudioServiceEx.isHwKaraokeEffectEnable(getPackageNameByPid(Binder.getCallingPid()));
        Slog.i(TAG, "HwKaraoke Effect enable = " + enable);
        return enable;
    }

    private void setICallBackForKaraoke(IBinder cb) {
        if (cb != null) {
            Slog.d(TAG, "setICallBackForKaraoke in audioService caller pid = " + Binder.getCallingPid());
            if (this.mSetICallBackForKaraokeHandler == null) {
                this.mSetICallBackForKaraokeHandler = new SetICallBackForKaraokeHandler(cb);
            } else if (this.mSetICallBackForKaraokeHandler.getBinder() == cb) {
                Slog.d(TAG, "mSetICallBackForKaraokeHandler cb:" + cb + " is already linked.");
            } else {
                this.mSetICallBackForKaraokeHandler.release();
                this.mSetICallBackForKaraokeHandler = new SetICallBackForKaraokeHandler(cb);
            }
        }
    }

    public boolean isScoAvailableOffCall() {
        if (this.mBluetoothHeadset != null) {
            return this.mBluetoothHeadset.isScoAvailableOffCall();
        }
        return true;
    }

    public static void printCtaifsLog(String applicationName, String packageName, String callingMethod, String desciption) {
        Slog.i(TAG_CTAIFS, "<" + applicationName + ">[" + applicationName + "][" + packageName + "][" + callingMethod + "] " + desciption);
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
            appInfo = pm.getApplicationInfoAsUser(packageName, 0, getCurrentUserId());
        } catch (PackageManager.NameNotFoundException e) {
            Slog.e(TAG, "App Name Not Found");
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
            throw th;
        }
        Binder.restoreCallingIdentity(ident);
        if (appInfo == null) {
            return null;
        }
        CharSequence displayName = pm.getApplicationLabel(appInfo);
        if (TextUtils.isEmpty(displayName)) {
            return null;
        }
        return String.valueOf(displayName);
    }

    /* access modifiers changed from: protected */
    public void appendExtraInfo(Intent intent) {
        if (DEBUG) {
            Slog.d(TAG, "appendExtraInfo mHeadsetSwitchState: " + this.mHeadsetSwitchState);
        }
        if (intent != null) {
            intent.putExtra("switch_state", this.mHeadsetSwitchState);
        }
    }

    /* access modifiers changed from: protected */
    public void sendDeviceConnectionIntentForImcs(int device, int state, String name) {
        Intent intent = new Intent();
        intent.putExtra("state", state);
        intent.putExtra("name", name);
        if (DEBUG) {
            Slog.d(TAG, "sendDeviceConnectionIntentForImcs mHeadsetSwitchState: " + this.mHeadsetSwitchState);
        }
        intent.putExtra("switch_state", this.mHeadsetSwitchState);
        if (device == 4) {
            intent.setAction("imcs.action.HEADSET_PLUG");
            intent.putExtra("microphone", 1);
        } else if (device == 8) {
            intent.setAction("imcs.action.HEADSET_PLUG");
            intent.putExtra("microphone", 0);
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
        return this.mEnableAdjustVolume;
    }

    private void enableVolumeAdjust(boolean enable, int callerUid, IBinder cb) {
        if (!hasSystemPriv(callerUid)) {
            Slog.i(TAG, "caller is not system app.Can not set volumeAdjust to enable or disable.");
            return;
        }
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
    }

    /* access modifiers changed from: protected */
    public void readPersistedSettingsEx(ContentResolver cr) {
        if (HW_SOUND_TRIGGER_SUPPORT) {
            getSoundTriggerSettings(cr);
        }
        if (SPK_RCV_STEREO_SUPPORT) {
            getSpkRcvStereoState(cr);
        }
    }

    /* access modifiers changed from: protected */
    public void onErrorCallBackEx(int exceptionId) {
        Slog.i(TAG, "AudioException onErrorCallBackEx exceptionId:" + exceptionId);
        if (exceptionId <= -1000) {
            sendMsg(this.mAudioHandler, 10000, 1, exceptionId, 0, null, 0);
        }
    }

    /* access modifiers changed from: protected */
    public void onScoExceptionOccur(int clientPid) {
        Slog.w(TAG, "AudioException ScoExceptionOccur,clientpid:" + clientPid + " have more than one sco connected!");
        String packageName = getPackageNameByPid(clientPid);
        sendMsg(this.mAudioHandler, 10000, 1, HW_SCO_CONNECT_EXCEPTION_OCCOUR, 0, new AudioExceptionMsg(HW_SCO_CONNECT_EXCEPTION_OCCOUR, packageName, getVersionName(packageName)), 0);
    }

    public void setMicrophoneMute(boolean on, String callingPackage, int userId) {
        boolean firstState = AudioSystem.isMicrophoneMuted();
        HwAudioService.super.setMicrophoneMute(on, callingPackage, userId);
        if (!firstState && AudioSystem.isMicrophoneMuted()) {
            this.mAudioExceptionRecord.updateMuteMsg(callingPackage, getVersionName(callingPackage));
        }
    }

    private boolean hasCameraRecordPermission(String packageName) {
        boolean z = false;
        if (packageName == null || packageName.length() == 0) {
            return false;
        }
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null) {
            return false;
        }
        if (pm.checkPermission("android.permission.CAMERA", packageName) == 0 && pm.checkPermission("android.permission.RECORD_AUDIO", packageName) == 0) {
            z = true;
        }
        return z;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v6, resolved type: com.android.server.audio.report.AudioExceptionMsg} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v7, resolved type: com.android.server.audio.report.AudioExceptionMsg} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v7, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v10, resolved type: com.android.server.audio.report.AudioExceptionMsg} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v14, resolved type: com.android.server.audio.report.AudioExceptionMsg} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v15, resolved type: com.android.server.audio.report.AudioExceptionMsg} */
    /* JADX WARNING: type inference failed for: r1v9, types: [java.lang.String] */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Multi-variable type inference failed */
    public void handleMessageEx(Message msg) {
        int i = msg.what;
        if (i == MSG_DUALPA_RCV_DEALY) {
            AudioSystem.setParameters(DUALPA_RCV_DEALY_OFF);
        } else if (i != 80) {
            AudioExceptionMsg audioExceptionMsg = null;
            switch (i) {
                case MSG_SHOW_DISABLE_MICROPHONE_TOAST /*90*/:
                    showDisableMicrophoneToast();
                    break;
                case 91:
                    disableHeadPhone();
                    break;
                case 92:
                    showDisableHeadphoneToast();
                    break;
                case 93:
                    if (msg.obj != null) {
                        audioExceptionMsg = (String) msg.obj;
                    }
                    String name = audioExceptionMsg;
                    if (name != null) {
                        setTypecParamToAudioSystem(name);
                        break;
                    }
                    break;
                default:
                    switch (i) {
                        case 10000:
                            int i2 = msg.arg1;
                            if (msg.obj != null) {
                                audioExceptionMsg = (AudioExceptionMsg) msg.obj;
                            }
                            onAudioException(i2, audioExceptionMsg);
                            break;
                        case IDisplayEngineService.DE_ACTION_PG_BROWSER_FRONT /*10001*/:
                            if (LOUD_VOICE_MODE_SUPPORT) {
                                handleLVMModeChangeProcess(msg.arg1, msg.obj);
                                break;
                            }
                            break;
                    }
            }
        } else if (msg.obj == null) {
            Slog.e(TAG, "MSG_RINGER_MODE_CHANGE msg obj is null!");
        } else {
            AudioExceptionMsg tempMsg = (AudioExceptionMsg) msg.obj;
            String caller = tempMsg.getMsgPackagename();
            if (caller == null) {
                caller = "";
            }
            int fromRingerMode = msg.arg1;
            int ringerMode = msg.arg2;
            if (!isFirstTimeShow(caller) || hasCameraRecordPermission(caller)) {
                Slog.i(TAG, "AudioException showRingerModeToast");
                showRingerModeToast(caller, fromRingerMode, ringerMode);
            } else {
                Slog.i(TAG, "AudioException showRingerModeDialog");
                showRingerModeDialog(caller, fromRingerMode, ringerMode);
                savePkgNameToDB(caller);
            }
            onAudioException(HW_RING_MODE_TYPE_EXCEPTION_OCCOUR, tempMsg);
        }
    }

    public void onAudioException(int exceptionId, AudioExceptionMsg exceptionMsg) {
        if (!this.mSystemReady) {
            Slog.e(TAG, "AudioException,but system is not ready! ");
        }
        if (exceptionId != -1001) {
            switch (exceptionId) {
                case HW_SCO_CONNECT_EXCEPTION_OCCOUR /*-2003*/:
                    Slog.w(TAG, "AudioException HW_SCO_CONNECT_EXCEPTION_OCCOUR");
                    return;
                case HW_MIC_MUTE_EXCEPTION_OCCOUR /*-2002*/:
                    if (!this.mUserManagerInternal.getUserRestriction(getCurrentUserId(), "no_unmute_microphone")) {
                        Slog.w(TAG, "AudioException HW_MIC_MUTE_EXCEPTION_OCCOUR");
                        if (EXCLUSIVE_PRODUCT_NAME.equals(CURRENT_PRODUCT_NAME)) {
                            Slog.w(TAG, "Not allowded to recovery according to the us operator's rules");
                            return;
                        } else if (exceptionMsg == null || !EXCLUSIVE_APP_NAME.equals(exceptionMsg.getMsgPackagename())) {
                            AudioSystem.muteMicrophone(false);
                            return;
                        } else {
                            Slog.w(TAG, "Not allowded to recovery according to this App");
                            return;
                        }
                    } else {
                        return;
                    }
                case HW_RING_MODE_TYPE_EXCEPTION_OCCOUR /*-2001*/:
                    Slog.w(TAG, "AudioException HW_RING_MODE_TYPE_EXCEPTION_OCCOUR");
                    return;
                default:
                    int tmp = -exceptionId;
                    if ((-(tmp >> 21)) == -1002) {
                        int exceptionPid = tmp - 2101346304;
                        if (exceptionPid > 0) {
                            String packageName = getPackageNameByPid(exceptionPid);
                            String packageVersion = getVersionName(packageName);
                            if (packageName != null) {
                                Slog.e(TAG, "AudioTrack_Overflow packageName = " + packageName + " " + packageVersion + " pid = " + exceptionPid);
                                HwMediaMonitorManager.writeLogMsg(916010207, 3, HW_AUDIO_TRACK_OVERFLOW_TYPE_EXCEPTION_OCCOUR, "OAE5");
                                return;
                            }
                            Slog.w(TAG, "AudioTrack_Overflow getPackageNameByPid failed");
                            return;
                        }
                        Slog.w(TAG, "AudioTrack_Overflow pid error");
                        return;
                    }
                    Slog.w(TAG, "No such AudioException exceptionId:" + exceptionId);
                    return;
            }
        } else {
            Slog.w(TAG, "AudioException HW_AUDIO_MODE_TYPE_EXCEPTION_OCCOUR, setModeInt AudioSystem.MODE_NORMAL");
        }
    }

    /* access modifiers changed from: protected */
    public void handleLVMModeChangeProcess(int state, Object object) {
        if (state == 1) {
            if (object != null) {
                AbsAudioService.DeviceVolumeState myObj = (AbsAudioService.DeviceVolumeState) object;
                setLVMMode(myObj.mDirection, myObj.mDevice, myObj.mOldIndex, myObj.mstreamType);
            }
        } else if (isInCall()) {
            int device = getInCallDevice();
            setLVMMode(0, device, -1, 0);
            setVoiceALGODeviceChange(device);
        }
    }

    private boolean isInCall() {
        if (getMode() != 2) {
            return false;
        }
        TelecomManager telecomManager = (TelecomManager) this.mContext.getSystemService("telecom");
        if (telecomManager == null) {
            return false;
        }
        return telecomManager.isInCall();
    }

    /* access modifiers changed from: protected */
    public int getOldInCallDevice(int mode) {
        if (mode == 2) {
            this.oldInCallDevice = getInCallDevice();
        } else if (mode == 0) {
            this.oldInCallDevice = 0;
        }
        return this.oldInCallDevice;
    }

    private int getInCallDevice() {
        int activeStreamType;
        if (AudioSystem.getForceUse(0) == 3) {
            activeStreamType = 6;
        } else {
            activeStreamType = 0;
        }
        return hwGetDeviceForStream(activeStreamType);
    }

    private void setVoiceALGODeviceChange(int device) {
        if (this.oldInCallDevice != device) {
            AudioSystem.setParameters("VOICE_ALGO_DeviceChange=true");
            this.oldInCallDevice = device;
        }
    }

    /* access modifiers changed from: private */
    public void force_exitLVMMode() {
        if ("true".equals(AudioSystem.getParameters("VOICE_LVM_Enable"))) {
            AudioSystem.setParameters("VOICE_LVM_Enable=false");
            this.oldLVMDevice = 0;
            if (DEBUG) {
                Slog.i(TAG, "force disable LVM");
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setLVMMode(int direction, int device, int oldIndex, int streamType) {
        if (getMode() == 2 && this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE") != 0) {
            Slog.e(TAG, "MODIFY_PHONE_STATE Permission Denial: setLVMMode");
        } else if (!LOUD_VOICE_MODE_SUPPORT || !isInCall() || streamType != 0) {
            Slog.e(TAG, "setLVMMode abort");
        } else {
            boolean isLVMMode = "true".equals(AudioSystem.getParameters("VOICE_LVM_Enable"));
            boolean isLVMDevice = 2 == device || 1 == device;
            boolean isMaxVolume = getStreamMaxIndex(0) == oldIndex;
            boolean isChangeVolume = false;
            if (isMaxVolume && isLVMDevice && this.oldLVMDevice == device) {
                isChangeVolume = getStreamIndex(0, device) < oldIndex;
            }
            if (DEBUG) {
                Slog.i(TAG, "direction:" + direction + " device:" + device + " oldIndex:" + oldIndex + " isLVMMode:" + isLVMMode + " isChangeVolume:" + isChangeVolume);
            }
            if (!isLVMMode && isMaxVolume && 1 == direction && isLVMDevice) {
                AudioSystem.setParameters("VOICE_LVM_Enable=true");
                if (device == 1) {
                }
                showLVMToast(33685776);
                this.oldLVMDevice = device;
                if (DEBUG) {
                    Slog.i(TAG, "enable LVM after  = " + this.oldLVMDevice);
                }
            } else if (isLVMMode && ((-1 == direction || device != this.oldLVMDevice || isChangeVolume) && this.oldLVMDevice != 0)) {
                AudioSystem.setParameters("VOICE_LVM_Enable=false");
                showLVMToast(33685777);
                this.oldLVMDevice = 0;
                if (DEBUG) {
                    Slog.i(TAG, "disable LVM after oldLVMDevice= " + this.oldLVMDevice);
                }
            }
        }
    }

    private void showLVMToast(int message) {
        if (this.mLVMToast == null) {
            this.mLVMToast = Toast.makeText(new ContextThemeWrapper(this.mContext, this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Toast", null, null)), "Unknown State", 0);
            this.mLVMToast.getWindowParams().privateFlags |= 16;
        }
        try {
            if (DEBUG != 0) {
                Slog.i(TAG, "showLVMToast ");
            }
            this.mLVMToast.setText(message);
            this.mLVMToast.show();
        } catch (Exception e) {
            Slog.e(TAG, "showLVMToast exception: " + e);
        }
    }

    /* access modifiers changed from: protected */
    public void getSoundTriggerSettings(ContentResolver cr) {
        this.mSoundTriggerStatus = Settings.Secure.getInt(cr, KEY_SOUNDTRIGGER_SWITCH, 0);
        this.mSoundTriggerRes = Settings.Secure.getString(cr, "hw_soundtrigger_resource");
        this.mSoundTriggerGram = Settings.Secure.getString(cr, "hw_soundtrigger_grammar");
        if (DEBUG) {
            Slog.i(TAG, "mSoundTriggerStatus = " + this.mSoundTriggerStatus + " mSoundTriggerRes = " + this.mSoundTriggerRes + " mSoundTriggerGram = " + this.mSoundTriggerGram);
        }
    }

    private void setSoundTrigger() {
        if (this.mSoundTriggerStatus == 1) {
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

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v6, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: android.telephony.TelephonyManager} */
    /* JADX WARNING: Multi-variable type inference failed */
    private boolean isInCallingState() {
        TelephonyManager tmgr = null;
        if (this.mContext != null) {
            tmgr = this.mContext.getSystemService("phone");
        }
        if (tmgr == null) {
            return false;
        }
        if (tmgr.getCallState() != 0) {
            if (DEBUG) {
                Slog.v(TAG, "phone is in call state");
            }
            return true;
        }
        if (DEBUG) {
            Slog.v(TAG, "phone is NOT in call state");
        }
        return false;
    }

    private boolean hasSystemPriv(int uid) {
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null) {
            return false;
        }
        String[] pkgs = pm.getPackagesForUid(uid);
        if (pkgs == null) {
            return false;
        }
        for (String pkg : pkgs) {
            try {
                ApplicationInfo info = pm.getApplicationInfo(pkg, 0);
                if (!(info == null || (info.flags & 1) == 0)) {
                    Slog.i(TAG, "system app " + pkg);
                    return true;
                }
            } catch (Exception e) {
                Slog.i(TAG, "not found app " + pkg);
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean checkAudioSettingAllowed(String msg) {
        if (isInCallingState()) {
            int uid = Binder.getCallingUid();
            if (1001 == uid || 1000 == uid || hasSystemPriv(uid)) {
                if (DEBUG) {
                    Slog.v(TAG, "Audio Settings ALLOW from func=" + msg + ", from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                }
                return true;
            }
            if (DEBUG) {
                Slog.v(TAG, "Audio Settings NOT allow from func=" + msg + ", from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            }
            return false;
        }
        Slog.w(TAG, "Audio Settings ALLOW from func=" + msg + ", from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
        return true;
    }

    /* access modifiers changed from: protected */
    public String getPackageNameByPid(int pid) {
        if (pid <= 0) {
            return null;
        }
        ActivityManager activityManager = (ActivityManager) this.mContext.getSystemService("activity");
        if (activityManager == null) {
            return null;
        }
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return null;
        }
        String packageName = null;
        Iterator<ActivityManager.RunningAppProcessInfo> it = appProcesses.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            ActivityManager.RunningAppProcessInfo appProcess = it.next();
            if (appProcess.pid == pid) {
                packageName = appProcess.processName;
                break;
            }
        }
        int indexProcessFlag = -1;
        if (packageName != null) {
            indexProcessFlag = packageName.indexOf(58);
        }
        return indexProcessFlag > 0 ? packageName.substring(0, indexProcessFlag) : packageName;
    }

    private String getVersionName(String packageName) {
        String versionName;
        if (TextUtils.isEmpty(packageName)) {
            return null;
        }
        try {
            versionName = this.mContext.getPackageManager().getPackageInfo(packageName, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "version isn't exist";
            Slog.e(TAG, "getVersionName failed" + packageName);
        }
        return versionName;
    }

    public void checkMicMute() {
        Slog.i(TAG, "AudioException record is not occpied, check mic mute.");
        String recordPkg = getPackageNameByPid(Binder.getCallingPid());
        printCtaifsLog(getRecordAppName(this.mContext, recordPkg), recordPkg, "onTransact", "本地录音");
        if (AudioSystem.isMicrophoneMuted() && getMode() != 2) {
            Slog.i(TAG, "AudioException mic is muted when record! set unmute!");
            sendMsg(this.mAudioHandler, 10000, 1, HW_MIC_MUTE_EXCEPTION_OCCOUR, 0, new AudioExceptionMsg(HW_MIC_MUTE_EXCEPTION_OCCOUR, this.mAudioExceptionRecord.getMutePackageName(), this.mAudioExceptionRecord.getMutePPackageVersion()), 500);
        }
    }

    private String getApplicationLabel(String pkgName) {
        try {
            PackageManager packageManager = this.mContext.getPackageManager();
            return packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkgName, 0)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            Slog.e(TAG, "getApplicationLabel exception", e);
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public boolean checkEnbaleVolumeAdjust() {
        return isAdjustVolumeEnable();
    }

    /* access modifiers changed from: protected */
    public void sendCommForceBroadcast() {
        if (getMode() == 2) {
            int uid = Binder.getCallingUid();
            if (1000 == uid || "com.android.bluetooth".equals(getPackageNameByPid(Binder.getCallingPid()))) {
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

    /* access modifiers changed from: protected */
    public void checkMuteRcvDelay(int curMode, int mode) {
        if (mode == 0 && (curMode == 2 || curMode == 3)) {
            AudioSystem.setParameters(DUALPA_RCV_DEALY_ON);
            sendMsg(this.mAudioHandler, MSG_DUALPA_RCV_DEALY, 0, 0, 0, null, 5000);
        } else if (curMode != mode && (mode == 1 || mode == 2 || mode == 3)) {
            AudioSystem.setParameters(DUALPA_RCV_DEALY_OFF);
        } else if (DEBUG) {
            Slog.i(TAG, "checkMuteRcvDelay Do Nothing");
        }
    }

    private void setAudioSystemParameters() {
        if (HW_SOUND_TRIGGER_SUPPORT) {
            setSoundTrigger();
        }
        if (SPK_RCV_STEREO_SUPPORT) {
            setSpkRcvStereoStatus();
        }
        this.mSetVbrlibFlag = false;
    }

    /* access modifiers changed from: package-private */
    public void reportAudioFlingerRestarted() {
        LogIAware.report(2101, "AudioFlinger");
    }

    /* access modifiers changed from: protected */
    public void processMediaServerRestart() {
        readPersistedSettingsEx(this.mContentResolver);
        setAudioSystemParameters();
        Slog.i(TAG, "mediaserver restart ,start soundtrigger!");
        createAndStartSoundTrigger(true);
        reportAudioFlingerRestarted();
        if (LOW_LATENCY_SUPPORT) {
            initLowlatencyUidsMap();
        }
    }

    private void getSpkRcvStereoState(ContentResolver contentResolver) {
        this.mSpkRcvStereoStatus = Settings.Secure.getInt(contentResolver, "stereo_landscape_portrait", 1);
    }

    private void setSpkRcvStereoStatus() {
        if (this.mSpkRcvStereoStatus == 1) {
            AudioSystem.setParameters(SPK_RCV_STEREO_ON_PARA);
            AudioSystem.setParameters("rotation=0");
        } else if (this.mSpkRcvStereoStatus == 0) {
            AudioSystem.setParameters(SPK_RCV_STEREO_OFF_PARA);
        } else {
            Slog.e(TAG, "setSpkRcvStereoStatus Fail " + this.mSpkRcvStereoStatus);
        }
    }

    public int getRingerModeExternal() {
        int ringerMode = HwAudioService.super.getRingerModeExternal();
        if (!(ringerMode == 0 || this.mNm == null || this.mNm.getZenMode() == 0)) {
            String pkgName = getPackageNameByPid(Binder.getCallingPid());
            if (!(pkgName == null || this.mZenModeWhiteList == null)) {
                for (String temp : this.mZenModeWhiteList) {
                    if (pkgName.equals(temp)) {
                        Slog.i(TAG, "Return ringer mode silent for " + pkgName + " under zen mode");
                        return 0;
                    }
                }
            }
        }
        return ringerMode;
    }

    public void setRingerModeExternal(int toRingerMode, String caller) {
        boolean shouldControll = !isSystemApp(caller);
        if (shouldControll) {
            boolean shouldReport = checkShouldAbortRingerModeChange(caller);
            Slog.i(TAG, "AudioException setRingerModeExternal shouldReport=" + shouldReport + " uid:" + Binder.getCallingUid() + " caller:" + caller);
        }
        int fromRingerMode = getRingerModeExternal();
        HwAudioService.super.setRingerModeExternal(toRingerMode, caller);
        if (fromRingerMode == getRingerModeInternal()) {
            Slog.d(TAG, "AudioException setRingerModeExternal ,but not change");
            return;
        }
        if (shouldControll) {
            alertUserRingerModeChange(caller, fromRingerMode, toRingerMode);
        }
    }

    private void alertUserRingerModeChange(String caller, int fromRingerMode, int ringerMode) {
        sendMsg(this.mAudioHandler, 80, 2, fromRingerMode, ringerMode, new AudioExceptionMsg(HW_RING_MODE_TYPE_EXCEPTION_OCCOUR, caller, getVersionName(caller)), 0);
    }

    private boolean isFirstTimeShow(String caller) {
        String pkgs = Settings.Secure.getStringForUser(this.mContentResolver, "change_ringer_mode_pkgs", -2);
        if (TextUtils.isEmpty(pkgs)) {
            return true;
        }
        return !pkgs.contains(caller);
    }

    private void savePkgNameToDB(String caller) {
        String pkgs;
        if (!TextUtils.isEmpty(caller)) {
            if (TextUtils.isEmpty(Settings.Secure.getStringForUser(this.mContentResolver, "change_ringer_mode_pkgs", -2))) {
                pkgs = caller + "\\";
            } else {
                pkgs = pkgs + caller + "\\";
            }
            Settings.Secure.putStringForUser(this.mContentResolver, "change_ringer_mode_pkgs", pkgs, -2);
        }
    }

    private boolean checkShouldAbortRingerModeChange(String caller) {
        return isScreenOff() || isKeyguardLocked() || !isCallerShowing(caller);
    }

    private boolean isKeyguardLocked() {
        KeyguardManager keyguard = (KeyguardManager) this.mContext.getSystemService("keyguard");
        if (keyguard != null) {
            return keyguard.isKeyguardLocked();
        }
        return false;
    }

    private boolean isCallerShowing(String caller) {
        String pkgName = getTopApp();
        if (pkgName != null) {
            return pkgName.startsWith(caller);
        }
        Slog.e(TAG, "AudioException getTopApp ,but pkgname is null.");
        return false;
    }

    private String getTopApp() {
        return ServiceManager.getService("activity").topAppName();
    }

    private boolean isScreenOff() {
        PowerManager power = (PowerManager) this.mContext.getSystemService("power");
        if (power != null) {
            return !power.isScreenOn();
        }
        return false;
    }

    private boolean isSystemApp(String caller) {
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null) {
            return false;
        }
        try {
            ApplicationInfo info = pm.getApplicationInfo(caller, 0);
            if (info == null || (info.flags & 1) == 0) {
                return false;
            }
            return true;
        } catch (Exception e) {
            Slog.i(TAG, "AudioException not found app:" + caller);
        }
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
            this.mRingerModeToast = Toast.makeText(new ContextThemeWrapper(this.mContext, this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Toast", null, null)), "Unknown State", 0);
        }
        try {
            String messageStr = this.mContext.getString(33685819, new Object[]{callerAppName, this.mContext.getString(fromResId), this.mContext.getString(toResId)});
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
        String messageStr = this.mContext.getString(33685819, new Object[]{callerAppName, this.mContext.getString(fromResId), this.mContext.getString(toResId)});
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext, this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null));
        builder.setMessage(messageStr);
        builder.setCancelable(false);
        builder.setPositiveButton(33685821, new DialogInterface.OnClickListener() {
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
            case 0:
                return 33685823;
            case 1:
                return 33685820;
            case 2:
                return 33685822;
            default:
                Slog.e(TAG, "getRingerModeStrResId RingerMode is error.RingerMode =" + RingerMode);
                return 0;
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: private */
    public static int getCurrentUserId() {
        long ident = Binder.clearCallingIdentity();
        try {
            int i = ActivityManagerNative.getDefault().getCurrentUser().id;
            Binder.restoreCallingIdentity(ident);
            return i;
        } catch (RemoteException e) {
            Slog.w(TAG, "Activity manager not running, nothing we can do assume user 0.");
            Binder.restoreCallingIdentity(ident);
            return 0;
        } catch (Throwable currentUser) {
            Binder.restoreCallingIdentity(ident);
            throw currentUser;
        }
    }

    private int getUidByPkg(String pkgName) {
        int uid = -1;
        if (pkgName == null) {
            return -1;
        }
        try {
            uid = this.mContext.getPackageManager().getPackageUidAsUser(pkgName, getCurrentUserId());
        } catch (Exception e) {
            Slog.w(TAG, "not found uid pkgName:" + pkgName);
        }
        return uid;
    }

    /* access modifiers changed from: private */
    public void updateLowlatencyUidsMap(String pkgName, int packageCmdType) {
        Slog.i(TAG, "updateLowlatencyUidsMap " + pkgName + " packageCmdType " + packageCmdType);
        switch (packageCmdType) {
            case 1:
                int uid = getUidByPkg(pkgName);
                if (uid != -1) {
                    AudioSystem.setParameters("AddLowlatencyPkg=" + uid);
                }
                this.mLowlatencyUidsMap.put(pkgName, Integer.valueOf(uid));
                return;
            case 2:
                AudioSystem.setParameters("RemLowlatencyPkg=" + this.mLowlatencyUidsMap.get(pkgName));
                this.mLowlatencyUidsMap.remove(pkgName);
                return;
            default:
                return;
        }
    }

    private void initLowlatencyUidsMap() {
        this.mLowlatencyUidsMap.clear();
        InputStream in = null;
        XmlPullParser xmlParser = null;
        try {
            in = new FileInputStream(new File(LOW_LATENCY_WHITE_LIST).getPath());
            xmlParser = Xml.newPullParser();
            xmlParser.setInput(in, null);
        } catch (FileNotFoundException e) {
            Slog.e(TAG, "LOW_LATENCY_WHITE_LIST not exist");
        } catch (XmlPullParserException e2) {
            Slog.e(TAG, "LOW_LATENCY_WHITE_LIST can not parse");
        }
        if (xmlParser == null) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e3) {
                    Slog.e(TAG, "LOW_LATENCY_WHITE_LIST IO Close Fail");
                }
            }
            Slog.e(TAG, "LOW_LATENCY_WHITE_LIST is null");
            return;
        }
        while (true) {
            try {
                int next = xmlParser.next();
                int eventType = next;
                if (next == 1) {
                    break;
                } else if (eventType == 2 && xmlParser.getName().equals(NODE_WHITEAPP)) {
                    String packageName = xmlParser.getAttributeValue(null, "package");
                    if (!(packageName == null || packageName.length() == 0)) {
                        this.mWhiteAppName.add(packageName);
                    }
                }
            } catch (XmlPullParserException e4) {
                Slog.e(TAG, "XmlPullParserException");
            } catch (IOException e5) {
                Slog.e(TAG, "IOException");
            }
        }
        List<PackageInfo> packageInfo = this.mContext.getPackageManager().getInstalledPackagesAsUser(0, getCurrentUserId());
        if (packageInfo != null) {
            int packageSize = packageInfo.size();
            int whiteAppSize = this.mWhiteAppName.size();
            for (int i = 0; i < packageSize; i++) {
                String pn = packageInfo.get(i).packageName;
                for (int j = 0; j < whiteAppSize; j++) {
                    if (pn.contains(this.mWhiteAppName.get(j))) {
                        updateLowlatencyUidsMap(pn, 1);
                    }
                }
            }
        }
        if (in != null) {
            try {
                in.close();
            } catch (IOException e6) {
                Slog.e(TAG, "LOW_LATENCY_WHITE_LIST IO Close Fail");
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isLowlatencyPkg(String pkgName) {
        int whiteAppSize = this.mWhiteAppName.size();
        for (int j = 0; j < whiteAppSize; j++) {
            if (pkgName.contains(this.mWhiteAppName.get(j))) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        HwAudioService.super.dump(fd, pw, args);
        try {
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
            pw.print("isCallForeground=");
            pw.println(isCallForeground);
            pw.print("DUAL_SMARTPA_SUPPORT=");
            pw.println(DUAL_SMARTPA_SUPPORT);
            pw.print("SPK_RCV_STEREO_SUPPORT=");
            pw.println(SPK_RCV_STEREO_SUPPORT);
            pw.print("mSpkRcvStereoStatus=");
            pw.println(this.mSpkRcvStereoStatus);
            pw.print("mLowlatencyUidsMap=");
            pw.println(this.mLowlatencyUidsMap);
            pw.print("mWhiteAppName=");
            pw.println(this.mWhiteAppName);
            pw.print("mVoipOptimizeInGameMode =");
            pw.println(mVoipOptimizeInGameMode);
        } catch (SecurityException e) {
            Slog.w(TAG, "enforceCallingOrSelfPermission dump failed ");
        }
    }

    private void showDisableMicrophoneToast() {
        Toast toast = Toast.makeText(new ContextThemeWrapper(this.mContext, this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Toast", null, null)), 33685911, 0);
        toast.getWindowParams().privateFlags |= 16;
        toast.show();
    }

    private void showDisableHeadphoneToast() {
        Toast toast = Toast.makeText(new ContextThemeWrapper(this.mContext, this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Toast", null, null)), 33685913, 0);
        toast.getWindowParams().privateFlags |= 16;
        toast.show();
    }

    public void setWiredDeviceConnectionState(int type, int state, String address, String name, String caller) {
        if ((-1375700964 & type) == 0) {
            HwAudioService.super.setWiredDeviceConnectionState(type, state, address, name, caller);
        } else if (HwDeviceManager.disallowOp(31)) {
            Slog.i(TAG, "disallow headphone by MDM");
            if (state == 1 && type > 0) {
                sendMsg(this.mAudioHandler, 92, 2, 0, 0, null, 0);
            }
        } else {
            this.mHwAudioServiceEx.updateTypeCNotify(type, state);
            if (mSws6Capability && state == 1 && (604004352 & type) != 0) {
                sendMsg(this.mAudioHandler, 93, 2, 0, 0, name, 0);
            }
            synchronized (this.mConnectedDevices) {
                if (state == 1) {
                    try {
                        this.mHeadphones.add(new HeadphoneInfo(type, address, name, caller));
                    } catch (Throwable th) {
                        throw th;
                    }
                } else {
                    int i = 0;
                    while (true) {
                        if (i >= this.mHeadphones.size()) {
                            break;
                        } else if (this.mHeadphones.get(i).mDeviceType == type) {
                            this.mHeadphones.remove(i);
                            break;
                        } else {
                            i++;
                        }
                    }
                }
                HwAudioService.super.setWiredDeviceConnectionState(type, state, address, name, caller);
            }
        }
    }

    private void disableHeadPhone() {
        synchronized (this.mConnectedDevices) {
            for (int i = 0; i < this.mHeadphones.size(); i++) {
                HeadphoneInfo info = this.mHeadphones.get(i);
                HwAudioService.super.setWiredDeviceConnectionState(info.mDeviceType, 0, info.mDeviceAddress, info.mDeviceName, info.mCaller);
            }
            this.mHeadphones.clear();
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002f, code lost:
        if (r2 == null) goto L_0x0039;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:?, code lost:
        r0.notifyIncallModeChange(r2.getPid(), r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0039, code lost:
        r0.notifyIncallModeChange(0, 0);
     */
    public void updateAftPolicy() {
        IHwAftPolicyService hwAft = HwAftPolicyManager.getService();
        if (hwAft != null) {
            try {
                int mode = getMode();
                synchronized (this.mSetModeDeathHandlers) {
                    if (this.mSetModeDeathHandlers.isEmpty()) {
                        Slog.i(TAG, "updateAftPolicy SetModeDeathHandlers is empty, trun off aft");
                        hwAft.notifyIncallModeChange(0, 0);
                        return;
                    }
                    AudioService.SetModeDeathHandler hdlr = (AudioService.SetModeDeathHandler) this.mSetModeDeathHandlers.get(0);
                }
            } catch (RemoteException e) {
                Slog.e(TAG, "binder call throw " + e);
            }
        }
    }

    private void desktopModeChanged(int desktopMode) {
        Slog.v(TAG, "desktopModeChanged desktopMode = " + desktopMode);
        if (this.mDesktopMode != desktopMode) {
            this.mDesktopMode = desktopMode;
            SystemProperties.set(PROP_DESKTOP_MODE, "" + desktopMode);
            MediaFocusControl mediaFocusControl = this.mMediaFocusControl;
            boolean z = true;
            if (this.mDesktopMode != 1) {
                z = false;
            }
            mediaFocusControl.desktopModeChanged(z);
        }
    }

    public int trackPlayer(PlayerBase.PlayerIdCard pic) {
        if (this.mDesktopMode == 1) {
            pic.setPkgName(getPackageNameByPid(Binder.getCallingPid()));
        }
        return HwAudioService.super.trackPlayer(pic);
    }

    public void playerEvent(int piid, int event) {
        HwAudioService.super.playerEvent(piid, event);
        this.mHwAudioServiceEx.notifyHiResIcon(event);
        this.mHwAudioServiceEx.notifyStartDolbyDms(event);
        recordLastPlaybackTime(piid, event);
        recoverFromExceptionMode(event);
    }

    private void recoverFromExceptionMode(int event) {
        if (event == 2) {
            AudioService.SetModeDeathHandler hdlr = setMode3Handler();
            if (hdlr != null) {
                List<AudioPlaybackConfiguration> list = this.mPlaybackMonitor.getActivePlaybackConfigurations(true);
                int i = 0;
                while (i < list.size()) {
                    AudioPlaybackConfiguration conf = list.get(i);
                    if (conf == null || conf.getClientPid() != hdlr.getPid()) {
                        i++;
                    } else {
                        return;
                    }
                }
                int rePid = -1;
                String recordPid = AudioSystem.getParameters("active_record_pid");
                if (recordPid != null) {
                    try {
                        rePid = Integer.parseInt(recordPid);
                    } catch (NumberFormatException e) {
                        NumberFormatException numberFormatException = e;
                        Slog.v(TAG, "NumberFormatException: " + recordPid);
                    }
                }
                long recoverTime = System.currentTimeMillis();
                boolean needRecoverForRecord = recoverTime - this.mLastStopAudioRecordTime > 5000;
                List<AudioPlaybackConfiguration> list2 = list;
                boolean needRecoverForPlay = recoverTime - this.mLastStopPlayBackTime > 5000;
                int rePid2 = rePid;
                boolean isSetModeTimePassBy = recoverTime - this.mLastSetMode3Time > 5000;
                if (rePid2 == hdlr.getPid() || isBluetoothScoOn() || !needRecoverForRecord || !needRecoverForPlay || !isSetModeTimePassBy) {
                    boolean z = isSetModeTimePassBy;
                } else {
                    long token = Binder.clearCallingIdentity();
                    try {
                        String pkgName = getPackageNameByPid(hdlr.getPid());
                        StringBuilder sb = new StringBuilder();
                        sb.append("mLastStopAudioRecordTime: ");
                        boolean z2 = needRecoverForPlay;
                        boolean z3 = isSetModeTimePassBy;
                        try {
                            sb.append(this.mLastStopAudioRecordTime);
                            sb.append(" mLastStopPlayBackTime: ");
                            sb.append(this.mLastStopPlayBackTime);
                            sb.append(" mLastSetMode3Time: ");
                            sb.append(this.mLastSetMode3Time);
                            sb.append(" pid: ");
                            sb.append(hdlr.getPid());
                            sb.append(" PkgName: ");
                            sb.append(pkgName);
                            Slog.v(TAG, sb.toString());
                            HwMediaMonitorManager.writeLogMsg(916010201, 3, HW_AUDIO_MODE_TYPE_EXCEPTION_OCCOUR, "OAE6:" + hdlr.getPid() + " :" + pkgName);
                            if (pkgName != null) {
                                setModeInt(0, hdlr.getBinder(), hdlr.getPid(), pkgName);
                            } else {
                                Slog.e(TAG, "packageName is null for pid: " + hdlr.getPid());
                            }
                            Binder.restoreCallingIdentity(token);
                        } catch (Throwable th) {
                            th = th;
                            Binder.restoreCallingIdentity(token);
                            throw th;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        boolean z4 = needRecoverForPlay;
                        boolean z5 = isSetModeTimePassBy;
                        Binder.restoreCallingIdentity(token);
                        throw th;
                    }
                }
            }
        }
    }

    private AudioService.SetModeDeathHandler setMode3Handler() {
        if (this.mSetModeDeathHandlers == null) {
            return null;
        }
        try {
            Iterator iter = this.mSetModeDeathHandlers.iterator();
            while (iter.hasNext()) {
                Object object = iter.next();
                if (object instanceof AudioService.SetModeDeathHandler) {
                    AudioService.SetModeDeathHandler hdlr = (AudioService.SetModeDeathHandler) object;
                    if (hdlr != null && hdlr.getMode() == 3) {
                        return hdlr;
                    }
                }
            }
        } catch (ConcurrentModificationException e) {
            Slog.i(TAG, "Exist ConcurrentModificationException", e);
        } catch (Exception e2) {
            Slog.i(TAG, "Exception: ", e2);
        }
        return null;
    }

    private void recordLastRecordTime(int state, int pid) {
        if (state == 1) {
            AudioService.SetModeDeathHandler hdlr = setMode3Handler();
            if (hdlr != null && hdlr.getPid() == pid) {
                this.mLastStopAudioRecordTime = System.currentTimeMillis();
                Slog.i(TAG, "mLastStopAudioRecordTime: " + this.mLastStopAudioRecordTime);
            }
        }
    }

    private void recordLastPlaybackTime(int piid, int event) {
        if (event == 4) {
            AudioService.SetModeDeathHandler hdlr = setMode3Handler();
            if (hdlr != null) {
                List<AudioPlaybackConfiguration> list = this.mPlaybackMonitor.getActivePlaybackConfigurations(true);
                int playbackNumber = list.size();
                int i = 0;
                while (true) {
                    if (i >= playbackNumber) {
                        break;
                    }
                    AudioPlaybackConfiguration conf = list.get(i);
                    if (conf != null && conf.getPlayerState() == 4 && conf.getClientPid() == hdlr.getPid() && conf.getPlayerInterfaceId() == piid) {
                        this.mLastStopPlayBackTime = System.currentTimeMillis();
                        Slog.i(TAG, "mLastStopPlayBackTime: " + this.mLastStopPlayBackTime);
                        break;
                    }
                    i++;
                }
            }
        }
    }

    public void setMode(int mode, IBinder cb, String callingPackage) {
        if (SUPPORT_GAME_MODE) {
            if (mode == 1 && this.IS_IN_GAMEMODE) {
                this.mHwAudioServiceEx.setDolbyEffect(0);
            } else if (mode == 0 && this.IS_IN_GAMEMODE) {
                this.mHwAudioServiceEx.setDolbyEffect(3);
            }
        }
        HwAudioService.super.setMode(mode, cb, callingPackage);
        AudioService.SetModeDeathHandler hdlr = setMode3Handler();
        if (hdlr != null && mode == 3 && hdlr.getBinder() == cb) {
            this.mLastSetMode3Time = System.currentTimeMillis();
            Slog.i(TAG, "mLastSetMode3Time: " + this.mLastSetMode3Time);
        }
    }

    /* access modifiers changed from: private */
    public void bindIAwareSdk() {
        Slog.i(TAG, "registerFgChangedCallback ForeAppTypeListener");
        AwareIntelligentRecg recg = AwareIntelligentRecg.getInstance();
        if (recg != null) {
            recg.registerFgChangedCallback(new ForeAppTypeListener());
        } else {
            Slog.e(TAG, "AwareIntelligentRecg.getInstance is null");
        }
    }

    private void setTypecParamToAudioSystem(String name) {
        if (name == null || name.length() <= 0) {
            Slog.e(TAG, "name is null or empty");
            return;
        }
        Slog.i(TAG, "setTypecParamToAudioSystem enter name:" + name);
        UsbManager usbMgr = (UsbManager) this.mContext.getSystemService("usb");
        if (usbMgr == null) {
            Slog.e(TAG, "get usb service failed");
            return;
        }
        HashMap<String, UsbDevice> deviceMap = usbMgr.getDeviceList();
        if (deviceMap != null && deviceMap.size() > 0) {
            for (Map.Entry<String, UsbDevice> entry : deviceMap.entrySet()) {
                UsbDevice device = (UsbDevice) entry.getValue();
                if (device != null && setUsbDeviceParamToAudioSystem(name, device)) {
                    break;
                }
            }
        } else {
            Slog.e(TAG, "usb size == 0");
        }
    }

    private boolean setUsbDeviceParamToAudioSystem(String name, UsbDevice device) {
        String product = device.getProductName();
        if (product == null || name.indexOf(product) < 0) {
            return false;
        }
        String serialNum = device.getSerialNumber();
        String manufacture = device.getManufacturerName();
        if (serialNum == null || manufacture == null) {
            Slog.i(TAG, "Found usbdevice but get serialNum or manufacture failed");
        } else {
            String keyVaulePairs = "SWS_TYPEC_MANUFACTURER=" + manufacture + CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER;
            String keyVaulePairs2 = keyVaulePairs + SWS_TYPEC_SERIALNO + "=" + serialNum + CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER;
            AudioSystem.setParameters(keyVaulePairs2 + SWS_TYPEC_PRODUCT_NAME + "=" + product + CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
            Slog.i(TAG, "Found usbdevice:" + name + " keyVaulePairs:" + keyVaulePairs);
        }
        return true;
    }
}
