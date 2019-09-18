package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import com.android.internal.annotations.VisibleForTesting;
import java.util.Collection;

public class WakeupEvaluator {
    private final int mThresholdMinimumRssi24;
    private final int mThresholdMinimumRssi5;

    public static WakeupEvaluator fromContext(Context context) {
        ScoringParams scoringParams = new ScoringParams(context);
        return new WakeupEvaluator(scoringParams.getEntryRssi(ScoringParams.BAND2), scoringParams.getEntryRssi(ScoringParams.BAND5));
    }

    @VisibleForTesting
    WakeupEvaluator(int minimumRssi24, int minimumRssi5) {
        this.mThresholdMinimumRssi24 = minimumRssi24;
        this.mThresholdMinimumRssi5 = minimumRssi5;
    }

    public ScanResult findViableNetwork(Collection<ScanResult> scanResults, Collection<ScanResultMatchInfo> savedNetworks) {
        ScanResult selectedScanResult = null;
        for (ScanResult scanResult : scanResults) {
            if (!isBelowThreshold(scanResult) && savedNetworks.contains(ScanResultMatchInfo.fromScanResult(scanResult))) {
                if (selectedScanResult == null || selectedScanResult.level < scanResult.level) {
                    selectedScanResult = scanResult;
                }
            }
        }
        return selectedScanResult;
    }

    public boolean isBelowThreshold(ScanResult scanResult) {
        return (scanResult.is24GHz() && scanResult.level < this.mThresholdMinimumRssi24) || (scanResult.is5GHz() && scanResult.level < this.mThresholdMinimumRssi5);
    }
}
