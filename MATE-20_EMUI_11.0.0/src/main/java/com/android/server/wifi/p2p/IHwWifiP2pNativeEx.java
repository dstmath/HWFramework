package com.android.server.wifi.p2p;

public interface IHwWifiP2pNativeEx {
    boolean addP2pRptGroup(String str);

    String deliverP2pData(int i, int i2, String str);

    boolean enableP2p(int i);

    int getP2pLinkSpeed(String str);

    boolean magiclinkConnect(String str);

    boolean magiclinkGroupAdd(int i, String str);

    boolean magiclinkGroupAdd(boolean z, String str);
}
