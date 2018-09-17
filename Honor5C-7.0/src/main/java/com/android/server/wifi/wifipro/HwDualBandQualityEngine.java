package com.android.server.wifi.wifipro;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import java.util.Set;

public class HwDualBandQualityEngine {
    private static final ApConnectTimeScore[] AP_CONNECT_TIME_SCORE_TABLE = null;
    public static final int DUAL_BAND_AP_SCORE = 6;
    private static final LossRateScore[] LOSE_RATE_SCORE_TABLE = null;
    private static final RssiScore[] RSSI_SCORE_TABLE = null;
    private static final RttScore[] RTT_SCORE_TABLE = null;
    private static final String TAG = "db_QoE";
    private static final int USING_BLUETOOTH_SCORE = 10;
    private BluetoothAdapter mBluetoothAdapter;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.wifipro.HwDualBandQualityEngine.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.wifipro.HwDualBandQualityEngine.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.wifipro.HwDualBandQualityEngine.<clinit>():void");
    }

    public HwDualBandQualityEngine(Context context, Handler msgHandler) {
        this.mBluetoothAdapter = null;
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
            return USING_BLUETOOTH_SCORE;
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
