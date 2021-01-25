package ohos.media.audio;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Locale;
import ohos.app.Context;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.eventhandler.InnerEvent;
import ohos.global.resource.BaseFileDescriptor;
import ohos.global.resource.NotExistException;
import ohos.global.resource.RawFileDescriptor;
import ohos.global.resource.WrongTypeException;
import ohos.media.audio.AudioRenderer;
import ohos.media.audio.AudioRendererInfo;
import ohos.media.audio.AudioStreamInfo;
import ohos.media.audio.ToneDescriptor;
import ohos.media.audiofwk.DtmfCreater;
import ohos.media.audioimpl.adapter.AudioRemoteAdapterException;
import ohos.media.audioimpl.adapter.AudioServiceAdapter;
import ohos.media.audioimpl.adapter.SoundPlayerAdapter;
import ohos.media.camera.mode.adapter.utils.constant.ConstantValue;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.media.utils.trace.Tracer;
import ohos.media.utils.trace.TracerFactory;

public class SoundPlayer {
    private static final int DTMF_SAMPLERATE = 48000;
    private static final Logger LOGGER = LoggerFactory.getAudioLogger(SoundPlayer.class);
    private static final int MAX_DURATION = 10000;
    private static final int MAX_SOUND_EFFECTS_NUM = 10;
    private static final int SOUND_CACHE_CREATED = 1;
    private static final Tracer TRACER = TracerFactory.getAudioTracer();
    private AudioServiceAdapter audioServiceAdapter;
    private DtmfCreater creater;
    private SoundPlayerEventHandler eventHandler;
    private final Object lock;
    private long nativeSoundPlayer;
    private OnCreateCompleteListener onCreateCompleteListener;
    private SoundPlayerAdapter playerAdapter;
    private AudioRenderer renderer;

    public interface OnCreateCompleteListener {
        void onCreateComplete(SoundPlayer soundPlayer, int i, int i2);
    }

    private native int nativeCreateSound(FileDescriptor fileDescriptor, long j, long j2);

    private native int nativeCreateSound(String str, int i, int i2, int i3);

    private native boolean nativeDeleteSound(int i);

    private native boolean nativePause(int i);

    private native boolean nativePauseAll();

    private native int nativePlay(int i, SoundPlayerParameters soundPlayerParameters);

    private native boolean nativeRelease();

    private native boolean nativeResume(int i);

    private native boolean nativeResumeAll();

    private native boolean nativeSetLoop(int i, int i2);

    private native boolean nativeSetPlaySpeedRate(int i, float f);

    private native boolean nativeSetPriority(int i, int i2);

    private native boolean nativeSetVolume(int i, AudioVolumes audioVolumes);

    private native int nativeSetup(Object obj, int i);

    private native boolean nativeStop(int i);

    static {
        System.loadLibrary("soundplayer_jni.z");
    }

    public enum SoundType {
        KEY_CLICK(0),
        NAVIGATION_UP(1),
        NAVIGATION_DOWN(2),
        NAVIGATION_LEFT(3),
        NAVIGATION_RIGHT(4),
        KEYPRESS_STANDARD(5),
        KEYPRESS_SPACEBAR(6),
        KEYPRESS_DELETE(7),
        KEYPRESS_RETURN(8),
        KEYPRESS_INVALID(9);
        
        private final int typeValue;

        private SoundType(int i) {
            this.typeValue = i;
        }

        public int getTypeValue() {
            return this.typeValue;
        }
    }

    public SoundPlayer() {
        this.lock = new Object();
        this.playerAdapter = new SoundPlayerAdapter();
    }

    public SoundPlayer(String str) {
        this.lock = new Object();
        this.audioServiceAdapter = new AudioServiceAdapter(str);
    }

    public SoundPlayer(int i) {
        this.lock = new Object();
        String format = String.format(Locale.ROOT, "SoundPlayer_Construct_kits:taskType=%d", Integer.valueOf(i));
        TRACER.startTrace(format);
        nativeSetup(new WeakReference(this), i);
        TRACER.finishTrace(format);
    }

