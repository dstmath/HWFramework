package ohos.media.audio;

import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class ToneDescriptor {
    private static final Logger LOGGER = LoggerFactory.getAudioLogger(ToneDescriptor.class);
    private final double highFrequency;
    private final double lowFrequency;

    public enum ToneType {
        DTMF_0(0),
        DTMF_1(1),
        DTMF_2(2),
        DTMF_3(3),
        DTMF_4(4),
        DTMF_5(5),
        DTMF_6(6),
        DTMF_7(7),
        DTMF_8(8),
        DTMF_9(9),
        DTMF_S(10),
        DTMF_P(11),
        DTMF_A(12),
        DTMF_B(13),
        DTMF_C(14),
        DTMF_D(15),
        PROP_PROMPT(16),
        SUP_CALL_WAITING(17);
        
        private final int toneType;

        private ToneType(int i) {
            this.toneType = i;
        }

        public int getValue() {
            return this.toneType;
        }
    }

    public ToneDescriptor(ToneType toneType) {
        switch (toneType) {
            case DTMF_0:
                this.highFrequency = 1336.0d;
                this.lowFrequency = 941.0d;
                return;
            case DTMF_1:
                this.highFrequency = 1209.0d;
                this.lowFrequency = 697.0d;
                return;
            case DTMF_2:
                this.highFrequency = 1336.0d;
                this.lowFrequency = 697.0d;
                return;
            case DTMF_3:
                this.highFrequency = 1477.0d;
                this.lowFrequency = 697.0d;
                return;
            case DTMF_4:
                this.highFrequency = 1209.0d;
                this.lowFrequency = 770.0d;
                return;
            case DTMF_5:
                this.highFrequency = 1336.0d;
                this.lowFrequency = 770.0d;
                return;
            case DTMF_6:
                this.highFrequency = 1477.0d;
                this.lowFrequency = 770.0d;
                return;
            case DTMF_7:
                this.highFrequency = 1209.0d;
                this.lowFrequency = 852.0d;
                return;
            case DTMF_8:
                this.highFrequency = 1366.0d;
                this.lowFrequency = 852.0d;
                return;
            case DTMF_9:
                this.highFrequency = 1477.0d;
                this.lowFrequency = 852.0d;
                return;
            case DTMF_S:
                this.highFrequency = 1209.0d;
                this.lowFrequency = 941.0d;
                return;
            case DTMF_P:
                this.highFrequency = 1477.0d;
                this.lowFrequency = 941.0d;
                return;
            case DTMF_A:
                this.highFrequency = 1633.0d;
                this.lowFrequency = 697.0d;
                return;
            case DTMF_B:
                this.highFrequency = 1633.0d;
                this.lowFrequency = 770.0d;
                return;
            case DTMF_C:
                this.highFrequency = 1633.0d;
                this.lowFrequency = 852.0d;
                return;
            case DTMF_D:
                this.highFrequency = 1633.0d;
                this.lowFrequency = 941.0d;
                return;
            default:
                LOGGER.error("Input tone type is invalid, type: %{public}d.", toneType);
                this.highFrequency = 0.0d;
                this.lowFrequency = 0.0d;
                return;
        }
    }

    public double getHighFrequency() {
        return this.highFrequency;
    }

    public double getLowFrequency() {
        return this.lowFrequency;
    }
}
