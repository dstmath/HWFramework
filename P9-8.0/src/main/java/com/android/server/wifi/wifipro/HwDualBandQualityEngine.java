package com.android.server.wifi.wifipro;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import com.android.server.wifi.HwQoE.HwQoEUtils;
import java.util.Set;

public class HwDualBandQualityEngine {
    private static final ApConnectTimeScore[] AP_CONNECT_TIME_SCORE_TABLE = new ApConnectTimeScore[]{new ApConnectTimeScore(100, 5), new ApConnectTimeScore(50, 3), new ApConnectTimeScore(10, 1), new ApConnectTimeScore(5, 0)};
    public static final int DUAL_BAND_AP_SCORE = 6;
    private static final LossRateScore[] LOSE_RATE_SCORE_TABLE = new LossRateScore[]{new LossRateScore(0.05d, 15), new LossRateScore(0.1d, 10), new LossRateScore(0.2d, 5), new LossRateScore(0.4d, 0), new LossRateScore(0.6d, -5), new LossRateScore(0.8d, -10)};
    private static final RssiScore[] RSSI_SCORE_TABLE = new RssiScore[]{new RssiScore(-45, 25), new RssiScore(-55, 20), new RssiScore(-65, 15), new RssiScore(-75, 5), new RssiScore(-82, 0)};
    private static final RttScore[] RTT_SCORE_TABLE = new RttScore[]{new RttScore(HwCHRWifiSpeedBaseChecker.RTT_THRESHOLD_400, 15), new RttScore(800, 10), new RttScore(WifiproBqeUtils.BQE_GOOD_RTT, 5), new RttScore(2000, 0), new RttScore(HwQoEUtils.WIFI_CHECK_TIMEOUT, -5), new RttScore(16000, -10)};
    private static final String TAG = "db_QoE";
    private static final int USING_BLUETOOTH_SCORE = 10;
    private BluetoothAdapter mBluetoothAdapter = null;
    private Handler mQosMonitorHandler;

    private static class ApConnectTimeScore {
        public final int SCORE;
        public final int TOTAL_CONNECT_TIME;

        public ApConnectTimeScore(int totalTime, int score) {
            this.TOTAL_CONNECT_TIME = totalTime;
            this.SCORE = score;
        }
    }

    private static class LossRateScore {
        public final double LOSE_RATE;
        public final int SCORE;

        public LossRateScore(double lossRate, int score) {
            this.LOSE_RATE = lossRate;
            this.SCORE = score;
        }
    }

    private static class RssiScore {
        public final int RSSI;
        public final int SCORE;

        public RssiScore(int rssi, int score) {
            this.RSSI = rssi;
            this.SCORE = score;
        }
    }

    private static class RttScore {
        public final int RTT;
        public final int SCORE;

        public RttScore(int rtt, int score) {
            this.RTT = rtt;
            this.SCORE = score;
        }
    }

