package ohos.media.audio;

import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class NoiseSuppressorEffect extends SoundEffect {
    private static final Logger LOGGER = LoggerFactory.getAudioLogger(NoiseSuppressorEffect.class);

    public static boolean isAvailable() {
        return SoundEffect.isEffectAvailable(SOUND_EFFECT_TYPE_NS);
    }

    public static NoiseSuppressorEffect create(int i, String str) {
        try {
            return new NoiseSuppressorEffect(i, str);
        } catch (UnsupportedOperationException e) {
            LOGGER.error("Operation failed, error message: %{public}s", e.getMessage());
            return null;
        }
    }

    private NoiseSuppressorEffect(int i, String str) throws UnsupportedOperationException {
        super(SOUND_EFFECT_TYPE_INVALID, SOUND_EFFECT_TYPE_NS, 0, i, str);
    }
}
