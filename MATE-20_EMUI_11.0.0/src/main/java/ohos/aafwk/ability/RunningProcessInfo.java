package ohos.aafwk.ability;

import java.util.Arrays;

public class RunningProcessInfo {
    public static final int DATA_IN_USE = 1;
    public static final int REASON_UNKNOWN = 0;
    public static final int SERVICE_IN_USE = 2;
    public static final int WEIGHT_CACHED = 400;
    public static final int WEIGHT_CANT_SAVE_STATE = 350;
    public static final int WEIGHT_FOREGROUND = 100;
    public static final int WEIGHT_FOREGROUND_SERVICE = 125;
    public static final int WEIGHT_GONE = 1000;
    public static final int WEIGHT_PERCEPTIBLE = 230;
    public static final int WEIGHT_SERVICE = 300;
    public static final int WEIGHT_TOP_SLEEPING = 325;
    public static final int WEIGHT_VISIBLE = 200;
    private int lastMemoryLevel;
    private int pid;
    private String[] pkgList;
    private String processName;
    private int uid;
    private int weight;
    private int weightReasonCode;

    public void setPid(int i) {
        this.pid = i;
    }

    public void setPkgList(String[] strArr) {
        if (strArr != null) {
            this.pkgList = (String[]) strArr.clone();
        } else {
            this.pkgList = null;
        }
    }

    public void setProcessName(String str) {
        this.processName = str;
    }

    public void setUid(int i) {
        this.uid = i;
    }

    public void setLastMemoryLevel(int i) {
        this.lastMemoryLevel = i;
    }

    public void setWeight(int i) {
        this.weight = i;
    }

    public void setWeightReasonCode(int i) {
        this.weightReasonCode = i;
    }

    public int getPid() {
        return this.pid;
    }

    public String[] getPkgList() {
        String[] strArr = this.pkgList;
        return strArr != null ? (String[]) strArr.clone() : new String[0];
    }

    public String getProcessName() {
        return this.processName;
    }

    public int getUid() {
        return this.uid;
    }

    public int getLastMemoryLevel() {
        return this.lastMemoryLevel;
    }

    public int getWeight() {
        return this.weight;
    }

    public int getWeightReasonCode() {
        return this.weightReasonCode;
    }

    public String toString() {
        return "pid = " + this.pid + "; processName = " + this.processName + "; uid = " + this.uid + "; pkgList = " + Arrays.toString(this.pkgList) + "; lastMemoryLevel = " + this.lastMemoryLevel + "; weight = " + this.weight + "; weightReasonCode = " + this.weightReasonCode + "; ";
    }
}
