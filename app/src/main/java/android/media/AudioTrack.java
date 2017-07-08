package android.media;

import android.hsm.MediaTransactWrapper;
import android.opengl.GLES30;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.provider.Downloads.Impl;
import android.service.notification.NotificationRankerService;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.NioUtils;

public class AudioTrack extends PlayerBase implements AudioRouting {
    public static final int CHANNEL_COUNT_MAX = 0;
    public static final int ERROR = -1;
    public static final int ERROR_BAD_VALUE = -2;
    public static final int ERROR_DEAD_OBJECT = -6;
    public static final int ERROR_INVALID_OPERATION = -3;
    private static final int ERROR_NATIVESETUP_AUDIOSYSTEM = -16;
    private static final int ERROR_NATIVESETUP_INVALIDCHANNELMASK = -17;
    private static final int ERROR_NATIVESETUP_INVALIDFORMAT = -18;
    private static final int ERROR_NATIVESETUP_INVALIDSTREAMTYPE = -19;
    private static final int ERROR_NATIVESETUP_NATIVEINITFAILED = -20;
    public static final int ERROR_WOULD_BLOCK = -7;
    private static final float GAIN_MAX = 1.0f;
    private static final float GAIN_MIN = 0.0f;
    public static final int MODE_STATIC = 0;
    public static final int MODE_STREAM = 1;
    private static final int NATIVE_EVENT_MARKER = 3;
    private static final int NATIVE_EVENT_NEW_POS = 4;
    public static final int PLAYSTATE_PAUSED = 2;
    public static final int PLAYSTATE_PLAYING = 3;
    public static final int PLAYSTATE_STOPPED = 1;
    public static final int STATE_INITIALIZED = 1;
    public static final int STATE_NO_STATIC_DATA = 2;
    public static final int STATE_UNINITIALIZED = 0;
    public static final int SUCCESS = 0;
    private static final int SUPPORTED_OUT_CHANNELS = 7420;
    private static final String TAG = "android.media.AudioTrack";
    public static final int WRITE_BLOCKING = 0;
    public static final int WRITE_NON_BLOCKING = 1;
    private int mAudioFormat;
    private int mAvSyncBytesRemaining;
    private ByteBuffer mAvSyncHeader;
    private int mChannelConfiguration;
    private int mChannelCount;
    private int mChannelIndexMask;
    private int mChannelMask;
    private int mDataLoadMode;
    private NativePositionEventHandlerDelegate mEventHandlerDelegate;
    private final Looper mInitializationLooper;
    private long mJniData;
    private int mNativeBufferSizeInBytes;
    private int mNativeBufferSizeInFrames;
    protected long mNativeTrackInJavaObj;
    private int mPlayState;
    private final Object mPlayStateLock;
    private AudioDeviceInfo mPreferredDevice;
    @GuardedBy("mRoutingChangeListeners")
    private ArrayMap<android.media.AudioRouting.OnRoutingChangedListener, NativeRoutingEventHandlerDelegate> mRoutingChangeListeners;
    private int mSampleRate;
    private int mSessionId;
    private int mState;
    private int mStreamType;

    public static class Builder {
        private AudioAttributes mAttributes;
        private int mBufferSizeInBytes;
        private AudioFormat mFormat;
        private int mMode;
        private int mSessionId;

        public Builder() {
            this.mSessionId = AudioTrack.WRITE_BLOCKING;
            this.mMode = AudioTrack.WRITE_NON_BLOCKING;
        }

        public Builder setAudioAttributes(AudioAttributes attributes) throws IllegalArgumentException {
            if (attributes == null) {
                throw new IllegalArgumentException("Illegal null AudioAttributes argument");
            }
            this.mAttributes = attributes;
            return this;
        }

        public Builder setAudioFormat(AudioFormat format) throws IllegalArgumentException {
            if (format == null) {
                throw new IllegalArgumentException("Illegal null AudioFormat argument");
            }
            this.mFormat = format;
            return this;
        }

        public Builder setBufferSizeInBytes(int bufferSizeInBytes) throws IllegalArgumentException {
            if (bufferSizeInBytes <= 0) {
                throw new IllegalArgumentException("Invalid buffer size " + bufferSizeInBytes);
            }
            this.mBufferSizeInBytes = bufferSizeInBytes;
            return this;
        }

        public Builder setTransferMode(int mode) throws IllegalArgumentException {
            switch (mode) {
                case AudioTrack.WRITE_BLOCKING /*0*/:
                case AudioTrack.WRITE_NON_BLOCKING /*1*/:
                    this.mMode = mode;
                    return this;
                default:
                    throw new IllegalArgumentException("Invalid transfer mode " + mode);
            }
        }

        public Builder setSessionId(int sessionId) throws IllegalArgumentException {
            if (sessionId == 0 || sessionId >= AudioTrack.WRITE_NON_BLOCKING) {
                this.mSessionId = sessionId;
                return this;
            }
            throw new IllegalArgumentException("Invalid audio session ID " + sessionId);
        }

        public AudioTrack build() throws UnsupportedOperationException {
            if (this.mAttributes == null) {
                this.mAttributes = new android.media.AudioAttributes.Builder().setUsage(AudioTrack.WRITE_NON_BLOCKING).build();
            }
            if (this.mFormat == null) {
                this.mFormat = new android.media.AudioFormat.Builder().setChannelMask(12).setEncoding(AudioTrack.WRITE_NON_BLOCKING).build();
            }
            try {
                if (this.mMode == AudioTrack.WRITE_NON_BLOCKING && this.mBufferSizeInBytes == 0) {
                    int channelCount = this.mFormat.getChannelCount();
                    AudioFormat audioFormat = this.mFormat;
                    this.mBufferSizeInBytes = channelCount * AudioFormat.getBytesPerSample(this.mFormat.getEncoding());
                }
                AudioTrack track = new AudioTrack(this.mAttributes, this.mFormat, this.mBufferSizeInBytes, this.mMode, this.mSessionId);
                if (track.getState() != 0) {
                    return track;
                }
                throw new UnsupportedOperationException("Cannot create AudioTrack");
            } catch (IllegalArgumentException e) {
                throw new UnsupportedOperationException(e.getMessage());
            }
        }
    }

