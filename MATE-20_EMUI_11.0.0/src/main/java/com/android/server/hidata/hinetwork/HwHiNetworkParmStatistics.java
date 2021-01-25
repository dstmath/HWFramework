package com.android.server.hidata.hinetwork;

import com.android.server.wifipro.WifiProCommonUtils;

public class HwHiNetworkParmStatistics {
    public static final String BOTHPROBS = "bothProbs";
    public static final String CELLACCRTT = "cellAccRtt";
    public static final String CELLBONRTT = "cellBonRtt";
    public static final String CELLPROBS = "cellProbs";
    public static final String CELLTOTRTT = "cellTotRtt";
    public static final int CHR_USER_CHOOSE_NO = -1;
    public static final int CHR_USER_CHOOSE_YES = 0;
    public static final String COUNTS = "counts";
    public static final String GAIN = "gain";
    public static final String GAIN_SUB = "Gain";
    public static final String GAMEACCNO = "gameAccNo";
    public static final String GAMEACCNUM = "gameAccNum";
    public static final String GAMEACCYES = "gameAccYes";
    public static final String LEVEL = "Level";
    public static final String MOD = "mod";
    public static final String NUMBER = "number";
    public static final String RTT = "Rtt";
    public static final String VIDEOACCNO = "videoAccNo";
    public static final String VIDEOACCNUM = "videoAccNum";
    public static final String VIDEOACCYES = "videoAccYes";
    public static final String WIFIACCRTT = "wfiAccRtt";
    public static final String WIFIBONRTT = "wfiBonRtt";
    public static final String WIFIPROBS = "wifiProbs";
    public static final String WIFITOTRTT = "wfiTotRtt";
    public short bothProbs;
    public Rtt[] cellAccRtt;
    public Rtt[] cellBonRtt;
    public short cellProbs;
    public Rtt[] cellTotRtt;
    public TheraticGain[] gain;
    public short gameAccNo;
    public short gameAccNum;
    public short gameAccYes;
    public UsageCounts[] mod;
    public short videoAccNo;
    public short videoAccNum;
    public short videoAccYes;
    public Rtt[] wifiAccRtt;
    public Rtt[] wifiBonRtt;
    public short wifiProbs;
    public Rtt[] wifiTotRtt;

    public HwHiNetworkParmStatistics() {
        init();
    }

    public void reset() {
        this.gameAccNum = 0;
        this.gameAccYes = 0;
        this.gameAccNo = 0;
        this.videoAccNum = 0;
        this.videoAccYes = 0;
        this.videoAccNo = 0;
        this.cellProbs = 0;
        this.wifiProbs = 0;
        this.bothProbs = 0;
        for (int i = 0; i < 10; i++) {
            Rtt[] rttArr = this.cellAccRtt;
            rttArr[i].level = (short) (i + 101);
            rttArr[i].rtt = 0;
        }
        for (int i2 = 0; i2 < 10; i2++) {
            Rtt[] rttArr2 = this.cellBonRtt;
            rttArr2[i2].level = (short) (i2 + 201);
            rttArr2[i2].rtt = 0;
        }
        for (int i3 = 0; i3 < 10; i3++) {
            Rtt[] rttArr3 = this.cellTotRtt;
            rttArr3[i3].level = (short) (i3 + 301);
            rttArr3[i3].rtt = 0;
        }
        for (int i4 = 0; i4 < 10; i4++) {
            Rtt[] rttArr4 = this.wifiAccRtt;
            rttArr4[i4].level = (short) (i4 + 401);
            rttArr4[i4].rtt = 0;
        }
        for (int i5 = 0; i5 < 10; i5++) {
            Rtt[] rttArr5 = this.wifiBonRtt;
            rttArr5[i5].level = (short) (i5 + 501);
            rttArr5[i5].rtt = 0;
        }
        for (int i6 = 0; i6 < 10; i6++) {
            Rtt[] rttArr6 = this.wifiTotRtt;
            rttArr6[i6].level = (short) (i6 + WifiProCommonUtils.RESP_CODE_UNSTABLE);
            rttArr6[i6].rtt = 0;
        }
        for (int i7 = 0; i7 < 10; i7++) {
            TheraticGain[] theraticGainArr = this.gain;
            theraticGainArr[i7].level = (short) (i7 + 1);
            theraticGainArr[i7].gain = 0;
        }
        for (int i8 = 0; i8 < 10; i8++) {
            UsageCounts[] usageCountsArr = this.mod;
            usageCountsArr[i8].number = (short) (i8 + 1);
            usageCountsArr[i8].counts = 0;
        }
    }

