package com.android.server.audio;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.AlarmManager;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.IUidObserver;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothHearingAid;
import android.bluetooth.BluetoothProfile;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.database.ContentObserver;
import android.hardware.hdmi.HdmiControlManager;
import android.hardware.hdmi.HdmiPlaybackClient;
import android.hardware.hdmi.HdmiTvClient;
import android.media.AudioAttributes;
import android.media.AudioDevicePort;
import android.media.AudioFocusInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioManagerInternal;
import android.media.AudioPlaybackConfiguration;
import android.media.AudioPort;
import android.media.AudioRecordingConfiguration;
import android.media.AudioRoutesInfo;
import android.media.AudioSystem;
import android.media.HwMediaMonitorManager;
import android.media.IAudioFocusDispatcher;
import android.media.IAudioModeDispatcher;
import android.media.IAudioRoutesObserver;
import android.media.IAudioServerStateDispatcher;
import android.media.IPlaybackConfigDispatcher;
import android.media.IRecordingConfigDispatcher;
import android.media.IRingtonePlayer;
import android.media.IVolumeController;
import android.media.MediaPlayer;
import android.media.PlayerBase;
import android.media.SoundPool;
import android.media.VolumePolicy;
import android.media.audiopolicy.AudioMix;
import android.media.audiopolicy.AudioPolicyConfig;
import android.media.audiopolicy.IAudioPolicyCallback;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManagerInternal;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.service.notification.ZenModeConfig;
import android.service.vr.IVrManager;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.IntArray;
import android.util.Jlog;
import android.util.Log;
import android.util.MathUtils;
import android.util.Slog;
import android.util.SparseIntArray;
import android.view.accessibility.AccessibilityManager;
import android.vrsystem.IVRSystemServiceManager;
import android.widget.Toast;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.os.HwBootAnimationOeminfo;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.XmlUtils;
import com.android.server.BatteryService;
import com.android.server.EventLogTags;
import com.android.server.HwServiceExFactory;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.UiModeManagerService;
import com.android.server.audio.AudioEventLogger;
import com.android.server.audio.AudioServiceEvents;
import com.android.server.pm.DumpState;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.UserManagerService;
import com.android.server.slice.SliceClientPermissions;
import com.android.server.utils.PriorityDump;
import com.huawei.android.audio.IHwAudioServiceManager;
import huawei.android.security.IHwBehaviorCollectManager;
import huawei.cust.HwCustUtils;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.xmlpull.v1.XmlPullParserException;

public class AudioService extends AbsAudioService implements AccessibilityManager.TouchExplorationStateChangeListener, AccessibilityManager.AccessibilityServicesStateChangeListener, IHwAudioServiceInner {
    private static final String ACTION_CHECK_MUSIC_ACTIVE = "ACTION_CHECK_MUSIC_ACTIVE";
    private static final String ASSET_FILE_VERSION = "1.0";
    private static final String ATTR_ASSET_FILE = "file";
    private static final String ATTR_ASSET_ID = "id";
    private static final String ATTR_GROUP_NAME = "name";
    private static final String ATTR_VERSION = "version";
    private static final int BTA2DP_DOCK_TIMEOUT_MILLIS = 8000;
    private static final int BT_HEADSET_CNCT_TIMEOUT_MS = 3000;
    private static final int BT_HEARING_AID_GAIN_MIN = -128;
    private static final int CHECK_MUSIC_ACTIVE_DELAY_MS = 3000;
    private static final int CHINAZONE_IDENTIFIER = 156;
    public static final String CONNECT_INTENT_KEY_ADDRESS = "address";
    public static final String CONNECT_INTENT_KEY_DEVICE_CLASS = "class";
    public static final String CONNECT_INTENT_KEY_HAS_CAPTURE = "hasCapture";
    public static final String CONNECT_INTENT_KEY_HAS_MIDI = "hasMIDI";
    public static final String CONNECT_INTENT_KEY_HAS_PLAYBACK = "hasPlayback";
    public static final String CONNECT_INTENT_KEY_PORT_NAME = "portName";
    public static final String CONNECT_INTENT_KEY_STATE = "state";
    /* access modifiers changed from: private */
    public static final boolean DEBUG = Log.isLoggable(TAG, 3);
    protected static final boolean DEBUG_AP = true;
    protected static final boolean DEBUG_DEVICES = true;
    protected static final boolean DEBUG_MODE = true;
    protected static final boolean DEBUG_VOL = Log.isLoggable("AudioService.VOL", 3);
    private static final int DEFAULT_STREAM_TYPE_OVERRIDE_DELAY_MS = 0;
    protected static final String DEFAULT_VOLUME_KEY_CTL = "default_volume_key_control";
    protected static final int DEFAULT_VOL_STREAM_NO_PLAYBACK = 3;
    private static final int DEVICE_MEDIA_UNMUTED_ON_PLUG = 604137356;
    private static final int DEVICE_OVERRIDE_A2DP_ROUTE_ON_PLUG = 604135436;
    private static final int FLAG_ADJUST_VOLUME = 1;
    private static final int FLAG_PERSIST_VOLUME = 2;
    private static final String GROUP_TOUCH_SOUNDS = "touch_sounds";
    private static final int INDICATE_SYSTEM_READY_RETRY_DELAY_MS = 1000;
    protected static int[] MAX_STREAM_VOLUME = {5, 7, 7, 15, 7, 7, 15, 7, 15, 15, 15};
    protected static int[] MIN_STREAM_VOLUME = {1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1};
    private static final int MSG_A2DP_DEVICE_CONFIG_CHANGE = 103;
    private static final int MSG_ACCESSORY_PLUG_MEDIA_UNMUTE = 27;
    private static final int MSG_AUDIO_SERVER_DIED = 4;
    private static final int MSG_BROADCAST_AUDIO_BECOMING_NOISY = 15;
    private static final int MSG_BROADCAST_BT_CONNECTION_STATE = 19;
    private static final int MSG_BTA2DP_DOCK_TIMEOUT = 106;
    private static final int MSG_BT_HEADSET_CNCT_FAILED = 9;
    private static final int MSG_CHECK_MUSIC_ACTIVE = 14;
    private static final int MSG_CONFIGURE_SAFE_MEDIA_VOLUME = 16;
    private static final int MSG_CONFIGURE_SAFE_MEDIA_VOLUME_FORCED = 17;
    private static final int MSG_DISABLE_AUDIO_FOR_UID = 104;
    private static final int MSG_DISPATCH_AUDIO_SERVER_STATE = 29;
    private static final int MSG_DYN_POLICY_MIX_STATE_UPDATE = 25;
    private static final int MSG_ENABLE_SURROUND_FORMATS = 30;
    private static final int MSG_INDICATE_SYSTEM_READY = 26;
    private static final int MSG_LOAD_SOUND_EFFECTS = 7;
    private static final int MSG_NOTIFY_VOL_EVENT = 28;
    private static final int MSG_PERSIST_MUSIC_ACTIVE_MS = 22;
    private static final int MSG_PERSIST_RINGER_MODE = 3;
    private static final int MSG_PERSIST_SAFE_VOLUME_STATE = 18;
    private static final int MSG_PERSIST_VOLUME = 1;
    private static final int MSG_PLAY_SOUND_EFFECT = 5;
    private static final int MSG_REPORT_NEW_ROUTES = 12;
    private static final int MSG_SET_A2DP_SINK_CONNECTION_STATE = 102;
    private static final int MSG_SET_A2DP_SRC_CONNECTION_STATE = 101;
    private static final int MSG_SET_ALL_VOLUMES = 10;
    private static final int MSG_SET_DEVICE_VOLUME = 0;
    private static final int MSG_SET_FORCE_BT_A2DP_USE = 13;
    protected static final int MSG_SET_FORCE_USE = 8;
    private static final int MSG_SET_HEARING_AID_CONNECTION_STATE = 105;
    private static final int MSG_SET_WIRED_DEVICE_CONNECTION_STATE = 100;
    private static final int MSG_SYSTEM_READY = 21;
    private static final int MSG_UNLOAD_SOUND_EFFECTS = 20;
    private static final int MSG_UNMUTE_STREAM = 24;
    private static final int MUSIC_ACTIVE_POLL_PERIOD_MS = 60000;
    private static final int NUM_SOUNDPOOL_CHANNELS = 4;
    protected static final int PERSIST_DELAY = 500;
    private static final String[] RINGER_MODE_NAMES = {"SILENT", "VIBRATE", PriorityDump.PRIORITY_ARG_NORMAL};
    private static final int SAFE_MEDIA_VOLUME_ACTIVE = 3;
    private static final int SAFE_MEDIA_VOLUME_DISABLED = 1;
    private static final int SAFE_MEDIA_VOLUME_INACTIVE = 2;
    private static final int SAFE_MEDIA_VOLUME_NOT_CONFIGURED = 0;
    private static final int SAFE_VOLUME_CONFIGURE_TIMEOUT_MS = 30000;
    private static final int SCO_MODE_MAX = 2;
    private static final int SCO_MODE_RAW = 1;
    private static final int SCO_MODE_UNDEFINED = -1;
    private static final int SCO_MODE_VIRTUAL_CALL = 0;
    private static final int SCO_MODE_VR = 2;
    private static final int SCO_STATE_ACTIVATE_REQ = 1;
    private static final int SCO_STATE_ACTIVE_EXTERNAL = 2;
    private static final int SCO_STATE_ACTIVE_INTERNAL = 3;
    private static final int SCO_STATE_DEACTIVATE_REQ = 4;
    private static final int SCO_STATE_DEACTIVATING = 5;
    private static final int SCO_STATE_INACTIVE = 0;
    protected static final int SENDMSG_NOOP = 1;
    protected static final int SENDMSG_QUEUE = 2;
    protected static final int SENDMSG_REPLACE = 0;
    private static final int SOUND_EFFECTS_LOAD_TIMEOUT_MS = 5000;
    private static final String SOUND_EFFECTS_PATH = "/media/audio/ui/";
    /* access modifiers changed from: private */
    public static final List<String> SOUND_EFFECT_FILES = new ArrayList();
    private static final int[] STREAM_VOLUME_OPS = {34, 36, 35, 36, 37, 38, 39, 36, 36, 36, 64};
    private static final String TAG = "AudioService";
    private static final String TAG_ASSET = "asset";
    private static final String TAG_AUDIO_ASSETS = "audio_assets";
    private static final String TAG_GROUP = "group";
    private static final int TOUCH_EXPLORE_STREAM_TYPE_OVERRIDE_DELAY_MS = 1000;
    private static final int UNMUTE_STREAM_DELAY = 350;
    private static final int UNSAFE_VOLUME_MUSIC_ACTIVE_MS_MAX = SystemProperties.getInt("ro.config.hw.security_test", 72000000);
    private static final AudioAttributes VIBRATION_ATTRIBUTES = new AudioAttributes.Builder().setContentType(4).setUsage(13).build();
    private static Long mLastDeviceConnectMsgTime = new Long(0);
    protected static int[] mStreamVolumeAlias;
    private static boolean sIndependentA11yVolume = false;
    /* access modifiers changed from: private */
    public static int sSoundEffectVolumeDb;
    private static int sStreamOverrideDelayMs;
    final int LOG_NB_EVENTS_DYN_POLICY = 10;
    final int LOG_NB_EVENTS_FORCE_USE = 20;
    final int LOG_NB_EVENTS_PHONE_STATE = 20;
    final int LOG_NB_EVENTS_VOLUME = 40;
    final int LOG_NB_EVENTS_WIRED_DEV_CONNECTION = 30;
    /* access modifiers changed from: private */
    public final int[][] SOUND_EFFECT_FILES_MAP = ((int[][]) Array.newInstance(int.class, new int[]{10, 2}));
    private final int[] STREAM_VOLUME_ALIAS_DEFAULT = {0, 2, 2, 3, 4, 2, 6, 2, 2, 3, 3};
    private final int[] STREAM_VOLUME_ALIAS_TELEVISION = {3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3};
    private final int[] STREAM_VOLUME_ALIAS_VOICE = {0, 2, 2, 3, 4, 2, 6, 2, 2, 3, 3};
    /* access modifiers changed from: private */
    public BluetoothA2dp mA2dp;
    /* access modifiers changed from: private */
    public final Object mA2dpAvrcpLock = new Object();
    /* access modifiers changed from: private */
    public int[] mAccessibilityServiceUids;
    /* access modifiers changed from: private */
    public final Object mAccessibilityServiceUidsLock = new Object();
    private final ActivityManagerInternal mActivityManagerInternal;
    /* access modifiers changed from: private */
    public AlarmManager mAlarmManager = null;
    private final AppOpsManager mAppOps;
    /* access modifiers changed from: private */
    public PowerManager.WakeLock mAudioEventWakeLock;
    protected AudioHandler mAudioHandler;
    /* access modifiers changed from: private */
    public final HashMap<IBinder, AudioPolicyProxy> mAudioPolicies = new HashMap<>();
    /* access modifiers changed from: private */
    @GuardedBy("mAudioPolicies")
    public int mAudioPolicyCounter = 0;
    /* access modifiers changed from: private */
    public HashMap<IBinder, AsdProxy> mAudioServerStateListeners = new HashMap<>();
    private final AudioSystem.ErrorCallback mAudioSystemCallback = new AudioSystem.ErrorCallback() {
        public void onError(int error) {
            AudioService.this.onErrorCallBackEx(error);
            if (error == 100) {
                AudioService.sendMsg(AudioService.this.mAudioHandler, 4, 1, 0, 0, null, 0);
                AudioService.sendMsg(AudioService.this.mAudioHandler, 29, 2, 0, 0, null, 0);
            }
        }
    };
    private AudioSystemThread mAudioSystemThread;
    /* access modifiers changed from: private */
    public boolean mAvrcpAbsVolSupported = false;
    int mBecomingNoisyIntentDevices = 738361228;
    private boolean mBluetoothA2dpEnabled;
    private final Object mBluetoothA2dpEnabledLock = new Object();
    protected BluetoothHeadset mBluetoothHeadset;
    /* access modifiers changed from: private */
    public BluetoothDevice mBluetoothHeadsetDevice;
    private BluetoothProfile.ServiceListener mBluetoothProfileServiceListener = new BluetoothProfile.ServiceListener() {
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            int i = profile;
            BluetoothProfile bluetoothProfile = proxy;
            Log.d(AudioService.TAG, "onServiceConnected profile=" + i);
            if (i != 11) {
                int intState = 1;
                if (i != 21) {
                    switch (i) {
                        case 1:
                            synchronized (AudioService.this.mScoClients) {
                                AudioService.this.mAudioHandler.removeMessages(9);
                                AudioService.this.mBluetoothHeadset = (BluetoothHeadset) bluetoothProfile;
                                AudioService.this.setBtScoActiveDevice(AudioService.this.mBluetoothHeadset.getActiveDevice());
                                AudioService.this.checkScoAudioState();
                                if (AudioService.this.mScoAudioState == 1 || AudioService.this.mScoAudioState == 4) {
                                    boolean status = false;
                                    if (AudioService.this.mBluetoothHeadsetDevice != null) {
                                        int access$2500 = AudioService.this.mScoAudioState;
                                        if (access$2500 == 1) {
                                            status = AudioService.connectBluetoothScoAudioHelper(AudioService.this.mBluetoothHeadset, AudioService.this.mBluetoothHeadsetDevice, AudioService.this.mScoAudioMode);
                                            if (status) {
                                                int unused = AudioService.this.mScoAudioState = 3;
                                            }
                                        } else if (access$2500 != 4) {
                                            Log.i(AudioService.TAG, "resolve findbugs");
                                        } else {
                                            status = AudioService.disconnectBluetoothScoAudioHelper(AudioService.this.mBluetoothHeadset, AudioService.this.mBluetoothHeadsetDevice, AudioService.this.mScoAudioMode);
                                            if (status) {
                                                int unused2 = AudioService.this.mScoAudioState = 5;
                                            }
                                        }
                                    }
                                    if (!status) {
                                        int unused3 = AudioService.this.mScoAudioState = 0;
                                        AudioService.this.broadcastScoConnectionState(0);
                                    }
                                }
                            }
                            return;
                        case 2:
                            synchronized (AudioService.this.mConnectedDevices) {
                                synchronized (AudioService.this.mA2dpAvrcpLock) {
                                    BluetoothA2dp unused4 = AudioService.this.mA2dp = (BluetoothA2dp) bluetoothProfile;
                                    List<BluetoothDevice> deviceList = AudioService.this.mA2dp.getConnectedDevices();
                                    if (deviceList.size() > 0) {
                                        BluetoothDevice btDevice = deviceList.get(0);
                                        int state = AudioService.this.mA2dp.getConnectionState(btDevice);
                                        if (state != 2) {
                                            intState = 0;
                                        }
                                        int i2 = state;
                                        AudioService.this.queueMsgUnderWakeLock(AudioService.this.mAudioHandler, 102, state, -1, btDevice, AudioService.this.checkSendBecomingNoisyIntent(128, intState, 0));
                                    }
                                }
                            }
                            return;
                        default:
                            return;
                    }
                } else {
                    synchronized (AudioService.this.mConnectedDevices) {
                        synchronized (AudioService.this.mHearingAidLock) {
                            BluetoothHearingAid unused5 = AudioService.this.mHearingAid = (BluetoothHearingAid) bluetoothProfile;
                            List<BluetoothDevice> deviceList2 = AudioService.this.mHearingAid.getConnectedDevices();
                            if (deviceList2.size() > 0) {
                                BluetoothDevice btDevice2 = deviceList2.get(0);
                                int state2 = AudioService.this.mHearingAid.getConnectionState(btDevice2);
                                if (state2 != 2) {
                                    intState = 0;
                                }
                                int i3 = state2;
                                AudioService.this.queueMsgUnderWakeLock(AudioService.this.mAudioHandler, 105, state2, 0, btDevice2, AudioService.this.checkSendBecomingNoisyIntent(134217728, intState, 0));
                            }
                        }
                    }
                }
            } else {
                List<BluetoothDevice> deviceList3 = proxy.getConnectedDevices();
                if (deviceList3.size() > 0) {
                    BluetoothDevice btDevice3 = deviceList3.get(0);
                    synchronized (AudioService.this.mConnectedDevices) {
                        AudioService.this.queueMsgUnderWakeLock(AudioService.this.mAudioHandler, 101, bluetoothProfile.getConnectionState(btDevice3), 0, btDevice3, 0);
                    }
                }
            }
        }

