package android.emcom;

import com.huawei.internal.widget.ConstantValues;

public class PolicyStatInfo {
    private static final String TAG = "PolicyStatInfo";
    private int flowType = 0;
    private short linkMode = 0;
    private String pkgName = null;
    private int policyEnCnt = 0;
    private int policyEnRspLat = 0;
    private int policyEnSuccCnt = 0;
    private int policyStartCond = 0;
    private int policySwCnt = 0;
    private int policySwRspLat = 0;
    private int policySwSuccCnt = 0;
    private short policyType = 0;
    private byte succFlg = -1;
    private int uid = -1;

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
        this.succFlg = (byte) (succFlg2 & ConstantValues.MAX_CHANNEL_VALUE);
        return this;
    }

    public byte getSuccFlg() {
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

    public int getPolicyStartCond() {
        return this.policyStartCond;
    }

    public void setPolicyStartCond(int policyStartCond2) {
        this.policyStartCond = policyStartCond2;
    }

    public short getLinkMode() {
        return this.linkMode;
    }

    public void setLinkMode(int linkMode2) {
        this.linkMode = (short) (65535 & linkMode2);
    }

    public int getPolicySwRspLat() {
        return this.policySwRspLat;
    }

    public void setPolicySwRspLat(int policySwRspLat2) {
        this.policySwRspLat = policySwRspLat2;
    }

    public String toString() {
        return "hash: " + hashCode() + ", pkgName: " + this.pkgName + ", uid: " + this.uid + ", flowType: " + this.flowType + ", policyType: " + ((int) this.policyType) + ", succFlg: " + ((int) this.succFlg) + ", policyEnCnt: " + this.policyEnCnt + ", policyEnSuccCnt: " + this.policyEnSuccCnt + ", policyEnRspLat: " + this.policyEnRspLat;
    }
}
