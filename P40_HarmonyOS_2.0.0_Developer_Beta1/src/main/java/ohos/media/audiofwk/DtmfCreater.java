package ohos.media.audiofwk;

import ohos.media.audio.ToneDescriptor;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class DtmfCreater {
    private static final Logger LOGGER = LoggerFactory.getAudioLogger(DtmfCreater.class);
    private short[] data;
    private ToneCreater highFrequencyToneCreater;
    private boolean isSamplesReady = false;
    private ToneCreater lowFrequencyToneCreater;

    public DtmfCreater(int i, ToneDescriptor toneDescriptor) {
        this.highFrequencyToneCreater = new ToneCreater(i, toneDescriptor.getHighFrequency());
        this.lowFrequencyToneCreater = new ToneCreater(i, toneDescriptor.getLowFrequency());
    }

    public short[] getData() {
        short[] sArr = this.data;
        if (sArr != null) {
            return (short[]) sArr.clone();
        }
        LOGGER.error("Data is null, must create before get", new Object[0]);
        return new short[0];
    }

    public boolean getIsSamplesReady() {
        return this.isSamplesReady;
    }

    public boolean createSamples(int i) {
        this.isSamplesReady = false;
        if (i <= 0) {
            LOGGER.error("Duration milliseconds is invalid, durationMs: %{public}d.", Integer.valueOf(i));
            return false;
        }
        ToneCreater toneCreater = this.highFrequencyToneCreater;
        if (toneCreater == null || this.lowFrequencyToneCreater == null) {
            LOGGER.error("Tone creater is null.", new Object[0]);
            return false;
        }
        short[] samples = toneCreater.getSamples(i);
        short[] samples2 = this.lowFrequencyToneCreater.getSamples(i);
        int length = samples.length < samples2.length ? samples.length : samples2.length;
        this.data = new short[(samples.length + samples2.length)];
        int i2 = 0;
        for (int i3 = 0; i3 < length; i3++) {
            short[] sArr = this.data;
            sArr[i2] = samples[i3];
            sArr[i2 + 1] = samples2[i3];
            i2 += 2;
        }
        this.isSamplesReady = true;
        return true;
    }
}
