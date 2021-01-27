package com.android.server.audio;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.AlarmManager;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.IUidObserver;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.role.OnRoleHoldersChangedListener;
import android.app.role.RoleManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import android.hardware.hdmi.HdmiAudioSystemClient;
import android.hardware.hdmi.HdmiControlManager;
import android.hardware.hdmi.HdmiPlaybackClient;
import android.hardware.hdmi.HdmiTvClient;
import android.media.AudioAttributes;
import android.media.AudioFocusInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioManagerInternal;
import android.media.AudioPlaybackConfiguration;
import android.media.AudioRecordingConfiguration;
import android.media.AudioRoutesInfo;
import android.media.AudioSystem;
import android.media.HwMediaMonitorManager;
import android.media.IAudioFocusChangeDispatcher;
import android.media.IAudioFocusDispatcher;
import android.media.IAudioModeDispatcher;
import android.media.IAudioRoutesObserver;
import android.media.IAudioServerStateDispatcher;
import android.media.IPlaybackConfigDispatcher;
import android.media.IRecordingConfigDispatcher;
import android.media.IRingtonePlayer;
import android.media.IVolumeChangeDispatcher;
import android.media.IVolumeController;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.media.PlayerBase;
import android.media.SoundPool;
import android.media.VolumePolicy;
import android.media.audiopolicy.AudioMix;
import android.media.audiopolicy.AudioPolicyConfig;
import android.media.audiopolicy.AudioProductStrategy;
import android.media.audiopolicy.AudioVolumeGroup;
import android.media.audiopolicy.IAudioPolicyCallback;
import android.media.projection.IMediaProjection;
import android.media.projection.IMediaProjectionCallback;
import android.media.projection.IMediaProjectionManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
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
import android.util.IntArray;
import android.util.Jlog;
import android.util.Log;
import android.util.MathUtils;
import android.util.Slog;
import android.util.SparseIntArray;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.os.HwBootAnimationOeminfo;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.Preconditions;
import com.android.internal.util.XmlUtils;
import com.android.server.BatteryService;
import com.android.server.EventLogTags;
import com.android.server.HwServiceExFactory;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.audio.AbsAudioService;
import com.android.server.audio.AudioEventLogger;
import com.android.server.audio.AudioServiceEvents;
import com.android.server.hdmi.HdmiCecKeycode;
import com.android.server.location.IHwLbsLogger;
import com.android.server.pm.DumpState;
import com.android.server.pm.UserManagerService;
import com.android.server.slice.SliceClientPermissions;
import com.android.server.utils.PriorityDump;
import com.android.server.wm.ActivityTaskManagerInternal;
import com.huawei.android.audio.IHwAudioServiceManager;
import huawei.android.security.IHwBehaviorCollectManager;
import huawei.cust.HwCustUtils;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import org.xmlpull.v1.XmlPullParserException;

