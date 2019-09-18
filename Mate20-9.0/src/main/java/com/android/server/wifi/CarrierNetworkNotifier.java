package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Looper;
import com.android.server.wifi.util.ScanResultUtil;

public class CarrierNetworkNotifier extends AvailableNetworkNotifier {
    private static final String STORE_DATA_IDENTIFIER = "CarrierNetworkNotifierBlacklist";
    public static final String TAG = "WifiCarrierNetworkNotifier";
    private static final String TOGGLE_SETTINGS_NAME = "wifi_carrier_networks_available_notification_on";

    public CarrierNetworkNotifier(Context context, Looper looper, FrameworkFacade framework, Clock clock, WifiMetrics wifiMetrics, WifiConfigManager wifiConfigManager, WifiConfigStore wifiConfigStore, WifiStateMachine wifiStateMachine, ConnectToNetworkNotificationBuilder connectToNetworkNotificationBuilder) {
        super(TAG, STORE_DATA_IDENTIFIER, TOGGLE_SETTINGS_NAME, 46, context, looper, framework, clock, wifiMetrics, wifiConfigManager, wifiConfigStore, wifiStateMachine, connectToNetworkNotificationBuilder);
    }

    /* access modifiers changed from: package-private */
    public WifiConfiguration createRecommendedNetworkConfig(ScanResult recommendedNetwork) {
        WifiConfiguration network = ScanResultUtil.createNetworkFromScanResult(recommendedNetwork);
        int eapMethod = recommendedNetwork.carrierApEapType;
        if (eapMethod == 4 || eapMethod == 5 || eapMethod == 6) {
            network.allowedKeyManagement.set(2);
            network.allowedKeyManagement.set(3);
            network.enterpriseConfig = new WifiEnterpriseConfig();
            network.enterpriseConfig.setEapMethod(recommendedNetwork.carrierApEapType);
            network.enterpriseConfig.setIdentity("");
            network.enterpriseConfig.setAnonymousIdentity("");
        }
        return network;
    }
}
