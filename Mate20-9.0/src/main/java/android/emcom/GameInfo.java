package android.emcom;

import android.emcom.SmartcareInfos;

public class GameInfo extends SmartcareInfos.SmartcareBaseInfo {
    private static final String TAG = "GameInfo";
    public String appName;
    public int battleDur;
    public short battleRttAvg;
    public short battleRttL1Dur;
    public short battleRttL2Dur;
    public short battleRttL3Dur;
    public short battleRttL4Dur;
    public short battleRttL5Dur;
    public short battleRttL6Dur;
    public short battleRttL7Dur;
    public short battleRttL8Dur;
    public String cellId;
    public byte connDropFlg;
    public int gameEndTime;
    public int gameLoadLat;
    public int gameLoadThrput;
    public int gameStartDate;
    public int gameStartTime;
    public String hostName;
    public short mcc;
    public short mnc;
    public byte networkType;
    public short radioL1Dur;
    public short radioL2Dur;
    public short radioL3Dur;
    public short radioL4Dur;
    public short radioL5Dur;
    public int radioSnr;
    public byte radioSq;
    public byte radioSs;
    public int selfRoleLoadLat;
    public byte selfRoleLoadSuccFlg;
    public MPStatInfo tcpPathStatInfo = new MPStatInfo();
    public PolicyStatInfo tcpPolicyStatInfo = new PolicyStatInfo();
    public String techCode;
    public TrafficInfo trafficInfo = new TrafficInfo();
    public byte type;
    public MPStatInfo udpPathStatInfo = new MPStatInfo();
    public PolicyStatInfo udpPolicyStatInfo = new PolicyStatInfo();
    public String wlanBssid;
    public short wlanL1Dur;
    public short wlanL2Dur;
    public short wlanL3Dur;
    public short wlanL4Dur;
    public byte wlanSs;
    public String wlanSsid;

    public GameInfo() {
        init();
    }

    public void addToInfos(SmartcareInfos cis) {
        super.addToInfos(cis);
        cis.gameInfo = this;
    }

