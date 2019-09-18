package android.privacy.internal.longitudinalreporting;

import android.privacy.DifferentialPrivacyEncoder;
import android.privacy.internal.rappor.RapporConfig;
import android.privacy.internal.rappor.RapporEncoder;
import com.android.internal.annotations.VisibleForTesting;

public class LongitudinalReportingEncoder implements DifferentialPrivacyEncoder {
    private static final boolean DEBUG = false;
    private static final String PRR1_ENCODER_ID = "prr1_encoder_id";
    private static final String PRR2_ENCODER_ID = "prr2_encoder_id";
    private static final String TAG = "LongitudinalEncoder";
    private final LongitudinalReportingConfig mConfig;
    private final Boolean mFakeValue;
    private final RapporEncoder mIRREncoder;
    private final boolean mIsSecure;

    public static LongitudinalReportingEncoder createEncoder(LongitudinalReportingConfig config, byte[] userSecret) {
        return new LongitudinalReportingEncoder(config, true, userSecret);
    }

    @VisibleForTesting
    public static LongitudinalReportingEncoder createInsecureEncoderForTest(LongitudinalReportingConfig config) {
        return new LongitudinalReportingEncoder(config, false, null);
    }

    private LongitudinalReportingEncoder(LongitudinalReportingConfig config, boolean secureEncoder, byte[] userSecret) {
        RapporEncoder rapporEncoder;
        this.mConfig = config;
        this.mIsSecure = secureEncoder;
        double probabilityP = config.getProbabilityP();
        if (getLongTermRandomizedResult(probabilityP, secureEncoder, userSecret, config.getEncoderId() + PRR1_ENCODER_ID)) {
            double probabilityQ = config.getProbabilityQ();
            this.mFakeValue = Boolean.valueOf(getLongTermRandomizedResult(probabilityQ, secureEncoder, userSecret, config.getEncoderId() + PRR2_ENCODER_ID));
        } else {
            this.mFakeValue = null;
        }
        RapporConfig irrConfig = config.getIRRConfig();
        if (secureEncoder) {
            rapporEncoder = RapporEncoder.createEncoder(irrConfig, userSecret);
        } else {
            rapporEncoder = RapporEncoder.createInsecureEncoderForTest(irrConfig);
        }
        this.mIRREncoder = rapporEncoder;
    }

    public byte[] encodeString(String original) {
        throw new UnsupportedOperationException();
    }

    public byte[] encodeBoolean(boolean original) {
        if (this.mFakeValue != null) {
            original = this.mFakeValue.booleanValue();
        }
        return this.mIRREncoder.encodeBoolean(original);
    }

    public byte[] encodeBits(byte[] bits) {
        throw new UnsupportedOperationException();
    }

    public LongitudinalReportingConfig getConfig() {
        return this.mConfig;
    }

    public boolean isInsecureEncoderForTest() {
        return !this.mIsSecure;
    }

    @VisibleForTesting
    public static boolean getLongTermRandomizedResult(double p, boolean secureEncoder, byte[] userSecret, String encoderId) {
        RapporEncoder encoder;
        double effectiveF = p < 0.5d ? p * 2.0d : (1.0d - p) * 2.0d;
        boolean prrInput = p >= 0.5d;
        RapporConfig rapporConfig = new RapporConfig(encoderId, 1, effectiveF, 0.0d, 1.0d, 1, 1);
        if (secureEncoder) {
            encoder = RapporEncoder.createEncoder(rapporConfig, userSecret);
        } else {
            byte[] bArr = userSecret;
            encoder = RapporEncoder.createInsecureEncoderForTest(rapporConfig);
        }
        if (encoder.encodeBoolean(prrInput)[0] > 0) {
            return true;
        }
        return false;
    }
}
