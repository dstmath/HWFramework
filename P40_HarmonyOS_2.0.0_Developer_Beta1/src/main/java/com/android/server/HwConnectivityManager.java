package com.android.server;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.net.INetworkPolicyManager;
import android.net.INetworkStatsService;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.INetworkManagementService;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.connectivity.Tethering;

public interface HwConnectivityManager {
    void captivePortalCheckCompleted(Context context, boolean z);

    boolean checkDunExisted(Context context);

    void clearIpCacheOfDnsEvent();

    ConnectivityService createHwConnectivityService(Context context, INetworkManagementService iNetworkManagementService, INetworkStatsService iNetworkStatsService, INetworkPolicyManager iNetworkPolicyManager);

    int getNetworkAgentInfoScore();

    Network getNetworkForTypeWifi();

    void informModemTetherStatusToChangeGRO(int i, String str);

    boolean isApIpv4AddressFixed();

    boolean isBypassPrivateDns(int i);

    boolean isInsecureVpnDisabled();

    boolean isP2pTether(String str);

    boolean isVpnDisabled();

    boolean needCaptivePortalCheck(NetworkAgentInfo networkAgentInfo, Context context);

    void onDnsEvent(Context context, Bundle bundle);

    void setApIpv4AddressFixed(boolean z);

    void setPushServicePowerNormalMode();

    boolean setPushServicePowerSaveMode(NetworkInfo networkInfo);

    void setTetheringProp(Tethering tethering, boolean z, boolean z2, String str);

    boolean setUsbFunctionForTethering(Context context, UsbManager usbManager, boolean z);

    void startBrowserOnClickNotification(Context context, String str);

    void stopP2pTether(Context context);
}
