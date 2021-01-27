package ohos.media.audio;

import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class AudioEqualizer extends SoundEffect {
    private static final Logger LOGGER = LoggerFactory.getAudioLogger(AudioEqualizer.class);
    public static final int PARA_ID_FREQS_NUM = 0;
    public static final int PARA_ID_FREQ_GAIN = 2;
    private short freqsNum = 0;

    public AudioEqualizer(int i, int i2, String str) throws RuntimeException {
        super(SOUND_EFFECT_TYPE_INVALID, SOUND_EFFECT_TYPE_AE, i, i2, str);
        if (i2 == 0) {
            LOGGER.warn("warning: try to attach an Equalizer to global output mix!", new Object[0]);
        }
        short[] sArr = new short[1];
        if (getPara(new int[]{0}, sArr)) {
            this.freqsNum = sArr[0];
            return;
        }
        throw new RuntimeException("get frequencys num fail!");
    }

    /* JADX DEBUG: Multi-variable search result rejected for r6v0, resolved type: short */
    /* JADX DEBUG: Multi-variable search result rejected for r1v0, resolved type: int[] */
    /* JADX WARN: Multi-variable type inference failed */
    public boolean setFrequencyGain(short s, short s2) {
        return setPara(new int[]{2, s}, new short[]{s2});
    }
}
