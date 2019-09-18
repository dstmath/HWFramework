package com.android.server.hidata.hinetwork;

import com.android.server.mtm.iaware.brjob.scheduler.AwareJobSchedulerService;
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
    public short bothProbs = 0;
    public Rtt[] cellAccRtt = new Rtt[10];
    public Rtt[] cellBonRtt = new Rtt[10];
    public short cellProbs = 0;
    public Rtt[] cellTotRtt = new Rtt[10];
    public TheraticGain[] gain = new TheraticGain[10];
    public short gameAccNo = 0;
    public short gameAccNum = 0;
    public short gameAccYes = 0;
    public UsageCounts[] mod = new UsageCounts[10];
    public short videoAccNo = 0;
    public short videoAccNum = 0;
    public short videoAccYes = 0;
    public Rtt[] wifiAccRtt = new Rtt[10];
    public Rtt[] wifiBonRtt = new Rtt[10];
    public short wifiProbs = 0;
    public Rtt[] wifiTotRtt = new Rtt[10];

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

    public HwHiNetworkParmStatistics() {
        for (int i = 0; i < 10; i++) {
            this.cellAccRtt[i] = new Rtt();
            this.cellAccRtt[i].level = (short) (i + 101);
            this.cellAccRtt[i].rtt = 0;
        }
        for (int i2 = 0; i2 < 10; i2++) {
            this.cellBonRtt[i2] = new Rtt();
            this.cellBonRtt[i2].level = (short) (i2 + 201);
            this.cellBonRtt[i2].rtt = 0;
        }
        for (int i3 = 0; i3 < 10; i3++) {
            this.cellTotRtt[i3] = new Rtt();
            this.cellTotRtt[i3].level = (short) (i3 + 301);
            this.cellTotRtt[i3].rtt = 0;
        }
        for (int i4 = 0; i4 < 10; i4++) {
            this.wifiAccRtt[i4] = new Rtt();
            this.wifiAccRtt[i4].level = (short) (i4 + AwareJobSchedulerService.MSG_JOB_EXPIRED);
            this.wifiAccRtt[i4].rtt = 0;
        }
        for (int i5 = 0; i5 < 10; i5++) {
            this.wifiBonRtt[i5] = new Rtt();
            this.wifiBonRtt[i5].level = (short) (i5 + 501);
            this.wifiBonRtt[i5].rtt = 0;
        }
        for (int i6 = 0; i6 < 10; i6++) {
            this.wifiTotRtt[i6] = new Rtt();
            this.wifiTotRtt[i6].level = (short) (i6 + WifiProCommonUtils.RESP_CODE_UNSTABLE);
            this.wifiTotRtt[i6].rtt = 0;
        }
        for (int i7 = 0; i7 < 10; i7++) {
            this.gain[i7] = new TheraticGain();
            this.gain[i7].level = (short) (i7 + 1);
            this.gain[i7].gain = 0;
        }
        for (int i8 = 0; i8 < 10; i8++) {
            this.mod[i8] = new UsageCounts();
            this.mod[i8].number = (short) (i8 + 1);
            this.mod[i8].counts = 0;
        }
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
            this.cellAccRtt[i].level = (short) (i + 101);
            this.cellAccRtt[i].rtt = 0;
        }
        for (int i2 = 0; i2 < 10; i2++) {
            this.cellBonRtt[i2].level = (short) (i2 + 201);
            this.cellBonRtt[i2].rtt = 0;
        }
        for (int i3 = 0; i3 < 10; i3++) {
            this.cellTotRtt[i3].level = (short) (i3 + 301);
            this.cellTotRtt[i3].rtt = 0;
        }
        for (int i4 = 0; i4 < 10; i4++) {
            this.wifiAccRtt[i4].level = (short) (i4 + AwareJobSchedulerService.MSG_JOB_EXPIRED);
            this.wifiAccRtt[i4].rtt = 0;
        }
        for (int i5 = 0; i5 < 10; i5++) {
            this.wifiBonRtt[i5].level = (short) (i5 + 501);
            this.wifiBonRtt[i5].rtt = 0;
        }
        for (int i6 = 0; i6 < 10; i6++) {
            this.wifiTotRtt[i6].level = (short) (i6 + WifiProCommonUtils.RESP_CODE_UNSTABLE);
            this.wifiTotRtt[i6].rtt = 0;
        }
        for (int i7 = 0; i7 < 10; i7++) {
            this.gain[i7].level = (short) (i7 + 1);
            this.gain[i7].gain = 0;
        }
        for (int i8 = 0; i8 < 10; i8++) {
            this.mod[i8].number = (short) (i8 + 1);
            this.mod[i8].counts = 0;
        }
    }
}
