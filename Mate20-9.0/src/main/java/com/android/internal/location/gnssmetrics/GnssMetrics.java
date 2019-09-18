package com.android.internal.location.gnssmetrics;

import android.os.SystemClock;
import android.os.connectivity.GpsBatteryStats;
import android.util.Base64;
import android.util.Log;
import android.util.TimeUtils;
import com.android.internal.app.IBatteryStats;
import com.android.internal.location.nano.GnssLogsProto;
import java.util.Arrays;

public class GnssMetrics {
    private static final int DEFAULT_TIME_BETWEEN_FIXES_MILLISECS = 1000;
    public static final int GPS_SIGNAL_QUALITY_GOOD = 1;
    public static final int GPS_SIGNAL_QUALITY_POOR = 0;
    public static final int NUM_GPS_SIGNAL_QUALITY_LEVELS = 2;
    /* access modifiers changed from: private */
    public static final String TAG = GnssMetrics.class.getSimpleName();
    private Statistics locationFailureStatistics = new Statistics();
    private String logStartInElapsedRealTime;
    /* access modifiers changed from: private */
    public GnssPowerMetrics mGnssPowerMetrics;
    private Statistics positionAccuracyMeterStatistics = new Statistics();
    private Statistics timeToFirstFixSecStatistics = new Statistics();
    private Statistics topFourAverageCn0Statistics = new Statistics();

    private class GnssPowerMetrics {
        public static final double POOR_TOP_FOUR_AVG_CN0_THRESHOLD_DB_HZ = 20.0d;
        private static final double REPORTING_THRESHOLD_DB_HZ = 1.0d;
        private final IBatteryStats mBatteryStats;
        private double mLastAverageCn0 = -100.0d;

        public GnssPowerMetrics(IBatteryStats stats) {
            this.mBatteryStats = stats;
        }

        public GnssLogsProto.PowerMetrics buildProto() {
            GnssLogsProto.PowerMetrics p = new GnssLogsProto.PowerMetrics();
            GpsBatteryStats stats = GnssMetrics.this.mGnssPowerMetrics.getGpsBatteryStats();
            if (stats != null) {
                p.loggingDurationMs = stats.getLoggingDurationMs();
                p.energyConsumedMah = ((double) stats.getEnergyConsumedMaMs()) / 3600000.0d;
                long[] t = stats.getTimeInGpsSignalQualityLevel();
                p.timeInSignalQualityLevelMs = new long[t.length];
                for (int i = 0; i < t.length; i++) {
                    p.timeInSignalQualityLevelMs[i] = t[i];
                }
            }
            return p;
        }

        public GpsBatteryStats getGpsBatteryStats() {
            try {
                return this.mBatteryStats.getGpsBatteryStats();
            } catch (Exception e) {
                Log.w(GnssMetrics.TAG, "Exception", e);
                return null;
            }
        }

        public void reportSignalQuality(float[] ascendingCN0Array, int numSv) {
            double avgCn0 = 0.0d;
            if (numSv > 0) {
                for (int i = Math.max(0, numSv - 4); i < numSv; i++) {
                    avgCn0 += (double) ascendingCN0Array[i];
                }
                avgCn0 /= (double) Math.min(numSv, 4);
            }
            if (Math.abs(avgCn0 - this.mLastAverageCn0) >= REPORTING_THRESHOLD_DB_HZ) {
                try {
                    this.mBatteryStats.noteGpsSignalQuality(getSignalLevel(avgCn0));
                    this.mLastAverageCn0 = avgCn0;
                } catch (Exception e) {
                    Log.w(GnssMetrics.TAG, "Exception", e);
                }
            }
        }

        private int getSignalLevel(double cn0) {
            if (cn0 > 20.0d) {
                return 1;
            }
            return 0;
        }
    }

    private class Statistics {
        private int count;
        private double sum;
        private double sumSquare;

        private Statistics() {
        }

        public void reset() {
            this.count = 0;
            this.sum = 0.0d;
            this.sumSquare = 0.0d;
        }

        public void addItem(double item) {
            this.count++;
            this.sum += item;
            this.sumSquare += item * item;
        }

        public int getCount() {
            return this.count;
        }

