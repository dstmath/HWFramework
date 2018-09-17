package com.android.server.wifi.scanner;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiScanner;
import android.net.wifi.WifiScanner.ScanData;
import android.util.Log;
import com.android.server.wifi.WifiNative.BucketSettings;
import com.android.server.wifi.WifiNative.ChannelSettings;
import com.android.server.wifi.WifiNative.ScanSettings;
import java.util.ArrayList;
import java.util.List;

public class ScanScheduleUtil {
    public static boolean channelEquals(ChannelSettings channel1, ChannelSettings channel2) {
        boolean z = true;
        if (channel1 == null || channel2 == null) {
            return false;
        }
        if (channel1 == channel2) {
            return true;
        }
        if (channel1.frequency != channel2.frequency || channel1.dwell_time_ms != channel2.dwell_time_ms) {
            return false;
        }
        if (channel1.passive != channel2.passive) {
            z = false;
        }
        return z;
    }

    public static boolean bucketEquals(BucketSettings bucket1, BucketSettings bucket2) {
        if (bucket1 == null || bucket2 == null) {
            return false;
        }
        if (bucket1 == bucket2) {
            return true;
        }
        if (bucket1.bucket != bucket2.bucket || bucket1.band != bucket2.band || bucket1.period_ms != bucket2.period_ms || bucket1.report_events != bucket2.report_events || bucket1.num_channels != bucket2.num_channels) {
            return false;
        }
        for (int c = 0; c < bucket1.num_channels; c++) {
            if (!channelEquals(bucket1.channels[c], bucket2.channels[c])) {
                return false;
            }
        }
        return true;
    }

    public static boolean scheduleEquals(ScanSettings schedule1, ScanSettings schedule2) {
        if (schedule1 == null || schedule2 == null) {
            return false;
        }
        if (schedule1 == schedule2) {
            return true;
        }
        if (schedule1.base_period_ms != schedule2.base_period_ms || schedule1.max_ap_per_scan != schedule2.max_ap_per_scan || schedule1.report_threshold_percent != schedule2.report_threshold_percent || schedule1.report_threshold_num_scans != schedule2.report_threshold_num_scans || schedule1.num_buckets != schedule2.num_buckets) {
            return false;
        }
        for (int b = 0; b < schedule1.num_buckets; b++) {
            if (!bucketEquals(schedule1.buckets[b], schedule2.buckets[b])) {
                return false;
            }
        }
        return true;
    }

    private static boolean isBucketMaybeScanned(int scheduledBucket, int bucketsScannedBitSet) {
        boolean z = true;
        if (bucketsScannedBitSet == 0 || scheduledBucket < 0) {
            return true;
        }
        if (((1 << scheduledBucket) & bucketsScannedBitSet) == 0) {
            z = false;
        }
        return z;
    }

    private static boolean isBucketDefinitlyScanned(int scheduledBucket, int bucketsScannedBitSet) {
        boolean z = true;
        if (scheduledBucket < 0) {
            return true;
        }
        if (bucketsScannedBitSet == 0) {
            return false;
        }
        if (((1 << scheduledBucket) & bucketsScannedBitSet) == 0) {
            z = false;
        }
        return z;
    }

    public static boolean shouldReportFullScanResultForSettings(ChannelHelper channelHelper, ScanResult result, int bucketsScanned, WifiScanner.ScanSettings settings, int scheduledBucket) {
        if (isBucketMaybeScanned(scheduledBucket, bucketsScanned)) {
            return channelHelper.settingsContainChannel(settings, result.frequency);
        }
        Log.w("WifiScanLog", "isBucketMaybeScanned return false " + scheduledBucket + ", " + bucketsScanned);
        return false;
    }

    public static ScanData[] filterResultsForSettings(ChannelHelper channelHelper, ScanData[] scanDatas, WifiScanner.ScanSettings settings, int scheduledBucket) {
        List<ScanData> filteredScanDatas = new ArrayList(scanDatas.length);
        List<ScanResult> filteredResults = new ArrayList();
        StringBuilder keyLogs = new StringBuilder();
        for (ScanData scanData : scanDatas) {
            if (isBucketMaybeScanned(scheduledBucket, scanData.getBucketsScanned())) {
                filteredResults.clear();
                for (ScanResult scanResult : scanData.getResults()) {
                    if (channelHelper.settingsContainChannel(settings, scanResult.frequency)) {
                        filteredResults.add(scanResult);
                    }
                    if (settings.numBssidsPerScan > 0 && filteredResults.size() >= settings.numBssidsPerScan) {
                        keyLogs.append(", result size:").append(filteredResults.size()).append(", numBssidsPerScan:").append(settings.numBssidsPerScan);
                        break;
                    }
                }
                if (filteredResults.size() == scanData.getResults().length) {
                    filteredScanDatas.add(scanData);
                } else if (filteredResults.size() > 0 || isBucketDefinitlyScanned(scheduledBucket, scanData.getBucketsScanned())) {
                    filteredScanDatas.add(new ScanData(scanData.getId(), scanData.getFlags(), (ScanResult[]) filteredResults.toArray(new ScanResult[filteredResults.size()])));
                }
            } else {
                keyLogs.append(",isBucketMaybeScanned false scheduledBucket:").append(scheduledBucket).append(" scanData.getBucketsScanned() ").append(scanData.getBucketsScanned());
            }
        }
        if (keyLogs.length() != 0) {
            Log.w("WifiScanLog", keyLogs.toString());
        }
        if (filteredScanDatas.size() != 0) {
            return (ScanData[]) filteredScanDatas.toArray(new ScanData[filteredScanDatas.size()]);
        }
        Log.w("WifiScanLog", "filteredScanDatas.size == 0");
        return null;
    }
}
