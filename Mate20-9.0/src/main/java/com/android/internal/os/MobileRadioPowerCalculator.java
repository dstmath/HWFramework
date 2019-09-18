package com.android.internal.os;

import android.os.BatteryStats;

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
        int i = statsType;
        double MOBILE_POWER = this.mPowerRadioOn / 3600.0d;
        long mobileData = this.mStats.getNetworkActivityPackets(0, i) + this.mStats.getNetworkActivityPackets(1, i);
        long radioDataUptimeMs = this.mStats.getMobileRadioActiveTime(rawRealtimeUs, i) / 1000;
        if (mobileData == 0 || radioDataUptimeMs == 0) {
            mobilePps = 12.20703125d;
        } else {
            mobilePps = ((double) mobileData) / ((double) radioDataUptimeMs);
        }
        return (MOBILE_POWER / mobilePps) / 3600.0d;
    }

    public MobileRadioPowerCalculator(PowerProfile profile, BatteryStats stats) {
        double temp = profile.getAveragePowerOrDefault(PowerProfile.POWER_RADIO_ACTIVE, -1.0d);
        int i = 0;
        int i2 = 1;
        if (temp != -1.0d) {
            this.mPowerRadioOn = temp;
        } else {
            double sum = 0.0d + profile.getAveragePower(PowerProfile.POWER_MODEM_CONTROLLER_RX);
            for (int i3 = 0; i3 < this.mPowerBins.length; i3++) {
                sum += profile.getAveragePower(PowerProfile.POWER_MODEM_CONTROLLER_TX, i3);
            }
            this.mPowerRadioOn = sum / ((double) (this.mPowerBins.length + 1));
        }
        if (profile.getAveragePowerOrDefault(PowerProfile.POWER_RADIO_ON, -1.0d) == -1.0d) {
            double idle = profile.getAveragePower(PowerProfile.POWER_MODEM_CONTROLLER_IDLE);
            this.mPowerBins[0] = (25.0d * idle) / 180.0d;
            while (true) {
                int i4 = i2;
                if (i4 >= this.mPowerBins.length) {
                    break;
                }
                this.mPowerBins[i4] = Math.max(1.0d, idle / 256.0d);
                i2 = i4 + 1;
            }
        } else {
            while (true) {
                int i5 = i;
                if (i5 >= this.mPowerBins.length) {
                    break;
                }
                this.mPowerBins[i5] = profile.getAveragePower(PowerProfile.POWER_RADIO_ON, i5);
                i = i5 + 1;
            }
        }
        this.mPowerScan = profile.getAveragePowerOrDefault(PowerProfile.POWER_RADIO_SCANNING, 0.0d);
        this.mStats = stats;
    }

    public void calculateApp(BatterySipper app, BatteryStats.Uid u, long rawRealtimeUs, long rawUptimeUs, int statsType) {
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
        BatterySipper batterySipper = app;
        BatteryStats batteryStats = stats;
        long j = rawRealtimeUs;
        int i = statsType;
        double power = 0.0d;
        long signalTimeMs = 0;
        double p = 0;
        for (int i2 = 0; i2 < this.mPowerBins.length; i2++) {
            long strengthTimeMs = batteryStats.getPhoneSignalStrengthTime(i2, j, i) / 1000;
            double noCoverageTimeMs = p;
            power += (((double) strengthTimeMs) * this.mPowerBins[i2]) / 3600000.0d;
            signalTimeMs += strengthTimeMs;
            if (i2 == 0) {
                p = strengthTimeMs;
            } else {
                p = noCoverageTimeMs;
            }
        }
        double noCoverageTimeMs2 = p;
        long scanningTimeMs = batteryStats.getPhoneSignalScanningTime(j, i) / 1000;
        double p2 = (((double) scanningTimeMs) * this.mPowerScan) / 3600000.0d;
        double power2 = power + p2;
        long remainingActiveTimeMs = 0 - this.mTotalAppMobileActiveMs;
        if (remainingActiveTimeMs > 0) {
            long j2 = scanningTimeMs;
            double d = p2;
            power2 += (this.mPowerRadioOn * ((double) remainingActiveTimeMs)) / 3600000.0d;
        } else {
            double d2 = p2;
        }
        if (power2 != 0.0d) {
            if (signalTimeMs != 0) {
                long noCoverageTimeMs3 = noCoverageTimeMs2;
                long j3 = noCoverageTimeMs3;
                batterySipper.noCoveragePercent = (((double) noCoverageTimeMs3) * 100.0d) / ((double) signalTimeMs);
            }
            batterySipper.mobileActive = remainingActiveTimeMs;
            batterySipper.mobileActiveCount = batteryStats.getMobileRadioActiveUnknownCount(i);
            batterySipper.mobileRadioPowerMah = power2;
            return;
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
