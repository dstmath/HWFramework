package com.android.server.wifi;

import android.hardware.wifi.supplicant.V1_0.ISupplicantIface;
import com.android.server.wifi.SupplicantStaIfaceHal;

public interface IHwSupplicantStaIfaceHalEx {
    boolean absPowerCtrl(String str, int i);

    String deliverStaIfaceData(String str, int i, int i2, String str2);

    boolean enableHiLinkHandshake(String str, boolean z, String str2);

    String getApVendorInfo(String str);

    String getMssState(String str);

    String getRsdbCapability(String str);

    String getWpasConfig(String str, int i);

    String heartBeat(String str, String str2);

    boolean pwrPercentBoostModeset(String str, int i);

    boolean query11vRoamingNetwork(String str, int i);

    boolean setAbsBlacklist(String str, String str2);

    boolean setAbsCapability(String str, int i);

    boolean setFilterEnable(String str, boolean z);

    boolean setTxPower(String str, int i);

    boolean trySetupForVendorV3_0(String str, ISupplicantIface iSupplicantIface, SupplicantStaIfaceHal.SupplicantStaIfaceHalCallback supplicantStaIfaceHalCallback);

    String voWifiDetect(String str, String str2);
}
