package com.android.internal.os;

import android.os.BatteryStats;
import android.os.BatteryStats.Uid;

public class MobileRadioPowerCalculator extends PowerCalculator {
    private static final boolean DEBUG = false;
    private static final String TAG = "MobileRadioPowerController";
    private final double[] mPowerBins = new double[5];
    private final double mPowerRadioOn;
    private final double mPowerScan;
    private BatteryStats mStats;
    private long mTotalAppMobileActiveMs = 0;

    private double getMobilePowerPerPacket(long rawRealtimeUs, int statsType) {
        double mobilePps;
        double MOBILE_POWER = this.mPowerRadioOn / 3600.0d;
        long mobileData = this.mStats.getNetworkActivityPackets(0, statsType) + this.mStats.getNetworkActivityPackets(1, statsType);
        long radioDataUptimeMs = this.mStats.getMobileRadioActiveTime(rawRealtimeUs, statsType) / 1000;
        if (mobileData == 0 || radioDataUptimeMs == 0) {
            mobilePps = 12.20703125d;
        } else {
            mobilePps = ((double) mobileData) / ((double) radioDataUptimeMs);
        }
        return (MOBILE_POWER / mobilePps) / 3600.0d;
    }

    public MobileRadioPowerCalculator(PowerProfile profile, BatteryStats stats) {
        this.mPowerRadioOn = profile.getAveragePower(PowerProfile.POWER_RADIO_ACTIVE);
        for (int i = 0; i < this.mPowerBins.length; i++) {
            this.mPowerBins[i] = profile.getAveragePower(PowerProfile.POWER_RADIO_ON, i);
        }
        this.mPowerScan = profile.getAveragePower(PowerProfile.POWER_RADIO_SCANNING);
        this.mStats = stats;
    }

    public void calculateApp(BatterySipper app, Uid u, long rawRealtimeUs, long rawUptimeUs, int statsType) {
        app.mobileRxPackets = u.getNetworkActivityPackets(0, statsType);
        app.mobileTxPackets = u.getNetworkActivityPackets(1, statsType);
        app.mobileActive = u.getMobileRadioActiveTime(statsType) / 1000;
        app.mobileActiveCount = u.getMobileRadioActiveCount(statsType);
        app.mobileRxBytes = u.getNetworkActivityBytes(0, statsType);
        app.mobileTxBytes = u.getNetworkActivityBytes(1, statsType);
        if (app.mobileActive > 0) {
            this.mTotalAppMobileActiveMs += app.mobileActive;
            app.mobileRadioPowerMah = (((double) app.mobileActive) * this.mPowerRadioOn) / 3600000.0d;
            return;
        }
        app.mobileRadioPowerMah = ((double) (app.mobileRxPackets + app.mobileTxPackets)) * getMobilePowerPerPacket(rawRealtimeUs, statsType);
    }

    public void calculateRemaining(BatterySipper app, BatteryStats stats, long rawRealtimeUs, long rawUptimeUs, int statsType) {
        double power = 0.0d;
        long signalTimeMs = 0;
        long noCoverageTimeMs = 0;
        for (int i = 0; i < this.mPowerBins.length; i++) {
            long strengthTimeMs = stats.getPhoneSignalStrengthTime(i, rawRealtimeUs, statsType) / 1000;
            power += (((double) strengthTimeMs) * this.mPowerBins[i]) / 3600000.0d;
            signalTimeMs += strengthTimeMs;
            if (i == 0) {
                noCoverageTimeMs = strengthTimeMs;
            }
        }
        power += (((double) (stats.getPhoneSignalScanningTime(rawRealtimeUs, statsType) / 1000)) * this.mPowerScan) / 3600000.0d;
        long remainingActiveTimeMs = 0 - this.mTotalAppMobileActiveMs;
        if (remainingActiveTimeMs > 0) {
            power += (this.mPowerRadioOn * ((double) remainingActiveTimeMs)) / 3600000.0d;
        }
        if (power != 0.0d) {
            if (signalTimeMs != 0) {
                app.noCoveragePercent = (((double) noCoverageTimeMs) * 100.0d) / ((double) signalTimeMs);
            }
            app.mobileActive = remainingActiveTimeMs;
            app.mobileActiveCount = stats.getMobileRadioActiveUnknownCount(statsType);
            app.mobileRadioPowerMah = power;
        }
    }

    public void reset() {
        this.mTotalAppMobileActiveMs = 0;
    }

    public void reset(BatteryStats stats) {
        reset();
        this.mStats = stats;
    }
}
