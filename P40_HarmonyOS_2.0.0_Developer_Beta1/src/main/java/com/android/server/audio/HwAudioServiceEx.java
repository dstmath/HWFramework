package com.android.server.audio;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.IApplicationThread;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.cover.HallState;
import android.cover.IHallCallback;
import android.cover.IHwCoverManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.AudioPlaybackConfiguration;
import android.media.AudioRecordingConfiguration;
import android.media.AudioSystem;
import android.media.HwMediaMonitorManager;
import android.media.IAudioModeDispatcher;
import android.media.IAudioService;
import android.media.IRecordingConfigDispatcher;
import android.media.IVolumeChangeDispatcher;
import android.media.audiofx.AudioEffect;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IHwBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UEventObserver;
import android.os.UserHandle;
import android.provider.Settings;
import android.rms.iaware.AppTypeRecoManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import android.util.Xml;
import android.view.ContextThemeWrapper;
import android.widget.Toast;
import com.android.server.appactcontrol.AppActConstant;
import com.android.server.hidata.wavemapping.modelservice.ModelBaseService;
import com.android.server.intellicom.common.SmartDualCardConsts;
import com.android.server.location.HwLocalLocationProvider;
import com.android.server.notification.HwCustZenModeHelper;
import com.android.server.wifipro.WifiProCommonUtils;
import com.android.server.wm.HwActivityTaskManagerServiceEx;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.IHwActivityNotifierEx;
import com.huawei.android.fsm.HwFoldScreenManagerEx;
import com.huawei.android.media.IDeviceSelectCallback;
import com.huawei.android.pgmng.plug.PowerKit;
import com.huawei.android.util.NoExtAPIException;
import com.huawei.cust.HwCfgFilePolicy;
import com.huawei.displayengine.IDisplayEngineService;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import huawei.cust.HwCustUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import vendor.huawei.hardware.dolby.dms.V1_0.IDms;
import vendor.huawei.hardware.dolby.dms.V1_0.IDmsCallbacks;

public final class HwAudioServiceEx implements IHwAudioServiceEx {
    private static final String ACTION_ADJUST_STREAM_VOLUME = "huawei.intent.action.AdjustStreamVolume";
    private static final String ACTION_ANALOG_TYPEC_NOTIFY = "ACTION_TYPEC_NONOTIFY";
    private static final String ACTION_BLUETOOTH_FB_APP_FOR_KIT = "huawei.intent.action.APP_BLUETOOTH_FB_FOR_ACTION";
    private static final String ACTION_CONNECTION_STATE_CHANGED = "android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED";
    private static final String ACTION_DIGITAL_TYPEC_NOTIFY = "ACTION_DIGITAL_TYPEC_NONOTIFY";
    private static final String ACTION_ICARRY_DEVICE_LIST_CHANGED = "com.huawei.bluetooth.action.ICARRY_DEVICE_LIST_CHANGED";
    private static final String ACTION_KILLED_APP_FOR_KARAOKE = "huawei.intent.action.APP_KILLED_FOR_KARAOKE_ACTION";
    private static final String ACTION_KILLED_APP_FOR_KIT = "huawei.intent.action.APP_KILLED_FOR_KIT_ACTION";
    private static final String ACTION_MULTI_AUDIORECORD_FLAG_CHANGED = "com.huawei.audio.MULTI_AUDIORECORD_FLAG_CHANGED";
    private static final String ACTION_MULTI_RECORD_NOTIFY = "ACTION_MULTI_RECORD_NOTIFY";
    private static final String ACTION_RECORDNAME_APP_FOR_KIT = "huawei.intent.action.APP_RECORDNAME_FOR_KIT_ACTION";
    private static final String ACTION_SEND_AUDIO_RECORD_STATE = "huawei.media.AUDIO_RECORD_STATE_CHANGED_ACTION";
    private static final String ACTION_SEND_TV = "com.huawei.audio.VOLUME_CHANGED";
    private static final String ACTION_START_APP_FOR_KARAOKE = "huawei.media.ACTIVITY_STARTING_FOR_KARAOKE_ACTION";
    private static final String ACTION_STATE_CHANGED = "android.bluetooth.adapter.action.STATE_CHANGED";
    private static final String ACTION_SWS_EQ = "huawei.intent.action.SWS_EQ";
    private static final String ACTIVE_PKG_STR = "activePkg";
    static final String ACTIVITY_NOTIFY_COMPONENTNAME = "comp";
    static final String ACTIVITY_NOTIFY_ONPAUSE = "onPause";
    static final String ACTIVITY_NOTIFY_ONRESUME = "onResume";
    static final String ACTIVITY_NOTIFY_REASON = "activityLifeState";
    static final String ACTIVITY_NOTIFY_STATE = "state";
    private static final int ANALOG_TYPEC = 1;
    private static final int ANALOG_TYPEC_CONNECTED_DISABLE = 0;
    private static final int ANALOG_TYPEC_CONNECTED_ENABLE = 1;
    private static final int ANALOG_TYPEC_CONNECTED_ID = 1;
    private static final int ANALOG_TYPEC_DEVICES = 131084;
    private static final String ANALOG_TYPEC_FLAG = "audio_capability#usb_analog_hs_report";
    private static final int APP_TYPE_GAME = 9;
    private static final int AUDIO_SOURCE_CAMCORDER = 5;
    private static final int AUDIO_SOURCE_DEFAULT = 0;
    private static final int AUDIO_SOURCE_MIC = 1;
    private static final int BLUETOOTHA2DP_ENABLE_KEY = 10006;
    private static final int BLUETOOTHA2DP_KEY = 10005;
    private static final int BLUETOOTH_STATE_DEFAULT_VALUE = 1000;
    private static final String BOOT_VOLUME_PROPERTY = "persist.sys.volume.ringIndex";
    private static final int BYTES_PER_INT = 4;
    private static final String CMDINTVALUE = "Integer Value";
    private static final String CONCURRENT_CAPTURE_PROPERTY = "ro.config.concurrent_capture";
    private static final String[] CONCURRENT_RECORD_OTHER = {"com.baidu.BaiduMap", "com.autonavi.minimap"};
    private static final String[] CONCURRENT_RECORD_SYSTEM = {"com.huawei.vassistant"};
    private static final String CONFIG_FILE_WHITE_BLACK_APP = "xml/hw_karaokeeffect_app_config.xml";
    private static final int DEFAULT_CHANNEL_CNT = 2;
    private static final int DEVICE_ID_MAX_LENGTH = 128;
    private static final int DIGITAL_TYPEC_CONNECTED_DISABLE = 0;
    private static final int DIGITAL_TYPEC_CONNECTED_ENABLE = 1;
    private static final int DIGITAL_TYPEC_CONNECTED_ID = 2;
    private static final String DIGITAL_TYPEC_FLAG = "typec_compatibility_check";
    private static final String DIGITAL_TYPEC_REPORT_FLAG = "audio_capability#usb_compatibility_report";
    private static final int DIGTIAL_TYPEC = 2;
    private static final int DISABLE_VIRTUAL_AUDIO = 0;
    private static final int DOLBY_CHANNEL_COUNT = SystemProperties.getInt("ro.config.dolby_channel", 2);
    private static final int DOLBY_EFFECT_GAME_MODE = 3;
    private static final int DOLBY_EFFECT_MOVIE_MODE = 1;
    private static final int DOLBY_EFFECT_MUSIC_MODE = 2;
    private static final int DOLBY_EFFECT_SMART_MODE = 0;
    private static final int DOLBY_HIGH_PRIO = 1;
    private static final int DOLBY_LOW_PRIO = -1;
    private static final String DOLBY_MODE_HEADSET_STATE = "persist.sys.dolby.state";
    private static final String DOLBY_MODE_OFF_PARA = "dolby_profile=off";
    private static final String DOLBY_MODE_ON_PARA = "dolby_profile=on";
    private static final String DOLBY_MODE_PRESTATE = "persist.sys.dolby.prestate";
    private static final String[] DOLBY_PROFILE = {"off", "smart", "movie", "music"};
    private static final boolean DOLBY_SOUND_EFFECTS_SUPPORT = SystemProperties.getBoolean("ro.config.dolby_dap", false);
    private static final String DOLBY_UPDATE_EVENT = "dolby_dap_params_update";
    private static final String DOLBY_UPDATE_EVENT_DS_STATE = "ds_state_change";
    private static final String DOLBY_UPDATE_EVENT_PROFILE = "profile_change";
    private static final byte[] DS_DISABLE_ARRAY = {0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0};
    private static final byte[] DS_ENABLE_ARRAY = {0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0};
    private static final byte[] DS_GAME_MODE_ARRAY = {0, 0, 0, 10, 1, 0, 0, 0, 3, 0, 0, 0};
    private static final byte[] DS_MOVIE_MODE_ARRAY = {0, 0, 0, 10, 1, 0, 0, 0, 1, 0, 0, 0};
    private static final byte[] DS_MUSIC_MODE_ARRAY = {0, 0, 0, 10, 1, 0, 0, 0, 2, 0, 0, 0};
    private static final byte[] DS_SMART_MODE_ARRAY = {0, 0, 0, 10, 1, 0, 0, 0, 0, 0, 0, 0};
    private static final int EFFECT_PARAM_EFF_ENAB = 0;
    private static final int EFFECT_PARAM_EFF_PROF = 167772160;
    private static final int EFFECT_SUPPORT_DEVICE = 603980172;
    private static final UUID EFFECT_TYPE_DS = UUID.fromString("9d4921da-8225-4f29-aefa-39537a04bcaa");
    private static final UUID EFFECT_TYPE_NULL = UUID.fromString("ec7178ec-e5e1-4432-a3f4-4657e6795210");
    private static final int ENABLE_VIRTUAL_AUDIO = 1;
    private static final String ENGINE_PACKAGE_NAME = "com.huawei.multimedia.audioengine";
    private static final String EVENTNAME = "event name";
    private static final String EXTRA_VOLUME_STREAM_DIRECTION = "Direction";
    private static final String EXTRA_VOLUME_STREAM_FLAGS = "Flags";
    private static final String FIRST_START_INWARD_FOLD_SCREEN_DIALOG = "FoldScreen_Tip_TimeStamp";
    private static final String FOLD_ANSWER_TIMES = "FoldScreen_Tip_Record_S";
    private static final String[] FORBIDDEN_SOUND_EFFECT_WHITE_LIST = {"com.jawbone.up", "com.codoon.gps", "com.lakala.cardwatch", "com.hoolai.magic", "com.android.bankabc", "com.icbc", "com.icbc.wapc", "com.chinamworld.klb", "com.yitong.bbw.mbank.android", "com.chinamworld.bocmbci", "com.cloudcore.emobile.szrcb", "com.chinamworld.main", "com.nbbank", "com.cmb.ubank.UBUI", "com.nxy.mobilebank.ln", "cn.com.cmbc.newmbank"};
    private static final int GLOBAL_SESSION_ID = 0;
    private static final String HIDE_HIRES_ICON = "huawei.intent.action.hideHiResIcon";
    private static final String HIRES_REPORT_FLAG = "typec_need_show_hires";
    private static final String HS_NO_CHARGE_OFF = "hs_no_charge=off";
    private static final String HS_NO_CHARGE_ON = "hs_no_charge=on";
    private static final boolean HUAWEI_SWS31_CONFIG = (!"sws2".equalsIgnoreCase(SWS_VERSION) && !"sws3".equalsIgnoreCase(SWS_VERSION));
    private static final int HW_DOBBLY_SOUND_EFFECT_BIT = 4;
    private static final int HW_KARAOKE_EFFECT_BIT = 2;
    private static final boolean HW_KARAOKE_EFFECT_ENABLED = ((2 & SystemProperties.getInt("ro.config.hw_media_flags", 0)) != 0);
    private static final int INVALID_RECORD_STATE = -1;
    private static final String ISONTOP = "isOnTop";
    private static final boolean IS_CHINA_AREA = "CN".equalsIgnoreCase(SystemProperties.get(WifiProCommonUtils.KEY_PROP_LOCALE, ""));
    private static final boolean IS_MULTI_AUDIO_RECORD_ALLOWED = SystemProperties.getBoolean("hw_mc.audio.voice_recording_enable", true);
    private static final boolean IS_SUPPORT_SLIDE = ((SystemProperties.getInt("ro.config.hw_hall_prop", 0) & 1) != 0);
    private static final boolean IS_USB_POWER_COSUME_TIPS = SystemProperties.getBoolean("ro.config.usb_power_tips", false);
    private static final Object LOCK_AUDIOKIT_RECORDNAME = new Object();
    private static final Object LOCK_SWS = new Object();
    private static final String MSDP_DEVICE_NAME = "DMSDP";
    private static final int MSG_CHECK_SHOW_EARPIECE_TIPS = 23;
    private static final int MSG_CONNECT_DOLBY_SERVICE = 11;
    private static final int MSG_INIT_POWERKIT = 15;
    private static final int MSG_SEND_AUDIOKIT_BROADCAST = 19;
    private static final int MSG_SEND_DOLBYUPDATE_BROADCAST = 12;
    private static final int MSG_SEND_KILL_STATE_AUDIOKIT = 22;
    private static final int MSG_SEND_KIT_BLUETOOTH_ENABLE = 21;
    private static final int MSG_SET_DEVICE_STATE = 17;
    private static final int MSG_SET_SOUND_EFFECT_STATE = 13;
    private static final int MSG_SET_TYPEC_PARAM = 14;
    private static final int MSG_SHOW_MULTI_AUDIO_RECORD_NOTIFICATION = 25;
    private static final int MSG_SHOW_MULTI_RECORD_DIALOG = 26;
    private static final int MSG_SHOW_MULTI_RECORD_TOAST = 24;
    private static final int MSG_SHOW_OR_HIDE_HIRES = 10;
    private static final int MSG_SHOW_RECORD_SILENCE_TOAST = 18;
    private static final int MSG_START_DOLBY_DMS = 16;
    private static final int MSG_START_DOLBY_DMS_DELAY_FOUR_CHANNEL = 1000;
    private static final int MSG_START_SWS_SERVICE = 20;
    private static final String MULTI_AUDIORECORD_FLAG = "MULTI_AUDIORECORD_FLAG";
    private static final String NODE_ATTR_PACKAGE = "package";
    private static final String NODE_WHITEAPP = "whiteapp";
    private static final boolean NOT_SHOW_TYPEC_TIP = SystemProperties.getBoolean("ro.config.typec_not_show", false);
    private static final int NO_ERROR = 0;
    private static final String PACKAGENAME = "packageName";
    private static final String PACKAGENAME_LIST = "packageNameList";
    private static final String PACKAGE_PACKAGEINSTALLER = "com.android.packageinstaller";
    private static final String PERMISSION_ADJUST_STREAM_VOLUME = "com.huawei.permission.ADJUSTSTREAMVOLUME";
    private static final String PERMISSION_BLUETOOTH_FB_APP_FOR_KIT = "com.huawei.multimedia.audioengine.permission.APP_BLUETOOTH_FB_FOR_ACTION";
    private static final String PERMISSION_HIRES_CHANGE = "huawei.permission.HIRES_ICON_CHANGE_ACTION";
    private static final String PERMISSION_KILLED_APP_FOR_KARAOKE = "com.huawei.permission.APP_KILLED_FOR_KARAOKE_ACTION";
    private static final String PERMISSION_KILLED_APP_FOR_KIT = "com.huawei.multimedia.audioengine.permission.APP_KILLED_FOR_KIT_ACTION";
    private static final String PERMISSION_RECORDNAME_APP_FOR_KIT = "com.huawei.multimedia.audioengine.permission.APP_RECORDNAME_FOR_KIT_ACTION";
    private static final String PERMISSION_SEND_AUDIO_RECORD_STATE = "com.huawei.permission.AUDIO_RECORD_STATE_CHANGED_ACTION";
    private static final String PERMISSION_SEND_TV = "com.huawei.permission.AUDIOVOLUMECHANGED";
    private static final String PERMISSION_SWS_EQ = "sws.permission.SWS_EQ";
    private static final String PHONE_PKG = "com.android.phone";
    private static final String PRE_PKG_STR = "prePkg";
    private static final int PRIMARY_OUTPUT = 13;
    private static final int PROPVALUE_NOT_SUPPORT_SLIDE = 0;
    private static final int PROPVALUE_PRODUCT_SUPPORT_SLIDE = 1;
    private static final int QUAD_CHANNEL_CNT = 4;
    private static final String RECEIVER_NAME = "audioserver";
    private static final String[] RECORD_ACTIVE_APP_LIST = {"com.realvnc.android.remote"};
    private static final String[] RECORD_REQUEST_APP_LIST = {"com.google.android", "com.google.android.googlequicksearchbox:search", "com.google.android.googlequicksearchbox:interactor"};
    private static final int RECORD_TYPE_OTHER = 536870912;
    private static final int RECORD_TYPE_SYSTEM = 1073741824;
    private static final String REMOTE_DEVICE_NAME = "DeviceName";
    private static final String REMOTE_DEVICE_TYPE = "DeviceType";
    private static final String RESERVED = "reserved";
    private static final String RESTORE = "restore";
    private static final int SENDMSG_NOOP = 1;
    private static final int SENDMSG_QUEUE = 2;
    private static final int SENDMSG_REPLACE = 0;
    private static final int SERVICE_ID_MAX_LENGTH = 64;
    private static final int SERVICE_TYPE_MIC = 2;
    private static final int SERVICE_TYPE_SPEAKER = 3;
    private static final String SHARED_UID_STR = "SHARED_UID";
    private static final boolean SHOW_EARPIECE_TIP = SystemProperties.getBoolean("hw_mc.audio.fold_noti_earpiece", false);
    private static final String SHOW_HIRES_ICON = "huawei.intent.action.showHiResIcon";
    private static final int SHOW_OR_HIDE_HIRES_DELAY = 500;
    private static final String SILENCE_PKG_STR = "silencePkg";
    private static final boolean SOUND_EFFECTS_SUPPORT;
    private static final int SOUND_EFFECT_CLOSE = 1;
    private static final int SOUND_EFFECT_OPEN = 2;
    private static final int START_DOLBY_DMS_DELAY = 200;
    private static final String SWS_MODE_OFF_PARA = "HIFIPARA=STEREOWIDEN_Enable=false";
    private static final String SWS_MODE_ON_PARA = "HIFIPARA=STEREOWIDEN_Enable=true";
    private static final String SWS_MODE_PARA = "HIFIPARA=STEREOWIDEN_Enable";
    private static final String SWS_MODE_PRESTATE = "persist.sys.sws.prestate";
    private static final int SWS_OFF = 0;
    private static final int SWS_ON = 3;
    private static final int SWS_SCENE_AUDIO = 0;
    private static final int SWS_SCENE_VIDEO = 1;
    private static final boolean SWS_SOUND_EFFECTS_SUPPORT = SystemProperties.getBoolean("ro.config.hw_sws", false);
    private static final boolean SWS_TYPEC_ADAPTE_SUPPORT = SWS_SOUND_EFFECTS_SUPPORT;
    private static final String SWS_TYPEC_MANUFACTURER = "SWS_TYPEC_MANUFACTURER";
    private static final String SWS_TYPEC_PRODUCT_NAME = "SWS_TYPEC_PRODUCT_NAME";
    private static final String SWS_TYPEC_RESTORE = "SWS_TYPEC_RESTORE";
    private static final String SWS_VERSION = SystemProperties.get("ro.config.sws_version", "0600");
    private static final boolean SWS_VIDEO_MODE = SystemProperties.getBoolean("ro.config.sws_moviemode", false);
    private static final String SYSTEMSERVER_START = "com.huawei.systemserver.START";
    private static final String SYSTEM_MANAGER_PERMISSION = "com.huawei.systemmanager.permission.ACCESS_INTERFACE";
    private static final String TAG = "HwAudioServiceEx";
    private static final int THREE_TIMES = 3;
    private static final String TOAST_THEME = "androidhwext:style/Theme.Emui.Toast";
    private static final Long TWO_WEEKS = 1209600000L;
    private static final int UNKNOWN_DEVICE = -1;
    private static final int USB_SECURITY_VOLUME_INDEX = SystemProperties.getInt("ro.config.hw.usb_security_volume", 0);
    private static final String USER_CLICK_CLOSE_TIMES = "FoldScreen_Tip_Record_X";
    private static final int VOLUME_INDEX_UNIT = 10;
    private static final int mSetVolumeByPidStreamMapMaxSize = 100;
    private static int sSwsScene = 0;
    private int PowerKitAppType;
    private final int SYSTEMUID = 1000;
    private List<BluetoothDevice> connectedA2dpDevices;
    private BluetoothA2dp mA2dpProfile;
    private BroadcastReceiver mAnalogTypecReceiver;
    private List<String> mAudioKitList;
    private int mAudioMode;
    private IAudioService mAudioService;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    private volatile int mBluetoothState;
    private boolean mBootCompleted;
    private IHallCallback.Stub mCallback;
    private final ArrayList<AudioModeClient> mClients = new ArrayList<>();
    private boolean mConcurrentCaptureEnable;
    private ContentResolver mContentResolver;
    private final Context mContext;
    private IHwCoverManager mCoverManager;
    private HwCustZenModeHelper mCustZenModeHelper;
    private BroadcastReceiver mDigitalTypecReceiver;
    private HwFoldScreenManagerEx.FoldDisplayModeListener mDisplayListener;
    private IDms mDms;
    private IHwBinder.DeathRecipient mDmsDeathRecipient;
    private boolean mDmsStarted;
    private IDmsCallbacks mDolbyClient;
    private int mDolbyEnable;
    private AudioEffect mEffect;
    private final UEventObserver mFanAndPhoneCoverObserver;
    private boolean mFmDeviceOn;
    private final HwFoldScreenManagerEx.FoldFsmTipsRequestListener mFoldTipsListener;
    private boolean mHeadSetPlugState;
    private HwAudioHandlerEx mHwAudioHandlerEx;
    private HwAudioHandlerExThread mHwAudioHandlerExThread;
    private HwHistenNaturalModeService mHwHistenNaturalModeService;
    private IHwAudioServiceInner mIAsInner;
    private boolean mIsAnalogTypecReceiverRegisterd;
    private boolean mIsBluetoothA2dpHD;
    private boolean mIsBluetoothHDEnable;
    private boolean mIsCoverOn;
    private boolean mIsDigitalTypecOn;
    private boolean mIsDigitalTypecReceiverRegisterd;
    private boolean mIsFanOn;
    private boolean mIsMultiAudioRecordEnable;
    private boolean mIsWirelessChargeOn;
    private Map<String, Integer> mKaraokeUidsMap;
    private ArrayList<String> mKaraokeWhiteList;
    private List<AudioRecordingConfiguration> mLastRecordingConfigs;
    private AudioRecordingConfiguration mLastSilenceRec;
    private final AudioSystem.MultiAudioRecordCallback mMultiAudioRecordCallback;
    private BroadcastReceiver mMultiAudioRecordReceiver;
    private boolean mMultiAudioRecordToastShowing;
    private Handler mMyHandler;
    private NotificationManager mNm;
    private NotificationManager mNotificationManager;
    private IHwActivityNotifierEx mNotifierForLifeState;
    private String mOldPackageName = "";
    private Map<String, Boolean> mPackageKillStateMap;
    private ArrayList<String> mPackageNameList;
    private HashMap<String, Integer> mPackageUidMap;
    private final AudioSystem.PlaybackActivityMonitorCallback mPlaybackActivityMonitorCallback;
    private PowerKit mPowerKit;
    private final BroadcastReceiver mReceiver;
    private final IRecordingConfigDispatcher mRecordingListener;
    private HwScoRecordService mScoRecordService;
    private SetVolumeByPidStreamClient mSetVolumeByPidStreamClient;
    private final Object mSetVolumeByPidStreamLock;
    private HashMap<Pair<Integer, Integer>, Float> mSetVolumeByPidStreamMap;
    private boolean mShowEarpieceTip;
    private boolean mShowHiResIcon;
    private String mStartedPackageName = null;
    private PowerKit.Sink mStateRecognitionListener;
    private int mSupportDevcieRef;
    private boolean mSwsConnect;
    private ServiceConnection mSwsServiceConnect;
    private int mSwsStatus;
    private boolean mSwsbind;
    private boolean mSystemReady;
    private Toast mToast;
    private final ArrayList<VolumeChangeClient> mVolumeClients = new ArrayList<>();
    private String mWlanCoverConnected = "1";
    private String mWlanFanConnected = "1";
    private String[] mZenModeWhiteList;
    IDeviceSelectCallback mcb = null;
    private BroadcastReceiver sBluetoothDeviceReceiver;
    private BluetoothProfile.ServiceListener serviceListener;
    private String swsClassName;
    private HashSet<Integer> swsHashSet;
    private String swsPackageName;
    private ArrayList<BluetoothDevice> virtualDevices;

