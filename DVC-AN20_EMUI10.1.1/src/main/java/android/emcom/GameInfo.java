package android.emcom;

import android.emcom.SmartcareInfos;
import com.huawei.uikit.effect.BuildConfig;

public class GameInfo extends SmartcareInfos.SmartcareBaseInfo {
    private static final int INVALID_INTEGER_VALUE = -1;
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
    private int mWifiApCap;
    private int mWifiFrequency;
    private int mWifiMode;
    private int mWifiSecurity;
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

    @Override // android.emcom.SmartcareInfos.SmartcareBaseInfo
    public void addToInfos(SmartcareInfos cis) {
        super.addToInfos(cis);
        cis.gameInfo = this;
    }

    public final void copyFrom(GameInfo gameInfo) {
        this.hostName = gameInfo.hostName;
        this.gameStartDate = gameInfo.gameStartDate;
        this.gameStartTime = gameInfo.gameStartTime;
        this.gameEndTime = gameInfo.gameEndTime;
        this.mcc = gameInfo.mcc;
        this.mnc = gameInfo.mnc;
        this.networkType = gameInfo.networkType;
        this.cellId = gameInfo.cellId;
        this.radioSs = gameInfo.radioSs;
        this.radioSq = gameInfo.radioSq;
        this.radioSnr = gameInfo.radioSnr;
        this.radioL1Dur = gameInfo.radioL1Dur;
        this.radioL2Dur = gameInfo.radioL2Dur;
        this.radioL3Dur = gameInfo.radioL3Dur;
        this.radioL4Dur = gameInfo.radioL4Dur;
        this.radioL5Dur = gameInfo.radioL5Dur;
        this.wlanSs = gameInfo.wlanSs;
        this.wlanBssid = gameInfo.wlanBssid;
        this.wlanSsid = gameInfo.wlanSsid;
        this.wlanL1Dur = gameInfo.wlanL1Dur;
        this.wlanL2Dur = gameInfo.wlanL2Dur;
        this.wlanL3Dur = gameInfo.wlanL3Dur;
        this.wlanL4Dur = gameInfo.wlanL4Dur;
        setWifiApCap(gameInfo.mWifiApCap);
        setWifiMode(gameInfo.mWifiMode);
        setWifiFrequency(gameInfo.mWifiFrequency);
        setWifiSecurity(gameInfo.mWifiSecurity);
        this.appName = gameInfo.appName;
        this.type = gameInfo.type;
        this.gameLoadLat = gameInfo.gameLoadLat;
        this.gameLoadThrput = gameInfo.gameLoadThrput;
        this.connDropFlg = gameInfo.connDropFlg;
        this.selfRoleLoadLat = gameInfo.selfRoleLoadLat;
        this.selfRoleLoadSuccFlg = gameInfo.selfRoleLoadSuccFlg;
        this.battleDur = gameInfo.battleDur;
        this.battleRttAvg = gameInfo.battleRttAvg;
        this.battleRttL1Dur = gameInfo.battleRttL1Dur;
        this.battleRttL2Dur = gameInfo.battleRttL2Dur;
        this.battleRttL3Dur = gameInfo.battleRttL3Dur;
        this.battleRttL4Dur = gameInfo.battleRttL4Dur;
        this.battleRttL5Dur = gameInfo.battleRttL5Dur;
        this.battleRttL6Dur = gameInfo.battleRttL6Dur;
        this.battleRttL7Dur = gameInfo.battleRttL7Dur;
        this.battleRttL8Dur = gameInfo.battleRttL8Dur;
        this.techCode = gameInfo.techCode;
        copyUdpTcpTrafficFrom(gameInfo);
    }

