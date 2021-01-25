package com.android.server;

public interface IHwBluetoothManagerServiceEx {
    void disableAirplaneModeChangingFlag(int i);

    void handleExternalMessageOfHandler(int i);

    boolean needAllowByUser(int i);

    boolean refuseDisableForMDM(boolean z);

    boolean refuseEnableForMDM();

    void reportBtChrBindTimeout();

    void reportBtChrThirdPartApkCalling(boolean z, String str);

    boolean sendMessageIfLastChangeNotFinish();

    void setAirplaneModeChangingFlag(boolean z, int i, boolean z2);

    void setLastMessageDisable();

    boolean shouldIgnoreExtraEnableMessage(boolean z, boolean z2);
}
