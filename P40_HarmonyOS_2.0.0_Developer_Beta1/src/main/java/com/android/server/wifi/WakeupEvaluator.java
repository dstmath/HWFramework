package com.android.server.wifi;

import android.net.wifi.ScanResult;
import java.util.Collection;

public class WakeupEvaluator {
    private final ScoringParams mScoringParams;

    WakeupEvaluator(ScoringParams scoringParams) {
        this.mScoringParams = scoringParams;
    }

    public ScanResult findViableNetwork(Collection<ScanResult> scanResults, Collection<ScanResultMatchInfo> networks) {
        ScanResult selectedScanResult = null;
        for (ScanResult scanResult : scanResults) {
            if (!isBelowThreshold(scanResult) && networks.contains(ScanResultMatchInfo.fromScanResult(scanResult))) {
                if (selectedScanResult == null || selectedScanResult.level < scanResult.level) {
                    selectedScanResult = scanResult;
                }
            }
        }
        return selectedScanResult;
    }

    public boolean isBelowThreshold(ScanResult scanResult) {
        return scanResult.level < this.mScoringParams.getEntryRssi(scanResult.frequency);
    }
}