    private void copyUdpTcpTrafficFrom(GameInfo gameInfo) {
        TrafficInfo trafficInfo2;
        MPStatInfo mPStatInfo;
        PolicyStatInfo policyStatInfo;
        MPStatInfo mPStatInfo2;
        PolicyStatInfo policyStatInfo2;
        PolicyStatInfo policyStatInfo3 = this.udpPolicyStatInfo;
        if (!(policyStatInfo3 == null || (policyStatInfo2 = gameInfo.udpPolicyStatInfo) == null)) {
            policyStatInfo3.setPolicyStartCond(policyStatInfo2.getPolicyStartCond());
            this.udpPolicyStatInfo.setFlowType(gameInfo.udpPolicyStatInfo.getFlowType());
            this.udpPolicyStatInfo.setPolicyType(gameInfo.udpPolicyStatInfo.getPolicyType());
            this.udpPolicyStatInfo.setLinkMode(gameInfo.udpPolicyStatInfo.getLinkMode());
            this.udpPolicyStatInfo.setSuccFlg(gameInfo.udpPolicyStatInfo.getSuccFlg());
            this.udpPolicyStatInfo.setPolicyEnSuccCnt(gameInfo.udpPolicyStatInfo.getPolicyEnSuccCnt());
            this.udpPolicyStatInfo.setPolicyEnCnt(gameInfo.udpPolicyStatInfo.getPolicyEnCnt());
            this.udpPolicyStatInfo.setPolicyEnRspLat(gameInfo.udpPolicyStatInfo.getPolicyEnRspLat());
            this.udpPolicyStatInfo.setPolicySwSuccCnt(gameInfo.udpPolicyStatInfo.getPolicySwSuccCnt());
            this.udpPolicyStatInfo.setPolicySwCnt(gameInfo.udpPolicyStatInfo.getPolicySwCnt());
            this.udpPolicyStatInfo.setPolicySwRspLat(gameInfo.udpPolicyStatInfo.getPolicySwRspLat());
        }
        MPStatInfo mPStatInfo3 = this.udpPathStatInfo;
        if (!(mPStatInfo3 == null || (mPStatInfo2 = gameInfo.udpPathStatInfo) == null)) {
            mPStatInfo3.setInitPrbRoute(mPStatInfo2.getInitPrbRoute());
            this.udpPathStatInfo.setSwCnt(gameInfo.udpPathStatInfo.getSwCnt());
            this.udpPathStatInfo.setSwB2GCnt(gameInfo.udpPathStatInfo.getSwB2GCnt());
            this.udpPathStatInfo.setSwB2BCnt(gameInfo.udpPathStatInfo.getSwB2BCnt());
            this.udpPathStatInfo.setSwG2GCnt(gameInfo.udpPathStatInfo.getSwG2GCnt());
            this.udpPathStatInfo.setSwG2BCnt(gameInfo.udpPathStatInfo.getSwG2BCnt());
        }
        PolicyStatInfo policyStatInfo4 = this.tcpPolicyStatInfo;
        if (!(policyStatInfo4 == null || (policyStatInfo = gameInfo.tcpPolicyStatInfo) == null)) {
            policyStatInfo4.setPolicyStartCond(policyStatInfo.getPolicyStartCond());
            this.tcpPolicyStatInfo.setFlowType(gameInfo.tcpPolicyStatInfo.getFlowType());
            this.tcpPolicyStatInfo.setPolicyType(gameInfo.tcpPolicyStatInfo.getPolicyType());
            this.tcpPolicyStatInfo.setLinkMode(gameInfo.tcpPolicyStatInfo.getLinkMode());
            this.tcpPolicyStatInfo.setSuccFlg(gameInfo.tcpPolicyStatInfo.getSuccFlg());
            this.tcpPolicyStatInfo.setPolicyEnSuccCnt(gameInfo.tcpPolicyStatInfo.getPolicyEnSuccCnt());
            this.tcpPolicyStatInfo.setPolicyEnCnt(gameInfo.tcpPolicyStatInfo.getPolicyEnCnt());
            this.tcpPolicyStatInfo.setPolicyEnRspLat(gameInfo.tcpPolicyStatInfo.getPolicyEnRspLat());
            this.tcpPolicyStatInfo.setPolicySwSuccCnt(gameInfo.tcpPolicyStatInfo.getPolicySwSuccCnt());
            this.tcpPolicyStatInfo.setPolicySwCnt(gameInfo.tcpPolicyStatInfo.getPolicySwCnt());
            this.tcpPolicyStatInfo.setPolicySwRspLat(gameInfo.tcpPolicyStatInfo.getPolicySwRspLat());
        }
        MPStatInfo mPStatInfo4 = this.tcpPathStatInfo;
        if (!(mPStatInfo4 == null || (mPStatInfo = gameInfo.tcpPathStatInfo) == null)) {
            mPStatInfo4.setInitPrbRoute(mPStatInfo.getInitPrbRoute());
            this.tcpPathStatInfo.setSwCnt(gameInfo.tcpPathStatInfo.getSwCnt());
            this.tcpPathStatInfo.setSwB2GCnt(gameInfo.tcpPathStatInfo.getSwB2GCnt());
            this.tcpPathStatInfo.setSwB2BCnt(gameInfo.tcpPathStatInfo.getSwB2BCnt());
            this.tcpPathStatInfo.setSwG2GCnt(gameInfo.tcpPathStatInfo.getSwG2GCnt());
            this.tcpPathStatInfo.setSwG2BCnt(gameInfo.tcpPathStatInfo.getSwG2BCnt());
        }
        TrafficInfo trafficInfo3 = this.trafficInfo;
        if (trafficInfo3 != null && (trafficInfo2 = gameInfo.trafficInfo) != null) {
            trafficInfo3.setWifiTraffic(trafficInfo2.getWifiTraffic());
            this.trafficInfo.setRadioTraffic(gameInfo.trafficInfo.getRadioTraffic());
        }
    }

