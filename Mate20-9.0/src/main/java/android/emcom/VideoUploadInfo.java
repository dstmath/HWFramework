package android.emcom;

import android.emcom.SmartcareInfos;
import android.util.Log;

public class VideoUploadInfo extends SmartcareInfos.SmartcareBaseInfo {
    private static final String TAG = "VideoUploadInfo";
    public String appName;
    public String cellId;
    public int fullDelay;
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
    public boolean result;
    public int srDelay;
    public int streamDur;
    public MPStatInfo tcpPathStatInfo = new MPStatInfo();
    public PolicyStatInfo tcpPolicyStatInfo = new PolicyStatInfo();
    public String techCode;
    public short times;
    public int totalLen;
    public TrafficInfo trafficInfo = new TrafficInfo();
    public short type;
    public byte uVMos;
    public int videoDataRate;
    public int videoEndTime;
    public int videoStartDate;
    public int videoStartTime;
    public byte videoTerminateFlag;
    public String wlanBssid;
    public short wlanL1Dur;
    public short wlanL2Dur;
    public short wlanL3Dur;
    public short wlanL4Dur;
    public byte wlanSs;
    public String wlanSsid;

    public VideoUploadInfo() {
        init();
    }

    private void init() {
        this.appName = "";
        this.type = 0;
        this.srDelay = -1;
        this.fullDelay = -1;
        this.times = -1;
        this.totalLen = -1;
        this.streamDur = -1;
        this.videoDataRate = -1;
        this.videoTerminateFlag = 1;
        this.uVMos = 0;
        this.hostName = "";
        this.videoStartDate = 0;
        this.videoStartTime = 0;
        this.videoEndTime = 0;
        this.result = true;
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
        this.techCode = "";
        if (this.tcpPolicyStatInfo == null || this.tcpPathStatInfo == null || this.trafficInfo == null) {
            Log.e(TAG, "copyFrom: Video this.tcpPolicyStatInfo, this.tcpPathStatInfo or this.trafficInfo is null");
            return;
        }
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
        this.tcpPathStatInfo.mpDur = 0;
        this.trafficInfo.mpWifiTraffic = 0;
        this.trafficInfo.mpRadioTraffic = 0;
    }

    public final void copyFrom(VideoUploadInfo o) {
        this.appName = o.appName;
        this.type = o.type;
        this.techCode = o.techCode;
        this.srDelay = o.srDelay;
        this.fullDelay = o.fullDelay;
        this.times = o.times;
        this.totalLen = o.totalLen;
        this.streamDur = o.streamDur;
        this.videoDataRate = o.videoDataRate;
        this.videoTerminateFlag = o.videoTerminateFlag;
        this.uVMos = o.uVMos;
        this.hostName = o.hostName;
        this.videoStartDate = o.videoStartDate;
        this.videoStartTime = o.videoStartTime;
        this.videoEndTime = o.videoEndTime;
        this.result = o.result;
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
        if (this.tcpPolicyStatInfo == null || this.tcpPathStatInfo == null || this.trafficInfo == null) {
            Log.e(TAG, "copyFrom: Video this.tcpPolicyStatInfo, this.tcpPathStatInfo or this.trafficInfo is null");
        } else if (o.tcpPolicyStatInfo == null || o.tcpPathStatInfo == null || o.trafficInfo == null) {
            Log.e(TAG, "copyFrom: Video o.tcpPolicyStatInfo, o.tcpPathStatInfo or o.trafficInfo is null");
        } else {
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
            this.tcpPathStatInfo.mpDur = o.tcpPathStatInfo.mpDur;
            this.trafficInfo.mpWifiTraffic = o.trafficInfo.mpWifiTraffic;
            this.trafficInfo.mpRadioTraffic = o.trafficInfo.mpRadioTraffic;
        }
    }

    public void addToInfos(SmartcareInfos cis) {
        super.addToInfos(cis);
        cis.videoUploadInfo = this;
    }

    public String toString() {
        return "hash: " + hashCode() + ",app: " + this.appName + ",srDelay: " + this.srDelay + ",fullDelay: " + this.fullDelay + ",times: " + this.times + ",totalLen: " + this.totalLen + ",streamDur: " + this.streamDur + ",videoDataRate: " + this.videoDataRate + ",videoTerminateFlag: " + this.videoTerminateFlag + ",uVMos: " + this.uVMos + ",videoStartDate: " + this.videoStartDate + ",videoStartTime: " + this.videoStartTime + ",videoEndTime: " + this.videoEndTime + ",result: " + this.result + ",networkType: " + this.networkType + ",radioSs: " + this.radioSs + ",radioSnr: " + this.radioSnr + ",wlanSs: " + this.wlanSs + ",flowType: " + this.tcpPolicyStatInfo.flowType + ",policyType: " + this.tcpPolicyStatInfo.policyType + ",linkMode: " + this.tcpPolicyStatInfo.linkMode + ",initPrbRoute: " + this.tcpPathStatInfo.initPrbRoute + ",wifiTraffic: " + this.trafficInfo.wifiTraffic + ",radioTraffic: " + this.trafficInfo.radioTraffic + ",mpDur:" + this.tcpPathStatInfo.mpDur + ",mpWifiTraffic:" + this.trafficInfo.mpWifiTraffic + ",mpRadioTraffic:" + this.trafficInfo.mpRadioTraffic + ",type:" + this.type;
    }

    public void recycle() {
        super.recycle();
        init();
    }
}
