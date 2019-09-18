package android.emcom;

public class PolicyStatInfo {
    private static final String TAG = "PolicyStatInfo";
    public int flowType = 0;
    public short linkMode = 0;
    public String pkgName = null;
    public int policyEnCnt = 0;
    public int policyEnRspLat = 0;
    public int policyEnSuccCnt = 0;
    public int policyStartCond = 0;
    public int policySwCnt = 0;
    public int policySwRspLat = 0;
    public int policySwSuccCnt = 0;
    public short policyType = 0;
    public int succFlg = -1;
    public int uid = -1;

    public void cleanPolicyStatInfo() {
        this.flowType = 0;
        this.policyType = 0;
        this.succFlg = -1;
        this.policyEnSuccCnt = 0;
        this.policyEnCnt = 0;
        this.policySwSuccCnt = 0;
        this.policySwCnt = 0;
        this.policyEnRspLat = 0;
        this.policySwRspLat = 0;
    }

    public PolicyStatInfo setPkgName(String pkgName2) {
        this.pkgName = pkgName2;
        return this;
    }

    public String getPkgName() {
        return this.pkgName;
    }

    public PolicyStatInfo setUid(int uid2) {
        this.uid = uid2;
        return this;
    }

    public int getUid() {
        return this.uid;
    }

    public PolicyStatInfo setFlowType(int flowType2) {
        this.flowType = flowType2;
        return this;
    }

    public int getFlowType() {
        return this.flowType;
    }

    public PolicyStatInfo setPolicyType(int policyType2) {
        this.policyType = (short) (65535 & policyType2);
        return this;
    }

    public short getPolicyType() {
        return this.policyType;
    }

    public PolicyStatInfo setSuccFlg(int succFlg2) {
        this.succFlg = succFlg2;
        return this;
    }

    public int getSuccFlg() {
        return this.succFlg;
    }

    public PolicyStatInfo setPolicyEnCnt(int cnt) {
        this.policyEnCnt = cnt;
        return this;
    }

    public int getPolicyEnCnt() {
        return this.policyEnCnt;
    }

    public PolicyStatInfo setPolicyEnSuccCnt(int cnt) {
        this.policyEnSuccCnt = cnt;
        return this;
    }

    public int getPolicyEnSuccCnt() {
        return this.policyEnSuccCnt;
    }

    public PolicyStatInfo setPolicySwCnt(int cnt) {
        this.policySwCnt = cnt;
        return this;
    }

    public int getPolicySwCnt() {
        return this.policySwCnt;
    }

    public PolicyStatInfo setPolicySwSuccCnt(int cnt) {
        this.policySwSuccCnt = cnt;
        return this;
    }

    public int getPolicySwSuccCnt() {
        return this.policySwSuccCnt;
    }

    public PolicyStatInfo setPolicyEnRspLat(int latency) {
        this.policyEnRspLat = latency;
        return this;
    }

    public int getPolicyEnRspLat() {
        return this.policyEnRspLat;
    }

    public String toString() {
        return "hash: " + hashCode() + ", pkgName: " + this.pkgName + ", uid: " + this.uid + ", flowType: " + this.flowType + ", policyType: " + this.policyType + ", succFlg: " + this.succFlg + ", policyEnCnt: " + this.policyEnCnt + ", policyEnSuccCnt: " + this.policyEnSuccCnt + ", policyEnRspLat: " + this.policyEnRspLat;
    }
}