    private void init() {
        this.hostName = BuildConfig.FLAVOR;
        this.gameStartDate = 0;
        this.gameStartTime = 0;
        this.gameEndTime = 0;
        this.mcc = 0;
        this.mnc = 0;
        this.networkType = 0;
        this.cellId = BuildConfig.FLAVOR;
        this.radioSs = -1;
        this.radioSq = -1;
        this.radioSnr = -1;
        this.radioL1Dur = 0;
        this.radioL2Dur = 0;
        this.radioL3Dur = 0;
        this.radioL4Dur = 0;
        this.radioL5Dur = 0;
        this.wlanSs = -1;
        this.wlanBssid = BuildConfig.FLAVOR;
        this.mWifiApCap = -1;
        this.mWifiMode = -1;
        this.mWifiFrequency = -1;
        this.mWifiSecurity = -1;
        this.wlanSsid = BuildConfig.FLAVOR;
        this.wlanL1Dur = 0;
        this.wlanL2Dur = 0;
        this.wlanL3Dur = 0;
        this.wlanL4Dur = 0;
        this.appName = BuildConfig.FLAVOR;
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
        this.techCode = BuildConfig.FLAVOR;
        initUdpTcpTraffic();
    }

    private void initUdpTcpTraffic() {
        PolicyStatInfo policyStatInfo = this.udpPolicyStatInfo;
        if (policyStatInfo != null) {
            policyStatInfo.setPolicyStartCond(0);
            this.udpPolicyStatInfo.setFlowType(0);
            this.udpPolicyStatInfo.setPolicyType(-1);
            this.udpPolicyStatInfo.setLinkMode(-1);
            this.udpPolicyStatInfo.setSuccFlg(-1);
            this.udpPolicyStatInfo.setPolicyEnSuccCnt(0);
            this.udpPolicyStatInfo.setPolicyEnCnt(0);
            this.udpPolicyStatInfo.setPolicyEnRspLat(0);
            this.udpPolicyStatInfo.setPolicySwSuccCnt(0);
            this.udpPolicyStatInfo.setPolicySwCnt(0);
            this.udpPolicyStatInfo.setPolicySwRspLat(0);
        }
        MPStatInfo mPStatInfo = this.udpPathStatInfo;
        if (mPStatInfo != null) {
            mPStatInfo.setInitPrbRoute(0);
            this.udpPathStatInfo.setSwCnt(0);
            this.udpPathStatInfo.setSwB2GCnt(0);
            this.udpPathStatInfo.setSwB2BCnt(0);
            this.udpPathStatInfo.setSwG2GCnt(0);
            this.udpPathStatInfo.setSwG2BCnt(0);
        }
        PolicyStatInfo policyStatInfo2 = this.tcpPolicyStatInfo;
        if (policyStatInfo2 != null) {
            policyStatInfo2.setPolicyStartCond(0);
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
        }
        MPStatInfo mPStatInfo2 = this.tcpPathStatInfo;
        if (mPStatInfo2 != null) {
            mPStatInfo2.setInitPrbRoute(-1);
            this.tcpPathStatInfo.setSwCnt(0);
            this.tcpPathStatInfo.setSwB2GCnt(0);
            this.tcpPathStatInfo.setSwB2BCnt(0);
            this.tcpPathStatInfo.setSwG2GCnt(0);
            this.tcpPathStatInfo.setSwG2BCnt(0);
        }
        TrafficInfo trafficInfo2 = this.trafficInfo;
        if (trafficInfo2 != null) {
            trafficInfo2.setWifiTraffic(0);
            this.trafficInfo.setRadioTraffic(0);
        }
    }

