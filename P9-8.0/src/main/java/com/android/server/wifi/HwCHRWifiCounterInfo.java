package com.android.server.wifi;

abstract class HwCHRWifiCounterInfo {
    protected long mDelta = 0;
    protected String mOperate;
    protected String mTag;

    public abstract void parserValue(String str, String str2);

    public HwCHRWifiCounterInfo(String info) {
        this.mTag = info;
        this.mOperate = "=";
    }

    public HwCHRWifiCounterInfo(String info, String op) {
        this.mTag = info;
        this.mOperate = op;
    }

    public boolean match(String line) {
        if (line == null) {
            return false;
        }
        return line.startsWith(this.mTag + this.mOperate);
    }

    public long getDelta() {
        return this.mDelta;
    }

    public String getTag() {
        return this.mTag;
    }

    public boolean isSameTag(String value) {
        return this.mTag.equals(value);
    }

    public String toString() {
        return this.mTag + "=" + this.mDelta + HwCHRWifiCPUUsage.COL_SEP;
    }
}
