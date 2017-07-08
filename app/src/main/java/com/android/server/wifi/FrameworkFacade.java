package com.android.server.wifi;

import android.app.AppGlobals;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.net.ip.IpManager;
import android.net.ip.IpManager.Callback;
import android.net.wifi.IWifiScanner.Stub;
import android.net.wifi.WifiScanner;
import android.os.Handler;
import android.os.IBinder;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.security.KeyStore;
import android.telephony.CarrierConfigManager;
import com.android.server.wifi.SoftApManager.Listener;
import java.util.ArrayList;

public class FrameworkFacade {
    public static final String TAG = "FrameworkFacade";

    public BaseWifiLogger makeBaseLogger() {
        return new BaseWifiLogger();
    }

    public BaseWifiLogger makeRealLogger(WifiStateMachine stateMachine, WifiNative wifiNative, BuildProperties buildProperties) {
        return new WifiLogger(stateMachine, wifiNative, buildProperties);
    }

    public boolean setIntegerSetting(Context context, String name, int def) {
        return Global.putInt(context.getContentResolver(), name, def);
    }

    public int getIntegerSetting(Context context, String name, int def) {
        return Global.getInt(context.getContentResolver(), name, def);
    }

    public long getLongSetting(Context context, String name, long def) {
        return Global.getLong(context.getContentResolver(), name, def);
    }

    public boolean setStringSetting(Context context, String name, String def) {
        return Global.putString(context.getContentResolver(), name, def);
    }

    public String getStringSetting(Context context, String name) {
        return Global.getString(context.getContentResolver(), name);
    }

    public IBinder getService(String serviceName) {
        return ServiceManager.getService(serviceName);
    }

    public WifiScanner makeWifiScanner(Context context, Looper looper) {
        return new WifiScanner(context, Stub.asInterface(getService("wifiscanner")), looper);
    }

    public PendingIntent getBroadcast(Context context, int requestCode, Intent intent, int flags) {
        return PendingIntent.getBroadcast(context, requestCode, intent, flags);
    }

    public SupplicantStateTracker makeSupplicantStateTracker(Context context, WifiConfigManager configManager, Handler handler) {
        return new SupplicantStateTracker(context, configManager, handler);
    }

    public boolean getConfigWiFiDisableInECBM(Context context) {
        CarrierConfigManager configManager = (CarrierConfigManager) context.getSystemService("carrier_config");
        if (configManager != null) {
            return configManager.getConfig().getBoolean("config_wifi_disable_in_ecbm");
        }
        return true;
    }

    public WifiApConfigStore makeApConfigStore(Context context, BackupManagerProxy backupManagerProxy) {
        return new WifiApConfigStore(context, backupManagerProxy);
    }

    public long getTxPackets(String iface) {
        return TrafficStats.getTxPackets(iface);
    }

    public long getRxPackets(String iface) {
        return TrafficStats.getRxPackets(iface);
    }

    public IpManager makeIpManager(Context context, String iface, Callback callback) {
        return new IpManager(context, iface, callback);
    }

    public SoftApManager makeSoftApManager(Context context, Looper looper, WifiNative wifiNative, INetworkManagementService nmService, ConnectivityManager cm, String countryCode, ArrayList<Integer> allowed2GChannels, Listener listener) {
        return HwWifiServiceFactory.getHwWifiServiceManager().createHwSoftApManager(context, looper, wifiNative, nmService, cm, countryCode, allowed2GChannels, listener);
    }

    public int checkUidPermission(String permName, int uid) throws RemoteException {
        return AppGlobals.getPackageManager().checkUidPermission(permName, uid);
    }

    public WifiConfigManager makeWifiConfigManager(Context context, WifiNative wifiNative, FrameworkFacade frameworkFacade, Clock clock, UserManager userManager, KeyStore keyStore) {
        return HwWifiServiceFactory.getHwWifiServiceManager().createHwWifiConfigManager(context, wifiNative, frameworkFacade, clock, userManager, keyStore);
    }
}
