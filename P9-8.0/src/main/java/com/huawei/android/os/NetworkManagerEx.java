package com.huawei.android.os;

import android.net.LinkAddress;
import android.net.RouteInfo;
import android.os.IBinder;
import android.os.INetworkManagementService;
import android.os.INetworkManagementService.Stub;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.android.server.net.BaseNetworkObserver;
import com.huawei.android.net.InterfaceConfigurationEx;
import com.huawei.android.server.net.BaseNetworkObserverEx;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class NetworkManagerEx {
    private static final int CODE_CLEAR_AD_APKDL_STRATEGY = 1103;
    private static final int CODE_PRINT_AD_APKDL_STRATEGY = 1104;
    private static final int CODE_SET_AD_STRATEGY = 1101;
    private static final int CODE_SET_APK_CONTROL_STRATEGY = 1109;
    private static final int CODE_SET_APK_DL_STRATEGY = 1102;
    private static final int CODE_SET_APK_DL_URL_USER_RESULT = 1105;
    private static final String DESCRIPTOR_NETWORKMANAGEMENT_SERVICE = "android.os.INetworkManagementService";
    private static final String TAG = "NetworkManagerEx";

    private static class InnerBaseNetworkObserver extends BaseNetworkObserver {
        private BaseNetworkObserverEx mOutter;

        public InnerBaseNetworkObserver(BaseNetworkObserverEx outter) {
            this.mOutter = outter;
        }

        public void interfaceStatusChanged(String iface, boolean up) {
            this.mOutter.interfaceStatusChanged(iface, up);
        }

        public void interfaceRemoved(String iface) {
            this.mOutter.interfaceRemoved(iface);
        }

        public void addressUpdated(String iface, LinkAddress address) {
            this.mOutter.addressUpdated(iface, address);
        }

        public void addressRemoved(String iface, LinkAddress address) {
            this.mOutter.addressRemoved(iface, address);
        }

        public void interfaceLinkStateChanged(String iface, boolean up) {
            this.mOutter.interfaceLinkStateChanged(iface, up);
        }

        public void interfaceAdded(String iface) {
            this.mOutter.interfaceAdded(iface);
        }

        public void interfaceClassDataActivityChanged(String label, boolean active, long tsNanos) {
            this.mOutter.interfaceClassDataActivityChanged(label, active, tsNanos);
        }

        public void limitReached(String limitName, String iface) {
            this.mOutter.limitReached(limitName, iface);
        }

        public void interfaceDnsServerInfo(String iface, long lifetime, String[] servers) {
            this.mOutter.interfaceDnsServerInfo(iface, lifetime, servers);
        }

        public void routeUpdated(RouteInfo route) {
            this.mOutter.routeUpdated(route);
        }

        public void routeRemoved(RouteInfo route) {
            this.mOutter.routeRemoved(route);
        }
    }

    public static void setAdFilterRules(HashMap<String, List<String>> adStrategy, boolean needReset) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("network_management");
        Log.d(TAG, "setAdFilterRules, adStrategy=" + adStrategy + ", needReset=" + needReset);
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR_NETWORKMANAGEMENT_SERVICE);
                if (adStrategy == null) {
                    _data.writeInt(-1);
                } else {
                    int size = adStrategy.size();
                    _data.writeInt(size);
                    Log.d(TAG, "setAdFilterRules, adStrategy size=" + size);
                    if (size > 0) {
                        Set<String> keysSet = adStrategy.keySet();
                        List<String> keysList = new ArrayList();
                        for (String keyString : keysSet) {
                            keysList.add(keyString);
                        }
                        for (int i = 0; i < size; i++) {
                            String key = (String) keysList.get(i);
                            List<String> value = (List) adStrategy.get(key);
                            _data.writeString(key);
                            _data.writeStringList(value);
                        }
                    }
                }
                _data.writeInt(needReset ? 1 : 0);
                b.transact(1101, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
                return;
            } finally {
                _reply.recycle();
                _data.recycle();
            }
        }
        _reply.recycle();
        _data.recycle();
    }

    public static void setApkDlFilterRules(String[] pkgName, boolean needReset) {
        int i = 0;
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("network_management");
        if (pkgName != null) {
            Log.d(TAG, "setApkDlFilterRules, pkgName=" + Arrays.asList(pkgName) + ", needReset=" + needReset);
        }
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR_NETWORKMANAGEMENT_SERVICE);
                _data.writeStringArray(pkgName);
                if (needReset) {
                    i = 1;
                }
                _data.writeInt(i);
                b.transact(CODE_SET_APK_DL_STRATEGY, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
                return;
            } finally {
                _reply.recycle();
                _data.recycle();
            }
        }
        _reply.recycle();
        _data.recycle();
    }

    public static void clearAdOrApkDlFilterRules(String[] pkgName, boolean needReset, int strategy) {
        int i = 0;
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("network_management");
        if (pkgName != null) {
            Log.d(TAG, "clearAdOrApkDlFilterRules, pkgName=" + Arrays.asList(pkgName) + ", needReset=" + needReset + ", strategy=" + strategy);
        }
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR_NETWORKMANAGEMENT_SERVICE);
                _data.writeStringArray(pkgName);
                if (needReset) {
                    i = 1;
                }
                _data.writeInt(i);
                _data.writeInt(strategy);
                b.transact(CODE_CLEAR_AD_APKDL_STRATEGY, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
                return;
            } finally {
                _reply.recycle();
                _data.recycle();
            }
        }
        _reply.recycle();
        _data.recycle();
    }

    public static void printAdOrApkDlFilterRules(int strategy) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("network_management");
        Log.d(TAG, "printAdOrApkDlFilterRules, strategy=" + strategy);
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR_NETWORKMANAGEMENT_SERVICE);
                _data.writeInt(strategy);
                b.transact(CODE_PRINT_AD_APKDL_STRATEGY, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
                return;
            } finally {
                _reply.recycle();
                _data.recycle();
            }
        }
        _reply.recycle();
        _data.recycle();
    }

    public static void setApkDlUrlUserResult(String downloadId, boolean result) {
        int i = 0;
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("network_management");
        Log.d(TAG, "setApkDlUrlUserResult, downloadId=" + downloadId + ", result=" + result);
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR_NETWORKMANAGEMENT_SERVICE);
                _data.writeString(downloadId);
                if (result) {
                    i = 1;
                }
                _data.writeInt(i);
                b.transact(CODE_SET_APK_DL_URL_USER_RESULT, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
                return;
            } finally {
                _reply.recycle();
                _data.recycle();
            }
        }
        _reply.recycle();
        _data.recycle();
    }

    public static void setApkControlFilterRules(HashMap<String, List<String>> apkStrategy, boolean needReset) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("network_management");
        Log.d(TAG, "setApkControlFilterRules, apkStrategy=" + apkStrategy + ", needReset=" + needReset);
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR_NETWORKMANAGEMENT_SERVICE);
                if (apkStrategy == null) {
                    _data.writeInt(-1);
                } else {
                    int size = apkStrategy.size();
                    _data.writeInt(size);
                    Log.d(TAG, "setApkControlFilterRules, apkStrategy size=" + size);
                    if (size > 0) {
                        Set<String> keysSet = apkStrategy.keySet();
                        List<String> keysList = new ArrayList();
                        for (String keyString : keysSet) {
                            keysList.add(keyString);
                        }
                        for (int i = 0; i < size; i++) {
                            String key = (String) keysList.get(i);
                            List<String> value = (List) apkStrategy.get(key);
                            _data.writeString(key);
                            _data.writeStringList(value);
                        }
                    }
                }
                _data.writeInt(needReset ? 1 : 0);
                b.transact(CODE_SET_APK_CONTROL_STRATEGY, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
                return;
            } finally {
                _reply.recycle();
                _data.recycle();
            }
        }
        _reply.recycle();
        _data.recycle();
    }

    public static void registerObserver(BaseNetworkObserverEx obs) throws RemoteException {
        INetworkManagementService networkManagement = Stub.asInterface(ServiceManager.getService("network_management"));
        BaseNetworkObserver observer = new InnerBaseNetworkObserver(obs);
        obs.setInnerBaseNetworkObserver(observer);
        networkManagement.registerObserver(observer);
    }

    public static void unregisterObserver(BaseNetworkObserverEx obs) throws RemoteException {
        Stub.asInterface(ServiceManager.getService("network_management")).unregisterObserver(obs.getInnerBaseNetworkObserver());
    }

    public static String[] listTetheredInterfaces() throws RemoteException {
        return Stub.asInterface(ServiceManager.getService("network_management")).listTetheredInterfaces();
    }

    public static void tetherInterface(String iface) throws RemoteException {
        Stub.asInterface(ServiceManager.getService("network_management")).tetherInterface(iface);
    }

    public static void untetherInterface(String iface) throws RemoteException {
        Stub.asInterface(ServiceManager.getService("network_management")).untetherInterface(iface);
    }

    public static boolean isTetheringStarted() throws RemoteException {
        return Stub.asInterface(ServiceManager.getService("network_management")).isTetheringStarted();
    }

    public static void startTethering(String[] addressRange) throws RemoteException {
        Stub.asInterface(ServiceManager.getService("network_management")).startTethering(addressRange);
    }

    public static void stopTethering() throws RemoteException {
        Stub.asInterface(ServiceManager.getService("network_management")).stopTethering();
    }

    public static InterfaceConfigurationEx getInterfaceConfig(String iface) throws RemoteException {
        return new InterfaceConfigurationEx(Stub.asInterface(ServiceManager.getService("network_management")).getInterfaceConfig(iface));
    }

    public static void setInterfaceConfig(String iface, InterfaceConfigurationEx config) throws RemoteException {
        Stub.asInterface(ServiceManager.getService("network_management")).setInterfaceConfig(iface, config.getInterfaceConfiguration());
    }
}
