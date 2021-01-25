package com.huawei.android.net.wifi.p2p;

import android.content.Context;
import android.net.wifi.IWifiActionListener;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.server.wifi.p2p.HwWifiP2pManagerEx;
import com.huawei.android.net.wifi.HwHiLogEx;
import com.huawei.android.net.wifi.HwWifiAdapterEx;

public class WifiP2pManagerCommonEx {
    private static final int CODE_REQUEST_DFS_STATUS = 1007;
    private static final int CODE_UPDATE_DFS_STATUS = 1008;
    private static final String DESCRIPTOR = "android.net.wifi.p2p.IWifiP2pManager";
    public static final String EXTRA_WIFI_P2P_CONNECT_STATE = "extraState";
    public static final String EXTRA_WIFI_RPT_STATE = "wifi_rpt_state";
    private static final String TAG = "WifiP2pManagerCommonEx";
    public static final int WIFI_DISABLE_P2P_GC_DHCP_FOREVER = 2;
    public static final int WIFI_DISABLE_P2P_GC_DHCP_NONE = 0;
    public static final int WIFI_DISABLE_P2P_GC_DHCP_ONCE = 1;
    public static final String WIFI_P2P_CONNECT_STATE_CHANGED_ACTION = "android.net.wifi.p2p.CONNECT_STATE_CHANGE";
    public static final int WIFI_P2P_STATE_CONNECTED = 2;
    public static final int WIFI_P2P_STATE_CONNECTIING = 1;
    public static final int WIFI_P2P_STATE_CONNECTION_FAIL = 3;
    public static final String WIFI_P2P_VALID_DEVICE = "avlidDevice";
    public static final String WIFI_RPT_STATE_CHANGED_ACTION = "com.huawei.android.net.wifi.p2p.action.WIFI_RPT_STATE_CHANGED";
    public static final int WIFI_RPT_STATE_CREATE_GO_UNTETHERD = 6;
    public static final int WIFI_RPT_STATE_DISABLED = 0;
    public static final int WIFI_RPT_STATE_DISABLING = 2;
    public static final int WIFI_RPT_STATE_ENABLED = 1;
    public static final int WIFI_RPT_STATE_ENABLING = 3;
    public static final int WIFI_RPT_STATE_START_FAIL = 5;
    public static final int WIFI_RPT_STATE_STOP_FAIL = 4;
    public static final int WIFI_RPT_STATE_UNKNOWN = -1;

    public static void createGroupWifiRepeater(WifiP2pManager.Channel c, WifiConfiguration wifiConfig, WifiP2pManager.ActionListener listener) {
        HwWifiP2pManagerEx.getDefault().createGroupWifiRepeater(c, wifiConfig, listener);
    }

    public static WifiConfiguration getWifiRepeaterConfiguration() {
        return HwWifiAdapterEx.getHwInnerWifiP2pManager().getWifiRepeaterConfiguration();
    }

    public static String getGroupConfigInfo() {
        return HwWifiAdapterEx.getHwInnerWifiP2pManager().getGroupConfigInfo();
    }

    public static boolean setWifiRepeaterConfiguration(WifiConfiguration wifiConfig) {
        return HwWifiAdapterEx.getHwInnerWifiP2pManager().setWifiRepeaterConfiguration(wifiConfig);
    }

    public static void magiclinkConnect(WifiP2pManager.Channel c, String config, WifiP2pManager.ActionListener listener) {
        HwWifiP2pManagerEx.getDefault().magiclinkConnect(c, config, listener);
    }

    public static void magiclinkCreateGroup(WifiP2pManager.Channel c, String frequency, WifiP2pManager.ActionListener listener) {
        HwWifiP2pManagerEx.getDefault().magiclinkCreateGroup(c, frequency, listener);
    }

    public static void magiclinkRemoveGcGroup(WifiP2pManager.Channel c, String iface, WifiP2pManager.ActionListener listener) {
        HwWifiP2pManagerEx.getDefault().magiclinkRemoveGcGroup(c, iface, listener);
    }

