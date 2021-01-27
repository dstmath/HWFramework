package com.android.server.wifi;

import android.net.MacAddress;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.util.ArrayMap;
import com.android.internal.util.Preconditions;
import com.android.server.wifi.WifiScoreCard;
import com.android.server.wifi.WifiScoreCardProto;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

public class WifiCandidates {
    private static final String TAG = "WifiCandidates";
    private final Map<Key, CandidateImpl> mCandidates = new ArrayMap();
    private MacAddress mCurrentBssid = null;
    private int mCurrentNetworkId = -1;
    private int mFaultCount = 0;
    private RuntimeException mLastFault = null;
    private boolean mPicky = false;
    private final WifiScoreCard mWifiScoreCard;

    public interface Candidate {
        int getEvaluatorId();

        int getEvaluatorScore();

        WifiScoreCardProto.Signal getEventStatistics(WifiScoreCardProto.Event event);

        int getFrequency();

        Key getKey();

        double getLastSelectionWeight();

        int getNetworkConfigId();

        ScanDetail getScanDetail();

        int getScanRssi();

        boolean isCurrentBssid();

        boolean isCurrentNetwork();

        boolean isEphemeral();

        boolean isOpenNetwork();

        boolean isPasspoint();

        boolean isTrusted();
    }

    public interface CandidateScorer {
        String getIdentifier();

        ScoredCandidate scoreCandidates(Collection<Candidate> collection);

        boolean userConnectChoiceOverrideWanted();
    }

    WifiCandidates(WifiScoreCard wifiScoreCard) {
        this.mWifiScoreCard = (WifiScoreCard) Preconditions.checkNotNull(wifiScoreCard);
    }

    /* access modifiers changed from: package-private */
    public static class CandidateImpl implements Candidate {
        public final WifiConfiguration config;
        public final int evaluatorId;
        public final int evaluatorScore;
        public final Key key;
        public final double lastSelectionWeight;
        private final boolean mIsCurrentBssid;
        private final boolean mIsCurrentNetwork;
        private WifiScoreCard.PerBssid mPerBssid;
        public final ScanDetail scanDetail;

        CandidateImpl(Key key2, ScanDetail scanDetail2, WifiConfiguration config2, int evaluatorId2, int evaluatorScore2, WifiScoreCard.PerBssid perBssid, double lastSelectionWeight2, boolean isCurrentNetwork, boolean isCurrentBssid) {
            this.key = key2;
            this.scanDetail = scanDetail2;
            this.config = config2;
            this.evaluatorId = evaluatorId2;
            this.evaluatorScore = evaluatorScore2;
            this.mPerBssid = perBssid;
            this.lastSelectionWeight = lastSelectionWeight2;
            this.mIsCurrentNetwork = isCurrentNetwork;
            this.mIsCurrentBssid = isCurrentBssid;
        }

        @Override // com.android.server.wifi.WifiCandidates.Candidate
        public Key getKey() {
            return this.key;
        }

        @Override // com.android.server.wifi.WifiCandidates.Candidate
        public int getNetworkConfigId() {
            return this.key.networkId;
        }

        @Override // com.android.server.wifi.WifiCandidates.Candidate
        public ScanDetail getScanDetail() {
            return this.scanDetail;
        }

        @Override // com.android.server.wifi.WifiCandidates.Candidate
        public boolean isOpenNetwork() {
            return WifiConfigurationUtil.isConfigForOpenNetwork(this.config);
        }

        @Override // com.android.server.wifi.WifiCandidates.Candidate
        public boolean isPasspoint() {
            return this.config.isPasspoint();
        }

        @Override // com.android.server.wifi.WifiCandidates.Candidate
        public boolean isEphemeral() {
            return this.config.ephemeral;
        }

        @Override // com.android.server.wifi.WifiCandidates.Candidate
        public boolean isTrusted() {
            return this.config.trusted;
        }

        @Override // com.android.server.wifi.WifiCandidates.Candidate
        public int getEvaluatorId() {
            return this.evaluatorId;
        }

        @Override // com.android.server.wifi.WifiCandidates.Candidate
        public int getEvaluatorScore() {
            return this.evaluatorScore;
        }

        @Override // com.android.server.wifi.WifiCandidates.Candidate
        public double getLastSelectionWeight() {
            return this.lastSelectionWeight;
        }

        @Override // com.android.server.wifi.WifiCandidates.Candidate
        public boolean isCurrentNetwork() {
            return this.mIsCurrentNetwork;
        }

        @Override // com.android.server.wifi.WifiCandidates.Candidate
        public boolean isCurrentBssid() {
            return this.mIsCurrentBssid;
        }

        @Override // com.android.server.wifi.WifiCandidates.Candidate
        public int getScanRssi() {
            return this.scanDetail.getScanResult().level;
        }

        @Override // com.android.server.wifi.WifiCandidates.Candidate
        public int getFrequency() {
            return this.scanDetail.getScanResult().frequency;
        }

        @Override // com.android.server.wifi.WifiCandidates.Candidate
        public WifiScoreCardProto.Signal getEventStatistics(WifiScoreCardProto.Event event) {
            WifiScoreCard.PerSignal perSignal;
            WifiScoreCard.PerBssid perBssid = this.mPerBssid;
            if (perBssid == null || (perSignal = perBssid.lookupSignal(event, getFrequency())) == null) {
                return null;
            }
            return perSignal.toSignal();
        }
    }

    public static class ScoredCandidate {
        public static final ScoredCandidate NONE = new ScoredCandidate(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, null);
        public final Key candidateKey;
        public final double err;
        public final double value;