        public double getMean() {
            return this.sum / ((double) this.count);
        }

        public double getStandardDeviation() {
            double m = this.sum / ((double) this.count);
            double m2 = m * m;
            double v = this.sumSquare / ((double) this.count);
            if (v > m2) {
                return Math.sqrt(v - m2);
            }
            return 0.0d;
        }
    }

    public GnssMetrics(IBatteryStats stats) {
        this.mGnssPowerMetrics = new GnssPowerMetrics(stats);
        reset();
    }

    public void logReceivedLocationStatus(boolean isSuccessful) {
        if (!isSuccessful) {
            this.locationFailureStatistics.addItem(1.0d);
        } else {
            this.locationFailureStatistics.addItem(0.0d);
        }
    }

    public void logMissedReports(int desiredTimeBetweenFixesMilliSeconds, int actualTimeBetweenFixesMilliSeconds) {
        int numReportMissed = (actualTimeBetweenFixesMilliSeconds / Math.max(1000, desiredTimeBetweenFixesMilliSeconds)) - 1;
        if (numReportMissed > 0) {
            for (int i = 0; i < numReportMissed; i++) {
                this.locationFailureStatistics.addItem(1.0d);
            }
        }
    }

    public void logTimeToFirstFixMilliSecs(int timeToFirstFixMilliSeconds) {
        this.timeToFirstFixSecStatistics.addItem((double) (timeToFirstFixMilliSeconds / 1000));
    }

    public void logPositionAccuracyMeters(float positionAccuracyMeters) {
        this.positionAccuracyMeterStatistics.addItem((double) positionAccuracyMeters);
    }

    public void logCn0(float[] cn0s, int numSv) {
        if (numSv == 0 || cn0s == null || cn0s.length == 0 || cn0s.length < numSv) {
            if (numSv == 0) {
                this.mGnssPowerMetrics.reportSignalQuality(null, 0);
            }
            return;
        }
        float[] cn0Array = Arrays.copyOf(cn0s, numSv);
        Arrays.sort(cn0Array);
        this.mGnssPowerMetrics.reportSignalQuality(cn0Array, numSv);
        if (numSv >= 4 && ((double) cn0Array[numSv - 4]) > 0.0d) {
            double top4AvgCn0 = 0.0d;
            for (int i = numSv - 4; i < numSv; i++) {
                top4AvgCn0 += (double) cn0Array[i];
            }
            this.topFourAverageCn0Statistics.addItem(top4AvgCn0 / 4.0d);
        }
    }

    public String dumpGnssMetricsAsProtoString() {
        GnssLogsProto.GnssLog msg = new GnssLogsProto.GnssLog();
        if (this.locationFailureStatistics.getCount() > 0) {
            msg.numLocationReportProcessed = this.locationFailureStatistics.getCount();
            msg.percentageLocationFailure = (int) (100.0d * this.locationFailureStatistics.getMean());
        }
        if (this.timeToFirstFixSecStatistics.getCount() > 0) {
            msg.numTimeToFirstFixProcessed = this.timeToFirstFixSecStatistics.getCount();
            msg.meanTimeToFirstFixSecs = (int) this.timeToFirstFixSecStatistics.getMean();
            msg.standardDeviationTimeToFirstFixSecs = (int) this.timeToFirstFixSecStatistics.getStandardDeviation();
        }
        if (this.positionAccuracyMeterStatistics.getCount() > 0) {
            msg.numPositionAccuracyProcessed = this.positionAccuracyMeterStatistics.getCount();
            msg.meanPositionAccuracyMeters = (int) this.positionAccuracyMeterStatistics.getMean();
            msg.standardDeviationPositionAccuracyMeters = (int) this.positionAccuracyMeterStatistics.getStandardDeviation();
        }
        if (this.topFourAverageCn0Statistics.getCount() > 0) {
            msg.numTopFourAverageCn0Processed = this.topFourAverageCn0Statistics.getCount();
            msg.meanTopFourAverageCn0DbHz = this.topFourAverageCn0Statistics.getMean();
            msg.standardDeviationTopFourAverageCn0DbHz = this.topFourAverageCn0Statistics.getStandardDeviation();
        }
        msg.powerMetrics = this.mGnssPowerMetrics.buildProto();
        String s = Base64.encodeToString(GnssLogsProto.GnssLog.toByteArray(msg), 0);
        reset();
        return s;
    }

