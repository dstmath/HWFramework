package com.android.server.wifi.p2p;

import android.hardware.wifi.supplicant.V1_0.ISupplicantP2pIface;

public interface IHwSupplicantP2pIfaceHalInner {
    Object getLock();

    ISupplicantP2pIface getSupplicantP2pIface();

    int getValueOfResultNotValid();

    boolean hwLinkToSupplicantP2pIfaceDeath();

    void hwSupplicantServiceDiedHandler();

    void setSupplicantP2pIface(ISupplicantP2pIface iSupplicantP2pIface);
}
