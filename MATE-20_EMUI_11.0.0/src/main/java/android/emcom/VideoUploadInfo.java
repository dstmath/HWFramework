package android.emcom;

import android.emcom.SmartcareInfos;
import android.util.Log;
import com.huawei.android.os.storage.StorageManagerExt;

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
        this.appName = StorageManagerExt.INVALID_KEY_DESC;
        this.type = 0;
        this.srDelay = -1;
        this.fullDelay = -1;
        this.times = -1;
        this.totalLen = -1;
        this.streamDur = -1;
        this.videoDataRate = -1;
        this.videoTerminateFlag = 1;
        this.uVMos = 0;
        this.hostName = StorageManagerExt.INVALID_KEY_DESC;
        this.videoStartDate = 0;
        this.videoStartTime = 0;
        this.videoEndTime = 0;
        this.result = true;
        this.mcc = 0;
        this.mnc = 0;
        this.networkType = 0;
        this.cellId = StorageManagerExt.INVALID_KEY_DESC;
        this.radioSs = -1;
        this.radioSq = -1;
        this.radioSnr = -1;
        this.radioL1Dur = 0;
        this.radioL2Dur = 0;
        this.radioL3Dur = 0;
        this.radioL4Dur = 0;
        this.radioL5Dur = 0;
        this.wlanSs = -1;
        this.wlanBssid = StorageManagerExt.INVALID_KEY_DESC;
        this.wlanSsid = StorageManagerExt.INVALID_KEY_DESC;
        this.wlanL1Dur = 0;
        this.wlanL2Dur = 0;
        this.wlanL3Dur = 0;
        this.wlanL4Dur = 0;
        this.techCode = StorageManagerExt.INVALID_KEY_DESC;
        initUdpTcpTrafficInfo();
    }

    private void initUdpTcpTrafficInfo() {
        PolicyStatInfo policyStatInfo = this.tcpPolicyStatInfo;
        if (policyStatInfo == null || this.tcpPathStatInfo == null || this.trafficInfo == null) {
            Log.e(TAG, "copyFrom: Video this.tcpPolicyStatInfo, this.tcpPathStatInfo or this.trafficInfo is null");
            return;
        }
        policyStatInfo.setPolicyStartCond(0);
        this.tcpPolicyStatInfo.setFlowType(0);
        this.tcpPolicyStatInfo.setPolicyType(-1);
        this.tcpPolicyStatInfo.setLinkMode(-1);
        this.tcpPolicyStatInfo.setSuccFlg(0);
        this.tcpPolicyStatInfo.setPolicyEnSuccCnt(0);
        this.tcpPolicyStatInfo.setPolicyEnCnt(0);
        this.tcpPolicyStatInfo.setPolicyEnRspLat(0);
        this.tcpPolicyStatInfo.setPolicySwSuccCnt(0);
        this.tcpPolicyStatInfo.setPolicySwCnt(0);
        this.tcpPolicyStatInfo.setPolicySwRspLat(0);
        this.tcpPathStatInfo.setInitPrbRoute(-1);
        this.tcpPathStatInfo.setSwCnt(0);
        this.tcpPathStatInfo.setSwB2GCnt(0);
        this.tcpPathStatInfo.setSwB2BCnt(0);
        this.tcpPathStatInfo.setSwG2GCnt(0);
        this.tcpPathStatInfo.setSwG2BCnt(0);
        this.trafficInfo.setWifiTraffic(0);
        this.trafficInfo.setRadioTraffic(0);
        this.tcpPathStatInfo.setMpDur(0);
        this.trafficInfo.setMpWifiTraffic(0);
        this.trafficInfo.setMpRadioTraffic(0);
    }

    public final void copyFrom(VideoUploadInfo vi) {
        this.appName = vi.appName;
        this.type = vi.type;
        this.techCode = vi.techCode;
        this.srDelay = vi.srDelay;
        this.fullDelay = vi.fullDelay;
        this.times = vi.times;
        this.totalLen = vi.totalLen;
        this.streamDur = vi.streamDur;
        this.videoDataRate = vi.videoDataRate;
        this.videoTerminateFlag = vi.videoTerminateFlag;
        this.uVMos = vi.uVMos;
        this.hostName = vi.hostName;
        this.videoStartDate = vi.videoStartDate;
        this.videoStartTime = vi.videoStartTime;
        this.videoEndTime = vi.videoEndTime;
        this.result = vi.result;
        this.mcc = vi.mcc;
        this.mnc = vi.mnc;
        this.networkType = vi.networkType;
        this.cellId = vi.cellId;
        this.radioSs = vi.radioSs;
        this.radioSq = vi.radioSq;
        this.radioSnr = vi.radioSnr;
        this.radioL1Dur = vi.radioL1Dur;
        this.radioL2Dur = vi.radioL2Dur;
        this.radioL3Dur = vi.radioL3Dur;
        this.radioL4Dur = vi.radioL4Dur;
        this.radioL5Dur = vi.radioL5Dur;
        this.wlanSs = vi.wlanSs;
        this.wlanBssid = vi.wlanBssid;
        this.wlanSsid = vi.wlanSsid;
        this.wlanL1Dur = vi.wlanL1Dur;
        this.wlanL2Dur = vi.wlanL2Dur;
        this.wlanL3Dur = vi.wlanL3Dur;
        this.wlanL4Dur = vi.wlanL4Dur;
        copyUdpTcpTrafficFrom(vi);
    }

    private void copyUdpTcpTrafficFrom(VideoUploadInfo vi) {
        PolicyStatInfo policyStatInfo = this.tcpPolicyStatInfo;
        if (policyStatInfo == null || this.tcpPathStatInfo == null || this.trafficInfo == null) {
            Log.e(TAG, "copyFrom: Video this.tcpPolicyStatInfo, this.tcpPathStatInfo or this.trafficInfo is null");
            return;
        }
        PolicyStatInfo policyStatInfo2 = vi.tcpPolicyStatInfo;
        if (policyStatInfo2 == null || vi.tcpPathStatInfo == null || vi.trafficInfo == null) {
            Log.e(TAG, "copyFrom: Video vi.tcpPolicyStatInfo, vi.tcpPathStatInfo or vi.trafficInfo is null");
            return;
        }
        policyStatInfo.setPolicyStartCond(policyStatInfo2.getPolicyStartCond());
        this.tcpPolicyStatInfo.setFlowType(vi.tcpPolicyStatInfo.getFlowType());
        this.tcpPolicyStatInfo.setPolicyType(vi.tcpPolicyStatInfo.getPolicyType());
        this.tcpPolicyStatInfo.setLinkMode(vi.tcpPolicyStatInfo.getLinkMode());
        this.tcpPolicyStatInfo.setSuccFlg(vi.tcpPolicyStatInfo.getSuccFlg());
        this.tcpPolicyStatInfo.setPolicyEnSuccCnt(vi.tcpPolicyStatInfo.getPolicyEnSuccCnt());
        this.tcpPolicyStatInfo.setPolicyEnCnt(vi.tcpPolicyStatInfo.getPolicyEnCnt());
        this.tcpPolicyStatInfo.setPolicyEnRspLat(vi.tcpPolicyStatInfo.getPolicyEnRspLat());
        this.tcpPolicyStatInfo.setPolicySwSuccCnt(vi.tcpPolicyStatInfo.getPolicySwSuccCnt());
        this.tcpPolicyStatInfo.setPolicySwCnt(vi.tcpPolicyStatInfo.getPolicySwCnt());
        this.tcpPolicyStatInfo.setPolicySwRspLat(vi.tcpPolicyStatInfo.getPolicySwRspLat());
        this.tcpPathStatInfo.setInitPrbRoute(vi.tcpPathStatInfo.getInitPrbRoute());
        this.tcpPathStatInfo.setSwCnt(vi.tcpPathStatInfo.getSwCnt());
        this.tcpPathStatInfo.setSwB2GCnt(vi.tcpPathStatInfo.getSwB2GCnt());
        this.tcpPathStatInfo.setSwB2BCnt(vi.tcpPathStatInfo.getSwB2BCnt());
        this.tcpPathStatInfo.setSwG2GCnt(vi.tcpPathStatInfo.getSwG2GCnt());
        this.tcpPathStatInfo.setSwG2BCnt(vi.tcpPathStatInfo.getSwG2BCnt());
        this.trafficInfo.setWifiTraffic(vi.trafficInfo.getWifiTraffic());
        this.trafficInfo.setRadioTraffic(vi.trafficInfo.getRadioTraffic());
        this.tcpPathStatInfo.setMpDur(vi.tcpPathStatInfo.getMpDur());
        this.trafficInfo.setMpWifiTraffic(vi.trafficInfo.getMpWifiTraffic());
        this.trafficInfo.setMpRadioTraffic(vi.trafficInfo.getMpRadioTraffic());
    }

    @Override // android.emcom.SmartcareInfos.SmartcareBaseInfo
    public void addToInfos(SmartcareInfos sci) {
        super.addToInfos(sci);
        sci.videoUploadInfo = this;
    }

    public String toString() {
        return "hash: " + hashCode() + ",app: " + this.appName + ",srDelay: " + this.srDelay + ",fullDelay: " + this.fullDelay + ",times: " + ((int) this.times) + ",totalLen: " + this.totalLen + ",streamDur: " + this.streamDur + ",videoDataRate: " + this.videoDataRate + ",videoTerminateFlag: " + ((int) this.videoTerminateFlag) + ",uVMos: " + ((int) this.uVMos) + ",videoStartDate: " + this.videoStartDate + ",videoStartTime: " + this.videoStartTime + ",videoEndTime: " + this.videoEndTime + ",result: " + this.result + ",networkType: " + ((int) this.networkType) + ",radioSs: " + ((int) this.radioSs) + ",radioSnr: " + this.radioSnr + ",wlanSs: " + ((int) this.wlanSs) + ",flowType: " + this.tcpPolicyStatInfo.getFlowType() + ",policyType: " + ((int) this.tcpPolicyStatInfo.getPolicyType()) + ",linkMode: " + ((int) this.tcpPolicyStatInfo.getLinkMode()) + ",initPrbRoute: " + ((int) this.tcpPathStatInfo.getInitPrbRoute()) + ",wifiTraffic: " + this.trafficInfo.getWifiTraffic() + ",radioTraffic: " + this.trafficInfo.getRadioTraffic() + ",mpDur:" + this.tcpPathStatInfo.getMpDur() + ",mpWifiTraffic:" + this.trafficInfo.getMpWifiTraffic() + ",mpRadioTraffic:" + this.trafficInfo.getMpRadioTraffic() + ",type:" + ((int) this.type);
    }

    @Override // android.emcom.SmartcareInfos.SmartcareBaseInfo
    public void recycle() {
        super.recycle();
        init();
    }
}
