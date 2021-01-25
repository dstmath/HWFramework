package com.android.internal.os;

import android.os.BatteryStats;

public class MediaPowerCalculator extends PowerCalculator {
    private static final int MS_IN_HR = 3600000;
    private final double mAudioAveragePowerMa;
    private final double mVideoAveragePowerMa;

    public MediaPowerCalculator(PowerProfile profile) {
        this.mAudioAveragePowerMa = profile.getAveragePower("audio");
        this.mVideoAveragePowerMa = profile.getAveragePower("video");
    }

    @Override // com.android.internal.os.PowerCalculator
    public void calculateApp(BatterySipper app, BatteryStats.Uid u, long rawRealtimeUs, long rawUptimeUs, int statsType) {
        BatteryStats.Timer audioTimer = u.getAudioTurnedOnTimer();
        if (audioTimer == null) {
            app.audioTimeMs = 0;
            app.audioPowerMah = 0.0d;
        } else {
            long totalTime = audioTimer.getTotalTimeLocked(rawRealtimeUs, statsType) / 1000;
            app.audioTimeMs = totalTime;
            app.audioPowerMah = (((double) totalTime) * this.mAudioAveragePowerMa) / 3600000.0d;
        }
        BatteryStats.Timer videoTimer = u.getVideoTurnedOnTimer();
        if (videoTimer == null) {
            app.videoTimeMs = 0;
            app.videoPowerMah = 0.0d;
            return;
        }
        long totalTime2 = videoTimer.getTotalTimeLocked(rawRealtimeUs, statsType) / 1000;
        app.videoTimeMs = totalTime2;
        app.videoPowerMah = (((double) totalTime2) * this.mVideoAveragePowerMa) / 3600000.0d;
    }
}
