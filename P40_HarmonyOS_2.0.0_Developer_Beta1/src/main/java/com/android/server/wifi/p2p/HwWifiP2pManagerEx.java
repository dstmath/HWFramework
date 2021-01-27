package com.android.server.wifi.p2p;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManagerUtils;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import com.huawei.android.net.wifi.HwHiLogEx;
import com.huawei.android.net.wifi.HwWifiAdapterEx;
import com.huawei.utils.reflect.EasyInvokeFactory;
import java.lang.reflect.Field;

public class HwWifiP2pManagerEx {
    public static final int ADD_P2P_VALID_DEVICE = 141264;
    public static final int BASE = 139264;
    public static final int BEAM_CONNECT = 141267;
    public static final int CLEAR_P2P_VALID_DEVICE = 141266;
    private static final int CODE_MAGICLINK_APPLY_IP = 1009;
    private static final int CODE_NOTIFY_P2P_BINDER_ADD = 1124;
    private static final int CODE_NOTIFY_P2P_BINDER_REMOVE = 1125;
    public static final int CREATE_GROUP_PSK = 141268;
    private static final String DESCRIPTOR = "android.net.wifi.p2p.IWifiP2pManager";
    public static final int DISABLE_P2P_RANDOM_MAC = 141272;
    public static final String EXTRA_BSSID = "bssid";
    public static final String EXTRA_FREQUENCY = "freq";
    public static final String EXTRA_INTERFACE_NAME = "p2pInterfaceName";
    public static final String EXTRA_LINKSPEED = "linkSpeed";
    public static final String EXTRA_P2P_CONFIG_INFO = "p2pconfigInfo";
    public static final String EXTRA_WFD_INFO = "exinfo";
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
    public static final int SET_HWSINKCONFIG = 141273;
    public static final int SHARELINK_APPLY_P2P_REUSE = 141274;
    public static final int SHARELINK_REMOVE_GROUP = 141275;
    public static final String STA_FREQUENCY_CREATED_ACTION = "android.net.wifi.p2p.STA_FREQUENCY_CREATED";
    private static final String TAG = "HwWifiP2pManagerEx";
    public static final String WFD_HW_DEVICE_EX_INFO = "com.huawei.net.wifi.p2p.peers.hw.extend.info";
    public static final String WFD_LINKSPEED_INFO = "com.huawei.net.wifi.p2p.LINK_SPEED";
    public static final String WFD_PERMISSION = "com.huawei.wfd.permission.ACCESS_HW_P2P_WFD";
    public static final String WIFI_P2P_CONNECT_STATE_CHANGED_ACTION = "android.net.wifi.p2p.CONNECT_STATE_CHANGE";
    public static final int WIFI_P2P_STATE_CONNECTED = 2;
    public static final int WIFI_P2P_STATE_CONNECTIING = 1;
    public static final int WIFI_P2P_STATE_CONNECTION_FAIL = 3;
    public static final String WIFI_P2P_VALID_DEVICE = "avlidDevice";
    private static HwWifiP2pManagerEx mInstance = new HwWifiP2pManagerEx();
    private static WifiP2pManagerUtils wifiP2pManagerUtils = EasyInvokeFactory.getInvokeUtils(WifiP2pManagerUtils.class);

    public static HwWifiP2pManagerEx getDefault() {
        return mInstance;
    }

    public void addP2PValidDevice(WifiP2pManager.Channel c, String deviceAddress, WifiP2pManager.ActionListener listener) {
        checkChannel(c);
        Message msg = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putString("avlidDevice", deviceAddress);
        msg.what = ADD_P2P_VALID_DEVICE;
        msg.setData(bundle);
    }

    private void checkChannel(WifiP2pManager.Channel c) {
        if (c == null) {
            throw new IllegalArgumentException("Channel needs to be initialized");
        }
    }

    public void removeP2PValidDevice(WifiP2pManager.Channel c, String deviceAddress, WifiP2pManager.ActionListener listener) {
        checkChannel(c);
        Message msg = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putString("avlidDevice", deviceAddress);
        msg.what = REMOVE_P2P_VALID_DEVICE;
        msg.setData(bundle);
    }

    public void clearP2PValidDevice(WifiP2pManager.Channel c, WifiP2pManager.ActionListener listener) {
        checkChannel(c);
        Message.obtain().what = CLEAR_P2P_VALID_DEVICE;
    }

    public void beam_connect(WifiP2pManager.Channel c, WifiP2pConfig config, WifiP2pManager.ActionListener listener) {
        sendMessageToChanel(c, BEAM_CONNECT, 0, wifiP2pManagerUtils.putListener(c, listener), config);
    }