    private class NativePositionEventHandlerDelegate {
        private final Handler mHandler;

        /* renamed from: android.media.AudioTrack.NativePositionEventHandlerDelegate.1 */
        class AnonymousClass1 extends Handler {
            final /* synthetic */ OnPlaybackPositionUpdateListener val$listener;
            final /* synthetic */ AudioTrack val$track;

            AnonymousClass1(Looper $anonymous0, AudioTrack val$track, OnPlaybackPositionUpdateListener val$listener) {
                this.val$track = val$track;
                this.val$listener = val$listener;
                super($anonymous0);
            }

            public void handleMessage(Message msg) {
                if (this.val$track != null) {
                    switch (msg.what) {
                        case AudioTrack.PLAYSTATE_PLAYING /*3*/:
                            if (this.val$listener != null) {
                                this.val$listener.onMarkerReached(this.val$track);
                                break;
                            }
                            break;
                        case AudioTrack.NATIVE_EVENT_NEW_POS /*4*/:
                            if (this.val$listener != null) {
                                this.val$listener.onPeriodicNotification(this.val$track);
                                break;
                            }
                            break;
                        default:
                            AudioTrack.loge("Unknown native event type: " + msg.what);
                            break;
                    }
                }
            }
        }

        NativePositionEventHandlerDelegate(AudioTrack track, OnPlaybackPositionUpdateListener listener, Handler handler) {
            Looper looper;
            if (handler != null) {
                looper = handler.getLooper();
            } else {
                looper = AudioTrack.this.mInitializationLooper;
            }
            if (looper != null) {
                this.mHandler = new AnonymousClass1(looper, track, listener);
            } else {
                this.mHandler = null;
            }
        }

        Handler getHandler() {
            return this.mHandler;
        }
    }

    private class NativeRoutingEventHandlerDelegate {
        private final Handler mHandler;

        /* renamed from: android.media.AudioTrack.NativeRoutingEventHandlerDelegate.1 */
        class AnonymousClass1 extends Handler {
            final /* synthetic */ android.media.AudioRouting.OnRoutingChangedListener val$listener;
            final /* synthetic */ AudioTrack val$track;

            AnonymousClass1(Looper $anonymous0, AudioTrack val$track, android.media.AudioRouting.OnRoutingChangedListener val$listener) {
                this.val$track = val$track;
                this.val$listener = val$listener;
                super($anonymous0);
            }

            public void handleMessage(Message msg) {
                if (this.val$track != null) {
                    switch (msg.what) {
                        case Process.SYSTEM_UID /*1000*/:
                            if (this.val$listener != null) {
                                this.val$listener.onRoutingChanged(this.val$track);
                                break;
                            }
                            break;
                        default:
                            AudioTrack.loge("Unknown native event type: " + msg.what);
                            break;
                    }
                }
            }
        }

        NativeRoutingEventHandlerDelegate(AudioTrack track, android.media.AudioRouting.OnRoutingChangedListener listener, Handler handler) {
            Looper looper;
            if (handler != null) {
                looper = handler.getLooper();
            } else {
                looper = AudioTrack.this.mInitializationLooper;
            }
            if (looper != null) {
                this.mHandler = new AnonymousClass1(looper, track, listener);
            } else {
                this.mHandler = null;
            }
        }

        Handler getHandler() {
            return this.mHandler;
        }
    }

    public interface OnPlaybackPositionUpdateListener {
        void onMarkerReached(AudioTrack audioTrack);

        void onPeriodicNotification(AudioTrack audioTrack);
    }

    @Deprecated
    public interface OnRoutingChangedListener extends android.media.AudioRouting.OnRoutingChangedListener {
        void onRoutingChanged(AudioTrack audioTrack);

        void onRoutingChanged(AudioRouting router) {
            if (router instanceof AudioTrack) {
                onRoutingChanged((AudioTrack) router);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.AudioTrack.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.AudioTrack.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.AudioTrack.<clinit>():void");
    }

    private void audioParamCheck(int r1, int r2, int r3, int r4, int r5) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.AudioTrack.audioParamCheck(int, int, int, int, int):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.AudioTrack.audioParamCheck(int, int, int, int, int):void");
    }

    private final native int native_attachAuxEffect(int i);

    private final native void native_disableDeviceCallback();

    private final native void native_enableDeviceCallback();

    private final native void native_finalize();

    private final native void native_flush();

    private final native int native_getRoutedDeviceId();

    private static native int native_get_FCC_8();

    private final native int native_get_buffer_capacity_frames();

    private final native int native_get_buffer_size_frames();

    private final native int native_get_latency();

    private final native int native_get_marker_pos();

    private static final native int native_get_min_buff_size(int i, int i2, int i3);

    private static final native int native_get_output_sample_rate(int i);

    private final native PlaybackParams native_get_playback_params();

    private final native int native_get_playback_rate();

    private final native int native_get_pos_update_period();

    private final native int native_get_position();

    private final native int native_get_timestamp(long[] jArr);

    private final native int native_get_underrun_count();

    private final native void native_pause();

    private final native int native_reload_static();

    private final native int native_setAuxEffectSendLevel(float f);

    private final native boolean native_setOutputDevice(int i);

    private final native void native_setVolume(float f, float f2);

    private final native int native_set_buffer_size_frames(int i);