    public final void copyFrom(GameInfo o) {
        this.hostName = o.hostName;
        this.gameStartDate = o.gameStartDate;
        this.gameStartTime = o.gameStartTime;
        this.gameEndTime = o.gameEndTime;
        this.mcc = o.mcc;
        this.mnc = o.mnc;
        this.networkType = o.networkType;
        this.cellId = o.cellId;
        this.radioSs = o.radioSs;
        this.radioSq = o.radioSq;
        this.radioSnr = o.radioSnr;
        this.radioL1Dur = o.radioL1Dur;
        this.radioL2Dur = o.radioL2Dur;
        this.radioL3Dur = o.radioL3Dur;
        this.radioL4Dur = o.radioL4Dur;
        this.radioL5Dur = o.radioL5Dur;
        this.wlanSs = o.wlanSs;
        this.wlanBssid = o.wlanBssid;
        this.wlanSsid = o.wlanSsid;
        this.wlanL1Dur = o.wlanL1Dur;
        this.wlanL2Dur = o.wlanL2Dur;
        this.wlanL3Dur = o.wlanL3Dur;
        this.wlanL4Dur = o.wlanL4Dur;
        this.appName = o.appName;
        this.type = o.type;
        this.gameLoadLat = o.gameLoadLat;
        this.gameLoadThrput = o.gameLoadThrput;
        this.connDropFlg = o.connDropFlg;
        this.selfRoleLoadLat = o.selfRoleLoadLat;
        this.selfRoleLoadSuccFlg = o.selfRoleLoadSuccFlg;
        this.battleDur = o.battleDur;
        this.battleRttAvg = o.battleRttAvg;
        this.battleRttL1Dur = o.battleRttL1Dur;
        this.battleRttL2Dur = o.battleRttL2Dur;
        this.battleRttL3Dur = o.battleRttL3Dur;
        this.battleRttL4Dur = o.battleRttL4Dur;
        this.battleRttL5Dur = o.battleRttL5Dur;
        this.battleRttL6Dur = o.battleRttL6Dur;
        this.battleRttL7Dur = o.battleRttL7Dur;
        this.battleRttL8Dur = o.battleRttL8Dur;
        this.techCode = o.techCode;
        if (this.udpPolicyStatInfo != null && this.tcpPolicyStatInfo != null && this.udpPathStatInfo != null && this.tcpPathStatInfo != null && this.trafficInfo != null && o.udpPolicyStatInfo != null && o.tcpPolicyStatInfo != null && o.udpPathStatInfo != null && o.tcpPathStatInfo != null && o.trafficInfo != null) {
            this.udpPolicyStatInfo.policyStartCond = o.udpPolicyStatInfo.policyStartCond;
            this.udpPolicyStatInfo.flowType = o.udpPolicyStatInfo.flowType;
            this.udpPolicyStatInfo.policyType = o.udpPolicyStatInfo.policyType;
            this.udpPolicyStatInfo.linkMode = o.udpPolicyStatInfo.linkMode;
            this.udpPolicyStatInfo.succFlg = o.udpPolicyStatInfo.succFlg;
            this.udpPolicyStatInfo.policyEnSuccCnt = o.udpPolicyStatInfo.policyEnSuccCnt;
            this.udpPolicyStatInfo.policyEnCnt = o.udpPolicyStatInfo.policyEnCnt;
            this.udpPolicyStatInfo.policyEnRspLat = o.udpPolicyStatInfo.policyEnRspLat;
            this.udpPolicyStatInfo.policySwSuccCnt = o.udpPolicyStatInfo.policySwSuccCnt;
            this.udpPolicyStatInfo.policySwCnt = o.udpPolicyStatInfo.policySwCnt;
            this.udpPolicyStatInfo.policySwRspLat = o.udpPolicyStatInfo.policySwRspLat;
            this.udpPathStatInfo.initPrbRoute = o.udpPathStatInfo.initPrbRoute;
            this.udpPathStatInfo.sWCnt = o.udpPathStatInfo.sWCnt;
            this.udpPathStatInfo.sWB2GCnt = o.udpPathStatInfo.sWB2GCnt;
            this.udpPathStatInfo.sWB2BCnt = o.udpPathStatInfo.sWB2BCnt;
            this.udpPathStatInfo.sWG2GCnt = o.udpPathStatInfo.sWG2GCnt;
            this.udpPathStatInfo.sWG2BCnt = o.udpPathStatInfo.sWG2BCnt;
            this.tcpPolicyStatInfo.policyStartCond = o.tcpPolicyStatInfo.policyStartCond;
            this.tcpPolicyStatInfo.flowType = o.tcpPolicyStatInfo.flowType;
            this.tcpPolicyStatInfo.policyType = o.tcpPolicyStatInfo.policyType;
            this.tcpPolicyStatInfo.linkMode = o.tcpPolicyStatInfo.linkMode;
            this.tcpPolicyStatInfo.succFlg = o.tcpPolicyStatInfo.succFlg;
            this.tcpPolicyStatInfo.policyEnSuccCnt = o.tcpPolicyStatInfo.policyEnSuccCnt;
            this.tcpPolicyStatInfo.policyEnCnt = o.tcpPolicyStatInfo.policyEnCnt;
            this.tcpPolicyStatInfo.policyEnRspLat = o.tcpPolicyStatInfo.policyEnRspLat;
            this.tcpPolicyStatInfo.policySwSuccCnt = o.tcpPolicyStatInfo.policySwSuccCnt;
            this.tcpPolicyStatInfo.policySwCnt = o.tcpPolicyStatInfo.policySwCnt;
            this.tcpPolicyStatInfo.policySwRspLat = o.tcpPolicyStatInfo.policySwRspLat;
            this.tcpPathStatInfo.initPrbRoute = o.tcpPathStatInfo.initPrbRoute;
            this.tcpPathStatInfo.sWCnt = o.tcpPathStatInfo.sWCnt;
            this.tcpPathStatInfo.sWB2GCnt = o.tcpPathStatInfo.sWB2GCnt;
            this.tcpPathStatInfo.sWB2BCnt = o.tcpPathStatInfo.sWB2BCnt;
            this.tcpPathStatInfo.sWG2GCnt = o.tcpPathStatInfo.sWG2GCnt;
            this.tcpPathStatInfo.sWG2BCnt = o.tcpPathStatInfo.sWG2BCnt;
            this.trafficInfo.wifiTraffic = o.trafficInfo.wifiTraffic;
            this.trafficInfo.radioTraffic = o.trafficInfo.radioTraffic;
        }
    }

