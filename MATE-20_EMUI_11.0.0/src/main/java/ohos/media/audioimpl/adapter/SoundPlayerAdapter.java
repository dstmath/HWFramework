package ohos.media.audioimpl.adapter;

import android.media.ToneGenerator;
import ohos.media.audio.AudioStreamInfo;
import ohos.media.audio.ToneDescriptor;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class SoundPlayerAdapter {
    private static final Logger LOGGER = LoggerFactory.getAudioLogger(AudioSystemAdapter.class);
    private static final int[] STREAM_TYPE_MATCH_TABLE = {0, 1, 2, 3, 4, 5, 8, 10};
    private static final int[] TONE_TYPE_MATCH_TABLE = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 27, 22};
    private ToneGenerator toneGenerator;

    public final boolean createTone(AudioStreamInfo.StreamType streamType, float f) {
        if (streamType.getValue() >= 0) {
            int value = streamType.getValue();
            int[] iArr = STREAM_TYPE_MATCH_TABLE;
            if (value < iArr.length) {
                this.toneGenerator = new ToneGenerator(iArr[streamType.getValue()], (int) (f * 100.0f));
                return true;
            }
        }
        LOGGER.error("streamType is invalid, return", new Object[0]);
        return false;
    }

    public final boolean playTone(ToneDescriptor.ToneType toneType, int i) {
        if (this.toneGenerator == null) {
            LOGGER.error("toneGenerator is null, return", new Object[0]);
            return false;
        }
        if (toneType.getValue() >= 0) {
            int value = toneType.getValue();
            int[] iArr = TONE_TYPE_MATCH_TABLE;
            if (value < iArr.length) {
                return this.toneGenerator.startTone(iArr[toneType.getValue()], i);
            }
        }
        LOGGER.error("toneType is invalid, return", new Object[0]);
        return false;
    }

    public final boolean release() {
        ToneGenerator toneGenerator2 = this.toneGenerator;
        if (toneGenerator2 == null) {
            LOGGER.error("toneGenerator is null, return", new Object[0]);
            return false;
        }
        toneGenerator2.release();
        return true;
    }
}