    private final native int native_set_loop(int i, int i2, int i3);

    private final native int native_set_marker_pos(int i);

    private final native void native_set_playback_params(PlaybackParams playbackParams);

    private final native int native_set_playback_rate(int i);

    private final native int native_set_pos_update_period(int i);

    private final native int native_set_position(int i);

    private final native int native_setup(Object obj, Object obj2, int[] iArr, int i, int i2, int i3, int i4, int i5, int[] iArr2, long j);

    private final native void native_start();

    private final native void native_stop();

    private final native int native_write_byte(byte[] bArr, int i, int i2, int i3, boolean z);

    private final native int native_write_float(float[] fArr, int i, int i2, int i3, boolean z);

    private final native int native_write_native_bytes(Object obj, int i, int i2, int i3, boolean z);

    private final native int native_write_short(short[] sArr, int i, int i2, int i3, boolean z);

    public final native void native_release();

    public AudioTrack(int streamType, int sampleRateInHz, int channelConfig, int audioFormat, int bufferSizeInBytes, int mode) throws IllegalArgumentException {
        this(streamType, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes, mode, WRITE_BLOCKING);
    }

    public AudioTrack(int streamType, int sampleRateInHz, int channelConfig, int audioFormat, int bufferSizeInBytes, int mode, int sessionId) throws IllegalArgumentException {
        this(new android.media.AudioAttributes.Builder().setLegacyStreamType(streamType).build(), new android.media.AudioFormat.Builder().setChannelMask(channelConfig).setEncoding(audioFormat).setSampleRate(sampleRateInHz).build(), bufferSizeInBytes, mode, sessionId);
    }

    public AudioTrack(AudioAttributes attributes, AudioFormat format, int bufferSizeInBytes, int mode, int sessionId) throws IllegalArgumentException {
        super(attributes);
        this.mState = WRITE_BLOCKING;
        this.mPlayState = WRITE_NON_BLOCKING;
        this.mPlayStateLock = new Object();
        this.mNativeBufferSizeInBytes = WRITE_BLOCKING;
        this.mNativeBufferSizeInFrames = WRITE_BLOCKING;
        this.mChannelCount = WRITE_NON_BLOCKING;
        this.mChannelMask = NATIVE_EVENT_NEW_POS;
        this.mStreamType = PLAYSTATE_PLAYING;
        this.mDataLoadMode = WRITE_NON_BLOCKING;
        this.mChannelConfiguration = NATIVE_EVENT_NEW_POS;
        this.mChannelIndexMask = WRITE_BLOCKING;
        this.mSessionId = WRITE_BLOCKING;
        this.mAvSyncHeader = null;
        this.mAvSyncBytesRemaining = WRITE_BLOCKING;
        this.mPreferredDevice = null;
        this.mRoutingChangeListeners = new ArrayMap();
        if (format == null) {
            throw new IllegalArgumentException("Illegal null AudioFormat");
        }
        HwMediaMonitorManager.writeMediaBigData(Process.myPid(), HwMediaMonitorManager.getStreamBigDataType(AudioAttributes.toLegacyStreamType(attributes)), "AudioTrack");
        Looper looper = Looper.myLooper();
        if (looper == null) {
            looper = Looper.getMainLooper();
        }
        int rate = format.getSampleRate();
        if (rate == 0) {
            rate = WRITE_BLOCKING;
        }
        int channelIndexMask = WRITE_BLOCKING;
        if ((format.getPropertySetMask() & 8) != 0) {
            channelIndexMask = format.getChannelIndexMask();
        }
        int channelMask = WRITE_BLOCKING;
        if ((format.getPropertySetMask() & NATIVE_EVENT_NEW_POS) != 0) {
            channelMask = format.getChannelMask();
        } else if (channelIndexMask == 0) {
            channelMask = 12;
        }
        int encoding = WRITE_NON_BLOCKING;
        if ((format.getPropertySetMask() & WRITE_NON_BLOCKING) != 0) {
            encoding = format.getEncoding();
        }
        audioParamCheck(rate, channelMask, channelIndexMask, encoding, mode);
        this.mStreamType = ERROR;
        audioBuffSizeCheck(bufferSizeInBytes);
        this.mInitializationLooper = looper;
        if (sessionId < 0) {
            throw new IllegalArgumentException("Invalid audio session ID: " + sessionId);
        }
        int[] sampleRate = new int[WRITE_NON_BLOCKING];
        sampleRate[WRITE_BLOCKING] = this.mSampleRate;
        int[] session = new int[WRITE_NON_BLOCKING];
        session[WRITE_BLOCKING] = sessionId;
        int initResult = native_setup(new WeakReference(this), this.mAttributes, sampleRate, this.mChannelMask, this.mChannelIndexMask, this.mAudioFormat, this.mNativeBufferSizeInBytes, this.mDataLoadMode, session, 0);
        if (initResult != 0) {
            loge("Error code " + initResult + " when initializing AudioTrack.");
            return;
        }
        this.mSampleRate = sampleRate[WRITE_BLOCKING];
        this.mSessionId = session[WRITE_BLOCKING];
        if (this.mDataLoadMode == 0) {
            this.mState = STATE_NO_STATIC_DATA;
        } else {
            this.mState = WRITE_NON_BLOCKING;
        }
    }

