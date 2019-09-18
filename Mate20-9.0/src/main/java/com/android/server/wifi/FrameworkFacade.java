package com.android.server.wifi;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.TrafficStats;
import android.net.Uri;
import android.net.ip.IpClient;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.telephony.CarrierConfigManager;
import com.android.internal.app.IBatteryStats;
import com.android.server.wifi.util.WifiAsyncChannel;

public class FrameworkFacade {
    public static final String TAG = "FrameworkFacade";

    public boolean setIntegerSetting(Context context, String name, int def) {
        return Settings.Global.putInt(context.getContentResolver(), name, def);
    }

    public int getIntegerSetting(Context context, String name, int def) {
        return Settings.Global.getInt(context.getContentResolver(), name, def);
    }

    public long getLongSetting(Context context, String name, long def) {
        return Settings.Global.getLong(context.getContentResolver(), name, def);
    }

    public boolean setStringSetting(Context context, String name, String def) {
        return Settings.Global.putString(context.getContentResolver(), name, def);
    }

    public String getStringSetting(Context context, String name) {
        return Settings.Global.getString(context.getContentResolver(), name);
    }

    public int getSecureIntegerSetting(Context context, String name, int def) {
        return Settings.Secure.getInt(context.getContentResolver(), name, def);
    }

    public void registerContentObserver(Context context, Uri uri, boolean notifyForDescendants, ContentObserver contentObserver) {
        context.getContentResolver().registerContentObserver(uri, notifyForDescendants, contentObserver);
    }

    public void unregisterContentObserver(Context context, ContentObserver contentObserver) {
        context.getContentResolver().unregisterContentObserver(contentObserver);
    }

    public IBinder getService(String serviceName) {
        return ServiceManager.getService(serviceName);
    }

    public IBatteryStats getBatteryService() {
        return IBatteryStats.Stub.asInterface(getService("batterystats"));
    }

    public PendingIntent getBroadcast(Context context, int requestCode, Intent intent, int flags) {
        return PendingIntent.getBroadcast(context, requestCode, intent, flags);
    }

    public PendingIntent getActivity(Context context, int requestCode, Intent intent, int flags) {
        return PendingIntent.getActivity(context, requestCode, intent, flags);
    }

    public SupplicantStateTracker makeSupplicantStateTracker(Context context, WifiConfigManager configManager, Handler handler) {
        return new SupplicantStateTracker(context, configManager, this, handler);
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

    public IpClient makeIpClient(Context context, String iface, IpClient.Callback callback) {
        return new IpClient(context, iface, callback);
    }

    public int checkUidPermission(String permName, int uid) throws RemoteException {
        return AppGlobals.getPackageManager().checkUidPermission(permName, uid);
    }

    public WifiAsyncChannel makeWifiAsyncChannel(String tag) {
        return new WifiAsyncChannel(tag);
    }

    public boolean inStorageManagerCryptKeeperBounce() {
        return StorageManager.inCryptKeeperBounce();
    }

    public boolean isAppForeground(int uid) throws RemoteException {
        return ActivityManager.getService().isAppForeground(uid);
    }

    public Notification.Builder makeNotificationBuilder(Context context, String channelId) {
        return new Notification.Builder(context, channelId);
    }
}
