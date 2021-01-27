package ohos.media.audio;

import android.os.Looper;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.eventhandler.InnerEvent;
import ohos.media.audio.AudioCapturerInfo;
import ohos.media.audio.AudioDeviceDescriptor;
import ohos.media.audio.AudioStreamInfo;
import ohos.media.audio.Timestamp;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.media.utils.trace.Tracer;
import ohos.media.utils.trace.TracerFactory;

public class AudioCapturer {
    public static final int CAPTURER_SESSION_ID_NONE = 0;
    public static final int ERROR = -1;
    public static final int ERROR_BAD_VALUE = -1;
    public static final int ERROR_DEAD_OBJECT = -3;
    public static final int ERROR_INVALID_OPERATION = -2;
    public static final int INPUT_DEVICE_ID_NONE = 0;
    private static final Logger LOGGER = LoggerFactory.getAudioLogger(AudioCapturer.class);
    private static final int NATIVE_EVENT_INTERVAL = 3;
    private static final int NATIVE_EVENT_POSITION = 2;
    public static final int SUCCESS = 0;
    private static final Tracer TRACER = TracerFactory.getAudioTracer();
    private final Set<SoundEffect> activatedSoundEffects;
    private AudioCapturerInfo audioCapturerInfo;
    private int cachedBufferFrameCount;
    private int cachedChannelCount;
    private AudioStreamInfo.EncodingFormat cachedEncodingFormat;
    private long jniData;
    private final Object mFrameIntervalObserverLock;
    private final Object mFramePositionObserverLock;
    private Looper mInitializationLooper;
    private NativeEventIntervalHandler mIntervalEventHandler;
    private FrameIntervalObserver mIntervalListener;
    private NativeEventPositionHandler mPositionEventHandler;
    private FramePositionObserver mPositionListener;
    private long nativeAudioCapturer;
    private volatile AudioDeviceDescriptor selectedDevice;
    private volatile State state;

    private native boolean nativeCapturerSetFrameInterval(int i);

    private native boolean nativeCapturerSetFramePosition(int i);

    private native int nativeGetBufferSizeInFrames();

    private native int nativeGetCapturerSessionId();

    private native int nativeGetCurrentDeviceId();

    private static native int nativeGetMinBuffSize(int i, int i2, int i3);

    private native int nativeGetSampleRate();

    private native boolean nativeGetTimestamp(Timestamp timestamp, int i);

    private native int nativeReadInByteArray(byte[] bArr, int i, int i2, boolean z);

    private native int nativeReadInDirectBuffer(Object obj, int i, boolean z);

    private native int nativeReadInFloatArray(float[] fArr, int i, int i2, boolean z);

    private native int nativeReadInShortArray(short[] sArr, int i, int i2, boolean z);

    private native boolean nativeRelease();

    private native boolean nativeSetInputDevice(int i);

    private native boolean nativeSetup(Object obj, AudioCapturerInfo audioCapturerInfo2);

    private native boolean nativeStart();

    private native boolean nativeStop();

    static {
        System.loadLibrary("audiocapturer_jni.z");
    }

    public enum State {
        STATE_UNINITIALIZED(0),
        STATE_INITIALIZED(1),
        STATE_RECORDING(2),
        STATE_STOPPED(3);
        
        private final int state;

        private State(int i) {
            this.state = i;
        }

        public int getValue() {
            return this.state;
        }
    }