    AudioTrack(long nativeTrackInJavaObj) {
        super(new android.media.AudioAttributes.Builder().build());
        this.mState = WRITE_BLOCKING;
        this.mPlayState = WRITE_NON_BLOCKING;
        this.mPlayStateLock = new Object();
        this.mNativeBufferSizeInBytes = WRITE_BLOCKING;
        this.mNativeBufferSizeInFrames = WRITE_BLOCKING;
        this.mChannelCount = WRITE_NON_BLOCKING;
        this.mChannelMask = NATIVE_EVENT_NEW_POS;
        this.mStreamType = PLAYSTATE_PLAYING;
        this.mDataLoadMode = WRITE_NON_BLOCKING;
        this.mChannelConfiguration = NATIVE_EVENT_NEW_POS;
        this.mChannelIndexMask = WRITE_BLOCKING;
        this.mSessionId = WRITE_BLOCKING;
        this.mAvSyncHeader = null;
        this.mAvSyncBytesRemaining = WRITE_BLOCKING;
        this.mPreferredDevice = null;
        this.mRoutingChangeListeners = new ArrayMap();
        this.mNativeTrackInJavaObj = 0;
        this.mJniData = 0;
        Looper looper = Looper.myLooper();
        if (looper == null) {
            looper = Looper.getMainLooper();
        }
        this.mInitializationLooper = looper;
        if (nativeTrackInJavaObj != 0) {
            deferred_connect(nativeTrackInJavaObj);
        } else {
            this.mState = WRITE_BLOCKING;
        }
    }

    void deferred_connect(long nativeTrackInJavaObj) {
        if (this.mState != WRITE_NON_BLOCKING) {
            int[] session = new int[WRITE_NON_BLOCKING];
            session[WRITE_BLOCKING] = WRITE_BLOCKING;
            int[] rates = new int[WRITE_NON_BLOCKING];
            rates[WRITE_BLOCKING] = WRITE_BLOCKING;
            int initResult = native_setup(new WeakReference(this), null, rates, WRITE_BLOCKING, WRITE_BLOCKING, WRITE_BLOCKING, WRITE_BLOCKING, WRITE_BLOCKING, session, nativeTrackInJavaObj);
            if (initResult != 0) {
                loge("Error code " + initResult + " when initializing AudioTrack.");
            } else {
                this.mSessionId = session[WRITE_BLOCKING];
                this.mState = WRITE_NON_BLOCKING;
            }
        }
    }

    private static boolean isMultichannelConfigSupported(int channelConfig) {
        if ((channelConfig & SUPPORTED_OUT_CHANNELS) != channelConfig) {
            loge("Channel configuration features unsupported channels");
            return false;
        }
        int channelCount = AudioFormat.channelCountFromOutChannelMask(channelConfig);
        if (channelCount > CHANNEL_COUNT_MAX) {
            loge("Channel configuration contains too many channels " + channelCount + ">" + CHANNEL_COUNT_MAX);
            return false;
        } else if ((channelConfig & 12) != 12) {
            loge("Front channels must be present in multichannel configurations");
            return false;
        } else if ((channelConfig & Impl.STATUS_RUNNING) != 0 && (channelConfig & Impl.STATUS_RUNNING) != Impl.STATUS_RUNNING) {
            loge("Rear channels can't be used independently");
            return false;
        } else if ((channelConfig & GLES30.GL_COLOR) == 0 || (channelConfig & GLES30.GL_COLOR) == GLES30.GL_COLOR) {
            return true;
        } else {
            loge("Side channels can't be used independently");
            return false;
        }
    }

    private void audioBuffSizeCheck(int audioBufferSize) {
        int frameSizeInBytes;
        if (AudioFormat.isEncodingLinearFrames(this.mAudioFormat)) {
            frameSizeInBytes = this.mChannelCount * AudioFormat.getBytesPerSample(this.mAudioFormat);
        } else {
            frameSizeInBytes = WRITE_NON_BLOCKING;
        }
        if (audioBufferSize % frameSizeInBytes != 0 || audioBufferSize < WRITE_NON_BLOCKING) {
            throw new IllegalArgumentException("Invalid audio buffer size.");
        }
        this.mNativeBufferSizeInBytes = audioBufferSize;
        this.mNativeBufferSizeInFrames = audioBufferSize / frameSizeInBytes;
    }

    public void release() {
        try {
            stop();
        } catch (IllegalStateException e) {
        }
        baseRelease();
        native_release();
        this.mState = WRITE_BLOCKING;
    }

    protected void finalize() {
        baseRelease();
        native_finalize();
    }

    public static float getMinVolume() {
        return GAIN_MIN;
    }

    public static float getMaxVolume() {
        return GAIN_MAX;
    }

    public int getSampleRate() {
        return this.mSampleRate;
    }

    public int getPlaybackRate() {
        return native_get_playback_rate();
    }

    public PlaybackParams getPlaybackParams() {
        return native_get_playback_params();
    }

    public int getAudioFormat() {
        return this.mAudioFormat;
    }

    public int getStreamType() {
        return this.mStreamType;
    }

    public int getChannelConfiguration() {
        return this.mChannelConfiguration;
    }

    public AudioFormat getFormat() {
        android.media.AudioFormat.Builder builder = new android.media.AudioFormat.Builder().setSampleRate(this.mSampleRate).setEncoding(this.mAudioFormat);
        if (this.mChannelConfiguration != 0) {
            builder.setChannelMask(this.mChannelConfiguration);
        }
        if (this.mChannelIndexMask != 0) {
            builder.setChannelIndexMask(this.mChannelIndexMask);
        }
        return builder.build();
    }

    public int getChannelCount() {
        return this.mChannelCount;
    }

    public int getState() {
        return this.mState;
    }

    public int getPlayState() {
        int i;
        synchronized (this.mPlayStateLock) {
            i = this.mPlayState;
        }
        return i;
    }

    public int getBufferSizeInFrames() {
        return native_get_buffer_size_frames();
    }

    public int setBufferSizeInFrames(int bufferSizeInFrames) {
        if (this.mDataLoadMode == 0 || this.mState == 0) {
            return ERROR_INVALID_OPERATION;
        }
        if (bufferSizeInFrames < 0) {
            return ERROR_BAD_VALUE;
        }
        return native_set_buffer_size_frames(bufferSizeInFrames);
    }