    public boolean playSound(SoundType soundType) {
        if (soundType == null) {
            LOGGER.error("playSound type is null.", new Object[0]);
            return false;
        }
        int typeValue = soundType.getTypeValue();
        if (this.audioServiceAdapter == null) {
            LOGGER.error("Audio service adapter is null.", new Object[0]);
            return false;
        }
        String format = String.format(Locale.ROOT, "SoundPlayer_playSoundEffect_kits:type=%s", soundType);
        TRACER.startTrace(format);
        try {
            this.audioServiceAdapter.playSoundEffect(typeValue);
            TRACER.finishTrace(format);
            return true;
        } catch (AudioRemoteAdapterException e) {
            TRACER.finishTrace(format);
            LOGGER.error("Public playSoundEffect error: %{public}s.", e.toString());
            return false;
        }
    }

    public boolean playSound(SoundType soundType, float f) {
        if (soundType == null) {
            LOGGER.error("playSound type is null.", new Object[0]);
            return false;
        }
        int typeValue = soundType.getTypeValue();
        if (this.audioServiceAdapter == null) {
            LOGGER.error("Audio service adapter is null.", new Object[0]);
            return false;
        }
        String format = String.format(Locale.ROOT, "SoundPlayer_playSoundEffect_kits:type=%s,volume=%s", soundType, Float.valueOf(f));
        TRACER.startTrace(format);
        try {
            this.audioServiceAdapter.playSoundEffect(typeValue, f);
            TRACER.finishTrace(format);
            return true;
        } catch (AudioRemoteAdapterException e) {
            TRACER.finishTrace(format);
            LOGGER.error("Public playSoundEffect error: %{public}s.", e.toString());
            return false;
        }
    }

    public final boolean createSound(ToneDescriptor.ToneType toneType, int i) {
        if (i > MAX_DURATION || i <= 0) {
            LOGGER.error("Duration milliseconds is invalid, durationMs: %{public}d.", Integer.valueOf(i));
            return false;
        }
        ToneDescriptor toneDescriptor = new ToneDescriptor(toneType);
        if (toneDescriptor.getHighFrequency() == 0.0d) {
            LOGGER.error("Create ToneDescriptor failed.", new Object[0]);
            return false;
        }
        this.creater = new DtmfCreater(DTMF_SAMPLERATE, toneDescriptor);
        String format = String.format(Locale.ROOT, "SoundPlayer_createSound_ToneType_kits:type=%s,duration=%d", toneType, Integer.valueOf(i));
        TRACER.startTrace(format);
        if (!this.creater.createSamples(i)) {
            LOGGER.error("Create samples failed, durationMs: %{public}d.", Integer.valueOf(i));
            TRACER.finishTrace(format);
            return false;
        }
        try {
            this.renderer = new AudioRenderer(new AudioRendererInfo.Builder().audioStreamInfo(new AudioStreamInfo.Builder().encodingFormat(AudioStreamInfo.EncodingFormat.ENCODING_PCM_16BIT).channelMask(AudioStreamInfo.ChannelMask.CHANNEL_OUT_STEREO).sampleRate(DTMF_SAMPLERATE).build()).bufferSizeInBytes((long) (this.creater.getData().length * 2)).build(), AudioRenderer.PlayMode.MODE_STATIC);
            TRACER.finishTrace(format);
            return true;
        } catch (IllegalArgumentException unused) {
            LOGGER.error("Set AudioStreamInfo failed.", new Object[0]);
            TRACER.finishTrace(format);
            return false;
        }
    }

    public final boolean createSound(AudioStreamInfo.StreamType streamType, float f) {
        if (this.playerAdapter == null) {
            LOGGER.error("playerAdapter is null, return", new Object[0]);
            return false;
        } else if (Float.compare(f, ConstantValue.MIN_ZOOM_VALUE) != -1 && Float.compare(f, 1.0f) != 1) {
            return this.playerAdapter.createTone(streamType, f);
        } else {
            LOGGER.error("invalid volume, volume must be ranges from 0.0 to 1.0", new Object[0]);
            return false;
        }
    }

    public final int createSound(String str) {
        return createSound(str, new AudioRendererInfo.Builder().audioStreamInfo(new AudioStreamInfo.Builder().build()).build());
    }

