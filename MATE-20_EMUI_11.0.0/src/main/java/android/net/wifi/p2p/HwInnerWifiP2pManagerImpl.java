package android.net.wifi.p2p;

import android.net.wifi.WifiConfiguration;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.android.net.wifi.HwHiLogEx;
import com.huawei.android.net.wifi.HwWifiAdapterEx;

public class HwInnerWifiP2pManagerImpl implements HwInnerWifiP2pManager {
    private static final int CODE_DISABLE_P2P_GC_DHCP = 1006;
    private static final int CODE_GET_GROUP_CONFIG_INFO = 1005;
    private static final int CODE_GET_WIFI_REPEATER_CONFIG = 1001;
    private static final int CODE_SET_WIFI_REPEATER_CONFIG = 1002;
    private static final int CODE_WIFI_MAGICLINK_CONFIG_IP = 1003;
    private static final int CODE_WIFI_MAGICLINK_RELEAGE_IP = 1004;
    private static final String DESCRIPTOR = "android.net.wifi.p2p.IWifiP2pManager";
    private static final String TAG = "HwInnerWifiP2pManagerImpl";
    private static HwInnerWifiP2pManager mInstance = new HwInnerWifiP2pManagerImpl();

    public static HwInnerWifiP2pManager getDefault() {
        return mInstance;
    }

    public WifiConfiguration getWifiRepeaterConfiguration() {
        return HwWifiAdapterEx.getWifiRepeaterConfiguration(DESCRIPTOR, (int) CODE_GET_WIFI_REPEATER_CONFIG);
    }

    public String getGroupConfigInfo() {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        String _result = "";
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifip2p");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                b.transact(CODE_GET_GROUP_CONFIG_INFO, _data, _reply, 0);
                _reply.readException();
                if (1 == _reply.readInt()) {
                    _result = _reply.readString();
                } else {
                    _result = "";
                }
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
                _reply.recycle();
                _data.recycle();
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
        return _result;
    }

    public boolean setWifiRepeaterConfiguration(WifiConfiguration config) {
        boolean result = true;
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifip2p");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                if (config != null) {
                    _data.writeInt(1);
                    config.writeToParcel(_data, 0);
                } else {
                    _data.writeInt(0);
                }
                b.transact(CODE_SET_WIFI_REPEATER_CONFIG, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
                _data.recycle();
                _reply.recycle();
                return false;
            } catch (Throwable th) {
                _data.recycle();
                _reply.recycle();
                return false;
            }
        } else {
            result = false;
        }
        _data.recycle();
        _reply.recycle();
        return result;
    }

    public boolean releaseIPAddr(String ifName) {
        boolean result = true;
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifip2p");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                if (ifName != null) {
                    _data.writeInt(1);
                    _data.writeString(ifName);
                } else {
                    _data.writeInt(0);
                }
                b.transact(CODE_WIFI_MAGICLINK_RELEAGE_IP, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException remoteException) {
                remoteException.printStackTrace();
                _reply.recycle();
                _data.recycle();
                return false;
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                return false;
            }
        } else {
            result = false;
        }
        _reply.recycle();
        _data.recycle();
        return result;
    }

    public boolean configIPAddr(String ifName, String ipAddr, String server) {
        boolean result = true;
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = HwWifiAdapterEx.getWifiserviceBinder("wifip2p");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                if (ifName != null) {
                    _data.writeInt(3);
                    _data.writeString(ifName);
                    _data.writeString(ipAddr);
                    _data.writeString(server);
                } else {
                    _data.writeInt(0);
                }
                b.transact(CODE_WIFI_MAGICLINK_CONFIG_IP, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
                _reply.recycle();
                _data.recycle();
                return false;
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                return false;
            }
        } else {
            result = false;
        }
        _reply.recycle();
        _data.recycle();
        return result;
    }

    public boolean disableP2pGcDhcp(String tag, int type) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        IBinder binder = HwWifiAdapterEx.getWifiserviceBinder("wifip2p");
        boolean isSuccess = false;
        if (binder != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                data.writeString(tag);
                data.writeInt(type);
                binder.transact(CODE_DISABLE_P2P_GC_DHCP, data, reply, 0);
                reply.readException();
                boolean[] resultArray = new boolean[1];
                reply.readBooleanArray(resultArray);
                isSuccess = resultArray[0];
            } catch (RemoteException e) {
                HwHiLogEx.e(TAG, false, "Exceptions happen when disableP2pGcDhcp", new Object[0]);
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
        return isSuccess;
    }
}
