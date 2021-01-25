package com.huawei.hwwifiproservice;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import java.util.Set;

public class HwDualBandQualityEngine {
    private static final ApConnectTimeScore[] AP_CONNECT_TIME_SCORE_TABLE = {new ApConnectTimeScore(100, 5), new ApConnectTimeScore(50, 3), new ApConnectTimeScore(10, 1), new ApConnectTimeScore(5, 0)};
    public static final int DUAL_BAND_AP_SCORE = 6;
    private static final LossRateScore[] LOSE_RATE_SCORE_TABLE = {new LossRateScore(0.05d, 15), new LossRateScore(0.1d, 10), new LossRateScore(0.2d, 5), new LossRateScore(0.4d, 0), new LossRateScore(0.6d, -5), new LossRateScore(0.8d, -10)};
    private static final RssiScore[] RSSI_SCORE_TABLE = {new RssiScore(-45, 25), new RssiScore(-55, 20), new RssiScore(-65, 15), new RssiScore(-75, 5), new RssiScore(-82, 0)};
    private static final RttScore[] RTT_SCORE_TABLE = {new RttScore(400, 15), new RttScore(800, 10), new RttScore(WifiproBqeUtils.BQE_GOOD_RTT, 5), new RttScore(WifiScanGenieDataBaseImpl.SCAN_GENIE_MAX_RECORD, 0), new RttScore(4000, -5), new RttScore(16000, -10)};
    private static final String TAG = "db_QoE";
    private static final int USING_BLUETOOTH_SCORE = 10;
    private BluetoothAdapter mBluetoothAdapter = null;
    private Handler mQosMonitorHandler;