    public int getBufferCapacityInFrames() {
        return native_get_buffer_capacity_frames();
    }

    @Deprecated
    protected int getNativeFrameCount() {
        return native_get_buffer_capacity_frames();
    }

    public int getNotificationMarkerPosition() {
        return native_get_marker_pos();
    }

    public int getPositionNotificationPeriod() {
        return native_get_pos_update_period();
    }

    public int getPlaybackHeadPosition() {
        return native_get_position();
    }

    public int getLatency() {
        return native_get_latency();
    }

    public int getUnderrunCount() {
        return native_get_underrun_count();
    }

    public static int getNativeOutputSampleRate(int streamType) {
        return native_get_output_sample_rate(streamType);
    }

    public static int getMinBufferSize(int sampleRateInHz, int channelConfig, int audioFormat) {
        int channelCount;
        switch (channelConfig) {
            case STATE_NO_STATIC_DATA /*2*/:
            case NATIVE_EVENT_NEW_POS /*4*/:
                channelCount = WRITE_NON_BLOCKING;
                break;
            case PLAYSTATE_PLAYING /*3*/:
            case NotificationRankerService.REASON_GROUP_SUMMARY_CANCELED /*12*/:
                channelCount = STATE_NO_STATIC_DATA;
                break;
            default:
                if (isMultichannelConfigSupported(channelConfig)) {
                    channelCount = AudioFormat.channelCountFromOutChannelMask(channelConfig);
                    break;
                }
                loge("getMinBufferSize(): Invalid channel configuration.");
                return ERROR_BAD_VALUE;
        }
        if (!AudioFormat.isPublicEncoding(audioFormat)) {
            loge("getMinBufferSize(): Invalid audio format.");
            return ERROR_BAD_VALUE;
        } else if (sampleRateInHz < AudioFormat.SAMPLE_RATE_HZ_MIN || sampleRateInHz > AudioFormat.SAMPLE_RATE_HZ_MAX) {
            loge("getMinBufferSize(): " + sampleRateInHz + " Hz is not a supported sample rate.");
            return ERROR_BAD_VALUE;
        } else {
            int size = native_get_min_buff_size(sampleRateInHz, channelCount, audioFormat);
            if (size > 0) {
                return size;
            }
            loge("getMinBufferSize(): error querying hardware");
            return ERROR;
        }
    }

    public int getAudioSessionId() {
        return this.mSessionId;
    }

    public boolean getTimestamp(AudioTimestamp timestamp) {
        if (timestamp == null) {
            throw new IllegalArgumentException();
        }
        long[] longArray = new long[STATE_NO_STATIC_DATA];
        if (native_get_timestamp(longArray) != 0) {
            return false;
        }
        timestamp.framePosition = longArray[WRITE_BLOCKING];
        timestamp.nanoTime = longArray[WRITE_NON_BLOCKING];
        return true;
    }

    public int getTimestampWithStatus(AudioTimestamp timestamp) {
        if (timestamp == null) {
            throw new IllegalArgumentException();
        }
        long[] longArray = new long[STATE_NO_STATIC_DATA];
        int ret = native_get_timestamp(longArray);
        timestamp.framePosition = longArray[WRITE_BLOCKING];
        timestamp.nanoTime = longArray[WRITE_NON_BLOCKING];
        return ret;
    }

    public void setPlaybackPositionUpdateListener(OnPlaybackPositionUpdateListener listener) {
        setPlaybackPositionUpdateListener(listener, null);
    }

    public void setPlaybackPositionUpdateListener(OnPlaybackPositionUpdateListener listener, Handler handler) {
        if (listener != null) {
            this.mEventHandlerDelegate = new NativePositionEventHandlerDelegate(this, listener, handler);
        } else {
            this.mEventHandlerDelegate = null;
        }
    }

    private static float clampGainOrLevel(float gainOrLevel) {
        if (Float.isNaN(gainOrLevel)) {
            throw new IllegalArgumentException();
        } else if (gainOrLevel < GAIN_MIN) {
            return GAIN_MIN;
        } else {
            if (gainOrLevel > GAIN_MAX) {
                return GAIN_MAX;
            }
            return gainOrLevel;
        }
    }

    @Deprecated
    public int setStereoVolume(float leftGain, float rightGain) {
        if (this.mState == 0) {
            return ERROR_INVALID_OPERATION;
        }
        baseSetVolume(leftGain, rightGain);
        return WRITE_BLOCKING;
    }

    void playerSetVolume(float leftVolume, float rightVolume) {
        native_setVolume(clampGainOrLevel(leftVolume), clampGainOrLevel(rightVolume));
    }

    public int setVolume(float gain) {
        return setStereoVolume(gain, gain);
    }

    public int setPlaybackRate(int sampleRateInHz) {
        if (this.mState != WRITE_NON_BLOCKING) {
            return ERROR_INVALID_OPERATION;
        }
        if (sampleRateInHz <= 0) {
            return ERROR_BAD_VALUE;
        }
        return native_set_playback_rate(sampleRateInHz);
    }

    public void setPlaybackParams(PlaybackParams params) {
        if (params == null) {
            throw new IllegalArgumentException("params is null");
        }
        native_set_playback_params(params);
    }

    public int setNotificationMarkerPosition(int markerInFrames) {
        if (this.mState == 0) {
            return ERROR_INVALID_OPERATION;
        }
        return native_set_marker_pos(markerInFrames);
    }

    public int setPositionNotificationPeriod(int periodInFrames) {
        if (this.mState == 0) {
            return ERROR_INVALID_OPERATION;
        }
        return native_set_pos_update_period(periodInFrames);
    }

