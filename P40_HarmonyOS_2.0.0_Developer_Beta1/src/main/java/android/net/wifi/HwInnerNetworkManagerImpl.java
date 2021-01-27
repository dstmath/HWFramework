package android.net.wifi;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.android.net.wifi.HwHiLogEx;
import com.huawei.android.net.wifi.HwWifiAdapterEx;
import java.util.List;

public class HwInnerNetworkManagerImpl implements HwInnerNetworkManager {
    private static final int CODE_CLOSE_SOCKETS_FOR_UID = 1107;
    private static final int CODE_GET_APLINKED_STA_LIST = 1005;
    private static final int CODE_GET_WIFI_DNS_STAT = 1011;
    private static final int CODE_SET_AP_CONFIGRATION_HW = 1008;
    private static final int CODE_SET_SOFTAP_DISASSOCIATESTA = 1007;
    private static final int CODE_SET_SOFTAP_MACFILTER = 1006;
    private static final int CODE_SET_SOFTAP_TX_POWER = 1009;
    private static final int CODE_SET_WIFIPRO_FIREWALL_DROP = 1014;
    private static final int CODE_SET_WIFIPRO_FIREWALL_ENABLE = 1012;
    private static final int CODE_SET_WIFIPRO_FIREWALL_WHITELIST = 1013;
    private static final boolean DBG = false;
    private static final String DESCRIPTOR = "android.net.wifi.INetworkManager";
    private static final String DESCRIPTOR_NETWORKMANAGEMENT_SERVICE = "android.os.INetworkManagementService";
    private static final String TAG = "HwInnerNetworkManagerImpl";
    private static HwInnerNetworkManager mHwInnerNetworkManager = null;

    public static HwInnerNetworkManager getDefault() {
        if (mHwInnerNetworkManager == null) {
            mHwInnerNetworkManager = new HwInnerNetworkManagerImpl();
        }
        return mHwInnerNetworkManager;
    }

    public List<String> getApLinkedStaList() {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("network_management");
        HwHiLogEx.d(TAG, false, "HwInnerNetworkManagerImpl getApLinkedStaList", new Object[0]);
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                b.transact(CODE_GET_APLINKED_STA_LIST, _data, _reply, 0);
                _reply.readException();
                return _reply.createStringArrayList();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
            } finally {
                _reply.recycle();
                _data.recycle();
            }
        } else {
            _reply.recycle();
            _data.recycle();
            return null;
        }
    }

    public void setSoftapMacFilter(String macFilter) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("network_management");
        HwHiLogEx.d(TAG, false, "HwInnerNetworkManagerImpl setSoftapMacFilter macFilter = %{private}s", new Object[]{macFilter});
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                _data.writeString(macFilter);
                b.transact(CODE_SET_SOFTAP_MACFILTER, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
                _data.recycle();
                _reply.recycle();
                return;
            } catch (Throwable th) {
                _data.recycle();
                _reply.recycle();
                throw th;
            }
        }
        _data.recycle();
        _reply.recycle();
    }

    public void setSoftapDisassociateSta(String mac) {
        Parcel _reply = Parcel.obtain();
        Parcel _data = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("network_management");
        HwHiLogEx.d(TAG, false, "setSoftapDisassociateSta", new Object[0]);
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                _data.writeString(mac);
                HwHiLogEx.d(TAG, false, "setSoftapDisassociateSta, mac =%{private}s", new Object[]{mac});
                b.transact(CODE_SET_SOFTAP_DISASSOCIATESTA, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
                _reply.recycle();
                _data.recycle();
                return;
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
    }

    public void setAccessPointHw(String wlanIface, String softapIface) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("network_management");
        HwHiLogEx.d(TAG, false, "setWifiApConfigurationHw", new Object[0]);
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                _data.writeString(wlanIface);
                _data.writeString(softapIface);
                b.transact(CODE_SET_AP_CONFIGRATION_HW, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
                _reply.recycle();
                _data.recycle();
                return;
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
    }

    public void setWifiTxPower(String reduceCmd) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("network_management");
        HwHiLogEx.d(TAG, false, "setSoftapDisassociateSta", new Object[0]);
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                _data.writeString(reduceCmd);
                HwHiLogEx.d(TAG, false, "setWifiTxPower, reduceCmd = %{public}s", new Object[]{reduceCmd});
                b.transact(CODE_SET_SOFTAP_TX_POWER, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
                _reply.recycle();
                _data.recycle();
                return;
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
    }

    public String getWiFiDnsStats(int netid) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("network_management");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                _data.writeInt(netid);
                b.transact(CODE_GET_WIFI_DNS_STAT, _data, _reply, 0);
                _reply.readException();
                return _reply.readString();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
            } finally {
                _reply.recycle();
                _data.recycle();
            }
        } else {
            _reply.recycle();
            _data.recycle();
            return "";
        }
    }

    public void closeSocketsForUid(int uid) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("network_management");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR_NETWORKMANAGEMENT_SERVICE);
                _data.writeInt(uid);
                b.transact(CODE_CLOSE_SOCKETS_FOR_UID, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
                _reply.recycle();
                _data.recycle();
                return;
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
    }

    public void setWifiproFirewallEnable(boolean enabled) {
        int i = 0;
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("network_management");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                if (enabled) {
                    i = 1;
                }
                _data.writeInt(i);
                b.transact(CODE_SET_WIFIPRO_FIREWALL_ENABLE, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
                _reply.recycle();
                _data.recycle();
                return;
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
    }

    public void setWifiproFirewallWhitelist(int appUid) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("network_management");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                _data.writeInt(appUid);
                b.transact(CODE_SET_WIFIPRO_FIREWALL_WHITELIST, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
                _reply.recycle();
                _data.recycle();
                return;
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
    }

    public void setWifiproFirewallDrop() {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("network_management");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                b.transact(CODE_SET_WIFIPRO_FIREWALL_DROP, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
                _reply.recycle();
                _data.recycle();
                return;
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
    }
}
