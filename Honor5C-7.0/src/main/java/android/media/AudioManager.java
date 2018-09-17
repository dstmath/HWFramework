package android.media;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAssignedNumbers;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes.Builder;
import android.media.IAudioFocusDispatcher.Stub;
import android.media.audiopolicy.AudioPolicy;
import android.media.session.MediaSessionLegacyHelper;
import android.net.ProxyInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings.System;
import android.rms.HwSysResource;
import android.rms.iaware.Events;
import android.util.ArrayMap;
import android.util.Jlog;
import android.util.Log;
import android.view.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
    public static final int AUDIO_SESSION_ID_GENERATE = 0;
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
    private static final String[] FLAG_NAMES = null;
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
    private static String TAG = null;
    public static final int USE_DEFAULT_STREAM_TYPE = Integer.MIN_VALUE;
    public static final String VIBRATE_SETTING_CHANGED_ACTION = "android.media.VIBRATE_SETTING_CHANGED";
    public static final int VIBRATE_SETTING_OFF = 0;
    public static final int VIBRATE_SETTING_ON = 1;
    public static final int VIBRATE_SETTING_ONLY_SILENT = 2;
    public static final int VIBRATE_TYPE_NOTIFICATION = 1;
    public static final int VIBRATE_TYPE_RINGER = 0;
    public static final String VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION";
    private static final boolean debug = true;
    static ArrayList<AudioPatch> sAudioPatchesCached;
    private static final AudioPortEventHandler sAudioPortEventHandler = null;
    static Integer sAudioPortGeneration;
    static ArrayList<AudioPort> sAudioPortsCached;
    static ArrayList<AudioPort> sPreviousAudioPortsCached;
    private static IAudioService sService;
    private Context mApplicationContext;
    private final IAudioFocusDispatcher mAudioFocusDispatcher;
    private final HashMap<String, OnAudioFocusChangeListener> mAudioFocusIdListenerMap;
    private ArrayMap<AudioDeviceCallback, NativeEventHandlerDelegate> mDeviceCallbacks;
    private final Object mFocusListenerLock;
    private final IBinder mICallBack;
    private Context mOriginalContext;
    private OnAmPortUpdateListener mPortListener;
    private ArrayList<AudioDevicePort> mPreviousPorts;
    private final IRecordingConfigDispatcher mRecCb;
    private List<AudioRecordingCallbackInfo> mRecordCallbackList;
    private final Object mRecordCallbackLock;
    private final ServiceEventHandlerDelegate mServiceEventHandlerDelegate;
    private final boolean mUseFixedVolume;
    private final boolean mUseVolumeKeySounds;
    private long mVolumeKeyUpTime;

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

    private class NativeEventHandlerDelegate {
        private final Handler mHandler;

        /* renamed from: android.media.AudioManager.NativeEventHandlerDelegate.1 */
        class AnonymousClass1 extends Handler {
            final /* synthetic */ AudioDeviceCallback val$callback;

            AnonymousClass1(Looper $anonymous0, AudioDeviceCallback val$callback) {
                this.val$callback = val$callback;
                super($anonymous0);
            }

            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case AudioManager.VIBRATE_TYPE_RINGER /*0*/:
                    case AudioManager.VIBRATE_TYPE_NOTIFICATION /*1*/:
                        if (this.val$callback != null) {
                            this.val$callback.onAudioDevicesAdded((AudioDeviceInfo[]) msg.obj);
                        }
                    case AudioManager.VIBRATE_SETTING_ONLY_SILENT /*2*/:
                        if (this.val$callback != null) {
                            this.val$callback.onAudioDevicesRemoved((AudioDeviceInfo[]) msg.obj);
                        }
                    default:
                        Log.e(AudioManager.TAG, "Unknown native event type: " + msg.what);
                }
            }
        }

        NativeEventHandlerDelegate(AudioDeviceCallback callback, Handler handler) {
            Looper looper;
            if (handler != null) {
                looper = handler.getLooper();
            } else {
                looper = Looper.getMainLooper();
            }
            if (looper != null) {
                this.mHandler = new AnonymousClass1(looper, callback);
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

        /* renamed from: android.media.AudioManager.ServiceEventHandlerDelegate.1 */
        class AnonymousClass1 extends Handler {
            AnonymousClass1(Looper $anonymous0) {
                super($anonymous0);
            }

            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case AudioManager.VIBRATE_TYPE_RINGER /*0*/:
                        OnAudioFocusChangeListener listener;
                        synchronized (AudioManager.this.mFocusListenerLock) {
                            listener = AudioManager.this.findFocusListener((String) msg.obj);
                            break;
                        }
                        if (listener != null) {
                            Log.d(AudioManager.TAG, "AudioManager dispatching onAudioFocusChange(" + msg.arg1 + ") for " + msg.obj);
                            listener.onAudioFocusChange(msg.arg1);
                        }
                    case AudioManager.VIBRATE_TYPE_NOTIFICATION /*1*/:
                        RecordConfigChangeCallbackData cbData = msg.obj;
                        if (cbData.mCb != null) {
                            cbData.mCb.onRecordingConfigChanged(cbData.mConfigs);
                        }
                    default:
                        Log.e(AudioManager.TAG, "Unknown event " + msg.what);
                }
            }
        }

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
                this.mHandler = new AnonymousClass1(looper);
            } else {
                this.mHandler = null;
            }
        }

        Handler getHandler() {
            return this.mHandler;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.AudioManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.AudioManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.media.AudioManager.<clinit>():void");
    }

    public static java.lang.String flagsToString(int r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.AudioManager.flagsToString(int):java.lang.String
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
        throw new UnsupportedOperationException("Method not decompiled: android.media.AudioManager.flagsToString(int):java.lang.String");
    }

    public AudioManager(Context context) {
        this.mAudioFocusIdListenerMap = new HashMap();
        this.mFocusListenerLock = new Object();
        this.mServiceEventHandlerDelegate = new ServiceEventHandlerDelegate(null);
        this.mAudioFocusDispatcher = new Stub() {
            public void dispatchAudioFocusChange(int focusChange, String id) {
                AudioManager.this.mServiceEventHandlerDelegate.getHandler().sendMessage(AudioManager.this.mServiceEventHandlerDelegate.getHandler().obtainMessage(AudioManager.VIBRATE_TYPE_RINGER, focusChange, AudioManager.VIBRATE_TYPE_RINGER, id));
            }
        };
        this.mRecordCallbackLock = new Object();
        this.mRecCb = new IRecordingConfigDispatcher.Stub() {
            public void dispatchRecordingConfigChange(List<AudioRecordingConfiguration> configs) {
                synchronized (AudioManager.this.mRecordCallbackLock) {
                    if (AudioManager.this.mRecordCallbackList != null) {
                        for (int i = AudioManager.VIBRATE_TYPE_RINGER; i < AudioManager.this.mRecordCallbackList.size(); i += AudioManager.VIBRATE_TYPE_NOTIFICATION) {
                            AudioRecordingCallbackInfo arci = (AudioRecordingCallbackInfo) AudioManager.this.mRecordCallbackList.get(i);
                            if (arci.mHandler != null) {
                                arci.mHandler.sendMessage(arci.mHandler.obtainMessage(AudioManager.VIBRATE_TYPE_NOTIFICATION, new RecordConfigChangeCallbackData(arci.mCb, configs)));
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
        setContext(context);
        this.mUseVolumeKeySounds = getContext().getResources().getBoolean(17956878);
        this.mUseFixedVolume = getContext().getResources().getBoolean(17956995);
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
        sService = IAudioService.Stub.asInterface(ServiceManager.getService(Context.AUDIO_SERVICE));
        return sService;
    }

    public void dispatchMediaKeyEvent(KeyEvent keyEvent) {
        MediaSessionLegacyHelper.getHelper(getContext()).sendMediaButtonEvent(keyEvent, false);
    }

    public void preDispatchKeyEvent(KeyEvent event, int stream) {
        int keyCode = event.getKeyCode();
        if (keyCode != 25 && keyCode != 24 && keyCode != BluetoothAssignedNumbers.LINAK && this.mVolumeKeyUpTime + 300 > SystemClock.uptimeMillis()) {
            adjustSuggestedStreamVolume(VIBRATE_TYPE_RINGER, stream, STREAM_DTMF);
        }
    }

    public void handleKeyDown(KeyEvent event, int stream) {
        int keyCode = event.getKeyCode();
        Log.v(TAG, "handleKeyDown keycode: " + keyCode + "  stream: " + stream);
        switch (keyCode) {
            case HwSysResource.ANR /*24*/:
            case HwSysResource.DELAY /*25*/:
                int i;
                if (keyCode == 24) {
                    i = VIBRATE_TYPE_NOTIFICATION;
                } else {
                    i = SCO_AUDIO_STATE_ERROR;
                }
                adjustSuggestedStreamVolume(i, stream, 17);
            case BluetoothAssignedNumbers.LINAK /*164*/:
                if (event.getRepeatCount() == 0) {
                    MediaSessionLegacyHelper.getHelper(getContext()).sendVolumeKeyEvent(event, false);
                }
            default:
        }
    }

    public void handleKeyUp(KeyEvent event, int stream) {
        int keyCode = event.getKeyCode();
        Log.v(TAG, "handleKeyUp keycode: " + keyCode + "  stream: " + stream);
        switch (keyCode) {
            case HwSysResource.ANR /*24*/:
            case HwSysResource.DELAY /*25*/:
                if (this.mUseVolumeKeySounds) {
                    adjustSuggestedStreamVolume(VIBRATE_TYPE_RINGER, stream, STREAM_ALARM);
                }
                this.mVolumeKeyUpTime = SystemClock.uptimeMillis();
            case BluetoothAssignedNumbers.LINAK /*164*/:
                MediaSessionLegacyHelper.getHelper(getContext()).sendVolumeKeyEvent(event, false);
            default:
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
        MediaSessionLegacyHelper.getHelper(getContext()).sendAdjustVolumeBy(USE_DEFAULT_STREAM_TYPE, direction, flags);
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
        if (ringerMode < 0 || ringerMode > VIBRATE_SETTING_ONLY_SILENT) {
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
        try {
            return getService().getStreamMinVolume(streamType);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getStreamVolume(int streamType) {
        Log.v(TAG, "getStreamVolume  treamType: " + streamType);
        try {
            return getService().getStreamVolume(streamType);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getLastAudibleStreamVolume(int streamType) {
        Log.v(TAG, "getLastAudibleStreamVolume  treamType: " + streamType);
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
        int direction = state ? ADJUST_MUTE : ADJUST_UNMUTE;
        if (streamType == USE_DEFAULT_STREAM_TYPE) {
            adjustSuggestedStreamVolume(direction, streamType, VIBRATE_TYPE_RINGER);
            Log.i(TAG, "setStreamMute streamType:" + streamType + "state:" + state);
            return;
        }
        adjustStreamVolume(streamType, direction, VIBRATE_TYPE_RINGER);
        if (direction == ADJUST_MUTE && streamType == VIBRATE_SETTING_ONLY_SILENT) {
            HwMediaMonitorManager.writeLogMsg(VIBRATE_TYPE_NOTIFICATION, VIBRATE_SETTING_ONLY_SILENT, getContext().getOpPackageName() + ":sSM");
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
        Log.v(TAG, "isSpeakerphoneOn...");
        try {
            return getService().isSpeakerphoneOn();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isBluetoothScoAvailableOffCall() {
        Log.v(TAG, "isBluetoothScoAvailableOffCall...");
        return getContext().getResources().getBoolean(17956951);
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
        if (AudioSystem.getDeviceConnectionState(FLAG_SHOW_SILENT_HINT, ProxyInfo.LOCAL_EXCL_LIST) == VIBRATE_TYPE_NOTIFICATION || AudioSystem.getDeviceConnectionState(FLAG_HDMI_SYSTEM_AUDIO_VOLUME, ProxyInfo.LOCAL_EXCL_LIST) == VIBRATE_TYPE_NOTIFICATION || AudioSystem.getDeviceConnectionState(FLAG_ACTIVE_MEDIA_ONLY, ProxyInfo.LOCAL_EXCL_LIST) == VIBRATE_TYPE_NOTIFICATION) {
            return debug;
        }
        return false;
    }

    @Deprecated
    public void setWiredHeadsetOn(boolean on) {
    }

    public boolean isWiredHeadsetOn() {
        Log.v(TAG, "isWiredHeadsetOn...");
        if (AudioSystem.getDeviceConnectionState(STREAM_ALARM, ProxyInfo.LOCAL_EXCL_LIST) == 0 && AudioSystem.getDeviceConnectionState(STREAM_DTMF, ProxyInfo.LOCAL_EXCL_LIST) == 0) {
            return false;
        }
        return debug;
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
        Log.v(TAG, "isMicrophoneMute... ");
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
        return SCO_AUDIO_STATE_ERROR;
    }

    public boolean isMusicActive() {
        Log.v(TAG, "isMusicActive...");
        return AudioSystem.isStreamActive(STREAM_MUSIC, VIBRATE_TYPE_RINGER);
    }

    public boolean isFMActive() {
        if (VIBRATE_TYPE_NOTIFICATION == AudioSystem.getDeviceConnectionState(DEVICE_OUT_FM, ProxyInfo.LOCAL_EXCL_LIST)) {
            return debug;
        }
        return false;
    }

    public boolean isMusicActiveRemotely() {
        Log.v(TAG, "isMusicActiveRemotely...");
        return AudioSystem.isStreamActiveRemotely(STREAM_MUSIC, VIBRATE_TYPE_RINGER);
    }

    public boolean isAudioFocusExclusive() {
        Log.v(TAG, "isAudioFocusExclusive...");
        try {
            return getService().getCurrentAudioFocus() == STREAM_ALARM ? debug : false;
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
        return SCO_AUDIO_STATE_ERROR;
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
            HwMediaMonitorManager.writeMediaBigData(Process.myPid(), Events.EVENT_BASE_MEM, "AudioManager");
        }
    }

    public String getParameters(String keys) {
        Log.v(TAG, "getParameters  keys: " + keys);
        return AudioSystem.getParameters(keys);
    }

    private boolean isOnCombineMode() {
        if (!isWiredHeadsetOn() && !isBluetoothA2dpOn()) {
            return false;
        }
        if (AudioSystem.isStreamActive(VIBRATE_SETTING_ONLY_SILENT, VIBRATE_TYPE_RINGER) || AudioSystem.isStreamActive(STREAM_ALARM, VIBRATE_TYPE_RINGER) || AudioSystem.isStreamActive(STREAM_NOTIFICATION, VIBRATE_TYPE_RINGER) || AudioSystem.isStreamActive(STREAM_SYSTEM_ENFORCED, VIBRATE_TYPE_RINGER)) {
            return debug;
        }
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void playSoundEffect(int effectType) {
        Log.v(TAG, "playSoundEffect   effectType: " + effectType);
        if (effectType >= 0 && effectType < NUM_SOUND_EFFECTS && querySoundEffectsEnabled(Process.myUserHandle().getIdentifier())) {
            IAudioService service = getService();
            try {
                Jlog.d(BluetoothAssignedNumbers.OMEGAWAVE, effectType, "playSoundEffect begin");
                service.playSoundEffect(effectType);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void playSoundEffect(int effectType, int userId) {
        if (effectType >= 0 && effectType < NUM_SOUND_EFFECTS && querySoundEffectsEnabled(userId)) {
            if (effectType != 0 || !isOnCombineMode()) {
                IAudioService service = getService();
                try {
                    Jlog.d(BluetoothAssignedNumbers.OMEGAWAVE, effectType, "playSoundEffect begin");
                    service.playSoundEffect(effectType);
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
        }
    }

    public void playSoundEffect(int effectType, float volume) {
        Log.v(TAG, "playSoundEffect   effectType: " + effectType + "  volume: " + volume);
        if (effectType >= 0 && effectType < NUM_SOUND_EFFECTS) {
            try {
                getService().playSoundEffectVolume(effectType, volume);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    private boolean querySoundEffectsEnabled(int user) {
        Log.v(TAG, "querySoundEffectsEnabled...");
        if (System.getIntForUser(getContext().getContentResolver(), System.SOUND_EFFECTS_ENABLED, VIBRATE_TYPE_RINGER, user) != 0) {
            return debug;
        }
        return false;
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

    private OnAudioFocusChangeListener findFocusListener(String id) {
        return (OnAudioFocusChangeListener) this.mAudioFocusIdListenerMap.get(id);
    }

    private String getIdForAudioFocusListener(OnAudioFocusChangeListener l) {
        if (l == null) {
            return new String(toString());
        }
        return new String(toString() + l.toString());
    }

    public void registerAudioFocusListener(OnAudioFocusChangeListener l) {
        Log.v(TAG, "registerAudioFocusListener...");
        synchronized (this.mFocusListenerLock) {
            if (this.mAudioFocusIdListenerMap.containsKey(getIdForAudioFocusListener(l))) {
                return;
            }
            this.mAudioFocusIdListenerMap.put(getIdForAudioFocusListener(l), l);
        }
    }

    public void unregisterAudioFocusListener(OnAudioFocusChangeListener l) {
        Log.v(TAG, "unregisterAudioFocusListener...");
        synchronized (this.mFocusListenerLock) {
            this.mAudioFocusIdListenerMap.remove(getIdForAudioFocusListener(l));
        }
    }

    public int requestAudioFocus(OnAudioFocusChangeListener l, int streamType, int durationHint) {
        Log.v(TAG, "requestAudioFocus  streamType: " + streamType + "  durationHint: " + durationHint);
        int status = VIBRATE_TYPE_RINGER;
        try {
            status = requestAudioFocus(l, new Builder().setInternalLegacyStreamType(streamType).build(), durationHint, VIBRATE_TYPE_RINGER);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Audio focus request denied due to ", e);
        }
        return status;
    }

    public int requestAudioFocus(OnAudioFocusChangeListener l, AudioAttributes requestAttributes, int durationHint, int flags) throws IllegalArgumentException {
        if (flags != (flags & STREAM_MUSIC)) {
            throw new IllegalArgumentException("Invalid flags 0x" + Integer.toHexString(flags).toUpperCase());
        }
        return requestAudioFocus(l, requestAttributes, durationHint, flags & STREAM_MUSIC, null);
    }

    public int requestAudioFocus(OnAudioFocusChangeListener l, AudioAttributes requestAttributes, int durationHint, int flags, AudioPolicy ap) throws IllegalArgumentException {
        if (requestAttributes == null) {
            throw new IllegalArgumentException("Illegal null AudioAttributes argument");
        } else if (durationHint < VIBRATE_TYPE_NOTIFICATION || durationHint > STREAM_ALARM) {
            throw new IllegalArgumentException("Invalid duration hint");
        } else if (flags != (flags & STREAM_SYSTEM_ENFORCED)) {
            throw new IllegalArgumentException("Illegal flags 0x" + Integer.toHexString(flags).toUpperCase());
        } else if ((flags & VIBRATE_TYPE_NOTIFICATION) == VIBRATE_TYPE_NOTIFICATION && l == null) {
            throw new IllegalArgumentException("Illegal null focus listener when flagged as accepting delayed focus grant");
        } else if ((flags & STREAM_ALARM) == STREAM_ALARM && ap == null) {
            throw new IllegalArgumentException("Illegal null audio policy when locking audio focus");
        } else {
            registerAudioFocusListener(l);
            try {
                return getService().requestAudioFocus(requestAttributes, durationHint, this.mICallBack, this.mAudioFocusDispatcher, getIdForAudioFocusListener(l), getContext().getOpPackageName(), flags, ap != null ? ap.cb() : null);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void requestAudioFocusForCall(int streamType, int durationHint) {
        Log.v(TAG, "requestAudioFocusForCall   streamType: " + streamType + "  durationHint: " + durationHint);
        IAudioService service = getService();
        try {
            service.requestAudioFocus(new Builder().setInternalLegacyStreamType(streamType).build(), durationHint, this.mICallBack, null, AudioSystem.IN_VOICE_COMM_FOCUS_ID, getContext().getOpPackageName(), STREAM_ALARM, null);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void abandonAudioFocusForCall() {
        Log.v(TAG, "abandonAudioFocusForCall...");
        try {
            getService().abandonAudioFocus(null, AudioSystem.IN_VOICE_COMM_FOCUS_ID, null);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int abandonAudioFocus(OnAudioFocusChangeListener l) {
        return abandonAudioFocus(l, null);
    }

    public int abandonAudioFocus(OnAudioFocusChangeListener l, AudioAttributes aa) {
        unregisterAudioFocusListener(l);
        IAudioService service = getService();
        Log.i(TAG, "abandonAudioFocus");
        try {
            return service.abandonAudioFocus(this.mAudioFocusDispatcher, getIdForAudioFocusListener(l), aa);
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
            registerMediaButtonIntent(PendingIntent.getBroadcast(getContext(), VIBRATE_TYPE_RINGER, mediaButtonIntent, VIBRATE_TYPE_RINGER), eventReceiver);
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
        unregisterMediaButtonIntent(PendingIntent.getBroadcast(getContext(), VIBRATE_TYPE_RINGER, mediaButtonIntent, VIBRATE_TYPE_RINGER));
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
        return debug;
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
            String regId = getService().registerAudioPolicy(policy.getConfig(), policy.cb(), policy.hasFocusListener());
            if (regId == null) {
                return SCO_AUDIO_STATE_ERROR;
            }
            policy.setRegistration(regId);
            return VIBRATE_TYPE_RINGER;
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
            for (int i = VIBRATE_TYPE_RINGER; i < this.mRecordCallbackList.size(); i += VIBRATE_TYPE_NOTIFICATION) {
                if (cb.equals(((AudioRecordingCallbackInfo) this.mRecordCallbackList.get(i)).mCb)) {
                    return debug;
                }
            }
        }
        return false;
    }

    private boolean removeRecordCallback_sync(AudioRecordingCallback cb) {
        if (this.mRecordCallbackList != null) {
            for (int i = VIBRATE_TYPE_RINGER; i < this.mRecordCallbackList.size(); i += VIBRATE_TYPE_NOTIFICATION) {
                if (cb.equals(((AudioRecordingCallbackInfo) this.mRecordCallbackList.get(i)).mCb)) {
                    this.mRecordCallbackList.remove(i);
                    return debug;
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
        if (ringerMode == 0 || ringerMode == VIBRATE_TYPE_NOTIFICATION) {
            return debug;
        }
        return false;
    }

    public static boolean isOutputDevice(int device) {
        return (USE_DEFAULT_STREAM_TYPE & device) == 0 ? debug : false;
    }

    public static boolean isInputDevice(int device) {
        return (device & USE_DEFAULT_STREAM_TYPE) == USE_DEFAULT_STREAM_TYPE ? debug : false;
    }

    public int getDevicesForStream(int streamType) {
        Log.v(TAG, "getDevicesForStream  streamType: " + streamType);
        switch (streamType) {
            case VIBRATE_TYPE_RINGER /*0*/:
            case VIBRATE_TYPE_NOTIFICATION /*1*/:
            case VIBRATE_SETTING_ONLY_SILENT /*2*/:
            case STREAM_MUSIC /*3*/:
            case STREAM_ALARM /*4*/:
            case STREAM_NOTIFICATION /*5*/:
            case STREAM_DTMF /*8*/:
                return AudioSystem.getDevicesForStream(streamType);
            default:
                return VIBRATE_TYPE_RINGER;
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
            if (STREAM_ALARM == type) {
                service.setWiredDeviceConnectionState(DEVICE_IN_WIRED_HEADSET, state, address, name, packageName);
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

    public IRingtonePlayer getRingtonePlayer() {
        Log.v(TAG, "getRingtonePlayer...");
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
            return String.valueOf(getContext().getResources().getBoolean(17957030));
        } else {
            if (PROPERTY_SUPPORT_SPEAKER_NEAR_ULTRASOUND.equals(key)) {
                return String.valueOf(getContext().getResources().getBoolean(17957031));
            }
            if (PROPERTY_SUPPORT_AUDIO_SOURCE_UNPROCESSED.equals(key)) {
                return String.valueOf(getContext().getResources().getBoolean(17957032));
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
            return MODE_INVALID;
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
            return MODE_INVALID;
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
        for (int i = VIBRATE_TYPE_RINGER; i < ports.size(); i += VIBRATE_TYPE_NOTIFICATION) {
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
            return MODE_INVALID;
        }
        AudioPortConfig activeConfig = port.activeConfig();
        AudioPortConfig config = new AudioPortConfig(port, activeConfig.samplingRate(), activeConfig.channelMask(), activeConfig.format(), gain);
        config.mConfigMask = STREAM_DTMF;
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
            sAudioPortGeneration = Integer.valueOf(VIBRATE_TYPE_RINGER);
        }
        return generation;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static int updateAudioPortCache(ArrayList<AudioPort> ports, ArrayList<AudioPatch> patches, ArrayList<AudioPort> previousPorts) {
        sAudioPortEventHandler.init();
        synchronized (sAudioPortGeneration) {
            if (sAudioPortGeneration.intValue() == 0) {
                int[] patchGeneration = new int[VIBRATE_TYPE_NOTIFICATION];
                int[] portGeneration = new int[VIBRATE_TYPE_NOTIFICATION];
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
                } while (patchGeneration[VIBRATE_TYPE_RINGER] != portGeneration[VIBRATE_TYPE_RINGER]);
                for (int i = VIBRATE_TYPE_RINGER; i < newPatches.size(); i += VIBRATE_TYPE_NOTIFICATION) {
                    int j;
                    for (j = VIBRATE_TYPE_RINGER; j < ((AudioPatch) newPatches.get(i)).sources().length; j += VIBRATE_TYPE_NOTIFICATION) {
                        ((AudioPatch) newPatches.get(i)).sources()[j] = updatePortConfig(((AudioPatch) newPatches.get(i)).sources()[j], newPorts);
                    }
                    for (j = VIBRATE_TYPE_RINGER; j < ((AudioPatch) newPatches.get(i)).sinks().length; j += VIBRATE_TYPE_NOTIFICATION) {
                        ((AudioPatch) newPatches.get(i)).sinks()[j] = updatePortConfig(((AudioPatch) newPatches.get(i)).sinks()[j], newPorts);
                    }
                }
                Iterator<AudioPatch> i2 = newPatches.iterator();
                while (i2.hasNext()) {
                    int i3;
                    AudioPatch newPatch = (AudioPatch) i2.next();
                    boolean hasInvalidPort = false;
                    AudioPortConfig[] sources = newPatch.sources();
                    int length = sources.length;
                    for (i3 = VIBRATE_TYPE_RINGER; i3 < length; i3 += VIBRATE_TYPE_NOTIFICATION) {
                        if (sources[i3] == null) {
                            hasInvalidPort = debug;
                            break;
                        }
                    }
                    sources = newPatch.sinks();
                    length = sources.length;
                    for (i3 = VIBRATE_TYPE_RINGER; i3 < length; i3 += VIBRATE_TYPE_NOTIFICATION) {
                        if (sources[i3] == null) {
                            hasInvalidPort = debug;
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
                sAudioPortGeneration = Integer.valueOf(portGeneration[VIBRATE_TYPE_RINGER]);
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
            return VIBRATE_TYPE_RINGER;
        }
    }

    static AudioPortConfig updatePortConfig(AudioPortConfig portCfg, ArrayList<AudioPort> ports) {
        AudioPort port = portCfg.port();
        int k = VIBRATE_TYPE_RINGER;
        while (k < ports.size()) {
            if (((AudioPort) ports.get(k)).handle().equals(port.handle())) {
                port = (AudioPort) ports.get(k);
                break;
            }
            k += VIBRATE_TYPE_NOTIFICATION;
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
        if (port.role() == VIBRATE_SETTING_ONLY_SILENT && (flags & VIBRATE_SETTING_ONLY_SILENT) != 0) {
            return debug;
        }
        if (port.role() != VIBRATE_TYPE_NOTIFICATION || (flags & VIBRATE_TYPE_NOTIFICATION) == 0) {
            return false;
        }
        return debug;
    }

    private static boolean checkTypes(AudioDevicePort port) {
        if (AudioDeviceInfo.convertInternalDeviceToDeviceType(port.type()) == 0 || port.type() == DEVICE_IN_BACK_MIC) {
            return false;
        }
        return debug;
    }

    public AudioDeviceInfo[] getDevices(int flags) {
        return getDevicesStatic(flags);
    }

    private static AudioDeviceInfo[] infoListFromPortList(ArrayList<AudioDevicePort> ports, int flags) {
        int numRecs = VIBRATE_TYPE_RINGER;
        for (AudioDevicePort port : ports) {
            if (checkTypes(port) && checkFlags(port, flags)) {
                numRecs += VIBRATE_TYPE_NOTIFICATION;
            }
        }
        AudioDeviceInfo[] deviceList = new AudioDeviceInfo[numRecs];
        int slot = VIBRATE_TYPE_RINGER;
        for (AudioDevicePort port2 : ports) {
            if (checkTypes(port2) && checkFlags(port2, flags)) {
                int slot2 = slot + VIBRATE_TYPE_NOTIFICATION;
                deviceList[slot] = new AudioDeviceInfo(port2);
                slot = slot2;
            }
        }
        return deviceList;
    }

    private static AudioDeviceInfo[] calcListDeltas(ArrayList<AudioDevicePort> ports_A, ArrayList<AudioDevicePort> ports_B, int flags) {
        ArrayList<AudioDevicePort> delta_ports = new ArrayList();
        for (int cur_index = VIBRATE_TYPE_RINGER; cur_index < ports_B.size(); cur_index += VIBRATE_TYPE_NOTIFICATION) {
            boolean cur_port_found = false;
            AudioDevicePort cur_port = (AudioDevicePort) ports_B.get(cur_index);
            for (int prev_index = VIBRATE_TYPE_RINGER; prev_index < ports_A.size() && !cur_port_found; prev_index += VIBRATE_TYPE_NOTIFICATION) {
                cur_port_found = cur_port.id() == ((AudioDevicePort) ports_A.get(prev_index)).id() ? debug : false;
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
            return new AudioDeviceInfo[VIBRATE_TYPE_RINGER];
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void broadcastDeviceListChange(Handler handler) {
        ArrayList<AudioDevicePort> current_ports = new ArrayList();
        if (listAudioDevicePorts(current_ports) == 0) {
            if (handler != null) {
                handler.sendMessage(Message.obtain(handler, VIBRATE_TYPE_RINGER, infoListFromPortList(current_ports, STREAM_MUSIC)));
            } else {
                AudioDeviceInfo[] added_devices = calcListDeltas(this.mPreviousPorts, current_ports, STREAM_MUSIC);
                AudioDeviceInfo[] removed_devices = calcListDeltas(current_ports, this.mPreviousPorts, STREAM_MUSIC);
                if (!(added_devices.length == 0 && removed_devices.length == 0)) {
                    synchronized (this.mDeviceCallbacks) {
                        int i = VIBRATE_TYPE_RINGER;
                        while (true) {
                            if (i >= this.mDeviceCallbacks.size()) {
                                break;
                            }
                            handler = ((NativeEventHandlerDelegate) this.mDeviceCallbacks.valueAt(i)).getHandler();
                            if (handler != null) {
                                if (added_devices.length != 0) {
                                    handler.sendMessage(Message.obtain(handler, VIBRATE_TYPE_NOTIFICATION, added_devices));
                                }
                                if (removed_devices.length != 0) {
                                    handler.sendMessage(Message.obtain(handler, VIBRATE_SETTING_ONLY_SILENT, removed_devices));
                                }
                            }
                            i += VIBRATE_TYPE_NOTIFICATION;
                        }
                    }
                }
            }
            this.mPreviousPorts = current_ports;
        }
    }
}