    public int setPlaybackHeadPosition(int positionInFrames) {
        if (this.mDataLoadMode == WRITE_NON_BLOCKING || this.mState == 0 || getPlayState() == PLAYSTATE_PLAYING) {
            return ERROR_INVALID_OPERATION;
        }
        if (positionInFrames < 0 || positionInFrames > this.mNativeBufferSizeInFrames) {
            return ERROR_BAD_VALUE;
        }
        return native_set_position(positionInFrames);
    }

    public int setLoopPoints(int startInFrames, int endInFrames, int loopCount) {
        if (this.mDataLoadMode == WRITE_NON_BLOCKING || this.mState == 0 || getPlayState() == PLAYSTATE_PLAYING) {
            return ERROR_INVALID_OPERATION;
        }
        if (loopCount != 0) {
            if (startInFrames >= 0 && startInFrames < this.mNativeBufferSizeInFrames && startInFrames < endInFrames) {
                if (endInFrames > this.mNativeBufferSizeInFrames) {
                }
            }
            return ERROR_BAD_VALUE;
        }
        return native_set_loop(startInFrames, endInFrames, loopCount);
    }

    @Deprecated
    protected void setState(int state) {
        this.mState = state;
    }

    public void play() throws IllegalStateException {
        if (this.mState != WRITE_NON_BLOCKING) {
            throw new IllegalStateException("play() called on uninitialized AudioTrack.");
        }
        baseStart();
        synchronized (this.mPlayStateLock) {
            native_start();
            if (PLAYSTATE_PLAYING != this.mPlayState) {
                Log.d(TAG, "[HSM] AudioTrace play() uid: " + Process.myUid() + ", pid: " + Process.myPid());
                MediaTransactWrapper.musicPlaying(Process.myUid(), Process.myPid());
            }
            this.mPlayState = PLAYSTATE_PLAYING;
        }
    }

    public void stop() throws IllegalStateException {
        if (this.mState != WRITE_NON_BLOCKING) {
            throw new IllegalStateException("stop() called on uninitialized AudioTrack.");
        }
        synchronized (this.mPlayStateLock) {
            native_stop();
            if (WRITE_NON_BLOCKING != this.mPlayState) {
                Log.d(TAG, "[HSM] AudioTrace stop() uid: " + Process.myUid() + ", pid: " + Process.myPid());
                MediaTransactWrapper.musicPausedOrStopped(Process.myUid(), Process.myPid());
            }
            this.mPlayState = WRITE_NON_BLOCKING;
            this.mAvSyncHeader = null;
            this.mAvSyncBytesRemaining = WRITE_BLOCKING;
        }
    }

    public void pause() throws IllegalStateException {
        if (this.mState != WRITE_NON_BLOCKING) {
            throw new IllegalStateException("pause() called on uninitialized AudioTrack.");
        }
        synchronized (this.mPlayStateLock) {
            native_pause();
            if (STATE_NO_STATIC_DATA != this.mPlayState) {
                Log.d(TAG, "[HSM] AudioTrace pause() uid: " + Process.myUid() + ", pid: " + Process.myPid());
                MediaTransactWrapper.musicPausedOrStopped(Process.myUid(), Process.myPid());
            }
            this.mPlayState = STATE_NO_STATIC_DATA;
        }
    }

    public void flush() {
        if (this.mState == WRITE_NON_BLOCKING) {
            native_flush();
            this.mAvSyncHeader = null;
            this.mAvSyncBytesRemaining = WRITE_BLOCKING;
        }
    }

    public int write(byte[] audioData, int offsetInBytes, int sizeInBytes) {
        return write(audioData, offsetInBytes, sizeInBytes, (int) WRITE_BLOCKING);
    }

    public int write(byte[] audioData, int offsetInBytes, int sizeInBytes, int writeMode) {
        boolean z = false;
        if (this.mState == 0 || this.mAudioFormat == NATIVE_EVENT_NEW_POS) {
            return ERROR_INVALID_OPERATION;
        }
        if (writeMode != 0 && writeMode != WRITE_NON_BLOCKING) {
            Log.e(TAG, "AudioTrack.write() called with invalid blocking mode");
            return ERROR_BAD_VALUE;
        } else if (audioData == null || offsetInBytes < 0 || sizeInBytes < 0 || offsetInBytes + sizeInBytes < 0 || offsetInBytes + sizeInBytes > audioData.length) {
            return ERROR_BAD_VALUE;
        } else {
            int i = this.mAudioFormat;
            if (writeMode == 0) {
                z = true;
            }
            int ret = native_write_byte(audioData, offsetInBytes, sizeInBytes, i, z);
            if (this.mDataLoadMode == 0 && this.mState == STATE_NO_STATIC_DATA && ret > 0) {
                this.mState = WRITE_NON_BLOCKING;
            }
            return ret;
        }
    }

    public int write(short[] audioData, int offsetInShorts, int sizeInShorts) {
        return write(audioData, offsetInShorts, sizeInShorts, (int) WRITE_BLOCKING);
    }

    public int write(short[] audioData, int offsetInShorts, int sizeInShorts, int writeMode) {
        boolean z = false;
        if (this.mState == 0 || this.mAudioFormat == NATIVE_EVENT_NEW_POS) {
            return ERROR_INVALID_OPERATION;
        }
        if (writeMode != 0 && writeMode != WRITE_NON_BLOCKING) {
            Log.e(TAG, "AudioTrack.write() called with invalid blocking mode");
            return ERROR_BAD_VALUE;
        } else if (audioData == null || offsetInShorts < 0 || sizeInShorts < 0 || offsetInShorts + sizeInShorts < 0 || offsetInShorts + sizeInShorts > audioData.length) {
            return ERROR_BAD_VALUE;
        } else {
            int i = this.mAudioFormat;
            if (writeMode == 0) {
                z = true;
            }
            int ret = native_write_short(audioData, offsetInShorts, sizeInShorts, i, z);
            if (this.mDataLoadMode == 0 && this.mState == STATE_NO_STATIC_DATA && ret > 0) {
                this.mState = WRITE_NON_BLOCKING;
            }
            return ret;
        }
    }

