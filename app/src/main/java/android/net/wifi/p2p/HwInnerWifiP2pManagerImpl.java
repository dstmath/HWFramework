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
    private static HwInnerWifiP2pManager mInstance;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.p2p.HwInnerWifiP2pManagerImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.p2p.HwInnerWifiP2pManagerImpl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.p2p.HwInnerWifiP2pManagerImpl.<clinit>():void");
    }

    public static HwInnerWifiP2pManager getDefault() {
        return mInstance;
    }

    public WifiConfiguration getWifiRepeaterConfiguration() {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        WifiConfiguration wifiConfiguration = null;
        IBinder b = ServiceManager.getService("wifip2p");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                b.transact(CODE_GET_WIFI_REPEATER_CONFIG, _data, _reply, 0);
                _reply.readException();
                if (_reply.readInt() != 0) {
                    wifiConfiguration = (WifiConfiguration) WifiConfiguration.CREATOR.createFromParcel(_reply);
                } else {
                    wifiConfiguration = null;
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
        return wifiConfiguration;
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
                b.transact(CODE_WIFI_MAGICLINK_RELEAGE_IP, _data, _reply, 0);
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
        }
        result = false;
        _reply.recycle();
        _data.recycle();
        return result;
    }
}
