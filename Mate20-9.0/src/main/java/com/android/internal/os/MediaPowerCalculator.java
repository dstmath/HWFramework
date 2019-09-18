package com.android.internal.os;

import android.os.BatteryStats;

public class MediaPowerCalculator extends PowerCalculator {
    private static final int MS_IN_HR = 3600000;
    private final double mAudioAveragePowerMa;
    private final double mVideoAveragePowerMa;

    public MediaPowerCalculator(PowerProfile profile) {
        this.mAudioAveragePowerMa = profile.getAveragePower(PowerProfile.POWER_AUDIO);
        this.mVideoAveragePowerMa = profile.getAveragePower(PowerProfile.POWER_VIDEO);
    }

    public void calculateApp(BatterySipper app, BatteryStats.Uid u, long rawRealtimeUs, long rawUptimeUs, int statsType) {
        BatterySipper batterySipper = app;
        long j = rawRealtimeUs;
        int i = statsType;
        BatteryStats.Timer audioTimer = u.getAudioTurnedOnTimer();
        if (audioTimer == null) {
            batterySipper.audioTimeMs = 0;
            batterySipper.audioPowerMah = 0.0d;
        } else {
            long totalTime = audioTimer.getTotalTimeLocked(j, i) / 1000;
            batterySipper.audioTimeMs = totalTime;
            batterySipper.audioPowerMah = (((double) totalTime) * this.mAudioAveragePowerMa) / 3600000.0d;
        }
        BatteryStats.Timer videoTimer = u.getVideoTurnedOnTimer();
        if (videoTimer == null) {
            batterySipper.videoTimeMs = 0;
            batterySipper.videoPowerMah = 0.0d;
            return;
        }
        long totalTime2 = videoTimer.getTotalTimeLocked(j, i) / 1000;
        batterySipper.videoTimeMs = totalTime2;
        batterySipper.videoPowerMah = (((double) totalTime2) * this.mVideoAveragePowerMa) / 3600000.0d;
    }
}