    public HwDualBandQualityEngine(Context context, Handler msgHandler) {
        this.mQosMonitorHandler = msgHandler;
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public boolean isBluetoothConnected() {
        if (this.mBluetoothAdapter == null) {
            Log.w(TAG, "BluetoothAdapter is null");
            return false;
        }
        Set<BluetoothDevice> pairedDevices = this.mBluetoothAdapter.getBondedDevices();
        if (pairedDevices == null || pairedDevices.size() <= 0) {
            return false;
        }
        Log.w(TAG, "Bluetooth is connected");
        return true;
    }

    public void resetSampleRtt() {
        if (this.mQosMonitorHandler != null) {
            this.mQosMonitorHandler.sendMessage(this.mQosMonitorHandler.obtainMessage(8, 3, 0));
        }
    }

    public void querySampleRtt() {
        if (this.mQosMonitorHandler != null) {
            this.mQosMonitorHandler.sendMessage(this.mQosMonitorHandler.obtainMessage(9, 3, 0));
        }
    }

    public int getScoreByBluetoothUsage() {
        if (isBluetoothConnected()) {
            return 10;
        }
        return 0;
    }

    public int getScoreByRssi(int rssi) {
        int maxIndex = RSSI_SCORE_TABLE.length - 1;
        int score = 0;
        for (int index = 0; index <= maxIndex; index++) {
            if (rssi > RSSI_SCORE_TABLE[index].RSSI) {
                if (index == 0) {
                    score = RSSI_SCORE_TABLE[0].SCORE;
                } else {
                    score = RSSI_SCORE_TABLE[index - 1].SCORE + (((RSSI_SCORE_TABLE[index - 1].RSSI - rssi) / (RSSI_SCORE_TABLE[index - 1].RSSI - RSSI_SCORE_TABLE[index].RSSI)) * (RSSI_SCORE_TABLE[index - 1].SCORE - RSSI_SCORE_TABLE[index].SCORE));
                }
                Log.d(TAG, "getScoreByRssi: rssi=" + rssi + " score=" + score);
                return score;
            }
        }
        Log.d(TAG, "getScoreByRssi: rssi=" + rssi + " score=" + score);
        return score;
    }

    public int getScoreByConnectTime(int connectTime) {
        int maxIndex = AP_CONNECT_TIME_SCORE_TABLE.length - 1;
        int score = 0;
        for (int index = 0; index <= maxIndex; index++) {
            if (connectTime > AP_CONNECT_TIME_SCORE_TABLE[index].TOTAL_CONNECT_TIME) {
                if (index == 0) {
                    score = AP_CONNECT_TIME_SCORE_TABLE[0].SCORE;
                } else {
                    score = AP_CONNECT_TIME_SCORE_TABLE[index - 1].SCORE + (((AP_CONNECT_TIME_SCORE_TABLE[index - 1].TOTAL_CONNECT_TIME - connectTime) / (AP_CONNECT_TIME_SCORE_TABLE[index - 1].TOTAL_CONNECT_TIME - AP_CONNECT_TIME_SCORE_TABLE[index].TOTAL_CONNECT_TIME)) * (AP_CONNECT_TIME_SCORE_TABLE[index - 1].SCORE - AP_CONNECT_TIME_SCORE_TABLE[index].SCORE));
                }
                Log.d(TAG, "getScoreByConnectTime: connectTime=" + connectTime + " score=" + score);
                return score;
            }
        }
        Log.d(TAG, "getScoreByConnectTime: connectTime=" + connectTime + " score=" + score);
        return score;
    }

    public int getScoreByRtt(int rtt) {
        int maxIndex = RTT_SCORE_TABLE.length - 1;
        int score = RTT_SCORE_TABLE[maxIndex].SCORE;
        if (rtt == 0) {
            return 0;
        }
        for (int index = 0; index <= maxIndex; index++) {
            if (rtt < RTT_SCORE_TABLE[index].RTT) {
                if (index == 0) {
                    score = RTT_SCORE_TABLE[0].SCORE;
                } else {
                    score = RTT_SCORE_TABLE[index - 1].SCORE + (((RTT_SCORE_TABLE[index - 1].RTT - rtt) / (RTT_SCORE_TABLE[index - 1].RTT - RTT_SCORE_TABLE[index].RTT)) * (RTT_SCORE_TABLE[index - 1].SCORE - RTT_SCORE_TABLE[index].SCORE));
                }
                Log.d(TAG, "getScoreByRtt: rtt=" + rtt + " score=" + score);
                return score;
            }
        }
        Log.d(TAG, "getScoreByRtt: rtt=" + rtt + " score=" + score);
        return score;
    }

    public int getScoreByLossRate(double lossRate) {
        int maxIndex = LOSE_RATE_SCORE_TABLE.length - 1;
        int score = LOSE_RATE_SCORE_TABLE[maxIndex].SCORE;
        for (int index = 0; index <= maxIndex; index++) {
            if (lossRate < LOSE_RATE_SCORE_TABLE[index].LOSE_RATE) {
                if (index == 0) {
                    score = LOSE_RATE_SCORE_TABLE[0].SCORE;
                } else {
                    score = LOSE_RATE_SCORE_TABLE[index - 1].SCORE + ((int) (((LOSE_RATE_SCORE_TABLE[index - 1].LOSE_RATE - lossRate) / (LOSE_RATE_SCORE_TABLE[index - 1].LOSE_RATE - LOSE_RATE_SCORE_TABLE[index].LOSE_RATE)) * ((double) (LOSE_RATE_SCORE_TABLE[index - 1].SCORE - LOSE_RATE_SCORE_TABLE[index].SCORE))));
                }
                Log.d(TAG, "getScoreByLossRate: lossRate=" + String.valueOf(lossRate) + " score=" + score);
                return score;
            }
        }
        Log.d(TAG, "getScoreByLossRate: lossRate=" + String.valueOf(lossRate) + " score=" + score);
        return score;
    }
}
