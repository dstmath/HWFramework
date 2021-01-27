package com.android.server.wifi.p2p;

import android.hardware.wifi.supplicant.V1_0.ISupplicantIface;

public interface IHwSupplicantP2pIfaceHalEx {
    boolean addP2pRptGroup(String str);

    String deliverP2pData(int i, int i2, String str);

    int getP2pLinkspeed(String str);

    boolean groupAddWithFreq(int i, boolean z, String str);

    boolean magiclinkConnect(String str);

    boolean trySetupForVendorV3_0(ISupplicantIface iSupplicantIface, String str);
}