        public ScoredCandidate(double value2, double err2, Candidate candidate) {
            this.value = value2;
            this.err = err2;
            this.candidateKey = candidate == null ? null : candidate.getKey();
        }
    }

    public static class Key {
        public final MacAddress bssid;
        public final ScanResultMatchInfo matchInfo;
        public final int networkId;

        public Key(ScanResultMatchInfo matchInfo2, MacAddress bssid2, int networkId2) {
            this.matchInfo = matchInfo2;
            this.bssid = bssid2;
            this.networkId = networkId2;
        }

        public boolean equals(Object other) {
            if (!(other instanceof Key)) {
                return false;
            }
            Key that = (Key) other;
            if (!this.matchInfo.equals(that.matchInfo) || !this.bssid.equals(that.bssid) || this.networkId != that.networkId) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            return Objects.hash(this.matchInfo, this.bssid, Integer.valueOf(this.networkId));
        }
    }

    public void setCurrent(int currentNetworkId, String currentBssid) {
        this.mCurrentNetworkId = currentNetworkId;
        this.mCurrentBssid = null;
        if (currentBssid != null) {
            try {
                this.mCurrentBssid = MacAddress.fromString(currentBssid);
            } catch (RuntimeException e) {
                failWithException(e);
            }
        }
    }

    public boolean add(ScanDetail scanDetail, WifiConfiguration config, int evaluatorId, int evaluatorScore, double lastSelectionWeightBetweenZeroAndOne) {
        if (config == null) {
            return failure(new Object[0]);
        }
        if (scanDetail == null) {
            return failure(new Object[0]);
        }
        ScanResult scanResult = scanDetail.getScanResult();
        if (scanResult == null) {
            return failure(new Object[0]);
        }
        try {
            MacAddress bssid = MacAddress.fromString(scanResult.BSSID);
            ScanResultMatchInfo key1 = ScanResultMatchInfo.fromWifiConfiguration(config);
            ScanResultMatchInfo key2 = ScanResultMatchInfo.fromScanResult(scanResult);
            if (!key1.equals(key2)) {
                return failure(key1, key2);
            }
            Key key = new Key(key1, bssid, config.networkId);
            CandidateImpl old = this.mCandidates.get(key);
            if (old != null) {
                if (evaluatorId < old.evaluatorId) {
                    return failure(new Object[0]);
                }
                if (evaluatorId > old.evaluatorId || evaluatorScore <= old.evaluatorScore) {
                    return false;
                }
                remove(old);
            }
            WifiScoreCard.PerBssid perBssid = this.mWifiScoreCard.lookupBssid(key.matchInfo.networkSsid, key.bssid.toString());
            perBssid.setSecurityType(WifiScoreCardProto.SecurityType.forNumber(key.matchInfo.networkType));
            perBssid.setNetworkConfigId(config.networkId);
            this.mCandidates.put(key, new CandidateImpl(key, scanDetail, config, evaluatorId, evaluatorScore, perBssid, Math.min(Math.max(lastSelectionWeightBetweenZeroAndOne, 0.0d), 1.0d), config.networkId == this.mCurrentNetworkId, bssid.equals(this.mCurrentBssid)));
            return true;
        } catch (RuntimeException e) {
            return failWithException(e);
        }
    }

    public boolean add(ScanDetail scanDetail, WifiConfiguration config, int evaluatorId, int evaluatorScore) {
        return add(scanDetail, config, evaluatorId, evaluatorScore, 0.0d);
    }

    public boolean remove(Candidate candidate) {
        if (!(candidate instanceof CandidateImpl)) {
            return failure(new Object[0]);
        }
        return this.mCandidates.remove(((CandidateImpl) candidate).key, (CandidateImpl) candidate);
    }

    public int size() {
        return this.mCandidates.size();
    }

    public Collection<Collection<Candidate>> getGroupedCandidates() {
        Map<Integer, Collection<Candidate>> candidatesForNetworkId = new ArrayMap<>();
        for (CandidateImpl candidate : this.mCandidates.values()) {
            Collection<Candidate> cc = candidatesForNetworkId.get(Integer.valueOf(candidate.key.networkId));
            if (cc == null) {
                cc = new ArrayList<>(2);
                candidatesForNetworkId.put(Integer.valueOf(candidate.key.networkId), cc);
            }
            cc.add(candidate);
        }
        return candidatesForNetworkId.values();
    }

    public ScoredCandidate choose(CandidateScorer candidateScorer) {
        Preconditions.checkNotNull(candidateScorer);
        ScoredCandidate choice = ScoredCandidate.NONE;
        for (Collection<Candidate> group : getGroupedCandidates()) {
            ScoredCandidate scoredCandidate = candidateScorer.scoreCandidates(group);
            if (scoredCandidate != null && scoredCandidate.value > choice.value) {
                choice = scoredCandidate;
            }
        }
        return choice;
    }

    public RuntimeException getLastFault() {
        return this.mLastFault;
    }

    public int getFaultCount() {
        return this.mFaultCount;
    }

    public void clearFaults() {
        this.mLastFault = null;
        this.mFaultCount = 0;
    }

    public WifiCandidates setPicky(boolean picky) {
        this.mPicky = picky;
        return this;
    }

    private boolean failure(Object... culprits) {
        StringJoiner joiner = new StringJoiner(",");
        for (Object c : culprits) {
            joiner.add("" + c);
        }
        return failWithException(new IllegalArgumentException(joiner.toString()));
    }

    private boolean failWithException(RuntimeException e) {
        this.mLastFault = e;
        this.mFaultCount++;
        if (!this.mPicky) {
            return false;
        }
        throw e;
    }
}
