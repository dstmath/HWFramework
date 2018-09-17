package com.android.server;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.net.INetworkPolicyManager;
import android.net.INetworkStatsService;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.INetworkManagementService;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.connectivity.NetworkMonitor;
import com.android.server.connectivity.Tethering;

public interface HwConnectivityManager {
    void captivePortalCheckCompleted(Context context, boolean z);

    boolean checkDunExisted(Context context);

    ConnectivityService createHwConnectivityService(Context context, INetworkManagementService iNetworkManagementService, INetworkStatsService iNetworkStatsService, INetworkPolicyManager iNetworkPolicyManager);

    NetworkMonitor createHwNetworkMonitor(Context context, Handler handler, NetworkAgentInfo networkAgentInfo, NetworkRequest networkRequest);

    Network getNetworkForTypeWifi();

    boolean isInsecureVpnDisabled();

    boolean isP2pTether(String str);

    boolean isVpnDisabled();

    boolean needCaptivePortalCheck(NetworkAgentInfo networkAgentInfo, Context context);

    void onDnsEvent(Context context, int i, int i2);

    void setPushServicePowerNormalMode();

    boolean setPushServicePowerSaveMode(NetworkInfo networkInfo);

    void setTetheringProp(Tethering tethering, boolean z, boolean z2, String str);

    boolean setUsbFunctionForTethering(Context context, UsbManager usbManager, boolean z);

    void startBrowserOnClickNotification(Context context, String str);

    void stopP2pTether(Context context);
}
