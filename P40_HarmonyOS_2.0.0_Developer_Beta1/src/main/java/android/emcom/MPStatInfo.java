package android.emcom;

import android.content.Context;
import android.util.Log;
import com.huawei.internal.widget.ConstantValues;
import org.json.JSONException;
import org.json.JSONObject;

public class MPStatInfo {
    private static final int PATH_DEFAULT = -1;
    private static final int POLICY_TYPE_AND = 65535;
    private static final int PRB_AND = 255;
    private static final byte PRB_DEFAULT = -1;
    private static final byte SUCC_DEFAULT = -1;
    private static final int SUC_FLG_AND = 255;
    private static final String TAG = "MPStatInfo";
    private static final int UID_DEFAULT = -1;
    private int currSelectedPath = -1;
    private int flowType = 0;
    private byte initPrbRoute = -1;
    private short linkMode = 0;
    private int mpDur = 0;
    private long mpEndTime = 0;
    private long mpStartTime = 0;
    private int msWb2Bcnt = 0;
    private int msWb2Gcnt = 0;
    private int msWcnt = 0;
    private int msWg2Bcnt = 0;
    private int msWg2Gcnt = 0;
    private String pkg = null;
    private short policyType = 0;
    private byte succFlg = -1;
    private int uid = -1;

    public void cleanStatisticInfo() {
        this.flowType = 0;
        this.policyType = 0;
        this.linkMode = 0;
        this.succFlg = -1;
        this.initPrbRoute = -1;
        this.currSelectedPath = -1;
        this.msWcnt = 0;
        this.msWb2Gcnt = 0;
        this.msWb2Bcnt = 0;
        this.msWg2Gcnt = 0;
        this.msWg2Bcnt = 0;
        this.mpStartTime = 0;
        this.mpEndTime = 0;
        this.mpDur = 0;
    }

    public MPStatInfo setPkgName(String pkgName) {
        this.pkg = pkgName;
        return this;
    }

    public String getPkgName() {
        return this.pkg;
    }

    public MPStatInfo setUid(int uid2) {
        this.uid = uid2;
        return this;
    }

    public int getUid() {
        return this.uid;
    }

    public MPStatInfo setFlowType(int flowType2) {
        this.flowType = flowType2;
        return this;
    }

    public int getFlowType() {
        return this.flowType;
    }

    public MPStatInfo setPolicyType(int policyType2) {
        this.policyType = (short) (POLICY_TYPE_AND & policyType2);
        return this;
    }

    public short getPolicyType() {
        return this.policyType;
    }

    public MPStatInfo setSuccFlg(int succFlg2) {
        this.succFlg = (byte) (succFlg2 & ConstantValues.MAX_CHANNEL_VALUE);
        return this;
    }

    public byte getSuccFlg() {
        return this.succFlg;
    }

    public MPStatInfo setInitPrbRoute(int initPrbRoute2) {
        this.initPrbRoute = (byte) (initPrbRoute2 & ConstantValues.MAX_CHANNEL_VALUE);
        return this;
    }

    public byte getInitPrbRoute() {
        return this.initPrbRoute;
    }

    public MPStatInfo setCurrSelectedPath(int currSelectedPath2) {
        this.currSelectedPath = currSelectedPath2;
        return this;
    }

    public int getCurrSelectedPath() {
        return this.currSelectedPath;
    }

    public MPStatInfo setSwCnt(int wcnt) {
        this.msWcnt = wcnt;
        return this;
    }

    public int getSwCnt() {
        return this.msWcnt;
    }

    public MPStatInfo setSwB2GCnt(int wb2Gcnt) {
        this.msWb2Gcnt = wb2Gcnt;
        return this;
    }

    public int getSwB2GCnt() {
        return this.msWb2Gcnt;
    }

    public MPStatInfo setSwB2BCnt(int wb2Bcnt) {
        this.msWb2Bcnt = wb2Bcnt;
        return this;
    }

