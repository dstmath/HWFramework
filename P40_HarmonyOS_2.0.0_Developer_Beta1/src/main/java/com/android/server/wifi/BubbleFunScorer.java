package com.android.server.wifi;

import com.android.server.wifi.WifiCandidates;
import java.util.Collection;

final class BubbleFunScorer implements WifiCandidates.CandidateScorer {
    private static final double BELS_PER_DECIBEL = 0.1d;
    public static final int BUBBLE_FUN_SCORER_DEFAULT_EXPID = 42598151;
    private static final double CURRENT_NETWORK_BOOST = 80.0d;
    private static final double LOW_BAND_FACTOR = 0.3d;
    private static final double RESCALE_FACTOR = (100.0d / (unscaledShapeFunction(RESCALE_FACTOR) - unscaledShapeFunction(-85.0d)));
    private static final double SECURITY_AWARD = 80.0d;
    private static final double TYPICAL_SCAN_RSSI_STD = 4.0d;
    private final ScoringParams mScoringParams;

    BubbleFunScorer(ScoringParams scoringParams) {
        this.mScoringParams = scoringParams;
    }

    @Override // com.android.server.wifi.WifiCandidates.CandidateScorer
    public String getIdentifier() {
        return "BubbleFunScorer_v1";
    }

    private WifiCandidates.ScoredCandidate scoreCandidate(WifiCandidates.Candidate candidate) {
        int rssi = candidate.getScanRssi();
        double score = shapeFunction((double) rssi) - shapeFunction((double) this.mScoringParams.getEntryRssi(candidate.getFrequency()));
        if (score < RESCALE_FACTOR) {
            score *= 10.0d;
        }
        double score2 = score + (candidate.getLastSelectionWeight() * 80.0d);
        if (candidate.isCurrentNetwork()) {
            score2 += 80.0d;
        }
        if (!candidate.isOpenNetwork()) {
            score2 += 80.0d;
        }
        double gain = shapeFunction(((double) rssi) + 0.5d) - shapeFunction(((double) rssi) - 0.5d);
        if (candidate.getFrequency() < 5000) {
            score2 *= LOW_BAND_FACTOR;
            gain *= LOW_BAND_FACTOR;
        }
        return new WifiCandidates.ScoredCandidate(score2, gain * TYPICAL_SCAN_RSSI_STD, candidate);
    }

    private static double unscaledShapeFunction(double rssi) {
        return -Math.exp((-rssi) * BELS_PER_DECIBEL);
    }

    private static double shapeFunction(double rssi) {
        return unscaledShapeFunction(rssi) * RESCALE_FACTOR;
    }

    @Override // com.android.server.wifi.WifiCandidates.CandidateScorer
    public WifiCandidates.ScoredCandidate scoreCandidates(Collection<WifiCandidates.Candidate> group) {
        WifiCandidates.ScoredCandidate choice = WifiCandidates.ScoredCandidate.NONE;
        for (WifiCandidates.Candidate candidate : group) {
            WifiCandidates.ScoredCandidate scoredCandidate = scoreCandidate(candidate);
            if (scoredCandidate.value > choice.value) {
                choice = scoredCandidate;
            }
        }
        return choice;
    }

    @Override // com.android.server.wifi.WifiCandidates.CandidateScorer
    public boolean userConnectChoiceOverrideWanted() {
        return true;
    }
}