public class AudioService extends AbsAudioService implements AccessibilityManager.TouchExplorationStateChangeListener, AccessibilityManager.AccessibilityServicesStateChangeListener, IHwAudioServiceInner {
    private static final int ABS_MEDIA_VOLUME_ACTIVE = 2;
    private static final int ABS_MEDIA_VOLUME_NOT_CONFIGURED = 0;
    private static final int ABS_MEDIA_VOLUME_STARTED = 1;
    private static final int ABS_MEDIA_VOLUME_TIME_OUT_MS = SystemProperties.getInt("persist.sys.abs_volume_timeout", 200);
    private static final String ACTION_CHANGE_ACTIVE_DEVICE = "ChangeActiveDevice";
    private static final String ACTION_CHECK_MUSIC_ACTIVE = "ACTION_CHECK_MUSIC_ACTIVE";
    private static final int ACTIVE_CHECK_RETRY_COUNT = 10;
    private static final String ASSET_FILE_VERSION = "1.0";
    private static final String ATTR_ASSET_FILE = "file";
    private static final String ATTR_ASSET_ID = "id";
    private static final String ATTR_GROUP_NAME = "name";
    private static final String ATTR_VERSION = "version";
    private static final int CHECK_MUSIC_ACTIVE_DELAY_MS = 3000;
    private static final int CHINAZONE_IDENTIFIER = 156;
    static final int CONNECTION_STATE_CONNECTED = 1;
    static final int CONNECTION_STATE_DISCONNECTED = 0;
    protected static final boolean DEBUG_AP = true;
    protected static final boolean DEBUG_DEVICES = true;
    protected static final boolean DEBUG_MODE = true;
    protected static final boolean DEBUG_VOL = true;
    private static final int DEFAULT_STREAM_TYPE_OVERRIDE_DELAY_MS = 0;
    protected static final String DEFAULT_VOLUME_KEY_CTL = "default_volume_key_control";
    protected static final int DEFAULT_VOL_STREAM_NO_PLAYBACK = 3;
    private static final int DEVICE_MEDIA_UNMUTED_ON_PLUG = 604137356;
    private static final int FLAG_ADJUST_VOLUME = 1;
    private static final String GROUP_TOUCH_SOUNDS = "touch_sounds";
    private static final int INDEX_DIVIDE_BY_TWO = 2;
    private static final int INDICATE_SYSTEM_READY_RETRY_DELAY_MS = 1000;
    private static final String LOCATE_CHANGED_PARAMETER = "locale_changed=true";
    static final int LOG_NB_EVENTS_DEVICE_CONNECTION = 30;
    static final int LOG_NB_EVENTS_DYN_POLICY = 10;
    static final int LOG_NB_EVENTS_FORCE_USE = 20;
    static final int LOG_NB_EVENTS_PHONE_STATE = 20;
    static final int LOG_NB_EVENTS_VOLUME = 40;
    protected static int[] MAX_STREAM_VOLUME = {5, 7, 7, 15, 7, 7, 15, 7, 15, 15, 15};
    private static final int MAX_VOLUME = 100;
    protected static int[] MIN_STREAM_VOLUME = {1, 0, 0, 0, 1, 0, 1, 0, 0, 1, 1};
    private static final int MIN_VOLUME = 0;
    private static final int MSG_ACCESSORY_PLUG_MEDIA_UNMUTE = 21;
    private static final int MSG_AUDIO_SERVER_DIED = 4;
    private static final int MSG_BT_HEADSET_CNCT_FAILED = 9;
    private static final int MSG_CHECK_ABS_VOLUME_STATE = 30;
    private static final int MSG_CHECK_MUSIC_ACTIVE = 11;
    private static final int MSG_CONFIGURE_SAFE_MEDIA_VOLUME = 12;
    private static final int MSG_CONFIGURE_SAFE_MEDIA_VOLUME_FORCED = 13;
    private static final int MSG_DISABLE_AUDIO_FOR_UID = 100;
    private static final int MSG_DISPATCH_AUDIO_SERVER_STATE = 23;
    private static final int MSG_DYN_POLICY_MIX_STATE_UPDATE = 19;
    private static final int MSG_ENABLE_SURROUND_FORMATS = 24;
    private static final int MSG_HDMI_VOLUME_CHECK = 28;
    private static final int MSG_INDICATE_SYSTEM_READY = 20;
    private static final int MSG_LOAD_SOUND_EFFECTS = 7;
    private static final int MSG_NOTIFY_VOL_EVENT = 22;
    private static final int MSG_OBSERVE_DEVICES_FOR_ALL_STREAMS = 27;
    private static final int MSG_PERSIST_MUSIC_ACTIVE_MS = 17;
    private static final int MSG_PERSIST_MUTE_STATE = 31;
    private static final int MSG_PERSIST_RINGER_MODE = 3;
    private static final int MSG_PERSIST_SAFE_VOLUME_STATE = 14;
    private static final int MSG_PERSIST_VOLUME = 1;
    private static final int MSG_PLAYBACK_CONFIG_CHANGE = 29;
    private static final int MSG_PLAY_SOUND_EFFECT = 5;
    private static final int MSG_SET_ALL_VOLUMES = 10;
    private static final int MSG_SET_DEVICE_STREAM_VOLUME = 26;
    private static final int MSG_SET_DEVICE_VOLUME = 0;
    protected static final int MSG_SET_FORCE_USE = 8;
    private static final int MSG_SYSTEM_READY = 16;
    private static final int MSG_UNLOAD_SOUND_EFFECTS = 15;
    private static final int MSG_UNMUTE_STREAM = 18;
    private static final int MSG_UPDATE_RINGER_MODE = 25;
    private static final int MUSIC_ACTIVE_POLL_PERIOD_MS = 60000;
    private static final int NUM_SOUNDPOOL_CHANNELS = 4;
    private static final String PERMISSION_CHANGE_ACTIVE_DEVICE = "com.huawei.android.permission.CHANGEACTIVEDEVICE";
    protected static final int PERSIST_DELAY = 500;
    private static final String[] RINGER_MODE_NAMES = {"SILENT", "VIBRATE", PriorityDump.PRIORITY_ARG_NORMAL};
    private static final int SAFE_MEDIA_VOLUME_ACTIVE = 3;
    private static final int SAFE_MEDIA_VOLUME_DISABLED = 1;
    private static final int SAFE_MEDIA_VOLUME_INACTIVE = 2;
    private static final int SAFE_MEDIA_VOLUME_NOT_CONFIGURED = 0;
    private static final int SAFE_VOLUME_CONFIGURE_TIMEOUT_MS = 30000;
    protected static final int SENDMSG_NOOP = 1;
    protected static final int SENDMSG_QUEUE = 2;
    protected static final int SENDMSG_REPLACE = 0;
    private static final String SHOULD_RECOVER_DEFAULT_VOLUME = "true";
    private static final int SOUND_EFFECTS_LOAD_TIMEOUT_MS = 5000;
    private static final String SOUND_EFFECTS_PATH = "/media/audio/ui/";
    private static final List<String> SOUND_EFFECT_FILES = new ArrayList();
    private static final int[] STREAM_VOLUME_OPS = {34, 36, 35, 36, 37, 38, 39, 36, 36, 36, 64};
    private static final String SYSTEM_PERMISSION = "huawei.android.permission.HW_SIGNATURE_OR_SYSTEM";
    private static final String TAG = "AS.AudioService";
    private static final String TAG_ASSET = "asset";
    private static final String TAG_AUDIO_ASSETS = "audio_assets";
    private static final String TAG_GROUP = "group";
    private static final int TOUCH_EXPLORE_STREAM_TYPE_OVERRIDE_DELAY_MS = 1000;
    private static final int UNMUTE_STREAM_DELAY = 350;
    private static final int UNSAFE_VOLUME_MUSIC_ACTIVE_MS_MAX = SystemProperties.getInt("ro.config.hw.security_test", 72000000);
    private static final AudioAttributes VIBRATION_ATTRIBUTES = new AudioAttributes.Builder().setContentType(4).setUsage(13).build();
    private static final String VOLUME_DEFAULT_NAME = "volume_default";
    private static final String VOLUME_MAX_NAME = "volume_max";
    private static final String VOLUME_MIN_NAME = "volume_min";
    private static final int VOLUME_NOT_SET = -1;
    private static final String VOLUME_RECOVERY_NAME = "volume_auto_recovery";
    private static final int VOLUME_TO_INDEX = 10;
    private static final boolean isTv = "tv".equals(SystemProperties.get("ro.build.characteristics", BatteryService.HealthServiceWrapper.INSTANCE_VENDOR));
    protected static int[] mStreamVolumeAlias;
    static final AudioEventLogger sDeviceLogger = new AudioEventLogger(30, "wired/A2DP/hearing aid device connection");
    static final AudioEventLogger sForceUseLogger = new AudioEventLogger(20, "force use (logged before setForceUse() is executed)");
    private static boolean sIndependentA11yVolume = false;
    private static int sSoundEffectVolumeDb;
    private static int sStreamOverrideDelayMs;
    static final AudioEventLogger sVolumeLogger = new AudioEventLogger(40, "volume changes (logged when command received by AudioService)");
    private final int[][] SOUND_EFFECT_FILES_MAP = ((int[][]) Array.newInstance(int.class, 10, 2));
    private final int[] STREAM_VOLUME_ALIAS_DEFAULT = {0, 2, 2, 3, 4, 2, 6, 2, 2, 9, 3};
    private final int[] STREAM_VOLUME_ALIAS_TELEVISION = {3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3};
    private final int[] STREAM_VOLUME_ALIAS_TV = {0, 2, 3, 3, 4, 2, 6, 2, 2, 9, 3};
    private final int[] STREAM_VOLUME_ALIAS_VOICE = {0, 2, 2, 3, 4, 2, 6, 2, 2, 9, 3};
    private final Object mAbsMediaVolumeStateLock = new Object();
    int mAbsVolumeMultiModeCaseDevices = DumpState.DUMP_HWFEATURES;
    private int mAbsVolumeState = 0;
    private int[] mAccessibilityServiceUids;
    private final Object mAccessibilityServiceUidsLock = new Object();
    private int mActiveCheckRetryCount = 10;
    protected int mActiveDevice = 1073741824;
    private final ActivityManagerInternal mActivityManagerInternal;
    private AlarmManager mAlarmManager = null;
    private final AppOpsManager mAppOps;
    @GuardedBy({"mSettingsLock"})
    private int mAssistantUid;
    private PowerManager.WakeLock mAudioEventWakeLock;
    protected AudioHandler mAudioHandler;
    private final HashMap<IBinder, AudioPolicyProxy> mAudioPolicies = new HashMap<>();
    @GuardedBy({"mAudioPolicies"})
    private int mAudioPolicyCounter = 0;
    private HashMap<IBinder, AsdProxy> mAudioServerStateListeners = new HashMap<>();
    private final AudioSystem.ErrorCallback mAudioSystemCallback = new AudioSystem.ErrorCallback() {
        /* class com.android.server.audio.AudioService.AnonymousClass1 */

        public void onError(int error) {
            if (error == 100) {
                if (AudioService.this.mRecordMonitor != null) {
                    AudioService.this.mRecordMonitor.onAudioServerDied();
                }
                AudioService.sendMsg(AudioService.this.mAudioHandler, 4, 1, 0, 0, null, 0);
                AudioService.sendMsg(AudioService.this.mAudioHandler, AudioService.MSG_DISPATCH_AUDIO_SERVER_STATE, 2, 0, 0, null, 0);
            }
        }
    };
    private AudioSystemThread mAudioSystemThread;
    @GuardedBy({"mSettingsLock"})
    private boolean mCameraSoundForced;
    private final ContentResolver mContentResolver;
    private final Context mContext;
    private HwCustAudioService mCust = null;
    protected int mDefaultVolStream = 3;
    protected final AudioDeviceBroker mDeviceBroker;
    private boolean mDockAudioMediaEnabled = true;
    private int mDockState = 0;
    private final AudioSystem.DynamicPolicyCallback mDynPolicyCallback = new AudioSystem.DynamicPolicyCallback() {
        /* class com.android.server.audio.AudioService.AnonymousClass5 */

        public void onDynamicPolicyMixStateUpdate(String regId, int state) {
            if (!TextUtils.isEmpty(regId)) {
                AudioService.sendMsg(AudioService.this.mAudioHandler, 19, 2, state, 0, regId, 0);
            }
        }
    };
    private final AudioEventLogger mDynPolicyLogger = new AudioEventLogger(10, "dynamic policy events (logged when command received by AudioService)");
    private String mEnabledSurroundFormats;
    private int mEncodedSurroundMode;
    private IAudioPolicyCallback mExtVolumeController;
    private final Object mExtVolumeControllerLock = new Object();
    private boolean mFactoryMode = "factory".equals(SystemProperties.get("ro.runmode", "normal"));
    int mFixedVolumeDevices = 2889728;
    private ForceControlStreamClient mForceControlStreamClient = null;
    private final Object mForceControlStreamLock = new Object();
    int mFullVolumeDevices = 0;
    private boolean mHasAlarm = false;
    private final boolean mHasVibrator;
    @GuardedBy({"mHdmiClientLock"})
    private HdmiAudioSystemClient mHdmiAudioSystemClient;
    private boolean mHdmiCecSink;
    private final Object mHdmiClientLock = new Object();
    private MyDisplayStatusCallback mHdmiDisplayStatusCallback = new MyDisplayStatusCallback();
    @GuardedBy({"mHdmiClientLock"})
    private HdmiControlManager mHdmiManager;
    @GuardedBy({"mHdmiClientLock"})
    private HdmiPlaybackClient mHdmiPlaybackClient;
    private boolean mHdmiSystemAudioSupported = false;
    @GuardedBy({"mHdmiClientLock"})
    private HdmiTvClient mHdmiTvClient;
    protected IHwAudioServiceEx mHwAudioServiceEx = null;
    private IHwBehaviorCollectManager mHwBehaviorManager;
    HwInnerAudioService mHwInnerService = new HwInnerAudioService(this);
    private boolean mIsChineseZone = true;
    private final boolean mIsSingleVolume;
    private long mLoweredFromNormalToVibrateTime;
    private int mMcc = 0;
    protected final MediaFocusControl mMediaFocusControl;
    private int mMode = 0;
    private final AudioEventLogger mModeLogger = new AudioEventLogger(20, "phone state (logged after successfull call to AudioSystem.setPhoneState(int))");
    private final boolean mMonitorRotation;
    private int mMusicActiveMs;
    private int mMuteAffectedStreams;
    private int mMutedStreams;
    private NotificationManager mNm;
    private PendingIntent mPendingIntent = null;
    private StreamVolumeCommand mPendingVolumeCommand;
    private final int mPlatformType;
    protected final PlaybackActivityMonitor mPlaybackMonitor;
    private float[] mPrescaleAbsoluteVolume = {0.5f, 0.7f, 0.85f};
    private int mPrevVolDirection = 0;
    private IMediaProjectionManager mProjectionService;
    private final BroadcastReceiver mReceiver = new AudioServiceBroadcastReceiver();
    private final RecordingActivityMonitor mRecordMonitor;
    private int mRingerAndZenModeMutedStreams;
    @GuardedBy({"mSettingsLock"})
    private int mRingerMode;
    private int mRingerModeAffectedStreams = 0;
    private AudioManagerInternal.RingerModeDelegate mRingerModeDelegate;
    @GuardedBy({"mSettingsLock"})
    private int mRingerModeExternal = -1;
    private volatile IRingtonePlayer mRingtonePlayer;
    private ArrayList<RmtSbmxFullVolDeathHandler> mRmtSbmxFullVolDeathHandlers = new ArrayList<>();
    private int mRmtSbmxFullVolRefCount = 0;
    RoleObserver mRoleObserver;
    final int mSafeMediaVolumeDevices = 603979788;
    private int mSafeMediaVolumeIndex;
    private int mSafeMediaVolumeState;
    private final Object mSafeMediaVolumeStateLock = new Object();
    private float mSafeUsbMediaVolumeDbfs;
    private int mSafeUsbMediaVolumeIndex;
    private String mSafeVolumeCaller = null;
    @GuardedBy({"mDeviceBroker.mSetModeLock"})
    protected final ArrayList<SetModeDeathHandler> mSetModeDeathHandlers = new ArrayList<>();
    private final Object mSettingsLock = new Object();
    private SettingsObserver mSettingsObserver;
    private final Object mSoundEffectsLock = new Object();
    private SoundPool mSoundPool;
    private SoundPoolCallback mSoundPoolCallBack;
    private SoundPoolListenerThread mSoundPoolListenerThread;
    private Looper mSoundPoolLooper = null;
    private VolumeStreamState[] mStreamStates;
    private boolean mSurroundModeChanged;
    protected boolean mSystemReady;
    private final IUidObserver mUidObserver = new IUidObserver.Stub() {
        /* class com.android.server.audio.AudioService.AnonymousClass2 */

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
            AudioService audioService = AudioService.this;
            audioService.queueMsgUnderWakeLock(audioService.mAudioHandler, 100, disable ? 1 : 0, uid, null, 0);
        }
    };
    private final boolean mUseFixedVolume;
    private final UserManagerInternal mUserManagerInternal;
    private final UserManagerInternal.UserRestrictionsListener mUserRestrictionsListener = new AudioServiceUserRestrictionsListener();
    private boolean mUserSelectedVolumeControlStream = false;
    private boolean mUserSwitchedReceived;
    private int mVibrateSetting;
    private Vibrator mVibrator;
    private AtomicBoolean mVoiceActive = new AtomicBoolean(false);
    private final IPlaybackConfigDispatcher mVoiceActivityMonitor = new IPlaybackConfigDispatcher.Stub() {
        /* class com.android.server.audio.AudioService.AnonymousClass3 */

        public void dispatchPlaybackConfigChange(List<AudioPlaybackConfiguration> configs, boolean flush) {
            AudioService.sendMsg(AudioService.this.mAudioHandler, 29, 0, 0, 0, configs, 0);
        }
    };
    private int mVolumeControlStream = -1;
    private final VolumeController mVolumeController = new VolumeController();
    private VolumePolicy mVolumePolicy = VolumePolicy.DEFAULT;
    private int mZenModeAffectedStreams = 0;

    @Retention(RetentionPolicy.SOURCE)
    public @interface BtProfileConnectionState {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ConnectionState {
    }

    static /* synthetic */ int access$7910(AudioService x0) {
        int i = x0.mActiveCheckRetryCount;
        x0.mActiveCheckRetryCount = i - 1;
        return i;
    }

    static /* synthetic */ int access$9808(AudioService x0) {
        int i = x0.mAudioPolicyCounter;
        x0.mAudioPolicyCounter = i + 1;
        return i;
    }

    private boolean isPlatformVoice() {
        return this.mPlatformType == 1;
    }

    /* access modifiers changed from: package-private */
    public boolean isPlatformTelevision() {
        return this.mPlatformType == 2;
    }

    /* access modifiers changed from: package-private */
    public boolean isPlatformAutomotive() {
        return this.mContext.getPackageManager().hasSystemFeature("android.hardware.type.automotive");
    }

    /* access modifiers changed from: package-private */
    public int getVssVolumeForDevice(int stream, int device) {
        return this.mStreamStates[stream].getIndex(device);
    }

    public static String makeAlsaAddressString(int card, int device) {
        return "card=" + card + ";device=" + device + ";";
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

        /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.server.audio.AudioService$Lifecycle */
        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.audio.AudioService, android.os.IBinder] */
        /* JADX WARNING: Unknown variable types count: 1 */
        @Override // com.android.server.SystemService
        public void onStart() {
            publishBinderService("audio", this.mService);
        }

        @Override // com.android.server.SystemService
        public void onBootPhase(int phase) {
            if (phase == 550) {
                this.mService.systemReady();
            }
        }
    }

    public AudioService(Context context) {
        int i;
        this.mContext = context;
        this.mContentResolver = context.getContentResolver();
        this.mCust = (HwCustAudioService) HwCustUtils.createObj(HwCustAudioService.class, new Object[]{this.mContext});
        this.mAppOps = (AppOpsManager) context.getSystemService("appops");
        this.mPlatformType = AudioSystem.getPlatformType(context);
        this.mIsSingleVolume = AudioSystem.isSingleVolume(context);
        this.mUserManagerInternal = (UserManagerInternal) LocalServices.getService(UserManagerInternal.class);
        this.mActivityManagerInternal = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
        this.mAudioEventWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, "handleAudioEvent");
        this.mVibrator = (Vibrator) context.getSystemService("vibrator");
        Vibrator vibrator = this.mVibrator;
        this.mHasVibrator = vibrator == null ? false : vibrator.hasVibrator();
        if (AudioProductStrategy.getAudioProductStrategies().size() > 0) {
            for (int streamType = AudioSystem.getNumStreamTypes() - 1; streamType >= 0; streamType--) {
                AudioAttributes attr = AudioProductStrategy.getAudioAttributesForStrategyWithLegacyStreamType(streamType);
                int maxVolume = AudioSystem.getMaxVolumeIndexForAttributes(attr);
                if (maxVolume != -1) {
                    MAX_STREAM_VOLUME[streamType] = maxVolume;
                }
                int minVolume = AudioSystem.getMinVolumeIndexForAttributes(attr);
                if (minVolume != -1) {
                    MIN_STREAM_VOLUME[streamType] = minVolume;
                }
            }
        }
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
        int maxVolume2 = SystemProperties.getInt("ro.config.vol_steps", -1);
        if (maxVolume2 > 0) {
            int index = 0;
            while (true) {
                int[] iArr = MAX_STREAM_VOLUME;
                if (index >= iArr.length) {
                    break;
                }
                iArr[index] = maxVolume2;
                index++;
            }
        }
        int maxCallVolume = SystemProperties.getInt("ro.config.vc_call_vol_steps", -1);
        if (maxCallVolume != -1) {
            MAX_STREAM_VOLUME[0] = maxCallVolume;
            AudioSystem.DEFAULT_STREAM_VOLUME[0] = (maxCallVolume * 4) / 5;
        }
        int defaultCallVolume = SystemProperties.getInt("ro.config.vc_call_vol_default", -1);
        if (defaultCallVolume != -1 && defaultCallVolume <= MAX_STREAM_VOLUME[0] && defaultCallVolume >= MIN_STREAM_VOLUME[0]) {
            AudioSystem.DEFAULT_STREAM_VOLUME[0] = defaultCallVolume;
        }
        int maxMusicVolume = SystemProperties.getInt("ro.config.media_vol_steps", -1);
        if (maxMusicVolume != -1) {
            MAX_STREAM_VOLUME[3] = maxMusicVolume;
        }
        int defaultMusicVolume = SystemProperties.getInt("ro.config.media_vol_default", -1);
        if (defaultMusicVolume != -1 && defaultMusicVolume <= MAX_STREAM_VOLUME[3] && defaultMusicVolume >= MIN_STREAM_VOLUME[3]) {
            AudioSystem.DEFAULT_STREAM_VOLUME[3] = defaultMusicVolume;
        } else if (isPlatformTelevision()) {
            AudioSystem.DEFAULT_STREAM_VOLUME[3] = MAX_STREAM_VOLUME[3] / 4;
        } else {
            AudioSystem.DEFAULT_STREAM_VOLUME[3] = MAX_STREAM_VOLUME[3] / 3;
        }
        int maxAlarmVolume = SystemProperties.getInt("ro.config.alarm_vol_steps", -1);
        if (maxAlarmVolume != -1) {
            MAX_STREAM_VOLUME[4] = maxAlarmVolume;
        }
        int defaultAlarmVolume = SystemProperties.getInt("ro.config.alarm_vol_default", -1);
        if (defaultAlarmVolume == -1 || defaultAlarmVolume > MAX_STREAM_VOLUME[4]) {
            AudioSystem.DEFAULT_STREAM_VOLUME[4] = (MAX_STREAM_VOLUME[4] * 6) / 7;
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
        if (isTv) {
            loadTvVolumeConfig();
        }
        sSoundEffectVolumeDb = context.getResources().getInteger(17694895);
        createAudioSystemThread();
        AudioSystem.setErrorCallback(this.mAudioSystemCallback);
        boolean cameraSoundForced = readCameraSoundForced();
        this.mCameraSoundForced = new Boolean(cameraSoundForced).booleanValue();
        sendMsg(this.mAudioHandler, 8, 2, 4, cameraSoundForced ? 11 : 0, new String("AudioService ctor"), 0);
        this.mSafeMediaVolumeState = Settings.Global.getInt(this.mContentResolver, "audio_safe_volume_state", 0);
        this.mSafeMediaVolumeIndex = this.mContext.getResources().getInteger(17694879) * 10;
        this.mIsChineseZone = SystemProperties.getInt("ro.config.hw_optb", CHINAZONE_IDENTIFIER) == CHINAZONE_IDENTIFIER;
        if (usingHwSafeMediaConfig()) {
            this.mSafeMediaVolumeIndex = getHwSafeMediaVolumeIndex();
            if (isHwSafeMediaVolumeEnabled()) {
                i = new Integer(Settings.Global.getInt(this.mContentResolver, "audio_safe_volume_state", 0)).intValue();
            } else {
                i = 1;
            }
            this.mSafeMediaVolumeState = i;
        }
        this.mUseFixedVolume = this.mContext.getResources().getBoolean(17891561);
        this.mDeviceBroker = new AudioDeviceBroker(this.mContext, this);
        updateStreamVolumeAlias(false, TAG);
        readPersistedSettings();
        readUserRestrictions();
        this.mSettingsObserver = new SettingsObserver();
        createStreamStates();
        this.mSafeUsbMediaVolumeIndex = getSafeUsbMediaVolumeIndex();
        this.mPlaybackMonitor = new PlaybackActivityMonitor(context, MAX_STREAM_VOLUME[4]);
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
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        intentFilter.addAction("android.intent.action.USER_BACKGROUND");
        intentFilter.addAction("android.intent.action.USER_FOREGROUND");
        intentFilter.addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED");
        intentFilter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
        intentFilter.addAction("android.intent.action.PACKAGES_SUSPENDED");
        intentFilter.addAction("android.intent.action.FM");
        intentFilter.addAction("android.intent.action.CONFIGURATION_CHANGED");
        intentFilter.addAction("android.intent.action.USER_PRESENT");
        this.mMonitorRotation = SystemProperties.getBoolean("ro.audio.monitorRotation", false) || SPK_RCV_STEREO_SUPPORT;
        if (this.mMonitorRotation) {
            RotationHelper.init(this.mContext, this.mAudioHandler);
        }
        intentFilter.addAction("android.media.action.OPEN_AUDIO_EFFECT_CONTROL_SESSION");
        intentFilter.addAction("android.media.action.CLOSE_AUDIO_EFFECT_CONTROL_SESSION");
        intentFilter.addAction("android.intent.action.LOCALE_CHANGED");
        if (!this.mIsChineseZone) {
            intentFilter.addAction(ACTION_CHECK_MUSIC_ACTIVE);
        }
        context.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, intentFilter, null, null);
        context.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, new IntentFilter(ACTION_CHANGE_ACTIVE_DEVICE), PERMISSION_CHANGE_ACTIVE_DEVICE, null);
        LocalServices.addService(AudioManagerInternal.class, new AudioServiceInternal());
        this.mUserManagerInternal.addUserRestrictionsListener(this.mUserRestrictionsListener);
        this.mRecordMonitor.initMonitor();
        Intent intentCheckMusicActive = new Intent();
        intentCheckMusicActive.setAction(ACTION_CHECK_MUSIC_ACTIVE);
        this.mPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, intentCheckMusicActive, 268435456);
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        float[] preScale = {this.mContext.getResources().getFraction(18022403, 1, 1), this.mContext.getResources().getFraction(18022404, 1, 1), this.mContext.getResources().getFraction(18022405, 1, 1)};
        for (int i3 = 0; i3 < preScale.length; i3++) {
            if (0.0f <= preScale[i3] && preScale[i3] <= 1.0f) {
                this.mPrescaleAbsoluteVolume[i3] = preScale[i3];
            }
        }
        this.mHwAudioServiceEx = HwServiceExFactory.getHwAudioServiceEx(this, context);
        if (usingHwSafeMediaConfig() && this.mHwAudioServiceEx.isHwSafeUsbMediaVolumeEnabled()) {
            this.mSafeUsbMediaVolumeIndex = this.mHwAudioServiceEx.getHwSafeUsbMediaVolumeIndex();
        }
    }

    public void systemReady() {
        sendMsg(this.mAudioHandler, 16, 2, 0, 0, null, 0);
    }

    public void onSystemReady() {
        this.mSystemReady = true;
        this.mHwAudioServiceEx.setSystemReady();
        scheduleLoadSoundEffects();
        this.mDeviceBroker.onSystemReady();
        int i = 0;
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.hdmi.cec")) {
            synchronized (this.mHdmiClientLock) {
                this.mHdmiManager = (HdmiControlManager) this.mContext.getSystemService(HdmiControlManager.class);
                this.mHdmiTvClient = this.mHdmiManager.getTvClient();
                if (this.mHdmiTvClient != null) {
                    this.mFixedVolumeDevices &= -2883587;
                }
                this.mHdmiPlaybackClient = this.mHdmiManager.getPlaybackClient();
                if (this.mHdmiPlaybackClient != null) {
                    this.mFixedVolumeDevices &= -1025;
                    this.mFullVolumeDevices |= 1024;
                }
                this.mHdmiCecSink = false;
                this.mHdmiAudioSystemClient = this.mHdmiManager.getAudioSystemClient();
            }
        }
        this.mNm = (NotificationManager) this.mContext.getSystemService("notification");
        AudioHandler audioHandler = this.mAudioHandler;
        if (!SystemProperties.getBoolean("audio.safemedia.bypass", false)) {
            i = SAFE_VOLUME_CONFIGURE_TIMEOUT_MS;
        }
        sendMsg(audioHandler, 13, 0, 0, 0, TAG, i);
        initA11yMonitoring();
        this.mRoleObserver = new RoleObserver();
        this.mRoleObserver.register();
        onIndicateSystemReady();
        if (isTv) {
            setTvDefaultVolume();
        }
    }

    /* access modifiers changed from: package-private */
    public class RoleObserver implements OnRoleHoldersChangedListener {
        private final Executor mExecutor;
        private RoleManager mRm;

        RoleObserver() {
            this.mExecutor = AudioService.this.mContext.getMainExecutor();
        }

        public void register() {
            this.mRm = (RoleManager) AudioService.this.mContext.getSystemService("role");
            RoleManager roleManager = this.mRm;
            if (roleManager != null) {
                roleManager.addOnRoleHoldersChangedListenerAsUser(this.mExecutor, this, UserHandle.ALL);
                AudioService.this.updateAssistantUId(true);
            }
        }

        public void onRoleHoldersChanged(String roleName, UserHandle user) {
            if ("android.app.role.ASSISTANT".equals(roleName)) {
                AudioService.this.updateAssistantUId(false);
            }
        }

        public String getAssistantRoleHolder() {
            RoleManager roleManager = this.mRm;
            if (roleManager == null) {
                return "";
            }
            List<String> assistants = roleManager.getRoleHolders("android.app.role.ASSISTANT");
            return assistants.size() == 0 ? "" : assistants.get(0);
        }
    }

    /* access modifiers changed from: package-private */
    public void onIndicateSystemReady() {
        if (AudioSystem.systemReady() != 0) {
            sendMsg(this.mAudioHandler, 20, 0, 0, 0, null, 1000);
        }
    }

    public void onAudioServerDied() {
        int forDock;
        int forSys;
        if (!this.mSystemReady || AudioSystem.checkAudioFlinger() != 0) {
            Log.e(TAG, "Audioserver died.");
            sendMsg(this.mAudioHandler, 4, 1, 0, 0, null, 500);
            return;
        }
        Log.e(TAG, "Audioserver started.");
        AudioSystem.setParameters("restarting=true");
        readAndSetLowRamDevice();
        this.mDeviceBroker.onAudioServerDied();
        this.mHwAudioServiceEx.onRestoreDevices();
        if (AudioSystem.setPhoneState(this.mMode) == 0) {
            this.mModeLogger.log(new AudioEventLogger.StringEvent("onAudioServerDied causes setPhoneState(" + AudioSystem.modeToString(this.mMode) + ")"));
        }
        synchronized (this.mSettingsLock) {
            forDock = 0;
            forSys = this.mCameraSoundForced ? 11 : 0;
        }
        this.mDeviceBroker.setForceUse_Async(4, forSys, "onAudioServerDied");
        sendCommForceBroadcast();
        updateAftPolicy();
        this.mHwAudioServiceEx.setKaraokeWhiteListUID();
        for (int streamType = AudioSystem.getNumStreamTypes() - 1; streamType >= 0; streamType--) {
            VolumeStreamState streamState = this.mStreamStates[streamType];
            AudioSystem.initStreamVolume(streamType, streamState.mIndexMin / 10, streamState.mIndexMax / 10);
            streamState.applyAllVolumes();
        }
        updateMasterMono(this.mContentResolver);
        updateMasterBalance(this.mContentResolver);
        setRingerModeInt(getRingerModeInternal(), false);
        this.mHwAudioServiceEx.processAudioServerRestart();
        processMediaServerRestart();
        if (this.mMonitorRotation) {
            RotationHelper.updateOrientation();
        }
        synchronized (this.mSettingsLock) {
            if (this.mDockAudioMediaEnabled) {
                forDock = 8;
            }
            this.mDeviceBroker.setForceUse_Async(3, forDock, "onAudioServerDied");
            sendEncodedSurroundMode(this.mContentResolver, "onAudioServerDied");
            sendEnabledSurroundFormats(this.mContentResolver, true);
            updateAssistantUId(true);
            updateRttEanbled(this.mContentResolver);
        }
        synchronized (this.mAccessibilityServiceUidsLock) {
            AudioSystem.setA11yServicesUids(this.mAccessibilityServiceUids);
        }
        synchronized (this.mHdmiClientLock) {
            if (!(this.mHdmiManager == null || this.mHdmiTvClient == null)) {
                setHdmiSystemAudioSupported(this.mHdmiSystemAudioSupported);
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
        if (IS_SUPER_RECEIVER_ENABLED) {
            sendMsg(this.mAudioHandler, 10099, 0, 0, 0, null, 0);
        }
        sendMsg(this.mAudioHandler, MSG_DISPATCH_AUDIO_SERVER_STATE, 2, 1, 0, null, 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onDispatchAudioServerStateChange(boolean state) {
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

    public List<AudioProductStrategy> getAudioProductStrategies() {
        return AudioProductStrategy.getAudioProductStrategies();
    }

    public List<AudioVolumeGroup> getAudioVolumeGroups() {
        return AudioVolumeGroup.getAudioVolumeGroups();
    }

    private void checkAllAliasStreamVolumes() {
        boolean isAvrcpAbsVolSupported = this.mDeviceBroker.isAvrcpAbsoluteVolumeSupported();
        synchronized (this.mSettingsLock) {
            synchronized (VolumeStreamState.class) {
                int numStreamTypes = AudioSystem.getNumStreamTypes();
                for (int streamType = 0; streamType < numStreamTypes; streamType++) {
                    this.mStreamStates[streamType].setAllIndexes(this.mStreamStates[mStreamVolumeAlias[streamType]], TAG);
                    if (!this.mStreamStates[streamType].mIsMuted) {
                        this.mStreamStates[streamType].applyAllVolumes(isAvrcpAbsVolSupported);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void postCheckVolumeCecOnHdmiConnection(int state, String caller) {
        sendMsg(this.mAudioHandler, MSG_HDMI_VOLUME_CHECK, 0, state, 0, caller, 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onCheckVolumeCecOnHdmiConnection(int state, String caller) {
        if (state == 1) {
            if (isPlatformTelevision()) {
                checkAddAllFixedVolumeDevices(1024, caller);
                synchronized (this.mHdmiClientLock) {
                    if (!(this.mHdmiManager == null || this.mHdmiPlaybackClient == null)) {
                        this.mHdmiCecSink = false;
                        this.mHdmiPlaybackClient.queryDisplayStatus(this.mHdmiDisplayStatusCallback);
                    }
                }
            }
            sendEnabledSurroundFormats(this.mContentResolver, true);
        } else if (isPlatformTelevision()) {
            synchronized (this.mHdmiClientLock) {
                if (this.mHdmiManager != null) {
                    this.mHdmiCecSink = false;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkAddAllFixedVolumeDevices(int device, String caller) {
        int numStreamTypes = AudioSystem.getNumStreamTypes();
        for (int streamType = 0; streamType < numStreamTypes; streamType++) {
            if (!this.mStreamStates[streamType].hasIndexForDevice(device)) {
                VolumeStreamState[] volumeStreamStateArr = this.mStreamStates;
                volumeStreamStateArr[streamType].setIndex(volumeStreamStateArr[mStreamVolumeAlias[streamType]].getIndex(1073741824), device, caller);
            }
            this.mStreamStates[streamType].checkFixedVolumeDevices();
        }
    }

    private void checkAllFixedVolumeDevices() {
        int numStreamTypes = AudioSystem.getNumStreamTypes();
        for (int streamType = 0; streamType < numStreamTypes; streamType++) {
            this.mStreamStates[streamType].checkFixedVolumeDevices();
        }
    }

    private void checkAllFixedVolumeDevices(int streamType) {
        this.mStreamStates[streamType].checkFixedVolumeDevices();
    }

    private void checkMuteAffectedStreams() {
        int i = 0;
        while (true) {
            VolumeStreamState[] volumeStreamStateArr = this.mStreamStates;
            if (i < volumeStreamStateArr.length) {
                VolumeStreamState vss = volumeStreamStateArr[i];
                if (!(vss.mIndexMin <= 0 || vss.mStreamType == 0 || vss.mStreamType == 6)) {
                    this.mMuteAffectedStreams &= ~(1 << vss.mStreamType);
                }
                i++;
            } else {
                return;
            }
        }
    }

    private void updateMutedStreams() {
        int i = 0;
        while (true) {
            VolumeStreamState[] volumeStreamStateArr = this.mStreamStates;
            if (i < volumeStreamStateArr.length) {
                VolumeStreamState vss = volumeStreamStateArr[i];
                if ((this.mMutedStreams & (1 << vss.mStreamType)) != 0) {
                    vss.mute(true);
                }
                i++;
            } else {
                return;
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
        if (isTv) {
            updateMutedStreams();
        }
    }

    private void loadTvVolumeConfig() {
        ContentResolver contentResolver = this.mContentResolver;
        if (contentResolver != null && this.mContext != null) {
            String minVolumeString = Settings.System.getString(contentResolver, VOLUME_MIN_NAME);
            if (!TextUtils.isEmpty(minVolumeString)) {
                try {
                    int minCustomVolume = Integer.parseInt(minVolumeString);
                    if (minCustomVolume < 0 || minCustomVolume > 100) {
                        MIN_STREAM_VOLUME[3] = 0;
                    } else {
                        MIN_STREAM_VOLUME[3] = minCustomVolume;
                    }
                } catch (NumberFormatException e) {
                    MIN_STREAM_VOLUME[3] = 0;
                }
            }
            String maxVolumeString = Settings.System.getString(this.mContentResolver, VOLUME_MAX_NAME);
            if (!TextUtils.isEmpty(maxVolumeString)) {
                try {
                    int maxCustomVolume = Integer.parseInt(maxVolumeString);
                    if (maxCustomVolume < 0 || maxCustomVolume > 100) {
                        MAX_STREAM_VOLUME[3] = 100;
                    } else {
                        MAX_STREAM_VOLUME[3] = maxCustomVolume;
                    }
                } catch (NumberFormatException e2) {
                    MAX_STREAM_VOLUME[3] = 100;
                }
            }
            int[] iArr = MAX_STREAM_VOLUME;
            if (iArr[3] < MIN_STREAM_VOLUME[3]) {
                iArr[3] = 100;
            }
            String defaultVolumeString = Settings.System.getString(this.mContentResolver, VOLUME_DEFAULT_NAME);
            if (!TextUtils.isEmpty(defaultVolumeString)) {
                try {
                    AudioSystem.DEFAULT_STREAM_VOLUME[3] = Integer.parseInt(defaultVolumeString);
                } catch (NumberFormatException e3) {
                    AudioSystem.DEFAULT_STREAM_VOLUME[3] = MIN_STREAM_VOLUME[3];
                }
            }
            int tempDefaultVolume = AudioSystem.DEFAULT_STREAM_VOLUME[3];
            int tempMinVolume = MIN_STREAM_VOLUME[3];
            int tempMaxVolume = MAX_STREAM_VOLUME[3];
            if (tempDefaultVolume < tempMinVolume || tempDefaultVolume > tempMaxVolume) {
                AudioSystem.DEFAULT_STREAM_VOLUME[3] = MIN_STREAM_VOLUME[3];
            }
        }
    }

    private void setTvDefaultVolume() {
        ContentResolver contentResolver;
        VolumeStreamState musicStreamState;
        if (this.mContext != null && (contentResolver = this.mContentResolver) != null && SHOULD_RECOVER_DEFAULT_VOLUME.equals(Settings.System.getString(contentResolver, VOLUME_RECOVERY_NAME)) && (musicStreamState = this.mStreamStates[3]) != null) {
            int device = getDeviceForStream(3);
            musicStreamState.setIndex(AudioSystem.DEFAULT_STREAM_VOLUME[3] * 10, device, this.mContext.getPackageName());
            sendMsg(this.mAudioHandler, 0, 2, device, 0, musicStreamState, 0);
        }
    }

    private void updateDefaultVolumes() {
        for (int stream = 0; stream < this.mStreamStates.length; stream++) {
            if (stream != mStreamVolumeAlias[stream]) {
                int[] iArr = AudioSystem.DEFAULT_STREAM_VOLUME;
                int[] iArr2 = AudioSystem.DEFAULT_STREAM_VOLUME;
                int[] iArr3 = mStreamVolumeAlias;
                iArr[stream] = (rescaleIndex(iArr2[iArr3[stream]] * 10, iArr3[stream], stream) + 5) / 10;
            }
        }
    }

    private void dumpStreamStates(PrintWriter pw) {
        pw.println("\nStream volumes (device: index)");
        int numStreamTypes = AudioSystem.getNumStreamTypes();
        for (int i = 0; i < numStreamTypes; i++) {
            pw.println("- " + AudioSystem.STREAM_NAMES[i] + ":");
            this.mStreamStates[i].dump(pw);
            pw.println("");
        }
        pw.print("\n- mute affected streams = 0x");
        pw.println(Integer.toHexString(this.mMuteAffectedStreams));
        pw.print("\n- mute streams = 0x");
        pw.println(Integer.toHexString(this.mMutedStreams));
    }

    private void updateStreamVolumeAlias(boolean updateVolumes, String caller) {
        int dtmfStreamAlias;
        int dtmfStreamAlias2;
        int a11yStreamAlias = sIndependentA11yVolume ? 10 : 3;
        if (this.mIsSingleVolume) {
            mStreamVolumeAlias = this.STREAM_VOLUME_ALIAS_TELEVISION;
            dtmfStreamAlias = 3;
        } else if (this.mPlatformType != 1) {
            mStreamVolumeAlias = this.STREAM_VOLUME_ALIAS_DEFAULT;
            dtmfStreamAlias = 3;
        } else if (isTv) {
            mStreamVolumeAlias = this.STREAM_VOLUME_ALIAS_TV;
            dtmfStreamAlias = 3;
        } else {
            mStreamVolumeAlias = this.STREAM_VOLUME_ALIAS_VOICE;
            dtmfStreamAlias = 2;
        }
        if (this.mIsSingleVolume) {
            this.mRingerModeAffectedStreams = 0;
        } else if (isInCommunication()) {
            this.mRingerModeAffectedStreams &= -257;
            dtmfStreamAlias2 = 0;
            int[] iArr = mStreamVolumeAlias;
            iArr[8] = dtmfStreamAlias2;
            iArr[10] = a11yStreamAlias;
            if (updateVolumes && this.mStreamStates != null) {
                updateDefaultVolumes();
                synchronized (this.mSettingsLock) {
                    synchronized (VolumeStreamState.class) {
                        AudioSystem.initStreamVolume(mStreamVolumeAlias[8], this.mStreamStates[mStreamVolumeAlias[8]].mIndexMin / 10, this.mStreamStates[mStreamVolumeAlias[8]].mIndexMax / 10);
                        this.mStreamStates[8].setAllIndexes(this.mStreamStates[dtmfStreamAlias2], caller);
                        this.mStreamStates[10].mVolumeIndexSettingName = Settings.System.VOLUME_SETTINGS_INT[a11yStreamAlias];
                        this.mStreamStates[10].setAllIndexes(this.mStreamStates[a11yStreamAlias], caller);
                    }
                }
                if (sIndependentA11yVolume) {
                    this.mStreamStates[10].readSettings();
                }
                setRingerModeInt(getRingerModeInternal(), false);
                sendMsg(this.mAudioHandler, 10, 2, 0, 0, this.mStreamStates[8], 0);
                sendMsg(this.mAudioHandler, 10, 2, 0, 0, this.mStreamStates[10], 0);
                return;
            }
        } else {
            this.mRingerModeAffectedStreams |= 256;
        }
        dtmfStreamAlias2 = dtmfStreamAlias;
        int[] iArr2 = mStreamVolumeAlias;
        iArr2[8] = dtmfStreamAlias2;
        iArr2[10] = a11yStreamAlias;
        if (updateVolumes) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void readDockAudioSettings(ContentResolver cr) {
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
    /* access modifiers changed from: public */
    private void updateMasterMono(ContentResolver cr) {
        boolean masterMono = Settings.System.getIntForUser(cr, "master_mono", 0, -2) == 1;
        Log.i(TAG, String.format(Locale.ROOT, "Master mono %b", Boolean.valueOf(masterMono)));
        AudioSystem.setMasterMono(masterMono);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateMasterBalance(ContentResolver cr) {
        float masterBalance = Settings.System.getFloatForUser(cr, "master_balance", 0.0f, -2);
        Log.i(TAG, String.format(Locale.ROOT, "Master balance %f", Float.valueOf(masterBalance)));
        if (AudioSystem.setMasterBalance(masterBalance) != 0) {
            Log.e(TAG, String.format("setMasterBalance failed for %f", Float.valueOf(masterBalance)));
        }
    }

    private void sendEncodedSurroundMode(ContentResolver cr, String eventSource) {
        sendEncodedSurroundMode(Settings.Global.getInt(cr, "encoded_surround_output", 0), eventSource);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendEncodedSurroundMode(int encodedSurroundMode, String eventSource) {
        int forceSetting = MSG_OBSERVE_DEVICES_FOR_ALL_STREAMS;
        if (encodedSurroundMode == 0) {
            forceSetting = 0;
        } else if (encodedSurroundMode == 1) {
            forceSetting = 13;
        } else if (encodedSurroundMode == 2) {
            forceSetting = 14;
        } else if (encodedSurroundMode != 3) {
            Log.e(TAG, "updateSurroundSoundSettings: illegal value " + encodedSurroundMode);
        } else {
            forceSetting = 15;
        }
        if (forceSetting != MSG_OBSERVE_DEVICES_FOR_ALL_STREAMS) {
            this.mDeviceBroker.setForceUse_Async(6, forceSetting, eventSource);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendEnabledSurroundFormats(ContentResolver cr, boolean forceUpdate) {
        String enabledSurroundFormats;
        if (this.mEncodedSurroundMode == 3) {
            String enabledSurroundFormats2 = Settings.Global.getString(cr, "encoded_surround_output_enabled_formats");
            if (enabledSurroundFormats2 == null) {
                enabledSurroundFormats = "";
            } else {
                enabledSurroundFormats = enabledSurroundFormats2;
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
                sendMsg(this.mAudioHandler, MSG_ENABLE_SURROUND_FORMATS, 2, 0, 0, formats, 0);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onEnableSurroundFormats(ArrayList<Integer> enabledSurroundFormats) {
        int[] iArr = AudioFormat.SURROUND_SOUND_ENCODING;
        for (int surroundFormat : iArr) {
            boolean enabled = enabledSurroundFormats.contains(Integer.valueOf(surroundFormat));
            Log.i(TAG, "enable surround format:" + surroundFormat + " " + enabled + " " + AudioSystem.setSurroundFormatEnabled(surroundFormat, enabled));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mSettingsLock"})
    private void updateAssistantUId(boolean forceUpdate) {
        int assistantUid = 0;
        String packageName = "";
        RoleObserver roleObserver = this.mRoleObserver;
        if (roleObserver != null) {
            packageName = roleObserver.getAssistantRoleHolder();
        }
        if (TextUtils.isEmpty(packageName)) {
            String assistantName = Settings.Secure.getStringForUser(this.mContentResolver, "voice_interaction_service", -2);
            if (TextUtils.isEmpty(assistantName)) {
                assistantName = Settings.Secure.getStringForUser(this.mContentResolver, "assistant", -2);
            }
            if (!TextUtils.isEmpty(assistantName)) {
                ComponentName componentName = ComponentName.unflattenFromString(assistantName);
                if (componentName == null) {
                    Slog.w(TAG, "Invalid service name for voice_interaction_service: " + assistantName);
                    return;
                }
                packageName = componentName.getPackageName();
            }
        }
        if (!TextUtils.isEmpty(packageName)) {
            PackageManager pm = this.mContext.getPackageManager();
            if (pm.checkPermission("android.permission.CAPTURE_AUDIO_HOTWORD", packageName) == 0) {
                try {
                    assistantUid = pm.getPackageUid(packageName, 0);
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e(TAG, "updateAssistantUId() could not find UID for package: " + packageName);
                }
            }
        }
        if (assistantUid != this.mAssistantUid || forceUpdate) {
            AudioSystem.setAssistantUid(assistantUid);
            this.mAssistantUid = assistantUid;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateRttEanbled(ContentResolver cr) {
        boolean rttEnabled = false;
        if (Settings.Secure.getIntForUser(cr, "rtt_calling_mode", 0, -2) != 0) {
            rttEnabled = true;
        }
        AudioSystem.setRttEnabled(rttEnabled);
    }

    private void readPersistedSettings() {
        int i;
        ContentResolver cr = this.mContentResolver;
        int i2 = 2;
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
            if (this.mHasVibrator) {
                i = 2;
            } else {
                i = 0;
            }
            this.mVibrateSetting = AudioSystem.getValueForVibrateSetting(0, 1, i);
            int i3 = this.mVibrateSetting;
            if (!this.mHasVibrator) {
                i2 = 0;
            }
            this.mVibrateSetting = AudioSystem.getValueForVibrateSetting(i3, 0, i2);
            updateRingerAndZenModeAffectedStreams();
            readDockAudioSettings(cr);
            sendEncodedSurroundMode(cr, "readPersistedSettings");
            sendEnabledSurroundFormats(cr, true);
            updateAssistantUId(true);
            updateRttEanbled(cr);
        }
        this.mMuteAffectedStreams = Settings.System.getIntForUser(cr, "mute_streams_affected", IHwLbsLogger.LOCATION_POS_REPORT, -2);
        this.mMutedStreams = Settings.System.getIntForUser(cr, "muted_streams", 0, -2);
        updateMasterMono(cr);
        updateMasterBalance(cr);
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
        Log.i(TAG, String.format(Locale.ROOT, "Master mute %s, user=%d", Boolean.valueOf(masterMute), Integer.valueOf(currentUser)));
        setSystemAudioMute(masterMute);
        AudioSystem.setMasterMute(masterMute);
        broadcastMasterMuteStatus(masterMute);
        boolean microphoneMute = this.mUserManagerInternal.getUserRestriction(currentUser, "no_unmute_microphone");
        Log.i(TAG, String.format(Locale.ROOT, "Mic mute %s, user=%d", Boolean.valueOf(microphoneMute), Integer.valueOf(currentUser)));
        AudioSystem.muteMicrophone(microphoneMute);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int rescaleIndex(int index, int srcStream, int dstStream) {
        int srcRange = this.mStreamStates[srcStream].getMaxIndex() - this.mStreamStates[srcStream].getMinIndex();
        int dstRange = this.mStreamStates[dstStream].getMaxIndex() - this.mStreamStates[dstStream].getMinIndex();
        if (srcRange == 0) {
            Log.e(TAG, "rescaleIndex : index range should not be zero");
            return this.mStreamStates[dstStream].getMinIndex();
        } else if (isTv) {
            return ((index * 1000) + (1000 / 2)) / 1000;
        } else {
            return this.mStreamStates[dstStream].getMinIndex() + ((((index - this.mStreamStates[srcStream].getMinIndex()) * dstRange) + (srcRange / 2)) / srcRange);
        }
    }

    public void adjustSuggestedStreamVolume(int direction, int suggestedStreamType, int flags, String callingPackage, String caller) {
        IAudioPolicyCallback extVolCtlr;
        IHwAudioServiceEx iHwAudioServiceEx = this.mHwAudioServiceEx;
        if (iHwAudioServiceEx != null) {
            if (iHwAudioServiceEx.bypassVolumeProcessForTV(this.mActiveDevice, suggestedStreamType, direction, flags)) {
                return;
            }
        }
        synchronized (this.mExtVolumeControllerLock) {
            extVolCtlr = this.mExtVolumeController;
        }
        if (extVolCtlr != null) {
            sendMsg(this.mAudioHandler, 22, 2, direction, 0, extVolCtlr, 0);
        } else {
            adjustSuggestedStreamVolume(direction, suggestedStreamType, flags, callingPackage, caller, Binder.getCallingUid());
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x00d5  */
    public void adjustSuggestedStreamVolume(int direction, int suggestedStreamType, int flags, String callingPackage, String caller, int uid) {
        int flags2;
        int flags3;
        int streamType;
        boolean activeForReal;
        int flags4 = flags;
        Log.i(TAG, "adjustSuggestedStreamVolume() stream=" + suggestedStreamType + ", flags=" + flags4 + ", caller=" + caller + ", volControlStream=" + this.mVolumeControlStream + ", userSelect=" + this.mUserSelectedVolumeControlStream + ", direction =" + direction);
        if (callingPackage == null) {
            Log.e(TAG, "callingPackage is null");
            return;
        }
        Log.e(TAG, "adjustSuggestedStreamVolume mActiveDevice " + this.mActiveDevice);
        IHwAudioServiceEx iHwAudioServiceEx = this.mHwAudioServiceEx;
        if (iHwAudioServiceEx == null || !iHwAudioServiceEx.bypassVolumeProcessForTV(this.mActiveDevice, suggestedStreamType, direction, flags4)) {
            if (direction != 0) {
                sVolumeLogger.log(new AudioServiceEvents.VolumeEvent(0, suggestedStreamType, direction, flags, callingPackage + SliceClientPermissions.SliceAuthority.DELIMITER + caller + " uid:" + uid));
            }
            if (isTv) {
                streamType = 3;
                flags2 = flags4;
                flags3 = direction;
            } else {
                synchronized (this.mForceControlStreamLock) {
                    if (this.mUserSelectedVolumeControlStream) {
                        streamType = this.mVolumeControlStream;
                    } else {
                        streamType = getActiveStreamType(suggestedStreamType);
                        if (streamType != 2) {
                            if (streamType != 5) {
                                activeForReal = AudioSystem.isStreamActive(streamType, 0);
                                if (!activeForReal) {
                                    if (this.mVolumeControlStream != -1) {
                                        streamType = this.mVolumeControlStream;
                                    }
                                }
                            }
                        }
                        activeForReal = wasStreamActiveRecently(streamType, 0);
                        if (!activeForReal) {
                        }
                    }
                }
                boolean isMute = isMuteAdjust(direction);
                ensureValidStreamType(streamType);
                int resolvedStream = mStreamVolumeAlias[streamType];
                if (!((flags4 & 4) == 0 || resolvedStream == 2)) {
                    flags4 &= -5;
                }
                if (!this.mVolumeController.suppressAdjustment(resolvedStream, flags4, isMute) || this.mIsSingleVolume) {
                    flags2 = flags4;
                    flags3 = direction;
                } else {
                    Log.i(TAG, "Volume controller suppressed adjustment");
                    flags3 = 0;
                    flags2 = flags4 & -5 & -17;
                }
            }
            if (shouldStopAdjustVolume(uid)) {
                adjustStreamVolume(streamType, 0, flags2, callingPackage, caller, uid);
            } else {
                adjustStreamVolume(streamType, flags3, flags2, callingPackage, caller, uid);
            }
        }
    }

    public void adjustStreamVolume(int streamType, int direction, int flags, String callingPackage) {
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.AUDIO_ADJUSTSTREAMVOLUME, new Object[0]);
        IHwAudioServiceEx iHwAudioServiceEx = this.mHwAudioServiceEx;
        if (iHwAudioServiceEx != null) {
            if (iHwAudioServiceEx.bypassVolumeProcessForTV(this.mActiveDevice, streamType, direction, flags)) {
                return;
            }
        }
        if (streamType != 10 || canChangeAccessibilityVolume()) {
            sVolumeLogger.log(new AudioServiceEvents.VolumeEvent(1, streamType, direction, flags, callingPackage));
            if (shouldStopAdjustVolume(Binder.getCallingUid())) {
                adjustStreamVolume(streamType, 0, flags, callingPackage, callingPackage, Binder.getCallingUid());
            } else {
                adjustStreamVolume(streamType, direction, flags, callingPackage, callingPackage, Binder.getCallingUid());
            }
        } else {
            Log.w(TAG, "Trying to call adjustStreamVolume() for a11y withoutCHANGE_ACCESSIBILITY_VOLUME / callingPackage=" + callingPackage);
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:258:0x052d, code lost:
        r0 = th;
     */
    public void adjustStreamVolume(int streamType, int direction, int flags, String callingPackage, String caller, int uid) {
        int i;
        int uid2;
        int aliasIndex;
        int step;
        VolumeStreamState streamState;
        int uid3;
        int uid4;
        boolean isMuteAdjust;
        int device;
        String str;
        int flags2;
        int streamTypeAlias;
        int step2;
        VolumeStreamState streamState2;
        int step3;
        boolean isMuteAdjust2;
        int i2;
        int streamTypeAlias2;
        boolean isMuteAdjust3;
        int device2;
        int device3;
        boolean state;
        int flags3;
        int flags4;
        Object obj;
        if (checkEnbaleVolumeAdjust() && !this.mUseFixedVolume) {
            Log.i(TAG, "adjustStreamVolume() stream=" + streamType + ", dir=" + direction + ", flags=" + flags + ", caller=" + caller);
            ensureValidDirection(direction);
            ensureValidStreamType(streamType);
            boolean isMuteAdjust4 = isMuteAdjust(direction);
            if (isMuteAdjust4 && !isStreamAffectedByMute(streamType)) {
                return;
            }
            if (!isMuteAdjust4 || (!(streamType == 0 || streamType == 6) || this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE") == 0)) {
                int streamTypeAlias3 = mStreamVolumeAlias[streamType];
                VolumeStreamState streamState3 = this.mStreamStates[streamTypeAlias3];
                int device4 = getDeviceForStream(streamTypeAlias3);
                int aliasIndex2 = streamState3.getIndex(device4);
                boolean adjustVolume = true;
                if ((device4 & 896) != 0 || (flags & 64) == 0) {
                    if ((device4 & 896) != 0) {
                        int indexV = this.mStreamStates[streamType].getIndex(device4);
                        AudioSystem.setParameters("parentcontrol_volume_index=" + (indexV / 10));
                    }
                    if (!isTv) {
                        i = 3;
                    } else if (streamTypeAlias3 == 3 && (device4 & 896) != 0 && (flags & 64) == 0 && this.mDeviceBroker.isAvrcpAbsoluteVolumeSupported() && (direction == -1 || direction == 1)) {
                        Object obj2 = this.mAbsMediaVolumeStateLock;
                        synchronized (obj2) {
                            try {
                                Log.i(TAG, "adjustStreamVolume , mAbsVolumeState=" + this.mAbsVolumeState);
                                this.mAbsVolumeState = 1;
                                obj = obj2;
                                i = 3;
                                sendMsg(this.mAudioHandler, 30, 0, 0, 0, caller, ABS_MEDIA_VOLUME_TIME_OUT_MS);
                            } catch (Throwable th) {
                                th = th;
                                throw th;
                            }
                        }
                    } else {
                        i = 3;
                    }
                    if (uid == 1000) {
                        uid2 = UserHandle.getUid(getCurrentUserId(), UserHandle.getAppId(uid));
                    } else {
                        uid2 = uid;
                    }
                    if (this.mAppOps.noteOp(STREAM_VOLUME_OPS[streamTypeAlias3], uid2, callingPackage) == 0) {
                        synchronized (this.mSafeMediaVolumeStateLock) {
                            this.mPendingVolumeCommand = null;
                        }
                        int flags5 = flags & -33;
                        if (streamTypeAlias3 != i || (this.mFixedVolumeDevices & device4) == 0) {
                            step = rescaleIndex(10, streamType, streamTypeAlias3);
                            aliasIndex = aliasIndex2;
                        } else {
                            flags5 |= 32;
                            if (this.mSafeMediaVolumeState != i || (603979788 & device4) == 0) {
                                step = streamState3.getMaxIndex();
                            } else {
                                step = safeMediaVolumeIndex(device4);
                            }
                            if (aliasIndex2 != 0) {
                                aliasIndex = step;
                            } else {
                                aliasIndex = aliasIndex2;
                            }
                        }
                        int step4 = (!LOUD_VOICE_MODE_SUPPORT || streamType != 0 || !SHOULD_RECOVER_DEFAULT_VOLUME.equals(AudioSystem.getParameters("VOICE_LVM_Enable"))) ? step : 0;
                        if ((flags5 & 2) != 0 || streamTypeAlias3 == getUiSoundsStreamType()) {
                            if (getRingerModeInternal() == 1) {
                                flags3 = flags5 & -17;
                            } else {
                                flags3 = flags5;
                            }
                            device = device4;
                            streamState = streamState3;
                            uid3 = uid2;
                            uid4 = streamTypeAlias3;
                            isMuteAdjust = isMuteAdjust4;
                            str = caller;
                            int result = checkForRingerModeChange(aliasIndex, direction, step4, streamState3.mIsMuted, callingPackage, flags3);
                            adjustVolume = (result & 1) != 0;
                            if ((result & 128) != 0) {
                                flags4 = flags3 | 128;
                            } else {
                                flags4 = flags3;
                            }
                            if ((result & 2048) != 0) {
                                flags2 = flags4 | 2048;
                            } else {
                                flags2 = flags4;
                            }
                        } else {
                            device = device4;
                            streamState = streamState3;
                            isMuteAdjust = isMuteAdjust4;
                            str = caller;
                            uid3 = uid2;
                            flags2 = flags5;
                            uid4 = streamTypeAlias3;
                        }
                        if (!volumeAdjustmentAllowedByDnd(uid4, flags2)) {
                            adjustVolume = false;
                        }
                        int oldIndex = this.mStreamStates[streamType].getIndex(device);
                        if (!adjustVolume || direction == 0) {
                            step3 = step4;
                            step2 = device;
                            streamState2 = streamState;
                            streamTypeAlias = uid4;
                        } else {
                            this.mAudioHandler.removeMessages(18);
                            if (isMuteAdjust) {
                                if (direction == 101) {
                                    state = !streamState.mIsMuted;
                                } else {
                                    state = direction == -100;
                                }
                                if (uid4 == i) {
                                    setSystemAudioMute(state);
                                }
                                for (int stream = 0; stream < this.mStreamStates.length; stream++) {
                                    if (uid4 == mStreamVolumeAlias[stream] && (!readCameraSoundForced() || this.mStreamStates[stream].getStreamType() != 7)) {
                                        this.mStreamStates[stream].mute(state);
                                    }
                                }
                                step3 = step4;
                                step2 = device;
                                isMuteAdjust2 = isMuteAdjust;
                                streamTypeAlias2 = uid4;
                                streamState2 = streamState;
                                i2 = -1;
                            } else if (direction != 1 || checkSafeMediaVolume(uid4, aliasIndex + step4, device)) {
                                if ((this.mFullVolumeDevices & device) == 0) {
                                    streamState2 = streamState;
                                    if (streamState2.adjustIndex(direction * step4, device, str) || streamState2.mIsMuted) {
                                        if (!streamState2.mIsMuted) {
                                            step3 = step4;
                                            device3 = device;
                                            isMuteAdjust2 = isMuteAdjust;
                                            streamTypeAlias2 = uid4;
                                            i2 = -1;
                                        } else if (direction == 1) {
                                            streamState2.mute(false);
                                            step3 = step4;
                                            device3 = device;
                                            isMuteAdjust2 = isMuteAdjust;
                                            streamTypeAlias2 = uid4;
                                            i2 = -1;
                                        } else {
                                            i2 = -1;
                                            if (direction != -1) {
                                                step3 = step4;
                                                device3 = device;
                                                isMuteAdjust2 = isMuteAdjust;
                                                streamTypeAlias2 = uid4;
                                            } else if (this.mIsSingleVolume) {
                                                step3 = step4;
                                                device3 = device;
                                                isMuteAdjust2 = isMuteAdjust;
                                                streamTypeAlias2 = uid4;
                                                sendMsg(this.mAudioHandler, 18, 2, uid4, flags2, null, UNMUTE_STREAM_DELAY);
                                            } else {
                                                step3 = step4;
                                                device3 = device;
                                                isMuteAdjust2 = isMuteAdjust;
                                                streamTypeAlias2 = uid4;
                                            }
                                        }
                                        sendMsg(this.mAudioHandler, 0, 2, device3, 0, streamState2, 0);
                                        step2 = device3;
                                    } else {
                                        step3 = step4;
                                        device2 = device;
                                        isMuteAdjust2 = isMuteAdjust;
                                        streamTypeAlias2 = uid4;
                                        i2 = -1;
                                    }
                                } else {
                                    step3 = step4;
                                    device2 = device;
                                    isMuteAdjust2 = isMuteAdjust;
                                    streamTypeAlias2 = uid4;
                                    streamState2 = streamState;
                                    i2 = -1;
                                }
                                if (isTv) {
                                    step2 = device2;
                                    if ((step2 & 896) != 0 && streamType == i) {
                                        Log.i(TAG, "adjustStreamVolume: otherwise oldindex = " + oldIndex + "step = " + step3 + ", show volumeUI too");
                                        streamState2.mVolumeChanged.putExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", oldIndex / step3);
                                        streamState2.mVolumeChanged.putExtra("android.media.EXTRA_PREV_VOLUME_STREAM_VALUE", oldIndex / step3);
                                        streamState2.mVolumeChanged.putExtra("android.media.EXTRA_VOLUME_STREAM_TYPE_ALIAS", mStreamVolumeAlias[streamType]);
                                        sendBroadcastToAll(streamState2.mVolumeChanged);
                                    }
                                } else {
                                    step2 = device2;
                                }
                            } else {
                                Log.e(TAG, "adjustStreamVolume() safe volume index = " + oldIndex);
                                this.mVolumeController.postDisplaySafeVolumeWarning(flags2);
                                step3 = step4;
                                step2 = device;
                                isMuteAdjust2 = isMuteAdjust;
                                streamTypeAlias2 = uid4;
                                streamState2 = streamState;
                                i2 = -1;
                            }
                            int newIndex = this.mStreamStates[streamType].getIndex(step2);
                            if (streamTypeAlias2 == i && (step2 & 896) != 0 && (flags2 & 64) == 0) {
                                Log.i(TAG, "adjustStreamVolume: postSetAvrcpAbsoluteVolumeIndex index=" + newIndex + "stream=" + streamType);
                                this.mDeviceBroker.postSetAvrcpAbsoluteVolumeIndex(newIndex / 10);
                            }
                            if ((134217728 & step2) != 0 && streamType == getHearingAidStreamType()) {
                                Log.i(TAG, "adjustSreamVolume postSetHearingAidVolumeIndex index=" + newIndex + " stream=" + streamType);
                                this.mDeviceBroker.postSetHearingAidVolumeIndex(newIndex, streamType);
                            }
                            if (streamTypeAlias2 == i) {
                                setSystemAudioVolume(oldIndex, newIndex, getStreamMaxVolume(streamType), flags2);
                            }
                            synchronized (this.mHdmiClientLock) {
                                try {
                                    if (this.mHdmiManager != null) {
                                        if (this.mHdmiCecSink && streamTypeAlias2 == i) {
                                            try {
                                                if ((this.mFullVolumeDevices & step2) != 0) {
                                                    int keyCode = 0;
                                                    if (direction == i2) {
                                                        keyCode = MSG_UPDATE_RINGER_MODE;
                                                    } else if (direction == 1) {
                                                        keyCode = MSG_ENABLE_SURROUND_FORMATS;
                                                    } else if (direction == 101) {
                                                        keyCode = 164;
                                                    }
                                                    if (keyCode != 0) {
                                                        long ident = Binder.clearCallingIdentity();
                                                        try {
                                                            this.mHdmiPlaybackClient.sendKeyEvent(keyCode, true);
                                                            this.mHdmiPlaybackClient.sendKeyEvent(keyCode, false);
                                                        } finally {
                                                            Binder.restoreCallingIdentity(ident);
                                                        }
                                                    }
                                                }
                                            } catch (Throwable th2) {
                                                th = th2;
                                                throw th;
                                            }
                                        }
                                        if (this.mHdmiAudioSystemClient == null || !this.mHdmiSystemAudioSupported || streamTypeAlias2 != i) {
                                            streamTypeAlias = streamTypeAlias2;
                                        } else {
                                            if (oldIndex == newIndex) {
                                                isMuteAdjust3 = isMuteAdjust2;
                                                if (!isMuteAdjust3) {
                                                    streamTypeAlias = streamTypeAlias2;
                                                }
                                            } else {
                                                isMuteAdjust3 = isMuteAdjust2;
                                            }
                                            try {
                                                long identity = Binder.clearCallingIdentity();
                                                streamTypeAlias = streamTypeAlias2;
                                                this.mHdmiAudioSystemClient.sendReportAudioStatusCecCommand(isMuteAdjust3, getStreamVolume(i), getStreamMaxVolume(i), isStreamMute(i));
                                                Binder.restoreCallingIdentity(identity);
                                            } catch (Throwable th3) {
                                                th = th3;
                                                throw th;
                                            }
                                        }
                                    } else {
                                        streamTypeAlias = streamTypeAlias2;
                                    }
                                } catch (Throwable th4) {
                                    th = th4;
                                    throw th;
                                }
                            }
                        }
                        int index = this.mStreamStates[streamType].getIndex(step2);
                        sendVolumeUpdate(streamType, oldIndex, index, flags2, step2);
                        this.mHwAudioServiceEx.dispatchVolumeChange(step2, streamType, caller, (index + 5) / 10);
                        if (LOUD_VOICE_MODE_SUPPORT) {
                            sendMsg(this.mAudioHandler, 10001, 0, 1, 0, new AbsAudioService.DeviceVolumeState(direction, step2, oldIndex, streamType), 0);
                        }
                        if (IS_SUPER_RECEIVER_ENABLED) {
                            sendMsg(this.mAudioHandler, 10099, 0, 1, 0, new AbsAudioService.DeviceVolumeState(direction, step2, oldIndex, streamType), 0);
                            return;
                        }
                        return;
                    }
                    return;
                }
                return;
            }
            Log.w(TAG, "MODIFY_PHONE_STATE Permission Denial: adjustStreamVolume from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            return;
        }
        return;
        while (true) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onCheckAbsVolumeState() {
        synchronized (this.mAbsMediaVolumeStateLock) {
            Log.i(TAG, "onCheckAbsVolumeState() , mAbsVolumeState=" + this.mAbsVolumeState);
            if (this.mAbsVolumeState == 1) {
                this.mAbsVolumeState = 0;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onUnmuteStream(int stream, int flags) {
        this.mStreamStates[stream].mute(false);
        int device = getDeviceForStream(stream);
        int index = this.mStreamStates[stream].getIndex(device);
        sendVolumeUpdate(stream, index, index, flags, device);
    }

    private void setSystemAudioVolume(int oldVolume, int newVolume, int maxVolume, int flags) {
        synchronized (this.mHdmiClientLock) {
            if (!(this.mHdmiManager == null || this.mHdmiTvClient == null || oldVolume == newVolume || (flags & 256) != 0)) {
                if (this.mHdmiSystemAudioSupported) {
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

    /* access modifiers changed from: package-private */
    public class StreamVolumeCommand {
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

    private int getNewRingerMode(int stream, int index, int flags) {
        if (this.mIsSingleVolume) {
            return getRingerModeExternal();
        }
        if ((flags & 2) == 0 && stream != getUiSoundsStreamType()) {
            return getRingerModeExternal();
        }
        if (index != 0) {
            return 2;
        }
        if (this.mHasVibrator) {
            return 1;
        }
        if (this.mVolumePolicy.volumeDownToEnterSilent) {
            return 0;
        }
        return 2;
    }

    private boolean isAndroidNPlus(String caller) {
        try {
            if (this.mContext.getPackageManager().getApplicationInfoAsUser(caller, 0, UserHandle.getUserId(Binder.getCallingUid())).targetSdkVersion >= MSG_ENABLE_SURROUND_FORMATS) {
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
            setRingerMode(getNewRingerMode(stream, index, flags), "AS.AudioService.onSetStreamVolume", false);
        }
        if (streamType != 6) {
            VolumeStreamState volumeStreamState = this.mStreamStates[stream];
            if (index == 0) {
                z = true;
            }
            volumeStreamState.mute(z);
        }
    }

    private void enforceModifyAudioRoutingPermission() {
        if (this.mContext.checkCallingPermission("android.permission.MODIFY_AUDIO_ROUTING") != 0) {
            throw new SecurityException("Missing MODIFY_AUDIO_ROUTING permission");
        }
    }

    public void setVolumeIndexForAttributes(AudioAttributes attr, int index, int flags, String callingPackage) {
        enforceModifyAudioRoutingPermission();
        Preconditions.checkNotNull(attr, "attr must not be null");
        int device = getDeviceForStream(AudioProductStrategy.getLegacyStreamTypeForStrategyWithAudioAttributes(attr));
        AudioSystem.getVolumeIndexForAttributes(attr, device);
        AudioSystem.setVolumeIndexForAttributes(attr, index, device);
        AudioVolumeGroup avg = getAudioVolumeGroupById(getVolumeGroupIdForAttributes(attr));
        if (avg != null) {
            int[] legacyStreamTypes = avg.getLegacyStreamTypes();
            int i = 0;
            for (int length = legacyStreamTypes.length; i < length; length = length) {
                setStreamVolume(legacyStreamTypes[i], index, flags, callingPackage, callingPackage, Binder.getCallingUid());
                i++;
            }
        }
    }

    /* access modifiers changed from: protected */
    public AudioVolumeGroup getAudioVolumeGroupById(int volumeGroupId) {
        for (AudioVolumeGroup avg : AudioVolumeGroup.getAudioVolumeGroups()) {
            if (avg.getId() == volumeGroupId) {
                return avg;
            }
        }
        Log.e(TAG, ": invalid volume group id: " + volumeGroupId + " requested");
        return null;
    }

    public int getVolumeIndexForAttributes(AudioAttributes attr) {
        enforceModifyAudioRoutingPermission();
        Preconditions.checkNotNull(attr, "attr must not be null");
        return AudioSystem.getVolumeIndexForAttributes(attr, getDeviceForStream(AudioProductStrategy.getLegacyStreamTypeForStrategyWithAudioAttributes(attr)));
    }

    public int getMaxVolumeIndexForAttributes(AudioAttributes attr) {
        enforceModifyAudioRoutingPermission();
        Preconditions.checkNotNull(attr, "attr must not be null");
        return AudioSystem.getMaxVolumeIndexForAttributes(attr);
    }

    public int getMinVolumeIndexForAttributes(AudioAttributes attr) {
        enforceModifyAudioRoutingPermission();
        Preconditions.checkNotNull(attr, "attr must not be null");
        return AudioSystem.getMinVolumeIndexForAttributes(attr);
    }

    public void setStreamVolume(int streamType, int index, int flags, String callingPackage) {
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.AUDIO_SETSTREAMVOLUME, new Object[0]);
        if (streamType == 10 && !canChangeAccessibilityVolume()) {
            Log.w(TAG, "Trying to call setStreamVolume() for a11y without CHANGE_ACCESSIBILITY_VOLUME  callingPackage=" + callingPackage);
        } else if (streamType == 0 && index == 0 && this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE") != 0) {
            Log.w(TAG, "Trying to call setStreamVolume() for STREAM_VOICE_CALL and index 0 without MODIFY_PHONE_STATE  callingPackage=" + callingPackage);
        } else {
            sVolumeLogger.log(new AudioServiceEvents.VolumeEvent(2, streamType, index, flags, callingPackage));
            setStreamVolume(streamType, index, flags, callingPackage, callingPackage, Binder.getCallingUid());
        }
    }

    private boolean canChangeAccessibilityVolume() {
        synchronized (this.mAccessibilityServiceUidsLock) {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.CHANGE_ACCESSIBILITY_VOLUME") == 0) {
                return true;
            }
            if (this.mAccessibilityServiceUids != null) {
                int callingUid = Binder.getCallingUid();
                for (int i = 0; i < this.mAccessibilityServiceUids.length; i++) {
                    if (this.mAccessibilityServiceUids[i] == callingUid) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public int getHearingAidStreamType() {
        return getHearingAidStreamType(this.mMode);
    }

    private int getHearingAidStreamType(int mode) {
        return (mode == 2 || mode == 3 || this.mVoiceActive.get()) ? 0 : 3;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onPlaybackConfigChange(List<AudioPlaybackConfiguration> configs) {
        boolean voiceActive = false;
        Iterator<AudioPlaybackConfiguration> it = configs.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            AudioPlaybackConfiguration config = it.next();
            int usage = config.getAudioAttributes().getUsage();
            if ((usage == 2 || usage == 3) && config.getPlayerState() == 2) {
                voiceActive = true;
                break;
            }
        }
        if (this.mVoiceActive.getAndSet(voiceActive) != voiceActive) {
            updateHearingAidVolumeOnVoiceActivityUpdate();
        }
    }

    private void updateHearingAidVolumeOnVoiceActivityUpdate() {
        int streamType = getHearingAidStreamType();
        int index = getStreamVolume(streamType);
        sVolumeLogger.log(new AudioServiceEvents.VolumeEvent(6, this.mVoiceActive.get(), streamType, index));
        this.mDeviceBroker.postSetHearingAidVolumeIndex(index * 10, streamType);
    }

    /* access modifiers changed from: package-private */
    public void updateAbsVolumeMultiModeDevices(int oldMode, int newMode) {
        if (oldMode != newMode) {
            if (newMode != 0) {
                if (newMode == 1) {
                    return;
                }
                if (!(newMode == 2 || newMode == 3)) {
                    return;
                }
            }
            int streamType = getHearingAidStreamType(newMode);
            int device = AudioSystem.getDevicesForStream(streamType);
            int i = this.mAbsVolumeMultiModeCaseDevices;
            if ((device & i) != 0 && (i & device) == 134217728) {
                int index = getStreamVolume(streamType);
                sVolumeLogger.log(new AudioServiceEvents.VolumeEvent(7, newMode, streamType, index));
                this.mDeviceBroker.postSetHearingAidVolumeIndex(index * 10, streamType);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:115:0x0202 A[SYNTHETIC, Splitter:B:115:0x0202] */
    /* JADX WARNING: Removed duplicated region for block: B:122:0x0233 A[Catch:{ all -> 0x0228, all -> 0x02f7 }] */
    private void setStreamVolume(int streamType, int index, int flags, String callingPackage, String caller, int uid) {
        int uid2;
        Throwable th;
        int oldIndex;
        int index2;
        int flags2;
        int i;
        int i2;
        int flags3;
        int index3;
        Throwable th2;
        if (!checkEnbaleVolumeAdjust()) {
            Log.i(TAG, "setStreamVolume not allowed by checkEnbaleVolumeAdjust");
            return;
        }
        Log.i(TAG, "setStreamVolume(stream =" + streamType + ",index =" + index + ",calling =" + callingPackage + ")");
        if (this.mUseFixedVolume) {
            Log.i(TAG, "setStreamVolume not allowed by mUserFixedVolume");
            return;
        }
        ensureValidStreamType(streamType);
        int streamTypeAlias = mStreamVolumeAlias[streamType];
        VolumeStreamState streamState = this.mStreamStates[streamTypeAlias];
        HwCustAudioService hwCustAudioService = this.mCust;
        if (hwCustAudioService == null || !hwCustAudioService.isTurningAllSound()) {
            int device = getDeviceForStream(streamType);
            if ((device & 896) != 0 || (flags & 64) == 0) {
                if ((device & 128) != 0) {
                    AudioSystem.setParameters("parentcontrol_volume_index=" + index);
                }
                if (isTv) {
                    synchronized (this.mAbsMediaVolumeStateLock) {
                        if (!((device & 896) == 0 || (flags & 64) == 0 || callingPackage == null)) {
                            if (callingPackage.equals("com.android.bluetooth") && this.mAbsVolumeState == 1) {
                                Log.i(TAG, "setStreamVolume not allowed by absvolume for com.android.bluetooth , mAbsVolumeState " + this.mAbsVolumeState);
                                return;
                            }
                        }
                    }
                }
                if (uid == 1000) {
                    uid2 = UserHandle.getUid(getCurrentUserId(), UserHandle.getAppId(uid));
                } else {
                    uid2 = uid;
                }
                if (this.mAppOps.noteOp(STREAM_VOLUME_OPS[streamTypeAlias], uid2, callingPackage) != 0) {
                    Log.i(TAG, "setStreamVolume not allowed by appops");
                } else if (isAndroidNPlus(callingPackage) && wouldToggleZenMode(getNewRingerMode(streamTypeAlias, index, flags)) && !this.mNm.isNotificationPolicyAccessGrantedForPackage(callingPackage)) {
                    throw new SecurityException("Not allowed to change Do Not Disturb state");
                } else if (!volumeAdjustmentAllowedByDnd(streamTypeAlias, flags)) {
                    Log.i(TAG, "setStreamVolume not allowed by dnd");
                } else {
                    synchronized (this.mSafeMediaVolumeStateLock) {
                        try {
                            this.mPendingVolumeCommand = null;
                            oldIndex = streamState.getIndex(device);
                            int index4 = rescaleIndex(index * 10, streamType, streamTypeAlias);
                            try {
                                if (isTv && streamType == 3) {
                                    try {
                                        index4 = streamState.getValidIndex(index4);
                                    } catch (Throwable th3) {
                                        th = th3;
                                        while (true) {
                                            try {
                                                break;
                                            } catch (Throwable th4) {
                                                th = th4;
                                            }
                                        }
                                        throw th;
                                    }
                                }
                                if (streamTypeAlias == 3 && (device & 896) != 0 && (flags & 64) == 0) {
                                    Log.i(TAG, "setStreamVolume postSetAvrcpAbsoluteVolumeIndex index=" + index4 + "stream=" + streamType);
                                    this.mDeviceBroker.postSetAvrcpAbsoluteVolumeIndex(index4 / 10);
                                }
                                if ((134217728 & device) != 0 && streamType == getHearingAidStreamType()) {
                                    Log.i(TAG, "setStreamVolume postSetHearingAidVolumeIndex index=" + index4 + " stream=" + streamType);
                                    this.mDeviceBroker.postSetHearingAidVolumeIndex(index4, streamType);
                                }
                                if (streamTypeAlias == 3) {
                                    setSystemAudioVolume(oldIndex, index4, getStreamMaxVolume(streamType), flags);
                                }
                                int flags4 = flags & -33;
                                if (streamTypeAlias == 3) {
                                    try {
                                        if ((this.mFixedVolumeDevices & device) != 0) {
                                            int flags5 = flags4 | 32;
                                            if (index4 == 0) {
                                                index2 = index4;
                                                flags2 = flags5;
                                            } else if (this.mSafeMediaVolumeState != 3 || (603979788 & device) == 0) {
                                                index2 = streamState.getMaxIndex();
                                                flags2 = flags5;
                                            } else {
                                                index2 = safeMediaVolumeIndex(device);
                                                flags2 = flags5;
                                            }
                                            if (checkSafeMediaVolume(streamTypeAlias, index2, device)) {
                                                try {
                                                    this.mVolumeController.postDisplaySafeVolumeWarning(flags2);
                                                    flags3 = flags2;
                                                    this.mPendingVolumeCommand = new StreamVolumeCommand(streamType, index2, flags3, device);
                                                    Log.i(TAG, "setStreamVolume not allowed by safevolume");
                                                    index3 = index2;
                                                } catch (Throwable th5) {
                                                    th = th5;
                                                    while (true) {
                                                        break;
                                                    }
                                                    throw th;
                                                }
                                            } else {
                                                flags3 = flags2;
                                                onSetStreamVolume(streamType, index2, flags2, device, caller);
                                                index3 = this.mStreamStates[streamType].getIndex(device);
                                            }
                                            try {
                                            } catch (Throwable th6) {
                                                th = th6;
                                                while (true) {
                                                    break;
                                                }
                                                throw th;
                                            }
                                        }
                                    } catch (Throwable th7) {
                                        th = th7;
                                        while (true) {
                                            break;
                                        }
                                        throw th;
                                    }
                                }
                                index2 = index4;
                                flags2 = flags4;
                            } catch (Throwable th8) {
                                th = th8;
                                while (true) {
                                    break;
                                }
                                throw th;
                            }
                            try {
                                if (checkSafeMediaVolume(streamTypeAlias, index2, device)) {
                                }
                            } catch (Throwable th9) {
                                th = th9;
                                i2 = flags2;
                                i = index2;
                                while (true) {
                                    break;
                                }
                                throw th;
                            }
                        } catch (Throwable th10) {
                            th = th10;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                    }
                    synchronized (this.mHdmiClientLock) {
                        try {
                            if (this.mHdmiManager != null) {
                                try {
                                    if (this.mHdmiAudioSystemClient != null && this.mHdmiSystemAudioSupported && streamTypeAlias == 3 && oldIndex != index3) {
                                        long identity = Binder.clearCallingIdentity();
                                        this.mHdmiAudioSystemClient.sendReportAudioStatusCecCommand(false, getStreamVolume(3), getStreamMaxVolume(3), isStreamMute(3));
                                        Binder.restoreCallingIdentity(identity);
                                    }
                                } catch (Throwable th11) {
                                    th2 = th11;
                                    while (true) {
                                        try {
                                            break;
                                        } catch (Throwable th12) {
                                            th2 = th12;
                                        }
                                    }
                                    throw th2;
                                }
                            }
                        } catch (Throwable th13) {
                            th2 = th13;
                            while (true) {
                                break;
                            }
                            throw th2;
                        }
                    }
                    sendVolumeUpdate(streamType, oldIndex, index3, flags3, device);
                    this.mHwAudioServiceEx.dispatchVolumeChange(device, streamType, caller, (index3 + 5) / 10);
                    if (LOUD_VOICE_MODE_SUPPORT) {
                        sendMsg(this.mAudioHandler, 10001, 0, 1, 0, new AbsAudioService.DeviceVolumeState(0, device, oldIndex, streamType), 0);
                    }
                    if (IS_SUPER_RECEIVER_ENABLED) {
                        sendMsg(this.mAudioHandler, 10099, 0, 1, 0, new AbsAudioService.DeviceVolumeState(0, device, oldIndex, streamType), 0);
                    }
                }
            } else {
                Log.i(TAG, "setStreamVolume not allowed by absvolume");
            }
        } else {
            sendMsg(this.mAudioHandler, 10, 2, 1, 0, streamState, 0);
        }
    }

    /* access modifiers changed from: protected */
    public int getVolumeGroupIdForAttributes(AudioAttributes attributes) {
        Preconditions.checkNotNull(attributes, "attributes must not be null");
        int volumeGroupId = getVolumeGroupIdForAttributesInt(attributes);
        if (volumeGroupId != -1) {
            return volumeGroupId;
        }
        return getVolumeGroupIdForAttributesInt(AudioProductStrategy.sDefaultAttributes);
    }

    /* access modifiers changed from: protected */
    public int getVolumeGroupIdForAttributesInt(AudioAttributes attributes) {
        Preconditions.checkNotNull(attributes, "attributes must not be null");
        for (AudioProductStrategy productStrategy : AudioProductStrategy.getAudioProductStrategies()) {
            int volumeGroupId = productStrategy.getVolumeGroupIdForAudioAttributes(attributes);
            if (volumeGroupId != -1) {
                return volumeGroupId;
            }
        }
        return -1;
    }

    private boolean volumeAdjustmentAllowedByDnd(int streamTypeAlias, int flags) {
        int zenMode = this.mNm.getZenMode();
        if (zenMode == 0) {
            return true;
        }
        if (zenMode != 1 && zenMode != 2 && zenMode != 3) {
            return true;
        }
        boolean isTotalSilence = Settings.Global.getInt(this.mContext.getContentResolver(), "total_silence_mode", 0) == 1;
        Log.i(TAG, "isTotalSilence : " + isTotalSilence + ", mRingerAndZenModeMutedStreams :" + this.mRingerAndZenModeMutedStreams);
        if ((isStreamMutedByRingerOrZenMode(streamTypeAlias) || (this.mNm.getZenMode() != 1 && streamTypeAlias == 3)) && streamTypeAlias != getUiSoundsStreamType() && (flags & 2) == 0 && isTotalSilence) {
            return false;
        }
        return true;
    }

    public void forceVolumeControlStream(int streamType, IBinder cb) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE") == 0) {
            Log.i(TAG, String.format(Locale.ROOT, "forceVolumeControlStream(%d)", Integer.valueOf(streamType)));
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
                    Log.i(TAG, "forceVolumeControlStream cb:" + cb + " is already linked.");
                } else {
                    this.mForceControlStreamClient.release();
                    this.mForceControlStreamClient = new ForceControlStreamClient(cb);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class ForceControlStreamClient implements IBinder.DeathRecipient {
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

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            synchronized (AudioService.this.mForceControlStreamLock) {
                Log.w(AudioService.TAG, "SCO client died");
                if (AudioService.this.mForceControlStreamClient != this) {
                    Log.w(AudioService.TAG, "unregistered control stream client died");
                } else {
                    AudioService.this.mForceControlStreamClient = null;
                    AudioService.this.mVolumeControlStream = -1;
                    AudioService.this.mUserSelectedVolumeControlStream = false;
                }
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
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendBroadcastToAll(Intent intent) {
        intent.addFlags(DumpState.DUMP_HANDLE);
        intent.addFlags(268435456);
        long ident = Binder.clearCallingIdentity();
        try {
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private void sendStickyBroadcastToAll(Intent intent) {
        intent.addFlags(268435456);
        long ident = Binder.clearCallingIdentity();
        try {
            this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* JADX INFO: finally extract failed */
    private int getCurrentUserId() {
        long ident = Binder.clearCallingIdentity();
        try {
            int i = ActivityManager.getService().getCurrentUser().id;
            Binder.restoreCallingIdentity(ident);
            return i;
        } catch (RemoteException e) {
            Binder.restoreCallingIdentity(ident);
            return 0;
        } catch (Throwable currentUser) {
            Binder.restoreCallingIdentity(ident);
            throw currentUser;
        }
    }

    /* access modifiers changed from: protected */
    public void sendVolumeUpdate(int streamType, int oldIndex, int index, int flags, int device) {
        int streamType2 = mStreamVolumeAlias[streamType];
        if (streamType2 == 3) {
            flags = updateFlagsForTvPlatform(flags);
            if ((this.mFullVolumeDevices & device) != 0) {
                flags &= -2;
            }
        }
        this.mVolumeController.postVolumeChanged(streamType2, flags);
    }

    private int updateFlagsForTvPlatform(int flags) {
        synchronized (this.mHdmiClientLock) {
            if (this.mHdmiTvClient != null && this.mHdmiSystemAudioSupported && (flags & 256) == 0) {
                flags &= -2;
            }
        }
        return flags;
    }

    private void sendMasterMuteUpdate(boolean muted, int flags) {
        this.mVolumeController.postMasterMuteChanged(updateFlagsForTvPlatform(flags));
        broadcastMasterMuteStatus(muted);
    }

    private void broadcastMasterMuteStatus(boolean muted) {
        Intent intent = new Intent("android.media.MASTER_MUTE_CHANGED_ACTION");
        intent.putExtra("android.media.EXTRA_MASTER_VOLUME_MUTED", muted);
        intent.addFlags(603979776);
        sendStickyBroadcastToAll(intent);
    }

    private void setStreamVolumeInt(int streamType, int index, int device, boolean force, String caller) {
        if ((this.mFullVolumeDevices & device) == 0) {
            VolumeStreamState streamState = this.mStreamStates[streamType];
            if (streamState.setIndex(index, device, caller) || force) {
                sendMsg(this.mAudioHandler, 0, 2, device, 0, streamState, 0);
            }
        }
    }

    private void setSystemAudioMute(boolean state) {
        synchronized (this.mHdmiClientLock) {
            if (!(this.mHdmiManager == null || this.mHdmiTvClient == null)) {
                if (this.mHdmiSystemAudioSupported) {
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

    public boolean isStreamMute(int streamType) {
        boolean z;
        if (streamType == Integer.MIN_VALUE) {
            streamType = getActiveStreamType(streamType);
        }
        synchronized (VolumeStreamState.class) {
            ensureValidStreamType(streamType);
            z = this.mStreamStates[streamType].mIsMuted;
        }
        return z;
    }

    /* access modifiers changed from: private */
    public class RmtSbmxFullVolDeathHandler implements IBinder.DeathRecipient {
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

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            Log.w(AudioService.TAG, "Recorder with remote submix at full volume died " + this.mICallback);
            AudioService.this.forceRemoteSubmixFullVolume(false, this.mICallback);
        }
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
                    if (!hasRmtSbmxFullVolDeathHandlerFor(cb)) {
                        this.mRmtSbmxFullVolDeathHandlers.add(new RmtSbmxFullVolDeathHandler(cb));
                        if (this.mRmtSbmxFullVolRefCount == 0) {
                            this.mFullVolumeDevices |= 32768;
                            this.mFixedVolumeDevices |= 32768;
                            applyRequired = true;
                        }
                        this.mRmtSbmxFullVolRefCount++;
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
    /* access modifiers changed from: public */
    private void setMasterMuteInternalNoCallerCheck(boolean mute, int flags, int userId) {
        Log.i(TAG, String.format(Locale.ROOT, "Master mute %s, %d, user=%d", Boolean.valueOf(mute), Integer.valueOf(flags), Integer.valueOf(userId)));
        if (!isPlatformAutomotive() && this.mUseFixedVolume) {
            return;
        }
        if (((isPlatformAutomotive() && userId == 0) || getCurrentUserId() == userId) && mute != AudioSystem.getMasterMute()) {
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
        if (Binder.getCallingUid() > 1000) {
            Log.e(TAG, "setMasterMute() called permission denial, need system uid.");
            return;
        }
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
    /* access modifiers changed from: public */
    private void setMicrophoneMuteNoCallerCheck(boolean on, int userId) {
        Log.i(TAG, String.format(Locale.ROOT, "Mic mute %s, user=%d", Boolean.valueOf(on), Integer.valueOf(userId)));
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
        if (isAndroidNPlus(caller) && wouldToggleZenMode(ringerMode)) {
            if ("com.huawei.camera".equals(caller) && this.mContext.checkCallingPermission(SYSTEM_PERMISSION) == 0) {
                Slog.i(TAG, caller + ": skip verification succ");
            } else if (!this.mNm.isNotificationPolicyAccessGrantedForPackage(caller)) {
                throw new SecurityException("Not allowed to change Do Not Disturb state");
            }
        }
        setRingerMode(ringerMode, caller, true);
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
        if (this.mContext.getResources().getBoolean(17891574)) {
            silenceRingerSetting = Settings.Secure.getIntForUser(this.mContentResolver, "volume_hush_gesture", 0, -2);
        }
        if (silenceRingerSetting == 1) {
            effect = VibrationEffect.get(5);
            ringerMode = 1;
            toastText = 17041457;
        } else if (silenceRingerSetting == 2) {
            effect = VibrationEffect.get(1);
            ringerMode = 0;
            toastText = 17041456;
        }
        maybeVibrate(effect, reason);
        setRingerModeInternal(ringerMode, reason);
        Toast.makeText(this.mContext, toastText, 0).show();
    }

    private boolean maybeVibrate(VibrationEffect effect, String reason) {
        if (!this.mHasVibrator) {
            return false;
        }
        if ((Settings.System.getIntForUser(this.mContext.getContentResolver(), "haptic_feedback_enabled", 0, -2) == 0) || effect == null) {
            return false;
        }
        this.mVibrator.vibrate(Binder.getCallingUid(), this.mContext.getOpPackageName(), effect, reason, VIBRATION_ATTRIBUTES);
        return true;
    }

    private void setRingerMode(int ringerMode, String caller, boolean external) {
        int ringerMode2;
        if (checkEnbaleVolumeAdjust()) {
            if (this.mUseFixedVolume) {
                return;
            }
            if (!this.mIsSingleVolume) {
                if (caller == null || caller.length() == 0) {
                    throw new IllegalArgumentException("Bad caller: " + caller);
                }
                ensureValidRingerMode(ringerMode);
                if (ringerMode != 1 || this.mHasVibrator) {
                    ringerMode2 = ringerMode;
                } else {
                    ringerMode2 = 0;
                }
                long identity = Binder.clearCallingIdentity();
                try {
                    synchronized (this.mSettingsLock) {
                        int ringerModeInternal = getRingerModeInternal();
                        int ringerModeExternal = getRingerModeExternal();
                        if (external) {
                            setRingerModeExt(ringerMode2);
                            if (this.mRingerModeDelegate != null) {
                                ringerMode2 = this.mRingerModeDelegate.onSetRingerModeExternal(ringerModeExternal, ringerMode2, caller, ringerModeInternal, this.mVolumePolicy);
                            }
                            if (ringerMode2 != ringerModeInternal) {
                                setRingerModeInt(ringerMode2, true);
                            }
                        } else {
                            if (ringerMode2 != ringerModeInternal) {
                                setRingerModeInt(ringerMode2, true);
                            }
                            if (this.mRingerModeDelegate != null) {
                                ringerMode2 = this.mRingerModeDelegate.onSetRingerModeInternal(ringerModeInternal, ringerMode2, caller, ringerModeExternal, this.mVolumePolicy);
                            }
                            setRingerModeExt(ringerMode2);
                        }
                    }
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
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

    @GuardedBy({"mSettingsLock"})
    private void muteRingerModeStreams() {
        int numStreamTypes;
        int numStreamTypes2;
        int numStreamTypes3;
        int numStreamTypes4 = AudioSystem.getNumStreamTypes();
        if (this.mNm == null) {
            this.mNm = (NotificationManager) this.mContext.getSystemService("notification");
        }
        int ringerMode = this.mRingerMode;
        int i = 0;
        int i2 = 1;
        boolean ringerModeMute = ringerMode == 1 || ringerMode == 0;
        boolean shouldRingSco = ringerMode == 1 && isBluetoothScoOn() && AudioSystem.getDeviceConnectionState(DumpState.DUMP_APEX, "") == 0;
        sendMsg(this.mAudioHandler, 8, 2, 7, shouldRingSco ? 3 : 0, "muteRingerModeStreams() from u/pid:" + Binder.getCallingUid() + SliceClientPermissions.SliceAuthority.DELIMITER + Binder.getCallingPid(), 0);
        int streamType = numStreamTypes4 - 1;
        while (streamType >= 0) {
            boolean isMuted = isStreamMutedByRingerOrZenMode(streamType);
            int i3 = (shouldZenMuteStream(streamType) || (ringerModeMute && isStreamAffectedByRingerMode(streamType) && ((!shouldRingSco || streamType != 2) ? i2 : i) != 0)) ? i2 : i;
            if (isMuted == i3) {
                numStreamTypes = numStreamTypes4;
                numStreamTypes2 = i;
            } else if (this.mStreamStates[streamType] == null || i3 != 0) {
                numStreamTypes = numStreamTypes4;
                numStreamTypes2 = i;
                VolumeStreamState[] volumeStreamStateArr = this.mStreamStates;
                if (volumeStreamStateArr[streamType] != null) {
                    i2 = 1;
                    volumeStreamStateArr[streamType].mute(true);
                    this.mRingerAndZenModeMutedStreams |= 1 << streamType;
                } else {
                    i2 = 1;
                }
            } else {
                if (mStreamVolumeAlias[streamType] == 2) {
                    synchronized (VolumeStreamState.class) {
                        try {
                            VolumeStreamState vss = this.mStreamStates[streamType];
                            int i4 = i;
                            while (i4 < vss.mIndexMap.size()) {
                                int device = vss.mIndexMap.keyAt(i4);
                                if (vss.mIndexMap.valueAt(i4) == 0) {
                                    numStreamTypes3 = numStreamTypes4;
                                    vss.setIndex(10, device, TAG);
                                } else {
                                    numStreamTypes3 = numStreamTypes4;
                                }
                                i4++;
                                numStreamTypes4 = numStreamTypes3;
                            }
                            numStreamTypes = numStreamTypes4;
                            sendMsg(this.mAudioHandler, 1, 2, getDeviceForStream(streamType), 0, this.mStreamStates[streamType], 500);
                        } catch (Throwable th) {
                            th = th;
                            throw th;
                        }
                    }
                } else {
                    numStreamTypes = numStreamTypes4;
                }
                numStreamTypes2 = 0;
                this.mStreamStates[streamType].mute(false);
                this.mRingerAndZenModeMutedStreams &= ~(1 << streamType);
                i2 = 1;
            }
            streamType--;
            i = numStreamTypes2;
            numStreamTypes4 = numStreamTypes;
        }
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
    /* access modifiers changed from: public */
    private void setRingerModeInt(int ringerMode, boolean persist) {
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

    /* access modifiers changed from: package-private */
    public void postUpdateRingerModeServiceInt() {
        sendMsg(this.mAudioHandler, MSG_UPDATE_RINGER_MODE, 2, 0, 0, null, 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onUpdateRingerModeServiceInt() {
        setRingerModeInt(getRingerModeInternal(), false);
    }

    public boolean shouldVibrate(int vibrateType) {
        int vibrateSetting;
        if (!this.mHasVibrator || (vibrateSetting = getVibrateSetting(vibrateType)) == 0) {
            return false;
        }
        if (vibrateSetting != 1) {
            if (vibrateSetting == 2 && getRingerModeExternal() == 1) {
                return true;
            }
            return false;
        } else if (getRingerModeExternal() != 0) {
            return true;
        } else {
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

    /* access modifiers changed from: package-private */
    public int getModeOwnerPid() {
        try {
            return this.mSetModeDeathHandlers.get(0).getPid();
        } catch (Exception e) {
            return 0;
        }
    }

    /* access modifiers changed from: protected */
    public class SetModeDeathHandler implements IBinder.DeathRecipient {
        private IBinder mCb;
        private int mMode = 0;
        private int mPid;
        private boolean mSpeakerPhoneState;

        SetModeDeathHandler(IBinder cb, int pid) {
            this.mCb = cb;
            this.mPid = pid;
            this.mSpeakerPhoneState = AudioService.this.isSpeakerphoneOn();
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            int oldModeOwnerPid = 0;
            int newModeOwnerPid = 0;
            synchronized (AudioService.this.mDeviceBroker.mSetModeLock) {
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
                AudioService.this.mDeviceBroker.postDisconnectBluetoothSco(newModeOwnerPid);
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

        public void setSpeakerPhoneState(boolean on) {
            Log.i(AudioService.TAG, "setModeInt setSpeakerPhoneState " + on + ", pid = " + this.mPid);
            this.mSpeakerPhoneState = on;
        }

        public boolean getSpeakerPhoneState() {
            return this.mSpeakerPhoneState;
        }
    }

    private void dumpAudioMode(PrintWriter pw) {
        pw.println("\nAudio Mode:");
        synchronized (this.mDeviceBroker.mSetModeLock) {
            Iterator iter = this.mSetModeDeathHandlers.iterator();
            while (iter.hasNext()) {
                iter.next().dump(pw);
            }
        }
    }

    public void setMode(int mode, IBinder cb, String callingPackage) {
        int newModeOwnerPid;
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.AUDIO_SETMODE, new Object[0]);
        Log.i(TAG, "setMode(mode=" + mode + ", callingPackage=" + callingPackage + ")");
        if (checkAudioSettingsPermission("setMode()")) {
            if (mode == 2 && this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE") != 0) {
                Log.w(TAG, "MODIFY_PHONE_STATE Permission Denial: setMode(MODE_IN_CALL) from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            } else if (mode >= -1 && mode < 4) {
                this.mDeviceBroker.setModeForBtMetadata(mode, callingPackage);
                if (this.mMode == 2 && (mode == 3 || mode == 1)) {
                    synchronized (this.mDeviceBroker.mSetModeLock) {
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
                synchronized (this.mDeviceBroker.mSetModeLock) {
                    if (!this.mSetModeDeathHandlers.isEmpty()) {
                        oldModeOwnerPid = this.mSetModeDeathHandlers.get(0).getPid();
                    }
                    if (mode == -1) {
                        mode = this.mMode;
                    }
                    newModeOwnerPid = setModeInt(mode, cb, Binder.getCallingPid(), callingPackage);
                }
                if (!(newModeOwnerPid == oldModeOwnerPid || newModeOwnerPid == 0)) {
                    this.mDeviceBroker.postDisconnectBluetoothSco(newModeOwnerPid);
                }
                if (LOUD_VOICE_MODE_SUPPORT) {
                    getOldInCallDevice(mode);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @GuardedBy({"mDeviceBroker.mSetModeLock"})
    public int setModeInt(int mode, IBinder cb, int pid, String caller) {
        int i;
        boolean isCallStopped;
        int actualMode;
        IBinder cb2;
        int mode2;
        int status;
        int newModeOwnerPid;
        SetModeDeathHandler hdlr;
        StringBuilder sb = new StringBuilder();
        sb.append("setModeInt(mode=");
        int mode3 = mode;
        sb.append(mode3);
        sb.append(", pid=");
        sb.append(pid);
        sb.append(", caller=");
        sb.append(caller);
        sb.append(")");
        Log.i(TAG, sb.toString());
        if (cb == null) {
            Log.e(TAG, "setModeInt() called with null binder");
            return 0;
        }
        SetModeDeathHandler hdlr2 = null;
        Iterator iter = this.mSetModeDeathHandlers.iterator();
        while (true) {
            i = 0;
            if (!iter.hasNext()) {
                isCallStopped = false;
                break;
            }
            SetModeDeathHandler h = iter.next();
            if (h.getPid() == pid) {
                hdlr2 = h;
                iter.remove();
                hdlr2.getBinder().unlinkToDeath(hdlr2, 0);
                isCallStopped = true;
                break;
            }
        }
        int oldMode = this.mMode;
        IBinder cb3 = cb;
        while (true) {
            actualMode = mode3;
            if (mode3 != 0) {
                if (hdlr2 == null) {
                    hdlr = new SetModeDeathHandler(cb3, pid);
                } else {
                    hdlr = hdlr2;
                }
                try {
                    cb3.linkToDeath(hdlr, 0);
                } catch (RemoteException e) {
                    Log.w(TAG, "setMode() could not link to " + cb3 + " binder death");
                }
                this.mSetModeDeathHandlers.add(0, hdlr);
                hdlr.setMode(mode3);
                cb2 = cb3;
                hdlr2 = hdlr;
            } else if (!this.mSetModeDeathHandlers.isEmpty()) {
                hdlr2 = this.mSetModeDeathHandlers.get(i);
                IBinder cb4 = hdlr2.getBinder();
                actualMode = hdlr2.getMode();
                boolean speakerPhoneState = hdlr2.getSpeakerPhoneState();
                if (isCallStopped && speakerPhoneState != isSpeakerphoneOn()) {
                    Log.i(TAG, "setModeInt setSpeakerphoneOn(speakerPhoneState) = " + speakerPhoneState);
                    setSpeakerphoneOn(speakerPhoneState);
                }
                Log.w(TAG, " using mode=" + mode3 + " instead due to death hdlr at pid=" + hdlr2.mPid + ";pkgName=" + getPackageNameByPid(hdlr2.mPid));
                cb2 = cb4;
            } else {
                cb2 = cb3;
            }
            if (actualMode != this.mMode) {
                long identity = Binder.clearCallingIdentity();
                int status2 = AudioSystem.setPhoneState(actualMode);
                Binder.restoreCallingIdentity(identity);
                if (status2 == 0) {
                    Log.i(TAG, " mode successfully set to " + actualMode);
                    this.mMode = actualMode;
                    this.mHwAudioServiceEx.dipatchAudioModeChanged(actualMode);
                } else {
                    if (hdlr2 != null) {
                        this.mSetModeDeathHandlers.remove(hdlr2);
                        cb2.unlinkToDeath(hdlr2, 0);
                    }
                    Log.w(TAG, " mode set to MODE_NORMAL after phoneState pb");
                    mode3 = 0;
                }
                mode2 = mode3;
                status = status2;
            } else {
                mode2 = mode3;
                status = 0;
            }
            if (status == 0 || this.mSetModeDeathHandlers.isEmpty()) {
                break;
            }
            cb3 = cb2;
            mode3 = mode2;
            i = 0;
        }
        if (status != 0) {
            return 0;
        }
        if (actualMode != 0) {
            if (this.mSetModeDeathHandlers.isEmpty()) {
                Log.e(TAG, "setMode() different from MODE_NORMAL with empty mode client stack");
            } else {
                newModeOwnerPid = this.mSetModeDeathHandlers.get(0).getPid();
                this.mModeLogger.log(new AudioServiceEvents.PhoneStateEvent(caller, pid, mode2, newModeOwnerPid, actualMode));
                int streamType = getActiveStreamType(Integer.MIN_VALUE);
                int device = getDeviceForStream(streamType);
                setStreamVolumeInt(mStreamVolumeAlias[streamType], this.mStreamStates[mStreamVolumeAlias[streamType]].getIndex(device), device, true, caller);
                updateStreamVolumeAlias(true, caller);
                updateAbsVolumeMultiModeDevices(oldMode, actualMode);
                updateAftPolicy();
                return newModeOwnerPid;
            }
        }
        newModeOwnerPid = 0;
        this.mModeLogger.log(new AudioServiceEvents.PhoneStateEvent(caller, pid, mode2, newModeOwnerPid, actualMode));
        int streamType2 = getActiveStreamType(Integer.MIN_VALUE);
        int device2 = getDeviceForStream(streamType2);
        setStreamVolumeInt(mStreamVolumeAlias[streamType2], this.mStreamStates[mStreamVolumeAlias[streamType2]].getIndex(device2), device2, true, caller);
        updateStreamVolumeAlias(true, caller);
        updateAbsVolumeMultiModeDevices(oldMode, actualMode);
        updateAftPolicy();
        return newModeOwnerPid;
    }

    public int getMode() {
        return this.mMode;
    }

    class LoadSoundEffectReply {
        public int mStatus = 1;

        LoadSoundEffectReply() {
        }
    }

    private void loadTouchSoundAssetDefaults() {
        SOUND_EFFECT_FILES.add("Effect_Tick.ogg");
        for (int i = 0; i < 10; i++) {
            int[][] iArr = this.SOUND_EFFECT_FILES_MAP;
            iArr[i][0] = 0;
            iArr[i][1] = -1;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loadTouchSoundAssets() {
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
                if (0 == 0) {
                    return;
                }
            } catch (XmlPullParserException e3) {
                Log.w(TAG, "XML parser exception reading touch sound assets", e3);
                if (0 == 0) {
                    return;
                }
            } catch (IOException e4) {
                Log.w(TAG, "I/O exception reading touch sound assets", e4);
                if (0 == 0) {
                    return;
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    parser.close();
                }
                throw th;
            }
            parser.close();
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

    public boolean loadSoundEffects() {
        Throwable th;
        int attempts = 3;
        LoadSoundEffectReply reply = new LoadSoundEffectReply();
        synchronized (reply) {
            try {
                sendMsg(this.mAudioHandler, 7, 2, 0, 0, reply, 0);
                while (true) {
                    if (reply.mStatus != 1) {
                        break;
                    }
                    int attempts2 = attempts - 1;
                    if (attempts <= 0) {
                        break;
                    }
                    try {
                        reply.wait(5000);
                    } catch (InterruptedException e) {
                        Log.w(TAG, "loadSoundEffects Interrupted while waiting sound pool loaded.");
                    } catch (Throwable th2) {
                        th = th2;
                    }
                    attempts = attempts2;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
        if (reply.mStatus == 0) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void scheduleLoadSoundEffects() {
        sendMsg(this.mAudioHandler, 7, 2, 0, 0, null, 0);
    }

    public void unloadSoundEffects() {
        sendMsg(this.mAudioHandler, 15, 2, 0, 0, null, 0);
    }

    /* access modifiers changed from: package-private */
    public class SoundPoolListenerThread extends Thread {
        public SoundPoolListenerThread() {
            super("SoundPoolListenerThread");
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            Looper.prepare();
            AudioService.this.mSoundPoolLooper = Looper.myLooper();
            synchronized (AudioService.this.mSoundEffectsLock) {
                if (AudioService.this.mSoundPool != null) {
                    AudioService.this.mSoundPoolCallBack = new SoundPoolCallback();
                    AudioService.this.mSoundPool.setOnLoadCompleteListener(AudioService.this.mSoundPoolCallBack);
                }
                AudioService.this.mSoundEffectsLock.notify();
            }
            Looper.loop();
        }
    }

    /* access modifiers changed from: private */
    public final class SoundPoolCallback implements SoundPool.OnLoadCompleteListener {
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

        @Override // android.media.SoundPool.OnLoadCompleteListener
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

    public void reloadAudioSettings() {
        readAudioSettings(false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void readAudioSettings(boolean userSwitch) {
        readPersistedSettings();
        readUserRestrictions();
        int numStreamTypes = AudioSystem.getNumStreamTypes();
        for (int streamType = 0; streamType < numStreamTypes; streamType++) {
            VolumeStreamState streamState = this.mStreamStates[streamType];
            if (!userSwitch || mStreamVolumeAlias[streamType] != 3) {
                streamState.readSettings();
                synchronized (VolumeStreamState.class) {
                    if (streamState.mIsMuted && ((!isStreamAffectedByMute(streamType) && !isStreamMutedByRingerOrZenMode(streamType)) || this.mUseFixedVolume)) {
                        streamState.mIsMuted = false;
                    }
                    if (!streamState.mIsMuted && streamState.getIndex(2) == 0 && streamState.mStreamType == 2 && getRingerModeInternal() == 2) {
                        this.mRingerAndZenModeMutedStreams |= 1 << streamType;
                    }
                }
            }
        }
        setRingerModeInt(getRingerModeInternal(), false);
        checkAllFixedVolumeDevices();
        checkAllAliasStreamVolumes();
        checkMuteAffectedStreams();
        synchronized (this.mSafeMediaVolumeStateLock) {
            this.mMusicActiveMs = MathUtils.constrain(Settings.Secure.getIntForUser(this.mContentResolver, "unsafe_volume_music_active_ms", 0, -2), 0, UNSAFE_VOLUME_MUSIC_ACTIVE_MS_MAX);
            if (this.mSafeMediaVolumeState == 3) {
                enforceSafeMediaVolume(TAG);
            }
        }
    }

    public void setSpeakerphoneOn(boolean on) {
        String eventSource = "setSpeakerphoneOn(" + on + ") from u/pid:" + Binder.getCallingUid() + SliceClientPermissions.SliceAuthority.DELIMITER + Binder.getCallingPid();
        Log.i(TAG, eventSource);
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.AUDIO_SETSPEAKERPHONEON, new Object[0]);
        if (checkAudioSettingsPermission("setSpeakerphoneOn()")) {
            if (checkAudioSettingAllowed("ASsso" + on)) {
                if (this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE") != 0) {
                    synchronized (this.mDeviceBroker.mSetModeLock) {
                        Iterator<SetModeDeathHandler> it = this.mSetModeDeathHandlers.iterator();
                        while (it.hasNext()) {
                            if (it.next().getMode() == 2) {
                                Log.w(TAG, "getMode is call, Permission Denial: setSpeakerphoneOn from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                                return;
                            }
                        }
                    }
                }
                if (this.mDeviceBroker.setSpeakerphoneOn(on, eventSource)) {
                    synchronized (this.mDeviceBroker.mSetModeLock) {
                        Iterator<SetModeDeathHandler> it2 = this.mSetModeDeathHandlers.iterator();
                        while (true) {
                            if (!it2.hasNext()) {
                                break;
                            }
                            SetModeDeathHandler h = it2.next();
                            if (h.getPid() == Binder.getCallingPid()) {
                                h.setSpeakerPhoneState(on);
                                break;
                            }
                        }
                    }
                    long ident = Binder.clearCallingIdentity();
                    try {
                        this.mContext.sendBroadcastAsUser(new Intent("android.media.action.SPEAKERPHONE_STATE_CHANGED").setFlags(1073741824), UserHandle.ALL);
                    } finally {
                        Binder.restoreCallingIdentity(ident);
                    }
                }
                if (LOUD_VOICE_MODE_SUPPORT) {
                    sendMsg(this.mAudioHandler, 10001, 0, 0, 0, null, 500);
                }
                if (IS_SUPER_RECEIVER_ENABLED) {
                    sendMsg(this.mAudioHandler, 10099, 0, 0, 0, null, 0);
                }
                sendCommForceBroadcast();
            }
        }
    }

    public boolean isSpeakerphoneOn() {
        return this.mDeviceBroker.isSpeakerphoneOn();
    }

    public void setBluetoothScoOn(boolean on) {
        String eventSource = "setBluetoothScoOn(" + on + ") from u/pid:" + Binder.getCallingUid() + SliceClientPermissions.SliceAuthority.DELIMITER + Binder.getCallingPid();
        Log.i(TAG, eventSource);
        if (checkAudioSettingsPermission("setBluetoothScoOn()")) {
            if (checkAudioSettingAllowed("ASsbso" + on)) {
                if (UserHandle.getCallingAppId() >= 10000) {
                    this.mDeviceBroker.setBluetoothScoOnByApp(on);
                    return;
                }
                this.mDeviceBroker.setBluetoothScoOn(on, eventSource);
                if (LOUD_VOICE_MODE_SUPPORT) {
                    sendMsg(this.mAudioHandler, 10001, 0, 0, 0, null, 500);
                }
                if (IS_SUPER_RECEIVER_ENABLED) {
                    sendMsg(this.mAudioHandler, 10099, 0, 0, 0, null, 0);
                }
                sendCommForceBroadcast();
            }
        }
    }

    public boolean isBluetoothScoOn() {
        return this.mDeviceBroker.isBluetoothScoOnForApp();
    }

    public void setBluetoothA2dpOn(boolean on) {
        String eventSource = "setBluetoothA2dpOn(" + on + ") from u/pid:" + Binder.getCallingUid() + SliceClientPermissions.SliceAuthority.DELIMITER + Binder.getCallingPid();
        Log.i(TAG, eventSource);
        this.mDeviceBroker.setBluetoothA2dpOn_Async(on, eventSource);
    }

    public boolean isBluetoothA2dpOn() {
        return this.mDeviceBroker.isBluetoothA2dpOn();
    }

    public void startBluetoothSco(IBinder cb, int targetSdkVersion) {
        Log.i(TAG, "In startBluetoothSco()");
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || adapter.getState() != 12) {
            Log.i(TAG, "startBluetoothSco(), BT is not turned ON or adapter is null");
            return;
        }
        int scoAudioMode = targetSdkVersion < 18 ? 0 : -1;
        startBluetoothScoInt(cb, scoAudioMode, "startBluetoothSco()) from u/pid:" + Binder.getCallingUid() + SliceClientPermissions.SliceAuthority.DELIMITER + Binder.getCallingPid());
    }

    public void startBluetoothScoVirtualCall(IBinder cb) {
        startBluetoothScoInt(cb, 0, "startBluetoothScoVirtualCall()) from u/pid:" + Binder.getCallingUid() + SliceClientPermissions.SliceAuthority.DELIMITER + Binder.getCallingPid());
    }

    /* access modifiers changed from: package-private */
    public void startBluetoothScoInt(IBinder cb, int scoAudioMode, String eventSource) {
        if (checkAudioSettingsPermission("startBluetoothSco()") && this.mSystemReady) {
            synchronized (this.mDeviceBroker.mSetModeLock) {
                this.mDeviceBroker.startBluetoothScoForClient_Sync(cb, scoAudioMode, eventSource);
            }
        }
    }

    public void stopBluetoothSco(IBinder cb) {
        Log.i(TAG, "In stopBluetoothSco()");
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || adapter.getState() != 12) {
            Log.i(TAG, "stopBluetoothSco(), BT is not turned ON or adapter is null");
        } else if (checkAudioSettingsPermission("stopBluetoothSco()") && this.mSystemReady) {
            String eventSource = "stopBluetoothSco()) from u/pid:" + Binder.getCallingUid() + SliceClientPermissions.SliceAuthority.DELIMITER + Binder.getCallingPid();
            synchronized (this.mDeviceBroker.mSetModeLock) {
                this.mDeviceBroker.stopBluetoothScoForClient_Sync(cb, eventSource);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public ContentResolver getContentResolver() {
        return this.mContentResolver;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onCheckMusicActive(String caller) {
        synchronized (this.mSafeMediaVolumeStateLock) {
            if (this.mSafeMediaVolumeState == 2) {
                int device = getDeviceForStream(3);
                if ((603979788 & device) != 0) {
                    Log.i(TAG, "onCheckMusicActive enter " + this.mMusicActiveMs);
                    if (!this.mHasAlarm) {
                        sendMsg(this.mAudioHandler, 11, 0, 0, 0, caller, MUSIC_ACTIVE_POLL_PERIOD_MS);
                    }
                    this.mSafeVolumeCaller = caller;
                    int index = this.mStreamStates[3].getIndex(device);
                    if (AudioSystem.isStreamActive(3, (int) CHECK_MUSIC_ACTIVE_DELAY_MS) && index > safeMediaVolumeIndex(device)) {
                        this.mMusicActiveMs += MUSIC_ACTIVE_POLL_PERIOD_MS;
                        if (this.mMusicActiveMs > UNSAFE_VOLUME_MUSIC_ACTIVE_MS_MAX) {
                            setSafeMediaVolumeEnabled(true, caller);
                            this.mMusicActiveMs = 0;
                            Log.i(TAG, "music is active more than " + UNSAFE_VOLUME_MUSIC_ACTIVE_MS_MAX + " ,so reset to safe volume:" + safeMediaVolumeIndex(device) + " for device:" + device);
                            HwMediaMonitorManager.writeLogMsg(916010205, 1, 0, "OCMA");
                        }
                        saveMusicActiveMs();
                    }
                }
            }
        }
    }

    private void saveMusicActiveMs() {
        this.mAudioHandler.obtainMessage(17, this.mMusicActiveMs, 0).sendToTarget();
    }

    private int getSafeUsbMediaVolumeIndex() {
        int min = MIN_STREAM_VOLUME[3];
        int max = MAX_STREAM_VOLUME[3];
        this.mSafeUsbMediaVolumeDbfs = ((float) this.mContext.getResources().getInteger(17694880)) / 100.0f;
        while (true) {
            if (Math.abs(max - min) <= 1) {
                break;
            }
            int index = (max + min) / 2;
            float gainDB = AudioSystem.getStreamVolumeDB(3, index, (int) DumpState.DUMP_HANDLE);
            if (Float.isNaN(gainDB)) {
                break;
            }
            float f = this.mSafeUsbMediaVolumeDbfs;
            if (gainDB == f) {
                min = index;
                break;
            } else if (gainDB < f) {
                min = index;
            } else {
                max = index;
            }
        }
        return min * 10;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0059  */
    private void onConfigureSafeVolume(boolean force, String caller) {
        boolean safeMediaVolumeEnabled;
        int persistedState;
        synchronized (this.mSafeMediaVolumeStateLock) {
            int mcc = this.mContext.getResources().getConfiguration().mcc;
            if (this.mMcc != mcc || (this.mMcc == 0 && force)) {
                this.mSafeMediaVolumeIndex = this.mContext.getResources().getInteger(17694879) * 10;
                this.mSafeUsbMediaVolumeIndex = getSafeUsbMediaVolumeIndex();
                if (!SystemProperties.getBoolean("audio.safemedia.force", false)) {
                    if (!this.mContext.getResources().getBoolean(17891506)) {
                        safeMediaVolumeEnabled = false;
                        boolean safeMediaVolumeBypass = SystemProperties.getBoolean("audio.safemedia.bypass", false);
                        if (usingHwSafeMediaConfig()) {
                            this.mSafeMediaVolumeIndex = getHwSafeMediaVolumeIndex();
                            safeMediaVolumeEnabled = isHwSafeMediaVolumeEnabled();
                            this.mSafeUsbMediaVolumeIndex = getHwSafeMediaVolumeIndex();
                        }
                        if (usingHwSafeMediaConfig() && this.mHwAudioServiceEx.isHwSafeUsbMediaVolumeEnabled()) {
                            this.mSafeUsbMediaVolumeIndex = this.mHwAudioServiceEx.getHwSafeUsbMediaVolumeIndex();
                        }
                        if (safeMediaVolumeEnabled || safeMediaVolumeBypass) {
                            this.mSafeMediaVolumeState = 1;
                            persistedState = 1;
                        } else {
                            persistedState = 3;
                            if (this.mSafeMediaVolumeState != 2) {
                                if (this.mMusicActiveMs == 0) {
                                    this.mSafeMediaVolumeState = 3;
                                    enforceSafeMediaVolume(caller);
                                } else {
                                    this.mSafeMediaVolumeState = 2;
                                }
                            }
                        }
                        this.mMcc = mcc;
                        sendMsg(this.mAudioHandler, 14, 2, persistedState, 0, null, 0);
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
                sendMsg(this.mAudioHandler, 14, 2, persistedState, 0, null, 0);
            }
        }
    }

    private int checkForRingerModeChange(int oldIndex, int direction, int step, boolean isMuted, String caller, int flags) {
        int result = 1;
        if (isPlatformTelevision() || this.mIsSingleVolume) {
            return 1;
        }
        int ringerMode = getRingerModeInternal();
        if (ringerMode == 0) {
            if (this.mIsSingleVolume && direction == -1 && oldIndex >= step * 2 && isMuted) {
                ringerMode = 2;
            } else if (direction == 1 || direction == 101 || direction == 100) {
                if (!this.mVolumePolicy.volumeUpToExitSilent) {
                    result = 1 | 128;
                } else {
                    ringerMode = (!this.mHasVibrator || direction != 1) ? 2 : 1;
                }
            }
            result &= -2;
        } else if (ringerMode != 1) {
            if (ringerMode != 2) {
                Log.e(TAG, "checkForRingerModeChange() wrong ringer mode: " + ringerMode);
            } else if (direction == -1) {
                if (this.mHasVibrator) {
                    if (step <= oldIndex && oldIndex < step * 2) {
                        ringerMode = 1;
                        this.mLoweredFromNormalToVibrateTime = SystemClock.uptimeMillis();
                    }
                } else if (oldIndex == step && this.mVolumePolicy.volumeDownToEnterSilent) {
                    ringerMode = 0;
                }
            } else if (this.mIsSingleVolume && (direction == 101 || direction == -100)) {
                if (this.mHasVibrator) {
                    ringerMode = 1;
                } else {
                    ringerMode = 0;
                }
                result = 1 & -2;
            }
        } else if (!this.mHasVibrator) {
            Log.e(TAG, "checkForRingerModeChange() current ringer mode is vibratebut no vibrator is present");
        } else {
            if (direction == -1) {
                if (this.mIsSingleVolume && oldIndex >= step * 2 && isMuted) {
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
            }
            result &= -2;
        }
        if (!isAndroidNPlus(caller) || !wouldToggleZenMode(ringerMode) || this.mNm.isNotificationPolicyAccessGrantedForPackage(caller) || (flags & 4096) != 0) {
            setRingerMode(ringerMode, "AS.AudioService.checkForRingerModeChange", false);
            this.mPrevVolDirection = direction;
            return result;
        }
        throw new SecurityException("Not allowed to change Do Not Disturb state");
    }

    public boolean isStreamAffectedByRingerMode(int streamType) {
        return (this.mRingerModeAffectedStreams & (1 << streamType)) != 0;
    }

    private boolean shouldZenMuteStream(int streamType) {
        if (this.mNm.getZenMode() != 1) {
            return false;
        }
        NotificationManager.Policy zenPolicy = this.mNm.getConsolidatedNotificationPolicy();
        return (((zenPolicy.priorityCategories & 32) == 0) && isAlarm(streamType)) || (((zenPolicy.priorityCategories & 64) == 0) && isMedia(streamType)) || ((((zenPolicy.priorityCategories & 128) == 0) && isSystem(streamType)) || (ZenModeConfig.areAllPriorityOnlyNotificationZenSoundsMuted(this.mNm.getConsolidatedNotificationPolicy()) && isNotificationOrRinger(streamType)));
    }

    private boolean isStreamMutedByRingerOrZenMode(int streamType) {
        return (this.mRingerAndZenModeMutedStreams & (1 << streamType)) != 0;
    }

    private boolean updateZenModeAffectedStreams() {
        int zenModeAffectedStreams = 0;
        if (this.mSystemReady && this.mNm.getZenMode() == 1) {
            NotificationManager.Policy zenPolicy = this.mNm.getConsolidatedNotificationPolicy();
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
    /* access modifiers changed from: public */
    @GuardedBy({"mSettingsLock"})
    private boolean updateRingerAndZenModeAffectedStreams() {
        int ringerModeAffectedStreams;
        int ringerModeAffectedStreams2;
        boolean updatedZenModeAffectedStreams = updateZenModeAffectedStreams();
        int ringerModeAffectedStreams3 = Settings.System.getIntForUser(this.mContentResolver, "mode_ringer_streams_affected", 166, -2);
        if (this.mIsSingleVolume) {
            ringerModeAffectedStreams3 = 0;
        } else {
            AudioManagerInternal.RingerModeDelegate ringerModeDelegate = this.mRingerModeDelegate;
            if (ringerModeDelegate != null) {
                ringerModeAffectedStreams3 = ringerModeDelegate.getRingerModeAffectedStreams(ringerModeAffectedStreams3);
            }
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
        if (ringerModeAffectedStreams2 == this.mRingerModeAffectedStreams) {
            return updatedZenModeAffectedStreams;
        }
        Settings.System.putIntForUser(this.mContentResolver, "mode_ringer_streams_affected", ringerModeAffectedStreams2, -2);
        this.mRingerModeAffectedStreams = ringerModeAffectedStreams2;
        return true;
    }

    public boolean isStreamAffectedByMute(int streamType) {
        return (this.mMuteAffectedStreams & (1 << streamType)) != 0;
    }

    private void ensureValidDirection(int direction) {
        if (direction != -100 && direction != -1 && direction != 0 && direction != 1 && direction != 100 && direction != 101) {
            throw new IllegalArgumentException("Bad direction " + direction);
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

    /* access modifiers changed from: package-private */
    public boolean isInCommunication() {
        if (getMode() == 3 || getMode() == 2 || getMode() == 1) {
            return true;
        }
        long ident = Binder.clearCallingIdentity();
        boolean inCall = ((TelecomManager) this.mContext.getSystemService("telecom")).isInCall();
        Binder.restoreCallingIdentity(ident);
        Log.i(TAG, "isInCommunication: isInCall is " + inCall);
        return inCall;
    }

    private boolean wasStreamActiveRecently(int stream, int delay_ms) {
        return AudioSystem.isStreamActive(stream, delay_ms) || AudioSystem.isStreamActiveRemotely(stream, delay_ms);
    }

    private int getActiveStreamType(int suggestedStreamType) {
        if (this.mIsSingleVolume && suggestedStreamType == Integer.MIN_VALUE) {
            return 3;
        }
        if (this.mPlatformType == 1) {
            if (isInCommunication()) {
                return AudioSystem.getForceUse(0) == 3 ? 6 : 0;
            }
            if (suggestedStreamType == Integer.MIN_VALUE) {
                if (wasStreamActiveRecently(0, 0)) {
                    if (AudioSystem.getForceUse(0) == 3) {
                        Log.i(TAG, "getActiveStreamType: Forcing STREAM_BLUETOOTH_SCO");
                        return 6;
                    }
                    Log.i(TAG, "getActiveStreamType: Forcing STREAM_VOICE_CALL stream active");
                    return 0;
                } else if (wasStreamActiveRecently(6, 0)) {
                    Log.i(TAG, "getActiveStreamType: Forcing STREAM_BLUETOOTH_SCO stream active");
                    return 6;
                } else if (wasStreamActiveRecently(2, 0)) {
                    Log.i(TAG, "getActiveStreamType: Forcing STREAM_RING stream active");
                    return 2;
                } else if (wasStreamActiveRecently(4, 0)) {
                    Log.i(TAG, "getActiveStreamType: Forcing STREAM_ALARM stream active");
                    return 4;
                } else if (wasStreamActiveRecently(3, 0)) {
                    Log.i(TAG, "getActiveStreamType: Forcing STREAM_MUSIC b/c default");
                    return 3;
                } else if (wasStreamActiveRecently(5, sStreamOverrideDelayMs)) {
                    Log.i(TAG, "getActiveStreamType: Forcing STREAM_NOTIFICATION stream active");
                    return 5;
                } else if (wasStreamActiveRecently(9, 0)) {
                    Log.i(TAG, "getActiveStreamType: Forcing STREAM_TTS stream active");
                    return 9;
                } else if (wasStreamActiveRecently(1, 0)) {
                    Log.i(TAG, "getActiveStreamType: Forcing STREAM_SYSTEM stream active");
                    return 2;
                } else {
                    Log.i(TAG, "getActiveStreamType: Forcing DEFAULT_VOL_STREAM_NO_PLAYBACK(3) b/c default");
                    return this.mDefaultVolStream;
                }
            } else if (wasStreamActiveRecently(5, sStreamOverrideDelayMs)) {
                Log.i(TAG, "getActiveStreamType: Forcing STREAM_NOTIFICATION stream active");
                return 5;
            } else if (wasStreamActiveRecently(2, sStreamOverrideDelayMs)) {
                Log.i(TAG, "getActiveStreamType: Forcing STREAM_RING stream active");
                return 2;
            }
        }
        if (isInCommunication()) {
            if (AudioSystem.getForceUse(0) == 3) {
                Log.i(TAG, "getActiveStreamType: Forcing STREAM_BLUETOOTH_SCO");
                return 6;
            }
            Log.i(TAG, "getActiveStreamType: Forcing STREAM_VOICE_CALL");
            return 0;
        } else if (AudioSystem.isStreamActive(5, sStreamOverrideDelayMs)) {
            Log.i(TAG, "getActiveStreamType: Forcing STREAM_NOTIFICATION");
            return 5;
        } else if (AudioSystem.isStreamActive(9, sStreamOverrideDelayMs)) {
            Log.i(TAG, "getActiveStreamType: Forcing STREAM_TTS");
            return 9;
        } else if (wasStreamActiveRecently(3, 0)) {
            Log.i(TAG, "getActiveStreamType: Forcing STREAM_MUSIC b/c default");
            return 3;
        } else if (AudioSystem.isStreamActive(2, sStreamOverrideDelayMs)) {
            Log.i(TAG, "getActiveStreamType: Forcing STREAM_RING");
            return 2;
        } else if (suggestedStreamType != Integer.MIN_VALUE) {
            Log.i(TAG, "getActiveStreamType: Returning suggested type " + suggestedStreamType);
            return suggestedStreamType;
        } else if (AudioSystem.isStreamActive(5, sStreamOverrideDelayMs)) {
            Log.i(TAG, "getActiveStreamType: Forcing STREAM_NOTIFICATION");
            return 5;
        } else if (AudioSystem.isStreamActive(2, sStreamOverrideDelayMs)) {
            Log.i(TAG, "getActiveStreamType: Forcing STREAM_RING");
            return 2;
        } else {
            Log.i(TAG, "getActiveStreamType: Forcing DEFAULT_VOL_STREAM_NO_PLAYBACK(3) b/c default");
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
    /* access modifiers changed from: public */
    private void queueMsgUnderWakeLock(Handler handler, int msg, int arg1, int arg2, Object obj, int delay) {
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
        handler.sendMessageAtTime(handler.obtainMessage(msg, arg1, arg2, obj), SystemClock.uptimeMillis() + ((long) delay));
    }

    /* access modifiers changed from: package-private */
    public boolean checkAudioSettingsPermission(String method) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_AUDIO_SETTINGS") == 0) {
            return true;
        }
        Log.w(TAG, "Audio Settings Permission Denial: " + method + " from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
        return false;
    }

    /* access modifiers changed from: package-private */
    public int getDeviceForStream(int stream) {
        int device = getDevicesForStream(stream);
        if (((device - 1) & device) == 0) {
            return device;
        }
        if ((device & 2) != 0) {
            return 2;
        }
        if ((262144 & device) != 0) {
            return DumpState.DUMP_DOMAIN_PREFERRED;
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
    /* access modifiers changed from: public */
    private int getDevicesForStream(int stream) {
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
    /* access modifiers changed from: public */
    private void observeDevicesForStreams(int skipStream) {
        synchronized (VolumeStreamState.class) {
            for (int stream = 0; stream < this.mStreamStates.length; stream++) {
                if (stream != skipStream) {
                    this.mStreamStates[stream].observeDevicesForStream_syncVSS(false);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void postObserveDevicesForAllStreams() {
        sendMsg(this.mAudioHandler, MSG_OBSERVE_DEVICES_FOR_ALL_STREAMS, 2, 0, 0, null, 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onObserveDevicesForAllStreams() {
        observeDevicesForStreams(-1);
    }

    public void setWiredDeviceConnectionState(int type, int state, String address, String name, String caller) {
        if (state == 1 || state == 0) {
            this.mDeviceBroker.setWiredDeviceConnectionState(type, state, address, name, caller);
            updateAftPolicy();
            return;
        }
        throw new IllegalArgumentException("Invalid state " + state);
    }

    public void setBluetoothHearingAidDeviceConnectionState(BluetoothDevice device, int state, boolean suppressNoisyIntent, int musicDevice) {
        if (device == null) {
            throw new IllegalArgumentException("Illegal null device");
        } else if (state == 2 || state == 0) {
            if (state == 2) {
                this.mPlaybackMonitor.registerPlaybackCallback(this.mVoiceActivityMonitor, true);
            } else {
                this.mPlaybackMonitor.unregisterPlaybackCallback(this.mVoiceActivityMonitor);
            }
            this.mDeviceBroker.postBluetoothHearingAidDeviceConnectionState(device, state, suppressNoisyIntent, musicDevice, "AudioService");
        } else {
            throw new IllegalArgumentException("Illegal BluetoothProfile state for device  (dis)connection, got " + state);
        }
    }

    public void setBluetoothA2dpDeviceConnectionStateSuppressNoisyIntent(BluetoothDevice device, int state, int profile, boolean suppressNoisyIntent, int a2dpVolume) {
        if (device == null) {
            throw new IllegalArgumentException("Illegal null device");
        } else if (state == 2 || state == 0) {
            this.mDeviceBroker.postBluetoothA2dpDeviceConnectionStateSuppressNoisyIntent(device, state, profile, suppressNoisyIntent, a2dpVolume);
        } else {
            throw new IllegalArgumentException("Illegal BluetoothProfile state for device  (dis)connection, got " + state);
        }
    }

    public void handleBluetoothA2dpDeviceConfigChange(BluetoothDevice device) {
        if (device != null) {
            this.mDeviceBroker.postBluetoothA2dpDeviceConfigChange(device);
            return;
        }
        throw new IllegalArgumentException("Illegal null device");
    }

    /* access modifiers changed from: package-private */
    public void postAccessoryPlugMediaUnmute(int newDevice) {
        if ((Integer.MIN_VALUE & newDevice) == 0) {
            if ((67108864 & newDevice) != 0) {
                this.mStreamStates[3].initIndex(536870912);
            }
            this.mStreamStates[3].initIndex(newDevice);
        }
        sendMsg(this.mAudioHandler, 21, 2, newDevice, 0, null, 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onAccessoryPlugMediaUnmute(int newDevice) {
        boolean z = false;
        Log.i(TAG, String.format("onAccessoryPlugMediaUnmute newDevice=%d [%s]", Integer.valueOf(newDevice), AudioSystem.getOutputDeviceName(newDevice)));
        if (this.mNm.getZenMode() == 2 || (((DEVICE_MEDIA_UNMUTED_ON_PLUG & newDevice) == 0 && !this.mHwAudioServiceEx.isVirtualAudio(newDevice)) || !this.mStreamStates[3].mIsMuted || this.mStreamStates[3].getIndex(newDevice) == 0 || ((604004352 | newDevice) & AudioSystem.getDevicesForStream(3)) == 0)) {
            StringBuilder sb = new StringBuilder();
            sb.append("onAccessoryPlugMediaUnmute not unmute! ZenMode:");
            if (this.mNm.getZenMode() != 2) {
                z = true;
            }
            sb.append(z);
            sb.append(" ismuted:");
            sb.append(this.mStreamStates[3].mIsMuted);
            sb.append(" index:");
            sb.append(this.mStreamStates[3].getIndex(newDevice));
            sb.append(" DeviceForStream:");
            sb.append(AudioSystem.getDevicesForStream(3));
            Log.i(TAG, sb.toString());
            return;
        }
        Log.i(TAG, String.format(" onAccessoryPlugMediaUnmute unmuting device=%d [%s]", Integer.valueOf(newDevice), AudioSystem.getOutputDeviceName(newDevice)));
        this.mStreamStates[3].mute(false);
    }

    public boolean hasHapticChannels(Uri uri) {
        MediaExtractor extractor = new MediaExtractor();
        try {
            extractor.setDataSource(this.mContext, uri, (Map<String, String>) null);
            for (int i = 0; i < extractor.getTrackCount(); i++) {
                MediaFormat format = extractor.getTrackFormat(i);
                if (format.containsKey("haptic-channel-count") && format.getInteger("haptic-channel-count") > 0) {
                    return true;
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "hasHapticChannels failure:" + e);
        }
        return false;
    }

    /* access modifiers changed from: private */
    public class VolumeStreamState {
        private final SparseIntArray mIndexMap;
        private int mIndexMax;
        private int mIndexMin;
        private boolean mIsMuted;
        private int mObservedDevices;
        private final Intent mStreamDevicesChanged;
        private final int mStreamType;
        private HwCustAudioServiceVolumeStreamState mVSSCust;
        private final Intent mVolumeChanged;
        private String mVolumeIndexSettingName;

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
            int[] iArr = AudioService.mStreamVolumeAlias;
            int i = this.mStreamType;
            if (iArr[i] == i) {
                EventLogTags.writeStreamDevicesChanged(i, prevDevices, devices);
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
        /* access modifiers changed from: public */
        private boolean hasValidSettingsName() {
            String str = this.mVolumeIndexSettingName;
            return str != null && !str.isEmpty();
        }

        public void readSettings() {
            int index;
            synchronized (AudioService.this.mSettingsLock) {
                synchronized (VolumeStreamState.class) {
                    if (AudioService.this.mUseFixedVolume) {
                        this.mIndexMap.put(1073741824, this.mIndexMax);
                        return;
                    } else if (this.mStreamType == 1 || this.mStreamType == 7) {
                        int index2 = AudioSystem.DEFAULT_STREAM_VOLUME[this.mStreamType] * 10;
                        if (AudioService.this.mCameraSoundForced) {
                            index2 = this.mIndexMax;
                        }
                        this.mIndexMap.put(1073741824, index2);
                        return;
                    }
                }
            }
            synchronized (VolumeStreamState.class) {
                int remainingDevices = 1879048191;
                int i = 0;
                while (remainingDevices != 0) {
                    int device = 1 << i;
                    if ((device & remainingDevices) != 0) {
                        remainingDevices &= ~device;
                        int defaultIndex = device == 1073741824 ? AudioSystem.DEFAULT_STREAM_VOLUME[this.mStreamType] : -1;
                        if (!hasValidSettingsName()) {
                            index = defaultIndex;
                        } else {
                            index = Settings.System.getIntForUser(AudioService.this.mContentResolver, getSettingNameForDevice(device), defaultIndex, -2);
                        }
                        if (index != -1) {
                            this.mIndexMap.put(device, getValidIndex(index * 10));
                        } else if (this.mVSSCust != null) {
                            this.mVSSCust.readSettings(this.mStreamType, device);
                        }
                    }
                    i++;
                }
            }
        }

        private int getAbsoluteVolumeIndex(int index) {
            return index == 0 ? index : (this.mIndexMax + 5) / 10;
        }

        private void setStreamVolumeIndex(int index, int device) {
            if (this.mStreamType == 6 && index == 0 && !this.mIsMuted) {
                index = 1;
            }
            AudioSystem.setStreamVolumeIndexAS(this.mStreamType, index, device);
        }

        /* access modifiers changed from: package-private */
        public void applyDeviceVolume_syncVSS(int device, boolean isAvrcpAbsVolSupported) {
            int index;
            boolean isTurnOff = false;
            HwCustAudioServiceVolumeStreamState hwCustAudioServiceVolumeStreamState = this.mVSSCust;
            if (hwCustAudioServiceVolumeStreamState != null) {
                isTurnOff = hwCustAudioServiceVolumeStreamState.isTurnOffAllSound();
            }
            if (this.mIsMuted || isTurnOff) {
                index = 0;
            } else if ((device & 896) != 0 && isAvrcpAbsVolSupported) {
                index = getAbsoluteVolumeIndex((getIndex(device) + 5) / 10);
            } else if ((AudioService.this.mFullVolumeDevices & device) != 0) {
                index = (this.mIndexMax + 5) / 10;
            } else if ((134217728 & device) != 0) {
                index = (this.mIndexMax + 5) / 10;
            } else {
                index = (getIndex(device) + 5) / 10;
            }
            if (!AudioService.isTv || (AudioService.isTv && this.mStreamType == 3)) {
                setStreamVolumeIndex(index, device);
            }
        }

        public void applyAllVolumes() {
            applyAllVolumes(AudioService.this.mDeviceBroker.isAvrcpAbsoluteVolumeSupported());
        }

        /* JADX WARNING: Removed duplicated region for block: B:45:0x00b1  */
        public void applyAllVolumes(boolean isAvrcpAbsVolSupported) {
            int index;
            int index2;
            synchronized (VolumeStreamState.class) {
                boolean isTurnOff = false;
                if (this.mVSSCust != null) {
                    isTurnOff = this.mVSSCust.isTurnOffAllSound();
                }
                int streamTypeAlias = AudioService.mStreamVolumeAlias[this.mStreamType];
                if (!AudioService.isTv || streamTypeAlias == 3) {
                    for (int i = 0; i < this.mIndexMap.size(); i++) {
                        int device = this.mIndexMap.keyAt(i);
                        if (device != 1073741824) {
                            if (!this.mIsMuted) {
                                if (!isTurnOff) {
                                    if ((device & 896) == 0 || !isAvrcpAbsVolSupported) {
                                        if ((AudioService.this.mFullVolumeDevices & device) != 0) {
                                            index2 = (this.mIndexMax + 5) / 10;
                                        } else if ((134217728 & device) != 0) {
                                            index2 = (this.mIndexMax + 5) / 10;
                                        } else {
                                            index2 = (this.mIndexMap.valueAt(i) + 5) / 10;
                                        }
                                        setStreamVolumeIndex(index2, device);
                                    } else {
                                        index2 = getAbsoluteVolumeIndex((getIndex(device) + 5) / 10);
                                        setStreamVolumeIndex(index2, device);
                                    }
                                }
                            }
                            index2 = 0;
                            setStreamVolumeIndex(index2, device);
                        }
                    }
                    if (!this.mIsMuted) {
                        if (!isTurnOff) {
                            index = (getIndex(1073741824) + 5) / 10;
                            setStreamVolumeIndex(index, 1073741824);
                            if (this.mVSSCust != null) {
                                this.mVSSCust.applyAllVolumes(this.mIsMuted, this.mStreamType);
                            }
                            return;
                        }
                    }
                    index = 0;
                    setStreamVolumeIndex(index, 1073741824);
                    if (this.mVSSCust != null) {
                    }
                    return;
                }
                Log.e(AudioService.TAG, "applyAllVolumes mStreamType=" + this.mStreamType + ", streamTypeAlias=" + streamTypeAlias);
            }
        }

        public boolean adjustIndex(int deltaIndex, int device, String caller) {
            return setIndex(getIndex(device) + deltaIndex, device, caller);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void initIndex(int device) {
            synchronized (VolumeStreamState.class) {
                if (this.mIndexMap.get(device, -1) == -1) {
                    if (AudioService.this.mHwAudioServiceEx.isVirtualAudio(device)) {
                        initVirtualAudioIndex(device);
                    } else {
                        int index = AudioSystem.DEFAULT_STREAM_VOLUME[this.mStreamType] * 10;
                        this.mIndexMap.put(device, index);
                        AudioSystem.setStreamVolumeIndexAS(this.mStreamType, (index + 5) / 10, device);
                        Log.i(AudioService.TAG, "initIndex mIndexMap put device " + device + " index " + index + " stream " + this.mStreamType);
                    }
                }
            }
        }

        private void initVirtualAudioIndex(int device) {
            int index = getMaxIndex();
            this.mIndexMap.put(device, index);
            Log.i(AudioService.TAG, "initIndex mIndexMap.put device " + device + " index " + index + " stream " + this.mStreamType);
            for (int i = 0; i < AudioSystem.getNumStreamTypes(); i++) {
                AudioService.this.postSetVolumeIndexOnDevice(i, index, device, "DEVICE_OUT_PROXY");
            }
        }

        public boolean setIndex(int index, int device, String caller) {
            int oldIndex;
            int index2;
            boolean changed;
            synchronized (AudioService.this.mSettingsLock) {
                synchronized (VolumeStreamState.class) {
                    oldIndex = getIndex(device);
                    index2 = getValidIndex(index);
                    if (this.mStreamType == 7 && AudioService.this.mCameraSoundForced) {
                        index2 = this.mIndexMax;
                    }
                    this.mIndexMap.put(device, index2);
                    if (device == 1073741824) {
                        Log.w(AudioService.TAG, "setIndex default device index " + index2 + " stream " + this.mStreamType + " caller " + caller);
                    }
                    boolean isCurrentDevice = true;
                    changed = oldIndex != index2;
                    if (device != AudioService.this.getDeviceForStream(this.mStreamType)) {
                        isCurrentDevice = false;
                    }
                    for (int streamType = AudioSystem.getNumStreamTypes() - 1; streamType >= 0; streamType--) {
                        if (!AudioService.isTv || AudioService.mStreamVolumeAlias[streamType] == 3) {
                            VolumeStreamState aliasStreamState = AudioService.this.mStreamStates[streamType];
                            if (streamType != this.mStreamType && AudioService.mStreamVolumeAlias[streamType] == this.mStreamType && (changed || !aliasStreamState.hasIndexForDevice(device))) {
                                int scaledIndex = AudioService.this.rescaleIndex(index2, this.mStreamType, streamType);
                                aliasStreamState.setIndex(scaledIndex, device, caller);
                                if (isCurrentDevice) {
                                    aliasStreamState.setIndex(scaledIndex, AudioService.this.getDeviceForStream(streamType), caller);
                                }
                            }
                        } else {
                            Log.i(AudioService.TAG, "streamType is not supported: " + streamType);
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
            }
            if (changed) {
                int oldIndex2 = (oldIndex + 5) / 10;
                int index3 = (index2 + 5) / 10;
                int[] iArr = AudioService.mStreamVolumeAlias;
                int i2 = this.mStreamType;
                if (iArr[i2] == i2) {
                    if (caller == null) {
                        Log.w(AudioService.TAG, "No caller for volume_changed event", new Throwable());
                    }
                    EventLogTags.writeVolumeChanged(this.mStreamType, oldIndex2, index3, this.mIndexMax / 10, caller);
                }
                if (!AudioService.isTv || (AudioService.isTv && this.mStreamType == 3)) {
                    this.mVolumeChanged.putExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", index3);
                    this.mVolumeChanged.putExtra("android.media.EXTRA_PREV_VOLUME_STREAM_VALUE", oldIndex2);
                    this.mVolumeChanged.putExtra("android.media.EXTRA_VOLUME_STREAM_TYPE_ALIAS", AudioService.mStreamVolumeAlias[this.mStreamType]);
                    AudioService.this.sendBroadcastToAll(this.mVolumeChanged);
                }
            }
            return changed;
        }

        public int getIndex(int device) {
            int index;
            synchronized (VolumeStreamState.class) {
                index = this.mIndexMap.get(device, -1);
                if (index == -1) {
                    index = device == 33554432 ? getMaxIndex() : this.mIndexMap.get(1073741824);
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

        /* JADX INFO: Multiple debug info for r3v3 android.util.SparseIntArray: [D('i' int), D('srcMap' android.util.SparseIntArray)] */
        @GuardedBy({"VolumeStreamState.class"})
        public void setAllIndexes(VolumeStreamState srcStream, String caller) {
            int[] iArr = AudioService.mStreamVolumeAlias;
            int i = this.mStreamType;
            int streamTypeAlias = iArr[i];
            if (i == srcStream.mStreamType || (AudioService.isTv && streamTypeAlias != 3)) {
                Log.i(AudioService.TAG, "mStreamType: " + this.mStreamType + "  return");
                return;
            }
            int srcStreamType = srcStream.getStreamType();
            int index = AudioService.this.rescaleIndex(srcStream.getIndex(1073741824), srcStreamType, this.mStreamType);
            for (int i2 = 0; i2 < this.mIndexMap.size(); i2++) {
                SparseIntArray sparseIntArray = this.mIndexMap;
                sparseIntArray.put(sparseIntArray.keyAt(i2), index);
            }
            SparseIntArray srcMap = srcStream.mIndexMap;
            for (int i3 = 0; i3 < srcMap.size(); i3++) {
                setIndex(AudioService.this.rescaleIndex(srcMap.valueAt(i3), srcStreamType, this.mStreamType), srcMap.keyAt(i3), caller);
            }
        }

        @GuardedBy({"VolumeStreamState.class"})
        public void setAllIndexesToMax() {
            for (int i = 0; i < this.mIndexMap.size(); i++) {
                SparseIntArray sparseIntArray = this.mIndexMap;
                sparseIntArray.put(sparseIntArray.keyAt(i), this.mIndexMax);
            }
        }

        public void mute(boolean state) {
            boolean changed = false;
            synchronized (VolumeStreamState.class) {
                if (state != this.mIsMuted) {
                    changed = true;
                    this.mIsMuted = state;
                    AudioService.sendMsg(AudioService.this.mAudioHandler, 10, 2, 0, 0, this, 0);
                    if (AudioService.isTv) {
                        AudioService.sendMsg(AudioService.this.mAudioHandler, 31, 2, this.mIsMuted ? 1 : 0, this.mStreamType, this, 0);
                    }
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
            boolean isAvrcpAbsVolSupported = AudioService.this.mDeviceBroker.isAvrcpAbsoluteVolumeSupported();
            synchronized (VolumeStreamState.class) {
                if (AudioService.mStreamVolumeAlias[this.mStreamType] == 3) {
                    for (int i = 0; i < this.mIndexMap.size(); i++) {
                        int device = this.mIndexMap.keyAt(i);
                        int index = this.mIndexMap.valueAt(i);
                        if (!((AudioService.this.mFullVolumeDevices & device) == 0 && ((AudioService.this.mFixedVolumeDevices & device) == 0 || index == 0))) {
                            this.mIndexMap.put(device, this.mIndexMax);
                        }
                        applyDeviceVolume_syncVSS(device, isAvrcpAbsVolSupported);
                    }
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int getValidIndex(int index) {
            int i = this.mIndexMin;
            if (index < i) {
                return i;
            }
            if (AudioService.this.mUseFixedVolume || index > this.mIndexMax) {
                return this.mIndexMax;
            }
            return index;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void dump(PrintWriter pw) {
            String deviceName;
            pw.print("   Muted: ");
            pw.println(this.mIsMuted);
            pw.print("   Min: ");
            pw.println((this.mIndexMin + 5) / 10);
            pw.print("   Max: ");
            pw.println((this.mIndexMax + 5) / 10);
            pw.print("   streamVolume:");
            pw.println(AudioService.this.getStreamVolume(this.mStreamType));
            pw.print("   Current: ");
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
            int n = 0;
            while (true) {
                int device2 = 1 << i2;
                if (device2 != 1073741824) {
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

    /* access modifiers changed from: private */
    public class AudioSystemThread extends Thread {
        AudioSystemThread() {
            super("AudioService");
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            Looper.prepare();
            synchronized (AudioService.this) {
                AudioService.this.mAudioHandler = new AudioHandler();
                AudioService.this.notify();
            }
            Looper.loop();
        }
    }

    /* access modifiers changed from: private */
    public static final class DeviceVolumeUpdate {
        private static final int NO_NEW_INDEX = -2049;
        final String mCaller;
        final int mDevice;
        final int mStreamType;
        private final int mVssVolIndex;

        DeviceVolumeUpdate(int streamType, int vssVolIndex, int device, String caller) {
            this.mStreamType = streamType;
            this.mVssVolIndex = vssVolIndex;
            this.mDevice = device;
            this.mCaller = caller;
        }

        DeviceVolumeUpdate(int streamType, int device, String caller) {
            this.mStreamType = streamType;
            this.mVssVolIndex = NO_NEW_INDEX;
            this.mDevice = device;
            this.mCaller = caller;
        }

        /* access modifiers changed from: package-private */
        public boolean hasVolumeIndex() {
            return this.mVssVolIndex != NO_NEW_INDEX;
        }

        /* access modifiers changed from: package-private */
        public int getVolumeIndex() throws IllegalStateException {
            Preconditions.checkState(this.mVssVolIndex != NO_NEW_INDEX);
            return this.mVssVolIndex;
        }
    }

    /* access modifiers changed from: package-private */
    public void postSetVolumeIndexOnDevice(int streamType, int vssVolIndex, int device, String caller) {
        this.mHwAudioServiceEx.dispatchVolumeChange(device, streamType, caller, (vssVolIndex + 5) / 10);
        sendMsg(this.mAudioHandler, 26, 2, 0, 0, new DeviceVolumeUpdate(streamType, vssVolIndex, device, caller), 0);
    }

    /* access modifiers changed from: package-private */
    public void postApplyVolumeOnDevice(int streamType, int device, String caller) {
        sendMsg(this.mAudioHandler, 26, 2, 0, 0, new DeviceVolumeUpdate(streamType, device, caller), 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onSetVolumeIndexOnDevice(DeviceVolumeUpdate update) {
        VolumeStreamState streamState = this.mStreamStates[update.mStreamType];
        if (update.hasVolumeIndex()) {
            int index = update.getVolumeIndex();
            streamState.setIndex(index, update.mDevice, update.mCaller);
            AudioEventLogger audioEventLogger = sVolumeLogger;
            audioEventLogger.log(new AudioEventLogger.StringEvent(update.mCaller + " dev:0x" + Integer.toHexString(update.mDevice) + " volIdx:" + index));
        } else {
            AudioEventLogger audioEventLogger2 = sVolumeLogger;
            audioEventLogger2.log(new AudioEventLogger.StringEvent(update.mCaller + " update vol on dev:0x" + Integer.toHexString(update.mDevice)));
        }
        setDeviceVolume(streamState, update.mDevice);
    }

    /* access modifiers changed from: package-private */
    public void setDeviceVolume(VolumeStreamState streamState, int device) {
        boolean isAvrcpAbsVolSupported = this.mDeviceBroker.isAvrcpAbsoluteVolumeSupported();
        synchronized (VolumeStreamState.class) {
            streamState.applyDeviceVolume_syncVSS(device, isAvrcpAbsVolSupported);
            for (int streamType = AudioSystem.getNumStreamTypes() - 1; streamType >= 0; streamType--) {
                if (streamType != streamState.mStreamType && mStreamVolumeAlias[streamType] == streamState.mStreamType) {
                    int streamDevice = getDeviceForStream(streamType);
                    if (!(device == streamDevice || !isAvrcpAbsVolSupported || (device & 896) == 0)) {
                        this.mStreamStates[streamType].applyDeviceVolume_syncVSS(device, isAvrcpAbsVolSupported);
                    }
                    this.mStreamStates[streamType].applyDeviceVolume_syncVSS(streamDevice, isAvrcpAbsVolSupported);
                }
            }
        }
        if (isTv) {
            sendMsg(this.mAudioHandler, 1, 2, device, 0, streamState, 0);
        } else {
            sendMsg(this.mAudioHandler, 1, 2, device, 0, streamState, 500);
        }
    }

    /* access modifiers changed from: private */
    public class AudioHandler extends Handler {
        private AudioHandler() {
        }

        private void setAllVolumes(VolumeStreamState streamState) {
            streamState.applyAllVolumes();
            for (int streamType = AudioSystem.getNumStreamTypes() - 1; streamType >= 0; streamType--) {
                if (streamType != streamState.mStreamType && AudioService.mStreamVolumeAlias[streamType] == streamState.mStreamType && (!AudioService.isTv || (AudioService.isTv && streamType == 3))) {
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

        private boolean onLoadSoundEffects() {
            int status;
            synchronized (AudioService.this.mSoundEffectsLock) {
                if (!AudioService.this.mSystemReady) {
                    Log.w(AudioService.TAG, "onLoadSoundEffects() called before boot complete");
                    return false;
                } else if (AudioService.this.mSoundPool != null) {
                    return true;
                } else {
                    AudioService.this.loadTouchSoundAssets();
                    AudioService.this.mSoundPool = new SoundPool.Builder().setMaxStreams(4).setAudioAttributes(new AudioAttributes.Builder().setUsage(13).setContentType(4).build()).build();
                    AudioService.this.mSoundPoolCallBack = null;
                    AudioService.this.mSoundPoolListenerThread = new SoundPoolListenerThread();
                    AudioService.this.mSoundPoolListenerThread.start();
                    int attempts = 3;
                    while (true) {
                        if (AudioService.this.mSoundPoolCallBack != null) {
                            break;
                        }
                        int attempts2 = attempts - 1;
                        if (attempts <= 0) {
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
                            AudioService.this.mSoundPoolLooper = null;
                        }
                        AudioService.this.mSoundPoolListenerThread = null;
                        AudioService.this.mSoundPool.release();
                        AudioService.this.mSoundPool = null;
                        return false;
                    }
                    int[] poolId = new int[AudioService.SOUND_EFFECT_FILES.size()];
                    for (int fileIdx = 0; fileIdx < AudioService.SOUND_EFFECT_FILES.size(); fileIdx++) {
                        poolId[fileIdx] = -1;
                    }
                    int numSamples = 0;
                    for (int effect = 0; effect < 10; effect++) {
                        if (AudioService.this.SOUND_EFFECT_FILES_MAP[effect][1] != 0) {
                            if (poolId[AudioService.this.SOUND_EFFECT_FILES_MAP[effect][0]] == -1) {
                                int sampleId = AudioService.this.mSoundPool.load(getSoundEffectFilePath(effect), 0);
                                if (sampleId <= 0) {
                                    Log.w(AudioService.TAG, "Soundpool could not load file");
                                } else {
                                    AudioService.this.SOUND_EFFECT_FILES_MAP[effect][1] = sampleId;
                                    poolId[AudioService.this.SOUND_EFFECT_FILES_MAP[effect][0]] = sampleId;
                                    numSamples++;
                                }
                            } else {
                                AudioService.this.SOUND_EFFECT_FILES_MAP[effect][1] = poolId[AudioService.this.SOUND_EFFECT_FILES_MAP[effect][0]];
                            }
                        }
                    }
                    if (numSamples > 0) {
                        AudioService.this.mSoundPoolCallBack.setSamples(poolId);
                        int attempts3 = 3;
                        status = 1;
                        while (true) {
                            if (status != 1) {
                                break;
                            }
                            int attempts4 = attempts3 - 1;
                            if (attempts3 <= 0) {
                                break;
                            }
                            try {
                                AudioService.this.mSoundEffectsLock.wait(5000);
                                status = AudioService.this.mSoundPoolCallBack.status();
                                attempts3 = attempts4;
                            } catch (InterruptedException e2) {
                                Log.w(AudioService.TAG, "Interrupted while waiting sound pool callback.");
                                attempts3 = attempts4;
                            }
                        }
                    } else {
                        status = -1;
                    }
                    if (AudioService.this.mSoundPoolLooper != null) {
                        AudioService.this.mSoundPoolLooper.quit();
                        AudioService.this.mSoundPoolLooper = null;
                    }
                    AudioService.this.mSoundPoolListenerThread = null;
                    if (status != 0) {
                        Log.w(AudioService.TAG, "onLoadSoundEffects(), Error " + status + " while loading samples");
                        for (int effect2 = 0; effect2 < 10; effect2++) {
                            if (AudioService.this.SOUND_EFFECT_FILES_MAP[effect2][1] > 0) {
                                AudioService.this.SOUND_EFFECT_FILES_MAP[effect2][1] = -1;
                            }
                        }
                        AudioService.this.mSoundPool.release();
                        AudioService.this.mSoundPool = null;
                    }
                }
            }
            if (status == 0) {
                return true;
            }
            return false;
        }

        private void onUnloadSoundEffects() {
            synchronized (AudioService.this.mSoundEffectsLock) {
                if (AudioService.this.mSoundPool != null) {
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
                    AudioService.this.mSoundPool = null;
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
                                /* class com.android.server.audio.AudioService.AudioHandler.AnonymousClass1 */

                                @Override // android.media.MediaPlayer.OnCompletionListener
                                public void onCompletion(MediaPlayer mp) {
                                    AudioHandler.this.cleanupPlayer(mp);
                                }
                            });
                            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                                /* class com.android.server.audio.AudioService.AudioHandler.AnonymousClass2 */

                                @Override // android.media.MediaPlayer.OnErrorListener
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
        /* access modifiers changed from: public */
        private void cleanupPlayer(MediaPlayer mp) {
            if (mp != null) {
                try {
                    mp.stop();
                    mp.release();
                } catch (IllegalStateException ex) {
                    Log.w(AudioService.TAG, "MediaPlayer IllegalStateException: " + ex);
                }
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

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != 0) {
                boolean z = true;
                if (i == 1) {
                    persistVolume((VolumeStreamState) msg.obj, msg.arg1);
                } else if (i == 3) {
                    persistRingerMode(AudioService.this.getRingerModeInternal());
                } else if (i == 4) {
                    AudioService.this.onAudioServerDied();
                } else if (i != 5) {
                    int i2 = 0;
                    boolean isMuted = false;
                    if (i == 7) {
                        boolean loaded = onLoadSoundEffects();
                        if (msg.obj != null) {
                            LoadSoundEffectReply reply = (LoadSoundEffectReply) msg.obj;
                            synchronized (reply) {
                                if (!loaded) {
                                    i2 = -1;
                                }
                                reply.mStatus = i2;
                                reply.notify();
                            }
                        }
                    } else if (i == 8) {
                        String eventSource = (String) msg.obj;
                        int useCase = msg.arg1;
                        int config = msg.arg2;
                        if (useCase == 1) {
                            AudioService.this.mDeviceBroker.postReportNewRoutes();
                        }
                        AudioService.sForceUseLogger.log(new AudioServiceEvents.ForceUseEvent(useCase, config, eventSource));
                        AudioSystem.setForceUse(useCase, config);
                        AudioService.this.updateAftPolicy();
                    } else if (i != 100) {
                        switch (i) {
                            case 10:
                                setAllVolumes((VolumeStreamState) msg.obj);
                                return;
                            case 11:
                                if (AudioService.this.mIsChineseZone || !AudioService.this.mHasAlarm) {
                                    AudioService.this.onCheckMusicActive((String) msg.obj);
                                    return;
                                }
                                return;
                            case 12:
                            case 13:
                                AudioService audioService = AudioService.this;
                                if (msg.what != 13) {
                                    z = false;
                                }
                                audioService.onConfigureSafeVolume(z, (String) msg.obj);
                                return;
                            case 14:
                                onPersistSafeVolumeState(msg.arg1);
                                return;
                            case 15:
                                onUnloadSoundEffects();
                                return;
                            case 16:
                                AudioService.this.onSystemReady();
                                return;
                            case 17:
                                Settings.Secure.putIntForUser(AudioService.this.mContentResolver, "unsafe_volume_music_active_ms", msg.arg1, -2);
                                return;
                            case 18:
                                AudioService.this.onUnmuteStream(msg.arg1, msg.arg2);
                                return;
                            case 19:
                                AudioService.this.onDynPolicyMixStateUpdate((String) msg.obj, msg.arg1);
                                return;
                            case 20:
                                AudioService.this.onIndicateSystemReady();
                                return;
                            case 21:
                                AudioService.this.onAccessoryPlugMediaUnmute(msg.arg1);
                                return;
                            case 22:
                                onNotifyVolumeEvent((IAudioPolicyCallback) msg.obj, msg.arg1);
                                return;
                            case AudioService.MSG_DISPATCH_AUDIO_SERVER_STATE /* 23 */:
                                AudioService audioService2 = AudioService.this;
                                if (msg.arg1 != 1) {
                                    z = false;
                                }
                                audioService2.onDispatchAudioServerStateChange(z);
                                return;
                            case AudioService.MSG_ENABLE_SURROUND_FORMATS /* 24 */:
                                AudioService.this.onEnableSurroundFormats((ArrayList) msg.obj);
                                return;
                            case AudioService.MSG_UPDATE_RINGER_MODE /* 25 */:
                                AudioService.this.onUpdateRingerModeServiceInt();
                                return;
                            case 26:
                                AudioService.this.onSetVolumeIndexOnDevice((DeviceVolumeUpdate) msg.obj);
                                return;
                            case AudioService.MSG_OBSERVE_DEVICES_FOR_ALL_STREAMS /* 27 */:
                                AudioService.this.onObserveDevicesForAllStreams();
                                return;
                            case AudioService.MSG_HDMI_VOLUME_CHECK /* 28 */:
                                AudioService.this.onCheckVolumeCecOnHdmiConnection(msg.arg1, (String) msg.obj);
                                return;
                            case 29:
                                AudioService.this.onPlaybackConfigChange((List) msg.obj);
                                return;
                            case HdmiCecKeycode.CEC_KEYCODE_NUMBER_11 /* 30 */:
                                AudioService.this.onCheckAbsVolumeState();
                                return;
                            case 31:
                                if (msg.arg1 == 1) {
                                    isMuted = true;
                                }
                                int streamType = msg.arg2;
                                AudioService audioService3 = AudioService.this;
                                int i3 = audioService3.mMutedStreams;
                                int i4 = 1 << streamType;
                                audioService3.mMutedStreams = isMuted ? i4 | i3 : (~i4) & i3;
                                Log.i(AudioService.TAG, "isMuted " + isMuted + ", streamType " + streamType + ", mMutedStreams " + AudioService.this.mMutedStreams);
                                Settings.System.putIntForUser(AudioService.this.mContentResolver, "muted_streams", AudioService.this.mMutedStreams, -2);
                                return;
                            default:
                                AudioService.this.handleMessageEx(msg);
                                return;
                        }
                    } else {
                        PlaybackActivityMonitor playbackActivityMonitor = AudioService.this.mPlaybackMonitor;
                        if (msg.arg1 != 1) {
                            z = false;
                        }
                        playbackActivityMonitor.disableAudioForUid(z, msg.arg2);
                        AudioService.this.mAudioEventWakeLock.release();
                    }
                } else {
                    onPlaySoundEffect(msg.arg1, msg.arg2);
                    Jlog.d(175, msg.arg1, "playSoundEffect end");
                }
            } else {
                AudioService.this.setDeviceVolume((VolumeStreamState) msg.obj, msg.arg1);
            }
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
            AudioService.this.mContentResolver.registerContentObserver(Settings.System.getUriFor("master_balance"), false, this);
            AudioService.this.mEncodedSurroundMode = Settings.Global.getInt(AudioService.this.mContentResolver, "encoded_surround_output", 0);
            AudioService.this.mContentResolver.registerContentObserver(Settings.Global.getUriFor("encoded_surround_output"), false, this);
            AudioService.this.mEnabledSurroundFormats = Settings.Global.getString(AudioService.this.mContentResolver, "encoded_surround_output_enabled_formats");
            AudioService.this.mContentResolver.registerContentObserver(Settings.Global.getUriFor("encoded_surround_output_enabled_formats"), false, this);
            AudioService.this.mContentResolver.registerContentObserver(Settings.Secure.getUriFor("voice_interaction_service"), false, this);
            AudioService.this.mContentResolver.registerContentObserver(Settings.Secure.getUriFor("rtt_calling_mode"), false, this);
            AudioService.this.mContentResolver.registerContentObserver(Settings.System.getUriFor(AudioService.DEFAULT_VOLUME_KEY_CTL), false, this, -1);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            synchronized (AudioService.this.mSettingsLock) {
                if (AudioService.this.updateRingerAndZenModeAffectedStreams()) {
                    AudioService.this.setRingerModeInt(AudioService.this.getRingerModeInternal(), false);
                }
                AudioService.this.readDockAudioSettings(AudioService.this.mContentResolver);
                AudioService.this.updateMasterMono(AudioService.this.mContentResolver);
                AudioService.this.updateMasterBalance(AudioService.this.mContentResolver);
                updateEncodedSurroundOutput();
                AudioService.this.sendEnabledSurroundFormats(AudioService.this.mContentResolver, AudioService.this.mSurroundModeChanged);
                AudioService.this.updateAssistantUId(false);
                AudioService.this.updateRttEanbled(AudioService.this.mContentResolver);
                AudioService.this.updateDefaultStream();
            }
        }

        private void updateEncodedSurroundOutput() {
            int newSurroundMode = Settings.Global.getInt(AudioService.this.mContentResolver, "encoded_surround_output", 0);
            if (AudioService.this.mEncodedSurroundMode != newSurroundMode) {
                AudioService.this.sendEncodedSurroundMode(newSurroundMode, "SettingsObserver");
                AudioService.this.mDeviceBroker.toggleHdmiIfConnected_Async();
                AudioService.this.mEncodedSurroundMode = newSurroundMode;
                AudioService.this.mSurroundModeChanged = true;
                return;
            }
            AudioService.this.mSurroundModeChanged = false;
        }
    }

    public void avrcpSupportsAbsoluteVolume(String address, boolean support) {
        AudioEventLogger audioEventLogger = sVolumeLogger;
        audioEventLogger.log(new AudioEventLogger.StringEvent("avrcpSupportsAbsoluteVolume addr=" + address + " support=" + support));
        this.mDeviceBroker.setAvrcpAbsoluteVolumeSupported(support);
        sendMsg(this.mAudioHandler, 0, 2, 128, 0, this.mStreamStates[3], 0);
    }

    /* access modifiers changed from: package-private */
    public boolean hasMediaDynamicPolicy() {
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

    /* access modifiers changed from: package-private */
    public void checkMusicActive(int deviceType, String caller) {
        if ((603979788 & deviceType) != 0) {
            sendMsg(this.mAudioHandler, 11, 0, 0, 0, caller, MUSIC_ACTIVE_POLL_PERIOD_MS);
        }
    }

    private class AudioServiceBroadcastReceiver extends BroadcastReceiver {
        private AudioServiceBroadcastReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            int config;
            String action = intent.getAction();
            if (action.equals("android.intent.action.DOCK_EVENT")) {
                int dockState = intent.getIntExtra("android.intent.extra.DOCK_STATE", 0);
                if (dockState == 1) {
                    config = 7;
                } else if (dockState == 2) {
                    config = 6;
                } else if (dockState == 3) {
                    config = 8;
                } else if (dockState != 4) {
                    config = 0;
                } else {
                    config = 9;
                }
                if (!(dockState == 3 || (dockState == 0 && AudioService.this.mDockState == 3))) {
                    AudioService.this.mDeviceBroker.setForceUse_Async(3, config, "ACTION_DOCK_EVENT intent");
                }
                AudioService.this.mDockState = dockState;
            } else if (action.equals("android.bluetooth.headset.profile.action.ACTIVE_DEVICE_CHANGED") || action.equals("android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED")) {
                AudioService.this.mDeviceBroker.receiveBtEvent(intent);
            } else if (action.equals("android.intent.action.SCREEN_ON")) {
                if (AudioService.this.mMonitorRotation) {
                    RotationHelper.enable();
                }
                AudioSystem.setParameters("screen_state=on");
                if (!AudioService.this.mIsChineseZone && AudioService.this.mHasAlarm) {
                    AudioService.this.mAlarmManager.cancel(AudioService.this.mPendingIntent);
                    AudioService.this.mHasAlarm = false;
                    AudioService.sendMsg(AudioService.this.mAudioHandler, 11, 0, 0, 0, AudioService.this.mSafeVolumeCaller, 0);
                }
            } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                if (AudioService.this.mMonitorRotation) {
                    RotationHelper.disable();
                }
                AudioSystem.setParameters("screen_state=off");
                if (!AudioService.this.mIsChineseZone && AudioService.this.mAudioHandler.hasMessages(11) && AudioSystem.isStreamActive(3, (int) AudioService.CHECK_MUSIC_ACTIVE_DELAY_MS)) {
                    AudioService.this.mActiveCheckRetryCount = 10;
                    AudioService.this.setCheckMusicActiveAlarm();
                }
            } else if (!action.equals("android.intent.action.USER_PRESENT") || !AbsAudioService.SPK_RCV_STEREO_SUPPORT) {
                if (action.equals("android.intent.action.CONFIGURATION_CHANGED")) {
                    AudioService.this.handleConfigurationChanged(context);
                } else if (action.equals("android.intent.action.USER_SWITCHED")) {
                    if (AudioService.this.mUserSwitchedReceived) {
                        AudioService.this.mDeviceBroker.postBroadcastBecomingNoisy();
                    }
                    AudioService.this.mUserSwitchedReceived = true;
                    AudioService.this.mMediaFocusControl.discardAudioFocusOwner();
                    AudioService.this.readAudioSettings(true);
                    AudioService.sendMsg(AudioService.this.mAudioHandler, 10, 2, 0, 0, AudioService.this.mStreamStates[3], 0);
                } else if (action.equals("android.intent.action.FM")) {
                    AudioService.this.mHwAudioServiceEx.setFmDeviceAvailable(intent.getIntExtra("state", 0), false);
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
                    int state = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", -1);
                    if (state == 10 || state == 13) {
                        AudioService.this.mDeviceBroker.disconnectAllBluetoothProfiles();
                    }
                } else if (action.equals("android.media.action.OPEN_AUDIO_EFFECT_CONTROL_SESSION") || action.equals("android.media.action.CLOSE_AUDIO_EFFECT_CONTROL_SESSION")) {
                    AudioService.this.handleAudioEffectBroadcast(context, intent);
                } else if (action.equals("android.intent.action.PACKAGES_SUSPENDED")) {
                    int[] suspendedUids = intent.getIntArrayExtra("android.intent.extra.changed_uid_list");
                    String[] suspendedPackages = intent.getStringArrayExtra("android.intent.extra.changed_package_list");
                    if (!(suspendedPackages == null || suspendedUids == null || suspendedPackages.length != suspendedUids.length)) {
                        for (int i = 0; i < suspendedUids.length; i++) {
                            if (!TextUtils.isEmpty(suspendedPackages[i])) {
                                AudioService.this.mMediaFocusControl.noFocusForSuspendedApp(suspendedPackages[i], suspendedUids[i]);
                            }
                        }
                    }
                } else if (!AudioService.this.mIsChineseZone && action.equals(AudioService.ACTION_CHECK_MUSIC_ACTIVE)) {
                    boolean isActive = AudioSystem.isStreamActive(3, (int) AudioService.CHECK_MUSIC_ACTIVE_DELAY_MS);
                    if (isActive || (!isActive && AudioService.access$7910(AudioService.this) > 0)) {
                        Log.w(AudioService.TAG, "ACTION_CHECK_MUSIC isActive: " + isActive + ",retry count:" + AudioService.this.mActiveCheckRetryCount);
                        if (isActive) {
                            AudioService.this.mActiveCheckRetryCount = 10;
                        }
                        AudioService audioService = AudioService.this;
                        audioService.onCheckMusicActive(audioService.mSafeVolumeCaller);
                        AudioService.this.setCheckMusicActiveAlarm();
                    }
                } else if (action.equals(AudioService.ACTION_CHANGE_ACTIVE_DEVICE)) {
                    AudioService.this.mActiveDevice = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_DEVICES", 1073741824);
                    Log.i(AudioService.TAG, "change active device new device = " + AudioService.this.mActiveDevice);
                } else if (action.equals("android.intent.action.LOCALE_CHANGED")) {
                    int state2 = AudioSystem.setParameters(AudioService.LOCATE_CHANGED_PARAMETER);
                    if (state2 != 0) {
                        Log.e(AudioService.TAG, "LOCATE_CHANGED_PARAMETER action process fail. state = " + state2);
                        return;
                    }
                    Log.i(AudioService.TAG, "ACTION_LOCALE_CHANGED received");
                }
            } else if (AudioService.this.mMonitorRotation) {
                RotationHelper.updateOrientation();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setCheckMusicActiveAlarm() {
        this.mAlarmManager.cancel(this.mPendingIntent);
        this.mAlarmManager.setExact(0, Calendar.getInstance().getTimeInMillis() + 60000, this.mPendingIntent);
        this.mHasAlarm = true;
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAudioEffectBroadcast(Context context, Intent intent) {
        ResolveInfo ri;
        String target = intent.getPackage();
        if (target != null) {
            Log.w(TAG, "effect broadcast already targeted to " + target);
            return;
        }
        intent.addFlags(32);
        List<ResolveInfo> ril = context.getPackageManager().queryBroadcastReceivers(intent, 0);
        if (ril == null || ril.size() == 0 || (ri = ril.get(0)) == null || ri.activityInfo == null || ri.activityInfo.packageName == null) {
            Log.w(TAG, "couldn't find receiver package for effect intent");
            return;
        }
        intent.setPackage(ri.activityInfo.packageName);
        context.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void killBackgroundUserProcessesWithRecordAudioPermission(UserInfo oldUser) {
        PackageManager pm = this.mContext.getPackageManager();
        ComponentName homeActivityName = null;
        if (!oldUser.isManagedProfile()) {
            homeActivityName = ((ActivityTaskManagerInternal) LocalServices.getService(ActivityTaskManagerInternal.class)).getHomeActivityForUser(oldUser.id);
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

    private boolean forceFocusDuckingForAccessibility(AudioAttributes aa, int request, int uid) {
        Bundle extraInfo;
        if (aa == null || aa.getUsage() != 11 || request != 3 || (extraInfo = aa.getBundle()) == null || !extraInfo.getBoolean("a11y_force_ducking")) {
            return false;
        }
        if (uid == 0) {
            return true;
        }
        synchronized (this.mAccessibilityServiceUidsLock) {
            if (this.mAccessibilityServiceUids != null) {
                int callingUid = Binder.getCallingUid();
                for (int i = 0; i < this.mAccessibilityServiceUids.length; i++) {
                    if (this.mAccessibilityServiceUids[i] == callingUid) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public int requestAudioFocus(AudioAttributes aa, int durationHint, IBinder cb, IAudioFocusDispatcher fd, String clientId, String callingPackageName, int flags, IAudioPolicyCallback pcb, int sdk) {
        sendBehavior(IHwBehaviorCollectManager.BehaviorId.AUDIO_REQUESTAUDIOFOCUS, new Object[0]);
        if ((flags & 4) == 4) {
            if (!"AudioFocus_For_Phone_Ring_And_Calls".equals(clientId)) {
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
        }
        if (callingPackageName != null && clientId != null) {
            if (aa != null) {
                return this.mMediaFocusControl.requestAudioFocus(aa, durationHint, cb, fd, clientId, callingPackageName, flags, sdk, forceFocusDuckingForAccessibility(aa, durationHint, Binder.getCallingUid()));
            }
        }
        Log.e(TAG, "Invalid null parameter to request audio focus");
        return 0;
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

    /* access modifiers changed from: package-private */
    public boolean hasAudioFocusUsers() {
        return this.mMediaFocusControl.hasAudioFocusUsers();
    }

    private boolean readCameraSoundForced() {
        if (SystemProperties.getBoolean("audio.camerasound.force", false) || this.mContext.getResources().getBoolean(17891386)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleConfigurationChanged(Context context) {
        try {
            Configuration config = context.getResources().getConfiguration();
            sendMsg(this.mAudioHandler, 12, 0, 0, 0, TAG, 0);
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
                    AudioDeviceBroker audioDeviceBroker = this.mDeviceBroker;
                    if (cameraSoundForced) {
                        i = 11;
                    }
                    audioDeviceBroker.setForceUse_Async(4, i, "handleConfigurationChanged");
                    sendMsg(this.mAudioHandler, 10, 2, 0, 0, this.mStreamStates[7], 0);
                }
            }
            this.mVolumeController.setLayoutDirection(config.getLayoutDirection());
        } catch (Exception e) {
            Log.e(TAG, "Error handling configuration change: ", e);
        }
    }

    public void setRingtonePlayer(IRingtonePlayer player) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.REMOTE_AUDIO_PLAYBACK", null);
        this.mRingtonePlayer = player;
    }

    public IRingtonePlayer getRingtonePlayer() {
        return this.mRingtonePlayer;
    }

    public AudioRoutesInfo startWatchingRoutes(IAudioRoutesObserver observer) {
        return this.mDeviceBroker.startWatchingRoutes(observer);
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
        synchronized (this.mSafeMediaVolumeStateLock) {
            if (!(this.mSafeMediaVolumeState == 0 || this.mSafeMediaVolumeState == 1)) {
                if (on && this.mSafeMediaVolumeState == 2) {
                    this.mSafeMediaVolumeState = 3;
                    enforceSafeMediaVolume(caller);
                } else if (!on && this.mSafeMediaVolumeState == 3) {
                    this.mSafeMediaVolumeState = 2;
                    this.mMusicActiveMs = 1;
                    saveMusicActiveMs();
                    sendMsg(this.mAudioHandler, 11, 0, 0, 0, caller, MUSIC_ACTIVE_POLL_PERIOD_MS);
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
            if ((device & devices) == 0) {
                i = i2;
            } else {
                if (streamState.getIndex(device) > safeMediaVolumeIndex(device)) {
                    streamState.setIndex(safeMediaVolumeIndex(device), device, caller);
                    sendMsg(this.mAudioHandler, 0, 2, device, 0, streamState, 0);
                }
                devices &= ~device;
                i = i2;
            }
        }
    }

    private boolean checkSafeMediaVolume(int streamType, int index, int device) {
        synchronized (this.mSafeMediaVolumeStateLock) {
            if (this.mFactoryMode || this.mSafeMediaVolumeState != 3 || mStreamVolumeAlias[streamType] != 3 || (603979788 & device) == 0 || index <= safeMediaVolumeIndex(device)) {
                return true;
            }
            return false;
        }
    }

    public void disableSafeMediaVolume(String callingPackage) {
        enforceVolumeController("disable the safe media volume");
        synchronized (this.mSafeMediaVolumeStateLock) {
            setSafeMediaVolumeEnabled(false, callingPackage);
            if (this.mPendingVolumeCommand != null) {
                onSetStreamVolume(this.mPendingVolumeCommand.mStreamType, this.mPendingVolumeCommand.mIndex, this.mPendingVolumeCommand.mFlags, this.mPendingVolumeCommand.mDevice, callingPackage);
                this.mPendingVolumeCommand = null;
            }
        }
    }

    public int getSafeMediaVolumeIndex(int streamType) {
        int safeVolumeIndex = 0;
        if (this.mContext.checkCallingOrSelfPermission("android.permission.CAPTURE_AUDIO_OUTPUT") != 0) {
            Log.w(TAG, "Trying to call getSafeMediaVolumeIndex() without CAPTURE_AUDIO_OUTPUT");
            return 0;
        }
        if (streamType >= 0) {
            int[] iArr = mStreamVolumeAlias;
            if (streamType < iArr.length) {
                int streamTypeAlias = iArr[streamType];
                int device = getDeviceForStream(streamTypeAlias);
                synchronized (this.mSafeMediaVolumeStateLock) {
                    if (!this.mFactoryMode && this.mSafeMediaVolumeState == 3 && streamTypeAlias == 3 && (603979788 & device) != 0) {
                        safeVolumeIndex = safeMediaVolumeIndex(device);
                    }
                }
                return safeVolumeIndex;
            }
        }
        Log.w(TAG, "Trying to call getSafeMediaVolumeIndex() with wrong stream type");
        return 0;
    }

    /* access modifiers changed from: private */
    public class MyDisplayStatusCallback implements HdmiPlaybackClient.DisplayStatusCallback {
        private MyDisplayStatusCallback() {
        }

        public void onComplete(int status) {
            synchronized (AudioService.this.mHdmiClientLock) {
                if (AudioService.this.mHdmiManager != null) {
                    AudioService.this.mHdmiCecSink = status != -1;
                    if (AudioService.this.mHdmiCecSink) {
                        Log.i(AudioService.TAG, "CEC sink: setting HDMI as full vol device");
                        AudioService.this.mFullVolumeDevices |= 1024;
                    } else {
                        Log.i(AudioService.TAG, "TV, no CEC: setting HDMI as regular vol device");
                        AudioService.this.mFullVolumeDevices &= -1025;
                    }
                    AudioService.this.checkAddAllFixedVolumeDevices(1024, "HdmiPlaybackClient.DisplayStatusCallback");
                }
            }
        }
    }

    public int setHdmiSystemAudioSupported(boolean on) {
        int config;
        int device = 0;
        synchronized (this.mHdmiClientLock) {
            if (this.mHdmiManager != null) {
                if (this.mHdmiTvClient == null && this.mHdmiAudioSystemClient == null) {
                    Log.w(TAG, "Only Hdmi-Cec enabled TV or audio system device supportssystem audio mode.");
                    return 0;
                }
                if (this.mHdmiSystemAudioSupported != on) {
                    this.mHdmiSystemAudioSupported = on;
                    if (on) {
                        config = 12;
                    } else {
                        config = 0;
                    }
                    this.mDeviceBroker.setForceUse_Async(5, config, "setHdmiSystemAudioSupported");
                }
                device = getDevicesForStream(3);
            }
            return device;
        }
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

    @Override // android.view.accessibility.AccessibilityManager.TouchExplorationStateChangeListener
    public void onTouchExplorationStateChanged(boolean enabled) {
        updateDefaultStreamOverrideDelay(enabled);
    }

    private void updateDefaultStreamOverrideDelay(boolean touchExploreEnabled) {
        if (touchExploreEnabled) {
            sStreamOverrideDelayMs = 1000;
        } else {
            sStreamOverrideDelayMs = 0;
        }
        Log.i(TAG, "Touch exploration enabled=" + touchExploreEnabled + " stream override delay is now " + sStreamOverrideDelayMs + " ms");
    }

    public void onAccessibilityServicesStateChanged(AccessibilityManager accessibilityManager) {
        updateA11yVolumeAlias(accessibilityManager.isAccessibilityVolumeStreamActive());
    }

    private void updateA11yVolumeAlias(boolean a11VolEnabled) {
        Log.i(TAG, "Accessibility volume enabled = " + a11VolEnabled);
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
            pw.println(Integer.toHexString(this.mDeviceBroker.getCurAudioRoutes().mainType));
            pw.print("  mBluetoothName=");
            pw.println(this.mDeviceBroker.getCurAudioRoutes().bluetoothName);
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
            pw.print("  mMcc=");
            pw.println(this.mMcc);
            pw.print("  mCameraSoundForced=");
            pw.println(this.mCameraSoundForced);
            pw.print("  mHasVibrator=");
            pw.println(this.mHasVibrator);
            pw.print("  mVolumePolicy=");
            pw.println(this.mVolumePolicy);
            pw.print("  mAvrcpAbsVolSupported=");
            pw.println(this.mDeviceBroker.isAvrcpAbsoluteVolumeSupported());
            pw.print("  mIsSingleVolume=");
            pw.println(this.mIsSingleVolume);
            pw.print("  mUseFixedVolume=");
            pw.println(this.mUseFixedVolume);
            pw.print("  mFixedVolumeDevices=0x");
            pw.println(Integer.toHexString(this.mFixedVolumeDevices));
            pw.print("  mHdmiCecSink=");
            pw.println(this.mHdmiCecSink);
            pw.print("  mHdmiAudioSystemClient=");
            pw.println(this.mHdmiAudioSystemClient);
            pw.print("  mHdmiPlaybackClient=");
            pw.println(this.mHdmiPlaybackClient);
            pw.print("  mHdmiTvClient=");
            pw.println(this.mHdmiTvClient);
            pw.print("  mHdmiSystemAudioSupported=");
            pw.println(this.mHdmiSystemAudioSupported);
            pw.print("  UNSAFE_VOLUME_MUSIC_ACTIVE_MS_MAX=");
            pw.println(UNSAFE_VOLUME_MUSIC_ACTIVE_MS_MAX);
            dumpAudioPolicies(pw);
            this.mDynPolicyLogger.dump(pw);
            this.mPlaybackMonitor.dump(pw);
            this.mRecordMonitor.dump(pw);
            pw.println("\n");
            pw.println("\nEvent logs:");
            this.mModeLogger.dump(pw);
            pw.println("\n");
            sDeviceLogger.dump(pw);
            pw.println("\n");
            sForceUseLogger.dump(pw);
            pw.println("\n");
            sVolumeLogger.dump(pw);
            dumpAudioMode(pw);
        }
    }

    private static String safeMediaVolumeStateToString(int state) {
        if (state == 0) {
            return "SAFE_MEDIA_VOLUME_NOT_CONFIGURED";
        }
        if (state == 1) {
            return "SAFE_MEDIA_VOLUME_DISABLED";
        }
        if (state == 2) {
            return "SAFE_MEDIA_VOLUME_INACTIVE";
        }
        if (state != 3) {
            return null;
        }
        return "SAFE_MEDIA_VOLUME_ACTIVE";
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
                        /* class com.android.server.audio.AudioService.AnonymousClass4 */

                        @Override // android.os.IBinder.DeathRecipient
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
            Log.i(TAG, "Volume controller: " + this.mVolumeController);
        }
    }

    public void notifyVolumeControllerVisible(IVolumeController controller, boolean visible) {
        enforceVolumeController("notify about volume controller visibility");
        if (this.mVolumeController.isSameBinder(controller)) {
            this.mVolumeController.setVisible(visible);
            Log.i(TAG, "Volume controller visible: " + visible);
        }
    }

    public void setVolumePolicy(VolumePolicy policy) {
        enforceVolumeController("set volume policy");
        if (policy != null && !policy.equals(this.mVolumePolicy)) {
            this.mVolumePolicy = policy;
            Log.i(TAG, "Volume policy changed: " + this.mVolumePolicy);
        }
    }

    public static class VolumeController {
        private static final String TAG = "VolumeController";
        private IVolumeController mController;
        private int mLongPressTimeout;
        private long mNextLongPress;
        private boolean mVisible;
        private IVrManager mVrManager;

        public void setController(IVolumeController controller) {
            this.mController = controller;
            this.mVisible = false;
        }

        public void loadSettings(ContentResolver cr) {
            this.mLongPressTimeout = Settings.Secure.getIntForUser(cr, "long_press_timeout", 500, -2);
        }

        public boolean suppressAdjustment(int resolvedStream, int flags, boolean isMute) {
            if (isMute || resolvedStream != 2 || this.mController == null) {
                return false;
            }
            long now = SystemClock.uptimeMillis();
            if ((flags & 1) == 0 || this.mVisible) {
                long j = this.mNextLongPress;
                if (j <= 0) {
                    return false;
                }
                if (now <= j) {
                    return true;
                }
                this.mNextLongPress = 0;
                return false;
            }
            if (this.mNextLongPress < now) {
                this.mNextLongPress = ((long) this.mLongPressTimeout) + now;
            }
            return true;
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
            if (this.mController != null && !HwFrameworkFactory.getVRSystemServiceManager().isVRMode()) {
                try {
                    this.mController.displaySafeVolumeWarning(flags | 1);
                } catch (RemoteException e3) {
                    Log.w(TAG, "Error calling displaySafeVolumeWarning", e3);
                }
            }
        }

        public void postVolumeChanged(int streamType, int flags) {
            if (this.mController == null || HwFrameworkFactory.getVRSystemServiceManager().isVRMode()) {
                Log.i(TAG, "is Vr mode");
                return;
            }
            try {
                Log.i(TAG, "call systemui volumeChanged");
                this.mController.volumeChanged(streamType, flags);
            } catch (RemoteException e) {
                Log.w(TAG, "Error calling volumeChanged", e);
            }
        }

        public void postMasterMuteChanged(int flags) {
            if (this.mController != null && !HwFrameworkFactory.getVRSystemServiceManager().isVRMode()) {
                try {
                    this.mController.masterMuteChanged(flags);
                } catch (RemoteException e) {
                    Log.w(TAG, "Error calling masterMuteChanged", e);
                }
            }
        }

        public void setLayoutDirection(int layoutDirection) {
            if (this.mController != null && !HwFrameworkFactory.getVRSystemServiceManager().isVRMode()) {
                try {
                    this.mController.setLayoutDirection(layoutDirection);
                } catch (RemoteException e) {
                    Log.w(TAG, "Error calling setLayoutDirection", e);
                }
            }
        }

        public void postDismiss() {
            IVolumeController iVolumeController = this.mController;
            if (iVolumeController != null) {
                try {
                    iVolumeController.dismiss();
                } catch (RemoteException e) {
                    Log.w(TAG, "Error calling dismiss", e);
                }
            }
        }

        public void setA11yMode(int a11yMode) {
            IVolumeController iVolumeController = this.mController;
            if (iVolumeController != null) {
                try {
                    iVolumeController.setA11yMode(a11yMode);
                } catch (RemoteException e) {
                    Log.w(TAG, "Error calling setA11Mode", e);
                }
            }
        }
    }

    final class AudioServiceInternal extends AudioManagerInternal {
        AudioServiceInternal() {
        }

        public void setRingerModeDelegate(AudioManagerInternal.RingerModeDelegate delegate) {
            AudioService.this.mRingerModeDelegate = delegate;
            if (AudioService.this.mRingerModeDelegate != null) {
                synchronized (AudioService.this.mSettingsLock) {
                    AudioService.this.updateRingerAndZenModeAffectedStreams();
                }
                setRingerModeInternal(getRingerModeInternal(), "AS.AudioService.setRingerModeDelegate");
            }
        }

        public void adjustSuggestedStreamVolumeForUid(int streamType, int direction, int flags, String callingPackage, int uid) {
            AudioService.this.adjustSuggestedStreamVolume(direction, streamType, flags, callingPackage, callingPackage, uid);
        }

        public void adjustStreamVolumeForUid(int streamType, int direction, int flags, String callingPackage, int uid) {
            if (direction != 0) {
                AudioEventLogger audioEventLogger = AudioService.sVolumeLogger;
                audioEventLogger.log(new AudioServiceEvents.VolumeEvent(5, streamType, direction, flags, callingPackage + " uid:" + uid));
            }
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
                    AudioService.this.mAccessibilityServiceUids = null;
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
                                AudioService.this.mAccessibilityServiceUids = uids.toArray();
                            }
                        }
                    }
                    changed = true;
                    if (!changed) {
                    }
                    if (changed) {
                    }
                }
                AudioSystem.setA11yServicesUids(AudioService.this.mAccessibilityServiceUids);
            }
        }
    }

    public String registerAudioPolicy(AudioPolicyConfig policyConfig, IAudioPolicyCallback pcb, boolean hasFocusListener, boolean isFocusPolicy, boolean isTestFocusPolicy, boolean isVolumeController, IMediaProjection projection) {
        HashMap<IBinder, AudioPolicyProxy> hashMap;
        RemoteException e;
        IllegalStateException e2;
        AudioSystem.setDynamicPolicyCallback(this.mDynPolicyCallback);
        if (!isPolicyRegisterAllowed(policyConfig, isFocusPolicy || isTestFocusPolicy || hasFocusListener, isVolumeController, projection)) {
            Slog.w(TAG, "Permission denied to register audio policy for pid " + Binder.getCallingPid() + " / uid " + Binder.getCallingUid() + ", need MODIFY_AUDIO_ROUTING or MediaProjection that can project audio");
            return null;
        }
        AudioEventLogger audioEventLogger = this.mDynPolicyLogger;
        audioEventLogger.log(new AudioEventLogger.StringEvent("registerAudioPolicy for " + pcb.asBinder() + " with config:" + policyConfig).printLog(TAG));
        HashMap<IBinder, AudioPolicyProxy> hashMap2 = this.mAudioPolicies;
        synchronized (hashMap2) {
            try {
                if (this.mAudioPolicies.containsKey(pcb.asBinder())) {
                    Slog.e(TAG, "Cannot re-register policy");
                } else {
                    try {
                        hashMap = hashMap2;
                        try {
                            AudioPolicyProxy app = new AudioPolicyProxy(policyConfig, pcb, hasFocusListener, isFocusPolicy, isTestFocusPolicy, isVolumeController, projection);
                            pcb.asBinder().linkToDeath(app, 0);
                            String regId = app.getRegistrationId();
                            this.mAudioPolicies.put(pcb.asBinder(), app);
                            return regId;
                        } catch (RemoteException e3) {
                            e = e3;
                        } catch (IllegalStateException e4) {
                            e2 = e4;
                            Slog.w(TAG, "Audio policy registration failed for binder " + pcb, e2);
                            return null;
                        } catch (Throwable th) {
                            e = th;
                            throw e;
                        }
                    } catch (RemoteException e5) {
                        e = e5;
                        hashMap = hashMap2;
                        Slog.w(TAG, "Audio policy registration failed, could not link to " + pcb + " binder death", e);
                        return null;
                    } catch (IllegalStateException e6) {
                        e2 = e6;
                        hashMap = hashMap2;
                        Slog.w(TAG, "Audio policy registration failed for binder " + pcb, e2);
                        return null;
                    }
                }
            } catch (Throwable th2) {
                e = th2;
                hashMap = hashMap2;
                throw e;
            }
        }
        return null;
    }

    private boolean isPolicyRegisterAllowed(AudioPolicyConfig policyConfig, boolean hasFocusAccess, boolean isVolumeController, IMediaProjection projection) {
        boolean requireValidProjection = false;
        boolean requireCaptureAudioOrMediaOutputPerm = false;
        boolean requireModifyRouting = false;
        if (hasFocusAccess || isVolumeController) {
            requireModifyRouting = false | true;
        } else if (policyConfig.getMixes().isEmpty()) {
            requireModifyRouting = false | true;
        }
        Iterator it = policyConfig.getMixes().iterator();
        while (it.hasNext()) {
            AudioMix mix = (AudioMix) it.next();
            if (mix.getRule().allowPrivilegedPlaybackCapture()) {
                requireCaptureAudioOrMediaOutputPerm |= true;
                String error = AudioMix.canBeUsedForPrivilegedCapture(mix.getFormat());
                if (error != null) {
                    Log.e(TAG, error);
                    return false;
                }
            }
            if (mix.getRouteFlags() != 3 || projection == null) {
                requireModifyRouting |= true;
            } else {
                requireValidProjection |= true;
            }
        }
        if (requireCaptureAudioOrMediaOutputPerm && !callerHasPermission("android.permission.CAPTURE_MEDIA_OUTPUT") && !callerHasPermission("android.permission.CAPTURE_AUDIO_OUTPUT")) {
            Log.e(TAG, "Privileged audio capture requires CAPTURE_MEDIA_OUTPUT or CAPTURE_AUDIO_OUTPUT system permission");
            return false;
        } else if (requireValidProjection && !canProjectAudio(projection)) {
            return false;
        } else {
            if (!requireModifyRouting || callerHasPermission("android.permission.MODIFY_AUDIO_ROUTING")) {
                return true;
            }
            Log.e(TAG, "Can not capture audio without MODIFY_AUDIO_ROUTING");
            return false;
        }
    }

    private boolean callerHasPermission(String permission) {
        return this.mContext.checkCallingPermission(permission) == 0;
    }

    private boolean canProjectAudio(IMediaProjection projection) {
        if (projection == null) {
            Log.e(TAG, "MediaProjection is null");
            return false;
        }
        IMediaProjectionManager projectionService = getProjectionService();
        if (projectionService == null) {
            Log.e(TAG, "Can't get service IMediaProjectionManager");
            return false;
        }
        try {
            if (!projectionService.isValidMediaProjection(projection)) {
                Log.w(TAG, "App passed invalid MediaProjection token");
                return false;
            }
            try {
                if (projection.canProjectAudio()) {
                    return true;
                }
                Log.w(TAG, "App passed MediaProjection that can not project audio");
                return false;
            } catch (RemoteException e) {
                Log.e(TAG, "Can't call .canProjectAudio() on valid IMediaProjection" + projection.asBinder(), e);
                return false;
            }
        } catch (RemoteException e2) {
            Log.e(TAG, "Can't call .isValidMediaProjection() on IMediaProjectionManager" + projectionService.asBinder(), e2);
            return false;
        }
    }

    private IMediaProjectionManager getProjectionService() {
        if (this.mProjectionService == null) {
            this.mProjectionService = IMediaProjectionManager.Stub.asInterface(ServiceManager.getService("media_projection"));
        }
        return this.mProjectionService;
    }

    public void unregisterAudioPolicyAsync(IAudioPolicyCallback pcb) {
        unregisterAudioPolicy(pcb);
    }

    public void unregisterAudioPolicy(IAudioPolicyCallback pcb) {
        if (pcb != null) {
            unregisterAudioPolicyInt(pcb);
        }
    }

    private void unregisterAudioPolicyInt(IAudioPolicyCallback pcb) {
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

    @GuardedBy({"mAudioPolicies"})
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
        Log.i(TAG, "addMixForPolicy for " + pcb.asBinder() + " with config:" + policyConfig);
        synchronized (this.mAudioPolicies) {
            AudioPolicyProxy app = checkUpdateForPolicy(pcb, "Cannot add AudioMix in audio policy");
            int i = -1;
            if (app == null) {
                return -1;
            }
            if (app.addMixes(policyConfig.getMixes()) == 0) {
                i = 0;
            }
            return i;
        }
    }

    public int removeMixForPolicy(AudioPolicyConfig policyConfig, IAudioPolicyCallback pcb) {
        Log.i(TAG, "removeMixForPolicy for " + pcb.asBinder() + " with config:" + policyConfig);
        synchronized (this.mAudioPolicies) {
            AudioPolicyProxy app = checkUpdateForPolicy(pcb, "Cannot add AudioMix in audio policy");
            int i = -1;
            if (app == null) {
                return -1;
            }
            if (app.removeMixes(policyConfig.getMixes()) == 0) {
                i = 0;
            }
            return i;
        }
    }

    public int setUidDeviceAffinity(IAudioPolicyCallback pcb, int uid, int[] deviceTypes, String[] deviceAddresses) {
        Log.i(TAG, "setUidDeviceAffinity for " + pcb.asBinder() + " uid:" + uid);
        synchronized (this.mAudioPolicies) {
            AudioPolicyProxy app = checkUpdateForPolicy(pcb, "Cannot change device affinity in audio policy");
            if (app == null) {
                return -1;
            }
            if (!app.hasMixRoutedToDevices(deviceTypes, deviceAddresses)) {
                return -1;
            }
            return app.setUidDeviceAffinities(uid, deviceTypes, deviceAddresses);
        }
    }

    public int removeUidDeviceAffinity(IAudioPolicyCallback pcb, int uid) {
        Log.i(TAG, "removeUidDeviceAffinity for " + pcb.asBinder() + " uid:" + uid);
        synchronized (this.mAudioPolicies) {
            AudioPolicyProxy app = checkUpdateForPolicy(pcb, "Cannot remove device affinity in audio policy");
            if (app == null) {
                return -1;
            }
            return app.removeUidDeviceAffinities(uid);
        }
    }

    public int setFocusPropertiesForPolicy(int duckingBehavior, IAudioPolicyCallback pcb) {
        Log.i(TAG, "setFocusPropertiesForPolicy() duck behavior=" + duckingBehavior + " policy " + pcb.asBinder());
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

    public boolean hasRegisteredDynamicPolicy() {
        boolean z;
        synchronized (this.mAudioPolicies) {
            z = !this.mAudioPolicies.isEmpty();
        }
        return z;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setExtVolumeController(IAudioPolicyCallback apc) {
        if (!this.mContext.getResources().getBoolean(17891465)) {
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
    /* access modifiers changed from: public */
    private void onDynPolicyMixStateUpdate(String regId, int state) {
        Log.i(TAG, "onDynamicPolicyMixStateUpdate(" + regId + ", " + state + ")");
        synchronized (this.mAudioPolicies) {
            for (AudioPolicyProxy policy : this.mAudioPolicies.values()) {
                Iterator it = policy.getMixes().iterator();
                while (true) {
                    if (it.hasNext()) {
                        if (((AudioMix) it.next()).getRegistration().equals(regId)) {
                            try {
                                policy.mPolicyCallback.notifyMixStateUpdate(regId, state);
                            } catch (RemoteException e) {
                                Log.e(TAG, "Can't call notifyMixStateUpdate() on IAudioPolicyCallback " + policy.mPolicyCallback.asBinder(), e);
                            }
                            return;
                        }
                    }
                }
            }
        }
    }

    public void registerRecordingCallback(IRecordingConfigDispatcher rcdb) {
        boolean isPrivileged = this.mContext.checkCallingPermission("android.permission.MODIFY_AUDIO_ROUTING") == 0;
        if (Binder.getCallingUid() == 1000) {
            isPrivileged = true;
        }
        this.mRecordMonitor.registerRecordingCallback(rcdb, isPrivileged);
    }

    public void unregisterRecordingCallback(IRecordingConfigDispatcher rcdb) {
        this.mRecordMonitor.unregisterRecordingCallback(rcdb);
    }

    public List<AudioRecordingConfiguration> getActiveRecordingConfigurations() {
        return this.mRecordMonitor.getActiveRecordingConfigurations(this.mContext.checkCallingPermission("android.permission.MODIFY_AUDIO_ROUTING") == 0);
    }

    public int trackRecorder(IBinder recorder) {
        return this.mRecordMonitor.trackRecorder(recorder);
    }

    public void recorderEvent(int riid, int event) {
        this.mRecordMonitor.recorderEvent(riid, event);
    }

    public void releaseRecorder(int riid) {
        this.mRecordMonitor.releaseRecorder(riid);
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

    /* access modifiers changed from: private */
    public static final class AudioDeviceArray {
        final String[] mDeviceAddresses;
        final int[] mDeviceTypes;

        AudioDeviceArray(int[] types, String[] addresses) {
            this.mDeviceTypes = types;
            this.mDeviceAddresses = addresses;
        }
    }

    public class AudioPolicyProxy extends AudioPolicyConfig implements IBinder.DeathRecipient {
        private static final String TAG = "AudioPolicyProxy";
        int mFocusDuckBehavior = 0;
        final boolean mHasFocusListener;
        boolean mIsFocusPolicy = false;
        boolean mIsTestFocusPolicy = false;
        final boolean mIsVolumeController;
        final IAudioPolicyCallback mPolicyCallback;
        final IMediaProjection mProjection;
        UnregisterOnStopCallback mProjectionCallback;
        final HashMap<Integer, AudioDeviceArray> mUidDeviceAffinities = new HashMap<>();

        /* access modifiers changed from: private */
        public final class UnregisterOnStopCallback extends IMediaProjectionCallback.Stub {
            private UnregisterOnStopCallback() {
            }

            public void onStop() {
                AudioService.this.unregisterAudioPolicyAsync(AudioPolicyProxy.this.mPolicyCallback);
            }
        }

        AudioPolicyProxy(AudioPolicyConfig config, IAudioPolicyCallback token, boolean hasFocusListener, boolean isFocusPolicy, boolean isTestFocusPolicy, boolean isVolumeController, IMediaProjection projection) {
            super(config);
            setRegistration(new String(config.hashCode() + ":ap:" + AudioService.access$9808(AudioService.this)));
            this.mPolicyCallback = token;
            this.mHasFocusListener = hasFocusListener;
            this.mIsVolumeController = isVolumeController;
            this.mProjection = projection;
            if (this.mHasFocusListener) {
                AudioService.this.mMediaFocusControl.addFocusFollower(this.mPolicyCallback);
                if (isFocusPolicy) {
                    this.mIsFocusPolicy = true;
                    this.mIsTestFocusPolicy = isTestFocusPolicy;
                    AudioService.this.mMediaFocusControl.setFocusPolicy(this.mPolicyCallback, this.mIsTestFocusPolicy);
                }
            }
            if (this.mIsVolumeController) {
                AudioService.this.setExtVolumeController(this.mPolicyCallback);
            }
            if (this.mProjection != null) {
                this.mProjectionCallback = new UnregisterOnStopCallback();
                try {
                    this.mProjection.registerCallback(this.mProjectionCallback);
                } catch (RemoteException e) {
                    release();
                    throw new IllegalStateException("MediaProjection callback registration failed, could not link to " + projection + " binder death", e);
                }
            }
            int status = connectMixes();
            if (status != 0) {
                release();
                throw new IllegalStateException("Could not connect mix, error: " + status);
            }
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            synchronized (AudioService.this.mAudioPolicies) {
                Log.i(TAG, "audio policy " + this.mPolicyCallback + " died");
                release();
                AudioService.this.mAudioPolicies.remove(this.mPolicyCallback.asBinder());
            }
            if (this.mIsVolumeController) {
                synchronized (AudioService.this.mExtVolumeControllerLock) {
                    AudioService.this.mExtVolumeController = null;
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
                AudioService.this.mMediaFocusControl.unsetFocusPolicy(this.mPolicyCallback, this.mIsTestFocusPolicy);
            }
            if (this.mFocusDuckBehavior == 1) {
                AudioService.this.mMediaFocusControl.setDuckingInExtPolicyAvailable(false);
            }
            if (this.mHasFocusListener) {
                AudioService.this.mMediaFocusControl.removeFocusFollower(this.mPolicyCallback);
            }
            UnregisterOnStopCallback unregisterOnStopCallback = this.mProjectionCallback;
            if (unregisterOnStopCallback != null) {
                try {
                    this.mProjection.unregisterCallback(unregisterOnStopCallback);
                } catch (RemoteException e) {
                    Log.e(TAG, "Fail to unregister Audiopolicy callback from MediaProjection");
                }
            }
            int uid = Binder.getCallingUid();
            long identity = Binder.clearCallingIdentity();
            AudioSystem.registerPolicyMixes(this.mMixes, false, AudioService.this.mHwAudioServiceEx.isSystemApp(uid), uid);
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
        public boolean hasMixRoutedToDevices(int[] deviceTypes, String[] deviceAddresses) {
            for (int i = 0; i < deviceTypes.length; i++) {
                boolean hasDevice = false;
                Iterator it = this.mMixes.iterator();
                while (true) {
                    if (it.hasNext()) {
                        if (((AudioMix) it.next()).isRoutedToDevice(deviceTypes[i], deviceAddresses[i])) {
                            hasDevice = true;
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if (!hasDevice) {
                    return false;
                }
            }
            return true;
        }

        /* access modifiers changed from: package-private */
        public int addMixes(ArrayList<AudioMix> mixes) {
            int registerPolicyMixes;
            synchronized (this.mMixes) {
                int uid = Binder.getCallingUid();
                boolean isSystemApp = AudioService.this.mHwAudioServiceEx.isSystemApp(uid);
                AudioSystem.registerPolicyMixes(this.mMixes, false, isSystemApp, uid);
                add(mixes);
                registerPolicyMixes = AudioSystem.registerPolicyMixes(this.mMixes, true, isSystemApp, uid);
            }
            return registerPolicyMixes;
        }

        /* access modifiers changed from: package-private */
        public int removeMixes(ArrayList<AudioMix> mixes) {
            int registerPolicyMixes;
            synchronized (this.mMixes) {
                int uid = Binder.getCallingUid();
                boolean isSystemApp = AudioService.this.mHwAudioServiceEx.isSystemApp(uid);
                AudioSystem.registerPolicyMixes(this.mMixes, false, isSystemApp, uid);
                remove(mixes);
                registerPolicyMixes = AudioSystem.registerPolicyMixes(this.mMixes, true, isSystemApp, uid);
            }
            return registerPolicyMixes;
        }

        /* access modifiers changed from: package-private */
        public int connectMixes() {
            int uid = Binder.getCallingUid();
            long identity = Binder.clearCallingIdentity();
            int status = AudioSystem.registerPolicyMixes(this.mMixes, true, AudioService.this.mHwAudioServiceEx.isSystemApp(uid), uid);
            Binder.restoreCallingIdentity(identity);
            return status;
        }

        /* access modifiers changed from: package-private */
        public int setUidDeviceAffinities(int uid, int[] types, String[] addresses) {
            Integer Uid = new Integer(uid);
            if (this.mUidDeviceAffinities.remove(Uid) != null) {
                long identity = Binder.clearCallingIdentity();
                int res = AudioSystem.removeUidDeviceAffinities(uid);
                Binder.restoreCallingIdentity(identity);
                if (res != 0) {
                    Log.e(TAG, "AudioSystem. removeUidDeviceAffinities(" + uid + ") failed,  cannot call AudioSystem.setUidDeviceAffinities");
                    return -1;
                }
            }
            long identity2 = Binder.clearCallingIdentity();
            int res2 = AudioSystem.setUidDeviceAffinities(uid, types, addresses);
            Binder.restoreCallingIdentity(identity2);
            if (res2 == 0) {
                this.mUidDeviceAffinities.put(Uid, new AudioDeviceArray(types, addresses));
                return 0;
            }
            Log.e(TAG, "AudioSystem. setUidDeviceAffinities(" + uid + ") failed");
            return -1;
        }

        /* access modifiers changed from: package-private */
        public int removeUidDeviceAffinities(int uid) {
            if (this.mUidDeviceAffinities.remove(new Integer(uid)) != null) {
                long identity = Binder.clearCallingIdentity();
                int res = AudioSystem.removeUidDeviceAffinities(uid);
                Binder.restoreCallingIdentity(identity);
                if (res == 0) {
                    return 0;
                }
            }
            Log.e(TAG, "AudioSystem. removeUidDeviceAffinities failed");
            return -1;
        }

        public String toLogFriendlyString() {
            String textDump = (AudioService.super.toLogFriendlyString() + " Proxy:\n") + "   is focus policy= " + this.mIsFocusPolicy + "\n";
            if (this.mIsFocusPolicy) {
                textDump = ((textDump + "     focus duck behaviour= " + this.mFocusDuckBehavior + "\n") + "     is test focus policy= " + this.mIsTestFocusPolicy + "\n") + "     has focus listener= " + this.mHasFocusListener + "\n";
            }
            return textDump + "   media projection= " + this.mProjection + "\n";
        }
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

    /* access modifiers changed from: private */
    public class AsdProxy implements IBinder.DeathRecipient {
        private final IAudioServerStateDispatcher mAsd;

        AsdProxy(IAudioServerStateDispatcher asd) {
            this.mAsd = asd;
        }

        @Override // android.os.IBinder.DeathRecipient
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

    public void notifySendBroadcastForKaraoke(int uid) {
        Slog.w(TAG, "notifySendBroadcastForKaraoke");
        IHwAudioServiceEx iHwAudioServiceEx = this.mHwAudioServiceEx;
        if (iHwAudioServiceEx != null) {
            iHwAudioServiceEx.notifySendBroadcastForKaraoke(uid);
        }
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

    private void sendBehavior(IHwBehaviorCollectManager.BehaviorId bid, Object... params) {
        if (this.mHwBehaviorManager == null) {
            this.mHwBehaviorManager = HwFrameworkFactory.getHwBehaviorCollectManager();
        }
        IHwBehaviorCollectManager iHwBehaviorCollectManager = this.mHwBehaviorManager;
        if (iHwBehaviorCollectManager == null) {
            Log.w(TAG, "HwBehaviorCollectManager is null");
        } else if (params == null || params.length == 0) {
            this.mHwBehaviorManager.sendBehavior(Binder.getCallingUid(), Binder.getCallingPid(), bid);
        } else {
            iHwBehaviorCollectManager.sendBehavior(Binder.getCallingUid(), Binder.getCallingPid(), bid, params);
        }
    }

    /* access modifiers changed from: package-private */
    public void handleDeviceConnectionNotify(int device, boolean connect) {
        if (LOUD_VOICE_MODE_SUPPORT) {
            sendMsg(this.mAudioHandler, 10001, 0, 0, 0, null, 500);
        }
        if (IS_SUPER_RECEIVER_ENABLED) {
            sendMsg(this.mAudioHandler, 10099, 0, 0, 0, null, 0);
        }
        this.mHwAudioServiceEx.onSetSoundEffectState(device, connect ? 1 : 0);
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.audio.AudioService$HwInnerAudioService, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public IBinder getHwInnerService() {
        return this.mHwInnerService;
    }

    @Override // com.android.server.audio.IHwAudioServiceInner
    public boolean checkAudioSettingsPermissionEx(String method) {
        return checkAudioSettingsPermission(method);
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

        public int startVirtualAudio(String deviceId, String serviceId, int serviceType, Map dataMap) {
            return AudioService.this.mHwAudioServiceEx.startVirtualAudio(deviceId, serviceId, serviceType, dataMap);
        }

        public int removeVirtualAudio(String deviceId, String serviceId, int serviceType, Map dataMap) {
            return AudioService.this.mHwAudioServiceEx.removeVirtualAudio(deviceId, serviceId, serviceType, dataMap);
        }

        public boolean checkMuteZenMode() {
            return AudioService.this.mHwAudioServiceEx.checkMuteZenMode();
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

        public IBinder getDeviceSelectCallback() {
            return AudioService.this.mHwAudioServiceEx.getDeviceSelectCallback();
        }

        public boolean registerAudioDeviceSelectCallback(IBinder cb) {
            if (AudioService.this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_AUDIO_ROUTING") == 0) {
                return AudioService.this.mHwAudioServiceEx.registerAudioDeviceSelectCallback(cb);
            }
            Log.e(AudioService.TAG, "not allowed to registerAudioDeviceSelectCallback");
            return false;
        }

        public boolean unregisterAudioDeviceSelectCallback(IBinder cb) {
            if (AudioService.this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_AUDIO_ROUTING") == 0) {
                return AudioService.this.mHwAudioServiceEx.unregisterAudioDeviceSelectCallback(cb);
            }
            Log.e(AudioService.TAG, "not allowed to unregisterAudioDeviceSelectCallback");
            return false;
        }

        public boolean registerAudioFocusChangeCallback(IAudioFocusChangeDispatcher cb, String callback, String pkgName) {
            return AudioService.this.mMediaFocusControl.registerAudioFocusChangeCallback(cb, callback, pkgName);
        }

        public boolean unregisterAudioFocusChangeCallback(IAudioFocusChangeDispatcher cb, String callback, String pkgName) {
            return AudioService.this.mMediaFocusControl.unregisterAudioFocusChangeCallback(cb, callback, pkgName);
        }

        public AudioFocusInfo getAudioFocusInfo(String pkgName) {
            return AudioService.this.mMediaFocusControl.getAudioFocusInfo(pkgName);
        }

        public boolean setFmDeviceAvailable(int state) {
            return AudioService.this.mHwAudioServiceEx.setFmDeviceAvailable(state, true);
        }

        public void setBtScoForRecord(boolean on) {
            AudioService.this.mHwAudioServiceEx.setBtScoForRecord(on);
        }

        public void setBluetoothScoState(int state, int sessionId) {
            AudioService.this.mHwAudioServiceEx.setBluetoothScoState(state, sessionId);
        }

        public boolean registerVolumeChangeCallback(IVolumeChangeDispatcher cb, String callback, String pkgName) {
            return AudioService.this.mHwAudioServiceEx.registerVolumeChangeCallback(cb, callback, pkgName);
        }

        public boolean unregisterVolumeChangeCallback(IVolumeChangeDispatcher cb, String callback, String pkgName) {
            return AudioService.this.mHwAudioServiceEx.unregisterVolumeChangeCallback(cb, callback, pkgName);
        }

        public void setHistenNaturalMode(boolean on, IBinder cb) {
            AudioService.this.mHwAudioServiceEx.setHistenNaturalMode(on, cb);
        }

        public void setMultiAudioRecordEnable(boolean enable) {
            AudioService.this.mHwAudioServiceEx.setMultiAudioRecordEnable(enable);
        }

        public boolean isMultiAudioRecordEnable() {
            return AudioService.this.mHwAudioServiceEx.isMultiAudioRecordEnable();
        }

        public void setVoiceRecordingEnable(boolean enable) {
            AudioService.this.mHwAudioServiceEx.setVoiceRecordingEnable(enable);
        }

        public boolean isVoiceRecordingEnable() {
            return AudioService.this.mHwAudioServiceEx.isVoiceRecordingEnable();
        }

        public boolean setVolumeByPidStream(int pid, int streamType, float volume, IBinder cb) {
            return AudioService.this.mHwAudioServiceEx.setVolumeByPidStream(pid, streamType, volume, cb);
        }
    }
}
