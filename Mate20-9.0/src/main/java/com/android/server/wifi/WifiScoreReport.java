package com.android.server.wifi;

import android.net.NetworkAgent;
import android.net.wifi.WifiInfo;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;

public class WifiScoreReport {
    private static final int DUMPSYS_ENTRY_COUNT_LIMIT = 3600;
    public static final String DUMP_ARG = "WifiScoreReport";
    private static final long FIRST_REASONABLE_WALL_CLOCK = 1490000000000L;
    private static final int LOW_SCORE_COUNT_MAX = 10;
    private static final long NUD_THROTTLE_MILLIS = 5000;
    private static final String TAG = "WifiScoreReport";
    private static final double TIME_CONSTANT_MILLIS = 30000.0d;
    private static final int WIFI_SCORE_BAD = 58;
    private static final int WIFI_SCORE_GOOD = 60;
    private int lowScoreCount = 0;
    ConnectedScore mAggressiveConnectedScore;
    private final Clock mClock;
    private int mLastKnownNudCheckScore = 50;
    private long mLastKnownNudCheckTimeMillis = 0;
    private LinkedList<String> mLinkMetricsHistory = new LinkedList<>();
    private int mNudCount = 0;
    private int mNudYes = 0;
    private int mScore = 60;
    private final ScoringParams mScoringParams;
    private int mSessionNumber = 0;
    VelocityBasedConnectedScore mVelocityBasedConnectedScore;
    private boolean mVerboseLoggingEnabled = false;

    public void setLowScoreCount(int lowScoreCount2) {
        this.lowScoreCount = lowScoreCount2;
    }

    WifiScoreReport(ScoringParams scoringParams, Clock clock) {
        this.mScoringParams = scoringParams;
        this.mClock = clock;
        this.mAggressiveConnectedScore = new AggressiveConnectedScore(scoringParams, clock);
        this.mVelocityBasedConnectedScore = new VelocityBasedConnectedScore(scoringParams, clock);
    }

    public void reset() {
        this.mSessionNumber++;
        this.mScore = 60;
        this.mLastKnownNudCheckScore = 50;
        this.mAggressiveConnectedScore.reset();
        this.mVelocityBasedConnectedScore.reset();
        if (this.mVerboseLoggingEnabled) {
            Log.d("WifiScoreReport", "reset");
        }
    }

    public void enableVerboseLogging(boolean enable) {
        this.mVerboseLoggingEnabled = enable;
    }

    public void calculateAndReportScore(WifiInfo wifiInfo, NetworkAgent networkAgent, WifiMetrics wifiMetrics) {
        WifiInfo wifiInfo2 = wifiInfo;
        NetworkAgent networkAgent2 = networkAgent;
        if (wifiInfo.getRssi() == -127) {
            Log.d("WifiScoreReport", "Not reporting score because RSSI is invalid");
            return;
        }
        long millis = this.mClock.getWallClockMillis();
        int netId = 0;
        if (networkAgent2 != null) {
            netId = networkAgent2.netId;
        }
        int netId2 = netId;
        this.mAggressiveConnectedScore.updateUsingWifiInfo(wifiInfo2, millis);
        this.mVelocityBasedConnectedScore.updateUsingWifiInfo(wifiInfo2, millis);
        int s1 = this.mAggressiveConnectedScore.generateScore();
        int s2 = this.mVelocityBasedConnectedScore.generateScore();
        int score = s2;
        if (wifiInfo2.score > 50 && score <= 50 && wifiInfo2.txSuccessRate >= ((double) this.mScoringParams.getYippeeSkippyPacketsPerSecond()) && wifiInfo2.rxSuccessRate >= ((double) this.mScoringParams.getYippeeSkippyPacketsPerSecond())) {
            score = 51;
        }
        if (wifiInfo2.score > 50 && score <= 50) {
            int entry = this.mScoringParams.getEntryRssi(wifiInfo.getFrequency());
            if (this.mVelocityBasedConnectedScore.getFilteredRssi() >= ((double) entry) || wifiInfo.getRssi() >= entry) {
                score = 51;
            }
        }
        int rawScore = score;
        if (score > 60) {
            score = 60;
        }
        if (score < 0) {
            score = 0;
        }
        int score2 = score;
        long j = millis;
        logLinkMetrics(wifiInfo2, millis, netId2, s1, s2, score2);
        int score3 = WifiInjector.getInstance().getWifiStateMachine().resetScoreByInetAccess(score2 == 60 ? 60 : 58);
        boolean wifiConnectivityManagerEnabled = WifiInjector.getInstance().getWifiStateMachine().isWifiConnectivityManagerEnabled();
        Log.d("WifiScoreReport", "Score = " + score3 + ", wifiConnectivityManagerEnabled = " + wifiConnectivityManagerEnabled + ", lowScoreCount = " + this.lowScoreCount);
        if (score3 == 60) {
            if (!wifiConnectivityManagerEnabled) {
                this.lowScoreCount = 0;
                return;
            }
        } else if (score3 == 58) {
            if (!wifiConnectivityManagerEnabled) {
                int i = this.lowScoreCount;
                this.lowScoreCount = i + 1;
                if (i < 10) {
                    return;
                }
            } else {
                return;
            }
        }
        this.lowScoreCount = 0;
        if (score3 != wifiInfo2.score) {
            Log.d("WifiScoreReport", " rawScore = " + rawScore + ", score = " + score3);
            if (this.mVerboseLoggingEnabled) {
                Log.d("WifiScoreReport", "report new wifi score " + score3);
            }
            wifiInfo2.score = score3;
            if (networkAgent2 != null) {
                networkAgent2.sendNetworkScore(score3);
            }
        }
        wifiMetrics.incrementWifiScoreCount(score3);
        this.mScore = score3;
    }