    public final int createSound(BaseFileDescriptor baseFileDescriptor) {
        if (baseFileDescriptor == null) {
            LOGGER.error("create sound failed, baseFileDescriptor is null", new Object[0]);
            return 0;
        }
        FileDescriptor fileDescriptor = baseFileDescriptor.getFileDescriptor();
        if (fileDescriptor == null) {
            LOGGER.error("create sound failed, getFileDescriptor return null", new Object[0]);
            return 0;
        }
        long startPosition = baseFileDescriptor.getStartPosition();
        if (startPosition < 0) {
            LOGGER.error("create sound failed, fd offset is invalid", new Object[0]);
            return 0;
        }
        long fileSize = baseFileDescriptor.getFileSize();
        if (fileSize > 0) {
            return nativeCreateSound(fileDescriptor, startPosition, fileSize);
        }
        LOGGER.error("create sound failed, fd length is invalid", new Object[0]);
        return 0;
    }

    public final int createSound(Context context, int i) {
        if (context == null || context.getResourceManager() == null) {
            LOGGER.error("create sound failed, invalid context", new Object[0]);
            return 0;
        }
        RawFileDescriptor rawFileDescriptor = null;
        try {
            rawFileDescriptor = context.getResourceManager().getRawFileEntry(context.getResourceManager().getElement(i).getString()).openRawFileDescriptor();
            int createSound = createSound(rawFileDescriptor);
            if (rawFileDescriptor != null) {
                try {
                    rawFileDescriptor.close();
                } catch (IOException unused) {
                    LOGGER.error("file descriptor close failed.", new Object[0]);
                }
            }
            return createSound;
        } catch (IOException unused2) {
            LOGGER.error("create sound failed, invalid resource", new Object[0]);
            if (rawFileDescriptor != null) {
                try {
                    rawFileDescriptor.close();
                } catch (IOException unused3) {
                    LOGGER.error("file descriptor close failed.", new Object[0]);
                }
            }
            return 0;
        } catch (NotExistException unused4) {
            LOGGER.error("create sound failed, resource is not exist", new Object[0]);
            if (rawFileDescriptor != null) {
                try {
                    rawFileDescriptor.close();
                } catch (IOException unused5) {
                    LOGGER.error("file descriptor close failed.", new Object[0]);
                }
            }
            return 0;
        } catch (WrongTypeException unused6) {
            LOGGER.error("create sound failed, invalid resource type", new Object[0]);
            if (rawFileDescriptor != null) {
                try {
                    rawFileDescriptor.close();
                } catch (IOException unused7) {
                    LOGGER.error("file descriptor close failed.", new Object[0]);
                }
            }
            return 0;
        } catch (Throwable th) {
            if (rawFileDescriptor != null) {
                try {
                    rawFileDescriptor.close();
                } catch (IOException unused8) {
                    LOGGER.error("file descriptor close failed.", new Object[0]);
                }
            }
            throw th;
        }
    }

    public final int createSound(FileDescriptor fileDescriptor, long j, long j2) {
        if (fileDescriptor != null && j >= 0 && j2 > 0) {
            return nativeCreateSound(fileDescriptor, j, j2);
        }
        LOGGER.error("create sound failed, invalid parameters", new Object[0]);
        return 0;
    }

    public final int createSound(String str, AudioRendererInfo audioRendererInfo) {
        if (str == null || str.isEmpty()) {
            LOGGER.error("Create sound failed, path is invalid.", new Object[0]);
            return 0;
        } else if (audioRendererInfo == null || audioRendererInfo.getAudioStreamInfo() == null) {
            LOGGER.error("Create sound failed, rendererInfo is invalid.", new Object[0]);
            return 0;
        } else {
            AudioStreamInfo audioStreamInfo = audioRendererInfo.getAudioStreamInfo();
            AudioStreamInfo.StreamUsage usage = audioStreamInfo.getUsage();
            AudioStreamInfo.ContentType contentType = audioStreamInfo.getContentType();
            AudioStreamInfo.AudioStreamFlag audioStreamFlag = audioStreamInfo.getAudioStreamFlag();
            if (usage == null || contentType == null || audioStreamFlag == null) {
                LOGGER.error("Create sound failed, streamInfo is invalid.", new Object[0]);
                return 0;
            }
            String format = String.format(Locale.ROOT, "SoundPlayer_createSound_path_kits:path=%s", str);
            TRACER.startTrace(format);
            int nativeCreateSound = nativeCreateSound(str, usage.getValue(), contentType.getValue(), audioStreamFlag.getValue());
            TRACER.finishTrace(format);
            return nativeCreateSound;
        }
    }

