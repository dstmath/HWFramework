package com.huawei.rms.iaware;

import android.rms.HwSysResManager;
import android.rms.iaware.NetLocationStrategy;

public class NetLocationStrategyEx {
    public static final int NETLOCATION_MODEM = 2;
    public static final int NETLOCATION_WIFI = 1;
    private NetLocationStrategy mNetLocationStrategy;

    private void setNetLocationStrategy(NetLocationStrategy netLocationStrategy) {
        this.mNetLocationStrategy = netLocationStrategy;
    }

    public static NetLocationStrategyEx getNetLocationStrategy(String pkgName, int uid, int type) {
        NetLocationStrategy netLocationStrategy = HwSysResManager.getInstance().getNetLocationStrategy(pkgName, uid, 2);
        if (netLocationStrategy == null) {
            return null;
        }
        NetLocationStrategyEx netLocationStrategyEx = new NetLocationStrategyEx();
        netLocationStrategyEx.setNetLocationStrategy(netLocationStrategy);
        return netLocationStrategyEx;
    }

    public long getCycle() {
        NetLocationStrategy netLocationStrategy = this.mNetLocationStrategy;
        if (netLocationStrategy != null) {
            return netLocationStrategy.getCycle();
        }
        return 0;
    }
}
