package com.huawei.android.net.wifi;

import android.common.HwFrameworkFactory;
import android.net.wifi.PPPOEInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.p2p.HwInnerWifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class HwWifiAdapterEx {
    public static final int BASE_WIFI_P2P_MANAGER = 139264;
    public static final int DISCOVER_PEERS = 139265;
    public static final String NETWORKMANAGEMENT_SERVICE = "network_management";
    public static final int PERSISTENT_NET_ID = -2;
    private static final String TAG = "HwWifiAdapterEx";
    public static final int TEMPORARY_NET_ID = -1;

    private HwWifiAdapterEx() {
    }

    public static IBinder getWifiserviceBinder(String serviceName) {
        if ("wifi".equals(serviceName)) {
            return ServiceManager.getService(serviceName);
        }
        if ("network_management".equals(serviceName)) {
            return ServiceManager.getService(serviceName);
        }
        if ("wifip2p".equals(serviceName)) {
            return ServiceManager.getService(serviceName);
        }
        HwHiLogEx.e(TAG, false, "serviceName is unknown", new Object[0]);
        return null;
    }

    public static HwInnerWifiP2pManager getHwInnerWifiP2pManager() {
        return HwFrameworkFactory.getHwInnerWifiP2pManager();
    }

    public static boolean is5GHz(int freq) {
        return ScanResult.is5GHz(freq);
    }

    public static boolean getMeteredHint(WifiInfo wifiInfo) {
        if (wifiInfo != null) {
            return wifiInfo.getMeteredHint();
        }
        throw new IllegalArgumentException("Channel needs to be initialized");
    }

    public static String getSystemProperties(String properties) {
        return SystemProperties.get(properties);
    }

    public static boolean controlHidataOptimize(String descriptor, int transactId, String pkgName, int action, boolean enable) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        boolean result = false;
        try {
            data.writeInterfaceToken(descriptor);
            data.writeString(pkgName);
            data.writeInt(action);
            data.writeBoolean(enable);
            IBinder b = getWifiserviceBinder("wifi");
            if (b != null) {
                b.transact(transactId, data, reply, 0);
                reply.readException();
                result = reply.readBoolean();
            }
        } catch (RemoteException e) {
            HwHiLogEx.e(TAG, false, "controlHidataOptimize, localRemoteException", new Object[0]);
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
        return result;
    }

    public static PPPOEInfo getPPPOEInfo(String descriptor, int transactId) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        PPPOEInfo result = null;
        IBinder b = getWifiserviceBinder("wifi");
        if (b != null) {
            try {
                data.writeInterfaceToken(descriptor);
                b.transact(transactId, data, reply, 0);
                reply.readException();
                if (reply.readInt() != 0) {
                    result = (PPPOEInfo) PPPOEInfo.CREATOR.createFromParcel(reply);
                } else {
                    result = null;
                }
                reply.readException();
            } catch (RemoteException e) {
                HwHiLogEx.e(TAG, false, "getPPPOEInfo, localRemoteException", new Object[0]);
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
        return result;
    }

    public static WifiConfiguration getWifiRepeaterConfiguration(String descriptor, int transactId) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        WifiConfiguration result = null;
        IBinder b = getWifiserviceBinder("wifip2p");
        if (b != null) {
            try {
                data.writeInterfaceToken(descriptor);
                b.transact(transactId, data, reply, 0);
                reply.readException();
                if (reply.readInt() != 0) {
                    result = (WifiConfiguration) WifiConfiguration.CREATOR.createFromParcel(reply);
                } else {
                    result = null;
                }
                reply.readException();
            } catch (RemoteException e) {
                HwHiLogEx.e(TAG, false, "getWifiRepeaterConfiguration, localRemoteException", new Object[0]);
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
        return result;
    }

    public static void sendMessage(WifiP2pManager.Channel channel, Message message) {
        if (channel == null || message == null) {
            throw new IllegalArgumentException("Channel needs to be initialized");
        }
        channel.getAsyncChannel().sendMessage(message);
    }
}
