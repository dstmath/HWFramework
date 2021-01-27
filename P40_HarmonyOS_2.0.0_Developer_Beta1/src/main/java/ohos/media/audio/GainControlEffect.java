package ohos.media.audio;

import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class GainControlEffect extends SoundEffect {
    private static final Logger LOGGER = LoggerFactory.getAudioLogger(GainControlEffect.class);

    public static boolean isAvailable() {
        return SoundEffect.isEffectAvailable(SOUND_EFFECT_TYPE_GC);
    }

    public static GainControlEffect create(int i, String str) {
        try {
            return new GainControlEffect(i, str);
        } catch (UnsupportedOperationException e) {
            LOGGER.error("Operation failed, error message: %{public}s", e.getMessage());
            return null;
        }
    }

    private GainControlEffect(int i, String str) throws UnsupportedOperationException {
        super(SOUND_EFFECT_TYPE_INVALID, SOUND_EFFECT_TYPE_GC, 0, i, str);
    }
}
