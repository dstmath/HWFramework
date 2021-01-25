package com.android.server.wifi;

import com.android.server.wifi.WifiCandidates;
import java.util.Collection;

/* access modifiers changed from: package-private */
public final class CompatibilityScorer implements WifiCandidates.CandidateScorer {
    public static final int BAND_5GHZ_AWARD_IS_40 = 40;
    public static final int COMPATIBILITY_SCORER_DEFAULT_EXPID = 42504592;
    public static final int CURRENT_NETWORK_BOOST_IS_16 = 16;
    public static final int LAST_SELECTION_AWARD_IS_480 = 480;
    public static final int RSSI_SCORE_OFFSET = 85;
    public static final int RSSI_SCORE_SLOPE_IS_4 = 4;
    public static final int SAME_BSSID_AWARD_IS_24 = 24;
    public static final int SECURITY_AWARD_IS_80 = 80;
    private final ScoringParams mScoringParams;

    CompatibilityScorer(ScoringParams scoringParams) {
        this.mScoringParams = scoringParams;
    }

    @Override // com.android.server.wifi.WifiCandidates.CandidateScorer
    public String getIdentifier() {
        return WifiNetworkSelector.PRESET_CANDIDATE_SCORER_NAME;
    }

    private WifiCandidates.ScoredCandidate scoreCandidate(WifiCandidates.Candidate candidate) {
        int score = (Math.min(candidate.getScanRssi(), this.mScoringParams.getGoodRssi(candidate.getFrequency())) + 85) * 4;
        if (candidate.getFrequency() >= 5000) {
            score += 40;
        }
        int score2 = score + ((int) (candidate.getLastSelectionWeight() * 480.0d));
        if (candidate.isCurrentNetwork()) {
            score2 += 40;
        }
        if (!candidate.isOpenNetwork()) {
            score2 += 80;
        }
        return new WifiCandidates.ScoredCandidate(((double) (score2 - (candidate.getEvaluatorId() * 1000))) + (((double) candidate.getScanRssi()) / 1000.0d), 10.0d, candidate);
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