    public final boolean deleteSound(int i) {
        String format = String.format(Locale.ROOT, "SoundPlayer_deleteSound_kits:soundId=%d", Integer.valueOf(i));
        TRACER.startTrace(format);
        boolean nativeDeleteSound = nativeDeleteSound(i);
        TRACER.finishTrace(format);
        return nativeDeleteSound;
    }

    public final boolean play(ToneDescriptor.ToneType toneType, int i) {
        SoundPlayerAdapter soundPlayerAdapter = this.playerAdapter;
        if (soundPlayerAdapter != null) {
            return soundPlayerAdapter.playTone(toneType, i);
        }
        LOGGER.error("playerAdapter is null, return", new Object[0]);
        return false;
    }

    public final boolean play() {
        DtmfCreater dtmfCreater = this.creater;
        if (dtmfCreater == null || !dtmfCreater.getIsSamplesReady() || this.renderer == null) {
            LOGGER.error("Tone samples is not ready.", new Object[0]);
            return false;
        }
        TRACER.startTrace("SoundPlayer_play_kits");
        if (!this.renderer.write(this.creater.getData(), 0, this.creater.getData().length)) {
            LOGGER.error("AudioRenderer write data failed, data length is %{public}d", Integer.valueOf(this.creater.getData().length));
            TRACER.finishTrace("SoundPlayer_play_kits");
            return false;
        } else if (!this.renderer.start()) {
            LOGGER.error("AudioRenderer play failed.", new Object[0]);
            TRACER.finishTrace("SoundPlayer_play_kits");
            return false;
        } else {
            TRACER.finishTrace("SoundPlayer_play_kits");
            return true;
        }
    }

    public final int play(int i) {
        SoundPlayerParameters soundPlayerParameters = new SoundPlayerParameters();
        String format = String.format(Locale.ROOT, "SoundPlayer_nativePlay_kits:soundId=%d", Integer.valueOf(i));
        TRACER.startTrace(format);
        int nativePlay = nativePlay(i, soundPlayerParameters);
        TRACER.finishTrace(format);
        return nativePlay;
    }

    public final int play(int i, SoundPlayerParameters soundPlayerParameters) {
        String format = String.format(Locale.ROOT, "SoundPlayer_nativePlay_withParam_kits:soundId=%d", Integer.valueOf(i));
        TRACER.startTrace(format);
        int nativePlay = nativePlay(i, soundPlayerParameters);
        TRACER.finishTrace(format);
        return nativePlay;
    }

    public final boolean pause() {
        if (this.renderer == null) {
            LOGGER.error("Pause failed because the renderer is null.", new Object[0]);
            return false;
        }
        TRACER.startTrace("SoundPlayer_pause_kits");
        boolean pause = this.renderer.pause();
        TRACER.finishTrace("SoundPlayer_pause_kits");
        return pause;
    }

    public final boolean pause(int i) {
        return nativePause(i);
    }

    public final boolean resume(int i) {
        return nativeResume(i);
    }

    public final boolean stop(int i) {
        return nativeStop(i);
    }

    public final boolean release() {
        SoundPlayerAdapter soundPlayerAdapter = this.playerAdapter;
        boolean release = soundPlayerAdapter != null ? soundPlayerAdapter.release() : false;
        AudioRenderer audioRenderer = this.renderer;
        if (audioRenderer != null) {
            return audioRenderer.release();
        }
        return (this.playerAdapter == null && audioRenderer == null) ? nativeRelease() : release;
    }