    private void init() {
        this.hostName = "";
        this.gameStartDate = 0;
        this.gameStartTime = 0;
        this.gameEndTime = 0;
        this.mcc = 0;
        this.mnc = 0;
        this.networkType = 0;
        this.cellId = "";
        this.radioSs = -1;
        this.radioSq = -1;
        this.radioSnr = -1;
        this.radioL1Dur = 0;
        this.radioL2Dur = 0;
        this.radioL3Dur = 0;
        this.radioL4Dur = 0;
        this.radioL5Dur = 0;
        this.wlanSs = -1;
        this.wlanBssid = "";
        this.wlanSsid = "";
        this.wlanL1Dur = 0;
        this.wlanL2Dur = 0;
        this.wlanL3Dur = 0;
        this.wlanL4Dur = 0;
        this.appName = "";
        this.type = 0;
        this.gameLoadLat = 0;
        this.gameLoadThrput = 0;
        this.connDropFlg = 0;
        this.selfRoleLoadLat = 0;
        this.selfRoleLoadSuccFlg = -1;
        this.battleDur = 0;
        this.battleRttAvg = 0;
        this.battleRttL1Dur = 0;
        this.battleRttL2Dur = 0;
        this.battleRttL3Dur = 0;
        this.battleRttL4Dur = 0;
        this.battleRttL5Dur = 0;
        this.battleRttL6Dur = 0;
        this.battleRttL7Dur = 0;
        this.battleRttL8Dur = 0;
        this.techCode = "";
        if (this.udpPolicyStatInfo != null && this.tcpPolicyStatInfo != null && this.udpPathStatInfo != null && this.tcpPathStatInfo != null && this.trafficInfo != null) {
            this.udpPolicyStatInfo.policyStartCond = 0;
            this.udpPolicyStatInfo.flowType = 0;
            this.udpPolicyStatInfo.policyType = -1;
            this.udpPolicyStatInfo.linkMode = -1;
            this.udpPolicyStatInfo.succFlg = -1;
            this.udpPolicyStatInfo.policyEnSuccCnt = 0;
            this.udpPolicyStatInfo.policyEnCnt = 0;
            this.udpPolicyStatInfo.policyEnRspLat = 0;
            this.udpPolicyStatInfo.policySwSuccCnt = 0;
            this.udpPolicyStatInfo.policySwCnt = 0;
            this.udpPolicyStatInfo.policySwRspLat = 0;
            this.udpPathStatInfo.initPrbRoute = 0;
            this.udpPathStatInfo.sWCnt = 0;
            this.udpPathStatInfo.sWB2GCnt = 0;
            this.udpPathStatInfo.sWB2BCnt = 0;
            this.udpPathStatInfo.sWG2GCnt = 0;
            this.udpPathStatInfo.sWG2BCnt = 0;
            this.tcpPolicyStatInfo.policyStartCond = 0;
            this.tcpPolicyStatInfo.flowType = 0;
            this.tcpPolicyStatInfo.policyType = -1;
            this.tcpPolicyStatInfo.linkMode = -1;
            this.tcpPolicyStatInfo.succFlg = 0;
            this.tcpPolicyStatInfo.policyEnSuccCnt = 0;
            this.tcpPolicyStatInfo.policyEnCnt = 0;
            this.tcpPolicyStatInfo.policyEnRspLat = 0;
            this.tcpPolicyStatInfo.policySwSuccCnt = 0;
            this.tcpPolicyStatInfo.policySwCnt = 0;
            this.tcpPolicyStatInfo.policySwRspLat = 0;
            this.tcpPathStatInfo.initPrbRoute = -1;
            this.tcpPathStatInfo.sWCnt = 0;
            this.tcpPathStatInfo.sWB2GCnt = 0;
            this.tcpPathStatInfo.sWB2BCnt = 0;
            this.tcpPathStatInfo.sWG2GCnt = 0;
            this.tcpPathStatInfo.sWG2BCnt = 0;
            this.trafficInfo.wifiTraffic = 0;
            this.trafficInfo.radioTraffic = 0;
        }
    }

    public void recycle() {
        super.recycle();
        init();
    }

    public String toString() {
        return "hash: " + hashCode() + ",StartDate: " + this.gameStartDate + ",StartTime: " + this.gameStartTime + ",EndTime: " + this.gameEndTime + ",app: " + this.appName + ",type: " + this.type + ",gameLoadLat: " + this.gameLoadLat + ",connDropFlg: " + this.connDropFlg + ",selfRoleLoadLat: " + this.selfRoleLoadLat + ",battleDur: " + this.battleDur + ",battleRttAvg: " + this.battleRttAvg + ",networkType: " + this.networkType + ",radioSs: " + this.radioSs + ",radioSnr: " + this.radioSnr + ",wlanSs: " + this.wlanSs + ",techCode: " + this.techCode + ",flowType: " + this.udpPolicyStatInfo.flowType + ",policyType: " + this.udpPolicyStatInfo.policyType + ",linkMode: " + this.udpPolicyStatInfo.linkMode + ",initPrbRoute: " + this.udpPathStatInfo.initPrbRoute + ",flowType2: " + this.tcpPolicyStatInfo.flowType + ",policyType2: " + this.tcpPolicyStatInfo.policyType + ",linkMode2: " + this.tcpPolicyStatInfo.linkMode + ",initPrbRoute2: " + this.tcpPathStatInfo.initPrbRoute;
    }
}
