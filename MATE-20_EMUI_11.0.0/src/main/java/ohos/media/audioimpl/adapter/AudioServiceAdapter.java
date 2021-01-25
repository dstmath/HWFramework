package ohos.media.audioimpl.adapter;

import android.app.ActivityThread;
import android.content.res.Resources;
import android.media.AudioAttributes;
import android.media.AudioDeviceInfo;
import android.media.AudioDevicePort;
import android.media.AudioManager;
import android.media.AudioPlaybackConfiguration;
import android.media.AudioRecordingConfiguration;
import android.media.IAudioFocusDispatcher;
import android.media.IAudioService;
import android.media.IPlaybackConfigDispatcher;
import android.media.IRecordingConfigDispatcher;
import android.media.audiopolicy.IAudioPolicyCallback;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import ohos.app.Context;
import ohos.media.audio.AudioCapturerCallback;
import ohos.media.audio.AudioCapturerConfig;
import ohos.media.audio.AudioDeviceDescriptor;
import ohos.media.audio.AudioInterrupt;
import ohos.media.audio.AudioRendererCallback;
import ohos.media.audio.AudioRendererInfo;
import ohos.media.audio.AudioStreamInfo;
import ohos.media.audiofwk.IAudioInterruptProxy;
import ohos.media.audioimpl.adapter.AudioServiceAdapter;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class AudioServiceAdapter implements IAudioInterruptProxy {
    private static final int ADJUST_MUTE = -100;
    private static final int ADJUST_UNMUTE = 100;
    private static final String AUDIO_SERVICE = "audio";
    private static final AudioStreamInfo.EncodingFormat[] ENCODING_MATCH_TABLE = {AudioStreamInfo.EncodingFormat.ENCODING_INVALID, AudioStreamInfo.EncodingFormat.ENCODING_DEFAULT, AudioStreamInfo.EncodingFormat.ENCODING_PCM_16BIT, AudioStreamInfo.EncodingFormat.ENCODING_PCM_8BIT, AudioStreamInfo.EncodingFormat.ENCODING_PCM_FLOAT, AudioStreamInfo.EncodingFormat.ENCODING_INVALID, AudioStreamInfo.EncodingFormat.ENCODING_INVALID, AudioStreamInfo.EncodingFormat.ENCODING_INVALID, AudioStreamInfo.EncodingFormat.ENCODING_INVALID, AudioStreamInfo.EncodingFormat.ENCODING_MP3};
    private static final int FLAG_DEFAULT = 0;
    private static final int FLAG_PLAY_SOUND = 4;
    private static final int FLAG_SHOW_UI = 1;
    private static final String GET_AUDIO_SERVICE_FAILED = "get IAudioService failed.";
    private static final Object GET_DEVICE_LOCK = new Object();
    private static final String INVALID_CONTEXT = "invalid context";
    private static final Logger LOGGER = LoggerFactory.getAudioLogger(AudioServiceAdapter.class);
    private static final int PER_USER_RANGE = 100000;
    private static final int SDK_VERSION = Build.VERSION.SDK_INT;
    private static final int WAIT_EXTERNAL_POLICY_TIME = 500;
    private static volatile IAudioService audioService;
    private static List<AudioDevicePort> cachedDevicePorts = new ArrayList();
    private static AudioDeviceDescriptor[] cachedInputDeviceDescriptors = new AudioDeviceDescriptor[0];
    private static AudioDeviceDescriptor[] cachedOutputDeviceDescriptors = new AudioDeviceDescriptor[0];
    private final IBinder binder;
    private final IRecordingConfigDispatcher capturerCallbackDispatcher;
    private List<AudioCapturerCallback> capturerCallbackList;
    private final Object capturerCallbackLock;
    private final IAudioFocusDispatcher dispatcher;
    private final Object focusLock;
    private IAudioInterruptProxy.AudioInterruptObserver interruptObserver;
    private boolean isCapturerCbRegistered;
    private boolean isRendererCbRegistered;
    private final String packageName;
    private final IPlaybackConfigDispatcher rendererCallbackDispatcher;
    private List<AudioRendererCallback> rendererCallbackList;
    private final Object rendererCallbackLock;
    private final Map<String, WaitingRequestObject> waitingRequestMap;

    public AudioServiceAdapter(String str) {
        this.binder = new Binder();
        this.capturerCallbackLock = new Object();
        this.rendererCallbackLock = new Object();
        this.focusLock = new Object();
        this.isRendererCbRegistered = false;
        this.isCapturerCbRegistered = false;
        this.waitingRequestMap = new ConcurrentHashMap();
        this.dispatcher = new IAudioFocusDispatcher.Stub() {
            /* class ohos.media.audioimpl.adapter.AudioServiceAdapter.AnonymousClass1 */

            public void dispatchAudioFocusChange(int i, String str) {
                int i2;
                int i3 = 1;
                AudioServiceAdapter.LOGGER.debug("dispatchAudioFocusChange focusChange = %{public}d, clientId = %{public}s", Integer.valueOf(i), str);
                if (AudioServiceAdapter.this.interruptObserver == null) {
                    AudioServiceAdapter.LOGGER.error("dispatchAudioFocusChange interruptObserver is null.", new Object[0]);
                    return;
                }
                if (i == -3) {
                    i2 = 4;
                } else if (i == -2) {
                    i2 = 2;
                } else if (i != -1) {
                    if (!(i == 1 || i == 2)) {
                        if (i == 3) {
                            i2 = 5;
                            i3 = 2;
                        } else if (i != 4) {
                            i2 = 0;
                        }
                    }
                    i2 = 1;
                    i3 = 2;
                } else {
                    i2 = 3;
                }
                AudioServiceAdapter.this.interruptObserver.onInterrupt(str, i3, i2);
            }

            public void dispatchFocusResultFromExtPolicy(int i, String str) {
                AudioServiceAdapter.LOGGER.debug("dispatchFocusResultFromExtPolicy requestResult = %{public}d, clientId = %{public}s", Integer.valueOf(i), str);
                WaitingRequestObject waitingRequestObject = (WaitingRequestObject) AudioServiceAdapter.this.waitingRequestMap.remove(str);
                if (waitingRequestObject == null) {
                    AudioServiceAdapter.LOGGER.error("dispatchFocusResultFromExtPolicy cannot find %{public}s", str);
                } else {
                    waitingRequestObject.setResult(i);
                }
            }
        };
        this.capturerCallbackDispatcher = new IRecordingConfigDispatcher.Stub() {
            /* class ohos.media.audioimpl.adapter.AudioServiceAdapter.AnonymousClass2 */

            public void dispatchRecordingConfigChange(List<AudioRecordingConfiguration> list) {
                synchronized (AudioServiceAdapter.this.capturerCallbackLock) {
                    if (list != null) {
                        ArrayList arrayList = new ArrayList();
                        list.forEach(new Consumer(arrayList) {
                            /* class ohos.media.audioimpl.adapter.$$Lambda$AudioServiceAdapter$2$WPNG74XI64Ty3PgdVQKlJ5wLsZU */
                            private final /* synthetic */ List f$0;

                            {
                                this.f$0 = r1;
                            }

                            @Override // java.util.function.Consumer
                            public final void accept(Object obj) {
                                AudioServiceAdapter.AnonymousClass2.lambda$dispatchRecordingConfigChange$0(this.f$0, (AudioRecordingConfiguration) obj);
                            }
                        });
                        AudioServiceAdapter.this.capturerCallbackList.forEach(new Consumer(arrayList) {
                            /* class ohos.media.audioimpl.adapter.$$Lambda$AudioServiceAdapter$2$gpdRyrcd13wLtUSvWJ75SU1SnjI */
                            private final /* synthetic */ List f$0;

                            {
                                this.f$0 = r1;
                            }

                            @Override // java.util.function.Consumer
                            public final void accept(Object obj) {
                                ((AudioCapturerCallback) obj).onCapturerConfigChanged(this.f$0);
                            }
                        });
                    }
                }
            }

            static /* synthetic */ void lambda$dispatchRecordingConfigChange$0(List list, AudioRecordingConfiguration audioRecordingConfiguration) {
                list.add(AudioServiceAdapter.convertAudioCapturerConfig(audioRecordingConfiguration));
            }
        };
        this.rendererCallbackDispatcher = new IPlaybackConfigDispatcher.Stub() {
            /* class ohos.media.audioimpl.adapter.AudioServiceAdapter.AnonymousClass3 */

            public void dispatchPlaybackConfigChange(List<AudioPlaybackConfiguration> list, boolean z) {
                if (list != null) {
                    AudioServiceAdapter.LOGGER.debug("[dispatchPlaybackConfigChange] begin config size = %{public}s", Integer.valueOf(list.size()));
                    if (z) {
                        Binder.flushPendingCommands();
                    }
                    synchronized (AudioServiceAdapter.this.rendererCallbackLock) {
                        ArrayList arrayList = new ArrayList();
                        list.forEach(new Consumer(arrayList) {
                            /* class ohos.media.audioimpl.adapter.$$Lambda$AudioServiceAdapter$3$KsRjPHXceKqO4W4fLkhE2DbkTkM */
                            private final /* synthetic */ List f$0;

                            {
                                this.f$0 = r1;
                            }

                            @Override // java.util.function.Consumer
                            public final void accept(Object obj) {
                                AudioServiceAdapter.AnonymousClass3.lambda$dispatchPlaybackConfigChange$0(this.f$0, (AudioPlaybackConfiguration) obj);
                            }
                        });
                        AudioServiceAdapter.this.rendererCallbackList.forEach(new Consumer(arrayList) {
                            /* class ohos.media.audioimpl.adapter.$$Lambda$AudioServiceAdapter$3$QcvvneztVkGXUBM0k4tGSsgIiJ4 */
                            private final /* synthetic */ List f$0;

                            {
                                this.f$0 = r1;
                            }

                            @Override // java.util.function.Consumer
                            public final void accept(Object obj) {
                                ((AudioRendererCallback) obj).onRendererConfigChanged(this.f$0);
                            }
                        });
                    }
                    AudioServiceAdapter.LOGGER.debug("[dispatchPlaybackConfigChange] end", new Object[0]);
                }
            }

            static /* synthetic */ void lambda$dispatchPlaybackConfigChange$0(List list, AudioPlaybackConfiguration audioPlaybackConfiguration) {
                list.add(AudioServiceAdapter.convertAudioRendererInfo(audioPlaybackConfiguration));
            }
        };
        this.packageName = str;
    }

    public AudioServiceAdapter() {
        this.binder = new Binder();
        this.capturerCallbackLock = new Object();
        this.rendererCallbackLock = new Object();
        this.focusLock = new Object();
        this.isRendererCbRegistered = false;
        this.isCapturerCbRegistered = false;
        this.waitingRequestMap = new ConcurrentHashMap();
        this.dispatcher = new IAudioFocusDispatcher.Stub() {
            /* class ohos.media.audioimpl.adapter.AudioServiceAdapter.AnonymousClass1 */

            public void dispatchAudioFocusChange(int i, String str) {
                int i2;
                int i3 = 1;
                AudioServiceAdapter.LOGGER.debug("dispatchAudioFocusChange focusChange = %{public}d, clientId = %{public}s", Integer.valueOf(i), str);
                if (AudioServiceAdapter.this.interruptObserver == null) {
                    AudioServiceAdapter.LOGGER.error("dispatchAudioFocusChange interruptObserver is null.", new Object[0]);
                    return;
                }
                if (i == -3) {
                    i2 = 4;
                } else if (i == -2) {
                    i2 = 2;
                } else if (i != -1) {
                    if (!(i == 1 || i == 2)) {
                        if (i == 3) {
                            i2 = 5;
                            i3 = 2;
                        } else if (i != 4) {
                            i2 = 0;
                        }
                    }
                    i2 = 1;
                    i3 = 2;
                } else {
                    i2 = 3;
                }
                AudioServiceAdapter.this.interruptObserver.onInterrupt(str, i3, i2);
            }

            public void dispatchFocusResultFromExtPolicy(int i, String str) {
                AudioServiceAdapter.LOGGER.debug("dispatchFocusResultFromExtPolicy requestResult = %{public}d, clientId = %{public}s", Integer.valueOf(i), str);
                WaitingRequestObject waitingRequestObject = (WaitingRequestObject) AudioServiceAdapter.this.waitingRequestMap.remove(str);
                if (waitingRequestObject == null) {
                    AudioServiceAdapter.LOGGER.error("dispatchFocusResultFromExtPolicy cannot find %{public}s", str);
                } else {
                    waitingRequestObject.setResult(i);
                }
            }
        };
        this.capturerCallbackDispatcher = new IRecordingConfigDispatcher.Stub() {
            /* class ohos.media.audioimpl.adapter.AudioServiceAdapter.AnonymousClass2 */

            public void dispatchRecordingConfigChange(List<AudioRecordingConfiguration> list) {
                synchronized (AudioServiceAdapter.this.capturerCallbackLock) {
                    if (list != null) {
                        ArrayList arrayList = new ArrayList();
                        list.forEach(new Consumer(arrayList) {
                            /* class ohos.media.audioimpl.adapter.$$Lambda$AudioServiceAdapter$2$WPNG74XI64Ty3PgdVQKlJ5wLsZU */
                            private final /* synthetic */ List f$0;

                            {
                                this.f$0 = r1;
                            }

                            @Override // java.util.function.Consumer
                            public final void accept(Object obj) {
                                AudioServiceAdapter.AnonymousClass2.lambda$dispatchRecordingConfigChange$0(this.f$0, (AudioRecordingConfiguration) obj);
                            }
                        });
                        AudioServiceAdapter.this.capturerCallbackList.forEach(new Consumer(arrayList) {
                            /* class ohos.media.audioimpl.adapter.$$Lambda$AudioServiceAdapter$2$gpdRyrcd13wLtUSvWJ75SU1SnjI */
                            private final /* synthetic */ List f$0;

                            {
                                this.f$0 = r1;
                            }

                            @Override // java.util.function.Consumer
                            public final void accept(Object obj) {
                                ((AudioCapturerCallback) obj).onCapturerConfigChanged(this.f$0);
                            }
                        });
                    }
                }
            }

            static /* synthetic */ void lambda$dispatchRecordingConfigChange$0(List list, AudioRecordingConfiguration audioRecordingConfiguration) {
                list.add(AudioServiceAdapter.convertAudioCapturerConfig(audioRecordingConfiguration));
            }
        };
        this.rendererCallbackDispatcher = new IPlaybackConfigDispatcher.Stub() {
            /* class ohos.media.audioimpl.adapter.AudioServiceAdapter.AnonymousClass3 */

            public void dispatchPlaybackConfigChange(List<AudioPlaybackConfiguration> list, boolean z) {
                if (list != null) {
                    AudioServiceAdapter.LOGGER.debug("[dispatchPlaybackConfigChange] begin config size = %{public}s", Integer.valueOf(list.size()));
                    if (z) {
                        Binder.flushPendingCommands();
                    }
                    synchronized (AudioServiceAdapter.this.rendererCallbackLock) {
                        ArrayList arrayList = new ArrayList();
                        list.forEach(new Consumer(arrayList) {
                            /* class ohos.media.audioimpl.adapter.$$Lambda$AudioServiceAdapter$3$KsRjPHXceKqO4W4fLkhE2DbkTkM */
                            private final /* synthetic */ List f$0;

                            {
                                this.f$0 = r1;
                            }

                            @Override // java.util.function.Consumer
                            public final void accept(Object obj) {
                                AudioServiceAdapter.AnonymousClass3.lambda$dispatchPlaybackConfigChange$0(this.f$0, (AudioPlaybackConfiguration) obj);
                            }
                        });
                        AudioServiceAdapter.this.rendererCallbackList.forEach(new Consumer(arrayList) {
                            /* class ohos.media.audioimpl.adapter.$$Lambda$AudioServiceAdapter$3$QcvvneztVkGXUBM0k4tGSsgIiJ4 */
                            private final /* synthetic */ List f$0;

                            {
                                this.f$0 = r1;
                            }

                            @Override // java.util.function.Consumer
                            public final void accept(Object obj) {
                                ((AudioRendererCallback) obj).onRendererConfigChanged(this.f$0);
                            }
                        });
                    }
                    AudioServiceAdapter.LOGGER.debug("[dispatchPlaybackConfigChange] end", new Object[0]);
                }
            }

            static /* synthetic */ void lambda$dispatchPlaybackConfigChange$0(List list, AudioPlaybackConfiguration audioPlaybackConfiguration) {
                list.add(AudioServiceAdapter.convertAudioRendererInfo(audioPlaybackConfiguration));
            }
        };
        this.packageName = ActivityThread.currentPackageName();
    }

    public static IAudioService getService() throws AudioRemoteAdapterException {
        if (audioService == null) {
            synchronized (IAudioService.class) {
                if (audioService == null) {
                    audioService = IAudioService.Stub.asInterface(ServiceManager.getService(AUDIO_SERVICE));
                }
            }
        }
        if (audioService != null) {
            return audioService;
        }
        LOGGER.error(GET_AUDIO_SERVICE_FAILED, new Object[0]);
        throw new AudioRemoteAdapterException(GET_AUDIO_SERVICE_FAILED);
    }

    public void playSoundEffect(int i) throws AudioRemoteAdapterException {
        try {
            getService().playSoundEffect(i);
        } catch (RemoteException e) {
            throw new AudioRemoteAdapterException(e.toString());
        }
    }

    public void playSoundEffect(int i, float f) throws AudioRemoteAdapterException {
        try {
            getService().playSoundEffectVolume(i, f);
        } catch (RemoteException e) {
            throw new AudioRemoteAdapterException(e.toString());
        }
    }

    public boolean setVolume(int i, int i2) throws AudioRemoteAdapterException {
        try {
            getService().setStreamVolume(i, i2, 5, this.packageName);
            return true;
        } catch (RemoteException e) {
            throw new AudioRemoteAdapterException(e.toString());
        }
    }

    public int getVolume(int i) throws AudioRemoteAdapterException {
        try {
            return getService().getStreamVolume(i);
        } catch (RemoteException e) {
            throw new AudioRemoteAdapterException(e.toString());
        }
    }

    public int getMinVolume(int i) throws AudioRemoteAdapterException {
        try {
            return getService().getStreamMinVolume(i);
        } catch (RemoteException e) {
            throw new AudioRemoteAdapterException(e.toString());
        }
    }

    public int getMaxVolume(int i) throws AudioRemoteAdapterException {
        try {
            return getService().getStreamMaxVolume(i);
        } catch (RemoteException e) {
            throw new AudioRemoteAdapterException(e.toString());
        }
    }

    public void setMute(int i, boolean z) throws AudioRemoteAdapterException {
        try {
            getService().adjustStreamVolume(i, z ? -100 : 100, 0, this.packageName);
        } catch (RemoteException e) {
            throw new AudioRemoteAdapterException(e.toString());
        }
    }

    public boolean isMute(int i) throws AudioRemoteAdapterException {
        try {
            return getService().isStreamMute(i);
        } catch (RemoteException e) {
            throw new AudioRemoteAdapterException(e.toString());
        }
    }

    public void setRingerMode(int i) throws AudioRemoteAdapterException {
        try {
            if (isValidRingerMode(i)) {
                getService().setRingerModeExternal(i, this.packageName);
                return;
            }
            throw new AudioRemoteAdapterException("setRingerMode error, Invalid mode;");
        } catch (RemoteException e) {
            throw new AudioRemoteAdapterException(e.toString());
        }
    }

    public int getRingerMode() throws AudioRemoteAdapterException {
        try {
            return getService().getRingerModeExternal();
        } catch (RemoteException e) {
            throw new AudioRemoteAdapterException(e.toString());
        }
    }

    public void setSpeakerOn(boolean z) throws AudioRemoteAdapterException {
        try {
            getService().setSpeakerphoneOn(z);
        } catch (RemoteException e) {
            throw new AudioRemoteAdapterException(e.toString());
        }
    }

    public void setBlueToothOn(boolean z) throws AudioRemoteAdapterException {
        try {
            getService().setBluetoothScoOn(z);
        } catch (RemoteException e) {
            throw new AudioRemoteAdapterException(e.toString());
        }
    }

    public boolean isSpeakerOn() throws AudioRemoteAdapterException {
        try {
            return getService().isSpeakerphoneOn();
        } catch (RemoteException e) {
            throw new AudioRemoteAdapterException(e.toString());
        }
    }

    public boolean isBluetoothOn() throws AudioRemoteAdapterException {
        try {
            return getService().isBluetoothScoOn();
        } catch (RemoteException e) {
            throw new AudioRemoteAdapterException(e.toString());
        }
    }

    @Override // ohos.media.audiofwk.IAudioInterruptProxy
    public boolean activateAudioInterrupt(AudioInterrupt audioInterrupt) {
        if (audioInterrupt != null) {
            return requestAudioFocus(audioInterrupt);
        }
        LOGGER.warn("activateAudioInterrupt interrupt is null.", new Object[0]);
        return false;
    }

    @Override // ohos.media.audiofwk.IAudioInterruptProxy
    public boolean deactivateAudioInterrupt(AudioInterrupt audioInterrupt) {
        if (audioInterrupt != null) {
            return abandonAudioFocus(audioInterrupt);
        }
        LOGGER.warn("deactivateAudioInterrupt interrupt is null.", new Object[0]);
        return false;
    }

    @Override // ohos.media.audiofwk.IAudioInterruptProxy
    public void setAudioInterruptObserver(IAudioInterruptProxy.AudioInterruptObserver audioInterruptObserver) {
        this.interruptObserver = audioInterruptObserver;
    }

    private boolean isValidRingerMode(int i) throws AudioRemoteAdapterException {
        try {
            return getService().isValidRingerMode(i);
        } catch (RemoteException e) {
            LOGGER.error("isValidRingerMode remote exception: %{public}s", e.getMessage());
            throw new AudioRemoteAdapterException(e.toString());
        }
    }

    private boolean requestAudioFocus(AudioInterrupt audioInterrupt) {
        int requestAudioFocus;
        try {
            IAudioService service = getService();
            AudioStreamInfo streamInfo = audioInterrupt.getStreamInfo();
            if (streamInfo == null) {
                LOGGER.error("AudioStreamInfo in interrupt is null", new Object[0]);
                return false;
            }
            String obj = audioInterrupt.toString();
            LOGGER.debug("%{public}s requestAudioFocus begin", obj);
            synchronized (this.focusLock) {
                try {
                    requestAudioFocus = service.requestAudioFocus(getAudioAttributes(streamInfo), getAudioFocus(streamInfo), this.binder, this.dispatcher, getClientId(audioInterrupt), this.packageName, 3, (IAudioPolicyCallback) null, SDK_VERSION);
                    LOGGER.debug("%{public}s request focus, status = %{public}d", obj, Integer.valueOf(requestAudioFocus));
                } catch (RemoteException e) {
                    LOGGER.error("requestAudioFocus remote exception: %{public}s", e.getMessage());
                    return false;
                }
            }
            if (requestAudioFocus == 0) {
                LOGGER.warn("%{public}s request focus failed", obj);
                return false;
            } else if (requestAudioFocus == 1) {
                return true;
            } else {
                WaitingRequestObject waitingRequestObject = new WaitingRequestObject(obj);
                this.waitingRequestMap.put(obj, waitingRequestObject);
                waitingRequestObject.waitResult(500);
                this.waitingRequestMap.remove(obj);
                if (waitingRequestObject.getResult() != 1) {
                    LOGGER.debug("%{public}s request focus failed, result = %{public}d", obj, Integer.valueOf(waitingRequestObject.getResult()));
                    return false;
                }
                LOGGER.debug("%{public}s request focus success, result = %{public}d", obj, Integer.valueOf(waitingRequestObject.getResult()));
                return true;
            }
        } catch (AudioRemoteAdapterException unused) {
            LOGGER.error("requestAudioFocus get service failed", new Object[0]);
            return false;
        }
    }

    private boolean abandonAudioFocus(AudioInterrupt audioInterrupt) {
        try {
            IAudioService service = getService();
            AudioStreamInfo streamInfo = audioInterrupt.getStreamInfo();
            if (streamInfo == null) {
                LOGGER.error("AudioStreamInfo in interrupt is null", new Object[0]);
                return false;
            }
            String clientId = getClientId(audioInterrupt);
            LOGGER.debug("%{public}s abandonAudioFocus begin", clientId);
            try {
                if (service.abandonAudioFocus(this.dispatcher, clientId, getAudioAttributes(streamInfo), this.packageName) == 0) {
                    LOGGER.error("abandonAudioFocus failed.", new Object[0]);
                    return false;
                }
                LOGGER.debug("%{public}s abandon focus success", clientId);
                return true;
            } catch (RemoteException e) {
                LOGGER.error("abandonAudioFocus remote exception: %{public}s", e.getMessage());
                return false;
            }
        } catch (AudioRemoteAdapterException unused) {
            LOGGER.error("abandonAudioFocus get service failed", new Object[0]);
            return false;
        }
    }

    private AudioAttributes getAudioAttributes(AudioStreamInfo audioStreamInfo) {
        return new AudioAttributes.Builder().setUsage(convertUsage(audioStreamInfo.getUsage())).setContentType(convertContent(audioStreamInfo.getContentType())).setFlags(convertFlags(audioStreamInfo.getAudioStreamFlag().getValue())).build();
    }

    private int getAudioFocus(AudioStreamInfo audioStreamInfo) {
        AudioStreamInfo.AudioStreamFlag audioStreamFlag = audioStreamInfo.getAudioStreamFlag();
        if ((audioStreamFlag.getValue() & AudioStreamInfo.AudioStreamFlag.AUDIO_STREAM_FLAG_MAY_DUCK.getValue()) != 0) {
            return 3;
        }
        return (audioStreamFlag.getValue() & AudioStreamInfo.AudioStreamFlag.AUDIO_STREAM_FLAG_MAY_RESUME.getValue()) != 0 ? 2 : 1;
    }

    private String getClientId(AudioInterrupt audioInterrupt) {
        return audioInterrupt.toString();
    }

    private int convertUsage(AudioStreamInfo.StreamUsage streamUsage) {
        switch (streamUsage) {
            case STREAM_USAGE_MEDIA:
                return 1;
            case STREAM_USAGE_VOICE_COMMUNICATION:
                return 2;
            case STREAM_USAGE_VOICE_COMMUNICATION_SIGNALLING:
                return 3;
            case STREAM_USAGE_ALARM:
                return 4;
            case STREAM_USAGE_NOTIFICATION:
                return 5;
            case STREAM_USAGE_NOTIFICATION_RINGTONE:
                return 6;
            case STREAM_USAGE_NOTIFICATION_COMMUNICATION_REQUEST:
                return 7;
            case STREAM_USAGE_NOTIFICATION_COMMUNICATION_INSTANT:
                return 8;
            case STREAM_USAGE_NOTIFICATION_COMMUNICATION_DELAYED:
                return 9;
            case STREAM_USAGE_NOTIFICATION_EVENT:
                return 10;
            case STREAM_USAGE_ASSISTANCE_ACCESSIBILITY:
                return 11;
            case STREAM_USAGE_ASSISTANCE_NAVIGATION_GUIDANCE:
                return 12;
            case STREAM_USAGE_ASSISTANCE_SONIFICATION:
                return 13;
            case STREAM_USAGE_GAME:
                return 14;
            case STREAM_USAGE_VIRTUAL_SOURCE:
                return 15;
            case STREAM_USAGE_ASSISTANT:
                return 16;
            case STREAM_USAGE_TTS:
                return 17;
            default:
                return 0;
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.media.audioimpl.adapter.AudioServiceAdapter$4  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass4 {
        static final /* synthetic */ int[] $SwitchMap$ohos$media$audio$AudioStreamInfo$ContentType = new int[AudioStreamInfo.ContentType.values().length];

        static {
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$ContentType[AudioStreamInfo.ContentType.CONTENT_TYPE_SPEECH.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$ContentType[AudioStreamInfo.ContentType.CONTENT_TYPE_MUSIC.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$ContentType[AudioStreamInfo.ContentType.CONTENT_TYPE_MOVIE.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$ContentType[AudioStreamInfo.ContentType.CONTENT_TYPE_SONIFICATION.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            $SwitchMap$ohos$media$audio$AudioStreamInfo$StreamUsage = new int[AudioStreamInfo.StreamUsage.values().length];
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$StreamUsage[AudioStreamInfo.StreamUsage.STREAM_USAGE_MEDIA.ordinal()] = 1;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$StreamUsage[AudioStreamInfo.StreamUsage.STREAM_USAGE_VOICE_COMMUNICATION.ordinal()] = 2;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$StreamUsage[AudioStreamInfo.StreamUsage.STREAM_USAGE_VOICE_COMMUNICATION_SIGNALLING.ordinal()] = 3;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$StreamUsage[AudioStreamInfo.StreamUsage.STREAM_USAGE_ALARM.ordinal()] = 4;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$StreamUsage[AudioStreamInfo.StreamUsage.STREAM_USAGE_NOTIFICATION.ordinal()] = 5;
            } catch (NoSuchFieldError unused9) {
            }
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$StreamUsage[AudioStreamInfo.StreamUsage.STREAM_USAGE_NOTIFICATION_RINGTONE.ordinal()] = 6;
            } catch (NoSuchFieldError unused10) {
            }
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$StreamUsage[AudioStreamInfo.StreamUsage.STREAM_USAGE_NOTIFICATION_COMMUNICATION_REQUEST.ordinal()] = 7;
            } catch (NoSuchFieldError unused11) {
            }
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$StreamUsage[AudioStreamInfo.StreamUsage.STREAM_USAGE_NOTIFICATION_COMMUNICATION_INSTANT.ordinal()] = 8;
            } catch (NoSuchFieldError unused12) {
            }
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$StreamUsage[AudioStreamInfo.StreamUsage.STREAM_USAGE_NOTIFICATION_COMMUNICATION_DELAYED.ordinal()] = 9;
            } catch (NoSuchFieldError unused13) {
            }
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$StreamUsage[AudioStreamInfo.StreamUsage.STREAM_USAGE_NOTIFICATION_EVENT.ordinal()] = 10;
            } catch (NoSuchFieldError unused14) {
            }
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$StreamUsage[AudioStreamInfo.StreamUsage.STREAM_USAGE_ASSISTANCE_ACCESSIBILITY.ordinal()] = 11;
            } catch (NoSuchFieldError unused15) {
            }
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$StreamUsage[AudioStreamInfo.StreamUsage.STREAM_USAGE_ASSISTANCE_NAVIGATION_GUIDANCE.ordinal()] = 12;
            } catch (NoSuchFieldError unused16) {
            }
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$StreamUsage[AudioStreamInfo.StreamUsage.STREAM_USAGE_ASSISTANCE_SONIFICATION.ordinal()] = 13;
            } catch (NoSuchFieldError unused17) {
            }
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$StreamUsage[AudioStreamInfo.StreamUsage.STREAM_USAGE_GAME.ordinal()] = 14;
            } catch (NoSuchFieldError unused18) {
            }
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$StreamUsage[AudioStreamInfo.StreamUsage.STREAM_USAGE_VIRTUAL_SOURCE.ordinal()] = 15;
            } catch (NoSuchFieldError unused19) {
            }
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$StreamUsage[AudioStreamInfo.StreamUsage.STREAM_USAGE_ASSISTANT.ordinal()] = 16;
            } catch (NoSuchFieldError unused20) {
            }
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$StreamUsage[AudioStreamInfo.StreamUsage.STREAM_USAGE_TTS.ordinal()] = 17;
            } catch (NoSuchFieldError unused21) {
            }
        }
    }

    private int convertContent(AudioStreamInfo.ContentType contentType) {
        int i = AnonymousClass4.$SwitchMap$ohos$media$audio$AudioStreamInfo$ContentType[contentType.ordinal()];
        int i2 = 1;
        if (i != 1) {
            i2 = 2;
            if (i != 2) {
                i2 = 3;
                if (i != 3) {
                    i2 = 4;
                    if (i != 4) {
                        return 0;
                    }
                }
            }
        }
        return i2;
    }

    private int convertFlags(int i) {
        int i2 = (AudioStreamInfo.AudioStreamFlag.AUDIO_STREAM_FLAG_AUDIBILITY_ENFORCED.getValue() & i) != 0 ? 1 : 0;
        if ((AudioStreamInfo.AudioStreamFlag.AUDIO_STREAM_FLAG_SECURE.getValue() & i) != 0) {
            i2 |= 2;
        }
        if ((AudioStreamInfo.AudioStreamFlag.AUDIO_STREAM_FLAG_SCO.getValue() & i) != 0) {
            i2 |= 4;
        }
        if ((AudioStreamInfo.AudioStreamFlag.AUDIO_STREAM_FLAG_BEACON.getValue() & i) != 0) {
            i2 |= 8;
        }
        if ((AudioStreamInfo.AudioStreamFlag.AUDIO_STREAM_FLAG_HW_AV_SYNC.getValue() & i) != 0) {
            i2 |= 16;
        }
        if ((AudioStreamInfo.AudioStreamFlag.AUDIO_STREAM_FLAG_HW_HOTWORD.getValue() & i) != 0) {
            i2 |= 32;
        }
        if ((AudioStreamInfo.AudioStreamFlag.AUDIO_STREAM_FLAG_BYPASS_INTERRUPTION_POLICY.getValue() & i) != 0) {
            i2 |= 64;
        }
        if ((AudioStreamInfo.AudioStreamFlag.AUDIO_STREAM_FLAG_BYPASS_MUTE.getValue() & i) != 0) {
            i2 |= 128;
        }
        if ((AudioStreamInfo.AudioStreamFlag.AUDIO_STREAM_FLAG_DEEP_BUFFER.getValue() & i) != 0) {
            i2 |= 512;
        }
        if ((AudioStreamInfo.AudioStreamFlag.AUDIO_STREAM_FLAG_NO_MEDIA_PROJECTION.getValue() & i) != 0) {
            i2 |= 1024;
        }
        if ((AudioStreamInfo.AudioStreamFlag.AUDIO_STREAM_FLAG_MUTE_HAPTIC.getValue() & i) != 0) {
            i2 |= 2048;
        }
        if ((AudioStreamInfo.AudioStreamFlag.AUDIO_STREAM_FLAG_NO_SYSTEM_CAPTURE.getValue() & i) != 0) {
            i2 |= 4096;
        }
        return (i & AudioStreamInfo.AudioStreamFlag.AUDIO_STREAM_FLAG_DIRECT_OUTPUT.getValue()) != 0 ? i2 | 1048576 : i2;
    }

    private int getCallingUserId() {
        return Binder.getCallingUid() / 100000;
    }

    public void setMasterMute(boolean z) throws AudioRemoteAdapterException {
        try {
            getService().setMasterMute(z, 0, this.packageName, getCallingUserId());
        } catch (RemoteException e) {
            throw new AudioRemoteAdapterException(e.toString());
        }
    }

    public void setMicrophoneMute(boolean z) throws AudioRemoteAdapterException {
        try {
            getService().setMicrophoneMute(z, this.packageName, getCallingUserId());
        } catch (RemoteException e) {
            throw new AudioRemoteAdapterException(e.toString());
        }
    }

    /* access modifiers changed from: private */
    public static AudioCapturerConfig convertAudioCapturerConfig(AudioRecordingConfiguration audioRecordingConfiguration) {
        if (audioRecordingConfiguration == null) {
            LOGGER.warn("[convertAudioCapturerConfig] config is null", new Object[0]);
            return null;
        }
        return new AudioCapturerConfig(audioRecordingConfiguration.getClientAudioSessionId(), new AudioStreamInfo.Builder().encodingFormat(AudioStreamInfoAdapter.convertEncodingFormat(audioRecordingConfiguration.getFormat().getEncoding())).channelMask(AudioStreamInfoAdapter.convertChannelMaskIn(audioRecordingConfiguration.getFormat().getChannelMask())).sampleRate(audioRecordingConfiguration.getFormat().getSampleRate()).build(), audioRecordingConfiguration.getClientPackageName(), audioRecordingConfiguration.getClientPortId(), audioRecordingConfiguration.isClientSilenced(), AudioStreamInfoAdapter.convertInputSource(audioRecordingConfiguration.getAudioSource()));
    }

    /* access modifiers changed from: private */
    public static AudioRendererInfo convertAudioRendererInfo(AudioPlaybackConfiguration audioPlaybackConfiguration) {
        if (audioPlaybackConfiguration == null || audioPlaybackConfiguration.getAudioAttributes() == null) {
            LOGGER.warn("[convertAudioRendererInfo] config is null", new Object[0]);
            return null;
        }
        AudioAttributes audioAttributes = audioPlaybackConfiguration.getAudioAttributes();
        AudioStreamInfo build = new AudioStreamInfo.Builder().streamUsage(AudioStreamInfoAdapter.convertUsage(audioAttributes.getUsage())).audioStreamFlag(AudioStreamInfoAdapter.convertFlag(audioAttributes.getFlags())).build();
        if (build.getContentType() != AudioStreamInfoAdapter.convertContentType(audioAttributes.getContentType())) {
            LOGGER.warn("[convertAudioRendererInfo] contentType different size = %{public}s", build.getContentType());
        }
        return new AudioRendererInfo.Builder().audioStreamInfo(build).build();
    }

    public void registerAudioCapturerCallback(AudioCapturerCallback audioCapturerCallback) {
        LOGGER.debug("registerAudioCapturerCallback start.", new Object[0]);
        synchronized (this.capturerCallbackLock) {
            if (this.capturerCallbackList == null) {
                this.capturerCallbackList = new ArrayList();
            }
            if (!this.capturerCallbackList.contains(audioCapturerCallback)) {
                this.capturerCallbackList.add(audioCapturerCallback);
            }
            if (!this.isCapturerCbRegistered) {
                try {
                    getService().registerRecordingCallback(this.capturerCallbackDispatcher);
                    this.isCapturerCbRegistered = true;
                } catch (RemoteException e) {
                    LOGGER.error("registerAudioCapturerCallback remote exception: %{public}s", e.getMessage());
                }
            } else {
                LOGGER.warn("registerAudioCapturerCallback have done before", new Object[0]);
            }
        }
        LOGGER.debug("registerAudioCapturerCallback end.", new Object[0]);
    }

    public void registerAudioRendererCallback(AudioRendererCallback audioRendererCallback) {
        LOGGER.debug("registerAudioRendererCallback start.", new Object[0]);
        synchronized (this.rendererCallbackLock) {
            if (this.rendererCallbackList == null) {
                this.rendererCallbackList = new ArrayList();
            }
            if (!this.rendererCallbackList.contains(audioRendererCallback)) {
                this.rendererCallbackList.add(audioRendererCallback);
            }
            if (!this.isRendererCbRegistered) {
                try {
                    getService().registerPlaybackCallback(this.rendererCallbackDispatcher);
                    this.isRendererCbRegistered = true;
                } catch (RemoteException e) {
                    LOGGER.error("registerAudioRendererCallback remote exception: %{public}s", e.getMessage());
                }
            } else {
                LOGGER.warn("registerAudioRendererCallback have done before", new Object[0]);
            }
        }
        LOGGER.debug("registerAudioRendererCallback end.", new Object[0]);
    }

    public void unregisterAudioCapturerCallback(AudioCapturerCallback audioCapturerCallback) {
        synchronized (this.capturerCallbackLock) {
            if (this.capturerCallbackList != null) {
                if (this.isCapturerCbRegistered) {
                    if (!this.capturerCallbackList.remove(audioCapturerCallback)) {
                        LOGGER.warn("Attempt to unregister an already unregistered or never registered capturer callback.", new Object[0]);
                    }
                    int size = this.capturerCallbackList.size();
                    if (!this.isCapturerCbRegistered || size != 0) {
                        LOGGER.warn("capturerCallbackList not empty, no need to unregister capturer callback.", new Object[0]);
                    } else {
                        try {
                            try {
                                getService().unregisterRecordingCallback(this.capturerCallbackDispatcher);
                                this.isCapturerCbRegistered = false;
                            } catch (RemoteException e) {
                                LOGGER.error("unregisterAudioCapturerCallback remote exception: %{public}s", e.getMessage());
                            }
                        } catch (AudioRemoteAdapterException e2) {
                            LOGGER.error("getService remote exception: %{public}s", e2.getMessage());
                            return;
                        }
                    }
                    return;
                }
            }
            LOGGER.warn("attempt to call unregisterAudioCapturerCallback on a callback that was never registered", new Object[0]);
        }
    }

    public void unregisterAudioRendererCallback(AudioRendererCallback audioRendererCallback) {
        synchronized (this.rendererCallbackLock) {
            if (this.rendererCallbackList != null) {
                if (this.isRendererCbRegistered) {
                    if (!this.rendererCallbackList.remove(audioRendererCallback)) {
                        LOGGER.warn("Attempt to unregister an already unregistered or never registered renderer callback.", new Object[0]);
                    }
                    int size = this.rendererCallbackList.size();
                    if (!this.isRendererCbRegistered || size != 0) {
                        LOGGER.warn("rendererCallbackList not empty, no need to unregister renderer callback.", new Object[0]);
                    } else {
                        try {
                            getService().unregisterPlaybackCallback(this.rendererCallbackDispatcher);
                            this.isRendererCbRegistered = false;
                        } catch (RemoteException e) {
                            LOGGER.error("unregisterAudioRendererCallback remote exception: %{public}s", e.getMessage());
                        }
                    }
                    return;
                }
            }
            LOGGER.warn("attempt to call unregisterAudioRendererCallback on a callback that was never registered", new Object[0]);
        }
    }

    public List<AudioCapturerConfig> getActiveCapturerConfigs() {
        try {
            List activeRecordingConfigurations = getService().getActiveRecordingConfigurations();
            ArrayList arrayList = new ArrayList();
            activeRecordingConfigurations.forEach(new Consumer(arrayList) {
                /* class ohos.media.audioimpl.adapter.$$Lambda$AudioServiceAdapter$eHPDy7wMFZe63Ouonapl1YSGQoE */
                private final /* synthetic */ List f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    AudioServiceAdapter.lambda$getActiveCapturerConfigs$0(this.f$0, (AudioRecordingConfiguration) obj);
                }
            });
            return arrayList;
        } catch (RemoteException e) {
            LOGGER.error("getActiveCapturerConfigs remote exception: %{public}s", e.getMessage());
            return new ArrayList();
        }
    }

    static /* synthetic */ void lambda$getActiveCapturerConfigs$0(List list, AudioRecordingConfiguration audioRecordingConfiguration) {
        list.add(convertAudioCapturerConfig(audioRecordingConfiguration));
    }

    private static void cloneDeviceDescriptor(AudioDeviceDescriptor[] audioDeviceDescriptorArr, AudioDeviceDescriptor[] audioDeviceDescriptorArr2) {
        for (int i = 0; i < audioDeviceDescriptorArr.length; i++) {
            audioDeviceDescriptorArr2[i] = new AudioDeviceDescriptor(audioDeviceDescriptorArr[i].getId(), audioDeviceDescriptorArr[i].getName(), audioDeviceDescriptorArr[i].getAddress(), audioDeviceDescriptorArr[i].getType(), audioDeviceDescriptorArr[i].getRole(), audioDeviceDescriptorArr[i].getSamplingRates(), audioDeviceDescriptorArr[i].getChannelMasks(), audioDeviceDescriptorArr[i].getChannelIndexMasks(), audioDeviceDescriptorArr[i].getEncodings());
        }
    }

    private static AudioDeviceDescriptor[] getCachedDeviceDescriptor(AudioDeviceDescriptor.DeviceFlag deviceFlag) {
        if ((deviceFlag.getValue() & AudioDeviceDescriptor.DeviceFlag.OUTPUT_DEVICES_FLAG.getValue()) != 0) {
            AudioDeviceDescriptor[] audioDeviceDescriptorArr = cachedOutputDeviceDescriptors;
            AudioDeviceDescriptor[] audioDeviceDescriptorArr2 = new AudioDeviceDescriptor[audioDeviceDescriptorArr.length];
            cloneDeviceDescriptor(audioDeviceDescriptorArr, audioDeviceDescriptorArr2);
            return audioDeviceDescriptorArr2;
        } else if ((deviceFlag.getValue() & AudioDeviceDescriptor.DeviceFlag.INPUT_DEVICES_FLAG.getValue()) != 0) {
            AudioDeviceDescriptor[] audioDeviceDescriptorArr3 = cachedInputDeviceDescriptors;
            AudioDeviceDescriptor[] audioDeviceDescriptorArr4 = new AudioDeviceDescriptor[audioDeviceDescriptorArr3.length];
            cloneDeviceDescriptor(audioDeviceDescriptorArr3, audioDeviceDescriptorArr4);
            return audioDeviceDescriptorArr4;
        } else {
            LOGGER.error("getCachedDeviceDescriptor failed", new Object[0]);
            return new AudioDeviceDescriptor[0];
        }
    }

    public static AudioDeviceDescriptor[] getDevicesStatic(AudioDeviceDescriptor.DeviceFlag deviceFlag) {
        boolean z;
        ArrayList arrayList = new ArrayList();
        if (AudioManager.listAudioDevicePorts(arrayList) != 0 || arrayList.isEmpty()) {
            LOGGER.error("listAudioDevicePorts failed", new Object[0]);
            return new AudioDeviceDescriptor[0];
        }
        synchronized (GET_DEVICE_LOCK) {
            int size = cachedDevicePorts.size();
            if (arrayList.size() == size) {
                int i = 0;
                while (true) {
                    if (i >= size) {
                        z = true;
                        break;
                    } else if (arrayList.get(i) != cachedDevicePorts.get(i)) {
                        z = false;
                        break;
                    } else {
                        i++;
                    }
                }
                if (z) {
                    return getCachedDeviceDescriptor(deviceFlag);
                }
            }
            cachedDevicePorts = arrayList;
            ArrayList arrayList2 = new ArrayList();
            ArrayList arrayList3 = new ArrayList();
            int size2 = arrayList.size();
            for (int i2 = 0; i2 < size2; i2++) {
                if (((AudioDevicePort) arrayList.get(i2)).role() == 2) {
                    arrayList3.add((AudioDevicePort) arrayList.get(i2));
                }
                if (((AudioDevicePort) arrayList.get(i2)).role() == 1) {
                    arrayList2.add((AudioDevicePort) arrayList.get(i2));
                }
            }
            cachedInputDeviceDescriptors = convertToDescriptor(arrayList2, AudioDeviceDescriptor.DeviceRole.INPUT_DEVICE);
            cachedOutputDeviceDescriptors = convertToDescriptor(arrayList3, AudioDeviceDescriptor.DeviceRole.OUTPUT_DEVICE);
            return getCachedDeviceDescriptor(deviceFlag);
        }
    }

    private static AudioDeviceDescriptor[] convertToDescriptor(List<AudioDevicePort> list, AudioDeviceDescriptor.DeviceRole deviceRole) {
        int size = list.size();
        AudioDeviceDescriptor[] audioDeviceDescriptorArr = new AudioDeviceDescriptor[size];
        for (int i = 0; i < size; i++) {
            AudioDevicePort audioDevicePort = list.get(i);
            audioDeviceDescriptorArr[i] = new AudioDeviceDescriptor(audioDevicePort.id(), audioDevicePort.name().length() != 0 ? audioDevicePort.name() : Build.MODEL, audioDevicePort.address(), AudioDeviceDescriptor.DeviceType.valueOf(AudioDeviceInfo.convertInternalDeviceToDeviceType(audioDevicePort.type())), deviceRole, audioDevicePort.samplingRates(), audioDevicePort.channelMasks(), audioDevicePort.channelIndexMasks(), filterEncodingFormats(audioDevicePort.formats()));
        }
        return audioDeviceDescriptorArr;
    }

    private static List<AudioStreamInfo.EncodingFormat> filterEncodingFormats(int[] iArr) {
        ArrayList arrayList = new ArrayList();
        if (iArr == null) {
            return arrayList;
        }
        for (int i : iArr) {
            if ((i > 1 && i <= 4) || i == 9) {
                arrayList.add(ENCODING_MATCH_TABLE[i]);
            }
        }
        return arrayList;
    }

    public List<AudioRendererInfo> getActiveRendererConfigs() {
        try {
            List activePlaybackConfigurations = getService().getActivePlaybackConfigurations();
            ArrayList arrayList = new ArrayList();
            activePlaybackConfigurations.forEach(new Consumer(arrayList) {
                /* class ohos.media.audioimpl.adapter.$$Lambda$AudioServiceAdapter$d7cviD84YlmAbohMe4cNFQWuYgU */
                private final /* synthetic */ List f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    AudioServiceAdapter.lambda$getActiveRendererConfigs$1(this.f$0, (AudioPlaybackConfiguration) obj);
                }
            });
            return arrayList;
        } catch (RemoteException e) {
            LOGGER.error("getActiveRendererConfigs remote exception: %{public}s", e.getMessage());
            return new ArrayList();
        }
    }

    static /* synthetic */ void lambda$getActiveRendererConfigs$1(List list, AudioPlaybackConfiguration audioPlaybackConfiguration) {
        list.add(convertAudioRendererInfo(audioPlaybackConfiguration));
    }

    public int getPhoneState() throws AudioRemoteAdapterException {
        try {
            return getService().getMode();
        } catch (RemoteException e) {
            throw new AudioRemoteAdapterException(e.toString());
        }
    }

    public void setPhoneState(int i) {
        try {
            try {
                getService().setMode(i, this.binder, this.packageName);
            } catch (RemoteException e) {
                LOGGER.error("setPhoneState remote exception: %{public}s", e.getMessage());
            }
        } catch (AudioRemoteAdapterException unused) {
        }
    }

    public boolean changeVolumeBy(int i, int i2) throws AudioRemoteAdapterException {
        try {
            getService().adjustStreamVolume(i, i2, 0, this.packageName);
            return true;
        } catch (RemoteException e) {
            throw new AudioRemoteAdapterException(e.toString());
        }
    }

    public void connectBluetoothSco() throws AudioRemoteAdapterException {
        try {
            getService().startBluetoothSco(this.binder, SDK_VERSION);
        } catch (RemoteException e) {
            throw new AudioRemoteAdapterException(e.toString());
        }
    }

    public void disconnectBluetoothSco() throws AudioRemoteAdapterException {
        try {
            getService().stopBluetoothSco(this.binder);
        } catch (RemoteException e) {
            throw new AudioRemoteAdapterException(e.toString());
        }
    }

    public boolean canBluetoothScoUseNotInCall(Context context) throws AudioRemoteAdapterException {
        if (context.getHostContext() instanceof android.content.Context) {
            android.content.Context context2 = (android.content.Context) context.getHostContext();
            if (context2 == null || context2.getResources() == null) {
                LOGGER.error("[canBluetoothScoUseNotInCall] context transform failed.", new Object[0]);
                throw new AudioRemoteAdapterException(INVALID_CONTEXT);
            }
            try {
                return context2.getResources().getBoolean(17891380);
            } catch (Resources.NotFoundException e) {
                LOGGER.error("[canBluetoothScoUseNotInCall] get resource failed.: %{public}s", e.getMessage());
                throw new AudioRemoteAdapterException(INVALID_CONTEXT);
            }
        } else {
            LOGGER.error("[canBluetoothScoUseNotInCall] context transform failed.", new Object[0]);
            throw new AudioRemoteAdapterException(INVALID_CONTEXT);
        }
    }

    public boolean isMasterMute() throws AudioRemoteAdapterException {
        try {
            return getService().isMasterMute();
        } catch (RemoteException e) {
            throw new AudioRemoteAdapterException(e.toString());
        }
    }

    /* access modifiers changed from: private */
    public static final class WaitingRequestObject {
        private final String clientId;
        private int finalResult = 0;
        private volatile boolean getFinalResult = false;
        private final Object waitObject = new Object();

        WaitingRequestObject(String str) {
            this.clientId = str;
        }

        public void waitResult(long j) {
            synchronized (this.waitObject) {
                if (!this.getFinalResult) {
                    long currentTimeMillis = System.currentTimeMillis() + j;
                    while (true) {
                        if (this.getFinalResult) {
                            break;
                        }
                        long currentTimeMillis2 = currentTimeMillis - System.currentTimeMillis();
                        if (currentTimeMillis2 <= 0) {
                            AudioServiceAdapter.LOGGER.error("waitFinalResult error, end of waiting time", new Object[0]);
                            break;
                        }
                        try {
                            this.waitObject.wait(currentTimeMillis2);
                        } catch (InterruptedException e) {
                            AudioServiceAdapter.LOGGER.error("waitFinalResult wait error : %{public}s", e.getMessage());
                            return;
                        }
                    }
                }
            }
        }

        public void setResult(int i) {
            synchronized (this.waitObject) {
                this.getFinalResult = true;
                this.finalResult = i;
                this.waitObject.notifyAll();
            }
        }

        public int getResult() {
            int i;
            synchronized (this.waitObject) {
                i = this.finalResult;
            }
            return i;
        }
    }
}