    public final boolean setVolume(int i, AudioVolumes audioVolumes) {
        String format = String.format(Locale.ROOT, "SoundPlayer_setVolume_kits:taskId=%d", Integer.valueOf(i));
        TRACER.startTrace(format);
        boolean nativeSetVolume = nativeSetVolume(i, audioVolumes);
        TRACER.finishTrace(format);
        return nativeSetVolume;
    }

    public final boolean setVolume(int i, float f) {
        AudioVolumes audioVolumes = new AudioVolumes();
        audioVolumes.setFrontLeftVolume(f);
        audioVolumes.setFrontRightVolume(f);
        audioVolumes.setRearLeftVolume(f);
        audioVolumes.setRearRightVolume(f);
        audioVolumes.setCentralVolume(f);
        audioVolumes.setSubwooferVolume(f);
        return setVolume(i, audioVolumes);
    }

    public final boolean setPriority(int i, int i2) {
        String format = String.format(Locale.ROOT, "SoundPlayer_setPriority_kits:taskId=%d,priority=%d", Integer.valueOf(i), Integer.valueOf(i2));
        TRACER.startTrace(format);
        boolean nativeSetPriority = nativeSetPriority(i, i2);
        TRACER.finishTrace(format);
        return nativeSetPriority;
    }

    public final boolean setPlaySpeedRate(int i, float f) {
        String format = String.format(Locale.ROOT, "SoundPlayer_setPlaySpeedRate_kits:taskId=%d,speedRate=%f", Integer.valueOf(i), Float.valueOf(f));
        TRACER.startTrace(format);
        boolean nativeSetPlaySpeedRate = nativeSetPlaySpeedRate(i, f);
        TRACER.finishTrace(format);
        return nativeSetPlaySpeedRate;
    }

    public final boolean setLoop(int i, int i2) {
        String format = String.format(Locale.ROOT, "SoundPlayer_setLoop_kits:taskId=%d,loopNum=%d", Integer.valueOf(i), Integer.valueOf(i2));
        TRACER.startTrace(format);
        boolean nativeSetLoop = nativeSetLoop(i, i2);
        TRACER.finishTrace(format);
        return nativeSetLoop;
    }

    public final boolean pauseAll() {
        return nativePauseAll();
    }

    public final boolean resumeAll() {
        return nativeResumeAll();
    }

    public boolean setOnCreateCompleteListener(OnCreateCompleteListener onCreateCompleteListener2) {
        synchronized (this.lock) {
            if (onCreateCompleteListener2 != null) {
                EventRunner current = EventRunner.current();
                if (current != null) {
                    this.onCreateCompleteListener = onCreateCompleteListener2;
                    this.eventHandler = new SoundPlayerEventHandler(current);
                    return true;
                }
            }
            this.eventHandler = null;
            this.onCreateCompleteListener = onCreateCompleteListener2;
            return false;
        }
    }

    public boolean setOnCreateCompleteListener(OnCreateCompleteListener onCreateCompleteListener2, boolean z) {
        if (z) {
            return setOnCreateCompleteListener(onCreateCompleteListener2);
        }
        synchronized (this.lock) {
            if (onCreateCompleteListener2 == null) {
                return false;
            }
            if (this.eventHandler == null) {
                EventRunner current = EventRunner.current();
                if (current == null) {
                    return false;
                }
                this.eventHandler = new SoundPlayerEventHandler(current);
            }
            this.onCreateCompleteListener = onCreateCompleteListener2;
            return true;
        }
    }

    private static void postNativeEvent(Object obj, int i, int i2, int i3) {
        if (obj == null) {
            LOGGER.error("nativeRef is null, return", new Object[0]);
        } else if (!(obj instanceof WeakReference)) {
            LOGGER.error("nativeRef is not instance of WeakReference, return", new Object[0]);
        } else {
            SoundPlayer soundPlayer = (SoundPlayer) ((WeakReference) obj).get();
            if (soundPlayer == null) {
                LOGGER.error("soundPlayer is null, return", new Object[0]);
            } else if (soundPlayer.eventHandler == null) {
                LOGGER.warn("eventHandler is not set, return", new Object[0]);
            } else {
                soundPlayer.eventHandler.sendEvent(InnerEvent.get(i, new SoundCacheEventParam(i2, i3)));
            }
        }
    }

