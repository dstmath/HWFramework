package android.privacy.internal.rappor;

import android.privacy.DifferentialPrivacyConfig;
import android.text.TextUtils;
import com.android.internal.util.Preconditions;

public class RapporConfig implements DifferentialPrivacyConfig {
    private static final String ALGORITHM_NAME = "Rappor";
    final String mEncoderId;
    final int mNumBits;
    final int mNumBloomHashes;
    final int mNumCohorts;
    final double mProbabilityF;
    final double mProbabilityP;
    final double mProbabilityQ;

    public RapporConfig(String encoderId, int numBits, double probabilityF, double probabilityP, double probabilityQ, int numCohorts, int numBloomHashes) {
        Preconditions.checkArgument(!TextUtils.isEmpty(encoderId), "encoderId cannot be empty");
        this.mEncoderId = encoderId;
        boolean z = false;
        Preconditions.checkArgument(numBits > 0, "numBits needs to be > 0");
        this.mNumBits = numBits;
        Preconditions.checkArgument(probabilityF >= 0.0d && probabilityF <= 1.0d, "probabilityF must be in range [0.0, 1.0]");
        this.mProbabilityF = probabilityF;
        Preconditions.checkArgument(probabilityP >= 0.0d && probabilityP <= 1.0d, "probabilityP must be in range [0.0, 1.0]");
        this.mProbabilityP = probabilityP;
        Preconditions.checkArgument(probabilityQ >= 0.0d && probabilityQ <= 1.0d, "probabilityQ must be in range [0.0, 1.0]");
        this.mProbabilityQ = probabilityQ;
        Preconditions.checkArgument(numCohorts > 0, "numCohorts needs to be > 0");
        this.mNumCohorts = numCohorts;
        Preconditions.checkArgument(numBloomHashes > 0 ? true : z, "numBloomHashes needs to be > 0");
        this.mNumBloomHashes = numBloomHashes;
    }

    @Override // android.privacy.DifferentialPrivacyConfig
    public String getAlgorithm() {
        return ALGORITHM_NAME;
    }

    public String toString() {
        return String.format("EncoderId: %s, NumBits: %d, ProbabilityF: %.3f, ProbabilityP: %.3f, ProbabilityQ: %.3f, NumCohorts: %d, NumBloomHashes: %d", this.mEncoderId, Integer.valueOf(this.mNumBits), Double.valueOf(this.mProbabilityF), Double.valueOf(this.mProbabilityP), Double.valueOf(this.mProbabilityQ), Integer.valueOf(this.mNumCohorts), Integer.valueOf(this.mNumBloomHashes));
    }
}