    public int write(float[] audioData, int offsetInFloats, int sizeInFloats, int writeMode) {
        boolean z = false;
        if (this.mState == 0) {
            Log.e(TAG, "AudioTrack.write() called in invalid state STATE_UNINITIALIZED");
            return ERROR_INVALID_OPERATION;
        } else if (this.mAudioFormat != NATIVE_EVENT_NEW_POS) {
            Log.e(TAG, "AudioTrack.write(float[] ...) requires format ENCODING_PCM_FLOAT");
            return ERROR_INVALID_OPERATION;
        } else if (writeMode != 0 && writeMode != WRITE_NON_BLOCKING) {
            Log.e(TAG, "AudioTrack.write() called with invalid blocking mode");
            return ERROR_BAD_VALUE;
        } else if (audioData == null || offsetInFloats < 0 || sizeInFloats < 0 || offsetInFloats + sizeInFloats < 0 || offsetInFloats + sizeInFloats > audioData.length) {
            Log.e(TAG, "AudioTrack.write() called with invalid array, offset, or size");
            return ERROR_BAD_VALUE;
        } else {
            int i = this.mAudioFormat;
            if (writeMode == 0) {
                z = true;
            }
            int ret = native_write_float(audioData, offsetInFloats, sizeInFloats, i, z);
            if (this.mDataLoadMode == 0 && this.mState == STATE_NO_STATIC_DATA && ret > 0) {
                this.mState = WRITE_NON_BLOCKING;
            }
            return ret;
        }
    }

    public int write(ByteBuffer audioData, int sizeInBytes, int writeMode) {
        boolean z = false;
        if (this.mState == 0) {
            Log.e(TAG, "AudioTrack.write() called in invalid state STATE_UNINITIALIZED");
            return ERROR_INVALID_OPERATION;
        } else if (writeMode != 0 && writeMode != WRITE_NON_BLOCKING) {
            Log.e(TAG, "AudioTrack.write() called with invalid blocking mode");
            return ERROR_BAD_VALUE;
        } else if (audioData == null || sizeInBytes < 0 || sizeInBytes > audioData.remaining()) {
            Log.e(TAG, "AudioTrack.write() called with invalid size (" + sizeInBytes + ") value");
            return ERROR_BAD_VALUE;
        } else {
            int ret;
            int position;
            int i;
            if (audioData.isDirect()) {
                position = audioData.position();
                i = this.mAudioFormat;
                if (writeMode == 0) {
                    z = true;
                }
                ret = native_write_native_bytes(audioData, position, sizeInBytes, i, z);
            } else {
                byte[] unsafeArray = NioUtils.unsafeArray(audioData);
                position = audioData.position() + NioUtils.unsafeArrayOffset(audioData);
                i = this.mAudioFormat;
                if (writeMode == 0) {
                    z = true;
                }
                ret = native_write_byte(unsafeArray, position, sizeInBytes, i, z);
            }
            if (this.mDataLoadMode == 0 && this.mState == STATE_NO_STATIC_DATA && ret > 0) {
                this.mState = WRITE_NON_BLOCKING;
            }
            if (ret > 0) {
                audioData.position(audioData.position() + ret);
            }
            return ret;
        }
    }

    public int write(ByteBuffer audioData, int sizeInBytes, int writeMode, long timestamp) {
        if (this.mState == 0) {
            Log.e(TAG, "AudioTrack.write() called in invalid state STATE_UNINITIALIZED");
            return ERROR_INVALID_OPERATION;
        } else if (writeMode != 0 && writeMode != WRITE_NON_BLOCKING) {
            Log.e(TAG, "AudioTrack.write() called with invalid blocking mode");
            return ERROR_BAD_VALUE;
        } else if (this.mDataLoadMode != WRITE_NON_BLOCKING) {
            Log.e(TAG, "AudioTrack.write() with timestamp called for non-streaming mode track");
            return ERROR_INVALID_OPERATION;
        } else if ((this.mAttributes.getFlags() & 16) == 0) {
            Log.d(TAG, "AudioTrack.write() called on a regular AudioTrack. Ignoring pts...");
            return write(audioData, sizeInBytes, writeMode);
        } else if (audioData == null || sizeInBytes < 0 || sizeInBytes > audioData.remaining()) {
            Log.e(TAG, "AudioTrack.write() called with invalid size (" + sizeInBytes + ") value");
            return ERROR_BAD_VALUE;
        } else {
            int ret;
            if (this.mAvSyncHeader == null) {
                this.mAvSyncHeader = ByteBuffer.allocate(16);
                this.mAvSyncHeader.order(ByteOrder.BIG_ENDIAN);
                this.mAvSyncHeader.putInt(1431633921);
                this.mAvSyncHeader.putInt(sizeInBytes);
                this.mAvSyncHeader.putLong(timestamp);
                this.mAvSyncHeader.position(WRITE_BLOCKING);
                this.mAvSyncBytesRemaining = sizeInBytes;
            }
            if (this.mAvSyncHeader.remaining() != 0) {
                ret = write(this.mAvSyncHeader, this.mAvSyncHeader.remaining(), writeMode);
                if (ret < 0) {
                    Log.e(TAG, "AudioTrack.write() could not write timestamp header!");
                    this.mAvSyncHeader = null;
                    this.mAvSyncBytesRemaining = WRITE_BLOCKING;
                    return ret;
                } else if (this.mAvSyncHeader.remaining() > 0) {
                    Log.v(TAG, "AudioTrack.write() partial timestamp header written.");
                    return WRITE_BLOCKING;
                }
            }
            ret = write(audioData, Math.min(this.mAvSyncBytesRemaining, sizeInBytes), writeMode);
            if (ret < 0) {
                Log.e(TAG, "AudioTrack.write() could not write audio data!");
                this.mAvSyncHeader = null;
                this.mAvSyncBytesRemaining = WRITE_BLOCKING;
                return ret;
            }
            this.mAvSyncBytesRemaining -= ret;
            if (this.mAvSyncBytesRemaining == 0) {
                this.mAvSyncHeader = null;
            }
            return ret;
        }
    }