    private static final class SoundCacheEventParam {
        private final int soundCacheId;
        private final int status;

        private SoundCacheEventParam(int i, int i2) {
            this.soundCacheId = i;
            this.status = i2;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int getSoundCacheId() {
            return this.soundCacheId;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int getStatus() {
            return this.status;
        }
    }

    /* access modifiers changed from: private */
    public final class SoundPlayerEventHandler extends EventHandler {
        public SoundPlayerEventHandler(EventRunner eventRunner) {
            super(eventRunner);
        }

        public void processEvent(InnerEvent innerEvent) {
            if (innerEvent == null) {
                SoundPlayer.LOGGER.warn("event from native is null", new Object[0]);
            } else if (!(innerEvent.object instanceof SoundCacheEventParam)) {
                SoundPlayer.LOGGER.warn("event object is not instance of SoundCacheEventParam", new Object[0]);
            } else {
                int i = innerEvent.eventId;
                SoundCacheEventParam soundCacheEventParam = (SoundCacheEventParam) innerEvent.object;
                int soundCacheId = soundCacheEventParam.getSoundCacheId();
                int status = soundCacheEventParam.getStatus();
                if (i != 1) {
                    SoundPlayer.LOGGER.error("Unexpected message %{public}d", Integer.valueOf(i));
                    return;
                }
                synchronized (SoundPlayer.this.lock) {
                    if (SoundPlayer.this.onCreateCompleteListener != null) {
                        SoundPlayer.this.onCreateCompleteListener.onCreateComplete(SoundPlayer.this, soundCacheId, status);
                    }
                }
            }
        }
    }

    public static class SoundPlayerParameters {
        public static final float NORMAL_SPEED_RATE = 1.0f;
        public static final int NO_LOOP = 0;
        public static final int PLAY_DELAYED_PRIORITY = 1;
        public static final int PLAY_IMMEDIATELY_PRIORITY = 0;
        private AudioVolumes audioVolumes = new AudioVolumes();
        private int loop = 0;
        private float playSpeedRate = 1.0f;
        private int priority = 0;

        public AudioVolumes getAudioVolumes() {
            return this.audioVolumes;
        }

        public void setVolumes(AudioVolumes audioVolumes2) {
            this.audioVolumes = audioVolumes2;
        }

        public int getPriority() {
            return this.priority;
        }

        public void setPriority(int i) {
            this.priority = i;
        }

        public int getLoop() {
            return this.loop;
        }

        public void setLoop(int i) {
            this.loop = i;
        }

        public float getSpeed() {
            return this.playSpeedRate;
        }

        public void setSpeed(float f) {
            this.playSpeedRate = f;
        }
    }

    public static class AudioVolumes {
        private static final float DEFAULT_VOLUME = 1.0f;
        private float centralVolume = 1.0f;
        private float frontLeftVolume = 1.0f;
        private float frontRightVolume = 1.0f;
        private float rearLeftVolume = 1.0f;
        private float rearRightVolume = 1.0f;
        private float subwooferVolume = 1.0f;

        public float getFrontLeftVolume() {
            return this.frontLeftVolume;
        }

        public void setFrontLeftVolume(float f) {
            this.frontLeftVolume = f;
        }

        public float getFrontRightVolume() {
            return this.frontRightVolume;
        }

        public void setFrontRightVolume(float f) {
            this.frontRightVolume = f;
        }

        public float getRearLeftVolume() {
            return this.rearLeftVolume;
        }

        public void setRearLeftVolume(float f) {
            this.rearLeftVolume = f;
        }

        public float getRearRightVolume() {
            return this.rearRightVolume;
        }

        public void setRearRightVolume(float f) {
            this.rearRightVolume = f;
        }

        public float getCentralVolume() {
            return this.centralVolume;
        }

        public void setCentralVolume(float f) {
            this.centralVolume = f;
        }

        public float getSubwooferVolume() {
            return this.subwooferVolume;
        }

        public void setSubwooferVolume(float f) {
            this.subwooferVolume = f;
        }
    }
}
