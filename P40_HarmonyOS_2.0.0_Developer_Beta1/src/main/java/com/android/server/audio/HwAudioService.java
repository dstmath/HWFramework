package com.android.server.audio;

import android.aft.HwAftPolicyManager;
import android.aft.IHwAftPolicyService;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hdm.HwDeviceManager;
import android.media.AudioAttributes;
import android.media.AudioPlaybackConfiguration;
import android.media.AudioRoutesInfo;
import android.media.AudioSystem;
import android.media.HwMediaMonitorManager;
import android.media.IAudioRoutesObserver;
import android.media.PlayerBase;
import android.media.audiopolicy.AudioVolumeGroup;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManagerInternal;
import android.provider.Settings;
import android.rms.iaware.IAwareSdk;
import android.rms.iaware.IForegroundAppTypeCallback;
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
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.appactcontrol.AppActConstant;
import com.android.server.audio.AbsAudioService;
import com.android.server.audio.AudioService;
import com.android.server.audio.report.AudioExceptionMsg;
import com.android.server.cust.utils.HwCustPkgNameConstant;
import com.android.server.intellicom.common.SmartDualCardConsts;
import com.android.server.pm.PackageManagerServiceEx;
import com.android.server.pm.UserManagerService;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.swing.HwSwingMotionGestureConstant;
import com.android.server.wifipro.WifiProCommonUtils;
import com.android.server.wm.HwSplitBarConstants;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.IGameObserver;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import com.huawei.server.HwBasicPlatformFactory;
import com.huawei.server.sidetouch.DefaultHwSideTouchPolicy;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwAudioService extends AudioService {
    private static final String ACTION_COMMFORCE = "huawei.intent.action.COMMFORCE";
    private static final String ACTION_INCALL_EXTRA = "IsForegroundActivity";
    private static final String ACTION_INCALL_SCREEN = "InCallScreenIsForegroundActivity";
    private static final int ADJUST_VOLUME_DEFAULT = 1000;
    private static final int ADJUST_VOLUME_MAX = 1100;
    private static final int ADJUST_VOLUME_MIN = 900;
    private static final String BLUETOOTH_PKG_NAME = "com.android.bluetooth";
    private static final String CHIP_PLATFORM_NAME = SystemProperties.get("ro.board.platform", "UNDEFINED");
    private static final int COUNT_FIRST = 1;
    private static final int COUNT_SECOND = 2;
    private static final String CURRENT_PRODUCT_NAME = SystemProperties.get("ro.product.device", (String) null);
    private static final int DEFAULT_STREAM_TYPE = 1;
    private static final int DEFAULT_VOLUME = 0;
    private static final int DEVICE_IN_HEADPHONE = -2111825904;
    private static final int DEVICE_OUT_HEADPHONE = 604004364;
    private static final int DOLBY_EFFECT_MODE_GAME = 3;
    private static final int DUALPA_RCV_DEALY_DURATION = 5000;
    private static final String DUALPA_RCV_DEALY_OFF = "dualpa_security_delay=0";
    private static final String DUALPA_RCV_DEALY_ON = "dualpa_security_delay=1";
    private static final String EXCLUSIVE_APP_NAME = "com.rohdeschwarz.sit.topsecapp";
    private static final String EXCLUSIVE_PRODUCT_NAME = "HWH1711-Q";
    private static final int GAMEMODE_BACKGROUND = 2;
    private static final int GAMEMODE_FOREGROUND = 9;
    public static final int HW_AUDIO_EXCEPTION_OCCOUR = -1000;
    public static final int HW_AUDIO_MODE_TYPE_EXCEPTION_OCCOUR = -1001;
    public static final int HW_AUDIO_TRACK_OVERFLOW_TYPE_EXCEPTION_OCCOUR = -1002;
    private static final int HW_KARAOKE_EFFECT_BIT = 2;
    private static final boolean HW_KARAOKE_EFFECT_ENABLED = ((SystemProperties.getInt("ro.config.hw_media_flags", 0) & 2) != 0);
    public static final int HW_MIC_MUTE_EXCEPTION_OCCOUR = -2002;
    public static final int HW_RING_MODE_TYPE_EXCEPTION_OCCOUR = -2001;
    public static final int HW_SCO_CONNECT_EXCEPTION_OCCOUR = -2003;
    private static final int IAWARE_DELAY_MESSAGE = 0;
    private static final int IAWARE_DELAY_TIME = 2000;
    private static final int IAWARE_RECONNECT_COUNT = 5;
    private static final boolean IS_HUAWEI_SAFE_MEDIA_CONFIG = SystemProperties.getBoolean("ro.config.huawei_safe_media", true);
    private static final int JUDGE_MILLISECONDS = 5000;
    private static final boolean LOW_LATENCY_SUPPORT = SystemProperties.getBoolean("persist.media.lowlatency.enable", false);
    private static final String LOW_LATENCY_WHITE_LIST = "/odm/etc/audio/low_latency/white_list.xml";
    private static final int MILLISECONDS_TO_SECONDS_BASE = 1000;
    private static final int MSG_AUDIO_EXCEPTION_OCCUR = 10000;
    private static final int MSG_DISABLE_HEADPHONE = 91;
    private static final int MSG_DUALPA_RCV_DEALY = 71;
    private static final int MSG_RINGER_MODE_CHANGE = 80;
    private static final int MSG_SHOW_DISABLE_HEADPHONE_TOAST = 92;
    private static final int MSG_SHOW_DISABLE_MICROPHONE_TOAST = 90;
    private static final int MSG_UPDATE_AFT_POLICY = 93;
    private static final int MUSIC_STREAM_TYPE = 3;
    private static final int MUTE_VOLUME = 0;
    private static final String NODE_ATTR_PACKAGE = "package";
    private static final String NODE_WHITEAPP = "whiteapp";
    private static final int PACKAGE_ADDED = 1;
    private static final int PACKAGE_REMOVED = 2;
    private static final String PERMISSION_COMMFORCE = "android.permission.COMM_FORCE";
    public static final int PID_BIT_WIDE = 21;
    private static final String PROP_DESKTOP_MODE = "sys.desktop.mode";
    private static final int RADIO_UID = 1001;
    private static final int RECORDSTATE_STOPPED = 1;
    private static final int SECURITY_VOLUME_INDEX = SystemProperties.getInt("ro.config.hw.security_volume", 0);
    private static final int SETTINGS_RING_TYPE = 2;
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
    private static final boolean SUPPORT_GAME_MODE = SystemProperties.getBoolean("ro.config.dolby_game_mode", false);
    private static final boolean SUPPORT_SIDE_TOUCH = (!SystemProperties.get("ro.config.hw_curved_side_disp", "").equals(""));
    private static final int SYSTEM_UID = 1000;
    private static final String TAG = "HwAudioService";
    private static final String TAG_CTAIFS = "ctaifs ";
    private static final long THREE_SEC_LIMITED = 3000;
    private static final int TRANSACTION_ADJUST_VALUME = 1100;
    private static final int TRANSACTION_DESK_TOP = 1103;
    private static final int TRANSACTION_DISABLE_MICPHONE = 1004;
    private static final int TRANSACTION_ENABLE_VALUME = 1101;
    private static final int TRANSACTION_FORCE_USE = 100;
    private static final int TRANSACTION_HEAD_PHONE = 1005;
    private static final int TRANSACTION_IS_CALL_AVALEBAL = 1104;
    private static final int TRANSACTION_KARAOKE_CALL_BACK = 1106;
    private static final int TRANSACTION_KARAOKE_EFFECT_ENALE = 1105;
    private static final int TRANSACTION_LAST_RECORD = 1002;
    private static final int TRANSACTION_NO_EXCEPTION = 101;
    private static final int TRANSACTION_STOP_SOUND_TRIGER = 1102;
    private static final int UNKNOWN_IAWARE_APP_TYPE = -1;
    private static final boolean VOIP_OPTIMIZE_IN_GAMEMODE = SystemProperties.getBoolean("ro.config.gameassist_voipopt", false);
    private static boolean sIsCallForeground = false;
    private static PhoneStateListener sPhoneListener = new PhoneStateListener() {
        /* class com.android.server.audio.HwAudioService.AnonymousClass1 */

        @Override // android.telephony.PhoneStateListener
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            if (state == 0) {
                SystemProperties.set("persist.sys.audio_mode", "0");
            } else if (state == 1) {
                SystemProperties.set("persist.sys.audio_mode", "1");
            } else if (state == 2) {
                SystemProperties.set("persist.sys.audio_mode", "2");
            }
        }
    };
    private AudioExceptionRecord mAudioExceptionRecord = null;
    private int mBindIAwareCount;
    private IBinder mBinder;
    private ContentResolver mContentResolver;
    private Context mContext;
    private int mCount = 0;
    protected volatile int mDesktopMode = -1;
    private long mDialogShowTime = 0;
    private boolean mEnableAdjustVolume = true;
    private EnableVolumeClient mEnableVolumeClient = null;
    private final Object mEnableVolumeLock = new Object();
    private ForeAppTypeListener mForeTypelistener = new ForeAppTypeListener();
    private ArrayList<HeadphoneInfo> mHeadphones = new ArrayList<>();
    private int mHeadsetSwitchState = 0;
    private IAwareDeathRecipient mIAwareDeathRecipient = new IAwareDeathRecipient();
    private IAwareHandler mIAwareHandler;
    private IBinder mIAwareSdkService;
    private boolean mIsFmConnected = false;
    private boolean mIsInGameMode = false;
    private boolean mIsInSuperReceiverMode = false;
    private boolean mIsLinkDeathRecipient = false;
    private boolean mIsShowingDialog = false;
    private Toast mLVMToast = null;
    private long mLastSetMode3Time = -1;
    private long mLastStopAudioRecordTime = -1;
    private long mLastStopPlayBackTime = -1;
    private final BroadcastReceiver mLowlatencyReceiver = new BroadcastReceiver() {
        /* class com.android.server.audio.HwAudioService.AnonymousClass2 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Uri uri;
            String packageName;
            if (intent != null && (uri = intent.getData()) != null && (packageName = uri.getSchemeSpecificPart()) != null) {
                String action = intent.getAction();
                if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
                    HwAudioService.this.removeLowlatencyAndWhiteAppUId(packageName);
                } else if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
                    HwAudioService.this.addLowlatencyAndWhiteAppUId(packageName);
                } else {
                    Slog.v(HwAudioService.TAG, "mLowlatencyReceiver");
                }
            }
        }
    };
    private Map<String, Integer> mLowlatencyUidsMap = new ArrayMap();
    private Map<Long, String> mMapPkgs = new HashMap();
    private int mOldInCallDevice = 0;
    private int mOldLVmDevice = 0;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.server.audio.HwAudioService.AnonymousClass3 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (action != null && action.equals("android.intent.action.FM")) {
                    int state = intent.getIntExtra("state", 0);
                    boolean isConnected = HwAudioService.this.mIsFmConnected;
                    if (state == 0 && isConnected) {
                        AudioSystem.setDeviceConnectionState(1048576, 0, "", "", 0);
                        HwAudioService.this.mIsFmConnected = false;
                    } else if (state != 1 || isConnected) {
                        Slog.v(HwAudioService.TAG, "state = " + state);
                    } else {
                        AudioSystem.setDeviceConnectionState(1048576, 1, "", "", 0);
                        HwAudioService.this.mIsFmConnected = true;
                    }
                } else if (action != null && HwAudioService.ACTION_INCALL_SCREEN.equals(action)) {
                    boolean unused = HwAudioService.sIsCallForeground = intent.getBooleanExtra(HwAudioService.ACTION_INCALL_EXTRA, true);
                } else if (action != null && "android.intent.action.PHONE_STATE".equals(action)) {
                    if (TelephonyManager.EXTRA_STATE_IDLE.equals(intent.getStringExtra("state"))) {
                        HwAudioService.this.forceExitLVMMode();
                    }
                    if (AbsAudioService.IS_SUPER_RECEIVER_ENABLED) {
                        HwAudioService.this.closeSuperReceiverMode();
                    }
                } else if (action == null || !"android.intent.action.BOOT_COMPLETED".equals(action)) {
                    Slog.v(HwAudioService.TAG, "action does not match");
                } else {
                    HwAudioService.this.bindIAwareSdk();
                }
            }
        }
    };
    private Toast mRingerModeToast = null;
    private SetICallBackForKaraokeHandler mSetICallBackForKaraokeHandler = null;
    private boolean mSetVbrlibFlag = false;
    private DefaultHwSideTouchPolicy mSideTouchPolicy;
    private HwSoundTrigger mSoundTrigger;
    private int mSpkRcvStereoStatus = -1;
    private long mStartTime = 0;
    private long mSuperReceiverStartTime = 0;
    private int mType = -1;
    private UserManagerInternal mUserManagerInternal;
    private List<String> mWhiteAppName = new ArrayList();

    static /* synthetic */ int access$1608(HwAudioService x0) {
        int i = x0.mBindIAwareCount;
        x0.mBindIAwareCount = i + 1;
        return i;
    }

    /* access modifiers changed from: protected */
    public boolean usingHwSafeMediaConfig() {
        return IS_HUAWEI_SAFE_MEDIA_CONFIG;
    }

    /* access modifiers changed from: protected */
    public int getHwSafeMediaVolumeIndex() {
        return SECURITY_VOLUME_INDEX * 10;
    }

    /* access modifiers changed from: protected */
    public boolean isHwSafeMediaVolumeEnabled() {
        return SECURITY_VOLUME_INDEX > 0;
    }

    /* access modifiers changed from: protected */
    public boolean checkMMIRunning() {
        return AppActConstant.VALUE_TRUE.equals(SystemProperties.get("runtime.mmitest.isrunning", AppActConstant.VALUE_FALSE));
    }

    public HwAudioService(Context context) {
        super(context);
        TelephonyManager tm;
        Slog.i(TAG, TAG);
        this.mContext = context;
        registerReceivers();
        if ((context.getSystemService("phone") instanceof TelephonyManager) && (tm = (TelephonyManager) context.getSystemService("phone")) != null) {
            tm.listen(sPhoneListener, 32);
        }
        ActivityManagerEx.registerGameObserver(new HwAudioGameObserver());
        this.mSoundTrigger = new HwSoundTrigger(context);
        if (SUPPORT_SIDE_TOUCH) {
            this.mSideTouchPolicy = HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_SIDE_TOUCH_PART_FACTORY_IMPL).getHwSideTouchPolicyInstance(context);
        }
    }

    private void registerReceivers() {
        if (this.mContext != null) {
            IntentFilter intentFilter = new IntentFilter("android.intent.action.FM");
            intentFilter.addAction(ACTION_INCALL_SCREEN);
            intentFilter.addAction("android.intent.action.PHONE_STATE");
            intentFilter.addAction("android.intent.action.REBOOT");
            intentFilter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_ACTION_SHUTDOWN);
            intentFilter.addAction("android.intent.action.BOOT_COMPLETED");
            this.mContext.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, intentFilter, null, null);
            this.mContentResolver = this.mContext.getContentResolver();
            this.mAudioExceptionRecord = new AudioExceptionRecord();
            readPersistedSettingsEx(this.mContentResolver);
            setAudioSystemParameters();
            HwServiceFactory.IHwDrmDialogService iService = HwServiceFactory.getHwDrmDialogService();
            if (iService != null) {
                iService.startDrmDialogService(this.mContext);
            } else {
                Slog.e(TAG, "getHwDrmDialogService fail");
            }
            this.mUserManagerInternal = (UserManagerInternal) LocalServices.getService(UserManagerInternal.class);
            if (LOW_LATENCY_SUPPORT || HW_KARAOKE_EFFECT_ENABLED) {
                IntentFilter lowlatencyIntentFilter = new IntentFilter();
                lowlatencyIntentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
                lowlatencyIntentFilter.addAction("android.intent.action.PACKAGE_ADDED");
                lowlatencyIntentFilter.addDataScheme(NODE_ATTR_PACKAGE);
                this.mContext.registerReceiverAsUser(this.mLowlatencyReceiver, UserHandle.ALL, lowlatencyIntentFilter, null, null);
                if (LOW_LATENCY_SUPPORT) {
                    initLowlatencyUidsMap();
                }
            }
            ((TelephonyManager) this.mContext.getSystemService("phone")).listen(sPhoneListener, 32);
            ActivityManagerEx.registerGameObserver(new HwAudioGameObserver());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeLowlatencyAndWhiteAppUId(String packageName) {
        if (LOW_LATENCY_SUPPORT && isLowlatencyPkg(packageName)) {
            updateLowlatencyUidsMap(packageName, 2);
        }
        if (HW_KARAOKE_EFFECT_ENABLED && this.mHwAudioServiceEx.isKaraokeWhiteListApp(packageName)) {
            Slog.i(TAG, "uninstall combine white app");
            this.mHwAudioServiceEx.removeKaraokeWhiteAppUIDByPkgName(packageName);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void addLowlatencyAndWhiteAppUId(String packageName) {
        if (LOW_LATENCY_SUPPORT && isLowlatencyPkg(packageName)) {
            updateLowlatencyUidsMap(packageName, 1);
        }
        if (HW_KARAOKE_EFFECT_ENABLED && this.mHwAudioServiceEx.isKaraokeWhiteListApp(packageName)) {
            this.mHwAudioServiceEx.setKaraokeWhiteAppUIDByPkgName(packageName);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isSupportSoundToVibrateMode() {
        return SystemProperties.getInt("ro.config.gameassist_soundtovibrate", 0) == 1;
    }

    /* access modifiers changed from: private */
    public class HwAudioGameObserver extends IGameObserver.Stub {
        private HwAudioGameObserver() {
        }

        public void onGameListChanged() {
            Slog.v(HwAudioService.TAG, "onGameListChanged !");
        }

        public void onGameStatusChanged(String packageName, int event) {
            if (packageName != null) {
                Slog.d(HwAudioService.TAG, "onGameStatusChanged event = " + event);
                if (HwAudioService.this.isSupportSoundToVibrateMode() && Settings.Secure.getInt(HwAudioService.this.mContext.getContentResolver(), "sound_to_vibrate_effect", 1) == 1) {
                    if (event != 1) {
                        if (event == 2) {
                            AudioSystem.setParameters("vbrmode=background");
                            return;
                        } else if (event != 4) {
                            Slog.w(HwAudioService.TAG, "onGameStatusChanged event not in foreground or background!");
                            return;
                        }
                    }
                    if (!HwAudioService.this.mSetVbrlibFlag) {
                        Slog.d(HwAudioService.TAG, "HwAudioGameObserver set mSetVbrlibFalg = true means firstly load the vbr lib!");
                        AudioSystem.setParameters("vbrEnter=on");
                        HwAudioService.this.mSetVbrlibFlag = true;
                    }
                    AudioSystem.setParameters("vbrmode=front;gamepackagename=" + packageName);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onUserBackground(int userId) {
        checkWithUserSwith(userId, true);
    }

    /* access modifiers changed from: protected */
    public void onUserForeground(int userId) {
        checkWithUserSwith(userId, false);
    }

    private void checkWithUserSwith(int userId, boolean isBackground) {
        if (userId >= 0) {
            boolean isPrimary = UserManagerService.getInstance().getUserInfo(userId).isPrimary();
            Slog.i(TAG, "onUserBackground isPrimary : " + isPrimary);
            if (isPrimary) {
                this.mSoundTrigger.start(!isBackground);
            }
        }
    }

    public AudioRoutesInfo startWatchingRoutes(IAudioRoutesObserver observer) {
        Slog.d(TAG, "startWatchingRoutes");
        if (!this.mSoundTrigger.startWatchingRoutes(observer)) {
            return HwAudioService.super.startWatchingRoutes(observer);
        }
        return null;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r25v0, resolved type: android.os.Parcel */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r5v2, types: [boolean, int] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        boolean start = false;
        if (code == 101) {
            sendMsg(this.mAudioHandler, 8, 2, 1, data.readInt() == 1 ? 1 : 0, null, 0);
            reply.writeNoException();
            return true;
        } else if (code == 102) {
            reply.writeNoException();
            return true;
        } else if (code == 1003) {
            data.enforceInterface("android.media.IAudioService");
            data.readString();
            recordLastRecordTime(data.readInt(), data.readInt());
            reply.writeNoException();
            return true;
        } else if (code == 1005) {
            data.enforceInterface("android.media.IAudioService");
            sendMsg(this.mAudioHandler, MSG_SHOW_DISABLE_MICROPHONE_TOAST, 2, 0, 0, null, 20);
            reply.writeNoException();
            return true;
        } else if (code != 1006) {
            switch (code) {
                case TRANSACTION_ENABLE_VALUME /* 1101 */:
                    ?? isAdjustVolumeEnable = isAdjustVolumeEnable();
                    Slog.i(TAG, "isAdjustVolumeEnable transaction called. result:" + (isAdjustVolumeEnable == true ? 1 : 0));
                    reply.writeNoException();
                    reply.writeInt(isAdjustVolumeEnable);
                    return true;
                case TRANSACTION_STOP_SOUND_TRIGER /* 1102 */:
                    IBinder callerToken = data.readStrongBinder();
                    if (data.readInt() != 0) {
                        start = true;
                    }
                    Slog.i(TAG, "enableVolumeAdjust  transaction called.enable:" + start);
                    enableVolumeAdjust(start, Binder.getCallingUid(), callerToken);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_DESK_TOP /* 1103 */:
                    data.enforceInterface("android.media.IAudioService");
                    if (data.readInt() != 0) {
                        start = true;
                    }
                    boolean start2 = this.mSoundTrigger.start(start);
                    reply.writeNoException();
                    reply.writeInt(start2 ? 1 : 0);
                    this.mSoundTrigger.stopWatchingRoutes();
                    return true;
                case TRANSACTION_IS_CALL_AVALEBAL /* 1104 */:
                    int arg0 = data.readInt();
                    Slog.i(TAG, "desktopModeChanged transaction mode:" + arg0);
                    desktopModeChanged(arg0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_KARAOKE_EFFECT_ENALE /* 1105 */:
                    data.enforceInterface("android.media.IAudioService");
                    reply.writeNoException();
                    reply.writeBoolean(isScoAvailableOffCall());
                    return true;
                case TRANSACTION_KARAOKE_CALL_BACK /* 1106 */:
                    data.enforceInterface("android.media.IAudioService");
                    reply.writeNoException();
                    reply.writeBoolean(isHwKaraokeEffectEnable());
                    return true;
                case 1107:
                    data.enforceInterface("android.media.IAudioService");
                    setICallBackForKaraoke(data.readStrongBinder());
                    reply.writeNoException();
                    return true;
                default:
                    return HwAudioService.super.onTransact(code, data, reply, flags);
            }
        } else {
            data.enforceInterface("android.media.IAudioService");
            sendMsg(this.mAudioHandler, 91, 2, 0, 0, null, 0);
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
            SetICallBackForKaraokeHandler setICallBackForKaraokeHandler = this.mSetICallBackForKaraokeHandler;
            if (setICallBackForKaraokeHandler == null) {
                this.mSetICallBackForKaraokeHandler = new SetICallBackForKaraokeHandler(cb);
            } else if (setICallBackForKaraokeHandler.getBinder() == cb) {
                Slog.d(TAG, "mSetICallBackForKaraokeHandler cb:" + cb + " is already linked.");
            } else {
                this.mSetICallBackForKaraokeHandler.release();
                this.mSetICallBackForKaraokeHandler = new SetICallBackForKaraokeHandler(cb);
            }
        }
    }

    /* access modifiers changed from: private */
    public class SetICallBackForKaraokeHandler implements IBinder.DeathRecipient {
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

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            Slog.w(HwAudioService.TAG, "Karaoke client died");
            AudioSystem.setParameters("Karaoke_enable=disable");
            release();
        }

        public void release() {
            IBinder iBinder = this.mCb;
            if (iBinder != null) {
                iBinder.unlinkToDeath(this, 0);
                this.mCb = null;
            }
            HwAudioService.this.mSetICallBackForKaraokeHandler = null;
        }

        public IBinder getBinder() {
            return this.mCb;
        }
    }

    public boolean isScoAvailableOffCall() {
        return this.mDeviceBroker.isScoAvailableOffCall();
    }

    public static void printCtaifsLog(String applicationName, String packageName, String callingMethod, String description) {
        Slog.i(TAG_CTAIFS, "<" + applicationName + ">[" + applicationName + "][" + callingMethod + "] " + description);
    }

    public static String getRecordAppName(Context context, String packageName) {
        PackageManager pm;
        if (context == null || (pm = context.getPackageManager()) == null) {
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

    private boolean isAdjustVolumeEnable() {
        boolean z;
        synchronized (this.mEnableVolumeLock) {
            z = this.mEnableAdjustVolume;
        }
        return z;
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
                if (callerPkg == null || !callerPkg.equals(clientPkg)) {
                    Slog.w(TAG, "Just allowed one caller use the interface until older caller die.older.");
                } else {
                    this.mEnableAdjustVolume = enable;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class EnableVolumeClient implements IBinder.DeathRecipient {
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

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            synchronized (HwAudioService.this.mEnableVolumeLock) {
                Slog.i(HwAudioService.TAG, " EnableVolumeClient died");
                HwAudioService.this.mEnableAdjustVolume = true;
                release();
            }
        }

        public void release() {
            IBinder iBinder = this.mCb;
            if (iBinder != null) {
                iBinder.unlinkToDeath(this, 0);
                this.mCb = null;
            }
            HwAudioService.this.mEnableVolumeClient = null;
        }
    }

    /* access modifiers changed from: protected */
    public void readPersistedSettingsEx(ContentResolver cr) {
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
        PackageManager pm;
        if (packageName == null || packageName.length() == 0 || (pm = this.mContext.getPackageManager()) == null || pm.checkPermission("android.permission.CAMERA", packageName) != 0 || pm.checkPermission("android.permission.RECORD_AUDIO", packageName) != 0) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void handleMessageEx(Message msg) {
        int i = msg.what;
        if (i == MSG_DUALPA_RCV_DEALY) {
            AudioSystem.setParameters(DUALPA_RCV_DEALY_OFF);
        } else if (i != 80) {
            if (i == 10099) {
                handleSuperReceiverProcess(msg);
            } else if (i == 10000) {
                AudioExceptionMsg tempMsg = null;
                if (msg.obj != null && (msg.obj instanceof AudioExceptionMsg)) {
                    tempMsg = (AudioExceptionMsg) msg.obj;
                }
                onAudioException(msg.arg1, tempMsg);
            } else if (i != 10001) {
                switch (i) {
                    case MSG_SHOW_DISABLE_MICROPHONE_TOAST /* 90 */:
                        showDisableMicrophoneToast();
                        return;
                    case 91:
                        disableHeadPhone();
                        return;
                    case 92:
                        showDisableHeadphoneToast();
                        return;
                    case 93:
                        updateAftPolicyInternal();
                        return;
                    default:
                        return;
                }
            } else if (LOUD_VOICE_MODE_SUPPORT) {
                handleLVMModeChangeProcess(msg.arg1, msg.obj);
            }
        } else if (msg.obj == null) {
            Slog.e(TAG, "MSG_RINGER_MODE_CHANGE msg obj is null!");
        } else {
            ringModeChange(msg);
        }
    }

    private void openSuperReceiverMode() {
        Slog.i(TAG, "open super receiver");
        if (!this.mIsInSuperReceiverMode) {
            this.mIsInSuperReceiverMode = true;
            this.mSuperReceiverStartTime = System.currentTimeMillis();
        }
        AudioSystem.setParameters("super_receiver_mode=on");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void closeSuperReceiverMode() {
        Slog.i(TAG, "close super receiver");
        if (this.mIsInSuperReceiverMode) {
            this.mIsInSuperReceiverMode = false;
            HwMediaMonitorManager.writeBigData(916600020, "SUPER_RECIVIER_USE_TIME", (int) ((System.currentTimeMillis() - this.mSuperReceiverStartTime) / 1000), 1000);
        }
        AudioSystem.setParameters("super_receiver_mode=off");
    }

    private void handleSuperReceiverProcess(Message msg) {
        if (msg == null) {
            Slog.e(TAG, "Super Receiver msg is null");
        } else if (this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE") != 0) {
            Slog.e(TAG, "Super Receiver Permission Denial: setSuperReceiverMode");
        } else if (isInCall()) {
            if (msg.arg1 == 0) {
                closeSuperReceiverMode();
            } else if (msg.obj == null || !(msg.obj instanceof AbsAudioService.DeviceVolumeState)) {
                Slog.e(TAG, "Message object error");
            } else {
                AbsAudioService.DeviceVolumeState myObj = (AbsAudioService.DeviceVolumeState) msg.obj;
                if (myObj.mstreamType != 0) {
                    Slog.e(TAG, "handleSuperReceiverProcess abort for not stream error");
                } else if (myObj.mDevice == 1) {
                    changeSuperReceiverMode(myObj);
                } else {
                    closeSuperReceiverMode();
                }
            }
        }
    }

    private void changeSuperReceiverMode(AbsAudioService.DeviceVolumeState myObj) {
        if (getStreamMaxIndex(0) == myObj.mOldIndex) {
            Slog.i(TAG, "changeSuperReceiverMode");
            if (myObj.mDirection == 1) {
                openSuperReceiverMode();
            }
            if (myObj.mDirection == -1) {
                closeSuperReceiverMode();
            }
            if (myObj.mDirection == 0 && getStreamIndex(0, myObj.mDevice) - myObj.mOldIndex < 0) {
                closeSuperReceiverMode();
            }
        }
    }

    private void ringModeChange(Message msg) {
        AudioExceptionMsg tempMsg = null;
        String caller = null;
        if (msg.obj instanceof AudioExceptionMsg) {
            tempMsg = (AudioExceptionMsg) msg.obj;
        }
        if (tempMsg != null) {
            caller = tempMsg.getMsgPackagename();
        }
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

    public void onAudioException(int exceptionId, AudioExceptionMsg exceptionMsg) {
        if (!this.mSystemReady) {
            Slog.e(TAG, "AudioException,but system is not ready! ");
        }
        if (exceptionId != -1001) {
            switch (exceptionId) {
                case HW_SCO_CONNECT_EXCEPTION_OCCOUR /* -2003 */:
                    Slog.w(TAG, "AudioException HW_SCO_CONNECT_EXCEPTION_OCCOUR");
                    return;
                case HW_MIC_MUTE_EXCEPTION_OCCOUR /* -2002 */:
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
                case HW_RING_MODE_TYPE_EXCEPTION_OCCOUR /* -2001 */:
                    Slog.w(TAG, "AudioException HW_RING_MODE_TYPE_EXCEPTION_OCCOUR");
                    return;
                default:
                    onAudioExceptionDefault(exceptionId);
                    return;
            }
        } else {
            Slog.w(TAG, "AudioException HW_AUDIO_MODE_TYPE_EXCEPTION_OCCOUR, setModeInt AudioSystem.MODE_NORMAL");
        }
    }

    private void onAudioExceptionDefault(int exceptionId) {
        int tmp = -exceptionId;
        if ((-(tmp >> 21)) == -1002) {
            int exceptionPid = tmp - 2101346304;
            if (exceptionPid > 0) {
                String packageName = getPackageNameByPid(exceptionPid);
                getVersionName(packageName);
                if (packageName != null) {
                    Slog.e(TAG, "AudioTrack_Overflow pid = " + exceptionPid);
                    HwMediaMonitorManager.writeLogMsg(916010207, 3, -1002, "OAE5");
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
        TelecomManager telecomManager = null;
        if (this.mContext.getSystemService("telecom") instanceof TelecomManager) {
            telecomManager = (TelecomManager) this.mContext.getSystemService("telecom");
        }
        if (telecomManager == null) {
            return false;
        }
        return telecomManager.isInCall();
    }

    /* access modifiers changed from: protected */
    public int getOldInCallDevice(int mode) {
        if (mode == 2) {
            this.mOldInCallDevice = getInCallDevice();
        } else if (mode == 0) {
            this.mOldInCallDevice = 0;
        } else {
            this.mOldInCallDevice = 0;
        }
        return this.mOldInCallDevice;
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
        if (this.mOldInCallDevice != device) {
            AudioSystem.setParameters("VOICE_ALGO_DeviceChange=true");
            this.mOldInCallDevice = device;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void forceExitLVMMode() {
        if (AppActConstant.VALUE_TRUE.equals(AudioSystem.getParameters("VOICE_LVM_Enable"))) {
            AudioSystem.setParameters("VOICE_LVM_Enable=false");
            this.mOldLVmDevice = 0;
        }
    }

    /* access modifiers changed from: protected */
    public void setLVMMode(int direction, int device, int oldIndex, int streamType) {
        if (getMode() == 2 && this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE") != 0) {
            Slog.e(TAG, "MODIFY_PHONE_STATE Permission Denial: setLVMMode");
        } else if (!LOUD_VOICE_MODE_SUPPORT || !isInCall() || streamType != 0) {
            Slog.e(TAG, "setLVMMode abort");
        } else {
            boolean isLVMMode = AppActConstant.VALUE_TRUE.equals(AudioSystem.getParameters("VOICE_LVM_Enable"));
            boolean isLVMDevice = device == 2 || device == 1;
            boolean isMaxVolume = getStreamMaxIndex(0) == oldIndex;
            boolean isChangeVolume = false;
            if (isMaxVolume && isLVMDevice && this.mOldLVmDevice == device) {
                isChangeVolume = getStreamIndex(0, device) < oldIndex;
            }
            if (!isLVMMode && isMaxVolume && direction == 1 && isLVMDevice) {
                AudioSystem.setParameters("VOICE_LVM_Enable=true");
                showLVMToast(33685776);
                this.mOldLVmDevice = device;
            } else if (!isLVMMode || ((direction != -1 && device == this.mOldLVmDevice && !isChangeVolume) || this.mOldLVmDevice == 0)) {
                Slog.v(TAG, "LVMMode not set");
            } else {
                AudioSystem.setParameters("VOICE_LVM_Enable=false");
                showLVMToast(33685777);
                this.mOldLVmDevice = 0;
            }
        }
    }

    private void showLVMToast(int message) {
        Slog.i(TAG, "do not show LVM Toast");
    }

    private boolean isInCallingState() {
        TelephonyManager teleMgr = null;
        Context context = this.mContext;
        if (context != null && (context.getSystemService("phone") instanceof TelephonyManager)) {
            teleMgr = (TelephonyManager) this.mContext.getSystemService("phone");
        }
        if (teleMgr == null || teleMgr.getCallState() == 0) {
            return false;
        }
        return true;
    }

    private boolean hasSystemPriv(int uid) {
        String[] pkgs;
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null || (pkgs = pm.getPackagesForUid(uid)) == null) {
            return false;
        }
        for (String pkg : pkgs) {
            try {
                ApplicationInfo info = pm.getApplicationInfo(pkg, 0);
                if (!(info == null || (info.flags & 1) == 0)) {
                    return true;
                }
            } catch (Exception e) {
                Slog.i(TAG, "not found app");
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean checkAudioSettingAllowed(String msg) {
        if (isInCallingState()) {
            int uid = Binder.getCallingUid();
            if (uid == 1001 || uid == 1000 || hasSystemPriv(uid)) {
                return true;
            }
            return false;
        }
        Slog.w(TAG, "Audio Settings ALLOW from func=" + msg + ", from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
        return true;
    }

    /* access modifiers changed from: protected */
    public String getPackageNameByPid(int pid) {
        List<ActivityManager.RunningAppProcessInfo> appProcesses;
        if (pid <= 0) {
            return null;
        }
        ActivityManager activityManager = null;
        if (this.mContext.getSystemService("activity") instanceof ActivityManager) {
            activityManager = (ActivityManager) this.mContext.getSystemService("activity");
        }
        if (activityManager == null || (appProcesses = activityManager.getRunningAppProcesses()) == null) {
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
        if (TextUtils.isEmpty(packageName)) {
            return null;
        }
        try {
            return this.mContext.getPackageManager().getPackageInfo(packageName, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Slog.e(TAG, "getVersionName failed");
            return "version isn't exist";
        }
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
            if (Binder.getCallingUid() == 1000 || "com.android.bluetooth".equals(getPackageNameByPid(Binder.getCallingPid()))) {
                try {
                    this.mContext.sendBroadcastAsUser(new Intent(ACTION_COMMFORCE), UserHandle.getUserHandleForUid(UserHandle.getCallingUserId()), PERMISSION_COMMFORCE);
                } catch (Exception e) {
                    Slog.e(TAG, "sendCommForceBroadcast exception");
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void checkMuteRcvDelay(int curMode, int mode) {
        if (mode == 0 && (curMode == 2 || curMode == 3)) {
            AudioSystem.setParameters(DUALPA_RCV_DEALY_ON);
            sendMsg(this.mAudioHandler, MSG_DUALPA_RCV_DEALY, 0, 0, 0, null, HwSplitBarConstants.DARK_MODE_DELAY);
        } else if (curMode == mode || !(mode == 1 || mode == 2 || mode == 3)) {
            Slog.v(TAG, "checkMuteRcvDelay, curMode = " + curMode + ", mode = " + mode);
        } else {
            AudioSystem.setParameters(DUALPA_RCV_DEALY_OFF);
        }
    }

    private void setAudioSystemParameters() {
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
        this.mSoundTrigger.start(true);
        reportAudioFlingerRestarted();
        if (LOW_LATENCY_SUPPORT) {
            initLowlatencyUidsMap();
        }
    }

    private void getSpkRcvStereoState(ContentResolver contentResolver) {
        this.mSpkRcvStereoStatus = Settings.Secure.getInt(contentResolver, "stereo_landscape_portrait", 1);
    }

    private void setSpkRcvStereoStatus() {
        int i = this.mSpkRcvStereoStatus;
        if (i == 1) {
            AudioSystem.setParameters(SPK_RCV_STEREO_ON_PARA);
            AudioSystem.setParameters("rotation=0");
        } else if (i == 0) {
            AudioSystem.setParameters(SPK_RCV_STEREO_OFF_PARA);
        } else {
            Slog.e(TAG, "setSpkRcvStereoStatus Fail " + this.mSpkRcvStereoStatus);
        }
    }

    public void setRingerModeExternal(int toRingerMode, String caller) {
        boolean shouldControll = !isSystemApp(caller);
        if (shouldControll) {
            Slog.i(TAG, "AudioException setRingerModeExternal shouldReport=" + checkShouldAbortRingerModeChange(caller) + " uid:" + Binder.getCallingUid() + " caller:" + caller);
        }
        int fromRingerMode = getRingerModeExternal();
        HwAudioService.super.setRingerModeExternal(toRingerMode, caller);
        HwMediaMonitorManager.writeBigData(916600008, "SET_RINGER_MODE", caller, "2: " + String.valueOf(toRingerMode));
        if (fromRingerMode == getRingerModeInternal()) {
            Slog.d(TAG, "AudioException setRingerModeExternal ,but not change");
        } else if (shouldControll) {
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
            String pkgs2 = Settings.Secure.getStringForUser(this.mContentResolver, "change_ringer_mode_pkgs", -2);
            if (TextUtils.isEmpty(pkgs2)) {
                pkgs = caller + "\\";
            } else {
                pkgs = pkgs2 + caller + "\\";
            }
            Settings.Secure.putStringForUser(this.mContentResolver, "change_ringer_mode_pkgs", pkgs, -2);
        }
    }

    private boolean checkShouldAbortRingerModeChange(String caller) {
        return isScreenOff() || isKeyguardLocked() || !isCallerShowing(caller);
    }

    private boolean isKeyguardLocked() {
        KeyguardManager keyguard = null;
        if (this.mContext.getSystemService("keyguard") instanceof KeyguardManager) {
            keyguard = (KeyguardManager) this.mContext.getSystemService("keyguard");
        }
        if (keyguard != null) {
            return keyguard.isKeyguardLocked();
        }
        return false;
    }

    private boolean isCallerShowing(String caller) {
        String pkgName = getTopActivityPackageName();
        if (pkgName != null) {
            return pkgName.startsWith(caller);
        }
        Slog.e(TAG, "AudioException getTopApp ,but pkgname is null.");
        return false;
    }

    private String getTopActivityPackageName() {
        Object service = this.mContext.getSystemService("activity");
        if (service != null && (service instanceof ActivityManager)) {
            try {
                List<ActivityManager.RunningTaskInfo> tasks = ((ActivityManager) service).getRunningTasks(1);
                if (tasks != null) {
                    if (!tasks.isEmpty()) {
                        ComponentName topActivity = tasks.get(0).topActivity;
                        if (topActivity != null) {
                            return topActivity.getPackageName();
                        }
                    }
                }
                return null;
            } catch (SecurityException e) {
                Slog.e(TAG, "getTopActivityPackageName SecurityException");
            }
        }
        return null;
    }

    private boolean isScreenOff() {
        PowerManager power = null;
        if (this.mContext.getSystemService("power") instanceof PowerManager) {
            power = (PowerManager) this.mContext.getSystemService("power");
        }
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
            Slog.i(TAG, "AudioException not found app");
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
            String messageStr = this.mContext.getString(33685819, callerAppName, this.mContext.getString(fromResId), this.mContext.getString(toResId));
            Slog.i(TAG, "AudioException showRingerModeToast ");
            this.mRingerModeToast.setText(messageStr);
            this.mRingerModeToast.show();
        } catch (Exception e) {
            Slog.e(TAG, "AudioException showRingerModeToast exception");
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
        String messageStr = context.getString(33685819, callerAppName, context.getString(fromResId), this.mContext.getString(toResId));
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext, this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null));
        builder.setMessage(messageStr);
        builder.setCancelable(false);
        builder.setPositiveButton(33685821, new DialogInterface.OnClickListener() {
            /* class com.android.server.audio.HwAudioService.AnonymousClass4 */

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        Dialog dialog = builder.create();
        dialog.getWindow().setType(2003);
        dialog.show();
        Slog.i(TAG, "AudioException showRingerModeDialog show ");
        this.mIsShowingDialog = true;
    }

    private int getRingerModeStrResId(int ringerMode) {
        if (ringerMode == 0) {
            return 33685823;
        }
        if (ringerMode == 1) {
            return 33685820;
        }
        if (ringerMode == 2) {
            return 33685822;
        }
        Slog.e(TAG, "getRingerModeStrResId RingerMode is error.RingerMode =" + ringerMode);
        return 0;
    }

    /* JADX INFO: finally extract failed */
    private static int getCurrentUserId() {
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
        if (pkgName == null) {
            return -1;
        }
        try {
            return this.mContext.getPackageManager().getPackageUidAsUser(pkgName, getCurrentUserId());
        } catch (Exception e) {
            Slog.w(TAG, "not found uid");
            return -1;
        }
    }

    private void updateLowlatencyUidsMap(String pkgName, int packageCmdType) {
        Slog.i(TAG, "updateLowlatencyUidsMap packageCmdType " + packageCmdType);
        if (packageCmdType == 1) {
            int uid = getUidByPkg(pkgName);
            if (uid != -1) {
                AudioSystem.setParameters("AddLowlatencyPkg=" + uid);
            }
            this.mLowlatencyUidsMap.put(pkgName, Integer.valueOf(uid));
        } else if (packageCmdType == 2) {
            AudioSystem.setParameters("RemLowlatencyPkg=" + this.mLowlatencyUidsMap.get(pkgName));
            this.mLowlatencyUidsMap.remove(pkgName);
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
        updateWhiteApp(xmlParser);
        if (in != null) {
            try {
                in.close();
            } catch (IOException e4) {
                Slog.e(TAG, "LOW_LATENCY_WHITE_LIST IO Close Fail");
            }
        }
    }

    private void updateWhiteApp(XmlPullParser xmlParser) {
        try {
            int eventType = xmlParser.next();
            while (eventType != 1) {
                if (eventType == 2) {
                    updateWhiteAppName(xmlParser);
                }
                eventType = xmlParser.next();
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
        } catch (XmlPullParserException e) {
            Slog.e(TAG, "XmlPullParserException");
        } catch (IOException e2) {
            Slog.e(TAG, "IOException");
        }
    }

    private void updateWhiteAppName(XmlPullParser xmlParser) {
        String packageName;
        if (xmlParser.getName().equals(NODE_WHITEAPP) && (packageName = xmlParser.getAttributeValue(null, NODE_ATTR_PACKAGE)) != null && packageName.length() != 0) {
            this.mWhiteAppName.add(packageName);
        }
    }

    private boolean isLowlatencyPkg(String pkgName) {
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
            pw.print("IS_HUAWEI_SAFE_MEDIA_CONFIG =");
            pw.println(IS_HUAWEI_SAFE_MEDIA_CONFIG);
            pw.print("SECURITY_VOLUME_INDEX =");
            pw.println(SECURITY_VOLUME_INDEX);
            pw.print("LOUD_VOICE_MODE_SUPPORT=");
            pw.println(LOUD_VOICE_MODE_SUPPORT);
            pw.print("Lound Voice State:");
            pw.println(AudioSystem.getParameters("VOICE_LVM_Enable"));
            pw.print("HW_SOUND_TRIGGER_SUPPORT=");
            pw.println(HW_SOUND_TRIGGER_SUPPORT);
            pw.print("HW_KARAOKE_EFFECT_ENABLED=");
            pw.println(HW_KARAOKE_EFFECT_ENABLED);
            pw.print("sIsCallForeground=");
            pw.println(sIsCallForeground);
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
            pw.print("VOIP_OPTIMIZE_IN_GAMEMODE =");
            pw.println(VOIP_OPTIMIZE_IN_GAMEMODE);
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
        if (type == 33554432 || type == -2130706432) {
            Slog.i(TAG, "update virtual audio state" + type);
            if (this.mContext.getPackageManager() == null) {
                Slog.e(TAG, "PackageManager is null");
            } else if (this.mContext.checkCallingOrSelfPermission("com.huawei.permission.INVOKE_VIRTUAL_AUDIO") != 0) {
                Slog.e(TAG, "No com.huawei.permission.INVOKE_VIRTUAL_AUDIO permission");
            } else {
                HwAudioService.super.setWiredDeviceConnectionState(type, state, address, name, caller);
            }
        } else if ((-1507821540 & type) == 0) {
            HwAudioService.super.setWiredDeviceConnectionState(type, state, address, name, caller);
        } else if (HwDeviceManager.disallowOp(31)) {
            Slog.i(TAG, "disallow headphone by MDM");
            if (state == 1 && type > 0) {
                sendMsg(this.mAudioHandler, 92, 2, 0, 0, null, 0);
            }
        } else {
            this.mHwAudioServiceEx.updateTypeCNotify(type, state, name);
            if (state == 1) {
                this.mHeadphones.add(new HeadphoneInfo(type, address, name, caller));
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

    private void disableHeadPhone() {
        for (int i = 0; i < this.mHeadphones.size(); i++) {
            HeadphoneInfo info = this.mHeadphones.get(i);
            HwAudioService.super.setWiredDeviceConnectionState(info.mDeviceType, 0, info.mDeviceAddress, info.mDeviceName, info.mCaller);
        }
        this.mHeadphones.clear();
    }

    /* access modifiers changed from: private */
    public static class HeadphoneInfo {
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

    /* access modifiers changed from: protected */
    public void updateAftPolicy() {
        sendMsg(this.mAudioHandler, 93, 2, 0, 0, null, 0);
    }

    private void updateAftPolicyInternal() {
        if (this.mSystemReady) {
            int ownerPid = 0;
            synchronized (this.mDeviceBroker.mSetModeLock) {
                if (this.mSetModeDeathHandlers.isEmpty()) {
                    notifyUpdateAftPolicy(0, 0);
                    return;
                }
                AudioService.SetModeDeathHandler modeDeathHandler = (AudioService.SetModeDeathHandler) this.mSetModeDeathHandlers.get(0);
                if (modeDeathHandler != null) {
                    ownerPid = modeDeathHandler.getPid();
                }
            }
            if (ownerPid != 0) {
                notifyUpdateAftPolicy(ownerPid, getMode());
            } else {
                notifyUpdateAftPolicy(0, 0);
            }
        }
    }

    private void notifyUpdateAftPolicy(int ownerPid, int mode) {
        IHwAftPolicyService hwAft = HwAftPolicyManager.getService();
        if (hwAft != null) {
            try {
                hwAft.notifyIncallModeChange(ownerPid, mode);
            } catch (RemoteException e) {
                Slog.e(TAG, "notifyIncallModeChange RemoteException");
            }
        }
        Object hwPolicyRelatedObject = LocalServices.getService(WindowManagerPolicy.class);
        if (hwPolicyRelatedObject != null && (hwPolicyRelatedObject instanceof HwPhoneWindowManager)) {
            ((HwPhoneWindowManager) hwPolicyRelatedObject).notifyUpdateAftPolicy(ownerPid, mode);
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
        pic.setPkgName(getPackageNameByPid(Binder.getCallingPid()));
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
        AudioService.SetModeDeathHandler hdlr;
        Throwable th;
        if (event == 2 && (hdlr = setMode3Handler()) != null) {
            List<AudioPlaybackConfiguration> list = this.mPlaybackMonitor.getActivePlaybackConfigurations(true);
            for (int i = 0; i < list.size(); i++) {
                AudioPlaybackConfiguration conf = list.get(i);
                if (conf != null && conf.getClientPid() == hdlr.getPid()) {
                    return;
                }
            }
            int rePid = -1;
            String recordPid = AudioSystem.getParameters("active_record_pid");
            if (recordPid != null) {
                try {
                    rePid = Integer.parseInt(recordPid);
                } catch (NumberFormatException e) {
                    Slog.v(TAG, "NumberFormatException: " + recordPid);
                }
            }
            long recoverTime = System.currentTimeMillis();
            boolean needRecoverForRecord = recoverTime - this.mLastStopAudioRecordTime > HwSwingMotionGestureConstant.HOVER_SCREEN_OFF_THRESHOLD;
            boolean needRecoverForPlay = recoverTime - this.mLastStopPlayBackTime > HwSwingMotionGestureConstant.HOVER_SCREEN_OFF_THRESHOLD;
            boolean isSetModeTimePassBy = recoverTime - this.mLastSetMode3Time > HwSwingMotionGestureConstant.HOVER_SCREEN_OFF_THRESHOLD;
            if (rePid != hdlr.getPid() && !isBluetoothScoOn() && needRecoverForRecord && needRecoverForPlay && isSetModeTimePassBy) {
                long token = Binder.clearCallingIdentity();
                try {
                    String pkgName = getPackageNameByPid(hdlr.getPid());
                    StringBuilder sb = new StringBuilder();
                    sb.append("mLastStopAudioRecordTime: ");
                    try {
                        sb.append(this.mLastStopAudioRecordTime);
                        sb.append(" mLastStopPlayBackTime: ");
                        sb.append(this.mLastStopPlayBackTime);
                        sb.append(" mLastSetMode3Time: ");
                        sb.append(this.mLastSetMode3Time);
                        sb.append(" pid: ");
                        sb.append(hdlr.getPid());
                        Slog.v(TAG, sb.toString());
                        HwMediaMonitorManager.writeLogMsg(916010201, 3, (int) HW_AUDIO_MODE_TYPE_EXCEPTION_OCCOUR, "OAE6:" + hdlr.getPid());
                        if (pkgName != null) {
                            setModeInt(0, hdlr.getBinder(), hdlr.getPid(), pkgName);
                        } else {
                            Slog.e(TAG, "packageName is null for pid: " + hdlr.getPid());
                        }
                        Binder.restoreCallingIdentity(token);
                    } catch (Throwable th2) {
                        th = th2;
                        Binder.restoreCallingIdentity(token);
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    Binder.restoreCallingIdentity(token);
                    throw th;
                }
            }
        }
    }

    private AudioService.SetModeDeathHandler setMode3Handler() {
        AudioService.SetModeDeathHandler hdlr;
        if (this.mSetModeDeathHandlers == null) {
            return null;
        }
        try {
            Iterator iter = this.mSetModeDeathHandlers.iterator();
            while (iter.hasNext()) {
                Object object = iter.next();
                if ((object instanceof AudioService.SetModeDeathHandler) && (hdlr = (AudioService.SetModeDeathHandler) object) != null && hdlr.getMode() == 3) {
                    return hdlr;
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
        AudioService.SetModeDeathHandler hdlr;
        if (state == 1 && (hdlr = setMode3Handler()) != null && hdlr.getPid() == pid) {
            this.mLastStopAudioRecordTime = System.currentTimeMillis();
            Slog.i(TAG, "mLastStopAudioRecordTime: " + this.mLastStopAudioRecordTime);
        }
    }

    private void recordLastPlaybackTime(int piid, int event) {
        AudioService.SetModeDeathHandler hdlr;
        if (event == 4 && (hdlr = setMode3Handler()) != null) {
            List<AudioPlaybackConfiguration> list = this.mPlaybackMonitor.getActivePlaybackConfigurations(true);
            int playbackNumber = list.size();
            for (int i = 0; i < playbackNumber; i++) {
                AudioPlaybackConfiguration conf = list.get(i);
                if (conf != null && conf.getPlayerState() == 4 && conf.getClientPid() == hdlr.getPid() && conf.getPlayerInterfaceId() == piid) {
                    this.mLastStopPlayBackTime = System.currentTimeMillis();
                    Slog.i(TAG, "mLastStopPlayBackTime: " + this.mLastStopPlayBackTime);
                    return;
                }
            }
        }
    }

    public void setMode(int mode, IBinder cb, String callingPackage) {
        if (SUPPORT_GAME_MODE) {
            if (mode == 1 && this.mIsInGameMode) {
                this.mHwAudioServiceEx.setDolbyEffect(0);
            } else if (mode != 0 || !this.mIsInGameMode) {
                Slog.v(TAG, "mode = " + mode + ", mIsInGameMode = " + this.mIsInGameMode);
            } else {
                this.mHwAudioServiceEx.setDolbyEffect(3);
            }
        }
        HwAudioService.super.setMode(mode, cb, callingPackage);
        HwMediaMonitorManager.writeBigData(916600008, "SET_MODE", callingPackage, "1: " + String.valueOf(mode));
        AudioService.SetModeDeathHandler hdlr = setMode3Handler();
        if (hdlr != null && mode == 3 && hdlr.getBinder() == cb) {
            this.mLastSetMode3Time = System.currentTimeMillis();
            Slog.i(TAG, "mLastSetMode3Time: " + this.mLastSetMode3Time);
        }
    }

    /* access modifiers changed from: private */
    public class IAwareDeathRecipient implements IBinder.DeathRecipient {
        private IAwareDeathRecipient() {
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            HwAudioService.this.mBindIAwareCount = 0;
            HwAudioService.this.bindIAwareSdk();
        }
    }

    /* access modifiers changed from: private */
    public class IAwareHandler extends Handler {
        public IAwareHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                HwAudioService.this.bindIAwareSdk();
                HwAudioService.access$1608(HwAudioService.this);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void bindIAwareSdk() {
        IBinder iBinder = this.mIAwareSdkService;
        if (iBinder != null) {
            iBinder.unlinkToDeath(this.mIAwareDeathRecipient, 0);
        }
        Slog.i(TAG, "Intent.ACTION_BOOT_COMPLETED rigsterForegroundAppTypeWithCallback ForeAppTypeListener");
        this.mIAwareSdkService = ServiceManager.getService("IAwareSdkService");
        IBinder iBinder2 = this.mIAwareSdkService;
        if (iBinder2 != null) {
            try {
                iBinder2.linkToDeath(this.mIAwareDeathRecipient, 0);
                IAwareSdk.rigsterForegroundAppTypeWithCallback(this.mForeTypelistener);
            } catch (RemoteException e) {
                Slog.e(TAG, "IAwareSdkService linkToDeath error");
                this.mIAwareSdkService = null;
                postDelayBindIAware();
            }
        } else {
            Slog.e(TAG, "IAwareSdkService getService is null");
            postDelayBindIAware();
        }
    }

    private void postDelayBindIAware() {
        if (this.mIAwareHandler == null) {
            this.mIAwareHandler = new IAwareHandler(this.mContext.getMainLooper());
            this.mBindIAwareCount = 0;
        }
        if (this.mBindIAwareCount < 5) {
            this.mIAwareHandler.sendEmptyMessageDelayed(0, 2000);
        } else {
            IAwareSdk.rigsterForegroundAppTypeWithCallback(this.mForeTypelistener);
        }
    }

    /* access modifiers changed from: private */
    public class ForeAppTypeListener extends IForegroundAppTypeCallback {
        public ForeAppTypeListener() {
        }

        public void reportForegroundAppType(int type, String pkg) {
            Slog.i(HwAudioService.TAG, "now type = " + HwAudioService.this.mType + ",reportForegroundAppType = " + type + "systemload = " + SystemProperties.get("sys.iaware.type", "-1") + "VOIP_OPTIMIZE_IN_GAMEMODE" + HwAudioService.VOIP_OPTIMIZE_IN_GAMEMODE);
            if (type != HwAudioService.this.mType) {
                HwAudioService.this.mType = type;
                SystemProperties.set("sys.iaware.type", type + "");
                if (type != -1) {
                    SystemProperties.set("sys.iaware.filteredType", type + "");
                }
            }
            if (type != 9) {
                if (HwAudioService.SUPPORT_GAME_MODE && HwAudioService.this.mIsInGameMode) {
                    HwAudioService.this.mHwAudioServiceEx.setDolbyEffect(0);
                    HwAudioService.this.mIsInGameMode = false;
                    AudioSystem.setParameters("dolby_game_mode=off");
                }
                if (HwAudioService.VOIP_OPTIMIZE_IN_GAMEMODE) {
                    AudioSystem.setParameters("game_mode=off");
                    return;
                }
                return;
            }
            HwAudioService.this.mHwAudioServiceEx.setGameForeground();
            if (HwAudioService.SUPPORT_GAME_MODE && !HwAudioService.this.mIsInGameMode && !AudioSystem.isStreamActive(3, 0)) {
                Slog.i(HwAudioService.TAG, "Music steam not start");
                if (HwAudioService.this.mHwAudioServiceEx.setDolbyEffect(3)) {
                    HwAudioService.this.mIsInGameMode = true;
                } else {
                    Slog.d(HwAudioService.TAG, "set game mode fail.");
                }
            }
            if (HwAudioService.VOIP_OPTIMIZE_IN_GAMEMODE) {
                Slog.d(HwAudioService.TAG, "support voip optinization during game mode.");
                AudioSystem.setParameters("game_mode=on");
            }
        }
    }

    /* access modifiers changed from: protected */
    public void updateDefaultStream() {
        ContentResolver contentResolver = this.mContentResolver;
        if (contentResolver == null) {
            Slog.i(TAG, "mContentResolver is null.");
            return;
        }
        int streamType = Settings.System.getIntForUser(contentResolver, "default_volume_key_control", 1, -2);
        Slog.i(TAG, "get default stream : " + streamType);
        if (streamType == 2) {
            this.mDefaultVolStream = 2;
        } else {
            this.mDefaultVolStream = 3;
        }
    }

    /* access modifiers changed from: protected */
    public boolean shouldStopAdjustVolume(int uid) {
        DefaultHwSideTouchPolicy defaultHwSideTouchPolicy;
        if (!SUPPORT_SIDE_TOUCH) {
            return false;
        }
        if (uid != 1000) {
            Slog.d(TAG, "volumetrigger not system uid : " + uid);
            return false;
        } else if (this.mContext == null || (defaultHwSideTouchPolicy = this.mSideTouchPolicy) == null || !defaultHwSideTouchPolicy.checkVolumeTriggerStatusAndReset()) {
            return false;
        } else {
            Slog.d(TAG, "volumetrigger shouldStopAdjustVolume");
            return true;
        }
    }

    private long getCurrentMillTimes() {
        return Calendar.getInstance().getTimeInMillis();
    }

    private String getYearMonthDayHourMinuteSecond(long times) {
        String month;
        String day;
        String hour;
        String minute;
        String second;
        String milliSecond;
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
        calendar.setTimeInMillis(times);
        String month2 = String.valueOf(calendar.get(2) + 1);
        if (month2.length() == 1) {
            month = "0" + month2;
        } else {
            month = month2;
        }
        String day2 = String.valueOf(calendar.get(5));
        if (day2.length() == 1) {
            day = "0" + day2;
        } else {
            day = day2;
        }
        String hour2 = String.valueOf(calendar.get(11));
        if (hour2.length() == 1) {
            hour = "0" + hour2;
        } else {
            hour = hour2;
        }
        String minute2 = String.valueOf(calendar.get(12));
        if (minute2.length() == 1) {
            minute = "0" + minute2;
        } else {
            minute = minute2;
        }
        String second2 = String.valueOf(calendar.get(13));
        if (second2.length() == 1) {
            second = "0" + second2;
        } else {
            second = second2;
        }
        String milliSecond2 = String.valueOf(calendar.get(14));
        if (milliSecond2.length() == 1) {
            milliSecond = "0" + milliSecond2;
        } else {
            milliSecond = milliSecond2;
        }
        return String.valueOf(calendar.get(1)) + AwarenessInnerConstants.DASH_KEY + month + AwarenessInnerConstants.DASH_KEY + day + " " + hour + AwarenessInnerConstants.COLON_KEY + minute + AwarenessInnerConstants.COLON_KEY + second + "." + milliSecond;
    }

    private String getAdjustVolumeParameters(String param, int curVol, String callingPackage, int streamType, int preVol) {
        List<String> repeatColume = new ArrayList<>();
        for (Long num : this.mMapPkgs.keySet()) {
            repeatColume.add(this.mMapPkgs.get(num));
        }
        List<String> repeatColumeValue = new ArrayList<>();
        for (int i = 0; i < repeatColume.size(); i++) {
            for (int j = i + 1; j < repeatColume.size() - i; j++) {
                if (repeatColume.get(i).equals(repeatColume.get(j))) {
                    repeatColumeValue.add(repeatColume.get(i));
                }
            }
        }
        List<Long> repeatKey = new ArrayList<>();
        for (Long num2 : this.mMapPkgs.keySet()) {
            if (repeatColumeValue.contains(this.mMapPkgs.get(num2))) {
                repeatKey.add(num2);
            }
        }
        for (Long num3 : repeatKey) {
            this.mMapPkgs.remove(num3);
        }
        if (this.mMapPkgs.size() == 0) {
            this.mCount = 0;
            this.mMapPkgs.clear();
        }
        if (this.mMapPkgs.size() > 0) {
            Iterator<Map.Entry<Long, String>> it = this.mMapPkgs.entrySet().iterator();
            while (it.hasNext()) {
                param = param + "stream=" + String.valueOf(streamType) + ",beforeVol=" + String.valueOf(preVol) + ",afterVol=" + String.valueOf(curVol) + ",time=" + getYearMonthDayHourMinuteSecond(it.next().getKey().longValue());
            }
            this.mCount = 0;
            this.mMapPkgs.clear();
        }
        return param;
    }

    private String getApplicationAdjustVolumeInfos(int curVol, String callingPackage, int streamType, int preVol) {
        String param = "";
        this.mCount++;
        if (this.mCount == 1) {
            this.mStartTime = getCurrentMillTimes();
        }
        if (this.mCount >= 2) {
            long currentTime = getCurrentMillTimes();
            long gapTime = currentTime - this.mStartTime;
            if (gapTime <= 1000) {
                this.mMapPkgs.put(Long.valueOf(currentTime), callingPackage);
            }
            if (gapTime >= 900 && gapTime <= 1100 && this.mMapPkgs.size() > 0) {
                param = getAdjustVolumeParameters(param, curVol, callingPackage, streamType, preVol);
            }
            if (gapTime > 1100) {
                this.mCount = 0;
                this.mMapPkgs.clear();
            }
        }
        return param;
    }

    private int getPreAdjustVolume(int streamType) {
        return getVssVolumeForDevice(streamType, getDeviceForStream(streamType)) / 10;
    }

    private boolean getExcludePackageName(String packageName) {
        if (packageName.indexOf(WifiProCommonUtils.WIFI_SETTINGS_PHONE) == -1 && packageName.indexOf(HwCustPkgNameConstant.HW_SYSTEMUI_PACKAGE) == -1 && packageName.indexOf("com.android.incallui") == -1 && packageName.indexOf(PackageManagerServiceEx.PLATFORM_PACKAGE_NAME) == -1 && packageName.indexOf("MediaSessionService") == -1) {
            return true;
        }
        return false;
    }

    public void adjustStreamVolume(int streamType, int direction, int flags, String callingPackage) {
        HwAudioService.super.adjustStreamVolume(streamType, direction, flags, callingPackage);
        HwMediaMonitorManager.writeBigData(916600001, "ADJUST_STREAM_VOLUME", callingPackage, "3: " + String.valueOf(streamType) + ", " + String.valueOf(direction) + ", " + String.valueOf(flags), streamType);
    }

    /* access modifiers changed from: protected */
    public void adjustSuggestedStreamVolume(int direction, int suggestedStreamType, int flags, String callingPackage, String caller, int uid) {
        HwAudioService.super.adjustSuggestedStreamVolume(direction, suggestedStreamType, flags, callingPackage, caller, uid);
        HwMediaMonitorManager.writeBigData(916600001, "ADJUST_SUGGESTED_STREAM_VOLUME", callingPackage, "4: " + String.valueOf(direction) + ", " + String.valueOf(suggestedStreamType) + ", " + String.valueOf(flags), suggestedStreamType);
    }

    public void setStreamVolume(int streamType, int index, int flags, String callingPackage) {
        int preVol = 0;
        if (streamType == 3) {
            preVol = getPreAdjustVolume(streamType);
            Slog.i(TAG, "setStreamVolume index=" + index + ",preVol=" + preVol);
        }
        HwAudioService.super.setStreamVolume(streamType, index, flags, callingPackage);
        HwMediaMonitorManager.writeBigData(916600001, "SET_STREAM_VOLUME", callingPackage, "1: " + String.valueOf(streamType) + ", " + String.valueOf(index) + ", " + String.valueOf(flags), streamType);
        if (streamType == 3) {
            String setVolParams = getApplicationAdjustVolumeInfos(index, callingPackage, 3, preVol);
            if (!TextUtils.isEmpty(setVolParams) && getExcludePackageName(callingPackage)) {
                HwMediaMonitorManager.writeBigData(916000000, "SET_STREAM_VOLUME", callingPackage, setVolParams, streamType);
            }
        }
    }

    public void setVolumeIndexForAttributes(AudioAttributes attr, int index, int flags, String callingPackage) {
        AudioVolumeGroup avg = getAudioVolumeGroupById(getVolumeGroupIdForAttributes(attr));
        if (avg == null) {
            Slog.w(TAG, "avg == null, failed to setVolumeIndexForAttributes");
            return;
        }
        int[] legacyStreamTypes = avg.getLegacyStreamTypes();
        int preVol = 0;
        for (int groupedStream : legacyStreamTypes) {
            if (groupedStream == 3) {
                preVol = getPreAdjustVolume(groupedStream);
                Slog.i(TAG, "setVolumeIndexForAttributes index=" + index + ",preVol=" + preVol);
            }
        }
        HwAudioService.super.setVolumeIndexForAttributes(attr, index, flags, callingPackage);
        String param = "2:" + String.valueOf(index) + ", " + String.valueOf(flags);
        int[] legacyStreamTypes2 = avg.getLegacyStreamTypes();
        for (int groupedStream2 : legacyStreamTypes2) {
            HwMediaMonitorManager.writeBigData(916600001, "SET_VOLUME_INDEX_FOR_ATTR", callingPackage, param, groupedStream2);
            if (groupedStream2 == 3) {
                String setVolIdxParams = getApplicationAdjustVolumeInfos(index, callingPackage, groupedStream2, preVol);
                if (!TextUtils.isEmpty(setVolIdxParams) && getExcludePackageName(callingPackage)) {
                    HwMediaMonitorManager.writeBigData(916000000, "SET_VOLUME_INDEX_FOR_ATTR", callingPackage, setVolIdxParams, groupedStream2);
                }
            }
        }
    }

    public void setMasterMute(boolean mute, int flags, String callingPackage, int userId) {
        int preVol = getPreAdjustVolume(3);
        Slog.i(TAG, "setVolumeIndexForAttributes index=0,preVol=" + preVol);
        HwAudioService.super.setMasterMute(mute, flags, callingPackage, userId);
        HwMediaMonitorManager.writeBigData(916600008, "SET_MASTER_MUTE", callingPackage, "3: " + String.valueOf(mute) + ", " + String.valueOf(flags));
        String setMuteParams = getApplicationAdjustVolumeInfos(0, callingPackage, 3, preVol);
        if (!TextUtils.isEmpty(setMuteParams) && getExcludePackageName(callingPackage)) {
            HwMediaMonitorManager.writeBigData(916000000, "SET_MASTER_MUTE", setMuteParams, callingPackage);
        }
    }

    public void setSpeakerphoneOn(boolean on) {
        HwAudioService.super.setSpeakerphoneOn(on);
        readyForWriteBigData(916600007, "SET_SPEAKERPHONE_ON", "4: " + String.valueOf(on));
    }

    public void startBluetoothSco(IBinder cb, int targetSdkVersion) {
        HwAudioService.super.startBluetoothSco(cb, targetSdkVersion);
        readyForWriteBigData(916600005, "START_BLUETOOTH_SCO", "1: start");
    }

    public void startBluetoothScoVirtualCall(IBinder cb) {
        HwAudioService.super.startBluetoothScoVirtualCall(cb);
        readyForWriteBigData(916600005, "START_BLUETOOTH_SCO_VIRTUAL", "3: start");
    }

    public void stopBluetoothSco(IBinder cb) {
        HwAudioService.super.stopBluetoothSco(cb);
        readyForWriteBigData(916600005, "STOP_BLUETOOTH_SCO", "2: stop");
    }

    public void setBluetoothScoOn(boolean on) {
        HwAudioService.super.setBluetoothScoOn(on);
        readyForWriteBigData(916600005, "SET_BLUETOOTH_SCO_ON", "4: " + String.valueOf(on));
    }

    public void setBluetoothA2dpDeviceConnectionStateSuppressNoisyIntent(BluetoothDevice device, int state, int profile, boolean suppressNoisyIntent, int a2dpVolume) {
        HwAudioService.super.setBluetoothA2dpDeviceConnectionStateSuppressNoisyIntent(device, state, profile, suppressNoisyIntent, a2dpVolume);
        readyForWriteBigData(916600004, "SET_A2DP_CONNECTION_NOISY", "1: " + String.valueOf(state) + ", " + String.valueOf(suppressNoisyIntent) + ", " + String.valueOf(a2dpVolume));
    }

    public void handleBluetoothA2dpDeviceConfigChange(BluetoothDevice device) {
        HwAudioService.super.handleBluetoothA2dpDeviceConfigChange(device);
        readyForWriteBigData(916600004, "HANDLE_A2DP_DEVICE_CHANGE", "2: handle");
    }

    private void readyForWriteBigData(int eventId, String subType, String param) {
        String pkgName = getPackageNameByPid(Binder.getCallingPid());
        if (pkgName == null) {
            pkgName = "null";
            Slog.w(TAG, "could not get pkgName for big data");
        }
        HwMediaMonitorManager.writeBigData(eventId, subType, pkgName, param);
    }
}