    public static int getMinBufferSize(int i, int i2, int i3) {
        String format = String.format(Locale.ROOT, "AudioCapturer_getMinBufferSize_kits:sampleRate=%d,channelCount=%d,audioFormat=%d", Integer.valueOf(i), Integer.valueOf(i2), Integer.valueOf(i3));
        TRACER.startTrace(format);
        int nativeGetMinBuffSize = nativeGetMinBuffSize(i, i2, i3);
        TRACER.finishTrace(format);
        if (nativeGetMinBuffSize != 0) {
            return nativeGetMinBuffSize;
        }
        LOGGER.error("nativeGetMinBuffSize is zero.", new Object[0]);
        return -1;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.media.audio.AudioCapturer$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$media$audio$AudioStreamInfo$EncodingFormat = new int[AudioStreamInfo.EncodingFormat.values().length];

        static {
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$EncodingFormat[AudioStreamInfo.EncodingFormat.ENCODING_PCM_16BIT.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$EncodingFormat[AudioStreamInfo.EncodingFormat.ENCODING_PCM_8BIT.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$media$audio$AudioStreamInfo$EncodingFormat[AudioStreamInfo.EncodingFormat.ENCODING_PCM_FLOAT.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    private int getBytesPerSample(AudioStreamInfo.EncodingFormat encodingFormat) {
        int i = AnonymousClass1.$SwitchMap$ohos$media$audio$AudioStreamInfo$EncodingFormat[encodingFormat.ordinal()];
        if (i == 1) {
            return 2;
        }
        if (i != 2) {
            return i != 3 ? 0 : 4;
        }
        return 1;
    }

    private boolean isValidBufferSize(AudioCapturerInfo audioCapturerInfo2) {
        if (audioCapturerInfo2.getBufferSizeInBytes() == 0) {
            return true;
        }
        long bufferSizeInBytes = audioCapturerInfo2.getBufferSizeInBytes();
        int bitCount = Integer.bitCount(audioCapturerInfo2.getAudioStreamInfo().getChannelMask().getValue()) * getBytesPerSample(audioCapturerInfo2.getAudioStreamInfo().getEncodingFormat());
        if (bitCount == 0 || bufferSizeInBytes % ((long) bitCount) != 0 || bufferSizeInBytes <= 0) {
            return false;
        }
        return true;
    }

    public AudioCapturer(AudioCapturerInfo audioCapturerInfo2, AudioDeviceDescriptor audioDeviceDescriptor) throws IllegalArgumentException {
        this.state = State.STATE_UNINITIALIZED;
        this.audioCapturerInfo = null;
        this.selectedDevice = null;
        this.activatedSoundEffects = ConcurrentHashMap.newKeySet();
        this.cachedEncodingFormat = AudioStreamInfo.EncodingFormat.ENCODING_INVALID;
        this.cachedChannelCount = 0;
        this.cachedBufferFrameCount = 0;
        this.mInitializationLooper = null;
        this.mPositionListener = null;
        this.mIntervalListener = null;
        this.mFramePositionObserverLock = new Object();
        this.mFrameIntervalObserverLock = new Object();
        this.mIntervalEventHandler = null;
        this.mPositionEventHandler = null;
        if (audioCapturerInfo2 == null) {
            LOGGER.error("input AudioCapturerInfo is null.", new Object[0]);
            throw new IllegalArgumentException("Illegal null AudioCapturerInfo.");
        } else if (audioCapturerInfo2.getAudioStreamInfo() == null) {
            LOGGER.error("input AudioStreamInfo is null.", new Object[0]);
            throw new IllegalArgumentException("Illegal AudioCapturerInfo without AudioStreamInfo.");
        } else if (!isValidBufferSize(audioCapturerInfo2)) {
            LOGGER.error("invalid BufferSizeInBytes = %{public}d.", Long.valueOf(audioCapturerInfo2.getBufferSizeInBytes()));
            throw new IllegalArgumentException("Illegal AudioCapturerInfo with invalid BufferSizeInBytes.");
        } else if (audioDeviceDescriptor != null && !setInputDevice(audioDeviceDescriptor)) {
            LOGGER.error("setInputDevice failed.", new Object[0]);
            throw new IllegalArgumentException("Initial AudioCapturer Failed.");
        } else if (nativeSetup(new WeakReference(this), audioCapturerInfo2)) {
            this.cachedEncodingFormat = audioCapturerInfo2.getAudioStreamInfo().getEncodingFormat();
            this.cachedChannelCount = Integer.bitCount(audioCapturerInfo2.getAudioStreamInfo().getChannelMask().getValue());
            this.cachedBufferFrameCount = nativeGetBufferSizeInFrames();
            this.state = State.STATE_INITIALIZED;
            this.audioCapturerInfo = audioCapturerInfo2;
        } else {
            LOGGER.error("nativeSetup failed.", new Object[0]);
            throw new IllegalArgumentException("Initial AudioCapturer Failed.");
        }
    }

    public AudioCapturer(AudioCapturerInfo audioCapturerInfo2) throws IllegalArgumentException {
        this(audioCapturerInfo2, null);
    }

    public boolean addSoundEffect(UUID uuid, String str) {
        if (uuid == null || str == null || str.isEmpty()) {
            LOGGER.error("The input parameters is invalid", new Object[0]);
            return false;
        }
        for (SoundEffect soundEffect : this.activatedSoundEffects) {
            if (soundEffect.getEffectInfo().getType().equals(uuid)) {
                LOGGER.warn("The input SoundEffect is already activated, type: %{public}s", uuid.toString());
                return true;
            }
        }
        if (!SoundEffect.isEffectAvailable(uuid)) {
            LOGGER.error("The input type: %{public}s is not supported", uuid.toString());
            return false;
        }
        SoundEffect soundEffect2 = null;
        try {
            soundEffect2 = new SoundEffect(SoundEffect.SOUND_EFFECT_TYPE_INVALID, uuid, 0, getCapturerSessionId(), str);
        } catch (UnsupportedOperationException e) {
            LOGGER.error("operation failed, error message: %{public}s", e.getMessage());
        }
        if (soundEffect2 == null) {
            LOGGER.error("Initialize SoundEffect instance failed", new Object[0]);
            return false;
        }
        String format = String.format(Locale.ROOT, "AudioCapturer_addSoundEffect_kits:packageName=%s", str);
        TRACER.startTrace(format);
        if (!soundEffect2.setActivated(true)) {
            LOGGER.error("Activate failed, type: %{public}s", uuid.toString());
            soundEffect2.release();
            TRACER.finishTrace(format);
            return false;
        }
        TRACER.finishTrace(format);
        this.activatedSoundEffects.add(soundEffect2);
        return true;
    }

    public Set<SoundEffect> getSoundEffects() {
        return this.activatedSoundEffects;
    }

    private boolean setInputDevice(AudioDeviceDescriptor audioDeviceDescriptor) {
        if (audioDeviceDescriptor == null || !audioDeviceDescriptor.isInputDevice()) {
            LOGGER.error("Not an input device.", new Object[0]);
            return false;
        }
        int id = audioDeviceDescriptor.getId();
        TRACER.startTrace("AudioCapturer_setInputDevice_kits");
        boolean nativeSetInputDevice = nativeSetInputDevice(id);
        if (nativeSetInputDevice) {
            this.selectedDevice = audioDeviceDescriptor;
            LOGGER.info("setInputDevice id = %{public}d.", Integer.valueOf(id));
        }
        TRACER.finishTrace("AudioCapturer_setInputDevice_kits");
        return nativeSetInputDevice;
    }

    public int getCapturerSessionId() {
        TRACER.startTrace("AudioCapturer_getCapturerSessionId_kits");
        int nativeGetCapturerSessionId = nativeGetCapturerSessionId();
        TRACER.finishTrace("AudioCapturer_getCapturerSessionId_kits");
        return nativeGetCapturerSessionId;
    }

    public AudioDeviceDescriptor getSelectedDevice() {
        return this.selectedDevice;
    }

    public AudioDeviceDescriptor getCurrentDevice() {
        TRACER.startTrace("AudioCapturer_getCurrentDevice_kits");
        int nativeGetCurrentDeviceId = nativeGetCurrentDeviceId();
        TRACER.finishTrace("AudioCapturer_getCurrentDevice_kits");
        if (nativeGetCurrentDeviceId == 0) {
            LOGGER.error("native GetCurrentDeviceId failed.", new Object[0]);
            return null;
        }
        AudioDeviceDescriptor[] devices = AudioManager.getDevices(AudioDeviceDescriptor.DeviceFlag.INPUT_DEVICES_FLAG);
        for (AudioDeviceDescriptor audioDeviceDescriptor : devices) {
            if (audioDeviceDescriptor.getId() == nativeGetCurrentDeviceId) {
                return audioDeviceDescriptor;
            }
        }
        LOGGER.error("No device with id = %{public}d found.", Integer.valueOf(nativeGetCurrentDeviceId));
        return null;
    }

    public State getState() {
        return this.state;
    }

    public AudioStreamInfo.EncodingFormat getEncodingFormat() {
        return this.cachedEncodingFormat;
    }

    public int getChannelCount() {
        return this.cachedChannelCount;
    }

    public int getSampleRate() {
        return nativeGetSampleRate();
    }

    public int getBufferFrameCount() {
        return this.cachedBufferFrameCount;
    }

    public int getAudioInputSource() {
        AudioCapturerInfo audioCapturerInfo2 = this.audioCapturerInfo;
        if (audioCapturerInfo2 == null || audioCapturerInfo2.getInputSource() == null) {
            return AudioCapturerInfo.AudioInputSource.AUDIO_INPUT_SOURCE_INVALID.getValue();
        }
        return this.audioCapturerInfo.getInputSource().getValue();
    }

    public AudioStreamInfo.ChannelMask getAudioChannel() {
        return this.audioCapturerInfo.getAudioStreamInfo().getChannelMask();
    }

    public boolean getAudioTime(Timestamp timestamp, Timestamp.Timebase timebase) {
        if (timestamp == null) {
            LOGGER.error("timestamp is null.", new Object[0]);
            return false;
        } else if (nativeGetTimestamp(timestamp, timebase.getValue())) {
            return true;
        } else {
            LOGGER.error("native get time stamp failed.", new Object[0]);
            timestamp.setFramePosition(0);
            timestamp.setNanoTimestamp(0);
            return false;
        }
    }

    public final boolean start() {
        if (this.state == State.STATE_UNINITIALIZED) {
            LOGGER.error("can not record on an uninitialized AudioCapturer, state = %{public}d", Integer.valueOf(this.state.getValue()));
            return false;
        } else if (this.state == State.STATE_RECORDING) {
            LOGGER.error("can not record, state is already recording", new Object[0]);
            return false;
        } else {
            TRACER.startTrace("AudioCapturer_record_kits");
            if (!nativeStart()) {
                LOGGER.error("nativeStart failed.", new Object[0]);
                TRACER.finishTrace("AudioCapturer_record_kits");
                return false;
            }
            TRACER.finishTrace("AudioCapturer_record_kits");
            this.state = State.STATE_RECORDING;
            return true;
        }
    }

    public int read(byte[] bArr, int i, int i2) {
        return read(bArr, i, i2, true);
    }

    public int read(byte[] bArr, int i, int i2, boolean z) {
        int i3;
        if (this.state != State.STATE_RECORDING) {
            LOGGER.error("can not read, state is %{public}d.", Integer.valueOf(this.state.getValue()));
            return -2;
        } else if (bArr != null && i >= 0 && i2 > 0 && (i3 = i + i2) >= 0 && i3 <= bArr.length) {
            return nativeReadInByteArray(bArr, i, i2, z);
        } else {
            LOGGER.error("can not read, invalid parameters", new Object[0]);
            return -1;
        }
    }

    public int read(short[] sArr, int i, int i2) {
        return read(sArr, i, i2, true);
    }

    public int read(short[] sArr, int i, int i2, boolean z) {
        int i3;
        if (this.state != State.STATE_RECORDING) {
            LOGGER.error("can not read, state is %{public}d.", Integer.valueOf(this.state.getValue()));
            return -2;
        } else if (sArr != null && i >= 0 && i2 > 0 && (i3 = i + i2) >= 0 && i3 <= sArr.length) {
            return nativeReadInShortArray(sArr, i, i2, z);
        } else {
            LOGGER.error("can not read, invalid parameters", new Object[0]);
            return -1;
        }
    }

    public int read(float[] fArr, int i, int i2) {
        return read(fArr, i, i2, true);
    }

    public int read(float[] fArr, int i, int i2, boolean z) {
        int i3;
        if (this.state != State.STATE_RECORDING) {
            LOGGER.error("can not read, state is %{public}d.", Integer.valueOf(this.state.getValue()));
            return -2;
        } else if (fArr != null && i >= 0 && i2 > 0 && (i3 = i + i2) >= 0 && i3 <= fArr.length) {
            return nativeReadInFloatArray(fArr, i, i2, z);
        } else {
            LOGGER.error("can not read, invalid parameters", new Object[0]);
            return -1;
        }
    }

    public int read(ByteBuffer byteBuffer, int i) {
        return read(byteBuffer, i, true);
    }

    public int read(ByteBuffer byteBuffer, int i, boolean z) {
        if (this.state != State.STATE_RECORDING) {
            LOGGER.error("can not read, state is %{public}d.", Integer.valueOf(this.state.getValue()));
            return -2;
        } else if (byteBuffer != null && i > 0) {
            return nativeReadInDirectBuffer(byteBuffer, i, z);
        } else {
            LOGGER.error("invalid input parameters.", new Object[0]);
            return -1;
        }
    }

    public final boolean stop() {
        if (this.state == State.STATE_UNINITIALIZED) {
            LOGGER.error("can not stop on an uninitialized AudioCapturer, state = %{public}d", Integer.valueOf(this.state.getValue()));
            return false;
        } else if (this.state == State.STATE_STOPPED) {
            LOGGER.error("can not stop, state is already stopped", new Object[0]);
            return false;
        } else {
            TRACER.startTrace("AudioCapturer_stop_kits");
            if (!nativeStop()) {
                LOGGER.error("nativeStop failed.", new Object[0]);
                TRACER.finishTrace("AudioCapturer_stop_kits");
                return false;
            }
            TRACER.finishTrace("AudioCapturer_stop_kits");
            this.state = State.STATE_STOPPED;
            return true;
        }
    }

    public final boolean release() {
        if (this.state != State.STATE_STOPPED) {
            stop();
        }
        for (SoundEffect soundEffect : this.activatedSoundEffects) {
            soundEffect.release();
        }
        TRACER.startTrace("AudioCapturer_release_kits");
        if (!nativeRelease()) {
            LOGGER.error("nativeRelease failed.", new Object[0]);
            TRACER.finishTrace("AudioCapturer_release_kits");
            return false;
        }
        TRACER.finishTrace("AudioCapturer_release_kits");
        this.state = State.STATE_UNINITIALIZED;
        return true;
    }

    public void setFramePositionObserver(FramePositionObserver framePositionObserver, int i, EventHandler eventHandler) {
        EventRunner eventRunner;
        synchronized (this.mFramePositionObserverLock) {
            if (framePositionObserver == null) {
                LOGGER.error("setFramePositionObserver observer is null", new Object[0]);
            }
            this.mPositionListener = framePositionObserver;
            if (!nativeCapturerSetFramePosition(i)) {
                LOGGER.error("capturer set frame position in native fail", new Object[0]);
                return;
            }
            if (eventHandler != null) {
                eventRunner = eventHandler.getEventRunner();
                if (eventRunner == null) {
                    LOGGER.warn("setFramePositionObserver get event runner fail", new Object[0]);
                    eventRunner = EventRunner.create();
                }
            } else {
                eventRunner = EventRunner.create();
            }
            if (eventRunner == null) {
                LOGGER.error("setFramePositionObserver fail to get event runner", new Object[0]);
            } else {
                this.mPositionEventHandler = new NativeEventPositionHandler(this, eventRunner);
            }
        }
    }

    public void setFrameIntervalObserver(FrameIntervalObserver frameIntervalObserver, int i, EventHandler eventHandler) {
        EventRunner eventRunner;
        synchronized (this.mFrameIntervalObserverLock) {
            if (frameIntervalObserver == null) {
                LOGGER.error("setFrameIntervalObserver observer is null", new Object[0]);
            }
            this.mIntervalListener = frameIntervalObserver;
            if (!nativeCapturerSetFrameInterval(i)) {
                LOGGER.error("capturer set frame interval in native fail", new Object[0]);
                return;
            }
            if (eventHandler != null) {
                eventRunner = eventHandler.getEventRunner();
                if (eventRunner == null) {
                    LOGGER.warn("setFrameIntervalObserver get event runner fail", new Object[0]);
                    eventRunner = EventRunner.create();
                }
            } else {
                eventRunner = EventRunner.create();
            }
            if (eventRunner == null) {
                LOGGER.error("setFrameIntervalObserver fail to get event runner", new Object[0]);
            } else {
                this.mIntervalEventHandler = new NativeEventIntervalHandler(this, eventRunner);
            }
        }
    }

    private class NativeEventPositionHandler extends EventHandler {
        private final AudioCapturer mAudioCapturer;

        NativeEventPositionHandler(AudioCapturer audioCapturer, EventRunner eventRunner) {
            super(eventRunner);
            this.mAudioCapturer = audioCapturer;
        }

        public void processEvent(InnerEvent innerEvent) {
            FramePositionObserver framePositionObserver;
            AudioCapturer.LOGGER.info("NativeEventPositionHandler.processEvent called", new Object[0]);
            synchronized (AudioCapturer.this.mFramePositionObserverLock) {
                framePositionObserver = this.mAudioCapturer.mPositionListener;
            }
            if (innerEvent == null) {
                AudioCapturer.LOGGER.error("NativeEventPositionHandler InnerEvent object is null", new Object[0]);
            } else if (innerEvent.eventId != 2) {
                AudioCapturer.LOGGER.error("Unknown NativeEventPositionHandler event type %{public}d: ", Integer.valueOf(innerEvent.eventId));
            } else if (framePositionObserver != null) {
                framePositionObserver.onFramePosition();
            }
        }
    }

    private class NativeEventIntervalHandler extends EventHandler {
        private final AudioCapturer mAudioCapturer;

        NativeEventIntervalHandler(AudioCapturer audioCapturer, EventRunner eventRunner) {
            super(eventRunner);
            this.mAudioCapturer = audioCapturer;
        }

        public void processEvent(InnerEvent innerEvent) {
            FrameIntervalObserver frameIntervalObserver;
            AudioCapturer.LOGGER.info("NativeEventIntervalHandler.processEvent called", new Object[0]);
            synchronized (AudioCapturer.this.mFrameIntervalObserverLock) {
                frameIntervalObserver = this.mAudioCapturer.mIntervalListener;
            }
            if (innerEvent == null) {
                AudioCapturer.LOGGER.error("NativeEventIntervalHandler InnerEvent object is null", new Object[0]);
            } else if (innerEvent.eventId != 3) {
                AudioCapturer.LOGGER.error("Unknown NativeEventIntervalHandler event type %{public}d:", Integer.valueOf(innerEvent.eventId));
            } else if (frameIntervalObserver != null) {
                frameIntervalObserver.onFrameInterval();
            }
        }
    }

    private static void postEventFromNative(Object obj, int i, int i2, int i3, Object obj2) {
        if (obj == null || !(obj instanceof WeakReference)) {
            LOGGER.error("audiocapturer_ref is null or not instance of WeakReference, return", new Object[0]);
            return;
        }
        AudioCapturer audioCapturer = (AudioCapturer) ((WeakReference) obj).get();
        if (audioCapturer != null) {
            if (i == 2) {
                NativeEventPositionHandler nativeEventPositionHandler = audioCapturer.mPositionEventHandler;
                if (nativeEventPositionHandler == null) {
                    LOGGER.error("NativeEventPositionHandler is not set, return", new Object[0]);
                } else {
                    nativeEventPositionHandler.sendEvent(i);
                }
            } else if (i != 3) {
                LOGGER.error("postEventFromNative Unexpected message %{public}d", Integer.valueOf(i));
            } else {
                NativeEventIntervalHandler nativeEventIntervalHandler = audioCapturer.mIntervalEventHandler;
                if (nativeEventIntervalHandler == null) {
                    LOGGER.error("NativeEventIntervalHandler is not set, return", new Object[0]);
                } else {
                    nativeEventIntervalHandler.sendEvent(i);
                }
            }
        }
    }
}