    public void createGroupWifiRepeater(WifiP2pManager.Channel channel, WifiConfiguration wifiConfig, WifiP2pManager.ActionListener listener) {
        HwHiLogEx.d(TAG, false, "createGroupWifiRepeater is called", new Object[0]);
        try {
            Field field = WifiP2pManager.Channel.class.getDeclaredField("mBinder");
            field.setAccessible(true);
            if (field.get(channel) instanceof Binder) {
                addBinderMap((Binder) field.get(channel));
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            HwHiLogEx.e(TAG, false, "createGroupWifiRepeater, exception happens", new Object[0]);
        }
        sendMessageToChanel(channel, CREATE_GROUP_PSK, 0, wifiP2pManagerUtils.putListener(channel, listener), wifiConfig);
    }

    private void addBinderMap(Binder channelBinder) {
        HwHiLogEx.d(TAG, false, "addBinderMap is called binder=" + channelBinder, new Object[0]);
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder binder = HwWifiAdapterEx.getWifiserviceBinder("wifip2p");
        if (binder != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                data.writeStrongBinder(channelBinder);
                binder.transact(CODE_NOTIFY_P2P_BINDER_ADD, data, reply, 0);
                reply.readException();
            } catch (RemoteException e) {
                HwHiLogEx.e(TAG, false, "addBinderMap, localRemoteException", new Object[0]);
                reply.recycle();
                data.recycle();
                return;
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
    }

    private void removeBinderMap(Binder channelBinder) {
        HwHiLogEx.d(TAG, false, "removeBinderMap is called binder=" + channelBinder, new Object[0]);
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder binder = HwWifiAdapterEx.getWifiserviceBinder("wifip2p");
        if (binder != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                data.writeStrongBinder(channelBinder);
                binder.transact(CODE_NOTIFY_P2P_BINDER_REMOVE, data, reply, 0);
                reply.readException();
            } catch (RemoteException e) {
                HwHiLogEx.e(TAG, false, "removeBinderMap, localRemoteException", new Object[0]);
                reply.recycle();
                data.recycle();
                return;
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
    }

    public void magiclinkConnect(WifiP2pManager.Channel channel, String config, WifiP2pManager.ActionListener listener) {
        HwHiLogEx.d(TAG, false, "enter magiclinkConnect", new Object[0]);
        try {
            Field field = WifiP2pManager.Channel.class.getDeclaredField("mBinder");
            field.setAccessible(true);
            if (field.get(channel) instanceof Binder) {
                addBinderMap((Binder) field.get(channel));
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            HwHiLogEx.e(TAG, false, "magiclinkConnect, exception happens", new Object[0]);
        }
        Bundle bd = new Bundle();
        bd.putString("cfg", config);
        sendMessageToChanel(channel, MAGICLINK_CONNECT, 0, wifiP2pManagerUtils.putListener(channel, listener), bd);
    }

    public void magiclinkCreateGroup(WifiP2pManager.Channel channel, String frequency, WifiP2pManager.ActionListener listener) {
        HwHiLogEx.d(TAG, false, "magiclinkCreateGroup is called", new Object[0]);
        try {
            Field field = WifiP2pManager.Channel.class.getDeclaredField("mBinder");
            field.setAccessible(true);
            if (field.get(channel) instanceof Binder) {
                addBinderMap((Binder) field.get(channel));
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            HwHiLogEx.e(TAG, false, "magiclinkCreateGroup, exception happens", new Object[0]);
        }
        HwHiLogEx.d(TAG, false, "enter magiclinkCreateGroup temporary", new Object[0]);
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_FREQUENCY, frequency);
        sendMessageToChanel(channel, MAGICLINK_CREATE_GROUP, -1, wifiP2pManagerUtils.putListener(channel, listener), bundle);
    }

    public void magiclinkRemoveGcGroup(WifiP2pManager.Channel channel, String iface, WifiP2pManager.ActionListener listener) {
        HwHiLogEx.d(TAG, false, "enter magiclinkRemoveGcGroup", new Object[0]);
        try {
            Field field = WifiP2pManager.Channel.class.getDeclaredField("mBinder");
            field.setAccessible(true);
            if (field.get(channel) instanceof Binder) {
                removeBinderMap((Binder) field.get(channel));
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            HwHiLogEx.e(TAG, false, "magiclinkRemoveGcGroup, exception happens", new Object[0]);
        }
        Bundle bundle = new Bundle();
        bundle.putString("iface", iface);
        sendMessageToChanel(channel, MAGICLINK_REMOVE_GC_GROUP, 0, wifiP2pManagerUtils.putListener(channel, listener), bundle);
    }

    public void sharelinkReuse(Context context, WifiP2pManager.Channel channel, WifiP2pManager.ActionListener listener) {
        HwHiLogEx.d(TAG, false, "enter sharelinkReuse", new Object[0]);
        try {
            Field field = WifiP2pManager.Channel.class.getDeclaredField("mBinder");
            field.setAccessible(true);
            if (field.get(channel) instanceof Binder) {
                addBinderMap((Binder) field.get(channel));
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            HwHiLogEx.e(TAG, false, "magiclinkConnect, exception happens", new Object[0]);
        }
        if (context == null) {
            HwHiLogEx.d(TAG, false, "Context is null.", new Object[0]);
            if (listener != null) {
                listener.onFailure(0);
                return;
            }
            return;
        }
        checkChannel(channel);
        String packageName = context.getPackageName();
        Bundle bundle = new Bundle();
        bundle.putString("pkg", packageName);
        sendMessageToChanel(channel, SHARELINK_APPLY_P2P_REUSE, 0, wifiP2pManagerUtils.putListener(channel, listener), bundle);
    }

    public void sharelinkRemoveGroup(WifiP2pManager.Channel channel, String groupInfo, WifiP2pManager.ActionListener listener) {
        HwHiLogEx.d(TAG, false, "enter sharelinkRemoveGroup", new Object[0]);
        try {
            Field field = WifiP2pManager.Channel.class.getDeclaredField("mBinder");
            field.setAccessible(true);
            if (field.get(channel) instanceof Binder) {
                removeBinderMap((Binder) field.get(channel));
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            HwHiLogEx.e(TAG, false, "sharelinkRemoveGroup, exception happens", new Object[0]);
        }
        checkChannel(channel);
        Bundle bundle = new Bundle();
        bundle.putString("info", groupInfo);
        sendMessageToChanel(channel, SHARELINK_REMOVE_GROUP, 0, wifiP2pManagerUtils.putListener(channel, listener), bundle);
    }

    public String applyMagiclinkIp(String p2p0IfaceMac) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        String result = "";
        IBinder binder = HwWifiAdapterEx.getWifiserviceBinder("wifip2p");
        if (binder != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                if (p2p0IfaceMac != null) {
                    data.writeInt(1);
                    data.writeString(p2p0IfaceMac);
                } else {
                    data.writeInt(0);
                }
                binder.transact(CODE_MAGICLINK_APPLY_IP, data, reply, 0);
                reply.readException();
                if (reply.readInt() == 1) {
                    result = reply.readString();
                } else {
                    result = "";
                }
            } catch (RemoteException e) {
                HwHiLogEx.e(TAG, false, "Exception happened in applyMagiclinkIp", new Object[0]);
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
        return result;
    }

    public void discoverPeers(WifiP2pManager.Channel c, int channelId, WifiP2pManager.ActionListener listener) {
        HwHiLogEx.d(TAG, false, "discoverPeers, pid:%{public}d, tid:%{public}d, uid:%{public}d, channelId %{public}d", new Object[]{Integer.valueOf(Process.myPid()), Integer.valueOf(Process.myTid()), Integer.valueOf(Process.myUid()), Integer.valueOf(channelId)});
        sendMessageToChanel(c, 139265, channelId, wifiP2pManagerUtils.putListener(c, listener));
    }

    public void setHwSinkConfig(WifiP2pManager.Channel channel, String sinkConfig, WifiP2pManager.ActionListener listener) {
        HwHiLogEx.d(TAG, false, "setHwSinkConfig", new Object[0]);
        if (sinkConfig == null || listener == null || channel == null) {
            HwHiLogEx.d(TAG, false, "setHwSinkConfig invalid params.", new Object[0]);
            return;
        }
        String[] tokens = sinkConfig.split(",");
        if (tokens == null || tokens.length != 4) {
            HwHiLogEx.d(TAG, false, "invalid sinkconfig", new Object[0]);
            listener.onFailure(0);
        } else if (tokens[0].length() % 2 != 0) {
            listener.onFailure(0);
        } else {
            Bundle bundle = new Bundle();
            bundle.putString("sinkConfig", sinkConfig);
            sendMessageToChanel(channel, SET_HWSINKCONFIG, 0, wifiP2pManagerUtils.putListener(channel, listener), bundle);
            HwHiLogEx.d(TAG, false, "setHwSinkConfig End", new Object[0]);
        }
    }

    public void disableP2pRandomMac(WifiP2pManager.Channel channel) {
        sendMessageToChanel(channel, DISABLE_P2P_RANDOM_MAC, 0, 0);
    }

    private void sendMessageToChanel(WifiP2pManager.Channel c, int what, int arg1, int arg2, Object obj) {
        checkChannel(c);
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        msg.obj = obj;
        HwWifiAdapterEx.sendMessage(c, msg);
    }

    private void sendMessageToChanel(WifiP2pManager.Channel c, int what, int arg1, int arg2) {
        checkChannel(c);
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        HwWifiAdapterEx.sendMessage(c, msg);
    }
}
