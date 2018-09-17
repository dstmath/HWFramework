package com.huawei.android.pushagent.model.a;

import android.content.Context;

public class i extends c {
    private static final byte[] w = new byte[0];
    private static i x = null;

    private i(Context context) {
        super(context, "pushConfig");
        y();
    }

    public static i ea(Context context) {
        synchronized (w) {
            i iVar;
            if (x != null) {
                iVar = x;
                return iVar;
            }
            x = new i(context);
            iVar = x;
            return iVar;
        }
    }

    public long eh() {
        return getLong("upAnalyticUrlTime", 0);
    }

    public boolean ei(long j) {
        return setValue("upAnalyticUrlTime", Long.valueOf(j));
    }

    public long ep() {
        return getLong("run_time_less_times", 0);
    }

    public boolean eq(long j) {
        return setValue("run_time_less_times", Long.valueOf(j));
    }

    public int er() {
        return getInt("lastConnectPushSrvMethodIdx", 0);
    }

    public boolean et(int i) {
        return setValue("lastConnectPushSrvMethodIdx", Integer.valueOf(i));
    }

    public int es() {
        return getInt("tryConnectPushSevTimes", 0);
    }

    public boolean eu(int i) {
        return setValue("tryConnectPushSevTimes", Integer.valueOf(i));
    }

    public long ee() {
        return getLong("queryTrsTimes", 0);
    }

    public boolean ef(long j) {
        return setValue("queryTrsTimes", Long.valueOf(j));
    }

    public long ec() {
        return getLong("lastQueryTRSTime", 0);
    }

    public boolean ed(long j) {
        return setValue("lastQueryTRSTime", Long.valueOf(j));
    }

    public long ej() {
        return getLong("lastQueryTRSsucc_time", 0);
    }

    public boolean eg(long j) {
        return setValue("lastQueryTRSsucc_time", Long.valueOf(j));
    }

    public boolean el() {
        return w("isBadNetworkMode", false);
    }

    public boolean em(boolean z) {
        return setValue("isBadNetworkMode", Boolean.valueOf(z));
    }

    public int en() {
        return getInt("version_config", 0);
    }

    public boolean eo(int i) {
        return setValue("version_config", Integer.valueOf(i));
    }

    public int eb() {
        return getInt("networkPolicySwitch", 1);
    }

    public boolean ek(int i) {
        return setValue("networkPolicySwitch", Integer.valueOf(i));
    }
}
