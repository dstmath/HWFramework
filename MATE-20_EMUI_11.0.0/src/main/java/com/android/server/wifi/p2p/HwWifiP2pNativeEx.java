package com.android.server.wifi.p2p;

public class HwWifiP2pNativeEx implements IHwWifiP2pNativeEx {
    private SupplicantP2pIfaceHal mSupplicantP2pIfaceHal;

    private HwWifiP2pNativeEx(SupplicantP2pIfaceHal p2pIfaceHal) {
        this.mSupplicantP2pIfaceHal = p2pIfaceHal;
    }

    public static HwWifiP2pNativeEx createHwWifiP2pNativeEx(SupplicantP2pIfaceHal p2pIfaceHal) {
        return new HwWifiP2pNativeEx(p2pIfaceHal);
    }

    public boolean magiclinkGroupAdd(boolean persistent, String freq) {
        return this.mSupplicantP2pIfaceHal.mIHwSupplicantP2pIfaceHalEx.groupAddWithFreq(-1, persistent, freq);
    }

    public boolean magiclinkGroupAdd(int netId, String freq) {
        return this.mSupplicantP2pIfaceHal.mIHwSupplicantP2pIfaceHalEx.groupAddWithFreq(netId, true, freq);
    }

    public boolean magiclinkConnect(String cmd) {
        return this.mSupplicantP2pIfaceHal.mIHwSupplicantP2pIfaceHalEx.magiclinkConnect(cmd);
    }

    public boolean enableP2p(int p2pFlag) {
        return true;
    }

    public boolean addP2pRptGroup(String config) {
        return this.mSupplicantP2pIfaceHal.mIHwSupplicantP2pIfaceHalEx.addP2pRptGroup(config);
    }

    public int getP2pLinkSpeed(String ifname) {
        return this.mSupplicantP2pIfaceHal.mIHwSupplicantP2pIfaceHalEx.getP2pLinkspeed(ifname);
    }

    public String deliverP2pData(int cmdType, int dataType, String carryData) {
        return this.mSupplicantP2pIfaceHal.mIHwSupplicantP2pIfaceHalEx.deliverP2pData(cmdType, dataType, carryData);
    }
}