    @Override // android.emcom.SmartcareInfos.SmartcareBaseInfo
    public void recycle() {
        super.recycle();
        init();
    }

    public String toString() {
        return "hash: " + hashCode() + ",StartDate: " + this.gameStartDate + ",StartTime: " + this.gameStartTime + ",EndTime: " + this.gameEndTime + ",app: " + this.appName + ",type: " + ((int) this.type) + ",gameLoadLat: " + this.gameLoadLat + ",connDropFlg: " + ((int) this.connDropFlg) + ",selfRoleLoadLat: " + this.selfRoleLoadLat + ",battleDur: " + this.battleDur + ",battleRttAvg: " + ((int) this.battleRttAvg) + ",networkType: " + ((int) this.networkType) + ",radioSs: " + ((int) this.radioSs) + ",radioSnr: " + this.radioSnr + ",wlanSs: " + ((int) this.wlanSs) + ",techCode: " + this.techCode + ",flowType: " + this.udpPolicyStatInfo.getFlowType() + ",policyType: " + ((int) this.udpPolicyStatInfo.getPolicyType()) + ",linkMode: " + ((int) this.udpPolicyStatInfo.getLinkMode()) + ",initPrbRoute: " + ((int) this.udpPathStatInfo.getInitPrbRoute()) + ",flowType2: " + this.tcpPolicyStatInfo.getFlowType() + ",policyType2: " + ((int) this.tcpPolicyStatInfo.getPolicyType()) + ",linkMode2: " + ((int) this.tcpPolicyStatInfo.getLinkMode()) + ",initPrbRoute2: " + ((int) this.tcpPathStatInfo.getInitPrbRoute()) + ",mWifiApCap: " + this.mWifiApCap + ",mWifiMode: " + this.mWifiMode + ",mWifiFrequency: " + this.mWifiFrequency + ",mWifiSecurity: " + this.mWifiSecurity;
    }

    public void setWifiApCap(int wifiApCap) {
        this.mWifiApCap = wifiApCap;
    }

    public int getWifiApCap() {
        return this.mWifiApCap;
    }

    public void setWifiMode(int wifiMode) {
        this.mWifiMode = wifiMode;
    }

    public int getWifiMode() {
        return this.mWifiMode;
    }

    public void setWifiFrequency(int wifiFrequency) {
        this.mWifiFrequency = wifiFrequency;
    }

    public int getWifiFrequency() {
        return this.mWifiFrequency;
    }

    public void setWifiSecurity(int wifiSecurity) {
        this.mWifiSecurity = wifiSecurity;
    }

    public int getWifiSecurity() {
        return this.mWifiSecurity;
    }
}
