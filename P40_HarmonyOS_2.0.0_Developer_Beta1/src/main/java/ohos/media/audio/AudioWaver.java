package ohos.media.audio;

import java.lang.ref.WeakReference;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.eventhandler.InnerEvent;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class AudioWaver {
    private static final int JNI_EVENT_FREQUENCY_DATA_SAMPLING = 1;
    private static final int JNI_EVENT_WAVE_DATA_SAMPLING = 0;
    private static final Logger LOGGER = LoggerFactory.getAudioLogger(AudioWaver.class);
    private final Object flagLock = new Object();
    private boolean freqDataEnable = false;
    private FrequencyDataObserver freqDataObserver;
    private final Object handlerLock = new Object();
    private long jniAudioWaver;
    private long jniData;
    private JniEventHandler jniEventHandler;
    private boolean waveDataEnable = false;
    private WaveDataObserver waveDataObserver;
    private int waverId;

    public interface FrequencyDataObserver {
        void onFrequencyData(byte[] bArr, int i);
    }

    public interface WaveDataObserver {
        void onWaveData(byte[] bArr, int i);
    }

    private static native int nativeGetMaxDataSize();

    private static native int nativeGetMinDataSize();

    private static native int nativeGetMinInterval();

    private native void nativeRelease();

    private native boolean nativeSetActivated(boolean z);

    private native boolean nativeSetDataSize(int i);

    private native boolean nativeSetPeriodicSampling(int i, boolean z, boolean z2);

    private native int nativeSetup(Object obj, int i, int[] iArr, String str);

    static {
        System.loadLibrary("audiowaver_jni.z");
    }

    public AudioWaver(int i, String str) throws UnsupportedOperationException {
        int[] iArr = new int[1];
        if (nativeSetup(new WeakReference(this), i, iArr, str) == 0) {
            this.waverId = iArr[0];
            return;
        }
        throw new UnsupportedOperationException("Audio Waver setup failed.");
    }

    public void release() {
        nativeRelease();
    }

    public boolean setDataSize(int i) {
        return nativeSetDataSize(i);
    }

    public boolean setActivated(boolean z) {
        return nativeSetActivated(z);
    }

    public static int getMinDataSize() {
        return nativeGetMinDataSize();
    }

    public static int getMaxDataSize() {
        return nativeGetMaxDataSize();
    }

    public static int getMinInterval() {
        return nativeGetMinInterval();
    }

    public boolean setWaveDataObserver(WaveDataObserver waveDataObserver2, int i) {
        boolean nativeSetPeriodicSampling;
        synchronized (this.flagLock) {
            this.waveDataEnable = waveDataObserver2 != null;
            nativeSetPeriodicSampling = nativeSetPeriodicSampling(i, this.waveDataEnable, this.freqDataEnable);
        }
        synchronized (this.handlerLock) {
            this.waveDataObserver = waveDataObserver2;
            if (!nativeSetPeriodicSampling) {
                LOGGER.error("setWaveDataObserver: nativeSetPeriodicSampling return fail", new Object[0]);
            } else if (this.waveDataObserver != null && this.jniEventHandler == null) {
                EventRunner create = EventRunner.create();
                if (create != null) {
                    this.jniEventHandler = new JniEventHandler(create, this);
                } else {
                    LOGGER.error("setWaveDataObserver: fail to create eventRunner", new Object[0]);
                    this.jniEventHandler = null;
                    nativeSetPeriodicSampling = false;
                }
            }
        }
        return nativeSetPeriodicSampling;
    }

    public boolean setFrequencyDataObserver(FrequencyDataObserver frequencyDataObserver, int i) {
        boolean nativeSetPeriodicSampling;
        synchronized (this.flagLock) {
            this.freqDataEnable = frequencyDataObserver != null;
            nativeSetPeriodicSampling = nativeSetPeriodicSampling(i, this.waveDataEnable, this.freqDataEnable);
        }
        synchronized (this.handlerLock) {
            this.freqDataObserver = frequencyDataObserver;
            if (!nativeSetPeriodicSampling) {
                LOGGER.error("setFrequencyDataObserver: nativeSetPeriodicSampling return fail", new Object[0]);
            } else if (this.freqDataObserver != null && this.jniEventHandler == null) {
                EventRunner create = EventRunner.create();
                if (create != null) {
                    this.jniEventHandler = new JniEventHandler(create, this);
                } else {
                    LOGGER.error("setFrequencyDataObserver: fail to create EventRunner", new Object[0]);
                    this.jniEventHandler = null;
                    nativeSetPeriodicSampling = false;
                }
            }
        }
        return nativeSetPeriodicSampling;
    }

    private static void postJniEventToJava(Object obj, int i, int i2, int i3, Object obj2) {
        if (!(obj instanceof WeakReference)) {
            LOGGER.error("nativeRef is null or not instance of WeakReference, return", new Object[0]);
            return;
        }
        AudioWaver audioWaver = (AudioWaver) ((WeakReference) obj).get();
        if (audioWaver == null) {
            LOGGER.error("audioWaver is null, return", new Object[0]);
        } else if (audioWaver.jniEventHandler == null) {
            LOGGER.error("eventHandler is not set, return", new Object[0]);
        } else {
            audioWaver.jniEventHandler.sendEvent(InnerEvent.get(i, new DataObserverEventParam(i2, obj2)));
        }
    }

    private static class DataObserverEventParam {
        public final Object data;
        public final int samplingRate;

        public DataObserverEventParam(int i, Object obj) {
            this.samplingRate = i;
            this.data = obj;
        }
    }

    private class JniEventHandler extends EventHandler {
        private AudioWaver audioWaver;

        public JniEventHandler(EventRunner eventRunner, AudioWaver audioWaver2) {
            super(eventRunner);
            this.audioWaver = audioWaver2;
        }

        public void processEvent(InnerEvent innerEvent) {
            AudioWaver.LOGGER.debug("JniEventHandler.processEvent called", new Object[0]);
            if (innerEvent == null || !(innerEvent.object instanceof DataObserverEventParam)) {
                AudioWaver.LOGGER.error("event object is null or not instance of DataObserverEventParam", new Object[0]);
                return;
            }
            DataObserverEventParam dataObserverEventParam = (DataObserverEventParam) innerEvent.object;
            int i = innerEvent.eventId;
            if (i != 0) {
                if (i != 1) {
                    AudioWaver.LOGGER.error("Unexpected message %{public}d", Integer.valueOf(innerEvent.eventId));
                } else if (this.audioWaver.freqDataObserver == null || !AudioWaver.this.freqDataEnable) {
                    AudioWaver.LOGGER.error("JniEventHandler.processEvent: onFrequencyData not enable", new Object[0]);
                } else {
                    this.audioWaver.freqDataObserver.onFrequencyData((byte[]) dataObserverEventParam.data, dataObserverEventParam.samplingRate);
                }
            } else if (this.audioWaver.waveDataObserver == null || !AudioWaver.this.waveDataEnable) {
                AudioWaver.LOGGER.error("JniEventHandler.processEvent: onWaveData not enable", new Object[0]);
            } else {
                this.audioWaver.waveDataObserver.onWaveData((byte[]) dataObserverEventParam.data, dataObserverEventParam.samplingRate);
            }
        }
    }
}