        public void onServiceDisconnected(int profile) {
            Log.d(AudioService.TAG, "onServiceDisconnected profile=" + profile);
            if (profile == 11) {
                AudioService.this.disconnectA2dpSink();
            } else if (profile != 21) {
                switch (profile) {
                    case 1:
                        AudioService.this.disconnectHeadset();
                        return;
                    case 2:
                        AudioService.this.disconnectA2dp();
                        return;
                    default:
                        return;
                }
            } else {
                AudioService.this.disconnectHearingAid();
            }
        }
    };
    /* access modifiers changed from: private */
    @GuardedBy("mSettingsLock")
    public boolean mCameraSoundForced;
    protected final ArrayMap<String, DeviceListSpec> mConnectedDevices = new ArrayMap<>();
    /* access modifiers changed from: private */
    public final ContentResolver mContentResolver;
    /* access modifiers changed from: private */
    public final Context mContext;
    final AudioRoutesInfo mCurAudioRoutes = new AudioRoutesInfo();
    private HwCustAudioService mCust = null;
    protected int mDefaultVolStream = 3;
    private String mDockAddress;
    private boolean mDockAudioMediaEnabled = true;
    /* access modifiers changed from: private */
    public int mDockState = 0;
    private final AudioSystem.DynamicPolicyCallback mDynPolicyCallback = new AudioSystem.DynamicPolicyCallback() {
        public void onDynamicPolicyMixStateUpdate(String regId, int state) {
            if (!TextUtils.isEmpty(regId)) {
                AudioService.sendMsg(AudioService.this.mAudioHandler, 25, 2, state, 0, regId, 0);
            }
        }
    };
    private final AudioEventLogger mDynPolicyLogger = new AudioEventLogger(10, "dynamic policy events (logged when command received by AudioService)");
    /* access modifiers changed from: private */
    public String mEnabledSurroundFormats;
    /* access modifiers changed from: private */
    public int mEncodedSurroundMode;
    /* access modifiers changed from: private */
    public IAudioPolicyCallback mExtVolumeController;
    /* access modifiers changed from: private */
    public final Object mExtVolumeControllerLock = new Object();
    private boolean mFactoryMode = "factory".equals(SystemProperties.get("ro.runmode", "normal"));
    int mFixedVolumeDevices = 2889728;
    /* access modifiers changed from: private */
    public ForceControlStreamClient mForceControlStreamClient = null;
    /* access modifiers changed from: private */
    public final Object mForceControlStreamLock = new Object();
    /* access modifiers changed from: private */
    public final AudioEventLogger mForceUseLogger = new AudioEventLogger(20, "force use (logged before setForceUse() is executed)");
    private int mForcedUseForComm;
    private int mForcedUseForCommExt;
    int mFullVolumeDevices = 0;
    /* access modifiers changed from: private */
    public boolean mHasAlarm = false;
    /* access modifiers changed from: private */
    public boolean mHasSetRingMute = false;
    private final boolean mHasVibrator;
    /* access modifiers changed from: private */
    public boolean mHdmiCecSink;
    private MyDisplayStatusCallback mHdmiDisplayStatusCallback = new MyDisplayStatusCallback();
    /* access modifiers changed from: private */
    public HdmiControlManager mHdmiManager;
    private HdmiPlaybackClient mHdmiPlaybackClient;
    private boolean mHdmiSystemAudioSupported = false;
    private HdmiTvClient mHdmiTvClient;
    /* access modifiers changed from: private */
    public BluetoothHearingAid mHearingAid;
    /* access modifiers changed from: private */
    public final Object mHearingAidLock = new Object();
    protected IHwAudioServiceEx mHwAudioServiceEx = null;
    private IHwBehaviorCollectManager mHwBehaviorManager;
    HwInnerAudioService mHwInnerService = new HwInnerAudioService(this);
    /* access modifiers changed from: private */
    public boolean mIsChineseZone = true;
    private boolean mIsHisiPlatform = false;
    /* access modifiers changed from: private */
    public final boolean mIsSingleVolume;
    private KeyguardManager mKeyguardManager;
    private long mLoweredFromNormalToVibrateTime;
    private int mMcc = 0;
    protected final MediaFocusControl mMediaFocusControl;
    private int mMode = 0;
    private final AudioEventLogger mModeLogger = new AudioEventLogger(20, "phone state (logged after successfull call to AudioSystem.setPhoneState(int))");
    /* access modifiers changed from: private */
    public final boolean mMonitorRotation;
    private int mMusicActiveMs;
    private int mMuteAffectedStreams;
    private NotificationManager mNm;
    /* access modifiers changed from: private */
    public PendingIntent mPendingIntent = null;
    private StreamVolumeCommand mPendingVolumeCommand;
    private final int mPlatformType;
    protected final PlaybackActivityMonitor mPlaybackMonitor;
    private int mPrevVolDirection = 0;
    private final BroadcastReceiver mReceiver = new AudioServiceBroadcastReceiver();
    private final RecordingActivityMonitor mRecordMonitor;
    private int mRingerAndZenModeMutedStreams;
    @GuardedBy("mSettingsLock")
    private int mRingerMode;
    private int mRingerModeAffectedStreams = 0;
    /* access modifiers changed from: private */
    public AudioManagerInternal.RingerModeDelegate mRingerModeDelegate;
    @GuardedBy("mSettingsLock")
    private int mRingerModeExternal = -1;
    private volatile IRingtonePlayer mRingtonePlayer;
    private ArrayList<RmtSbmxFullVolDeathHandler> mRmtSbmxFullVolDeathHandlers = new ArrayList<>();
    private int mRmtSbmxFullVolRefCount = 0;
    final RemoteCallbackList<IAudioRoutesObserver> mRoutesObservers = new RemoteCallbackList<>();
    private final int mSafeMediaVolumeDevices = 603979788;
    private int mSafeMediaVolumeIndex;
    private Integer mSafeMediaVolumeState;
    private float mSafeUsbMediaVolumeDbfs;
    private int mSafeUsbMediaVolumeIndex;
    /* access modifiers changed from: private */
    public String mSafeVolumeCaller = null;
    /* access modifiers changed from: private */
    public int mScoAudioMode;
    /* access modifiers changed from: private */
    public int mScoAudioState;
    /* access modifiers changed from: private */
    public final ArrayList<ScoClient> mScoClients = new ArrayList<>();
    private int mScoConnectionState;
    /* access modifiers changed from: private */
    public boolean mScreenOn = true;
    protected final ArrayList<SetModeDeathHandler> mSetModeDeathHandlers = new ArrayList<>();
    /* access modifiers changed from: private */
    public final Object mSettingsLock = new Object();
    private SettingsObserver mSettingsObserver;
    /* access modifiers changed from: private */
    public final Object mSoundEffectsLock = new Object();
    /* access modifiers changed from: private */
    public SoundPool mSoundPool;
    /* access modifiers changed from: private */
    public SoundPoolCallback mSoundPoolCallBack;
    /* access modifiers changed from: private */
    public SoundPoolListenerThread mSoundPoolListenerThread;
    /* access modifiers changed from: private */
    public Looper mSoundPoolLooper = null;
    /* access modifiers changed from: private */
    public VolumeStreamState[] mStreamStates;
    /* access modifiers changed from: private */
    public boolean mSurroundModeChanged;
    protected boolean mSystemReady;
    private final IUidObserver mUidObserver = new IUidObserver.Stub() {
        public void onUidStateChanged(int uid, int procState, long procStateSeq) {
        }

        public void onUidGone(int uid, boolean disabled) {
            disableAudioForUid(false, uid);
        }

        public void onUidActive(int uid) throws RemoteException {
        }

        public void onUidIdle(int uid, boolean disabled) {
        }

        public void onUidCachedChanged(int uid, boolean cached) {
            disableAudioForUid(cached, uid);
        }

        private void disableAudioForUid(boolean disable, int uid) {
            AudioService.this.queueMsgUnderWakeLock(AudioService.this.mAudioHandler, 104, disable ? 1 : 0, uid, null, 0);
        }
    };
    /* access modifiers changed from: private */
    public final boolean mUseFixedVolume;
    private final UserManagerInternal mUserManagerInternal;
    private final UserManagerInternal.UserRestrictionsListener mUserRestrictionsListener = new AudioServiceUserRestrictionsListener();
    /* access modifiers changed from: private */
    public boolean mUserSelectedVolumeControlStream = false;
    /* access modifiers changed from: private */
    public boolean mUserSwitchedReceived;
    private int mVibrateSetting;
    private Vibrator mVibrator;
    /* access modifiers changed from: private */
    public int mVolumeControlStream = -1;
    /* access modifiers changed from: private */
    public final VolumeController mVolumeController = new VolumeController();
    private final AudioEventLogger mVolumeLogger = new AudioEventLogger(40, "volume changes (logged when command received by AudioService)");
    private VolumePolicy mVolumePolicy = VolumePolicy.DEFAULT;
    /* access modifiers changed from: private */
    public final AudioEventLogger mWiredDevLogger = new AudioEventLogger(30, "wired device connection (logged before onSetWiredDeviceConnectionState() is executed)");
    private int mZenModeAffectedStreams = 0;

    private class AsdProxy implements IBinder.DeathRecipient {
        private final IAudioServerStateDispatcher mAsd;

        AsdProxy(IAudioServerStateDispatcher asd) {
            this.mAsd = asd;
        }

        public void binderDied() {
            synchronized (AudioService.this.mAudioServerStateListeners) {
                AudioService.this.mAudioServerStateListeners.remove(this.mAsd.asBinder());
            }
        }

        /* access modifiers changed from: package-private */
        public IAudioServerStateDispatcher callback() {
            return this.mAsd;
        }
    }

    private class AudioHandler extends Handler {
        private AudioHandler() {
        }

        private void setAllVolumes(VolumeStreamState streamState) {
            streamState.applyAllVolumes();
            for (int streamType = AudioSystem.getNumStreamTypes() - 1; streamType >= 0; streamType--) {
                if (streamType != streamState.mStreamType && AudioService.mStreamVolumeAlias[streamType] == streamState.mStreamType) {
                    AudioService.this.mStreamStates[streamType].applyAllVolumes();
                }
            }
        }

        private void persistVolume(VolumeStreamState streamState, int device) {
            if (!AudioService.this.checkEnbaleVolumeAdjust() || AudioService.this.mUseFixedVolume) {
                return;
            }
            if (!AudioService.this.mIsSingleVolume || streamState.mStreamType == 3) {
                if (streamState.hasValidSettingsName()) {
                    Settings.System.putIntForUser(AudioService.this.mContentResolver, streamState.getSettingNameForDevice(device), (streamState.getIndex(device) + 5) / 10, -2);
                }
                if (2 == streamState.mStreamType && 2 == device) {
                    HwBootAnimationOeminfo.setBootAnimRing((streamState.getIndex(device) + 5) / 10);
                }
            }
        }

        private void persistRingerMode(int ringerMode) {
            if (AudioService.this.checkEnbaleVolumeAdjust() && !AudioService.this.mUseFixedVolume) {
                if (2 == ringerMode) {
                    Log.i(AudioService.TAG, "set 1 to ringermode");
                    HwBootAnimationOeminfo.setBootAnimRingMode(1);
                } else {
                    Log.i(AudioService.TAG, "set 0 to ringermode");
                    HwBootAnimationOeminfo.setBootAnimRingMode(0);
                }
                Settings.Global.putInt(AudioService.this.mContentResolver, "mode_ringer", ringerMode);
            }
        }

        private String getSoundEffectFilePath(int effectType) {
            String filePath = Environment.getProductDirectory() + AudioService.SOUND_EFFECTS_PATH + ((String) AudioService.SOUND_EFFECT_FILES.get(AudioService.this.SOUND_EFFECT_FILES_MAP[effectType][0]));
            if (new File(filePath).isFile()) {
                return filePath;
            }
            return Environment.getRootDirectory() + AudioService.SOUND_EFFECTS_PATH + ((String) AudioService.SOUND_EFFECT_FILES.get(AudioService.this.SOUND_EFFECT_FILES_MAP[effectType][0]));
        }

        /* JADX WARNING: Code restructure failed: missing block: B:85:0x0210, code lost:
            if (r0 != 0) goto L_0x0214;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:86:0x0212, code lost:
            r3 = true;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:87:0x0214, code lost:
            return r3;
         */
        private boolean onLoadSoundEffects() {
            int i;
            int status;
            synchronized (AudioService.this.mSoundEffectsLock) {
                boolean z = false;
                if (!AudioService.this.mSystemReady) {
                    Log.w(AudioService.TAG, "onLoadSoundEffects() called before boot complete");
                    return false;
                } else if (AudioService.this.mSoundPool != null) {
                    return true;
                } else {
                    AudioService.this.loadTouchSoundAssets();
                    SoundPool unused = AudioService.this.mSoundPool = new SoundPool.Builder().setMaxStreams(4).setAudioAttributes(new AudioAttributes.Builder().setUsage(13).setContentType(4).build()).build();
                    SoundPoolCallback unused2 = AudioService.this.mSoundPoolCallBack = null;
                    SoundPoolListenerThread unused3 = AudioService.this.mSoundPoolListenerThread = new SoundPoolListenerThread();
                    AudioService.this.mSoundPoolListenerThread.start();
                    int attempts = 3;
                    while (true) {
                        if (AudioService.this.mSoundPoolCallBack != null) {
                            break;
                        }
                        int attempts2 = attempts - 1;
                        if (attempts <= 0) {
                            attempts = attempts2;
                            break;
                        }
                        try {
                            AudioService.this.mSoundEffectsLock.wait(5000);
                        } catch (InterruptedException e) {
                            Log.w(AudioService.TAG, "Interrupted while waiting sound pool listener thread.");
                        }
                        attempts = attempts2;
                    }
                    if (AudioService.this.mSoundPoolCallBack == null) {
                        Log.w(AudioService.TAG, "onLoadSoundEffects() SoundPool listener or thread creation error");
                        if (AudioService.this.mSoundPoolLooper != null) {
                            AudioService.this.mSoundPoolLooper.quit();
                            Looper unused4 = AudioService.this.mSoundPoolLooper = null;
                        }
                        SoundPoolListenerThread unused5 = AudioService.this.mSoundPoolListenerThread = null;
                        AudioService.this.mSoundPool.release();
                        SoundPool unused6 = AudioService.this.mSoundPool = null;
                        return false;
                    }
                    int[] poolId = new int[AudioService.SOUND_EFFECT_FILES.size()];
                    int fileIdx = 0;
                    while (true) {
                        i = -1;
                        if (fileIdx >= AudioService.SOUND_EFFECT_FILES.size()) {
                            break;
                        }
                        poolId[fileIdx] = -1;
                        fileIdx++;
                    }
                    int numSamples = 0;
                    int effect = 0;
                    while (effect < 10) {
                        if (AudioService.this.SOUND_EFFECT_FILES_MAP[effect][1] != 0) {
                            if (poolId[AudioService.this.SOUND_EFFECT_FILES_MAP[effect][0]] == i) {
                                int sampleId = AudioService.this.getSampleId(AudioService.this.mSoundPool, effect, getSoundEffectFilePath(effect), 0);
                                if (sampleId > 0) {
                                    AudioService.this.SOUND_EFFECT_FILES_MAP[effect][1] = sampleId;
                                    poolId[AudioService.this.SOUND_EFFECT_FILES_MAP[effect][0]] = sampleId;
                                    numSamples++;
                                } else if (AudioService.DEBUG) {
                                    Log.w(AudioService.TAG, "Soundpool could not load file: " + filePath);
                                }
                            } else {
                                AudioService.this.SOUND_EFFECT_FILES_MAP[effect][1] = poolId[AudioService.this.SOUND_EFFECT_FILES_MAP[effect][0]];
                            }
                        }
                        effect++;
                        i = -1;
                    }
                    if (numSamples > 0) {
                        AudioService.this.mSoundPoolCallBack.setSamples(poolId);
                        int attempts3 = 3;
                        int status2 = 1;
                        while (true) {
                            status = status2;
                            if (status != 1) {
                                int i2 = attempts3;
                                break;
                            }
                            int attempts4 = attempts3 - 1;
                            if (attempts3 <= 0) {
                                break;
                            }
                            try {
                                AudioService.this.mSoundEffectsLock.wait(5000);
                                status2 = AudioService.this.mSoundPoolCallBack.status();
                            } catch (InterruptedException e2) {
                                Log.w(AudioService.TAG, "Interrupted while waiting sound pool callback.");
                                status2 = status;
                            }
                            attempts3 = attempts4;
                        }
                    } else {
                        int i3 = attempts;
                        status = -1;
                    }
                    int status3 = status;
                    if (AudioService.this.mSoundPoolLooper != null) {
                        AudioService.this.mSoundPoolLooper.quit();
                        Looper unused7 = AudioService.this.mSoundPoolLooper = null;
                    }
                    SoundPoolListenerThread unused8 = AudioService.this.mSoundPoolListenerThread = null;
                    if (status3 != 0) {
                        Log.w(AudioService.TAG, "onLoadSoundEffects(), Error " + status3 + " while loading samples");
                        for (int effect2 = 0; effect2 < 10; effect2++) {
                            if (AudioService.this.SOUND_EFFECT_FILES_MAP[effect2][1] > 0) {
                                AudioService.this.SOUND_EFFECT_FILES_MAP[effect2][1] = -1;
                            }
                        }
                        AudioService.this.mSoundPool.release();
                        SoundPool unused9 = AudioService.this.mSoundPool = null;
                    }
                }
            }
        }

        private void onUnloadSoundEffects() {
            synchronized (AudioService.this.mSoundEffectsLock) {
                if (AudioService.this.mSoundPool != null) {
                    AudioService.this.unloadHwThemeSoundEffects();
                    int[] poolId = new int[AudioService.SOUND_EFFECT_FILES.size()];
                    for (int fileIdx = 0; fileIdx < AudioService.SOUND_EFFECT_FILES.size(); fileIdx++) {
                        poolId[fileIdx] = 0;
                    }
                    for (int effect = 0; effect < 10; effect++) {
                        if (AudioService.this.SOUND_EFFECT_FILES_MAP[effect][1] > 0) {
                            if (poolId[AudioService.this.SOUND_EFFECT_FILES_MAP[effect][0]] == 0) {
                                AudioService.this.mSoundPool.unload(AudioService.this.SOUND_EFFECT_FILES_MAP[effect][1]);
                                AudioService.this.SOUND_EFFECT_FILES_MAP[effect][1] = -1;
                                poolId[AudioService.this.SOUND_EFFECT_FILES_MAP[effect][0]] = -1;
                            }
                        }
                    }
                    AudioService.this.mSoundPool.release();
                    SoundPool unused = AudioService.this.mSoundPool = null;
                }
            }
        }

        private void onPlaySoundEffect(int effectType, int volume) {
            float volFloat;
            synchronized (AudioService.this.mSoundEffectsLock) {
                onLoadSoundEffects();
                if (AudioService.this.mSoundPool != null) {
                    if (volume < 0) {
                        volFloat = (float) Math.pow(10.0d, (double) (((float) AudioService.sSoundEffectVolumeDb) / 20.0f));
                    } else {
                        volFloat = ((float) volume) / 1000.0f;
                    }
                    if (AudioService.this.SOUND_EFFECT_FILES_MAP[effectType][1] > 0) {
                        AudioService.this.mSoundPool.play(AudioService.this.SOUND_EFFECT_FILES_MAP[effectType][1], volFloat, volFloat, 0, 0, 1.0f);
                    } else {
                        MediaPlayer mediaPlayer = new MediaPlayer();
                        try {
                            mediaPlayer.setDataSource(getSoundEffectFilePath(effectType));
                            mediaPlayer.setAudioStreamType(1);
                            mediaPlayer.prepare();
                            mediaPlayer.setVolume(volFloat);
                            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                public void onCompletion(MediaPlayer mp) {
                                    AudioHandler.this.cleanupPlayer(mp);
                                }
                            });
                            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                                public boolean onError(MediaPlayer mp, int what, int extra) {
                                    AudioHandler.this.cleanupPlayer(mp);
                                    return true;
                                }
                            });
                            mediaPlayer.start();
                        } catch (IOException ex) {
                            Log.w(AudioService.TAG, "MediaPlayer IOException: " + ex);
                        } catch (IllegalArgumentException ex2) {
                            Log.w(AudioService.TAG, "MediaPlayer IllegalArgumentException: " + ex2);
                        } catch (IllegalStateException ex3) {
                            Log.w(AudioService.TAG, "MediaPlayer IllegalStateException: " + ex3);
                        }
                    }
                }
            }
        }

        /* access modifiers changed from: private */
        public void cleanupPlayer(MediaPlayer mp) {
            if (mp != null) {
                try {
                    mp.stop();
                    mp.release();
                } catch (IllegalStateException ex) {
                    Log.w(AudioService.TAG, "MediaPlayer IllegalStateException: " + ex);
                }
            }
        }

        private void setForceUse(int usage, int config, String eventSource) {
            synchronized (AudioService.this.mConnectedDevices) {
                AudioService.this.setForceUseInt_SyncDevices(usage, config, eventSource);
            }
        }

        private void onPersistSafeVolumeState(int state) {
            Settings.Global.putInt(AudioService.this.mContentResolver, "audio_safe_volume_state", state);
        }

        private void onNotifyVolumeEvent(IAudioPolicyCallback apc, int direction) {
            try {
                apc.notifyVolumeAdjust(direction);
            } catch (Exception e) {
            }
        }

        public void handleMessage(Message msg) {
            AudioRoutesInfo routes;
            int i = msg.what;
            switch (i) {
                case 0:
                    AudioService.this.setDeviceVolume((VolumeStreamState) msg.obj, msg.arg1);
                    return;
                case 1:
                    persistVolume((VolumeStreamState) msg.obj, msg.arg1);
                    return;
                default:
                    switch (i) {
                        case 3:
                            persistRingerMode(AudioService.this.getRingerModeInternal());
                            return;
                        case 4:
                            AudioService.this.onAudioServerDied();
                            return;
                        case 5:
                            onPlaySoundEffect(msg.arg1, msg.arg2);
                            Jlog.d(175, msg.arg1, "playSoundEffect end");
                            return;
                        default:
                            int i2 = -1;
                            boolean z = false;
                            switch (i) {
                                case 7:
                                    boolean loaded = onLoadSoundEffects();
                                    if (msg.obj != null) {
                                        LoadSoundEffectReply reply = (LoadSoundEffectReply) msg.obj;
                                        synchronized (reply) {
                                            if (loaded) {
                                                i2 = 0;
                                            }
                                            reply.mStatus = i2;
                                            reply.notify();
                                        }
                                        return;
                                    }
                                    return;
                                case 8:
                                    break;
                                case 9:
                                    AudioService.this.resetBluetoothSco();
                                    return;
                                case 10:
                                    setAllVolumes((VolumeStreamState) msg.obj);
                                    return;
                                default:
                                    switch (i) {
                                        case 12:
                                            int N = AudioService.this.mRoutesObservers.beginBroadcast();
                                            if (N > 0) {
                                                synchronized (AudioService.this.mCurAudioRoutes) {
                                                    routes = new AudioRoutesInfo(AudioService.this.mCurAudioRoutes);
                                                    Slog.i(AudioService.TAG, routes.toString());
                                                }
                                                while (N > 0) {
                                                    N--;
                                                    try {
                                                        AudioService.this.mRoutesObservers.getBroadcastItem(N).dispatchAudioRoutesChanged(routes);
                                                    } catch (RemoteException e) {
                                                    }
                                                }
                                            }
                                            AudioService.this.mRoutesObservers.finishBroadcast();
                                            AudioService.this.observeDevicesForStreams(-1);
                                            return;
                                        case 13:
                                            break;
                                        case 14:
                                            if (AudioService.this.mIsChineseZone || !AudioService.this.mHasAlarm) {
                                                AudioService.this.onCheckMusicActive((String) msg.obj);
                                                return;
                                            }
                                            return;
                                        case 15:
                                            AudioService.this.onSendBecomingNoisyIntent();
                                            return;
                                        case 16:
                                        case 17:
                                            AudioService audioService = AudioService.this;
                                            if (msg.what == 17) {
                                                z = true;
                                            }
                                            audioService.onConfigureSafeVolume(z, (String) msg.obj);
                                            return;
                                        case 18:
                                            onPersistSafeVolumeState(msg.arg1);
                                            return;
                                        case 19:
                                            AudioService.this.onBroadcastScoConnectionState(msg.arg1);
                                            return;
                                        case 20:
                                            onUnloadSoundEffects();
                                            return;
                                        case 21:
                                            AudioService.this.onSystemReady();
                                            return;
                                        case 22:
                                            Settings.Secure.putIntForUser(AudioService.this.mContentResolver, "unsafe_volume_music_active_ms", msg.arg1, -2);
                                            return;
                                        default:
                                            switch (i) {
                                                case 24:
                                                    AudioService.this.onUnmuteStream(msg.arg1, msg.arg2);
                                                    return;
                                                case 25:
                                                    AudioService.this.onDynPolicyMixStateUpdate((String) msg.obj, msg.arg1);
                                                    return;
                                                case 26:
                                                    AudioService.this.onIndicateSystemReady();
                                                    return;
                                                case AudioService.MSG_ACCESSORY_PLUG_MEDIA_UNMUTE /*27*/:
                                                    AudioService.this.onAccessoryPlugMediaUnmute(msg.arg1);
                                                    return;
                                                case 28:
                                                    onNotifyVolumeEvent((IAudioPolicyCallback) msg.obj, msg.arg1);
                                                    return;
                                                case 29:
                                                    AudioService audioService2 = AudioService.this;
                                                    if (msg.arg1 == 1) {
                                                        z = true;
                                                    }
                                                    audioService2.onDispatchAudioServerStateChange(z);
                                                    return;
                                                case 30:
                                                    AudioService.this.onEnableSurroundFormats((ArrayList) msg.obj);
                                                    return;
                                                default:
                                                    switch (i) {
                                                        case 100:
                                                            Log.i(AudioService.TAG, "handle msg:wired device connection");
                                                            WiredDeviceConnectionState connectState = (WiredDeviceConnectionState) msg.obj;
                                                            AudioService.this.mWiredDevLogger.log(new AudioServiceEvents.WiredDevConnectEvent(connectState));
                                                            AudioService.this.onSetWiredDeviceConnectionState(connectState.mType, connectState.mState, connectState.mAddress, connectState.mName, connectState.mCaller);
                                                            AudioService.this.mAudioEventWakeLock.release();
                                                            AudioService.this.updateAftPolicy();
                                                            return;
                                                        case 101:
                                                            AudioService.this.onSetA2dpSourceConnectionState((BluetoothDevice) msg.obj, msg.arg1);
                                                            AudioService.this.mAudioEventWakeLock.release();
                                                            return;
                                                        case 102:
                                                            AudioService.this.onSetA2dpSinkConnectionState((BluetoothDevice) msg.obj, msg.arg1, msg.arg2);
                                                            AudioService.this.mAudioEventWakeLock.release();
                                                            return;
                                                        case 103:
                                                            AudioService.this.onBluetoothA2dpDeviceConfigChange((BluetoothDevice) msg.obj);
                                                            AudioService.this.mAudioEventWakeLock.release();
                                                            return;
                                                        case 104:
                                                            PlaybackActivityMonitor playbackActivityMonitor = AudioService.this.mPlaybackMonitor;
                                                            if (msg.arg1 == 1) {
                                                                z = true;
                                                            }
                                                            playbackActivityMonitor.disableAudioForUid(z, msg.arg2);
                                                            AudioService.this.mAudioEventWakeLock.release();
                                                            return;
                                                        case 105:
                                                            AudioService.this.onSetHearingAidConnectionState((BluetoothDevice) msg.obj, msg.arg1);
                                                            AudioService.this.mAudioEventWakeLock.release();
                                                            return;
                                                        case 106:
                                                            synchronized (AudioService.this.mConnectedDevices) {
                                                                AudioService.this.makeA2dpDeviceUnavailableNow((String) msg.obj);
                                                                if (AudioService.this.mHasSetRingMute) {
                                                                    AudioService.this.adjustStreamVolume(2, 100, 0, AudioService.this.mContext.getOpPackageName());
                                                                    boolean unused = AudioService.this.mHasSetRingMute = false;
                                                                }
                                                            }
                                                            AudioService.this.mAudioEventWakeLock.release();
                                                            return;
                                                        default:
                                                            AudioService.this.handleMessageEx(msg);
                                                            return;
                                                    }
                                            }
                                    }
                            }
                            setForceUse(msg.arg1, msg.arg2, (String) msg.obj);
                            return;
                    }
            }
        }
    }

    public class AudioPolicyProxy extends AudioPolicyConfig implements IBinder.DeathRecipient {
        private static final String TAG = "AudioPolicyProxy";
        int mFocusDuckBehavior = 0;
        final boolean mHasFocusListener;
        boolean mIsFocusPolicy = false;
        final boolean mIsVolumeController;
        final IAudioPolicyCallback mPolicyCallback;

        AudioPolicyProxy(AudioPolicyConfig config, IAudioPolicyCallback token, boolean hasFocusListener, boolean isFocusPolicy, boolean isVolumeController) {
            super(config);
            setRegistration(new String(config.hashCode() + ":ap:" + this$0.mAudioPolicyCounter = this$0.mAudioPolicyCounter + 1));
            this.mPolicyCallback = token;
            this.mHasFocusListener = hasFocusListener;
            this.mIsVolumeController = isVolumeController;
            if (this.mHasFocusListener) {
                AudioService.this.mMediaFocusControl.addFocusFollower(this.mPolicyCallback);
                if (isFocusPolicy) {
                    this.mIsFocusPolicy = true;
                    AudioService.this.mMediaFocusControl.setFocusPolicy(this.mPolicyCallback);
                }
            }
            if (this.mIsVolumeController) {
                AudioService.this.setExtVolumeController(this.mPolicyCallback);
            }
            connectMixes();
        }

        public void binderDied() {
            synchronized (AudioService.this.mAudioPolicies) {
                Log.i(TAG, "audio policy " + this.mPolicyCallback + " died");
                release();
                AudioService.this.mAudioPolicies.remove(this.mPolicyCallback.asBinder());
            }
            if (this.mIsVolumeController) {
                synchronized (AudioService.this.mExtVolumeControllerLock) {
                    IAudioPolicyCallback unused = AudioService.this.mExtVolumeController = null;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public String getRegistrationId() {
            return getRegistration();
        }

        /* access modifiers changed from: package-private */
        public void release() {
            if (this.mIsFocusPolicy) {
                AudioService.this.mMediaFocusControl.unsetFocusPolicy(this.mPolicyCallback);
            }
            if (this.mFocusDuckBehavior == 1) {
                AudioService.this.mMediaFocusControl.setDuckingInExtPolicyAvailable(false);
            }
            if (this.mHasFocusListener) {
                AudioService.this.mMediaFocusControl.removeFocusFollower(this.mPolicyCallback);
            }
            long identity = Binder.clearCallingIdentity();
            AudioSystem.registerPolicyMixes(this.mMixes, false);
            Binder.restoreCallingIdentity(identity);
        }

        /* access modifiers changed from: package-private */
        public boolean hasMixAffectingUsage(int usage) {
            Iterator it = this.mMixes.iterator();
            while (it.hasNext()) {
                if (((AudioMix) it.next()).isAffectingUsage(usage)) {
                    return true;
                }
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public void addMixes(ArrayList<AudioMix> mixes) {
            synchronized (this.mMixes) {
                AudioSystem.registerPolicyMixes(this.mMixes, false);
                add(mixes);
                AudioSystem.registerPolicyMixes(this.mMixes, true);
            }
        }

        /* access modifiers changed from: package-private */
        public void removeMixes(ArrayList<AudioMix> mixes) {
            synchronized (this.mMixes) {
                AudioSystem.registerPolicyMixes(this.mMixes, false);
                remove(mixes);
                AudioSystem.registerPolicyMixes(this.mMixes, true);
            }
        }

        /* access modifiers changed from: package-private */
        public void connectMixes() {
            long identity = Binder.clearCallingIdentity();
            AudioSystem.registerPolicyMixes(this.mMixes, true);
            Binder.restoreCallingIdentity(identity);
        }
    }

    private class AudioServiceBroadcastReceiver extends BroadcastReceiver {
        private AudioServiceBroadcastReceiver() {
        }

        /* JADX WARNING: Code restructure failed: missing block: B:70:0x0166, code lost:
            r1 = false;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:72:0x0168, code lost:
            if (r1 == false) goto L_0x03b4;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:73:0x016a, code lost:
            com.android.server.audio.AudioService.access$2400(r13.this$0, r6);
            r2 = new android.content.Intent("android.media.SCO_AUDIO_STATE_CHANGED");
            r2.putExtra("android.media.extra.SCO_AUDIO_STATE", r6);
            com.android.server.audio.AudioService.access$9400(r13.this$0, r2);
         */
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(AudioService.TAG, "action: " + action);
            int config = 0;
            if (action.equals("android.intent.action.DOCK_EVENT")) {
                int dockState = intent.getIntExtra("android.intent.extra.DOCK_STATE", 0);
                switch (dockState) {
                    case 1:
                        config = 7;
                        break;
                    case 2:
                        config = 6;
                        break;
                    case 3:
                        config = 8;
                        break;
                    case 4:
                        config = 9;
                        break;
                }
                if (!(dockState == 3 || (dockState == 0 && AudioService.this.mDockState == 3))) {
                    AudioService.this.mForceUseLogger.log(new AudioServiceEvents.ForceUseEvent(3, config, "ACTION_DOCK_EVENT intent"));
                    AudioSystem.setForceUse(3, config);
                }
                int unused = AudioService.this.mDockState = dockState;
            } else if (action.equals("android.bluetooth.headset.profile.action.ACTIVE_DEVICE_CHANGED")) {
                AudioService.this.setBtScoActiveDevice((BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE"));
            } else {
                boolean z = true;
                if (action.equals("android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED")) {
                    boolean broadcast = false;
                    int scoAudioState = -1;
                    synchronized (AudioService.this.mScoClients) {
                        int btState = intent.getIntExtra("android.bluetooth.profile.extra.STATE", -1);
                        if (!AudioService.this.mScoClients.isEmpty() && (AudioService.this.mScoAudioState == 3 || AudioService.this.mScoAudioState == 1 || AudioService.this.mScoAudioState == 4 || AudioService.this.mScoAudioState == 5)) {
                            broadcast = true;
                        }
                        switch (btState) {
                            case 10:
                                BluetoothDevice scoDevice = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
                                if (scoDevice == null || AudioService.this.mBluetoothHeadsetDevice == null || scoDevice.equals(AudioService.this.mBluetoothHeadsetDevice)) {
                                    AudioService.this.setBluetoothScoOn(false);
                                    scoAudioState = 0;
                                    if (AudioService.this.mScoAudioState == 1 && AudioService.this.mBluetoothHeadset != null && AudioService.this.mBluetoothHeadsetDevice != null && AudioService.connectBluetoothScoAudioHelper(AudioService.this.mBluetoothHeadset, AudioService.this.mBluetoothHeadsetDevice, AudioService.this.mScoAudioMode)) {
                                        int unused2 = AudioService.this.mScoAudioState = 3;
                                        broadcast = false;
                                        break;
                                    } else {
                                        AudioService audioService = AudioService.this;
                                        if (AudioService.this.mScoAudioState != 3) {
                                            z = false;
                                        }
                                        audioService.clearAllScoClients(0, z);
                                        int unused3 = AudioService.this.mScoAudioState = 0;
                                        break;
                                    }
                                } else {
                                    return;
                                }
                                break;
                            case 11:
                                if (!(AudioService.this.mScoAudioState == 3 || AudioService.this.mScoAudioState == 4)) {
                                    int unused4 = AudioService.this.mScoAudioState = 2;
                                    break;
                                }
                            case 12:
                                scoAudioState = 1;
                                if (!(AudioService.this.mScoAudioState == 3 || AudioService.this.mScoAudioState == 4)) {
                                    int unused5 = AudioService.this.mScoAudioState = 2;
                                }
                                AudioService.this.setBluetoothScoOn(true);
                                break;
                        }
                    }
                } else if (action.equals("android.intent.action.SCREEN_ON")) {
                    boolean unused6 = AudioService.this.mScreenOn = true;
                    if (AudioService.this.mMonitorRotation) {
                        RotationHelper.enable();
                    }
                    AudioSystem.setParameters("screen_state=on");
                    if (!AudioService.this.mIsChineseZone && AudioService.this.mHasAlarm) {
                        AudioService.this.mAlarmManager.cancel(AudioService.this.mPendingIntent);
                        boolean unused7 = AudioService.this.mHasAlarm = false;
                        AudioService.sendMsg(AudioService.this.mAudioHandler, 14, 0, 0, 0, AudioService.this.mSafeVolumeCaller, 0);
                    }
                } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                    boolean unused8 = AudioService.this.mScreenOn = false;
                    if (AudioService.this.mMonitorRotation) {
                        RotationHelper.disable();
                    }
                    AudioSystem.setParameters("screen_state=off");
                    if (!AudioService.this.mIsChineseZone && AudioService.this.mAudioHandler.hasMessages(14) && AudioSystem.isStreamActive(3, 3000)) {
                        AudioService.this.setCheckMusicActiveAlarm();
                    }
                } else if (!action.equals("android.intent.action.USER_PRESENT") || !AbsAudioService.SPK_RCV_STEREO_SUPPORT) {
                    if (action.equals("android.intent.action.CONFIGURATION_CHANGED")) {
                        AudioService.this.handleConfigurationChanged(context);
                    } else if (action.equals("android.intent.action.USER_SWITCHED")) {
                        if (AudioService.this.mUserSwitchedReceived) {
                            AudioService.sendMsg(AudioService.this.mAudioHandler, 15, 0, 0, 0, null, 0);
                        }
                        boolean unused9 = AudioService.this.mUserSwitchedReceived = true;
                        AudioService.this.mMediaFocusControl.discardAudioFocusOwner();
                        AudioService.this.readAudioSettings(true);
                        AudioService.sendMsg(AudioService.this.mAudioHandler, 10, 2, 0, 0, AudioService.this.mStreamStates[3], 0);
                    } else if (action.equals("android.intent.action.FM")) {
                        int state = intent.getIntExtra(AudioService.CONNECT_INTENT_KEY_STATE, 0);
                        synchronized (AudioService.this.mConnectedDevices) {
                            String device_out_fm_key = AudioService.this.makeDeviceListKey(DumpState.DUMP_DEXOPT, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                            boolean isConnected = AudioService.this.mConnectedDevices.get(device_out_fm_key) != null;
                            if (state == 0 && isConnected) {
                                AudioSystem.setDeviceConnectionState(DumpState.DUMP_DEXOPT, 0, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                                AudioService.this.mConnectedDevices.remove(device_out_fm_key);
                            } else if (1 == state && !isConnected) {
                                AudioSystem.setDeviceConnectionState(DumpState.DUMP_DEXOPT, 1, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                                AudioService.this.mConnectedDevices.put(device_out_fm_key, new DeviceListSpec(DumpState.DUMP_DEXOPT, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS));
                            }
                        }
                    } else if (action.equals("android.intent.action.USER_BACKGROUND")) {
                        int userId = intent.getIntExtra("android.intent.extra.user_handle", -1);
                        if (userId >= 0) {
                            UserInfo userInfo = UserManagerService.getInstance().getUserInfo(userId);
                            AudioService.this.onUserBackground(userId);
                            AudioService.this.killBackgroundUserProcessesWithRecordAudioPermission(userInfo);
                        }
                        UserManagerService.getInstance().setUserRestriction("no_record_audio", true, userId);
                    } else if (action.equals("android.intent.action.USER_FOREGROUND")) {
                        int userId2 = intent.getIntExtra("android.intent.extra.user_handle", -1);
                        UserManagerService.getInstance().setUserRestriction("no_record_audio", false, userId2);
                        AudioService.this.onUserForeground(userId2);
                    } else if (action.equals("android.bluetooth.adapter.action.STATE_CHANGED")) {
                        int state2 = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", -1);
                        if (state2 == 10 || state2 == 13) {
                            if (state2 != 13 || !AudioService.this.wasStreamActiveRecently(3, 0) || !AudioService.this.isA2dpDeviceConnected()) {
                                AudioService.this.disconnectAllBluetoothProfiles();
                            } else {
                                AudioService.this.disconnectHeadset();
                                AudioService.this.disconnectA2dp();
                                AudioService.this.disconnectA2dpSink();
                            }
                        }
                    } else if (action.equals("android.media.action.OPEN_AUDIO_EFFECT_CONTROL_SESSION") || action.equals("android.media.action.CLOSE_AUDIO_EFFECT_CONTROL_SESSION")) {
                        AudioService.this.handleAudioEffectBroadcast(context, intent);
                    } else if (!AudioService.this.mIsChineseZone && action.equals(AudioService.ACTION_CHECK_MUSIC_ACTIVE) && AudioSystem.isStreamActive(3, 3000)) {
                        AudioService.this.onCheckMusicActive(AudioService.this.mSafeVolumeCaller);
                        AudioService.this.setCheckMusicActiveAlarm();
                    }
                } else if (AudioService.this.mMonitorRotation) {
                    RotationHelper.updateOrientation();
                }
            }
        }
    }

    final class AudioServiceInternal extends AudioManagerInternal {
        AudioServiceInternal() {
        }

        public void setRingerModeDelegate(AudioManagerInternal.RingerModeDelegate delegate) {
            AudioManagerInternal.RingerModeDelegate unused = AudioService.this.mRingerModeDelegate = delegate;
            if (AudioService.this.mRingerModeDelegate != null) {
                synchronized (AudioService.this.mSettingsLock) {
                    boolean unused2 = AudioService.this.updateRingerAndZenModeAffectedStreams();
                }
                setRingerModeInternal(getRingerModeInternal(), "AudioService.setRingerModeDelegate");
            }
        }

        public void adjustSuggestedStreamVolumeForUid(int streamType, int direction, int flags, String callingPackage, int uid) {
            AudioService.this.adjustSuggestedStreamVolume(direction, streamType, flags, callingPackage, callingPackage, uid);
        }

        public void adjustStreamVolumeForUid(int streamType, int direction, int flags, String callingPackage, int uid) {
            AudioService.this.adjustStreamVolume(streamType, direction, flags, callingPackage, callingPackage, uid);
        }

        public void setStreamVolumeForUid(int streamType, int direction, int flags, String callingPackage, int uid) {
            AudioService.this.setStreamVolume(streamType, direction, flags, callingPackage, callingPackage, uid);
        }

        public int getRingerModeInternal() {
            return AudioService.this.getRingerModeInternal();
        }

        public void setRingerModeInternal(int ringerMode, String caller) {
            AudioService.this.setRingerModeInternal(ringerMode, caller);
        }

        public void silenceRingerModeInternal(String caller) {
            AudioService.this.silenceRingerModeInternal(caller);
        }

        public void updateRingerModeAffectedStreamsInternal() {
            synchronized (AudioService.this.mSettingsLock) {
                if (AudioService.this.updateRingerAndZenModeAffectedStreams()) {
                    AudioService.this.setRingerModeInt(getRingerModeInternal(), false);
                }
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:14:0x0031 A[LOOP:0: B:14:0x0031->B:19:0x004a, LOOP_START, PHI: r2 
          PHI: (r2v2 'i' int) = (r2v0 'i' int), (r2v3 'i' int) binds: [B:13:0x002e, B:19:0x004a] A[DONT_GENERATE, DONT_INLINE]] */
        /* JADX WARNING: Removed duplicated region for block: B:21:0x004f  */
        public void setAccessibilityServiceUids(IntArray uids) {
            boolean changed;
            synchronized (AudioService.this.mAccessibilityServiceUidsLock) {
                if (uids.size() == 0) {
                    int[] unused = AudioService.this.mAccessibilityServiceUids = null;
                } else {
                    int i = 0;
                    if (AudioService.this.mAccessibilityServiceUids != null) {
                        if (AudioService.this.mAccessibilityServiceUids.length == uids.size()) {
                            changed = false;
                            if (!changed) {
                                while (true) {
                                    if (i >= AudioService.this.mAccessibilityServiceUids.length) {
                                        break;
                                    } else if (uids.get(i) != AudioService.this.mAccessibilityServiceUids[i]) {
                                        changed = true;
                                        break;
                                    } else {
                                        i++;
                                    }
                                }
                            }
                            if (changed) {
                                int[] unused2 = AudioService.this.mAccessibilityServiceUids = uids.toArray();
                            }
                        }
                    }
                    changed = true;
                    if (!changed) {
                    }
                    if (changed) {
                    }
                }
            }
        }
    }

    private class AudioServiceUserRestrictionsListener implements UserManagerInternal.UserRestrictionsListener {
        private AudioServiceUserRestrictionsListener() {
        }

        public void onUserRestrictionsChanged(int userId, Bundle newRestrictions, Bundle prevRestrictions) {
            boolean wasRestricted = prevRestrictions.getBoolean("no_unmute_microphone");
            boolean isRestricted = newRestrictions.getBoolean("no_unmute_microphone");
            if (wasRestricted != isRestricted) {
                AudioService.this.setMicrophoneMuteNoCallerCheck(isRestricted, userId);
            }
            boolean isRestricted2 = true;
            boolean wasRestricted2 = prevRestrictions.getBoolean("no_adjust_volume") || prevRestrictions.getBoolean("disallow_unmute_device");
            if (!newRestrictions.getBoolean("no_adjust_volume") && !newRestrictions.getBoolean("disallow_unmute_device")) {
                isRestricted2 = false;
            }
            if (wasRestricted2 != isRestricted2) {
                AudioService.this.setMasterMuteInternalNoCallerCheck(isRestricted2, 0, userId);
            }
        }
    }

    private class AudioSystemThread extends Thread {
        AudioSystemThread() {
            super(AudioService.TAG);
        }

        public void run() {
            Looper.prepare();
            synchronized (AudioService.this) {
                AudioService.this.mAudioHandler = new AudioHandler();
                AudioService.this.initHwThemeHandler();
                AudioService.this.notify();
            }
            Looper.loop();
        }
    }

    private class DeviceListSpec {
        String mDeviceAddress;
        String mDeviceName;
        int mDeviceType;

        public DeviceListSpec(int deviceType, String deviceName, String deviceAddress) {
            this.mDeviceType = deviceType;
            this.mDeviceName = deviceName;
            this.mDeviceAddress = deviceAddress;
        }

        public String toString() {
            return "[type:0x" + Integer.toHexString(this.mDeviceType) + " name:" + this.mDeviceName + "]";
        }
    }

    private class ForceControlStreamClient implements IBinder.DeathRecipient {
        private IBinder mCb;

        ForceControlStreamClient(IBinder cb) {
            if (cb != null) {
                try {
                    cb.linkToDeath(this, 0);
                } catch (RemoteException e) {
                    Log.w(AudioService.TAG, "ForceControlStreamClient() could not link to " + cb + " binder death");
                    cb = null;
                }
            }
            this.mCb = cb;
        }

        public void binderDied() {
            synchronized (AudioService.this.mForceControlStreamLock) {
                Log.w(AudioService.TAG, "SCO client died");
                if (AudioService.this.mForceControlStreamClient != this) {
                    Log.w(AudioService.TAG, "unregistered control stream client died");
                } else {
                    ForceControlStreamClient unused = AudioService.this.mForceControlStreamClient = null;
                    int unused2 = AudioService.this.mVolumeControlStream = -1;
                    boolean unused3 = AudioService.this.mUserSelectedVolumeControlStream = false;
                }
            }
        }

        public void release() {
            if (this.mCb != null) {
                this.mCb.unlinkToDeath(this, 0);
                this.mCb = null;
            }
        }

        public IBinder getBinder() {
            return this.mCb;
        }
    }

    public class HwInnerAudioService extends IHwAudioServiceManager.Stub {
        private AudioService mAudioService;

        HwInnerAudioService(AudioService as) {
            this.mAudioService = as;
        }

        public int setSoundEffectState(boolean restore, String packageName, boolean isOnTop, String reserved) {
            int res = AudioService.this.mHwAudioServiceEx.setSoundEffectState(restore, packageName, isOnTop, reserved);
            AudioService.this.mHwAudioServiceEx.hideHiResIconDueKilledAPP(restore, packageName);
            return res;
        }

        public boolean checkRecordActive() {
            return AudioService.this.mHwAudioServiceEx.checkRecordActive(Binder.getCallingPid());
        }

        public void checkMicMute() {
            this.mAudioService.checkMicMute();
        }

        public void sendRecordStateChangedIntent(String sender, int state, int pid, String packageName) {
            AudioService.this.mHwAudioServiceEx.sendAudioRecordStateChangedIntent(sender, state, pid, packageName);
        }

        public int getRecordConcurrentType(String packageName) {
            return AudioService.this.mHwAudioServiceEx.getRecordConcurrentType(packageName);
        }

        public void registerAudioModeCallback(IAudioModeDispatcher modeCb) {
            AudioService.this.mHwAudioServiceEx.registerAudioModeCallback(modeCb);
        }

        public void unregisterAudioModeCallback(IAudioModeDispatcher modeCb) {
            AudioService.this.mHwAudioServiceEx.unregisterAudioModeCallback(modeCb);
        }
    }

    public static final class Lifecycle extends SystemService {
        private AudioService mService;

        public Lifecycle(Context context) {
            super(context);
            HwServiceFactory.IHwAudioService audioService = HwServiceFactory.getHwAudioService();
            if (audioService != null) {
                this.mService = audioService.getInstance(context);
            } else {
                this.mService = new AudioService(context);
            }
        }

        /* JADX WARNING: type inference failed for: r1v0, types: [com.android.server.audio.AudioService, android.os.IBinder] */
        public void onStart() {
            publishBinderService("audio", this.mService);
        }

        public void onBootPhase(int phase) {
            if (phase == 550) {
                this.mService.systemReady();
            }
        }
    }

    class LoadSoundEffectReply {
        public int mStatus = 1;

        LoadSoundEffectReply() {
        }
    }

    private class MyDisplayStatusCallback implements HdmiPlaybackClient.DisplayStatusCallback {
        private MyDisplayStatusCallback() {
        }

        public void onComplete(int status) {
            if (AudioService.this.mHdmiManager != null) {
                synchronized (AudioService.this.mHdmiManager) {
                    boolean unused = AudioService.this.mHdmiCecSink = status != -1;
                    if (AudioService.this.isPlatformTelevision() && !AudioService.this.mHdmiCecSink) {
                        AudioService.this.mFixedVolumeDevices &= -1025;
                    }
                    AudioService.this.checkAllFixedVolumeDevices();
                }
            }
        }
    }

    private class RmtSbmxFullVolDeathHandler implements IBinder.DeathRecipient {
        private IBinder mICallback;

        RmtSbmxFullVolDeathHandler(IBinder cb) {
            this.mICallback = cb;
            try {
                cb.linkToDeath(this, 0);
            } catch (RemoteException e) {
                Log.e(AudioService.TAG, "can't link to death", e);
            }
        }

        /* access modifiers changed from: package-private */
        public boolean isHandlerFor(IBinder cb) {
            return this.mICallback.equals(cb);
        }

        /* access modifiers changed from: package-private */
        public void forget() {
            try {
                this.mICallback.unlinkToDeath(this, 0);
            } catch (NoSuchElementException e) {
                Log.e(AudioService.TAG, "error unlinking to death", e);
            }
        }

        public void binderDied() {
            Log.w(AudioService.TAG, "Recorder with remote submix at full volume died " + this.mICallback);
            AudioService.this.forceRemoteSubmixFullVolume(false, this.mICallback);
        }
    }

    private class ScoClient implements IBinder.DeathRecipient {
        private IBinder mCb;
        private int mCreatorPid = Binder.getCallingPid();
        private int mStartcount = 0;

        ScoClient(IBinder cb) {
            this.mCb = cb;
        }

        public void binderDied() {
            synchronized (AudioService.this.mScoClients) {
                Log.w(AudioService.TAG, "SCO client died");
                if (AudioService.this.mScoClients.indexOf(this) < 0) {
                    Log.w(AudioService.TAG, "unregistered SCO client died");
                } else {
                    clearCount(true);
                    AudioService.this.mScoClients.remove(this);
                }
            }
        }

        public void incCount(int scoAudioMode) {
            synchronized (AudioService.this.mScoClients) {
                requestScoState(12, scoAudioMode);
                if (this.mStartcount == 0) {
                    try {
                        this.mCb.linkToDeath(this, 0);
                    } catch (RemoteException e) {
                        Log.w(AudioService.TAG, "ScoClient  incCount() could not link to " + this.mCb + " binder death");
                    }
                }
                this.mStartcount++;
                if (this.mStartcount > 1) {
                    AudioService.this.onScoExceptionOccur(getPid());
                    this.mStartcount = 1;
                }
            }
        }

        public void decCount() {
            synchronized (AudioService.this.mScoClients) {
                if (this.mStartcount == 0) {
                    Log.w(AudioService.TAG, "ScoClient.decCount() already 0");
                } else {
                    this.mStartcount--;
                    if (this.mStartcount == 0) {
                        try {
                            this.mCb.unlinkToDeath(this, 0);
                        } catch (NoSuchElementException e) {
                            Log.w(AudioService.TAG, "decCount() going to 0 but not registered to binder");
                        }
                    }
                    requestScoState(10, 0);
                }
            }
        }

        public void clearCount(boolean stopSco) {
            synchronized (AudioService.this.mScoClients) {
                if (this.mStartcount != 0) {
                    try {
                        this.mCb.unlinkToDeath(this, 0);
                    } catch (NoSuchElementException e) {
                        Log.w(AudioService.TAG, "clearCount() mStartcount: " + this.mStartcount + " != 0 but not registered to binder");
                    }
                }
                this.mStartcount = 0;
                if (stopSco) {
                    requestScoState(10, 0);
                }
            }
        }

        public int getCount() {
            return this.mStartcount;
        }

        public IBinder getBinder() {
            return this.mCb;
        }

        public int getPid() {
            return this.mCreatorPid;
        }

        public int totalCount() {
            int count;
            synchronized (AudioService.this.mScoClients) {
                count = 0;
                Iterator it = AudioService.this.mScoClients.iterator();
                while (it.hasNext()) {
                    count += ((ScoClient) it.next()).getCount();
                }
            }
            return count;
        }

        private void requestScoState(int state, int scoAudioMode) {
            AudioService.this.checkScoAudioState();
            int clientCount = totalCount();
            if (clientCount != 0) {
                Log.i(AudioService.TAG, "requestScoState: state=" + state + ", scoAudioMode=" + scoAudioMode + ", clientCount=" + clientCount);
                return;
            }
            if (state == 12) {
                AudioService.this.broadcastScoConnectionState(2);
                synchronized (AudioService.this.mSetModeDeathHandlers) {
                    int modeOwnerPid = AudioService.this.mSetModeDeathHandlers.isEmpty() ? 0 : AudioService.this.mSetModeDeathHandlers.get(0).getPid();
                    if (modeOwnerPid == 0 || modeOwnerPid == this.mCreatorPid) {
                        int access$2500 = AudioService.this.mScoAudioState;
                        if (access$2500 != 0) {
                            switch (access$2500) {
                                case 4:
                                    int unused = AudioService.this.mScoAudioState = 3;
                                    AudioService.this.broadcastScoConnectionState(1);
                                    break;
                                case 5:
                                    int unused2 = AudioService.this.mScoAudioState = 1;
                                    break;
                                default:
                                    Log.w(AudioService.TAG, "requestScoState: failed to connect in state " + AudioService.this.mScoAudioState + ", scoAudioMode=" + scoAudioMode);
                                    AudioService.this.broadcastScoConnectionState(0);
                                    break;
                            }
                        } else {
                            int unused3 = AudioService.this.mScoAudioMode = scoAudioMode;
                            if (scoAudioMode == -1) {
                                int unused4 = AudioService.this.mScoAudioMode = 0;
                                if (AudioService.this.mBluetoothHeadsetDevice != null) {
                                    AudioService audioService = AudioService.this;
                                    ContentResolver access$2800 = AudioService.this.mContentResolver;
                                    int unused5 = audioService.mScoAudioMode = Settings.Global.getInt(access$2800, "bluetooth_sco_channel_" + AudioService.this.mBluetoothHeadsetDevice.getAddress(), 0);
                                    if (AudioService.this.mScoAudioMode > 2 || AudioService.this.mScoAudioMode < 0) {
                                        int unused6 = AudioService.this.mScoAudioMode = 0;
                                    }
                                }
                            }
                            if (AudioService.this.mBluetoothHeadset == null) {
                                if (AudioService.this.getBluetoothHeadset()) {
                                    int unused7 = AudioService.this.mScoAudioState = 1;
                                } else {
                                    Log.w(AudioService.TAG, "requestScoState: getBluetoothHeadset failed during connection, mScoAudioMode=" + AudioService.this.mScoAudioMode);
                                    AudioService.this.broadcastScoConnectionState(0);
                                }
                            } else if (AudioService.this.mBluetoothHeadsetDevice == null) {
                                Log.w(AudioService.TAG, "requestScoState: no active device while connecting, mScoAudioMode=" + AudioService.this.mScoAudioMode);
                                AudioService.this.broadcastScoConnectionState(0);
                            } else if (AudioService.connectBluetoothScoAudioHelper(AudioService.this.mBluetoothHeadset, AudioService.this.mBluetoothHeadsetDevice, AudioService.this.mScoAudioMode)) {
                                int unused8 = AudioService.this.mScoAudioState = 3;
                            } else {
                                Log.w(AudioService.TAG, "requestScoState: connect to " + AudioService.this.mBluetoothHeadsetDevice + " failed, mScoAudioMode=" + AudioService.this.mScoAudioMode);
                                AudioService.this.broadcastScoConnectionState(0);
                            }
                        }
                    } else {
                        Log.w(AudioService.TAG, "requestScoState: audio mode is not NORMAL and modeOwnerPid " + modeOwnerPid + " != creatorPid " + this.mCreatorPid);
                        AudioService.this.broadcastScoConnectionState(0);
                    }
                }
            } else if (state == 10) {
                int access$25002 = AudioService.this.mScoAudioState;
                if (access$25002 == 1) {
                    int unused9 = AudioService.this.mScoAudioState = 0;
                    AudioService.this.broadcastScoConnectionState(0);
                } else if (access$25002 != 3) {
                    Log.w(AudioService.TAG, "requestScoState: failed to disconnect in state " + AudioService.this.mScoAudioState + ", scoAudioMode=" + scoAudioMode);
                    AudioService.this.broadcastScoConnectionState(0);
                } else if (AudioService.this.mBluetoothHeadset == null) {
                    if (AudioService.this.getBluetoothHeadset()) {
                        int unused10 = AudioService.this.mScoAudioState = 4;
                    } else {
                        Log.w(AudioService.TAG, "requestScoState: getBluetoothHeadset failed during disconnection, mScoAudioMode=" + AudioService.this.mScoAudioMode);
                        int unused11 = AudioService.this.mScoAudioState = 0;
                        AudioService.this.broadcastScoConnectionState(0);
                    }
                } else if (AudioService.this.mBluetoothHeadsetDevice == null) {
                    int unused12 = AudioService.this.mScoAudioState = 0;
                    AudioService.this.broadcastScoConnectionState(0);
                } else if (AudioService.disconnectBluetoothScoAudioHelper(AudioService.this.mBluetoothHeadset, AudioService.this.mBluetoothHeadsetDevice, AudioService.this.mScoAudioMode)) {
                    int unused13 = AudioService.this.mScoAudioState = 5;
                } else {
                    int unused14 = AudioService.this.mScoAudioState = 0;
                    AudioService.this.broadcastScoConnectionState(0);
                }
            }
        }
    }

    protected class SetModeDeathHandler implements IBinder.DeathRecipient {
        private IBinder mCb;
        private int mMode = 0;
        /* access modifiers changed from: private */
        public int mPid;

        SetModeDeathHandler(IBinder cb, int pid) {
            this.mCb = cb;
            this.mPid = pid;
        }

        public void binderDied() {
            int oldModeOwnerPid = 0;
            int newModeOwnerPid = 0;
            synchronized (AudioService.this.mSetModeDeathHandlers) {
                Log.w(AudioService.TAG, "setMode() client died");
                if (!AudioService.this.mSetModeDeathHandlers.isEmpty()) {
                    oldModeOwnerPid = AudioService.this.mSetModeDeathHandlers.get(0).getPid();
                }
                if (AudioService.this.mSetModeDeathHandlers.indexOf(this) < 0) {
                    Log.w(AudioService.TAG, "unregistered setMode() client died");
                } else {
                    newModeOwnerPid = AudioService.this.setModeInt(0, this.mCb, this.mPid, AudioService.TAG);
                }
            }
            if (newModeOwnerPid != oldModeOwnerPid && newModeOwnerPid != 0) {
                long ident = Binder.clearCallingIdentity();
                AudioService.this.disconnectBluetoothSco(newModeOwnerPid);
                Binder.restoreCallingIdentity(ident);
            }
        }

        public int getPid() {
            return this.mPid;
        }

        public void setMode(int mode) {
            this.mMode = mode;
        }

        public int getMode() {
            return this.mMode;
        }

        public IBinder getBinder() {
            return this.mCb;
        }

        public void dump(PrintWriter pw) {
            pw.println("Mode=" + this.mMode + "; Pid=" + this.mPid + "; PkgName=" + AudioService.this.getPackageNameByPid(this.mPid));
        }
    }

    private class SettingsObserver extends ContentObserver {
        SettingsObserver() {
            super(new Handler());
            AudioService.this.mContentResolver.registerContentObserver(Settings.Global.getUriFor("zen_mode"), false, this);
            AudioService.this.mContentResolver.registerContentObserver(Settings.Global.getUriFor("zen_mode_config_etag"), false, this);
            AudioService.this.mContentResolver.registerContentObserver(Settings.System.getUriFor("mode_ringer_streams_affected"), false, this);
            AudioService.this.mContentResolver.registerContentObserver(Settings.Global.getUriFor("dock_audio_media_enabled"), false, this);
            AudioService.this.mContentResolver.registerContentObserver(Settings.System.getUriFor("master_mono"), false, this);
            int unused = AudioService.this.mEncodedSurroundMode = Settings.Global.getInt(AudioService.this.mContentResolver, "encoded_surround_output", 0);
            AudioService.this.mContentResolver.registerContentObserver(Settings.Global.getUriFor("encoded_surround_output"), false, this);
            String unused2 = AudioService.this.mEnabledSurroundFormats = Settings.Global.getString(AudioService.this.mContentResolver, "encoded_surround_output_enabled_formats");
            AudioService.this.mContentResolver.registerContentObserver(Settings.Global.getUriFor("encoded_surround_output_enabled_formats"), false, this);
            AudioService.this.mContentResolver.registerContentObserver(Settings.System.getUriFor(AudioService.DEFAULT_VOLUME_KEY_CTL), false, this, -1);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            synchronized (AudioService.this.mSettingsLock) {
                if (AudioService.this.updateRingerAndZenModeAffectedStreams()) {
                    AudioService.this.setRingerModeInt(AudioService.this.getRingerModeInternal(), false);
                }
                AudioService.this.readDockAudioSettings(AudioService.this.mContentResolver);
                AudioService.this.updateMasterMono(AudioService.this.mContentResolver);
                updateEncodedSurroundOutput();
                AudioService.this.sendEnabledSurroundFormats(AudioService.this.mContentResolver, AudioService.this.mSurroundModeChanged);
                AudioService.this.updateDefaultStream();
            }
        }

        private void updateEncodedSurroundOutput() {
            int newSurroundMode = Settings.Global.getInt(AudioService.this.mContentResolver, "encoded_surround_output", 0);
            if (AudioService.this.mEncodedSurroundMode != newSurroundMode) {
                AudioService.this.sendEncodedSurroundMode(newSurroundMode, "SettingsObserver");
                synchronized (AudioService.this.mConnectedDevices) {
                    if (AudioService.this.mConnectedDevices.get(AudioService.this.makeDeviceListKey(1024, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS)) != null) {
                        AudioService.this.setWiredDeviceConnectionState(1024, 0, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, PackageManagerService.PLATFORM_PACKAGE_NAME);
                        AudioService.this.setWiredDeviceConnectionState(1024, 1, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, PackageManagerService.PLATFORM_PACKAGE_NAME);
                    }
                }
                int unused = AudioService.this.mEncodedSurroundMode = newSurroundMode;
                boolean unused2 = AudioService.this.mSurroundModeChanged = true;
                return;
            }
            boolean unused3 = AudioService.this.mSurroundModeChanged = false;
        }
    }

    private final class SoundPoolCallback implements SoundPool.OnLoadCompleteListener {
        List<Integer> mSamples;
        int mStatus;

        private SoundPoolCallback() {
            this.mStatus = 1;
            this.mSamples = new ArrayList();
        }

        public int status() {
            return this.mStatus;
        }

        public void setSamples(int[] samples) {
            for (int i = 0; i < samples.length; i++) {
                if (samples[i] > 0) {
                    this.mSamples.add(Integer.valueOf(samples[i]));
                }
            }
        }

        public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
            synchronized (AudioService.this.mSoundEffectsLock) {
                int i = this.mSamples.indexOf(Integer.valueOf(sampleId));
                if (i >= 0) {
                    this.mSamples.remove(i);
                }
                if (status != 0 || this.mSamples.isEmpty()) {
                    this.mStatus = status;
                    AudioService.this.mSoundEffectsLock.notify();
                }
            }
        }
    }

    class SoundPoolListenerThread extends Thread {
        public SoundPoolListenerThread() {
            super("SoundPoolListenerThread");
        }

        public void run() {
            Looper.prepare();
            Looper unused = AudioService.this.mSoundPoolLooper = Looper.myLooper();
            synchronized (AudioService.this.mSoundEffectsLock) {
                if (AudioService.this.mSoundPool != null) {
                    SoundPoolCallback unused2 = AudioService.this.mSoundPoolCallBack = new SoundPoolCallback();
                    AudioService.this.mSoundPool.setOnLoadCompleteListener(AudioService.this.mSoundPoolCallBack);
                }
                AudioService.this.mSoundEffectsLock.notify();
            }
            Looper.loop();
        }
    }

    class StreamVolumeCommand {
        public final int mDevice;
        public final int mFlags;
        public final int mIndex;
        public final int mStreamType;

        StreamVolumeCommand(int streamType, int index, int flags, int device) {
            this.mStreamType = streamType;
            this.mIndex = index;
            this.mFlags = flags;
            this.mDevice = device;
        }

        public String toString() {
            return "{streamType=" + this.mStreamType + ",index=" + this.mIndex + ",flags=" + this.mFlags + ",device=" + this.mDevice + '}';
        }
    }

    public static class VolumeController {
        private static final String TAG = "VolumeController";
        private IVolumeController mController;
        private int mLongPressTimeout;
        private long mNextLongPress;
        private IVRSystemServiceManager mVRManager;
        private boolean mVisible;
        private IVrManager mVrManager;

        public void setController(IVolumeController controller) {
            this.mController = controller;
            this.mVisible = false;
            this.mVRManager = HwFrameworkFactory.getVRSystemServiceManager();
        }

        public void loadSettings(ContentResolver cr) {
            this.mLongPressTimeout = Settings.Secure.getIntForUser(cr, "long_press_timeout", 500, -2);
        }

        public boolean suppressAdjustment(int resolvedStream, int flags, boolean isMute) {
            if (isMute) {
                return false;
            }
            boolean suppress = false;
            if (resolvedStream == 2 && this.mController != null) {
                long now = SystemClock.uptimeMillis();
                if ((flags & 1) != 0 && !this.mVisible) {
                    if (this.mNextLongPress < now) {
                        this.mNextLongPress = ((long) this.mLongPressTimeout) + now;
                    }
                    suppress = true;
                } else if (this.mNextLongPress > 0) {
                    if (now > this.mNextLongPress) {
                        this.mNextLongPress = 0;
                    } else {
                        suppress = true;
                    }
                }
            }
            return suppress;
        }

        public void setVisible(boolean visible) {
            this.mVisible = visible;
        }

        public boolean isSameBinder(IVolumeController controller) {
            return Objects.equals(asBinder(), binder(controller));
        }

        public IBinder asBinder() {
            return binder(this.mController);
        }

        private static IBinder binder(IVolumeController controller) {
            if (controller == null) {
                return null;
            }
            return controller.asBinder();
        }

        public String toString() {
            return "VolumeController(" + asBinder() + ",mVisible=" + this.mVisible + ")";
        }

        private boolean isVRConnect() {
            return this.mVRManager != null && this.mVRManager.isVRDeviceConnected() && !this.mVRManager.isVirtualScreenMode();
        }

        public void postDisplaySafeVolumeWarning(int flags) {
            this.mVrManager = IVrManager.Stub.asInterface(ServiceManager.getService("vrmanager"));
            try {
                if (this.mVrManager != null && this.mVrManager.getVrModeState()) {
                    return;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "checkSafeMediaVolume cannot get VR mode");
            } catch (SecurityException e2) {
                Log.e(TAG, "checkSafeMediaVolume cannot get android.permission.ACCESS_VR_MANAGER, android.permission.ACCESS_VR_STATE");
            }
            if (this.mController != null && !isVRConnect()) {
                try {
                    this.mController.displaySafeVolumeWarning(flags | 1);
                } catch (RemoteException e3) {
                    Log.w(TAG, "Error calling displaySafeVolumeWarning", e3);
                }
            }
        }

        public void postVolumeChanged(int streamType, int flags) {
            if (this.mController != null && !isVRConnect()) {
                try {
                    this.mController.volumeChanged(streamType, flags);
                } catch (RemoteException e) {
                    Log.w(TAG, "Error calling volumeChanged", e);
                } catch (NullPointerException e2) {
                    Log.e(TAG, "Error Controller is Null");
                }
            }
        }

        public void postMasterMuteChanged(int flags) {
            if (this.mController != null && !isVRConnect()) {
                try {
                    this.mController.masterMuteChanged(flags);
                } catch (RemoteException e) {
                    Log.w(TAG, "Error calling masterMuteChanged", e);
                }
            }
        }

        public void setLayoutDirection(int layoutDirection) {
            if (this.mController != null && !isVRConnect()) {
                try {
                    this.mController.setLayoutDirection(layoutDirection);
                } catch (RemoteException e) {
                    Log.w(TAG, "Error calling setLayoutDirection", e);
                }
            }
        }

        public void postDismiss() {
            if (this.mController != null) {
                try {
                    this.mController.dismiss();
                } catch (RemoteException e) {
                    Log.w(TAG, "Error calling dismiss", e);
                }
            }
        }

        public void setA11yMode(int a11yMode) {
            if (this.mController != null) {
                try {
                    this.mController.setA11yMode(a11yMode);
                } catch (RemoteException e) {
                    Log.w(TAG, "Error calling setA11Mode", e);
                }
            }
        }
    }

    public class VolumeStreamState {
        /* access modifiers changed from: private */
        public final SparseIntArray mIndexMap;
        /* access modifiers changed from: private */
        public int mIndexMax;
        /* access modifiers changed from: private */
        public int mIndexMin;
        /* access modifiers changed from: private */
        public boolean mIsMuted;
        private int mObservedDevices;
        private final Intent mStreamDevicesChanged;
        /* access modifiers changed from: private */
        public final int mStreamType;
        private HwCustAudioServiceVolumeStreamState mVSSCust;
        private final Intent mVolumeChanged;
        /* access modifiers changed from: private */
        public String mVolumeIndexSettingName;

        private VolumeStreamState(String settingName, int streamType) {
            this.mIndexMap = new SparseIntArray(8);
            this.mVSSCust = null;
            this.mVolumeIndexSettingName = settingName;
            this.mStreamType = streamType;
            this.mIndexMin = AudioService.MIN_STREAM_VOLUME[streamType] * 10;
            this.mIndexMax = AudioService.MAX_STREAM_VOLUME[streamType] * 10;
            AudioSystem.initStreamVolume(streamType, this.mIndexMin / 10, this.mIndexMax / 10);
            this.mVSSCust = (HwCustAudioServiceVolumeStreamState) HwCustUtils.createObj(HwCustAudioServiceVolumeStreamState.class, new Object[]{AudioService.this.mContext});
            readSettings();
            this.mVolumeChanged = new Intent("android.media.VOLUME_CHANGED_ACTION");
            this.mVolumeChanged.putExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", this.mStreamType);
            this.mStreamDevicesChanged = new Intent("android.media.STREAM_DEVICES_CHANGED_ACTION");
            this.mStreamDevicesChanged.putExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", this.mStreamType);
        }

        public int observeDevicesForStream_syncVSS(boolean checkOthers) {
            int devices = AudioSystem.getDevicesForStream(this.mStreamType);
            if (devices == this.mObservedDevices) {
                return devices;
            }
            int prevDevices = this.mObservedDevices;
            this.mObservedDevices = devices;
            if (checkOthers) {
                AudioService.this.observeDevicesForStreams(this.mStreamType);
            }
            if (AudioService.mStreamVolumeAlias[this.mStreamType] == this.mStreamType) {
                EventLogTags.writeStreamDevicesChanged(this.mStreamType, prevDevices, devices);
            }
            AudioService.this.sendBroadcastToAll(this.mStreamDevicesChanged.putExtra("android.media.EXTRA_PREV_VOLUME_STREAM_DEVICES", prevDevices).putExtra("android.media.EXTRA_VOLUME_STREAM_DEVICES", devices));
            return devices;
        }

        public String getSettingNameForDevice(int device) {
            if (!hasValidSettingsName()) {
                return null;
            }
            String suffix = AudioSystem.getOutputDeviceName(device);
            if (suffix.isEmpty()) {
                return this.mVolumeIndexSettingName;
            }
            return this.mVolumeIndexSettingName + "_" + suffix;
        }

        /* access modifiers changed from: private */
        public boolean hasValidSettingsName() {
            return this.mVolumeIndexSettingName != null && !this.mVolumeIndexSettingName.isEmpty();
        }

        /* JADX WARNING: Code restructure failed: missing block: B:22:0x002d, code lost:
            r1 = com.android.server.audio.AudioService.VolumeStreamState.class;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:23:0x002f, code lost:
            monitor-enter(r1);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:24:0x0030, code lost:
            r0 = 1879048191;
            r2 = 0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:25:0x0034, code lost:
            if (r0 == 0) goto L_0x0084;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:26:0x0036, code lost:
            r6 = 1 << r2;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:27:0x003a, code lost:
            if ((r6 & r0) != 0) goto L_0x003d;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:28:0x003d, code lost:
            r0 = r0 & (~r6);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:29:0x0040, code lost:
            if (r6 == 1073741824) goto L_0x004a;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:32:0x0045, code lost:
            if (r12.mStreamType != 3) goto L_0x0048;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:34:0x0048, code lost:
            r8 = -1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:35:0x004a, code lost:
            r8 = android.media.AudioSystem.DEFAULT_STREAM_VOLUME[r12.mStreamType];
         */
        /* JADX WARNING: Code restructure failed: missing block: B:37:0x0054, code lost:
            if (hasValidSettingsName() != false) goto L_0x0058;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:38:0x0056, code lost:
            r9 = r8;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:39:0x0058, code lost:
            r9 = android.provider.Settings.System.getIntForUser(com.android.server.audio.AudioService.access$2800(r12.this$0), getSettingNameForDevice(r6), r8, -2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:40:0x0068, code lost:
            if (r9 != -1) goto L_0x0076;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:42:0x006c, code lost:
            if (r12.mVSSCust == null) goto L_0x0081;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:43:0x006e, code lost:
            r12.mVSSCust.readSettings(r12.mStreamType, r6);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:44:0x0076, code lost:
            r12.mIndexMap.put(r6, getValidIndex(10 * r9));
         */
        /* JADX WARNING: Code restructure failed: missing block: B:45:0x0081, code lost:
            r2 = r2 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:46:0x0084, code lost:
            monitor-exit(r1);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:47:0x0085, code lost:
            return;
         */
        public void readSettings() {
            synchronized (AudioService.this.mSettingsLock) {
                synchronized (VolumeStreamState.class) {
                    if (AudioService.this.mUseFixedVolume) {
                        this.mIndexMap.put(1073741824, this.mIndexMax);
                        return;
                    }
                    if (this.mStreamType != 1) {
                        if (this.mStreamType == 7) {
                        }
                    }
                    int index = 10 * AudioSystem.DEFAULT_STREAM_VOLUME[this.mStreamType];
                    if (AudioService.this.mCameraSoundForced) {
                        index = this.mIndexMax;
                    }
                    this.mIndexMap.put(1073741824, index);
                }
            }
        }

        private int getAbsoluteVolumeIndex(int index) {
            if (index == 0) {
                return 0;
            }
            if (index == 1) {
                return ((int) (((double) this.mIndexMax) * 0.5d)) / 10;
            }
            if (index == 2) {
                return ((int) (((double) this.mIndexMax) * 0.7d)) / 10;
            }
            if (index == 3) {
                return ((int) (((double) this.mIndexMax) * 0.85d)) / 10;
            }
            return (this.mIndexMax + 5) / 10;
        }

        public void applyDeviceVolume_syncVSS(int device) {
            int index;
            boolean isTurnOff = false;
            if (this.mVSSCust != null) {
                isTurnOff = this.mVSSCust.isTurnOffAllSound();
            }
            if (this.mIsMuted || isTurnOff) {
                index = 0;
            } else if ((device & 896) != 0 && AudioService.this.mAvrcpAbsVolSupported) {
                index = getAbsoluteVolumeIndex((getIndex(device) + 5) / 10);
            } else if ((AudioService.this.mFullVolumeDevices & device) != 0) {
                index = (this.mIndexMax + 5) / 10;
            } else {
                index = (134217728 & device) != 0 ? (this.mIndexMax + 5) / 10 : (getIndex(device) + 5) / 10;
            }
            AudioSystem.setStreamVolumeIndex(this.mStreamType, index, device);
        }

        /* JADX WARNING: Removed duplicated region for block: B:39:0x00d5  */
        public void applyAllVolumes() {
            int index;
            int index2;
            synchronized (VolumeStreamState.class) {
                boolean isTurnOff = false;
                if (this.mVSSCust != null) {
                    isTurnOff = this.mVSSCust.isTurnOffAllSound();
                }
                for (int i = 0; i < this.mIndexMap.size(); i++) {
                    int device = this.mIndexMap.keyAt(i);
                    if (device != 1073741824) {
                        if (!this.mIsMuted) {
                            if (!isTurnOff) {
                                if ((device & 896) == 0 || !AudioService.this.mAvrcpAbsVolSupported) {
                                    if ((AudioService.this.mFullVolumeDevices & device) != 0) {
                                        index2 = (this.mIndexMax + 5) / 10;
                                    } else {
                                        index2 = (134217728 & device) != 0 ? (this.mIndexMax + 5) / 10 : (this.mIndexMap.valueAt(i) + 5) / 10;
                                    }
                                    Log.i(AudioService.TAG, "all_setStreamVolumeIndex: " + this.mStreamType + " index: " + index2 + " device: " + device);
                                    AudioSystem.setStreamVolumeIndex(this.mStreamType, index2, device);
                                } else {
                                    index2 = getAbsoluteVolumeIndex((getIndex(device) + 5) / 10);
                                    Log.i(AudioService.TAG, "all_setStreamVolumeIndex: " + this.mStreamType + " index: " + index2 + " device: " + device);
                                    AudioSystem.setStreamVolumeIndex(this.mStreamType, index2, device);
                                }
                            }
                        }
                        index2 = 0;
                        Log.i(AudioService.TAG, "all_setStreamVolumeIndex: " + this.mStreamType + " index: " + index2 + " device: " + device);
                        AudioSystem.setStreamVolumeIndex(this.mStreamType, index2, device);
                    }
                }
                if (this.mIsMuted == 0) {
                    if (!isTurnOff) {
                        index = (getIndex(1073741824) + 5) / 10;
                        Log.i(AudioService.TAG, "defalut_setStreamVolumeIndex: " + this.mStreamType + " index: " + index);
                        AudioSystem.setStreamVolumeIndex(this.mStreamType, index, 1073741824);
                        if (this.mVSSCust != null) {
                            this.mVSSCust.applyAllVolumes(this.mIsMuted, this.mStreamType);
                        }
                    }
                }
                index = 0;
                Log.i(AudioService.TAG, "defalut_setStreamVolumeIndex: " + this.mStreamType + " index: " + index);
                AudioSystem.setStreamVolumeIndex(this.mStreamType, index, 1073741824);
                if (this.mVSSCust != null) {
                }
            }
        }

        public boolean adjustIndex(int deltaIndex, int device, String caller) {
            return setIndex(getIndex(device) + deltaIndex, device, caller);
        }

        public boolean setIndex(int index, int device, String caller) {
            int oldIndex;
            int index2;
            boolean changed;
            boolean changed2;
            synchronized (AudioService.this.mSettingsLock) {
                synchronized (VolumeStreamState.class) {
                    oldIndex = getIndex(device);
                    index2 = getValidIndex(index);
                    if (this.mStreamType == 7 && AudioService.this.mCameraSoundForced) {
                        index2 = this.mIndexMax;
                    }
                    this.mIndexMap.put(device, index2);
                    boolean isCurrentDevice = true;
                    changed = oldIndex != index2;
                    if (device != AudioService.this.getDeviceForStream(this.mStreamType)) {
                        isCurrentDevice = false;
                    }
                    for (int streamType = AudioSystem.getNumStreamTypes() - 1; streamType >= 0; streamType--) {
                        VolumeStreamState aliasStreamState = AudioService.this.mStreamStates[streamType];
                        if (streamType != this.mStreamType && AudioService.mStreamVolumeAlias[streamType] == this.mStreamType && (changed || !aliasStreamState.hasIndexForDevice(device))) {
                            int scaledIndex = AudioService.this.rescaleIndex(index2, this.mStreamType, streamType);
                            aliasStreamState.setIndex(scaledIndex, device, caller);
                            if (isCurrentDevice) {
                                aliasStreamState.setIndex(scaledIndex, AudioService.this.getDeviceForStream(streamType), caller);
                            }
                        }
                    }
                    if (changed && this.mStreamType == 2 && device == 2) {
                        for (int i = 0; i < this.mIndexMap.size(); i++) {
                            int otherDevice = this.mIndexMap.keyAt(i);
                            if ((otherDevice & 112) != 0) {
                                this.mIndexMap.put(otherDevice, index2);
                            }
                        }
                    }
                }
                changed2 = changed;
            }
            if (changed2) {
                int oldIndex2 = (oldIndex + 5) / 10;
                int index3 = (index2 + 5) / 10;
                if (AudioService.mStreamVolumeAlias[this.mStreamType] == this.mStreamType) {
                    if (caller == null) {
                        Log.w(AudioService.TAG, "No caller for volume_changed event", new Throwable());
                    }
                    EventLogTags.writeVolumeChanged(this.mStreamType, oldIndex2, index3, this.mIndexMax / 10, caller);
                }
                this.mVolumeChanged.putExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", index3);
                this.mVolumeChanged.putExtra("android.media.EXTRA_PREV_VOLUME_STREAM_VALUE", oldIndex2);
                this.mVolumeChanged.putExtra("android.media.EXTRA_VOLUME_STREAM_TYPE_ALIAS", AudioService.mStreamVolumeAlias[this.mStreamType]);
                this.mVolumeChanged.putExtra("DEVICE_TYPE", device);
                AudioService.this.sendBroadcastToAll(this.mVolumeChanged);
            }
            return changed2;
        }

        public int getIndex(int device) {
            int index;
            synchronized (VolumeStreamState.class) {
                index = this.mIndexMap.get(device, -1);
                if (index == -1) {
                    index = this.mIndexMap.get(1073741824);
                }
            }
            return index;
        }

        public boolean hasIndexForDevice(int device) {
            boolean z;
            synchronized (VolumeStreamState.class) {
                z = this.mIndexMap.get(device, -1) != -1;
            }
            return z;
        }

        public int getMaxIndex() {
            return this.mIndexMax;
        }

        public int getMinIndex() {
            return this.mIndexMin;
        }

        @GuardedBy("VolumeStreamState.class")
        public void refreshRange(int sourceStreamType) {
            this.mIndexMin = AudioService.MIN_STREAM_VOLUME[sourceStreamType] * 10;
            this.mIndexMax = AudioService.MAX_STREAM_VOLUME[sourceStreamType] * 10;
            for (int i = 0; i < this.mIndexMap.size(); i++) {
                this.mIndexMap.put(this.mIndexMap.keyAt(i), getValidIndex(this.mIndexMap.valueAt(i)));
            }
        }

        @GuardedBy("VolumeStreamState.class")
        public void setAllIndexes(VolumeStreamState srcStream, String caller) {
            if (this.mStreamType != srcStream.mStreamType) {
                int srcStreamType = srcStream.getStreamType();
                int index = AudioService.this.rescaleIndex(srcStream.getIndex(1073741824), srcStreamType, this.mStreamType);
                for (int i = 0; i < this.mIndexMap.size(); i++) {
                    this.mIndexMap.put(this.mIndexMap.keyAt(i), index);
                }
                SparseIntArray srcMap = srcStream.mIndexMap;
                for (int i2 = 0; i2 < srcMap.size(); i2++) {
                    setIndex(AudioService.this.rescaleIndex(srcMap.valueAt(i2), srcStreamType, this.mStreamType), srcMap.keyAt(i2), caller);
                }
            }
        }

        @GuardedBy("VolumeStreamState.class")
        public void setAllIndexesToMax() {
            for (int i = 0; i < this.mIndexMap.size(); i++) {
                this.mIndexMap.put(this.mIndexMap.keyAt(i), this.mIndexMax);
            }
        }

        public void mute(boolean state) {
            boolean changed = false;
            synchronized (VolumeStreamState.class) {
                if (state != this.mIsMuted) {
                    changed = true;
                    this.mIsMuted = state;
                    AudioService.sendMsg(AudioService.this.mAudioHandler, 10, 2, 0, 0, this, 0);
                }
            }
            if (changed) {
                Intent intent = new Intent("android.media.STREAM_MUTE_CHANGED_ACTION");
                intent.putExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", this.mStreamType);
                intent.putExtra("android.media.EXTRA_STREAM_VOLUME_MUTED", state);
                AudioService.this.sendBroadcastToAll(intent);
            }
        }

        public int getStreamType() {
            return this.mStreamType;
        }

        public void checkFixedVolumeDevices() {
            synchronized (VolumeStreamState.class) {
                if (AudioService.mStreamVolumeAlias[this.mStreamType] == 3) {
                    for (int i = 0; i < this.mIndexMap.size(); i++) {
                        int device = this.mIndexMap.keyAt(i);
                        int index = this.mIndexMap.valueAt(i);
                        if (!((AudioService.this.mFullVolumeDevices & device) == 0 && ((AudioService.this.mFixedVolumeDevices & device) == 0 || index == 0))) {
                            this.mIndexMap.put(device, this.mIndexMax);
                        }
                        applyDeviceVolume_syncVSS(device);
                    }
                }
            }
        }

        private int getValidIndex(int index) {
            if (index < this.mIndexMin) {
                return this.mIndexMin;
            }
            if (AudioService.this.mUseFixedVolume || index > this.mIndexMax) {
                return this.mIndexMax;
            }
            return index;
        }

        /* access modifiers changed from: private */
        public void dump(PrintWriter pw) {
            String deviceName;
            pw.print("   Muted: ");
            pw.println(this.mIsMuted);
            pw.print("   Min: ");
            pw.println((this.mIndexMin + 5) / 10);
            pw.print("   Max: ");
            pw.println((this.mIndexMax + 5) / 10);
            pw.print("   Current: ");
            int n = 0;
            for (int i = 0; i < this.mIndexMap.size(); i++) {
                if (i > 0) {
                    pw.print(", ");
                }
                int device = this.mIndexMap.keyAt(i);
                pw.print(Integer.toHexString(device));
                if (device == 1073741824) {
                    deviceName = BatteryService.HealthServiceWrapper.INSTANCE_VENDOR;
                } else {
                    deviceName = AudioSystem.getOutputDeviceName(device);
                }
                if (!deviceName.isEmpty()) {
                    pw.print(" (");
                    pw.print(deviceName);
                    pw.print(")");
                }
                pw.print(": ");
                pw.print((this.mIndexMap.valueAt(i) + 5) / 10);
            }
            pw.println();
            pw.print("   Devices: ");
            int devices = AudioService.this.getDevicesForStream(this.mStreamType);
            int i2 = 0;
            while (true) {
                int i3 = 1 << i2;
                int device2 = i3;
                if (i3 != 1073741824) {
                    if ((devices & device2) != 0) {
                        int n2 = n + 1;
                        if (n > 0) {
                            pw.print(", ");
                        }
                        pw.print(AudioSystem.getOutputDeviceName(device2));
                        n = n2;
                    }
                    i2++;
                } else {
                    return;
                }
            }
        }
    }

    class WiredDeviceConnectionState {
        public final String mAddress;
        public final String mCaller;
        public final String mName;
        public final int mState;
        public final int mType;

        public WiredDeviceConnectionState(int type, int state, String address, String name, String caller) {
            this.mType = type;
            this.mState = state;
            this.mAddress = address;
            this.mName = name;
            this.mCaller = caller;
        }
    }

    private boolean isPlatformVoice() {
        return this.mPlatformType == 1;
    }

    /* access modifiers changed from: private */
    public boolean isPlatformTelevision() {
        return this.mPlatformType == 2;
    }

    private boolean isPlatformAutomotive() {
        return this.mContext.getPackageManager().hasSystemFeature("android.hardware.type.automotive");
    }

    /* access modifiers changed from: private */
    public String makeDeviceListKey(int device, String deviceAddress) {
        return "0x" + Integer.toHexString(device) + ":" + deviceAddress;
    }

    public static String makeAlsaAddressString(int card, int device) {
        return "card=" + card + ";device=" + device + ";";
    }

    public AudioService(Context context) {
        int i;
        Context context2 = context;
        this.mContext = context2;
        this.mContentResolver = context.getContentResolver();
        this.mCust = (HwCustAudioService) HwCustUtils.createObj(HwCustAudioService.class, new Object[]{this.mContext});
        this.mAppOps = (AppOpsManager) context2.getSystemService("appops");
        this.mPlatformType = AudioSystem.getPlatformType(context);
        this.mIsSingleVolume = AudioSystem.isSingleVolume(context);
        this.mUserManagerInternal = (UserManagerInternal) LocalServices.getService(UserManagerInternal.class);
        this.mActivityManagerInternal = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
        this.mAudioEventWakeLock = ((PowerManager) context2.getSystemService("power")).newWakeLock(1, "handleAudioEvent");
        this.mVibrator = (Vibrator) context2.getSystemService("vibrator");
        this.mHasVibrator = this.mVibrator == null ? false : this.mVibrator.hasVibrator();
        String maxStreamVolumeFromDtsiString = AudioSystem.getParameters("audio_capability#max_stream_volume");
        if (maxStreamVolumeFromDtsiString != null) {
            Log.i(TAG, "max_stream_volume: " + maxStreamVolumeFromDtsiString);
            try {
                int maxStreamVolumeFromDtsi = Integer.parseInt(maxStreamVolumeFromDtsiString);
                for (int i2 = 0; i2 < MAX_STREAM_VOLUME.length; i2++) {
                    MAX_STREAM_VOLUME[i2] = maxStreamVolumeFromDtsi;
                }
            } catch (NumberFormatException e) {
                Log.i(TAG, "cannot parse max_stream_volume, use default.");
            }
        }
        int maxMusicVolume = SystemProperties.getInt("ro.config.media_vol_steps", -1);
        if (maxMusicVolume != -1) {
            MAX_STREAM_VOLUME[3] = maxMusicVolume;
        }
        int defaultMusicVolume = SystemProperties.getInt("ro.config.media_vol_default", -1);
        if (defaultMusicVolume != -1 && defaultMusicVolume <= MAX_STREAM_VOLUME[3]) {
            AudioSystem.DEFAULT_STREAM_VOLUME[3] = defaultMusicVolume;
        } else if (isPlatformTelevision()) {
            AudioSystem.DEFAULT_STREAM_VOLUME[3] = MAX_STREAM_VOLUME[3] / 4;
        } else {
            AudioSystem.DEFAULT_STREAM_VOLUME[3] = MAX_STREAM_VOLUME[3] / 3;
        }
        int maxVolume = SystemProperties.getInt("ro.config.vol_steps", -1);
        if (maxVolume > 0) {
            for (int index = 0; index < MAX_STREAM_VOLUME.length; index++) {
                MAX_STREAM_VOLUME[index] = maxVolume;
            }
        }
        int voiceCallMaxVolume = SystemProperties.getInt("ro.config.vc_call_vol_steps", -1);
        if (voiceCallMaxVolume > 0) {
            MAX_STREAM_VOLUME[0] = voiceCallMaxVolume;
            AudioSystem.DEFAULT_STREAM_VOLUME[0] = (voiceCallMaxVolume * 4) / 5;
        }
        int maxAlarmVolume = SystemProperties.getInt("ro.config.alarm_vol_steps", -1);
        if (maxAlarmVolume != -1) {
            MAX_STREAM_VOLUME[4] = maxAlarmVolume;
        }
        int defaultAlarmVolume = SystemProperties.getInt("ro.config.alarm_vol_default", -1);
        if (defaultAlarmVolume == -1 || defaultAlarmVolume > MAX_STREAM_VOLUME[4]) {
            AudioSystem.DEFAULT_STREAM_VOLUME[4] = (6 * MAX_STREAM_VOLUME[4]) / 7;
        } else {
            AudioSystem.DEFAULT_STREAM_VOLUME[4] = defaultAlarmVolume;
        }
        int maxSystemVolume = SystemProperties.getInt("ro.config.system_vol_steps", -1);
        if (maxSystemVolume != -1) {
            MAX_STREAM_VOLUME[1] = maxSystemVolume;
        }
        int defaultSystemVolume = SystemProperties.getInt("ro.config.system_vol_default", -1);
        if (defaultSystemVolume == -1 || defaultSystemVolume > MAX_STREAM_VOLUME[1]) {
            AudioSystem.DEFAULT_STREAM_VOLUME[1] = MAX_STREAM_VOLUME[1];
        } else {
            AudioSystem.DEFAULT_STREAM_VOLUME[1] = defaultSystemVolume;
        }
        sSoundEffectVolumeDb = context.getResources().getInteger(17694867);
        this.mForcedUseForComm = 0;
        createAudioSystemThread();
        AudioSystem.setErrorCallback(this.mAudioSystemCallback);
        boolean cameraSoundForced = readCameraSoundForced();
        this.mCameraSoundForced = new Boolean(cameraSoundForced).booleanValue();
        AudioHandler audioHandler = this.mAudioHandler;
        if (cameraSoundForced) {
            i = 11;
        } else {
            i = 0;
        }
        sendMsg(audioHandler, 8, 2, 4, i, new String("AudioService ctor"), 0);
        int defaultAlarmVolume2 = defaultAlarmVolume;
        this.mSafeMediaVolumeState = new Integer(Settings.Global.getInt(this.mContentResolver, "audio_safe_volume_state", 0));
        this.mSafeMediaVolumeIndex = this.mContext.getResources().getInteger(17694852) * 10;
        if (usingHwSafeMediaConfig()) {
            this.mSafeMediaVolumeIndex = getHwSafeMediaVolumeIndex();
            this.mSafeMediaVolumeState = Integer.valueOf(isHwSafeMediaVolumeEnabled() ? new Integer(Settings.Global.getInt(this.mContentResolver, "audio_safe_volume_state", 0)).intValue() : 1);
        }
        this.mIsChineseZone = SystemProperties.getInt("ro.config.hw_optb", CHINAZONE_IDENTIFIER) == CHINAZONE_IDENTIFIER;
        this.mUseFixedVolume = this.mContext.getResources().getBoolean(17957060);
        updateStreamVolumeAlias(false, TAG);
        readPersistedSettings();
        readUserRestrictions();
        this.mSettingsObserver = new SettingsObserver();
        createStreamStates();
        this.mSafeUsbMediaVolumeIndex = getSafeUsbMediaVolumeIndex();
        this.mPlaybackMonitor = new PlaybackActivityMonitor(context2, MAX_STREAM_VOLUME[4]);
        MediaFocusControl mediaFocusControl = HwServiceFactory.getHwMediaFocusControl(this.mContext, this.mPlaybackMonitor);
        if (mediaFocusControl != null) {
            this.mMediaFocusControl = mediaFocusControl;
        } else {
            this.mMediaFocusControl = new MediaFocusControl(this.mContext, this.mPlaybackMonitor);
        }
        this.mPlaybackMonitor.setMediaFocusControl(this.mMediaFocusControl);
        this.mRecordMonitor = new RecordingActivityMonitor(this.mContext);
        readAndSetLowRamDevice();
        this.mRingerAndZenModeMutedStreams = 0;
        setRingerModeInt(getRingerModeInternal(), false);
        IntentFilter intentFilter = new IntentFilter("android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED");
        intentFilter.addAction("android.bluetooth.headset.profile.action.ACTIVE_DEVICE_CHANGED");
        intentFilter.addAction("android.intent.action.DOCK_EVENT");
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.USER_PRESENT");
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        intentFilter.addAction("android.intent.action.USER_BACKGROUND");
        intentFilter.addAction("android.intent.action.USER_FOREGROUND");
        intentFilter.addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED");
        intentFilter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
        intentFilter.addAction("android.intent.action.FM");
        intentFilter.addAction("android.intent.action.CONFIGURATION_CHANGED");
        this.mMonitorRotation = SystemProperties.getBoolean("ro.audio.monitorRotation", false) || SPK_RCV_STEREO_SUPPORT;
        if (this.mMonitorRotation) {
            RotationHelper.init(this.mContext, this.mAudioHandler);
        }
        intentFilter.addAction("android.media.action.OPEN_AUDIO_EFFECT_CONTROL_SESSION");
        intentFilter.addAction("android.media.action.CLOSE_AUDIO_EFFECT_CONTROL_SESSION");
        if (!this.mIsChineseZone) {
            intentFilter.addAction(ACTION_CHECK_MUSIC_ACTIVE);
        }
        int i3 = defaultAlarmVolume2;
        int i4 = maxSystemVolume;
        IntentFilter intentFilter2 = intentFilter;
        int i5 = defaultSystemVolume;
        boolean z = cameraSoundForced;
        context2.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, intentFilter, null, null);
        LocalServices.addService(AudioManagerInternal.class, new AudioServiceInternal());
        this.mUserManagerInternal.addUserRestrictionsListener(this.mUserRestrictionsListener);
        this.mRecordMonitor.initMonitor();
        this.mIsHisiPlatform = isHisiPlatform();
        if (!this.mIsChineseZone) {
            Intent intentCheckMusicActive = new Intent();
            intentCheckMusicActive.setAction(ACTION_CHECK_MUSIC_ACTIVE);
            this.mPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, intentCheckMusicActive, 268435456);
            this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        }
        this.mHwAudioServiceEx = HwServiceExFactory.getHwAudioServiceEx(this, context);
        if (usingHwSafeMediaConfig() && this.mHwAudioServiceEx.isHwSafeUsbMediaVolumeEnabled()) {
            this.mSafeUsbMediaVolumeIndex = this.mHwAudioServiceEx.getHwSafeUsbMediaVolumeIndex();
        }
    }

    public void systemReady() {
        sendMsg(this.mAudioHandler, 21, 2, 0, 0, null, 0);
    }

    public void onSystemReady() {
        this.mSystemReady = true;
        this.mHwAudioServiceEx.setSystemReady();
        sendMsg(this.mAudioHandler, 7, 2, 0, 0, null, 0);
        this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
        this.mScoConnectionState = -1;
        resetBluetoothSco();
        getBluetoothHeadset();
        Intent newIntent = new Intent("android.media.SCO_AUDIO_STATE_CHANGED");
        int i = 0;
        newIntent.putExtra("android.media.extra.SCO_AUDIO_STATE", 0);
        sendStickyBroadcastToAll(newIntent);
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            adapter.getProfileProxy(this.mContext, this.mBluetoothProfileServiceListener, 2);
            adapter.getProfileProxy(this.mContext, this.mBluetoothProfileServiceListener, 21);
        }
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.hdmi.cec")) {
            this.mHdmiManager = (HdmiControlManager) this.mContext.getSystemService(HdmiControlManager.class);
            synchronized (this.mHdmiManager) {
                this.mHdmiTvClient = this.mHdmiManager.getTvClient();
                if (this.mHdmiTvClient != null) {
                    this.mFixedVolumeDevices &= -2883587;
                }
                this.mHdmiPlaybackClient = this.mHdmiManager.getPlaybackClient();
                this.mHdmiCecSink = false;
            }
        }
        this.mNm = (NotificationManager) this.mContext.getSystemService("notification");
        AudioHandler audioHandler = this.mAudioHandler;
        if (!SystemProperties.getBoolean("audio.safemedia.bypass", false)) {
            i = SAFE_VOLUME_CONFIGURE_TIMEOUT_MS;
        }
        sendMsg(audioHandler, 17, 0, 0, 0, TAG, i);
        initA11yMonitoring();
        onIndicateSystemReady();
    }

    /* access modifiers changed from: package-private */
    public void onIndicateSystemReady() {
        if (AudioSystem.systemReady() != 0) {
            sendMsg(this.mAudioHandler, 26, 0, 0, 0, null, 1000);
        }
    }

    public void onAudioServerDied() {
        int forDock;
        int forSys;
        int i;
        if (!this.mSystemReady || AudioSystem.checkAudioFlinger() != 0) {
            Log.e(TAG, "Audioserver died.");
            sendMsg(this.mAudioHandler, 4, 1, 0, 0, null, 500);
            return;
        }
        Log.e(TAG, "Audioserver started.");
        AudioSystem.setParameters("restarting=true");
        readAndSetLowRamDevice();
        synchronized (this.mConnectedDevices) {
            forDock = 0;
            for (int i2 = 0; i2 < this.mConnectedDevices.size(); i2++) {
                DeviceListSpec spec = this.mConnectedDevices.valueAt(i2);
                AudioSystem.setDeviceConnectionState(spec.mDeviceType, 1, spec.mDeviceAddress, spec.mDeviceName);
            }
        }
        if (AudioSystem.setPhoneState(this.mMode) == 0) {
            this.mModeLogger.log(new AudioEventLogger.StringEvent("onAudioServerDied causes setPhoneState(" + AudioSystem.modeToString(this.mMode) + ")"));
        }
        this.mForceUseLogger.log(new AudioServiceEvents.ForceUseEvent(0, this.mForcedUseForComm, "onAudioServerDied"));
        AudioSystem.setForceUse(0, this.mForcedUseForComm);
        this.mForceUseLogger.log(new AudioServiceEvents.ForceUseEvent(2, this.mForcedUseForComm, "onAudioServerDied"));
        AudioSystem.setForceUse(2, this.mForcedUseForComm);
        synchronized (this.mSettingsLock) {
            forSys = this.mCameraSoundForced ? 11 : 0;
        }
        this.mForceUseLogger.log(new AudioServiceEvents.ForceUseEvent(4, forSys, "onAudioServerDied"));
        AudioSystem.setForceUse(4, forSys);
        sendCommForceBroadcast();
        updateAftPolicy();
        int streamType = AudioSystem.getNumStreamTypes() - 1;
        while (true) {
            i = 10;
            if (streamType < 0) {
                break;
            }
            VolumeStreamState streamState = this.mStreamStates[streamType];
            if (streamState != null) {
                AudioSystem.initStreamVolume(streamType, streamState.mIndexMin / 10, streamState.mIndexMax / 10);
                streamState.applyAllVolumes();
            }
            streamType--;
        }
        updateMasterMono(this.mContentResolver);
        setRingerModeInt(getRingerModeInternal(), false);
        this.mHwAudioServiceEx.processAudioServerRestart();
        processMediaServerRestart();
        if (this.mMonitorRotation) {
            RotationHelper.reset();
        }
        synchronized (this.mBluetoothA2dpEnabledLock) {
            if (this.mBluetoothA2dpEnabled) {
                i = 0;
            }
            int forMed = i;
            this.mForceUseLogger.log(new AudioServiceEvents.ForceUseEvent(1, forMed, "onAudioServerDied"));
            AudioSystem.setForceUse(1, forMed);
        }
        synchronized (this.mSettingsLock) {
            if (this.mDockAudioMediaEnabled) {
                forDock = 8;
            }
            this.mForceUseLogger.log(new AudioServiceEvents.ForceUseEvent(3, forDock, "onAudioServerDied"));
            AudioSystem.setForceUse(3, forDock);
            sendEncodedSurroundMode(this.mContentResolver, "onAudioServerDied");
            sendEnabledSurroundFormats(this.mContentResolver, true);
        }
        if (this.mHdmiManager != null) {
            synchronized (this.mHdmiManager) {
                if (this.mHdmiTvClient != null) {
                    setHdmiSystemAudioSupported(this.mHdmiSystemAudioSupported);
                }
            }
        }
        synchronized (this.mAudioPolicies) {
            for (AudioPolicyProxy policy : this.mAudioPolicies.values()) {
                policy.connectMixes();
            }
        }
        onIndicateSystemReady();
        AudioSystem.setParameters("restarting=false");
        if (LOUD_VOICE_MODE_SUPPORT) {
            sendMsg(this.mAudioHandler, 10001, 0, 0, 0, null, 500);
        }
        sendMsg(this.mAudioHandler, 29, 2, 1, 0, null, 0);
    }

    /* access modifiers changed from: private */
    public void onDispatchAudioServerStateChange(boolean state) {
        synchronized (this.mAudioServerStateListeners) {
            for (AsdProxy asdp : this.mAudioServerStateListeners.values()) {
                try {
                    asdp.callback().dispatchAudioServerStateChange(state);
                } catch (RemoteException e) {
                    Log.w(TAG, "Could not call dispatchAudioServerStateChange()", e);
                }
            }
        }
    }

    private void createAudioSystemThread() {
        this.mAudioSystemThread = new AudioSystemThread();
        this.mAudioSystemThread.start();
        waitForAudioHandlerCreation();
    }

    private void waitForAudioHandlerCreation() {
        synchronized (this) {
            while (this.mAudioHandler == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Log.e(TAG, "Interrupted while waiting on volume handler.");
                }
            }
        }
    }

    private void checkAllAliasStreamVolumes() {
        synchronized (this.mSettingsLock) {
            synchronized (VolumeStreamState.class) {
                int numStreamTypes = AudioSystem.getNumStreamTypes();
                for (int streamType = 0; streamType < numStreamTypes; streamType++) {
                    this.mStreamStates[streamType].setAllIndexes(this.mStreamStates[mStreamVolumeAlias[streamType]], TAG);
                    if (!this.mStreamStates[streamType].mIsMuted) {
                        this.mStreamStates[streamType].applyAllVolumes();
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void checkAllFixedVolumeDevices() {
        int numStreamTypes = AudioSystem.getNumStreamTypes();
        for (int streamType = 0; streamType < numStreamTypes; streamType++) {
            this.mStreamStates[streamType].checkFixedVolumeDevices();
        }
    }

    private void checkAllFixedVolumeDevices(int streamType) {
        this.mStreamStates[streamType].checkFixedVolumeDevices();
    }

    private void checkMuteAffectedStreams() {
        for (VolumeStreamState vss : this.mStreamStates) {
            if (vss.mIndexMin > 0 && vss.mStreamType != 0) {
                this.mMuteAffectedStreams &= ~(1 << vss.mStreamType);
            }
        }
    }

    private void createStreamStates() {
        int numStreamTypes = AudioSystem.getNumStreamTypes();
        VolumeStreamState[] streams = new VolumeStreamState[numStreamTypes];
        this.mStreamStates = streams;
        for (int i = 0; i < numStreamTypes; i++) {
            streams[i] = new VolumeStreamState(Settings.System.VOLUME_SETTINGS_INT[mStreamVolumeAlias[i]], i);
        }
        checkAllFixedVolumeDevices();
        checkAllAliasStreamVolumes();
        checkMuteAffectedStreams();
        updateDefaultVolumes();
    }

    private void updateDefaultVolumes() {
        for (int stream = 0; stream < this.mStreamStates.length; stream++) {
            if (stream != mStreamVolumeAlias[stream]) {
                AudioSystem.DEFAULT_STREAM_VOLUME[stream] = rescaleIndex(AudioSystem.DEFAULT_STREAM_VOLUME[mStreamVolumeAlias[stream]], mStreamVolumeAlias[stream], stream);
            }
        }
    }

    private void dumpStreamStates(PrintWriter pw) {
        pw.println("\nStream volumes (device: index)");
        int numStreamTypes = AudioSystem.getNumStreamTypes();
        for (int i = 0; i < numStreamTypes; i++) {
            pw.println("- " + AudioSystem.STREAM_NAMES[i] + ":");
            this.mStreamStates[i].dump(pw);
            pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        }
        pw.print("\n- mute affected streams = 0x");
        pw.println(Integer.toHexString(this.mMuteAffectedStreams));
    }

    private void updateStreamVolumeAlias(boolean updateVolumes, String caller) {
        String str = caller;
        int dtmfStreamAlias = 3;
        int a11yStreamAlias = sIndependentA11yVolume ? 10 : 3;
        if (this.mIsSingleVolume) {
            mStreamVolumeAlias = this.STREAM_VOLUME_ALIAS_TELEVISION;
            dtmfStreamAlias = 3;
        } else if (this.mPlatformType != 1) {
            mStreamVolumeAlias = this.STREAM_VOLUME_ALIAS_DEFAULT;
        } else {
            mStreamVolumeAlias = this.STREAM_VOLUME_ALIAS_VOICE;
            dtmfStreamAlias = 2;
        }
        int dtmfStreamAlias2 = dtmfStreamAlias;
        if (this.mIsSingleVolume != 0) {
            this.mRingerModeAffectedStreams = 0;
        } else if (isInCommunication()) {
            dtmfStreamAlias2 = 0;
            this.mRingerModeAffectedStreams &= -257;
        } else {
            this.mRingerModeAffectedStreams |= 256;
        }
        int dtmfStreamAlias3 = dtmfStreamAlias2;
        mStreamVolumeAlias[8] = dtmfStreamAlias3;
        mStreamVolumeAlias[10] = a11yStreamAlias;
        if (updateVolumes && this.mStreamStates != null) {
            updateDefaultVolumes();
            synchronized (this.mSettingsLock) {
                synchronized (VolumeStreamState.class) {
                    this.mStreamStates[8].setAllIndexes(this.mStreamStates[dtmfStreamAlias3], str);
                    String unused = this.mStreamStates[10].mVolumeIndexSettingName = Settings.System.VOLUME_SETTINGS_INT[a11yStreamAlias];
                    this.mStreamStates[10].setAllIndexes(this.mStreamStates[a11yStreamAlias], str);
                    this.mStreamStates[10].refreshRange(mStreamVolumeAlias[10]);
                }
            }
            if (sIndependentA11yVolume) {
                this.mStreamStates[10].readSettings();
            }
            setRingerModeInt(getRingerModeInternal(), false);
            sendMsg(this.mAudioHandler, 10, 2, 0, 0, this.mStreamStates[8], 0);
            sendMsg(this.mAudioHandler, 10, 2, 0, 0, this.mStreamStates[10], 0);
        }
    }

    /* access modifiers changed from: private */
    public void readDockAudioSettings(ContentResolver cr) {
        int i = 0;
        boolean z = true;
        if (Settings.Global.getInt(cr, "dock_audio_media_enabled", 0) != 1) {
            z = false;
        }
        this.mDockAudioMediaEnabled = z;
        AudioHandler audioHandler = this.mAudioHandler;
        if (this.mDockAudioMediaEnabled) {
            i = 8;
        }
        sendMsg(audioHandler, 8, 2, 3, i, new String("readDockAudioSettings"), 0);
    }

    /* access modifiers changed from: private */
    public void updateMasterMono(ContentResolver cr) {
        boolean masterMono = Settings.System.getIntForUser(cr, "master_mono", 0, -2) == 1;
        if (DEBUG_VOL) {
            Log.d(TAG, String.format("Master mono %b", new Object[]{Boolean.valueOf(masterMono)}));
        }
        AudioSystem.setMasterMono(masterMono);
    }

    private void sendEncodedSurroundMode(ContentResolver cr, String eventSource) {
        sendEncodedSurroundMode(Settings.Global.getInt(cr, "encoded_surround_output", 0), eventSource);
    }

    /* access modifiers changed from: private */
    public void sendEncodedSurroundMode(int encodedSurroundMode, String eventSource) {
        int forceSetting = 18;
        switch (encodedSurroundMode) {
            case 0:
                forceSetting = 0;
                break;
            case 1:
                forceSetting = 13;
                break;
            case 2:
                forceSetting = 14;
                break;
            case 3:
                forceSetting = 15;
                break;
            default:
                Log.e(TAG, "updateSurroundSoundSettings: illegal value " + encodedSurroundMode);
                break;
        }
        if (forceSetting != 18) {
            sendMsg(this.mAudioHandler, 8, 2, 6, forceSetting, eventSource, 0);
        }
    }

    /* access modifiers changed from: private */
    public void sendEnabledSurroundFormats(ContentResolver cr, boolean forceUpdate) {
        if (this.mEncodedSurroundMode == 3) {
            String enabledSurroundFormats = Settings.Global.getString(cr, "encoded_surround_output_enabled_formats");
            if (enabledSurroundFormats == null) {
                enabledSurroundFormats = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
            }
            if (forceUpdate || !TextUtils.equals(enabledSurroundFormats, this.mEnabledSurroundFormats)) {
                this.mEnabledSurroundFormats = enabledSurroundFormats;
                String[] surroundFormats = TextUtils.split(enabledSurroundFormats, ",");
                ArrayList<Integer> formats = new ArrayList<>();
                for (String format : surroundFormats) {
                    try {
                        int audioFormat = Integer.valueOf(format).intValue();
                        boolean isSurroundFormat = false;
                        int[] iArr = AudioFormat.SURROUND_SOUND_ENCODING;
                        int length = iArr.length;
                        int i = 0;
                        while (true) {
                            if (i >= length) {
                                break;
                            } else if (iArr[i] == audioFormat) {
                                isSurroundFormat = true;
                                break;
                            } else {
                                i++;
                            }
                        }
                        if (isSurroundFormat && !formats.contains(Integer.valueOf(audioFormat))) {
                            formats.add(Integer.valueOf(audioFormat));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Invalid enabled surround format:" + format);
                    }
                }
                Settings.Global.putString(this.mContext.getContentResolver(), "encoded_surround_output_enabled_formats", TextUtils.join(",", formats));
                sendMsg(this.mAudioHandler, 30, 2, 0, 0, formats, 0);
            }
        }
    }

    /* access modifiers changed from: private */
    public void onEnableSurroundFormats(ArrayList<Integer> enabledSurroundFormats) {
        for (int surroundFormat : AudioFormat.SURROUND_SOUND_ENCODING) {
            int ret = AudioSystem.setSurroundFormatEnabled(surroundFormat, enabledSurroundFormats.contains(Integer.valueOf(surroundFormat)));
            Log.i(TAG, "enable surround format:" + surroundFormat + " " + enabled + " " + ret);
        }
    }

    private void readPersistedSettings() {
        ContentResolver cr = this.mContentResolver;
        int i = 2;
        int ringerModeFromSettings = Settings.Global.getInt(cr, "mode_ringer", 2);
        int ringerMode = ringerModeFromSettings;
        if (!isValidRingerMode(ringerMode)) {
            ringerMode = 2;
        }
        if (ringerMode == 1 && !this.mHasVibrator) {
            ringerMode = 0;
        }
        if (ringerMode != ringerModeFromSettings) {
            Settings.Global.putInt(cr, "mode_ringer", ringerMode);
        }
        if (this.mUseFixedVolume || this.mIsSingleVolume) {
            ringerMode = 2;
        }
        synchronized (this.mSettingsLock) {
            this.mRingerMode = ringerMode;
            if (this.mRingerModeExternal == -1) {
                this.mRingerModeExternal = this.mRingerMode;
            }
            this.mVibrateSetting = AudioSystem.getValueForVibrateSetting(0, 1, this.mHasVibrator ? 2 : 0);
            int i2 = this.mVibrateSetting;
            if (!this.mHasVibrator) {
                i = 0;
            }
            this.mVibrateSetting = AudioSystem.getValueForVibrateSetting(i2, 0, i);
            updateRingerAndZenModeAffectedStreams();
            readDockAudioSettings(cr);
            sendEncodedSurroundMode(cr, "readPersistedSettings");
            sendEnabledSurroundFormats(cr, true);
        }
        this.mMuteAffectedStreams = Settings.System.getIntForUser(cr, "mute_streams_affected", 47, -2);
        updateMasterMono(cr);
        updateDefaultStream();
        readPersistedSettingsEx(cr);
        broadcastRingerMode("android.media.RINGER_MODE_CHANGED", this.mRingerModeExternal);
        broadcastRingerMode("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION", this.mRingerMode);
        broadcastVibrateSetting(0);
        broadcastVibrateSetting(1);
        this.mVolumeController.loadSettings(cr);
    }

    private void readUserRestrictions() {
        int currentUser = getCurrentUserId();
        boolean masterMute = this.mUserManagerInternal.getUserRestriction(currentUser, "disallow_unmute_device") || this.mUserManagerInternal.getUserRestriction(currentUser, "no_adjust_volume");
        if (this.mUseFixedVolume) {
            masterMute = false;
            AudioSystem.setMasterVolume(1.0f);
        }
        if (DEBUG_VOL) {
            Log.d(TAG, String.format("Master mute %s, user=%d", new Object[]{Boolean.valueOf(masterMute), Integer.valueOf(currentUser)}));
        }
        setSystemAudioMute(masterMute);
        AudioSystem.setMasterMute(masterMute);
        broadcastMasterMuteStatus(masterMute);
        boolean microphoneMute = this.mUserManagerInternal.getUserRestriction(currentUser, "no_unmute_microphone");
        if (DEBUG_VOL) {
            Log.d(TAG, String.format("Mic mute %s, user=%d", new Object[]{Boolean.valueOf(microphoneMute), Integer.valueOf(currentUser)}));
        }
        AudioSystem.muteMicrophone(microphoneMute);
    }

    /* access modifiers changed from: private */
    public int rescaleIndex(int index, int srcStream, int dstStream) {
        int rescaled = ((this.mStreamStates[dstStream].getMaxIndex() * index) + (this.mStreamStates[srcStream].getMaxIndex() / 2)) / this.mStreamStates[srcStream].getMaxIndex();
        if (rescaled < this.mStreamStates[dstStream].getMinIndex()) {
            return this.mStreamStates[dstStream].getMinIndex();
        }
        return rescaled;
    }

    public void adjustSuggestedStreamVolume(int direction, int suggestedStreamType, int flags, String callingPackage, String caller) {
        IAudioPolicyCallback extVolCtlr;
        synchronized (this.mExtVolumeControllerLock) {
            extVolCtlr = this.mExtVolumeController;
        }
        if (extVolCtlr != null) {
            sendMsg(this.mAudioHandler, 28, 2, direction, 0, extVolCtlr, 0);
            return;
        }
        adjustSuggestedStreamVolume(direction, suggestedStreamType, flags, callingPackage, caller, Binder.getCallingUid());
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0096  */
    public void adjustSuggestedStreamVolume(int direction, int suggestedStreamType, int flags, String callingPackage, String caller, int uid) {
        int streamType;
        int maybeActiveStreamType;
        int flags2;
        int direction2;
        boolean activeForReal;
        int i = suggestedStreamType;
        int flags3 = flags;
        String str = caller;
        if (DEBUG_VOL) {
            Log.d(TAG, "adjustSuggestedStreamVolume() stream=" + i + ", flags=" + flags3 + ", caller=" + str + ", volControlStream=" + this.mVolumeControlStream + ", userSelect=" + this.mUserSelectedVolumeControlStream);
        }
        AudioEventLogger audioEventLogger = this.mVolumeLogger;
        String str2 = callingPackage;
        StringBuilder sb = new StringBuilder(str2);
        sb.append(SliceClientPermissions.SliceAuthority.DELIMITER);
        sb.append(str);
        sb.append(" uid:");
        int i2 = uid;
        sb.append(i2);
        AudioServiceEvents.VolumeEvent volumeEvent = new AudioServiceEvents.VolumeEvent(0, i, direction, flags3, sb.toString());
        audioEventLogger.log(volumeEvent);
        synchronized (this.mForceControlStreamLock) {
            if (this.mUserSelectedVolumeControlStream) {
                streamType = this.mVolumeControlStream;
            } else {
                int streamType2 = getActiveStreamType(i);
                if (streamType2 != 2) {
                    if (streamType2 != 5) {
                        activeForReal = AudioSystem.isStreamActive(streamType2, 0);
                        if (!activeForReal) {
                            if (this.mVolumeControlStream != -1) {
                                streamType = this.mVolumeControlStream;
                            }
                        }
                        streamType = streamType2;
                    }
                }
                activeForReal = wasStreamActiveRecently(streamType2, 0);
                if (!activeForReal) {
                }
                streamType = streamType2;
            }
            maybeActiveStreamType = streamType;
        }
        boolean isMute = isMuteAdjust(direction);
        ensureValidStreamType(maybeActiveStreamType);
        int resolvedStream = mStreamVolumeAlias[maybeActiveStreamType];
        if (!((flags3 & 4) == 0 || resolvedStream == 2)) {
            flags3 &= -5;
        }
        if (this.mVolumeController.suppressAdjustment(resolvedStream, flags3, isMute)) {
            int flags4 = flags3 & -5 & -17;
            if (DEBUG_VOL) {
                Log.d(TAG, "Volume controller suppressed adjustment");
            }
            direction2 = 0;
            flags2 = flags4;
        } else {
            direction2 = direction;
            flags2 = flags3;
        }
        adjustStreamVolume(maybeActiveStreamType, direction2, flags2, str2, str, i2);
    }

    public void adjustStreamVolume(int streamType, int direction, int flags, String callingPackage) {
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.AUDIO_ADJUSTSTREAMVOLUME, new Object[0]);
        if (streamType != 10 || canChangeAccessibilityVolume()) {
            AudioEventLogger audioEventLogger = this.mVolumeLogger;
            AudioServiceEvents.VolumeEvent volumeEvent = new AudioServiceEvents.VolumeEvent(1, streamType, direction, flags, callingPackage);
            audioEventLogger.log(volumeEvent);
            adjustStreamVolume(streamType, direction, flags, callingPackage, callingPackage, Binder.getCallingUid());
            return;
        }
        Log.w(TAG, "Trying to call adjustStreamVolume() for a11y withoutCHANGE_ACCESSIBILITY_VOLUME / callingPackage=" + callingPackage);
    }

    /* JADX INFO: finally extract failed */
    /*  JADX ERROR: NullPointerException in pass: CodeShrinkVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.instructions.args.InsnArg.wrapInstruction(InsnArg.java:117)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.inline(CodeShrinkVisitor.java:119)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkBlock(CodeShrinkVisitor.java:70)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkMethod(CodeShrinkVisitor.java:42)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.visit(CodeShrinkVisitor.java:34)
        */
    protected void adjustStreamVolume(int r33, int r34, int r35, java.lang.String r36, java.lang.String r37, int r38) {
        /*
            r32 = this;
            r8 = r32
            r9 = r33
            r10 = r34
            r1 = r35
            boolean r0 = r32.checkEnbaleVolumeAdjust()
            if (r0 != 0) goto L_0x000f
            return
        L_0x000f:
            boolean r0 = r8.mUseFixedVolume
            if (r0 == 0) goto L_0x0014
            return
        L_0x0014:
            java.lang.String r0 = "AudioService"
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "adjustStreamVolume() stream="
            r2.append(r3)
            r2.append(r9)
            java.lang.String r3 = ", dir="
            r2.append(r3)
            r2.append(r10)
            java.lang.String r3 = ", flags="
            r2.append(r3)
            r2.append(r1)
            java.lang.String r3 = ", mScreenOn= "
            r2.append(r3)
            boolean r3 = r8.mScreenOn
            r2.append(r3)
            java.lang.String r3 = ", caller="
            r2.append(r3)
            int r3 = android.os.Binder.getCallingPid()
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            android.util.Log.d(r0, r2)
            r8.ensureValidDirection(r10)
            r32.ensureValidStreamType(r33)
            boolean r11 = r8.isMuteAdjust(r10)
            if (r11 == 0) goto L_0x0063
            boolean r0 = r32.isStreamAffectedByMute(r33)
            if (r0 != 0) goto L_0x0063
            return
        L_0x0063:
            if (r11 == 0) goto L_0x0098
            if (r9 != 0) goto L_0x0098
            android.content.Context r0 = r8.mContext
            java.lang.String r2 = "android.permission.MODIFY_PHONE_STATE"
            int r0 = r0.checkCallingOrSelfPermission(r2)
            if (r0 == 0) goto L_0x0098
            java.lang.String r0 = "AudioService"
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "MODIFY_PHONE_STATE Permission Denial: adjustStreamVolume from pid="
            r2.append(r3)
            int r3 = android.os.Binder.getCallingPid()
            r2.append(r3)
            java.lang.String r3 = ", uid="
            r2.append(r3)
            int r3 = android.os.Binder.getCallingUid()
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            android.util.Log.w(r0, r2)
            return
        L_0x0098:
            int[] r0 = mStreamVolumeAlias
            r12 = r0[r9]
            com.android.server.audio.AudioService$VolumeStreamState[] r0 = r8.mStreamStates
            r13 = r0[r12]
            int r14 = r8.getDeviceForStream(r12)
            int r2 = r13.getIndex(r14)
            r15 = 1
            r16 = 0
            r0 = r14 & 896(0x380, float:1.256E-42)
            if (r0 != 0) goto L_0x00b4
            r0 = r1 & 64
            if (r0 == 0) goto L_0x00b4
            return
        L_0x00b4:
            r0 = 1000(0x3e8, float:1.401E-42)
            r3 = r38
            if (r3 != r0) goto L_0x00c8
            int r0 = r32.getCurrentUserId()
            int r4 = android.os.UserHandle.getAppId(r38)
            int r0 = android.os.UserHandle.getUid(r0, r4)
            r7 = r0
            goto L_0x00c9
        L_0x00c8:
            r7 = r3
        L_0x00c9:
            android.app.AppOpsManager r0 = r8.mAppOps     // Catch:{ SecurityException -> 0x0359 }
            int[] r3 = STREAM_VOLUME_OPS     // Catch:{ SecurityException -> 0x0359 }
            r3 = r3[r12]     // Catch:{ SecurityException -> 0x0359 }
            r6 = r36
            int r0 = r0.noteOp(r3, r7, r6)     // Catch:{ SecurityException -> 0x0359 }
            if (r0 == 0) goto L_0x00d8
            return
        L_0x00d8:
            java.lang.Integer r3 = r8.mSafeMediaVolumeState
            monitor-enter(r3)
            r0 = 0
            r8.mPendingVolumeCommand = r0     // Catch:{ all -> 0x0350 }
            monitor-exit(r3)     // Catch:{ all -> 0x0350 }
            r0 = r1 & -33
            r5 = 3
            if (r12 != r5) goto L_0x0107
            int r1 = r8.mFixedVolumeDevices
            r1 = r1 & r14
            if (r1 == 0) goto L_0x0107
            r0 = r0 | 32
            java.lang.Integer r1 = r8.mSafeMediaVolumeState
            int r1 = r1.intValue()
            if (r1 != r5) goto L_0x00ff
            r1 = 603979788(0x2400000c, float:2.7755615E-17)
            r1 = r1 & r14
            if (r1 == 0) goto L_0x00ff
            int r1 = r8.safeMediaVolumeIndex(r14)
            goto L_0x0103
        L_0x00ff:
            int r1 = r13.getMaxIndex()
        L_0x0103:
            if (r2 == 0) goto L_0x010d
            r2 = r1
            goto L_0x010d
        L_0x0107:
            r1 = 10
            int r1 = r8.rescaleIndex(r1, r9, r12)
        L_0x010d:
            r17 = r2
            boolean r2 = LOUD_VOICE_MODE_SUPPORT
            if (r2 == 0) goto L_0x0125
            if (r9 != 0) goto L_0x0125
            java.lang.String r2 = "true"
            java.lang.String r3 = "VOICE_LVM_Enable"
            java.lang.String r3 = android.media.AudioSystem.getParameters(r3)
            boolean r2 = r2.equals(r3)
            if (r2 == 0) goto L_0x0125
            r1 = 0
        L_0x0125:
            r18 = r1
            r1 = r0 & 2
            r4 = 0
            r3 = 1
            if (r1 != 0) goto L_0x013a
            int r1 = r32.getUiSoundsStreamType()
            if (r12 != r1) goto L_0x0134
            goto L_0x013a
        L_0x0134:
            r19 = r7
            r2 = r15
            r7 = r0
            r15 = r3
            goto L_0x017a
        L_0x013a:
            int r2 = r32.getRingerModeInternal()
            if (r2 != r3) goto L_0x0142
            r0 = r0 & -17
        L_0x0142:
            boolean r19 = r13.mIsMuted
            r1 = r8
            r20 = r2
            r2 = r17
            r21 = r15
            r15 = r3
            r3 = r10
            r4 = r18
            r5 = r19
            r6 = r36
            r19 = r7
            r7 = r0
            int r1 = r1.checkForRingerModeChange(r2, r3, r4, r5, r6, r7)
            r2 = r1 & 1
            if (r2 == 0) goto L_0x0163
            r2 = r15
            goto L_0x0164
        L_0x0163:
            r2 = 0
        L_0x0164:
            r3 = r1 & 2
            if (r3 == 0) goto L_0x016a
            r3 = r15
            goto L_0x016b
        L_0x016a:
            r3 = 0
        L_0x016b:
            r16 = r3
            r3 = r1 & 128(0x80, float:1.794E-43)
            if (r3 == 0) goto L_0x0173
            r0 = r0 | 128(0x80, float:1.794E-43)
        L_0x0173:
            r3 = r1 & 2048(0x800, float:2.87E-42)
            if (r3 == 0) goto L_0x0179
            r0 = r0 | 2048(0x800, float:2.87E-42)
        L_0x0179:
            r7 = r0
        L_0x017a:
            boolean r0 = r8.volumeAdjustmentAllowedByDnd(r12, r7)
            if (r0 != 0) goto L_0x0181
            r2 = 0
        L_0x0181:
            r20 = r2
            com.android.server.audio.AudioService$VolumeStreamState[] r0 = r8.mStreamStates
            r0 = r0[r9]
            int r6 = r0.getIndex(r14)
            if (r20 == 0) goto L_0x02ef
            if (r10 == 0) goto L_0x02ef
            com.android.server.audio.AudioService$AudioHandler r0 = r8.mAudioHandler
            r5 = 24
            r0.removeMessages(r5)
            r0 = -1
            if (r11 == 0) goto L_0x01d9
            r1 = 101(0x65, float:1.42E-43)
            if (r10 != r1) goto L_0x01a3
            boolean r1 = r13.mIsMuted
            r1 = r1 ^ r15
            goto L_0x01aa
        L_0x01a3:
            r1 = -100
            if (r10 != r1) goto L_0x01a9
            r1 = r15
            goto L_0x01aa
        L_0x01a9:
            r1 = 0
        L_0x01aa:
            r4 = 3
            if (r12 != r4) goto L_0x01b0
            r8.setSystemAudioMute(r1)
        L_0x01b0:
            r2 = 0
        L_0x01b1:
            com.android.server.audio.AudioService$VolumeStreamState[] r3 = r8.mStreamStates
            int r3 = r3.length
            if (r2 >= r3) goto L_0x01d8
            int[] r3 = mStreamVolumeAlias
            r3 = r3[r2]
            if (r12 != r3) goto L_0x01d4
            boolean r3 = r32.readCameraSoundForced()
            if (r3 == 0) goto L_0x01cd
            com.android.server.audio.AudioService$VolumeStreamState[] r3 = r8.mStreamStates
            r3 = r3[r2]
            int r3 = r3.getStreamType()
            r4 = 7
            if (r3 == r4) goto L_0x01d4
        L_0x01cd:
            com.android.server.audio.AudioService$VolumeStreamState[] r3 = r8.mStreamStates
            r3 = r3[r2]
            r3.mute(r1)
        L_0x01d4:
            int r2 = r2 + 1
            r4 = 3
            goto L_0x01b1
        L_0x01d8:
            goto L_0x01fe
        L_0x01d9:
            if (r10 != r15) goto L_0x0206
            int r1 = r17 + r18
            boolean r1 = r8.checkSafeMediaVolume(r12, r1, r14)
            if (r1 != 0) goto L_0x0206
            java.lang.String r1 = "AudioService"
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "adjustStreamVolume() safe volume index = "
            r2.append(r3)
            r2.append(r6)
            java.lang.String r2 = r2.toString()
            android.util.Log.e(r1, r2)
            com.android.server.audio.AudioService$VolumeController r1 = r8.mVolumeController
            r1.postDisplaySafeVolumeWarning(r7)
        L_0x01fe:
            r21 = r5
            r27 = r6
            r28 = r7
            r15 = 3
            goto L_0x0260
        L_0x0206:
            int r1 = r10 * r18
            r4 = r37
            boolean r1 = r13.adjustIndex(r1, r14, r4)
            if (r1 != 0) goto L_0x0216
            boolean r1 = r13.mIsMuted
            if (r1 == 0) goto L_0x01fe
        L_0x0216:
            boolean r1 = r13.mIsMuted
            if (r1 == 0) goto L_0x024e
            if (r10 != r15) goto L_0x022a
            r3 = 0
            r13.mute(r3)
            r21 = r5
            r27 = r6
            r28 = r7
            r15 = 3
            goto L_0x0255
        L_0x022a:
            r3 = 0
            if (r10 != r0) goto L_0x024e
            boolean r1 = r8.mIsSingleVolume
            if (r1 == 0) goto L_0x024e
            com.android.server.audio.AudioService$AudioHandler r1 = r8.mAudioHandler
            r2 = 24
            r21 = 2
            r22 = 0
            r23 = 350(0x15e, float:4.9E-43)
            r3 = r21
            r15 = 3
            r4 = r12
            r21 = r5
            r5 = r7
            r27 = r6
            r6 = r22
            r28 = r7
            r7 = r23
            sendMsg(r1, r2, r3, r4, r5, r6, r7)
            goto L_0x0255
        L_0x024e:
            r21 = r5
            r27 = r6
            r28 = r7
            r15 = 3
        L_0x0255:
            com.android.server.audio.AudioService$AudioHandler r1 = r8.mAudioHandler
            r2 = 0
            r3 = 2
            r5 = 0
            r7 = 0
            r4 = r14
            r6 = r13
            sendMsg(r1, r2, r3, r4, r5, r6, r7)
        L_0x0260:
            com.android.server.audio.AudioService$VolumeStreamState[] r1 = r8.mStreamStates
            r1 = r1[r9]
            int r1 = r1.getIndex(r14)
            if (r12 != r15) goto L_0x028b
            r2 = r14 & 896(0x380, float:1.256E-42)
            if (r2 == 0) goto L_0x028b
            r7 = r28
            r2 = r7 & 64
            if (r2 != 0) goto L_0x028d
            java.lang.Object r2 = r8.mA2dpAvrcpLock
            monitor-enter(r2)
            android.bluetooth.BluetoothA2dp r3 = r8.mA2dp     // Catch:{ all -> 0x0288 }
            if (r3 == 0) goto L_0x0286
            boolean r3 = r8.mAvrcpAbsVolSupported     // Catch:{ all -> 0x0288 }
            if (r3 == 0) goto L_0x0286
            android.bluetooth.BluetoothA2dp r3 = r8.mA2dp     // Catch:{ all -> 0x0288 }
            int r4 = r1 / 10
            r3.setAvrcpAbsoluteVolume(r4)     // Catch:{ all -> 0x0288 }
        L_0x0286:
            monitor-exit(r2)     // Catch:{ all -> 0x0288 }
            goto L_0x028d
        L_0x0288:
            r0 = move-exception
            monitor-exit(r2)     // Catch:{ all -> 0x0288 }
            throw r0
        L_0x028b:
            r7 = r28
        L_0x028d:
            r2 = 134217728(0x8000000, float:3.85186E-34)
            r2 = r2 & r14
            if (r2 == 0) goto L_0x0295
            r8.setHearingAidVolume(r1, r9)
        L_0x0295:
            if (r12 != r15) goto L_0x02a1
            int r2 = r32.getStreamMaxVolume(r33)
            r6 = r27
            r8.setSystemAudioVolume(r6, r1, r2, r7)
            goto L_0x02a3
        L_0x02a1:
            r6 = r27
        L_0x02a3:
            android.hardware.hdmi.HdmiControlManager r2 = r8.mHdmiManager
            if (r2 == 0) goto L_0x02ea
            android.hardware.hdmi.HdmiControlManager r2 = r8.mHdmiManager
            monitor-enter(r2)
            boolean r3 = r8.mHdmiCecSink     // Catch:{ all -> 0x02e7 }
            if (r3 == 0) goto L_0x02e5
            if (r12 != r15) goto L_0x02e5
            if (r6 == r1) goto L_0x02e5
            android.hardware.hdmi.HdmiPlaybackClient r3 = r8.mHdmiPlaybackClient     // Catch:{ all -> 0x02e7 }
            monitor-enter(r3)     // Catch:{ all -> 0x02e7 }
            if (r10 != r0) goto L_0x02ba
            r5 = 25
            goto L_0x02bc
        L_0x02ba:
            r5 = r21
        L_0x02bc:
            r4 = r5
            long r21 = android.os.Binder.clearCallingIdentity()     // Catch:{ all -> 0x02e2 }
            r29 = r21
            android.hardware.hdmi.HdmiPlaybackClient r0 = r8.mHdmiPlaybackClient     // Catch:{ all -> 0x02d9 }
            r5 = 1
            r0.sendKeyEvent(r4, r5)     // Catch:{ all -> 0x02d9 }
            android.hardware.hdmi.HdmiPlaybackClient r0 = r8.mHdmiPlaybackClient     // Catch:{ all -> 0x02d9 }
            r5 = 0
            r0.sendKeyEvent(r4, r5)     // Catch:{ all -> 0x02d9 }
            r31 = r4
            r4 = r29
            android.os.Binder.restoreCallingIdentity(r4)     // Catch:{ all -> 0x02e2 }
            monitor-exit(r3)     // Catch:{ all -> 0x02e2 }
            goto L_0x02e5
        L_0x02d9:
            r0 = move-exception
            r31 = r4
            r4 = r29
            android.os.Binder.restoreCallingIdentity(r4)     // Catch:{ all -> 0x02e2 }
            throw r0     // Catch:{ all -> 0x02e2 }
        L_0x02e2:
            r0 = move-exception
            monitor-exit(r3)     // Catch:{ all -> 0x02e2 }
            throw r0     // Catch:{ all -> 0x02e7 }
        L_0x02e5:
            monitor-exit(r2)     // Catch:{ all -> 0x02e7 }
            goto L_0x02ea
        L_0x02e7:
            r0 = move-exception
            monitor-exit(r2)     // Catch:{ all -> 0x02e7 }
            throw r0
        L_0x02ea:
            r15 = r6
            r21 = r7
            goto L_0x0304
        L_0x02ef:
            if (r16 == 0) goto L_0x0301
            com.android.server.audio.AudioService$AudioHandler r1 = r8.mAudioHandler
            r2 = 0
            r3 = 2
            r5 = 0
            r0 = 0
            r4 = r14
            r15 = r6
            r6 = r13
            r21 = r7
            r7 = r0
            sendMsg(r1, r2, r3, r4, r5, r6, r7)
            goto L_0x0304
        L_0x0301:
            r15 = r6
            r21 = r7
        L_0x0304:
            com.android.server.audio.AudioService$VolumeStreamState[] r0 = r8.mStreamStates
            r0 = r0[r9]
            int r0 = r0.getIndex(r14)
            r1 = r21 & 4
            if (r1 == 0) goto L_0x031f
            android.app.KeyguardManager r1 = r8.mKeyguardManager
            if (r1 == 0) goto L_0x031f
            android.app.KeyguardManager r1 = r8.mKeyguardManager
            boolean r1 = r1.isKeyguardLocked()
            if (r1 == 0) goto L_0x031f
            r7 = r21 & -5
            goto L_0x0321
        L_0x031f:
            r7 = r21
        L_0x0321:
            r1 = r7 & 1
            if (r1 == 0) goto L_0x032b
            boolean r1 = r8.mScreenOn
            if (r1 != 0) goto L_0x032b
            r7 = r7 & -2
        L_0x032b:
            r8.sendVolumeUpdate(r9, r15, r0, r7)
            boolean r1 = LOUD_VOICE_MODE_SUPPORT
            if (r1 == 0) goto L_0x034f
            com.android.server.audio.AudioService$AudioHandler r6 = r8.mAudioHandler
            r22 = 10001(0x2711, float:1.4014E-41)
            r23 = 0
            r24 = 1
            r25 = 0
            com.android.server.audio.AbsAudioService$DeviceVolumeState r26 = new com.android.server.audio.AbsAudioService$DeviceVolumeState
            r1 = r26
            r2 = r8
            r3 = r10
            r4 = r14
            r5 = r15
            r21 = r6
            r6 = r9
            r1.<init>(r3, r4, r5, r6)
            r27 = 0
            sendMsg(r21, r22, r23, r24, r25, r26, r27)
        L_0x034f:
            return
        L_0x0350:
            r0 = move-exception
            r19 = r7
            r21 = r15
        L_0x0355:
            monitor-exit(r3)     // Catch:{ all -> 0x0357 }
            throw r0
        L_0x0357:
            r0 = move-exception
            goto L_0x0355
        L_0x0359:
            r0 = move-exception
            r19 = r7
            r21 = r15
            java.lang.String r3 = "AudioService"
            java.lang.String r4 = "mAppOps.noteOp cannot match the uid and packagename"
            android.util.Log.e(r3, r4)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.audio.AudioService.adjustStreamVolume(int, int, int, java.lang.String, java.lang.String, int):void");
    }

    /* access modifiers changed from: private */
    public void onUnmuteStream(int stream, int flags) {
        this.mStreamStates[stream].mute(false);
        int index = this.mStreamStates[stream].getIndex(getDeviceForStream(stream));
        sendVolumeUpdate(stream, index, index, flags);
    }

    private void setSystemAudioVolume(int oldVolume, int newVolume, int maxVolume, int flags) {
        if (this.mHdmiManager != null && this.mHdmiTvClient != null && oldVolume != newVolume && (flags & 256) == 0) {
            synchronized (this.mHdmiManager) {
                if (this.mHdmiSystemAudioSupported) {
                    synchronized (this.mHdmiTvClient) {
                        long token = Binder.clearCallingIdentity();
                        try {
                            this.mHdmiTvClient.setSystemAudioVolume(oldVolume, newVolume, maxVolume);
                        } finally {
                            Binder.restoreCallingIdentity(token);
                        }
                    }
                }
            }
        }
    }

    private int getNewRingerMode(int stream, int index, int flags) {
        if (this.mIsSingleVolume) {
            return getRingerModeExternal();
        }
        if ((flags & 2) == 0 && stream != getUiSoundsStreamType()) {
            return getRingerModeExternal();
        }
        int newRingerMode = 2;
        if (index == 0) {
            if (this.mHasVibrator) {
                newRingerMode = 1;
            } else if (this.mVolumePolicy.volumeDownToEnterSilent) {
                newRingerMode = 0;
            }
        }
        return newRingerMode;
    }

    private boolean isAndroidNPlus(String caller) {
        try {
            if (this.mContext.getPackageManager().getApplicationInfoAsUser(caller, 0, UserHandle.getUserId(Binder.getCallingUid())).targetSdkVersion >= 24) {
                return true;
            }
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            return true;
        }
    }

    private boolean wouldToggleZenMode(int newMode) {
        if (getRingerModeExternal() == 0 && newMode != 0) {
            return true;
        }
        if (getRingerModeExternal() == 0 || newMode != 0) {
            return false;
        }
        return true;
    }

    private void onSetStreamVolume(int streamType, int index, int flags, int device, String caller) {
        int stream = mStreamVolumeAlias[streamType];
        setStreamVolumeInt(stream, index, device, false, caller);
        boolean z = false;
        if ((flags & 2) != 0 || stream == getUiSoundsStreamType()) {
            setRingerMode(getNewRingerMode(stream, index, flags), "AudioService.onSetStreamVolume", false);
        }
        VolumeStreamState volumeStreamState = this.mStreamStates[stream];
        if (index == 0) {
            z = true;
        }
        volumeStreamState.mute(z);
    }

    public void setStreamVolume(int streamType, int index, int flags, String callingPackage) {
        int i = streamType;
        String str = callingPackage;
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.AUDIO_SETSTREAMVOLUME, new Object[0]);
        if (i == 10 && !canChangeAccessibilityVolume()) {
            Log.w(TAG, "Trying to call setStreamVolume() for a11y without CHANGE_ACCESSIBILITY_VOLUME  callingPackage=" + str);
        } else if (i == 0 && index == 0 && this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE") != 0) {
            Log.w(TAG, "Trying to call setStreamVolume() for STREAM_VOICE_CALL and index 0 without MODIFY_PHONE_STATE  callingPackage=" + str);
        } else {
            AudioEventLogger audioEventLogger = this.mVolumeLogger;
            String str2 = str;
            AudioServiceEvents.VolumeEvent volumeEvent = new AudioServiceEvents.VolumeEvent(2, i, index, flags, str2);
            audioEventLogger.log(volumeEvent);
            setStreamVolume(i, index, flags, str, str2, Binder.getCallingUid());
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x002b, code lost:
        return false;
     */
    private boolean canChangeAccessibilityVolume() {
        synchronized (this.mAccessibilityServiceUidsLock) {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.CHANGE_ACCESSIBILITY_VOLUME") == 0) {
                return true;
            }
            if (this.mAccessibilityServiceUids != null) {
                int callingUid = Binder.getCallingUid();
                for (int i : this.mAccessibilityServiceUids) {
                    if (i == callingUid) {
                        return true;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:108:0x0193, code lost:
        if ((r16 & 1) == 0) goto L_0x019d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:110:0x0197, code lost:
        if (r7.mScreenOn != false) goto L_0x019d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:111:0x0199, code lost:
        r0 = r16 & -2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:112:0x019d, code lost:
        r0 = r16;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:113:0x019f, code lost:
        sendVolumeUpdate(r8, r14, r15, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:114:0x01a4, code lost:
        if (LOUD_VOICE_MODE_SUPPORT == false) goto L_0x01c3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:115:0x01a6, code lost:
        r13 = r7.mAudioHandler;
        r1 = new com.android.server.audio.AbsAudioService.DeviceVolumeState(r7, 0, r11, r14, r8);
        sendMsg(r13, 10001, 0, 1, 0, r1, 0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:116:0x01c3, code lost:
        return;
     */
    /* JADX WARNING: Removed duplicated region for block: B:101:0x0162 A[Catch:{ all -> 0x01c9 }] */
    /* JADX WARNING: Removed duplicated region for block: B:105:0x0178 A[Catch:{ all -> 0x01c4 }] */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x010f A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x0128 A[Catch:{ all -> 0x00e2 }] */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x012d A[Catch:{ all -> 0x00e2 }] */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x0141 A[Catch:{ all -> 0x00e2 }] */
    public void setStreamVolume(int streamType, int index, int flags, String callingPackage, String caller, int uid) {
        int uid2;
        int index2;
        int index3;
        int flags2;
        int flags3;
        int index4;
        int index5;
        int i = streamType;
        int index6 = index;
        int i2 = flags;
        String str = callingPackage;
        if (checkEnbaleVolumeAdjust()) {
            if (DEBUG_VOL) {
                Log.d(TAG, "setStreamVolume(stream=" + i + ", index=" + index6 + ", calling=" + str + ")");
            }
            if (!this.mUseFixedVolume) {
                ensureValidStreamType(streamType);
                int streamTypeAlias = mStreamVolumeAlias[i];
                VolumeStreamState streamState = this.mStreamStates[streamTypeAlias];
                if (this.mCust == null || !this.mCust.isTurningAllSound()) {
                    int device = getDeviceForStream(streamTypeAlias);
                    if ((device & 896) != 0 || (i2 & 64) == 0) {
                        int i3 = uid;
                        if (i3 == 1000) {
                            uid2 = UserHandle.getUid(getCurrentUserId(), UserHandle.getAppId(uid));
                        } else {
                            uid2 = i3;
                        }
                        if (this.mAppOps.noteOp(STREAM_VOLUME_OPS[streamTypeAlias], uid2, str) == 0) {
                            if (isAndroidNPlus(str) && wouldToggleZenMode(getNewRingerMode(streamTypeAlias, index6, i2)) && !this.mNm.isNotificationPolicyAccessGrantedForPackage(str)) {
                                throw new SecurityException("Not allowed to change Do Not Disturb state");
                            } else if (volumeAdjustmentAllowedByDnd(streamTypeAlias, i2)) {
                                synchronized (this.mSafeMediaVolumeState) {
                                    try {
                                        this.mPendingVolumeCommand = null;
                                        int oldIndex = streamState.getIndex(device);
                                        if (index6 < (streamState.getMinIndex() + 5) / 10) {
                                            try {
                                                index5 = (streamState.getMinIndex() + 5) / 10;
                                            } catch (Throwable th) {
                                                th = th;
                                                VolumeStreamState volumeStreamState = streamState;
                                            }
                                        } else {
                                            if (index6 > (streamState.getMaxIndex() + 5) / 10) {
                                                index5 = (streamState.getMaxIndex() + 5) / 10;
                                            }
                                            index2 = rescaleIndex(index6 * 10, i, streamTypeAlias);
                                            if (streamTypeAlias == 3 && (device & 896) != 0 && (i2 & 64) == 0) {
                                                synchronized (this.mA2dpAvrcpLock) {
                                                    if (this.mA2dp != null && this.mAvrcpAbsVolSupported) {
                                                        this.mA2dp.setAvrcpAbsoluteVolume(index2 / 10);
                                                    }
                                                }
                                            }
                                            if ((134217728 & device) != 0) {
                                                setHearingAidVolume(index2, i);
                                            }
                                            if (streamTypeAlias == 3) {
                                                setSystemAudioVolume(oldIndex, index2, getStreamMaxVolume(streamType), i2);
                                            }
                                            int flags4 = i2 & -33;
                                            if (streamTypeAlias == 3 && (this.mFixedVolumeDevices & device) != 0) {
                                                flags4 |= 32;
                                                if (index2 != 0) {
                                                    if (this.mSafeMediaVolumeState.intValue() != 3 || (603979788 & device) == 0) {
                                                        index4 = streamState.getMaxIndex();
                                                    } else {
                                                        index4 = safeMediaVolumeIndex(device);
                                                    }
                                                    index3 = index4;
                                                    flags2 = flags4;
                                                    if (!checkSafeMediaVolume(streamTypeAlias, index3, device)) {
                                                        this.mVolumeController.postDisplaySafeVolumeWarning(flags2);
                                                        r1 = r1;
                                                        flags3 = flags2;
                                                        VolumeStreamState volumeStreamState2 = streamState;
                                                        try {
                                                            StreamVolumeCommand streamVolumeCommand = new StreamVolumeCommand(i, index3, flags2, device);
                                                            this.mPendingVolumeCommand = streamVolumeCommand;
                                                        } catch (Throwable th2) {
                                                            th = th2;
                                                            int i4 = index3;
                                                            int i5 = flags3;
                                                            while (true) {
                                                                try {
                                                                    break;
                                                                } catch (Throwable th3) {
                                                                    th = th3;
                                                                }
                                                            }
                                                            throw th;
                                                        }
                                                    } else {
                                                        flags3 = flags2;
                                                        VolumeStreamState volumeStreamState3 = streamState;
                                                        onSetStreamVolume(i, index3, flags3, device, caller);
                                                        index3 = this.mStreamStates[i].getIndex(device);
                                                    }
                                                }
                                            }
                                            index3 = index2;
                                            flags2 = flags4;
                                            if (!checkSafeMediaVolume(streamTypeAlias, index3, device)) {
                                            }
                                        }
                                        index6 = index5;
                                        index2 = rescaleIndex(index6 * 10, i, streamTypeAlias);
                                        synchronized (this.mA2dpAvrcpLock) {
                                        }
                                        if ((134217728 & device) != 0) {
                                        }
                                        if (streamTypeAlias == 3) {
                                        }
                                        int flags42 = i2 & -33;
                                        flags42 |= 32;
                                        if (index2 != 0) {
                                        }
                                        index3 = index2;
                                        flags2 = flags42;
                                        try {
                                            if (!checkSafeMediaVolume(streamTypeAlias, index3, device)) {
                                            }
                                        } catch (Throwable th4) {
                                            th = th4;
                                            VolumeStreamState volumeStreamState4 = streamState;
                                            int i6 = index3;
                                            int i7 = flags2;
                                            while (true) {
                                                break;
                                            }
                                            throw th;
                                        }
                                    } catch (Throwable th5) {
                                        th = th5;
                                        VolumeStreamState volumeStreamState5 = streamState;
                                        while (true) {
                                            break;
                                        }
                                        throw th;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    sendMsg(this.mAudioHandler, 10, 2, 1, 0, streamState, 0);
                }
            }
        }
    }

    private boolean volumeAdjustmentAllowedByDnd(int streamTypeAlias, int flags) {
        boolean isTotalSilence;
        boolean z = true;
        switch (this.mNm.getZenMode()) {
            case 0:
                return true;
            case 1:
            case 2:
            case 3:
                Log.d(TAG, "isTotalSilence : " + isTotalSilence + ", mRingerAndZenModeMutedStreams :" + this.mRingerAndZenModeMutedStreams);
                if ((isStreamMutedByRingerOrZenMode(streamTypeAlias) || (this.mNm.getZenMode() != 1 && streamTypeAlias == 3)) && streamTypeAlias != getUiSoundsStreamType() && (flags & 2) == 0 && isTotalSilence) {
                    z = false;
                }
                return z;
            default:
                return true;
        }
    }

    public void forceVolumeControlStream(int streamType, IBinder cb) {
        if (DEBUG_VOL) {
            Log.d(TAG, String.format("forceVolumeControlStream(%d)", new Object[]{Integer.valueOf(streamType)}));
        }
        synchronized (this.mForceControlStreamLock) {
            if (!(this.mVolumeControlStream == -1 || streamType == -1)) {
                this.mUserSelectedVolumeControlStream = true;
            }
            this.mVolumeControlStream = streamType;
            if (this.mVolumeControlStream == -1) {
                if (this.mForceControlStreamClient != null) {
                    this.mForceControlStreamClient.release();
                    this.mForceControlStreamClient = null;
                }
                this.mUserSelectedVolumeControlStream = false;
            } else if (this.mForceControlStreamClient == null) {
                this.mForceControlStreamClient = new ForceControlStreamClient(cb);
            } else if (this.mForceControlStreamClient.getBinder() == cb) {
                Log.d(TAG, "forceVolumeControlStream cb:" + cb + " is already linked.");
            } else {
                this.mForceControlStreamClient.release();
                this.mForceControlStreamClient = new ForceControlStreamClient(cb);
            }
        }
    }

    /* access modifiers changed from: private */
    public void sendBroadcastToAll(Intent intent) {
        intent.addFlags(67108864);
        intent.addFlags(268435456);
        long ident = Binder.clearCallingIdentity();
        try {
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* access modifiers changed from: private */
    public void sendStickyBroadcastToAll(Intent intent) {
        intent.addFlags(268435456);
        long ident = Binder.clearCallingIdentity();
        try {
            this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private int getCurrentUserId() {
        long ident = Binder.clearCallingIdentity();
        try {
            UserInfo currentUser = ActivityManager.getService().getCurrentUser();
            if (currentUser == null) {
                Log.e(TAG, "currentUser is null");
                Binder.restoreCallingIdentity(ident);
                return 0;
            }
            int i = currentUser.id;
            Binder.restoreCallingIdentity(ident);
            return i;
        } catch (RemoteException e) {
        } catch (Throwable currentUser2) {
            Binder.restoreCallingIdentity(ident);
            throw currentUser2;
        }
    }

    /* access modifiers changed from: protected */
    public void sendVolumeUpdate(int streamType, int oldIndex, int index, int flags) {
        int streamType2 = mStreamVolumeAlias[streamType];
        if (streamType2 == 3) {
            flags = updateFlagsForSystemAudio(flags);
        }
        this.mVolumeController.postVolumeChanged(streamType2, flags);
    }

    private int updateFlagsForSystemAudio(int flags) {
        if (this.mHdmiTvClient != null) {
            synchronized (this.mHdmiTvClient) {
                if (this.mHdmiSystemAudioSupported && (flags & 256) == 0) {
                    flags &= -2;
                }
            }
        }
        return flags;
    }

    private void sendMasterMuteUpdate(boolean muted, int flags) {
        this.mVolumeController.postMasterMuteChanged(updateFlagsForSystemAudio(flags));
        broadcastMasterMuteStatus(muted);
    }

    private void broadcastMasterMuteStatus(boolean muted) {
        Intent intent = new Intent("android.media.MASTER_MUTE_CHANGED_ACTION");
        intent.putExtra("android.media.EXTRA_MASTER_VOLUME_MUTED", muted);
        intent.addFlags(603979776);
        sendStickyBroadcastToAll(intent);
    }

    private void setStreamVolumeInt(int streamType, int index, int device, boolean force, String caller) {
        VolumeStreamState streamState = this.mStreamStates[streamType];
        if (streamState.setIndex(index, device, caller) || force) {
            sendMsg(this.mAudioHandler, 0, 2, device, 0, streamState, 0);
        }
    }

    private void setSystemAudioMute(boolean state) {
        if (this.mHdmiManager != null && this.mHdmiTvClient != null) {
            synchronized (this.mHdmiManager) {
                if (this.mHdmiSystemAudioSupported) {
                    synchronized (this.mHdmiTvClient) {
                        long token = Binder.clearCallingIdentity();
                        try {
                            this.mHdmiTvClient.setSystemAudioMute(state);
                        } finally {
                            Binder.restoreCallingIdentity(token);
                        }
                    }
                }
            }
        }
    }

    public boolean isStreamMute(int streamType) {
        boolean access$500;
        if (streamType == Integer.MIN_VALUE) {
            streamType = getActiveStreamType(streamType);
        }
        synchronized (VolumeStreamState.class) {
            ensureValidStreamType(streamType);
            access$500 = this.mStreamStates[streamType].mIsMuted;
        }
        return access$500;
    }

    private boolean discardRmtSbmxFullVolDeathHandlerFor(IBinder cb) {
        Iterator<RmtSbmxFullVolDeathHandler> it = this.mRmtSbmxFullVolDeathHandlers.iterator();
        while (it.hasNext()) {
            RmtSbmxFullVolDeathHandler handler = it.next();
            if (handler.isHandlerFor(cb)) {
                handler.forget();
                this.mRmtSbmxFullVolDeathHandlers.remove(handler);
                return true;
            }
        }
        return false;
    }

    private boolean hasRmtSbmxFullVolDeathHandlerFor(IBinder cb) {
        Iterator<RmtSbmxFullVolDeathHandler> it = this.mRmtSbmxFullVolDeathHandlers.iterator();
        while (it.hasNext()) {
            if (it.next().isHandlerFor(cb)) {
                return true;
            }
        }
        return false;
    }

    public void forceRemoteSubmixFullVolume(boolean startForcing, IBinder cb) {
        if (cb != null) {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.CAPTURE_AUDIO_OUTPUT") != 0) {
                Log.w(TAG, "Trying to call forceRemoteSubmixFullVolume() without CAPTURE_AUDIO_OUTPUT");
                return;
            }
            synchronized (this.mRmtSbmxFullVolDeathHandlers) {
                boolean applyRequired = false;
                if (startForcing) {
                    try {
                        if (!hasRmtSbmxFullVolDeathHandlerFor(cb)) {
                            this.mRmtSbmxFullVolDeathHandlers.add(new RmtSbmxFullVolDeathHandler(cb));
                            if (this.mRmtSbmxFullVolRefCount == 0) {
                                this.mFullVolumeDevices |= 32768;
                                this.mFixedVolumeDevices |= 32768;
                                applyRequired = true;
                            }
                            this.mRmtSbmxFullVolRefCount++;
                        }
                    } catch (Throwable th) {
                        throw th;
                    }
                } else if (discardRmtSbmxFullVolDeathHandlerFor(cb) && this.mRmtSbmxFullVolRefCount > 0) {
                    this.mRmtSbmxFullVolRefCount--;
                    if (this.mRmtSbmxFullVolRefCount == 0) {
                        this.mFullVolumeDevices &= -32769;
                        this.mFixedVolumeDevices &= -32769;
                        applyRequired = true;
                    }
                }
                if (applyRequired) {
                    checkAllFixedVolumeDevices(3);
                    this.mStreamStates[3].applyAllVolumes();
                }
            }
        }
    }

    private void setMasterMuteInternal(boolean mute, int flags, String callingPackage, int uid, int userId) {
        if (uid == 1000) {
            uid = UserHandle.getUid(userId, UserHandle.getAppId(uid));
        }
        if (!mute && this.mAppOps.noteOp(33, uid, callingPackage) != 0) {
            return;
        }
        if (userId == UserHandle.getCallingUserId() || this.mContext.checkCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL") == 0) {
            setMasterMuteInternalNoCallerCheck(mute, flags, userId);
        }
    }

    /* access modifiers changed from: private */
    public void setMasterMuteInternalNoCallerCheck(boolean mute, int flags, int userId) {
        if (DEBUG_VOL) {
            Log.d(TAG, String.format("Master mute %s, %d, user=%d", new Object[]{Boolean.valueOf(mute), Integer.valueOf(flags), Integer.valueOf(userId)}));
        }
        if ((isPlatformAutomotive() || !this.mUseFixedVolume) && getCurrentUserId() == userId && mute != AudioSystem.getMasterMute()) {
            setSystemAudioMute(mute);
            AudioSystem.setMasterMute(mute);
            sendMasterMuteUpdate(mute, flags);
            Intent intent = new Intent("android.media.MASTER_MUTE_CHANGED_ACTION");
            intent.putExtra("android.media.EXTRA_MASTER_VOLUME_MUTED", mute);
            sendBroadcastToAll(intent);
        }
    }

    public boolean isMasterMute() {
        return AudioSystem.getMasterMute();
    }

    public void setMasterMute(boolean mute, int flags, String callingPackage, int userId) {
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.AUDIO_SETMASTERMUTE, new Object[0]);
        setMasterMuteInternal(mute, flags, callingPackage, Binder.getCallingUid(), userId);
    }

    public int getStreamVolume(int streamType) {
        int i;
        ensureValidStreamType(streamType);
        int device = getDeviceForStream(streamType);
        synchronized (VolumeStreamState.class) {
            int index = this.mStreamStates[streamType].getIndex(device);
            if (this.mStreamStates[streamType].mIsMuted) {
                index = 0;
            }
            if (!(index == 0 || mStreamVolumeAlias[streamType] != 3 || (this.mFixedVolumeDevices & device) == 0)) {
                index = this.mStreamStates[streamType].getMaxIndex();
            }
            i = (index + 5) / 10;
        }
        return i;
    }

    public int getStreamMaxVolume(int streamType) {
        ensureValidStreamType(streamType);
        return (this.mStreamStates[streamType].getMaxIndex() + 5) / 10;
    }

    public int getStreamMinVolume(int streamType) {
        ensureValidStreamType(streamType);
        return (this.mStreamStates[streamType].getMinIndex() + 5) / 10;
    }

    public int getLastAudibleStreamVolume(int streamType) {
        ensureValidStreamType(streamType);
        return (this.mStreamStates[streamType].getIndex(getDeviceForStream(streamType)) + 5) / 10;
    }

    public int getUiSoundsStreamType() {
        return mStreamVolumeAlias[1];
    }

    public void setMicrophoneMute(boolean on, String callingPackage, int userId) {
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.AUDIO_SETMICROPHONEMUTE, new Object[0]);
        int uid = Binder.getCallingUid();
        if (uid == 1000) {
            uid = UserHandle.getUid(userId, UserHandle.getAppId(uid));
        }
        if ((!on && this.mAppOps.noteOp(44, uid, callingPackage) != 0) || !checkAudioSettingsPermission("setMicrophoneMute()")) {
            return;
        }
        if (userId == UserHandle.getCallingUserId() || this.mContext.checkCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL") == 0) {
            setMicrophoneMuteNoCallerCheck(on, userId);
        }
    }

    /* access modifiers changed from: private */
    public void setMicrophoneMuteNoCallerCheck(boolean on, int userId) {
        if (DEBUG_VOL) {
            Log.d(TAG, String.format("Mic mute %s, user=%d", new Object[]{Boolean.valueOf(on), Integer.valueOf(userId)}));
        }
        if (checkAudioSettingAllowed("ASsmm" + on) && getCurrentUserId() == userId) {
            boolean currentMute = AudioSystem.isMicrophoneMuted();
            long identity = Binder.clearCallingIdentity();
            AudioSystem.muteMicrophone(on);
            Binder.restoreCallingIdentity(identity);
            if (on != currentMute) {
                this.mContext.sendBroadcast(new Intent("android.media.action.MICROPHONE_MUTE_CHANGED").setFlags(1073741824));
            }
        }
    }

    public int getRingerModeExternal() {
        int i;
        synchronized (this.mSettingsLock) {
            i = this.mRingerModeExternal;
        }
        return i;
    }

    public int getRingerModeInternal() {
        int i;
        synchronized (this.mSettingsLock) {
            i = this.mRingerMode;
        }
        return i;
    }

    private void ensureValidRingerMode(int ringerMode) {
        if (!isValidRingerMode(ringerMode)) {
            throw new IllegalArgumentException("Bad ringer mode " + ringerMode);
        }
    }

    public boolean isValidRingerMode(int ringerMode) {
        return ringerMode >= 0 && ringerMode <= 2;
    }

    public void setRingerModeExternal(int ringerMode, String caller) {
        if (!isAndroidNPlus(caller) || !wouldToggleZenMode(ringerMode) || this.mNm.isNotificationPolicyAccessGrantedForPackage(caller)) {
            setRingerMode(ringerMode, caller, true);
            return;
        }
        throw new SecurityException("Not allowed to change Do Not Disturb state");
    }

    public void setRingerModeInternal(int ringerMode, String caller) {
        enforceVolumeController("setRingerModeInternal");
        setRingerMode(ringerMode, caller, false);
    }

    public void silenceRingerModeInternal(String reason) {
        VibrationEffect effect = null;
        int ringerMode = 0;
        int toastText = 0;
        int silenceRingerSetting = 0;
        if (this.mContext.getResources().getBoolean(17957069)) {
            silenceRingerSetting = Settings.Secure.getIntForUser(this.mContentResolver, "volume_hush_gesture", 0, -2);
        }
        switch (silenceRingerSetting) {
            case 1:
                effect = VibrationEffect.get(5);
                ringerMode = 1;
                toastText = 17041325;
                break;
            case 2:
                effect = VibrationEffect.get(1);
                ringerMode = 0;
                toastText = 17041324;
                break;
        }
        maybeVibrate(effect);
        setRingerModeInternal(ringerMode, reason);
        Toast.makeText(this.mContext, toastText, 0).show();
    }

    private boolean maybeVibrate(VibrationEffect effect) {
        if (!this.mHasVibrator) {
            return false;
        }
        if ((Settings.System.getIntForUser(this.mContext.getContentResolver(), "haptic_feedback_enabled", 0, -2) == 0) || effect == null) {
            return false;
        }
        this.mVibrator.vibrate(Binder.getCallingUid(), this.mContext.getOpPackageName(), effect, VIBRATION_ATTRIBUTES);
        return true;
    }

    private void setRingerMode(int ringerMode, String caller, boolean external) {
        if (!checkEnbaleVolumeAdjust() || this.mUseFixedVolume || this.mIsSingleVolume) {
            return;
        }
        if (caller == null || caller.length() == 0) {
            throw new IllegalArgumentException("Bad caller: " + caller);
        }
        ensureValidRingerMode(ringerMode);
        if (ringerMode == 1 && !this.mHasVibrator) {
            ringerMode = 0;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            int ringerModeInternal = getRingerModeInternal();
            int ringerModeExternal = getRingerModeExternal();
            if (external) {
                setRingerModeExt(ringerMode);
                if (this.mRingerModeDelegate != null) {
                    ringerMode = this.mRingerModeDelegate.onSetRingerModeExternal(ringerModeExternal, ringerMode, caller, ringerModeInternal, this.mVolumePolicy);
                }
                synchronized (this.mSettingsLock) {
                    if (ringerMode != ringerModeInternal) {
                        setRingerModeInt(ringerMode, true);
                    }
                }
            } else {
                synchronized (this.mSettingsLock) {
                    if (ringerMode != ringerModeInternal) {
                        setRingerModeInt(ringerMode, true);
                    }
                }
                if (this.mRingerModeDelegate != null) {
                    ringerMode = this.mRingerModeDelegate.onSetRingerModeInternal(ringerModeInternal, ringerMode, caller, ringerModeExternal, this.mVolumePolicy);
                }
                setRingerModeExt(ringerMode);
            }
            Binder.restoreCallingIdentity(identity);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
    }

    private void setRingerModeExt(int ringerMode) {
        synchronized (this.mSettingsLock) {
            if (ringerMode != this.mRingerModeExternal) {
                this.mRingerModeExternal = ringerMode;
                broadcastRingerMode("android.media.RINGER_MODE_CHANGED", ringerMode);
            }
        }
    }

    @GuardedBy("mSettingsLock")
    private void muteRingerModeStreams() {
        int ringerMode;
        int numStreamTypes;
        boolean ringerMode2;
        int ringerMode3;
        int numStreamTypes2 = AudioSystem.getNumStreamTypes();
        if (this.mNm == null) {
            this.mNm = (NotificationManager) this.mContext.getSystemService("notification");
        }
        int ringerMode4 = this.mRingerMode;
        boolean z = true;
        boolean ringerModeMute = ringerMode4 == 1 || ringerMode4 == 0;
        boolean shouldRingSco = ringerMode4 == 1 && isBluetoothScoOn();
        sendMsg(this.mAudioHandler, 8, 2, 7, shouldRingSco ? 3 : 0, "muteRingerModeStreams() from u/pid:" + Binder.getCallingUid() + SliceClientPermissions.SliceAuthority.DELIMITER + Binder.getCallingPid(), 0);
        int streamType = numStreamTypes2 - 1;
        while (streamType >= 0) {
            boolean isMuted = isStreamMutedByRingerOrZenMode(streamType);
            boolean shouldMute = (shouldZenMuteStream(streamType) || (ringerModeMute && isStreamAffectedByRingerMode(streamType) && ((!shouldRingSco || streamType != 2) ? z : false))) ? z : false;
            if (isMuted == shouldMute) {
                numStreamTypes = numStreamTypes2;
                ringerMode = ringerMode4;
                ringerMode2 = z;
            } else {
                if (this.mStreamStates[streamType] == null || shouldMute) {
                    numStreamTypes = numStreamTypes2;
                    ringerMode = ringerMode4;
                    if (this.mStreamStates[streamType] != null) {
                        ringerMode2 = true;
                        this.mStreamStates[streamType].mute(true);
                        this.mRingerAndZenModeMutedStreams |= 1 << streamType;
                    }
                } else {
                    if (mStreamVolumeAlias[streamType] == 2) {
                        synchronized (this.mSettingsLock) {
                            try {
                                synchronized (VolumeStreamState.class) {
                                    try {
                                        VolumeStreamState vss = this.mStreamStates[streamType];
                                        int i = 0;
                                        while (true) {
                                            int i2 = i;
                                            if (i2 >= vss.mIndexMap.size()) {
                                                break;
                                            }
                                            int device = vss.mIndexMap.keyAt(i2);
                                            int numStreamTypes3 = numStreamTypes2;
                                            try {
                                                int value = vss.mIndexMap.valueAt(i2);
                                                if (value == 0) {
                                                    int i3 = value;
                                                    ringerMode3 = ringerMode4;
                                                    vss.setIndex(10, device, TAG);
                                                } else {
                                                    ringerMode3 = ringerMode4;
                                                }
                                                i = i2 + 1;
                                                numStreamTypes2 = numStreamTypes3;
                                                ringerMode4 = ringerMode3;
                                            } catch (Throwable th) {
                                                th = th;
                                                throw th;
                                            }
                                        }
                                        numStreamTypes = numStreamTypes2;
                                        ringerMode = ringerMode4;
                                        sendMsg(this.mAudioHandler, 1, 2, getDeviceForStream(streamType), 0, this.mStreamStates[streamType], 500);
                                    } catch (Throwable th2) {
                                        th = th2;
                                        int i4 = numStreamTypes2;
                                        int i5 = ringerMode4;
                                        throw th;
                                    }
                                }
                            } catch (Throwable th3) {
                                th = th3;
                            }
                        }
                    } else {
                        numStreamTypes = numStreamTypes2;
                        ringerMode = ringerMode4;
                    }
                    this.mStreamStates[streamType].mute(false);
                    this.mRingerAndZenModeMutedStreams &= ~(1 << streamType);
                }
                ringerMode2 = true;
            }
            streamType--;
            z = ringerMode2;
            numStreamTypes2 = numStreamTypes;
            ringerMode4 = ringerMode;
        }
        int i6 = ringerMode4;
        return;
        throw th;
    }

    private boolean isAlarm(int streamType) {
        return streamType == 4;
    }

    private boolean isNotificationOrRinger(int streamType) {
        return streamType == 5 || streamType == 2;
    }

    private boolean isMedia(int streamType) {
        return streamType == 3;
    }

    private boolean isSystem(int streamType) {
        return streamType == 1;
    }

    /* access modifiers changed from: private */
    public void setRingerModeInt(int ringerMode, boolean persist) {
        boolean change;
        synchronized (this.mSettingsLock) {
            change = this.mRingerMode != ringerMode;
            this.mRingerMode = ringerMode;
            muteRingerModeStreams();
        }
        if (persist) {
            sendMsg(this.mAudioHandler, 3, 0, 0, 0, null, 500);
        }
        if (change) {
            broadcastRingerMode("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION", ringerMode);
        }
    }

    public boolean shouldVibrate(int vibrateType) {
        boolean z = false;
        if (!this.mHasVibrator) {
            return false;
        }
        switch (getVibrateSetting(vibrateType)) {
            case 0:
                return false;
            case 1:
                if (getRingerModeExternal() != 0) {
                    z = true;
                }
                return z;
            case 2:
                if (getRingerModeExternal() == 1) {
                    z = true;
                }
                return z;
            default:
                return false;
        }
    }

    public int getVibrateSetting(int vibrateType) {
        if (!this.mHasVibrator) {
            return 0;
        }
        return (this.mVibrateSetting >> (vibrateType * 2)) & 3;
    }

    public void setVibrateSetting(int vibrateType, int vibrateSetting) {
        if (this.mHasVibrator) {
            this.mVibrateSetting = AudioSystem.getValueForVibrateSetting(this.mVibrateSetting, vibrateType, vibrateSetting);
            broadcastVibrateSetting(vibrateType);
        }
    }

    private void dumpAudioMode(PrintWriter pw) {
        pw.println("\nAudio Mode:");
        synchronized (this.mSetModeDeathHandlers) {
            Iterator iter = this.mSetModeDeathHandlers.iterator();
            while (iter.hasNext()) {
                iter.next().dump(pw);
            }
        }
    }

    public void setMode(int mode, IBinder cb, String callingPackage) {
        int newModeOwnerPid;
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.AUDIO_SETMODE, new Object[0]);
        Log.v(TAG, "setMode(mode=" + mode + ", callingPackage=" + callingPackage + ")");
        if (checkAudioSettingsPermission("setMode()")) {
            if (DUAL_SMARTPA_SUPPORT || DUAL_SMARTPA_DELAY) {
                checkMuteRcvDelay(this.mMode, mode);
            }
            if (mode == 2 && this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE") != 0) {
                Log.w(TAG, "MODIFY_PHONE_STATE Permission Denial: setMode(MODE_IN_CALL) from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            } else if (mode >= -1 && mode < 4) {
                if (this.mMode == 2 && mode == 3 && this.mIsHisiPlatform) {
                    synchronized (this.mSetModeDeathHandlers) {
                        int pid = Binder.getCallingPid();
                        boolean flag = true;
                        Iterator iter = this.mSetModeDeathHandlers.iterator();
                        while (true) {
                            if (!iter.hasNext()) {
                                break;
                            }
                            SetModeDeathHandler h = iter.next();
                            if (h.getPid() == pid && h.getMode() == 2) {
                                flag = false;
                                Log.w(TAG, "reset the mode by the same pid");
                                break;
                            }
                        }
                        if (flag) {
                            Log.w(TAG, "Forbid set MODE_IN_COMMUNICATION when current mode is MODE_IN_CALL from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                            return;
                        }
                    }
                }
                int oldModeOwnerPid = 0;
                synchronized (this.mSetModeDeathHandlers) {
                    if (!this.mSetModeDeathHandlers.isEmpty()) {
                        oldModeOwnerPid = this.mSetModeDeathHandlers.get(0).getPid();
                    }
                    if (mode == -1) {
                        mode = this.mMode;
                    }
                    newModeOwnerPid = setModeInt(mode, cb, Binder.getCallingPid(), callingPackage);
                }
                if (!(newModeOwnerPid == oldModeOwnerPid || newModeOwnerPid == 0)) {
                    disconnectBluetoothSco(newModeOwnerPid);
                }
                if (LOUD_VOICE_MODE_SUPPORT) {
                    getOldInCallDevice(mode);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x00f8  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0139  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x014f  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x01ae  */
    public int setModeInt(int mode, IBinder cb, int pid, String caller) {
        IBinder cb2;
        SetModeDeathHandler hdlr;
        int mode2;
        int status;
        int newModeOwnerPid;
        int i = pid;
        String str = caller;
        StringBuilder sb = new StringBuilder();
        sb.append("setModeInt(mode=");
        int mode3 = mode;
        sb.append(mode3);
        sb.append(", pid=");
        sb.append(i);
        sb.append(", caller=");
        sb.append(str);
        sb.append(")");
        Log.v(TAG, sb.toString());
        int newModeOwnerPid2 = 0;
        if (cb == null) {
            Log.e(TAG, "setModeInt() called with null binder");
            return 0;
        }
        SetModeDeathHandler hdlr2 = null;
        Iterator iter = this.mSetModeDeathHandlers.iterator();
        while (true) {
            Iterator iter2 = iter;
            if (!iter2.hasNext()) {
                break;
            }
            SetModeDeathHandler h = iter2.next();
            if (h.getPid() == i) {
                hdlr2 = h;
                iter2.remove();
                hdlr2.getBinder().unlinkToDeath(hdlr2, 0);
                break;
            }
            iter = iter2;
        }
        IBinder cb3 = cb;
        int status2 = 0;
        while (true) {
            int i2 = status2;
            int actualMode = mode3;
            if (mode3 != 0) {
                if (hdlr2 == null) {
                    hdlr2 = new SetModeDeathHandler(cb3, i);
                }
                try {
                    cb3.linkToDeath(hdlr2, 0);
                } catch (RemoteException e) {
                    RemoteException remoteException = e;
                    Log.w(TAG, "setMode() could not link to " + cb3 + " binder death");
                }
                this.mSetModeDeathHandlers.add(0, hdlr2);
                hdlr2.setMode(mode3);
            } else if (!this.mSetModeDeathHandlers.isEmpty()) {
                hdlr = this.mSetModeDeathHandlers.get(0);
                IBinder cb4 = hdlr.getBinder();
                int actualMode2 = hdlr.getMode();
                Log.w(TAG, " using mode=" + mode3 + " instead due to death hdlr at pid=" + hdlr.mPid + ";pkgName=" + getPackageNameByPid(hdlr.mPid));
                cb2 = cb4;
                actualMode = actualMode2;
                if (actualMode == this.mMode) {
                    long identity = Binder.clearCallingIdentity();
                    int status3 = AudioSystem.setPhoneState(actualMode);
                    Binder.restoreCallingIdentity(identity);
                    if (status3 == 0) {
                        Log.v(TAG, " mode successfully set to " + actualMode);
                        this.mMode = actualMode;
                        this.mHwAudioServiceEx.dipatchAudioModeChanged(actualMode);
                    } else {
                        if (hdlr != null) {
                            this.mSetModeDeathHandlers.remove(hdlr);
                            cb2.unlinkToDeath(hdlr, 0);
                        }
                        Log.w(TAG, " mode set to MODE_NORMAL after phoneState pb");
                        mode3 = 0;
                    }
                    mode2 = mode3;
                    status = status3;
                } else {
                    mode2 = mode3;
                    status = 0;
                }
                if (status != 0 && !this.mSetModeDeathHandlers.isEmpty()) {
                    hdlr2 = hdlr;
                    cb3 = cb2;
                    status2 = status;
                    mode3 = mode2;
                } else if (status != 0) {
                    if (actualMode != 0) {
                        if (this.mSetModeDeathHandlers.isEmpty()) {
                            Log.e(TAG, "setMode() different from MODE_NORMAL with empty mode client stack");
                        } else {
                            newModeOwnerPid2 = this.mSetModeDeathHandlers.get(0).getPid();
                        }
                    }
                    newModeOwnerPid = newModeOwnerPid2;
                    SetModeDeathHandler setModeDeathHandler = hdlr;
                    AudioServiceEvents.PhoneStateEvent phoneStateEvent = r1;
                    AudioEventLogger audioEventLogger = this.mModeLogger;
                    AudioServiceEvents.PhoneStateEvent phoneStateEvent2 = new AudioServiceEvents.PhoneStateEvent(str, i, mode2, newModeOwnerPid, actualMode);
                    audioEventLogger.log(phoneStateEvent);
                    int streamType = getActiveStreamType(Integer.MIN_VALUE);
                    int device = getDeviceForStream(streamType);
                    setStreamVolumeInt(mStreamVolumeAlias[streamType], this.mStreamStates[mStreamVolumeAlias[streamType]].getIndex(device), device, true, str);
                    updateStreamVolumeAlias(true, str);
                    updateAftPolicy();
                } else {
                    newModeOwnerPid = 0;
                }
            }
            hdlr = hdlr2;
            cb2 = cb3;
            if (actualMode == this.mMode) {
            }
            if (status != 0) {
                break;
            }
            break;
        }
        if (status != 0) {
        }
        return newModeOwnerPid;
    }

    public int getMode() {
        return this.mMode;
    }

    private void loadTouchSoundAssetDefaults() {
        SOUND_EFFECT_FILES.add("Effect_Tick.ogg");
        for (int i = 0; i < 10; i++) {
            this.SOUND_EFFECT_FILES_MAP[i][0] = 0;
            this.SOUND_EFFECT_FILES_MAP[i][1] = -1;
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00b9, code lost:
        if (r0 != null) goto L_0x00bb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00df, code lost:
        if (r0 == null) goto L_0x00e2;
     */
    public void loadTouchSoundAssets() {
        XmlResourceParser parser = null;
        if (SOUND_EFFECT_FILES.isEmpty()) {
            loadTouchSoundAssetDefaults();
            try {
                parser = this.mContext.getResources().getXml(18284545);
                XmlUtils.beginDocument(parser, TAG_AUDIO_ASSETS);
                boolean inTouchSoundsGroup = false;
                if (ASSET_FILE_VERSION.equals(parser.getAttributeValue(null, ATTR_VERSION))) {
                    while (true) {
                        XmlUtils.nextElement(parser);
                        String element = parser.getName();
                        if (element != null) {
                            if (element.equals(TAG_GROUP) && GROUP_TOUCH_SOUNDS.equals(parser.getAttributeValue(null, "name"))) {
                                inTouchSoundsGroup = true;
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                    while (true) {
                        if (!inTouchSoundsGroup) {
                            break;
                        }
                        XmlUtils.nextElement(parser);
                        String element2 = parser.getName();
                        if (element2 != null) {
                            if (!element2.equals(TAG_ASSET)) {
                                break;
                            }
                            String id = parser.getAttributeValue(null, ATTR_ASSET_ID);
                            String file = parser.getAttributeValue(null, ATTR_ASSET_FILE);
                            try {
                                int fx = AudioManager.class.getField(id).getInt(null);
                                int i = SOUND_EFFECT_FILES.indexOf(file);
                                if (i == -1) {
                                    i = SOUND_EFFECT_FILES.size();
                                    SOUND_EFFECT_FILES.add(file);
                                }
                                this.SOUND_EFFECT_FILES_MAP[fx][0] = i;
                            } catch (Exception e) {
                                Log.w(TAG, "Invalid touch sound ID: " + id);
                            }
                        } else {
                            break;
                        }
                    }
                }
            } catch (Resources.NotFoundException e2) {
                Log.w(TAG, "audio assets file not found", e2);
            } catch (XmlPullParserException e3) {
                Log.w(TAG, "XML parser exception reading touch sound assets", e3);
                if (parser != null) {
                    parser.close();
                }
            } catch (IOException e4) {
                Log.w(TAG, "I/O exception reading touch sound assets", e4);
                if (parser != null) {
                    parser.close();
                }
            } catch (Throwable th) {
                if (parser != null) {
                    parser.close();
                }
                throw th;
            }
        }
    }

    public void playSoundEffect(int effectType) {
        playSoundEffectVolume(effectType, -1.0f);
    }

    public void playSoundEffectVolume(int effectType, float volume) {
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.AUDIO_PLAYSOUNDEFFECTVOLUME, new Object[0]);
        if (!isStreamMutedByRingerOrZenMode(1)) {
            if (effectType >= 10 || effectType < 0) {
                Log.w(TAG, "AudioService effectType value " + effectType + " out of range");
                return;
            }
            sendMsg(this.mAudioHandler, 5, 2, effectType, (int) (1000.0f * volume), null, 0);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0031, code lost:
        if (r1.mStatus != 0) goto L_0x0034;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:?, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:?, code lost:
        return true;
     */
    public boolean loadSoundEffects() {
        int attempts = 3;
        LoadSoundEffectReply reply = new LoadSoundEffectReply();
        synchronized (reply) {
            try {
                sendMsg(this.mAudioHandler, 7, 2, 0, 0, reply, 0);
                while (true) {
                    if (reply.mStatus == 1) {
                        int attempts2 = attempts - 1;
                        if (attempts <= 0) {
                            attempts = attempts2;
                            break;
                        }
                        try {
                            reply.wait(5000);
                        } catch (InterruptedException e) {
                            Log.w(TAG, "loadSoundEffects Interrupted while waiting sound pool loaded.");
                        } catch (Throwable th) {
                            th = th;
                        }
                        attempts = attempts2;
                    }
                }
                break;
            } catch (Throwable th2) {
                Throwable th3 = th2;
                int i = attempts;
                th = th3;
                throw th;
            }
        }
    }

    public void unloadSoundEffects() {
        sendMsg(this.mAudioHandler, 20, 2, 0, 0, null, 0);
    }

    public void reloadAudioSettings() {
        readAudioSettings(false);
    }

    /* access modifiers changed from: private */
    public void readAudioSettings(boolean userSwitch) {
        readPersistedSettings();
        readUserRestrictions();
        int numStreamTypes = AudioSystem.getNumStreamTypes();
        for (int streamType = 0; streamType < numStreamTypes; streamType++) {
            VolumeStreamState streamState = this.mStreamStates[streamType];
            if (!userSwitch || mStreamVolumeAlias[streamType] != 3) {
                streamState.readSettings();
                synchronized (VolumeStreamState.class) {
                    if (streamState.mIsMuted && ((!isStreamAffectedByMute(streamType) && !isStreamMutedByRingerOrZenMode(streamType)) || this.mUseFixedVolume)) {
                        boolean unused = streamState.mIsMuted = false;
                    }
                }
            }
        }
        setRingerModeInt(getRingerModeInternal(), false);
        checkAllFixedVolumeDevices();
        checkAllAliasStreamVolumes();
        checkMuteAffectedStreams();
        synchronized (this.mSafeMediaVolumeState) {
            this.mMusicActiveMs = MathUtils.constrain(Settings.Secure.getIntForUser(this.mContentResolver, "unsafe_volume_music_active_ms", 0, -2), 0, UNSAFE_VOLUME_MUSIC_ACTIVE_MS_MAX);
            if (this.mSafeMediaVolumeState.intValue() == 3) {
                enforceSafeMediaVolume(TAG);
            }
        }
    }

    public void setSpeakerphoneOn(boolean on) {
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.AUDIO_SETSPEAKERPHONEON, new Object[0]);
        if (checkAudioSettingsPermission("setSpeakerphoneOn()")) {
            String eventSource = "setSpeakerphoneOn(" + on + ") from u/pid:" + Binder.getCallingUid() + SliceClientPermissions.SliceAuthority.DELIMITER + Binder.getCallingPid();
            if (checkAudioSettingAllowed("ASsso" + on)) {
                if (on) {
                    if (this.mForcedUseForComm == 3) {
                        sendMsg(this.mAudioHandler, 8, 2, 2, 0, eventSource, 0);
                    }
                    this.mForcedUseForComm = 1;
                } else if (this.mForcedUseForComm == 1) {
                    this.mForcedUseForComm = 0;
                }
                this.mForcedUseForCommExt = this.mForcedUseForComm;
                sendMsg(this.mAudioHandler, 8, 2, 0, this.mForcedUseForComm, eventSource, 0);
                if (LOUD_VOICE_MODE_SUPPORT) {
                    sendMsg(this.mAudioHandler, 10001, 0, 0, 0, null, 500);
                }
                sendCommForceBroadcast();
            }
        }
    }

    public boolean isSpeakerphoneOn() {
        return this.mForcedUseForCommExt == 1;
    }

    public void setBluetoothScoOn(boolean on) {
        Log.i(TAG, "setBluetoothScoOn callingPid: " + Binder.getCallingPid());
        if (checkAudioSettingsPermission("setBluetoothScoOn()")) {
            if (UserHandle.getAppId(Binder.getCallingUid()) >= 10000) {
                int i = 3;
                if (!on) {
                    i = this.mForcedUseForCommExt == 3 ? 0 : this.mForcedUseForCommExt;
                }
                this.mForcedUseForCommExt = i;
                Log.d(TAG, "Only enable calls from system components.");
                return;
            }
            String eventSource = "setBluetoothScoOn(" + on + ") from u/pid:" + Binder.getCallingUid() + SliceClientPermissions.SliceAuthority.DELIMITER + Binder.getCallingPid();
            if (checkAudioSettingAllowed("ASsbso" + on)) {
                setBluetoothScoOnInt(on, eventSource);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0059, code lost:
        r10.mForcedUseForComm = 3;
     */
    public void setBluetoothScoOnInt(boolean on, String eventSource) {
        Log.i(TAG, "setBluetoothScoOnInt: " + on + " " + eventSource);
        if (on) {
            synchronized (this.mScoClients) {
                if (this.mBluetoothHeadset != null && this.mBluetoothHeadset.getAudioState(this.mBluetoothHeadsetDevice) != 12) {
                    this.mForcedUseForCommExt = 3;
                    Log.w(TAG, "setBluetoothScoOnInt(true) failed because " + this.mBluetoothHeadsetDevice + " is not in audio connected mode");
                    return;
                }
            }
        } else if (this.mForcedUseForComm == 3) {
            this.mForcedUseForComm = 0;
        }
        this.mForcedUseForCommExt = this.mForcedUseForComm;
        StringBuilder sb = new StringBuilder();
        sb.append("BT_SCO=");
        sb.append(on ? "on" : "off");
        AudioSystem.setParameters(sb.toString());
        String str = eventSource;
        sendMsg(this.mAudioHandler, 8, 2, 0, this.mForcedUseForComm, str, 0);
        sendMsg(this.mAudioHandler, 8, 2, 2, this.mForcedUseForComm, str, 0);
        setRingerModeInt(getRingerModeInternal(), false);
        if (LOUD_VOICE_MODE_SUPPORT) {
            sendMsg(this.mAudioHandler, 10001, 0, 0, 0, null, 500);
        }
        sendCommForceBroadcast();
    }

    public boolean isBluetoothScoOn() {
        return this.mForcedUseForCommExt == 3;
    }

    public void setBluetoothA2dpOn(boolean on) {
        String eventSource = "setBluetoothA2dpOn(" + on + ") from u/pid:" + Binder.getCallingUid() + SliceClientPermissions.SliceAuthority.DELIMITER + Binder.getCallingPid();
        Log.d(TAG, eventSource);
        synchronized (this.mBluetoothA2dpEnabledLock) {
            if (this.mBluetoothA2dpEnabled != on) {
                this.mBluetoothA2dpEnabled = on;
                sendMsg(this.mAudioHandler, 13, 2, 1, this.mBluetoothA2dpEnabled ? 0 : 10, eventSource, 0);
            }
        }
    }

    public boolean isBluetoothA2dpOn() {
        boolean z;
        synchronized (this.mBluetoothA2dpEnabledLock) {
            z = this.mBluetoothA2dpEnabled;
        }
        return z;
    }

    public void startBluetoothSco(IBinder cb, int targetSdkVersion) {
        Log.i(TAG, "startBluetoothSco callingPid: " + Binder.getCallingPid());
        startBluetoothScoInt(cb, targetSdkVersion < 18 ? 0 : -1);
    }

    public void startBluetoothScoVirtualCall(IBinder cb) {
        Log.i(TAG, "startBluetoothScoVirtualCall callingPid: " + Binder.getCallingPid());
        startBluetoothScoInt(cb, 0);
    }

    /* access modifiers changed from: package-private */
    public void startBluetoothScoInt(IBinder cb, int scoAudioMode) {
        if (checkAudioSettingsPermission("startBluetoothSco()") && this.mSystemReady) {
            ScoClient client = getScoClient(cb, true);
            long ident = Binder.clearCallingIdentity();
            client.incCount(scoAudioMode);
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void stopBluetoothSco(IBinder cb) {
        Log.i(TAG, "stopBluetoothSco callingPid: " + Binder.getCallingPid());
        if (checkAudioSettingsPermission("stopBluetoothSco()") && this.mSystemReady) {
            ScoClient client = getScoClient(cb, false);
            long ident = Binder.clearCallingIdentity();
            if (client != null) {
                client.decCount();
            }
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* access modifiers changed from: private */
    public void checkScoAudioState() {
        synchronized (this.mScoClients) {
            if (!(this.mBluetoothHeadset == null || this.mBluetoothHeadsetDevice == null || this.mScoAudioState != 0 || this.mBluetoothHeadset.getAudioState(this.mBluetoothHeadsetDevice) == 10)) {
                this.mScoAudioState = 2;
            }
        }
    }

    private ScoClient getScoClient(IBinder cb, boolean create) {
        synchronized (this.mScoClients) {
            Iterator<ScoClient> it = this.mScoClients.iterator();
            while (it.hasNext()) {
                ScoClient existingClient = it.next();
                if (existingClient.getBinder() == cb) {
                    return existingClient;
                }
            }
            if (!create) {
                return null;
            }
            ScoClient newClient = new ScoClient(cb);
            this.mScoClients.add(newClient);
            return newClient;
        }
    }

    public void clearAllScoClients(int exceptPid, boolean stopSco) {
        synchronized (this.mScoClients) {
            ScoClient savedClient = null;
            Iterator<ScoClient> it = this.mScoClients.iterator();
            while (it.hasNext()) {
                ScoClient cl = it.next();
                if (cl.getPid() != exceptPid) {
                    cl.clearCount(stopSco);
                } else {
                    savedClient = cl;
                }
            }
            this.mScoClients.clear();
            if (savedClient != null) {
                this.mScoClients.add(savedClient);
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean getBluetoothHeadset() {
        boolean result = false;
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            result = adapter.getProfileProxy(this.mContext, this.mBluetoothProfileServiceListener, 1);
        }
        sendMsg(this.mAudioHandler, 9, 0, 0, 0, null, result ? 3000 : 0);
        return result;
    }

    /* access modifiers changed from: private */
    public void disconnectBluetoothSco(int exceptPid) {
        synchronized (this.mScoClients) {
            checkScoAudioState();
            if (this.mScoAudioState != 2) {
                clearAllScoClients(exceptPid, true);
            }
        }
    }

    /* access modifiers changed from: private */
    public static boolean disconnectBluetoothScoAudioHelper(BluetoothHeadset bluetoothHeadset, BluetoothDevice device, int scoAudioMode) {
        switch (scoAudioMode) {
            case 0:
                return bluetoothHeadset.stopScoUsingVirtualVoiceCall();
            case 1:
                return bluetoothHeadset.disconnectAudio();
            case 2:
                return bluetoothHeadset.stopVoiceRecognition(device);
            default:
                return false;
        }
    }

    /* access modifiers changed from: private */
    public static boolean connectBluetoothScoAudioHelper(BluetoothHeadset bluetoothHeadset, BluetoothDevice device, int scoAudioMode) {
        switch (scoAudioMode) {
            case 0:
                return bluetoothHeadset.startScoUsingVirtualVoiceCall();
            case 1:
                return bluetoothHeadset.connectAudio();
            case 2:
                return bluetoothHeadset.startVoiceRecognition(device);
            default:
                return false;
        }
    }

    /* access modifiers changed from: private */
    public void resetBluetoothSco() {
        synchronized (this.mScoClients) {
            clearAllScoClients(0, false);
            this.mScoAudioState = 0;
            broadcastScoConnectionState(0);
        }
        AudioSystem.setParameters("A2dpSuspended=false");
        setBluetoothScoOnInt(false, "resetBluetoothSco");
    }

    /* access modifiers changed from: private */
    public void broadcastScoConnectionState(int state) {
        sendMsg(this.mAudioHandler, 19, 2, state, 0, null, 0);
    }

    /* access modifiers changed from: private */
    public void onBroadcastScoConnectionState(int state) {
        Log.i(TAG, "onBroadcastScoConnectionState() state=" + state + ", pre-state=" + this.mScoConnectionState);
        if (state != this.mScoConnectionState) {
            Intent newIntent = new Intent("android.media.ACTION_SCO_AUDIO_STATE_UPDATED");
            newIntent.putExtra("android.media.extra.SCO_AUDIO_STATE", state);
            newIntent.putExtra("android.media.extra.SCO_AUDIO_PREVIOUS_STATE", this.mScoConnectionState);
            sendStickyBroadcastToAll(newIntent);
            this.mScoConnectionState = state;
        }
    }

    private boolean handleBtScoActiveDeviceChange(BluetoothDevice btDevice, boolean isActive) {
        boolean result;
        boolean result2 = true;
        if (btDevice == null) {
            return true;
        }
        String address = btDevice.getAddress();
        BluetoothClass btClass = btDevice.getBluetoothClass();
        int[] outDeviceTypes = {16, 32, 64};
        if (btClass != null) {
            int deviceClass = btClass.getDeviceClass();
            if (deviceClass == 1028 || deviceClass == 1032) {
                outDeviceTypes = new int[]{32};
            } else if (deviceClass == 1056) {
                outDeviceTypes = new int[]{64};
            }
        }
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            address = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        }
        String btDeviceName = btDevice.getName();
        if (isActive) {
            result = false | handleDeviceConnection(isActive, outDeviceTypes[0], address, btDeviceName);
        } else {
            boolean result3 = false;
            for (int outDeviceType : outDeviceTypes) {
                result3 |= handleDeviceConnection(isActive, outDeviceType, address, btDeviceName);
            }
            result = result3;
        }
        if (!handleDeviceConnection(isActive, -2147483640, address, btDeviceName) || !result) {
            result2 = false;
        }
        return result2;
    }

    /* access modifiers changed from: private */
    public void setBtScoActiveDevice(BluetoothDevice btDevice) {
        synchronized (this.mScoClients) {
            Log.i(TAG, "setBtScoActiveDevice: " + getBtDevicePartAddress(this.mBluetoothHeadsetDevice) + " -> " + getBtDevicePartAddress(btDevice));
            BluetoothDevice previousActiveDevice = this.mBluetoothHeadsetDevice;
            if (!Objects.equals(btDevice, previousActiveDevice)) {
                if (!handleBtScoActiveDeviceChange(previousActiveDevice, false)) {
                    Log.w(TAG, "setBtScoActiveDevice() failed to remove previous device " + getBtDevicePartAddress(previousActiveDevice));
                }
                if (!handleBtScoActiveDeviceChange(btDevice, true)) {
                    Log.e(TAG, "setBtScoActiveDevice() failed to add new device " + getBtDevicePartAddress(btDevice));
                    btDevice = null;
                }
                this.mBluetoothHeadsetDevice = btDevice;
                if (this.mBluetoothHeadsetDevice == null) {
                    resetBluetoothSco();
                }
            }
        }
    }

    private String getBtDevicePartAddress(BluetoothDevice btDevice) {
        if (btDevice == null) {
            return "null";
        }
        return btDevice.getPartAddress();
    }

    /* access modifiers changed from: package-private */
    public void disconnectAllBluetoothProfiles() {
        disconnectA2dp();
        disconnectA2dpSink();
        disconnectHeadset();
        disconnectHearingAid();
    }

    /* access modifiers changed from: package-private */
    public void disconnectA2dp() {
        synchronized (this.mConnectedDevices) {
            synchronized (this.mA2dpAvrcpLock) {
                ArraySet<String> toRemove = null;
                for (int i = 0; i < this.mConnectedDevices.size(); i++) {
                    DeviceListSpec deviceSpec = this.mConnectedDevices.valueAt(i);
                    if (deviceSpec.mDeviceType == 128) {
                        toRemove = toRemove != null ? toRemove : new ArraySet<>();
                        toRemove.add(deviceSpec.mDeviceAddress);
                    }
                }
                if (toRemove != null) {
                    int delay = checkSendBecomingNoisyIntent(128, 0, 0);
                    for (int i2 = 0; i2 < toRemove.size(); i2++) {
                        makeA2dpDeviceUnavailableLater(toRemove.valueAt(i2), delay);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void disconnectA2dpSink() {
        synchronized (this.mConnectedDevices) {
            int i = 0;
            ArraySet<String> toRemove = null;
            for (int i2 = 0; i2 < this.mConnectedDevices.size(); i2++) {
                DeviceListSpec deviceSpec = this.mConnectedDevices.valueAt(i2);
                if (deviceSpec.mDeviceType == -2147352576) {
                    toRemove = toRemove != null ? toRemove : new ArraySet<>();
                    toRemove.add(deviceSpec.mDeviceAddress);
                }
            }
            if (toRemove != null) {
                while (true) {
                    int i3 = i;
                    if (i3 >= toRemove.size()) {
                        break;
                    }
                    makeA2dpSrcUnavailable(toRemove.valueAt(i3));
                    i = i3 + 1;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void disconnectHeadset() {
        synchronized (this.mScoClients) {
            setBtScoActiveDevice(null);
            this.mBluetoothHeadset = null;
        }
    }

    /* access modifiers changed from: package-private */
    public void disconnectHearingAid() {
        synchronized (this.mConnectedDevices) {
            synchronized (this.mHearingAidLock) {
                ArraySet<String> toRemove = null;
                for (int i = 0; i < this.mConnectedDevices.size(); i++) {
                    DeviceListSpec deviceSpec = this.mConnectedDevices.valueAt(i);
                    if (deviceSpec.mDeviceType == 134217728) {
                        toRemove = toRemove != null ? toRemove : new ArraySet<>();
                        toRemove.add(deviceSpec.mDeviceAddress);
                    }
                }
                if (toRemove != null) {
                    int checkSendBecomingNoisyIntent = checkSendBecomingNoisyIntent(134217728, 0, 0);
                    for (int i2 = 0; i2 < toRemove.size(); i2++) {
                        makeHearingAidDeviceUnavailable(toRemove.valueAt(i2));
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void onCheckMusicActive(String caller) {
        synchronized (this.mSafeMediaVolumeState) {
            if (this.mSafeMediaVolumeState.intValue() == 2) {
                int device = getDeviceForStream(3);
                if ((603979788 & device) != 0) {
                    if (!this.mHasAlarm) {
                        sendMsg(this.mAudioHandler, 14, 0, 0, 0, caller, MUSIC_ACTIVE_POLL_PERIOD_MS);
                    }
                    this.mSafeVolumeCaller = caller;
                    int index = this.mStreamStates[3].getIndex(device);
                    if (AudioSystem.isStreamActive(3, 0) && index > safeMediaVolumeIndex(device)) {
                        this.mMusicActiveMs += MUSIC_ACTIVE_POLL_PERIOD_MS;
                        if (this.mMusicActiveMs > UNSAFE_VOLUME_MUSIC_ACTIVE_MS_MAX) {
                            setSafeMediaVolumeEnabled(true, caller);
                            this.mMusicActiveMs = 0;
                            Log.d(TAG, "music is active more than " + UNSAFE_VOLUME_MUSIC_ACTIVE_MS_MAX + ", so reset to safe volume: " + safeMediaVolumeIndex(device) + " for device: " + device);
                            HwMediaMonitorManager.writeLogMsg(916010205, 1, 0, "OCMA");
                        }
                        saveMusicActiveMs();
                    }
                }
            }
        }
    }

    private void saveMusicActiveMs() {
        this.mAudioHandler.obtainMessage(22, this.mMusicActiveMs, 0).sendToTarget();
    }

    private int getSafeUsbMediaVolumeIndex() {
        int min = MIN_STREAM_VOLUME[3];
        int max = MAX_STREAM_VOLUME[3];
        this.mSafeUsbMediaVolumeDbfs = ((float) this.mContext.getResources().getInteger(17694853)) / 100.0f;
        while (true) {
            if (Math.abs(max - min) <= 1) {
                break;
            }
            int index = (max + min) / 2;
            float gainDB = AudioSystem.getStreamVolumeDB(3, index, 67108864);
            if (Float.isNaN(gainDB)) {
                break;
            } else if (gainDB == this.mSafeUsbMediaVolumeDbfs) {
                min = index;
                break;
            } else if (gainDB < this.mSafeUsbMediaVolumeDbfs) {
                min = index;
            } else {
                max = index;
            }
        }
        return min * 10;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0059  */
    public void onConfigureSafeVolume(boolean force, String caller) {
        boolean safeMediaVolumeEnabled;
        int persistedState;
        synchronized (this.mSafeMediaVolumeState) {
            int mcc = this.mContext.getResources().getConfiguration().mcc;
            if (this.mMcc != mcc || (this.mMcc == 0 && force)) {
                this.mSafeMediaVolumeIndex = this.mContext.getResources().getInteger(17694852) * 10;
                this.mSafeUsbMediaVolumeIndex = getSafeUsbMediaVolumeIndex();
                if (!SystemProperties.getBoolean("audio.safemedia.force", false)) {
                    if (!this.mContext.getResources().getBoolean(17957011)) {
                        safeMediaVolumeEnabled = false;
                        boolean safeMediaVolumeBypass = SystemProperties.getBoolean("audio.safemedia.bypass", false);
                        if (usingHwSafeMediaConfig()) {
                            this.mSafeMediaVolumeIndex = getHwSafeMediaVolumeIndex();
                            safeMediaVolumeEnabled = isHwSafeMediaVolumeEnabled();
                        }
                        if (usingHwSafeMediaConfig() && this.mHwAudioServiceEx.isHwSafeUsbMediaVolumeEnabled()) {
                            this.mSafeUsbMediaVolumeIndex = this.mHwAudioServiceEx.getHwSafeUsbMediaVolumeIndex();
                        }
                        if (safeMediaVolumeEnabled || safeMediaVolumeBypass) {
                            this.mSafeMediaVolumeState = 1;
                            persistedState = 1;
                        } else {
                            persistedState = 3;
                            if (this.mSafeMediaVolumeState.intValue() != 2) {
                                if (this.mMusicActiveMs == 0) {
                                    this.mSafeMediaVolumeState = 3;
                                    enforceSafeMediaVolume(caller);
                                } else {
                                    this.mSafeMediaVolumeState = 2;
                                }
                            }
                        }
                        this.mMcc = mcc;
                        sendMsg(this.mAudioHandler, 18, 2, persistedState, 0, null, 0);
                    }
                }
                safeMediaVolumeEnabled = true;
                boolean safeMediaVolumeBypass2 = SystemProperties.getBoolean("audio.safemedia.bypass", false);
                if (usingHwSafeMediaConfig()) {
                }
                this.mSafeUsbMediaVolumeIndex = this.mHwAudioServiceEx.getHwSafeUsbMediaVolumeIndex();
                if (safeMediaVolumeEnabled) {
                }
                this.mSafeMediaVolumeState = 1;
                persistedState = 1;
                this.mMcc = mcc;
                sendMsg(this.mAudioHandler, 18, 2, persistedState, 0, null, 0);
            }
        }
    }

    private int checkForRingerModeChange(int oldIndex, int direction, int step, boolean isMuted, String caller, int flags) {
        int result = 1;
        if (isPlatformTelevision() || this.mIsSingleVolume) {
            return 1;
        }
        int ringerMode = getRingerModeInternal();
        switch (ringerMode) {
            case 0:
                if (this.mIsSingleVolume && direction == -1 && oldIndex >= 2 * step && isMuted) {
                    ringerMode = 2;
                } else if (direction == 1 || direction == 101 || direction == 100) {
                    if (!this.mVolumePolicy.volumeUpToExitSilent) {
                        result = 1 | 128;
                    } else if (!this.mHasVibrator || direction != 1) {
                        ringerMode = 2;
                        result = 1 | 2;
                    } else {
                        ringerMode = 1;
                    }
                }
                result &= -2;
                break;
            case 1:
                if (this.mHasVibrator) {
                    if (direction == -1) {
                        if (this.mIsSingleVolume && oldIndex >= 2 * step && isMuted) {
                            ringerMode = 2;
                        } else if (this.mPrevVolDirection != -1) {
                            if (!this.mVolumePolicy.volumeDownToEnterSilent) {
                                result = 1 | 2048;
                            } else if (SystemClock.uptimeMillis() - this.mLoweredFromNormalToVibrateTime > ((long) this.mVolumePolicy.vibrateToSilentDebounce) && this.mRingerModeDelegate.canVolumeDownEnterSilent()) {
                                ringerMode = 0;
                            }
                        }
                    } else if (direction == 1 || direction == 101 || direction == 100) {
                        ringerMode = 2;
                        result = 1 | 2;
                    }
                    result &= -2;
                    break;
                } else {
                    Log.e(TAG, "checkForRingerModeChange() current ringer mode is vibratebut no vibrator is present");
                    break;
                }
            case 2:
                if (direction != -1) {
                    if (this.mIsSingleVolume && (direction == 101 || direction == -100)) {
                        if (this.mHasVibrator) {
                            ringerMode = 1;
                        } else {
                            ringerMode = 0;
                        }
                        result = 1 & -2;
                        break;
                    }
                } else if (!this.mHasVibrator) {
                    if (oldIndex == step && this.mVolumePolicy.volumeDownToEnterSilent) {
                        ringerMode = 0;
                        break;
                    }
                } else if (step <= oldIndex && oldIndex < 2 * step) {
                    ringerMode = 1;
                    this.mLoweredFromNormalToVibrateTime = SystemClock.uptimeMillis();
                    break;
                }
            default:
                Log.e(TAG, "checkForRingerModeChange() wrong ringer mode: " + ringerMode);
                break;
        }
        if (!isAndroidNPlus(caller) || !wouldToggleZenMode(ringerMode) || this.mNm.isNotificationPolicyAccessGrantedForPackage(caller) || (flags & 4096) != 0) {
            setRingerMode(ringerMode, "AudioService.checkForRingerModeChange", false);
            this.mPrevVolDirection = direction;
            return result;
        }
        throw new SecurityException("Not allowed to change Do Not Disturb state");
    }

    public boolean isStreamAffectedByRingerMode(int streamType) {
        return (this.mRingerModeAffectedStreams & (1 << streamType)) != 0;
    }

    private boolean shouldZenMuteStream(int streamType) {
        boolean z = false;
        if (this.mNm.getZenMode() != 1) {
            return false;
        }
        NotificationManager.Policy zenPolicy = this.mNm.getNotificationPolicy();
        boolean muteAlarms = (zenPolicy.priorityCategories & 32) == 0;
        boolean muteMedia = (zenPolicy.priorityCategories & 64) == 0;
        boolean muteSystem = (zenPolicy.priorityCategories & 128) == 0;
        boolean muteNotificationAndRing = ZenModeConfig.areAllPriorityOnlyNotificationZenSoundsMuted(this.mNm.getNotificationPolicy());
        if ((muteAlarms && isAlarm(streamType)) || ((muteMedia && isMedia(streamType)) || ((muteSystem && isSystem(streamType)) || (muteNotificationAndRing && isNotificationOrRinger(streamType))))) {
            z = true;
        }
        return z;
    }

    private boolean isStreamMutedByRingerOrZenMode(int streamType) {
        return (this.mRingerAndZenModeMutedStreams & (1 << streamType)) != 0;
    }

    private boolean updateZenModeAffectedStreams() {
        int zenModeAffectedStreams = 0;
        if (this.mSystemReady && this.mNm.getZenMode() == 1) {
            NotificationManager.Policy zenPolicy = this.mNm.getNotificationPolicy();
            if ((zenPolicy.priorityCategories & 32) == 0) {
                zenModeAffectedStreams = 0 | 16;
            }
            if ((zenPolicy.priorityCategories & 64) == 0) {
                zenModeAffectedStreams |= 8;
            }
            if ((zenPolicy.priorityCategories & 128) == 0) {
                zenModeAffectedStreams |= 2;
            }
        }
        if (this.mZenModeAffectedStreams == zenModeAffectedStreams) {
            return false;
        }
        this.mZenModeAffectedStreams = zenModeAffectedStreams;
        return true;
    }

    /* access modifiers changed from: private */
    @GuardedBy("mSettingsLock")
    public boolean updateRingerAndZenModeAffectedStreams() {
        int ringerModeAffectedStreams;
        int ringerModeAffectedStreams2;
        boolean updatedZenModeAffectedStreams = updateZenModeAffectedStreams();
        int ringerModeAffectedStreams3 = Settings.System.getIntForUser(this.mContentResolver, "mode_ringer_streams_affected", 166, -2);
        if (this.mIsSingleVolume) {
            ringerModeAffectedStreams3 = 0;
        } else if (this.mRingerModeDelegate != null) {
            ringerModeAffectedStreams3 = this.mRingerModeDelegate.getRingerModeAffectedStreams(ringerModeAffectedStreams3);
        }
        if (this.mCameraSoundForced) {
            ringerModeAffectedStreams = ringerModeAffectedStreams3 & -129;
        } else {
            ringerModeAffectedStreams = ringerModeAffectedStreams3 | 128;
        }
        if (mStreamVolumeAlias[8] == 2) {
            ringerModeAffectedStreams2 = ringerModeAffectedStreams | 256;
        } else {
            ringerModeAffectedStreams2 = ringerModeAffectedStreams & -257;
        }
        if (ringerModeAffectedStreams2 != this.mRingerModeAffectedStreams) {
            Settings.System.putIntForUser(this.mContentResolver, "mode_ringer_streams_affected", ringerModeAffectedStreams2, -2);
            this.mRingerModeAffectedStreams = ringerModeAffectedStreams2;
            return true;
        } else if (ringerModeAffectedStreams2 != this.mRingerModeAffectedStreams || ActivityManager.getCurrentUser() == 0) {
            return updatedZenModeAffectedStreams;
        } else {
            Settings.System.putIntForUser(this.mContentResolver, "mode_ringer_streams_affected", ringerModeAffectedStreams2, -2);
            Log.d(TAG, "updateRingerModeAffectedStreams enter sub user ringerModeAffectedStreams:" + ringerModeAffectedStreams2);
            return true;
        }
    }

    public boolean isStreamAffectedByMute(int streamType) {
        return (this.mMuteAffectedStreams & (1 << streamType)) != 0;
    }

    private void ensureValidDirection(int direction) {
        if (direction != -100) {
            switch (direction) {
                case -1:
                case 0:
                case 1:
                    return;
                default:
                    switch (direction) {
                        case 100:
                        case 101:
                            return;
                        default:
                            throw new IllegalArgumentException("Bad direction " + direction);
                    }
            }
        }
    }

    private void ensureValidStreamType(int streamType) {
        if (streamType < 0 || streamType >= this.mStreamStates.length) {
            throw new IllegalArgumentException("Bad stream type " + streamType);
        }
    }

    private boolean isMuteAdjust(int adjust) {
        return adjust == -100 || adjust == 100 || adjust == 101;
    }

    private boolean isInCommunication() {
        long ident = Binder.clearCallingIdentity();
        boolean IsInCall = ((TelecomManager) this.mContext.getSystemService("telecom")).isInCall();
        Binder.restoreCallingIdentity(ident);
        return IsInCall || getMode() == 3 || getMode() == 2;
    }

    /* access modifiers changed from: private */
    public boolean wasStreamActiveRecently(int stream, int delay_ms) {
        return AudioSystem.isStreamActive(stream, delay_ms) || AudioSystem.isStreamActiveRemotely(stream, delay_ms);
    }

    /* access modifiers changed from: protected */
    public int getActiveStreamType(int suggestedStreamType) {
        if (this.mIsSingleVolume && suggestedStreamType == Integer.MIN_VALUE) {
            return 3;
        }
        if (this.mPlatformType == 1) {
            if (isInCommunication()) {
                return AudioSystem.getForceUse(0) == 3 ? 6 : 0;
            }
            if (suggestedStreamType == Integer.MIN_VALUE) {
                if (wasStreamActiveRecently(3, 0)) {
                    if (DEBUG_VOL) {
                        Log.v(TAG, "getActiveStreamType: Forcing STREAM_MUSIC b/c default");
                    }
                    return 3;
                } else if (wasStreamActiveRecently(0, 0)) {
                    if (AudioSystem.getForceUse(0) == 3) {
                        if (DEBUG_VOL) {
                            Log.v(TAG, "getActiveStreamType: STREAM_VOICE_CALL is active, but eForcing STREAM_BLUETOOTH_SCO...");
                        }
                        return 6;
                    }
                    if (DEBUG_VOL) {
                        Log.v(TAG, "getActiveStreamType: Forcing STREAM_VOICE_CALL stream active");
                    }
                    return 0;
                } else if (wasStreamActiveRecently(6, 0)) {
                    if (DEBUG_VOL) {
                        Log.v(TAG, "getActiveStreamType: Forcing STREAM_BLUETOOTH_SCO stream active");
                    }
                    return 6;
                } else if (wasStreamActiveRecently(2, 0) || wasStreamActiveRecently(1, 0)) {
                    if (DEBUG_VOL) {
                        Log.v(TAG, "getActiveStreamType: Forcing STREAM_RING stream active");
                    }
                    return 2;
                } else if (wasStreamActiveRecently(4, 0)) {
                    if (DEBUG_VOL) {
                        Log.v(TAG, "getActiveStreamType: Forcing STREAM_ALARM stream active");
                    }
                    return 4;
                } else if (wasStreamActiveRecently(5, sStreamOverrideDelayMs)) {
                    if (DEBUG_VOL) {
                        Log.v(TAG, "getActiveStreamType: Forcing STREAM_NOTIFICATION stream active");
                    }
                    return 5;
                } else {
                    if (DEBUG_VOL) {
                        Log.v(TAG, "getActiveStreamType: Forcing DEFAULT_VOL_STREAM_NO_PLAYBACK(3) b/c default");
                    }
                    return this.mDefaultVolStream;
                }
            } else if (wasStreamActiveRecently(5, sStreamOverrideDelayMs)) {
                if (DEBUG_VOL) {
                    Log.v(TAG, "getActiveStreamType: Forcing STREAM_NOTIFICATION stream active");
                }
                return 5;
            } else if (wasStreamActiveRecently(2, sStreamOverrideDelayMs)) {
                if (DEBUG_VOL) {
                    Log.v(TAG, "getActiveStreamType: Forcing STREAM_RING stream active");
                }
                return 2;
            }
        }
        if (isInCommunication()) {
            if (AudioSystem.getForceUse(0) == 3) {
                if (DEBUG_VOL) {
                    Log.v(TAG, "getActiveStreamType: Forcing STREAM_BLUETOOTH_SCO");
                }
                return 6;
            }
            if (DEBUG_VOL) {
                Log.v(TAG, "getActiveStreamType: Forcing STREAM_VOICE_CALL");
            }
            return 0;
        } else if (AudioSystem.isStreamActive(5, sStreamOverrideDelayMs)) {
            if (DEBUG_VOL) {
                Log.v(TAG, "getActiveStreamType: Forcing STREAM_NOTIFICATION");
            }
            return 5;
        } else if (AudioSystem.isStreamActive(2, sStreamOverrideDelayMs)) {
            if (DEBUG_VOL) {
                Log.v(TAG, "getActiveStreamType: Forcing STREAM_RING");
            }
            return 2;
        } else if (suggestedStreamType != Integer.MIN_VALUE) {
            if (DEBUG_VOL) {
                Log.v(TAG, "getActiveStreamType: Returning suggested type " + suggestedStreamType);
            }
            return suggestedStreamType;
        } else if (AudioSystem.isStreamActive(5, sStreamOverrideDelayMs)) {
            if (DEBUG_VOL) {
                Log.v(TAG, "getActiveStreamType: Forcing STREAM_NOTIFICATION");
            }
            return 5;
        } else if (AudioSystem.isStreamActive(2, sStreamOverrideDelayMs)) {
            if (DEBUG_VOL) {
                Log.v(TAG, "getActiveStreamType: Forcing STREAM_RING");
            }
            return 2;
        } else {
            if (DEBUG_VOL) {
                Log.v(TAG, "getActiveStreamType: Forcing DEFAULT_VOL_STREAM_NO_PLAYBACK(3) b/c default");
            }
            return this.mDefaultVolStream;
        }
    }

    private void broadcastRingerMode(String action, int ringerMode) {
        Intent broadcast = new Intent(action);
        broadcast.putExtra("android.media.EXTRA_RINGER_MODE", ringerMode);
        broadcast.addFlags(603979776);
        sendStickyBroadcastToAll(broadcast);
    }

    private void broadcastVibrateSetting(int vibrateType) {
        if (this.mActivityManagerInternal.isSystemReady()) {
            Intent broadcast = new Intent("android.media.VIBRATE_SETTING_CHANGED");
            broadcast.putExtra("android.media.EXTRA_VIBRATE_TYPE", vibrateType);
            broadcast.putExtra("android.media.EXTRA_VIBRATE_SETTING", getVibrateSetting(vibrateType));
            sendBroadcastToAll(broadcast);
        }
    }

    /* access modifiers changed from: private */
    public void queueMsgUnderWakeLock(Handler handler, int msg, int arg1, int arg2, Object obj, int delay) {
        long ident = Binder.clearCallingIdentity();
        this.mAudioEventWakeLock.acquire();
        Binder.restoreCallingIdentity(ident);
        sendMsg(handler, msg, 2, arg1, arg2, obj, delay);
    }

    protected static void sendMsg(Handler handler, int msg, int existingMsgPolicy, int arg1, int arg2, Object obj, int delay) {
        if (existingMsgPolicy == 0) {
            handler.removeMessages(msg);
        } else if (existingMsgPolicy == 1 && handler.hasMessages(msg)) {
            return;
        }
        synchronized (mLastDeviceConnectMsgTime) {
            long time = SystemClock.uptimeMillis() + ((long) delay);
            if (msg == 101 || msg == 102 || msg == 105 || msg == 100 || msg == 103 || msg == 106) {
                if (mLastDeviceConnectMsgTime.longValue() >= time) {
                    time = mLastDeviceConnectMsgTime.longValue() + 30;
                }
                mLastDeviceConnectMsgTime = Long.valueOf(time);
            }
            if (!handler.sendMessageAtTime(handler.obtainMessage(msg, arg1, arg2, obj), time)) {
                Log.e(TAG, "send msg:" + msg + " failed!");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean checkAudioSettingsPermission(String method) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_AUDIO_SETTINGS") == 0) {
            return true;
        }
        Log.w(TAG, "Audio Settings Permission Denial: " + method + " from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
        return false;
    }

    /* access modifiers changed from: private */
    public int getDeviceForStream(int stream) {
        int device = getDevicesForStream(stream);
        if (((device - 1) & device) == 0) {
            return device;
        }
        if ((device & 2) != 0) {
            return 2;
        }
        if ((262144 & device) != 0) {
            return 262144;
        }
        if ((524288 & device) != 0) {
            return DumpState.DUMP_FROZEN;
        }
        if ((2097152 & device) != 0) {
            return DumpState.DUMP_COMPILER_STATS;
        }
        if ((device & 896) != 0) {
            return device & 896;
        }
        return device & 604004364;
    }

    /* access modifiers changed from: private */
    public int getDevicesForStream(int stream) {
        return getDevicesForStream(stream, true);
    }

    private int getDevicesForStream(int stream, boolean checkOthers) {
        int observeDevicesForStream_syncVSS;
        ensureValidStreamType(stream);
        synchronized (VolumeStreamState.class) {
            observeDevicesForStream_syncVSS = this.mStreamStates[stream].observeDevicesForStream_syncVSS(checkOthers);
        }
        return observeDevicesForStream_syncVSS;
    }

    /* access modifiers changed from: private */
    public void observeDevicesForStreams(int skipStream) {
        synchronized (VolumeStreamState.class) {
            for (int stream = 0; stream < this.mStreamStates.length; stream++) {
                if (stream != skipStream) {
                    this.mStreamStates[stream].observeDevicesForStream_syncVSS(false);
                }
            }
        }
    }

    public void setWiredDeviceConnectionState(int type, int state, String address, String name, String caller) {
        ArrayMap<String, DeviceListSpec> arrayMap;
        int i = type;
        int i2 = state;
        ArrayMap<String, DeviceListSpec> arrayMap2 = this.mConnectedDevices;
        synchronized (arrayMap2) {
            try {
                StringBuilder sb = new StringBuilder();
                sb.append("setWiredDeviceConnectionState(");
                sb.append(i2);
                sb.append(" nm: ");
                String str = name;
                sb.append(str);
                sb.append(") type: ");
                sb.append(i);
                sb.append(" caller: ");
                String str2 = caller;
                sb.append(str2);
                Slog.i(TAG, sb.toString());
                int delay = checkSendBecomingNoisyIntent(i, i2, 0);
                AudioHandler audioHandler = this.mAudioHandler;
                WiredDeviceConnectionState wiredDeviceConnectionState = new WiredDeviceConnectionState(i, i2, address, str, str2);
                arrayMap = arrayMap2;
                queueMsgUnderWakeLock(audioHandler, 100, 0, 0, wiredDeviceConnectionState, delay);
            } catch (Throwable th) {
                th = th;
                throw th;
            }
        }
    }

    public void setHearingAidDeviceConnectionState(BluetoothDevice device, int state) {
        Log.i(TAG, "setBluetoothHearingAidDeviceConnectionState");
        setBluetoothHearingAidDeviceConnectionState(device, state, false, 0);
    }

    public int setBluetoothHearingAidDeviceConnectionState(BluetoothDevice device, int state, boolean suppressNoisyIntent, int musicDevice) {
        int intState;
        synchronized (this.mConnectedDevices) {
            intState = 0;
            if (!suppressNoisyIntent) {
                if (state == 2) {
                    intState = 1;
                }
                intState = checkSendBecomingNoisyIntent(134217728, intState, musicDevice);
            }
            queueMsgUnderWakeLock(this.mAudioHandler, 105, state, 0, device, intState);
        }
        return intState;
    }

    public int setBluetoothA2dpDeviceConnectionState(BluetoothDevice device, int state, int profile) {
        return setBluetoothA2dpDeviceConnectionStateSuppressNoisyIntent(device, state, profile, false, -1);
    }

    public int setBluetoothA2dpDeviceConnectionStateSuppressNoisyIntent(BluetoothDevice device, int state, int profile, boolean suppressNoisyIntent, int a2dpVolume) {
        if (this.mAudioHandler.hasMessages(102, device)) {
            return 0;
        }
        return setBluetoothA2dpDeviceConnectionStateInt(device, state, profile, suppressNoisyIntent, 0, a2dpVolume);
    }

    public int setBluetoothA2dpDeviceConnectionStateInt(BluetoothDevice device, int state, int profile, boolean suppressNoisyIntent, int musicDevice, int a2dpVolume) {
        int delay;
        int i = state;
        int i2 = profile;
        boolean z = suppressNoisyIntent;
        if (i2 == 2 || i2 == 11) {
            synchronized (this.mConnectedDevices) {
                int intState = 0;
                if (i2 != 2 || z) {
                    int i3 = musicDevice;
                } else {
                    if (i == 2) {
                        intState = 1;
                    }
                    intState = checkSendBecomingNoisyIntent(128, intState, musicDevice);
                }
                delay = intState;
                Log.d(TAG, "setBluetoothA2dpDeviceConnectionStateInt device: " + getBtDevicePartAddress(device) + " state: " + i + " delay(ms): " + delay + " suppressNoisyIntent: " + z);
                queueMsgUnderWakeLock(this.mAudioHandler, i2 == 2 ? 102 : 101, i, a2dpVolume, device, delay);
                Log.v(TAG, "state: " + i + " delay: " + delay);
            }
            return delay;
        }
        throw new IllegalArgumentException("invalid profile " + i2);
    }

    public void handleBluetoothA2dpDeviceConfigChange(BluetoothDevice device) {
        synchronized (this.mConnectedDevices) {
            queueMsgUnderWakeLock(this.mAudioHandler, 103, 0, 0, device, 0);
        }
    }

    /* access modifiers changed from: private */
    public void onAccessoryPlugMediaUnmute(int newDevice) {
        if (DEBUG_VOL) {
            Log.i(TAG, String.format("onAccessoryPlugMediaUnmute newDevice=%d [%s]", new Object[]{Integer.valueOf(newDevice), AudioSystem.getOutputDeviceName(newDevice)}));
        }
        synchronized (this.mConnectedDevices) {
            if (!(this.mNm.getZenMode() == 2 || (DEVICE_MEDIA_UNMUTED_ON_PLUG & newDevice) == 0 || !this.mStreamStates[3].mIsMuted || this.mStreamStates[3].getIndex(newDevice) == 0 || ((604004352 | newDevice) & AudioSystem.getDevicesForStream(3)) == 0)) {
                if (DEBUG_VOL) {
                    Log.i(TAG, String.format(" onAccessoryPlugMediaUnmute unmuting device=%d [%s]", new Object[]{Integer.valueOf(newDevice), AudioSystem.getOutputDeviceName(newDevice)}));
                }
                this.mStreamStates[3].mute(false);
            }
        }
    }

    /* access modifiers changed from: private */
    public void setDeviceVolume(VolumeStreamState streamState, int device) {
        synchronized (VolumeStreamState.class) {
            streamState.applyDeviceVolume_syncVSS(device);
            for (int streamType = AudioSystem.getNumStreamTypes() - 1; streamType >= 0; streamType--) {
                if (streamType != streamState.mStreamType && mStreamVolumeAlias[streamType] == streamState.mStreamType) {
                    int streamDevice = getDeviceForStream(streamType);
                    if (!(device == streamDevice || !this.mAvrcpAbsVolSupported || (device & 896) == 0)) {
                        this.mStreamStates[streamType].applyDeviceVolume_syncVSS(device);
                    }
                    this.mStreamStates[streamType].applyDeviceVolume_syncVSS(streamDevice);
                }
            }
        }
        sendMsg(this.mAudioHandler, 1, 2, device, 0, streamState, 500);
    }

    private void makeA2dpDeviceAvailable(String address, String name, String eventSource) {
        VolumeStreamState volumeStreamState = this.mStreamStates[3];
        setBluetoothA2dpOnInt(true, eventSource);
        AudioSystem.setDeviceConnectionState(128, 1, address, name);
        AudioSystem.setParameters("A2dpSuspended=false");
        this.mConnectedDevices.put(makeDeviceListKey(128, address), new DeviceListSpec(128, name, address));
        sendMsg(this.mAudioHandler, MSG_ACCESSORY_PLUG_MEDIA_UNMUTE, 2, 128, 0, null, 0);
    }

    /* access modifiers changed from: private */
    public void onSendBecomingNoisyIntent() {
        Log.d(TAG, "send AUDIO_BECOMING_NOISY");
        sendBroadcastToAll(new Intent("android.media.AUDIO_BECOMING_NOISY"));
    }

    /* access modifiers changed from: private */
    public void makeA2dpDeviceUnavailableNow(String address) {
        if (address != null) {
            synchronized (this.mA2dpAvrcpLock) {
                this.mAvrcpAbsVolSupported = false;
            }
            AudioSystem.setDeviceConnectionState(128, 0, address, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
            this.mConnectedDevices.remove(makeDeviceListKey(128, address));
            setCurrentAudioRouteName(null);
            if (this.mDockAddress == address) {
                this.mDockAddress = null;
            }
        }
    }

    private void makeA2dpDeviceUnavailableLater(String address, int delayMs) {
        AudioSystem.setParameters("A2dpSuspended=true");
        this.mConnectedDevices.remove(makeDeviceListKey(128, address));
        if (!this.mHasSetRingMute && getStreamVolume(2) != 0 && AudioSystem.isStreamActive(2, 0)) {
            adjustStreamVolume(2, -100, 0, this.mContext.getOpPackageName());
            this.mHasSetRingMute = true;
        }
        queueMsgUnderWakeLock(this.mAudioHandler, 106, 0, 0, address, delayMs);
    }

    private void makeA2dpSrcAvailable(String address) {
        AudioSystem.setDeviceConnectionState(-2147352576, 1, address, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        this.mConnectedDevices.put(makeDeviceListKey(-2147352576, address), new DeviceListSpec(-2147352576, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, address));
    }

    private void makeA2dpSrcUnavailable(String address) {
        AudioSystem.setDeviceConnectionState(-2147352576, 0, address, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        this.mConnectedDevices.remove(makeDeviceListKey(-2147352576, address));
    }

    private void setHearingAidVolume(int index, int streamType) {
        synchronized (this.mHearingAidLock) {
            if (this.mHearingAid != null) {
                int gainDB = (int) AudioSystem.getStreamVolumeDB(streamType, index / 10, 134217728);
                if (gainDB < -128) {
                    gainDB = -128;
                }
                this.mHearingAid.setVolume(gainDB);
            }
        }
    }

    private void makeHearingAidDeviceAvailable(String address, String name, String eventSource) {
        setHearingAidVolume(this.mStreamStates[3].getIndex(134217728), 3);
        AudioSystem.setDeviceConnectionState(134217728, 1, address, name);
        this.mConnectedDevices.put(makeDeviceListKey(134217728, address), new DeviceListSpec(134217728, name, address));
        sendMsg(this.mAudioHandler, MSG_ACCESSORY_PLUG_MEDIA_UNMUTE, 2, 134217728, 0, null, 0);
    }

    private void makeHearingAidDeviceUnavailable(String address) {
        AudioSystem.setDeviceConnectionState(134217728, 0, address, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        this.mConnectedDevices.remove(makeDeviceListKey(134217728, address));
        setCurrentAudioRouteName(null);
    }

    private void cancelA2dpDeviceTimeout() {
        this.mAudioHandler.removeMessages(106);
    }

    private boolean hasScheduledA2dpDockTimeout() {
        return this.mAudioHandler.hasMessages(106);
    }

    /* access modifiers changed from: private */
    public void onSetA2dpSinkConnectionState(BluetoothDevice btDevice, int state, int a2dpVolume) {
        if (btDevice != null) {
            Log.d(TAG, "onSetA2dpSinkConnectionState btDevice= " + btDevice.getPartAddress() + " state= " + state + " is dock: " + btDevice.isBluetoothDock());
            String address = btDevice.getAddress();
            if (!BluetoothAdapter.checkBluetoothAddress(address)) {
                address = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
            }
            synchronized (this.mConnectedDevices) {
                boolean isConnected = this.mConnectedDevices.get(makeDeviceListKey(128, btDevice.getAddress())) != null;
                if (isConnected && state != 2) {
                    if (!btDevice.isBluetoothDock()) {
                        makeA2dpDeviceUnavailableNow(address);
                    } else if (state == 0) {
                        makeA2dpDeviceUnavailableLater(address, 8000);
                    }
                    setCurrentAudioRouteName(null);
                } else if (!isConnected && state == 2) {
                    if (btDevice.isBluetoothDock()) {
                        cancelA2dpDeviceTimeout();
                        this.mDockAddress = address;
                    } else if (hasScheduledA2dpDockTimeout() && this.mDockAddress != null) {
                        cancelA2dpDeviceTimeout();
                        makeA2dpDeviceUnavailableNow(this.mDockAddress);
                    }
                    if (a2dpVolume != -1) {
                        VolumeStreamState streamState = this.mStreamStates[3];
                        streamState.setIndex(a2dpVolume * 10, 128, "onSetA2dpSinkConnectionState");
                        setDeviceVolume(streamState, 128);
                    }
                    makeA2dpDeviceAvailable(address, btDevice.getName(), "onSetA2dpSinkConnectionState");
                    setCurrentAudioRouteName(btDevice.getAliasName());
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void onSetA2dpSourceConnectionState(BluetoothDevice btDevice, int state) {
        if (DEBUG_VOL) {
            Log.d(TAG, "onSetA2dpSourceConnectionState btDevice=" + getBtDevicePartAddress(btDevice) + " state=" + state);
        }
        if (btDevice != null) {
            String address = btDevice.getAddress();
            if (!BluetoothAdapter.checkBluetoothAddress(address)) {
                address = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
            }
            synchronized (this.mConnectedDevices) {
                boolean isConnected = this.mConnectedDevices.get(makeDeviceListKey(-2147352576, address)) != null;
                if (isConnected && state != 2) {
                    makeA2dpSrcUnavailable(address);
                } else if (!isConnected && state == 2) {
                    makeA2dpSrcAvailable(address);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void onSetHearingAidConnectionState(BluetoothDevice btDevice, int state) {
        Log.d(TAG, "onSetHearingAidConnectionState btDevice=" + getBtDevicePartAddress(btDevice) + ", state=" + state);
        if (btDevice != null) {
            String address = btDevice.getAddress();
            if (!BluetoothAdapter.checkBluetoothAddress(address)) {
                address = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
            }
            synchronized (this.mConnectedDevices) {
                boolean isConnected = this.mConnectedDevices.get(makeDeviceListKey(134217728, btDevice.getAddress())) != null;
                if (isConnected && state != 2) {
                    makeHearingAidDeviceUnavailable(address);
                    setCurrentAudioRouteName(null);
                } else if (!isConnected && state == 2) {
                    makeHearingAidDeviceAvailable(address, btDevice.getName(), "onSetHearingAidConnectionState");
                    setCurrentAudioRouteName(btDevice.getAliasName());
                }
            }
        }
    }

    private void setCurrentAudioRouteName(String name) {
        synchronized (this.mCurAudioRoutes) {
            if (!TextUtils.equals(this.mCurAudioRoutes.bluetoothName, name)) {
                this.mCurAudioRoutes.bluetoothName = name;
                sendMsg(this.mAudioHandler, 12, 1, 0, 0, null, 0);
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0063, code lost:
        return;
     */
    public void onBluetoothA2dpDeviceConfigChange(BluetoothDevice btDevice) {
        Log.d(TAG, "onBluetoothA2dpDeviceConfigChange btDevice=" + getBtDevicePartAddress(btDevice));
        if (btDevice != null) {
            String address = btDevice.getAddress();
            if (!BluetoothAdapter.checkBluetoothAddress(address)) {
                address = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
            }
            synchronized (this.mConnectedDevices) {
                if (!this.mAudioHandler.hasMessages(102, btDevice)) {
                    if (this.mConnectedDevices.get(makeDeviceListKey(128, address)) != null) {
                        int musicDevice = getDeviceForStream(3);
                        if (AudioSystem.handleDeviceConfigChange(128, address, btDevice.getName()) != 0) {
                            setBluetoothA2dpDeviceConnectionStateInt(btDevice, 0, 2, false, musicDevice, -1);
                        }
                    }
                }
            }
        }
    }

    public void avrcpSupportsAbsoluteVolume(String address, boolean support) {
        synchronized (this.mA2dpAvrcpLock) {
            this.mAvrcpAbsVolSupported = support;
            sendMsg(this.mAudioHandler, 0, 2, 128, 0, this.mStreamStates[3], 0);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:51:0x0173, code lost:
        return true;
     */
    private boolean handleDeviceConnection(boolean connect, int device, String address, String deviceName) {
        boolean z = connect;
        int i = device;
        String str = address;
        String str2 = deviceName;
        Slog.i(TAG, "handleDeviceConnection(" + z + " dev:" + Integer.toHexString(device) + " name:" + str2 + ")");
        this.mHwAudioServiceEx.onSetSoundEffectState(i, z ? 1 : 0);
        synchronized (this.mConnectedDevices) {
            String deviceKey = makeDeviceListKey(i, str);
            DeviceListSpec deviceSpec = this.mConnectedDevices.get(deviceKey);
            boolean isConnected = deviceSpec != null;
            Slog.i(TAG, "deviceSpec:" + deviceSpec + " is(already)Connected:" + isConnected);
            if (!z || isConnected) {
                boolean isConnected2 = isConnected;
                if (!z && isConnected2) {
                    AudioSystem.setDeviceConnectionState(i, 0, str, str2);
                    this.mConnectedDevices.remove(deviceKey);
                    if (LOUD_VOICE_MODE_SUPPORT) {
                        sendMsg(this.mAudioHandler, 10001, 0, 0, 0, null, 500);
                    }
                } else if (!z || !isConnected2) {
                    Log.w(TAG, "handleDeviceConnection() failed, deviceSpec=" + deviceSpec + ", connect=" + z);
                    return false;
                } else {
                    Slog.i(TAG, "the device:" + str2 + " has already beeen connected, ignore");
                    return true;
                }
            } else {
                int res = AudioSystem.setDeviceConnectionState(i, 1, str, str2);
                if (res != 0) {
                    Slog.e(TAG, "not connecting device 0x" + Integer.toHexString(device) + " due to command error " + res);
                    return false;
                }
                this.mConnectedDevices.put(deviceKey, new DeviceListSpec(i, str2, str));
                if (4 == i) {
                    String device_out_key = makeDeviceListKey(8, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                    if (this.mConnectedDevices.get(device_out_key) != null) {
                        AudioSystem.setDeviceConnectionState(8, 0, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                        this.mConnectedDevices.remove(device_out_key);
                    }
                }
                if (8 == i) {
                    String device_out_key2 = makeDeviceListKey(4, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                    boolean isConnectedHeadPhone = this.mConnectedDevices.get(device_out_key2) != null;
                    if (isConnectedHeadPhone) {
                        AudioSystem.setDeviceConnectionState(4, 0, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                        this.mConnectedDevices.remove(device_out_key2);
                    }
                    String device_in_key = makeDeviceListKey(-2147483632, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                    if (this.mConnectedDevices.get(device_in_key) != null) {
                        String str3 = device_out_key2;
                        boolean z2 = isConnectedHeadPhone;
                        AudioSystem.setDeviceConnectionState(-2147483632, 0, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                        this.mConnectedDevices.remove(device_in_key);
                    }
                }
                if (LOUD_VOICE_MODE_SUPPORT) {
                    sendMsg(this.mAudioHandler, 10001, 0, 0, 0, null, 500);
                }
                int i2 = res;
                boolean z3 = isConnected;
                sendMsg(this.mAudioHandler, MSG_ACCESSORY_PLUG_MEDIA_UNMUTE, 2, i, 0, null, 0);
                return true;
            }
        }
    }

    /* access modifiers changed from: private */
    public int checkSendBecomingNoisyIntent(int device, int state, int musicDevice) {
        Log.d(TAG, "checkSendBecomingNoisyIntent device:" + device + " state:" + state + " musicDevice:" + musicDevice);
        if (state != 0 || (this.mBecomingNoisyIntentDevices & device) == 0) {
            return 0;
        }
        int devices = 0;
        for (int i = 0; i < this.mConnectedDevices.size(); i++) {
            int dev = this.mConnectedDevices.valueAt(i).mDeviceType;
            if ((Integer.MIN_VALUE & dev) == 0 && (this.mBecomingNoisyIntentDevices & dev) != 0) {
                devices |= dev;
            }
        }
        if (musicDevice == 0) {
            musicDevice = getDeviceForStream(3);
            if ((536870912 & musicDevice) != 0) {
                int i2 = -536870913 & musicDevice;
                int i3 = 16384;
                if (device != 16384) {
                    i3 = 67108864;
                }
                musicDevice = i2 | i3;
                Log.i(TAG, "newDevice: " + Integer.toHexString(musicDevice));
            }
        }
        if ((device != musicDevice && !isInCommunication()) || device != devices || hasMediaDynamicPolicy()) {
            return 0;
        }
        this.mAudioHandler.removeMessages(15);
        sendMsg(this.mAudioHandler, 15, 0, 0, 0, null, 0);
        return 1000;
    }

    private boolean hasMediaDynamicPolicy() {
        synchronized (this.mAudioPolicies) {
            if (this.mAudioPolicies.isEmpty()) {
                return false;
            }
            for (AudioPolicyProxy app : this.mAudioPolicies.values()) {
                if (app.hasMixAffectingUsage(1)) {
                    return true;
                }
            }
            return false;
        }
    }

    private void updateAudioRoutes(int device, int state) {
        int newConn;
        int connType = 0;
        if (device == 4) {
            connType = 1;
        } else if (device == 8 || device == 131072) {
            connType = 2;
        } else if (device == 1024 || device == 262144) {
            connType = 8;
        } else if (device == 67108864) {
            connType = 16;
        }
        synchronized (this.mCurAudioRoutes) {
            if (connType != 0) {
                try {
                    int newConn2 = this.mCurAudioRoutes.mainType;
                    if (state != 0) {
                        newConn = newConn2 | connType;
                    } else {
                        newConn = newConn2 & (~connType);
                        if (connType == 2 || connType == 1) {
                            newConn &= -4;
                        }
                    }
                    if (newConn != this.mCurAudioRoutes.mainType) {
                        this.mCurAudioRoutes.mainType = newConn;
                        sendMsg(this.mAudioHandler, 12, 1, 0, 0, null, 0);
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
        }
    }

    private void sendDeviceConnectionIntent(int device, int state, String address, String deviceName) {
        Slog.i(TAG, "sendDeviceConnectionIntent(dev:0x" + Integer.toHexString(device) + " state:0x" + Integer.toHexString(state) + " name:" + deviceName + ");");
        Intent intent = new Intent();
        if (device == 4) {
            intent.setAction("android.intent.action.HEADSET_PLUG");
            intent.putExtra("microphone", 1);
            appendExtraInfo(intent);
        } else if (device == 8 || device == 131072) {
            intent.setAction("android.intent.action.HEADSET_PLUG");
            intent.putExtra("microphone", 0);
            appendExtraInfo(intent);
        } else if (device == 67108864) {
            intent.setAction("android.intent.action.HEADSET_PLUG");
            if (isConnectedHeadSet()) {
                intent.putExtra("microphone", 1);
            } else if (isConnectedHeadPhone()) {
                intent.putExtra("microphone", 0);
            } else if (isConnectedUsbInDevice()) {
                intent.putExtra("microphone", 1);
            } else {
                intent.putExtra("microphone", 0);
            }
        } else if (device == 1024 || device == 262144) {
            configureHdmiPlugIntent(intent, state);
        }
        if (intent.getAction() != null) {
            intent.putExtra(CONNECT_INTENT_KEY_STATE, state);
            intent.putExtra(CONNECT_INTENT_KEY_ADDRESS, address);
            intent.putExtra(CONNECT_INTENT_KEY_PORT_NAME, deviceName);
            intent.addFlags(1073741824);
            long ident = Binder.clearCallingIdentity();
            try {
                ActivityManager.broadcastStickyIntent(intent, -1);
                if (state == 0) {
                    this.mHwAudioServiceEx.updateMicIcon();
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    /* access modifiers changed from: private */
    public void onSetWiredDeviceConnectionState(int device, int state, String address, String deviceName, String caller) {
        int i = device;
        int i2 = state;
        String str = deviceName;
        StringBuilder sb = new StringBuilder();
        sb.append("onSetWiredDeviceConnectionState(dev:");
        sb.append(Integer.toHexString(device));
        sb.append(" state:");
        sb.append(Integer.toHexString(state));
        sb.append(" deviceName:");
        sb.append(str);
        sb.append(" caller: ");
        String str2 = caller;
        sb.append(str2);
        sb.append(");");
        Slog.i(TAG, sb.toString());
        synchronized (this.mConnectedDevices) {
            if (i2 == 0 && (i & DEVICE_OVERRIDE_A2DP_ROUTE_ON_PLUG) != 0) {
                try {
                    setBluetoothA2dpOnInt(true, "onSetWiredDeviceConnectionState state 0");
                } catch (Throwable th) {
                    throw th;
                }
            }
            if (handleDeviceConnection(i2 == 1, i, address, str)) {
                if (i2 != 0) {
                    if ((DEVICE_OVERRIDE_A2DP_ROUTE_ON_PLUG & i) != 0) {
                        setBluetoothA2dpOnInt(false, "onSetWiredDeviceConnectionState state not 0");
                    }
                    if ((i & 603979788) != 0) {
                        sendMsg(this.mAudioHandler, 14, 0, 0, 0, str2, MUSIC_ACTIVE_POLL_PERIOD_MS);
                    }
                    if (isPlatformTelevision() && (i & 1024) != 0) {
                        this.mFixedVolumeDevices |= 1024;
                        checkAllFixedVolumeDevices();
                        if (this.mHdmiManager != null) {
                            synchronized (this.mHdmiManager) {
                                if (this.mHdmiPlaybackClient != null) {
                                    this.mHdmiCecSink = false;
                                    this.mHdmiPlaybackClient.queryDisplayStatus(this.mHdmiDisplayStatusCallback);
                                }
                            }
                        }
                    }
                    if ((i & 1024) != 0) {
                        sendEnabledSurroundFormats(this.mContentResolver, true);
                    }
                } else {
                    if (!(!isPlatformTelevision() || (i & 1024) == 0 || this.mHdmiManager == null)) {
                        synchronized (this.mHdmiManager) {
                            this.mHdmiCecSink = false;
                        }
                    }
                    if (!this.mIsChineseZone && (i & 603979788) != 0 && this.mHasAlarm) {
                        this.mAlarmManager.cancel(this.mPendingIntent);
                        this.mHasAlarm = false;
                    }
                }
                sendDeviceConnectionIntent(device, state, address, deviceName);
                updateAudioRoutes(device, state);
            }
        }
    }

    private void configureHdmiPlugIntent(Intent intent, int state) {
        Intent intent2 = intent;
        int i = state;
        intent2.setAction("android.media.action.HDMI_AUDIO_PLUG");
        intent2.putExtra("android.media.extra.AUDIO_PLUG_STATE", i);
        if (i == 1) {
            ArrayList<AudioPort> ports = new ArrayList<>();
            if (AudioSystem.listAudioPorts(ports, new int[1]) == 0) {
                Iterator<AudioPort> it = ports.iterator();
                while (it.hasNext()) {
                    AudioDevicePort next = it.next();
                    if (next instanceof AudioDevicePort) {
                        AudioDevicePort devicePort = next;
                        if (devicePort.type() == 1024 || devicePort.type() == 262144) {
                            int[] formats = AudioFormat.filterPublicFormats(devicePort.formats());
                            if (formats.length > 0) {
                                ArrayList<Integer> encodingList = new ArrayList<>(1);
                                for (int format : formats) {
                                    if (format != 0) {
                                        encodingList.add(Integer.valueOf(format));
                                    }
                                }
                                int[] encodingArray = new int[encodingList.size()];
                                for (int i2 = 0; i2 < encodingArray.length; i2++) {
                                    encodingArray[i2] = encodingList.get(i2).intValue();
                                }
                                intent2.putExtra("android.media.extra.ENCODINGS", encodingArray);
                            }
                            int maxChannels = 0;
                            for (int mask : devicePort.channelMasks()) {
                                int channelCount = AudioFormat.channelCountFromOutChannelMask(mask);
                                if (channelCount > maxChannels) {
                                    maxChannels = channelCount;
                                }
                            }
                            intent2.putExtra("android.media.extra.MAX_CHANNEL_COUNT", maxChannels);
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void setCheckMusicActiveAlarm() {
        this.mAlarmManager.cancel(this.mPendingIntent);
        this.mAlarmManager.setExact(0, Calendar.getInstance().getTimeInMillis() + 60000, this.mPendingIntent);
        this.mHasAlarm = true;
    }

    /* access modifiers changed from: private */
    public boolean isA2dpDeviceConnected() {
        synchronized (this.mConnectedDevices) {
            synchronized (this.mA2dpAvrcpLock) {
                for (int i = 0; i < this.mConnectedDevices.size(); i++) {
                    if (this.mConnectedDevices.valueAt(i).mDeviceType == 128) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleAudioEffectBroadcast(Context context, Intent intent) {
        String target = intent.getPackage();
        if (target != null) {
            Log.w(TAG, "effect broadcast already targeted to " + target);
            return;
        }
        intent.addFlags(32);
        List<ResolveInfo> ril = context.getPackageManager().queryBroadcastReceivers(intent, 0);
        if (!(ril == null || ril.size() == 0)) {
            ResolveInfo ri = ril.get(0);
            if (!(ri == null || ri.activityInfo == null || ri.activityInfo.packageName == null)) {
                intent.setPackage(ri.activityInfo.packageName);
                context.sendBroadcastAsUser(intent, UserHandle.ALL);
                return;
            }
        }
        Log.w(TAG, "couldn't find receiver package for effect intent");
    }

    /* access modifiers changed from: private */
    public void killBackgroundUserProcessesWithRecordAudioPermission(UserInfo oldUser) {
        PackageManager pm = this.mContext.getPackageManager();
        ComponentName homeActivityName = null;
        if (!oldUser.isManagedProfile()) {
            homeActivityName = this.mActivityManagerInternal.getHomeActivityForUser(oldUser.id);
        }
        try {
            List<PackageInfo> packages = AppGlobals.getPackageManager().getPackagesHoldingPermissions(new String[]{"android.permission.RECORD_AUDIO"}, 0, oldUser.id).getList();
            for (int j = packages.size() - 1; j >= 0; j--) {
                PackageInfo pkg = packages.get(j);
                if (UserHandle.getAppId(pkg.applicationInfo.uid) >= 10000 && pm.checkPermission("android.permission.INTERACT_ACROSS_USERS", pkg.packageName) != 0 && (homeActivityName == null || !pkg.packageName.equals(homeActivityName.getPackageName()) || !pkg.applicationInfo.isSystemApp())) {
                    try {
                        int uid = pkg.applicationInfo.uid;
                        ActivityManager.getService().killUid(UserHandle.getAppId(uid), UserHandle.getUserId(uid), "killBackgroundUserProcessesWithAudioRecordPermission");
                    } catch (RemoteException e) {
                        Log.w(TAG, "Error calling killUid", e);
                    }
                }
            }
        } catch (RemoteException e2) {
            throw new AndroidRuntimeException(e2);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:27:0x003f, code lost:
        return false;
     */
    private boolean forceFocusDuckingForAccessibility(AudioAttributes aa, int request, int uid) {
        if (aa == null || aa.getUsage() != 11 || request != 3) {
            return false;
        }
        Bundle extraInfo = aa.getBundle();
        if (extraInfo == null || !extraInfo.getBoolean("a11y_force_ducking")) {
            return false;
        }
        if (uid == 0) {
            return true;
        }
        synchronized (this.mAccessibilityServiceUidsLock) {
            if (this.mAccessibilityServiceUids != null) {
                int callingUid = Binder.getCallingUid();
                for (int i : this.mAccessibilityServiceUids) {
                    if (i == callingUid) {
                        return true;
                    }
                }
            }
        }
    }

    public int requestAudioFocus(AudioAttributes aa, int durationHint, IBinder cb, IAudioFocusDispatcher fd, String clientId, String callingPackageName, int flags, IAudioPolicyCallback pcb, int sdk) {
        String str;
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.AUDIO_REQUESTAUDIOFOCUS, new Object[0]);
        if ((flags & 4) == 4) {
            str = clientId;
            if (!"AudioFocus_For_Phone_Ring_And_Calls".equals(str)) {
                synchronized (this.mAudioPolicies) {
                    if (!this.mAudioPolicies.containsKey(pcb.asBinder())) {
                        Log.e(TAG, "Invalid unregistered AudioPolicy to (un)lock audio focus");
                        return 0;
                    }
                }
            } else if (this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE") != 0) {
                Log.e(TAG, "Invalid permission to (un)lock audio focus", new Exception());
                return 0;
            }
        } else {
            str = clientId;
        }
        AudioAttributes audioAttributes = aa;
        int i = durationHint;
        return this.mMediaFocusControl.requestAudioFocus(audioAttributes, i, cb, fd, str, callingPackageName, flags, sdk, forceFocusDuckingForAccessibility(audioAttributes, i, Binder.getCallingUid()));
    }

    public int abandonAudioFocus(IAudioFocusDispatcher fd, String clientId, AudioAttributes aa, String callingPackageName) {
        return this.mMediaFocusControl.abandonAudioFocus(fd, clientId, aa, callingPackageName);
    }

    public void unregisterAudioFocusClient(String clientId) {
        this.mMediaFocusControl.unregisterAudioFocusClient(clientId);
    }

    public int getCurrentAudioFocus() {
        return this.mMediaFocusControl.getCurrentAudioFocus();
    }

    public int getFocusRampTimeMs(int focusGain, AudioAttributes attr) {
        MediaFocusControl mediaFocusControl = this.mMediaFocusControl;
        return MediaFocusControl.getFocusRampTimeMs(focusGain, attr);
    }

    private boolean readCameraSoundForced() {
        if (SystemProperties.getBoolean("audio.camerasound.force", false) || this.mContext.getResources().getBoolean(17956909)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void handleConfigurationChanged(Context context) {
        try {
            Configuration config = context.getResources().getConfiguration();
            sendMsg(this.mAudioHandler, 16, 0, 0, 0, TAG, 0);
            boolean cameraSoundForced = readCameraSoundForced();
            synchronized (this.mSettingsLock) {
                int i = 0;
                boolean cameraSoundForcedChanged = cameraSoundForced != this.mCameraSoundForced;
                this.mCameraSoundForced = cameraSoundForced;
                if (cameraSoundForcedChanged) {
                    if (!this.mIsSingleVolume) {
                        synchronized (VolumeStreamState.class) {
                            VolumeStreamState s = this.mStreamStates[7];
                            if (cameraSoundForced) {
                                s.setAllIndexesToMax();
                                this.mRingerModeAffectedStreams &= -129;
                            } else {
                                s.setAllIndexes(this.mStreamStates[1], TAG);
                                this.mRingerModeAffectedStreams |= 128;
                            }
                        }
                        setRingerModeInt(getRingerModeInternal(), false);
                    }
                    AudioHandler audioHandler = this.mAudioHandler;
                    if (cameraSoundForced) {
                        i = 11;
                    }
                    sendMsg(audioHandler, 8, 2, 4, i, new String("handleConfigurationChanged"), 0);
                    sendMsg(this.mAudioHandler, 10, 2, 0, 0, this.mStreamStates[7], 0);
                }
            }
            this.mVolumeController.setLayoutDirection(config.getLayoutDirection());
        } catch (Exception e) {
            Log.e(TAG, "Error handling configuration change: ", e);
        }
    }

    public void setBluetoothA2dpOnInt(boolean on, String eventSource) {
        synchronized (this.mBluetoothA2dpEnabledLock) {
            this.mBluetoothA2dpEnabled = on;
            this.mAudioHandler.removeMessages(13);
            setForceUseInt_SyncDevices(1, this.mBluetoothA2dpEnabled ? 0 : 10, eventSource);
        }
    }

    /* access modifiers changed from: private */
    public void setForceUseInt_SyncDevices(int usage, int config, String eventSource) {
        if (usage == 1) {
            sendMsg(this.mAudioHandler, 12, 1, 0, 0, null, 0);
        }
        this.mForceUseLogger.log(new AudioServiceEvents.ForceUseEvent(usage, config, eventSource));
        AudioSystem.setForceUse(usage, config);
        updateAftPolicy();
    }

    public void setRingtonePlayer(IRingtonePlayer player) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.REMOTE_AUDIO_PLAYBACK", null);
        this.mRingtonePlayer = player;
    }

    public IRingtonePlayer getRingtonePlayer() {
        return this.mRingtonePlayer;
    }

    public AudioRoutesInfo startWatchingRoutes(IAudioRoutesObserver observer) {
        AudioRoutesInfo routes;
        synchronized (this.mCurAudioRoutes) {
            routes = new AudioRoutesInfo(this.mCurAudioRoutes);
            this.mRoutesObservers.register(observer);
        }
        return routes;
    }

    private int safeMediaVolumeIndex(int device) {
        if ((603979788 & device) == 0) {
            return MAX_STREAM_VOLUME[3];
        }
        if (device == 67108864) {
            return this.mSafeUsbMediaVolumeIndex;
        }
        return this.mSafeMediaVolumeIndex;
    }

    private void setSafeMediaVolumeEnabled(boolean on, String caller) {
        synchronized (this.mSafeMediaVolumeState) {
            if (!(this.mSafeMediaVolumeState.intValue() == 0 || this.mSafeMediaVolumeState.intValue() == 1)) {
                if (on && this.mSafeMediaVolumeState.intValue() == 2) {
                    this.mSafeMediaVolumeState = 3;
                    enforceSafeMediaVolume(caller);
                } else if (!on && this.mSafeMediaVolumeState.intValue() == 3) {
                    this.mSafeMediaVolumeState = 2;
                    this.mMusicActiveMs = 1;
                    saveMusicActiveMs();
                    sendMsg(this.mAudioHandler, 14, 0, 0, 0, caller, MUSIC_ACTIVE_POLL_PERIOD_MS);
                }
            }
        }
    }

    private void enforceSafeMediaVolume(String caller) {
        VolumeStreamState streamState = this.mStreamStates[3];
        int devices = 603979788;
        int i = 0;
        while (devices != 0) {
            int i2 = i + 1;
            int device = 1 << i;
            if ((device & devices) != 0) {
                if (streamState.getIndex(device) > safeMediaVolumeIndex(device)) {
                    streamState.setIndex(safeMediaVolumeIndex(device), device, caller);
                    sendMsg(this.mAudioHandler, 0, 2, device, 0, streamState, 0);
                }
                devices &= ~device;
            }
            i = i2;
        }
    }

    private boolean checkSafeMediaVolume(int streamType, int index, int device) {
        synchronized (this.mSafeMediaVolumeState) {
            if (this.mFactoryMode || this.mSafeMediaVolumeState.intValue() != 3 || mStreamVolumeAlias[streamType] != 3 || (603979788 & device) == 0 || index <= safeMediaVolumeIndex(device)) {
                return true;
            }
            return false;
        }
    }

    public void disableSafeMediaVolume(String callingPackage) {
        enforceVolumeController("disable the safe media volume");
        synchronized (this.mSafeMediaVolumeState) {
            setSafeMediaVolumeEnabled(false, callingPackage);
            if (this.mPendingVolumeCommand != null) {
                onSetStreamVolume(this.mPendingVolumeCommand.mStreamType, this.mPendingVolumeCommand.mIndex, this.mPendingVolumeCommand.mFlags, this.mPendingVolumeCommand.mDevice, callingPackage);
                this.mPendingVolumeCommand = null;
            }
        }
    }

    public int setHdmiSystemAudioSupported(boolean on) {
        int config;
        int device = 0;
        if (this.mHdmiManager != null) {
            synchronized (this.mHdmiManager) {
                if (this.mHdmiTvClient == null) {
                    Log.w(TAG, "Only Hdmi-Cec enabled TV device supports system audio mode.");
                    return 0;
                }
                synchronized (this.mHdmiTvClient) {
                    if (this.mHdmiSystemAudioSupported != on) {
                        this.mHdmiSystemAudioSupported = on;
                        if (on) {
                            config = 12;
                        } else {
                            config = 0;
                        }
                        this.mForceUseLogger.log(new AudioServiceEvents.ForceUseEvent(5, config, "setHdmiSystemAudioSupported"));
                        AudioSystem.setForceUse(5, config);
                    }
                    device = getDevicesForStream(3);
                }
            }
        }
        return device;
    }

    public boolean isHdmiSystemAudioSupported() {
        return this.mHdmiSystemAudioSupported;
    }

    private void initA11yMonitoring() {
        AccessibilityManager accessibilityManager = (AccessibilityManager) this.mContext.getSystemService("accessibility");
        updateDefaultStreamOverrideDelay(accessibilityManager.isTouchExplorationEnabled());
        updateA11yVolumeAlias(accessibilityManager.isAccessibilityVolumeStreamActive());
        accessibilityManager.addTouchExplorationStateChangeListener(this, null);
        accessibilityManager.addAccessibilityServicesStateChangeListener(this, null);
    }

    public void onTouchExplorationStateChanged(boolean enabled) {
        updateDefaultStreamOverrideDelay(enabled);
    }

    private void updateDefaultStreamOverrideDelay(boolean touchExploreEnabled) {
        if (touchExploreEnabled) {
            sStreamOverrideDelayMs = 1000;
        } else {
            sStreamOverrideDelayMs = 0;
        }
        if (DEBUG_VOL) {
            Log.d(TAG, "Touch exploration enabled=" + touchExploreEnabled + " stream override delay is now " + sStreamOverrideDelayMs + " ms");
        }
    }

    public void onAccessibilityServicesStateChanged(AccessibilityManager accessibilityManager) {
        updateA11yVolumeAlias(accessibilityManager.isAccessibilityVolumeStreamActive());
    }

    private void updateA11yVolumeAlias(boolean a11VolEnabled) {
        if (DEBUG_VOL) {
            Log.d(TAG, "Accessibility volume enabled = " + a11VolEnabled);
        }
        if (sIndependentA11yVolume != a11VolEnabled) {
            sIndependentA11yVolume = a11VolEnabled;
            int i = 1;
            updateStreamVolumeAlias(true, TAG);
            VolumeController volumeController = this.mVolumeController;
            if (!sIndependentA11yVolume) {
                i = 0;
            }
            volumeController.setA11yMode(i);
            this.mVolumeController.postVolumeChanged(10, 0);
        }
    }

    public boolean isCameraSoundForced() {
        boolean z;
        synchronized (this.mSettingsLock) {
            z = this.mCameraSoundForced;
        }
        return z;
    }

    private void dumpRingerMode(PrintWriter pw) {
        pw.println("\nRinger mode: ");
        pw.println("- mode (internal) = " + RINGER_MODE_NAMES[this.mRingerMode]);
        pw.println("- mode (external) = " + RINGER_MODE_NAMES[this.mRingerModeExternal]);
        dumpRingerModeStreams(pw, "affected", this.mRingerModeAffectedStreams);
        dumpRingerModeStreams(pw, "muted", this.mRingerAndZenModeMutedStreams);
        pw.print("- delegate = ");
        pw.println(this.mRingerModeDelegate);
    }

    private void dumpRingerModeStreams(PrintWriter pw, String type, int streams) {
        pw.print("- ringer mode ");
        pw.print(type);
        pw.print(" streams = 0x");
        pw.print(Integer.toHexString(streams));
        if (streams != 0) {
            pw.print(" (");
            boolean first = true;
            for (int i = 0; i < AudioSystem.STREAM_NAMES.length; i++) {
                int stream = 1 << i;
                if ((streams & stream) != 0) {
                    if (!first) {
                        pw.print(',');
                    }
                    pw.print(AudioSystem.STREAM_NAMES[i]);
                    streams &= ~stream;
                    first = false;
                }
            }
            if (streams != 0) {
                if (!first) {
                    pw.print(',');
                }
                pw.print(streams);
            }
            pw.print(')');
        }
        pw.println();
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, pw)) {
            this.mMediaFocusControl.dump(pw);
            dumpStreamStates(pw);
            dumpRingerMode(pw);
            pw.println("\nAudio routes:");
            pw.print("  mMainType=0x");
            pw.println(Integer.toHexString(this.mCurAudioRoutes.mainType));
            pw.print("  mBluetoothName=");
            pw.println(this.mCurAudioRoutes.bluetoothName);
            pw.println("\nOther state:");
            pw.print("  mVolumeController=");
            pw.println(this.mVolumeController);
            pw.print("  mSafeMediaVolumeState=");
            pw.println(safeMediaVolumeStateToString(this.mSafeMediaVolumeState));
            pw.print("  mSafeMediaVolumeIndex=");
            pw.println(this.mSafeMediaVolumeIndex);
            pw.print("  mSafeUsbMediaVolumeIndex=");
            pw.println(this.mSafeUsbMediaVolumeIndex);
            pw.print("  mSafeUsbMediaVolumeDbfs=");
            pw.println(this.mSafeUsbMediaVolumeDbfs);
            pw.print("  sIndependentA11yVolume=");
            pw.println(sIndependentA11yVolume);
            pw.print("  mPendingVolumeCommand=");
            pw.println(this.mPendingVolumeCommand);
            pw.print("  mMusicActiveMs=");
            pw.println(this.mMusicActiveMs);
            pw.print("  UNSAFE_VOLUME_MUSIC_ACTIVE_MS_MAX=");
            pw.println(UNSAFE_VOLUME_MUSIC_ACTIVE_MS_MAX);
            pw.print("  mMcc=");
            pw.println(this.mMcc);
            pw.print("  mCameraSoundForced=");
            pw.println(this.mCameraSoundForced);
            pw.print("  mHasVibrator=");
            pw.println(this.mHasVibrator);
            pw.print("  mVolumePolicy=");
            pw.println(this.mVolumePolicy);
            pw.print("  mAvrcpAbsVolSupported=");
            pw.println(this.mAvrcpAbsVolSupported);
            dumpAudioPolicies(pw);
            this.mDynPolicyLogger.dump(pw);
            this.mPlaybackMonitor.dump(pw);
            this.mRecordMonitor.dump(pw);
            pw.println("\n");
            pw.println("\nEvent logs:");
            this.mModeLogger.dump(pw);
            pw.println("\n");
            this.mWiredDevLogger.dump(pw);
            pw.println("\n");
            this.mForceUseLogger.dump(pw);
            pw.println("\n");
            this.mVolumeLogger.dump(pw);
            dumpAudioMode(pw);
        }
    }

    private static String safeMediaVolumeStateToString(Integer state) {
        switch (state.intValue()) {
            case 0:
                return "SAFE_MEDIA_VOLUME_NOT_CONFIGURED";
            case 1:
                return "SAFE_MEDIA_VOLUME_DISABLED";
            case 2:
                return "SAFE_MEDIA_VOLUME_INACTIVE";
            case 3:
                return "SAFE_MEDIA_VOLUME_ACTIVE";
            default:
                return null;
        }
    }

    private static void readAndSetLowRamDevice() {
        boolean isLowRamDevice = ActivityManager.isLowRamDeviceStatic();
        long totalMemory = 1073741824;
        try {
            ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
            ActivityManager.getService().getMemoryInfo(info);
            totalMemory = info.totalMem;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot obtain MemoryInfo from ActivityManager, assume low memory device");
            isLowRamDevice = true;
        }
        int status = AudioSystem.setLowRamDevice(isLowRamDevice, totalMemory);
        if (status != 0) {
            Log.w(TAG, "AudioFlinger informed of device's low RAM attribute; status " + status);
        }
    }

    private void enforceVolumeController(String action) {
        Context context = this.mContext;
        context.enforceCallingOrSelfPermission("android.permission.STATUS_BAR_SERVICE", "Only SystemUI can " + action);
    }

    public void setVolumeController(final IVolumeController controller) {
        enforceVolumeController("set the volume controller");
        if (!this.mVolumeController.isSameBinder(controller)) {
            this.mVolumeController.postDismiss();
            if (controller != null) {
                try {
                    controller.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                        public void binderDied() {
                            if (AudioService.this.mVolumeController.isSameBinder(controller)) {
                                Log.w(AudioService.TAG, "Current remote volume controller died, unregistering");
                                AudioService.this.setVolumeController(null);
                            }
                        }
                    }, 0);
                } catch (RemoteException e) {
                }
            }
            this.mVolumeController.setController(controller);
            if (DEBUG_VOL) {
                Log.d(TAG, "Volume controller: " + this.mVolumeController);
            }
        }
    }

    public void notifyVolumeControllerVisible(IVolumeController controller, boolean visible) {
        enforceVolumeController("notify about volume controller visibility");
        if (this.mVolumeController.isSameBinder(controller)) {
            this.mVolumeController.setVisible(visible);
        }
    }

    public void setVolumePolicy(VolumePolicy policy) {
        enforceVolumeController("set volume policy");
        if (policy != null && !policy.equals(this.mVolumePolicy)) {
            this.mVolumePolicy = policy;
            if (DEBUG_VOL) {
                Log.d(TAG, "Volume policy changed: " + this.mVolumePolicy);
            }
        }
    }

    public String registerAudioPolicy(AudioPolicyConfig policyConfig, IAudioPolicyCallback pcb, boolean hasFocusListener, boolean isFocusPolicy, boolean isVolumeController) {
        AudioSystem.setDynamicPolicyCallback(this.mDynPolicyCallback);
        if (!(this.mContext.checkCallingPermission("android.permission.MODIFY_AUDIO_ROUTING") == 0)) {
            Slog.w(TAG, "Can't register audio policy for pid " + Binder.getCallingPid() + " / uid " + Binder.getCallingUid() + ", need MODIFY_AUDIO_ROUTING");
            return null;
        }
        AudioEventLogger audioEventLogger = this.mDynPolicyLogger;
        StringBuilder sb = new StringBuilder();
        sb.append("registerAudioPolicy for ");
        sb.append(pcb.asBinder());
        sb.append(" with config:");
        AudioPolicyConfig audioPolicyConfig = policyConfig;
        sb.append(audioPolicyConfig);
        audioEventLogger.log(new AudioEventLogger.StringEvent(sb.toString()).printLog(TAG));
        synchronized (this.mAudioPolicies) {
            try {
                if (this.mAudioPolicies.containsKey(pcb.asBinder())) {
                    Slog.e(TAG, "Cannot re-register policy");
                    return null;
                }
                AudioPolicyProxy audioPolicyProxy = new AudioPolicyProxy(audioPolicyConfig, pcb, hasFocusListener, isFocusPolicy, isVolumeController);
                pcb.asBinder().linkToDeath(audioPolicyProxy, 0);
                String regId = audioPolicyProxy.getRegistrationId();
                this.mAudioPolicies.put(pcb.asBinder(), audioPolicyProxy);
                return regId;
            } catch (RemoteException e) {
                StringBuilder sb2 = new StringBuilder();
                sb2.append("Audio policy registration failed, could not link to ");
                sb2.append(pcb);
                sb2.append(" binder death");
                Slog.w(TAG, sb2.toString(), e);
                return null;
            } catch (Throwable th) {
                e = th;
                throw e;
            }
        }
    }

    public void unregisterAudioPolicyAsync(IAudioPolicyCallback pcb) {
        AudioEventLogger audioEventLogger = this.mDynPolicyLogger;
        audioEventLogger.log(new AudioEventLogger.StringEvent("unregisterAudioPolicyAsync for " + pcb.asBinder()).printLog(TAG));
        synchronized (this.mAudioPolicies) {
            AudioPolicyProxy app = this.mAudioPolicies.remove(pcb.asBinder());
            if (app == null) {
                Slog.w(TAG, "Trying to unregister unknown audio policy for pid " + Binder.getCallingPid() + " / uid " + Binder.getCallingUid());
                return;
            }
            pcb.asBinder().unlinkToDeath(app, 0);
            app.release();
        }
    }

    @GuardedBy("mAudioPolicies")
    private AudioPolicyProxy checkUpdateForPolicy(IAudioPolicyCallback pcb, String errorMsg) {
        if (!(this.mContext.checkCallingPermission("android.permission.MODIFY_AUDIO_ROUTING") == 0)) {
            Slog.w(TAG, errorMsg + " for pid " + Binder.getCallingPid() + " / uid " + Binder.getCallingUid() + ", need MODIFY_AUDIO_ROUTING");
            return null;
        }
        AudioPolicyProxy app = this.mAudioPolicies.get(pcb.asBinder());
        if (app != null) {
            return app;
        }
        Slog.w(TAG, errorMsg + " for pid " + Binder.getCallingPid() + " / uid " + Binder.getCallingUid() + ", unregistered policy");
        return null;
    }

    public int addMixForPolicy(AudioPolicyConfig policyConfig, IAudioPolicyCallback pcb) {
        Log.d(TAG, "addMixForPolicy for " + pcb.asBinder() + " with config:" + policyConfig);
        synchronized (this.mAudioPolicies) {
            AudioPolicyProxy app = checkUpdateForPolicy(pcb, "Cannot add AudioMix in audio policy");
            if (app == null) {
                return -1;
            }
            app.addMixes(policyConfig.getMixes());
            return 0;
        }
    }

    public int removeMixForPolicy(AudioPolicyConfig policyConfig, IAudioPolicyCallback pcb) {
        Log.d(TAG, "removeMixForPolicy for " + pcb.asBinder() + " with config:" + policyConfig);
        synchronized (this.mAudioPolicies) {
            AudioPolicyProxy app = checkUpdateForPolicy(pcb, "Cannot add AudioMix in audio policy");
            if (app == null) {
                return -1;
            }
            app.removeMixes(policyConfig.getMixes());
            return 0;
        }
    }

    public int setFocusPropertiesForPolicy(int duckingBehavior, IAudioPolicyCallback pcb) {
        Log.d(TAG, "setFocusPropertiesForPolicy() duck behavior=" + duckingBehavior + " policy " + pcb.asBinder());
        synchronized (this.mAudioPolicies) {
            AudioPolicyProxy app = checkUpdateForPolicy(pcb, "Cannot change audio policy focus properties");
            if (app == null) {
                return -1;
            }
            if (!this.mAudioPolicies.containsKey(pcb.asBinder())) {
                Slog.e(TAG, "Cannot change audio policy focus properties, unregistered policy");
                return -1;
            }
            boolean z = true;
            if (duckingBehavior == 1) {
                for (AudioPolicyProxy policy : this.mAudioPolicies.values()) {
                    if (policy.mFocusDuckBehavior == 1) {
                        Slog.e(TAG, "Cannot change audio policy ducking behavior, already handled");
                        return -1;
                    }
                }
            }
            app.mFocusDuckBehavior = duckingBehavior;
            MediaFocusControl mediaFocusControl = this.mMediaFocusControl;
            if (duckingBehavior != 1) {
                z = false;
            }
            mediaFocusControl.setDuckingInExtPolicyAvailable(z);
            return 0;
        }
    }

    /* access modifiers changed from: private */
    public void setExtVolumeController(IAudioPolicyCallback apc) {
        if (!this.mContext.getResources().getBoolean(17956980)) {
            Log.e(TAG, "Cannot set external volume controller: device not set for volume keys handled in PhoneWindowManager");
            return;
        }
        synchronized (this.mExtVolumeControllerLock) {
            if (this.mExtVolumeController != null && !this.mExtVolumeController.asBinder().pingBinder()) {
                Log.e(TAG, "Cannot set external volume controller: existing controller");
            }
            this.mExtVolumeController = apc;
        }
    }

    private void dumpAudioPolicies(PrintWriter pw) {
        pw.println("\nAudio policies:");
        synchronized (this.mAudioPolicies) {
            for (AudioPolicyProxy policy : this.mAudioPolicies.values()) {
                pw.println(policy.toLogFriendlyString());
            }
        }
    }

    /* access modifiers changed from: private */
    public void onDynPolicyMixStateUpdate(String regId, int state) {
        Log.d(TAG, "onDynamicPolicyMixStateUpdate(" + regId + ", " + state + ")");
        synchronized (this.mAudioPolicies) {
            for (AudioPolicyProxy policy : this.mAudioPolicies.values()) {
                Iterator it = policy.getMixes().iterator();
                while (it.hasNext()) {
                    if (((AudioMix) it.next()).getRegistration().equals(regId)) {
                        try {
                            policy.mPolicyCallback.notifyMixStateUpdate(regId, state);
                        } catch (RemoteException e) {
                            Log.e(TAG, "Can't call notifyMixStateUpdate() on IAudioPolicyCallback " + policy.mPolicyCallback.asBinder(), e);
                        }
                    }
                }
            }
        }
    }

    public void registerRecordingCallback(IRecordingConfigDispatcher rcdb) {
        this.mRecordMonitor.registerRecordingCallback(rcdb, this.mContext.checkCallingPermission("android.permission.MODIFY_AUDIO_ROUTING") == 0);
    }

    public void unregisterRecordingCallback(IRecordingConfigDispatcher rcdb) {
        this.mRecordMonitor.unregisterRecordingCallback(rcdb);
    }

    public List<AudioRecordingConfiguration> getActiveRecordingConfigurations() {
        return this.mRecordMonitor.getActiveRecordingConfigurations(this.mContext.checkCallingPermission("android.permission.MODIFY_AUDIO_ROUTING") == 0);
    }

    public void disableRingtoneSync(int userId) {
        if (UserHandle.getCallingUserId() != userId) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL", "disable sound settings syncing for another profile");
        }
        long token = Binder.clearCallingIdentity();
        try {
            Settings.Secure.putIntForUser(this.mContentResolver, "sync_parent_sounds", 0, userId);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public void registerPlaybackCallback(IPlaybackConfigDispatcher pcdb) {
        this.mPlaybackMonitor.registerPlaybackCallback(pcdb, this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_AUDIO_ROUTING") == 0);
    }

    public void unregisterPlaybackCallback(IPlaybackConfigDispatcher pcdb) {
        this.mPlaybackMonitor.unregisterPlaybackCallback(pcdb);
    }

    public List<AudioPlaybackConfiguration> getActivePlaybackConfigurations() {
        return this.mPlaybackMonitor.getActivePlaybackConfigurations(this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_AUDIO_ROUTING") == 0);
    }

    public int trackPlayer(PlayerBase.PlayerIdCard pic) {
        return this.mPlaybackMonitor.trackPlayer(pic);
    }

    public void playerAttributes(int piid, AudioAttributes attr) {
        this.mPlaybackMonitor.playerAttributes(piid, attr, Binder.getCallingUid());
    }

    public void playerEvent(int piid, int event) {
        this.mPlaybackMonitor.playerEvent(piid, event, Binder.getCallingUid());
    }

    public void playerHasOpPlayAudio(int piid, boolean hasOpPlayAudio) {
        this.mPlaybackMonitor.playerHasOpPlayAudio(piid, hasOpPlayAudio, Binder.getCallingUid());
    }

    public void releasePlayer(int piid) {
        this.mPlaybackMonitor.releasePlayer(piid, Binder.getCallingUid());
    }

    public int dispatchFocusChange(AudioFocusInfo afi, int focusChange, IAudioPolicyCallback pcb) {
        int dispatchFocusChange;
        if (afi == null) {
            throw new IllegalArgumentException("Illegal null AudioFocusInfo");
        } else if (pcb != null) {
            synchronized (this.mAudioPolicies) {
                if (this.mAudioPolicies.containsKey(pcb.asBinder())) {
                    dispatchFocusChange = this.mMediaFocusControl.dispatchFocusChange(afi, focusChange);
                } else {
                    throw new IllegalStateException("Unregistered AudioPolicy for focus dispatch");
                }
            }
            return dispatchFocusChange;
        } else {
            throw new IllegalArgumentException("Illegal null AudioPolicy callback");
        }
    }

    public void setFocusRequestResultFromExtPolicy(AudioFocusInfo afi, int requestResult, IAudioPolicyCallback pcb) {
        if (afi == null) {
            throw new IllegalArgumentException("Illegal null AudioFocusInfo");
        } else if (pcb != null) {
            synchronized (this.mAudioPolicies) {
                if (this.mAudioPolicies.containsKey(pcb.asBinder())) {
                    this.mMediaFocusControl.setFocusRequestResultFromExtPolicy(afi, requestResult);
                } else {
                    throw new IllegalStateException("Unregistered AudioPolicy for external focus");
                }
            }
        } else {
            throw new IllegalArgumentException("Illegal null AudioPolicy callback");
        }
    }

    private void checkMonitorAudioServerStatePermission() {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE") != 0 && this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_AUDIO_ROUTING") != 0) {
            throw new SecurityException("Not allowed to monitor audioserver state");
        }
    }

    public void registerAudioServerStateDispatcher(IAudioServerStateDispatcher asd) {
        checkMonitorAudioServerStatePermission();
        synchronized (this.mAudioServerStateListeners) {
            if (this.mAudioServerStateListeners.containsKey(asd.asBinder())) {
                Slog.w(TAG, "Cannot re-register audio server state dispatcher");
                return;
            }
            AsdProxy asdp = new AsdProxy(asd);
            try {
                asd.asBinder().linkToDeath(asdp, 0);
            } catch (RemoteException e) {
            }
            this.mAudioServerStateListeners.put(asd.asBinder(), asdp);
        }
    }

    public void unregisterAudioServerStateDispatcher(IAudioServerStateDispatcher asd) {
        checkMonitorAudioServerStatePermission();
        synchronized (this.mAudioServerStateListeners) {
            AsdProxy asdp = this.mAudioServerStateListeners.remove(asd.asBinder());
            if (asdp == null) {
                Slog.w(TAG, "Trying to unregister unknown audioserver state dispatcher for pid " + Binder.getCallingPid() + " / uid " + Binder.getCallingUid());
                return;
            }
            asd.asBinder().unlinkToDeath(asdp, 0);
        }
    }

    public boolean isAudioServerRunning() {
        checkMonitorAudioServerStatePermission();
        return AudioSystem.checkAudioFlinger() == 0;
    }

    /* access modifiers changed from: protected */
    public int hwGetDeviceForStream(int activeStreamType) {
        return getDeviceForStream(mStreamVolumeAlias[activeStreamType]);
    }

    /* access modifiers changed from: protected */
    public int getStreamIndex(int stream_type, int device) {
        if (stream_type > -1 && stream_type < AudioSystem.getNumStreamTypes()) {
            return this.mStreamStates[stream_type].getIndex(device);
        }
        Log.e(TAG, "invalid stream type!!!");
        return -1;
    }

    /* access modifiers changed from: protected */
    public int getStreamMaxIndex(int stream_type) {
        if (stream_type > -1 && stream_type < AudioSystem.getNumStreamTypes()) {
            return this.mStreamStates[stream_type].getMaxIndex();
        }
        Log.e(TAG, "invalid stream type!!!");
        return -1;
    }

    /* access modifiers changed from: protected */
    public boolean isConnectedUsbOutDevice() {
        for (int i = 0; i < this.mConnectedDevices.size(); i++) {
            if (this.mConnectedDevices.valueAt(i).mDeviceType == 67108864) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isConnectedUsbInDevice() {
        return SystemProperties.getBoolean("persist.sys.usb.capture", false);
    }

    /* access modifiers changed from: protected */
    public boolean isConnectedHeadSet() {
        return this.mConnectedDevices.get(makeDeviceListKey(4, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS)) != null;
    }

    /* access modifiers changed from: protected */
    public boolean isConnectedHeadPhone() {
        boolean headphoneConnected = this.mConnectedDevices.get(makeDeviceListKey(8, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS)) != null;
        boolean lineConnected = this.mConnectedDevices.get(makeDeviceListKey(131072, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS)) != null;
        if (headphoneConnected || lineConnected) {
            return true;
        }
        return false;
    }

    public int getSampleId(SoundPool soundpool, int effect, String defFilePath, int index) {
        return this.mSoundPool.load(defFilePath, 0);
    }

    /* access modifiers changed from: protected */
    public void appendExtraInfo(Intent intent) {
    }

    /* access modifiers changed from: protected */
    public String getPackageNameByPid(int pid) {
        return null;
    }

    /* access modifiers changed from: protected */
    public void sendCommForceBroadcast() {
    }

    /* access modifiers changed from: protected */
    public void checkMuteRcvDelay(int curMode, int mode) {
    }

    /* access modifiers changed from: protected */
    public boolean checkEnbaleVolumeAdjust() {
        return true;
    }

    /* access modifiers changed from: protected */
    public void processMediaServerRestart() {
    }

    /* access modifiers changed from: protected */
    public void onUserBackground(int userId) {
    }

    /* access modifiers changed from: protected */
    public void onUserForeground(int userId) {
    }

    /* access modifiers changed from: protected */
    public void updateDefaultStream() {
    }

    private boolean isHisiPlatform() {
        String platform = SystemProperties.get("ro.board.platform", UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN);
        if (platform == null || (!platform.startsWith("hi") && !platform.startsWith("kirin"))) {
            return false;
        }
        return true;
    }

    private void sendBehavior(IHwBehaviorCollectManager.BehaviorId bid, Object... params) {
        if (this.mHwBehaviorManager == null) {
            this.mHwBehaviorManager = HwFrameworkFactory.getHwBehaviorCollectManager();
        }
        if (this.mHwBehaviorManager == null) {
            Log.w(TAG, "HwBehaviorCollectManager is null");
        } else if (params == null || params.length == 0) {
            this.mHwBehaviorManager.sendBehavior(Binder.getCallingUid(), Binder.getCallingPid(), bid);
        } else {
            this.mHwBehaviorManager.sendBehavior(Binder.getCallingUid(), Binder.getCallingPid(), bid, params);
        }
    }

    /* JADX WARNING: type inference failed for: r0v0, types: [com.android.server.audio.AudioService$HwInnerAudioService, android.os.IBinder] */
    public IBinder getHwInnerService() {
        return this.mHwInnerService;
    }

    public boolean isConnectedHeadSetEx() {
        return isConnectedHeadSet();
    }

    public boolean isConnectedHeadPhoneEx() {
        return isConnectedHeadPhone();
    }

    public boolean isConnectedUsbOutDeviceEx() {
        return isConnectedUsbOutDevice();
    }

    public boolean isConnectedUsbInDeviceEx() {
        return isConnectedUsbInDevice();
    }

    public boolean checkAudioSettingsPermissionEx(String method) {
        return checkAudioSettingsPermission(method);
    }
}
