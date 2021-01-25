package ohos.media.audio;

import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class EchoCancelerEffect extends SoundEffect {
    private static final Logger LOGGER = LoggerFactory.getAudioLogger(EchoCancelerEffect.class);

    public static boolean isAvailable() {
        return SoundEffect.isEffectAvailable(SOUND_EFFECT_TYPE_EC);
    }

    public static EchoCancelerEffect create(int i, String str) {
        try {
            return new EchoCancelerEffect(i, str);
        } catch (UnsupportedOperationException e) {
            LOGGER.error("Operation failed, error message: %{public}s", e.getMessage());
            return null;
        }
    }

    private EchoCancelerEffect(int i, String str) throws UnsupportedOperationException {
        super(SOUND_EFFECT_TYPE_INVALID, SOUND_EFFECT_TYPE_EC, 0, i, str);
    }
}
