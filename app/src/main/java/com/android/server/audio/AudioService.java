package com.android.server.audio;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.ActivityManagerNative;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothProfile.ServiceListener;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.content.res.Resources.NotFoundException;
import android.content.res.XmlResourceParser;
import android.database.ContentObserver;
import android.hardware.hdmi.HdmiControlManager;
import android.hardware.hdmi.HdmiPlaybackClient;
import android.hardware.hdmi.HdmiPlaybackClient.DisplayStatusCallback;
import android.hardware.hdmi.HdmiTvClient;
import android.media.AudioAttributes;
import android.media.AudioDevicePort;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioManagerInternal;
import android.media.AudioManagerInternal.RingerModeDelegate;
import android.media.AudioPort;
import android.media.AudioRecordingConfiguration;
import android.media.AudioRoutesInfo;
import android.media.AudioSystem;
import android.media.AudioSystem.DynamicPolicyCallback;
import android.media.AudioSystem.ErrorCallback;
import android.media.HwMediaMonitorManager;
import android.media.IAudioFocusDispatcher;
import android.media.IAudioRoutesObserver;
import android.media.IRecordingConfigDispatcher;
import android.media.IRingtonePlayer;
import android.media.IVolumeController;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.SoundPool;
import android.media.SoundPool.Builder;
import android.media.SoundPool.OnLoadCompleteListener;
import android.media.VolumePolicy;
import android.media.audiopolicy.AudioMix;
import android.media.audiopolicy.AudioPolicyConfig;
import android.media.audiopolicy.IAudioPolicyCallback;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManagerInternal;
import android.os.UserManagerInternal.UserRestrictionsListener;
import android.os.Vibrator;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.service.vr.IVrManager;
import android.service.vr.IVrManager.Stub;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Jlog;
import android.util.Log;
import android.util.MathUtils;
import android.util.Slog;
import android.util.SparseIntArray;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityManager.TouchExplorationStateChangeListener;
import com.android.internal.os.HwBootAnimationOeminfo;
import com.android.internal.util.XmlUtils;
import com.android.server.EventLogTags;
import com.android.server.HwServiceFactory;
import com.android.server.HwServiceFactory.IHwAudioService;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.pm.UserManagerService;
import com.android.server.power.AbsPowerManagerService;
import com.android.server.usb.UsbAudioDevice;
import com.android.server.vr.VrManagerService;
import com.android.server.wm.WindowManagerService.H;
import huawei.cust.HwCustUtils;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.xmlpull.v1.XmlPullParserException;

public class AudioService extends AbsAudioService {
    private static final String ASSET_FILE_VERSION = "1.0";
    private static final String ATTR_ASSET_FILE = "file";
    private static final String ATTR_ASSET_ID = "id";
    private static final String ATTR_GROUP_NAME = "name";
    private static final String ATTR_VERSION = "version";
    private static final int BTA2DP_DOCK_TIMEOUT_MILLIS = 8000;
    private static final int BT_HEADSET_CNCT_TIMEOUT_MS = 3000;
    public static final String CONNECT_INTENT_KEY_ADDRESS = "address";
    public static final String CONNECT_INTENT_KEY_DEVICE_CLASS = "class";
    public static final String CONNECT_INTENT_KEY_HAS_CAPTURE = "hasCapture";
    public static final String CONNECT_INTENT_KEY_HAS_MIDI = "hasMIDI";
    public static final String CONNECT_INTENT_KEY_HAS_PLAYBACK = "hasPlayback";
    public static final String CONNECT_INTENT_KEY_PORT_NAME = "portName";
    public static final String CONNECT_INTENT_KEY_STATE = "state";
    protected static final boolean DEBUG_AP = false;
    protected static final boolean DEBUG_DEVICES = false;
    protected static final boolean DEBUG_MODE = false;
    protected static final boolean DEBUG_VOL = false;
    private static final int FLAG_ADJUST_VOLUME = 1;
    private static final int FLAG_PERSIST_VOLUME = 2;
    private static final String GROUP_TOUCH_SOUNDS = "touch_sounds";
    private static final int INDICATE_SYSTEM_READY_RETRY_DELAY_MS = 1000;
    private static int[] MAX_STREAM_VOLUME = null;
    private static int[] MIN_STREAM_VOLUME = null;
    private static final int MSG_AUDIO_SERVER_DIED = 4;
    private static final int MSG_BROADCAST_AUDIO_BECOMING_NOISY = 15;
    private static final int MSG_BROADCAST_BT_CONNECTION_STATE = 19;
    private static final int MSG_BTA2DP_DOCK_TIMEOUT = 6;
    private static final int MSG_BT_HEADSET_CNCT_FAILED = 9;
    private static final int MSG_CHECK_MUSIC_ACTIVE = 14;
    private static final int MSG_CONFIGURE_SAFE_MEDIA_VOLUME = 16;
    private static final int MSG_CONFIGURE_SAFE_MEDIA_VOLUME_FORCED = 17;
    private static final int MSG_DYN_POLICY_MIX_STATE_UPDATE = 25;
    private static final int MSG_INDICATE_SYSTEM_READY = 26;
    private static final int MSG_LOAD_SOUND_EFFECTS = 7;
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
    private static final int MSG_SET_WIRED_DEVICE_CONNECTION_STATE = 100;
    private static final int MSG_SYSTEM_READY = 21;
    private static final int MSG_UNLOAD_SOUND_EFFECTS = 20;
    private static final int MSG_UNMUTE_STREAM = 24;
    private static final int MUSIC_ACTIVE_POLL_PERIOD_MS = 60000;
    private static final int NUM_SOUNDPOOL_CHANNELS = 4;
    protected static final int PERSIST_DELAY = 500;
    private static final String[] RINGER_MODE_NAMES = null;
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
    private static final int SCO_STATE_DEACTIVATE_EXT_REQ = 4;
    private static final int SCO_STATE_DEACTIVATE_REQ = 5;
    private static final int SCO_STATE_INACTIVE = 0;
    protected static final int SENDMSG_NOOP = 1;
    protected static final int SENDMSG_QUEUE = 2;
    protected static final int SENDMSG_REPLACE = 0;
    private static final int SOUND_EFFECTS_LOAD_TIMEOUT_MS = 5000;
    private static final String SOUND_EFFECTS_PATH = "/media/audio/ui/";
    private static final List<String> SOUND_EFFECT_FILES = null;
    private static final int[] STREAM_VOLUME_OPS = null;
    private static final String TAG = "AudioService";
    private static final String TAG_ASSET = "asset";
    private static final String TAG_AUDIO_ASSETS = "audio_assets";
    private static final String TAG_GROUP = "group";
    private static final int UNMUTE_STREAM_DELAY = 350;
    private static final int UNSAFE_VOLUME_MUSIC_ACTIVE_MS_MAX = 72000000;
    private static Long mLastDeviceConnectMsgTime;
    private static int sSoundEffectVolumeDb;
    private final int[][] SOUND_EFFECT_FILES_MAP;
    private final int[] STREAM_VOLUME_ALIAS_DEFAULT;
    private final int[] STREAM_VOLUME_ALIAS_TELEVISION;
    private final int[] STREAM_VOLUME_ALIAS_VOICE;
    private BluetoothA2dp mA2dp;
    private final Object mA2dpAvrcpLock;
    private final AppOpsManager mAppOps;
    private WakeLock mAudioEventWakeLock;
    protected AudioHandler mAudioHandler;
    private HashMap<IBinder, AudioPolicyProxy> mAudioPolicies;
    private int mAudioPolicyCounter;
    private final ErrorCallback mAudioSystemCallback;
    private AudioSystemThread mAudioSystemThread;
    private boolean mAvrcpAbsVolSupported;
    int mBecomingNoisyIntentDevices;
    private boolean mBluetoothA2dpEnabled;
    private final Object mBluetoothA2dpEnabledLock;
    private BluetoothHeadset mBluetoothHeadset;
    private BluetoothDevice mBluetoothHeadsetDevice;
    private ServiceListener mBluetoothProfileServiceListener;
    private Boolean mCameraSoundForced;
    private final ArrayMap<String, DeviceListSpec> mConnectedDevices;
    private final ContentResolver mContentResolver;
    private final Context mContext;
    private final ControllerService mControllerService;
    final AudioRoutesInfo mCurAudioRoutes;
    private HwCustAudioService mCust;
    private int mDeviceOrientation;
    private String mDockAddress;
    private boolean mDockAudioMediaEnabled;
    private int mDockState;
    private final DynamicPolicyCallback mDynPolicyCallback;
    private boolean mFactoryMode;
    int mFixedVolumeDevices;
    private ForceControlStreamClient mForceControlStreamClient;
    private final Object mForceControlStreamLock;
    private int mForcedUseForComm;
    int mFullVolumeDevices;
    private final boolean mHasVibrator;
    private boolean mHdmiCecSink;
    private MyDisplayStatusCallback mHdmiDisplayStatusCallback;
    private HdmiControlManager mHdmiManager;
    private HdmiPlaybackClient mHdmiPlaybackClient;
    private boolean mHdmiSystemAudioSupported;
    private HdmiTvClient mHdmiTvClient;
    private boolean mIsHisiPlatform;
    private KeyguardManager mKeyguardManager;
    private long mLoweredFromNormalToVibrateTime;
    private int mMcc;
    private final MediaFocusControl mMediaFocusControl;
    private int mMode;
    private final boolean mMonitorOrientation;
    private final boolean mMonitorRotation;
    private int mMusicActiveMs;
    private int mMuteAffectedStreams;
    private NotificationManager mNm;
    private StreamVolumeCommand mPendingVolumeCommand;
    private final int mPlatformType;
    private int mPrevVolDirection;
    private final BroadcastReceiver mReceiver;
    private final RecordingActivityMonitor mRecordMonitor;
    private int mRingerMode;
    private int mRingerModeAffectedStreams;
    private RingerModeDelegate mRingerModeDelegate;
    private int mRingerModeExternal;
    private int mRingerModeMutedStreams;
    private volatile IRingtonePlayer mRingtonePlayer;
    private ArrayList<RmtSbmxFullVolDeathHandler> mRmtSbmxFullVolDeathHandlers;
    private int mRmtSbmxFullVolRefCount;
    final RemoteCallbackList<IAudioRoutesObserver> mRoutesObservers;
    private final int mSafeMediaVolumeDevices;
    private int mSafeMediaVolumeIndex;
    private Integer mSafeMediaVolumeState;
    private int mScoAudioMode;
    private int mScoAudioState;
    private final ArrayList<ScoClient> mScoClients;
    private int mScoConnectionState;
    private boolean mScreenOn;
    protected final ArrayList<SetModeDeathHandler> mSetModeDeathHandlers;
    private final Object mSettingsLock;
    private SettingsObserver mSettingsObserver;
    private final Object mSoundEffectsLock;
    private SoundPool mSoundPool;
    private SoundPoolCallback mSoundPoolCallBack;
    private SoundPoolListenerThread mSoundPoolListenerThread;
    private Looper mSoundPoolLooper;
    private VolumeStreamState[] mStreamStates;
    private int[] mStreamVolumeAlias;
    protected boolean mSystemReady;
    private final boolean mUseFixedVolume;
    private final UserManagerInternal mUserManagerInternal;
    private final UserRestrictionsListener mUserRestrictionsListener;
    private boolean mUserSwitchedReceived;
    private int mVibrateSetting;
    private int mVolumeControlStream;
    private final VolumeController mVolumeController;
    private VolumePolicy mVolumePolicy;

    /* renamed from: com.android.server.audio.AudioService.4 */
    class AnonymousClass4 implements DeathRecipient {
        final /* synthetic */ IVolumeController val$controller;

        AnonymousClass4(IVolumeController val$controller) {
            this.val$controller = val$controller;
        }

        public void binderDied() {
            if (AudioService.this.mVolumeController.isSameBinder(this.val$controller)) {
                Log.w(AudioService.TAG, "Current remote volume controller died, unregistering");
                AudioService.this.setVolumeController(null);
            }
        }
    }

    private class AudioHandler extends Handler {
        private AudioHandler() {
        }

        private void setDeviceVolume(VolumeStreamState streamState, int device) {
            synchronized (VolumeStreamState.class) {
                streamState.applyDeviceVolume_syncVSS(device);
                int streamType = AudioSystem.getNumStreamTypes() + AudioService.SCO_MODE_UNDEFINED;
                while (streamType >= 0) {
                    if (streamType != streamState.mStreamType && AudioService.this.mStreamVolumeAlias[streamType] == streamState.mStreamType) {
                        int streamDevice = AudioService.this.getDeviceForStream(streamType);
                        if (!(device == streamDevice || !AudioService.this.mAvrcpAbsVolSupported || (device & 896) == 0)) {
                            AudioService.this.mStreamStates[streamType].applyDeviceVolume_syncVSS(device);
                        }
                        AudioService.this.mStreamStates[streamType].applyDeviceVolume_syncVSS(streamDevice);
                    }
                    streamType += AudioService.SCO_MODE_UNDEFINED;
                }
            }
            AudioService.sendMsg(AudioService.this.mAudioHandler, AudioService.SENDMSG_NOOP, AudioService.SENDMSG_QUEUE, device, AudioService.SENDMSG_REPLACE, streamState, AudioService.PERSIST_DELAY);
        }

        private void setAllVolumes(VolumeStreamState streamState) {
            streamState.applyAllVolumes();
            int streamType = AudioSystem.getNumStreamTypes() + AudioService.SCO_MODE_UNDEFINED;
            while (streamType >= 0) {
                if (streamType != streamState.mStreamType && AudioService.this.mStreamVolumeAlias[streamType] == streamState.mStreamType) {
                    AudioService.this.mStreamStates[streamType].applyAllVolumes();
                }
                streamType += AudioService.SCO_MODE_UNDEFINED;
            }
        }

        private void persistVolume(VolumeStreamState streamState, int device) {
            if (!AudioService.this.checkEnbaleVolumeAdjust() || AudioService.this.mUseFixedVolume) {
                return;
            }
            if (!AudioService.this.isPlatformTelevision() || streamState.mStreamType == AudioService.SCO_STATE_ACTIVE_INTERNAL) {
                System.putIntForUser(AudioService.this.mContentResolver, streamState.getSettingNameForDevice(device), (streamState.getIndex(device) + AudioService.SCO_STATE_DEACTIVATE_REQ) / AudioService.MSG_SET_ALL_VOLUMES, -2);
                if (AudioService.SENDMSG_QUEUE == streamState.mStreamType && AudioService.SENDMSG_QUEUE == device) {
                    HwBootAnimationOeminfo.setBootAnimRing((streamState.getIndex(device) + AudioService.SCO_STATE_DEACTIVATE_REQ) / AudioService.MSG_SET_ALL_VOLUMES);
                }
            }
        }

        private void persistRingerMode(int ringerMode) {
            if (AudioService.this.checkEnbaleVolumeAdjust() && !AudioService.this.mUseFixedVolume) {
                if (AudioService.SENDMSG_QUEUE == ringerMode) {
                    Log.i(AudioService.TAG, "set 1 to ringermode");
                    HwBootAnimationOeminfo.setBootAnimRingMode(AudioService.SENDMSG_NOOP);
                } else {
                    Log.i(AudioService.TAG, "set 0 to ringermode");
                    HwBootAnimationOeminfo.setBootAnimRingMode(AudioService.SENDMSG_REPLACE);
                }
                Global.putInt(AudioService.this.mContentResolver, "mode_ringer", ringerMode);
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private boolean onLoadSoundEffects() {
            synchronized (AudioService.this.mSoundEffectsLock) {
                if (!AudioService.this.mSystemReady) {
                    Log.w(AudioService.TAG, "onLoadSoundEffects() called before boot complete");
                    return AudioService.DEBUG_VOL;
                } else if (AudioService.this.mSoundPool != null) {
                    return true;
                } else {
                    int attempts;
                    int[] poolId;
                    int fileIdx;
                    int numSamples;
                    int effect;
                    String filePath;
                    int sampleId;
                    int status;
                    AudioService.this.loadTouchSoundAssets();
                    AudioService.this.mSoundPool = new Builder().setMaxStreams(AudioService.SCO_STATE_DEACTIVATE_EXT_REQ).setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioService.MSG_SET_FORCE_BT_A2DP_USE).setContentType(AudioService.SCO_STATE_DEACTIVATE_EXT_REQ).build()).build();
                    AudioService.this.mSoundPoolCallBack = null;
                    AudioService.this.mSoundPoolListenerThread = new SoundPoolListenerThread();
                    AudioService.this.mSoundPoolListenerThread.start();
                    int attempts2 = AudioService.SCO_STATE_ACTIVE_INTERNAL;
                    while (AudioService.this.mSoundPoolCallBack == null) {
                        attempts = attempts2 + AudioService.SCO_MODE_UNDEFINED;
                        if (attempts2 > 0) {
                            try {
                                AudioService.this.mSoundEffectsLock.wait(5000);
                            } catch (InterruptedException e) {
                                Log.w(AudioService.TAG, "Interrupted while waiting sound pool listener thread.");
                            }
                            attempts2 = attempts;
                        }
                        if (AudioService.this.mSoundPoolCallBack != null) {
                            Log.w(AudioService.TAG, "onLoadSoundEffects() SoundPool listener or thread creation error");
                            if (AudioService.this.mSoundPoolLooper != null) {
                                AudioService.this.mSoundPoolLooper.quit();
                                AudioService.this.mSoundPoolLooper = null;
                            }
                            AudioService.this.mSoundPoolListenerThread = null;
                            AudioService.this.mSoundPool.release();
                            AudioService.this.mSoundPool = null;
                            return AudioService.DEBUG_VOL;
                        }
                        poolId = new int[AudioService.SOUND_EFFECT_FILES.size()];
                        for (fileIdx = AudioService.SENDMSG_REPLACE; fileIdx < AudioService.SOUND_EFFECT_FILES.size(); fileIdx += AudioService.SENDMSG_NOOP) {
                            poolId[fileIdx] = AudioService.SCO_MODE_UNDEFINED;
                        }
                        numSamples = AudioService.SENDMSG_REPLACE;
                        for (effect = AudioService.SENDMSG_REPLACE; effect < AudioService.MSG_SET_ALL_VOLUMES; effect += AudioService.SENDMSG_NOOP) {
                            if (AudioService.this.SOUND_EFFECT_FILES_MAP[effect][AudioService.SENDMSG_NOOP] != 0) {
                                if (poolId[AudioService.this.SOUND_EFFECT_FILES_MAP[effect][AudioService.SENDMSG_REPLACE]] != AudioService.SCO_MODE_UNDEFINED) {
                                    filePath = Environment.getRootDirectory() + AudioService.SOUND_EFFECTS_PATH + ((String) AudioService.SOUND_EFFECT_FILES.get(AudioService.this.SOUND_EFFECT_FILES_MAP[effect][AudioService.SENDMSG_REPLACE]));
                                    sampleId = AudioService.this.getSampleId(AudioService.this.mSoundPool, effect, filePath, AudioService.SENDMSG_REPLACE);
                                    if (sampleId > 0) {
                                        Log.w(AudioService.TAG, "Soundpool could not load file: " + filePath);
                                    } else {
                                        AudioService.this.SOUND_EFFECT_FILES_MAP[effect][AudioService.SENDMSG_NOOP] = sampleId;
                                        poolId[AudioService.this.SOUND_EFFECT_FILES_MAP[effect][AudioService.SENDMSG_REPLACE]] = sampleId;
                                        numSamples += AudioService.SENDMSG_NOOP;
                                    }
                                } else {
                                    AudioService.this.SOUND_EFFECT_FILES_MAP[effect][AudioService.SENDMSG_NOOP] = poolId[AudioService.this.SOUND_EFFECT_FILES_MAP[effect][AudioService.SENDMSG_REPLACE]];
                                }
                            }
                        }
                        if (numSamples <= 0) {
                            AudioService.this.mSoundPoolCallBack.setSamples(poolId);
                            status = AudioService.SENDMSG_NOOP;
                            attempts2 = AudioService.SCO_STATE_ACTIVE_INTERNAL;
                            while (status == AudioService.SENDMSG_NOOP) {
                                attempts = attempts2 + AudioService.SCO_MODE_UNDEFINED;
                                if (attempts2 > 0) {
                                    try {
                                        AudioService.this.mSoundEffectsLock.wait(5000);
                                        status = AudioService.this.mSoundPoolCallBack.status();
                                    } catch (InterruptedException e2) {
                                        Log.w(AudioService.TAG, "Interrupted while waiting sound pool callback.");
                                    }
                                    attempts2 = attempts;
                                }
                            }
                            attempts = attempts2;
                        } else {
                            status = AudioService.SCO_MODE_UNDEFINED;
                        }
                        if (AudioService.this.mSoundPoolLooper != null) {
                            AudioService.this.mSoundPoolLooper.quit();
                            AudioService.this.mSoundPoolLooper = null;
                        }
                        AudioService.this.mSoundPoolListenerThread = null;
                        if (status != 0) {
                            Log.w(AudioService.TAG, "onLoadSoundEffects(), Error " + status + " while loading samples");
                            for (effect = AudioService.SENDMSG_REPLACE; effect < AudioService.MSG_SET_ALL_VOLUMES; effect += AudioService.SENDMSG_NOOP) {
                                if (AudioService.this.SOUND_EFFECT_FILES_MAP[effect][AudioService.SENDMSG_NOOP] > 0) {
                                    AudioService.this.SOUND_EFFECT_FILES_MAP[effect][AudioService.SENDMSG_NOOP] = AudioService.SCO_MODE_UNDEFINED;
                                }
                            }
                            AudioService.this.mSoundPool.release();
                            AudioService.this.mSoundPool = null;
                        }
                        return status != 0 ? true : AudioService.DEBUG_VOL;
                    }
                    attempts = attempts2;
                    if (AudioService.this.mSoundPoolCallBack != null) {
                        poolId = new int[AudioService.SOUND_EFFECT_FILES.size()];
                        for (fileIdx = AudioService.SENDMSG_REPLACE; fileIdx < AudioService.SOUND_EFFECT_FILES.size(); fileIdx += AudioService.SENDMSG_NOOP) {
                            poolId[fileIdx] = AudioService.SCO_MODE_UNDEFINED;
                        }
                        numSamples = AudioService.SENDMSG_REPLACE;
                        for (effect = AudioService.SENDMSG_REPLACE; effect < AudioService.MSG_SET_ALL_VOLUMES; effect += AudioService.SENDMSG_NOOP) {
                            if (AudioService.this.SOUND_EFFECT_FILES_MAP[effect][AudioService.SENDMSG_NOOP] != 0) {
                                if (poolId[AudioService.this.SOUND_EFFECT_FILES_MAP[effect][AudioService.SENDMSG_REPLACE]] != AudioService.SCO_MODE_UNDEFINED) {
                                    AudioService.this.SOUND_EFFECT_FILES_MAP[effect][AudioService.SENDMSG_NOOP] = poolId[AudioService.this.SOUND_EFFECT_FILES_MAP[effect][AudioService.SENDMSG_REPLACE]];
                                } else {
                                    filePath = Environment.getRootDirectory() + AudioService.SOUND_EFFECTS_PATH + ((String) AudioService.SOUND_EFFECT_FILES.get(AudioService.this.SOUND_EFFECT_FILES_MAP[effect][AudioService.SENDMSG_REPLACE]));
                                    sampleId = AudioService.this.getSampleId(AudioService.this.mSoundPool, effect, filePath, AudioService.SENDMSG_REPLACE);
                                    if (sampleId > 0) {
                                        AudioService.this.SOUND_EFFECT_FILES_MAP[effect][AudioService.SENDMSG_NOOP] = sampleId;
                                        poolId[AudioService.this.SOUND_EFFECT_FILES_MAP[effect][AudioService.SENDMSG_REPLACE]] = sampleId;
                                        numSamples += AudioService.SENDMSG_NOOP;
                                    } else {
                                        Log.w(AudioService.TAG, "Soundpool could not load file: " + filePath);
                                    }
                                }
                            }
                        }
                        if (numSamples <= 0) {
                            status = AudioService.SCO_MODE_UNDEFINED;
                        } else {
                            AudioService.this.mSoundPoolCallBack.setSamples(poolId);
                            status = AudioService.SENDMSG_NOOP;
                            attempts2 = AudioService.SCO_STATE_ACTIVE_INTERNAL;
                            while (status == AudioService.SENDMSG_NOOP) {
                                attempts = attempts2 + AudioService.SCO_MODE_UNDEFINED;
                                if (attempts2 > 0) {
                                    AudioService.this.mSoundEffectsLock.wait(5000);
                                    status = AudioService.this.mSoundPoolCallBack.status();
                                    attempts2 = attempts;
                                }
                            }
                            attempts = attempts2;
                        }
                        if (AudioService.this.mSoundPoolLooper != null) {
                            AudioService.this.mSoundPoolLooper.quit();
                            AudioService.this.mSoundPoolLooper = null;
                        }
                        AudioService.this.mSoundPoolListenerThread = null;
                        if (status != 0) {
                            Log.w(AudioService.TAG, "onLoadSoundEffects(), Error " + status + " while loading samples");
                            for (effect = AudioService.SENDMSG_REPLACE; effect < AudioService.MSG_SET_ALL_VOLUMES; effect += AudioService.SENDMSG_NOOP) {
                                if (AudioService.this.SOUND_EFFECT_FILES_MAP[effect][AudioService.SENDMSG_NOOP] > 0) {
                                    AudioService.this.SOUND_EFFECT_FILES_MAP[effect][AudioService.SENDMSG_NOOP] = AudioService.SCO_MODE_UNDEFINED;
                                }
                            }
                            AudioService.this.mSoundPool.release();
                            AudioService.this.mSoundPool = null;
                        }
                        if (status != 0) {
                        }
                        return status != 0 ? true : AudioService.DEBUG_VOL;
                    }
                    Log.w(AudioService.TAG, "onLoadSoundEffects() SoundPool listener or thread creation error");
                    if (AudioService.this.mSoundPoolLooper != null) {
                        AudioService.this.mSoundPoolLooper.quit();
                        AudioService.this.mSoundPoolLooper = null;
                    }
                    AudioService.this.mSoundPoolListenerThread = null;
                    AudioService.this.mSoundPool.release();
                    AudioService.this.mSoundPool = null;
                    return AudioService.DEBUG_VOL;
                }
            }
        }

        private void onUnloadSoundEffects() {
            synchronized (AudioService.this.mSoundEffectsLock) {
                if (AudioService.this.mSoundPool == null) {
                    return;
                }
                AudioService.this.unloadHwThemeSoundEffects();
                int[] poolId = new int[AudioService.SOUND_EFFECT_FILES.size()];
                for (int fileIdx = AudioService.SENDMSG_REPLACE; fileIdx < AudioService.SOUND_EFFECT_FILES.size(); fileIdx += AudioService.SENDMSG_NOOP) {
                    poolId[fileIdx] = AudioService.SENDMSG_REPLACE;
                }
                int effect = AudioService.SENDMSG_REPLACE;
                while (effect < AudioService.MSG_SET_ALL_VOLUMES) {
                    if (AudioService.this.SOUND_EFFECT_FILES_MAP[effect][AudioService.SENDMSG_NOOP] > 0 && poolId[AudioService.this.SOUND_EFFECT_FILES_MAP[effect][AudioService.SENDMSG_REPLACE]] == 0) {
                        AudioService.this.mSoundPool.unload(AudioService.this.SOUND_EFFECT_FILES_MAP[effect][AudioService.SENDMSG_NOOP]);
                        AudioService.this.SOUND_EFFECT_FILES_MAP[effect][AudioService.SENDMSG_NOOP] = AudioService.SCO_MODE_UNDEFINED;
                        poolId[AudioService.this.SOUND_EFFECT_FILES_MAP[effect][AudioService.SENDMSG_REPLACE]] = AudioService.SCO_MODE_UNDEFINED;
                    }
                    effect += AudioService.SENDMSG_NOOP;
                }
                AudioService.this.mSoundPool.release();
                AudioService.this.mSoundPool = null;
            }
        }

