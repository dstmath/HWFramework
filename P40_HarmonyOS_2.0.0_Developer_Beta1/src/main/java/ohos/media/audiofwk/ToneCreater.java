package ohos.media.audiofwk;

import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class ToneCreater {
    private static final float AMPLITUDE_RATE = 0.3f;
    private static final int DEFAULT_SAMPLERATE = 44800;
    private static final Logger LOGGER = LoggerFactory.getAudioLogger(ToneCreater.class);
    private static final short MEDIUM_POW_NUM = 14;
    private static final int NORMAL_AMPLITUDE = 32000;
    private static final int STRONG_AMPLITUDE = 32767;
    private static final short STRONG_POW_NUM = 15;
    private short ampPow14;
    private short ampPow15;
    private short firstSample;
    private long nextSample;
    private long prevSample;
    private int sampleRate;

    public ToneCreater(int i, double d) {
        i = i <= 0 ? DEFAULT_SAMPLERATE : i;
        double d2 = (d * 6.283185307179586d) / ((double) i);
        this.firstSample = (short) ((int) (Math.sin(d2) * -32000.0d));
        this.prevSample = 0;
        this.nextSample = (long) this.firstSample;
        this.ampPow15 = 10065;
        this.ampPow14 = (short) ((int) (Math.cos(d2) * 32767.0d));
        this.sampleRate = i;
    }

    private void createSamples(short[] sArr) {
        if (sArr.length == 0) {
            LOGGER.error("Input buffer is null.", new Object[0]);
            return;
        }
        int length = sArr.length;
        long j = this.prevSample;
        long j2 = this.nextSample;
        long j3 = (long) this.ampPow14;
        long j4 = (long) this.ampPow15;
        long j5 = j;
        int i = 0;
        while (true) {
            j2 = j5;
            if (length != 0) {
                length--;
                long j6 = ((j3 * j2) >> 14) - j2;
                sArr[i] = (short) ((int) ((j4 * j6) >> 15));
                i++;
                j5 = j6;
            } else {
                this.prevSample = j2;
                this.nextSample = j2;
                return;
            }
        }
    }

    private int getSampleCount(int i) {
        int i2;
        if (i <= 0 || (i2 = this.sampleRate) <= 0) {
            LOGGER.error("Duration milliseconds or sample rate is invalid,durationMs: %{public}d, sampleRate: %{public}d.", Integer.valueOf(i), Integer.valueOf(this.sampleRate));
            return 0;
        } else if (Integer.MAX_VALUE / i >= i2) {
            return (i * i2) / 1000;
        } else {
            LOGGER.error("Multiplier is invalid, durationMs: %{public}d, sampleRate: %{public}d.", Integer.valueOf(i), Integer.valueOf(this.sampleRate));
            return 0;
        }
    }

    public short[] getSamples(int i) {
        short[] sArr = new short[getSampleCount(i)];
        createSamples(sArr);
        return sArr;
    }
}
