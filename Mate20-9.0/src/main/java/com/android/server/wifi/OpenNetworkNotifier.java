package com.android.server.wifi;

import android.content.Context;
import android.os.Looper;

public class OpenNetworkNotifier extends AvailableNetworkNotifier {
    private static final String STORE_DATA_IDENTIFIER = "OpenNetworkNotifierBlacklist";
    public static final String TAG = "WifiOpenNetworkNotifier";
    private static final String TOGGLE_SETTINGS_NAME = "wifi_networks_available_notification_on";

    public OpenNetworkNotifier(Context context, Looper looper, FrameworkFacade framework, Clock clock, WifiMetrics wifiMetrics, WifiConfigManager wifiConfigManager, WifiConfigStore wifiConfigStore, WifiStateMachine wifiStateMachine, ConnectToNetworkNotificationBuilder connectToNetworkNotificationBuilder) {
        super(TAG, STORE_DATA_IDENTIFIER, TOGGLE_SETTINGS_NAME, 17303299, context, looper, framework, clock, wifiMetrics, wifiConfigManager, wifiConfigStore, wifiStateMachine, connectToNetworkNotificationBuilder);
    }
}
