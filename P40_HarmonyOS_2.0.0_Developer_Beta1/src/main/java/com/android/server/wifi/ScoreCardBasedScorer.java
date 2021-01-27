package com.android.server.wifi;

import com.android.server.wifi.WifiCandidates;
import com.android.server.wifi.WifiScoreCardProto;
import java.util.Collection;

/* access modifiers changed from: package-private */
public final class ScoreCardBasedScorer implements WifiCandidates.CandidateScorer {
    public static final int BAND_5GHZ_AWARD_IS_40 = 40;
    public static final int CURRENT_NETWORK_BOOST_IS_16 = 16;
    public static final int LAST_SELECTION_AWARD_IS_480 = 480;
    public static final int MIN_POLLS_FOR_SIGNIFICANCE = 30;
    public static final int RSSI_RAIL = 5;
    public static final int RSSI_SCORE_OFFSET = 85;
    public static final int RSSI_SCORE_SLOPE_IS_4 = 4;
    public static final int SAME_BSSID_AWARD_IS_24 = 24;
    public static final int SCORE_CARD_BASED_SCORER_DEFAULT_EXPID = 42902385;
    public static final int SECURITY_AWARD_IS_80 = 80;
    private final ScoringParams mScoringParams;

    ScoreCardBasedScorer(ScoringParams scoringParams) {
        this.mScoringParams = scoringParams;
    }

    @Override // com.android.server.wifi.WifiCandidates.CandidateScorer
    public String getIdentifier() {
        return "ScoreCardBasedScorer";
    }

    private WifiCandidates.ScoredCandidate scoreCandidate(WifiCandidates.Candidate candidate) {
        int score = (Math.min(candidate.getScanRssi(), this.mScoringParams.getGoodRssi(candidate.getFrequency())) - estimatedCutoff(candidate)) * 4;
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
        return new WifiCandidates.ScoredCandidate((double) (score2 - (candidate.getEvaluatorId() * 1000)), 10.0d, candidate);
    }

    private int estimatedCutoff(WifiCandidates.Candidate candidate) {
        int lowest = -85 - 5;
        int highest = -85 + 5;
        WifiScoreCardProto.Signal signal = candidate.getEventStatistics(WifiScoreCardProto.Event.SIGNAL_POLL);
        if (signal == null || !signal.hasRssi()) {
            return -85;
        }
        if (signal.getRssi().getCount() <= 30) {
            return -85;
        }
        double mean = signal.getRssi().getSum() / ((double) signal.getRssi().getCount());
        return (int) Math.min(Math.max(mean - (2.0d * Math.sqrt((signal.getRssi().getSumOfSquares() / ((double) signal.getRssi().getCount())) - (mean * mean))), (double) lowest), (double) highest);
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