        private void onPlaySoundEffect(int effectType, int volume) {
            synchronized (AudioService.this.mSoundEffectsLock) {
                onLoadSoundEffects();
                if (AudioService.this.mSoundPool == null) {
                    return;
                }
                float volFloat;
                if (volume < 0) {
                    volFloat = (float) Math.pow(10.0d, (double) (((float) AudioService.sSoundEffectVolumeDb) / 20.0f));
                } else {
                    volFloat = ((float) volume) / 1000.0f;
                }
                if (AudioService.this.SOUND_EFFECT_FILES_MAP[effectType][AudioService.SENDMSG_NOOP] > 0) {
                    AudioService.this.mSoundPool.play(AudioService.this.SOUND_EFFECT_FILES_MAP[effectType][AudioService.SENDMSG_NOOP], volFloat, volFloat, AudioService.SENDMSG_REPLACE, AudioService.SENDMSG_REPLACE, 1.0f);
                } else {
                    MediaPlayer mediaPlayer = new MediaPlayer();
                    try {
                        mediaPlayer.setDataSource(Environment.getRootDirectory() + AudioService.SOUND_EFFECTS_PATH + ((String) AudioService.SOUND_EFFECT_FILES.get(AudioService.this.SOUND_EFFECT_FILES_MAP[effectType][AudioService.SENDMSG_REPLACE])));
                        mediaPlayer.setAudioStreamType(AudioService.SENDMSG_NOOP);
                        mediaPlayer.prepare();
                        mediaPlayer.setVolume(volFloat);
                        mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
                            public void onCompletion(MediaPlayer mp) {
                                AudioHandler.this.cleanupPlayer(mp);
                            }
                        });
                        mediaPlayer.setOnErrorListener(new OnErrorListener() {
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

        private void setForceUse(int usage, int config) {
            synchronized (AudioService.this.mConnectedDevices) {
                AudioService.this.setForceUseInt_SyncDevices(usage, config);
            }
        }

        private void onPersistSafeVolumeState(int state) {
            Global.putInt(AudioService.this.mContentResolver, "audio_safe_volume_state", state);
        }

        public void handleMessage(Message msg) {
            int i = AudioService.SENDMSG_REPLACE;
            switch (msg.what) {
                case AudioService.SENDMSG_REPLACE /*0*/:
                    setDeviceVolume((VolumeStreamState) msg.obj, msg.arg1);
                case AudioService.SENDMSG_NOOP /*1*/:
                    persistVolume((VolumeStreamState) msg.obj, msg.arg1);
                case AudioService.SCO_STATE_ACTIVE_INTERNAL /*3*/:
                    persistRingerMode(AudioService.this.getRingerModeInternal());
                case AudioService.SCO_STATE_DEACTIVATE_EXT_REQ /*4*/:
                    AudioService.this.onAudioServerDied();
                case AudioService.SCO_STATE_DEACTIVATE_REQ /*5*/:
                    onPlaySoundEffect(msg.arg1, msg.arg2);
                    Jlog.d(175, msg.arg1, "playSoundEffect end");
                case AudioService.MSG_BTA2DP_DOCK_TIMEOUT /*6*/:
                    synchronized (AudioService.this.mConnectedDevices) {
                        AudioService.this.makeA2dpDeviceUnavailableNow((String) msg.obj);
                        break;
                    }
                case AudioService.MSG_LOAD_SOUND_EFFECTS /*7*/:
                    boolean loaded = onLoadSoundEffects();
                    if (msg.obj != null) {
                        LoadSoundEffectReply reply = msg.obj;
                        synchronized (reply) {
                            if (!loaded) {
                                i = AudioService.SCO_MODE_UNDEFINED;
                            }
                            reply.mStatus = i;
                            reply.notify();
                            break;
                        }
                    }
                case AudioService.MSG_SET_FORCE_USE /*8*/:
                case AudioService.MSG_SET_FORCE_BT_A2DP_USE /*13*/:
                    setForceUse(msg.arg1, msg.arg2);
                case AudioService.MSG_BT_HEADSET_CNCT_FAILED /*9*/:
                    AudioService.this.resetBluetoothSco();
                case AudioService.MSG_SET_ALL_VOLUMES /*10*/:
                    setAllVolumes((VolumeStreamState) msg.obj);
                case AudioService.MSG_REPORT_NEW_ROUTES /*12*/:
                    int N = AudioService.this.mRoutesObservers.beginBroadcast();
                    if (N > 0) {
                        AudioRoutesInfo routes;
                        synchronized (AudioService.this.mCurAudioRoutes) {
                            routes = new AudioRoutesInfo(AudioService.this.mCurAudioRoutes);
                            break;
                        }
                        while (N > 0) {
                            N += AudioService.SCO_MODE_UNDEFINED;
                            try {
                                ((IAudioRoutesObserver) AudioService.this.mRoutesObservers.getBroadcastItem(N)).dispatchAudioRoutesChanged(routes);
                            } catch (RemoteException e) {
                            }
                        }
                    }
                    AudioService.this.mRoutesObservers.finishBroadcast();
                    AudioService.this.observeDevicesForStreams(AudioService.SCO_MODE_UNDEFINED);
                case AudioService.MSG_CHECK_MUSIC_ACTIVE /*14*/:
                    AudioService.this.onCheckMusicActive((String) msg.obj);
                case AudioService.MSG_BROADCAST_AUDIO_BECOMING_NOISY /*15*/:
                    AudioService.this.onSendBecomingNoisyIntent();
                case AudioService.MSG_CONFIGURE_SAFE_MEDIA_VOLUME /*16*/:
                case AudioService.MSG_CONFIGURE_SAFE_MEDIA_VOLUME_FORCED /*17*/:
                    AudioService.this.onConfigureSafeVolume(msg.what == AudioService.MSG_CONFIGURE_SAFE_MEDIA_VOLUME_FORCED ? true : AudioService.DEBUG_VOL, (String) msg.obj);
                case AudioService.MSG_PERSIST_SAFE_VOLUME_STATE /*18*/:
                    onPersistSafeVolumeState(msg.arg1);
                case AudioService.MSG_BROADCAST_BT_CONNECTION_STATE /*19*/:
                    AudioService.this.onBroadcastScoConnectionState(msg.arg1);
                case AudioService.MSG_UNLOAD_SOUND_EFFECTS /*20*/:
                    onUnloadSoundEffects();
                case AudioService.MSG_SYSTEM_READY /*21*/:
                    AudioService.this.onSystemReady();
                case AudioService.MSG_PERSIST_MUSIC_ACTIVE_MS /*22*/:
                    Secure.putIntForUser(AudioService.this.mContentResolver, "unsafe_volume_music_active_ms", msg.arg1, -2);
                case AudioService.MSG_UNMUTE_STREAM /*24*/:
                    AudioService.this.onUnmuteStream(msg.arg1, msg.arg2);
                case AudioService.MSG_DYN_POLICY_MIX_STATE_UPDATE /*25*/:
                    AudioService.this.onDynPolicyMixStateUpdate((String) msg.obj, msg.arg1);
                case AudioService.MSG_INDICATE_SYSTEM_READY /*26*/:
                    AudioService.this.onIndicateSystemReady();
                case AudioService.MSG_SET_WIRED_DEVICE_CONNECTION_STATE /*100*/:
                    WiredDeviceConnectionState connectState = msg.obj;
                    AudioService.this.onSetWiredDeviceConnectionState(connectState.mType, connectState.mState, connectState.mAddress, connectState.mName, connectState.mCaller);
                    AudioService.this.mAudioEventWakeLock.release();
                case AudioService.MSG_SET_A2DP_SRC_CONNECTION_STATE /*101*/:
                    AudioService.this.onSetA2dpSourceConnectionState((BluetoothDevice) msg.obj, msg.arg1);
                    AudioService.this.mAudioEventWakeLock.release();
                case AudioService.MSG_SET_A2DP_SINK_CONNECTION_STATE /*102*/:
                    AudioService.this.onSetA2dpSinkConnectionState((BluetoothDevice) msg.obj, msg.arg1);
                    AudioService.this.mAudioEventWakeLock.release();
                case 10002:
                    if (AudioService.SOUND_EFFECTS_SUPPORT) {
                        AudioService.this.onSetSoundEffectState(msg.arg1, msg.arg2);
                        AudioService.this.mAudioEventWakeLock.release();
                    }
                default:
                    AudioService.this.handleMessageEx(msg);
            }
        }
    }

    public class AudioPolicyProxy extends AudioPolicyConfig implements DeathRecipient {
        private static final String TAG = "AudioPolicyProxy";
        int mFocusDuckBehavior;
        boolean mHasFocusListener;
        IAudioPolicyCallback mPolicyCallback;

        AudioPolicyProxy(AudioPolicyConfig config, IAudioPolicyCallback token, boolean hasFocusListener) {
            super(config);
            this.mFocusDuckBehavior = AudioService.SENDMSG_REPLACE;
            StringBuilder append = new StringBuilder().append(config.hashCode()).append(":ap:");
            int -get8 = AudioService.this.mAudioPolicyCounter;
            AudioService.this.mAudioPolicyCounter = -get8 + AudioService.SENDMSG_NOOP;
            setRegistration(new String(append.append(-get8).toString()));
            this.mPolicyCallback = token;
            this.mHasFocusListener = hasFocusListener;
            if (this.mHasFocusListener) {
                AudioService.this.mMediaFocusControl.addFocusFollower(this.mPolicyCallback);
            }
            connectMixes();
        }

        public void binderDied() {
            synchronized (AudioService.this.mAudioPolicies) {
                Log.i(TAG, "audio policy " + this.mPolicyCallback + " died");
                release();
                AudioService.this.mAudioPolicies.remove(this.mPolicyCallback.asBinder());
            }
        }

        String getRegistrationId() {
            return getRegistration();
        }

        void release() {
            if (this.mFocusDuckBehavior == AudioService.SENDMSG_NOOP) {
                AudioService.this.mMediaFocusControl.setDuckingInExtPolicyAvailable(AudioService.DEBUG_VOL);
            }
            if (this.mHasFocusListener) {
                AudioService.this.mMediaFocusControl.removeFocusFollower(this.mPolicyCallback);
            }
            AudioSystem.registerPolicyMixes(this.mMixes, AudioService.DEBUG_VOL);
        }

        void connectMixes() {
            AudioSystem.registerPolicyMixes(this.mMixes, true);
        }
    }

    private class AudioServiceBroadcastReceiver extends BroadcastReceiver {
        private AudioServiceBroadcastReceiver() {
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.intent.action.DOCK_EVENT")) {
                int config;
                int dockState = intent.getIntExtra("android.intent.extra.DOCK_STATE", AudioService.SENDMSG_REPLACE);
                switch (dockState) {
                    case AudioService.SENDMSG_NOOP /*1*/:
                        config = AudioService.MSG_LOAD_SOUND_EFFECTS;
                        break;
                    case AudioService.SENDMSG_QUEUE /*2*/:
                        config = AudioService.MSG_BTA2DP_DOCK_TIMEOUT;
                        break;
                    case AudioService.SCO_STATE_ACTIVE_INTERNAL /*3*/:
                        config = AudioService.MSG_SET_FORCE_USE;
                        break;
                    case AudioService.SCO_STATE_DEACTIVATE_EXT_REQ /*4*/:
                        config = AudioService.MSG_BT_HEADSET_CNCT_FAILED;
                        break;
                    default:
                        config = AudioService.SENDMSG_REPLACE;
                        break;
                }
                if (!(dockState == AudioService.SCO_STATE_ACTIVE_INTERNAL || (dockState == 0 && AudioService.this.mDockState == AudioService.SCO_STATE_ACTIVE_INTERNAL))) {
                    AudioSystem.setForceUse(AudioService.SCO_STATE_ACTIVE_INTERNAL, config);
                }
                AudioService.this.mDockState = dockState;
            } else if (action.equals("android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED")) {
                AudioService.this.setBtScoDeviceConnectionState((BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE"), intent.getIntExtra("android.bluetooth.profile.extra.STATE", AudioService.SENDMSG_REPLACE));
            } else if (action.equals("android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED")) {
                boolean broadcast = AudioService.DEBUG_VOL;
                int scoAudioState = AudioService.SCO_MODE_UNDEFINED;
                synchronized (AudioService.this.mScoClients) {
                    int btState = intent.getIntExtra("android.bluetooth.profile.extra.STATE", AudioService.SCO_MODE_UNDEFINED);
                    if (!AudioService.this.mScoClients.isEmpty()) {
                        if (!(AudioService.this.mScoAudioState == AudioService.SCO_STATE_ACTIVE_INTERNAL || AudioService.this.mScoAudioState == AudioService.SENDMSG_NOOP)) {
                            if (AudioService.this.mScoAudioState == AudioService.SCO_STATE_DEACTIVATE_REQ) {
                            }
                        }
                        broadcast = true;
                    }
                    switch (btState) {
                        case AudioService.MSG_SET_ALL_VOLUMES /*10*/:
                            scoAudioState = AudioService.SENDMSG_REPLACE;
                            AudioService.this.mScoAudioState = AudioService.SENDMSG_REPLACE;
                            AudioService.this.clearAllScoClients(AudioService.SENDMSG_REPLACE, AudioService.DEBUG_VOL);
                            break;
                        case H.WINDOW_FREEZE_TIMEOUT /*11*/:
                            if (!(AudioService.this.mScoAudioState == AudioService.SCO_STATE_ACTIVE_INTERNAL || AudioService.this.mScoAudioState == AudioService.SCO_STATE_DEACTIVATE_REQ || AudioService.this.mScoAudioState == AudioService.SCO_STATE_DEACTIVATE_EXT_REQ)) {
                                AudioService.this.mScoAudioState = AudioService.SENDMSG_QUEUE;
                                break;
                            }
                        case AudioService.MSG_REPORT_NEW_ROUTES /*12*/:
                            scoAudioState = AudioService.SENDMSG_NOOP;
                            if (!(AudioService.this.mScoAudioState == AudioService.SCO_STATE_ACTIVE_INTERNAL || AudioService.this.mScoAudioState == AudioService.SCO_STATE_DEACTIVATE_REQ || AudioService.this.mScoAudioState == AudioService.SCO_STATE_DEACTIVATE_EXT_REQ)) {
                                AudioService.this.mScoAudioState = AudioService.SENDMSG_QUEUE;
                                break;
                            }
                    }
                }
                if (broadcast) {
                    AudioService.this.broadcastScoConnectionState(scoAudioState);
                    Intent intent2 = new Intent("android.media.SCO_AUDIO_STATE_CHANGED");
                    intent2.putExtra("android.media.extra.SCO_AUDIO_STATE", scoAudioState);
                    AudioService.this.sendStickyBroadcastToAll(intent2);
                }
            } else if (action.equals("android.intent.action.SCREEN_ON")) {
                AudioService.this.mScreenOn = true;
                if (AudioService.this.mMonitorRotation) {
                    RotationHelper.enable();
                }
                AudioSystem.setParameters("screen_state=on");
            } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                AudioService.this.mScreenOn = AudioService.DEBUG_VOL;
                if (AudioService.this.mMonitorRotation) {
                    RotationHelper.disable();
                }
                AudioSystem.setParameters("screen_state=off");
            } else if (action.equals("android.intent.action.USER_PRESENT") && AudioService.SPK_RCV_STEREO_SUPPORT) {
                if (AudioService.this.mMonitorRotation) {
                    RotationHelper.updateOrientation();
                }
            } else if (action.equals("android.intent.action.CONFIGURATION_CHANGED")) {
                AudioService.this.handleConfigurationChanged(context);
            } else if (action.equals("android.intent.action.USER_SWITCHED")) {
                if (AudioService.this.mUserSwitchedReceived) {
                    AudioService.sendMsg(AudioService.this.mAudioHandler, AudioService.MSG_BROADCAST_AUDIO_BECOMING_NOISY, AudioService.SENDMSG_REPLACE, AudioService.SENDMSG_REPLACE, AudioService.SENDMSG_REPLACE, null, AudioService.SENDMSG_REPLACE);
                }
                AudioService.this.mUserSwitchedReceived = true;
                AudioService.this.mMediaFocusControl.discardAudioFocusOwner();
                AudioService.this.readAudioSettings(true);
                AudioService.sendMsg(AudioService.this.mAudioHandler, AudioService.MSG_SET_ALL_VOLUMES, AudioService.SENDMSG_QUEUE, AudioService.SENDMSG_REPLACE, AudioService.SENDMSG_REPLACE, AudioService.this.mStreamStates[AudioService.SCO_STATE_ACTIVE_INTERNAL], AudioService.SENDMSG_REPLACE);
            } else if (action.equals("android.intent.action.FM")) {
                state = intent.getIntExtra(AudioService.CONNECT_INTENT_KEY_STATE, AudioService.SENDMSG_REPLACE);
                synchronized (AudioService.this.mConnectedDevices) {
                    String device_out_fm_key = AudioService.this.makeDeviceListKey(DumpState.DUMP_DEXOPT, "");
                    boolean isConnected = AudioService.this.mConnectedDevices.get(device_out_fm_key) != null ? true : AudioService.DEBUG_VOL;
                    if (state == 0 && isConnected) {
                        AudioSystem.setDeviceConnectionState(DumpState.DUMP_DEXOPT, AudioService.SENDMSG_REPLACE, "", "");
                        AudioService.this.mConnectedDevices.remove(device_out_fm_key);
                    } else if (AudioService.SENDMSG_NOOP == state && !isConnected) {
                        AudioSystem.setDeviceConnectionState(DumpState.DUMP_DEXOPT, AudioService.SENDMSG_NOOP, "", "");
                        AudioService.this.mConnectedDevices.put(device_out_fm_key, new DeviceListSpec(DumpState.DUMP_DEXOPT, "", ""));
                    }
                }
            } else if (action.equals("android.intent.action.USER_BACKGROUND")) {
                int userId = intent.getIntExtra("android.intent.extra.user_handle", AudioService.SCO_MODE_UNDEFINED);
                if (userId >= 0) {
                    AudioService.this.killBackgroundUserProcessesWithRecordAudioPermission(UserManagerService.getInstance().getUserInfo(userId));
                }
                UserManagerService.getInstance().setUserRestriction("no_record_audio", true, userId);
            } else if (action.equals("android.intent.action.USER_FOREGROUND")) {
                UserManagerService.getInstance().setUserRestriction("no_record_audio", AudioService.DEBUG_VOL, intent.getIntExtra("android.intent.extra.user_handle", AudioService.SCO_MODE_UNDEFINED));
            } else if (action.equals("android.bluetooth.adapter.action.STATE_CHANGED")) {
                state = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", AudioService.SCO_MODE_UNDEFINED);
                if (state == AudioService.MSG_SET_ALL_VOLUMES || state == AudioService.MSG_SET_FORCE_BT_A2DP_USE) {
                    AudioService.this.disconnectAllBluetoothProfiles();
                }
            }
        }
    }

    final class AudioServiceInternal extends AudioManagerInternal {
        AudioServiceInternal() {
        }

        public void setRingerModeDelegate(RingerModeDelegate delegate) {
            AudioService.this.mRingerModeDelegate = delegate;
            if (AudioService.this.mRingerModeDelegate != null) {
                AudioService.this.updateRingerModeAffectedStreams();
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

        public int getVolumeControllerUid() {
            return AudioService.this.mControllerService.mUid;
        }

        public void updateRingerModeAffectedStreamsInternal() {
            synchronized (AudioService.this.mSettingsLock) {
                if (AudioService.this.updateRingerModeAffectedStreams()) {
                    AudioService.this.setRingerModeInt(getRingerModeInternal(), AudioService.DEBUG_VOL);
                }
            }
        }
    }

    private class AudioServiceUserRestrictionsListener implements UserRestrictionsListener {
        private AudioServiceUserRestrictionsListener() {
        }

        public void onUserRestrictionsChanged(int userId, Bundle newRestrictions, Bundle prevRestrictions) {
            boolean wasRestricted = prevRestrictions.getBoolean("no_unmute_microphone");
            boolean isRestricted = newRestrictions.getBoolean("no_unmute_microphone");
            if (wasRestricted != isRestricted) {
                AudioService.this.setMicrophoneMuteNoCallerCheck(isRestricted, userId);
            }
            wasRestricted = prevRestrictions.getBoolean("no_adjust_volume");
            isRestricted = newRestrictions.getBoolean("no_adjust_volume");
            if (wasRestricted != isRestricted) {
                AudioService.this.setMasterMuteInternalNoCallerCheck(isRestricted, AudioService.SENDMSG_REPLACE, userId);
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
                AudioService.this.mAudioHandler = new AudioHandler(null);
                AudioService.this.initHwThemeHandler();
                AudioService.this.notify();
            }
            Looper.loop();
        }
    }

    private class ControllerService extends ContentObserver {
        private ComponentName mComponent;
        private int mUid;

        public ControllerService() {
            super(null);
        }

        public String toString() {
            Object[] objArr = new Object[AudioService.SENDMSG_QUEUE];
            objArr[AudioService.SENDMSG_REPLACE] = Integer.valueOf(this.mUid);
            objArr[AudioService.SENDMSG_NOOP] = this.mComponent;
            return String.format("{mUid=%d,mComponent=%s}", objArr);
        }

        public void init() {
            onChange(true);
            AudioService.this.mContentResolver.registerContentObserver(Secure.getUriFor("volume_controller_service_component"), AudioService.DEBUG_VOL, this);
        }

        public void onChange(boolean selfChange) {
            this.mUid = AudioService.SENDMSG_REPLACE;
            this.mComponent = null;
            String setting = Secure.getString(AudioService.this.mContentResolver, "volume_controller_service_component");
            if (setting != null) {
                try {
                    this.mComponent = ComponentName.unflattenFromString(setting);
                    if (this.mComponent != null) {
                        this.mUid = AudioService.this.mContext.getPackageManager().getApplicationInfo(this.mComponent.getPackageName(), AudioService.SENDMSG_REPLACE).uid;
                        if (AudioService.DEBUG_VOL) {
                            Log.d(AudioService.TAG, "Reloaded controller service: " + this);
                        }
                    }
                } catch (Exception e) {
                    Log.w(AudioService.TAG, "Error loading controller service", e);
                }
            }
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
            return "[type:0x" + Integer.toHexString(this.mDeviceType) + " name:" + this.mDeviceName + " address:" + this.mDeviceAddress + "]";
        }
    }

    private class ForceControlStreamClient implements DeathRecipient {
        private IBinder mCb;

        ForceControlStreamClient(IBinder cb) {
            if (cb != null) {
                try {
                    cb.linkToDeath(this, AudioService.SENDMSG_REPLACE);
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
                    AudioService.this.mForceControlStreamClient = null;
                    AudioService.this.mVolumeControlStream = AudioService.SCO_MODE_UNDEFINED;
                }
            }
        }

        public void release() {
            if (this.mCb != null) {
                this.mCb.unlinkToDeath(this, AudioService.SENDMSG_REPLACE);
                this.mCb = null;
            }
        }
    }

    public static final class Lifecycle extends SystemService {
        private AudioService mService;

        public Lifecycle(Context context) {
            super(context);
            IHwAudioService audioService = HwServiceFactory.getHwAudioService();
            if (audioService != null) {
                this.mService = audioService.getInstance(context);
            } else {
                this.mService = new AudioService(context);
            }
        }

        public void onStart() {
            publishBinderService("audio", this.mService);
        }

        public void onBootPhase(int phase) {
            if (phase == SystemService.PHASE_ACTIVITY_MANAGER_READY) {
                this.mService.systemReady();
            }
        }
    }

    class LoadSoundEffectReply {
        public int mStatus;

        LoadSoundEffectReply() {
            this.mStatus = AudioService.SENDMSG_NOOP;
        }
    }

    private class MyDisplayStatusCallback implements DisplayStatusCallback {
        private MyDisplayStatusCallback() {
        }

        public void onComplete(int status) {
            if (AudioService.this.mHdmiManager != null) {
                synchronized (AudioService.this.mHdmiManager) {
                    AudioService.this.mHdmiCecSink = status != AudioService.SCO_MODE_UNDEFINED ? true : AudioService.DEBUG_VOL;
                    if (AudioService.this.isPlatformTelevision() && !AudioService.this.mHdmiCecSink) {
                        AudioService audioService = AudioService.this;
                        audioService.mFixedVolumeDevices &= -1025;
                    }
                    AudioService.this.checkAllFixedVolumeDevices();
                }
            }
        }
    }

    private class RmtSbmxFullVolDeathHandler implements DeathRecipient {
        private IBinder mICallback;

        RmtSbmxFullVolDeathHandler(IBinder cb) {
            this.mICallback = cb;
            try {
                cb.linkToDeath(this, AudioService.SENDMSG_REPLACE);
            } catch (RemoteException e) {
                Log.e(AudioService.TAG, "can't link to death", e);
            }
        }

        boolean isHandlerFor(IBinder cb) {
            return this.mICallback.equals(cb);
        }

        void forget() {
            try {
                this.mICallback.unlinkToDeath(this, AudioService.SENDMSG_REPLACE);
            } catch (NoSuchElementException e) {
                Log.e(AudioService.TAG, "error unlinking to death", e);
            }
        }

        public void binderDied() {
            Log.w(AudioService.TAG, "Recorder with remote submix at full volume died " + this.mICallback);
            AudioService.this.forceRemoteSubmixFullVolume(AudioService.DEBUG_VOL, this.mICallback);
        }
    }

    private class ScoClient implements DeathRecipient {
        private IBinder mCb;
        private int mCreatorPid;
        private int mStartcount;

        ScoClient(IBinder cb) {
            this.mCb = cb;
            this.mCreatorPid = Binder.getCallingPid();
            this.mStartcount = AudioService.SENDMSG_REPLACE;
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
                requestScoState(AudioService.MSG_REPORT_NEW_ROUTES, scoAudioMode);
                if (this.mStartcount == 0) {
                    try {
                        this.mCb.linkToDeath(this, AudioService.SENDMSG_REPLACE);
                    } catch (RemoteException e) {
                        Log.w(AudioService.TAG, "ScoClient  incCount() could not link to " + this.mCb + " binder death");
                    }
                }
                this.mStartcount += AudioService.SENDMSG_NOOP;
                if (this.mStartcount > AudioService.SENDMSG_NOOP) {
                    AudioService.this.onScoExceptionOccur(getPid());
                    this.mStartcount = AudioService.SENDMSG_NOOP;
                }
            }
        }

        public void decCount() {
            synchronized (AudioService.this.mScoClients) {
                if (this.mStartcount == 0) {
                    Log.w(AudioService.TAG, "ScoClient.decCount() already 0");
                } else {
                    this.mStartcount += AudioService.SCO_MODE_UNDEFINED;
                    if (this.mStartcount == 0) {
                        try {
                            this.mCb.unlinkToDeath(this, AudioService.SENDMSG_REPLACE);
                        } catch (NoSuchElementException e) {
                            Log.w(AudioService.TAG, "decCount() going to 0 but not registered to binder");
                        }
                    }
                    requestScoState(AudioService.MSG_SET_ALL_VOLUMES, AudioService.SENDMSG_REPLACE);
                }
            }
        }

        public void clearCount(boolean stopSco) {
            synchronized (AudioService.this.mScoClients) {
                if (this.mStartcount != 0) {
                    try {
                        this.mCb.unlinkToDeath(this, AudioService.SENDMSG_REPLACE);
                    } catch (NoSuchElementException e) {
                        Log.w(AudioService.TAG, "clearCount() mStartcount: " + this.mStartcount + " != 0 but not registered to binder");
                    }
                }
                this.mStartcount = AudioService.SENDMSG_REPLACE;
                if (stopSco) {
                    requestScoState(AudioService.MSG_SET_ALL_VOLUMES, AudioService.SENDMSG_REPLACE);
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
                count = AudioService.SENDMSG_REPLACE;
                int size = AudioService.this.mScoClients.size();
                for (int i = AudioService.SENDMSG_REPLACE; i < size; i += AudioService.SENDMSG_NOOP) {
                    count += ((ScoClient) AudioService.this.mScoClients.get(i)).getCount();
                }
            }
            return count;
        }

        private void requestScoState(int state, int scoAudioMode) {
            AudioService.this.checkScoAudioState();
            if (totalCount() != 0) {
                return;
            }
            boolean status;
            if (state == AudioService.MSG_REPORT_NEW_ROUTES) {
                AudioService.this.broadcastScoConnectionState(AudioService.SENDMSG_QUEUE);
                synchronized (AudioService.this.mSetModeDeathHandlers) {
                    if ((!AudioService.this.mSetModeDeathHandlers.isEmpty() && ((SetModeDeathHandler) AudioService.this.mSetModeDeathHandlers.get(AudioService.SENDMSG_REPLACE)).getPid() != this.mCreatorPid) || (AudioService.this.mScoAudioState != 0 && AudioService.this.mScoAudioState != AudioService.SCO_STATE_DEACTIVATE_REQ)) {
                        AudioService.this.broadcastScoConnectionState(AudioService.SENDMSG_REPLACE);
                    } else if (AudioService.this.mScoAudioState == 0) {
                        AudioService.this.mScoAudioMode = scoAudioMode;
                        if (scoAudioMode == AudioService.SCO_MODE_UNDEFINED) {
                            if (AudioService.this.mBluetoothHeadsetDevice != null) {
                                AudioService.this.mScoAudioMode = new Integer(Global.getInt(AudioService.this.mContentResolver, "bluetooth_sco_channel_" + AudioService.this.mBluetoothHeadsetDevice.getAddress(), AudioService.SENDMSG_REPLACE)).intValue();
                                if (AudioService.this.mScoAudioMode > AudioService.SENDMSG_QUEUE || AudioService.this.mScoAudioMode < 0) {
                                    AudioService.this.mScoAudioMode = AudioService.SENDMSG_REPLACE;
                                }
                            } else {
                                AudioService.this.mScoAudioMode = AudioService.SENDMSG_NOOP;
                            }
                        }
                        if (AudioService.this.mBluetoothHeadset != null && AudioService.this.mBluetoothHeadsetDevice != null) {
                            status = AudioService.DEBUG_VOL;
                            if (AudioService.this.mScoAudioMode == AudioService.SENDMSG_NOOP) {
                                status = AudioService.this.mBluetoothHeadset.connectAudio();
                            } else if (AudioService.this.mScoAudioMode == 0) {
                                status = AudioService.this.mBluetoothHeadset.startScoUsingVirtualVoiceCall(AudioService.this.mBluetoothHeadsetDevice);
                            } else if (AudioService.this.mScoAudioMode == AudioService.SENDMSG_QUEUE) {
                                status = AudioService.this.mBluetoothHeadset.startVoiceRecognition(AudioService.this.mBluetoothHeadsetDevice);
                            }
                            if (status) {
                                AudioService.this.mScoAudioState = AudioService.SCO_STATE_ACTIVE_INTERNAL;
                            } else {
                                AudioService.this.broadcastScoConnectionState(AudioService.SENDMSG_REPLACE);
                            }
                        } else if (AudioService.this.getBluetoothHeadset()) {
                            AudioService.this.mScoAudioState = AudioService.SENDMSG_NOOP;
                        }
                    } else {
                        AudioService.this.mScoAudioState = AudioService.SCO_STATE_ACTIVE_INTERNAL;
                        AudioService.this.broadcastScoConnectionState(AudioService.SENDMSG_NOOP);
                    }
                }
            } else if (state != AudioService.MSG_SET_ALL_VOLUMES) {
            } else {
                if (AudioService.this.mScoAudioState != AudioService.SCO_STATE_ACTIVE_INTERNAL && AudioService.this.mScoAudioState != AudioService.SENDMSG_NOOP) {
                    return;
                }
                if (AudioService.this.mScoAudioState != AudioService.SCO_STATE_ACTIVE_INTERNAL) {
                    AudioService.this.mScoAudioState = AudioService.SENDMSG_REPLACE;
                    AudioService.this.broadcastScoConnectionState(AudioService.SENDMSG_REPLACE);
                } else if (AudioService.this.mBluetoothHeadset != null && AudioService.this.mBluetoothHeadsetDevice != null) {
                    status = AudioService.DEBUG_VOL;
                    if (AudioService.this.mScoAudioMode == AudioService.SENDMSG_NOOP) {
                        status = AudioService.this.mBluetoothHeadset.disconnectAudio();
                    } else if (AudioService.this.mScoAudioMode == 0) {
                        status = AudioService.this.mBluetoothHeadset.stopScoUsingVirtualVoiceCall(AudioService.this.mBluetoothHeadsetDevice);
                    } else if (AudioService.this.mScoAudioMode == AudioService.SENDMSG_QUEUE) {
                        status = AudioService.this.mBluetoothHeadset.stopVoiceRecognition(AudioService.this.mBluetoothHeadsetDevice);
                    }
                    if (!status) {
                        AudioService.this.mScoAudioState = AudioService.SENDMSG_REPLACE;
                        AudioService.this.broadcastScoConnectionState(AudioService.SENDMSG_REPLACE);
                    }
                } else if (AudioService.this.getBluetoothHeadset()) {
                    AudioService.this.mScoAudioState = AudioService.SCO_STATE_DEACTIVATE_REQ;
                }
            }
        }
    }

    protected class SetModeDeathHandler implements DeathRecipient {
        private IBinder mCb;
        private int mMode;
        private int mPid;

        SetModeDeathHandler(IBinder cb, int pid) {
            this.mMode = AudioService.SENDMSG_REPLACE;
            this.mCb = cb;
            this.mPid = pid;
        }

        public void binderDied() {
            int newModeOwnerPid = AudioService.SENDMSG_REPLACE;
            synchronized (AudioService.this.mSetModeDeathHandlers) {
                Log.w(AudioService.TAG, "setMode() client died");
                if (AudioService.this.mSetModeDeathHandlers.indexOf(this) < 0) {
                    Log.w(AudioService.TAG, "unregistered setMode() client died");
                } else {
                    newModeOwnerPid = AudioService.this.setModeInt(AudioService.SENDMSG_REPLACE, this.mCb, this.mPid, AudioService.TAG);
                }
            }
            if (newModeOwnerPid != 0) {
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
    }

    private class SettingsObserver extends ContentObserver {
        private int mEncodedSurroundMode;

        SettingsObserver() {
            super(new Handler());
            AudioService.this.mContentResolver.registerContentObserver(System.getUriFor("mode_ringer_streams_affected"), AudioService.DEBUG_VOL, this);
            AudioService.this.mContentResolver.registerContentObserver(Global.getUriFor("dock_audio_media_enabled"), AudioService.DEBUG_VOL, this);
            AudioService.this.mContentResolver.registerContentObserver(System.getUriFor("master_mono"), AudioService.DEBUG_VOL, this);
            this.mEncodedSurroundMode = Global.getInt(AudioService.this.mContentResolver, "encoded_surround_output", AudioService.SENDMSG_REPLACE);
            AudioService.this.mContentResolver.registerContentObserver(Global.getUriFor("encoded_surround_output"), AudioService.DEBUG_VOL, this);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            synchronized (AudioService.this.mSettingsLock) {
                if (AudioService.this.updateRingerModeAffectedStreams()) {
                    AudioService.this.setRingerModeInt(AudioService.this.getRingerModeInternal(), AudioService.DEBUG_VOL);
                }
                AudioService.this.readDockAudioSettings(AudioService.this.mContentResolver);
                AudioService.this.updateMasterMono(AudioService.this.mContentResolver);
                updateEncodedSurroundOutput();
            }
        }

        private void updateEncodedSurroundOutput() {
            int newSurroundMode = Global.getInt(AudioService.this.mContentResolver, "encoded_surround_output", AudioService.SENDMSG_REPLACE);
            if (this.mEncodedSurroundMode != newSurroundMode) {
                AudioService.this.sendEncodedSurroundMode(newSurroundMode);
                synchronized (AudioService.this.mConnectedDevices) {
                    if (((DeviceListSpec) AudioService.this.mConnectedDevices.get(AudioService.this.makeDeviceListKey(DumpState.DUMP_PROVIDERS, ""))) != null) {
                        AudioService.this.setWiredDeviceConnectionState(DumpState.DUMP_PROVIDERS, AudioService.SENDMSG_REPLACE, "", "", "android");
                        AudioService.this.setWiredDeviceConnectionState(DumpState.DUMP_PROVIDERS, AudioService.SENDMSG_NOOP, "", "", "android");
                    }
                }
                this.mEncodedSurroundMode = newSurroundMode;
            }
        }
    }

    private final class SoundPoolCallback implements OnLoadCompleteListener {
        List<Integer> mSamples;
        int mStatus;

        private SoundPoolCallback() {
            this.mStatus = AudioService.SENDMSG_NOOP;
            this.mSamples = new ArrayList();
        }

        public int status() {
            return this.mStatus;
        }

        public void setSamples(int[] samples) {
            for (int i = AudioService.SENDMSG_REPLACE; i < samples.length; i += AudioService.SENDMSG_NOOP) {
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
            AudioService.this.mSoundPoolLooper = Looper.myLooper();
            synchronized (AudioService.this.mSoundEffectsLock) {
                if (AudioService.this.mSoundPool != null) {
                    AudioService.this.mSoundPoolCallBack = new SoundPoolCallback(null);
                    AudioService.this.mSoundPool.setOnLoadCompleteListener(AudioService.this.mSoundPoolCallBack);
                }
                AudioService.this.mSoundEffectsLock.notify();
            }
            Looper.loop();
        }
    }

    private static class StreamOverride implements TouchExplorationStateChangeListener {
        private static final int DEFAULT_STREAM_TYPE_OVERRIDE_DELAY_MS = 0;
        private static final int TOUCH_EXPLORE_STREAM_TYPE_OVERRIDE_DELAY_MS = 1000;
        static int sDelayMs;

        private StreamOverride() {
        }

        static void init(Context ctxt) {
            AccessibilityManager accessibilityManager = (AccessibilityManager) ctxt.getSystemService("accessibility");
            updateDefaultStreamOverrideDelay(accessibilityManager.isTouchExplorationEnabled());
            accessibilityManager.addTouchExplorationStateChangeListener(new StreamOverride());
        }

        public void onTouchExplorationStateChanged(boolean enabled) {
            updateDefaultStreamOverrideDelay(enabled);
        }

        private static void updateDefaultStreamOverrideDelay(boolean touchExploreEnabled) {
            if (touchExploreEnabled) {
                sDelayMs = TOUCH_EXPLORE_STREAM_TYPE_OVERRIDE_DELAY_MS;
            } else {
                sDelayMs = DEFAULT_STREAM_TYPE_OVERRIDE_DELAY_MS;
            }
            if (AudioService.DEBUG_VOL) {
                Log.d(AudioService.TAG, "Touch exploration enabled=" + touchExploreEnabled + " stream override delay is now " + sDelayMs + " ms");
            }
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
        private boolean mVisible;
        private IVrManager mVrManager;

        public void setController(IVolumeController controller) {
            this.mController = controller;
            this.mVisible = AudioService.DEBUG_VOL;
        }

        public void loadSettings(ContentResolver cr) {
            this.mLongPressTimeout = Secure.getIntForUser(cr, "long_press_timeout", AudioService.PERSIST_DELAY, -2);
        }

        public boolean suppressAdjustment(int resolvedStream, int flags, boolean isMute) {
            if (isMute) {
                return AudioService.DEBUG_VOL;
            }
            boolean suppress = AudioService.DEBUG_VOL;
            if (resolvedStream == AudioService.SENDMSG_QUEUE && this.mController != null) {
                long now = SystemClock.uptimeMillis();
                if ((flags & AudioService.SENDMSG_NOOP) != 0 && !this.mVisible) {
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
            return controller == null ? null : controller.asBinder();
        }

        public String toString() {
            return "VolumeController(" + asBinder() + ",mVisible=" + this.mVisible + ")";
        }

        public void postDisplaySafeVolumeWarning(int flags) {
            this.mVrManager = Stub.asInterface(ServiceManager.getService(VrManagerService.VR_MANAGER_BINDER_SERVICE));
            try {
                if (this.mVrManager != null && this.mVrManager.getVrModeState()) {
                    return;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "checkSafeMediaVolume cannot get VR mode");
            }
            if (this.mController != null && !HwFrameworkFactory.getVRSystemServiceManager().isVRMode()) {
                try {
                    this.mController.displaySafeVolumeWarning(flags | AudioService.SENDMSG_NOOP);
                } catch (RemoteException e2) {
                    Log.w(TAG, "Error calling displaySafeVolumeWarning", e2);
                }
            }
        }

        public void postVolumeChanged(int streamType, int flags) {
            if (this.mController != null && !HwFrameworkFactory.getVRSystemServiceManager().isVRMode()) {
                try {
                    this.mController.volumeChanged(streamType, flags);
                } catch (RemoteException e) {
                    Log.w(TAG, "Error calling volumeChanged", e);
                }
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
            if (this.mController != null) {
                try {
                    this.mController.dismiss();
                } catch (RemoteException e) {
                    Log.w(TAG, "Error calling dismiss", e);
                }
            }
        }
    }

    public class VolumeStreamState {
        private final SparseIntArray mIndexMap;
        private final int mIndexMax;
        private final int mIndexMin;
        private boolean mIsMuted;
        private int mObservedDevices;
        private final Intent mStreamDevicesChanged;
        private final int mStreamType;
        private HwCustAudioServiceVolumeStreamState mVSSCust;
        private final Intent mVolumeChanged;
        private String mVolumeIndexSettingName;

        public void readSettings() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.audio.AudioService.VolumeStreamState.readSettings():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 8 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.audio.AudioService.VolumeStreamState.readSettings():void");
        }

        private VolumeStreamState(String settingName, int streamType) {
            this.mIndexMap = new SparseIntArray(AudioService.MSG_SET_FORCE_USE);
            this.mVSSCust = null;
            this.mVolumeIndexSettingName = settingName;
            this.mStreamType = streamType;
            this.mIndexMin = AudioService.MIN_STREAM_VOLUME[streamType] * AudioService.MSG_SET_ALL_VOLUMES;
            this.mIndexMax = AudioService.MAX_STREAM_VOLUME[streamType] * AudioService.MSG_SET_ALL_VOLUMES;
            AudioSystem.initStreamVolume(streamType, this.mIndexMin / AudioService.MSG_SET_ALL_VOLUMES, this.mIndexMax / AudioService.MSG_SET_ALL_VOLUMES);
            Object[] objArr = new Object[AudioService.SENDMSG_NOOP];
            objArr[AudioService.SENDMSG_REPLACE] = AudioService.this.mContext;
            this.mVSSCust = (HwCustAudioServiceVolumeStreamState) HwCustUtils.createObj(HwCustAudioServiceVolumeStreamState.class, objArr);
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
            if (AudioService.this.mStreamVolumeAlias[this.mStreamType] == this.mStreamType) {
                EventLogTags.writeStreamDevicesChanged(this.mStreamType, prevDevices, devices);
            }
            AudioService.this.sendBroadcastToAll(this.mStreamDevicesChanged.putExtra("android.media.EXTRA_PREV_VOLUME_STREAM_DEVICES", prevDevices).putExtra("android.media.EXTRA_VOLUME_STREAM_DEVICES", devices));
            return devices;
        }

        public String getSettingNameForDevice(int device) {
            String name = this.mVolumeIndexSettingName;
            String suffix = AudioSystem.getOutputDeviceName(device);
            if (suffix.isEmpty()) {
                return name;
            }
            return name + "_" + suffix;
        }

        private int getAbsoluteVolumeIndex(int index) {
            if (index == 0) {
                return AudioService.SENDMSG_REPLACE;
            }
            if (index == AudioService.SENDMSG_NOOP) {
                return ((int) (((double) this.mIndexMax) * 0.5d)) / AudioService.MSG_SET_ALL_VOLUMES;
            }
            if (index == AudioService.SENDMSG_QUEUE) {
                return ((int) (((double) this.mIndexMax) * 0.7d)) / AudioService.MSG_SET_ALL_VOLUMES;
            }
            if (index == AudioService.SCO_STATE_ACTIVE_INTERNAL) {
                return ((int) (((double) this.mIndexMax) * 0.85d)) / AudioService.MSG_SET_ALL_VOLUMES;
            }
            return (this.mIndexMax + AudioService.SCO_STATE_DEACTIVATE_REQ) / AudioService.MSG_SET_ALL_VOLUMES;
        }

        public void applyDeviceVolume_syncVSS(int device) {
            int index;
            boolean isTurnOff = AudioService.DEBUG_VOL;
            if (this.mVSSCust != null) {
                isTurnOff = this.mVSSCust.isTurnOffAllSound();
            }
            if (this.mIsMuted || r1) {
                index = AudioService.SENDMSG_REPLACE;
            } else if ((device & 896) != 0 && AudioService.this.mAvrcpAbsVolSupported) {
                index = getAbsoluteVolumeIndex((getIndex(device) + AudioService.SCO_STATE_DEACTIVATE_REQ) / AudioService.MSG_SET_ALL_VOLUMES);
            } else if ((AudioService.this.mFullVolumeDevices & device) != 0) {
                index = (this.mIndexMax + AudioService.SCO_STATE_DEACTIVATE_REQ) / AudioService.MSG_SET_ALL_VOLUMES;
            } else {
                index = (getIndex(device) + AudioService.SCO_STATE_DEACTIVATE_REQ) / AudioService.MSG_SET_ALL_VOLUMES;
            }
            AudioSystem.setStreamVolumeIndex(this.mStreamType, index, device);
        }

        public void applyAllVolumes() {
            synchronized (VolumeStreamState.class) {
                int index;
                boolean isTurnOff = AudioService.DEBUG_VOL;
                if (this.mVSSCust != null) {
                    isTurnOff = this.mVSSCust.isTurnOffAllSound();
                }
                for (int i = AudioService.SENDMSG_REPLACE; i < this.mIndexMap.size(); i += AudioService.SENDMSG_NOOP) {
                    int device = this.mIndexMap.keyAt(i);
                    if (device != 1073741824) {
                        if (this.mIsMuted || r3) {
                            index = AudioService.SENDMSG_REPLACE;
                        } else if ((device & 896) != 0 && AudioService.this.mAvrcpAbsVolSupported) {
                            index = getAbsoluteVolumeIndex((getIndex(device) + AudioService.SCO_STATE_DEACTIVATE_REQ) / AudioService.MSG_SET_ALL_VOLUMES);
                        } else if ((AudioService.this.mFullVolumeDevices & device) != 0) {
                            index = (this.mIndexMax + AudioService.SCO_STATE_DEACTIVATE_REQ) / AudioService.MSG_SET_ALL_VOLUMES;
                        } else {
                            index = (this.mIndexMap.valueAt(i) + AudioService.SCO_STATE_DEACTIVATE_REQ) / AudioService.MSG_SET_ALL_VOLUMES;
                        }
                        AudioSystem.setStreamVolumeIndex(this.mStreamType, index, device);
                    }
                }
                if (this.mIsMuted) {
                    index = AudioService.SENDMSG_REPLACE;
                } else {
                    index = (getIndex(1073741824) + AudioService.SCO_STATE_DEACTIVATE_REQ) / AudioService.MSG_SET_ALL_VOLUMES;
                }
                AudioSystem.setStreamVolumeIndex(this.mStreamType, index, 1073741824);
                if (this.mVSSCust != null) {
                    this.mVSSCust.applyAllVolumes(this.mIsMuted, this.mStreamType);
                }
            }
        }

        public boolean adjustIndex(int deltaIndex, int device, String caller) {
            return setIndex(getIndex(device) + deltaIndex, device, caller);
        }

        public boolean setIndex(int index, int device, String caller) {
            boolean changed;
            synchronized (VolumeStreamState.class) {
                int oldIndex = getIndex(device);
                index = getValidIndex(index);
                synchronized (AudioService.this.mCameraSoundForced) {
                    if (this.mStreamType == AudioService.MSG_LOAD_SOUND_EFFECTS && AudioService.this.mCameraSoundForced.booleanValue()) {
                        index = this.mIndexMax;
                    }
                }
                this.mIndexMap.put(device, index);
                if (oldIndex != index) {
                    changed = true;
                } else {
                    changed = AudioService.DEBUG_VOL;
                }
                if (changed) {
                    boolean currentDevice = device == AudioService.this.getDeviceForStream(this.mStreamType) ? true : AudioService.DEBUG_VOL;
                    int streamType = AudioSystem.getNumStreamTypes() + AudioService.SCO_MODE_UNDEFINED;
                    while (streamType >= 0) {
                        if (streamType != this.mStreamType && AudioService.this.mStreamVolumeAlias[streamType] == this.mStreamType) {
                            int scaledIndex = AudioService.this.rescaleIndex(index, this.mStreamType, streamType);
                            AudioService.this.mStreamStates[streamType].setIndex(scaledIndex, device, caller);
                            if (currentDevice) {
                                AudioService.this.mStreamStates[streamType].setIndex(scaledIndex, AudioService.this.getDeviceForStream(streamType), caller);
                            }
                        }
                        streamType += AudioService.SCO_MODE_UNDEFINED;
                    }
                }
            }
            if (changed) {
                oldIndex = (oldIndex + AudioService.SCO_STATE_DEACTIVATE_REQ) / AudioService.MSG_SET_ALL_VOLUMES;
                index = (index + AudioService.SCO_STATE_DEACTIVATE_REQ) / AudioService.MSG_SET_ALL_VOLUMES;
                if (AudioService.this.mStreamVolumeAlias[this.mStreamType] == this.mStreamType) {
                    if (caller == null) {
                        Log.w(AudioService.TAG, "No caller for volume_changed event", new Throwable());
                    }
                    EventLogTags.writeVolumeChanged(this.mStreamType, oldIndex, index, this.mIndexMax / AudioService.MSG_SET_ALL_VOLUMES, caller);
                }
                this.mVolumeChanged.putExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", index);
                this.mVolumeChanged.putExtra("android.media.EXTRA_PREV_VOLUME_STREAM_VALUE", oldIndex);
                this.mVolumeChanged.putExtra("android.media.EXTRA_VOLUME_STREAM_TYPE_ALIAS", AudioService.this.mStreamVolumeAlias[this.mStreamType]);
                AudioService.this.sendBroadcastToAll(this.mVolumeChanged);
            }
            return changed;
        }

        public int getIndex(int device) {
            int index;
            synchronized (VolumeStreamState.class) {
                index = this.mIndexMap.get(device, AudioService.SCO_MODE_UNDEFINED);
                if (index == AudioService.SCO_MODE_UNDEFINED) {
                    index = this.mIndexMap.get(1073741824);
                }
            }
            return index;
        }

        public int getMaxIndex() {
            return this.mIndexMax;
        }

        public int getMinIndex() {
            return this.mIndexMin;
        }

        public void setAllIndexes(VolumeStreamState srcStream, String caller) {
            synchronized (VolumeStreamState.class) {
                int i;
                int srcStreamType = srcStream.getStreamType();
                int index = AudioService.this.rescaleIndex(srcStream.getIndex(1073741824), srcStreamType, this.mStreamType);
                for (i = AudioService.SENDMSG_REPLACE; i < this.mIndexMap.size(); i += AudioService.SENDMSG_NOOP) {
                    this.mIndexMap.put(this.mIndexMap.keyAt(i), index);
                }
                SparseIntArray srcMap = srcStream.mIndexMap;
                for (i = AudioService.SENDMSG_REPLACE; i < srcMap.size(); i += AudioService.SENDMSG_NOOP) {
                    setIndex(AudioService.this.rescaleIndex(srcMap.valueAt(i), srcStreamType, this.mStreamType), srcMap.keyAt(i), caller);
                }
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void setAllIndexesToMax() {
            synchronized (VolumeStreamState.class) {
                int i = AudioService.SENDMSG_REPLACE;
                while (true) {
                    if (i < this.mIndexMap.size()) {
                        this.mIndexMap.put(this.mIndexMap.keyAt(i), this.mIndexMax);
                        i += AudioService.SENDMSG_NOOP;
                    }
                }
            }
        }

        public void mute(boolean state) {
            boolean changed = AudioService.DEBUG_VOL;
            synchronized (VolumeStreamState.class) {
                if (state != this.mIsMuted) {
                    changed = true;
                    this.mIsMuted = state;
                    AudioService.sendMsg(AudioService.this.mAudioHandler, AudioService.MSG_SET_ALL_VOLUMES, AudioService.SENDMSG_QUEUE, AudioService.SENDMSG_REPLACE, AudioService.SENDMSG_REPLACE, this, AudioService.SENDMSG_REPLACE);
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
                if (AudioService.this.mStreamVolumeAlias[this.mStreamType] == AudioService.SCO_STATE_ACTIVE_INTERNAL) {
                    for (int i = AudioService.SENDMSG_REPLACE; i < this.mIndexMap.size(); i += AudioService.SENDMSG_NOOP) {
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

        private void dump(PrintWriter pw) {
            int i;
            pw.print("   Muted: ");
            pw.println(this.mIsMuted);
            pw.print("   Min: ");
            pw.println((this.mIndexMin + AudioService.SCO_STATE_DEACTIVATE_REQ) / AudioService.MSG_SET_ALL_VOLUMES);
            pw.print("   Max: ");
            pw.println((this.mIndexMax + AudioService.SCO_STATE_DEACTIVATE_REQ) / AudioService.MSG_SET_ALL_VOLUMES);
            pw.print("   Current: ");
            for (i = AudioService.SENDMSG_REPLACE; i < this.mIndexMap.size(); i += AudioService.SENDMSG_NOOP) {
                String deviceName;
                if (i > 0) {
                    pw.print(", ");
                }
                int device = this.mIndexMap.keyAt(i);
                pw.print(Integer.toHexString(device));
                if (device == 1073741824) {
                    deviceName = "default";
                } else {
                    deviceName = AudioSystem.getOutputDeviceName(device);
                }
                if (!deviceName.isEmpty()) {
                    pw.print(" (");
                    pw.print(deviceName);
                    pw.print(")");
                }
                pw.print(": ");
                pw.print((this.mIndexMap.valueAt(i) + AudioService.SCO_STATE_DEACTIVATE_REQ) / AudioService.MSG_SET_ALL_VOLUMES);
            }
            pw.println();
            pw.print("   Devices: ");
            int devices = AudioService.this.getDevicesForStream(this.mStreamType);
            i = AudioService.SENDMSG_REPLACE;
            int n = AudioService.SENDMSG_REPLACE;
            while (true) {
                device = AudioService.SENDMSG_NOOP << i;
                if (device != 1073741824) {
                    int n2;
                    if ((devices & device) != 0) {
                        n2 = n + AudioService.SENDMSG_NOOP;
                        if (n > 0) {
                            pw.print(", ");
                        }
                        pw.print(AudioSystem.getOutputDeviceName(device));
                    } else {
                        n2 = n;
                    }
                    i += AudioService.SENDMSG_NOOP;
                    n = n2;
                } else {
                    return;
                }
            }
        }
    }

    private class WiredDeviceConnectionState {
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.audio.AudioService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.audio.AudioService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.audio.AudioService.<clinit>():void");
    }

    private void checkMuteAffectedStreams() {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.audio.AudioService.checkMuteAffectedStreams():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.audio.AudioService.checkMuteAffectedStreams():void");
    }

    private void dumpRingerModeStreams(java.io.PrintWriter r1, java.lang.String r2, int r3) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.audio.AudioService.dumpRingerModeStreams(java.io.PrintWriter, java.lang.String, int):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.audio.AudioService.dumpRingerModeStreams(java.io.PrintWriter, java.lang.String, int):void");
    }

    private void enforceSafeMediaVolume(java.lang.String r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.audio.AudioService.enforceSafeMediaVolume(java.lang.String):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.audio.AudioService.enforceSafeMediaVolume(java.lang.String):void");
    }

    private void muteRingerModeStreams() {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.audio.AudioService.muteRingerModeStreams():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.audio.AudioService.muteRingerModeStreams():void");
    }

    private void sendDeviceConnectionIntent(int r1, int r2, java.lang.String r3, java.lang.String r4) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.audio.AudioService.sendDeviceConnectionIntent(int, int, java.lang.String, java.lang.String):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.audio.AudioService.sendDeviceConnectionIntent(int, int, java.lang.String, java.lang.String):void");
    }

    private boolean isPlatformVoice() {
        return this.mPlatformType == SENDMSG_NOOP ? true : DEBUG_VOL;
    }

    private boolean isPlatformTelevision() {
        return this.mPlatformType == SENDMSG_QUEUE ? true : DEBUG_VOL;
    }

    private String makeDeviceListKey(int device, String deviceAddress) {
        return "0x" + Integer.toHexString(device) + ":" + deviceAddress;
    }

    public static String makeAlsaAddressString(int card, int device) {
        return "card=" + card + ";device=" + device + ";";
    }

    public AudioService(Context context) {
        int i;
        boolean z;
        boolean z2 = true;
        this.mVolumeController = new VolumeController();
        this.mControllerService = new ControllerService();
        this.mMode = SENDMSG_REPLACE;
        this.mSettingsLock = new Object();
        this.mSoundEffectsLock = new Object();
        this.mFactoryMode = "factory".equals(SystemProperties.get("ro.runmode", "normal"));
        this.SOUND_EFFECT_FILES_MAP = (int[][]) Array.newInstance(Integer.TYPE, new int[]{MSG_SET_ALL_VOLUMES, SENDMSG_QUEUE});
        this.STREAM_VOLUME_ALIAS_VOICE = new int[]{SENDMSG_REPLACE, SENDMSG_QUEUE, SENDMSG_QUEUE, SCO_STATE_ACTIVE_INTERNAL, SCO_STATE_DEACTIVATE_EXT_REQ, SENDMSG_QUEUE, MSG_BTA2DP_DOCK_TIMEOUT, SENDMSG_QUEUE, SENDMSG_QUEUE, SCO_STATE_ACTIVE_INTERNAL};
        this.STREAM_VOLUME_ALIAS_TELEVISION = new int[]{SCO_STATE_ACTIVE_INTERNAL, SCO_STATE_ACTIVE_INTERNAL, SCO_STATE_ACTIVE_INTERNAL, SCO_STATE_ACTIVE_INTERNAL, SCO_STATE_ACTIVE_INTERNAL, SCO_STATE_ACTIVE_INTERNAL, SCO_STATE_ACTIVE_INTERNAL, SCO_STATE_ACTIVE_INTERNAL, SCO_STATE_ACTIVE_INTERNAL, SCO_STATE_ACTIVE_INTERNAL};
        this.STREAM_VOLUME_ALIAS_DEFAULT = new int[]{SENDMSG_REPLACE, SENDMSG_QUEUE, SENDMSG_QUEUE, SCO_STATE_ACTIVE_INTERNAL, SCO_STATE_DEACTIVATE_EXT_REQ, SENDMSG_QUEUE, MSG_BTA2DP_DOCK_TIMEOUT, SENDMSG_QUEUE, SENDMSG_QUEUE, SCO_STATE_ACTIVE_INTERNAL};
        this.mAudioSystemCallback = new ErrorCallback() {
            public void onError(int error) {
                AudioService.this.onErrorCallBackEx(error);
                switch (error) {
                    case AudioService.MSG_SET_WIRED_DEVICE_CONNECTION_STATE /*100*/:
                        AudioService.sendMsg(AudioService.this.mAudioHandler, AudioService.SCO_STATE_DEACTIVATE_EXT_REQ, AudioService.SENDMSG_NOOP, AudioService.SENDMSG_REPLACE, AudioService.SENDMSG_REPLACE, null, AudioService.SENDMSG_REPLACE);
                    default:
                }
            }
        };
        this.mRingerModeExternal = SCO_MODE_UNDEFINED;
        this.mRingerModeAffectedStreams = SENDMSG_REPLACE;
        this.mReceiver = new AudioServiceBroadcastReceiver();
        this.mUserRestrictionsListener = new AudioServiceUserRestrictionsListener();
        this.mConnectedDevices = new ArrayMap();
        this.mSetModeDeathHandlers = new ArrayList();
        this.mScoClients = new ArrayList();
        this.mSoundPoolLooper = null;
        this.mPrevVolDirection = SENDMSG_REPLACE;
        this.mVolumeControlStream = SCO_MODE_UNDEFINED;
        this.mForceControlStreamLock = new Object();
        this.mForceControlStreamClient = null;
        this.mDeviceOrientation = SENDMSG_REPLACE;
        this.mBluetoothA2dpEnabledLock = new Object();
        this.mCurAudioRoutes = new AudioRoutesInfo();
        this.mRoutesObservers = new RemoteCallbackList();
        this.mFixedVolumeDevices = 2890752;
        this.mFullVolumeDevices = SENDMSG_REPLACE;
        this.mDockAudioMediaEnabled = true;
        this.mDockState = SENDMSG_REPLACE;
        this.mA2dpAvrcpLock = new Object();
        this.mAvrcpAbsVolSupported = DEBUG_VOL;
        this.mVolumePolicy = VolumePolicy.DEFAULT;
        this.mScreenOn = true;
        this.mCust = null;
        this.mIsHisiPlatform = DEBUG_VOL;
        this.mRmtSbmxFullVolRefCount = SENDMSG_REPLACE;
        this.mRmtSbmxFullVolDeathHandlers = new ArrayList();
        this.mBluetoothProfileServiceListener = new ServiceListener() {
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                Log.d(AudioService.TAG, "onServiceConnected profile=" + profile);
                List<BluetoothDevice> deviceList;
                BluetoothDevice btDevice;
                switch (profile) {
                    case AudioService.SENDMSG_NOOP /*1*/:
                        synchronized (AudioService.this.mScoClients) {
                            AudioService.this.mAudioHandler.removeMessages(AudioService.MSG_BT_HEADSET_CNCT_FAILED);
                            AudioService.this.mBluetoothHeadset = (BluetoothHeadset) proxy;
                            deviceList = AudioService.this.mBluetoothHeadset.getConnectedDevices();
                            if (deviceList.size() <= 0) {
                                AudioService.this.mBluetoothHeadsetDevice = null;
                                break;
                            }
                            AudioService.this.mBluetoothHeadsetDevice = (BluetoothDevice) deviceList.get(AudioService.SENDMSG_REPLACE);
                            AudioService.this.checkScoAudioState();
                            if (!(AudioService.this.mScoAudioState == AudioService.SENDMSG_NOOP || AudioService.this.mScoAudioState == AudioService.SCO_STATE_DEACTIVATE_REQ)) {
                                if (AudioService.this.mScoAudioState == AudioService.SCO_STATE_DEACTIVATE_EXT_REQ) {
                                }
                                break;
                            }
                            boolean status = AudioService.DEBUG_VOL;
                            if (AudioService.this.mBluetoothHeadsetDevice != null) {
                                switch (AudioService.this.mScoAudioState) {
                                    case AudioService.SENDMSG_NOOP /*1*/:
                                        AudioService.this.mScoAudioState = AudioService.SCO_STATE_ACTIVE_INTERNAL;
                                        if (AudioService.this.mScoAudioMode != AudioService.SENDMSG_NOOP) {
                                            if (AudioService.this.mScoAudioMode != 0) {
                                                if (AudioService.this.mScoAudioMode == AudioService.SENDMSG_QUEUE) {
                                                    status = AudioService.this.mBluetoothHeadset.startVoiceRecognition(AudioService.this.mBluetoothHeadsetDevice);
                                                    break;
                                                }
                                            }
                                            status = AudioService.this.mBluetoothHeadset.startScoUsingVirtualVoiceCall(AudioService.this.mBluetoothHeadsetDevice);
                                            break;
                                        }
                                        status = AudioService.this.mBluetoothHeadset.connectAudio();
                                        break;
                                        break;
                                    case AudioService.SCO_STATE_DEACTIVATE_EXT_REQ /*4*/:
                                        status = AudioService.this.mBluetoothHeadset.stopVoiceRecognition(AudioService.this.mBluetoothHeadsetDevice);
                                        break;
                                    case AudioService.SCO_STATE_DEACTIVATE_REQ /*5*/:
                                        if (AudioService.this.mScoAudioMode != AudioService.SENDMSG_NOOP) {
                                            if (AudioService.this.mScoAudioMode != 0) {
                                                if (AudioService.this.mScoAudioMode == AudioService.SENDMSG_QUEUE) {
                                                    status = AudioService.this.mBluetoothHeadset.stopVoiceRecognition(AudioService.this.mBluetoothHeadsetDevice);
                                                    break;
                                                }
                                            }
                                            status = AudioService.this.mBluetoothHeadset.stopScoUsingVirtualVoiceCall(AudioService.this.mBluetoothHeadsetDevice);
                                            break;
                                        }
                                        status = AudioService.this.mBluetoothHeadset.disconnectAudio();
                                        break;
                                        break;
                                    default:
                                        Log.i(AudioService.TAG, "resolve findbugs");
                                        break;
                                }
                            }
                            if (!status) {
                                AudioService.sendMsg(AudioService.this.mAudioHandler, AudioService.MSG_BT_HEADSET_CNCT_FAILED, AudioService.SENDMSG_REPLACE, AudioService.SENDMSG_REPLACE, AudioService.SENDMSG_REPLACE, null, AudioService.SENDMSG_REPLACE);
                            }
                            break;
                        }
                    case AudioService.SENDMSG_QUEUE /*2*/:
                        synchronized (AudioService.this.mConnectedDevices) {
                            synchronized (AudioService.this.mA2dpAvrcpLock) {
                                AudioService.this.mA2dp = (BluetoothA2dp) proxy;
                                deviceList = AudioService.this.mA2dp.getConnectedDevices();
                                if (deviceList.size() > 0) {
                                    btDevice = (BluetoothDevice) deviceList.get(AudioService.SENDMSG_REPLACE);
                                    int state = AudioService.this.mA2dp.getConnectionState(btDevice);
                                    AudioService.this.queueMsgUnderWakeLock(AudioService.this.mAudioHandler, AudioService.MSG_SET_A2DP_SINK_CONNECTION_STATE, state, AudioService.SENDMSG_REPLACE, btDevice, AudioService.this.checkSendBecomingNoisyIntent(DumpState.DUMP_PACKAGES, state == AudioService.SENDMSG_QUEUE ? AudioService.SENDMSG_NOOP : AudioService.SENDMSG_REPLACE));
                                }
                                break;
                            }
                            break;
                        }
                    case H.WINDOW_FREEZE_TIMEOUT /*11*/:
                        deviceList = proxy.getConnectedDevices();
                        if (deviceList.size() > 0) {
                            btDevice = (BluetoothDevice) deviceList.get(AudioService.SENDMSG_REPLACE);
                            synchronized (AudioService.this.mConnectedDevices) {
                                AudioService.this.queueMsgUnderWakeLock(AudioService.this.mAudioHandler, AudioService.MSG_SET_A2DP_SRC_CONNECTION_STATE, proxy.getConnectionState(btDevice), AudioService.SENDMSG_REPLACE, btDevice, AudioService.SENDMSG_REPLACE);
                                break;
                            }
                        }
                    default:
                }
            }

            public void onServiceDisconnected(int profile) {
                Log.d(AudioService.TAG, "onServiceDisconnected profile=" + profile);
                switch (profile) {
                    case AudioService.SENDMSG_NOOP /*1*/:
                        AudioService.this.disconnectHeadset();
                    case AudioService.SENDMSG_QUEUE /*2*/:
                        AudioService.this.disconnectA2dp();
                    case H.WINDOW_FREEZE_TIMEOUT /*11*/:
                        AudioService.this.disconnectA2dpSink();
                    default:
                }
            }
        };
        this.mBecomingNoisyIntentDevices = 163724;
        this.mMcc = SENDMSG_REPLACE;
        this.mSafeMediaVolumeDevices = MSG_REPORT_NEW_ROUTES;
        this.mHdmiSystemAudioSupported = DEBUG_VOL;
        this.mHdmiDisplayStatusCallback = new MyDisplayStatusCallback();
        this.mDynPolicyCallback = new DynamicPolicyCallback() {
            public void onDynamicPolicyMixStateUpdate(String regId, int state) {
                if (!TextUtils.isEmpty(regId)) {
                    AudioService.sendMsg(AudioService.this.mAudioHandler, AudioService.MSG_DYN_POLICY_MIX_STATE_UPDATE, AudioService.SENDMSG_QUEUE, state, AudioService.SENDMSG_REPLACE, regId, AudioService.SENDMSG_REPLACE);
                }
            }
        };
        this.mRecordMonitor = new RecordingActivityMonitor();
        this.mAudioPolicies = new HashMap();
        this.mAudioPolicyCounter = SENDMSG_REPLACE;
        this.mContext = context;
        this.mContentResolver = context.getContentResolver();
        Object[] objArr = new Object[SENDMSG_NOOP];
        objArr[SENDMSG_REPLACE] = this.mContext;
        this.mCust = (HwCustAudioService) HwCustUtils.createObj(HwCustAudioService.class, objArr);
        this.mAppOps = (AppOpsManager) context.getSystemService("appops");
        this.mPlatformType = AudioSystem.getPlatformType(context);
        this.mUserManagerInternal = (UserManagerInternal) LocalServices.getService(UserManagerInternal.class);
        this.mAudioEventWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(SENDMSG_NOOP, "handleAudioEvent");
        Vibrator vibrator = (Vibrator) context.getSystemService("vibrator");
        this.mHasVibrator = vibrator == null ? DEBUG_VOL : vibrator.hasVibrator();
        int maxVolume = SystemProperties.getInt("ro.config.vc_call_vol_steps", MAX_STREAM_VOLUME[SENDMSG_REPLACE]);
        if (maxVolume != MAX_STREAM_VOLUME[SENDMSG_REPLACE]) {
            MAX_STREAM_VOLUME[SENDMSG_REPLACE] = maxVolume;
            AudioSystem.DEFAULT_STREAM_VOLUME[SENDMSG_REPLACE] = (maxVolume * SCO_STATE_ACTIVE_INTERNAL) / SCO_STATE_DEACTIVATE_EXT_REQ;
        }
        maxVolume = SystemProperties.getInt("ro.config.media_vol_steps", MAX_STREAM_VOLUME[SCO_STATE_ACTIVE_INTERNAL]);
        if (maxVolume != MAX_STREAM_VOLUME[SCO_STATE_ACTIVE_INTERNAL]) {
            MAX_STREAM_VOLUME[SCO_STATE_ACTIVE_INTERNAL] = maxVolume;
            AudioSystem.DEFAULT_STREAM_VOLUME[SCO_STATE_ACTIVE_INTERNAL] = (maxVolume * SCO_STATE_ACTIVE_INTERNAL) / SCO_STATE_DEACTIVATE_EXT_REQ;
        }
        sSoundEffectVolumeDb = context.getResources().getInteger(17694724);
        this.mForcedUseForComm = SENDMSG_REPLACE;
        createAudioSystemThread();
        AudioSystem.setErrorCallback(this.mAudioSystemCallback);
        boolean cameraSoundForced = readCameraSoundForced();
        this.mCameraSoundForced = new Boolean(cameraSoundForced);
        Handler handler = this.mAudioHandler;
        if (cameraSoundForced) {
            i = 11;
        } else {
            i = SENDMSG_REPLACE;
        }
        sendMsg(handler, MSG_SET_FORCE_USE, SENDMSG_QUEUE, SCO_STATE_DEACTIVATE_EXT_REQ, i, null, SENDMSG_REPLACE);
        this.mSafeMediaVolumeState = new Integer(Global.getInt(this.mContentResolver, "audio_safe_volume_state", SENDMSG_REPLACE));
        this.mSafeMediaVolumeIndex = this.mContext.getResources().getInteger(17694863) * MSG_SET_ALL_VOLUMES;
        if (usingHwSafeMediaConfig()) {
            int intValue;
            this.mSafeMediaVolumeIndex = getHwSafeMediaVolumeIndex();
            if (isHwSafeMediaVolumeEnabled()) {
                intValue = new Integer(Global.getInt(this.mContentResolver, "audio_safe_volume_state", SENDMSG_REPLACE)).intValue();
            } else {
                intValue = SENDMSG_NOOP;
            }
            this.mSafeMediaVolumeState = Integer.valueOf(intValue);
        }
        this.mUseFixedVolume = this.mContext.getResources().getBoolean(17956995);
        updateStreamVolumeAlias(DEBUG_VOL, TAG);
        readPersistedSettings();
        readUserRestrictions();
        this.mSettingsObserver = new SettingsObserver();
        createStreamStates();
        this.mMediaFocusControl = new MediaFocusControl(this.mContext);
        readAndSetLowRamDevice();
        this.mRingerModeMutedStreams = SENDMSG_REPLACE;
        setRingerModeInt(getRingerModeInternal(), DEBUG_VOL);
        IntentFilter intentFilter = new IntentFilter("android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED");
        intentFilter.addAction("android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED");
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
        if (SystemProperties.getBoolean("ro.audio.monitorOrientation", DEBUG_VOL)) {
            z = true;
        } else {
            z = SPK_RCV_STEREO_SUPPORT;
        }
        this.mMonitorOrientation = z;
        if (this.mMonitorOrientation) {
            Log.v(TAG, "monitoring device orientation");
            setOrientationForAudioSystem();
        }
        if (!SystemProperties.getBoolean("ro.audio.monitorRotation", DEBUG_VOL)) {
            z2 = SPK_RCV_STEREO_SUPPORT;
        }
        this.mMonitorRotation = z2;
        if (this.mMonitorRotation) {
            RotationHelper.init(this.mContext, this.mAudioHandler);
        }
        context.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, intentFilter, null, null);
        LocalServices.addService(AudioManagerInternal.class, new AudioServiceInternal());
        this.mUserManagerInternal.addUserRestrictionsListener(this.mUserRestrictionsListener);
        this.mRecordMonitor.initMonitor();
        this.mIsHisiPlatform = isHisiPlatform();
    }

    public void systemReady() {
        sendMsg(this.mAudioHandler, MSG_SYSTEM_READY, SENDMSG_QUEUE, SENDMSG_REPLACE, SENDMSG_REPLACE, null, SENDMSG_REPLACE);
    }

    public void onSystemReady() {
        this.mSystemReady = true;
        sendMsg(this.mAudioHandler, MSG_LOAD_SOUND_EFFECTS, SENDMSG_QUEUE, SENDMSG_REPLACE, SENDMSG_REPLACE, null, SENDMSG_REPLACE);
        this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
        this.mScoConnectionState = SCO_MODE_UNDEFINED;
        resetBluetoothSco();
        getBluetoothHeadset();
        Intent newIntent = new Intent("android.media.SCO_AUDIO_STATE_CHANGED");
        newIntent.putExtra("android.media.extra.SCO_AUDIO_STATE", SENDMSG_REPLACE);
        sendStickyBroadcastToAll(newIntent);
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            adapter.getProfileProxy(this.mContext, this.mBluetoothProfileServiceListener, SENDMSG_QUEUE);
        }
        this.mHdmiManager = (HdmiControlManager) this.mContext.getSystemService("hdmi_control");
        if (this.mHdmiManager != null) {
            synchronized (this.mHdmiManager) {
                this.mHdmiTvClient = this.mHdmiManager.getTvClient();
                if (this.mHdmiTvClient != null) {
                    this.mFixedVolumeDevices &= -2883587;
                }
                this.mHdmiPlaybackClient = this.mHdmiManager.getPlaybackClient();
                this.mHdmiCecSink = DEBUG_VOL;
            }
        }
        this.mNm = (NotificationManager) this.mContext.getSystemService("notification");
        sendMsg(this.mAudioHandler, MSG_CONFIGURE_SAFE_MEDIA_VOLUME_FORCED, SENDMSG_REPLACE, SENDMSG_REPLACE, SENDMSG_REPLACE, TAG, SAFE_VOLUME_CONFIGURE_TIMEOUT_MS);
        StreamOverride.init(this.mContext);
        this.mControllerService.init();
        onIndicateSystemReady();
    }

    void onIndicateSystemReady() {
        if (AudioSystem.systemReady() != 0) {
            sendMsg(this.mAudioHandler, MSG_INDICATE_SYSTEM_READY, SENDMSG_REPLACE, SENDMSG_REPLACE, SENDMSG_REPLACE, null, INDICATE_SYSTEM_READY_RETRY_DELAY_MS);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onAudioServerDied() {
        if (this.mSystemReady && AudioSystem.checkAudioFlinger() == 0) {
            Log.e(TAG, "Audioserver started.");
            AudioSystem.setParameters("restarting=true");
            readAndSetLowRamDevice();
            synchronized (this.mConnectedDevices) {
                int i = SENDMSG_REPLACE;
                while (true) {
                    if (i >= this.mConnectedDevices.size()) {
                        break;
                    }
                    DeviceListSpec spec = (DeviceListSpec) this.mConnectedDevices.valueAt(i);
                    AudioSystem.setDeviceConnectionState(spec.mDeviceType, SENDMSG_NOOP, spec.mDeviceAddress, spec.mDeviceName);
                    i += SENDMSG_NOOP;
                }
            }
            AudioSystem.setPhoneState(this.mMode);
            AudioSystem.setForceUse(SENDMSG_REPLACE, this.mForcedUseForComm);
            AudioSystem.setForceUse(SENDMSG_QUEUE, this.mForcedUseForComm);
            AudioSystem.setForceUse(SCO_STATE_DEACTIVATE_EXT_REQ, this.mCameraSoundForced.booleanValue() ? 11 : SENDMSG_REPLACE);
            sendCommForceBroadcast();
            for (int streamType = AudioSystem.getNumStreamTypes() + SCO_MODE_UNDEFINED; streamType >= 0; streamType += SCO_MODE_UNDEFINED) {
                VolumeStreamState streamState = this.mStreamStates[streamType];
                if (streamState != null) {
                    AudioSystem.initStreamVolume(streamType, streamState.mIndexMin / MSG_SET_ALL_VOLUMES, streamState.mIndexMax / MSG_SET_ALL_VOLUMES);
                    streamState.applyAllVolumes();
                }
            }
            updateMasterMono(this.mContentResolver);
            setRingerModeInt(getRingerModeInternal(), DEBUG_VOL);
            processMediaServerRestart();
            if (this.mMonitorOrientation) {
                setOrientationForAudioSystem();
            }
            if (this.mMonitorRotation) {
                RotationHelper.updateOrientation();
            }
            synchronized (this.mBluetoothA2dpEnabledLock) {
                AudioSystem.setForceUse(SENDMSG_NOOP, this.mBluetoothA2dpEnabled ? SENDMSG_REPLACE : MSG_SET_ALL_VOLUMES);
            }
            synchronized (this.mSettingsLock) {
                AudioSystem.setForceUse(SCO_STATE_ACTIVE_INTERNAL, this.mDockAudioMediaEnabled ? MSG_SET_FORCE_USE : SENDMSG_REPLACE);
                sendEncodedSurroundMode(this.mContentResolver);
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
                sendMsg(this.mAudioHandler, 10001, SENDMSG_REPLACE, SENDMSG_REPLACE, SENDMSG_REPLACE, null, PERSIST_DELAY);
            }
            return;
        }
        Log.e(TAG, "Audioserver died.");
        sendMsg(this.mAudioHandler, SCO_STATE_DEACTIVATE_EXT_REQ, SENDMSG_NOOP, SENDMSG_REPLACE, SENDMSG_REPLACE, null, PERSIST_DELAY);
    }

    private void createAudioSystemThread() {
        this.mAudioSystemThread = new AudioSystemThread();
        this.mAudioSystemThread.start();
        waitForAudioHandlerCreation();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void waitForAudioHandlerCreation() {
        synchronized (this) {
            while (true) {
                if (this.mAudioHandler == null) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Interrupted while waiting on volume handler.");
                    }
                }
            }
        }
    }

    private void checkAllAliasStreamVolumes() {
        synchronized (VolumeStreamState.class) {
            int numStreamTypes = AudioSystem.getNumStreamTypes();
            for (int streamType = SENDMSG_REPLACE; streamType < numStreamTypes; streamType += SENDMSG_NOOP) {
                if (streamType != this.mStreamVolumeAlias[streamType]) {
                    this.mStreamStates[streamType].setAllIndexes(this.mStreamStates[this.mStreamVolumeAlias[streamType]], TAG);
                }
                if (!this.mStreamStates[streamType].mIsMuted) {
                    this.mStreamStates[streamType].applyAllVolumes();
                }
            }
        }
    }

    private void checkAllFixedVolumeDevices() {
        int numStreamTypes = AudioSystem.getNumStreamTypes();
        for (int streamType = SENDMSG_REPLACE; streamType < numStreamTypes; streamType += SENDMSG_NOOP) {
            this.mStreamStates[streamType].checkFixedVolumeDevices();
        }
    }

    private void checkAllFixedVolumeDevices(int streamType) {
        this.mStreamStates[streamType].checkFixedVolumeDevices();
    }

    private void createStreamStates() {
        int numStreamTypes = AudioSystem.getNumStreamTypes();
        VolumeStreamState[] streams = new VolumeStreamState[numStreamTypes];
        this.mStreamStates = streams;
        for (int i = SENDMSG_REPLACE; i < numStreamTypes; i += SENDMSG_NOOP) {
            streams[i] = new VolumeStreamState(System.VOLUME_SETTINGS[this.mStreamVolumeAlias[i]], i, null);
        }
        checkAllFixedVolumeDevices();
        checkAllAliasStreamVolumes();
        checkMuteAffectedStreams();
    }

    private void dumpStreamStates(PrintWriter pw) {
        pw.println("\nStream volumes (device: index)");
        int numStreamTypes = AudioSystem.getNumStreamTypes();
        for (int i = SENDMSG_REPLACE; i < numStreamTypes; i += SENDMSG_NOOP) {
            pw.println("- " + AudioSystem.STREAM_NAMES[i] + ":");
            this.mStreamStates[i].dump(pw);
            pw.println("");
        }
        pw.print("\n- mute affected streams = 0x");
        pw.println(Integer.toHexString(this.mMuteAffectedStreams));
    }

    private void updateStreamVolumeAlias(boolean updateVolumes, String caller) {
        int dtmfStreamAlias;
        switch (this.mPlatformType) {
            case SENDMSG_NOOP /*1*/:
                this.mStreamVolumeAlias = this.STREAM_VOLUME_ALIAS_VOICE;
                dtmfStreamAlias = SENDMSG_QUEUE;
                break;
            case SENDMSG_QUEUE /*2*/:
                this.mStreamVolumeAlias = this.STREAM_VOLUME_ALIAS_TELEVISION;
                dtmfStreamAlias = SCO_STATE_ACTIVE_INTERNAL;
                break;
            default:
                this.mStreamVolumeAlias = this.STREAM_VOLUME_ALIAS_DEFAULT;
                dtmfStreamAlias = SCO_STATE_ACTIVE_INTERNAL;
                break;
        }
        if (isPlatformTelevision()) {
            this.mRingerModeAffectedStreams = SENDMSG_REPLACE;
        } else if (isInCommunication()) {
            dtmfStreamAlias = SENDMSG_REPLACE;
            this.mRingerModeAffectedStreams &= -257;
        } else {
            this.mRingerModeAffectedStreams |= DumpState.DUMP_SHARED_USERS;
        }
        this.mStreamVolumeAlias[MSG_SET_FORCE_USE] = dtmfStreamAlias;
        if (updateVolumes) {
            this.mStreamStates[MSG_SET_FORCE_USE].setAllIndexes(this.mStreamStates[dtmfStreamAlias], caller);
            setRingerModeInt(getRingerModeInternal(), DEBUG_VOL);
            sendMsg(this.mAudioHandler, MSG_SET_ALL_VOLUMES, SENDMSG_QUEUE, SENDMSG_REPLACE, SENDMSG_REPLACE, this.mStreamStates[MSG_SET_FORCE_USE], SENDMSG_REPLACE);
        }
    }

    private void readDockAudioSettings(ContentResolver cr) {
        int i;
        boolean z = true;
        if (Global.getInt(cr, "dock_audio_media_enabled", SENDMSG_REPLACE) != SENDMSG_NOOP) {
            z = DEBUG_VOL;
        }
        this.mDockAudioMediaEnabled = z;
        Handler handler = this.mAudioHandler;
        if (this.mDockAudioMediaEnabled) {
            i = MSG_SET_FORCE_USE;
        } else {
            i = SENDMSG_REPLACE;
        }
        sendMsg(handler, MSG_SET_FORCE_USE, SENDMSG_QUEUE, SCO_STATE_ACTIVE_INTERNAL, i, null, SENDMSG_REPLACE);
    }

    private void updateMasterMono(ContentResolver cr) {
        boolean masterMono = System.getIntForUser(cr, "master_mono", SENDMSG_REPLACE, -2) == SENDMSG_NOOP ? true : DEBUG_VOL;
        if (DEBUG_VOL) {
            String str = TAG;
            Object[] objArr = new Object[SENDMSG_NOOP];
            objArr[SENDMSG_REPLACE] = Boolean.valueOf(masterMono);
            Log.d(str, String.format("Master mono %b", objArr));
        }
        AudioSystem.setMasterMono(masterMono);
    }

    private void sendEncodedSurroundMode(ContentResolver cr) {
        sendEncodedSurroundMode(Global.getInt(cr, "encoded_surround_output", SENDMSG_REPLACE));
    }

    private void sendEncodedSurroundMode(int encodedSurroundMode) {
        int forceSetting = MSG_BROADCAST_AUDIO_BECOMING_NOISY;
        switch (encodedSurroundMode) {
            case SENDMSG_REPLACE /*0*/:
                forceSetting = SENDMSG_REPLACE;
                break;
            case SENDMSG_NOOP /*1*/:
                forceSetting = MSG_SET_FORCE_BT_A2DP_USE;
                break;
            case SENDMSG_QUEUE /*2*/:
                forceSetting = MSG_CHECK_MUSIC_ACTIVE;
                break;
            default:
                Log.e(TAG, "updateSurroundSoundSettings: illegal value " + encodedSurroundMode);
                break;
        }
        if (forceSetting != MSG_BROADCAST_AUDIO_BECOMING_NOISY) {
            sendMsg(this.mAudioHandler, MSG_SET_FORCE_USE, SENDMSG_QUEUE, MSG_BTA2DP_DOCK_TIMEOUT, forceSetting, null, SENDMSG_REPLACE);
        }
    }

    private void readPersistedSettings() {
        int i = SENDMSG_QUEUE;
        ContentResolver cr = this.mContentResolver;
        int ringerModeFromSettings = Global.getInt(cr, "mode_ringer", SENDMSG_QUEUE);
        int ringerMode = ringerModeFromSettings;
        if (!isValidRingerMode(ringerModeFromSettings)) {
            ringerMode = SENDMSG_QUEUE;
        }
        if (ringerMode == SENDMSG_NOOP && !this.mHasVibrator) {
            ringerMode = SENDMSG_REPLACE;
        }
        if (ringerMode != ringerModeFromSettings) {
            Global.putInt(cr, "mode_ringer", ringerMode);
        }
        if (this.mUseFixedVolume || isPlatformTelevision()) {
            ringerMode = SENDMSG_QUEUE;
        }
        synchronized (this.mSettingsLock) {
            int i2;
            this.mRingerMode = ringerMode;
            if (this.mRingerModeExternal == SCO_MODE_UNDEFINED) {
                this.mRingerModeExternal = this.mRingerMode;
            }
            if (this.mHasVibrator) {
                i2 = SENDMSG_QUEUE;
            } else {
                i2 = SENDMSG_REPLACE;
            }
            this.mVibrateSetting = AudioSystem.getValueForVibrateSetting(SENDMSG_REPLACE, SENDMSG_NOOP, i2);
            i2 = this.mVibrateSetting;
            if (!this.mHasVibrator) {
                i = SENDMSG_REPLACE;
            }
            this.mVibrateSetting = AudioSystem.getValueForVibrateSetting(i2, SENDMSG_REPLACE, i);
            updateRingerModeAffectedStreams();
            readDockAudioSettings(cr);
            sendEncodedSurroundMode(cr);
        }
        this.mMuteAffectedStreams = System.getIntForUser(cr, "mute_streams_affected", 46, -2);
        updateMasterMono(cr);
        readPersistedSettingsEx(cr);
        broadcastRingerMode("android.media.RINGER_MODE_CHANGED", this.mRingerModeExternal);
        broadcastRingerMode("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION", this.mRingerMode);
        broadcastVibrateSetting(SENDMSG_REPLACE);
        broadcastVibrateSetting(SENDMSG_NOOP);
        this.mVolumeController.loadSettings(cr);
    }

    private void readUserRestrictions() {
        int currentUser = getCurrentUserId();
        boolean userRestriction = this.mUserManagerInternal.getUserRestriction(currentUser, "no_adjust_volume");
        if (this.mUseFixedVolume) {
            userRestriction = DEBUG_VOL;
            AudioSystem.setMasterVolume(1.0f);
        }
        if (DEBUG_VOL) {
            String str = TAG;
            Object[] objArr = new Object[SENDMSG_QUEUE];
            objArr[SENDMSG_REPLACE] = Boolean.valueOf(userRestriction);
            objArr[SENDMSG_NOOP] = Integer.valueOf(currentUser);
            Log.d(str, String.format("Master mute %s, user=%d", objArr));
        }
        setSystemAudioMute(userRestriction);
        AudioSystem.setMasterMute(userRestriction);
        broadcastMasterMuteStatus(userRestriction);
        boolean microphoneMute = this.mUserManagerInternal.getUserRestriction(currentUser, "no_unmute_microphone");
        if (DEBUG_VOL) {
            str = TAG;
            objArr = new Object[SENDMSG_QUEUE];
            objArr[SENDMSG_REPLACE] = Boolean.valueOf(microphoneMute);
            objArr[SENDMSG_NOOP] = Integer.valueOf(currentUser);
            Log.d(str, String.format("Mic mute %s, user=%d", objArr));
        }
        AudioSystem.muteMicrophone(microphoneMute);
    }

    private int rescaleIndex(int index, int srcStream, int dstStream) {
        return ((this.mStreamStates[dstStream].getMaxIndex() * index) + (this.mStreamStates[srcStream].getMaxIndex() / SENDMSG_QUEUE)) / this.mStreamStates[srcStream].getMaxIndex();
    }

    public void adjustSuggestedStreamVolume(int direction, int suggestedStreamType, int flags, String callingPackage, String caller) {
        adjustSuggestedStreamVolume(direction, suggestedStreamType, flags, callingPackage, caller, Binder.getCallingUid());
    }

    private void adjustSuggestedStreamVolume(int direction, int suggestedStreamType, int flags, String callingPackage, String caller, int uid) {
        int streamType;
        if (DEBUG_VOL) {
            Log.d(TAG, "adjustSuggestedStreamVolume() stream=" + suggestedStreamType + ", flags=" + flags + ", caller=" + caller);
        }
        boolean isMute = isMuteAdjust(direction);
        if (this.mVolumeControlStream != SCO_MODE_UNDEFINED) {
            streamType = this.mVolumeControlStream;
        } else {
            streamType = getActiveStreamType(suggestedStreamType);
        }
        ensureValidStreamType(streamType);
        int resolvedStream = this.mStreamVolumeAlias[streamType];
        if (!((flags & SCO_STATE_DEACTIVATE_EXT_REQ) == 0 || resolvedStream == SENDMSG_QUEUE)) {
            flags &= -5;
        }
        if (this.mVolumeController.suppressAdjustment(resolvedStream, flags, isMute)) {
            direction = SENDMSG_REPLACE;
            flags = (flags & -5) & -17;
            if (DEBUG_VOL) {
                Log.d(TAG, "Volume controller suppressed adjustment");
            }
        }
        adjustStreamVolume(streamType, direction, flags, callingPackage, caller, uid);
    }

    public void adjustStreamVolume(int streamType, int direction, int flags, String callingPackage) {
        adjustStreamVolume(streamType, direction, flags, callingPackage, callingPackage, Binder.getCallingUid());
    }

    private void adjustStreamVolume(int streamType, int direction, int flags, String callingPackage, String caller, int uid) {
        if (checkEnbaleVolumeAdjust() && !this.mUseFixedVolume) {
            Log.d(TAG, "adjustStreamVolume() stream=" + streamType + ", dir=" + direction + ", flags=" + flags + ", caller=" + Binder.getCallingPid());
            ensureValidDirection(direction);
            ensureValidStreamType(streamType);
            boolean isMuteAdjust = isMuteAdjust(direction);
            if (!isMuteAdjust || isStreamAffectedByMute(streamType)) {
                int streamTypeAlias = this.mStreamVolumeAlias[streamType];
                VolumeStreamState streamState = this.mStreamStates[streamTypeAlias];
                int device = getDeviceForStream(streamTypeAlias);
                int aliasIndex = streamState.getIndex(device);
                boolean z = true;
                boolean persistVolume = DEBUG_VOL;
                if ((device & 896) != 0 || (flags & 64) == 0) {
                    if (uid == INDICATE_SYSTEM_READY_RETRY_DELAY_MS) {
                        uid = UserHandle.getUid(getCurrentUserId(), UserHandle.getAppId(uid));
                    }
                    if (this.mAppOps.noteOp(STREAM_VOLUME_OPS[streamTypeAlias], uid, callingPackage) == 0) {
                        int step;
                        synchronized (this.mSafeMediaVolumeState) {
                            this.mPendingVolumeCommand = null;
                        }
                        flags &= -33;
                        if (streamTypeAlias != SCO_STATE_ACTIVE_INTERNAL || (this.mFixedVolumeDevices & device) == 0) {
                            step = rescaleIndex(MSG_SET_ALL_VOLUMES, streamType, streamTypeAlias);
                        } else {
                            flags |= 32;
                            if (this.mSafeMediaVolumeState.intValue() != SCO_STATE_ACTIVE_INTERNAL || (device & MSG_REPORT_NEW_ROUTES) == 0) {
                                step = streamState.getMaxIndex();
                            } else {
                                step = this.mSafeMediaVolumeIndex;
                            }
                            if (aliasIndex != 0) {
                                aliasIndex = step;
                            }
                        }
                        if (LOUD_VOICE_MODE_SUPPORT && streamType == 0 && "true".equals(AudioSystem.getParameters("VOICE_LVM_Enable"))) {
                            step = SENDMSG_REPLACE;
                        }
                        if ((flags & SENDMSG_QUEUE) != 0 || streamTypeAlias == getUiSoundsStreamType()) {
                            if (getRingerModeInternal() == SENDMSG_NOOP) {
                                flags &= -17;
                            }
                            int result = checkForRingerModeChange(aliasIndex, direction, step, streamState.mIsMuted, callingPackage, flags);
                            z = (result & SENDMSG_NOOP) != 0 ? true : DEBUG_VOL;
                            persistVolume = (result & SENDMSG_QUEUE) != 0 ? true : DEBUG_VOL;
                            if ((result & DumpState.DUMP_PACKAGES) != 0) {
                                flags |= DumpState.DUMP_PACKAGES;
                            }
                            if ((result & DumpState.DUMP_VERIFIERS) != 0) {
                                flags |= DumpState.DUMP_VERIFIERS;
                            }
                        }
                        if (!volumeAdjustmentAllowedByDnd(streamTypeAlias, flags)) {
                            z = DEBUG_VOL;
                        }
                        int oldIndex = this.mStreamStates[streamType].getIndex(device);
                        if (z && direction != 0) {
                            this.mAudioHandler.removeMessages(MSG_UNMUTE_STREAM);
                            if (streamTypeAlias == SCO_STATE_ACTIVE_INTERNAL && (device & 896) != 0 && (flags & 64) == 0) {
                                synchronized (this.mA2dpAvrcpLock) {
                                    if (this.mA2dp != null && this.mAvrcpAbsVolSupported) {
                                        this.mA2dp.adjustAvrcpAbsoluteVolume(direction);
                                    }
                                }
                            }
                            if (isMuteAdjust) {
                                boolean state = direction == MSG_SET_A2DP_SRC_CONNECTION_STATE ? streamState.mIsMuted ? DEBUG_VOL : true : direction == -100 ? true : DEBUG_VOL;
                                if (streamTypeAlias == SCO_STATE_ACTIVE_INTERNAL) {
                                    setSystemAudioMute(state);
                                }
                                for (int stream = SENDMSG_REPLACE; stream < this.mStreamStates.length; stream += SENDMSG_NOOP) {
                                    if (streamTypeAlias == this.mStreamVolumeAlias[stream]) {
                                        Object obj = readCameraSoundForced() ? this.mStreamStates[stream].getStreamType() == MSG_LOAD_SOUND_EFFECTS ? SENDMSG_NOOP : null : null;
                                        if (obj == null) {
                                            this.mStreamStates[stream].mute(state);
                                        }
                                    }
                                }
                            } else if (direction != SENDMSG_NOOP || checkSafeMediaVolume(streamTypeAlias, aliasIndex + step, device)) {
                                if (streamState.adjustIndex(direction * step, device, caller) || streamState.mIsMuted) {
                                    if (streamState.mIsMuted) {
                                        if (direction == SENDMSG_NOOP) {
                                            streamState.mute(DEBUG_VOL);
                                        } else if (direction == SCO_MODE_UNDEFINED && this.mPlatformType == SENDMSG_QUEUE) {
                                            sendMsg(this.mAudioHandler, MSG_UNMUTE_STREAM, SENDMSG_QUEUE, streamTypeAlias, flags, null, UNMUTE_STREAM_DELAY);
                                        }
                                    }
                                    sendMsg(this.mAudioHandler, SENDMSG_REPLACE, SENDMSG_QUEUE, device, SENDMSG_REPLACE, streamState, SENDMSG_REPLACE);
                                }
                            } else {
                                Log.e(TAG, "adjustStreamVolume() safe volume index = " + oldIndex);
                                this.mVolumeController.postDisplaySafeVolumeWarning(flags);
                            }
                            int newIndex = this.mStreamStates[streamType].getIndex(device);
                            if (streamTypeAlias == SCO_STATE_ACTIVE_INTERNAL) {
                                setSystemAudioVolume(oldIndex, newIndex, getStreamMaxVolume(streamType), flags);
                            }
                            if (this.mHdmiManager != null) {
                                synchronized (this.mHdmiManager) {
                                    if (this.mHdmiCecSink && streamTypeAlias == SCO_STATE_ACTIVE_INTERNAL && oldIndex != newIndex) {
                                        synchronized (this.mHdmiPlaybackClient) {
                                            int keyCode;
                                            if (direction == SCO_MODE_UNDEFINED) {
                                                keyCode = MSG_DYN_POLICY_MIX_STATE_UPDATE;
                                            } else {
                                                keyCode = MSG_UNMUTE_STREAM;
                                            }
                                            long ident = Binder.clearCallingIdentity();
                                            try {
                                                this.mHdmiPlaybackClient.sendKeyEvent(keyCode, true);
                                                this.mHdmiPlaybackClient.sendKeyEvent(keyCode, DEBUG_VOL);
                                                Binder.restoreCallingIdentity(ident);
                                            } catch (Throwable th) {
                                                Binder.restoreCallingIdentity(ident);
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (persistVolume) {
                            sendMsg(this.mAudioHandler, SENDMSG_REPLACE, SENDMSG_QUEUE, device, SENDMSG_REPLACE, streamState, SENDMSG_REPLACE);
                        }
                        int index = this.mStreamStates[streamType].getIndex(device);
                        if (!((flags & SCO_STATE_DEACTIVATE_EXT_REQ) == 0 || this.mKeyguardManager == null || !this.mKeyguardManager.isKeyguardLocked())) {
                            flags &= -5;
                        }
                        if (!((flags & SENDMSG_NOOP) == 0 || this.mScreenOn)) {
                            flags &= -2;
                        }
                        if (DEBUG_VOL) {
                            Log.d(TAG, "adjustStreamVolume() stream=" + streamType + ", flags=" + flags + ",mScreenOn= " + this.mScreenOn);
                        }
                        sendVolumeUpdate(streamType, oldIndex, index, flags);
                        if (LOUD_VOICE_MODE_SUPPORT) {
                            sendMsg(this.mAudioHandler, 10001, SENDMSG_REPLACE, SENDMSG_NOOP, SENDMSG_REPLACE, new DeviceVolumeState(direction, device, oldIndex, streamType), SENDMSG_REPLACE);
                        }
                    }
                }
            }
        }
    }

    private void onUnmuteStream(int stream, int flags) {
        this.mStreamStates[stream].mute(DEBUG_VOL);
        int index = this.mStreamStates[stream].getIndex(getDeviceForStream(stream));
        sendVolumeUpdate(stream, index, index, flags);
    }

    private void setSystemAudioVolume(int oldVolume, int newVolume, int maxVolume, int flags) {
        if (this.mHdmiManager != null && this.mHdmiTvClient != null && oldVolume != newVolume && (flags & DumpState.DUMP_SHARED_USERS) == 0) {
            synchronized (this.mHdmiManager) {
                if (this.mHdmiSystemAudioSupported) {
                    synchronized (this.mHdmiTvClient) {
                        long token = Binder.clearCallingIdentity();
                        try {
                            this.mHdmiTvClient.setSystemAudioVolume(oldVolume, newVolume, maxVolume);
                            Binder.restoreCallingIdentity(token);
                        } catch (Throwable th) {
                            Binder.restoreCallingIdentity(token);
                        }
                    }
                    return;
                }
            }
        }
    }

    private int getNewRingerMode(int stream, int index, int flags) {
        if ((flags & SENDMSG_QUEUE) == 0 && stream != getUiSoundsStreamType()) {
            return getRingerModeExternal();
        }
        int newRingerMode;
        if (index != 0) {
            newRingerMode = SENDMSG_QUEUE;
        } else if (this.mHasVibrator) {
            newRingerMode = SENDMSG_NOOP;
        } else if (this.mVolumePolicy.volumeDownToEnterSilent) {
            newRingerMode = SENDMSG_REPLACE;
        } else {
            newRingerMode = SENDMSG_QUEUE;
        }
        return newRingerMode;
    }

    private boolean isAndroidNPlus(String caller) {
        try {
            return this.mContext.getPackageManager().getApplicationInfoAsUser(caller, SENDMSG_REPLACE, UserHandle.getUserId(Binder.getCallingUid())).targetSdkVersion >= MSG_UNMUTE_STREAM ? true : DEBUG_VOL;
        } catch (NameNotFoundException e) {
            return true;
        }
    }

    private boolean wouldToggleZenMode(int newMode) {
        if (getRingerModeExternal() != 0 || newMode == 0) {
            return (getRingerModeExternal() == 0 || newMode != 0) ? DEBUG_VOL : true;
        } else {
            return true;
        }
    }

    private void onSetStreamVolume(int streamType, int index, int flags, int device, String caller) {
        boolean z = DEBUG_VOL;
        int stream = this.mStreamVolumeAlias[streamType];
        setStreamVolumeInt(stream, index, device, DEBUG_VOL, caller);
        if ((flags & SENDMSG_QUEUE) != 0 || stream == getUiSoundsStreamType()) {
            setRingerMode(getNewRingerMode(stream, index, flags), "AudioService.onSetStreamVolume", DEBUG_VOL);
        }
        VolumeStreamState volumeStreamState = this.mStreamStates[stream];
        if (index == 0) {
            z = true;
        }
        volumeStreamState.mute(z);
    }

    public void setStreamVolume(int streamType, int index, int flags, String callingPackage) {
        setStreamVolume(streamType, index, flags, callingPackage, callingPackage, Binder.getCallingUid());
    }

    private void setStreamVolume(int streamType, int index, int flags, String callingPackage, String caller, int uid) {
        if (checkEnbaleVolumeAdjust() && !this.mUseFixedVolume) {
            ensureValidStreamType(streamType);
            int streamTypeAlias = this.mStreamVolumeAlias[streamType];
            VolumeStreamState streamState = this.mStreamStates[streamTypeAlias];
            if (this.mCust == null || !this.mCust.isTurningAllSound()) {
                int device = getDeviceForStream(streamTypeAlias);
                if ((device & 896) != 0 || (flags & 64) == 0) {
                    if (uid == INDICATE_SYSTEM_READY_RETRY_DELAY_MS) {
                        uid = UserHandle.getUid(getCurrentUserId(), UserHandle.getAppId(uid));
                    }
                    if (this.mAppOps.noteOp(STREAM_VOLUME_OPS[streamTypeAlias], uid, callingPackage) == 0) {
                        if (isAndroidNPlus(callingPackage)) {
                            if (wouldToggleZenMode(getNewRingerMode(streamTypeAlias, index, flags)) && !this.mNm.isNotificationPolicyAccessGrantedForPackage(callingPackage)) {
                                throw new SecurityException("Not allowed to change Do Not Disturb state");
                            }
                        }
                        if (volumeAdjustmentAllowedByDnd(streamTypeAlias, flags)) {
                            int oldIndex;
                            synchronized (this.mSafeMediaVolumeState) {
                                this.mPendingVolumeCommand = null;
                                oldIndex = streamState.getIndex(device);
                                if (index < (streamState.getMinIndex() + SCO_STATE_DEACTIVATE_REQ) / MSG_SET_ALL_VOLUMES) {
                                    index = (streamState.getMinIndex() + SCO_STATE_DEACTIVATE_REQ) / MSG_SET_ALL_VOLUMES;
                                } else if (index > (streamState.getMaxIndex() + SCO_STATE_DEACTIVATE_REQ) / MSG_SET_ALL_VOLUMES) {
                                    index = (streamState.getMaxIndex() + SCO_STATE_DEACTIVATE_REQ) / MSG_SET_ALL_VOLUMES;
                                }
                                index = rescaleIndex(index * MSG_SET_ALL_VOLUMES, streamType, streamTypeAlias);
                                if (streamTypeAlias == SCO_STATE_ACTIVE_INTERNAL && (device & 896) != 0 && (flags & 64) == 0) {
                                    synchronized (this.mA2dpAvrcpLock) {
                                        if (this.mA2dp != null && this.mAvrcpAbsVolSupported) {
                                            this.mA2dp.setAvrcpAbsoluteVolume(index / MSG_SET_ALL_VOLUMES);
                                        }
                                    }
                                }
                                if (streamTypeAlias == SCO_STATE_ACTIVE_INTERNAL) {
                                    setSystemAudioVolume(oldIndex, index, getStreamMaxVolume(streamType), flags);
                                }
                                flags &= -33;
                                if (streamTypeAlias == SCO_STATE_ACTIVE_INTERNAL && (this.mFixedVolumeDevices & device) != 0) {
                                    flags |= 32;
                                    if (index != 0) {
                                        index = (this.mSafeMediaVolumeState.intValue() != SCO_STATE_ACTIVE_INTERNAL || (device & MSG_REPORT_NEW_ROUTES) == 0) ? streamState.getMaxIndex() : this.mSafeMediaVolumeIndex;
                                    }
                                }
                                if (checkSafeMediaVolume(streamTypeAlias, index, device)) {
                                    onSetStreamVolume(streamType, index, flags, device, caller);
                                    index = this.mStreamStates[streamType].getIndex(device);
                                } else {
                                    this.mVolumeController.postDisplaySafeVolumeWarning(flags);
                                    this.mPendingVolumeCommand = new StreamVolumeCommand(streamType, index, flags, device);
                                }
                            }
                            if (!((flags & SENDMSG_NOOP) == 0 || this.mScreenOn)) {
                                flags &= -2;
                            }
                            sendVolumeUpdate(streamType, oldIndex, index, flags);
                            if (LOUD_VOICE_MODE_SUPPORT) {
                                sendMsg(this.mAudioHandler, 10001, SENDMSG_REPLACE, SENDMSG_NOOP, SENDMSG_REPLACE, new DeviceVolumeState(SENDMSG_REPLACE, device, oldIndex, streamType), SENDMSG_REPLACE);
                            }
                            return;
                        }
                        return;
                    }
                    return;
                }
                return;
            }
            sendMsg(this.mAudioHandler, MSG_SET_ALL_VOLUMES, SENDMSG_QUEUE, SENDMSG_NOOP, SENDMSG_REPLACE, streamState, SENDMSG_REPLACE);
        }
    }

    private boolean volumeAdjustmentAllowedByDnd(int streamTypeAlias, int flags) {
        if (this.mNm.getZenMode() == SENDMSG_QUEUE && (isStreamMutedByRingerMode(streamTypeAlias) || streamTypeAlias == SCO_STATE_ACTIVE_INTERNAL)) {
            boolean isTotalSilence = Global.getInt(this.mContext.getContentResolver(), "total_silence_mode", SENDMSG_REPLACE) == SENDMSG_NOOP ? true : DEBUG_VOL;
            if ((flags & SENDMSG_QUEUE) == 0 && streamTypeAlias != getUiSoundsStreamType() && isTotalSilence) {
                Log.d(TAG, "volumeAdjustmentAllowedByDnd false");
                return DEBUG_VOL;
            }
        }
        return true;
    }

    public void forceVolumeControlStream(int streamType, IBinder cb) {
        synchronized (this.mForceControlStreamLock) {
            this.mVolumeControlStream = streamType;
            if (this.mVolumeControlStream != SCO_MODE_UNDEFINED) {
                this.mForceControlStreamClient = new ForceControlStreamClient(cb);
            } else if (this.mForceControlStreamClient != null) {
                this.mForceControlStreamClient.release();
                this.mForceControlStreamClient = null;
            }
        }
    }

    private void sendBroadcastToAll(Intent intent) {
        intent.addFlags(67108864);
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

    private int getCurrentUserId() {
        long ident = Binder.clearCallingIdentity();
        try {
            int i = ActivityManagerNative.getDefault().getCurrentUser().id;
            return i;
        } catch (RemoteException e) {
            return SENDMSG_REPLACE;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private void sendVolumeUpdate(int streamType, int oldIndex, int index, int flags) {
        streamType = this.mStreamVolumeAlias[streamType];
        if (streamType == SCO_STATE_ACTIVE_INTERNAL) {
            flags = updateFlagsForSystemAudio(flags);
        }
        Log.d(TAG, "sendVolumeUpdate() stream=" + streamType + " flags=" + flags + " index=" + index + " oldIndex=" + oldIndex);
        this.mVolumeController.postVolumeChanged(streamType, flags);
    }

    private int updateFlagsForSystemAudio(int flags) {
        if (this.mHdmiTvClient != null) {
            synchronized (this.mHdmiTvClient) {
                if (this.mHdmiSystemAudioSupported && (flags & DumpState.DUMP_SHARED_USERS) == 0) {
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
            sendMsg(this.mAudioHandler, SENDMSG_REPLACE, SENDMSG_QUEUE, device, SENDMSG_REPLACE, streamState, SENDMSG_REPLACE);
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
                            Binder.restoreCallingIdentity(token);
                        } catch (Throwable th) {
                            Binder.restoreCallingIdentity(token);
                        }
                    }
                    return;
                }
            }
        }
    }

    public boolean isStreamMute(int streamType) {
        boolean -get3;
        if (streamType == UsbAudioDevice.kAudioDeviceMeta_Alsa) {
            streamType = getActiveStreamType(streamType);
        }
        synchronized (VolumeStreamState.class) {
            -get3 = this.mStreamStates[streamType].mIsMuted;
        }
        return -get3;
    }

    private boolean discardRmtSbmxFullVolDeathHandlerFor(IBinder cb) {
        Iterator<RmtSbmxFullVolDeathHandler> it = this.mRmtSbmxFullVolDeathHandlers.iterator();
        while (it.hasNext()) {
            RmtSbmxFullVolDeathHandler handler = (RmtSbmxFullVolDeathHandler) it.next();
            if (handler.isHandlerFor(cb)) {
                handler.forget();
                this.mRmtSbmxFullVolDeathHandlers.remove(handler);
                return true;
            }
        }
        return DEBUG_VOL;
    }

    private boolean hasRmtSbmxFullVolDeathHandlerFor(IBinder cb) {
        Iterator<RmtSbmxFullVolDeathHandler> it = this.mRmtSbmxFullVolDeathHandlers.iterator();
        while (it.hasNext()) {
            if (((RmtSbmxFullVolDeathHandler) it.next()).isHandlerFor(cb)) {
                return true;
            }
        }
        return DEBUG_VOL;
    }

    public void forceRemoteSubmixFullVolume(boolean startForcing, IBinder cb) {
        if (cb != null) {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.CAPTURE_AUDIO_OUTPUT") != 0) {
                Log.w(TAG, "Trying to call forceRemoteSubmixFullVolume() without CAPTURE_AUDIO_OUTPUT");
                return;
            }
            synchronized (this.mRmtSbmxFullVolDeathHandlers) {
                boolean applyRequired = DEBUG_VOL;
                if (startForcing) {
                    if (!hasRmtSbmxFullVolDeathHandlerFor(cb)) {
                        this.mRmtSbmxFullVolDeathHandlers.add(new RmtSbmxFullVolDeathHandler(cb));
                        if (this.mRmtSbmxFullVolRefCount == 0) {
                            this.mFullVolumeDevices |= DumpState.DUMP_VERSION;
                            this.mFixedVolumeDevices |= DumpState.DUMP_VERSION;
                            applyRequired = true;
                        }
                        this.mRmtSbmxFullVolRefCount += SENDMSG_NOOP;
                    }
                } else if (discardRmtSbmxFullVolDeathHandlerFor(cb) && this.mRmtSbmxFullVolRefCount > 0) {
                    this.mRmtSbmxFullVolRefCount += SCO_MODE_UNDEFINED;
                    if (this.mRmtSbmxFullVolRefCount == 0) {
                        this.mFullVolumeDevices &= -32769;
                        this.mFixedVolumeDevices &= -32769;
                        applyRequired = true;
                    }
                }
                if (applyRequired) {
                    checkAllFixedVolumeDevices(SCO_STATE_ACTIVE_INTERNAL);
                    this.mStreamStates[SCO_STATE_ACTIVE_INTERNAL].applyAllVolumes();
                }
            }
        }
    }

    private void setMasterMuteInternal(boolean mute, int flags, String callingPackage, int uid, int userId) {
        if (uid == INDICATE_SYSTEM_READY_RETRY_DELAY_MS) {
            uid = UserHandle.getUid(userId, UserHandle.getAppId(uid));
        }
        if (!mute && this.mAppOps.noteOp(33, uid, callingPackage) != 0) {
            return;
        }
        if (userId == UserHandle.getCallingUserId() || this.mContext.checkCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL") == 0) {
            setMasterMuteInternalNoCallerCheck(mute, flags, userId);
        }
    }

    private void setMasterMuteInternalNoCallerCheck(boolean mute, int flags, int userId) {
        if (DEBUG_VOL) {
            String str = TAG;
            Object[] objArr = new Object[SCO_STATE_ACTIVE_INTERNAL];
            objArr[SENDMSG_REPLACE] = Boolean.valueOf(mute);
            objArr[SENDMSG_NOOP] = Integer.valueOf(flags);
            objArr[SENDMSG_QUEUE] = Integer.valueOf(userId);
            Log.d(str, String.format("Master mute %s, %d, user=%d", objArr));
        }
        if (!(this.mUseFixedVolume || getCurrentUserId() != userId || mute == AudioSystem.getMasterMute())) {
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
        setMasterMuteInternal(mute, flags, callingPackage, Binder.getCallingUid(), userId);
    }

    public int getStreamVolume(int streamType) {
        int i;
        ensureValidStreamType(streamType);
        int device = getDeviceForStream(streamType);
        synchronized (VolumeStreamState.class) {
            int index = this.mStreamStates[streamType].getIndex(device);
            if (this.mStreamStates[streamType].mIsMuted) {
                index = SENDMSG_REPLACE;
            }
            if (!(index == 0 || this.mStreamVolumeAlias[streamType] != SCO_STATE_ACTIVE_INTERNAL || (this.mFixedVolumeDevices & device) == 0)) {
                index = this.mStreamStates[streamType].getMaxIndex();
            }
            i = (index + SCO_STATE_DEACTIVATE_REQ) / MSG_SET_ALL_VOLUMES;
        }
        return i;
    }

    public int getStreamMaxVolume(int streamType) {
        ensureValidStreamType(streamType);
        return (this.mStreamStates[streamType].getMaxIndex() + SCO_STATE_DEACTIVATE_REQ) / MSG_SET_ALL_VOLUMES;
    }

    public int getStreamMinVolume(int streamType) {
        ensureValidStreamType(streamType);
        return (this.mStreamStates[streamType].getMinIndex() + SCO_STATE_DEACTIVATE_REQ) / MSG_SET_ALL_VOLUMES;
    }

    public int getLastAudibleStreamVolume(int streamType) {
        ensureValidStreamType(streamType);
        return (this.mStreamStates[streamType].getIndex(getDeviceForStream(streamType)) + SCO_STATE_DEACTIVATE_REQ) / MSG_SET_ALL_VOLUMES;
    }

    public int getUiSoundsStreamType() {
        return this.mStreamVolumeAlias[SENDMSG_NOOP];
    }

    public void setMicrophoneMute(boolean on, String callingPackage, int userId) {
        int uid = Binder.getCallingUid();
        if (uid == INDICATE_SYSTEM_READY_RETRY_DELAY_MS) {
            uid = UserHandle.getUid(userId, UserHandle.getAppId(uid));
        }
        if ((!on && this.mAppOps.noteOp(44, uid, callingPackage) != 0) || !checkAudioSettingsPermission("setMicrophoneMute()")) {
            return;
        }
        if (userId == UserHandle.getCallingUserId() || this.mContext.checkCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL") == 0) {
            setMicrophoneMuteNoCallerCheck(on, userId);
        }
    }

    private void setMicrophoneMuteNoCallerCheck(boolean on, int userId) {
        if (DEBUG_VOL) {
            String str = TAG;
            Object[] objArr = new Object[SENDMSG_QUEUE];
            objArr[SENDMSG_REPLACE] = Boolean.valueOf(on);
            objArr[SENDMSG_NOOP] = Integer.valueOf(userId);
            Log.d(str, String.format("Mic mute %s, user=%d", objArr));
        }
        if (checkAudioSettingAllowed("ASsmm" + on) && getCurrentUserId() == userId) {
            AudioSystem.muteMicrophone(on);
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
        return (ringerMode < 0 || ringerMode > SENDMSG_QUEUE) ? DEBUG_VOL : true;
    }

    public void setRingerModeExternal(int ringerMode, String caller) {
        if (isAndroidNPlus(caller) && wouldToggleZenMode(ringerMode) && !this.mNm.isNotificationPolicyAccessGrantedForPackage(caller)) {
            throw new SecurityException("Not allowed to change Do Not Disturb state");
        }
        setRingerMode(ringerMode, caller, true);
    }

    public void setRingerModeInternal(int ringerMode, String caller) {
        enforceVolumeController("setRingerModeInternal");
        setRingerMode(ringerMode, caller, DEBUG_VOL);
    }

    private void setRingerMode(int ringerMode, String caller, boolean external) {
        if (checkEnbaleVolumeAdjust() && !this.mUseFixedVolume && !isPlatformTelevision()) {
            if (caller == null || caller.length() == 0) {
                throw new IllegalArgumentException("Bad caller: " + caller);
            }
            ensureValidRingerMode(ringerMode);
            if (ringerMode == SENDMSG_NOOP && !this.mHasVibrator) {
                ringerMode = SENDMSG_REPLACE;
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
            }
        }
    }

    private void setRingerModeExt(int ringerMode) {
        synchronized (this.mSettingsLock) {
            if (ringerMode == this.mRingerModeExternal) {
                return;
            }
            this.mRingerModeExternal = ringerMode;
            broadcastRingerMode("android.media.RINGER_MODE_CHANGED", ringerMode);
        }
    }

    private void setRingerModeInt(int ringerMode, boolean persist) {
        synchronized (this.mSettingsLock) {
            boolean change = this.mRingerMode != ringerMode ? true : DEBUG_VOL;
            this.mRingerMode = ringerMode;
        }
        muteRingerModeStreams();
        if (persist) {
            sendMsg(this.mAudioHandler, SCO_STATE_ACTIVE_INTERNAL, SENDMSG_REPLACE, SENDMSG_REPLACE, SENDMSG_REPLACE, null, PERSIST_DELAY);
        }
        if (change) {
            broadcastRingerMode("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION", ringerMode);
        }
    }

    public boolean shouldVibrate(int vibrateType) {
        boolean z = true;
        if (!this.mHasVibrator) {
            return DEBUG_VOL;
        }
        switch (getVibrateSetting(vibrateType)) {
            case SENDMSG_REPLACE /*0*/:
                return DEBUG_VOL;
            case SENDMSG_NOOP /*1*/:
                if (getRingerModeExternal() == 0) {
                    z = DEBUG_VOL;
                }
                return z;
            case SENDMSG_QUEUE /*2*/:
                if (getRingerModeExternal() != SENDMSG_NOOP) {
                    z = DEBUG_VOL;
                }
                return z;
            default:
                return DEBUG_VOL;
        }
    }

    public int getVibrateSetting(int vibrateType) {
        if (this.mHasVibrator) {
            return (this.mVibrateSetting >> (vibrateType * SENDMSG_QUEUE)) & SCO_STATE_ACTIVE_INTERNAL;
        }
        return SENDMSG_REPLACE;
    }

    public void setVibrateSetting(int vibrateType, int vibrateSetting) {
        if (this.mHasVibrator) {
            this.mVibrateSetting = AudioSystem.getValueForVibrateSetting(this.mVibrateSetting, vibrateType, vibrateSetting);
            broadcastVibrateSetting(vibrateType);
        }
    }

    public void setMode(int mode, IBinder cb, String callingPackage) {
        if (DEBUG_MODE) {
            Log.v(TAG, "setMode(mode=" + mode + ", callingPackage=" + callingPackage + ")");
        }
        if (checkAudioSettingsPermission("setMode()")) {
            if (DUAL_SMARTPA_SUPPORT) {
                checkMuteRcvDelay(this.mMode, mode);
            }
            if (mode == SENDMSG_QUEUE && this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE") != 0) {
                Log.w(TAG, "MODIFY_PHONE_STATE Permission Denial: setMode(MODE_IN_CALL) from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            } else if (mode >= SCO_MODE_UNDEFINED && mode < SCO_STATE_DEACTIVATE_EXT_REQ) {
                if (this.mMode == SENDMSG_QUEUE && mode == SCO_STATE_ACTIVE_INTERNAL && this.mIsHisiPlatform) {
                    Log.w(TAG, "Forbid set MODE_IN_COMMUNICATION when current mode is MODE_IN_CALL from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                    return;
                }
                int newModeOwnerPid;
                synchronized (this.mSetModeDeathHandlers) {
                    if (mode == SCO_MODE_UNDEFINED) {
                        mode = this.mMode;
                    }
                    if (mode == 0 && !this.mSetModeDeathHandlers.isEmpty()) {
                        if (((SetModeDeathHandler) this.mSetModeDeathHandlers.get(SENDMSG_REPLACE)).getMode() == SCO_STATE_ACTIVE_INTERNAL && Integer.valueOf(AudioSystem.getParameters("active_record_pid")).intValue() != SCO_MODE_UNDEFINED) {
                            this.mSetModeDeathHandlers.clear();
                            Log.i(TAG, "SetMode clear mSetModeDeathHandlers");
                        }
                    }
                    newModeOwnerPid = setModeInt(mode, cb, Binder.getCallingPid(), callingPackage);
                }
                if (newModeOwnerPid != 0) {
                    disconnectBluetoothSco(newModeOwnerPid);
                }
                if (LOUD_VOICE_MODE_SUPPORT) {
                    getOldInCallDevice(mode);
                }
            }
        }
    }

    private int setModeInt(int mode, IBinder cb, int pid, String caller) {
        if (DEBUG_MODE) {
            Log.v(TAG, "setModeInt(mode=" + mode + ", pid=" + pid + ", caller=" + caller + ")");
        }
        int newModeOwnerPid = SENDMSG_REPLACE;
        if (cb == null) {
            Log.e(TAG, "setModeInt() called with null binder");
            return SENDMSG_REPLACE;
        }
        SetModeDeathHandler setModeDeathHandler = null;
        Iterator iter = this.mSetModeDeathHandlers.iterator();
        while (iter.hasNext()) {
            SetModeDeathHandler h = (SetModeDeathHandler) iter.next();
            if (h.getPid() == pid) {
                setModeDeathHandler = h;
                iter.remove();
                h.getBinder().unlinkToDeath(h, SENDMSG_REPLACE);
                break;
            }
        }
        do {
            int status;
            if (mode != 0) {
                if (setModeDeathHandler == null) {
                    setModeDeathHandler = new SetModeDeathHandler(cb, pid);
                }
                try {
                    cb.linkToDeath(setModeDeathHandler, SENDMSG_REPLACE);
                } catch (RemoteException e) {
                    Log.w(TAG, "setMode() could not link to " + cb + " binder death");
                }
                this.mSetModeDeathHandlers.add(SENDMSG_REPLACE, setModeDeathHandler);
                setModeDeathHandler.setMode(mode);
            } else if (!this.mSetModeDeathHandlers.isEmpty()) {
                setModeDeathHandler = (SetModeDeathHandler) this.mSetModeDeathHandlers.get(SENDMSG_REPLACE);
                cb = setModeDeathHandler.getBinder();
                mode = setModeDeathHandler.getMode();
                if (DEBUG_MODE) {
                    Log.w(TAG, " using mode=" + mode + " instead due to death hdlr at pid=" + setModeDeathHandler.mPid);
                }
                if (mode == SCO_STATE_ACTIVE_INTERNAL) {
                    HwMediaMonitorManager.writeLogMsg(SCO_STATE_DEACTIVATE_EXT_REQ, SENDMSG_NOOP, getPackageNameByPid(setModeDeathHandler.mPid) + mode);
                }
            }
            if (mode != this.mMode) {
                status = AudioSystem.setPhoneState(mode);
                if (status == 0) {
                    if (DEBUG_MODE) {
                        Log.v(TAG, " mode successfully set to " + mode);
                    }
                    this.mMode = mode;
                } else {
                    if (setModeDeathHandler != null) {
                        this.mSetModeDeathHandlers.remove(setModeDeathHandler);
                        cb.unlinkToDeath(setModeDeathHandler, SENDMSG_REPLACE);
                    }
                    if (DEBUG_MODE) {
                        Log.w(TAG, " mode set to MODE_NORMAL after phoneState pb");
                    }
                    mode = SENDMSG_REPLACE;
                }
            } else {
                status = SENDMSG_REPLACE;
            }
            if (status == 0) {
                break;
            }
        } while (!this.mSetModeDeathHandlers.isEmpty());
        if (status == 0) {
            if (mode != 0) {
                if (this.mSetModeDeathHandlers.isEmpty()) {
                    Log.e(TAG, "setMode() different from MODE_NORMAL with empty mode client stack");
                } else {
                    newModeOwnerPid = ((SetModeDeathHandler) this.mSetModeDeathHandlers.get(SENDMSG_REPLACE)).getPid();
                }
            }
            int streamType = getActiveStreamType(UsbAudioDevice.kAudioDeviceMeta_Alsa);
            int device = getDeviceForStream(streamType);
            int index = this.mStreamStates[this.mStreamVolumeAlias[streamType]].getIndex(device);
            setStreamVolumeInt(this.mStreamVolumeAlias[streamType], index, device, true, caller);
            updateStreamVolumeAlias(true, caller);
        }
        return newModeOwnerPid;
    }

    public int getMode() {
        return this.mMode;
    }

    private void loadTouchSoundAssetDefaults() {
        SOUND_EFFECT_FILES.add("Effect_Tick.ogg");
        for (int i = SENDMSG_REPLACE; i < MSG_SET_ALL_VOLUMES; i += SENDMSG_NOOP) {
            this.SOUND_EFFECT_FILES_MAP[i][SENDMSG_REPLACE] = SENDMSG_REPLACE;
            this.SOUND_EFFECT_FILES_MAP[i][SENDMSG_NOOP] = SCO_MODE_UNDEFINED;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void loadTouchSoundAssets() {
        XmlResourceParser xmlResourceParser = null;
        if (SOUND_EFFECT_FILES.isEmpty()) {
            loadTouchSoundAssetDefaults();
            try {
                xmlResourceParser = this.mContext.getResources().getXml(17891329);
                XmlUtils.beginDocument(xmlResourceParser, TAG_AUDIO_ASSETS);
                String version = xmlResourceParser.getAttributeValue(null, ATTR_VERSION);
                boolean inTouchSoundsGroup = DEBUG_VOL;
                if (ASSET_FILE_VERSION.equals(version)) {
                    String element;
                    while (true) {
                        XmlUtils.nextElement(xmlResourceParser);
                        element = xmlResourceParser.getName();
                        if (element == null) {
                            break;
                        } else if (element.equals(TAG_GROUP)) {
                            if (GROUP_TOUCH_SOUNDS.equals(xmlResourceParser.getAttributeValue(null, ATTR_GROUP_NAME))) {
                                break;
                            }
                        }
                    }
                    while (inTouchSoundsGroup) {
                        XmlUtils.nextElement(xmlResourceParser);
                        element = xmlResourceParser.getName();
                        if (element != null && element.equals(TAG_ASSET)) {
                            String id = xmlResourceParser.getAttributeValue(null, ATTR_ASSET_ID);
                            String file = xmlResourceParser.getAttributeValue(null, ATTR_ASSET_FILE);
                            try {
                                int fx = AudioManager.class.getField(id).getInt(null);
                                int i = SOUND_EFFECT_FILES.indexOf(file);
                                if (i == SCO_MODE_UNDEFINED) {
                                    i = SOUND_EFFECT_FILES.size();
                                    SOUND_EFFECT_FILES.add(file);
                                }
                                this.SOUND_EFFECT_FILES_MAP[fx][SENDMSG_REPLACE] = i;
                            } catch (Exception e) {
                                Log.w(TAG, "Invalid touch sound ID: " + id);
                            }
                        }
                    }
                }
                if (xmlResourceParser != null) {
                    xmlResourceParser.close();
                }
            } catch (NotFoundException e2) {
                Log.w(TAG, "audio assets file not found", e2);
                if (xmlResourceParser != null) {
                    xmlResourceParser.close();
                }
            } catch (XmlPullParserException e3) {
                Log.w(TAG, "XML parser exception reading touch sound assets", e3);
                if (xmlResourceParser != null) {
                    xmlResourceParser.close();
                }
            } catch (IOException e4) {
                Log.w(TAG, "I/O exception reading touch sound assets", e4);
                if (xmlResourceParser != null) {
                    xmlResourceParser.close();
                }
            } catch (Throwable th) {
                if (xmlResourceParser != null) {
                    xmlResourceParser.close();
                }
            }
        }
    }

    public void playSoundEffect(int effectType) {
        playSoundEffectVolume(effectType, -1.0f);
    }

    public void playSoundEffectVolume(int effectType, float volume) {
        if (effectType >= MSG_SET_ALL_VOLUMES || effectType < 0) {
            Log.w(TAG, "AudioService effectType value " + effectType + " out of range");
            return;
        }
        sendMsg(this.mAudioHandler, SCO_STATE_DEACTIVATE_REQ, SENDMSG_QUEUE, effectType, (int) (1000.0f * volume), null, SENDMSG_REPLACE);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean loadSoundEffects() {
        Throwable th;
        LoadSoundEffectReply reply = new LoadSoundEffectReply();
        synchronized (reply) {
            try {
                int attempts;
                sendMsg(this.mAudioHandler, MSG_LOAD_SOUND_EFFECTS, SENDMSG_QUEUE, SENDMSG_REPLACE, SENDMSG_REPLACE, reply, SENDMSG_REPLACE);
                int attempts2 = SCO_STATE_ACTIVE_INTERNAL;
                while (reply.mStatus == SENDMSG_NOOP) {
                    try {
                        attempts = attempts2 + SCO_MODE_UNDEFINED;
                        if (attempts2 <= 0) {
                            break;
                        }
                        reply.wait(5000);
                        attempts2 = attempts;
                    } catch (Throwable th2) {
                        th = th2;
                        attempts = attempts2;
                    }
                }
                attempts = attempts2;
                if (reply.mStatus == 0) {
                    return true;
                }
                return DEBUG_VOL;
            } catch (InterruptedException e) {
                Log.w(TAG, "loadSoundEffects Interrupted while waiting sound pool loaded.");
            } catch (Throwable th3) {
                th = th3;
            }
        }
    }

    public void unloadSoundEffects() {
        sendMsg(this.mAudioHandler, MSG_UNLOAD_SOUND_EFFECTS, SENDMSG_QUEUE, SENDMSG_REPLACE, SENDMSG_REPLACE, null, SENDMSG_REPLACE);
    }

    public void reloadAudioSettings() {
        readAudioSettings(DEBUG_VOL);
    }

    private void readAudioSettings(boolean userSwitch) {
        readPersistedSettings();
        readUserRestrictions();
        int numStreamTypes = AudioSystem.getNumStreamTypes();
        int streamType = SENDMSG_REPLACE;
        while (streamType < numStreamTypes) {
            VolumeStreamState streamState = this.mStreamStates[streamType];
            if (!userSwitch || this.mStreamVolumeAlias[streamType] != SCO_STATE_ACTIVE_INTERNAL) {
                streamState.readSettings();
                synchronized (VolumeStreamState.class) {
                    if (streamState.mIsMuted && (!(isStreamAffectedByMute(streamType) || isStreamMutedByRingerMode(streamType)) || this.mUseFixedVolume)) {
                        streamState.mIsMuted = DEBUG_VOL;
                    }
                }
            }
            streamType += SENDMSG_NOOP;
        }
        setRingerModeInt(getRingerModeInternal(), DEBUG_VOL);
        checkAllFixedVolumeDevices();
        checkAllAliasStreamVolumes();
        checkMuteAffectedStreams();
        synchronized (this.mSafeMediaVolumeState) {
            this.mMusicActiveMs = MathUtils.constrain(Secure.getIntForUser(this.mContentResolver, "unsafe_volume_music_active_ms", SENDMSG_REPLACE, -2), SENDMSG_REPLACE, UNSAFE_VOLUME_MUSIC_ACTIVE_MS_MAX);
            if (this.mSafeMediaVolumeState.intValue() == SCO_STATE_ACTIVE_INTERNAL) {
                enforceSafeMediaVolume(TAG);
            }
        }
    }

    public void setSpeakerphoneOn(boolean on) {
        if (checkAudioSettingsPermission("setSpeakerphoneOn()") && checkAudioSettingAllowed("ASsso" + on)) {
            if (on) {
                if (this.mForcedUseForComm == SCO_STATE_ACTIVE_INTERNAL) {
                    sendMsg(this.mAudioHandler, MSG_SET_FORCE_USE, SENDMSG_QUEUE, SENDMSG_QUEUE, SENDMSG_REPLACE, null, SENDMSG_REPLACE);
                }
                this.mForcedUseForComm = SENDMSG_NOOP;
            } else if (this.mForcedUseForComm == SENDMSG_NOOP) {
                this.mForcedUseForComm = SENDMSG_REPLACE;
            } else {
                return;
            }
            sendMsg(this.mAudioHandler, MSG_SET_FORCE_USE, SENDMSG_QUEUE, SENDMSG_REPLACE, this.mForcedUseForComm, null, SENDMSG_REPLACE);
            if (LOUD_VOICE_MODE_SUPPORT) {
                sendMsg(this.mAudioHandler, 10001, SENDMSG_REPLACE, SENDMSG_REPLACE, SENDMSG_REPLACE, null, PERSIST_DELAY);
            }
            sendCommForceBroadcast();
        }
    }

    public boolean isSpeakerphoneOn() {
        return this.mForcedUseForComm == SENDMSG_NOOP ? true : DEBUG_VOL;
    }

    public void setBluetoothScoOn(boolean on) {
        if (checkAudioSettingsPermission("setBluetoothScoOn()") && checkAudioSettingAllowed("ASsbso" + on)) {
            setBluetoothScoOnInt(on);
        }
    }

    public void setBluetoothScoOnInt(boolean on) {
        if (on) {
            this.mForcedUseForComm = SCO_STATE_ACTIVE_INTERNAL;
        } else if (this.mForcedUseForComm == SCO_STATE_ACTIVE_INTERNAL) {
            this.mForcedUseForComm = SENDMSG_REPLACE;
        } else {
            return;
        }
        sendMsg(this.mAudioHandler, MSG_SET_FORCE_USE, SENDMSG_QUEUE, SENDMSG_REPLACE, this.mForcedUseForComm, null, SENDMSG_REPLACE);
        sendMsg(this.mAudioHandler, MSG_SET_FORCE_USE, SENDMSG_QUEUE, SENDMSG_QUEUE, this.mForcedUseForComm, null, SENDMSG_REPLACE);
        if (LOUD_VOICE_MODE_SUPPORT) {
            sendMsg(this.mAudioHandler, 10001, SENDMSG_REPLACE, SENDMSG_REPLACE, SENDMSG_REPLACE, null, PERSIST_DELAY);
        }
        sendCommForceBroadcast();
    }

    public boolean isBluetoothScoOn() {
        return this.mForcedUseForComm == SCO_STATE_ACTIVE_INTERNAL ? true : DEBUG_VOL;
    }

    public void setBluetoothA2dpOn(boolean on) {
        int i = SENDMSG_REPLACE;
        synchronized (this.mBluetoothA2dpEnabledLock) {
            this.mBluetoothA2dpEnabled = on;
            Handler handler = this.mAudioHandler;
            if (!this.mBluetoothA2dpEnabled) {
                i = MSG_SET_ALL_VOLUMES;
            }
            sendMsg(handler, MSG_SET_FORCE_BT_A2DP_USE, SENDMSG_QUEUE, SENDMSG_NOOP, i, null, SENDMSG_REPLACE);
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
        startBluetoothScoInt(cb, targetSdkVersion < MSG_PERSIST_SAFE_VOLUME_STATE ? SENDMSG_REPLACE : SCO_MODE_UNDEFINED);
    }

    public void startBluetoothScoVirtualCall(IBinder cb) {
        startBluetoothScoInt(cb, SENDMSG_REPLACE);
    }

    void startBluetoothScoInt(IBinder cb, int scoAudioMode) {
        if (checkAudioSettingsPermission("startBluetoothSco()") && this.mSystemReady) {
            ScoClient client = getScoClient(cb, true);
            long ident = Binder.clearCallingIdentity();
            client.incCount(scoAudioMode);
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void stopBluetoothSco(IBinder cb) {
        if (checkAudioSettingsPermission("stopBluetoothSco()") && this.mSystemReady) {
            ScoClient client = getScoClient(cb, DEBUG_VOL);
            long ident = Binder.clearCallingIdentity();
            if (client != null) {
                client.decCount();
            }
            Binder.restoreCallingIdentity(ident);
        }
    }

    private void checkScoAudioState() {
        if (this.mBluetoothHeadset != null && this.mBluetoothHeadsetDevice != null && this.mScoAudioState == 0 && this.mBluetoothHeadset.getAudioState(this.mBluetoothHeadsetDevice) != MSG_SET_ALL_VOLUMES) {
            this.mScoAudioState = SENDMSG_QUEUE;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private ScoClient getScoClient(IBinder cb, boolean create) {
        Throwable th;
        synchronized (this.mScoClients) {
            try {
                ScoClient client;
                int size = this.mScoClients.size();
                int i = SENDMSG_REPLACE;
                ScoClient client2 = null;
                while (i < size) {
                    try {
                        client = (ScoClient) this.mScoClients.get(i);
                        if (client.getBinder() == cb) {
                            return client;
                        }
                        i += SENDMSG_NOOP;
                        client2 = client;
                    } catch (Throwable th2) {
                        th = th2;
                        client = client2;
                    }
                }
                if (create) {
                    client = new ScoClient(cb);
                    this.mScoClients.add(client);
                } else {
                    client = client2;
                }
                return client;
            } catch (Throwable th3) {
                th = th3;
            }
        }
    }

    public void clearAllScoClients(int exceptPid, boolean stopSco) {
        synchronized (this.mScoClients) {
            Object savedClient = null;
            int size = this.mScoClients.size();
            for (int i = SENDMSG_REPLACE; i < size; i += SENDMSG_NOOP) {
                ScoClient cl = (ScoClient) this.mScoClients.get(i);
                if (cl.getPid() != exceptPid) {
                    cl.clearCount(stopSco);
                } else {
                    ScoClient savedClient2 = cl;
                }
            }
            this.mScoClients.clear();
            if (savedClient != null) {
                this.mScoClients.add(savedClient);
            }
        }
    }

    private boolean getBluetoothHeadset() {
        int i;
        boolean result = DEBUG_VOL;
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            result = adapter.getProfileProxy(this.mContext, this.mBluetoothProfileServiceListener, SENDMSG_NOOP);
        }
        Handler handler = this.mAudioHandler;
        if (result) {
            i = BT_HEADSET_CNCT_TIMEOUT_MS;
        } else {
            i = SENDMSG_REPLACE;
        }
        sendMsg(handler, MSG_BT_HEADSET_CNCT_FAILED, SENDMSG_REPLACE, SENDMSG_REPLACE, SENDMSG_REPLACE, null, i);
        return result;
    }

    private void disconnectBluetoothSco(int exceptPid) {
        synchronized (this.mScoClients) {
            checkScoAudioState();
            if (this.mScoAudioState != SENDMSG_QUEUE && this.mScoAudioState != SCO_STATE_DEACTIVATE_EXT_REQ) {
                clearAllScoClients(exceptPid, true);
            } else if (this.mBluetoothHeadsetDevice != null) {
                if (this.mBluetoothHeadset != null) {
                    if (!this.mBluetoothHeadset.stopVoiceRecognition(this.mBluetoothHeadsetDevice)) {
                        sendMsg(this.mAudioHandler, MSG_BT_HEADSET_CNCT_FAILED, SENDMSG_REPLACE, SENDMSG_REPLACE, SENDMSG_REPLACE, null, SENDMSG_REPLACE);
                    }
                } else if (this.mScoAudioState == SENDMSG_QUEUE && getBluetoothHeadset()) {
                    this.mScoAudioState = SCO_STATE_DEACTIVATE_EXT_REQ;
                }
            }
        }
    }

    private void resetBluetoothSco() {
        synchronized (this.mScoClients) {
            clearAllScoClients(SENDMSG_REPLACE, DEBUG_VOL);
            this.mScoAudioState = SENDMSG_REPLACE;
            broadcastScoConnectionState(SENDMSG_REPLACE);
        }
        AudioSystem.setParameters("A2dpSuspended=false");
        setBluetoothScoOnInt(DEBUG_VOL);
    }

    private void broadcastScoConnectionState(int state) {
        sendMsg(this.mAudioHandler, MSG_BROADCAST_BT_CONNECTION_STATE, SENDMSG_QUEUE, state, SENDMSG_REPLACE, null, SENDMSG_REPLACE);
    }

    private void onBroadcastScoConnectionState(int state) {
        Log.i(TAG, "onBroadcastScoConnectionState() state=" + state + ", pre-state=" + this.mScoConnectionState);
        if (state != this.mScoConnectionState) {
            Intent newIntent = new Intent("android.media.ACTION_SCO_AUDIO_STATE_UPDATED");
            newIntent.putExtra("android.media.extra.SCO_AUDIO_STATE", state);
            newIntent.putExtra("android.media.extra.SCO_AUDIO_PREVIOUS_STATE", this.mScoConnectionState);
            sendStickyBroadcastToAll(newIntent);
            this.mScoConnectionState = state;
        }
    }

    void setBtScoDeviceConnectionState(BluetoothDevice btDevice, int state) {
        if (btDevice != null) {
            boolean success;
            String address = btDevice.getAddress();
            BluetoothClass btClass = btDevice.getBluetoothClass();
            int outDevice = MSG_CONFIGURE_SAFE_MEDIA_VOLUME;
            if (btClass != null) {
                switch (btClass.getDeviceClass()) {
                    case 1028:
                    case 1032:
                        outDevice = 32;
                        break;
                    case 1056:
                        outDevice = 64;
                        break;
                }
            }
            if (!BluetoothAdapter.checkBluetoothAddress(address)) {
                address = "";
            }
            boolean connected = state == SENDMSG_QUEUE ? true : DEBUG_VOL;
            String btDeviceName = btDevice.getName();
            if (handleDeviceConnection(connected, outDevice, address, btDeviceName)) {
                success = handleDeviceConnection(connected, -2147483640, address, btDeviceName);
            } else {
                success = DEBUG_VOL;
            }
            if (success) {
                synchronized (this.mScoClients) {
                    if (connected) {
                        this.mBluetoothHeadsetDevice = btDevice;
                    } else {
                        this.mBluetoothHeadsetDevice = null;
                        resetBluetoothSco();
                    }
                }
            }
        }
    }

    void disconnectAllBluetoothProfiles() {
        disconnectA2dp();
        disconnectA2dpSink();
        disconnectHeadset();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void disconnectA2dp() {
        synchronized (this.mConnectedDevices) {
            synchronized (this.mA2dpAvrcpLock) {
                int i = SENDMSG_REPLACE;
                ArraySet<String> toRemove = null;
                while (i < this.mConnectedDevices.size()) {
                    ArraySet<String> toRemove2;
                    try {
                        DeviceListSpec deviceSpec = (DeviceListSpec) this.mConnectedDevices.valueAt(i);
                        if (deviceSpec.mDeviceType == DumpState.DUMP_PACKAGES) {
                            toRemove2 = toRemove != null ? toRemove : new ArraySet();
                            try {
                                toRemove2.add(deviceSpec.mDeviceAddress);
                            } catch (Throwable th) {
                                Throwable th2 = th;
                            }
                        } else {
                            toRemove2 = toRemove;
                        }
                        i += SENDMSG_NOOP;
                        toRemove = toRemove2;
                    } catch (Throwable th3) {
                        th2 = th3;
                        toRemove2 = toRemove;
                    }
                }
                if (toRemove != null) {
                    int delay = checkSendBecomingNoisyIntent(DumpState.DUMP_PACKAGES, SENDMSG_REPLACE);
                    for (i = SENDMSG_REPLACE; i < toRemove.size(); i += SENDMSG_NOOP) {
                        makeA2dpDeviceUnavailableLater((String) toRemove.valueAt(i), delay);
                    }
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void disconnectA2dpSink() {
        synchronized (this.mConnectedDevices) {
            int i = SENDMSG_REPLACE;
            ArraySet<String> toRemove = null;
            while (i < this.mConnectedDevices.size()) {
                ArraySet<String> toRemove2;
                try {
                    DeviceListSpec deviceSpec = (DeviceListSpec) this.mConnectedDevices.valueAt(i);
                    if (deviceSpec.mDeviceType == -2147352576) {
                        toRemove2 = toRemove != null ? toRemove : new ArraySet();
                        try {
                            toRemove2.add(deviceSpec.mDeviceAddress);
                        } catch (Throwable th) {
                            Throwable th2 = th;
                        }
                    } else {
                        toRemove2 = toRemove;
                    }
                    i += SENDMSG_NOOP;
                    toRemove = toRemove2;
                } catch (Throwable th3) {
                    th2 = th3;
                    toRemove2 = toRemove;
                }
            }
            if (toRemove != null) {
                for (i = SENDMSG_REPLACE; i < toRemove.size(); i += SENDMSG_NOOP) {
                    makeA2dpSrcUnavailable((String) toRemove.valueAt(i));
                }
            }
        }
    }

    void disconnectHeadset() {
        synchronized (this.mScoClients) {
            if (this.mBluetoothHeadsetDevice != null) {
                setBtScoDeviceConnectionState(this.mBluetoothHeadsetDevice, SENDMSG_REPLACE);
            }
            this.mBluetoothHeadset = null;
        }
    }

    private void onCheckMusicActive(String caller) {
        synchronized (this.mSafeMediaVolumeState) {
            if (this.mSafeMediaVolumeState.intValue() == SENDMSG_QUEUE) {
                int device = getDeviceForStream(SCO_STATE_ACTIVE_INTERNAL);
                if ((device & MSG_REPORT_NEW_ROUTES) != 0) {
                    sendMsg(this.mAudioHandler, MSG_CHECK_MUSIC_ACTIVE, SENDMSG_REPLACE, SENDMSG_REPLACE, SENDMSG_REPLACE, caller, MUSIC_ACTIVE_POLL_PERIOD_MS);
                    int index = this.mStreamStates[SCO_STATE_ACTIVE_INTERNAL].getIndex(device);
                    if (AudioSystem.isStreamActive(SCO_STATE_ACTIVE_INTERNAL, SENDMSG_REPLACE) && index > this.mSafeMediaVolumeIndex) {
                        this.mMusicActiveMs += MUSIC_ACTIVE_POLL_PERIOD_MS;
                        if (this.mMusicActiveMs > UNSAFE_VOLUME_MUSIC_ACTIVE_MS_MAX) {
                            setSafeMediaVolumeEnabled(true, caller);
                            this.mMusicActiveMs = SENDMSG_REPLACE;
                        }
                        saveMusicActiveMs();
                    }
                }
            }
        }
    }

    private void saveMusicActiveMs() {
        this.mAudioHandler.obtainMessage(MSG_PERSIST_MUSIC_ACTIVE_MS, this.mMusicActiveMs, SENDMSG_REPLACE).sendToTarget();
    }

    private void onConfigureSafeVolume(boolean force, String caller) {
        synchronized (this.mSafeMediaVolumeState) {
            int mcc = this.mContext.getResources().getConfiguration().mcc;
            if (this.mMcc != mcc || (this.mMcc == 0 && force)) {
                boolean safeMediaVolumeEnabled;
                int persistedState;
                this.mSafeMediaVolumeIndex = this.mContext.getResources().getInteger(17694863) * MSG_SET_ALL_VOLUMES;
                if (SystemProperties.getBoolean("audio.safemedia.force", DEBUG_VOL)) {
                    safeMediaVolumeEnabled = true;
                } else {
                    safeMediaVolumeEnabled = this.mContext.getResources().getBoolean(17956988);
                }
                boolean safeMediaVolumeBypass = SystemProperties.getBoolean("audio.safemedia.bypass", DEBUG_VOL);
                if (usingHwSafeMediaConfig()) {
                    this.mSafeMediaVolumeIndex = getHwSafeMediaVolumeIndex();
                    safeMediaVolumeEnabled = isHwSafeMediaVolumeEnabled();
                }
                if (!safeMediaVolumeEnabled || safeMediaVolumeBypass) {
                    persistedState = SENDMSG_NOOP;
                    this.mSafeMediaVolumeState = Integer.valueOf(SENDMSG_NOOP);
                } else {
                    persistedState = SCO_STATE_ACTIVE_INTERNAL;
                    if (this.mSafeMediaVolumeState.intValue() != SENDMSG_QUEUE) {
                        if (this.mMusicActiveMs == 0) {
                            this.mSafeMediaVolumeState = Integer.valueOf(SCO_STATE_ACTIVE_INTERNAL);
                            enforceSafeMediaVolume(caller);
                        } else {
                            this.mSafeMediaVolumeState = Integer.valueOf(SENDMSG_QUEUE);
                        }
                    }
                }
                this.mMcc = mcc;
                sendMsg(this.mAudioHandler, MSG_PERSIST_SAFE_VOLUME_STATE, SENDMSG_QUEUE, persistedState, SENDMSG_REPLACE, null, SENDMSG_REPLACE);
            }
        }
    }

    private int checkForRingerModeChange(int oldIndex, int direction, int step, boolean isMuted, String caller, int flags) {
        boolean isTv = this.mPlatformType == SENDMSG_QUEUE ? true : DEBUG_VOL;
        int result = SENDMSG_NOOP;
        int ringerMode = getRingerModeInternal();
        switch (ringerMode) {
            case SENDMSG_REPLACE /*0*/:
                if (isTv && direction == SCO_MODE_UNDEFINED && oldIndex >= step * SENDMSG_QUEUE && isMuted) {
                    ringerMode = SENDMSG_QUEUE;
                } else {
                    if (!(direction == SENDMSG_NOOP || direction == MSG_SET_A2DP_SRC_CONNECTION_STATE)) {
                        if (direction == MSG_SET_WIRED_DEVICE_CONNECTION_STATE) {
                        }
                    }
                    if (!this.mVolumePolicy.volumeUpToExitSilent) {
                        result = 129;
                    } else if (this.mHasVibrator && direction == SENDMSG_NOOP) {
                        ringerMode = SENDMSG_NOOP;
                    } else {
                        ringerMode = SENDMSG_QUEUE;
                        result = SCO_STATE_ACTIVE_INTERNAL;
                    }
                }
                result &= -2;
                break;
            case SENDMSG_NOOP /*1*/:
                if (!this.mHasVibrator) {
                    Log.e(TAG, "checkForRingerModeChange() current ringer mode is vibratebut no vibrator is present");
                    break;
                }
                if (direction != SCO_MODE_UNDEFINED) {
                    if (!(direction == SENDMSG_NOOP || direction == MSG_SET_A2DP_SRC_CONNECTION_STATE)) {
                        if (direction == MSG_SET_WIRED_DEVICE_CONNECTION_STATE) {
                        }
                    }
                    ringerMode = SENDMSG_QUEUE;
                    result = SCO_STATE_ACTIVE_INTERNAL;
                } else if (isTv && oldIndex >= step * SENDMSG_QUEUE && isMuted) {
                    ringerMode = SENDMSG_QUEUE;
                } else if (this.mPrevVolDirection != SCO_MODE_UNDEFINED) {
                    if (!this.mVolumePolicy.volumeDownToEnterSilent) {
                        result = 2049;
                    } else if (SystemClock.uptimeMillis() - this.mLoweredFromNormalToVibrateTime > ((long) this.mVolumePolicy.vibrateToSilentDebounce) && this.mRingerModeDelegate.canVolumeDownEnterSilent()) {
                        ringerMode = SENDMSG_REPLACE;
                    }
                }
                result &= -2;
                break;
                break;
            case SENDMSG_QUEUE /*2*/:
                if (direction != SCO_MODE_UNDEFINED) {
                    if (isTv && (direction == MSG_SET_A2DP_SRC_CONNECTION_STATE || direction == -100)) {
                        if (this.mHasVibrator) {
                            ringerMode = SENDMSG_NOOP;
                        } else {
                            ringerMode = SENDMSG_REPLACE;
                        }
                        result = SENDMSG_REPLACE;
                        break;
                    }
                } else if (!this.mHasVibrator) {
                    if (oldIndex == step && this.mVolumePolicy.volumeDownToEnterSilent) {
                        ringerMode = SENDMSG_REPLACE;
                        break;
                    }
                } else if (step <= oldIndex && oldIndex < step * SENDMSG_QUEUE) {
                    ringerMode = SENDMSG_NOOP;
                    this.mLoweredFromNormalToVibrateTime = SystemClock.uptimeMillis();
                    break;
                }
            default:
                Log.e(TAG, "checkForRingerModeChange() wrong ringer mode: " + ringerMode);
                break;
        }
        if (isAndroidNPlus(caller) && wouldToggleZenMode(ringerMode) && !this.mNm.isNotificationPolicyAccessGrantedForPackage(caller) && (flags & DumpState.DUMP_PREFERRED) == 0) {
            throw new SecurityException("Not allowed to change Do Not Disturb state");
        }
        setRingerMode(ringerMode, "AudioService.checkForRingerModeChange", DEBUG_VOL);
        this.mPrevVolDirection = direction;
        return result;
    }

    public boolean isStreamAffectedByRingerMode(int streamType) {
        return (this.mRingerModeAffectedStreams & (SENDMSG_NOOP << streamType)) != 0 ? true : DEBUG_VOL;
    }

    private boolean isStreamMutedByRingerMode(int streamType) {
        return (this.mRingerModeMutedStreams & (SENDMSG_NOOP << streamType)) != 0 ? true : DEBUG_VOL;
    }

    private boolean updateRingerModeAffectedStreams() {
        int ringerModeAffectedStreams = System.getIntForUser(this.mContentResolver, "mode_ringer_streams_affected", 166, -2);
        if (this.mPlatformType == SENDMSG_QUEUE) {
            ringerModeAffectedStreams = SENDMSG_REPLACE;
        } else if (this.mRingerModeDelegate != null) {
            ringerModeAffectedStreams = this.mRingerModeDelegate.getRingerModeAffectedStreams(ringerModeAffectedStreams);
        }
        synchronized (this.mCameraSoundForced) {
            if (this.mCameraSoundForced.booleanValue()) {
                ringerModeAffectedStreams &= -129;
            } else {
                ringerModeAffectedStreams |= DumpState.DUMP_PACKAGES;
            }
        }
        if (this.mStreamVolumeAlias[MSG_SET_FORCE_USE] == SENDMSG_QUEUE) {
            ringerModeAffectedStreams |= DumpState.DUMP_SHARED_USERS;
        } else {
            ringerModeAffectedStreams &= -257;
        }
        if (ringerModeAffectedStreams != this.mRingerModeAffectedStreams) {
            System.putIntForUser(this.mContentResolver, "mode_ringer_streams_affected", ringerModeAffectedStreams, -2);
            this.mRingerModeAffectedStreams = ringerModeAffectedStreams;
            return true;
        } else if (ringerModeAffectedStreams != this.mRingerModeAffectedStreams || ActivityManager.getCurrentUser() == 0) {
            return DEBUG_VOL;
        } else {
            System.putIntForUser(this.mContentResolver, "mode_ringer_streams_affected", ringerModeAffectedStreams, -2);
            Log.d(TAG, "updateRingerModeAffectedStreams enter sub user ringerModeAffectedStreams:" + ringerModeAffectedStreams);
            return true;
        }
    }

    public boolean isStreamAffectedByMute(int streamType) {
        return (this.mMuteAffectedStreams & (SENDMSG_NOOP << streamType)) != 0 ? true : DEBUG_VOL;
    }

    private void ensureValidDirection(int direction) {
        switch (direction) {
            case -100:
            case SCO_MODE_UNDEFINED /*-1*/:
            case SENDMSG_REPLACE /*0*/:
            case SENDMSG_NOOP /*1*/:
            case MSG_SET_WIRED_DEVICE_CONNECTION_STATE /*100*/:
            case MSG_SET_A2DP_SRC_CONNECTION_STATE /*101*/:
            default:
                throw new IllegalArgumentException("Bad direction " + direction);
        }
    }

    private void ensureValidStreamType(int streamType) {
        if (streamType < 0 || streamType >= this.mStreamStates.length) {
            throw new IllegalArgumentException("Bad stream type " + streamType);
        }
    }

    private boolean isMuteAdjust(int adjust) {
        if (adjust == -100 || adjust == MSG_SET_WIRED_DEVICE_CONNECTION_STATE || adjust == MSG_SET_A2DP_SRC_CONNECTION_STATE) {
            return true;
        }
        return DEBUG_VOL;
    }

    private boolean isInCommunication() {
        TelecomManager telecomManager = (TelecomManager) this.mContext.getSystemService("telecom");
        long ident = Binder.clearCallingIdentity();
        boolean IsInCall = telecomManager.isInCall();
        Binder.restoreCallingIdentity(ident);
        if (IsInCall || getMode() == SCO_STATE_ACTIVE_INTERNAL) {
            return true;
        }
        return DEBUG_VOL;
    }

    private boolean isAfMusicActiveRecently(int delay_ms) {
        if (AudioSystem.isStreamActive(SCO_STATE_ACTIVE_INTERNAL, delay_ms)) {
            return true;
        }
        return AudioSystem.isStreamActiveRemotely(SCO_STATE_ACTIVE_INTERNAL, delay_ms);
    }

    protected int getActiveStreamType(int suggestedStreamType) {
        switch (this.mPlatformType) {
            case SENDMSG_NOOP /*1*/:
                if (isInCommunication()) {
                    return AudioSystem.getForceUse(SENDMSG_REPLACE) == SCO_STATE_ACTIVE_INTERNAL ? MSG_BTA2DP_DOCK_TIMEOUT : SENDMSG_REPLACE;
                } else {
                    if (suggestedStreamType == UsbAudioDevice.kAudioDeviceMeta_Alsa) {
                        if (isAfMusicActiveRecently(SENDMSG_REPLACE)) {
                            if (DEBUG_VOL) {
                                Log.v(TAG, "getActiveStreamType: Forcing STREAM_MUSIC stream active");
                            }
                            return SCO_STATE_ACTIVE_INTERNAL;
                        } else if (AudioSystem.isStreamActive(SENDMSG_REPLACE, SENDMSG_REPLACE)) {
                            if (AudioSystem.getForceUse(SENDMSG_REPLACE) == SCO_STATE_ACTIVE_INTERNAL) {
                                if (DEBUG_VOL) {
                                    Log.v(TAG, "getActiveStreamType: STREAM_VOICE_CALL is active, but eForcing STREAM_BLUETOOTH_SCO...");
                                }
                                return MSG_BTA2DP_DOCK_TIMEOUT;
                            }
                            if (DEBUG_VOL) {
                                Log.v(TAG, "getActiveStreamType: Forcing STREAM_VOICE_CALL stream active");
                            }
                            return SENDMSG_REPLACE;
                        } else if (AudioSystem.isStreamActive(MSG_BTA2DP_DOCK_TIMEOUT, SENDMSG_REPLACE)) {
                            if (DEBUG_VOL) {
                                Log.v(TAG, "getActiveStreamType: Forcing STREAM_BLUETOOTH_SCO stream active");
                            }
                            return MSG_BTA2DP_DOCK_TIMEOUT;
                        } else {
                            if (DEBUG_VOL) {
                                Log.v(TAG, "getActiveStreamType: Forcing STREAM_RING b/c default");
                            }
                            return SENDMSG_QUEUE;
                        }
                    } else if (isAfMusicActiveRecently(SENDMSG_REPLACE)) {
                        if (DEBUG_VOL) {
                            Log.v(TAG, "getActiveStreamType: Forcing STREAM_MUSIC stream active");
                        }
                        return SCO_STATE_ACTIVE_INTERNAL;
                    }
                }
                break;
            case SENDMSG_QUEUE /*2*/:
                if (suggestedStreamType == UsbAudioDevice.kAudioDeviceMeta_Alsa) {
                    return SCO_STATE_ACTIVE_INTERNAL;
                }
                break;
            default:
                if (isInCommunication()) {
                    if (AudioSystem.getForceUse(SENDMSG_REPLACE) == SCO_STATE_ACTIVE_INTERNAL) {
                        if (DEBUG_VOL) {
                            Log.v(TAG, "getActiveStreamType: Forcing STREAM_BLUETOOTH_SCO");
                        }
                        return MSG_BTA2DP_DOCK_TIMEOUT;
                    }
                    if (DEBUG_VOL) {
                        Log.v(TAG, "getActiveStreamType: Forcing STREAM_VOICE_CALL");
                    }
                    return SENDMSG_REPLACE;
                } else if (AudioSystem.isStreamActive(SCO_STATE_DEACTIVATE_REQ, StreamOverride.sDelayMs) || AudioSystem.isStreamActive(SENDMSG_QUEUE, StreamOverride.sDelayMs)) {
                    if (DEBUG_VOL) {
                        Log.v(TAG, "getActiveStreamType: Forcing STREAM_NOTIFICATION");
                    }
                    return SCO_STATE_DEACTIVATE_REQ;
                } else if (suggestedStreamType == UsbAudioDevice.kAudioDeviceMeta_Alsa) {
                    if (isAfMusicActiveRecently(StreamOverride.sDelayMs)) {
                        if (DEBUG_VOL) {
                            Log.v(TAG, "getActiveStreamType: forcing STREAM_MUSIC");
                        }
                        return SCO_STATE_ACTIVE_INTERNAL;
                    }
                    if (DEBUG_VOL) {
                        Log.v(TAG, "getActiveStreamType: using STREAM_NOTIFICATION as default");
                    }
                    return SCO_STATE_DEACTIVATE_REQ;
                }
                break;
        }
        if (DEBUG_VOL) {
            Log.v(TAG, "getActiveStreamType: Returning suggested type " + suggestedStreamType);
        }
        return suggestedStreamType;
    }

    private void broadcastRingerMode(String action, int ringerMode) {
        Intent broadcast = new Intent(action);
        broadcast.putExtra("android.media.EXTRA_RINGER_MODE", ringerMode);
        broadcast.addFlags(603979776);
        sendStickyBroadcastToAll(broadcast);
    }

    private void broadcastVibrateSetting(int vibrateType) {
        if (ActivityManagerNative.isSystemReady()) {
            Intent broadcast = new Intent("android.media.VIBRATE_SETTING_CHANGED");
            broadcast.putExtra("android.media.EXTRA_VIBRATE_TYPE", vibrateType);
            broadcast.putExtra("android.media.EXTRA_VIBRATE_SETTING", getVibrateSetting(vibrateType));
            sendBroadcastToAll(broadcast);
        }
    }

    private void queueMsgUnderWakeLock(Handler handler, int msg, int arg1, int arg2, Object obj, int delay) {
        long ident = Binder.clearCallingIdentity();
        this.mAudioEventWakeLock.acquire();
        Binder.restoreCallingIdentity(ident);
        sendMsg(handler, msg, SENDMSG_QUEUE, arg1, arg2, obj, delay);
    }

    protected static void sendMsg(Handler handler, int msg, int existingMsgPolicy, int arg1, int arg2, Object obj, int delay) {
        if (existingMsgPolicy == 0) {
            handler.removeMessages(msg);
        } else if (existingMsgPolicy == SENDMSG_NOOP && handler.hasMessages(msg)) {
            return;
        }
        synchronized (mLastDeviceConnectMsgTime) {
            long time = SystemClock.uptimeMillis() + ((long) delay);
            handler.sendMessageAtTime(handler.obtainMessage(msg, arg1, arg2, obj), time);
            if (!(msg == MSG_SET_WIRED_DEVICE_CONNECTION_STATE || msg == MSG_SET_A2DP_SRC_CONNECTION_STATE)) {
                if (msg == MSG_SET_A2DP_SINK_CONNECTION_STATE) {
                }
            }
            mLastDeviceConnectMsgTime = Long.valueOf(time);
        }
    }

    boolean checkAudioSettingsPermission(String method) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_AUDIO_SETTINGS") == 0) {
            return true;
        }
        Log.w(TAG, "Audio Settings Permission Denial: " + method + " from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
        return DEBUG_VOL;
    }

    private int getDeviceForStream(int stream) {
        int device = getDevicesForStream(stream);
        if (((device + SCO_MODE_UNDEFINED) & device) == 0) {
            return device;
        }
        if ((device & SENDMSG_QUEUE) != 0) {
            return SENDMSG_QUEUE;
        }
        if ((DumpState.DUMP_DOMAIN_PREFERRED & device) != 0) {
            return DumpState.DUMP_DOMAIN_PREFERRED;
        }
        if ((DumpState.DUMP_FROZEN & device) != 0) {
            return DumpState.DUMP_FROZEN;
        }
        if ((2097152 & device) != 0) {
            return 2097152;
        }
        return device & 896;
    }

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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void observeDevicesForStreams(int skipStream) {
        synchronized (VolumeStreamState.class) {
            int stream = SENDMSG_REPLACE;
            while (true) {
                if (stream < this.mStreamStates.length) {
                    if (stream != skipStream) {
                        this.mStreamStates[stream].observeDevicesForStream_syncVSS(DEBUG_VOL);
                    }
                    stream += SENDMSG_NOOP;
                }
            }
        }
    }

    public void setWiredDeviceConnectionState(int type, int state, String address, String name, String caller) {
        synchronized (this.mConnectedDevices) {
            if (DEBUG_DEVICES) {
                Slog.i(TAG, "setWiredDeviceConnectionState(" + state + " nm: " + name + " addr:" + address + ")");
            }
            int delay = checkSendBecomingNoisyIntent(type, state);
            queueMsgUnderWakeLock(this.mAudioHandler, MSG_SET_WIRED_DEVICE_CONNECTION_STATE, SENDMSG_REPLACE, SENDMSG_REPLACE, new WiredDeviceConnectionState(type, state, address, name, caller), delay);
            if (SOUND_EFFECTS_SUPPORT) {
                queueMsgUnderWakeLock(this.mAudioHandler, 10002, type, state, name, delay);
            }
        }
    }

    public int setBluetoothA2dpDeviceConnectionState(BluetoothDevice device, int state, int profile) {
        int i = SENDMSG_NOOP;
        if (profile == SENDMSG_QUEUE || profile == 11) {
            int delay;
            if (state != 0) {
                Log.i(TAG, "state=" + state);
                SystemProperties.set("runtime.enable.hw_fadein", String.valueOf(DEBUG_VOL));
            } else {
                Log.i(TAG, "state=" + state);
                SystemProperties.set("runtime.enable.hw_fadein", String.valueOf(true));
            }
            synchronized (this.mConnectedDevices) {
                if (profile == SENDMSG_QUEUE) {
                    if (state != SENDMSG_QUEUE) {
                        i = SENDMSG_REPLACE;
                    }
                    delay = checkSendBecomingNoisyIntent(DumpState.DUMP_PACKAGES, i);
                } else {
                    delay = SENDMSG_REPLACE;
                }
                queueMsgUnderWakeLock(this.mAudioHandler, profile == SENDMSG_QUEUE ? MSG_SET_A2DP_SINK_CONNECTION_STATE : MSG_SET_A2DP_SRC_CONNECTION_STATE, state, SENDMSG_REPLACE, device, delay);
            }
            return delay;
        }
        throw new IllegalArgumentException("invalid profile " + profile);
    }

    private void makeA2dpDeviceAvailable(String address, String name) {
        sendMsg(this.mAudioHandler, SENDMSG_REPLACE, SENDMSG_QUEUE, DumpState.DUMP_PACKAGES, SENDMSG_REPLACE, this.mStreamStates[SCO_STATE_ACTIVE_INTERNAL], SENDMSG_REPLACE);
        setBluetoothA2dpOnInt(true);
        AudioSystem.setDeviceConnectionState(DumpState.DUMP_PACKAGES, SENDMSG_NOOP, address, name);
        AudioSystem.setParameters("A2dpSuspended=false");
        this.mConnectedDevices.put(makeDeviceListKey(DumpState.DUMP_PACKAGES, address), new DeviceListSpec(DumpState.DUMP_PACKAGES, name, address));
    }

    private void onSendBecomingNoisyIntent() {
        sendBroadcastToAll(new Intent("android.media.AUDIO_BECOMING_NOISY"));
    }

    private void makeA2dpDeviceUnavailableNow(String address) {
        synchronized (this.mA2dpAvrcpLock) {
            this.mAvrcpAbsVolSupported = DEBUG_VOL;
        }
        AudioSystem.setDeviceConnectionState(DumpState.DUMP_PACKAGES, SENDMSG_REPLACE, address, "");
        this.mConnectedDevices.remove(makeDeviceListKey(DumpState.DUMP_PACKAGES, address));
        synchronized (this.mCurAudioRoutes) {
            if (this.mCurAudioRoutes.bluetoothName != null) {
                this.mCurAudioRoutes.bluetoothName = null;
                sendMsg(this.mAudioHandler, MSG_REPORT_NEW_ROUTES, SENDMSG_NOOP, SENDMSG_REPLACE, SENDMSG_REPLACE, null, SENDMSG_REPLACE);
            }
        }
    }

    private void makeA2dpDeviceUnavailableLater(String address, int delayMs) {
        AudioSystem.setParameters("A2dpSuspended=true");
        this.mConnectedDevices.remove(makeDeviceListKey(DumpState.DUMP_PACKAGES, address));
        this.mAudioHandler.sendMessageDelayed(this.mAudioHandler.obtainMessage(MSG_BTA2DP_DOCK_TIMEOUT, address), (long) delayMs);
    }

    private void makeA2dpSrcAvailable(String address) {
        AudioSystem.setDeviceConnectionState(-2147352576, SENDMSG_NOOP, address, "");
        this.mConnectedDevices.put(makeDeviceListKey(-2147352576, address), new DeviceListSpec(-2147352576, "", address));
    }

    private void makeA2dpSrcUnavailable(String address) {
        AudioSystem.setDeviceConnectionState(-2147352576, SENDMSG_REPLACE, address, "");
        this.mConnectedDevices.remove(makeDeviceListKey(-2147352576, address));
    }

    private void cancelA2dpDeviceTimeout() {
        this.mAudioHandler.removeMessages(MSG_BTA2DP_DOCK_TIMEOUT);
    }

    private boolean hasScheduledA2dpDockTimeout() {
        return this.mAudioHandler.hasMessages(MSG_BTA2DP_DOCK_TIMEOUT);
    }

    private void onSetA2dpSinkConnectionState(BluetoothDevice btDevice, int state) {
        Log.d(TAG, "onSetA2dpSinkConnectionState state=" + state);
        if (btDevice != null) {
            String address = btDevice.getAddress();
            if (!BluetoothAdapter.checkBluetoothAddress(address)) {
                address = "";
            }
            synchronized (this.mConnectedDevices) {
                boolean isConnected = ((DeviceListSpec) this.mConnectedDevices.get(makeDeviceListKey(DumpState.DUMP_PACKAGES, btDevice.getAddress()))) != null ? true : DEBUG_VOL;
                if (isConnected && state != SENDMSG_QUEUE) {
                    if (!btDevice.isBluetoothDock()) {
                        makeA2dpDeviceUnavailableNow(address);
                    } else if (state == 0) {
                        makeA2dpDeviceUnavailableLater(address, BTA2DP_DOCK_TIMEOUT_MILLIS);
                    }
                    synchronized (this.mCurAudioRoutes) {
                        if (this.mCurAudioRoutes.bluetoothName != null) {
                            this.mCurAudioRoutes.bluetoothName = null;
                            sendMsg(this.mAudioHandler, MSG_REPORT_NEW_ROUTES, SENDMSG_NOOP, SENDMSG_REPLACE, SENDMSG_REPLACE, null, SENDMSG_REPLACE);
                        }
                    }
                } else if (!isConnected && state == SENDMSG_QUEUE) {
                    if (btDevice.isBluetoothDock()) {
                        cancelA2dpDeviceTimeout();
                        this.mDockAddress = address;
                    } else if (hasScheduledA2dpDockTimeout()) {
                        cancelA2dpDeviceTimeout();
                        makeA2dpDeviceUnavailableNow(this.mDockAddress);
                    }
                    makeA2dpDeviceAvailable(address, btDevice.getName());
                    synchronized (this.mCurAudioRoutes) {
                        String name = btDevice.getAliasName();
                        if (!TextUtils.equals(this.mCurAudioRoutes.bluetoothName, name)) {
                            this.mCurAudioRoutes.bluetoothName = name;
                            sendMsg(this.mAudioHandler, MSG_REPORT_NEW_ROUTES, SENDMSG_NOOP, SENDMSG_REPLACE, SENDMSG_REPLACE, null, SENDMSG_REPLACE);
                        }
                    }
                }
            }
        }
    }

    private void onSetA2dpSourceConnectionState(BluetoothDevice btDevice, int state) {
        Log.d(TAG, "onSetA2dpSourceConnectionState state=" + state);
        if (btDevice != null) {
            String address = btDevice.getAddress();
            if (!BluetoothAdapter.checkBluetoothAddress(address)) {
                address = "";
            }
            synchronized (this.mConnectedDevices) {
                boolean isConnected = ((DeviceListSpec) this.mConnectedDevices.get(makeDeviceListKey(-2147352576, address))) != null ? true : DEBUG_VOL;
                if (isConnected && state != SENDMSG_QUEUE) {
                    makeA2dpSrcUnavailable(address);
                } else if (!isConnected && state == SENDMSG_QUEUE) {
                    makeA2dpSrcAvailable(address);
                }
            }
        }
    }

    public void avrcpSupportsAbsoluteVolume(String address, boolean support) {
        synchronized (this.mA2dpAvrcpLock) {
            this.mAvrcpAbsVolSupported = support;
            sendMsg(this.mAudioHandler, SENDMSG_REPLACE, SENDMSG_QUEUE, DumpState.DUMP_PACKAGES, SENDMSG_REPLACE, this.mStreamStates[SCO_STATE_ACTIVE_INTERNAL], SENDMSG_REPLACE);
            sendMsg(this.mAudioHandler, SENDMSG_REPLACE, SENDMSG_QUEUE, DumpState.DUMP_PACKAGES, SENDMSG_REPLACE, this.mStreamStates[SENDMSG_QUEUE], SENDMSG_REPLACE);
        }
    }

    private boolean handleDeviceConnection(boolean connect, int device, String address, String deviceName) {
        if (DEBUG_DEVICES) {
            Slog.i(TAG, "handleDeviceConnection(" + connect + " dev:" + Integer.toHexString(device) + " address:" + address + " name:" + deviceName + ")");
        }
        synchronized (this.mConnectedDevices) {
            String deviceKey = makeDeviceListKey(device, address);
            if (DEBUG_DEVICES) {
                Slog.i(TAG, "deviceKey:" + deviceKey);
            }
            DeviceListSpec deviceSpec = (DeviceListSpec) this.mConnectedDevices.get(deviceKey);
            boolean isConnected = deviceSpec != null ? true : DEBUG_VOL;
            if (DEBUG_DEVICES) {
                Slog.i(TAG, "deviceSpec:" + deviceSpec + " is(already)Connected:" + isConnected);
            }
            if (connect && !isConnected) {
                int res = AudioSystem.setDeviceConnectionState(device, SENDMSG_NOOP, address, deviceName);
                if (res != 0) {
                    Slog.e(TAG, "not connecting device 0x" + Integer.toHexString(device) + " due to command error " + res);
                    return DEBUG_VOL;
                }
                String device_out_key;
                this.mConnectedDevices.put(deviceKey, new DeviceListSpec(device, deviceName, address));
                if (SCO_STATE_DEACTIVATE_EXT_REQ == device) {
                    device_out_key = makeDeviceListKey(MSG_SET_FORCE_USE, "");
                    if (this.mConnectedDevices.get(device_out_key) != null ? true : DEBUG_VOL) {
                        AudioSystem.setDeviceConnectionState(MSG_SET_FORCE_USE, SENDMSG_REPLACE, "", "");
                        this.mConnectedDevices.remove(device_out_key);
                    }
                }
                if (MSG_SET_FORCE_USE == device) {
                    device_out_key = makeDeviceListKey(SCO_STATE_DEACTIVATE_EXT_REQ, "");
                    if (this.mConnectedDevices.get(device_out_key) != null ? true : DEBUG_VOL) {
                        AudioSystem.setDeviceConnectionState(SCO_STATE_DEACTIVATE_EXT_REQ, SENDMSG_REPLACE, "", "");
                        this.mConnectedDevices.remove(device_out_key);
                    }
                    String device_in_key = makeDeviceListKey(-2147483632, "");
                    if (this.mConnectedDevices.get(device_in_key) != null ? true : DEBUG_VOL) {
                        AudioSystem.setDeviceConnectionState(-2147483632, SENDMSG_REPLACE, "", "");
                        this.mConnectedDevices.remove(device_in_key);
                    }
                }
                if (LOUD_VOICE_MODE_SUPPORT) {
                    sendMsg(this.mAudioHandler, 10001, SENDMSG_REPLACE, SENDMSG_REPLACE, SENDMSG_REPLACE, null, PERSIST_DELAY);
                }
                return true;
            } else if (connect || !isConnected) {
                return DEBUG_VOL;
            } else {
                AudioSystem.setDeviceConnectionState(device, SENDMSG_REPLACE, address, deviceName);
                this.mConnectedDevices.remove(deviceKey);
                if (LOUD_VOICE_MODE_SUPPORT) {
                    sendMsg(this.mAudioHandler, 10001, SENDMSG_REPLACE, SENDMSG_REPLACE, SENDMSG_REPLACE, null, PERSIST_DELAY);
                }
                return true;
            }
        }
    }

    private int checkSendBecomingNoisyIntent(int device, int state) {
        int delay = SENDMSG_REPLACE;
        if (state == 0 && (this.mBecomingNoisyIntentDevices & device) != 0) {
            int devices = SENDMSG_REPLACE;
            for (int i = SENDMSG_REPLACE; i < this.mConnectedDevices.size(); i += SENDMSG_NOOP) {
                int dev = ((DeviceListSpec) this.mConnectedDevices.valueAt(i)).mDeviceType;
                if ((UsbAudioDevice.kAudioDeviceMeta_Alsa & dev) == 0 && (this.mBecomingNoisyIntentDevices & dev) != 0) {
                    devices |= dev;
                }
            }
            if (devices == device) {
                sendMsg(this.mAudioHandler, MSG_BROADCAST_AUDIO_BECOMING_NOISY, SENDMSG_REPLACE, SENDMSG_REPLACE, SENDMSG_REPLACE, null, SENDMSG_REPLACE);
                delay = INDICATE_SYSTEM_READY_RETRY_DELAY_MS;
            }
        }
        if (this.mAudioHandler.hasMessages(MSG_SET_A2DP_SRC_CONNECTION_STATE) || this.mAudioHandler.hasMessages(MSG_SET_A2DP_SINK_CONNECTION_STATE) || this.mAudioHandler.hasMessages(MSG_SET_WIRED_DEVICE_CONNECTION_STATE)) {
            synchronized (mLastDeviceConnectMsgTime) {
                long time = SystemClock.uptimeMillis();
                if (mLastDeviceConnectMsgTime.longValue() > time) {
                    delay = ((int) (mLastDeviceConnectMsgTime.longValue() - time)) + 30;
                }
            }
        }
        return delay;
    }

    private void onSetWiredDeviceConnectionState(int device, int state, String address, String deviceName, String caller) {
        boolean z = true;
        if (DEBUG_DEVICES) {
            Slog.i(TAG, "onSetWiredDeviceConnectionState(dev:" + Integer.toHexString(device) + " state:" + Integer.toHexString(state) + " address:" + address + " deviceName:" + deviceName + " caller: " + caller + ");");
        }
        synchronized (this.mConnectedDevices) {
            if (state == 0) {
                if (!(device == SCO_STATE_DEACTIVATE_EXT_REQ || device == MSG_SET_FORCE_USE)) {
                    if (device == DumpState.DUMP_INTENT_FILTER_VERIFIERS) {
                    }
                }
                setBluetoothA2dpOnInt(true);
            }
            boolean isUsb = (device & -24577) != 0 ? (UsbAudioDevice.kAudioDeviceMeta_Alsa & device) != 0 ? (2147477503 & device) == 0 ? true : DEBUG_VOL : DEBUG_VOL : true;
            if (state != SENDMSG_NOOP) {
                z = DEBUG_VOL;
            }
            if (handleDeviceConnection(z, device, address, deviceName)) {
                if (state != 0) {
                    if (!(device == SCO_STATE_DEACTIVATE_EXT_REQ || device == MSG_SET_FORCE_USE)) {
                        if (device == DumpState.DUMP_INTENT_FILTER_VERIFIERS) {
                        }
                        if ((device & MSG_REPORT_NEW_ROUTES) != 0) {
                            sendMsg(this.mAudioHandler, MSG_CHECK_MUSIC_ACTIVE, SENDMSG_REPLACE, SENDMSG_REPLACE, SENDMSG_REPLACE, caller, MUSIC_ACTIVE_POLL_PERIOD_MS);
                        }
                        if (isPlatformTelevision() && (device & DumpState.DUMP_PROVIDERS) != 0) {
                            this.mFixedVolumeDevices |= DumpState.DUMP_PROVIDERS;
                            checkAllFixedVolumeDevices();
                            if (this.mHdmiManager != null) {
                                synchronized (this.mHdmiManager) {
                                    if (this.mHdmiPlaybackClient != null) {
                                        this.mHdmiCecSink = DEBUG_VOL;
                                        this.mHdmiPlaybackClient.queryDisplayStatus(this.mHdmiDisplayStatusCallback);
                                    }
                                }
                            }
                        }
                    }
                    setBluetoothA2dpOnInt(DEBUG_VOL);
                    if ((device & MSG_REPORT_NEW_ROUTES) != 0) {
                        sendMsg(this.mAudioHandler, MSG_CHECK_MUSIC_ACTIVE, SENDMSG_REPLACE, SENDMSG_REPLACE, SENDMSG_REPLACE, caller, MUSIC_ACTIVE_POLL_PERIOD_MS);
                    }
                    this.mFixedVolumeDevices |= DumpState.DUMP_PROVIDERS;
                    checkAllFixedVolumeDevices();
                    if (this.mHdmiManager != null) {
                        synchronized (this.mHdmiManager) {
                            if (this.mHdmiPlaybackClient != null) {
                                this.mHdmiCecSink = DEBUG_VOL;
                                this.mHdmiPlaybackClient.queryDisplayStatus(this.mHdmiDisplayStatusCallback);
                            }
                        }
                    }
                } else if (!(!isPlatformTelevision() || (device & DumpState.DUMP_PROVIDERS) == 0 || this.mHdmiManager == null)) {
                    synchronized (this.mHdmiManager) {
                        this.mHdmiCecSink = DEBUG_VOL;
                    }
                }
                if (!(isUsb || device == -2147483632)) {
                    sendDeviceConnectionIntent(device, state, address, deviceName);
                    sendDeviceConnectionIntentForImcs(device, state, deviceName);
                }
                return;
            }
        }
    }

    private void configureHdmiPlugIntent(Intent intent, int state) {
        intent.setAction("android.media.action.HDMI_AUDIO_PLUG");
        intent.putExtra("android.media.extra.AUDIO_PLUG_STATE", state);
        if (state == SENDMSG_NOOP) {
            ArrayList<AudioPort> ports = new ArrayList();
            if (AudioSystem.listAudioPorts(ports, new int[SENDMSG_NOOP]) == 0) {
                for (AudioPort port : ports) {
                    if (port instanceof AudioDevicePort) {
                        AudioDevicePort devicePort = (AudioDevicePort) port;
                        if (devicePort.type() == 1024 || devicePort.type() == 262144) {
                            int i;
                            int[] formats = AudioFormat.filterPublicFormats(devicePort.formats());
                            if (formats.length > 0) {
                                ArrayList<Integer> encodingList = new ArrayList(SENDMSG_NOOP);
                                int length = formats.length;
                                for (i = SENDMSG_REPLACE; i < length; i += SENDMSG_NOOP) {
                                    int format = formats[i];
                                    if (format != 0) {
                                        encodingList.add(Integer.valueOf(format));
                                    }
                                }
                                int[] encodingArray = new int[encodingList.size()];
                                int i2 = SENDMSG_REPLACE;
                                while (true) {
                                    i = encodingArray.length;
                                    if (i2 >= r0) {
                                        break;
                                    }
                                    encodingArray[i2] = ((Integer) encodingList.get(i2)).intValue();
                                    i2 += SENDMSG_NOOP;
                                }
                                intent.putExtra("android.media.extra.ENCODINGS", encodingArray);
                            }
                            int maxChannels = SENDMSG_REPLACE;
                            int[] channelMasks = devicePort.channelMasks();
                            int length2 = channelMasks.length;
                            for (i = SENDMSG_REPLACE; i < length2; i += SENDMSG_NOOP) {
                                int channelCount = AudioFormat.channelCountFromOutChannelMask(channelMasks[i]);
                                if (channelCount > maxChannels) {
                                    maxChannels = channelCount;
                                }
                            }
                            intent.putExtra("android.media.extra.MAX_CHANNEL_COUNT", maxChannels);
                        }
                    }
                }
            }
        }
    }

    private void killBackgroundUserProcessesWithRecordAudioPermission(UserInfo oldUser) {
        PackageManager pm = this.mContext.getPackageManager();
        ComponentName homeActivityName = null;
        if (!oldUser.isManagedProfile()) {
            homeActivityName = ((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).getHomeActivityForUser(oldUser.id);
        }
        String[] permissions = new String[SENDMSG_NOOP];
        permissions[SENDMSG_REPLACE] = "android.permission.RECORD_AUDIO";
        try {
            List<PackageInfo> packages = AppGlobals.getPackageManager().getPackagesHoldingPermissions(permissions, SENDMSG_REPLACE, oldUser.id).getList();
            for (int j = packages.size() + SCO_MODE_UNDEFINED; j >= 0; j += SCO_MODE_UNDEFINED) {
                PackageInfo pkg = (PackageInfo) packages.get(j);
                if (!(UserHandle.getAppId(pkg.applicationInfo.uid) < AbsPowerManagerService.MIN_COVER_SCREEN_OFF_TIMEOUT || pm.checkPermission("android.permission.INTERACT_ACROSS_USERS", pkg.packageName) == 0 || (r1 != null && pkg.packageName.equals(r1.getPackageName()) && pkg.applicationInfo.isSystemApp()))) {
                    try {
                        int uid = pkg.applicationInfo.uid;
                        ActivityManagerNative.getDefault().killUid(UserHandle.getAppId(uid), UserHandle.getUserId(uid), "killBackgroundUserProcessesWithAudioRecordPermission");
                    } catch (RemoteException e) {
                        Log.w(TAG, "Error calling killUid", e);
                    }
                }
            }
        } catch (RemoteException e2) {
            throw new AndroidRuntimeException(e2);
        }
    }

    public int requestAudioFocus(AudioAttributes aa, int durationHint, IBinder cb, IAudioFocusDispatcher fd, String clientId, String callingPackageName, int flags, IAudioPolicyCallback pcb) {
        if ((flags & SCO_STATE_DEACTIVATE_EXT_REQ) == SCO_STATE_DEACTIVATE_EXT_REQ) {
            if (!"AudioFocus_For_Phone_Ring_And_Calls".equals(clientId)) {
                synchronized (this.mAudioPolicies) {
                    if (this.mAudioPolicies.containsKey(pcb.asBinder())) {
                    } else {
                        Log.e(TAG, "Invalid unregistered AudioPolicy to (un)lock audio focus");
                        return SENDMSG_REPLACE;
                    }
                }
            } else if (this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE") != 0) {
                Log.e(TAG, "Invalid permission to (un)lock audio focus", new Exception());
                return SENDMSG_REPLACE;
            }
        }
        return this.mMediaFocusControl.requestAudioFocus(aa, durationHint, cb, fd, clientId, callingPackageName, flags);
    }

    public int abandonAudioFocus(IAudioFocusDispatcher fd, String clientId, AudioAttributes aa) {
        return this.mMediaFocusControl.abandonAudioFocus(fd, clientId, aa);
    }

    public void unregisterAudioFocusClient(String clientId) {
        this.mMediaFocusControl.unregisterAudioFocusClient(clientId);
    }

    public int getCurrentAudioFocus() {
        return this.mMediaFocusControl.getCurrentAudioFocus();
    }

    private boolean readCameraSoundForced() {
        if (SystemProperties.getBoolean("audio.camerasound.force", DEBUG_VOL)) {
            return true;
        }
        return this.mContext.getResources().getBoolean(17956990);
    }

    private void handleConfigurationChanged(Context context) {
        try {
            Configuration config = context.getResources().getConfiguration();
            if (this.mMonitorOrientation) {
                int newOrientation = config.orientation;
                if (newOrientation != this.mDeviceOrientation) {
                    this.mDeviceOrientation = newOrientation;
                    setOrientationForAudioSystem();
                }
            }
            sendMsg(this.mAudioHandler, MSG_CONFIGURE_SAFE_MEDIA_VOLUME, SENDMSG_REPLACE, SENDMSG_REPLACE, SENDMSG_REPLACE, TAG, SENDMSG_REPLACE);
            boolean cameraSoundForced = readCameraSoundForced();
            synchronized (this.mSettingsLock) {
                boolean cameraSoundForcedChanged = DEBUG_VOL;
                synchronized (this.mCameraSoundForced) {
                    if (cameraSoundForced != this.mCameraSoundForced.booleanValue()) {
                        this.mCameraSoundForced = Boolean.valueOf(cameraSoundForced);
                        cameraSoundForcedChanged = true;
                    }
                }
                if (cameraSoundForcedChanged) {
                    if (!isPlatformTelevision()) {
                        VolumeStreamState s = this.mStreamStates[MSG_LOAD_SOUND_EFFECTS];
                        if (cameraSoundForced) {
                            s.setAllIndexesToMax();
                            this.mRingerModeAffectedStreams &= -129;
                        } else {
                            s.setAllIndexes(this.mStreamStates[SENDMSG_NOOP], TAG);
                            this.mRingerModeAffectedStreams |= DumpState.DUMP_PACKAGES;
                        }
                        setRingerModeInt(getRingerModeInternal(), DEBUG_VOL);
                    }
                    sendMsg(this.mAudioHandler, MSG_SET_FORCE_USE, SENDMSG_QUEUE, SCO_STATE_DEACTIVATE_EXT_REQ, cameraSoundForced ? 11 : SENDMSG_REPLACE, null, SENDMSG_REPLACE);
                    sendMsg(this.mAudioHandler, MSG_SET_ALL_VOLUMES, SENDMSG_QUEUE, SENDMSG_REPLACE, SENDMSG_REPLACE, this.mStreamStates[MSG_LOAD_SOUND_EFFECTS], SENDMSG_REPLACE);
                }
            }
            this.mVolumeController.setLayoutDirection(config.getLayoutDirection());
        } catch (Exception e) {
            Log.e(TAG, "Error handling configuration change: ", e);
        }
    }

    private void setOrientationForAudioSystem() {
        switch (this.mDeviceOrientation) {
            case SENDMSG_REPLACE /*0*/:
                AudioSystem.setParameters("orientation=undefined");
            case SENDMSG_NOOP /*1*/:
                AudioSystem.setParameters("orientation=portrait");
                if (this.mSystemReady && SPK_RCV_STEREO_SUPPORT) {
                    RotationHelper.updateOrientation();
                }
            case SENDMSG_QUEUE /*2*/:
                AudioSystem.setParameters("orientation=landscape");
                if (this.mSystemReady && SPK_RCV_STEREO_SUPPORT) {
                    RotationHelper.updateOrientation();
                }
            case SCO_STATE_ACTIVE_INTERNAL /*3*/:
                AudioSystem.setParameters("orientation=square");
            default:
                Log.e(TAG, "Unknown orientation");
        }
    }

    public void setBluetoothA2dpOnInt(boolean on) {
        synchronized (this.mBluetoothA2dpEnabledLock) {
            this.mBluetoothA2dpEnabled = on;
            this.mAudioHandler.removeMessages(MSG_SET_FORCE_BT_A2DP_USE);
            setForceUseInt_SyncDevices(SENDMSG_NOOP, this.mBluetoothA2dpEnabled ? SENDMSG_REPLACE : MSG_SET_ALL_VOLUMES);
        }
    }

    private void setForceUseInt_SyncDevices(int usage, int config) {
        switch (usage) {
            case SENDMSG_NOOP /*1*/:
                if (config != MSG_SET_ALL_VOLUMES) {
                    this.mBecomingNoisyIntentDevices |= 896;
                    break;
                } else {
                    this.mBecomingNoisyIntentDevices &= -897;
                    break;
                }
            case SCO_STATE_ACTIVE_INTERNAL /*3*/:
                if (config != MSG_SET_FORCE_USE) {
                    this.mBecomingNoisyIntentDevices &= -2049;
                    break;
                } else {
                    this.mBecomingNoisyIntentDevices |= DumpState.DUMP_VERIFIERS;
                    break;
                }
        }
        AudioSystem.setForceUse(usage, config);
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

    private void setSafeMediaVolumeEnabled(boolean on, String caller) {
        synchronized (this.mSafeMediaVolumeState) {
            if (!(this.mSafeMediaVolumeState.intValue() == 0 || this.mSafeMediaVolumeState.intValue() == SENDMSG_NOOP)) {
                if (on && this.mSafeMediaVolumeState.intValue() == SENDMSG_QUEUE) {
                    this.mSafeMediaVolumeState = Integer.valueOf(SCO_STATE_ACTIVE_INTERNAL);
                    enforceSafeMediaVolume(caller);
                } else if (!on) {
                    if (this.mSafeMediaVolumeState.intValue() == SCO_STATE_ACTIVE_INTERNAL) {
                        this.mSafeMediaVolumeState = Integer.valueOf(SENDMSG_QUEUE);
                        this.mMusicActiveMs = SENDMSG_NOOP;
                        saveMusicActiveMs();
                        sendMsg(this.mAudioHandler, MSG_CHECK_MUSIC_ACTIVE, SENDMSG_REPLACE, SENDMSG_REPLACE, SENDMSG_REPLACE, caller, MUSIC_ACTIVE_POLL_PERIOD_MS);
                    }
                }
            }
        }
    }

    private boolean checkSafeMediaVolume(int streamType, int index, int device) {
        synchronized (this.mSafeMediaVolumeState) {
            if (this.mFactoryMode || this.mSafeMediaVolumeState.intValue() != SCO_STATE_ACTIVE_INTERNAL || this.mStreamVolumeAlias[streamType] != SCO_STATE_ACTIVE_INTERNAL || (device & MSG_REPORT_NEW_ROUTES) == 0 || index <= this.mSafeMediaVolumeIndex) {
                return true;
            }
            return DEBUG_VOL;
        }
    }

    public void disableSafeMediaVolume(String callingPackage) {
        enforceVolumeController("disable the safe media volume");
        synchronized (this.mSafeMediaVolumeState) {
            setSafeMediaVolumeEnabled(DEBUG_VOL, callingPackage);
            if (this.mPendingVolumeCommand != null) {
                onSetStreamVolume(this.mPendingVolumeCommand.mStreamType, this.mPendingVolumeCommand.mIndex, this.mPendingVolumeCommand.mFlags, this.mPendingVolumeCommand.mDevice, callingPackage);
                this.mPendingVolumeCommand = null;
            }
        }
    }

    public int setHdmiSystemAudioSupported(boolean on) {
        int i = SENDMSG_REPLACE;
        int device = SENDMSG_REPLACE;
        if (this.mHdmiManager != null) {
            synchronized (this.mHdmiManager) {
                if (this.mHdmiTvClient == null) {
                    Log.w(TAG, "Only Hdmi-Cec enabled TV device supports system audio mode.");
                    return SENDMSG_REPLACE;
                }
                synchronized (this.mHdmiTvClient) {
                    if (this.mHdmiSystemAudioSupported != on) {
                        this.mHdmiSystemAudioSupported = on;
                        if (on) {
                            i = MSG_REPORT_NEW_ROUTES;
                        }
                        AudioSystem.setForceUse(SCO_STATE_DEACTIVATE_REQ, i);
                    }
                    device = getDevicesForStream(SCO_STATE_ACTIVE_INTERNAL);
                }
            }
        }
        return device;
    }

    public boolean isHdmiSystemAudioSupported() {
        return this.mHdmiSystemAudioSupported;
    }

    public boolean isCameraSoundForced() {
        boolean booleanValue;
        synchronized (this.mCameraSoundForced) {
            booleanValue = this.mCameraSoundForced.booleanValue();
        }
        return booleanValue;
    }

    private void dumpRingerMode(PrintWriter pw) {
        pw.println("\nRinger mode: ");
        pw.println("- mode (internal) = " + RINGER_MODE_NAMES[this.mRingerMode]);
        pw.println("- mode (external) = " + RINGER_MODE_NAMES[this.mRingerModeExternal]);
        dumpRingerModeStreams(pw, "affected", this.mRingerModeAffectedStreams);
        dumpRingerModeStreams(pw, "muted", this.mRingerModeMutedStreams);
        pw.print("- delegate = ");
        pw.println(this.mRingerModeDelegate);
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DUMP", TAG);
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
        pw.print("  mControllerService=");
        pw.println(this.mControllerService);
        pw.print("  mVolumePolicy=");
        pw.println(this.mVolumePolicy);
        dumpAudioPolicies(pw);
    }

    private static String safeMediaVolumeStateToString(Integer state) {
        switch (state.intValue()) {
            case SENDMSG_REPLACE /*0*/:
                return "SAFE_MEDIA_VOLUME_NOT_CONFIGURED";
            case SENDMSG_NOOP /*1*/:
                return "SAFE_MEDIA_VOLUME_DISABLED";
            case SENDMSG_QUEUE /*2*/:
                return "SAFE_MEDIA_VOLUME_INACTIVE";
            case SCO_STATE_ACTIVE_INTERNAL /*3*/:
                return "SAFE_MEDIA_VOLUME_ACTIVE";
            default:
                return null;
        }
    }

    private static void readAndSetLowRamDevice() {
        int status = AudioSystem.setLowRamDevice(ActivityManager.isLowRamDeviceStatic());
        if (status != 0) {
            Log.w(TAG, "AudioFlinger informed of device's low RAM attribute; status " + status);
        }
    }

    private void enforceVolumeController(String action) {
        if (this.mControllerService.mUid == 0 || Binder.getCallingUid() != this.mControllerService.mUid) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.STATUS_BAR_SERVICE", "Only SystemUI can " + action);
        }
    }

    public void setVolumeController(IVolumeController controller) {
        enforceVolumeController("set the volume controller");
        if (!this.mVolumeController.isSameBinder(controller)) {
            this.mVolumeController.postDismiss();
            if (controller != null) {
                try {
                    controller.asBinder().linkToDeath(new AnonymousClass4(controller), SENDMSG_REPLACE);
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
            if (DEBUG_VOL) {
                Log.d(TAG, "Volume controller visible: " + visible);
            }
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

    public String registerAudioPolicy(AudioPolicyConfig policyConfig, IAudioPolicyCallback pcb, boolean hasFocusListener) {
        boolean hasPermissionForPolicy = DEBUG_VOL;
        AudioSystem.setDynamicPolicyCallback(this.mDynPolicyCallback);
        if (DEBUG_AP) {
            Log.d(TAG, "registerAudioPolicy for " + pcb.asBinder() + " with config:" + policyConfig);
        }
        if (this.mContext.checkCallingPermission("android.permission.MODIFY_AUDIO_ROUTING") == 0) {
            hasPermissionForPolicy = true;
        }
        if (hasPermissionForPolicy) {
            synchronized (this.mAudioPolicies) {
                try {
                    if (this.mAudioPolicies.containsKey(pcb.asBinder())) {
                        Slog.e(TAG, "Cannot re-register policy");
                        return null;
                    }
                    AudioPolicyProxy app = new AudioPolicyProxy(policyConfig, pcb, hasFocusListener);
                    pcb.asBinder().linkToDeath(app, SENDMSG_REPLACE);
                    String regId = app.getRegistrationId();
                    this.mAudioPolicies.put(pcb.asBinder(), app);
                    return regId;
                } catch (RemoteException e) {
                    Slog.w(TAG, "Audio policy registration failed, could not link to " + pcb + " binder death", e);
                    return null;
                }
            }
        }
        Slog.w(TAG, "Can't register audio policy for pid " + Binder.getCallingPid() + " / uid " + Binder.getCallingUid() + ", need MODIFY_AUDIO_ROUTING");
        return null;
    }

    public void unregisterAudioPolicyAsync(IAudioPolicyCallback pcb) {
        if (DEBUG_AP) {
            Log.d(TAG, "unregisterAudioPolicyAsync for " + pcb.asBinder());
        }
        synchronized (this.mAudioPolicies) {
            AudioPolicyProxy app = (AudioPolicyProxy) this.mAudioPolicies.remove(pcb.asBinder());
            if (app == null) {
                Slog.w(TAG, "Trying to unregister unknown audio policy for pid " + Binder.getCallingPid() + " / uid " + Binder.getCallingUid());
                return;
            }
            pcb.asBinder().unlinkToDeath(app, SENDMSG_REPLACE);
            app.release();
        }
    }

    public int setFocusPropertiesForPolicy(int duckingBehavior, IAudioPolicyCallback pcb) {
        boolean hasPermissionForPolicy;
        boolean z = true;
        if (DEBUG_AP) {
            Log.d(TAG, "setFocusPropertiesForPolicy() duck behavior=" + duckingBehavior + " policy " + pcb.asBinder());
        }
        if (this.mContext.checkCallingPermission("android.permission.MODIFY_AUDIO_ROUTING") == 0) {
            hasPermissionForPolicy = true;
        } else {
            hasPermissionForPolicy = DEBUG_VOL;
        }
        if (hasPermissionForPolicy) {
            synchronized (this.mAudioPolicies) {
                if (this.mAudioPolicies.containsKey(pcb.asBinder())) {
                    AudioPolicyProxy app = (AudioPolicyProxy) this.mAudioPolicies.get(pcb.asBinder());
                    if (duckingBehavior == SENDMSG_NOOP) {
                        for (AudioPolicyProxy policy : this.mAudioPolicies.values()) {
                            if (policy.mFocusDuckBehavior == SENDMSG_NOOP) {
                                Slog.e(TAG, "Cannot change audio policy ducking behavior, already handled");
                                return SCO_MODE_UNDEFINED;
                            }
                        }
                    }
                    app.mFocusDuckBehavior = duckingBehavior;
                    MediaFocusControl mediaFocusControl = this.mMediaFocusControl;
                    if (duckingBehavior != SENDMSG_NOOP) {
                        z = DEBUG_VOL;
                    }
                    mediaFocusControl.setDuckingInExtPolicyAvailable(z);
                    return SENDMSG_REPLACE;
                }
                Slog.e(TAG, "Cannot change audio policy focus properties, unregistered policy");
                return SCO_MODE_UNDEFINED;
            }
        }
        Slog.w(TAG, "Cannot change audio policy ducking handling for pid " + Binder.getCallingPid() + " / uid " + Binder.getCallingUid() + ", need MODIFY_AUDIO_ROUTING");
        return SCO_MODE_UNDEFINED;
    }

    private void dumpAudioPolicies(PrintWriter pw) {
        pw.println("\nAudio policies:");
        synchronized (this.mAudioPolicies) {
            for (AudioPolicyProxy policy : this.mAudioPolicies.values()) {
                pw.println(policy.toLogFriendlyString());
            }
        }
    }

    private void onDynPolicyMixStateUpdate(String regId, int state) {
        if (DEBUG_AP) {
            Log.d(TAG, "onDynamicPolicyMixStateUpdate(" + regId + ", " + state + ")");
        }
        synchronized (this.mAudioPolicies) {
            for (AudioPolicyProxy policy : this.mAudioPolicies.values()) {
                for (AudioMix mix : policy.getMixes()) {
                    if (mix.getRegistration().equals(regId)) {
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

    public void registerRecordingCallback(IRecordingConfigDispatcher rcdb) {
        this.mRecordMonitor.registerRecordingCallback(rcdb);
    }

    public void unregisterRecordingCallback(IRecordingConfigDispatcher rcdb) {
        this.mRecordMonitor.unregisterRecordingCallback(rcdb);
    }

    public List<AudioRecordingConfiguration> getActiveRecordingConfigurations() {
        return this.mRecordMonitor.getActiveRecordingConfigurations();
    }

    protected int hwGetDeviceForStream(int activeStreamType) {
        return getDeviceForStream(this.mStreamVolumeAlias[activeStreamType]);
    }

    protected int getStreamIndex(int stream_type, int device) {
        if (stream_type > SCO_MODE_UNDEFINED && stream_type < AudioSystem.getNumStreamTypes()) {
            return this.mStreamStates[stream_type].getIndex(device);
        }
        Log.e(TAG, "invalid stream type!!!");
        return SCO_MODE_UNDEFINED;
    }

    protected int getStreamMaxIndex(int stream_type) {
        if (stream_type > SCO_MODE_UNDEFINED && stream_type < AudioSystem.getNumStreamTypes()) {
            return this.mStreamStates[stream_type].getMaxIndex();
        }
        Log.e(TAG, "invalid stream type!!!");
        return SCO_MODE_UNDEFINED;
    }

    public int getSampleId(SoundPool soundpool, int effect, String defFilePath, int index) {
        return this.mSoundPool.load(defFilePath, SENDMSG_REPLACE);
    }

    protected void appendExtraInfo(Intent intent) {
    }

    protected void checkRecordActive() {
    }

    protected String getPackageNameByPid(int pid) {
        return null;
    }

    protected void sendCommForceBroadcast() {
    }

    protected void checkMuteRcvDelay(int curMode, int mode) {
    }

    protected boolean checkEnbaleVolumeAdjust() {
        return true;
    }

    protected void processMediaServerRestart() {
    }

    private boolean isHisiPlatform() {
        String platform = SystemProperties.get("ro.board.platform", "unknown");
        if (platform == null || !platform.startsWith("hi")) {
            return DEBUG_VOL;
        }
        return true;
    }
}