    public HwDualBandQualityEngine(Context context, Handler msgHandler) {
        this.mQosMonitorHandler = msgHandler;
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public boolean isBluetoothConnected() {
        BluetoothAdapter bluetoothAdapter = this.mBluetoothAdapter;
        if (bluetoothAdapter == null) {
            Log.w(TAG, "BluetoothAdapter is null");
            return false;
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices == null || pairedDevices.size() <= 0) {
            return false;
        }
        Log.w(TAG, "Bluetooth is connected");
        return true;
    }

    public void resetSampleRtt() {
        Handler handler = this.mQosMonitorHandler;
        if (handler != null) {
            this.mQosMonitorHandler.sendMessage(handler.obtainMessage(8, 3, 0));
        }
    }

    public void querySampleRtt() {
        Handler handler = this.mQosMonitorHandler;
        if (handler != null) {
            this.mQosMonitorHandler.sendMessage(handler.obtainMessage(9, 3, 0));
        }
    }

    public int getScoreByBluetoothUsage() {
        if (isBluetoothConnected()) {
            return 10;
        }
        return 0;
    }

    private static class RssiScore {
        public final int apConnectScore;
        public final int mRssi;

        public RssiScore(int rssi, int score) {
            this.mRssi = rssi;
            this.apConnectScore = score;
        }
    }

    public int getScoreByRssi(int rssi) {
        int index = 0;
        int maxIndex = RSSI_SCORE_TABLE.length - 1;
        int score = 0;
        while (true) {
            if (index > maxIndex) {
                break;
            } else if (rssi <= RSSI_SCORE_TABLE[index].mRssi) {
                index++;
            } else if (index == 0) {
                score = RSSI_SCORE_TABLE[0].apConnectScore;
            } else {
                score = RSSI_SCORE_TABLE[index - 1].apConnectScore + (((RSSI_SCORE_TABLE[index - 1].mRssi - rssi) / (RSSI_SCORE_TABLE[index - 1].mRssi - RSSI_SCORE_TABLE[index].mRssi)) * (RSSI_SCORE_TABLE[index - 1].apConnectScore - RSSI_SCORE_TABLE[index].apConnectScore));
            }
        }
        Log.i(TAG, "getScoreByRssi: rssi=" + rssi + " score=" + score);
        return score;
    }

    private static class ApConnectTimeScore {
        public final int apConnectScore;
        public final int totalConnectTime;

        public ApConnectTimeScore(int totalTime, int score) {
            this.totalConnectTime = totalTime;
            this.apConnectScore = score;
        }
    }

    public int getScoreByConnectTime(int connectTime) {
        int index = 0;
        int maxIndex = AP_CONNECT_TIME_SCORE_TABLE.length - 1;
        int score = 0;
        while (true) {
            if (index > maxIndex) {
                break;
            } else if (connectTime <= AP_CONNECT_TIME_SCORE_TABLE[index].totalConnectTime) {
                index++;
            } else if (index == 0) {
                score = AP_CONNECT_TIME_SCORE_TABLE[0].apConnectScore;
            } else {
                score = AP_CONNECT_TIME_SCORE_TABLE[index - 1].apConnectScore + (((AP_CONNECT_TIME_SCORE_TABLE[index - 1].totalConnectTime - connectTime) / (AP_CONNECT_TIME_SCORE_TABLE[index - 1].totalConnectTime - AP_CONNECT_TIME_SCORE_TABLE[index].totalConnectTime)) * (AP_CONNECT_TIME_SCORE_TABLE[index - 1].apConnectScore - AP_CONNECT_TIME_SCORE_TABLE[index].apConnectScore));
            }
        }
        Log.i(TAG, "getScoreByConnectTime: connectTime=" + connectTime + " score=" + score);
        return score;
    }

    private static class RttScore {
        public final int RTT;
        public final int apConnectScore;

        public RttScore(int rtt, int score) {
            this.RTT = rtt;
            this.apConnectScore = score;
        }
    }

    public int getScoreByRtt(int rtt) {
        int index = 0;
        RttScore[] rttScoreArr = RTT_SCORE_TABLE;
        int maxIndex = rttScoreArr.length - 1;
        int score = rttScoreArr[maxIndex].apConnectScore;
        if (rtt == 0) {
            return 0;
        }
        while (true) {
            if (index > maxIndex) {
                break;
            } else if (rtt >= RTT_SCORE_TABLE[index].RTT) {
                index++;
            } else if (index == 0) {
                score = RTT_SCORE_TABLE[0].apConnectScore;
            } else {
                score = RTT_SCORE_TABLE[index - 1].apConnectScore + (((RTT_SCORE_TABLE[index - 1].RTT - rtt) / (RTT_SCORE_TABLE[index - 1].RTT - RTT_SCORE_TABLE[index].RTT)) * (RTT_SCORE_TABLE[index - 1].apConnectScore - RTT_SCORE_TABLE[index].apConnectScore));
            }
        }
        Log.i(TAG, "getScoreByRtt: rtt=" + rtt + " score=" + score);
        return score;
    }

    private static class LossRateScore {
        public final int apConnectScore;
        public final double loseRate;

        public LossRateScore(double tempLossRate, int score) {
            this.loseRate = tempLossRate;
            this.apConnectScore = score;
        }
    }

    public int getScoreByLossRate(double lossRate) {
        int index = 0;
        LossRateScore[] lossRateScoreArr = LOSE_RATE_SCORE_TABLE;
        int maxIndex = lossRateScoreArr.length - 1;
        int score = lossRateScoreArr[maxIndex].apConnectScore;
        while (true) {
            if (index > maxIndex) {
                break;
            } else if (lossRate >= LOSE_RATE_SCORE_TABLE[index].loseRate) {
                index++;
            } else if (index == 0) {
                score = LOSE_RATE_SCORE_TABLE[0].apConnectScore;
            } else {
                score = LOSE_RATE_SCORE_TABLE[index - 1].apConnectScore + ((int) (((LOSE_RATE_SCORE_TABLE[index - 1].loseRate - lossRate) / (LOSE_RATE_SCORE_TABLE[index - 1].loseRate - LOSE_RATE_SCORE_TABLE[index].loseRate)) * ((double) (LOSE_RATE_SCORE_TABLE[index - 1].apConnectScore - LOSE_RATE_SCORE_TABLE[index].apConnectScore))));
            }
        }
        Log.i(TAG, "getScoreByLossRate: lossRate=" + String.valueOf(lossRate) + " score=" + score);
        return score;
    }
}