    public String dumpGnssMetricsAsText() {
        StringBuilder s = new StringBuilder();
        s.append("GNSS_KPI_START");
        s.append(10);
        s.append("  KPI logging start time: ");
        s.append(this.logStartInElapsedRealTime);
        s.append("\n");
        s.append("  KPI logging end time: ");
        TimeUtils.formatDuration(SystemClock.elapsedRealtimeNanos() / 1000000, s);
        s.append("\n");
        s.append("  Number of location reports: ");
        s.append(this.locationFailureStatistics.getCount());
        s.append("\n");
        if (this.locationFailureStatistics.getCount() > 0) {
            s.append("  Percentage location failure: ");
            s.append(100.0d * this.locationFailureStatistics.getMean());
            s.append("\n");
        }
        s.append("  Number of TTFF reports: ");
        s.append(this.timeToFirstFixSecStatistics.getCount());
        s.append("\n");
        if (this.timeToFirstFixSecStatistics.getCount() > 0) {
            s.append("  TTFF mean (sec): ");
            s.append(this.timeToFirstFixSecStatistics.getMean());
            s.append("\n");
            s.append("  TTFF standard deviation (sec): ");
            s.append(this.timeToFirstFixSecStatistics.getStandardDeviation());
            s.append("\n");
        }
        s.append("  Number of position accuracy reports: ");
        s.append(this.positionAccuracyMeterStatistics.getCount());
        s.append("\n");
        if (this.positionAccuracyMeterStatistics.getCount() > 0) {
            s.append("  Position accuracy mean (m): ");
            s.append(this.positionAccuracyMeterStatistics.getMean());
            s.append("\n");
            s.append("  Position accuracy standard deviation (m): ");
            s.append(this.positionAccuracyMeterStatistics.getStandardDeviation());
            s.append("\n");
        }
        s.append("  Number of CN0 reports: ");
        s.append(this.topFourAverageCn0Statistics.getCount());
        s.append("\n");
        if (this.topFourAverageCn0Statistics.getCount() > 0) {
            s.append("  Top 4 Avg CN0 mean (dB-Hz): ");
            s.append(this.topFourAverageCn0Statistics.getMean());
            s.append("\n");
            s.append("  Top 4 Avg CN0 standard deviation (dB-Hz): ");
            s.append(this.topFourAverageCn0Statistics.getStandardDeviation());
            s.append("\n");
        }
        s.append("GNSS_KPI_END");
        s.append("\n");
        GpsBatteryStats stats = this.mGnssPowerMetrics.getGpsBatteryStats();
        if (stats != null) {
            s.append("Power Metrics");
            s.append("\n");
            s.append("  Time on battery (min): " + (((double) stats.getLoggingDurationMs()) / 60000.0d));
            s.append("\n");
            long[] t = stats.getTimeInGpsSignalQualityLevel();
            if (t != null && t.length == 2) {
                s.append("  Amount of time (while on battery) Top 4 Avg CN0 > " + Double.toString(20.0d) + " dB-Hz (min): ");
                s.append(((double) t[1]) / 60000.0d);
                s.append("\n");
                s.append("  Amount of time (while on battery) Top 4 Avg CN0 <= " + Double.toString(20.0d) + " dB-Hz (min): ");
                s.append(((double) t[0]) / 60000.0d);
                s.append("\n");
            }
            s.append("  Energy consumed while on battery (mAh): ");
            s.append(((double) stats.getEnergyConsumedMaMs()) / 3600000.0d);
            s.append("\n");
        }
        return s.toString();
    }

    private void reset() {
        StringBuilder s = new StringBuilder();
        TimeUtils.formatDuration(SystemClock.elapsedRealtimeNanos() / 1000000, s);
        this.logStartInElapsedRealTime = s.toString();
        this.locationFailureStatistics.reset();
        this.timeToFirstFixSecStatistics.reset();
        this.positionAccuracyMeterStatistics.reset();
        this.topFourAverageCn0Statistics.reset();
    }
}