    private void init() {
        this.gameAccNum = 0;
        this.gameAccYes = 0;
        this.gameAccNo = 0;
        this.videoAccNum = 0;
        this.videoAccYes = 0;
        this.videoAccNo = 0;
        this.cellProbs = 0;
        this.wifiProbs = 0;
        this.bothProbs = 0;
        this.cellAccRtt = new Rtt[10];
        this.cellBonRtt = new Rtt[10];
        this.cellTotRtt = new Rtt[10];
        this.wifiAccRtt = new Rtt[10];
        this.wifiBonRtt = new Rtt[10];
        this.wifiTotRtt = new Rtt[10];
        this.gain = new TheraticGain[10];
        this.mod = new UsageCounts[10];
        for (int i = 0; i < 10; i++) {
            this.cellAccRtt[i] = new Rtt();
            Rtt[] rttArr = this.cellAccRtt;
            rttArr[i].level = (short) (i + 101);
            rttArr[i].rtt = 0;
        }
        for (int i2 = 0; i2 < 10; i2++) {
            this.cellBonRtt[i2] = new Rtt();
            Rtt[] rttArr2 = this.cellBonRtt;
            rttArr2[i2].level = (short) (i2 + 201);
            rttArr2[i2].rtt = 0;
        }
        for (int i3 = 0; i3 < 10; i3++) {
            this.cellTotRtt[i3] = new Rtt();
            Rtt[] rttArr3 = this.cellTotRtt;
            rttArr3[i3].level = (short) (i3 + 301);
            rttArr3[i3].rtt = 0;
        }
        for (int i4 = 0; i4 < 10; i4++) {
            this.wifiAccRtt[i4] = new Rtt();
            Rtt[] rttArr4 = this.wifiAccRtt;
            rttArr4[i4].level = (short) (i4 + 401);
            rttArr4[i4].rtt = 0;
        }
        for (int i5 = 0; i5 < 10; i5++) {
            this.wifiBonRtt[i5] = new Rtt();
            Rtt[] rttArr5 = this.wifiBonRtt;
            rttArr5[i5].level = (short) (i5 + 501);
            rttArr5[i5].rtt = 0;
        }
        for (int i6 = 0; i6 < 10; i6++) {
            this.wifiTotRtt[i6] = new Rtt();
            Rtt[] rttArr6 = this.wifiTotRtt;
            rttArr6[i6].level = (short) (i6 + WifiProCommonUtils.RESP_CODE_UNSTABLE);
            rttArr6[i6].rtt = 0;
        }
        for (int i7 = 0; i7 < 10; i7++) {
            this.gain[i7] = new TheraticGain();
            TheraticGain[] theraticGainArr = this.gain;
            theraticGainArr[i7].level = (short) (i7 + 1);
            theraticGainArr[i7].gain = 0;
        }
        for (int i8 = 0; i8 < 10; i8++) {
            this.mod[i8] = new UsageCounts();
            UsageCounts[] usageCountsArr = this.mod;
            usageCountsArr[i8].number = (short) (i8 + 1);
            usageCountsArr[i8].counts = 0;
        }
    }

    public class Rtt {
        public short level = -1;
        public short rtt = 0;

        public Rtt() {
        }
    }

    public class TheraticGain {
        public short gain = 0;
        public short level = -1;

        public TheraticGain() {
        }
    }

    public class UsageCounts {
        public short counts = 0;
        public short number = -1;

        public UsageCounts() {
        }
    }
}
