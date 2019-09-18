package android.privacy.internal.longitudinalreporting;

import android.privacy.DifferentialPrivacyConfig;
import android.privacy.internal.rappor.RapporConfig;
import android.text.TextUtils;
import com.android.internal.util.Preconditions;

public class LongitudinalReportingConfig implements DifferentialPrivacyConfig {
    private static final String ALGORITHM_NAME = "LongitudinalReporting";
    private final String mEncoderId;
    private final RapporConfig mIRRConfig;
    private final double mProbabilityF;
    private final double mProbabilityP;
    private final double mProbabilityQ;

    public LongitudinalReportingConfig(String encoderId, double probabilityF, double probabilityP, double probabilityQ) {
        double d = probabilityF;
        double d2 = probabilityP;
        double d3 = probabilityQ;
        boolean z = false;
        Preconditions.checkArgument(d >= 0.0d && d <= 1.0d, "probabilityF must be in range [0.0, 1.0]");
        this.mProbabilityF = d;
        Preconditions.checkArgument(d2 >= 0.0d && d2 <= 1.0d, "probabilityP must be in range [0.0, 1.0]");
        this.mProbabilityP = d2;
        if (d3 >= 0.0d && d3 <= 1.0d) {
            z = true;
        }
        Preconditions.checkArgument(z, "probabilityQ must be in range [0.0, 1.0]");
        this.mProbabilityQ = d3;
        Preconditions.checkArgument(!TextUtils.isEmpty(encoderId), "encoderId cannot be empty");
        String str = encoderId;
        this.mEncoderId = str;
        RapporConfig rapporConfig = r1;
        RapporConfig rapporConfig2 = new RapporConfig(str, 1, 0.0d, d, 1.0d - d, 1, 1);
        this.mIRRConfig = rapporConfig;
    }

    public String getAlgorithm() {
        return ALGORITHM_NAME;
    }

    /* access modifiers changed from: package-private */
    public RapporConfig getIRRConfig() {
        return this.mIRRConfig;
    }

    /* access modifiers changed from: package-private */
    public double getProbabilityP() {
        return this.mProbabilityP;
    }

    /* access modifiers changed from: package-private */
    public double getProbabilityQ() {
        return this.mProbabilityQ;
    }

    /* access modifiers changed from: package-private */
    public String getEncoderId() {
        return this.mEncoderId;
    }

    public String toString() {
        return String.format("EncoderId: %s, ProbabilityF: %.3f, ProbabilityP: %.3f, ProbabilityQ: %.3f", new Object[]{this.mEncoderId, Double.valueOf(this.mProbabilityF), Double.valueOf(this.mProbabilityP), Double.valueOf(this.mProbabilityQ)});
    }
}