    public int reloadStaticData() {
        if (this.mDataLoadMode == WRITE_NON_BLOCKING || this.mState != WRITE_NON_BLOCKING) {
            return ERROR_INVALID_OPERATION;
        }
        return native_reload_static();
    }

    public int attachAuxEffect(int effectId) {
        if (this.mState == 0) {
            return ERROR_INVALID_OPERATION;
        }
        return native_attachAuxEffect(effectId);
    }

    public int setAuxEffectSendLevel(float level) {
        if (this.mState == 0) {
            return ERROR_INVALID_OPERATION;
        }
        return baseSetAuxEffectSendLevel(level);
    }

    int playerSetAuxEffectSendLevel(float level) {
        if (native_setAuxEffectSendLevel(clampGainOrLevel(level)) == 0) {
            return WRITE_BLOCKING;
        }
        return ERROR;
    }

    public boolean setPreferredDevice(AudioDeviceInfo deviceInfo) {
        int preferredDeviceId = WRITE_BLOCKING;
        if (deviceInfo != null && !deviceInfo.isSink()) {
            return false;
        }
        if (deviceInfo != null) {
            preferredDeviceId = deviceInfo.getId();
        }
        boolean status = native_setOutputDevice(preferredDeviceId);
        if (status) {
            synchronized (this) {
                this.mPreferredDevice = deviceInfo;
            }
        }
        return status;
    }

    public AudioDeviceInfo getPreferredDevice() {
        AudioDeviceInfo audioDeviceInfo;
        synchronized (this) {
            audioDeviceInfo = this.mPreferredDevice;
        }
        return audioDeviceInfo;
    }

    public AudioDeviceInfo getRoutedDevice() {
        int deviceId = native_getRoutedDeviceId();
        if (deviceId == 0) {
            return null;
        }
        AudioDeviceInfo[] devices = AudioManager.getDevicesStatic(STATE_NO_STATIC_DATA);
        for (int i = WRITE_BLOCKING; i < devices.length; i += WRITE_NON_BLOCKING) {
            if (devices[i].getId() == deviceId) {
                return devices[i];
            }
        }
        return null;
    }

    private void testEnableNativeRoutingCallbacksLocked() {
        if (this.mRoutingChangeListeners.size() == 0) {
            native_enableDeviceCallback();
        }
    }

    private void testDisableNativeRoutingCallbacksLocked() {
        if (this.mRoutingChangeListeners.size() == 0) {
            native_disableDeviceCallback();
        }
    }

    public void addOnRoutingChangedListener(android.media.AudioRouting.OnRoutingChangedListener listener, Handler handler) {
        synchronized (this.mRoutingChangeListeners) {
            if (listener != null) {
                if (!this.mRoutingChangeListeners.containsKey(listener)) {
                    testEnableNativeRoutingCallbacksLocked();
                    ArrayMap arrayMap = this.mRoutingChangeListeners;
                    if (handler == null) {
                        handler = new Handler(this.mInitializationLooper);
                    }
                    arrayMap.put(listener, new NativeRoutingEventHandlerDelegate(this, listener, handler));
                }
            }
        }
    }

    public void removeOnRoutingChangedListener(android.media.AudioRouting.OnRoutingChangedListener listener) {
        synchronized (this.mRoutingChangeListeners) {
            if (this.mRoutingChangeListeners.containsKey(listener)) {
                this.mRoutingChangeListeners.remove(listener);
            }
            testDisableNativeRoutingCallbacksLocked();
        }
    }

    @Deprecated
    public void addOnRoutingChangedListener(OnRoutingChangedListener listener, Handler handler) {
        addOnRoutingChangedListener((android.media.AudioRouting.OnRoutingChangedListener) listener, handler);
    }

    @Deprecated
    public void removeOnRoutingChangedListener(OnRoutingChangedListener listener) {
        removeOnRoutingChangedListener((android.media.AudioRouting.OnRoutingChangedListener) listener);
    }

    private void broadcastRoutingChange() {
        AudioManager.resetAudioPortGeneration();
        synchronized (this.mRoutingChangeListeners) {
            for (NativeRoutingEventHandlerDelegate delegate : this.mRoutingChangeListeners.values()) {
                Handler handler = delegate.getHandler();
                if (handler != null) {
                    handler.sendEmptyMessage(Process.SYSTEM_UID);
                }
            }
        }
    }

    private static void postEventFromNative(Object audiotrack_ref, int what, int arg1, int arg2, Object obj) {
        AudioTrack track = (AudioTrack) ((WeakReference) audiotrack_ref).get();
        if (track != null) {
            if (what == Process.SYSTEM_UID) {
                track.broadcastRoutingChange();
                return;
            }
            NativePositionEventHandlerDelegate delegate = track.mEventHandlerDelegate;
            if (delegate != null) {
                Handler handler = delegate.getHandler();
                if (handler != null) {
                    handler.sendMessage(handler.obtainMessage(what, arg1, arg2, obj));
                }
            }
        }
    }

    private static void logd(String msg) {
        Log.d(TAG, msg);
    }

    private static void loge(String msg) {
        Log.e(TAG, msg);
    }
}