    static {
        boolean z = false;
        if (SWS_SOUND_EFFECTS_SUPPORT || DOLBY_SOUND_EFFECTS_SUPPORT) {
            z = true;
        }
        SOUND_EFFECTS_SUPPORT = z;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setParametersOnUEvent(String vbusDisconnect) {
        if (isWirelessChargeOn(vbusDisconnect)) {
            Log.i(TAG, "wlanCharge status of vbus is disconnected.");
            AudioSystem.setParameters("wireless_charge=off");
            this.mIsFanOn = false;
            this.mIsCoverOn = false;
            this.mIsWirelessChargeOn = false;
        } else if (isWirelessChargeOff()) {
            AudioSystem.setParameters("wireless_charge=on");
            this.mIsWirelessChargeOn = true;
        } else {
            Log.w(TAG, "wrong state from observer, do nothing.");
        }
    }

    private boolean isWirelessChargeOn(String vbusDisconnect) {
        boolean isVbusDisconnected = false;
        if (vbusDisconnect != null && TextUtils.isEmpty(vbusDisconnect)) {
            isVbusDisconnected = true;
        }
        return (this.mIsFanOn || this.mIsCoverOn) && isVbusDisconnected && this.mIsWirelessChargeOn;
    }

    private boolean isWirelessChargeOff() {
        return (this.mIsFanOn || this.mIsCoverOn) && !this.mIsWirelessChargeOn;
    }

    public HwAudioServiceEx(IHwAudioServiceInner ias, Context context) {
        boolean z = false;
        this.mAudioMode = 0;
        this.PowerKitAppType = -1;
        this.mBluetoothState = 1000;
        this.mSwsStatus = 0;
        this.mDolbyEnable = 0;
        this.mSupportDevcieRef = 0;
        this.mFmDeviceOn = false;
        this.mDmsStarted = false;
        this.mIsDigitalTypecOn = false;
        this.mIsDigitalTypecReceiverRegisterd = false;
        this.mIsAnalogTypecReceiverRegisterd = false;
        this.mHeadSetPlugState = false;
        this.mShowHiResIcon = false;
        this.mIsFanOn = false;
        this.mIsCoverOn = false;
        this.mIsWirelessChargeOn = false;
        this.mIsBluetoothA2dpHD = false;
        this.mIsBluetoothHDEnable = false;
        this.mBootCompleted = false;
        this.mIsMultiAudioRecordEnable = false;
        this.mMultiAudioRecordToastShowing = false;
        this.mDms = null;
        this.mPowerKit = null;
        this.mDigitalTypecReceiver = null;
        this.mNotificationManager = null;
        this.mAnalogTypecReceiver = null;
        this.mMultiAudioRecordReceiver = null;
        this.mKaraokeWhiteList = null;
        this.mAudioKitList = new ArrayList();
        this.mEffect = null;
        this.mPackageUidMap = new HashMap<>();
        this.mKaraokeUidsMap = new HashMap();
        this.mLastSilenceRec = null;
        this.mLastRecordingConfigs = new ArrayList();
        this.mIAsInner = null;
        this.swsHashSet = new HashSet<>();
        this.virtualDevices = new ArrayList<>();
        this.connectedA2dpDevices = new ArrayList();
        this.swsPackageName = "com.huawei.imedia.sws";
        this.swsClassName = "com.huawei.imedia.sws.CreateControlCenter";
        this.mSwsbind = false;
        this.mSwsConnect = false;
        this.mPackageNameList = new ArrayList<>();
        this.mPackageKillStateMap = new HashMap();
        this.mSetVolumeByPidStreamClient = null;
        this.mSetVolumeByPidStreamLock = new Object();
        this.mSetVolumeByPidStreamMap = new HashMap<>();
        this.mFanAndPhoneCoverObserver = new UEventObserver() {
            /* class com.android.server.audio.HwAudioServiceEx.AnonymousClass1 */

            public void onUEvent(UEventObserver.UEvent event) {
                String wlanFanStatus = event.get("UI_WL_FAN_STATUS");
                String wlanCoverStatus = event.get("UI_WL_COVER_STATUS");
                String vbusDisconnect = event.get("VBUS_DISCONNECT");
                if (wlanFanStatus != null && wlanFanStatus.equals(HwAudioServiceEx.this.mWlanFanConnected)) {
                    Log.i(HwAudioServiceEx.TAG, "wlanCharge status of fan is connected");
                    HwAudioServiceEx.this.mIsFanOn = true;
                }
                if (wlanCoverStatus != null && wlanCoverStatus.equals(HwAudioServiceEx.this.mWlanCoverConnected)) {
                    Log.i(HwAudioServiceEx.TAG, "wlanCharge status of cover is connected");
                    HwAudioServiceEx.this.mIsCoverOn = true;
                }
                HwAudioServiceEx.this.setParametersOnUEvent(vbusDisconnect);
            }
        };
        this.serviceListener = new BluetoothProfile.ServiceListener() {
            /* class com.android.server.audio.HwAudioServiceEx.AnonymousClass3 */
            private static final String BINDING_BT = "binding_BT";

            @Override // android.bluetooth.BluetoothProfile.ServiceListener
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                synchronized (HwAudioServiceEx.LOCK_SWS) {
                    Slog.i(HwAudioServiceEx.TAG, "onServiceConnected profile " + profile);
                    if (profile != 2) {
                        Slog.i(HwAudioServiceEx.TAG, "ignore profile: " + profile);
                    } else {
                        HwAudioServiceEx.this.mA2dpProfile = (BluetoothA2dp) proxy;
                    }
                }
            }

            @Override // android.bluetooth.BluetoothProfile.ServiceListener
            public void onServiceDisconnected(int profile) {
                synchronized (HwAudioServiceEx.LOCK_SWS) {
                    if (HwAudioServiceEx.this.mBluetoothAdapter == null) {
                        Slog.i(HwAudioServiceEx.TAG, "onServiceDisconnected get adapter failed");
                        return;
                    }
                    Slog.i(HwAudioServiceEx.TAG, "onServiceDisconnected profile " + profile);
                    if (profile != 2) {
                        Slog.i(HwAudioServiceEx.TAG, "ignore profile: " + profile);
                    } else {
                        if (HwAudioServiceEx.this.mA2dpProfile != null) {
                            HwAudioServiceEx.this.mBluetoothAdapter.closeProfileProxy(2, HwAudioServiceEx.this.mA2dpProfile);
                        }
                        HwAudioServiceEx.this.mA2dpProfile = null;
                    }
                }
            }
        };
        this.sBluetoothDeviceReceiver = new BroadcastReceiver() {
            /* class com.android.server.audio.HwAudioServiceEx.AnonymousClass4 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                BluetoothDevice bluetoothDevice;
                if (intent == null) {
                    Slog.w(HwAudioServiceEx.TAG, "sBluetoothDeviceReceiver, intent=null");
                    return;
                }
                String action = intent.getAction();
                if (HwAudioServiceEx.ACTION_ICARRY_DEVICE_LIST_CHANGED.equals(action)) {
                    try {
                        HwAudioServiceEx.this.virtualDevices = intent.getParcelableArrayListExtra("com.huawei.bluetooth.extra.ICARRY_A2DP_DEVICE_LIST");
                        if (!HwAudioServiceEx.this.virtualDevices.isEmpty()) {
                            HwAudioServiceEx.sendMsgEx(HwAudioServiceEx.this.mHwAudioHandlerEx, 20, 0, 0, 1, null, 0);
                        }
                        if (HwAudioServiceEx.this.virtualDevices.isEmpty() && HwAudioServiceEx.this.connectedA2dpDevices.isEmpty() && HwAudioServiceEx.this.swsHashSet.isEmpty() && HwAudioServiceEx.this.mSwsConnect) {
                            HwAudioServiceEx.sendMsgEx(HwAudioServiceEx.this.mHwAudioHandlerEx, 20, 0, 0, 0, null, 0);
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        Slog.e(HwAudioServiceEx.TAG, "get virtual device failed");
                    }
                }
                if (HwAudioServiceEx.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                    if (HwAudioServiceEx.this.mA2dpProfile == null) {
                        HwAudioServiceEx.this.connectedA2dpDevices = new ArrayList();
                    } else {
                        HwAudioServiceEx hwAudioServiceEx = HwAudioServiceEx.this;
                        hwAudioServiceEx.connectedA2dpDevices = hwAudioServiceEx.mA2dpProfile.getConnectedDevices();
                    }
                    if (!HwAudioServiceEx.this.connectedA2dpDevices.isEmpty()) {
                        HwAudioServiceEx.sendMsgEx(HwAudioServiceEx.this.mHwAudioHandlerEx, 20, 0, 0, 1, null, 0);
                    }
                    if (HwAudioServiceEx.this.virtualDevices.isEmpty() && HwAudioServiceEx.this.connectedA2dpDevices.isEmpty() && HwAudioServiceEx.this.swsHashSet.isEmpty() && HwAudioServiceEx.this.mSwsConnect) {
                        HwAudioServiceEx.sendMsgEx(HwAudioServiceEx.this.mHwAudioHandlerEx, 20, 0, 0, 0, null, 0);
                    }
                }
                if ("android.intent.action.BOOT_COMPLETED".equals(action) && HwAudioServiceEx.this.mSwsbind && !HwAudioServiceEx.this.mSwsConnect) {
                    HwAudioServiceEx.sendMsgEx(HwAudioServiceEx.this.mHwAudioHandlerEx, 20, 0, 0, 1, null, 0);
                }
                if (HwAudioServiceEx.ACTION_STATE_CHANGED.equals(action)) {
                    HwAudioServiceEx.this.mBluetoothState = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", 1000);
                    if (HwAudioServiceEx.this.mBluetoothState == 12) {
                        HwAudioServiceEx.this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        if (HwAudioServiceEx.this.mBluetoothAdapter == null) {
                            Slog.i(HwAudioServiceEx.TAG, "bindBluetooth get adapter failed");
                            return;
                        }
                        HwAudioServiceEx.this.closeProfileProxy();
                        if (HwAudioServiceEx.this.mBluetoothAdapter.isEnabled()) {
                            HwAudioServiceEx.this.mBluetoothAdapter.getProfileProxy(HwAudioServiceEx.this.mContext, HwAudioServiceEx.this.serviceListener, 2);
                        }
                        if (!HwAudioServiceEx.this.swsHashSet.isEmpty()) {
                            HwAudioServiceEx.sendMsgEx(HwAudioServiceEx.this.mHwAudioHandlerEx, 20, 0, 0, 1, null, 0);
                        }
                    }
                    if (HwAudioServiceEx.this.mBluetoothState == 13) {
                        if (HwAudioServiceEx.this.mBluetoothAdapter == null) {
                            Slog.i(HwAudioServiceEx.TAG, "onDestroy adapter is null");
                            return;
                        } else {
                            HwAudioServiceEx.this.closeProfileProxy();
                            HwAudioServiceEx.sendMsgEx(HwAudioServiceEx.this.mHwAudioHandlerEx, 20, 0, 0, 0, null, 0);
                        }
                    }
                }
                if (("android.bluetooth.headset.profile.action.ACTIVE_DEVICE_CHANGED".equals(action) || "android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED".equals(action)) && (bluetoothDevice = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE")) != null) {
                    HwAudioServiceEx.this.mBluetoothDevice = bluetoothDevice;
                    byte[] deviceByte = HwAudioServiceEx.this.mBluetoothDevice.getMetadata(10005);
                    HwAudioServiceEx hwAudioServiceEx2 = HwAudioServiceEx.this;
                    hwAudioServiceEx2.mIsBluetoothA2dpHD = hwAudioServiceEx2.getBluetoothMetadata(deviceByte);
                    byte[] enableByte = HwAudioServiceEx.this.mBluetoothDevice.getMetadata(10006);
                    HwAudioServiceEx hwAudioServiceEx3 = HwAudioServiceEx.this;
                    hwAudioServiceEx3.mIsBluetoothHDEnable = hwAudioServiceEx3.getBluetoothMetadata(enableByte);
                    HwAudioServiceEx hwAudioServiceEx4 = HwAudioServiceEx.this;
                    hwAudioServiceEx4.sendBluetoothHanderMsg(hwAudioServiceEx4.mIsBluetoothHDEnable);
                }
            }
        };
        this.mNotifierForLifeState = new IHwActivityNotifierEx() {
            /* class com.android.server.audio.HwAudioServiceEx.AnonymousClass5 */

            public void call(Bundle extras) {
                boolean isOnResume;
                if (extras == null) {
                    Slog.w(HwAudioServiceEx.TAG, "AMS callback, but extras=null");
                    return;
                }
                ComponentName comp = extras.getParcelable(HwAudioServiceEx.ACTIVITY_NOTIFY_COMPONENTNAME) instanceof ComponentName ? (ComponentName) extras.getParcelable(HwAudioServiceEx.ACTIVITY_NOTIFY_COMPONENTNAME) : null;
                if (comp != null) {
                    String packageName = comp.getPackageName();
                    String flag = extras.getString(HwAudioServiceEx.ACTIVITY_NOTIFY_STATE);
                    boolean isTop = extras.getBoolean("isTop");
                    Slog.i(HwAudioServiceEx.TAG, "ComponentInfo : className = " + comp.getClassName() + ", flag = " + flag + ", isHomeActivity = , isTop = " + isTop);
                    if (HwAudioServiceEx.ACTIVITY_NOTIFY_ONRESUME.equals(flag)) {
                        HwAudioServiceEx.this.setSoundEffectState(false, packageName, true, null);
                        isOnResume = true;
                    } else if (HwAudioServiceEx.ACTIVITY_NOTIFY_ONPAUSE.equals(flag)) {
                        HwAudioServiceEx.this.setSoundEffectState(false, packageName, false, null);
                        isOnResume = false;
                    } else {
                        Slog.i(HwAudioServiceEx.TAG, "not onResume or onPause");
                        isOnResume = false;
                    }
                    if (!isTop && HwAudioServiceEx.this.mShowEarpieceTip) {
                        Bundle data = new Bundle();
                        data.putString("KEY_TIPS_STR_CALLER_NAME", HwAudioServiceEx.TAG);
                        HwAudioServiceEx.this.mShowEarpieceTip = false;
                        HwFoldScreenManagerEx.reqShowTipsToFsm(1, data);
                        HwAudioServiceEx hwAudioServiceEx = HwAudioServiceEx.this;
                        hwAudioServiceEx.unregisterFsmTipsRequestListener(hwAudioServiceEx.mFoldTipsListener);
                    } else if (isTop && !HwAudioServiceEx.this.mShowEarpieceTip) {
                        IAudioService audioService = HwAudioServiceEx.this.getAudioService();
                        if (audioService == null) {
                            Slog.e(HwAudioServiceEx.TAG, "audioservice is null");
                            return;
                        }
                        try {
                            for (AudioPlaybackConfiguration playback : audioService.getActivePlaybackConfigurations()) {
                                if (playback.getPkgName() != null && playback.getPkgName().equals(HwAudioServiceEx.this.getTopActivityPackageName()) && playback.getPlayerState() == 2) {
                                    HwAudioServiceEx.sendMsgEx(HwAudioServiceEx.this.mHwAudioHandlerEx, 23, 2, playback.getDevice(), 0, playback.getPkgName(), 0);
                                }
                            }
                        } catch (RemoteException e) {
                            Log.e(HwAudioServiceEx.TAG, "failed to getActivePlaybackConfigurations");
                        }
                    }
                    HwAudioServiceEx.this.mPackageKillStateMap.put(packageName, Boolean.valueOf(isOnResume));
                    return;
                }
                Slog.e(HwAudioServiceEx.TAG, "ComponentName is null");
            }
        };
        this.mSwsServiceConnect = new ServiceConnection() {
            /* class com.android.server.audio.HwAudioServiceEx.AnonymousClass7 */

            @Override // android.content.ServiceConnection
            public void onServiceConnected(ComponentName name, IBinder service) {
                HwAudioServiceEx.this.mSwsConnect = true;
            }

            @Override // android.content.ServiceConnection
            public void onServiceDisconnected(ComponentName name) {
                HwAudioServiceEx.this.mSwsConnect = false;
                if (HwAudioServiceEx.this.mSwsbind) {
                    HwAudioServiceEx.sendMsgEx(HwAudioServiceEx.this.mHwAudioHandlerEx, 20, 0, 0, 1, null, 0);
                }
            }
        };
        this.mStateRecognitionListener = new PowerKit.Sink() {
            /* class com.android.server.audio.HwAudioServiceEx.AnonymousClass8 */

            public void onStateChanged(int stateType, int eventType, int pid, String pkg, int uid) {
                if (pid != Process.myPid()) {
                    if (stateType == 2) {
                        if (eventType == 1) {
                            HwAudioServiceEx hwAudioServiceEx = HwAudioServiceEx.this;
                            hwAudioServiceEx.PowerKitAppType = hwAudioServiceEx.getAppTypeForVBR(uid);
                            Slog.i(HwAudioServiceEx.TAG, "VBR HwAudioService:music app enter " + HwAudioServiceEx.this.PowerKitAppType);
                            if (HwAudioServiceEx.this.PowerKitAppType == 12) {
                                AudioSystem.setParameters("VBRMuiscState=enter");
                            } else if (HwAudioServiceEx.this.PowerKitAppType == 11) {
                                AudioSystem.setParameters("VBRIMState=enter");
                            } else {
                                Slog.v(HwAudioServiceEx.TAG, "no process apptype");
                            }
                        } else if (eventType == 2) {
                            HwAudioServiceEx hwAudioServiceEx2 = HwAudioServiceEx.this;
                            hwAudioServiceEx2.PowerKitAppType = hwAudioServiceEx2.getAppTypeForVBR(uid);
                            Slog.i(HwAudioServiceEx.TAG, "VBR HwAudioService:music app exit " + HwAudioServiceEx.this.PowerKitAppType);
                            if (HwAudioServiceEx.this.PowerKitAppType == 12) {
                                AudioSystem.setParameters("VBRMuiscState=exit");
                            } else if (HwAudioServiceEx.this.PowerKitAppType == 11) {
                                AudioSystem.setParameters("VBRIMState=exit");
                            } else {
                                Slog.v(HwAudioServiceEx.TAG, "no process apptype");
                            }
                        } else {
                            Slog.v(HwAudioServiceEx.TAG, "no process eventType");
                        }
                    }
                    if (stateType != 10009) {
                        return;
                    }
                    if (eventType == 1) {
                        if (HwAudioServiceEx.sSwsScene == 0) {
                            int unused = HwAudioServiceEx.sSwsScene = 1;
                            Slog.i(HwAudioServiceEx.TAG, "Video_Front state");
                            AudioSystem.setParameters("IPGPMode=video");
                        }
                    } else if (eventType != 2) {
                        Slog.v(HwAudioServiceEx.TAG, "no process eventType");
                    } else if (HwAudioServiceEx.sSwsScene != 0) {
                        int unused2 = HwAudioServiceEx.sSwsScene = 0;
                        Slog.i(HwAudioServiceEx.TAG, "Video_Not_Front state");
                        AudioSystem.setParameters("IPGPMode=audio");
                    }
                }
            }
        };
        this.mMyHandler = new Handler() {
            /* class com.android.server.audio.HwAudioServiceEx.AnonymousClass9 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i != 1) {
                    if (i == 2) {
                        HwAudioServiceEx.this.setDolbyOrSwsStateOpen();
                    }
                } else if (AudioSystem.getDeviceConnectionState(8, "") == 1 || AudioSystem.getDeviceConnectionState(4, "") == 1 || AudioSystem.getDeviceConnectionState((int) HwActivityTaskManagerServiceEx.APP_ATTRIBUTE_FORCE_NOT_ROTATE_WINDOW, "") == 1 || AudioSystem.getDeviceConnectionState((int) HwAudioServiceEx.RECORD_TYPE_OTHER, "") == 1) {
                    HwAudioServiceEx.this.setDolbyOrSwsStateClose();
                }
            }
        };
        this.mDmsDeathRecipient = new IHwBinder.DeathRecipient() {
            /* class com.android.server.audio.HwAudioServiceEx.AnonymousClass10 */

            public void serviceDied(long cookie) {
                if (HwAudioServiceEx.this.mHwAudioHandlerEx != null) {
                    Slog.e(HwAudioServiceEx.TAG, "Dolby service has died, try to reconnect 1s later.");
                    HwAudioServiceEx.sendMsgEx(HwAudioServiceEx.this.mHwAudioHandlerEx, 11, 0, 0, 0, null, 1000);
                }
            }
        };
        this.mReceiver = new BroadcastReceiver() {
            /* class com.android.server.audio.HwAudioServiceEx.AnonymousClass11 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null) {
                    if (action.equals("android.intent.action.REBOOT") || action.equals(SmartDualCardConsts.SYSTEM_STATE_NAME_ACTION_SHUTDOWN)) {
                        if (HwAudioServiceEx.DOLBY_SOUND_EFFECTS_SUPPORT && HwAudioServiceEx.this.mDms == null) {
                            HwAudioServiceEx.this.setDolbyServiceClient();
                        }
                        Slog.i(HwAudioServiceEx.TAG, "Release Dolby service HIDL client");
                        if (!(HwAudioServiceEx.this.mDms == null || HwAudioServiceEx.this.mDolbyClient == null)) {
                            try {
                                HwAudioServiceEx.this.mDms.unregisterClient(HwAudioServiceEx.this.mDolbyClient, 3, HwAudioServiceEx.this.mDolbyClient.hashCode());
                                HwAudioServiceEx.this.mDms.unlinkToDeath(HwAudioServiceEx.this.mDmsDeathRecipient);
                            } catch (RuntimeException e) {
                                Slog.e(HwAudioServiceEx.TAG, "Release Dolby RuntimeException");
                            } catch (Exception e2) {
                                Slog.e(HwAudioServiceEx.TAG, "Release Dolby error");
                            }
                        }
                        SystemProperties.set(HwAudioServiceEx.BOOT_VOLUME_PROPERTY, Integer.toString(AudioSystem.getStreamVolumeIndex(7, 2)));
                    } else if (HwAudioServiceEx.IS_SUPPORT_SLIDE && action.equals("com.huawei.systemserver.START")) {
                        boolean ret = HwAudioServiceEx.this.mCoverManager.registerHallCallback(HwAudioServiceEx.RECEIVER_NAME, 1, HwAudioServiceEx.this.mCallback);
                        Slog.d(HwAudioServiceEx.TAG, "registerHallCallback return " + ret);
                        if (HwAudioServiceEx.this.mCoverManager.getHallState(1) == 2) {
                            AudioSystem.setParameters("action_slide=true");
                            Slog.i(HwAudioServiceEx.TAG, "originSlideOnStatus True");
                        } else if (HwAudioServiceEx.this.mCoverManager.getHallState(1) == 0) {
                            AudioSystem.setParameters("action_slide=false");
                            Slog.i(HwAudioServiceEx.TAG, "originSlideOnStatus false");
                        } else {
                            Slog.e(HwAudioServiceEx.TAG, "no support hall");
                        }
                    } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                        HwAudioServiceEx.this.mBootCompleted = true;
                    } else {
                        Slog.v(HwAudioServiceEx.TAG, "action does not match");
                    }
                }
            }
        };
        this.mDolbyClient = new IDmsCallbacks.Stub() {
            /* class com.android.server.audio.HwAudioServiceEx.AnonymousClass12 */

            @Override // vendor.huawei.hardware.dolby.dms.V1_0.IDmsCallbacks
            public void onDapParamUpdate(ArrayList<Byte> params) {
                if (params.size() != 0) {
                    byte[] buf = new byte[params.size()];
                    for (int i = 0; i < buf.length; i++) {
                        buf[i] = params.get(i).byteValue();
                    }
                    int dlbParam = HwAudioServiceEx.byteArrayToInt32(buf, 0);
                    int status = HwAudioServiceEx.byteArrayToInt32(buf, 4);
                    Slog.d(HwAudioServiceEx.TAG, "Got dap param update, param = " + dlbParam + " status = " + status);
                    if (dlbParam == 0 || dlbParam == HwAudioServiceEx.EFFECT_PARAM_EFF_PROF) {
                        if (!HwAudioServiceEx.this.mSystemReady) {
                            Slog.w(HwAudioServiceEx.TAG, "onDapParamUpdate() called before boot complete");
                        } else {
                            HwAudioServiceEx.sendMsgEx(HwAudioServiceEx.this.mHwAudioHandlerEx, 12, 0, dlbParam, status, null, 1000);
                        }
                    }
                    if (dlbParam == 0) {
                        HwAudioServiceEx.this.mDolbyEnable = status;
                        if (HwAudioServiceEx.this.mHeadSetPlugState) {
                            SystemProperties.set(HwAudioServiceEx.DOLBY_MODE_HEADSET_STATE, HwAudioServiceEx.this.mDolbyEnable > 0 ? "on" : "off");
                        }
                    }
                }
            }
        };
        this.mRecordingListener = new IRecordingConfigDispatcher.Stub() {
            /* class com.android.server.audio.HwAudioServiceEx.AnonymousClass14 */

            /* JADX WARNING: Code restructure failed: missing block: B:46:0x015f, code lost:
                if (r7.isPrivacySensitiveSource(r7.mLastSilenceRec.getClientAudioSource()) != false) goto L_0x0161;
             */
            public void dispatchRecordingConfigChange(List<AudioRecordingConfiguration> configs) {
                if (configs == null) {
                    Log.w(HwAudioServiceEx.TAG, "dispatchRecordingConfigChange configs is null.");
                    HwAudioServiceEx.this.mLastSilenceRec = null;
                    return;
                }
                String appType = "";
                for (AudioRecordingConfiguration audioRecordingConfig : configs) {
                    if (audioRecordingConfig != null && !audioRecordingConfig.isClientSilenced()) {
                        if (!appType.isEmpty()) {
                            appType = appType + ",";
                        }
                        appType = appType + AppTypeRecoManager.getInstance().getAppType(audioRecordingConfig.getClientPackageName());
                    }
                }
                if (!appType.isEmpty()) {
                    AudioSystem.setParameters("VOIP_APPSCENE=" + appType);
                }
                Slog.i(HwAudioServiceEx.TAG, "dispatchRecordingConfigChange");
                ArrayList<String> packageNameList = new ArrayList<>();
                for (AudioRecordingConfiguration audioRecordingConfig2 : configs) {
                    if (audioRecordingConfig2 != null) {
                        String packageNames = audioRecordingConfig2.getClientPackageName();
                        if (!packageNameList.contains(packageNames)) {
                            packageNameList.add(packageNames);
                        }
                    }
                    HwAudioServiceEx.this.mPackageNameList = packageNameList;
                    Slog.i(HwAudioServiceEx.TAG, "dispatchRecordingConfigChange, sendBroadcastForAudioKit");
                    Bundle bundle = new Bundle();
                    bundle.putStringArrayList(HwAudioServiceEx.PACKAGENAME_LIST, packageNameList);
                    HwAudioServiceEx.this.mHwAudioHandlerEx.sendMessage(HwAudioServiceEx.this.mHwAudioHandlerEx.obtainMessage(19, bundle));
                }
                HwAudioServiceEx.this.findLastSilenceRecord(configs);
                if (HwAudioServiceEx.this.mAudioMode == 2 && HwAudioServiceEx.this.mLastSilenceRec != null) {
                    HwAudioServiceEx.this.mLastSilenceRec = null;
                }
                if (HwAudioServiceEx.this.mLastSilenceRec != null) {
                    AudioRecordingConfiguration activeRecord = null;
                    Iterator<AudioRecordingConfiguration> it = configs.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        AudioRecordingConfiguration arc = it.next();
                        if (!arc.isClientSilenced() && HwAudioServiceEx.this.isMicSource(arc.getClientAudioSource())) {
                            activeRecord = arc;
                            Slog.i(HwAudioServiceEx.TAG, "Audio record from active uid:" + HwAudioServiceEx.this.mLastSilenceRec.getClientUid() + " to silenced uid:" + activeRecord.getClientUid());
                            break;
                        }
                    }
                    if (activeRecord != null) {
                        if (!HwAudioServiceEx.this.isPrivacySensitiveSource(activeRecord.getClientAudioSource())) {
                            HwAudioServiceEx hwAudioServiceEx = HwAudioServiceEx.this;
                        }
                        if (!HwAudioServiceEx.this.mMultiAudioRecordToastShowing) {
                            HwAudioServiceEx hwAudioServiceEx2 = HwAudioServiceEx.this;
                            hwAudioServiceEx2.sendMicSilencedToastMesg(hwAudioServiceEx2.mLastSilenceRec.getClientPackageName(), activeRecord.getClientPackageName());
                        } else {
                            HwAudioServiceEx.this.mMultiAudioRecordToastShowing = false;
                        }
                        HwAudioServiceEx.this.mLastSilenceRec = null;
                    }
                }
                HwAudioServiceEx.this.mLastRecordingConfigs.clear();
                HwAudioServiceEx.this.mLastRecordingConfigs.addAll(configs);
                HwAudioServiceEx.this.mScoRecordService.checkScoRecording(configs);
                if (HwAudioServiceEx.this.mBluetoothDevice != null && HwAudioServiceEx.this.mIsBluetoothA2dpHD) {
                    for (AudioRecordingConfiguration arc2 : configs) {
                        int source = arc2.getClientAudioSource();
                        AudioDeviceInfo deviceInfo = arc2.getAudioDevice();
                        if (deviceInfo != null) {
                            int dType = deviceInfo.getType();
                            Slog.i(HwAudioServiceEx.TAG, "getAudioDevice getType: " + dType);
                            if (dType == 8 && (source == 0 || source == 1 || source == 5)) {
                                HwMediaMonitorManager.writeBigData(916700001, arc2.getClientPackageName(), source, arc2.getClientFormat().getSampleRate(), arc2.getClientFormat().getEncoding(), arc2.getClientFormat().getChannelCount());
                            }
                        } else {
                            return;
                        }
                    }
                }
            }
        };
        this.mFoldTipsListener = new HwFoldScreenManagerEx.FoldFsmTipsRequestListener() {
            /* class com.android.server.audio.HwAudioServiceEx.AnonymousClass15 */

            public void onRequestFsmTips(int reqTipsType, Bundle bundle) {
                HwAudioServiceEx.this.monitorFsmTipsRemoveBroadcast(bundle);
            }
        };
        this.mPlaybackActivityMonitorCallback = new AudioSystem.PlaybackActivityMonitorCallback() {
            /* class com.android.server.audio.HwAudioServiceEx.AnonymousClass16 */

            public void OnDeviceChanged(int session, int device, int ioHandle) {
                IAudioService audioService = HwAudioServiceEx.this.getAudioService();
                if (audioService == null) {
                    Slog.e(HwAudioServiceEx.TAG, "audioService is null");
                    return;
                }
                try {
                    for (AudioPlaybackConfiguration playback : audioService.getActivePlaybackConfigurations()) {
                        if (playback.getPkgName() != null) {
                            if (playback.getAudioSessionId() == session) {
                                try {
                                    if (playback.getPlayerState() == 2) {
                                        try {
                                            playback.setDevice(device);
                                            HwAudioServiceEx.sendMsgEx(HwAudioServiceEx.this.mHwAudioHandlerEx, 23, 2, playback.getDevice(), ioHandle, playback.getPkgName(), 0);
                                        } catch (RemoteException e) {
                                        }
                                    }
                                } catch (RemoteException e2) {
                                    Log.e(HwAudioServiceEx.TAG, "failed to getActivePlaybackConfigurations");
                                }
                            }
                        }
                    }
                } catch (RemoteException e3) {
                    Log.e(HwAudioServiceEx.TAG, "failed to getActivePlaybackConfigurations");
                }
            }
        };
        this.mDisplayListener = new HwFoldScreenManagerEx.FoldDisplayModeListener() {
            /* class com.android.server.audio.HwAudioServiceEx.AnonymousClass17 */

            public void onScreenDisplayModeChange(int displayMode) {
                IAudioService audioService = HwAudioServiceEx.this.getAudioService();
                if (audioService == null) {
                    Slog.e(HwAudioServiceEx.TAG, "audioService is null");
                    return;
                }
                try {
                    for (AudioPlaybackConfiguration playback : audioService.getActivePlaybackConfigurations()) {
                        if (playback.getPkgName() != null && playback.getPkgName().equals(HwAudioServiceEx.this.getTopActivityPackageName()) && playback.getPlayerState() == 2) {
                            HwAudioServiceEx.sendMsgEx(HwAudioServiceEx.this.mHwAudioHandlerEx, 23, 2, playback.getDevice(), 0, playback.getPkgName(), 0);
                        }
                    }
                } catch (RemoteException e) {
                    Log.e(HwAudioServiceEx.TAG, "failed to getActivePlaybackConfigurations");
                }
            }
        };
        this.mMultiAudioRecordCallback = new AudioSystem.MultiAudioRecordCallback() {
            /* class com.android.server.audio.HwAudioServiceEx.AnonymousClass18 */

            public void onMultiAudioRecord(int preUid, int newUid) {
                if (!HwAudioServiceEx.IS_MULTI_AUDIO_RECORD_ALLOWED || !HwAudioServiceEx.IS_CHINA_AREA) {
                    Slog.e(HwAudioServiceEx.TAG, "do not support MultiAudioRecord");
                    return;
                }
                PackageManager pm = HwAudioServiceEx.this.mContext.getPackageManager();
                String[] newPackages = pm.getPackagesForUid(newUid);
                if (newPackages == null) {
                    Slog.e(HwAudioServiceEx.TAG, "onMultiAudioRecord no newPackages for uid " + newUid);
                    return;
                }
                String[] prePackages = pm.getPackagesForUid(preUid);
                if (prePackages == null) {
                    Slog.e(HwAudioServiceEx.TAG, "onMultiAudioRecord no prePackages for uid " + preUid);
                    return;
                }
                int flag = HwAudioServiceEx.this.isMultiAudioRecordEnableFromProvider();
                if (flag == -1) {
                    Slog.i(HwAudioServiceEx.TAG, "onMultiAudioRecord flag = " + flag + ", show dialog");
                    HwAudioServiceEx.this.sendShowMultiAudioRecordDialogMesg();
                } else if (flag == 0) {
                    Slog.i(HwAudioServiceEx.TAG, "onMultiAudioRecord flag = " + flag + ", show notification and toast");
                    HwAudioServiceEx.this.sendShowMultiAudioRecordNotificationMesg();
                    for (String newPackageName : newPackages) {
                        HwAudioServiceEx.this.mMultiAudioRecordToastShowing = true;
                        HwAudioServiceEx.this.sendMultiAudioRecordToastMesg(newPackageName);
                    }
                } else {
                    String prePackageName = "";
                    String newPackageName2 = "";
                    for (int i = 0; i < newPackages.length; i++) {
                        newPackageName2 = newPackages[i];
                    }
                    for (int i2 = 0; i2 < prePackages.length; i2++) {
                        prePackageName = prePackages[i2];
                    }
                    Slog.i(HwAudioServiceEx.TAG, "onMultiAudioRecord write bigdata, preUid = " + preUid + ", newUid = " + newUid);
                    HwMediaMonitorManager.writeBigData(916600038, newPackageName2, prePackageName, "");
                }
            }
        };
        this.mIAsInner = ias;
        this.mContext = context;
        this.mContentResolver = this.mContext.getContentResolver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.REBOOT");
        intentFilter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_ACTION_SHUTDOWN);
        intentFilter.addAction("android.intent.action.BOOT_COMPLETED");
        intentFilter.addAction("com.huawei.systemserver.START");
        context.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, intentFilter, null, null);
        if (IS_SUPPORT_SLIDE) {
            this.mCoverManager = HwFrameworkFactory.getCoverManager();
            this.mCallback = new IHallCallback.Stub() {
                /* class com.android.server.audio.HwAudioServiceEx.AnonymousClass2 */

                public void onStateChange(HallState hallState) {
                    Slog.d(HwAudioServiceEx.TAG, "HallState=" + hallState);
                    if (hallState.state == 2) {
                        AudioSystem.setParameters("VOICE_SLIDE_STATUS=open");
                        Slog.i(HwAudioServiceEx.TAG, "set VOICE_SLIDE_STATUS open");
                    } else if (hallState.state == 0) {
                        AudioSystem.setParameters("VOICE_SLIDE_STATUS=close");
                        Slog.i(HwAudioServiceEx.TAG, "set VOICE_SLIDE_STATUS close");
                    } else {
                        Slog.e(HwAudioServiceEx.TAG, "hallState is not recognized");
                    }
                }
            };
        }
        createHwAudioHandlerExThread();
        readPersistedSettingsEx(this.mContentResolver);
        setAudioSystemParameters();
        ActivityManagerEx.registerHwActivityNotifier(this.mNotifierForLifeState, ACTIVITY_NOTIFY_REASON);
        if (this.mContext.getSystemService("notification") instanceof NotificationManager) {
            this.mNm = (NotificationManager) this.mContext.getSystemService("notification");
        }
        if (HwCustUtils.createObj(HwCustZenModeHelper.class, new Object[0]) instanceof HwCustZenModeHelper) {
            this.mCustZenModeHelper = (HwCustZenModeHelper) HwCustUtils.createObj(HwCustZenModeHelper.class, new Object[0]);
        }
        HwCustZenModeHelper hwCustZenModeHelper = this.mCustZenModeHelper;
        if (hwCustZenModeHelper != null) {
            this.mZenModeWhiteList = hwCustZenModeHelper.getWhiteAppsInZenMode();
        }
        AudioModeClient.sHwAudioService = this;
        this.mScoRecordService = new HwScoRecordService(this.mContext);
        this.mHwHistenNaturalModeService = new HwHistenNaturalModeService(this.mContext);
        this.mFanAndPhoneCoverObserver.startObserving("SUBSYSTEM=hw_power");
        registerBluetoothDevice(this.mContext);
        enableNotiOnEarpiece();
        AudioSystem.setMultiAudioRecordCallback(this.mMultiAudioRecordCallback);
        setMultiAudioRecordEnable(isMultiAudioRecordEnableFromProvider() == 1 ? true : z);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void closeProfileProxy() {
        BluetoothA2dp bluetoothA2dp = this.mA2dpProfile;
        if (bluetoothA2dp != null) {
            this.mBluetoothAdapter.closeProfileProxy(2, bluetoothA2dp);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean getBluetoothMetadata(byte[] byteValue) {
        if (!(byteValue instanceof byte[]) || byteValue == null || byteValue.length <= 0) {
            return false;
        }
        Slog.i(TAG, "getBluetoothMetadata byteValue[0]: " + ((int) byteValue[0]));
        if (byteValue[0] == 1) {
            return true;
        }
        return false;
    }

    private void registerBluetoothDevice(Context context) {
        IntentFilter intentFilter = new IntentFilter("android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED");
        intentFilter.addAction("android.bluetooth.headset.profile.action.ACTIVE_DEVICE_CHANGED");
        intentFilter.addAction("android.intent.action.BOOT_COMPLETED");
        intentFilter.addAction(ACTION_STATE_CHANGED);
        intentFilter.addAction(ACTION_ICARRY_DEVICE_LIST_CHANGED);
        intentFilter.addAction(ACTION_CONNECTION_STATE_CHANGED);
        context.registerReceiverAsUser(this.sBluetoothDeviceReceiver, UserHandle.ALL, intentFilter, null, null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendBluetoothHanderMsg(boolean isBluetoothHDEnable) {
        Slog.i(TAG, "sendBluetoothHanderMsg");
        Intent intent = new Intent();
        intent.setAction(ACTION_BLUETOOTH_FB_APP_FOR_KIT);
        intent.putExtra("isBluetoothHDEnable", isBluetoothHDEnable);
        long token = Binder.clearCallingIdentity();
        try {
            this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT, PERMISSION_BLUETOOTH_FB_APP_FOR_KIT, null);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private void createHwAudioHandlerExThread() {
        this.mHwAudioHandlerExThread = new HwAudioHandlerExThread();
        this.mHwAudioHandlerExThread.start();
        waitForHwAudioHandlerExCreation();
    }

    private void waitForHwAudioHandlerExCreation() {
        synchronized (this) {
            while (this.mHwAudioHandlerEx == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Slog.e(TAG, "Interrupted while waiting on HwAudioHandlerEx.");
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class HwAudioHandlerExThread extends Thread {
        HwAudioHandlerExThread() {
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            Looper.prepare();
            synchronized (HwAudioServiceEx.this) {
                HwAudioServiceEx.this.mHwAudioHandlerEx = new HwAudioHandlerEx();
                HwAudioServiceEx.this.notifyAll();
            }
            try {
                Looper.loop();
            } catch (IllegalStateException e) {
                Log.e(HwAudioServiceEx.TAG, "catch IllegalStateException" + e.getMessage());
            }
        }
    }

    private void readPersistedSettingsEx(ContentResolver cr) {
        if (SOUND_EFFECTS_SUPPORT) {
            getEffectsState(cr);
        }
    }

    private void setAudioSystemParameters() {
        if (SOUND_EFFECTS_SUPPORT) {
            setEffectsState();
        }
    }

    public void setSystemReady() {
        this.mSystemReady = true;
        this.mConcurrentCaptureEnable = SystemProperties.getBoolean(CONCURRENT_CAPTURE_PROPERTY, false);
        if (DOLBY_CHANNEL_COUNT == 4) {
            this.mHwAudioHandlerEx.sendEmptyMessageDelayed(16, 1000);
        }
        try {
            getAudioService().registerRecordingCallback(this.mRecordingListener);
            getAudioService().registerPlaybackCallback(this.mHwHistenNaturalModeService.sPlaybackListener);
            ((AudioManager) this.mContext.getSystemService("audio")).registerAudioDeviceCallback(this.mHwHistenNaturalModeService.sAudioDeviceCallback, null);
        } catch (Exception e) {
            Slog.e(TAG, "set drm stop prop fail");
        }
    }

    public void processAudioServerRestart() {
        Slog.i(TAG, "audioserver restart, resume audio settings and parameters");
        readPersistedSettingsEx(this.mContentResolver);
        setAudioSystemParameters();
        enableNotiOnEarpiece();
        AudioSystem.setMultiAudioRecordCallback(this.mMultiAudioRecordCallback);
        boolean z = true;
        if (isMultiAudioRecordEnableFromProvider() != 1) {
            z = false;
        }
        setMultiAudioRecordEnable(z);
    }

    private int identifyAudioDevice(int type) {
        int device = -1;
        if ((ANALOG_TYPEC_DEVICES & type) != 0) {
            if (AppActConstant.VALUE_TRUE.equals(AudioSystem.getParameters(ANALOG_TYPEC_FLAG))) {
                device = 1;
            }
        } else if (type == 67108864) {
            device = 2;
        } else {
            device = -1;
        }
        Slog.e(TAG, "identifyAudioDevice return device: " + device);
        return device;
    }

    private void broadcastHiresIntent(boolean showHiResIcon) {
        if (showHiResIcon != this.mShowHiResIcon) {
            this.mShowHiResIcon = showHiResIcon;
            Intent intent = new Intent();
            intent.setAction(showHiResIcon ? SHOW_HIRES_ICON : HIDE_HIRES_ICON);
            try {
                Slog.i(TAG, "sendBroadcastAsUser broadcastHiresIntent " + showHiResIcon);
                this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, PERMISSION_HIRES_CHANGE);
            } catch (IllegalStateException e) {
                Slog.e(TAG, "broadcastHiresIntent meet exception");
            }
        }
    }

    public void notifyHiResIcon(int event) {
        if (!this.mIsDigitalTypecOn) {
            return;
        }
        if (event == 2 || event == 3 || event == 4) {
            this.mHwAudioHandlerEx.sendEmptyMessageDelayed(10, 500);
        }
    }

    public void notifyStartDolbyDms(int event) {
        if (!this.mDmsStarted && event == 2) {
            this.mHwAudioHandlerEx.sendEmptyMessageDelayed(16, 200);
        }
    }

    public void updateTypeCNotify(int type, int state, String name) {
        int recognizedDevice = identifyAudioDevice(type);
        Slog.i(TAG, "updateTypeCNotify recognizedDevice: " + recognizedDevice);
        if (state != 0) {
            this.mHwAudioHandlerEx.sendEmptyMessageDelayed(10, 1000);
            if (recognizedDevice == 1) {
                notifyAnalogTypec(recognizedDevice);
            } else if (recognizedDevice != 2) {
                Slog.e(TAG, "updateTypeCNotify unknown device: " + recognizedDevice);
            } else {
                this.mIsDigitalTypecOn = true;
                boolean needTip = needTipForDigitalTypeC();
                Slog.i(TAG, "updateTypeCNotify needTip: " + needTip);
                if (Settings.System.getIntForUser(this.mContext.getContentResolver(), "typec_digital_enabled", 1, -2) == 1 && needTip) {
                    notifyTypecConnected(2);
                    if (!this.mIsDigitalTypecReceiverRegisterd) {
                        registerTypecReceiver(recognizedDevice);
                        this.mIsDigitalTypecReceiverRegisterd = true;
                    }
                }
                Settings.System.putIntForUser(this.mContext.getContentResolver(), "typec_analog_enabled", 1, -2);
            }
        } else {
            if (recognizedDevice == 2) {
                this.mIsDigitalTypecOn = false;
            }
            broadcastHiresIntent(false);
            Slog.i(TAG, "updateTypeCNotify plug out device: " + recognizedDevice);
            dismissNotification(recognizedDevice);
        }
        if (SWS_TYPEC_ADAPTE_SUPPORT) {
            setTypecParamAsync(state, type, name);
        }
    }

    private void notifyAnalogTypec(int recognizedDevice) {
        if (Settings.System.getIntForUser(this.mContext.getContentResolver(), "typec_analog_enabled", 1, -2) == 1) {
            notifyTypecConnected(1);
            if (!this.mIsAnalogTypecReceiverRegisterd) {
                registerTypecReceiver(recognizedDevice);
                this.mIsAnalogTypecReceiverRegisterd = true;
            }
        }
        Settings.System.putIntForUser(this.mContext.getContentResolver(), "typec_digital_enabled", 1, -2);
    }

    private boolean needTipForDigitalTypeC() {
        boolean isGoodTypec = AppActConstant.VALUE_TRUE.equals(AudioSystem.getParameters(DIGITAL_TYPEC_FLAG));
        boolean isNeedTip = AppActConstant.VALUE_TRUE.equals(AudioSystem.getParameters(DIGITAL_TYPEC_REPORT_FLAG));
        if (IS_USB_POWER_COSUME_TIPS) {
            return isNeedTip;
        }
        return !isGoodTypec && isNeedTip;
    }

    private void notifyTypecConnected(int device) {
        if (this.mContext == null || NOT_SHOW_TYPEC_TIP) {
            Slog.i(TAG, "context is null or NOT_SHOW_TYPEC_TIP: " + NOT_SHOW_TYPEC_TIP);
            return;
        }
        Slog.i(TAG, "notifyTypecConnected device: " + device);
        PendingIntent pi = getNeverNotifyIntent(device);
        TipRes tipRes = new TipRes(device, this.mContext);
        this.mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        this.mNotificationManager.createNotificationChannel(new NotificationChannel(tipRes.mChannelName, tipRes.mChannelName, 1));
        this.mNotificationManager.notifyAsUser(TAG, tipRes.mTypecNotificationId, new Notification.Builder(this.mContext).setSmallIcon(33751741).setContentTitle(tipRes.mTitle).setContentText(tipRes.mContent).setStyle(new Notification.BigTextStyle().bigText(tipRes.mContent)).addAction(0, tipRes.mTip, pi).setAutoCancel(true).setChannelId(tipRes.mChannelName).setDefaults(-1).build(), UserHandle.ALL);
    }

    /* access modifiers changed from: private */
    public static class TipRes {
        public String mChannelName = null;
        public String mContent = null;
        public Context mContext = null;
        public String mTip = null;
        public String mTitle = null;
        public int mTypecNotificationId = 0;

        public TipRes(int device, Context context) {
            this.mContext = context;
            if (device == 1) {
                this.mChannelName = this.mContext.getResources().getString(33685982);
                this.mTitle = this.mContext.getResources().getString(33685983);
                this.mContent = this.mContext.getResources().getString(33685984);
                this.mTip = this.mContext.getResources().getString(33685985);
                this.mTypecNotificationId = 1;
            } else if (device != 2) {
                Slog.e(HwAudioServiceEx.TAG, "TipRes constructor unKnown device");
            } else {
                this.mChannelName = this.mContext.getResources().getString(33685982);
                if (HwAudioServiceEx.IS_USB_POWER_COSUME_TIPS) {
                    Slog.i(HwAudioServiceEx.TAG, "Tips for pad, IS_USB_POWER_COSUME_TIPS = " + HwAudioServiceEx.IS_USB_POWER_COSUME_TIPS);
                    this.mTitle = this.mContext.getResources().getString(33686105);
                    this.mContent = this.mContext.getResources().getString(33686106);
                } else {
                    this.mTitle = this.mContext.getResources().getString(33686004);
                    this.mContent = this.mContext.getResources().getString(33686005);
                }
                this.mTip = this.mContext.getResources().getString(33685985);
                this.mTypecNotificationId = 2;
            }
        }
    }

    private PendingIntent getNeverNotifyIntent(int device) {
        Intent intent = null;
        if (device == 1) {
            intent = new Intent(ACTION_ANALOG_TYPEC_NOTIFY);
        } else if (device != 2) {
            Slog.e(TAG, "getNeverNotifyIntentAndUpdateTextId unKnown device");
        } else {
            intent = new Intent(ACTION_DIGITAL_TYPEC_NOTIFY);
        }
        return PendingIntent.getBroadcast(this.mContext, 0, intent, 268435456);
    }

    private void dismissNotification(int device) {
        NotificationManager notificationManager = this.mNotificationManager;
        if (notificationManager != null) {
            if (device == 1) {
                notificationManager.cancel(TAG, 1);
            } else if (device != 2) {
                Slog.e(TAG, "dismissNotification unKnown device: " + device);
            } else {
                notificationManager.cancel(TAG, 2);
            }
        }
    }

    private String getTypecAction(int device) {
        if (device == 1) {
            return ACTION_ANALOG_TYPEC_NOTIFY;
        }
        if (device == 2) {
            return ACTION_DIGITAL_TYPEC_NOTIFY;
        }
        Slog.e(TAG, "getTypecAction unKnown device: " + device);
        return null;
    }

    private void registerTypecReceiver(int device) {
        Slog.i(TAG, "registerTypecReceiver device: " + device);
        registerReceiverForTypec(device, new BroadcastReceiver() {
            /* class com.android.server.audio.HwAudioServiceEx.AnonymousClass6 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent == null) {
                    Slog.e(HwAudioServiceEx.TAG, "registerTypecReceiver null intent");
                    return;
                }
                String action = intent.getAction();
                if (action == null) {
                    Slog.e(HwAudioServiceEx.TAG, "registerTypecReceiver null action");
                    return;
                }
                char c = 65535;
                int hashCode = action.hashCode();
                if (hashCode != -2146785072) {
                    if (hashCode == 1549278377 && action.equals(HwAudioServiceEx.ACTION_ANALOG_TYPEC_NOTIFY)) {
                        c = 0;
                    }
                } else if (action.equals(HwAudioServiceEx.ACTION_DIGITAL_TYPEC_NOTIFY)) {
                    c = 1;
                }
                if (c == 0) {
                    Settings.System.putIntForUser(HwAudioServiceEx.this.mContext.getContentResolver(), "typec_analog_enabled", 0, -2);
                    HwAudioServiceEx.this.mNotificationManager.cancel(HwAudioServiceEx.TAG, 1);
                    HwAudioServiceEx.this.mIsAnalogTypecReceiverRegisterd = false;
                } else if (c != 1) {
                    Slog.e(HwAudioServiceEx.TAG, "registerTypecReceiver unKnown action");
                } else {
                    Settings.System.putIntForUser(HwAudioServiceEx.this.mContext.getContentResolver(), "typec_digital_enabled", 0, -2);
                    HwAudioServiceEx.this.mNotificationManager.cancel(HwAudioServiceEx.TAG, 2);
                    HwAudioServiceEx.this.mIsDigitalTypecReceiverRegisterd = false;
                }
                HwAudioServiceEx.this.mNotificationManager = null;
                if (HwAudioServiceEx.this.mContext != null) {
                    HwAudioServiceEx.this.mContext.unregisterReceiver(this);
                }
            }
        });
    }

    private void registerReceiverForTypec(int device, BroadcastReceiver typecReceiver) {
        if (device == 1) {
            this.mAnalogTypecReceiver = typecReceiver;
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(getTypecAction(device));
            this.mContext.registerReceiver(this.mAnalogTypecReceiver, intentFilter);
        } else if (device != 2) {
            Slog.e(TAG, "dismissNotification unKnown device: " + device);
        } else {
            this.mDigitalTypecReceiver = typecReceiver;
            IntentFilter intentFilterDigital = new IntentFilter();
            intentFilterDigital.addAction(getTypecAction(device));
            this.mContext.registerReceiver(this.mDigitalTypecReceiver, intentFilterDigital);
        }
    }

    /* access modifiers changed from: private */
    public class HwAudioHandlerEx extends Handler {
        private HwAudioHandlerEx() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            boolean shouldSendAgain = true;
            switch (msg.what) {
                case 10:
                    HwAudioServiceEx.this.msgShowOrHideHires();
                    return;
                case 11:
                    HwAudioServiceEx.this.setDolbyServiceClient();
                    return;
                case 12:
                    HwAudioServiceEx.this.sendDolbyUpdateBroadcast(msg.arg1, msg.arg2);
                    return;
                case 13:
                    HwAudioServiceEx.this.msgSoundEffectState(msg);
                    return;
                case 14:
                    HwAudioServiceEx.this.msgTypecParam(msg);
                    return;
                case 15:
                    HwAudioServiceEx.this.initPowerKit();
                    return;
                case 16:
                    boolean isMusicActive = AudioSystem.isStreamActive(3, 0);
                    boolean isRingActive = AudioSystem.isStreamActive(2, 0);
                    boolean isAlarmActive = AudioSystem.isStreamActive(4, 0);
                    Slog.i(HwAudioServiceEx.TAG, "restart dms isMusicActive : " + isMusicActive + ", isRingActive : " + isRingActive + ", isAlarmActive : " + isAlarmActive);
                    if (!(!isMusicActive && !isRingActive && !isAlarmActive && HwAudioServiceEx.DOLBY_CHANNEL_COUNT != 4)) {
                        boolean bootanimStoped = SystemProperties.getBoolean("service.bootanim.stop", false);
                        if (HwAudioServiceEx.this.mBootCompleted || bootanimStoped || HwAudioServiceEx.DOLBY_CHANNEL_COUNT != 4) {
                            shouldSendAgain = false;
                        }
                        if (shouldSendAgain) {
                            HwAudioServiceEx.this.mHwAudioHandlerEx.sendEmptyMessageDelayed(16, 1000);
                            return;
                        } else {
                            HwAudioServiceEx.this.setDolbyStatus();
                            return;
                        }
                    } else {
                        return;
                    }
                case 17:
                    HwAudioServiceEx.this.checkAndSetSoundEffectState(msg.arg1, msg.arg2);
                    return;
                case 18:
                    HwAudioServiceEx.this.msgShowRecordSilenceToast(msg);
                    return;
                case 19:
                    HwAudioServiceEx.this.setMsgSendAudiokitBroadcast(msg);
                    return;
                case 20:
                    Slog.i(HwAudioServiceEx.TAG, "MSG_START_SWS_SERVICE device : " + msg.arg1 + " state : " + msg.arg2 + " mSwsbind : " + HwAudioServiceEx.this.mSwsbind + " mSwsConnect : " + HwAudioServiceEx.this.mSwsConnect);
                    if (msg.arg2 == 1 && !HwAudioServiceEx.this.mSwsConnect) {
                        Intent startIntent = new Intent("android.intent.action.VIEW");
                        startIntent.setClassName(HwAudioServiceEx.this.swsPackageName, HwAudioServiceEx.this.swsClassName);
                        Context context = HwAudioServiceEx.this.mContext;
                        ServiceConnection serviceConnection = HwAudioServiceEx.this.mSwsServiceConnect;
                        Context unused = HwAudioServiceEx.this.mContext;
                        context.bindServiceAsUser(startIntent, serviceConnection, 1, UserHandle.CURRENT);
                        HwAudioServiceEx.this.mSwsbind = true;
                    }
                    if (msg.arg2 == 0 && HwAudioServiceEx.this.mSwsConnect) {
                        try {
                            HwAudioServiceEx.this.mContext.unbindService(HwAudioServiceEx.this.mSwsServiceConnect);
                            HwAudioServiceEx.this.mSwsbind = false;
                            HwAudioServiceEx.this.mSwsConnect = false;
                            return;
                        } catch (IllegalArgumentException e) {
                            Slog.e(HwAudioServiceEx.TAG, "IllegalArgumentException while sws unbindService.");
                            return;
                        }
                    } else {
                        return;
                    }
                case 21:
                    HwAudioServiceEx hwAudioServiceEx = HwAudioServiceEx.this;
                    hwAudioServiceEx.sendBluetoothHanderMsg(hwAudioServiceEx.mIsBluetoothHDEnable);
                    return;
                case 22:
                    HwAudioServiceEx.this.setMsgSendAudiokitKillStateBroadcast(msg);
                    return;
                case 23:
                    if (msg.obj != null && (msg.obj instanceof String)) {
                        HwAudioServiceEx.this.checkAppPlayOnEarpiece((String) msg.obj, msg.arg1, msg.arg2);
                        return;
                    }
                    return;
                case 24:
                    HwAudioServiceEx.this.msgShowMultiAudioRecordToast(msg);
                    return;
                case 25:
                    HwAudioServiceEx.this.notifyMultiAudioRecord();
                    return;
                case 26:
                    HwAudioServiceEx.this.showMultiAudioRecordDialog();
                    return;
                default:
                    Slog.i(HwAudioServiceEx.TAG, "HwAudioHandlerEx receive unknown msg");
                    return;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void msgShowOrHideHires() {
        if (AppActConstant.VALUE_TRUE.equals(AudioSystem.getParameters(HIRES_REPORT_FLAG))) {
            broadcastHiresIntent(true);
        } else {
            broadcastHiresIntent(false);
        }
        initPowerKit();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void msgSoundEffectState(Message msg) {
        if (msg.obj != null && (msg.obj instanceof Bundle)) {
            Bundle bundle = (Bundle) msg.obj;
            boolean restore = bundle.getBoolean(RESTORE);
            String packageName = bundle.getString("packageName");
            boolean isOnTop = bundle.getBoolean(ISONTOP);
            String reserved = bundle.getString(RESERVED);
            if (TextUtils.isEmpty(packageName)) {
                Slog.w(TAG, "SoundEffectState bundle get pkg name is invalid");
                packageName = "";
            }
            if (TextUtils.isEmpty(reserved)) {
                Slog.w(TAG, "SoundEffectState bundle get reserved is invalid");
                reserved = "";
            }
            setSoundEffectStateAsynch(restore, packageName, isOnTop, reserved);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void msgTypecParam(Message msg) {
        if (msg.obj != null && (msg.obj instanceof String)) {
            String name = (String) msg.obj;
            if (msg.arg1 == 1) {
                setTypecParamToAudioSystem(name);
            } else {
                restoreTypecParam(name);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void msgShowRecordSilenceToast(Message msg) {
        if (msg.obj != null && (msg.obj instanceof Bundle)) {
            Bundle bundle = (Bundle) msg.obj;
            String silencedPkgName = bundle.getString(SILENCE_PKG_STR);
            String activePkgName = bundle.getString(ACTIVE_PKG_STR);
            if (silencedPkgName == null || activePkgName == null) {
                Slog.e(TAG, "bundle getString is invalid");
            } else {
                showMicSilencedToast(silencedPkgName, activePkgName);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setMsgSendAudiokitBroadcast(Message msg) {
        Slog.i(TAG, "MSG_SEND_AUDIOKIT_BROADCAST");
        if (msg.obj != null && (msg.obj instanceof Bundle) && (msg.obj instanceof Bundle)) {
            sendRecordNameBroadCastForAudioKit((Bundle) msg.obj);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setMsgSendAudiokitKillStateBroadcast(Message msg) {
        Bundle bundle;
        Slog.i(TAG, "setMsgSendAudiokitKillStateBroadcast");
        if (msg.obj != null && (msg.obj instanceof Bundle) && (msg.obj instanceof Bundle) && (bundle = (Bundle) msg.obj) != null) {
            Intent intent = new Intent();
            intent.setAction(ACTION_KILLED_APP_FOR_KIT);
            intent.putExtra("packageName", bundle.getString("packageNameForKitKillState"));
            intent.putExtra(ISONTOP, bundle.getBoolean("isPackageOnResume"));
            intent.setPackage(ENGINE_PACKAGE_NAME);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT, PERMISSION_KILLED_APP_FOR_KIT);
            this.mPackageKillStateMap.clear();
        }
    }

    private void sendRecordNameBroadCastForAudioKit(Bundle bundle) {
        Slog.i(TAG, "sendRecordNameBroadCastForAudioKit");
        synchronized (LOCK_AUDIOKIT_RECORDNAME) {
            if (bundle != null) {
                try {
                    ArrayList<String> packageNameList = bundle.getStringArrayList(PACKAGENAME_LIST);
                    ActivityInfo activityInfo = ActivityManagerEx.getLastResumedActivity();
                    Slog.i(TAG, "sendRecordNameBroadCastForAudioKit, bundle not null");
                    String topPackageName = null;
                    if (activityInfo != null) {
                        topPackageName = activityInfo.packageName;
                        Slog.i(TAG, "sendRecordNameBroadCastForAudioKit, topPackageName");
                    }
                    if (packageNameList == null) {
                        Slog.i(TAG, "sendRecordNameBroadCastForAudioKit, packageNameList is null");
                        return;
                    }
                    Iterator<String> it = packageNameList.iterator();
                    while (it.hasNext()) {
                        String packageName = it.next();
                        if (packageName != null && packageName.equals(topPackageName)) {
                            Intent intent = new Intent();
                            intent.setAction(ACTION_RECORDNAME_APP_FOR_KIT);
                            intent.putExtra("packageName", packageName);
                            intent.putExtra("isBluetoothHDEnable", this.mIsBluetoothHDEnable);
                            if (this.mKaraokeWhiteList != null && this.mKaraokeWhiteList.contains(packageName)) {
                                Slog.i(TAG, "isKaraokeEnable");
                                intent.putExtra("isKaraokeEnable", true);
                            }
                            Slog.i(TAG, "sendBroadcastAsUser");
                            intent.setPackage(ENGINE_PACKAGE_NAME);
                            this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT, PERMISSION_RECORDNAME_APP_FOR_KIT);
                        }
                    }
                } catch (IndexOutOfBoundsException e) {
                    Slog.i(TAG, "HwAudioKaraokeFeature, IndexOutOfBoundsException");
                } catch (ConcurrentModificationException e2) {
                    Slog.i(TAG, "HwAudioKaraokeFeature, ConcurrentModificationException");
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static void sendMsgEx(Handler handler, int msg, int existingMsgPolicy, int arg1, int arg2, Object obj, int delay) {
        if (existingMsgPolicy == 0) {
            handler.removeMessages(msg);
        } else if (existingMsgPolicy != 1 || !handler.hasMessages(msg)) {
            Slog.i(TAG, "sendMsgEx, existingMsgPolicy = " + existingMsgPolicy);
        } else {
            return;
        }
        handler.sendMessageAtTime(handler.obtainMessage(msg, arg1, arg2, obj), SystemClock.uptimeMillis() + ((long) delay));
    }

    private String getPackageNameByPid(int pid) {
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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

    private String getApplicationLabel(String pkgName) {
        try {
            PackageManager packageManager = this.mContext.getPackageManager();
            return packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkgName, 0)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            Slog.e(TAG, "getApplicationLabel exception", e);
            return null;
        }
    }

    private void getAppInWhiteBlackList(List<String> whiteAppList) {
        InputStream inputStream = null;
        XmlPullParser xmlParser = null;
        try {
            File configFile = HwCfgFilePolicy.getCfgFile(CONFIG_FILE_WHITE_BLACK_APP, 0);
            if (configFile != null) {
                Slog.v(TAG, "HwCfgFilePolicy getCfgFile not null, path = " + configFile.getPath());
                inputStream = new FileInputStream(configFile.getPath());
                xmlParser = Xml.newPullParser();
                xmlParser.setInput(inputStream, null);
            } else {
                Slog.e(TAG, "HwCfgFilePolicy getCfgFile is null");
            }
            if (xmlParser != null) {
                parseXmlForWhiteBlackList(xmlParser, whiteAppList);
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Slog.e(TAG, "Karaoke IO Close Fail");
                }
            }
        } catch (NoExtAPIException e2) {
            Slog.e(TAG, "Karaoke NoExtAPIException");
            if (0 != 0) {
                inputStream.close();
            }
        } catch (FileNotFoundException e3) {
            Slog.e(TAG, "Karaoke FileNotFoundException");
            if (0 != 0) {
                inputStream.close();
            }
        } catch (XmlPullParserException e4) {
            Slog.e(TAG, "Karaoke XmlPullParserException");
            if (0 != 0) {
                inputStream.close();
            }
        } catch (Exception e5) {
            Slog.e(TAG, "Karaoke getAppInWhiteBlackList Exception ", e5);
            if (0 != 0) {
                inputStream.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    inputStream.close();
                } catch (IOException e6) {
                    Slog.e(TAG, "Karaoke IO Close Fail");
                }
            }
            throw th;
        }
    }

    private void parseXmlForWhiteBlackList(XmlPullParser parser, List<String> whiteAppList) {
        try {
            int eventType = parser.next();
            while (eventType != 1) {
                if (eventType == 2 && parser.getName().equals(NODE_WHITEAPP)) {
                    addPackageNameToWhiteList(whiteAppList, parser.getAttributeValue(null, NODE_ATTR_PACKAGE));
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException e) {
            Slog.e(TAG, "Karaoke XmlPullParserException");
        } catch (IOException e2) {
            Slog.e(TAG, "Karaoke IOException");
        }
    }

    private void addPackageNameToWhiteList(List<String> whiteAppList, String packageName) {
        if (isValidCharSequence(packageName)) {
            whiteAppList.add(packageName);
        }
    }

    private boolean isValidCharSequence(CharSequence charSeq) {
        if (charSeq == null || charSeq.length() == 0) {
            return false;
        }
        return true;
    }

    private void sendStartIntentForKaraoke(String packageName, boolean isOnTop) {
        if (HW_KARAOKE_EFFECT_ENABLED) {
            if (!this.mSystemReady) {
                Slog.e(TAG, "Start Karaoke system is not ready! ");
                return;
            }
            initHwKaraokeWhiteList();
            checkKaraokeWhiteAppUIDByPkgName(packageName);
            int uid = UserHandle.getAppId(getUidByPkg(packageName));
            if (isOnTop && !Objects.equals(packageName, this.mStartedPackageName)) {
                this.mStartedPackageName = packageName;
                if (this.mKaraokeWhiteList.contains(packageName)) {
                    if (AppActConstant.VALUE_TRUE.equals(AudioSystem.getParameters("queryKaraokeWhitePkg=" + uid))) {
                        startIntentForKaraoke(packageName, false);
                    }
                }
            }
        }
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
        if (pkgName == null || pkgName == "") {
            return -1;
        }
        try {
            return this.mContext.getPackageManager().getPackageUidAsUser(pkgName, getCurrentUserId());
        } catch (PackageManager.NameNotFoundException e) {
            return -1;
        }
    }

    public boolean isKaraokeWhiteListApp(String pkgName) {
        ArrayList<String> arrayList;
        if (pkgName == null || "".equals(pkgName) || (arrayList = this.mKaraokeWhiteList) == null) {
            return false;
        }
        int arraySize = arrayList.size();
        for (int i = 0; i < arraySize; i++) {
            if (pkgName.equals(this.mKaraokeWhiteList.get(i))) {
                return true;
            }
        }
        return false;
    }

    public void setKaraokeWhiteAppUIDByPkgName(String pkgName) {
        if (pkgName != null && !"".equals(pkgName)) {
            int uid = getUidByPkg(pkgName);
            if (uid == -1) {
                Slog.i(TAG, "combine white app package uid not found");
                return;
            }
            int uid2 = UserHandle.getAppId(uid);
            this.mKaraokeUidsMap.put(pkgName, Integer.valueOf(uid2));
            ((AudioManager) this.mContext.getSystemService("audio")).setParameters("AddKaraokeWhiteUID=" + String.valueOf(uid2));
        }
    }

    public void removeKaraokeWhiteAppUIDByPkgName(String pkgName) {
        Map<String, Integer> map;
        if (pkgName != null && !"".equals(pkgName) && (map = this.mKaraokeUidsMap) != null) {
            if (map.get(pkgName) == null) {
                Slog.e(TAG, "mKaraokeUidsMap get pkgName is null");
                return;
            }
            int uid = this.mKaraokeUidsMap.get(pkgName).intValue();
            if (this.mContext.getSystemService("audio") instanceof AudioManager) {
                ((AudioManager) this.mContext.getSystemService("audio")).setParameters("RemoveKaraokeWhiteUID=" + String.valueOf(uid));
            }
            this.mKaraokeUidsMap.remove(pkgName);
        }
    }

    public void setKaraokeWhiteListUID() {
        ArrayList<String> arrayList = this.mKaraokeWhiteList;
        if (arrayList != null) {
            int arraySize = arrayList.size();
            for (int i = 0; i < arraySize; i++) {
                setKaraokeWhiteAppUIDByPkgName(this.mKaraokeWhiteList.get(i));
            }
        }
    }

    private void initHwKaraokeWhiteList() {
        if (this.mKaraokeWhiteList == null) {
            this.mKaraokeWhiteList = new ArrayList<>();
            getAppInWhiteBlackList(this.mKaraokeWhiteList);
            Slog.i(TAG, "karaoke white list =" + this.mKaraokeWhiteList.toString());
            setKaraokeWhiteListUID();
        }
    }

    private void checkKaraokeWhiteAppUIDByPkgName(String pkgName) {
        Map<String, Integer> map;
        if (!(pkgName == null || "".equals(pkgName)) && (map = this.mKaraokeUidsMap) != null && this.mKaraokeWhiteList != null && map.get(pkgName) == null && this.mKaraokeWhiteList.contains(pkgName)) {
            setKaraokeWhiteAppUIDByPkgName(pkgName);
        }
    }

    public boolean isHwKaraokeEffectEnable(String packageName) {
        if (!HW_KARAOKE_EFFECT_ENABLED) {
            Slog.e(TAG, "prop do not support");
            return false;
        } else if (!this.mSystemReady) {
            Slog.e(TAG, "Start Karaoke system is not ready! ");
            return false;
        } else {
            initHwKaraokeWhiteList();
            if (this.mKaraokeWhiteList.contains(packageName)) {
                Slog.v(TAG, "app in white list");
                return true;
            }
            Slog.v(TAG, "not in white list");
            return false;
        }
    }

    public void sendAudioRecordStateChangedIntent(String sender, int state, int pid, String packageName) {
        if (state == -1) {
            if (this.mAudioMode == 2) {
                sendMicSilencedToastMesg(packageName, PHONE_PKG);
            }
        } else if (HW_KARAOKE_EFFECT_ENABLED) {
            sendAudioRecordStateForKaraoke(sender, state, pid, packageName);
        }
    }

    private void sendAudioRecordStateForKaraoke(String sender, int state, int pid, String packageName) {
        Throwable th;
        if (this.mContext.checkCallingPermission("android.permission.RECORD_AUDIO") == -1) {
            Slog.e(TAG, "sendAudioRecordStateChangedIntent dennied from pid:" + pid);
            return;
        }
        Intent intent = new Intent();
        intent.setAction(ACTION_SEND_AUDIO_RECORD_STATE);
        intent.addFlags(268435456);
        intent.putExtra("sender", sender);
        intent.putExtra(ACTIVITY_NOTIFY_STATE, state);
        intent.putExtra("packagename", packageName);
        long ident = Binder.clearCallingIdentity();
        try {
            try {
                ActivityManagerNative.getDefault().broadcastIntent((IApplicationThread) null, intent, (String) null, (IIntentReceiver) null, -1, (String) null, (Bundle) null, new String[]{PERMISSION_SEND_AUDIO_RECORD_STATE}, -1, (Bundle) null, false, false, -2);
            } catch (RemoteException e) {
            }
        } catch (RemoteException e2) {
            try {
                Slog.e(TAG, "sendAudioRecordStateChangedIntent failed: catch RemoteException!");
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th2) {
                th = th2;
            }
        } catch (Throwable th3) {
            th = th3;
            Binder.restoreCallingIdentity(ident);
            throw th;
        }
        Binder.restoreCallingIdentity(ident);
    }

    public void notifySendBroadcastForKaraoke(int uid) {
        PackageManager pm;
        if (this.mIAsInner.checkAudioSettingsPermissionEx("notifySendBroadcastForKaraoke()")) {
            Context context = this.mContext;
            if (!(context == null || (pm = context.getPackageManager()) == null)) {
                String pkgName = pm.getNameForUid(uid);
                if (pkgName != null && !this.mAudioKitList.contains(pkgName)) {
                    this.mAudioKitList.add(pkgName);
                }
                startIntentForKaraoke(pkgName, true);
            }
            for (String key : this.mPackageKillStateMap.keySet()) {
                if (this.mAudioKitList.contains(key)) {
                    Bundle bundle = new Bundle();
                    bundle.putString("packageNameForKitKillState", key);
                    bundle.putBoolean("isPackageOnResume", this.mPackageKillStateMap.get(key).booleanValue());
                    HwAudioHandlerEx hwAudioHandlerEx = this.mHwAudioHandlerEx;
                    hwAudioHandlerEx.sendMessage(hwAudioHandlerEx.obtainMessage(22, bundle));
                }
            }
            Bundle bundles = new Bundle();
            bundles.putStringArrayList(PACKAGENAME_LIST, this.mPackageNameList);
            HwAudioHandlerEx hwAudioHandlerEx2 = this.mHwAudioHandlerEx;
            hwAudioHandlerEx2.sendMessage(hwAudioHandlerEx2.obtainMessage(19, bundles));
            long token = Binder.clearCallingIdentity();
            try {
                this.mHwAudioHandlerEx.sendMessageAtTime(this.mHwAudioHandlerEx.obtainMessage(21), 200);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    private void startIntentForKaraoke(String packageName, boolean isAudioKit) {
        Throwable th;
        RemoteException e;
        Slog.v(TAG, "sendBroadcast : activity starting for karaoke");
        Intent startBroadcast = new Intent();
        startBroadcast.setAction(ACTION_START_APP_FOR_KARAOKE);
        startBroadcast.addFlags(268435456);
        startBroadcast.putExtra("packagename", packageName);
        startBroadcast.setPackage("com.huawei.android.karaoke");
        UserHandle.getAppId(getUidByPkg(packageName));
        if (isAudioKit) {
            startBroadcast.putExtra("sdkInitialize", true);
        }
        long ident = Binder.clearCallingIdentity();
        try {
            try {
                ActivityManagerNative.getDefault().broadcastIntent((IApplicationThread) null, startBroadcast, (String) null, (IIntentReceiver) null, -1, (String) null, (Bundle) null, new String[]{PERMISSION_SEND_AUDIO_RECORD_STATE}, -1, (Bundle) null, false, false, -2);
            } catch (RemoteException e2) {
                e = e2;
            }
        } catch (RemoteException e3) {
            e = e3;
            try {
                Slog.e(TAG, "Karaoke broadcast fail", e);
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th2) {
                th = th2;
            }
        } catch (Throwable th3) {
            th = th3;
            Binder.restoreCallingIdentity(ident);
            throw th;
        }
        Binder.restoreCallingIdentity(ident);
    }

    private void sendAppKilledIntentForKaraoke(boolean restore, String packageName, boolean isOnTop, String reserved) {
        if (HW_KARAOKE_EFFECT_ENABLED) {
            if (!this.mSystemReady) {
                Slog.e(TAG, "KILLED_APP system is not ready! ");
                return;
            }
            ArrayList<String> arrayList = this.mKaraokeWhiteList;
            if (arrayList == null || arrayList.size() < 1) {
                Slog.e(TAG, "Karaoke white list is empty! ");
                return;
            }
            int uid = UserHandle.getAppId(getUidByPkg(packageName));
            if (this.mKaraokeWhiteList.contains(packageName) || PACKAGE_PACKAGEINSTALLER.equals(packageName)) {
                if (AppActConstant.VALUE_TRUE.equals(AudioSystem.getParameters("queryKaraokeWhitePkg=" + uid))) {
                    Intent intent = new Intent();
                    intent.setAction(ACTION_KILLED_APP_FOR_KARAOKE);
                    intent.putExtra(RESTORE, restore);
                    intent.putExtra("packageName", packageName);
                    intent.putExtra(ISONTOP, isOnTop);
                    intent.putExtra(RESERVED, reserved);
                    this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT, PERMISSION_KILLED_APP_FOR_KARAOKE);
                }
            }
        }
    }

    private void sendAppKilledIntentForKit(boolean restore, String packageName, boolean isOnTop, String reserved) {
        if (HW_KARAOKE_EFFECT_ENABLED) {
            if (!this.mSystemReady) {
                Slog.e(TAG, "KILLED_APP system is not ready!");
                return;
            }
            boolean isKitPackageKill = true;
            boolean isKitPackageOnPause = !packageName.equals(this.mOldPackageName) && this.mAudioKitList.contains(this.mOldPackageName);
            boolean isKitPackageOnResume = !packageName.equals(this.mOldPackageName) && this.mAudioKitList.contains(packageName);
            if (!this.mAudioKitList.contains(packageName) || !restore) {
                isKitPackageKill = false;
            }
            Slog.i(TAG, "sendAppKilledIntentForKit, isKitPackageOnPause = " + isKitPackageOnPause + ", isKitPackageKill = " + isKitPackageKill + ", isKitPackageOnResume = " + isKitPackageOnResume);
            if (isKitPackageOnPause || isKitPackageKill || isKitPackageOnResume) {
                Intent intent = new Intent();
                intent.setAction(ACTION_KILLED_APP_FOR_KIT);
                intent.putExtra(RESTORE, restore);
                intent.putExtra("packageName", packageName);
                intent.putExtra(ISONTOP, isOnTop);
                intent.setPackage(ENGINE_PACKAGE_NAME);
                Slog.i(TAG, "sendAppKilledIntentForKit, isOnTop = " + isOnTop);
                this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT, PERMISSION_KILLED_APP_FOR_KIT);
            }
            this.mOldPackageName = packageName;
        }
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getAppTypeForVBR(int uid) {
        String[] packages = this.mContext.getPackageManager().getPackagesForUid(uid);
        if (packages == null) {
            return -1;
        }
        for (String packageName : packages) {
            try {
                return this.mPowerKit.getPkgType(this.mContext, packageName);
            } catch (RemoteException e) {
                Slog.i(TAG, "VBR getPkgType failed!");
            }
        }
        return -1;
    }

    private void getEffectsState(ContentResolver contentResolver) {
        if (SWS_SOUND_EFFECTS_SUPPORT) {
            getSwsStatus(contentResolver);
        }
    }

    private void setEffectsState() {
        if (SWS_SOUND_EFFECTS_SUPPORT) {
            if (HUAWEI_SWS31_CONFIG) {
                sendSwsEQBroadcast();
            } else {
                setSwsStatus();
            }
        }
        if (this.mDmsStarted && DOLBY_SOUND_EFFECTS_SUPPORT) {
            setDolbyStatus();
        }
    }

    private void getSwsStatus(ContentResolver contentResolver) {
        this.mSwsStatus = Settings.System.getInt(contentResolver, "sws_mode", 0);
    }

    private void setSwsStatus() {
        int i = this.mSwsStatus;
        if (i == 3) {
            AudioSystem.setParameters(SWS_MODE_ON_PARA);
        } else if (i == 0) {
            AudioSystem.setParameters(SWS_MODE_OFF_PARA);
        } else {
            Slog.v(TAG, "default mode");
        }
    }

    private void sendSwsEQBroadcast() {
        try {
            Intent intent = new Intent(ACTION_SWS_EQ);
            intent.setPackage("com.huawei.imedia.sws");
            this.mContext.sendBroadcastAsUser(intent, UserHandle.getUserHandleForUid(UserHandle.getCallingUserId()), PERMISSION_SWS_EQ);
        } catch (Exception e) {
            Slog.e(TAG, "sendSwsEQBroadcast exception");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setDolbyOrSwsStateClose() {
        String curSWSState;
        if (SWS_SOUND_EFFECTS_SUPPORT && HUAWEI_SWS31_CONFIG && (curSWSState = AudioSystem.getParameters(SWS_MODE_PARA)) != null && curSWSState.contains(SWS_MODE_ON_PARA)) {
            AudioSystem.setParameters(SWS_MODE_OFF_PARA);
            SystemProperties.set(SWS_MODE_PRESTATE, "on");
        }
        if (DOLBY_SOUND_EFFECTS_SUPPORT && this.mDolbyEnable > 0) {
            SystemProperties.set(DOLBY_MODE_PRESTATE, "on");
            setDolbyEnable(false);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setDolbyOrSwsStateOpen() {
        String preDolbyState;
        String preSWSState;
        if (SWS_SOUND_EFFECTS_SUPPORT && HUAWEI_SWS31_CONFIG && (preSWSState = SystemProperties.get(SWS_MODE_PRESTATE, ModelBaseService.UNKONW_IDENTIFY_RET)) != null && "on".equals(preSWSState)) {
            AudioSystem.setParameters(SWS_MODE_ON_PARA);
            SystemProperties.set(SWS_MODE_PRESTATE, ModelBaseService.UNKONW_IDENTIFY_RET);
        }
        if (DOLBY_SOUND_EFFECTS_SUPPORT && (preDolbyState = SystemProperties.get(DOLBY_MODE_PRESTATE, ModelBaseService.UNKONW_IDENTIFY_RET)) != null && "on".equals(preDolbyState)) {
            setDolbyEnable(true);
            SystemProperties.set(DOLBY_MODE_PRESTATE, ModelBaseService.UNKONW_IDENTIFY_RET);
        }
    }

    private void onSetSoundEffectAndHSState(String packageName, boolean isOnTOP) {
        int i = 0;
        while (true) {
            String[] strArr = FORBIDDEN_SOUND_EFFECT_WHITE_LIST;
            if (i >= strArr.length) {
                return;
            }
            if (strArr[i].equals(packageName)) {
                AudioSystem.setParameters(isOnTOP ? HS_NO_CHARGE_ON : HS_NO_CHARGE_OFF);
                this.mMyHandler.sendEmptyMessage(isOnTOP ? 1 : 2);
                return;
            }
            i++;
        }
    }

    private void restoreSoundEffectAndHSState(String processName) {
        String preSWSState = null;
        String preDOLBYState = null;
        if (SWS_SOUND_EFFECTS_SUPPORT && HUAWEI_SWS31_CONFIG) {
            preSWSState = SystemProperties.get(SWS_MODE_PRESTATE, ModelBaseService.UNKONW_IDENTIFY_RET);
        }
        if (DOLBY_SOUND_EFFECTS_SUPPORT) {
            preDOLBYState = SystemProperties.get(DOLBY_MODE_PRESTATE, ModelBaseService.UNKONW_IDENTIFY_RET);
        }
        int i = 0;
        while (true) {
            String[] strArr = FORBIDDEN_SOUND_EFFECT_WHITE_LIST;
            if (i >= strArr.length) {
                return;
            }
            if (strArr[i].equals(processName)) {
                AudioSystem.setParameters(HS_NO_CHARGE_OFF);
                if (SWS_SOUND_EFFECTS_SUPPORT && HUAWEI_SWS31_CONFIG && preSWSState != null && "on".equals(preSWSState)) {
                    AudioSystem.setParameters(SWS_MODE_ON_PARA);
                    SystemProperties.set(SWS_MODE_PRESTATE, ModelBaseService.UNKONW_IDENTIFY_RET);
                }
                if (DOLBY_SOUND_EFFECTS_SUPPORT && preDOLBYState != null && "on".equals(preDOLBYState)) {
                    setDolbyEnable(true);
                    SystemProperties.set(DOLBY_MODE_PRESTATE, ModelBaseService.UNKONW_IDENTIFY_RET);
                    return;
                }
                return;
            }
            i++;
        }
    }

    public void onSetSoundEffectState(int device, int state) {
        Slog.i(TAG, "onSetSoundEffectState : device : " + device + " state : " + state + " swsHashSet : " + this.swsHashSet);
        if (state == 1) {
            if (this.swsHashSet.isEmpty()) {
                sendMsgEx(this.mHwAudioHandlerEx, 20, 0, device, state, null, 0);
            }
            this.swsHashSet.add(Integer.valueOf(device));
        } else {
            this.swsHashSet.remove(Integer.valueOf(device));
            if (this.virtualDevices.isEmpty() && this.connectedA2dpDevices.isEmpty() && this.swsHashSet.isEmpty() && this.mSwsConnect) {
                sendMsgEx(this.mHwAudioHandlerEx, 20, 0, 0, 0, null, 0);
            }
        }
        sendMsgEx(this.mHwAudioHandlerEx, 17, 2, device, state, null, 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkAndSetSoundEffectState(int device, int state) {
        if (this.mIAsInner.checkAudioSettingsPermissionEx("onSetSoundEffectState()") && SOUND_EFFECTS_SUPPORT && (EFFECT_SUPPORT_DEVICE & device) != 0) {
            if (state == 1) {
                this.mSupportDevcieRef++;
                if (this.mSupportDevcieRef > 1) {
                    return;
                }
            } else {
                this.mSupportDevcieRef--;
                if (this.mSupportDevcieRef > 0) {
                    return;
                }
            }
            if (DOLBY_SOUND_EFFECTS_SUPPORT) {
                resetDolbyStateForHeadset(state);
            }
            if (!isScreenOff() && !isKeyguardLocked() && isTopActivity(FORBIDDEN_SOUND_EFFECT_WHITE_LIST)) {
                if (SWS_SOUND_EFFECTS_SUPPORT && HUAWEI_SWS31_CONFIG) {
                    onSetSwsstate(state);
                }
                if (DOLBY_SOUND_EFFECTS_SUPPORT) {
                    onSetDolbyState(state);
                }
            }
        }
    }

    private boolean isTopActivity(String[] appNames) {
        String topPackageName = getTopActivityPackageName();
        if (topPackageName == null) {
            return false;
        }
        for (String str : appNames) {
            if (str.equals(topPackageName)) {
                return true;
            }
        }
        return false;
    }

    private void onSetSwsstate(int state) {
        String preSWSState = SystemProperties.get(SWS_MODE_PRESTATE, ModelBaseService.UNKONW_IDENTIFY_RET);
        if (state == 1) {
            String curSWSState = AudioSystem.getParameters(SWS_MODE_PARA);
            if (curSWSState != null && curSWSState.contains(SWS_MODE_ON_PARA)) {
                AudioSystem.setParameters(SWS_MODE_OFF_PARA);
                SystemProperties.set(SWS_MODE_PRESTATE, "on");
            }
        } else if (state != 0 || preSWSState == null || !"on".equals(preSWSState)) {
            Slog.v(TAG, "default mode");
        } else {
            AudioSystem.setParameters(SWS_MODE_ON_PARA);
            SystemProperties.set(SWS_MODE_PRESTATE, ModelBaseService.UNKONW_IDENTIFY_RET);
        }
    }

    /* access modifiers changed from: private */
    public static int byteArrayToInt32(byte[] ba, int index) {
        return ((ba[index + 3] & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY) << 24) | ((ba[index + 2] & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY) << 16) | ((ba[index + 1] & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY) << 8) | (ba[index] & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendDolbyUpdateBroadcast(int dlbParam, int status) {
        String str;
        Intent intent = new Intent(DOLBY_UPDATE_EVENT);
        if (dlbParam == 0) {
            str = DOLBY_UPDATE_EVENT_DS_STATE;
        } else {
            str = DOLBY_UPDATE_EVENT_PROFILE;
        }
        intent.putExtra(EVENTNAME, str);
        intent.putExtra(CMDINTVALUE, status);
        this.mContext.sendBroadcastAsUser(intent, new UserHandle(ActivityManager.getCurrentUser()), "com.huawei.permission.DOLBYCONTROL");
    }

    private void resetDolbyStateForHeadset(int headsetState) {
        this.mHeadSetPlugState = headsetState == 1;
        String str = "on";
        if (headsetState != 1) {
            if (this.mDolbyEnable <= 0) {
                str = "off";
            }
            SystemProperties.set(DOLBY_MODE_HEADSET_STATE, str);
            setDolbyEnable(true);
        } else if (!this.mDmsStarted) {
            setDolbyStatus();
        } else if ("off".equals(SystemProperties.get(DOLBY_MODE_HEADSET_STATE, str))) {
            setDolbyEnable(false);
        }
    }

    private void onSetDolbyState(int state) {
        String preDolbyState = SystemProperties.get(DOLBY_MODE_PRESTATE, ModelBaseService.UNKONW_IDENTIFY_RET);
        if (state == 1) {
            if (this.mDolbyEnable == 1) {
                SystemProperties.set(DOLBY_MODE_PRESTATE, "on");
                setDolbyEnable(false);
            }
        } else if (state != 0 || preDolbyState == null || !"on".equals(preDolbyState)) {
            Slog.v(TAG, "state = " + state);
        } else {
            SystemProperties.set(DOLBY_MODE_HEADSET_STATE, preDolbyState);
            SystemProperties.set(DOLBY_MODE_PRESTATE, ModelBaseService.UNKONW_IDENTIFY_RET);
        }
    }

    private void setDolbyEnable(boolean state) {
        if (this.mDms != null) {
            ArrayList<Byte> params = new ArrayList<>();
            if (!state) {
                for (int i = 0; i < 12; i++) {
                    try {
                        params.add(Byte.valueOf(DS_DISABLE_ARRAY[i]));
                    } catch (Exception e) {
                        Slog.e(TAG, "setDolbyEnable exception");
                        return;
                    }
                }
                this.mDms.setDapParam(params);
                AudioSystem.setParameters(DOLBY_MODE_OFF_PARA);
                this.mDolbyEnable = 0;
                if (this.mEffect == null || !this.mEffect.hasControl()) {
                    AudioEffect dolbyEffect = new AudioEffect(EFFECT_TYPE_NULL, EFFECT_TYPE_DS, 1, 0);
                    dolbyEffect.setEnabled(false);
                    dolbyEffect.release();
                    return;
                }
                this.mEffect.setEnabled(false);
                return;
            }
            for (int i2 = 0; i2 < 12; i2++) {
                params.add(Byte.valueOf(DS_ENABLE_ARRAY[i2]));
            }
            this.mDms.setDapParam(params);
            AudioSystem.setParameters(DOLBY_MODE_ON_PARA);
            this.mDolbyEnable = 1;
            if (this.mEffect == null || !this.mEffect.hasControl()) {
                AudioEffect dolbyEffect2 = new AudioEffect(EFFECT_TYPE_NULL, EFFECT_TYPE_DS, 1, 0);
                dolbyEffect2.setEnabled(true);
                dolbyEffect2.release();
                return;
            }
            this.mEffect.setEnabled(true);
        }
    }

    private static int byteArrayToInt(Byte[] ba, int index) {
        return ((ba[index + 3].byteValue() & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY) << 24) | ((ba[index + 2].byteValue() & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY) << 16) | ((ba[index + 1].byteValue() & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY) << 8) | (ba[index].byteValue() & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY);
    }

    private static int int32ToByteArray(int src, Byte[] dst, int index) {
        int index2 = index + 1;
        dst[index] = Byte.valueOf((byte) (src & 255));
        int index3 = index2 + 1;
        dst[index2] = Byte.valueOf((byte) ((src >>> 8) & 255));
        dst[index3] = Byte.valueOf((byte) ((src >>> 16) & 255));
        dst[index3 + 1] = Byte.valueOf((byte) ((src >>> 24) & 255));
        return 4;
    }

    public boolean setDolbyEffect(int mode) {
        Slog.i(TAG, "setDolbyEffect1 mode = " + mode);
        if (this.mDms == null) {
            setDolbyStatus();
        }
        if (this.mDms == null) {
            return false;
        }
        ArrayList<Byte> params = new ArrayList<>();
        if (mode == 0) {
            AudioSystem.setParameters("dolbyMultich=off");
            AudioSystem.setParameters("dolby_game_mode=off");
            for (int i = 0; i < 12; i++) {
                params.add(Byte.valueOf(DS_SMART_MODE_ARRAY[i]));
            }
            this.mDms.setDapParam(params);
        } else if (mode == 1) {
            AudioSystem.setParameters("dolbyMultich=off");
            AudioSystem.setParameters("dolby_game_mode=off");
            for (int i2 = 0; i2 < 12; i2++) {
                params.add(Byte.valueOf(DS_MOVIE_MODE_ARRAY[i2]));
            }
            this.mDms.setDapParam(params);
        } else if (mode == 2) {
            AudioSystem.setParameters("dolbyMultich=off");
            AudioSystem.setParameters("dolby_game_mode=off");
            for (int i3 = 0; i3 < 12; i3++) {
                params.add(Byte.valueOf(DS_MUSIC_MODE_ARRAY[i3]));
            }
            this.mDms.setDapParam(params);
        } else if (mode == 3) {
            try {
                if (!setDolbyEffectGameMode(params)) {
                    return false;
                }
            } catch (Exception e) {
                Slog.e(TAG, "setDolbyEffect exception");
            }
        }
        return true;
    }

    private boolean setDolbyEffectGameMode(ArrayList<Byte> params) {
        final Byte[] byteArray = new Byte[12];
        for (int i = 0; i < 12; i++) {
            byteArray[i] = (byte) 0;
        }
        int index = 0 + int32ToByteArray(EFFECT_PARAM_EFF_PROF, byteArray, 0);
        try {
            this.mDms.getDapParam(new ArrayList<>(Arrays.asList(byteArray)), new IDms.getDapParamCallback() {
                /* class com.android.server.audio.HwAudioServiceEx.AnonymousClass13 */

                @Override // vendor.huawei.hardware.dolby.dms.V1_0.IDms.getDapParamCallback
                public void onValues(int retval, ArrayList<Byte> outVal) {
                    int index = 0;
                    Iterator<Byte> it = outVal.iterator();
                    while (it.hasNext()) {
                        byteArray[index] = it.next();
                        index++;
                    }
                }
            });
            if (byteArrayToInt(byteArray, 0) != 0) {
                return false;
            }
            AudioSystem.setParameters("dolbyMultich=on");
            for (int i2 = 0; i2 < 12; i2++) {
                params.add(Byte.valueOf(DS_GAME_MODE_ARRAY[i2]));
            }
            this.mDms.setDapParam(params);
            AudioSystem.setParameters("dolby_game_mode=on");
            return true;
        } catch (RemoteException e) {
            Slog.e(TAG, "Failed to receive byte array to DMS.");
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setDolbyStatus() {
        if (DOLBY_SOUND_EFFECTS_SUPPORT) {
            Slog.d(TAG, "Init Dolby effect on system ready");
            try {
                this.mEffect = new AudioEffect(EFFECT_TYPE_NULL, EFFECT_TYPE_DS, -1, 0);
                this.mEffect.setEnabled(true);
                setDolbyServiceClient();
                if ("unknow".equals(SystemProperties.get(DOLBY_MODE_HEADSET_STATE, "unknow"))) {
                    SystemProperties.set(DOLBY_MODE_HEADSET_STATE, "on");
                }
            } catch (RuntimeException e) {
                Slog.e(TAG, "create dolby effect failed, fatal problem!!!");
                this.mEffect = null;
                this.mDmsStarted = false;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setDolbyServiceClient() {
        try {
            if (this.mDms != null) {
                this.mDms.unlinkToDeath(this.mDmsDeathRecipient);
            }
            this.mDms = IDms.getService();
            if (this.mDms != null) {
                AudioSystem.setParameters(DOLBY_MODE_ON_PARA);
                String headSetDolbyState = SystemProperties.get(DOLBY_MODE_HEADSET_STATE, "on");
                if (!this.mHeadSetPlugState || !"off".equals(headSetDolbyState)) {
                    setDolbyEnable(true);
                } else {
                    setDolbyEnable(false);
                }
                this.mDms.linkToDeath(this.mDmsDeathRecipient, (long) this.mDolbyClient.hashCode());
                this.mDms.registerClient(this.mDolbyClient, 3, this.mDolbyClient.hashCode());
                this.mDmsStarted = true;
                return;
            }
            Slog.e(TAG, "Dolby service is not ready, try to reconnect 1s later.");
            sendMsgEx(this.mHwAudioHandlerEx, 11, 0, 0, 0, null, 1000);
        } catch (Exception e) {
            Slog.e(TAG, "Connect Dolby service caught exception, try to reconnect 1s later.");
            sendMsgEx(this.mHwAudioHandlerEx, 11, 0, 0, 0, null, 1000);
        }
    }

    public void hideHiResIconDueKilledAPP(boolean killed, String packageName) {
        if (killed && packageName != null) {
            this.mHwAudioHandlerEx.sendEmptyMessageDelayed(10, 500);
        }
    }

    public int setSoundEffectState(boolean restore, String packageName, boolean isOnTop, String reserved) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(RESTORE, restore);
        bundle.putString("packageName", packageName);
        bundle.putBoolean(ISONTOP, isOnTop);
        bundle.putString(RESERVED, reserved);
        HwAudioHandlerEx hwAudioHandlerEx = this.mHwAudioHandlerEx;
        hwAudioHandlerEx.sendMessage(hwAudioHandlerEx.obtainMessage(13, bundle));
        return 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private IAudioService getAudioService() {
        IAudioService iAudioService = this.mAudioService;
        if (iAudioService != null) {
            return iAudioService;
        }
        this.mAudioService = IAudioService.Stub.asInterface(ServiceManager.getService("audio"));
        return this.mAudioService;
    }

    public int startVirtualAudio(String deviceId, String serviceId, int serviceType, Map<String, Object> dataMap) {
        int type;
        Slog.i(TAG, "enter method startVirtualAudio");
        if (deviceId == null || serviceId == null || dataMap == null || deviceId.length() > 128 || serviceId.length() > 64) {
            Slog.e(TAG, "param of startVirtualAudio is illegal");
            return -1;
        }
        IAudioService audioService = getAudioService();
        if (audioService == null) {
            Slog.e(TAG, "audioService is null");
            return -1;
        }
        if (serviceType == 3) {
            type = 33554432;
        } else if (serviceType == 2) {
            type = -2130706432;
        } else {
            Slog.e(TAG, "type is illegal");
            return -1;
        }
        String deviceType = serviceId;
        String deviceName = MSDP_DEVICE_NAME;
        for (Map.Entry<String, Object> tempEntry : dataMap.entrySet()) {
            String tempKey = String.valueOf(tempEntry.getKey());
            if (tempKey.equals(REMOTE_DEVICE_NAME)) {
                deviceName = String.valueOf(tempEntry.getValue());
            }
            if (tempKey.equals(REMOTE_DEVICE_TYPE)) {
                deviceType = String.valueOf(tempEntry.getValue());
            }
        }
        try {
            audioService.setWiredDeviceConnectionState(type, 1, deviceType, deviceName, TAG);
            return 0;
        } catch (RemoteException e) {
            Slog.e(TAG, "audioService is unavailable ");
            return -1;
        }
    }

    public boolean isVirtualAudio(int newDevice) {
        Slog.i(TAG, "isVirtualAudio,newDevice is:" + newDevice);
        return newDevice == 33554432;
    }

    public int removeVirtualAudio(String deviceId, String serviceId, int serviceType, Map<String, Object> dataMap) {
        int type;
        Slog.i(TAG, "enter method removeVirtualAudio");
        if (deviceId == null || serviceId == null || dataMap == null || deviceId.length() > 128 || serviceId.length() > 64) {
            Slog.e(TAG, "param of removeVirtualAudio is illegal");
            return -1;
        }
        IAudioService audioService = getAudioService();
        if (audioService == null) {
            Slog.e(TAG, "audioService is null");
            return -1;
        }
        if (serviceType == 3) {
            type = 33554432;
        } else if (serviceType == 2) {
            type = -2130706432;
        } else {
            Slog.e(TAG, "type is illegal");
            return -1;
        }
        String deviceType = serviceId;
        String deviceName = MSDP_DEVICE_NAME;
        for (Map.Entry<String, Object> tempEntry : dataMap.entrySet()) {
            String tempKey = String.valueOf(tempEntry.getKey());
            if (tempKey.equals(REMOTE_DEVICE_NAME)) {
                deviceName = String.valueOf(tempEntry.getValue());
            }
            if (tempKey.equals(REMOTE_DEVICE_TYPE)) {
                deviceType = String.valueOf(tempEntry.getValue());
            }
        }
        try {
            audioService.setWiredDeviceConnectionState(type, 0, deviceType, deviceName, TAG);
            return 0;
        } catch (RemoteException e) {
            Slog.e(TAG, "audioService is unavailable ");
            return -1;
        }
    }

    private int setSoundEffectStateAsynch(boolean restore, String packageName, boolean isOnTop, String reserved) {
        sendStartIntentForKaraoke(packageName, isOnTop);
        sendAppKilledIntentForKaraoke(restore, packageName, isOnTop, reserved);
        sendAppKilledIntentForKit(restore, packageName, isOnTop, reserved);
        if (!SOUND_EFFECTS_SUPPORT || !this.mIAsInner.checkAudioSettingsPermissionEx("setSoundEffectState()") || isScreenOff() || isKeyguardLocked()) {
            return -1;
        }
        if (!restore) {
            onSetSoundEffectAndHSState(packageName, isOnTop);
            return 0;
        }
        restoreSoundEffectAndHSState(packageName);
        return 0;
    }

    private boolean getRecordConcurrentTypeInternal(String pkgName) {
        if (pkgName == null) {
            return false;
        }
        for (String name : CONCURRENT_RECORD_OTHER) {
            if (name.equals(pkgName)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkConcurRecList(String recordApk, int apkUid) {
        boolean authenticate = false;
        try {
            PackageInfo packageInfo = this.mContext.getPackageManager().getPackageInfoAsUser(recordApk, 134217728, UserHandle.getUserId(apkUid));
            Signature[] signs = null;
            if (!(packageInfo == null || packageInfo.signingInfo == null)) {
                signs = packageInfo.signingInfo.getSigningCertificateHistory();
            }
            byte[] verifySignByte = new ConcurrentRecSignatures().getSignByPkg(recordApk);
            byte[] recordApkSignByte = null;
            if (signs != null && signs.length > 0) {
                recordApkSignByte = signs[0].toByteArray();
            }
            if (verifySignByte != null && recordApkSignByte != null && verifySignByte.length == recordApkSignByte.length && Arrays.equals(verifySignByte, recordApkSignByte)) {
                authenticate = true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Slog.e(TAG, "can't resolve concurrent whitelist for recordApk");
        }
        if (authenticate) {
            this.mPackageUidMap.put(recordApk, Integer.valueOf(apkUid));
            Slog.i(TAG, "add recordApk into conCurrent record list");
            return true;
        }
        Slog.v(TAG, "can't trust recordApk in record white list");
        return false;
    }

    public int getRecordConcurrentType(String pkgName) {
        if (!this.mConcurrentCaptureEnable || !getRecordConcurrentTypeInternal(pkgName)) {
            return 0;
        }
        int uid = Binder.getCallingUid();
        Binder.getCallingPid();
        boolean allow = true;
        if (!this.mPackageUidMap.containsKey(pkgName) || this.mPackageUidMap.get(pkgName).intValue() != uid) {
            allow = checkConcurRecList(pkgName, uid);
        }
        if (allow) {
            return RECORD_TYPE_OTHER;
        }
        return 0;
    }

    /* access modifiers changed from: private */
    public static final class AudioModeClient implements IBinder.DeathRecipient {
        static HwAudioServiceEx sHwAudioService;
        final IAudioModeDispatcher mDispatcherCb;

        AudioModeClient(IAudioModeDispatcher pcdb) {
            this.mDispatcherCb = pcdb;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            Log.w(HwAudioServiceEx.TAG, "client died");
            sHwAudioService.unregisterAudioModeCallback(this.mDispatcherCb);
        }

        /* access modifiers changed from: package-private */
        public boolean init() {
            Log.w(HwAudioServiceEx.TAG, "client init");
            try {
                this.mDispatcherCb.asBinder().linkToDeath(this, 0);
                return true;
            } catch (RemoteException e) {
                Log.w(HwAudioServiceEx.TAG, "Could not link to client death");
                return false;
            }
        }

        /* access modifiers changed from: package-private */
        public void release() {
            Log.w(HwAudioServiceEx.TAG, "client release");
            this.mDispatcherCb.asBinder().unlinkToDeath(this, 0);
        }
    }

    public void registerAudioModeCallback(IAudioModeDispatcher pcdb) {
        Log.i(TAG, "registerAudioModeCallback. ");
        if (pcdb != null) {
            synchronized (this.mClients) {
                AudioModeClient pmc = new AudioModeClient(pcdb);
                if (pmc.init()) {
                    this.mClients.add(pmc);
                }
            }
        }
    }

    public void unregisterAudioModeCallback(IAudioModeDispatcher pcdb) {
        Log.i(TAG, "unregisterAudioModeCallback. ");
        if (pcdb != null) {
            synchronized (this.mClients) {
                Iterator<AudioModeClient> clientIterator = this.mClients.iterator();
                while (clientIterator.hasNext()) {
                    AudioModeClient pmc = clientIterator.next();
                    if (!(pmc == null || pmc.mDispatcherCb == null || !pcdb.equals(pmc.mDispatcherCb))) {
                        pmc.release();
                        clientIterator.remove();
                    }
                }
            }
        }
    }

    public void dipatchAudioModeChanged(int actualMode) {
        Log.i(TAG, "dipatchAudioModeChanged mode = " + actualMode);
        this.mAudioMode = actualMode;
        synchronized (this.mClients) {
            if (!this.mClients.isEmpty()) {
                Iterator<AudioModeClient> clientIterator = this.mClients.iterator();
                while (clientIterator.hasNext()) {
                    AudioModeClient pmc = clientIterator.next();
                    if (pmc != null) {
                        try {
                            if (pmc.mDispatcherCb != null) {
                                pmc.mDispatcherCb.dispatchAudioModeChange(actualMode);
                            }
                        } catch (RemoteException e) {
                            Log.i(TAG, "failed to dispatch audio mode changes");
                        }
                    }
                }
            }
        }
    }

    public IBinder getDeviceSelectCallback() {
        IDeviceSelectCallback iDeviceSelectCallback = this.mcb;
        if (iDeviceSelectCallback == null) {
            return null;
        }
        return iDeviceSelectCallback.asBinder();
    }

    public boolean registerAudioDeviceSelectCallback(IBinder cb) {
        IDeviceSelectCallback iCb = IDeviceSelectCallback.Stub.asInterface(cb);
        if (iCb == null) {
            return false;
        }
        this.mcb = iCb;
        return true;
    }

    public boolean unregisterAudioDeviceSelectCallback(IBinder cb) {
        if (this.mcb != IDeviceSelectCallback.Stub.asInterface(cb)) {
            return false;
        }
        this.mcb = null;
        return true;
    }

    public int getHwSafeUsbMediaVolumeIndex() {
        return USB_SECURITY_VOLUME_INDEX * 10;
    }

    public boolean isHwSafeUsbMediaVolumeEnabled() {
        return USB_SECURITY_VOLUME_INDEX > 0;
    }

    private void setTypecParamAsync(int state, int type, String name) {
        if ((604004352 & type) != 0) {
            sendMsgEx(this.mHwAudioHandlerEx, 14, 2, state, 0, name, 0);
        }
    }

    private void restoreTypecParam(String name) {
        if (name == null || name.length() <= 0) {
            Slog.e(TAG, "name is null or empty");
        } else {
            AudioSystem.setParameters("SWS_TYPEC_RESTORE=;");
        }
    }

    private void setTypecParamToAudioSystem(String name) {
        if (name == null || name.length() <= 0) {
            Slog.e(TAG, "name is null or empty");
            return;
        }
        UsbManager usbMgr = null;
        if (this.mContext.getSystemService("usb") instanceof UsbManager) {
            usbMgr = (UsbManager) this.mContext.getSystemService("usb");
        }
        if (usbMgr == null) {
            Slog.e(TAG, "get usb service failed");
            return;
        }
        HashMap<String, UsbDevice> deviceMap = usbMgr.getDeviceList();
        if (deviceMap == null || deviceMap.size() <= 0) {
            Slog.e(TAG, "usb list is empty");
            return;
        }
        for (Map.Entry<String, UsbDevice> entry : deviceMap.entrySet()) {
            UsbDevice device = entry.getValue();
            if (device != null && setUsbDeviceParamToAudioSystem(name, device)) {
                return;
            }
        }
    }

    private boolean setUsbDeviceParamToAudioSystem(String name, UsbDevice device) {
        String product = device.getProductName();
        String manufacture = device.getManufacturerName();
        if (product == null || name.indexOf(product) < 0 || manufacture == null) {
            return false;
        }
        AudioSystem.setParameters(("SWS_TYPEC_MANUFACTURER=" + manufacture + AwarenessInnerConstants.SEMI_COLON_KEY) + SWS_TYPEC_PRODUCT_NAME + "=" + product + AwarenessInnerConstants.SEMI_COLON_KEY);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initPowerKit() {
        if (this.mPowerKit == null) {
            this.mPowerKit = PowerKit.getInstance();
            if (this.mPowerKit != null) {
                Slog.i(TAG, "powerkit getInstance ok!");
                try {
                    this.mPowerKit.enableStateEvent(this.mStateRecognitionListener, 1);
                    this.mPowerKit.enableStateEvent(this.mStateRecognitionListener, 2);
                    if ((SWS_SOUND_EFFECTS_SUPPORT && SWS_VIDEO_MODE) || DOLBY_CHANNEL_COUNT == 4) {
                        this.mPowerKit.enableStateEvent(this.mStateRecognitionListener, (int) IDisplayEngineService.DE_ACTION_PG_VIDEO_FRONT);
                    }
                } catch (RemoteException | SecurityException e) {
                    Slog.w(TAG, "PG Exception: initialize powerkit error!");
                }
            } else {
                Slog.w(TAG, "powerkit getInstance fails!");
            }
        }
    }

    public boolean checkMuteZenMode() {
        String pkgName;
        String[] strArr;
        NotificationManager notificationManager = this.mNm;
        if (!(notificationManager == null || notificationManager.getZenMode() == 0 || (pkgName = getPackageNameByPid(Binder.getCallingPid())) == null || (strArr = this.mZenModeWhiteList) == null)) {
            for (String temp : strArr) {
                if (pkgName.equals(temp)) {
                    Slog.i(TAG, "need mute for apk under zen mode");
                    return true;
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isMicSource(int source) {
        if (source == 0 || source == 1 || source == 5 || source == 6 || source == 7) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isPrivacySensitiveSource(int source) {
        if (source == 5 || source == 7) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendMicSilencedToastMesg(String silencedPkgName, String activePkgName) {
        if (!shouldIgnoreSilenceToast(silencedPkgName)) {
            Message msg = Message.obtain();
            Bundle bundle = new Bundle();
            bundle.putString(SILENCE_PKG_STR, silencedPkgName);
            bundle.putString(ACTIVE_PKG_STR, activePkgName);
            msg.obj = bundle;
            msg.what = 18;
            this.mHwAudioHandlerEx.sendMessage(msg);
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x002b: APUT  (r5v10 java.lang.Object[]), (0 ??[int, short, byte, char]), (r6v6 java.lang.String) */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x004c: APUT  (r5v8 java.lang.Object[]), (0 ??[int, short, byte, char]), (r6v3 java.lang.String) */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0069: APUT  (r2v4 java.lang.Object[]), (0 ??[int, short, byte, char]), (r7v1 java.lang.String) */
    private void showMicSilencedToast(String silencedPkgName, String activePkgName) {
        String activePkgLabel;
        if (silencedPkgName != null && activePkgName != null) {
            String silencedPkgLabel = getApplicationLabel(silencedPkgName);
            if (activePkgName.equals(PHONE_PKG) && this.mAudioMode == 2) {
                Locale locale = Locale.ROOT;
                String string = this.mContext.getString(33686213);
                Object[] objArr = new Object[1];
                objArr[0] = silencedPkgLabel != null ? silencedPkgLabel : silencedPkgName;
                activePkgLabel = String.format(locale, string, objArr);
            } else if (activePkgName.equals(SHARED_UID_STR)) {
                Locale locale2 = Locale.ROOT;
                String string2 = this.mContext.getString(33686214);
                Object[] objArr2 = new Object[1];
                objArr2[0] = silencedPkgLabel != null ? silencedPkgLabel : silencedPkgName;
                activePkgLabel = String.format(locale2, string2, objArr2);
            } else {
                String activePkgLabel2 = getApplicationLabel(activePkgName);
                Locale locale3 = Locale.ROOT;
                String string3 = this.mContext.getString(33685818);
                Object[] objArr3 = new Object[2];
                objArr3[0] = activePkgLabel2 != null ? activePkgLabel2 : activePkgName;
                objArr3[1] = silencedPkgLabel != null ? silencedPkgLabel : silencedPkgName;
                activePkgLabel = String.format(locale3, string3, objArr3);
            }
            ContextThemeWrapper themeContext = new ContextThemeWrapper(this.mContext, this.mContext.getResources().getIdentifier(TOAST_THEME, null, null));
            Toast toast = this.mToast;
            if (toast == null) {
                this.mToast = Toast.makeText(themeContext, activePkgLabel, 1);
                this.mToast.getWindowParams().privateFlags |= 16;
            } else {
                toast.setText(activePkgLabel);
            }
            try {
                this.mToast.show();
            } catch (IllegalStateException e) {
                Log.e(TAG, "showMicSilencedToast fail");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void findLastSilenceRecord(List<AudioRecordingConfiguration> configs) {
        AudioRecordingConfiguration silencedRecord = null;
        boolean hasSilencdRecord = false;
        Iterator<AudioRecordingConfiguration> it = configs.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            AudioRecordingConfiguration arc = it.next();
            boolean found = false;
            if (arc.isClientSilenced() && isMicSource(arc.getClientAudioSource())) {
                hasSilencdRecord = true;
                Iterator<AudioRecordingConfiguration> it2 = this.mLastRecordingConfigs.iterator();
                while (true) {
                    if (!it2.hasNext()) {
                        break;
                    }
                    AudioRecordingConfiguration oldArc = it2.next();
                    if (oldArc.getClientAudioSessionId() == arc.getClientAudioSessionId()) {
                        found = true;
                        if (!oldArc.isClientSilenced()) {
                            silencedRecord = arc;
                            break;
                        }
                    }
                }
                if (silencedRecord != null) {
                    break;
                } else if (!found) {
                    silencedRecord = arc;
                    break;
                }
            } else {
                AudioRecordingConfiguration audioRecordingConfiguration = this.mLastSilenceRec;
                if (audioRecordingConfiguration != null && audioRecordingConfiguration.getClientAudioSessionId() == arc.getClientAudioSessionId()) {
                    this.mLastSilenceRec = null;
                }
            }
        }
        if (silencedRecord != null) {
            this.mLastSilenceRec = silencedRecord;
        }
        if (!hasSilencdRecord) {
            this.mLastSilenceRec = null;
        }
    }

    private boolean shouldIgnoreSilenceToast(String silencePkgName) {
        if (!TextUtils.isEmpty(silencePkgName) && !silencePkgName.contains(RECORD_REQUEST_APP_LIST[0])) {
            return false;
        }
        return true;
    }

    public boolean checkRecordActive(int requestRecordPid) {
        return false;
    }

    /* JADX INFO: finally extract failed */
    public boolean bypassVolumeProcessForTV(int curActiveDevice, int streamType, int direction, int flags) {
        Intent volumeIntent = new Intent();
        volumeIntent.setAction(ACTION_SEND_TV);
        long ident = Binder.clearCallingIdentity();
        try {
            this.mContext.sendBroadcastAsUser(volumeIntent, UserHandle.ALL, PERMISSION_SEND_TV);
            Binder.restoreCallingIdentity(ident);
            if (curActiveDevice != 524288 && curActiveDevice != 262144) {
                return false;
            }
            Intent intent = new Intent();
            intent.setAction(ACTION_ADJUST_STREAM_VOLUME);
            intent.putExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", streamType);
            intent.putExtra(EXTRA_VOLUME_STREAM_DIRECTION, direction);
            intent.putExtra(EXTRA_VOLUME_STREAM_FLAGS, flags);
            intent.putExtra("android.media.EXTRA_VOLUME_STREAM_DEVICES", curActiveDevice);
            long ident1 = Binder.clearCallingIdentity();
            try {
                this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, PERMISSION_ADJUST_STREAM_VOLUME);
                Binder.restoreCallingIdentity(ident1);
                Slog.d(TAG, "bypassVolumeProcessForTV sendBroadcast adjustvolume streamType = " + streamType + " direction = " + direction);
                return true;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident1);
                throw th;
            }
        } catch (Throwable th2) {
            Binder.restoreCallingIdentity(ident);
            throw th2;
        }
    }

    public boolean setFmDeviceAvailable(int state, boolean isNeedToCheckPermission) {
        if (!isNeedToCheckPermission || this.mContext.checkCallingOrSelfPermission("com.huawei.permission.ACCESS_FM") == 0) {
            Log.i(TAG, "setFmDeviceAvailable state = " + state + ", mFmDeviceOn = " + this.mFmDeviceOn);
            if (state == 0 && this.mFmDeviceOn) {
                AudioSystem.setDeviceConnectionState(1048576, 0, "", "", 0);
                this.mFmDeviceOn = false;
            } else if (state != 1 || this.mFmDeviceOn) {
                Log.w(TAG, "failed to set FM device!");
                return false;
            } else {
                AudioSystem.setDeviceConnectionState(1048576, 1, "", "", 0);
                this.mFmDeviceOn = true;
            }
            return true;
        }
        Log.w(TAG, "not allowed to set FM device available.");
        return false;
    }

    public void setBluetoothScoState(int state, int sessionId) {
        this.mScoRecordService.setBluetoothScoState(state, sessionId);
    }

    public void setBtScoForRecord(boolean on) {
        this.mScoRecordService.setBtScoForRecord(on);
    }

    /* access modifiers changed from: private */
    public static final class VolumeChangeClient implements IBinder.DeathRecipient {
        static HwAudioServiceEx sHwAudioService;
        final String mCallback;
        final IVolumeChangeDispatcher mDispatcherCb;
        final String mPkgName;

        VolumeChangeClient(IVolumeChangeDispatcher dispatcher, String callback, String pkgName) {
            this.mDispatcherCb = dispatcher;
            this.mCallback = callback;
            this.mPkgName = pkgName;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            Log.i(HwAudioServiceEx.TAG, "VolumeChangeClient died");
            sHwAudioService.unregisterVolumeChangeCallback(this.mDispatcherCb, this.mCallback, this.mPkgName);
        }

        /* access modifiers changed from: package-private */
        public boolean init() {
            try {
                this.mDispatcherCb.asBinder().linkToDeath(this, 0);
                return true;
            } catch (RemoteException e) {
                Log.w(HwAudioServiceEx.TAG, "Could not link to client death");
                return false;
            }
        }

        /* access modifiers changed from: package-private */
        public void release() {
            this.mDispatcherCb.asBinder().unlinkToDeath(this, 0);
        }
    }

    public boolean registerVolumeChangeCallback(IVolumeChangeDispatcher dispatcher, String callback, String pkgName) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_AUDIO_ROUTING") != 0) {
            Log.e(TAG, "Permission Denial to registerVolumeChangeCallback.");
            return false;
        } else if (dispatcher == null) {
            return false;
        } else {
            synchronized (this.mVolumeClients) {
                VolumeChangeClient client = new VolumeChangeClient(dispatcher, callback, pkgName);
                if (client.init()) {
                    this.mVolumeClients.add(client);
                    Log.i(TAG, "registerVolumeChangeCallback successfully.");
                    return true;
                }
                Log.w(TAG, "registerVolumeChangeCallback false");
                return false;
            }
        }
    }

    public boolean unregisterVolumeChangeCallback(IVolumeChangeDispatcher dispatcher, String callback, String pkgName) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_AUDIO_ROUTING") != 0) {
            Log.e(TAG, "Permission Denial to unregisterVolumeChangeCallback.");
            return false;
        } else if (dispatcher == null) {
            return false;
        } else {
            synchronized (this.mVolumeClients) {
                Iterator<VolumeChangeClient> clientIterator = this.mVolumeClients.iterator();
                while (clientIterator.hasNext()) {
                    VolumeChangeClient client = clientIterator.next();
                    if (client != null && client.mDispatcherCb != null && callback.equals(client.mCallback)) {
                        client.release();
                        clientIterator.remove();
                        Log.i(TAG, "unregisterVolumeChangeCallback successfully.");
                        return true;
                    }
                }
                Log.w(TAG, "unregisterVolumeChangeCallback false");
                return false;
            }
        }
    }

    public void dispatchVolumeChange(int device, int stream, String caller, int volume) {
        synchronized (this.mVolumeClients) {
            if (!this.mVolumeClients.isEmpty()) {
                Iterator<VolumeChangeClient> clientIterator = this.mVolumeClients.iterator();
                while (clientIterator.hasNext()) {
                    VolumeChangeClient client = clientIterator.next();
                    if (client != null) {
                        try {
                            if (client.mDispatcherCb != null) {
                                client.mDispatcherCb.dispatchVolumeChange(device, stream, caller, volume);
                            }
                        } catch (RemoteException e) {
                            Log.e(TAG, "failed to dispatch audio focus changes");
                        }
                    }
                }
            }
        }
    }

    public void setHistenNaturalMode(boolean on, IBinder cb) {
        this.mHwHistenNaturalModeService.setHistenNaturalMode(on, cb);
    }

    public void onRestoreDevices() {
        if (this.mFmDeviceOn) {
            AudioSystem.setDeviceConnectionState(1048576, 1, "", "", 0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void monitorFsmTipsRemoveBroadcast(Bundle bundle) {
        if (bundle != null) {
            int removeReason = bundle.getInt("KEY_TIPS_INT_REMOVED_REASON");
            if (removeReason == 3 || removeReason == 1) {
                unregisterFsmTipsRequestListener(this.mFoldTipsListener);
                this.mShowEarpieceTip = false;
            }
        }
    }

    public void registerFsmTipsRequestListener(HwFoldScreenManagerEx.FoldFsmTipsRequestListener listener, int type) {
        try {
            Log.d(TAG, "Register fsm tips request listener: ");
            HwFoldScreenManagerEx.registerFsmTipsRequestListener(listener, type);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Register fsm tips request listener error with IllegalArgumentException");
        } catch (NoSuchMethodError e2) {
            Log.e(TAG, "Not found such method.");
        } catch (Exception e3) {
            Log.e(TAG, "Register fsm tips request listener Exception");
        }
    }

    public void unregisterFsmTipsRequestListener(HwFoldScreenManagerEx.FoldFsmTipsRequestListener listener) {
        try {
            HwFoldScreenManagerEx.unregisterFsmTipsRequestListener(listener);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Unregister fsm tips request listener error with IllegalArgumentException");
        } catch (NoSuchMethodError e2) {
            Log.e(TAG, "Error. Not found such method. ");
        } catch (Exception e3) {
            Log.e(TAG, "Unregister fsm tips request listener Exception");
        }
    }

    private boolean isUserClickedCloseThreeTimes() {
        Context context = this.mContext;
        if (context != null && Settings.Global.getInt(context.getContentResolver(), USER_CLICK_CLOSE_TIMES, 0) >= 3) {
            return true;
        }
        return false;
    }

    private boolean isUsedFoldAnswerThreeTimes() {
        Context context = this.mContext;
        if (context != null && Settings.Global.getInt(context.getContentResolver(), FOLD_ANSWER_TIMES, 0) >= 3) {
            return true;
        }
        return false;
    }

    public boolean isWithinCycleTime() {
        Context context = this.mContext;
        if (context == null) {
            return false;
        }
        long tipTime = Settings.Global.getLong(context.getContentResolver(), FIRST_START_INWARD_FOLD_SCREEN_DIALOG, 0);
        if (tipTime <= 0) {
            return true;
        }
        if (System.currentTimeMillis() - tipTime < TWO_WEEKS.longValue()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkAppPlayOnEarpiece(String pkgname, int device, int ioHandle) {
        Bundle data = new Bundle();
        data.putString("KEY_TIPS_STR_CALLER_NAME", TAG);
        if (this.mAudioMode == 2 || HwFoldScreenManagerEx.getDisplayMode() != 1 || !pkgname.equals(getTopActivityPackageName()) || !isWithinCycleTime() || isUserClickedCloseThreeTimes() || isUsedFoldAnswerThreeTimes() || device != 1 || (ioHandle == 13 && this.mAudioMode == 3)) {
            this.mShowEarpieceTip = false;
            HwFoldScreenManagerEx.reqShowTipsToFsm(1, data);
            unregisterFsmTipsRequestListener(this.mFoldTipsListener);
            return;
        }
        data.putInt("KEY_TIPS_INT_VIEW_TYPE", 0);
        data.putString("KEY_TIPS_STR_CAMERA_ID", "1");
        data.putString("KEY_TIPS_TEXT", String.format(Locale.ROOT, this.mContext.getString(33686100), new Object[0]) + String.format(Locale.ROOT, this.mContext.getString(33686101), new Object[0]));
        data.putInt("KEY_PRIORITY_FOLD_TIPS", 7);
        this.mShowEarpieceTip = true;
        HwFoldScreenManagerEx.reqShowTipsToFsm(2, data);
        registerFsmTipsRequestListener(this.mFoldTipsListener, 4);
        if (Settings.Global.getLong(this.mContext.getContentResolver(), FIRST_START_INWARD_FOLD_SCREEN_DIALOG, 0) == 0) {
            Settings.Global.putLong(this.mContext.getContentResolver(), FIRST_START_INWARD_FOLD_SCREEN_DIALOG, System.currentTimeMillis());
        }
    }

    public void enableNotiOnEarpiece() {
        if (SHOW_EARPIECE_TIP) {
            AudioSystem.setDeviceCallback();
            AudioSystem.setPlaybackActivityMonitorCallback(this.mPlaybackActivityMonitorCallback);
            HwFoldScreenManagerEx.registerFoldDisplayMode(this.mDisplayListener);
        }
    }

    public boolean isSystemApp(int uid) {
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null) {
            Log.e(TAG, "get PackageManager is null");
            return false;
        }
        String[] packageNames = pm.getPackagesForUid(uid);
        if (packageNames == null || packageNames.length == 0) {
            Log.e(TAG, "packageNames is null");
            return false;
        }
        for (String pkgName : packageNames) {
            try {
                ApplicationInfo appInfo = pm.getApplicationInfo(pkgName, 0);
                if (appInfo == null) {
                    Log.w(TAG, "uid " + uid + " appinfo is null");
                    return false;
                } else if (!appInfo.isSystemApp() && !appInfo.isUpdatedSystemApp()) {
                    return false;
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "get ApplicationInfo failed");
                return false;
            }
        }
        return true;
    }

    public void setVoiceRecordingEnable(boolean enable) {
        if (!IS_MULTI_AUDIO_RECORD_ALLOWED || !IS_CHINA_AREA) {
            Slog.i(TAG, "do not support VoiceRecording");
        }
        int uid = Binder.getCallingUid();
        Log.i(TAG, "setVoiceRecordingEnable " + enable + ", uid = " + uid);
        AudioSystem.setVoiceRecordingEnable(enable, uid);
    }

    public boolean isVoiceRecordingEnable() {
        return AudioSystem.isVoiceRecordingEnable();
    }

    public void setMultiAudioRecordEnable(boolean enable) {
        if (!IS_MULTI_AUDIO_RECORD_ALLOWED || !IS_CHINA_AREA) {
            Slog.i(TAG, "do not support MultiAudioRecord, set enable failed");
        }
        if (Binder.getCallingUid() > 10000) {
            Log.e(TAG, "no permission to setMultiAudioRecordEnable");
        } else if (this.mIsMultiAudioRecordEnable != enable) {
            this.mIsMultiAudioRecordEnable = enable;
            setMultiAudioRecordEnableToProvider(this.mIsMultiAudioRecordEnable);
            AudioSystem.setMultiAudioRecordEnable(this.mIsMultiAudioRecordEnable);
            if (this.mSystemReady) {
                Intent intent = new Intent();
                intent.setAction(ACTION_MULTI_AUDIORECORD_FLAG_CHANGED);
                intent.putExtra(MULTI_AUDIORECORD_FLAG, this.mIsMultiAudioRecordEnable);
                this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, SYSTEM_MANAGER_PERMISSION);
            }
            Log.i(TAG, "setMultiAudioRecordEnable " + enable + " to provider and native");
            HwMediaMonitorManager.writeBigData(916600039, enable ? "enable" : AppActConstant.VALUE_DISABLE, "", "");
        }
    }

    public boolean isMultiAudioRecordEnable() {
        return this.mIsMultiAudioRecordEnable;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setMultiAudioRecordEnableToProvider(boolean enable) {
        Settings.Global.putInt(this.mContext.getContentResolver(), MULTI_AUDIORECORD_FLAG, enable ? 1 : 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int isMultiAudioRecordEnableFromProvider() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), MULTI_AUDIORECORD_FLAG, -1);
    }

    public void setGameForeground() {
        String pkg = getTopActivityPackageName();
        int uid = getUidByPkg(pkg);
        if (uid == -1) {
            Log.e(TAG, "failed to setGameForeground, caller uid = " + uid);
        } else if (AppTypeRecoManager.getInstance().getAppType(pkg) == 9) {
            AudioSystem.setGameForeground(uid);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendMultiAudioRecordToastMesg(String prePkgName) {
        if (!shouldIgnoreSilenceToast(prePkgName)) {
            Message msg = Message.obtain();
            Bundle bundle = new Bundle();
            bundle.putString(PRE_PKG_STR, prePkgName);
            msg.obj = bundle;
            msg.what = 24;
            this.mHwAudioHandlerEx.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void msgShowMultiAudioRecordToast(Message msg) {
        if (msg.obj != null && (msg.obj instanceof Bundle)) {
            String prePkgName = ((Bundle) msg.obj).getString(PRE_PKG_STR);
            if (prePkgName == null) {
                Slog.w(TAG, "bundle getString prePkgName is invalid");
            } else {
                showMultiAudioRecordToast(prePkgName);
            }
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0022: APUT  (r5v0 java.lang.Object[]), (0 ??[int, short, byte, char]), (r7v0 java.lang.String) */
    private void showMultiAudioRecordToast(String prePkgName) {
        if (prePkgName == null) {
            Slog.w(TAG, "prePkgName is invalid, stop showMultiAudioRecordToast");
            return;
        }
        String prePkgLabel = getApplicationLabel(prePkgName);
        Locale locale = Locale.ROOT;
        String string = this.mContext.getString(33686068);
        Object[] objArr = new Object[1];
        objArr[0] = prePkgLabel == null ? prePkgName : prePkgLabel;
        String toastStr = String.format(locale, string, objArr);
        ContextThemeWrapper themeContext = new ContextThemeWrapper(this.mContext, this.mContext.getResources().getIdentifier(TOAST_THEME, null, null));
        Toast toast = this.mToast;
        if (toast == null) {
            this.mToast = Toast.makeText(themeContext, toastStr, 1);
            this.mToast.getWindowParams().privateFlags |= 16;
        } else {
            toast.setText(toastStr);
        }
        try {
            this.mToast.show();
        } catch (IllegalStateException e) {
            Log.e(TAG, "showMultiAudioRecordToast fail");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendShowMultiAudioRecordNotificationMesg() {
        Message msg = Message.obtain();
        msg.obj = new Bundle();
        msg.what = 25;
        this.mHwAudioHandlerEx.sendMessage(msg);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyMultiAudioRecord() {
        if (this.mContext == null) {
            Slog.i(TAG, "context is null, stop notifyMultiAudioRecord");
            return;
        }
        Slog.i(TAG, "notifyMultiAudioRecord");
        PendingIntent pi = PendingIntent.getBroadcast(this.mContext, 0, new Intent(ACTION_MULTI_RECORD_NOTIFY), 268435456);
        this.mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        String contentTitle = String.format(Locale.ROOT, this.mContext.getString(33686060), new Object[0]);
        String bigText = String.format(Locale.ROOT, this.mContext.getString(33686051), new Object[0]);
        String tip = this.mContext.getString(17039588);
        this.mNotificationManager.createNotificationChannel(new NotificationChannel("MultiAudioRecordNotificationChannel", "MultiAudioRecordNotificationChannel", 4));
        this.mNotificationManager.notifyAsUser(TAG, 3, new Notification.Builder(this.mContext, "MultiAudioRecordNotificationChannel").setSmallIcon(33751741).setContentTitle(contentTitle).setStyle(new Notification.BigTextStyle().bigText(bigText)).addAction(0, tip, pi).setAutoCancel(true).setDefaults(-1).build(), UserHandle.ALL);
        registerMultiAudioRecordReceiver();
    }

    private void registerMultiAudioRecordReceiver() {
        Slog.i(TAG, "registerMultiAudioRecordReceiver");
        this.mMultiAudioRecordReceiver = new BroadcastReceiver() {
            /* class com.android.server.audio.HwAudioServiceEx.AnonymousClass19 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent == null) {
                    Slog.e(HwAudioServiceEx.TAG, "MultiAudioRecordReceiver onReceive null intent");
                    return;
                }
                String action = intent.getAction();
                if (action == null) {
                    Slog.e(HwAudioServiceEx.TAG, "MultiAudioRecordReceiver onReceive null action");
                    return;
                }
                char c = 65535;
                if (action.hashCode() == -731548984 && action.equals(HwAudioServiceEx.ACTION_MULTI_RECORD_NOTIFY)) {
                    c = 0;
                }
                if (c != 0) {
                    Slog.e(HwAudioServiceEx.TAG, "registerMultiAudioRecordReceiver onReceive unKnown action");
                } else {
                    HwAudioServiceEx.this.setMultiAudioRecordEnable(true);
                    if (HwAudioServiceEx.this.mNotificationManager != null) {
                        HwAudioServiceEx.this.mNotificationManager.cancel(HwAudioServiceEx.TAG, 3);
                    }
                }
                HwAudioServiceEx.this.mNotificationManager = null;
                if (HwAudioServiceEx.this.mContext != null) {
                    HwAudioServiceEx.this.mContext.unregisterReceiver(this);
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_MULTI_RECORD_NOTIFY);
        this.mContext.registerReceiver(this.mMultiAudioRecordReceiver, intentFilter);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendShowMultiAudioRecordDialogMesg() {
        Message msg = Message.obtain();
        msg.obj = new Bundle();
        msg.what = 26;
        this.mHwAudioHandlerEx.sendMessage(msg);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showMultiAudioRecordDialog() {
        Slog.i(TAG, "showMultiAudioRecordDialog");
        int themeId = this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null);
        String title = String.format(Locale.ROOT, this.mContext.getString(33686060), new Object[0]);
        String messageStr = String.format(Locale.ROOT, this.mContext.getString(33686051), new Object[0]);
        this.mContext.getString(17039588);
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext, themeId);
        builder.setTitle(title);
        builder.setMessage(messageStr);
        builder.setCancelable(false);
        builder.setPositiveButton(17039588, new DialogInterface.OnClickListener() {
            /* class com.android.server.audio.HwAudioServiceEx.AnonymousClass20 */

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int whichButton) {
                HwAudioServiceEx.this.setMultiAudioRecordEnable(true);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(17040319, new DialogInterface.OnClickListener() {
            /* class com.android.server.audio.HwAudioServiceEx.AnonymousClass21 */

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int whichButton) {
                HwAudioServiceEx.this.setMultiAudioRecordEnableToProvider(false);
                dialog.dismiss();
            }
        });
        Dialog dialog = builder.create();
        dialog.getWindow().setType(2003);
        dialog.show();
    }

    /* access modifiers changed from: private */
    public class SetVolumeByPidStreamClient implements IBinder.DeathRecipient {
        private IBinder mCb;

        SetVolumeByPidStreamClient(IBinder cb) {
            if (cb != null) {
                try {
                    cb.linkToDeath(this, 0);
                } catch (RemoteException e) {
                    Log.w(HwAudioServiceEx.TAG, "SetVolumeByPidStreamClient() could not link to " + cb + " binder death");
                    cb = null;
                }
            }
            this.mCb = cb;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            synchronized (HwAudioServiceEx.this.mSetVolumeByPidStreamLock) {
                Log.w(HwAudioServiceEx.TAG, "SetVolumeByPidStream Client died");
                release();
                restore();
            }
        }

        public void release() {
            IBinder iBinder = this.mCb;
            if (iBinder != null) {
                iBinder.unlinkToDeath(this, 0);
                this.mCb = null;
            }
        }

        public IBinder getBinder() {
            return this.mCb;
        }

        public void restore() {
            HwAudioServiceEx.this.mSetVolumeByPidStreamClient = null;
            for (Pair<Integer, Integer> key : HwAudioServiceEx.this.mSetVolumeByPidStreamMap.keySet()) {
                int pid = ((Integer) key.first).intValue();
                AudioSystem.setParameters(("setVolumePid=" + pid) + AwarenessInnerConstants.SEMI_COLON_KEY + ("setVolumeStream=" + ((Integer) key.second).intValue()) + AwarenessInnerConstants.SEMI_COLON_KEY + ("setVolume=1.0"));
            }
            HwAudioServiceEx.this.mSetVolumeByPidStreamMap.clear();
        }
    }

    public boolean isSystemAppByPid(int pid) {
        String pkgName = getPackageNameByPid(pid);
        if (pkgName == null) {
            Log.w(TAG, "isSystemAppByPid pkgName is null");
            return false;
        }
        Log.i(TAG, "isSystemAppByPid pkgName is " + pkgName);
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null) {
            Log.w(TAG, "get PackageManager is null");
            return false;
        }
        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(pkgName, 0);
            if (appInfo == null) {
                Log.w(TAG, "pid " + pid + " package " + pkgName + " appinfo is null");
                return false;
            } else if (appInfo.isSystemApp() || appInfo.isUpdatedSystemApp()) {
                return true;
            } else {
                return false;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "get ApplicationInfo failed");
            return false;
        }
    }

    public boolean setVolumeByPidStream(int pid, int streamType, float volume, IBinder cb) {
        if (!isSystemAppByPid(Binder.getCallingPid())) {
            Log.w(TAG, "setVolumeByPidStream: permission denied");
            return false;
        } else if (volume < 0.0f || volume > 1.0f) {
            Log.w(TAG, "setVolumeByPidStream: volume is invalid");
            return false;
        } else {
            synchronized (this.mSetVolumeByPidStreamLock) {
                if (this.mSetVolumeByPidStreamClient == null) {
                    this.mSetVolumeByPidStreamClient = new SetVolumeByPidStreamClient(cb);
                } else if (this.mSetVolumeByPidStreamClient.getBinder() == cb) {
                    Log.i(TAG, "SetVolumeByPidStreamClient cb:" + cb + " is already linked.");
                } else {
                    Log.w(TAG, "setVolumeByPidStream: Only allow at most one application call.");
                    this.mSetVolumeByPidStreamClient.release();
                    this.mSetVolumeByPidStreamClient.restore();
                    this.mSetVolumeByPidStreamClient = new SetVolumeByPidStreamClient(cb);
                }
                String param1 = "setVolumePid=" + pid;
                String param2 = "setVolumeStream=" + streamType;
                String param3 = "setVolume=" + volume;
                updateMSetVolumeByPidStreamMap(pid, streamType, volume);
                if (AudioSystem.setParameters(param1 + AwarenessInnerConstants.SEMI_COLON_KEY + param2 + AwarenessInnerConstants.SEMI_COLON_KEY + param3) == 0) {
                    return true;
                }
                return false;
            }
        }
    }

    private void updateMSetVolumeByPidStreamMap(int pid, int streamType, float volume) {
        Pair<Integer, Integer> key = new Pair<>(Integer.valueOf(pid), Integer.valueOf(streamType));
        Pair<Integer, Integer> allStreamKey = new Pair<>(Integer.valueOf(pid), -100);
        if (streamType == -100) {
            Iterator<Map.Entry<Pair<Integer, Integer>, Float>> entries = this.mSetVolumeByPidStreamMap.entrySet().iterator();
            while (entries.hasNext()) {
                if (((Integer) entries.next().getKey().first).intValue() == pid) {
                    entries.remove();
                }
            }
            if (Math.abs(volume - 1.0f) < 1.0E-6f) {
                this.mSetVolumeByPidStreamMap.remove(key);
            } else {
                this.mSetVolumeByPidStreamMap.put(key, Float.valueOf(volume));
            }
        } else if (Math.abs(volume - 1.0f) >= 1.0E-6f || this.mSetVolumeByPidStreamMap.containsKey(allStreamKey)) {
            this.mSetVolumeByPidStreamMap.put(key, Float.valueOf(volume));
        } else {
            this.mSetVolumeByPidStreamMap.remove(key);
        }
        if (this.mSetVolumeByPidStreamMap.size() > 100) {
            Log.w(TAG, "setVolumeByPidStream: HashMap size " + this.mSetVolumeByPidStreamMap.size());
        }
    }
}
