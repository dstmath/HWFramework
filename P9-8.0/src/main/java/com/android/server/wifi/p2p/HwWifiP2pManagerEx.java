package com.android.server.wifi.p2p;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManagerUtils;
import android.os.Bundle;
import android.os.Message;
import com.huawei.utils.reflect.EasyInvokeFactory;

public class HwWifiP2pManagerEx {
    public static final int ADD_P2P_VALID_DEVICE = 141264;
    private static final int BASE = 139264;
    public static final int BEAM_CONNECT = 141267;
    public static final int CLEAR_P2P_VALID_DEVICE = 141266;
    public static final int CREATE_GROUP_PSK = 141268;
    public static final String EXTRA_BSSID = "bssid";
    public static final String EXTRA_FREQUENCY = "freq";
    public static final String EXTRA_INTERFACE_NAME = "p2pInterfaceName";
    public static final String EXTRA_P2P_CONFIG_INFO = "p2pconfigInfo";
    public static final String EXTRA_WIFI_P2P_CONNECT_STATE = "extraState";
    public static final String GROUP_CREATED_ACTION = "android.net.wifi.p2p.GROUP_CREATED";
    public static final String MACICLINK_INTERFACE_CREATED_ACTION = "android.net.wifi.p2p.INTERFACE_CREATED";
    public static final int MAGICLINK_CONNECT = 141269;
    public static final int MAGICLINK_CREATE_GROUP = 141270;
    public static final String MAGICLINK_P2P_CONFIG_INFO = "android.net.wifi.p2p.CONFIG_INFO";
    public static final String MAGICLINK_PERMISSION = "com.huawei.instantshare.permission.ACCESS_INSTANTSHARE";
    public static final int MAGICLINK_REMOVE_GC_GROUP = 141271;
    public static final String NETWORK_CONNECTED_ACTION = "android.net.wifi.p2p.NETWORK_CONNECTED_ACTION";
    public static final String NETWORK_DISCONNECTED_ACTION = "android.net.wifi.p2p.NETWORK_DISCONNECTED_ACTION";
    public static final int REMOVE_P2P_VALID_DEVICE = 141265;
    public static final String STA_FREQUENCY_CREATED_ACTION = "android.net.wifi.p2p.STA_FREQUENCY_CREATED";
    public static final String WIFI_P2P_CONNECT_STATE_CHANGED_ACTION = "android.net.wifi.p2p.CONNECT_STATE_CHANGE";
    public static final int WIFI_P2P_STATE_CONNECTED = 2;
    public static final int WIFI_P2P_STATE_CONNECTIING = 1;
    public static final int WIFI_P2P_STATE_CONNECTION_FAIL = 3;
    public static final String WIFI_P2P_VALID_DEVICE = "avlidDevice";
    private static HwWifiP2pManagerEx mInstance = new HwWifiP2pManagerEx();
    private static WifiP2pManagerUtils wifiP2pManagerUtils = ((WifiP2pManagerUtils) EasyInvokeFactory.getInvokeUtils(WifiP2pManagerUtils.class));

    public static HwWifiP2pManagerEx getDefault() {
        return mInstance;
    }

    public void addP2PValidDevice(Channel c, String deviceAddress, ActionListener listener) {
        checkChannel(c);
        Message msg = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putString(WIFI_P2P_VALID_DEVICE, deviceAddress);
        msg.what = ADD_P2P_VALID_DEVICE;
        msg.setData(bundle);
        c.getAsyncChannel().sendMessage(msg);
    }

    private void checkChannel(Channel c) {
        if (c == null) {
            throw new IllegalArgumentException("Channel needs to be initialized");
        }
    }

    public void removeP2PValidDevice(Channel c, String deviceAddress, ActionListener listener) {
        checkChannel(c);
        Message msg = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putString(WIFI_P2P_VALID_DEVICE, deviceAddress);
        msg.what = REMOVE_P2P_VALID_DEVICE;
        msg.setData(bundle);
        c.getAsyncChannel().sendMessage(msg);
    }

    public void clearP2PValidDevice(Channel c, ActionListener listener) {
        checkChannel(c);
        Message msg = Message.obtain();
        msg.what = CLEAR_P2P_VALID_DEVICE;
        c.getAsyncChannel().sendMessage(msg);
    }

    public void beam_connect(Channel c, WifiP2pConfig config, ActionListener listener) {
        checkChannel(c);
        c.getAsyncChannel().sendMessage(BEAM_CONNECT, 0, wifiP2pManagerUtils.putListener(c, listener), config);
    }

    public void createGroupWifiRepeater(Channel c, WifiConfiguration wifiConfig, ActionListener listener) {
        checkChannel(c);
        c.getAsyncChannel().sendMessage(CREATE_GROUP_PSK, 0, wifiP2pManagerUtils.putListener(c, listener), wifiConfig);
    }

    public void magiclinkConnect(Channel c, String config, ActionListener listener) {
        checkChannel(c);
        Bundle bd = new Bundle();
        bd.putString("cfg", config);
        c.getAsyncChannel().sendMessage(MAGICLINK_CONNECT, 0, wifiP2pManagerUtils.putListener(c, listener), bd);
    }

    public void magiclinkCreateGroup(Channel c, String frequency, ActionListener listener) {
        checkChannel(c);
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_FREQUENCY, frequency);
        c.getAsyncChannel().sendMessage(MAGICLINK_CREATE_GROUP, -2, wifiP2pManagerUtils.putListener(c, listener), bundle);
    }

    public void magiclinkRemoveGcGroup(Channel c, String iface, ActionListener listener) {
        checkChannel(c);
        Bundle bundle = new Bundle();
        bundle.putString("iface", iface);
        c.getAsyncChannel().sendMessage(MAGICLINK_REMOVE_GC_GROUP, 0, wifiP2pManagerUtils.putListener(c, listener), bundle);
    }
}
