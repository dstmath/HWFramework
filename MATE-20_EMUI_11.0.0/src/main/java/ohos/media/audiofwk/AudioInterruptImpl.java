package ohos.media.audiofwk;

import java.util.HashMap;
import java.util.Map;
import ohos.media.audio.AudioInterrupt;
import ohos.media.audio.AudioStreamInfo;
import ohos.media.audiofwk.IAudioInterruptProxy;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class AudioInterruptImpl {
    private static final Logger LOGGER = LoggerFactory.getAudioLogger(AudioInterruptImpl.class);
    private final IAudioInterruptProxy audioProxy;
    private final AudioStreamInfo defaultAudioInfo = new AudioStreamInfo.Builder().streamUsage(AudioStreamInfo.StreamUsage.STREAM_USAGE_MEDIA).build();
    private final Map<String, AudioInterrupt> interruptMap;
    private final Object lock = new Object();
    private IAudioInterruptProxy.AudioInterruptObserver observer = new IAudioInterruptProxy.AudioInterruptObserver() {
        /* class ohos.media.audiofwk.AudioInterruptImpl.AnonymousClass1 */

        @Override // ohos.media.audiofwk.IAudioInterruptProxy.AudioInterruptObserver
        public void onInterrupt(String str, int i, int i2) {
            synchronized (AudioInterruptImpl.this.lock) {
                AudioInterrupt audioInterrupt = (AudioInterrupt) AudioInterruptImpl.this.interruptMap.get(str);
                if (!(audioInterrupt == null || audioInterrupt.getInterruptListener() == null)) {
                    AudioInterrupt.InterruptListener interruptListener = audioInterrupt.getInterruptListener();
                    if (audioInterrupt.isPauseWhenDucked()) {
                        i2 = (i == 2 && i2 == 0) ? 1 : (i == 1 && i2 == 0) ? 2 : i2;
                    }
                    interruptListener.onInterrupt(i, i2);
                }
            }
        }
    };

    public AudioInterruptImpl(IAudioInterruptProxy iAudioInterruptProxy) {
        this.audioProxy = iAudioInterruptProxy;
        this.audioProxy.setAudioInterruptObserver(this.observer);
        this.interruptMap = new HashMap();
    }

    public boolean activateAudioInterrupt(AudioInterrupt audioInterrupt) {
        if (audioInterrupt == null) {
            LOGGER.warn("activateAudioInterrupt interrupt is null.", new Object[0]);
            return false;
        } else if (audioInterrupt.getInterruptListener() == null) {
            LOGGER.warn("activateAudioInterrupt interrupt listener is null.", new Object[0]);
            return false;
        } else {
            if (audioInterrupt.getStreamInfo() == null) {
                audioInterrupt.setStreamInfo(this.defaultAudioInfo);
            }
            synchronized (this.lock) {
                String obj = audioInterrupt.toString();
                if (!this.interruptMap.containsKey(obj)) {
                    this.interruptMap.put(obj, audioInterrupt);
                }
            }
            return this.audioProxy.activateAudioInterrupt(audioInterrupt);
        }
    }

    public boolean deactivateAudioInterrupt(AudioInterrupt audioInterrupt) {
        if (audioInterrupt == null) {
            LOGGER.warn("activateAudioInterrupt interrupt is null.", new Object[0]);
            return false;
        } else if (audioInterrupt.getInterruptListener() == null) {
            LOGGER.warn("activateAudioInterrupt interrupt listener is null.", new Object[0]);
            return false;
        } else {
            if (audioInterrupt.getStreamInfo() == null) {
                audioInterrupt.setStreamInfo(this.defaultAudioInfo);
            }
            synchronized (this.lock) {
                String obj = audioInterrupt.toString();
                if (!this.interruptMap.containsKey(obj)) {
                    return false;
                }
                this.interruptMap.remove(obj);
                return this.audioProxy.deactivateAudioInterrupt(audioInterrupt);
            }
        }
    }
}
