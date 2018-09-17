package android.media;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes.Builder;
import android.media.IAudioFocusDispatcher.Stub;
import android.media.audiopolicy.AudioPolicy;
import android.media.audiopolicy.IAudioPolicyCallback;
import android.media.session.MediaSessionLegacyHelper;
import android.net.ProxyInfo;
import android.os.Binder;
import android.os.Build.VERSION;
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
import android.provider.Settings.System;
import android.util.ArrayMap;
import android.util.Jlog;
import android.util.Log;
import android.view.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class AudioManager {
    public static final String ACTION_AUDIO_BECOMING_NOISY = "android.media.AUDIO_BECOMING_NOISY";
    public static final String ACTION_HDMI_AUDIO_PLUG = "android.media.action.HDMI_AUDIO_PLUG";
    public static final String ACTION_HEADSET_PLUG = "android.intent.action.HEADSET_PLUG";
    @Deprecated
    public static final String ACTION_SCO_AUDIO_STATE_CHANGED = "android.media.SCO_AUDIO_STATE_CHANGED";
    public static final String ACTION_SCO_AUDIO_STATE_UPDATED = "android.media.ACTION_SCO_AUDIO_STATE_UPDATED";
    public static final int ADJUST_LOWER = -1;
    public static final int ADJUST_MUTE = -100;
    public static final int ADJUST_RAISE = 1;
    public static final int ADJUST_SAME = 0;
    public static final int ADJUST_TOGGLE_MUTE = 101;
    public static final int ADJUST_UNMUTE = 100;
    public static final int AUDIOFOCUS_FLAGS_APPS = 3;
    public static final int AUDIOFOCUS_FLAGS_SYSTEM = 7;
    public static final int AUDIOFOCUS_FLAG_DELAY_OK = 1;
    public static final int AUDIOFOCUS_FLAG_LOCK = 4;
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
    static final int AUDIOPORT_GENERATION_INIT = 0;
    public static final int AUDIO_DEVICE_IN_USB_DEVICE_EXTENDED = -2013265920;
    public static final int AUDIO_DEVICE_OUT_USB_DEVICE_EXTENDED = 536870912;
    public static final int AUDIO_SESSION_ID_GENERATE = 0;
    private static final boolean DEBUG = false;
    public static final int DEVICE_IN_ANLG_DOCK_HEADSET = -2147483136;
    public static final int DEVICE_IN_BACK_MIC = -2147483520;
    public static final int DEVICE_IN_BLUETOOTH_SCO_HEADSET = -2147483640;
    public static final int DEVICE_IN_BUILTIN_MIC = -2147483644;
    public static final int DEVICE_IN_DGTL_DOCK_HEADSET = -2147482624;
    public static final int DEVICE_IN_FM_TUNER = -2147475456;
    public static final int DEVICE_IN_HDMI = -2147483616;
    public static final int DEVICE_IN_LINE = -2147450880;
    public static final int DEVICE_IN_LOOPBACK = -2147221504;
    public static final int DEVICE_IN_SPDIF = -2147418112;
    public static final int DEVICE_IN_TELEPHONY_RX = -2147483584;
    public static final int DEVICE_IN_TV_TUNER = -2147467264;
    public static final int DEVICE_IN_USB_ACCESSORY = -2147481600;
    public static final int DEVICE_IN_USB_DEVICE = -2147479552;
    public static final int DEVICE_IN_WIRED_HEADSET = -2147483632;
    public static final int DEVICE_NONE = 0;
    public static final int DEVICE_OUT_ANLG_DOCK_HEADSET = 2048;
    public static final int DEVICE_OUT_AUX_DIGITAL = 1024;
    public static final int DEVICE_OUT_BLUETOOTH_A2DP = 128;
    public static final int DEVICE_OUT_BLUETOOTH_A2DP_HEADPHONES = 256;
    public static final int DEVICE_OUT_BLUETOOTH_A2DP_SPEAKER = 512;
    public static final int DEVICE_OUT_BLUETOOTH_SCO = 16;
    public static final int DEVICE_OUT_BLUETOOTH_SCO_CARKIT = 64;
    public static final int DEVICE_OUT_BLUETOOTH_SCO_HEADSET = 32;
    public static final int DEVICE_OUT_DEFAULT = 1073741824;
    public static final int DEVICE_OUT_DGTL_DOCK_HEADSET = 4096;
    public static final int DEVICE_OUT_EARPIECE = 1;
    public static final int DEVICE_OUT_FM = 1048576;
    public static final int DEVICE_OUT_HDMI = 1024;
    public static final int DEVICE_OUT_HDMI_ARC = 262144;
    public static final int DEVICE_OUT_LINE = 131072;
    public static final int DEVICE_OUT_REMOTE_SUBMIX = 32768;
    public static final int DEVICE_OUT_SPDIF = 524288;
    public static final int DEVICE_OUT_SPEAKER = 2;
    public static final int DEVICE_OUT_TELEPHONY_TX = 65536;
    public static final int DEVICE_OUT_USB_ACCESSORY = 8192;
    public static final int DEVICE_OUT_USB_DEVICE = 16384;
    public static final int DEVICE_OUT_WIRED_HEADPHONE = 8;
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
    public static final String EXTRA_VOLUME_STREAM_TYPE = "android.media.EXTRA_VOLUME_STREAM_TYPE";
    public static final String EXTRA_VOLUME_STREAM_TYPE_ALIAS = "android.media.EXTRA_VOLUME_STREAM_TYPE_ALIAS";
    public static final String EXTRA_VOLUME_STREAM_VALUE = "android.media.EXTRA_VOLUME_STREAM_VALUE";
    public static final int FLAG_ACTIVE_MEDIA_ONLY = 512;
    public static final int FLAG_ALLOW_RINGER_MODES = 2;
    public static final int FLAG_BLUETOOTH_ABS_VOLUME = 64;
    public static final int FLAG_FIXED_VOLUME = 32;
    public static final int FLAG_FROM_KEY = 4096;
    public static final int FLAG_HDMI_SYSTEM_AUDIO_VOLUME = 256;
    private static final String[] FLAG_NAMES = new String[]{"FLAG_SHOW_UI", "FLAG_ALLOW_RINGER_MODES", "FLAG_PLAY_SOUND", "FLAG_REMOVE_SOUND_AND_VIBRATE", "FLAG_VIBRATE", "FLAG_FIXED_VOLUME", "FLAG_BLUETOOTH_ABS_VOLUME", "FLAG_SHOW_SILENT_HINT", "FLAG_HDMI_SYSTEM_AUDIO_VOLUME", "FLAG_ACTIVE_MEDIA_ONLY", "FLAG_SHOW_UI_WARNINGS", "FLAG_SHOW_VIBRATE_HINT", "FLAG_FROM_KEY"};
    public static final int FLAG_PLAY_SOUND = 4;
    public static final int FLAG_REMOVE_SOUND_AND_VIBRATE = 8;
    public static final int FLAG_SHOW_SILENT_HINT = 128;
    public static final int FLAG_SHOW_UI = 1;
    public static final int FLAG_SHOW_UI_WARNINGS = 1024;
    public static final int FLAG_SHOW_VIBRATE_HINT = 2048;
    public static final int FLAG_VIBRATE = 16;
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
    public static final int NUM_SOUND_EFFECTS = 10;
    @Deprecated
    public static final int NUM_STREAMS = 5;
    public static final String PROPERTY_OUTPUT_FRAMES_PER_BUFFER = "android.media.property.OUTPUT_FRAMES_PER_BUFFER";
    public static final String PROPERTY_OUTPUT_SAMPLE_RATE = "android.media.property.OUTPUT_SAMPLE_RATE";
    public static final String PROPERTY_SUPPORT_AUDIO_SOURCE_UNPROCESSED = "android.media.property.SUPPORT_AUDIO_SOURCE_UNPROCESSED";
    public static final String PROPERTY_SUPPORT_MIC_NEAR_ULTRASOUND = "android.media.property.SUPPORT_MIC_NEAR_ULTRASOUND";
    public static final String PROPERTY_SUPPORT_SPEAKER_NEAR_ULTRASOUND = "android.media.property.SUPPORT_SPEAKER_NEAR_ULTRASOUND";
    public static final int RECORD_CONFIG_EVENT_START = 1;
    public static final int RECORD_CONFIG_EVENT_STOP = 0;
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
    public static final int STREAM_BLUETOOTH_SCO = 6;
    public static final String STREAM_DEVICES_CHANGED_ACTION = "android.media.STREAM_DEVICES_CHANGED_ACTION";
    public static final int STREAM_DTMF = 8;
    public static final int STREAM_MUSIC = 3;
    public static final String STREAM_MUTE_CHANGED_ACTION = "android.media.STREAM_MUTE_CHANGED_ACTION";
    public static final int STREAM_NOTIFICATION = 5;
    public static final int STREAM_RING = 2;
    public static final int STREAM_SYSTEM = 1;
    public static final int STREAM_SYSTEM_ENFORCED = 7;
    public static final int STREAM_TTS = 9;
    public static final int STREAM_VOICE_CALL = 0;
    public static final int SUCCESS = 0;
    private static final String TAG = "AudioManager";
    public static final int USE_DEFAULT_STREAM_TYPE = Integer.MIN_VALUE;
    public static final String VIBRATE_SETTING_CHANGED_ACTION = "android.media.VIBRATE_SETTING_CHANGED";
    public static final int VIBRATE_SETTING_OFF = 0;
    public static final int VIBRATE_SETTING_ON = 1;
    public static final int VIBRATE_SETTING_ONLY_SILENT = 2;
    public static final int VIBRATE_TYPE_NOTIFICATION = 1;
    public static final int VIBRATE_TYPE_RINGER = 0;
    public static final String VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION";
    private static final String WECHAT_NAME = "com.tencent.mm";
    private static final boolean debug = true;
    static ArrayList<AudioPatch> sAudioPatchesCached = new ArrayList();
    private static final AudioPortEventHandler sAudioPortEventHandler = new AudioPortEventHandler();
    static Integer sAudioPortGeneration = new Integer(0);
    static ArrayList<AudioPort> sAudioPortsCached = new ArrayList();
    static ArrayList<AudioPort> sPreviousAudioPortsCached = new ArrayList();
    private static IAudioService sService;
    private Context mApplicationContext;
    private final IAudioFocusDispatcher mAudioFocusDispatcher;
    private final ConcurrentHashMap<String, FocusRequestInfo> mAudioFocusIdListenerMap;
    private ArrayMap<AudioDeviceCallback, NativeEventHandlerDelegate> mDeviceCallbacks;
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

    public static abstract class AudioPlaybackCallback {
        public void onPlaybackConfigChanged(List<AudioPlaybackConfiguration> list) {
        }
    }

    private static class AudioPlaybackCallbackInfo {
        final AudioPlaybackCallback mCb;
        final Handler mHandler;

        AudioPlaybackCallbackInfo(AudioPlaybackCallback cb, Handler handler) {
            this.mCb = cb;
            this.mHandler = handler;
        }
    }

    public static abstract class AudioRecordingCallback {
        public void onRecordingConfigChanged(List<AudioRecordingConfiguration> list) {
        }
    }

    private static class AudioRecordingCallbackInfo {
        final AudioRecordingCallback mCb;
        final Handler mHandler;

        AudioRecordingCallbackInfo(AudioRecordingCallback cb, Handler handler) {
            this.mCb = cb;
            this.mHandler = handler;
        }
    }

    private static class FocusRequestInfo {
        final Handler mHandler;
        final AudioFocusRequest mRequest;

        FocusRequestInfo(AudioFocusRequest afr, Handler handler) {
            this.mRequest = afr;
            this.mHandler = handler;
        }
    }

    private class NativeEventHandlerDelegate {
        private final Handler mHandler;

        NativeEventHandlerDelegate(final AudioDeviceCallback callback, Handler handler) {
            Looper looper;
            if (handler != null) {
                looper = handler.getLooper();
            } else {
                looper = Looper.getMainLooper();
            }
            if (looper != null) {
                this.mHandler = new Handler(looper) {
                    public void handleMessage(Message msg) {
                        switch (msg.what) {
                            case 0:
                            case 1:
                                if (callback != null) {
                                    callback.onAudioDevicesAdded((AudioDeviceInfo[]) msg.obj);
                                    return;
                                }
                                return;
                            case 2:
                                if (callback != null) {
                                    callback.onAudioDevicesRemoved((AudioDeviceInfo[]) msg.obj);
                                    return;
                                }
                                return;
                            default:
                                Log.e(AudioManager.TAG, "Unknown native event type: " + msg.what);
                                return;
                        }
                    }
                };
            } else {
                this.mHandler = null;
            }
        }

        Handler getHandler() {
            return this.mHandler;
        }
    }

    public interface OnAudioPortUpdateListener {
        void onAudioPatchListUpdate(AudioPatch[] audioPatchArr);

        void onAudioPortListUpdate(AudioPort[] audioPortArr);

        void onServiceDied();
    }

    private class OnAmPortUpdateListener implements OnAudioPortUpdateListener {
        static final String TAG = "OnAmPortUpdateListener";

        /* synthetic */ OnAmPortUpdateListener(AudioManager this$0, OnAmPortUpdateListener -this1) {
            this();
        }

        private OnAmPortUpdateListener() {
        }

        public void onAudioPortListUpdate(AudioPort[] portList) {
            AudioManager.this.broadcastDeviceListChange(null);
        }

        public void onAudioPatchListUpdate(AudioPatch[] patchList) {
        }

        public void onServiceDied() {
            AudioManager.this.broadcastDeviceListChange(null);
        }
    }

    public interface OnAudioFocusChangeListener {
        void onAudioFocusChange(int i);
    }

    private static final class PlaybackConfigChangeCallbackData {
        final AudioPlaybackCallback mCb;
        final List<AudioPlaybackConfiguration> mConfigs;

        PlaybackConfigChangeCallbackData(AudioPlaybackCallback cb, List<AudioPlaybackConfiguration> configs) {
            this.mCb = cb;
            this.mConfigs = configs;
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

    private class ServiceEventHandlerDelegate {
        private final Handler mHandler;

        ServiceEventHandlerDelegate(Handler handler) {
            Looper looper;
            if (handler == null) {
                looper = Looper.myLooper();
                if (looper == null) {
                    looper = Looper.getMainLooper();
                }
            } else {
                looper = handler.getLooper();
            }
            if (looper != null) {
                this.mHandler = new Handler(looper) {
                    public void handleMessage(Message msg) {
                        switch (msg.what) {
                            case 0:
                                FocusRequestInfo fri = AudioManager.this.findFocusRequestInfo((String) msg.obj);
                                if (fri != null) {
                                    OnAudioFocusChangeListener listener = fri.mRequest.getOnAudioFocusChangeListener();
                                    if (listener != null) {
                                        Log.d(AudioManager.TAG, "dispatching onAudioFocusChange(" + msg.arg1 + ") to " + msg.obj);
                                        listener.onAudioFocusChange(msg.arg1);
                                        return;
                                    }
                                    return;
                                }
                                return;
                            case 1:
                                RecordConfigChangeCallbackData cbData = msg.obj;
                                if (cbData.mCb != null) {
                                    cbData.mCb.onRecordingConfigChanged(cbData.mConfigs);
                                    return;
                                }
                                return;
                            case 2:
                                PlaybackConfigChangeCallbackData cbData2 = msg.obj;
                                if (cbData2.mCb != null) {
                                    cbData2.mCb.onPlaybackConfigChanged(cbData2.mConfigs);
                                    return;
                                }
                                return;
                            default:
                                Log.e(AudioManager.TAG, "Unknown event " + msg.what);
                                return;
                        }
                    }
                };
            } else {
                this.mHandler = null;
            }
        }

        Handler getHandler() {
            return this.mHandler;
        }
    }

    public static String flagsToString(int flags) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < FLAG_NAMES.length; i++) {
            int flag = 1 << i;
            if ((flags & flag) != 0) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(FLAG_NAMES[i]);
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

    public AudioManager() {
        this.mAudioFocusIdListenerMap = new ConcurrentHashMap();
        this.mServiceEventHandlerDelegate = new ServiceEventHandlerDelegate(null);
        this.mAudioFocusDispatcher = new Stub() {
            public void dispatchAudioFocusChange(int focusChange, String id) {
                FocusRequestInfo fri = AudioManager.this.findFocusRequestInfo(id);
                if (fri != null && fri.mRequest.getOnAudioFocusChangeListener() != null) {
                    Handler h = fri.mHandler == null ? AudioManager.this.mServiceEventHandlerDelegate.getHandler() : fri.mHandler;
                    h.sendMessage(h.obtainMessage(0, focusChange, 0, id));
                }
            }
        };
        this.mPlaybackCallbackLock = new Object();
        this.mPlayCb = new IPlaybackConfigDispatcher.Stub() {
            public void dispatchPlaybackConfigChange(List<AudioPlaybackConfiguration> configs) {
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
        this.mDeviceCallbacks = new ArrayMap();
        this.mPreviousPorts = new ArrayList();
        this.mUseVolumeKeySounds = true;
        this.mUseFixedVolume = false;
    }

    public AudioManager(Context context) {
        this.mAudioFocusIdListenerMap = new ConcurrentHashMap();
        this.mServiceEventHandlerDelegate = new ServiceEventHandlerDelegate(null);
        this.mAudioFocusDispatcher = /* anonymous class already generated */;
        this.mPlaybackCallbackLock = new Object();
        this.mPlayCb = /* anonymous class already generated */;
        this.mRecordCallbackLock = new Object();
        this.mRecCb = /* anonymous class already generated */;
        this.mICallBack = new Binder();
        this.mPortListener = null;
        this.mDeviceCallbacks = new ArrayMap();
        this.mPreviousPorts = new ArrayList();
        setContext(context);
        this.mUseVolumeKeySounds = getContext().getResources().getBoolean(17957042);
        this.mUseFixedVolume = getContext().getResources().getBoolean(17957039);
    }

    private Context getContext() {
        if (this.mApplicationContext == null) {
            setContext(this.mOriginalContext);
        }
        if (this.mApplicationContext != null) {
            return this.mApplicationContext;
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

    private static IAudioService getService() {
        if (sService != null) {
            return sService;
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
        Log.v(TAG, "adjustStreamVolume streamType: " + streamType + "  direction: " + direction + "  flags: " + flags);
        try {
            getService().adjustStreamVolume(streamType, direction, flags, getContext().getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void adjustVolume(int direction, int flags) {
        Log.v(TAG, "adjustVolume  direction: " + direction + "  flags: " + flags);
        MediaSessionLegacyHelper.getHelper(getContext()).sendAdjustVolumeBy(Integer.MIN_VALUE, direction, flags);
    }

    public void adjustSuggestedStreamVolume(int direction, int suggestedStreamType, int flags) {
        Log.v(TAG, "adjustSuggestedStreamVolume  direction: " + direction + "  suggestedStreamType: " + suggestedStreamType + "  flags: " + flags);
        MediaSessionLegacyHelper.getHelper(getContext()).sendAdjustVolumeBy(suggestedStreamType, direction, flags);
    }

    public void setMasterMute(boolean mute, int flags) {
        Log.v(TAG, "setMasterMute  mute: " + mute + "  flags: " + flags);
        try {
            getService().setMasterMute(mute, flags, getContext().getOpPackageName(), UserHandle.getCallingUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getRingerMode() {
        Log.v(TAG, "getRingerMode...");
        try {
            return getService().getRingerModeExternal();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static boolean isValidRingerMode(int ringerMode) {
        Log.v(TAG, "isValidRingerMode  ringerMode: " + ringerMode);
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
        try {
            return getService().getStreamMaxVolume(streamType);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getStreamMinVolume(int streamType) {
        try {
            return getService().getStreamMinVolume(streamType);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getStreamVolume(int streamType) {
        try {
            return getService().getStreamVolume(streamType);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getLastAudibleStreamVolume(int streamType) {
        try {
            return getService().getLastAudibleStreamVolume(streamType);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getUiSoundsStreamType() {
        Log.v(TAG, "getUiSoundsStreamType...");
        try {
            return getService().getUiSoundsStreamType();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setRingerMode(int ringerMode) {
        Log.v(TAG, "setRingerMode  ringerMode : " + ringerMode);
        if (isValidRingerMode(ringerMode)) {
            try {
                getService().setRingerModeExternal(ringerMode, getContext().getOpPackageName());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void setStreamVolume(int streamType, int index, int flags) {
        Log.v(TAG, "setStreamVolume  streamType : " + streamType + "  index: " + index + "  flags: " + flags);
        try {
            getService().setStreamVolume(streamType, index, flags, getContext().getOpPackageName());
            Log.i(TAG, "setStreamVolume streamType:" + streamType + "index:" + index + "flags:" + flags);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public void setStreamSolo(int streamType, boolean state) {
        Log.v(TAG, "setStreamSolo  streamType: " + streamType + "  state:  " + state);
        Log.w(TAG, "setStreamSolo has been deprecated. Do not use.");
    }

    @Deprecated
    public void setStreamMute(int streamType, boolean state) {
        Log.v(TAG, "setStreamMute  streamType: " + streamType + "  state:  " + state);
        Log.w(TAG, "setStreamMute is deprecated. adjustStreamVolume should be used instead.");
        int direction = state ? -100 : 100;
        if (streamType == Integer.MIN_VALUE) {
            adjustSuggestedStreamVolume(direction, streamType, 0);
            Log.i(TAG, "setStreamMute streamType:" + streamType + "state:" + state);
            return;
        }
        adjustStreamVolume(streamType, direction, 0);
        if (direction == -100 && streamType == 2) {
            HwMediaMonitorManager.writeLogMsg(HwMediaMonitorUtils.LOG_AUDIO_POLICY_VOLUME, 1, 0, "SSM");
        }
    }

    public boolean isStreamMute(int streamType) {
        try {
            return getService().isStreamMute(streamType);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isMasterMute() {
        Log.v(TAG, "isMasterMute...");
        try {
            return getService().isMasterMute();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void forceVolumeControlStream(int streamType) {
        Log.v(TAG, "forceVolumeControlStream   streamType: " + streamType);
        try {
            getService().forceVolumeControlStream(streamType, this.mICallBack);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean shouldVibrate(int vibrateType) {
        Log.v(TAG, "shouldVibrate   vibrateType: " + vibrateType);
        try {
            return getService().shouldVibrate(vibrateType);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getVibrateSetting(int vibrateType) {
        Log.v(TAG, "getVibrateSetting   vibrateType: " + vibrateType);
        try {
            return getService().getVibrateSetting(vibrateType);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setVibrateSetting(int vibrateType, int vibrateSetting) {
        Log.v(TAG, "setVibrateSetting   vibrateType: " + vibrateType + "  vibrateSetting: " + vibrateSetting);
        try {
            getService().setVibrateSetting(vibrateType, vibrateSetting);
            Log.i(TAG, "setVibrateSetting vibrateType:" + vibrateType + "vibrateSetting:" + vibrateSetting);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setSpeakerphoneOn(boolean on) {
        Log.v(TAG, "setSpeakerphoneOn  on: " + on);
        try {
            getService().setSpeakerphoneOn(on);
            Log.i(TAG, "setSpeakerphoneOn on:" + on);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isSpeakerphoneOn() {
        try {
            return getService().isSpeakerphoneOn();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private boolean isWeChat() {
        int pid = Process.myPid();
        String processName = ProxyInfo.LOCAL_EXCL_LIST;
        for (RunningAppProcessInfo process : ((ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE)).getRunningAppProcesses()) {
            if (process.pid == pid) {
                processName = process.processName;
                break;
            }
        }
        Log.d(TAG, "isWeChat(),processName is" + processName);
        if (processName.equals(WECHAT_NAME)) {
            return true;
        }
        return false;
    }

    private boolean isScoAvailableOffCall() {
        Log.v(TAG, "isScoAvailableOffCall");
        boolean result = true;
        IBinder b = ServiceManager.getService("audio");
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken("android.media.IAudioService");
            if (b != null) {
                b.transact(1105, _data, _reply, 0);
            }
            _reply.readException();
            result = _reply.readBoolean();
        } catch (RemoteException e) {
            Log.e(TAG, "transact e: " + e);
        } finally {
            _reply.recycle();
            _data.recycle();
        }
        return result;
    }

    public boolean isBluetoothScoAvailableOffCall() {
        Log.v(TAG, "isBluetoothScoAvailableOffCall...");
        if (isScoAvailableOffCall() || !isWeChat()) {
            return getContext().getResources().getBoolean(17956903);
        }
        Log.v(TAG, "isScoAvailableOffCall...");
        return false;
    }

    public void startBluetoothSco() {
        Log.v(TAG, "startBluetoothSco...");
        try {
            getService().startBluetoothSco(this.mICallBack, getContext().getApplicationInfo().targetSdkVersion);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void startBluetoothScoVirtualCall() {
        Log.v(TAG, "startBluetoothScoVirtualCall...");
        try {
            getService().startBluetoothScoVirtualCall(this.mICallBack);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void stopBluetoothSco() {
        Log.v(TAG, "stopBluetoothSco...");
        try {
            getService().stopBluetoothSco(this.mICallBack);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setBluetoothScoOn(boolean on) {
        Log.v(TAG, "setBluetoothScoOn  on: " + on);
        try {
            getService().setBluetoothScoOn(on);
            Log.i(TAG, "setBluetoothScoOn on:" + on);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isBluetoothScoOn() {
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
        if (AudioSystem.getDeviceConnectionState(128, ProxyInfo.LOCAL_EXCL_LIST) == 1 || AudioSystem.getDeviceConnectionState(256, ProxyInfo.LOCAL_EXCL_LIST) == 1 || AudioSystem.getDeviceConnectionState(512, ProxyInfo.LOCAL_EXCL_LIST) == 1) {
            return true;
        }
        return false;
    }

    @Deprecated
    public void setWiredHeadsetOn(boolean on) {
    }

    public boolean isWiredHeadsetOn() {
        if (AudioSystem.getDeviceConnectionState(4, ProxyInfo.LOCAL_EXCL_LIST) == 0 && AudioSystem.getDeviceConnectionState(8, ProxyInfo.LOCAL_EXCL_LIST) == 0 && AudioSystem.getDeviceConnectionState(16384, ProxyInfo.LOCAL_EXCL_LIST) == 0 && AudioSystem.getDeviceConnectionState(536870912, ProxyInfo.LOCAL_EXCL_LIST) == 0) {
            return false;
        }
        return true;
    }

    public void setMicrophoneMute(boolean on) {
        Log.v(TAG, "setMicrophoneMute  on: " + on);
        try {
            getService().setMicrophoneMute(on, getContext().getOpPackageName(), UserHandle.getCallingUserId());
            Log.v(TAG, "setMicrophoneMute on:" + on);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isMicrophoneMute() {
        return AudioSystem.isMicrophoneMuted();
    }

    public void setMode(int mode) {
        Log.v(TAG, "setMode  mode: " + mode);
        try {
            getService().setMode(mode, this.mICallBack, this.mApplicationContext.getOpPackageName());
            Log.i(TAG, "setMode mode:" + mode);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getMode() {
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
        return AudioSystem.isStreamActive(3, 0);
    }

    public boolean isFMActive() {
        if (1 == AudioSystem.getDeviceConnectionState(1048576, ProxyInfo.LOCAL_EXCL_LIST)) {
            return true;
        }
        return false;
    }

    public boolean isMusicActiveRemotely() {
        Log.v(TAG, "isMusicActiveRemotely...");
        return AudioSystem.isStreamActiveRemotely(3, 0);
    }

    public boolean isAudioFocusExclusive() {
        Log.v(TAG, "isAudioFocusExclusive...");
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
        Log.v(TAG, "setParameter  key: " + key + "  value: " + value);
        setParameters(key + "=" + value);
    }

    public void setParameters(String keyValuePairs) {
        Log.v(TAG, "setParameters  keyValuePairs: " + keyValuePairs);
        AudioSystem.setParameters(keyValuePairs);
        if (keyValuePairs.contains("srs_cfg:trumedia_enable")) {
            HwMediaMonitorManager.writeMediaBigData(Process.myPid(), 20000, TAG);
        }
    }

    public String getParameters(String keys) {
        Log.v(TAG, "getParameters  keys: " + keys);
        return AudioSystem.getParameters(keys);
    }

    private boolean isOnCombineMode() {
        if (!isWiredHeadsetOn() && (isBluetoothA2dpOn() ^ 1) != 0) {
            return false;
        }
        if (AudioSystem.isStreamActive(2, 0) || AudioSystem.isStreamActive(4, 0) || AudioSystem.isStreamActive(5, 0) || AudioSystem.isStreamActive(7, 0)) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Missing block: B:4:0x0020, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void playSoundEffect(int effectType) {
        Log.v(TAG, "playSoundEffect   effectType: " + effectType);
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

    /* JADX WARNING: Missing block: B:3:0x0006, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void playSoundEffect(int effectType, int userId) {
        if (effectType >= 0 && effectType < 10 && querySoundEffectsEnabled(userId)) {
            if (effectType != 0 || !isOnCombineMode()) {
                IAudioService service = getService();
                try {
                    Jlog.d(174, effectType, "playSoundEffect begin");
                    service.playSoundEffect(effectType);
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
        }
    }

    public void playSoundEffect(int effectType, float volume) {
        Log.v(TAG, "playSoundEffect   effectType: " + effectType + "  volume: " + volume);
        if (effectType >= 0 && effectType < 10) {
            try {
                getService().playSoundEffectVolume(effectType, volume);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    private boolean querySoundEffectsEnabled(int user) {
        return System.getIntForUser(getContext().getContentResolver(), "sound_effects_enabled", 0, user) != 0;
    }

    public void loadSoundEffects() {
        Log.v(TAG, "loadSoundEffects...");
        try {
            getService().loadSoundEffects();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void unloadSoundEffects() {
        Log.v(TAG, "unloadSoundEffects...");
        try {
            getService().unloadSoundEffects();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private FocusRequestInfo findFocusRequestInfo(String id) {
        return (FocusRequestInfo) this.mAudioFocusIdListenerMap.get(id);
    }

    private String getIdForAudioFocusListener(OnAudioFocusChangeListener l) {
        if (l == null) {
            return new String(toString());
        }
        return new String(toString() + l.toString());
    }

    public void registerAudioFocusRequest(AudioFocusRequest afr) {
        Handler handler = null;
        Log.v(TAG, "registerAudioFocusListener...");
        Handler h = afr.getOnAudioFocusChangeListenerHandler();
        if (h != null) {
            handler = new ServiceEventHandlerDelegate(h).getHandler();
        }
        FocusRequestInfo fri = new FocusRequestInfo(afr, handler);
        this.mAudioFocusIdListenerMap.put(getIdForAudioFocusListener(afr.getOnAudioFocusChangeListener()), fri);
    }

    public void unregisterAudioFocusRequest(OnAudioFocusChangeListener l) {
        Log.v(TAG, "unregisterAudioFocusListener...");
        this.mAudioFocusIdListenerMap.remove(getIdForAudioFocusListener(l));
    }

    public int requestAudioFocus(OnAudioFocusChangeListener l, int streamType, int durationHint) {
        Log.v(TAG, "requestAudioFocus  streamType: " + streamType + "  durationHint: " + durationHint);
        PlayerBase.deprecateStreamTypeForPlayback(streamType, TAG, "requestAudioFocus()");
        int status = 0;
        try {
            return requestAudioFocus(l, new Builder().setInternalLegacyStreamType(streamType).build(), durationHint, 0);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Audio focus request denied due to ", e);
            return status;
        }
    }

    public int requestAudioFocus(AudioFocusRequest focusRequest) {
        return requestAudioFocus(focusRequest, null);
    }

    public int abandonAudioFocusRequest(AudioFocusRequest focusRequest) {
        if (focusRequest != null) {
            return abandonAudioFocus(focusRequest.getOnAudioFocusChangeListener(), focusRequest.getAudioAttributes());
        }
        throw new IllegalArgumentException("Illegal null AudioFocusRequest");
    }

    public int requestAudioFocus(OnAudioFocusChangeListener l, AudioAttributes requestAttributes, int durationHint, int flags) throws IllegalArgumentException {
        if (flags != (flags & 3)) {
            throw new IllegalArgumentException("Invalid flags 0x" + Integer.toHexString(flags).toUpperCase());
        }
        return requestAudioFocus(l, requestAttributes, durationHint, flags & 3, null);
    }

    public int requestAudioFocus(OnAudioFocusChangeListener l, AudioAttributes requestAttributes, int durationHint, int flags, AudioPolicy ap) throws IllegalArgumentException {
        boolean z = true;
        if (requestAttributes == null) {
            throw new IllegalArgumentException("Illegal null AudioAttributes argument");
        } else if (!AudioFocusRequest.isValidFocusGain(durationHint)) {
            throw new IllegalArgumentException("Invalid duration hint");
        } else if (flags != (flags & 7)) {
            throw new IllegalArgumentException("Illegal flags 0x" + Integer.toHexString(flags).toUpperCase());
        } else if ((flags & 1) == 1 && l == null) {
            throw new IllegalArgumentException("Illegal null focus listener when flagged as accepting delayed focus grant");
        } else if ((flags & 2) == 2 && l == null) {
            throw new IllegalArgumentException("Illegal null focus listener when flagged as pausing instead of ducking");
        } else if ((flags & 4) == 4 && ap == null) {
            throw new IllegalArgumentException("Illegal null audio policy when locking audio focus");
        } else {
            boolean z2;
            AudioFocusRequest.Builder audioAttributes = new AudioFocusRequest.Builder(durationHint).setOnAudioFocusChangeListenerInt(l, null).setAudioAttributes(requestAttributes);
            if ((flags & 1) == 1) {
                z2 = true;
            } else {
                z2 = false;
            }
            audioAttributes = audioAttributes.setAcceptsDelayedFocusGain(z2);
            if ((flags & 2) == 2) {
                z2 = true;
            } else {
                z2 = false;
            }
            AudioFocusRequest.Builder willPauseWhenDucked = audioAttributes.setWillPauseWhenDucked(z2);
            if ((flags & 4) != 4) {
                z = false;
            }
            return requestAudioFocus(willPauseWhenDucked.setLocksFocus(z).build(), ap);
        }
    }

    public int requestAudioFocus(AudioFocusRequest afr, AudioPolicy ap) {
        IAudioPolicyCallback iAudioPolicyCallback = null;
        if (afr == null) {
            throw new NullPointerException("Illegal null AudioFocusRequest");
        } else if (afr.locksFocus() && ap == null) {
            throw new IllegalArgumentException("Illegal null audio policy when locking audio focus");
        } else {
            int sdk;
            registerAudioFocusRequest(afr);
            IAudioService service = getService();
            try {
                sdk = getContext().getApplicationInfo().targetSdkVersion;
            } catch (NullPointerException e) {
                sdk = VERSION.SDK_INT;
            }
            try {
                AudioAttributes audioAttributes = afr.getAudioAttributes();
                int focusGain = afr.getFocusGain();
                IBinder iBinder = this.mICallBack;
                IAudioFocusDispatcher iAudioFocusDispatcher = this.mAudioFocusDispatcher;
                String idForAudioFocusListener = getIdForAudioFocusListener(afr.getOnAudioFocusChangeListener());
                String opPackageName = getContext().getOpPackageName();
                int flags = afr.getFlags();
                if (ap != null) {
                    iAudioPolicyCallback = ap.cb();
                }
                return service.requestAudioFocus(audioAttributes, focusGain, iBinder, iAudioFocusDispatcher, idForAudioFocusListener, opPackageName, flags, iAudioPolicyCallback, sdk);
            } catch (RemoteException e2) {
                throw e2.rethrowFromSystemServer();
            }
        }
    }

    public void requestAudioFocusForCall(int streamType, int durationHint) {
        Log.v(TAG, "requestAudioFocusForCall   streamType: " + streamType + "  durationHint: " + durationHint);
        IAudioService service = getService();
        try {
            service.requestAudioFocus(new Builder().setInternalLegacyStreamType(streamType).build(), durationHint, this.mICallBack, null, AudioSystem.IN_VOICE_COMM_FOCUS_ID, getContext().getOpPackageName(), 4, null, 0);
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

    public int dispatchAudioFocusChange(AudioFocusInfo afi, int focusChange, AudioPolicy ap) {
        if (afi == null) {
            throw new NullPointerException("Illegal null AudioFocusInfo");
        } else if (ap == null) {
            throw new NullPointerException("Illegal null AudioPolicy");
        } else {
            try {
                return getService().dispatchFocusChange(afi, focusChange, ap.cb());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void abandonAudioFocusForCall() {
        Log.v(TAG, "abandonAudioFocusForCall...");
        try {
            getService().abandonAudioFocus(null, AudioSystem.IN_VOICE_COMM_FOCUS_ID, null, getContext().getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int abandonAudioFocus(OnAudioFocusChangeListener l) {
        return abandonAudioFocus(l, null);
    }

    public int abandonAudioFocus(OnAudioFocusChangeListener l, AudioAttributes aa) {
        unregisterAudioFocusRequest(l);
        IAudioService service = getService();
        Log.i(TAG, "abandonAudioFocus");
        try {
            return service.abandonAudioFocus(this.mAudioFocusDispatcher, getIdForAudioFocusListener(l), aa, getContext().getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public void registerMediaButtonEventReceiver(ComponentName eventReceiver) {
        if (eventReceiver == null) {
            Log.e(TAG, "registerMediaButtonEventReceiver ComponentName eventReceiver is null ");
        } else if (eventReceiver.getPackageName().equals(getContext().getPackageName())) {
            Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            mediaButtonIntent.setComponent(eventReceiver);
            registerMediaButtonIntent(PendingIntent.getBroadcast(getContext(), 0, mediaButtonIntent, 0), eventReceiver);
        } else {
            Log.e(TAG, "registerMediaButtonEventReceiver() error: receiver and context package names don't match");
        }
    }

    @Deprecated
    public void registerMediaButtonEventReceiver(PendingIntent eventReceiver) {
        if (eventReceiver == null) {
            Log.e(TAG, "registerMediaButtonEventReceiver PendingIntent eventReceiver is null ");
        } else {
            registerMediaButtonIntent(eventReceiver, null);
        }
    }

    public void registerMediaButtonIntent(PendingIntent pi, ComponentName eventReceiver) {
        if (pi == null) {
            Log.e(TAG, "Cannot call registerMediaButtonIntent() with a null parameter");
            return;
        }
        Log.v(TAG, "registerMediaButtonIntent  pi: " + pi.toString() + "  eventReceiver: " + eventReceiver);
        MediaSessionLegacyHelper.getHelper(getContext()).addMediaButtonListener(pi, eventReceiver, getContext());
    }

    @Deprecated
    public void unregisterMediaButtonEventReceiver(ComponentName eventReceiver) {
        if (eventReceiver == null) {
            Log.e(TAG, "unregisterMediaButtonEventReceiver ComponentName eventReceiver is null ");
            return;
        }
        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setComponent(eventReceiver);
        unregisterMediaButtonIntent(PendingIntent.getBroadcast(getContext(), 0, mediaButtonIntent, 0));
    }

    @Deprecated
    public void unregisterMediaButtonEventReceiver(PendingIntent eventReceiver) {
        if (eventReceiver == null) {
            Log.e(TAG, "unregisterMediaButtonEventReceiver PendingIntent eventReceiver is null ");
        } else {
            unregisterMediaButtonIntent(eventReceiver);
        }
    }

    public void unregisterMediaButtonIntent(PendingIntent pi) {
        Log.v(TAG, "unregisterMediaButtonIntent  pi: " + pi.toString());
        MediaSessionLegacyHelper.getHelper(getContext()).removeMediaButtonListener(pi);
    }

    @Deprecated
    public void registerRemoteControlClient(RemoteControlClient rcClient) {
        if (rcClient == null || rcClient.getRcMediaIntent() == null) {
            Log.e(TAG, "registerRemoteControlClient rcClient or getRcMediaIntent is null ");
        } else {
            rcClient.registerWithSession(MediaSessionLegacyHelper.getHelper(getContext()));
        }
    }

    @Deprecated
    public void unregisterRemoteControlClient(RemoteControlClient rcClient) {
        if (rcClient == null || rcClient.getRcMediaIntent() == null) {
            Log.e(TAG, "unregisterRemoteControlClient rcClient or getRcMediaIntent is null ");
        } else {
            rcClient.unregisterWithSession(MediaSessionLegacyHelper.getHelper(getContext()));
        }
    }

    @Deprecated
    public boolean registerRemoteController(RemoteController rctlr) {
        if (rctlr == null) {
            Log.e(TAG, "registerRemoteController rctlr is null ");
            return false;
        }
        rctlr.startListeningToSessions();
        return true;
    }

    @Deprecated
    public void unregisterRemoteController(RemoteController rctlr) {
        if (rctlr == null) {
            Log.e(TAG, "unregisterRemoteController rctlr is null ");
        } else {
            rctlr.stopListeningToSessions();
        }
    }

    public int registerAudioPolicy(AudioPolicy policy) {
        if (policy == null) {
            throw new IllegalArgumentException("Illegal null AudioPolicy argument");
        }
        Log.v(TAG, "registerAudioPolicy...");
        try {
            String regId = getService().registerAudioPolicy(policy.getConfig(), policy.cb(), policy.hasFocusListener(), policy.isFocusPolicy());
            if (regId == null) {
                return -1;
            }
            policy.setRegistration(regId);
            return 0;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void unregisterAudioPolicyAsync(AudioPolicy policy) {
        if (policy == null) {
            throw new IllegalArgumentException("Illegal null AudioPolicy argument");
        }
        Log.v(TAG, "unregisterAudioPolicyAsync...");
        try {
            getService().unregisterAudioPolicyAsync(policy.cb());
            policy.setRegistration(null);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void registerAudioPlaybackCallback(AudioPlaybackCallback cb, Handler handler) {
        if (cb == null) {
            throw new IllegalArgumentException("Illegal null AudioPlaybackCallback argument");
        }
        synchronized (this.mPlaybackCallbackLock) {
            if (this.mPlaybackCallbackList == null) {
                this.mPlaybackCallbackList = new ArrayList();
            }
            int oldCbCount = this.mPlaybackCallbackList.size();
            if (hasPlaybackCallback_sync(cb)) {
                Log.w(TAG, "attempt to call registerAudioPlaybackCallback() on a previouslyregistered callback");
            } else {
                this.mPlaybackCallbackList.add(new AudioPlaybackCallbackInfo(cb, new ServiceEventHandlerDelegate(handler).getHandler()));
                int newCbCount = this.mPlaybackCallbackList.size();
                if (oldCbCount == 0 && newCbCount > 0) {
                    try {
                        getService().registerPlaybackCallback(this.mPlayCb);
                    } catch (RemoteException e) {
                        throw e.rethrowFromSystemServer();
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:20:0x003d, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void unregisterAudioPlaybackCallback(AudioPlaybackCallback cb) {
        if (cb == null) {
            throw new IllegalArgumentException("Illegal null AudioPlaybackCallback argument");
        }
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
            }
            Log.w(TAG, "attempt to call unregisterAudioPlaybackCallback() on a callback already unregistered or never registered");
        }
    }

    public List<AudioPlaybackConfiguration> getActivePlaybackConfigurations() {
        try {
            return getService().getActivePlaybackConfigurations();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private boolean hasPlaybackCallback_sync(AudioPlaybackCallback cb) {
        if (this.mPlaybackCallbackList != null) {
            for (int i = 0; i < this.mPlaybackCallbackList.size(); i++) {
                if (cb.equals(((AudioPlaybackCallbackInfo) this.mPlaybackCallbackList.get(i)).mCb)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean removePlaybackCallback_sync(AudioPlaybackCallback cb) {
        if (this.mPlaybackCallbackList != null) {
            for (int i = 0; i < this.mPlaybackCallbackList.size(); i++) {
                if (cb.equals(((AudioPlaybackCallbackInfo) this.mPlaybackCallbackList.get(i)).mCb)) {
                    this.mPlaybackCallbackList.remove(i);
                    return true;
                }
            }
        }
        return false;
    }

    public void registerAudioRecordingCallback(AudioRecordingCallback cb, Handler handler) {
        if (cb == null) {
            throw new IllegalArgumentException("Illegal null AudioRecordingCallback argument");
        }
        synchronized (this.mRecordCallbackLock) {
            if (this.mRecordCallbackList == null) {
                this.mRecordCallbackList = new ArrayList();
            }
            int oldCbCount = this.mRecordCallbackList.size();
            if (hasRecordCallback_sync(cb)) {
                Log.w(TAG, "attempt to call registerAudioRecordingCallback() on a previouslyregistered callback");
            } else {
                this.mRecordCallbackList.add(new AudioRecordingCallbackInfo(cb, new ServiceEventHandlerDelegate(handler).getHandler()));
                int newCbCount = this.mRecordCallbackList.size();
                if (oldCbCount == 0 && newCbCount > 0) {
                    try {
                        getService().registerRecordingCallback(this.mRecCb);
                    } catch (RemoteException e) {
                        throw e.rethrowFromSystemServer();
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:20:0x0034, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void unregisterAudioRecordingCallback(AudioRecordingCallback cb) {
        if (cb == null) {
            throw new IllegalArgumentException("Illegal null AudioRecordingCallback argument");
        }
        synchronized (this.mRecordCallbackLock) {
            if (this.mRecordCallbackList == null) {
                return;
            }
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
            }
            Log.w(TAG, "attempt to call unregisterAudioRecordingCallback() on a callback already unregistered or never registered");
        }
    }

    public List<AudioRecordingConfiguration> getActiveRecordingConfigurations() {
        try {
            return getService().getActiveRecordingConfigurations();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private boolean hasRecordCallback_sync(AudioRecordingCallback cb) {
        if (this.mRecordCallbackList != null) {
            for (int i = 0; i < this.mRecordCallbackList.size(); i++) {
                if (cb.equals(((AudioRecordingCallbackInfo) this.mRecordCallbackList.get(i)).mCb)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean removeRecordCallback_sync(AudioRecordingCallback cb) {
        if (this.mRecordCallbackList != null) {
            for (int i = 0; i < this.mRecordCallbackList.size(); i++) {
                if (cb.equals(((AudioRecordingCallbackInfo) this.mRecordCallbackList.get(i)).mCb)) {
                    this.mRecordCallbackList.remove(i);
                    return true;
                }
            }
        }
        return false;
    }

    public void reloadAudioSettings() {
        Log.v(TAG, "reloadAudioSettings...");
        try {
            getService().reloadAudioSettings();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void avrcpSupportsAbsoluteVolume(String address, boolean support) {
        Log.v(TAG, "avrcpSupportsAbsoluteVolume  support: " + support);
        try {
            getService().avrcpSupportsAbsoluteVolume(address, support);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isSilentMode() {
        int ringerMode = getRingerMode();
        Log.v(TAG, "isSilentMode  ringerMode: " + ringerMode);
        if (ringerMode != 0) {
            return ringerMode == 1;
        } else {
            return true;
        }
    }

    public static boolean isOutputDevice(int device) {
        return (Integer.MIN_VALUE & device) == 0;
    }

    public static boolean isInputDevice(int device) {
        return (device & Integer.MIN_VALUE) == Integer.MIN_VALUE;
    }

    public int getDevicesForStream(int streamType) {
        switch (streamType) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 8:
            case 10:
                return AudioSystem.getDevicesForStream(streamType);
            default:
                return 0;
        }
    }

    public void setWiredDeviceConnectionState(int type, int state, String name) {
        Log.v(TAG, "setWiredDeviceConnectionState  type: " + type + "  state: " + state);
        setWiredDeviceConnectionState(type, state, ProxyInfo.LOCAL_EXCL_LIST, name);
    }

    public void setWiredDeviceConnectionState(int type, int state, String address, String name) {
        Log.v(TAG, "setWiredDeviceConnectionState  type: " + type + "  state: " + state);
        IAudioService service = getService();
        try {
            String packageName = this.mApplicationContext.getOpPackageName();
            service.setWiredDeviceConnectionState(type, state, address, name, packageName);
            if (4 == type) {
                service.setWiredDeviceConnectionState(-2147483632, state, address, name, packageName);
            }
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int setBluetoothA2dpDeviceConnectionState(BluetoothDevice device, int state, int profile) {
        Log.v(TAG, "setBluetoothA2dpDeviceConnectionState  state: " + state + ", profile:" + profile);
        try {
            return getService().setBluetoothA2dpDeviceConnectionState(device, state, profile);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void handleBluetoothA2dpDeviceConfigChange(BluetoothDevice device) {
        try {
            getService().handleBluetoothA2dpDeviceConfigChange(device);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public IRingtonePlayer getRingtonePlayer() {
        try {
            return getService().getRingtonePlayer();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String getProperty(String key) {
        String str = null;
        Log.v(TAG, "getProperty  key: " + key);
        if (PROPERTY_OUTPUT_SAMPLE_RATE.equals(key)) {
            int outputSampleRate = AudioSystem.getPrimaryOutputSamplingRate();
            if (outputSampleRate > 0) {
                str = Integer.toString(outputSampleRate);
            }
            return str;
        } else if (PROPERTY_OUTPUT_FRAMES_PER_BUFFER.equals(key)) {
            int outputFramesPerBuffer = AudioSystem.getPrimaryOutputFrameCount();
            if (outputFramesPerBuffer > 0) {
                str = Integer.toString(outputFramesPerBuffer);
            }
            return str;
        } else if (PROPERTY_SUPPORT_MIC_NEAR_ULTRASOUND.equals(key)) {
            return String.valueOf(getContext().getResources().getBoolean(17957020));
        } else {
            if (PROPERTY_SUPPORT_SPEAKER_NEAR_ULTRASOUND.equals(key)) {
                return String.valueOf(getContext().getResources().getBoolean(17957022));
            }
            if (PROPERTY_SUPPORT_AUDIO_SOURCE_UNPROCESSED.equals(key)) {
                return String.valueOf(getContext().getResources().getBoolean(17957016));
            }
            return null;
        }
    }

    public int getOutputLatency(int streamType) {
        Log.v(TAG, "getOutputLatency  streamType: " + streamType);
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

    public void setRingerModeInternal(int ringerMode) {
        try {
            getService().setRingerModeInternal(ringerMode, getContext().getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

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

    public boolean isHdmiSystemAudioSupported() {
        try {
            return getService().isHdmiSystemAudioSupported();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

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
        ArrayList<AudioPort> ports = new ArrayList();
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
        ArrayList<AudioPort> ports = new ArrayList();
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

    public static int createAudioPatch(AudioPatch[] patch, AudioPortConfig[] sources, AudioPortConfig[] sinks) {
        return AudioSystem.createAudioPatch(patch, sources, sinks);
    }

    public static int releaseAudioPatch(AudioPatch patch) {
        return AudioSystem.releaseAudioPatch(patch);
    }

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

    public void registerAudioPortUpdateListener(OnAudioPortUpdateListener l) {
        sAudioPortEventHandler.init();
        sAudioPortEventHandler.registerListener(l);
    }

    public void unregisterAudioPortUpdateListener(OnAudioPortUpdateListener l) {
        sAudioPortEventHandler.unregisterListener(l);
    }

    static int resetAudioPortGeneration() {
        int generation;
        synchronized (sAudioPortGeneration) {
            generation = sAudioPortGeneration.intValue();
            sAudioPortGeneration = Integer.valueOf(0);
        }
        return generation;
    }

    /* JADX WARNING: Missing block: B:64:0x0125, code:
            return 0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static int updateAudioPortCache(ArrayList<AudioPort> ports, ArrayList<AudioPatch> patches, ArrayList<AudioPort> previousPorts) {
        sAudioPortEventHandler.init();
        synchronized (sAudioPortGeneration) {
            if (sAudioPortGeneration.intValue() == 0) {
                int[] patchGeneration = new int[1];
                int[] portGeneration = new int[1];
                ArrayList<AudioPort> newPorts = new ArrayList();
                ArrayList<AudioPatch> newPatches = new ArrayList();
                do {
                    newPorts.clear();
                    int status = AudioSystem.listAudioPorts(newPorts, portGeneration);
                    if (status != 0) {
                        Log.w(TAG, "updateAudioPortCache: listAudioPorts failed");
                        return status;
                    }
                    newPatches.clear();
                    status = AudioSystem.listAudioPatches(newPatches, patchGeneration);
                    if (status != 0) {
                        Log.w(TAG, "updateAudioPortCache: listAudioPatches failed");
                        return status;
                    }
                } while (patchGeneration[0] != portGeneration[0]);
                for (int i = 0; i < newPatches.size(); i++) {
                    int j;
                    for (j = 0; j < ((AudioPatch) newPatches.get(i)).sources().length; j++) {
                        ((AudioPatch) newPatches.get(i)).sources()[j] = updatePortConfig(((AudioPatch) newPatches.get(i)).sources()[j], newPorts);
                    }
                    for (j = 0; j < ((AudioPatch) newPatches.get(i)).sinks().length; j++) {
                        ((AudioPatch) newPatches.get(i)).sinks()[j] = updatePortConfig(((AudioPatch) newPatches.get(i)).sinks()[j], newPorts);
                    }
                }
                Iterator<AudioPatch> i2 = newPatches.iterator();
                while (i2.hasNext()) {
                    AudioPatch newPatch = (AudioPatch) i2.next();
                    boolean hasInvalidPort = false;
                    for (AudioPortConfig portCfg : newPatch.sources()) {
                        if (portCfg == null) {
                            hasInvalidPort = true;
                            break;
                        }
                    }
                    for (AudioPortConfig portCfg2 : newPatch.sinks()) {
                        if (portCfg2 == null) {
                            hasInvalidPort = true;
                            break;
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
        }
    }

    static AudioPortConfig updatePortConfig(AudioPortConfig portCfg, ArrayList<AudioPort> ports) {
        AudioPort port = portCfg.port();
        int k = 0;
        while (k < ports.size()) {
            if (((AudioPort) ports.get(k)).handle().equals(port.handle())) {
                port = (AudioPort) ports.get(k);
                break;
            }
            k++;
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
        if (AudioDeviceInfo.convertInternalDeviceToDeviceType(port.type()) == 0 || port.type() == -2147483520) {
            return false;
        }
        return true;
    }

    public AudioDeviceInfo[] getDevices(int flags) {
        return getDevicesStatic(flags);
    }

    private static AudioDeviceInfo[] infoListFromPortList(ArrayList<AudioDevicePort> ports, int flags) {
        int numRecs = 0;
        for (AudioDevicePort port : ports) {
            if (checkTypes(port) && checkFlags(port, flags)) {
                numRecs++;
            }
        }
        AudioDeviceInfo[] deviceList = new AudioDeviceInfo[numRecs];
        int slot = 0;
        for (AudioDevicePort port2 : ports) {
            if (checkTypes(port2) && checkFlags(port2, flags)) {
                int slot2 = slot + 1;
                deviceList[slot] = new AudioDeviceInfo(port2);
                slot = slot2;
            }
        }
        return deviceList;
    }

    private static AudioDeviceInfo[] calcListDeltas(ArrayList<AudioDevicePort> ports_A, ArrayList<AudioDevicePort> ports_B, int flags) {
        ArrayList<AudioDevicePort> delta_ports = new ArrayList();
        for (int cur_index = 0; cur_index < ports_B.size(); cur_index++) {
            boolean cur_port_found = false;
            AudioDevicePort cur_port = (AudioDevicePort) ports_B.get(cur_index);
            for (int prev_index = 0; prev_index < ports_A.size() && (cur_port_found ^ 1) != 0; prev_index++) {
                cur_port_found = cur_port.id() == ((AudioDevicePort) ports_A.get(prev_index)).id();
            }
            if (!cur_port_found) {
                delta_ports.add(cur_port);
            }
        }
        return infoListFromPortList(delta_ports, flags);
    }

    public static AudioDeviceInfo[] getDevicesStatic(int flags) {
        ArrayList<AudioDevicePort> ports = new ArrayList();
        if (listAudioDevicePorts(ports) != 0) {
            return new AudioDeviceInfo[0];
        }
        return infoListFromPortList(ports, flags);
    }

    public void registerAudioDeviceCallback(AudioDeviceCallback callback, Handler handler) {
        synchronized (this.mDeviceCallbacks) {
            if (callback != null) {
                if ((this.mDeviceCallbacks.containsKey(callback) ^ 1) != 0) {
                    if (this.mDeviceCallbacks.size() == 0) {
                        if (this.mPortListener == null) {
                            this.mPortListener = new OnAmPortUpdateListener(this, null);
                        }
                        registerAudioPortUpdateListener(this.mPortListener);
                    }
                    NativeEventHandlerDelegate delegate = new NativeEventHandlerDelegate(callback, handler);
                    this.mDeviceCallbacks.put(callback, delegate);
                    broadcastDeviceListChange(delegate.getHandler());
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

    private void broadcastDeviceListChange(Handler handler) {
        ArrayList<AudioDevicePort> current_ports = new ArrayList();
        if (listAudioDevicePorts(current_ports) == 0) {
            if (handler != null) {
                handler.sendMessage(Message.obtain(handler, 0, infoListFromPortList(current_ports, 3)));
            } else {
                AudioDeviceInfo[] added_devices = calcListDeltas(this.mPreviousPorts, current_ports, 3);
                AudioDeviceInfo[] removed_devices = calcListDeltas(current_ports, this.mPreviousPorts, 3);
                if (!(added_devices.length == 0 && removed_devices.length == 0)) {
                    synchronized (this.mDeviceCallbacks) {
                        for (int i = 0; i < this.mDeviceCallbacks.size(); i++) {
                            handler = ((NativeEventHandlerDelegate) this.mDeviceCallbacks.valueAt(i)).getHandler();
                            if (handler != null) {
                                if (added_devices.length != 0) {
                                    handler.sendMessage(Message.obtain(handler, 1, added_devices));
                                }
                                if (removed_devices.length != 0) {
                                    handler.sendMessage(Message.obtain(handler, 2, removed_devices));
                                }
                            }
                        }
                    }
                }
            }
            this.mPreviousPorts = current_ports;
        }
    }
}
