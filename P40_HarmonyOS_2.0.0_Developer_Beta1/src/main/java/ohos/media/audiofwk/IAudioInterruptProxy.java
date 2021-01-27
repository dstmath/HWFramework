package ohos.media.audiofwk;

import ohos.media.audio.AudioInterrupt;

public interface IAudioInterruptProxy {

    public interface AudioInterruptObserver {
        void onInterrupt(String str, int i, int i2);
    }

    boolean activateAudioInterrupt(AudioInterrupt audioInterrupt);

    boolean deactivateAudioInterrupt(AudioInterrupt audioInterrupt);

    void setAudioInterruptObserver(AudioInterruptObserver audioInterruptObserver);
}
