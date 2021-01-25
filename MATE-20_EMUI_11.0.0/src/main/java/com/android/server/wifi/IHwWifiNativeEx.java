package com.android.server.wifi;

public interface IHwWifiNativeEx {
    boolean deauthLastRoamingBssidHw(String str, String str2);

    boolean delStaticARP(String str);

    boolean disassociateSoftapStaHw(String str);

    boolean disassociateWifiRepeaterStationHw(String str, String str2);

    void enableHiLinkHandshake(boolean z, String str);

    void gameKOGAdjustSpeed(int i, int i2);

    String getApVendorInfo();

    int getChipsetWifiCategory();

    int getChipsetWifiFeatrureCapability();

    String getConnectionRawPsk();

    String getMssState();

    String getSoftapClientsHw();

    int getWifiAnt(String str, int i);

    String getWpaSuppConfig();

    String heartBeat(String str);

    boolean hwABSBlackList(String str);

    boolean hwABSSetCapability(int i);

    boolean hwABSSoftHandover(int i);

    int hwRegisterNatives();

    void hwUnregisterHwWifiExt();

    void initPrivFeatureCapability();

    boolean isDfsChannel(int i);

    boolean isSpHalNull();

    boolean isSupportRsdbByDriver();

    boolean isSupportVoWifiDetect();

    void pwrPercentBoostModeset(int i);

    void query11vRoamingNetwork(int i);

    void query11vRoamingNetwork(int i, String str);

    String readSoftapDhcpLeaseFileHw();

    int sendCmdToDriver(String str, int i, byte[] bArr);

    void sendWifiPowerCommand(int i);

    void setChipsetWifiCategory();

    void setChipsetWifiFeatrureCapability();

    int setCmdToWifiChip(String str, int i, int i2, int i3, int i4);

    boolean setFilterEnable(String str, boolean z);

    void setIsmcoexMode(boolean z);

    boolean setPwrBoost(int i);

    boolean setSoftapHw(String str, String str2);

    boolean setSoftapMacFltrHw(String str);

    boolean setStaticARP(String str, String str2);

    int setWifiAnt(String str, int i, int i2);

    boolean setWifiRepeaterMacFilterHw(String str, String str2);

    int setWifiTxPowerHw(int i);

    boolean startRxFilter(String str);

    boolean stopRxFilter(String str);

    boolean voWifiDetectSet(String str);
}
