package com.android.internal.os;

import android.os.BatteryStats;

public class MobileRadioPowerCalculator extends PowerCalculator {
    private static final boolean DEBUG = false;
    private static final String TAG = "MobileRadioPowerController";
    private final double[] mPowerBins = new double[6];
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
        double[] dArr;
        double temp = profile.getAveragePowerOrDefault(PowerProfile.POWER_RADIO_ACTIVE, -1.0d);
        if (temp != -1.0d) {
            this.mPowerRadioOn = temp;
        } else {
            double sum = 0.0d + profile.getAveragePower(PowerProfile.POWER_MODEM_CONTROLLER_RX);
            int i = 0;
            while (true) {
                dArr = this.mPowerBins;
                if (i >= dArr.length) {
                    break;
                }
                sum += profile.getAveragePower(PowerProfile.POWER_MODEM_CONTROLLER_TX, i);
                i++;
            }
            this.mPowerRadioOn = sum / ((double) (dArr.length + 1));
        }
        if (profile.getAveragePowerOrDefault(PowerProfile.POWER_RADIO_ON, -1.0d) == -1.0d) {
            double idle = profile.getAveragePower(PowerProfile.POWER_MODEM_CONTROLLER_IDLE);
            this.mPowerBins[0] = (25.0d * idle) / 180.0d;
            int i2 = 1;
            while (true) {
                double[] dArr2 = this.mPowerBins;
                if (i2 >= dArr2.length) {
                    break;
                }
                dArr2[i2] = Math.max(1.0d, idle / 256.0d);
                i2++;
            }
        } else {
            int i3 = 0;
            while (true) {
                double[] dArr3 = this.mPowerBins;
                if (i3 >= dArr3.length) {
                    break;
                }
                dArr3[i3] = profile.getAveragePower(PowerProfile.POWER_RADIO_ON, i3);
                i3++;
            }
        }
        this.mPowerScan = profile.getAveragePowerOrDefault(PowerProfile.POWER_RADIO_SCANNING, 0.0d);
        this.mStats = stats;
    }

    @Override // com.android.internal.os.PowerCalculator
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

    @Override // com.android.internal.os.PowerCalculator
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
            } else {
                noCoverageTimeMs = noCoverageTimeMs;
            }
        }
        double power2 = power + ((((double) (stats.getPhoneSignalScanningTime(rawRealtimeUs, statsType) / 1000)) * this.mPowerScan) / 3600000.0d);
        long remainingActiveTimeMs = (this.mStats.getMobileRadioActiveTime(rawRealtimeUs, statsType) / 1000) - this.mTotalAppMobileActiveMs;
        if (remainingActiveTimeMs > 0) {
            power2 += (this.mPowerRadioOn * ((double) remainingActiveTimeMs)) / 3600000.0d;
        }
        if (power2 != 0.0d) {
            if (signalTimeMs != 0) {
                app.noCoveragePercent = (((double) noCoverageTimeMs) * 100.0d) / ((double) signalTimeMs);
            }
            app.mobileActive = remainingActiveTimeMs;
            app.mobileActiveCount = stats.getMobileRadioActiveUnknownCount(statsType);
            app.mobileRadioPowerMah = power2;
        }
    }

    @Override // com.android.internal.os.PowerCalculator
    public void reset() {
        this.mTotalAppMobileActiveMs = 0;
    }

    public void reset(BatteryStats stats) {
        reset();
        this.mStats = stats;
    }
}
