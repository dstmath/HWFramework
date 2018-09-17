package android.net.wifi.p2p;

import android.net.wifi.WifiConfiguration;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;

public class HwInnerWifiP2pManagerImpl implements HwInnerWifiP2pManager {
    private static final int CODE_GET_WIFI_REPEATER_CONFIG = 1001;
    private static final int CODE_SET_WIFI_REPEATER_CONFIG = 1002;
    private static final int CODE_WIFI_MAGICLINK_CONFIG_IP = 1003;
    private static final int CODE_WIFI_MAGICLINK_RELEAGE_IP = 1004;
    private static final String DESCRIPTOR = "android.net.wifi.p2p.IWifiP2pManager";
    private static HwInnerWifiP2pManager mInstance = new HwInnerWifiP2pManagerImpl();

    public static HwInnerWifiP2pManager getDefault() {
        return mInstance;
    }

    public WifiConfiguration getWifiRepeaterConfiguration() {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        WifiConfiguration _result = null;
        IBinder b = ServiceManager.getService("wifip2p");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                b.transact(CODE_GET_WIFI_REPEATER_CONFIG, _data, _reply, 0);
                _reply.readException();
                if (_reply.readInt() != 0) {
                    _result = (WifiConfiguration) WifiConfiguration.CREATOR.createFromParcel(_reply);
                } else {
                    _result = null;
                }
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
            } finally {
                _reply.recycle();
                _data.recycle();
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
        IBinder b = ServiceManager.getService("wifip2p");
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
        }
        result = false;
        _data.recycle();
        _reply.recycle();
        return result;
    }

    public boolean releaseIPAddr(String ifName) {
        boolean result = true;
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("wifip2p");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                if (ifName != null) {
                    _data.writeInt(1);
                    _data.writeString(ifName);
                } else {
                    _data.writeInt(0);
                }
                b.transact(1004, _data, _reply, 0);
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
        }
        result = false;
        _reply.recycle();
        _data.recycle();
        return result;
    }

    public boolean configIPAddr(String ifName, String ipAddr, String server) {
        boolean result = true;
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("wifip2p");
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
                b.transact(1003, _data, _reply, 0);
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
        }
        result = false;
        _reply.recycle();
        _data.recycle();
        return result;
    }
}