    public static boolean releaseIPAddr(String ifName) {
        return HwWifiAdapterEx.getHwInnerWifiP2pManager().releaseIPAddr(ifName);
    }

    public static boolean configIPAddr(String ifName, String ipAddr, String server) {
        return HwWifiAdapterEx.getHwInnerWifiP2pManager().configIPAddr(ifName, ipAddr, server);
    }

    public static void sharelinkReuse(Context context, WifiP2pManager.Channel c, WifiP2pManager.ActionListener listener) {
        HwWifiP2pManagerEx.getDefault().sharelinkReuse(context, c, listener);
    }

    public static void sharelinkRemoveGroup(WifiP2pManager.Channel c, String groupInfo, WifiP2pManager.ActionListener listener) {
        HwWifiP2pManagerEx.getDefault().sharelinkRemoveGroup(c, groupInfo, listener);
    }

    public static String applyMagiclinkIp(String p2p0IfaceMac) {
        return HwWifiP2pManagerEx.getDefault().applyMagiclinkIp(p2p0IfaceMac);
    }

    public static void discoverPeers(WifiP2pManager.Channel c, int channelId, WifiP2pManager.ActionListener listener) {
        HwWifiP2pManagerEx.getDefault().discoverPeers(c, channelId, listener);
    }

    public static boolean disableP2pGcDhcp(String tag, int type) {
        return HwWifiAdapterEx.getHwInnerWifiP2pManager().disableP2pGcDhcp(tag, type);
    }

    public static void disableP2pRandomMac(WifiP2pManager.Channel channel) {
        HwWifiP2pManagerEx.getDefault().disableP2pRandomMac(channel);
    }

    public static void setHwSinkConfig(WifiP2pManager.Channel channel, String sinkConfig, WifiP2pManager.ActionListener listener) {
        HwWifiP2pManagerEx.getDefault().setHwSinkConfig(channel, sinkConfig, listener);
    }

    public static boolean requestDfsStatus(Context context, int frequency, int bandWidth, IWifiActionListener listener) {
        HwHiLogEx.i(TAG, false, "requestDfsStatus is called", new Object[0]);
        if (context == null) {
            HwHiLogEx.e(TAG, false, "requestDfsStatus: context is null", new Object[0]);
            return false;
        }
        String packageName = context.getPackageName();
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder binder = HwWifiAdapterEx.getWifiserviceBinder("wifip2p");
        int result = 0;
        if (binder != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                data.writeString(packageName);
                data.writeInt(frequency);
                data.writeInt(bandWidth);
                data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                binder.transact(CODE_REQUEST_DFS_STATUS, data, reply, 0);
                reply.readException();
                result = reply.readInt();
            } catch (RemoteException e) {
                HwHiLogEx.e(TAG, false, "requestDfsStatus, localRemoteException", new Object[0]);
                reply.recycle();
                data.recycle();
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
        if (result > 0) {
            return true;
        }
        return false;
    }

    public static boolean updateDfsStatus(Context context, int transferResult, int transferRate) {
        HwHiLogEx.i(TAG, false, "updateDfsStatus is called", new Object[0]);
        if (context == null) {
            HwHiLogEx.e(TAG, false, "updateDfsStatus: context is null", new Object[0]);
            return false;
        }
        String packageName = context.getPackageName();
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder binder = HwWifiAdapterEx.getWifiserviceBinder("wifip2p");
        int result = 0;
        if (binder != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                data.writeString(packageName);
                data.writeInt(transferResult);
                data.writeInt(transferRate);
                binder.transact(CODE_UPDATE_DFS_STATUS, data, reply, 0);
                reply.readException();
                result = reply.readInt();
            } catch (RemoteException e) {
                HwHiLogEx.e(TAG, false, "updateDfsStatus, localRemoteException", new Object[0]);
                reply.recycle();
                data.recycle();
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
        if (result > 0) {
            return true;
        }
        return false;
    }
}