    public boolean shouldCheckIpLayer() {
        int nud = this.mScoringParams.getNudKnob();
        if (nud == 0) {
            return false;
        }
        long deltaMillis = this.mClock.getWallClockMillis() - this.mLastKnownNudCheckTimeMillis;
        if (deltaMillis < NUD_THROTTLE_MILLIS) {
            return false;
        }
        double deltaLevel = (double) (11 - nud);
        double nextNudBreach = 50.0d;
        if (this.mLastKnownNudCheckScore < 50 && ((double) deltaMillis) < 150000.0d) {
            double a = Math.exp(((double) (-deltaMillis)) / TIME_CONSTANT_MILLIS);
            nextNudBreach = ((((double) this.mLastKnownNudCheckScore) - deltaLevel) * a) + ((1.0d - a) * 50.0d);
        }
        if (((double) this.mScore) >= nextNudBreach) {
            return false;
        }
        this.mNudYes++;
        return true;
    }

    public void noteIpCheck() {
        this.mLastKnownNudCheckTimeMillis = this.mClock.getWallClockMillis();
        this.mLastKnownNudCheckScore = this.mScore;
        this.mNudCount++;
    }

    private void logLinkMetrics(WifiInfo wifiInfo, long now, int netId, int s1, int s2, int score) {
        double txSuccessRate;
        WifiInfo wifiInfo2 = wifiInfo;
        long j = now;
        if (j >= FIRST_REASONABLE_WALL_CLOCK) {
            double rssi = (double) wifiInfo.getRssi();
            double filteredRssi = this.mVelocityBasedConnectedScore.getFilteredRssi();
            double rssiThreshold = this.mVelocityBasedConnectedScore.getAdjustedRssiThreshold();
            int freq = wifiInfo.getFrequency();
            int linkSpeed = wifiInfo.getLinkSpeed();
            double txSuccessRate2 = wifiInfo2.txSuccessRate;
            double txRetriesRate = wifiInfo2.txRetriesRate;
            double txBadRate = wifiInfo2.txBadRate;
            double rxSuccessRate = wifiInfo2.rxSuccessRate;
            try {
                String timestamp = new SimpleDateFormat("MM-dd HH:mm:ss.SSS").format(new Date(j));
                Locale locale = Locale.US;
                Object[] objArr = new Object[17];
                objArr[0] = timestamp;
                String str = timestamp;
                objArr[1] = Integer.valueOf(this.mSessionNumber);
                objArr[2] = Integer.valueOf(netId);
                objArr[3] = Double.valueOf(rssi);
                objArr[4] = Double.valueOf(filteredRssi);
                objArr[5] = Double.valueOf(rssiThreshold);
                objArr[6] = Integer.valueOf(freq);
                objArr[7] = Integer.valueOf(linkSpeed);
                double d = rssi;
                double txSuccessRate3 = txSuccessRate2;
                try {
                    objArr[8] = Double.valueOf(txSuccessRate3);
                    double d2 = txSuccessRate3;
                    txSuccessRate = txRetriesRate;
                } catch (Exception e) {
                    e = e;
                    double d3 = txSuccessRate3;
                    double d4 = txRetriesRate;
                    double txSuccessRate4 = txBadRate;
                    Log.e("WifiScoreReport", "format problem", e);
                }
                try {
                    objArr[9] = Double.valueOf(txSuccessRate);
                    double d5 = txSuccessRate;
                    try {
                        objArr[10] = Double.valueOf(txBadRate);
                        objArr[11] = Double.valueOf(rxSuccessRate);
                        objArr[12] = Integer.valueOf(this.mNudYes);
                        objArr[13] = Integer.valueOf(this.mNudCount);
                        objArr[14] = Integer.valueOf(s1);
                        objArr[15] = Integer.valueOf(s2);
                        objArr[16] = Integer.valueOf(score);
                        String s = String.format(locale, "%s,%d,%d,%.1f,%.1f,%.1f,%d,%d,%.2f,%.2f,%.2f,%.2f,%d,%d,%d,%d,%d", objArr);
                        synchronized (this.mLinkMetricsHistory) {
                            this.mLinkMetricsHistory.add(s);
                            while (this.mLinkMetricsHistory.size() > DUMPSYS_ENTRY_COUNT_LIMIT) {
                                this.mLinkMetricsHistory.removeFirst();
                            }
                        }
                    } catch (Exception e2) {
                        e = e2;
                        Log.e("WifiScoreReport", "format problem", e);
                    }
                } catch (Exception e3) {
                    e = e3;
                    double d6 = txSuccessRate;
                    double txRetriesRate2 = txBadRate;
                    Log.e("WifiScoreReport", "format problem", e);
                }
            } catch (Exception e4) {
                e = e4;
                double d7 = rssi;
                double d8 = txSuccessRate2;
                double d9 = txRetriesRate;
                double rssi2 = txBadRate;
                Log.e("WifiScoreReport", "format problem", e);
            }
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        LinkedList<String> history;
        synchronized (this.mLinkMetricsHistory) {
            history = new LinkedList<>(this.mLinkMetricsHistory);
        }
        pw.println("time,session,netid,rssi,filtered_rssi,rssi_threshold,freq,linkspeed,tx_good,tx_retry,tx_bad,rx_pps,nudrq,nuds,s1,s2,score");
        Iterator it = history.iterator();
        while (it.hasNext()) {
            pw.println((String) it.next());
        }
        history.clear();
    }
}