    public int getSwB2BCnt() {
        return this.msWb2Bcnt;
    }

    public MPStatInfo setSwG2GCnt(int wg2Gcnt) {
        this.msWg2Gcnt = wg2Gcnt;
        return this;
    }

    public int getSwG2GCnt() {
        return this.msWg2Gcnt;
    }

    public MPStatInfo setSwG2BCnt(int wg2Bcnt) {
        this.msWg2Bcnt = wg2Bcnt;
        return this;
    }

    public int getSwG2BCnt() {
        return this.msWg2Bcnt;
    }

    public void setMpStartTime(long mpStartTime2) {
        this.mpStartTime = mpStartTime2;
    }

    public long getMpStartTime() {
        return this.mpStartTime;
    }

    public void setMpEndTime(long mpEndTime2) {
        this.mpEndTime = mpEndTime2;
    }

    public long getMpEndTime() {
        return this.mpEndTime;
    }

    public void setMpDur(int mpDur2) {
        this.mpDur = mpDur2;
    }

    public int getMpDur() {
        return this.mpDur;
    }

    public short getLinkMode() {
        return this.linkMode;
    }

    public void setLinkMode(short linkMode2) {
        this.linkMode = linkMode2;
    }

    public void updateStatsInfo(Context context, String jsonStr) {
        if (context != null && jsonStr != null) {
            try {
                JSONObject jsonObject = new JSONObject(jsonStr);
                int tmpUid = jsonObject.optInt("uid", -1);
                if (tmpUid != -1) {
                    if (this.uid == tmpUid || this.uid == -1) {
                        this.pkg = context.getPackageManager().getNameForUid(this.uid);
                        this.initPrbRoute = (byte) (jsonObject.optInt("initPrbRoute", -1) & ConstantValues.MAX_CHANNEL_VALUE);
                        this.currSelectedPath = jsonObject.optInt("currSelectedPath", -1);
                        this.msWcnt = jsonObject.optInt("sWCnt", 0);
                        this.msWb2Gcnt = jsonObject.optInt("sWB2GCnt", 0);
                        this.msWb2Bcnt = jsonObject.optInt("sWB2BCnt", 0);
                        this.msWg2Gcnt = jsonObject.optInt("sWG2GCnt", 0);
                        this.msWg2Bcnt = jsonObject.optInt("sWG2BCnt", 0);
                        Log.d(TAG, "updateStatsInfo jsonStr=" + jsonStr + " newValue=" + toString());
                        return;
                    }
                }
                Log.d(TAG, "not the same app for updateStatsInfo.");
            } catch (JSONException e) {
                Log.e(TAG, "parse MPStatsInfo Error.");
            }
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("{uid=");
        sb.append(this.uid);
        sb.append(", ");
        sb.append("pkg=");
        sb.append(this.pkg);
        sb.append(", ");
        sb.append("flowType=");
        sb.append(this.flowType);
        sb.append(", ");
        sb.append("policyType=");
        sb.append((int) this.policyType);
        sb.append(", ");
        sb.append("linkMode=");
        sb.append((int) this.linkMode);
        sb.append(", ");
        sb.append("initPrbRoute=");
        sb.append((int) this.initPrbRoute);
        sb.append(", ");
        sb.append("currSelectedPath=");
        sb.append(this.currSelectedPath);
        sb.append(", ");
        sb.append("sWCnt=");
        sb.append(this.msWcnt);
        sb.append(", ");
        sb.append("sWB2GCnt=");
        sb.append(this.msWb2Gcnt);
        sb.append(", ");
        sb.append("sWB2BCnt=");
        sb.append(this.msWb2Bcnt);
        sb.append(", ");
        sb.append("sWG2GCnt=");
        sb.append(this.msWg2Gcnt);
        sb.append(", ");
        sb.append("sWG2BCnt=");
        sb.append(this.msWg2Bcnt);
        sb.append(", ");
        return sb.toString();
    }
}
