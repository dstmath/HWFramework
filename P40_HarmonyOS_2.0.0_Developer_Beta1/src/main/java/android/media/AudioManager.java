package android.media;

import android.annotation.SuppressLint;
import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothCodecConfig;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.IAudioFocusDispatcher;
import android.media.IAudioServerStateDispatcher;
import android.media.IAudioService;
import android.media.IPlaybackConfigDispatcher;
import android.media.IRecordingConfigDispatcher;
import android.media.audiopolicy.AudioPolicy;
import android.media.audiopolicy.AudioProductStrategy;
import android.media.audiopolicy.AudioVolumeGroup;
import android.media.audiopolicy.AudioVolumeGroupChangeHandler;
import android.media.projection.MediaProjection;
import android.media.session.MediaSessionLegacyHelper;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Jlog;
import android.util.Log;
import android.view.KeyEvent;
import com.android.internal.R;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.Preconditions;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public class AudioManager {
    public static final String ACTION_AUDIO_BECOMING_NOISY = "android.media.AUDIO_BECOMING_NOISY";
    public static final String ACTION_HDMI_AUDIO_PLUG = "android.media.action.HDMI_AUDIO_PLUG";
    public static final String ACTION_HEADSET_PLUG = "android.intent.action.HEADSET_PLUG";
    public static final String ACTION_MICROPHONE_MUTE_CHANGED = "android.media.action.MICROPHONE_MUTE_CHANGED";
    @Deprecated
    public static final String ACTION_SCO_AUDIO_STATE_CHANGED = "android.media.SCO_AUDIO_STATE_CHANGED";
    public static final String ACTION_SCO_AUDIO_STATE_UPDATED = "android.media.ACTION_SCO_AUDIO_STATE_UPDATED";
    public static final String ACTION_SPEAKERPHONE_STATE_CHANGED = "android.media.action.SPEAKERPHONE_STATE_CHANGED";
    public static final int ADJUST_LOWER = -1;
    public static final int ADJUST_MUTE = -100;
    public static final int ADJUST_RAISE = 1;
    public static final int ADJUST_SAME = 0;
    public static final int ADJUST_TOGGLE_MUTE = 101;
    public static final int ADJUST_UNMUTE = 100;
    public static final int AUDIOFOCUS_FLAGS_APPS = 3;
    public static final int AUDIOFOCUS_FLAGS_SYSTEM = 7;
    @SystemApi
    public static final int AUDIOFOCUS_FLAG_DELAY_OK = 1;
    @SystemApi
    public static final int AUDIOFOCUS_FLAG_LOCK = 4;
    @SystemApi
    public static final int AUDIOFOCUS_FLAG_PAUSES_ON_DUCKABLE_LOSS = 2;
    public static final int AUDIOFOCUS_GAIN = 1;
    public static final int AUDIOFOCUS_GAIN_TRANSIENT = 2;
    public static final int AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE = 4;
    public static final int AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK = 3;
    public static final int AUDIOFOCUS_LOSS = -1;
    public static final int AUDIOFOCUS_LOSS_TRANSIENT = -2;
    public static final int AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK = -3;
    public static final int AUDIOFOCUS_NONE = 0;
    public static final int AUDIOFOCUS_REQUEST_DELAYED = 2;
    public static final int AUDIOFOCUS_REQUEST_FAILED = 0;
    public static final int AUDIOFOCUS_REQUEST_GRANTED = 1;
    public static final int AUDIOFOCUS_REQUEST_WAITING_FOR_EXT_POLICY = 100;
    static final int AUDIOPORT_GENERATION_INIT = 0;
    public static final int AUDIO_DEVICE_IN_USB_DEVICE_EXTENDED = -2145386496;
    public static final int AUDIO_DEVICE_OUT_USB_DEVICE_EXTENDED = 536870912;
    public static final int AUDIO_SESSION_ID_GENERATE = 0;
    private static final boolean DEBUG = true;
    public static final int DEVICE_IN_ANLG_DOCK_HEADSET = -2147483136;
    public static final int DEVICE_IN_BACK_MIC = -2147483520;
    public static final int DEVICE_IN_BLUETOOTH_SCO_HEADSET = -2147483640;
    public static final int DEVICE_IN_BUILTIN_MIC = -2147483644;
    public static final int DEVICE_IN_DGTL_DOCK_HEADSET = -2147482624;
    public static final int DEVICE_IN_FM_TUNER = -2147475456;
    public static final int DEVICE_IN_HDMI = -2147483616;
    public static final int DEVICE_IN_HDMI_ARC = -2013265920;
    public static final int DEVICE_IN_LINE = -2147450880;
    public static final int DEVICE_IN_LOOPBACK = -2147221504;
    public static final int DEVICE_IN_SPDIF = -2147418112;
    public static final int DEVICE_IN_TELEPHONY_RX = -2147483584;
    public static final int DEVICE_IN_TV_TUNER = -2147467264;
    public static final int DEVICE_IN_USB_ACCESSORY = -2147481600;
    public static final int DEVICE_IN_USB_DEVICE = -2147479552;
    public static final int DEVICE_IN_WIRED_HEADSET = -2147483632;
    public static final int DEVICE_NONE = 0;
    @UnsupportedAppUsage
    public static final int DEVICE_OUT_ANLG_DOCK_HEADSET = 2048;
    public static final int DEVICE_OUT_AUX_DIGITAL = 1024;
    @UnsupportedAppUsage
    public static final int DEVICE_OUT_BLUETOOTH_A2DP = 128;
    @UnsupportedAppUsage
    public static final int DEVICE_OUT_BLUETOOTH_A2DP_HEADPHONES = 256;
    @UnsupportedAppUsage
    public static final int DEVICE_OUT_BLUETOOTH_A2DP_SPEAKER = 512;
    public static final int DEVICE_OUT_BLUETOOTH_SCO = 16;
    public static final int DEVICE_OUT_BLUETOOTH_SCO_CARKIT = 64;
    @UnsupportedAppUsage
    public static final int DEVICE_OUT_BLUETOOTH_SCO_HEADSET = 32;
    public static final int DEVICE_OUT_DEFAULT = 1073741824;
    @UnsupportedAppUsage
    public static final int DEVICE_OUT_DGTL_DOCK_HEADSET = 4096;
    @UnsupportedAppUsage
    public static final int DEVICE_OUT_EARPIECE = 1;
    public static final int DEVICE_OUT_FM = 1048576;
    @UnsupportedAppUsage
    public static final int DEVICE_OUT_HDMI = 1024;
    public static final int DEVICE_OUT_HDMI_ARC = 262144;
    public static final int DEVICE_OUT_LINE = 131072;
    public static final int DEVICE_OUT_REMOTE_SUBMIX = 32768;
    public static final int DEVICE_OUT_SPDIF = 524288;
    @UnsupportedAppUsage
    public static final int DEVICE_OUT_SPEAKER = 2;
    public static final int DEVICE_OUT_TELEPHONY_TX = 65536;
    public static final int DEVICE_OUT_USB_ACCESSORY = 8192;
    public static final int DEVICE_OUT_USB_DEVICE = 16384;
    public static final int DEVICE_OUT_USB_HEADSET = 67108864;
    @UnsupportedAppUsage
    public static final int DEVICE_OUT_WIRED_HEADPHONE = 8;
    @UnsupportedAppUsage
    public static final int DEVICE_OUT_WIRED_HEADSET = 4;
    public static final int ERROR = -1;
    public static final int ERROR_BAD_VALUE = -2;
    public static final int ERROR_DEAD_OBJECT = -6;
    public static final int ERROR_INVALID_OPERATION = -3;
    public static final int ERROR_NO_INIT = -5;
    public static final int ERROR_PERMISSION_DENIED = -4;
    public static final String EXTRA_AUDIO_PLUG_STATE = "android.media.extra.AUDIO_PLUG_STATE";
    public static final String EXTRA_ENCODINGS = "android.media.extra.ENCODINGS";
    public static final String EXTRA_MASTER_VOLUME_MUTED = "android.media.EXTRA_MASTER_VOLUME_MUTED";
    public static final String EXTRA_MAX_CHANNEL_COUNT = "android.media.extra.MAX_CHANNEL_COUNT";
    public static final String EXTRA_PREV_VOLUME_STREAM_DEVICES = "android.media.EXTRA_PREV_VOLUME_STREAM_DEVICES";
    public static final String EXTRA_PREV_VOLUME_STREAM_VALUE = "android.media.EXTRA_PREV_VOLUME_STREAM_VALUE";
    public static final String EXTRA_RINGER_MODE = "android.media.EXTRA_RINGER_MODE";
    public static final String EXTRA_SCO_AUDIO_PREVIOUS_STATE = "android.media.extra.SCO_AUDIO_PREVIOUS_STATE";
    public static final String EXTRA_SCO_AUDIO_STATE = "android.media.extra.SCO_AUDIO_STATE";
    public static final String EXTRA_STREAM_VOLUME_MUTED = "android.media.EXTRA_STREAM_VOLUME_MUTED";
    public static final String EXTRA_VIBRATE_SETTING = "android.media.EXTRA_VIBRATE_SETTING";
    public static final String EXTRA_VIBRATE_TYPE = "android.media.EXTRA_VIBRATE_TYPE";
    public static final String EXTRA_VOLUME_STREAM_DEVICES = "android.media.EXTRA_VOLUME_STREAM_DEVICES";
    @UnsupportedAppUsage
    public static final String EXTRA_VOLUME_STREAM_TYPE = "android.media.EXTRA_VOLUME_STREAM_TYPE";
    public static final String EXTRA_VOLUME_STREAM_TYPE_ALIAS = "android.media.EXTRA_VOLUME_STREAM_TYPE_ALIAS";
    @UnsupportedAppUsage
    public static final String EXTRA_VOLUME_STREAM_VALUE = "android.media.EXTRA_VOLUME_STREAM_VALUE";
    private static final int EXT_FOCUS_POLICY_TIMEOUT_MS = 200;
    public static final int FLAG_ACTIVE_MEDIA_ONLY = 512;
    public static final int FLAG_ALLOW_RINGER_MODES = 2;
    public static final int FLAG_BLUETOOTH_ABS_VOLUME = 64;
    public static final int FLAG_FIXED_VOLUME = 32;
    public static final int FLAG_FROM_KEY = 4096;
    public static final int FLAG_HDMI_SYSTEM_AUDIO_VOLUME = 256;
    private static final TreeMap<Integer, String> FLAG_NAMES = new TreeMap<>();
    public static final int FLAG_PLAY_SOUND = 4;
    public static final int FLAG_REMOVE_SOUND_AND_VIBRATE = 8;
    public static final int FLAG_SHOW_SILENT_HINT = 128;
    public static final int FLAG_SHOW_UI = 1;
    public static final int FLAG_SHOW_UI_WARNINGS = 1024;
    public static final int FLAG_SHOW_VIBRATE_HINT = 2048;
    public static final int FLAG_VIBRATE = 16;
    private static final String FOCUS_CLIENT_ID_STRING = "android_audio_focus_client_id";
    public static final int FX_FOCUS_NAVIGATION_DOWN = 2;
    public static final int FX_FOCUS_NAVIGATION_LEFT = 3;
    public static final int FX_FOCUS_NAVIGATION_RIGHT = 4;
    public static final int FX_FOCUS_NAVIGATION_UP = 1;
    public static final int FX_KEYPRESS_DELETE = 7;
    public static final int FX_KEYPRESS_INVALID = 9;
    public static final int FX_KEYPRESS_RETURN = 8;
    public static final int FX_KEYPRESS_SPACEBAR = 6;
    public static final int FX_KEYPRESS_STANDARD = 5;
    public static final int FX_KEY_CLICK = 0;
    public static final int GET_DEVICES_ALL = 3;
    public static final int GET_DEVICES_INPUTS = 1;
    public static final int GET_DEVICES_OUTPUTS = 2;
    private static final int HWKARAOKE_BINDER_CODE = 1105;
    private static final int HWKARAOKE_BINDER_DEAD_CODE = 1106;
    public static final String INTERNAL_RINGER_MODE_CHANGED_ACTION = "android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION";
    public static final String MASTER_MUTE_CHANGED_ACTION = "android.media.MASTER_MUTE_CHANGED_ACTION";
    public static final int MODE_CURRENT = -1;
    public static final int MODE_INVALID = -2;
    public static final int MODE_IN_CALL = 2;
    public static final int MODE_IN_COMMUNICATION = 3;
    public static final int MODE_NORMAL = 0;
    public static final int MODE_RINGTONE = 1;
    private static final int MSG_DEVICES_CALLBACK_REGISTERED = 0;
    private static final int MSG_DEVICES_DEVICES_ADDED = 1;
    private static final int MSG_DEVICES_DEVICES_REMOVED = 2;
    private static final int MSSG_FOCUS_CHANGE = 0;
    private static final int MSSG_PLAYBACK_CONFIG_CHANGE = 2;
    private static final int MSSG_RECORDING_CONFIG_CHANGE = 1;
    @UnsupportedAppUsage
    public static final int NUM_SOUND_EFFECTS = 10;
    @Deprecated
    public static final int NUM_STREAMS = 5;
    public static final String PROPERTY_OUTPUT_FRAMES_PER_BUFFER = "android.media.property.OUTPUT_FRAMES_PER_BUFFER";
    public static final String PROPERTY_OUTPUT_SAMPLE_RATE = "android.media.property.OUTPUT_SAMPLE_RATE";
    public static final String PROPERTY_SUPPORT_AUDIO_SOURCE_UNPROCESSED = "android.media.property.SUPPORT_AUDIO_SOURCE_UNPROCESSED";
    public static final String PROPERTY_SUPPORT_HWKARAOKE_EFFECT = "android.media.property.SUPPORT_HWKARAOKE_EFFECT";
    public static final String PROPERTY_SUPPORT_MIC_NEAR_ULTRASOUND = "android.media.property.SUPPORT_MIC_NEAR_ULTRASOUND";
    public static final String PROPERTY_SUPPORT_SPEAKER_NEAR_ULTRASOUND = "android.media.property.SUPPORT_SPEAKER_NEAR_ULTRASOUND";
    public static final int RECORDER_STATE_STARTED = 0;
    public static final int RECORDER_STATE_STOPPED = 1;
    public static final int RECORD_CONFIG_EVENT_NONE = -1;
    public static final int RECORD_CONFIG_EVENT_RELEASE = 3;
    public static final int RECORD_CONFIG_EVENT_START = 0;
    public static final int RECORD_CONFIG_EVENT_STOP = 1;
    public static final int RECORD_CONFIG_EVENT_UPDATE = 2;
    public static final int RECORD_RIID_INVALID = -1;
    public static final String RINGER_MODE_CHANGED_ACTION = "android.media.RINGER_MODE_CHANGED";
    public static final int RINGER_MODE_MAX = 2;
    public static final int RINGER_MODE_NORMAL = 2;
    public static final int RINGER_MODE_SILENT = 0;
    public static final int RINGER_MODE_VIBRATE = 1;
    @Deprecated
    public static final int ROUTE_ALL = -1;
    @Deprecated
    public static final int ROUTE_BLUETOOTH = 4;
    @Deprecated
    public static final int ROUTE_BLUETOOTH_A2DP = 16;
    @Deprecated
    public static final int ROUTE_BLUETOOTH_SCO = 4;
    @Deprecated
    public static final int ROUTE_EARPIECE = 1;
    @Deprecated
    public static final int ROUTE_HEADSET = 8;
    @Deprecated
    public static final int ROUTE_SPEAKER = 2;
    public static final int SCO_AUDIO_STATE_CONNECTED = 1;
    public static final int SCO_AUDIO_STATE_CONNECTING = 2;
    public static final int SCO_AUDIO_STATE_DISCONNECTED = 0;
    public static final int SCO_AUDIO_STATE_ERROR = -1;
    public static final int STREAM_ACCESSIBILITY = 10;
    public static final int STREAM_ALARM = 4;
    @UnsupportedAppUsage
    public static final int STREAM_BLUETOOTH_SCO = 6;
    public static final String STREAM_DEVICES_CHANGED_ACTION = "android.media.STREAM_DEVICES_CHANGED_ACTION";
    public static final int STREAM_DTMF = 8;
    public static final int STREAM_MUSIC = 3;
    public static final String STREAM_MUTE_CHANGED_ACTION = "android.media.STREAM_MUTE_CHANGED_ACTION";
    public static final int STREAM_NOTIFICATION = 5;
    public static final int STREAM_RING = 2;
    public static final int STREAM_SYSTEM = 1;
    @UnsupportedAppUsage
    public static final int STREAM_SYSTEM_ENFORCED = 7;
    public static final int STREAM_TTS = 9;
    public static final int STREAM_VOICE_CALL = 0;
    @SystemApi
    public static final int SUCCESS = 0;
    private static final String TAG = "AudioManager";
    public static final int USE_DEFAULT_STREAM_TYPE = Integer.MIN_VALUE;
    public static final String VIBRATE_SETTING_CHANGED_ACTION = "android.media.VIBRATE_SETTING_CHANGED";
    public static final int VIBRATE_SETTING_OFF = 0;
    public static final int VIBRATE_SETTING_ON = 1;
    public static final int VIBRATE_SETTING_ONLY_SILENT = 2;
    public static final int VIBRATE_TYPE_NOTIFICATION = 1;
    public static final int VIBRATE_TYPE_RINGER = 0;
    @UnsupportedAppUsage
    public static final String VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION";
    private static final float VOLUME_MIN_DB = -758.0f;
    private static final String WECHAT_NAME = "com.tencent.mm";
    private static final AudioVolumeGroupChangeHandler sAudioAudioVolumeGroupChangedHandler = new AudioVolumeGroupChangeHandler();
    static ArrayList<AudioPatch> sAudioPatchesCached = new ArrayList<>();
    private static final AudioPortEventHandler sAudioPortEventHandler = new AudioPortEventHandler();
    static Integer sAudioPortGeneration = new Integer(0);
    static ArrayList<AudioPort> sAudioPortsCached = new ArrayList<>();
    static ArrayList<AudioPort> sPreviousAudioPortsCached = new ArrayList<>();
    private static IAudioService sService;
    private Context mApplicationContext;
    private final IAudioFocusDispatcher mAudioFocusDispatcher;
    @UnsupportedAppUsage
    private final ConcurrentHashMap<String, FocusRequestInfo> mAudioFocusIdListenerMap;
    private AudioServerStateCallback mAudioServerStateCb;
    private final Object mAudioServerStateCbLock;
    private final IAudioServerStateDispatcher mAudioServerStateDispatcher;
    private Executor mAudioServerStateExec;
    private int mCapturePolicy;
    private final ArrayMap<AudioDeviceCallback, NativeEventHandlerDelegate> mDeviceCallbacks;
    @GuardedBy({"mFocusRequestsLock"})
    private HashMap<String, BlockingFocusResultReceiver> mFocusRequestsAwaitingResult;
    private final Object mFocusRequestsLock;
    private final IBinder mICallBack;
    private Context mOriginalContext;
    private final IPlaybackConfigDispatcher mPlayCb;
    private List<AudioPlaybackCallbackInfo> mPlaybackCallbackList;
    private final Object mPlaybackCallbackLock;
    private OnAmPortUpdateListener mPortListener;
    private ArrayList<AudioDevicePort> mPreviousPorts;
    private final IRecordingConfigDispatcher mRecCb;
    private List<AudioRecordingCallbackInfo> mRecordCallbackList;
    private final Object mRecordCallbackLock;
    private final ServiceEventHandlerDelegate mServiceEventHandlerDelegate;
    private final boolean mUseFixedVolume;
    private final boolean mUseVolumeKeySounds;
    private long mVolumeKeyUpTime;

    @Retention(RetentionPolicy.SOURCE)
    public @interface FocusRequestResult {
    }

    public interface OnAudioFocusChangeListener {
        void onAudioFocusChange(int i);
    }

    public interface OnAudioPortUpdateListener {
        void onAudioPatchListUpdate(AudioPatch[] audioPatchArr);

        void onAudioPortListUpdate(AudioPort[] audioPortArr);

        void onServiceDied();
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface PublicStreamTypes {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface VolumeAdjustment {
    }

    static {
        FLAG_NAMES.put(1, "FLAG_SHOW_UI");
        FLAG_NAMES.put(2, "FLAG_ALLOW_RINGER_MODES");
        FLAG_NAMES.put(4, "FLAG_PLAY_SOUND");
        FLAG_NAMES.put(8, "FLAG_REMOVE_SOUND_AND_VIBRATE");
        FLAG_NAMES.put(16, "FLAG_VIBRATE");
        FLAG_NAMES.put(32, "FLAG_FIXED_VOLUME");
        FLAG_NAMES.put(64, "FLAG_BLUETOOTH_ABS_VOLUME");
        FLAG_NAMES.put(128, "FLAG_SHOW_SILENT_HINT");
        FLAG_NAMES.put(256, "FLAG_HDMI_SYSTEM_AUDIO_VOLUME");
        FLAG_NAMES.put(512, "FLAG_ACTIVE_MEDIA_ONLY");
        FLAG_NAMES.put(1024, "FLAG_SHOW_UI_WARNINGS");
        FLAG_NAMES.put(2048, "FLAG_SHOW_VIBRATE_HINT");
        FLAG_NAMES.put(4096, "FLAG_FROM_KEY");
    }

    public static final String adjustToString(int adj) {
        if (adj == -100) {
            return "ADJUST_MUTE";
        }
        if (adj == -1) {
            return "ADJUST_LOWER";
        }
        if (adj == 0) {
            return "ADJUST_SAME";
        }
        if (adj == 1) {
            return "ADJUST_RAISE";
        }
        if (adj == 100) {
            return "ADJUST_UNMUTE";
        }
        if (adj == 101) {
            return "ADJUST_TOGGLE_MUTE";
        }
        return "unknown adjust mode " + adj;
    }

    public static String flagsToString(int flags) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, String> entry : FLAG_NAMES.entrySet()) {
            int flag = entry.getKey().intValue();
            if ((flags & flag) != 0) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(entry.getValue());
                flags &= ~flag;
            }
        }
        if (flags != 0) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(flags);
        }
        return sb.toString();
    }

    @UnsupportedAppUsage
    public AudioManager() {
        this.mCapturePolicy = 1;
        this.mAudioFocusIdListenerMap = new ConcurrentHashMap<>();
        this.mServiceEventHandlerDelegate = new ServiceEventHandlerDelegate(null);
        this.mAudioFocusDispatcher = new IAudioFocusDispatcher.Stub() {
            /* class android.media.AudioManager.AnonymousClass1 */

            @Override // android.media.IAudioFocusDispatcher
            public void dispatchAudioFocusChange(int focusChange, String id) {
                FocusRequestInfo fri = AudioManager.this.findFocusRequestInfo(id);
                if (fri != null && fri.mRequest.getOnAudioFocusChangeListener() != null) {
                    Handler h = fri.mHandler == null ? AudioManager.this.mServiceEventHandlerDelegate.getHandler() : fri.mHandler;
                    h.sendMessage(h.obtainMessage(0, focusChange, 0, id));
                }
            }

            @Override // android.media.IAudioFocusDispatcher
            public void dispatchFocusResultFromExtPolicy(int requestResult, String clientId) {
                synchronized (AudioManager.this.mFocusRequestsLock) {
                    BlockingFocusResultReceiver focusReceiver = (BlockingFocusResultReceiver) AudioManager.this.mFocusRequestsAwaitingResult.remove(clientId);
                    if (focusReceiver != null) {
                        focusReceiver.notifyResult(requestResult);
                    } else {
                        Log.e(AudioManager.TAG, "dispatchFocusResultFromExtPolicy found no result receiver");
                    }
                }
            }
        };
        this.mFocusRequestsLock = new Object();
        this.mPlaybackCallbackLock = new Object();
        this.mPlayCb = new IPlaybackConfigDispatcher.Stub() {
            /* class android.media.AudioManager.AnonymousClass2 */

            @Override // android.media.IPlaybackConfigDispatcher
            public void dispatchPlaybackConfigChange(List<AudioPlaybackConfiguration> configs, boolean flush) {
                if (flush) {
                    Binder.flushPendingCommands();
                }
                synchronized (AudioManager.this.mPlaybackCallbackLock) {
                    if (AudioManager.this.mPlaybackCallbackList != null) {
                        for (int i = 0; i < AudioManager.this.mPlaybackCallbackList.size(); i++) {
                            AudioPlaybackCallbackInfo arci = (AudioPlaybackCallbackInfo) AudioManager.this.mPlaybackCallbackList.get(i);
                            if (arci.mHandler != null) {
                                arci.mHandler.sendMessage(arci.mHandler.obtainMessage(2, new PlaybackConfigChangeCallbackData(arci.mCb, configs)));
                            }
                        }
                    }
                }
            }
        };
        this.mRecordCallbackLock = new Object();
        this.mRecCb = new IRecordingConfigDispatcher.Stub() {
            /* class android.media.AudioManager.AnonymousClass3 */

            @Override // android.media.IRecordingConfigDispatcher
            public void dispatchRecordingConfigChange(List<AudioRecordingConfiguration> configs) {
                synchronized (AudioManager.this.mRecordCallbackLock) {
                    if (AudioManager.this.mRecordCallbackList != null) {
                        for (int i = 0; i < AudioManager.this.mRecordCallbackList.size(); i++) {
                            AudioRecordingCallbackInfo arci = (AudioRecordingCallbackInfo) AudioManager.this.mRecordCallbackList.get(i);
                            if (arci.mHandler != null) {
                                arci.mHandler.sendMessage(arci.mHandler.obtainMessage(1, new RecordConfigChangeCallbackData(arci.mCb, configs)));
                            }
                        }
                    }
                }
            }
        };
        this.mICallBack = new Binder();
        this.mPortListener = null;
        this.mDeviceCallbacks = new ArrayMap<>();
        this.mPreviousPorts = new ArrayList<>();
        this.mAudioServerStateCbLock = new Object();
        this.mAudioServerStateDispatcher = new IAudioServerStateDispatcher.Stub() {
            /* class android.media.AudioManager.AnonymousClass4 */

            @Override // android.media.IAudioServerStateDispatcher
            public void dispatchAudioServerStateChange(boolean state) {
                Executor exec;
                AudioServerStateCallback cb;
                synchronized (AudioManager.this.mAudioServerStateCbLock) {
                    exec = AudioManager.this.mAudioServerStateExec;
                    cb = AudioManager.this.mAudioServerStateCb;
                }
                if (exec != null && cb != null) {
                    if (state) {
                        exec.execute(new Runnable() {
                            /* class android.media.$$Lambda$AudioManager$4$Q85LmhgKDCoq1YI14giFabZrM7A */

                            @Override // java.lang.Runnable
                            public final void run() {
                                AudioManager.AudioServerStateCallback.this.onAudioServerUp();
                            }
                        });
                    } else {
                        exec.execute(new Runnable() {
                            /* class android.media.$$Lambda$AudioManager$4$7k7uSoMGULBCueASQSmf9jAil7I */

                            @Override // java.lang.Runnable
                            public final void run() {
                                AudioManager.AudioServerStateCallback.this.onAudioServerDown();
                            }
                        });
                    }
                }
            }
        };
        this.mUseVolumeKeySounds = true;
        this.mUseFixedVolume = false;
    }

    @UnsupportedAppUsage
    public AudioManager(Context context) {
        this.mCapturePolicy = 1;
        this.mAudioFocusIdListenerMap = new ConcurrentHashMap<>();
        this.mServiceEventHandlerDelegate = new ServiceEventHandlerDelegate(null);
        this.mAudioFocusDispatcher = new IAudioFocusDispatcher.Stub() {
            /* class android.media.AudioManager.AnonymousClass1 */

            @Override // android.media.IAudioFocusDispatcher
            public void dispatchAudioFocusChange(int focusChange, String id) {
                FocusRequestInfo fri = AudioManager.this.findFocusRequestInfo(id);
                if (fri != null && fri.mRequest.getOnAudioFocusChangeListener() != null) {
                    Handler h = fri.mHandler == null ? AudioManager.this.mServiceEventHandlerDelegate.getHandler() : fri.mHandler;
                    h.sendMessage(h.obtainMessage(0, focusChange, 0, id));
                }
            }

            @Override // android.media.IAudioFocusDispatcher
            public void dispatchFocusResultFromExtPolicy(int requestResult, String clientId) {
                synchronized (AudioManager.this.mFocusRequestsLock) {
                    BlockingFocusResultReceiver focusReceiver = (BlockingFocusResultReceiver) AudioManager.this.mFocusRequestsAwaitingResult.remove(clientId);
                    if (focusReceiver != null) {
                        focusReceiver.notifyResult(requestResult);
                    } else {
                        Log.e(AudioManager.TAG, "dispatchFocusResultFromExtPolicy found no result receiver");
                    }
                }
            }
        };
        this.mFocusRequestsLock = new Object();
        this.mPlaybackCallbackLock = new Object();
        this.mPlayCb = new IPlaybackConfigDispatcher.Stub() {
            /* class android.media.AudioManager.AnonymousClass2 */

            @Override // android.media.IPlaybackConfigDispatcher
            public void dispatchPlaybackConfigChange(List<AudioPlaybackConfiguration> configs, boolean flush) {
                if (flush) {
                    Binder.flushPendingCommands();
                }
                synchronized (AudioManager.this.mPlaybackCallbackLock) {
                    if (AudioManager.this.mPlaybackCallbackList != null) {
                        for (int i = 0; i < AudioManager.this.mPlaybackCallbackList.size(); i++) {
                            AudioPlaybackCallbackInfo arci = (AudioPlaybackCallbackInfo) AudioManager.this.mPlaybackCallbackList.get(i);
                            if (arci.mHandler != null) {
                                arci.mHandler.sendMessage(arci.mHandler.obtainMessage(2, new PlaybackConfigChangeCallbackData(arci.mCb, configs)));
                            }
                        }
                    }
                }
            }
        };
        this.mRecordCallbackLock = new Object();
        this.mRecCb = new IRecordingConfigDispatcher.Stub() {
            /* class android.media.AudioManager.AnonymousClass3 */

            @Override // android.media.IRecordingConfigDispatcher
            public void dispatchRecordingConfigChange(List<AudioRecordingConfiguration> configs) {
                synchronized (AudioManager.this.mRecordCallbackLock) {
                    if (AudioManager.this.mRecordCallbackList != null) {
                        for (int i = 0; i < AudioManager.this.mRecordCallbackList.size(); i++) {
                            AudioRecordingCallbackInfo arci = (AudioRecordingCallbackInfo) AudioManager.this.mRecordCallbackList.get(i);
                            if (arci.mHandler != null) {
                                arci.mHandler.sendMessage(arci.mHandler.obtainMessage(1, new RecordConfigChangeCallbackData(arci.mCb, configs)));
                            }
                        }
                    }
                }
            }
        };
        this.mICallBack = new Binder();
        this.mPortListener = null;
        this.mDeviceCallbacks = new ArrayMap<>();
        this.mPreviousPorts = new ArrayList<>();
        this.mAudioServerStateCbLock = new Object();
        this.mAudioServerStateDispatcher = new IAudioServerStateDispatcher.Stub() {
            /* class android.media.AudioManager.AnonymousClass4 */

            @Override // android.media.IAudioServerStateDispatcher
            public void dispatchAudioServerStateChange(boolean state) {
                Executor exec;
                AudioServerStateCallback cb;
                synchronized (AudioManager.this.mAudioServerStateCbLock) {
                    exec = AudioManager.this.mAudioServerStateExec;
                    cb = AudioManager.this.mAudioServerStateCb;
                }
                if (exec != null && cb != null) {
                    if (state) {
                        exec.execute(new Runnable() {
                            /* class android.media.$$Lambda$AudioManager$4$Q85LmhgKDCoq1YI14giFabZrM7A */

                            @Override // java.lang.Runnable
                            public final void run() {
                                AudioManager.AudioServerStateCallback.this.onAudioServerUp();
                            }
                        });
                    } else {
                        exec.execute(new Runnable() {
                            /* class android.media.$$Lambda$AudioManager$4$7k7uSoMGULBCueASQSmf9jAil7I */

                            @Override // java.lang.Runnable
                            public final void run() {
                                AudioManager.AudioServerStateCallback.this.onAudioServerDown();
                            }
                        });
                    }
                }
            }
        };
        setContext(context);
        this.mUseVolumeKeySounds = getContext().getResources().getBoolean(R.bool.config_useVolumeKeySounds);
        this.mUseFixedVolume = getContext().getResources().getBoolean(R.bool.config_useFixedVolume);
    }

    private Context getContext() {
        if (this.mApplicationContext == null) {
            setContext(this.mOriginalContext);
        }
        Context context = this.mApplicationContext;
        if (context != null) {
            return context;
        }
        return this.mOriginalContext;
    }

    private void setContext(Context context) {
        this.mApplicationContext = context.getApplicationContext();
        if (this.mApplicationContext != null) {
            this.mOriginalContext = null;
        } else {
            this.mOriginalContext = context;
        }
    }

    @UnsupportedAppUsage
    private static IAudioService getService() {
        IAudioService iAudioService = sService;
        if (iAudioService != null) {
            return iAudioService;
        }
        sService = IAudioService.Stub.asInterface(ServiceManager.getService("audio"));
        return sService;
    }

    public void dispatchMediaKeyEvent(KeyEvent keyEvent) {
        MediaSessionLegacyHelper.getHelper(getContext()).sendMediaButtonEvent(keyEvent, false);
    }

    public void preDispatchKeyEvent(KeyEvent event, int stream) {
        int keyCode = event.getKeyCode();
        if (keyCode != 25 && keyCode != 24 && keyCode != 164 && this.mVolumeKeyUpTime + 300 > SystemClock.uptimeMillis()) {
            adjustSuggestedStreamVolume(0, stream, 8);
        }
    }

    public boolean isVolumeFixed() {
        return this.mUseFixedVolume;
    }

    public void adjustStreamVolume(int streamType, int direction, int flags) {
        Log.i(TAG, "adjustStreamVolume streamType " + streamType + " direction " + direction + " flags " + flags);
        try {
            getService().adjustStreamVolume(streamType, direction, flags, getContext().getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void adjustVolume(int direction, int flags) {
        Log.i(TAG, "adjustVolume direction " + direction + " flags " + flags);
        MediaSessionLegacyHelper.getHelper(getContext()).sendAdjustVolumeBy(Integer.MIN_VALUE, direction, flags);
    }

    public void adjustSuggestedStreamVolume(int direction, int suggestedStreamType, int flags) {
        Log.i(TAG, "adjustSuggestedStreamVolume direction " + direction + " suggestedStreamType " + suggestedStreamType + " flags " + flags);
        MediaSessionLegacyHelper.getHelper(getContext()).sendAdjustVolumeBy(suggestedStreamType, direction, flags);
    }

    @UnsupportedAppUsage
    public void setMasterMute(boolean mute, int flags) {
        Log.i(TAG, "setMasterMute mute " + mute + " flags " + flags);
        try {
            getService().setMasterMute(mute, flags, getContext().getOpPackageName(), UserHandle.getCallingUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getRingerMode() {
        try {
            return getService().getRingerModeExternal();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public static boolean isValidRingerMode(int ringerMode) {
        if (ringerMode < 0 || ringerMode > 2) {
            return false;
        }
        try {
            return getService().isValidRingerMode(ringerMode);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getStreamMaxVolume(int streamType) {
        Log.v(TAG, "getStreamMaxVolume  treamType: " + streamType);
        try {
            return getService().getStreamMaxVolume(streamType);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getStreamMinVolume(int streamType) {
        if (isPublicStreamType(streamType)) {
            return getStreamMinVolumeInt(streamType);
        }
        throw new IllegalArgumentException("Invalid stream type " + streamType);
    }

    public int getStreamMinVolumeInt(int streamType) {
        try {
            return getService().getStreamMinVolume(streamType);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getStreamVolume(int streamType) {
        try {
            int volume = getService().getStreamVolume(streamType);
            Log.v(TAG, "getStreamVolume streamType: " + streamType + " volume: " + volume);
            return volume;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public float getStreamVolumeDb(int streamType, int index, int deviceType) {
        if (!isPublicStreamType(streamType)) {
            throw new IllegalArgumentException("Invalid stream type " + streamType);
        } else if (index > getStreamMaxVolume(streamType) || index < getStreamMinVolume(streamType)) {
            throw new IllegalArgumentException("Invalid stream volume index " + index);
        } else if (AudioDeviceInfo.isValidAudioDeviceTypeOut(deviceType)) {
            float gain = AudioSystem.getStreamVolumeDB(streamType, index, AudioDeviceInfo.convertDeviceTypeToInternalDevice(deviceType));
            if (gain <= VOLUME_MIN_DB) {
                return Float.NEGATIVE_INFINITY;
            }
            return gain;
        } else {
            throw new IllegalArgumentException("Invalid audio output device type " + deviceType);
        }
    }

    private static boolean isPublicStreamType(int streamType) {
        switch (streamType) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 8:
            case 9:
            case 10:
                return true;
            case 6:
            case 7:
            default:
                return false;
        }
    }

    @UnsupportedAppUsage
    public int getLastAudibleStreamVolume(int streamType) {
        try {
            return getService().getLastAudibleStreamVolume(streamType);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getUiSoundsStreamType() {
        try {
            return getService().getUiSoundsStreamType();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setRingerMode(int ringerMode) {
        Log.i(TAG, "setRingerMode ringerMode" + ringerMode);
        if (isValidRingerMode(ringerMode)) {
            try {
                getService().setRingerModeExternal(ringerMode, getContext().getOpPackageName());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void setStreamVolume(int streamType, int index, int flags) {
        Log.i(TAG, "setStreamVolume " + streamType + " index: " + index + " flags: " + flags);
        try {
            getService().setStreamVolume(streamType, index, flags, getContext().getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void setVolumeIndexForAttributes(AudioAttributes attr, int index, int flags) {
        Preconditions.checkNotNull(attr, "attr must not be null");
        Log.i(TAG, "setVolumeIndexForAttributes attr" + attr + "index" + index);
        try {
            getService().setVolumeIndexForAttributes(attr, index, flags, getContext().getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public int getVolumeIndexForAttributes(AudioAttributes attr) {
        Preconditions.checkNotNull(attr, "attr must not be null");
        try {
            return getService().getVolumeIndexForAttributes(attr);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public int getMaxVolumeIndexForAttributes(AudioAttributes attr) {
        Preconditions.checkNotNull(attr, "attr must not be null");
        try {
            return getService().getMaxVolumeIndexForAttributes(attr);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public int getMinVolumeIndexForAttributes(AudioAttributes attr) {
        Preconditions.checkNotNull(attr, "attr must not be null");
        try {
            return getService().getMinVolumeIndexForAttributes(attr);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public void setStreamSolo(int streamType, boolean state) {
        Log.w(TAG, "setStreamSolo has been deprecated. Do not use.");
    }

    @Deprecated
    public void setStreamMute(int streamType, boolean state) {
        Log.i(TAG, "setStreamMute streamType " + streamType + " state " + state);
        int direction = state ? -100 : 100;
        if (streamType == Integer.MIN_VALUE) {
            adjustSuggestedStreamVolume(direction, streamType, 0);
            return;
        }
        adjustStreamVolume(streamType, direction, 0);
        if (direction == -100 && streamType == 2) {
            HwMediaMonitorManager.writeLogMsg(HwMediaMonitorUtils.LOG_AUDIO_POLICY_VOLUME, 1, 0, "SSM");
        }
    }

    public boolean isStreamMute(int streamType) {
        Log.v(TAG, "isStreamMute   streamType: " + streamType);
        try {
            return getService().isStreamMute(streamType);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public boolean isMasterMute() {
        try {
            return getService().isMasterMute();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public void forceVolumeControlStream(int streamType) {
        try {
            getService().forceVolumeControlStream(streamType, this.mICallBack);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean shouldVibrate(int vibrateType) {
        try {
            return getService().shouldVibrate(vibrateType);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getVibrateSetting(int vibrateType) {
        try {
            return getService().getVibrateSetting(vibrateType);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setVibrateSetting(int vibrateType, int vibrateSetting) {
        Log.i(TAG, "setVibrateSetting: vibrateType" + vibrateType + "vibrateSetting " + vibrateSetting);
        try {
            getService().setVibrateSetting(vibrateType, vibrateSetting);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setSpeakerphoneOn(boolean on) {
        Log.i(TAG, "setSpeakerphoneOn: " + on);
        try {
            getService().setSpeakerphoneOn(on);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isSpeakerphoneOn() {
        try {
            boolean result = getService().isSpeakerphoneOn();
            Log.v(TAG, "isSpeakerphoneOn: " + result);
            return result;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setAllowedCapturePolicy(int capturePolicy) {
        int result = AudioSystem.setAllowedCapturePolicy(Process.myUid(), AudioAttributes.capturePolicyToFlags(capturePolicy, 0));
        if (result != 0) {
            Log.e(TAG, "Could not setAllowedCapturePolicy: " + result);
            return;
        }
        this.mCapturePolicy = capturePolicy;
    }

    public int getAllowedCapturePolicy() {
        return this.mCapturePolicy;
    }

    public static boolean isOffloadedPlaybackSupported(AudioFormat format, AudioAttributes attributes) {
        if (format == null) {
            throw new NullPointerException("Illegal null AudioFormat");
        } else if (attributes != null) {
            return AudioSystem.isOffloadSupported(format, attributes);
        } else {
            throw new NullPointerException("Illegal null AudioAttributes");
        }
    }

    private boolean isWeChat() {
        int pid = Process.myPid();
        String processName = "";
        Iterator<ActivityManager.RunningAppProcessInfo> it = ((ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE)).getRunningAppProcesses().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            ActivityManager.RunningAppProcessInfo process = it.next();
            if (process.pid == pid) {
                processName = process.processName;
                break;
            }
        }
        if (processName.equals(WECHAT_NAME)) {
            return true;
        }
        return false;
    }

    private boolean isScoAvailableOffCall() {
        boolean result = true;
        IBinder binder = ServiceManager.getService("audio");
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken("android.media.IAudioService");
            if (binder != null) {
                binder.transact(1105, data, reply, 0);
            }
            reply.readException();
            result = reply.readBoolean();
        } catch (RemoteException e) {
            Log.e(TAG, "Get Remote Interface Fail!");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
        return result;
    }

    public boolean isBluetoothScoAvailableOffCall() {
        if (isScoAvailableOffCall() || !isWeChat()) {
            return getContext().getResources().getBoolean(R.bool.config_bluetooth_sco_off_call);
        }
        Log.v(TAG, "WeChat SCO is not available becasue of device is watch/Talkband...");
        return false;
    }

    public void startBluetoothSco() {
        Log.i(TAG, "startBluetoothSco");
        try {
            getService().startBluetoothSco(this.mICallBack, getContext().getApplicationInfo().targetSdkVersion);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public void startBluetoothScoVirtualCall() {
        Log.i(TAG, "startBluetoothScoVirtualCall");
        try {
            getService().startBluetoothScoVirtualCall(this.mICallBack);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void stopBluetoothSco() {
        Log.i(TAG, "stopBluetoothSco");
        try {
            getService().stopBluetoothSco(this.mICallBack);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setBluetoothScoOn(boolean on) {
        Log.i(TAG, "setBluetoothScoOn: " + on);
        try {
            getService().setBluetoothScoOn(on);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isBluetoothScoOn() {
        Log.v(TAG, "isBluetoothScoOn...");
        try {
            return getService().isBluetoothScoOn();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public void setBluetoothA2dpOn(boolean on) {
    }

    public boolean isBluetoothA2dpOn() {
        Log.v(TAG, "isBluetoothA2dpOn...");
        if (AudioSystem.getDeviceConnectionState(128, "") == 1 || AudioSystem.getDeviceConnectionState(256, "") == 1 || AudioSystem.getDeviceConnectionState(512, "") == 1) {
            return true;
        }
        return false;
    }

    @Deprecated
    public void setWiredHeadsetOn(boolean on) {
    }

    public boolean isWiredHeadsetOn() {
        Log.v(TAG, "isWiredHeadsetOn...");
        if (AudioSystem.getDeviceConnectionState(4, "") == 0 && AudioSystem.getDeviceConnectionState(8, "") == 0 && AudioSystem.getDeviceConnectionState(67108864, "") == 0 && AudioSystem.getDeviceConnectionState(16384, "") == 0 && AudioSystem.getDeviceConnectionState(536870912, "") == 0) {
            return false;
        }
        return true;
    }

    public void setMicrophoneMute(boolean on) {
        Log.i(TAG, "setMicrophoneMute " + on);
        try {
            getService().setMicrophoneMute(on, getContext().getOpPackageName(), UserHandle.getCallingUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isMicrophoneMute() {
        Log.v(TAG, "isMicrophoneMute... ");
        return AudioSystem.isMicrophoneMuted();
    }

    public void setMode(int mode) {
        Log.i(TAG, "setMode " + mode);
        try {
            getService().setMode(mode, this.mICallBack, this.mApplicationContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getMode() {
        Log.v(TAG, "getMode...");
        try {
            return getService().getMode();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public void setRouting(int mode, int routes, int mask) {
    }

    @Deprecated
    public int getRouting(int mode) {
        return -1;
    }

    public boolean isMusicActive() {
        Log.v(TAG, "isMusicActive...");
        return AudioSystem.isStreamActive(3, 0);
    }

    @UnsupportedAppUsage
    public boolean isMusicActiveRemotely() {
        return AudioSystem.isStreamActiveRemotely(3, 0);
    }

    public boolean isAudioFocusExclusive() {
        try {
            return getService().getCurrentAudioFocus() == 4;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int generateAudioSessionId() {
        int session = AudioSystem.newAudioSessionId();
        if (session > 0) {
            return session;
        }
        Log.e(TAG, "Failure to generate a new audio session ID");
        return -1;
    }

    @Deprecated
    public void setParameter(String key, String value) {
        setParameters(key + "=" + value);
    }

    public void setParameters(String keyValuePairs) {
        AudioSystem.setParameters(keyValuePairs);
        if ("Karaoke_enable=enable".equals(keyValuePairs)) {
            setICallBackForKaraoke();
        }
        if (keyValuePairs.startsWith("RemoveKaraokePkg=")) {
            Log.i(TAG, "setParameters, startsWith RemoveKaraokePkg");
            notifySendBroadcastForKaraoke(Integer.valueOf(keyValuePairs.split("=")[1]).intValue());
        }
    }

    private void notifySendBroadcastForKaraoke(int uid) {
        IAudioService service = getService();
        if (service != null) {
            try {
                service.notifySendBroadcastForKaraoke(uid);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    private void setICallBackForKaraoke() {
        Log.i(TAG, "setICallBackForKaraoke caller pid = " + Process.myPid());
        IBinder binder = ServiceManager.getService("audio");
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken("android.media.IAudioService");
            data.writeStrongBinder(this.mICallBack);
            if (binder != null) {
                binder.transact(1107, data, reply, 0);
            }
            reply.readException();
        } catch (RemoteException e) {
            Log.e(TAG, "transact e: RemoteException");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
    }

    public String getParameters(String keys) {
        return AudioSystem.getParameters(keys);
    }

    public void playSoundEffect(int effectType) {
        if (effectType >= 0 && effectType < 10 && querySoundEffectsEnabled(Process.myUserHandle().getIdentifier())) {
            IAudioService service = getService();
            try {
                Jlog.d(174, effectType, "playSoundEffect begin");
                service.playSoundEffect(effectType);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void playSoundEffect(int effectType, int userId) {
        if (effectType >= 0 && effectType < 10 && querySoundEffectsEnabled(userId)) {
            IAudioService service = getService();
            try {
                Jlog.d(174, effectType, "playSoundEffect begin");
                service.playSoundEffect(effectType);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void playSoundEffect(int effectType, float volume) {
        if (effectType >= 0 && effectType < 10) {
            IAudioService service = getService();
            try {
                Jlog.d(174, effectType, "playSoundEffect begin");
                service.playSoundEffectVolume(effectType, volume);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    private boolean querySoundEffectsEnabled(int user) {
        Log.v(TAG, "querySoundEffectsEnabled...");
        return Settings.System.getIntForUser(getContext().getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED, 0, user) != 0;
    }

    public void loadSoundEffects() {
        try {
            getService().loadSoundEffects();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void unloadSoundEffects() {
        try {
            getService().unloadSoundEffects();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /* access modifiers changed from: private */
    public static class FocusRequestInfo {
        final Handler mHandler;
        final AudioFocusRequest mRequest;

        FocusRequestInfo(AudioFocusRequest afr, Handler handler) {
            this.mRequest = afr;
            this.mHandler = handler;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private FocusRequestInfo findFocusRequestInfo(String id) {
        return this.mAudioFocusIdListenerMap.get(id);
    }

    /* access modifiers changed from: private */
    public class ServiceEventHandlerDelegate {
        private final Handler mHandler;

        ServiceEventHandlerDelegate(Handler handler) {
            Looper looper;
            if (handler == null) {
                Looper myLooper = Looper.myLooper();
                looper = myLooper;
                if (myLooper == null) {
                    looper = Looper.getMainLooper();
                }
            } else {
                looper = handler.getLooper();
            }
            if (looper != null) {
                this.mHandler = new Handler(looper, AudioManager.this) {
                    /* class android.media.AudioManager.ServiceEventHandlerDelegate.AnonymousClass1 */

                    @Override // android.os.Handler
                    public void handleMessage(Message msg) {
                        OnAudioFocusChangeListener listener;
                        int i = msg.what;
                        if (i == 0) {
                            FocusRequestInfo fri = AudioManager.this.findFocusRequestInfo((String) msg.obj);
                            if (fri != null && (listener = fri.mRequest.getOnAudioFocusChangeListener()) != null) {
                                Log.d(AudioManager.TAG, "dispatching onAudioFocusChange(" + msg.arg1 + ") to " + msg.obj);
                                listener.onAudioFocusChange(msg.arg1);
                            }
                        } else if (i == 1) {
                            RecordConfigChangeCallbackData cbData = (RecordConfigChangeCallbackData) msg.obj;
                            if (cbData.mCb != null) {
                                cbData.mCb.onRecordingConfigChanged(cbData.mConfigs);
                            }
                        } else if (i != 2) {
                            Log.e(AudioManager.TAG, "Unknown event " + msg.what);
                        } else {
                            PlaybackConfigChangeCallbackData cbData2 = (PlaybackConfigChangeCallbackData) msg.obj;
                            if (cbData2.mCb != null) {
                                Log.d(AudioManager.TAG, "dispatching onPlaybackConfigChanged()");
                                cbData2.mCb.onPlaybackConfigChanged(cbData2.mConfigs);
                            }
                        }
                    }
                };
            } else {
                this.mHandler = null;
            }
        }

        /* access modifiers changed from: package-private */
        public Handler getHandler() {
            return this.mHandler;
        }
    }

    private String getIdForAudioFocusListener(OnAudioFocusChangeListener l) {
        if (l == null) {
            return new String(toString());
        }
        return new String(toString() + l.toString());
    }

    public void registerAudioFocusRequest(AudioFocusRequest afr) {
        Handler handler;
        Handler h = afr.getOnAudioFocusChangeListenerHandler();
        if (h == null) {
            handler = null;
        } else {
            handler = new ServiceEventHandlerDelegate(h).getHandler();
        }
        FocusRequestInfo fri = new FocusRequestInfo(afr, handler);
        this.mAudioFocusIdListenerMap.put(getIdForAudioFocusListener(afr.getOnAudioFocusChangeListener()), fri);
    }

    public void unregisterAudioFocusRequest(OnAudioFocusChangeListener l) {
        this.mAudioFocusIdListenerMap.remove(getIdForAudioFocusListener(l));
    }

    public int requestAudioFocus(OnAudioFocusChangeListener l, int streamType, int durationHint) {
        Log.i(TAG, "requestAudioFocus streamType: " + streamType + " durationHint: " + durationHint);
        PlayerBase.deprecateStreamTypeForPlayback(streamType, TAG, "requestAudioFocus()");
        try {
            return requestAudioFocus(l, new AudioAttributes.Builder().setInternalLegacyStreamType(streamType).build(), durationHint, 0);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Audio focus request denied due to ", e);
            return 0;
        }
    }

    public int requestAudioFocus(AudioFocusRequest focusRequest) {
        Log.i(TAG, "requestAudioFocus focusRequest");
        return requestAudioFocus(focusRequest, null);
    }

    public int abandonAudioFocusRequest(AudioFocusRequest focusRequest) {
        Log.i(TAG, "abandonAudioFocusRequest focusRequest");
        if (focusRequest != null) {
            return abandonAudioFocus(focusRequest.getOnAudioFocusChangeListener(), focusRequest.getAudioAttributes());
        }
        throw new IllegalArgumentException("Illegal null AudioFocusRequest");
    }

    @SystemApi
    public int requestAudioFocus(OnAudioFocusChangeListener l, AudioAttributes requestAttributes, int durationHint, int flags) throws IllegalArgumentException {
        if (flags == (flags & 3)) {
            return requestAudioFocus(l, requestAttributes, durationHint, flags & 3, null);
        }
        throw new IllegalArgumentException("Invalid flags 0x" + Integer.toHexString(flags).toUpperCase());
    }

    @SystemApi
    public int requestAudioFocus(OnAudioFocusChangeListener l, AudioAttributes requestAttributes, int durationHint, int flags, AudioPolicy ap) throws IllegalArgumentException {
        if (requestAttributes == null) {
            throw new IllegalArgumentException("Illegal null AudioAttributes argument");
        } else if (!AudioFocusRequest.isValidFocusGain(durationHint)) {
            throw new IllegalArgumentException("Invalid duration hint");
        } else if (flags == (flags & 7)) {
            boolean z = true;
            if ((flags & 1) == 1 && l == null) {
                throw new IllegalArgumentException("Illegal null focus listener when flagged as accepting delayed focus grant");
            } else if ((flags & 2) == 2 && l == null) {
                throw new IllegalArgumentException("Illegal null focus listener when flagged as pausing instead of ducking");
            } else if ((flags & 4) == 4 && ap == null) {
                throw new IllegalArgumentException("Illegal null audio policy when locking audio focus");
            } else {
                AudioFocusRequest.Builder willPauseWhenDucked = new AudioFocusRequest.Builder(durationHint).setOnAudioFocusChangeListenerInt(l, null).setAudioAttributes(requestAttributes).setAcceptsDelayedFocusGain((flags & 1) == 1).setWillPauseWhenDucked((flags & 2) == 2);
                if ((flags & 4) != 4) {
                    z = false;
                }
                return requestAudioFocus(willPauseWhenDucked.setLocksFocus(z).build(), ap);
            }
        } else {
            throw new IllegalArgumentException("Illegal flags 0x" + Integer.toHexString(flags).toUpperCase());
        }
    }

    @SystemApi
    public int requestAudioFocus(AudioFocusRequest afr, AudioPolicy ap) {
        int sdk;
        BlockingFocusResultReceiver focusReceiver;
        if (afr == null) {
            throw new NullPointerException("Illegal null AudioFocusRequest");
        } else if (!afr.locksFocus() || ap != null) {
            registerAudioFocusRequest(afr);
            IAudioService service = getService();
            try {
                sdk = getContext().getApplicationInfo().targetSdkVersion;
            } catch (NullPointerException e) {
                sdk = Build.VERSION.SDK_INT;
            }
            String clientId = getIdForAudioFocusListener(afr.getOnAudioFocusChangeListener());
            synchronized (this.mFocusRequestsLock) {
                try {
                    int status = service.requestAudioFocus(afr.getAudioAttributes(), afr.getFocusGain(), this.mICallBack, this.mAudioFocusDispatcher, clientId, getContext().getOpPackageName(), afr.getFlags(), ap != null ? ap.cb() : null, sdk);
                    if (status != 100) {
                        return status;
                    }
                    if (this.mFocusRequestsAwaitingResult == null) {
                        this.mFocusRequestsAwaitingResult = new HashMap<>(1);
                    }
                    focusReceiver = new BlockingFocusResultReceiver(clientId);
                    this.mFocusRequestsAwaitingResult.put(clientId, focusReceiver);
                } catch (RemoteException e2) {
                    throw e2.rethrowFromSystemServer();
                }
            }
            focusReceiver.waitForResult(200);
            if (!focusReceiver.receivedResult()) {
                Log.e(TAG, "requestAudio response from ext policy timed out, denying request");
            }
            synchronized (this.mFocusRequestsLock) {
                this.mFocusRequestsAwaitingResult.remove(clientId);
            }
            return focusReceiver.requestResult();
        } else {
            throw new IllegalArgumentException("Illegal null audio policy when locking audio focus");
        }
    }

    /* access modifiers changed from: private */
    public static final class SafeWaitObject {
        private boolean mQuit;

        private SafeWaitObject() {
            this.mQuit = false;
        }

        public void safeNotify() {
            synchronized (this) {
                this.mQuit = true;
                notify();
            }
        }

        public void safeWait(long millis) throws InterruptedException {
            long timeOutTime = System.currentTimeMillis() + millis;
            synchronized (this) {
                while (true) {
                    if (this.mQuit) {
                        break;
                    }
                    long timeToWait = timeOutTime - System.currentTimeMillis();
                    if (timeToWait < 0) {
                        break;
                    }
                    wait(timeToWait);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class BlockingFocusResultReceiver {
        private final String mFocusClientId;
        private int mFocusRequestResult = 0;
        private final SafeWaitObject mLock = new SafeWaitObject();
        @GuardedBy({"mLock"})
        private boolean mResultReceived = false;

        BlockingFocusResultReceiver(String clientId) {
            this.mFocusClientId = clientId;
        }

        /* access modifiers changed from: package-private */
        public boolean receivedResult() {
            return this.mResultReceived;
        }

        /* access modifiers changed from: package-private */
        public int requestResult() {
            return this.mFocusRequestResult;
        }

        /* access modifiers changed from: package-private */
        public void notifyResult(int requestResult) {
            synchronized (this.mLock) {
                this.mResultReceived = true;
                this.mFocusRequestResult = requestResult;
                this.mLock.safeNotify();
            }
        }

        public void waitForResult(long timeOutMs) {
            synchronized (this.mLock) {
                if (!this.mResultReceived) {
                    try {
                        this.mLock.safeWait(timeOutMs);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }

    @UnsupportedAppUsage
    public void requestAudioFocusForCall(int streamType, int durationHint) {
        Log.i(TAG, "requestAudioFocusForCall streamType: " + streamType + " durationHint: " + durationHint);
        try {
            getService().requestAudioFocus(new AudioAttributes.Builder().setInternalLegacyStreamType(streamType).build(), durationHint, this.mICallBack, null, AudioSystem.IN_VOICE_COMM_FOCUS_ID, getContext().getOpPackageName(), 4, null, 0);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getFocusRampTimeMs(int focusGain, AudioAttributes attr) {
        try {
            return getService().getFocusRampTimeMs(focusGain, attr);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void setFocusRequestResult(AudioFocusInfo afi, int requestResult, AudioPolicy ap) {
        if (afi == null) {
            throw new IllegalArgumentException("Illegal null AudioFocusInfo");
        } else if (ap != null) {
            Log.i(TAG, "setFocusRequestResult: " + requestResult);
            try {
                getService().setFocusRequestResultFromExtPolicy(afi, requestResult, ap.cb());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("Illegal null AudioPolicy");
        }
    }

    @SystemApi
    public int dispatchAudioFocusChange(AudioFocusInfo afi, int focusChange, AudioPolicy ap) {
        if (afi == null) {
            throw new NullPointerException("Illegal null AudioFocusInfo");
        } else if (ap != null) {
            try {
                return getService().dispatchFocusChange(afi, focusChange, ap.cb());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new NullPointerException("Illegal null AudioPolicy");
        }
    }

    @UnsupportedAppUsage
    public void abandonAudioFocusForCall() {
        Log.i(TAG, "abandonAudioFocusForCall ");
        try {
            getService().abandonAudioFocus(null, AudioSystem.IN_VOICE_COMM_FOCUS_ID, null, getContext().getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int abandonAudioFocus(OnAudioFocusChangeListener l) {
        Log.i(TAG, "abandonAudioFocus listener");
        return abandonAudioFocus(l, null);
    }

    @SystemApi
    @SuppressLint({"Doclava125"})
    public int abandonAudioFocus(OnAudioFocusChangeListener l, AudioAttributes aa) {
        unregisterAudioFocusRequest(l);
        try {
            return getService().abandonAudioFocus(this.mAudioFocusDispatcher, getIdForAudioFocusListener(l), aa, getContext().getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public void registerMediaButtonEventReceiver(ComponentName eventReceiver) {
        if (eventReceiver != null) {
            if (!eventReceiver.getPackageName().equals(getContext().getPackageName())) {
                Log.e(TAG, "registerMediaButtonEventReceiver() error: receiver and context package names don't match");
                return;
            }
            Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            mediaButtonIntent.setComponent(eventReceiver);
            registerMediaButtonIntent(PendingIntent.getBroadcast(getContext(), 0, mediaButtonIntent, 0), eventReceiver);
        }
    }

    @Deprecated
    public void registerMediaButtonEventReceiver(PendingIntent eventReceiver) {
        if (eventReceiver != null) {
            registerMediaButtonIntent(eventReceiver, null);
        }
    }

    public void registerMediaButtonIntent(PendingIntent pi, ComponentName eventReceiver) {
        if (pi == null) {
            Log.e(TAG, "Cannot call registerMediaButtonIntent() with a null parameter");
        } else {
            MediaSessionLegacyHelper.getHelper(getContext()).addMediaButtonListener(pi, eventReceiver, getContext());
        }
    }

    @Deprecated
    public void unregisterMediaButtonEventReceiver(ComponentName eventReceiver) {
        if (eventReceiver != null) {
            Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            mediaButtonIntent.setComponent(eventReceiver);
            unregisterMediaButtonIntent(PendingIntent.getBroadcast(getContext(), 0, mediaButtonIntent, 0));
        }
    }

    @Deprecated
    public void unregisterMediaButtonEventReceiver(PendingIntent eventReceiver) {
        if (eventReceiver != null) {
            unregisterMediaButtonIntent(eventReceiver);
        }
    }

    public void unregisterMediaButtonIntent(PendingIntent pi) {
        MediaSessionLegacyHelper.getHelper(getContext()).removeMediaButtonListener(pi);
    }

    @Deprecated
    public void registerRemoteControlClient(RemoteControlClient rcClient) {
        if (rcClient != null && rcClient.getRcMediaIntent() != null) {
            rcClient.registerWithSession(MediaSessionLegacyHelper.getHelper(getContext()));
        }
    }

    @Deprecated
    public void unregisterRemoteControlClient(RemoteControlClient rcClient) {
        if (rcClient != null && rcClient.getRcMediaIntent() != null) {
            rcClient.unregisterWithSession(MediaSessionLegacyHelper.getHelper(getContext()));
        }
    }

    @Deprecated
    public boolean registerRemoteController(RemoteController rctlr) {
        if (rctlr == null) {
            return false;
        }
        rctlr.startListeningToSessions();
        return true;
    }

    @Deprecated
    public void unregisterRemoteController(RemoteController rctlr) {
        if (rctlr != null) {
            rctlr.stopListeningToSessions();
        }
    }

    @SystemApi
    public int registerAudioPolicy(AudioPolicy policy) {
        return registerAudioPolicyStatic(policy);
    }

    static int registerAudioPolicyStatic(AudioPolicy policy) {
        if (policy != null) {
            IAudioService service = getService();
            try {
                MediaProjection projection = policy.getMediaProjection();
                String regId = service.registerAudioPolicy(policy.getConfig(), policy.cb(), policy.hasFocusListener(), policy.isFocusPolicy(), policy.isTestFocusPolicy(), policy.isVolumeController(), projection == null ? null : projection.getProjection());
                if (regId == null) {
                    return -1;
                }
                policy.setRegistration(regId);
                return 0;
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("Illegal null AudioPolicy argument");
        }
    }

    @SystemApi
    public void unregisterAudioPolicyAsync(AudioPolicy policy) {
        unregisterAudioPolicyAsyncStatic(policy);
    }

    static void unregisterAudioPolicyAsyncStatic(AudioPolicy policy) {
        if (policy != null) {
            try {
                getService().unregisterAudioPolicyAsync(policy.cb());
                policy.setRegistration(null);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("Illegal null AudioPolicy argument");
        }
    }

    @SystemApi
    public void unregisterAudioPolicy(AudioPolicy policy) {
        Preconditions.checkNotNull(policy, "Illegal null AudioPolicy argument");
        IAudioService service = getService();
        try {
            policy.invalidateCaptorsAndInjectors();
            service.unregisterAudioPolicy(policy.cb());
            policy.setRegistration(null);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean hasRegisteredDynamicPolicy() {
        try {
            return getService().hasRegisteredDynamicPolicy();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static abstract class AudioPlaybackCallback {
        public void onPlaybackConfigChanged(List<AudioPlaybackConfiguration> list) {
        }
    }

    /* access modifiers changed from: private */
    public static class AudioPlaybackCallbackInfo {
        final AudioPlaybackCallback mCb;
        final Handler mHandler;

        AudioPlaybackCallbackInfo(AudioPlaybackCallback cb, Handler handler) {
            this.mCb = cb;
            this.mHandler = handler;
        }
    }

    private static final class PlaybackConfigChangeCallbackData {
        final AudioPlaybackCallback mCb;
        final List<AudioPlaybackConfiguration> mConfigs;

        PlaybackConfigChangeCallbackData(AudioPlaybackCallback cb, List<AudioPlaybackConfiguration> configs) {
            this.mCb = cb;
            this.mConfigs = configs;
        }
    }

    public void registerAudioPlaybackCallback(AudioPlaybackCallback cb, Handler handler) {
        if (cb != null) {
            synchronized (this.mPlaybackCallbackLock) {
                if (this.mPlaybackCallbackList == null) {
                    this.mPlaybackCallbackList = new ArrayList();
                }
                int oldCbCount = this.mPlaybackCallbackList.size();
                if (!hasPlaybackCallback_sync(cb)) {
                    this.mPlaybackCallbackList.add(new AudioPlaybackCallbackInfo(cb, new ServiceEventHandlerDelegate(handler).getHandler()));
                    int newCbCount = this.mPlaybackCallbackList.size();
                    if (oldCbCount == 0 && newCbCount > 0) {
                        try {
                            getService().registerPlaybackCallback(this.mPlayCb);
                        } catch (RemoteException e) {
                            throw e.rethrowFromSystemServer();
                        }
                    }
                } else {
                    Log.w(TAG, "attempt to call registerAudioPlaybackCallback() on a previouslyregistered callback");
                }
            }
            return;
        }
        throw new IllegalArgumentException("Illegal null AudioPlaybackCallback argument");
    }

    public void unregisterAudioPlaybackCallback(AudioPlaybackCallback cb) {
        if (cb != null) {
            synchronized (this.mPlaybackCallbackLock) {
                if (this.mPlaybackCallbackList == null) {
                    Log.w(TAG, "attempt to call unregisterAudioPlaybackCallback() on a callback that was never registered");
                    return;
                }
                int oldCbCount = this.mPlaybackCallbackList.size();
                if (removePlaybackCallback_sync(cb)) {
                    int newCbCount = this.mPlaybackCallbackList.size();
                    if (oldCbCount > 0 && newCbCount == 0) {
                        try {
                            getService().unregisterPlaybackCallback(this.mPlayCb);
                        } catch (RemoteException e) {
                            throw e.rethrowFromSystemServer();
                        }
                    }
                } else {
                    Log.w(TAG, "attempt to call unregisterAudioPlaybackCallback() on a callback already unregistered or never registered");
                }
                return;
            }
        }
        throw new IllegalArgumentException("Illegal null AudioPlaybackCallback argument");
    }

    public List<AudioPlaybackConfiguration> getActivePlaybackConfigurations() {
        try {
            return getService().getActivePlaybackConfigurations();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private boolean hasPlaybackCallback_sync(AudioPlaybackCallback cb) {
        if (this.mPlaybackCallbackList == null) {
            return false;
        }
        for (int i = 0; i < this.mPlaybackCallbackList.size(); i++) {
            if (cb.equals(this.mPlaybackCallbackList.get(i).mCb)) {
                return true;
            }
        }
        return false;
    }

    private boolean removePlaybackCallback_sync(AudioPlaybackCallback cb) {
        if (this.mPlaybackCallbackList == null) {
            return false;
        }
        for (int i = 0; i < this.mPlaybackCallbackList.size(); i++) {
            if (cb.equals(this.mPlaybackCallbackList.get(i).mCb)) {
                this.mPlaybackCallbackList.remove(i);
                return true;
            }
        }
        return false;
    }

    public static abstract class AudioRecordingCallback {
        public void onRecordingConfigChanged(List<AudioRecordingConfiguration> list) {
        }
    }

    /* access modifiers changed from: private */
    public static class AudioRecordingCallbackInfo {
        final AudioRecordingCallback mCb;
        final Handler mHandler;

        AudioRecordingCallbackInfo(AudioRecordingCallback cb, Handler handler) {
            this.mCb = cb;
            this.mHandler = handler;
        }
    }

    private static final class RecordConfigChangeCallbackData {
        final AudioRecordingCallback mCb;
        final List<AudioRecordingConfiguration> mConfigs;

        RecordConfigChangeCallbackData(AudioRecordingCallback cb, List<AudioRecordingConfiguration> configs) {
            this.mCb = cb;
            this.mConfigs = configs;
        }
    }

    public void registerAudioRecordingCallback(AudioRecordingCallback cb, Handler handler) {
        if (cb != null) {
            synchronized (this.mRecordCallbackLock) {
                if (this.mRecordCallbackList == null) {
                    this.mRecordCallbackList = new ArrayList();
                }
                int oldCbCount = this.mRecordCallbackList.size();
                if (!hasRecordCallback_sync(cb)) {
                    this.mRecordCallbackList.add(new AudioRecordingCallbackInfo(cb, new ServiceEventHandlerDelegate(handler).getHandler()));
                    int newCbCount = this.mRecordCallbackList.size();
                    if (oldCbCount == 0 && newCbCount > 0) {
                        try {
                            getService().registerRecordingCallback(this.mRecCb);
                        } catch (RemoteException e) {
                            throw e.rethrowFromSystemServer();
                        }
                    }
                } else {
                    Log.w(TAG, "attempt to call registerAudioRecordingCallback() on a previouslyregistered callback");
                }
            }
            return;
        }
        throw new IllegalArgumentException("Illegal null AudioRecordingCallback argument");
    }

    public void unregisterAudioRecordingCallback(AudioRecordingCallback cb) {
        if (cb != null) {
            synchronized (this.mRecordCallbackLock) {
                if (this.mRecordCallbackList != null) {
                    int oldCbCount = this.mRecordCallbackList.size();
                    if (removeRecordCallback_sync(cb)) {
                        int newCbCount = this.mRecordCallbackList.size();
                        if (oldCbCount > 0 && newCbCount == 0) {
                            try {
                                getService().unregisterRecordingCallback(this.mRecCb);
                            } catch (RemoteException e) {
                                throw e.rethrowFromSystemServer();
                            }
                        }
                    } else {
                        Log.w(TAG, "attempt to call unregisterAudioRecordingCallback() on a callback already unregistered or never registered");
                    }
                    return;
                }
                return;
            }
        }
        throw new IllegalArgumentException("Illegal null AudioRecordingCallback argument");
    }

    public List<AudioRecordingConfiguration> getActiveRecordingConfigurations() {
        try {
            return getService().getActiveRecordingConfigurations();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private boolean hasRecordCallback_sync(AudioRecordingCallback cb) {
        if (this.mRecordCallbackList == null) {
            return false;
        }
        for (int i = 0; i < this.mRecordCallbackList.size(); i++) {
            if (cb.equals(this.mRecordCallbackList.get(i).mCb)) {
                return true;
            }
        }
        return false;
    }

    private boolean removeRecordCallback_sync(AudioRecordingCallback cb) {
        if (this.mRecordCallbackList == null) {
            return false;
        }
        for (int i = 0; i < this.mRecordCallbackList.size(); i++) {
            if (cb.equals(this.mRecordCallbackList.get(i).mCb)) {
                this.mRecordCallbackList.remove(i);
                return true;
            }
        }
        return false;
    }

    @UnsupportedAppUsage
    public void reloadAudioSettings() {
        try {
            getService().reloadAudioSettings();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void avrcpSupportsAbsoluteVolume(String address, boolean support) {
        Log.i(TAG, "avrcpSupportsAbsoluteVolume support: " + support);
        try {
            getService().avrcpSupportsAbsoluteVolume(address, support);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public boolean isSilentMode() {
        int ringerMode = getRingerMode();
        return ringerMode == 0 || ringerMode == 1;
    }

    public static boolean isOutputDevice(int device) {
        return (Integer.MIN_VALUE & device) == 0;
    }

    public static boolean isInputDevice(int device) {
        return (device & Integer.MIN_VALUE) == Integer.MIN_VALUE;
    }

    @UnsupportedAppUsage
    public int getDevicesForStream(int streamType) {
        switch (streamType) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 8:
            case 9:
            case 10:
                return AudioSystem.getDevicesForStream(streamType);
            case 6:
            case 7:
            default:
                return 0;
        }
    }

    @UnsupportedAppUsage
    public void setWiredDeviceConnectionState(int type, int state, String address, String name) {
        Log.i(TAG, "setWiredDeviceConnectionState type" + type + "state" + state);
        try {
            getService().setWiredDeviceConnectionState(type, state, address, name, this.mApplicationContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setBluetoothHearingAidDeviceConnectionState(BluetoothDevice device, int state, boolean suppressNoisyIntent, int musicDevice) {
        try {
            getService().setBluetoothHearingAidDeviceConnectionState(device, state, suppressNoisyIntent, musicDevice);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setBluetoothA2dpDeviceConnectionStateSuppressNoisyIntent(BluetoothDevice device, int state, int profile, boolean suppressNoisyIntent, int a2dpVolume) {
        Log.i(TAG, "sbta2dp state: " + state + ", profile:" + profile + "suppressNoisyIntent: " + suppressNoisyIntent + " a2dpVolume: " + a2dpVolume);
        try {
            getService().setBluetoothA2dpDeviceConnectionStateSuppressNoisyIntent(device, state, profile, suppressNoisyIntent, a2dpVolume);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void handleBluetoothA2dpDeviceConfigChange(BluetoothDevice device) {
        Log.i(TAG, "handleBluetoothA2dpDeviceConfigChange.");
        try {
            getService().handleBluetoothA2dpDeviceConfigChange(device);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public IRingtonePlayer getRingtonePlayer() {
        Log.v(TAG, "getRingtonePlayer...");
        try {
            return getService().getRingtonePlayer();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String getProperty(String key) {
        if (PROPERTY_OUTPUT_SAMPLE_RATE.equals(key)) {
            int outputSampleRate = AudioSystem.getPrimaryOutputSamplingRate();
            if (outputSampleRate > 0) {
                return Integer.toString(outputSampleRate);
            }
            return null;
        } else if (PROPERTY_OUTPUT_FRAMES_PER_BUFFER.equals(key)) {
            int outputFramesPerBuffer = AudioSystem.getPrimaryOutputFrameCount();
            if (outputFramesPerBuffer > 0) {
                return Integer.toString(outputFramesPerBuffer);
            }
            return null;
        } else if (PROPERTY_SUPPORT_MIC_NEAR_ULTRASOUND.equals(key)) {
            return String.valueOf(getContext().getResources().getBoolean(R.bool.config_supportMicNearUltrasound));
        } else {
            if (PROPERTY_SUPPORT_SPEAKER_NEAR_ULTRASOUND.equals(key)) {
                return String.valueOf(getContext().getResources().getBoolean(R.bool.config_supportSpeakerNearUltrasound));
            }
            if (PROPERTY_SUPPORT_AUDIO_SOURCE_UNPROCESSED.equals(key)) {
                return String.valueOf(getContext().getResources().getBoolean(R.bool.config_supportAudioSourceUnprocessed));
            }
            if (PROPERTY_SUPPORT_HWKARAOKE_EFFECT.equals(key)) {
                return String.valueOf(isHwKaraokeEffectEnable());
            }
            return null;
        }
    }

    private boolean isHwKaraokeEffectEnable() {
        Log.d(TAG, "isHwKaraokeEffectEnable");
        boolean result = true;
        IBinder binder = ServiceManager.getService("audio");
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken("android.media.IAudioService");
            if (binder != null) {
                binder.transact(1106, data, reply, 0);
            }
            reply.readException();
            result = reply.readBoolean();
        } catch (RemoteException e) {
            Log.e(TAG, "transact e: RemoteException");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
        return result;
    }

    @UnsupportedAppUsage
    public int getOutputLatency(int streamType) {
        return AudioSystem.getOutputLatency(streamType);
    }

    public void setVolumeController(IVolumeController controller) {
        try {
            getService().setVolumeController(controller);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void notifyVolumeControllerVisible(IVolumeController controller, boolean visible) {
        try {
            getService().notifyVolumeControllerVisible(controller, visible);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isStreamAffectedByRingerMode(int streamType) {
        try {
            return getService().isStreamAffectedByRingerMode(streamType);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isStreamAffectedByMute(int streamType) {
        try {
            return getService().isStreamAffectedByMute(streamType);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void disableSafeMediaVolume() {
        try {
            getService().disableSafeMediaVolume(this.mApplicationContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public void setRingerModeInternal(int ringerMode) {
        try {
            getService().setRingerModeInternal(ringerMode, getContext().getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public int getRingerModeInternal() {
        try {
            return getService().getRingerModeInternal();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setVolumePolicy(VolumePolicy policy) {
        try {
            getService().setVolumePolicy(policy);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int setHdmiSystemAudioSupported(boolean on) {
        try {
            return getService().setHdmiSystemAudioSupported(on);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    @SuppressLint({"Doclava125"})
    public boolean isHdmiSystemAudioSupported() {
        try {
            return getService().isHdmiSystemAudioSupported();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public static int listAudioPorts(ArrayList<AudioPort> ports) {
        return updateAudioPortCache(ports, null, null);
    }

    public static int listPreviousAudioPorts(ArrayList<AudioPort> ports) {
        return updateAudioPortCache(null, null, ports);
    }

    public static int listAudioDevicePorts(ArrayList<AudioDevicePort> devices) {
        if (devices == null) {
            return -2;
        }
        ArrayList<AudioPort> ports = new ArrayList<>();
        int status = updateAudioPortCache(ports, null, null);
        if (status == 0) {
            filterDevicePorts(ports, devices);
        }
        return status;
    }

    public static int listPreviousAudioDevicePorts(ArrayList<AudioDevicePort> devices) {
        if (devices == null) {
            return -2;
        }
        ArrayList<AudioPort> ports = new ArrayList<>();
        int status = updateAudioPortCache(null, null, ports);
        if (status == 0) {
            filterDevicePorts(ports, devices);
        }
        return status;
    }

    private static void filterDevicePorts(ArrayList<AudioPort> ports, ArrayList<AudioDevicePort> devices) {
        devices.clear();
        for (int i = 0; i < ports.size(); i++) {
            if (ports.get(i) instanceof AudioDevicePort) {
                devices.add((AudioDevicePort) ports.get(i));
            }
        }
    }

    @UnsupportedAppUsage
    public static int createAudioPatch(AudioPatch[] patch, AudioPortConfig[] sources, AudioPortConfig[] sinks) {
        return AudioSystem.createAudioPatch(patch, sources, sinks);
    }

    @UnsupportedAppUsage
    public static int releaseAudioPatch(AudioPatch patch) {
        return AudioSystem.releaseAudioPatch(patch);
    }

    @UnsupportedAppUsage
    public static int listAudioPatches(ArrayList<AudioPatch> patches) {
        return updateAudioPortCache(null, patches, null);
    }

    public static int setAudioPortGain(AudioPort port, AudioGainConfig gain) {
        if (port == null || gain == null) {
            return -2;
        }
        AudioPortConfig activeConfig = port.activeConfig();
        AudioPortConfig config = new AudioPortConfig(port, activeConfig.samplingRate(), activeConfig.channelMask(), activeConfig.format(), gain);
        config.mConfigMask = 8;
        return AudioSystem.setAudioPortConfig(config);
    }

    @UnsupportedAppUsage
    public void registerAudioPortUpdateListener(OnAudioPortUpdateListener l) {
        sAudioPortEventHandler.init();
        sAudioPortEventHandler.registerListener(l);
    }

    @UnsupportedAppUsage
    public void unregisterAudioPortUpdateListener(OnAudioPortUpdateListener l) {
        sAudioPortEventHandler.unregisterListener(l);
    }

    static int resetAudioPortGeneration() {
        int generation;
        synchronized (sAudioPortGeneration) {
            generation = sAudioPortGeneration.intValue();
            sAudioPortGeneration = 0;
        }
        return generation;
    }

    static int updateAudioPortCache(ArrayList<AudioPort> ports, ArrayList<AudioPatch> patches, ArrayList<AudioPort> previousPorts) {
        sAudioPortEventHandler.init();
        synchronized (sAudioPortGeneration) {
            if (sAudioPortGeneration.intValue() == 0) {
                int[] patchGeneration = new int[1];
                int[] portGeneration = new int[1];
                ArrayList<AudioPort> newPorts = new ArrayList<>();
                ArrayList<AudioPatch> newPatches = new ArrayList<>();
                while (true) {
                    newPorts.clear();
                    int status = AudioSystem.listAudioPorts(newPorts, portGeneration);
                    if (status != 0) {
                        Log.w(TAG, "updateAudioPortCache: listAudioPorts failed");
                        return status;
                    }
                    newPatches.clear();
                    int status2 = AudioSystem.listAudioPatches(newPatches, patchGeneration);
                    if (status2 != 0) {
                        Log.w(TAG, "updateAudioPortCache: listAudioPatches failed");
                        return status2;
                    } else if (patchGeneration[0] == portGeneration[0] || !(ports == null || patches == null)) {
                        break;
                    }
                }
                if (patchGeneration[0] != portGeneration[0]) {
                    return -1;
                }
                for (int i = 0; i < newPatches.size(); i++) {
                    for (int j = 0; j < newPatches.get(i).sources().length; j++) {
                        newPatches.get(i).sources()[j] = updatePortConfig(newPatches.get(i).sources()[j], newPorts);
                    }
                    for (int j2 = 0; j2 < newPatches.get(i).sinks().length; j2++) {
                        newPatches.get(i).sinks()[j2] = updatePortConfig(newPatches.get(i).sinks()[j2], newPorts);
                    }
                }
                Iterator<AudioPatch> i2 = newPatches.iterator();
                while (i2.hasNext()) {
                    AudioPatch newPatch = i2.next();
                    boolean hasInvalidPort = false;
                    AudioPortConfig[] sources = newPatch.sources();
                    int length = sources.length;
                    int i3 = 0;
                    while (true) {
                        if (i3 >= length) {
                            break;
                        } else if (sources[i3] == null) {
                            hasInvalidPort = true;
                            break;
                        } else {
                            i3++;
                        }
                    }
                    AudioPortConfig[] sinks = newPatch.sinks();
                    int length2 = sinks.length;
                    int i4 = 0;
                    while (true) {
                        if (i4 >= length2) {
                            break;
                        } else if (sinks[i4] == null) {
                            hasInvalidPort = true;
                            break;
                        } else {
                            i4++;
                        }
                    }
                    if (hasInvalidPort) {
                        i2.remove();
                    }
                }
                sPreviousAudioPortsCached = sAudioPortsCached;
                sAudioPortsCached = newPorts;
                sAudioPatchesCached = newPatches;
                sAudioPortGeneration = Integer.valueOf(portGeneration[0]);
            }
            if (ports != null) {
                ports.clear();
                ports.addAll(sAudioPortsCached);
            }
            if (patches != null) {
                patches.clear();
                patches.addAll(sAudioPatchesCached);
            }
            if (previousPorts != null) {
                previousPorts.clear();
                previousPorts.addAll(sPreviousAudioPortsCached);
            }
            return 0;
        }
    }

    static AudioPortConfig updatePortConfig(AudioPortConfig portCfg, ArrayList<AudioPort> ports) {
        AudioPort port = portCfg.port();
        int k = 0;
        while (true) {
            if (k >= ports.size()) {
                break;
            } else if (ports.get(k).handle().equals(port.handle())) {
                port = ports.get(k);
                break;
            } else {
                k++;
            }
        }
        if (k == ports.size()) {
            Log.e(TAG, "updatePortConfig port not found for handle: " + port.handle().id());
            return null;
        }
        AudioGainConfig gainCfg = portCfg.gain();
        if (gainCfg != null) {
            gainCfg = port.gain(gainCfg.index()).buildConfig(gainCfg.mode(), gainCfg.channelMask(), gainCfg.values(), gainCfg.rampDurationMs());
        }
        return port.buildConfig(portCfg.samplingRate(), portCfg.channelMask(), portCfg.format(), gainCfg);
    }

    private static boolean checkFlags(AudioDevicePort port, int flags) {
        if (port.role() == 2 && (flags & 2) != 0) {
            return true;
        }
        if (port.role() != 1 || (flags & 1) == 0) {
            return false;
        }
        return true;
    }

    private static boolean checkTypes(AudioDevicePort port) {
        return AudioDeviceInfo.convertInternalDeviceToDeviceType(port.type()) != 0;
    }

    public AudioDeviceInfo[] getDevices(int flags) {
        return getDevicesStatic(flags);
    }

    private static AudioDeviceInfo[] infoListFromPortList(ArrayList<AudioDevicePort> ports, int flags) {
        int numRecs = 0;
        Iterator<AudioDevicePort> it = ports.iterator();
        while (it.hasNext()) {
            AudioDevicePort port = it.next();
            if (checkTypes(port) && checkFlags(port, flags)) {
                numRecs++;
            }
        }
        AudioDeviceInfo[] deviceList = new AudioDeviceInfo[numRecs];
        int slot = 0;
        Iterator<AudioDevicePort> it2 = ports.iterator();
        while (it2.hasNext()) {
            AudioDevicePort port2 = it2.next();
            if (checkTypes(port2) && checkFlags(port2, flags)) {
                deviceList[slot] = new AudioDeviceInfo(port2);
                slot++;
            }
        }
        return deviceList;
    }

    private static AudioDeviceInfo[] calcListDeltas(ArrayList<AudioDevicePort> ports_A, ArrayList<AudioDevicePort> ports_B, int flags) {
        ArrayList<AudioDevicePort> delta_ports = new ArrayList<>();
        for (int cur_index = 0; cur_index < ports_B.size(); cur_index++) {
            boolean cur_port_found = false;
            AudioDevicePort cur_port = ports_B.get(cur_index);
            for (int prev_index = 0; prev_index < ports_A.size() && !cur_port_found; prev_index++) {
                cur_port_found = cur_port.id() == ports_A.get(prev_index).id();
            }
            if (!cur_port_found) {
                delta_ports.add(cur_port);
            }
        }
        return infoListFromPortList(delta_ports, flags);
    }

    public static AudioDeviceInfo[] getDevicesStatic(int flags) {
        ArrayList<AudioDevicePort> ports = new ArrayList<>();
        if (listAudioDevicePorts(ports) != 0) {
            return new AudioDeviceInfo[0];
        }
        return infoListFromPortList(ports, flags);
    }

    public void registerAudioDeviceCallback(AudioDeviceCallback callback, Handler handler) {
        synchronized (this.mDeviceCallbacks) {
            if (callback != null) {
                if (!this.mDeviceCallbacks.containsKey(callback)) {
                    if (this.mDeviceCallbacks.size() == 0) {
                        if (this.mPortListener == null) {
                            this.mPortListener = new OnAmPortUpdateListener();
                        }
                        registerAudioPortUpdateListener(this.mPortListener);
                    }
                    NativeEventHandlerDelegate delegate = new NativeEventHandlerDelegate(callback, handler);
                    this.mDeviceCallbacks.put(callback, delegate);
                    broadcastDeviceListChange_sync(delegate.getHandler());
                }
            }
        }
    }

    public void unregisterAudioDeviceCallback(AudioDeviceCallback callback) {
        synchronized (this.mDeviceCallbacks) {
            if (this.mDeviceCallbacks.containsKey(callback)) {
                this.mDeviceCallbacks.remove(callback);
                if (this.mDeviceCallbacks.size() == 0) {
                    unregisterAudioPortUpdateListener(this.mPortListener);
                }
            }
        }
    }

    public static void setPortIdForMicrophones(ArrayList<MicrophoneInfo> microphones) {
        AudioDeviceInfo[] devices = getDevicesStatic(1);
        for (int i = microphones.size() - 1; i >= 0; i--) {
            boolean foundPortId = false;
            int length = devices.length;
            int i2 = 0;
            while (true) {
                if (i2 >= length) {
                    break;
                }
                AudioDeviceInfo device = devices[i2];
                if (device.getPort().type() == microphones.get(i).getInternalDeviceType() && TextUtils.equals(device.getAddress(), microphones.get(i).getAddress())) {
                    microphones.get(i).setId(device.getId());
                    foundPortId = true;
                    break;
                }
                i2++;
            }
            if (!foundPortId) {
                Log.i(TAG, "Failed to find port id for device with type:" + microphones.get(i).getType() + " address:" + microphones.get(i).getAddress());
                microphones.remove(i);
            }
        }
    }

    public static MicrophoneInfo microphoneInfoFromAudioDeviceInfo(AudioDeviceInfo deviceInfo) {
        int micLocation;
        int deviceType = deviceInfo.getType();
        if (deviceType == 15 || deviceType == 18) {
            micLocation = 1;
        } else if (deviceType == 0) {
            micLocation = 0;
        } else {
            micLocation = 3;
        }
        MicrophoneInfo microphone = new MicrophoneInfo(deviceInfo.getPort().name() + deviceInfo.getId(), deviceInfo.getPort().type(), deviceInfo.getAddress(), micLocation, -1, -1, MicrophoneInfo.POSITION_UNKNOWN, MicrophoneInfo.ORIENTATION_UNKNOWN, new ArrayList(), new ArrayList(), -3.4028235E38f, -3.4028235E38f, -3.4028235E38f, 0);
        microphone.setId(deviceInfo.getId());
        return microphone;
    }

    private void addMicrophonesFromAudioDeviceInfo(ArrayList<MicrophoneInfo> microphones, HashSet<Integer> filterTypes) {
        AudioDeviceInfo[] devices = getDevicesStatic(1);
        for (AudioDeviceInfo device : devices) {
            if (!filterTypes.contains(Integer.valueOf(device.getType()))) {
                microphones.add(microphoneInfoFromAudioDeviceInfo(device));
            }
        }
    }

    public List<MicrophoneInfo> getMicrophones() throws IOException {
        ArrayList<MicrophoneInfo> microphones = new ArrayList<>();
        int status = AudioSystem.getMicrophones(microphones);
        HashSet<Integer> filterTypes = new HashSet<>();
        filterTypes.add(18);
        if (status != 0) {
            if (status != -3) {
                Log.e(TAG, "getMicrophones failed:" + status);
            }
            Log.i(TAG, "fallback on device info");
            addMicrophonesFromAudioDeviceInfo(microphones, filterTypes);
            return microphones;
        }
        setPortIdForMicrophones(microphones);
        filterTypes.add(15);
        addMicrophonesFromAudioDeviceInfo(microphones, filterTypes);
        return microphones;
    }

    public List<BluetoothCodecConfig> getHwOffloadEncodingFormatsSupportedForA2DP() {
        ArrayList<Integer> formatsList = new ArrayList<>();
        ArrayList<BluetoothCodecConfig> codecConfigList = new ArrayList<>();
        int status = AudioSystem.getHwOffloadEncodingFormatsSupportedForA2DP(formatsList);
        if (status != 0) {
            Log.e(TAG, "getHwOffloadEncodingFormatsSupportedForA2DP failed:" + status);
            return codecConfigList;
        }
        Iterator<Integer> it = formatsList.iterator();
        while (it.hasNext()) {
            int btSourceCodec = AudioSystem.audioFormatToBluetoothSourceCodec(it.next().intValue());
            if (btSourceCodec != 1000000) {
                codecConfigList.add(new BluetoothCodecConfig(btSourceCodec));
            }
        }
        return codecConfigList;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void broadcastDeviceListChange_sync(Handler handler) {
        ArrayList<AudioDevicePort> current_ports = new ArrayList<>();
        if (listAudioDevicePorts(current_ports) == 0) {
            if (handler != null) {
                handler.sendMessage(Message.obtain(handler, 0, infoListFromPortList(current_ports, 3)));
            } else {
                AudioDeviceInfo[] added_devices = calcListDeltas(this.mPreviousPorts, current_ports, 3);
                AudioDeviceInfo[] removed_devices = calcListDeltas(current_ports, this.mPreviousPorts, 3);
                if (!(added_devices.length == 0 && removed_devices.length == 0)) {
                    for (int i = 0; i < this.mDeviceCallbacks.size(); i++) {
                        Handler handler2 = this.mDeviceCallbacks.valueAt(i).getHandler();
                        if (handler2 != null) {
                            if (removed_devices.length != 0) {
                                handler2.sendMessage(Message.obtain(handler2, 2, removed_devices));
                            }
                            if (added_devices.length != 0) {
                                handler2.sendMessage(Message.obtain(handler2, 1, added_devices));
                            }
                        }
                    }
                }
            }
            this.mPreviousPorts = current_ports;
        }
    }

    private class OnAmPortUpdateListener implements OnAudioPortUpdateListener {
        static final String TAG = "OnAmPortUpdateListener";

        private OnAmPortUpdateListener() {
        }

        @Override // android.media.AudioManager.OnAudioPortUpdateListener
        public void onAudioPortListUpdate(AudioPort[] portList) {
            synchronized (AudioManager.this.mDeviceCallbacks) {
                AudioManager.this.broadcastDeviceListChange_sync(null);
            }
        }

        @Override // android.media.AudioManager.OnAudioPortUpdateListener
        public void onAudioPatchListUpdate(AudioPatch[] patchList) {
        }

        @Override // android.media.AudioManager.OnAudioPortUpdateListener
        public void onServiceDied() {
            synchronized (AudioManager.this.mDeviceCallbacks) {
                AudioManager.this.broadcastDeviceListChange_sync(null);
            }
        }
    }

    @SystemApi
    public static abstract class AudioServerStateCallback {
        public void onAudioServerDown() {
        }

        public void onAudioServerUp() {
        }
    }

    @SystemApi
    public void setAudioServerStateCallback(Executor executor, AudioServerStateCallback stateCallback) {
        if (stateCallback == null) {
            throw new IllegalArgumentException("Illegal null AudioServerStateCallback");
        } else if (executor != null) {
            synchronized (this.mAudioServerStateCbLock) {
                if (this.mAudioServerStateCb == null) {
                    try {
                        getService().registerAudioServerStateDispatcher(this.mAudioServerStateDispatcher);
                        this.mAudioServerStateExec = executor;
                        this.mAudioServerStateCb = stateCallback;
                    } catch (RemoteException e) {
                        throw e.rethrowFromSystemServer();
                    }
                } else {
                    throw new IllegalStateException("setAudioServerStateCallback called with already registered callabck");
                }
            }
        } else {
            throw new IllegalArgumentException("Illegal null Executor for the AudioServerStateCallback");
        }
    }

    @SystemApi
    public void clearAudioServerStateCallback() {
        synchronized (this.mAudioServerStateCbLock) {
            if (this.mAudioServerStateCb != null) {
                try {
                    getService().unregisterAudioServerStateDispatcher(this.mAudioServerStateDispatcher);
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
            this.mAudioServerStateExec = null;
            this.mAudioServerStateCb = null;
        }
    }

    @SystemApi
    public boolean isAudioServerRunning() {
        try {
            return getService().isAudioServerRunning();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Map<Integer, Boolean> getSurroundFormats() {
        Map<Integer, Boolean> surroundFormats = new HashMap<>();
        int status = AudioSystem.getSurroundFormats(surroundFormats, false);
        if (status == 0) {
            return surroundFormats;
        }
        Log.e(TAG, "getSurroundFormats failed:" + status);
        return new HashMap();
    }

    public boolean setSurroundFormatEnabled(int audioFormat, boolean enabled) {
        return AudioSystem.setSurroundFormatEnabled(audioFormat, enabled) == 0;
    }

    public Map<Integer, Boolean> getReportedSurroundFormats() {
        Map<Integer, Boolean> reportedSurroundFormats = new HashMap<>();
        int status = AudioSystem.getSurroundFormats(reportedSurroundFormats, true);
        if (status == 0) {
            return reportedSurroundFormats;
        }
        Log.e(TAG, "getReportedSurroundFormats failed:" + status);
        return new HashMap();
    }

    public static boolean isHapticPlaybackSupported() {
        return AudioSystem.isHapticPlaybackSupported();
    }

    @SystemApi
    public static List<AudioProductStrategy> getAudioProductStrategies() {
        try {
            return getService().getAudioProductStrategies();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public static List<AudioVolumeGroup> getAudioVolumeGroups() {
        try {
            return getService().getAudioVolumeGroups();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public static abstract class VolumeGroupCallback {
        public void onAudioVolumeGroupChanged(int group, int flags) {
        }
    }

    @SystemApi
    public void registerVolumeGroupCallback(Executor executor, VolumeGroupCallback callback) {
        Preconditions.checkNotNull(executor, "executor must not be null");
        Preconditions.checkNotNull(callback, "volume group change cb must not be null");
        sAudioAudioVolumeGroupChangedHandler.init();
        sAudioAudioVolumeGroupChangedHandler.registerListener(callback);
    }

    @SystemApi
    public void unregisterVolumeGroupCallback(VolumeGroupCallback callback) {
        Preconditions.checkNotNull(callback, "volume group change cb must not be null");
        sAudioAudioVolumeGroupChangedHandler.unregisterListener(callback);
    }

    public static boolean hasHapticChannels(Uri uri) {
        try {
            return getService().hasHapticChannels(uri);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /* access modifiers changed from: private */
    public class NativeEventHandlerDelegate {
        private final Handler mHandler;

        NativeEventHandlerDelegate(final AudioDeviceCallback callback, Handler handler) {
            Looper looper;
            if (handler != null) {
                looper = handler.getLooper();
            } else {
                looper = Looper.getMainLooper();
            }
            if (looper != null) {
                this.mHandler = new Handler(looper, AudioManager.this) {
                    /* class android.media.AudioManager.NativeEventHandlerDelegate.AnonymousClass1 */

                    @Override // android.os.Handler
                    public void handleMessage(Message msg) {
                        int i = msg.what;
                        if (i == 0 || i == 1) {
                            AudioDeviceCallback audioDeviceCallback = callback;
                            if (audioDeviceCallback != null) {
                                audioDeviceCallback.onAudioDevicesAdded((AudioDeviceInfo[]) msg.obj);
                            }
                        } else if (i != 2) {
                            Log.e(AudioManager.TAG, "Unknown native event type: " + msg.what);
                        } else {
                            AudioDeviceCallback audioDeviceCallback2 = callback;
                            if (audioDeviceCallback2 != null) {
                                audioDeviceCallback2.onAudioDevicesRemoved((AudioDeviceInfo[]) msg.obj);
                            }
                        }
                    }
                };
            } else {
                this.mHandler = null;
            }
        }

        /* access modifiers changed from: package-private */
        public Handler getHandler() {
            return this.mHandler;
        }
    }
}
