package android.net.wifi;

import android.content.pm.ParceledListSlice;
import android.net.DhcpInfo;
import android.net.Network;
import android.net.wifi.ISoftApCallback;
import android.net.wifi.hotspot2.IProvisioningCallback;
import android.net.wifi.hotspot2.OsuProvider;
import android.net.wifi.hotspot2.PasspointConfiguration;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Messenger;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.WorkSource;
import java.util.List;

public interface IWifiManager extends IInterface {

    public static abstract class Stub extends Binder implements IWifiManager {
        private static final String DESCRIPTOR = "android.net.wifi.IWifiManager";
        static final int TRANSACTION_acquireMulticastLock = 41;
        static final int TRANSACTION_acquireWifiLock = 36;
        static final int TRANSACTION_addOrUpdateNetwork = 9;
        static final int TRANSACTION_addOrUpdatePasspointConfiguration = 10;
        static final int TRANSACTION_deauthenticateNetwork = 15;
        static final int TRANSACTION_disableEphemeralNetwork = 60;
        static final int TRANSACTION_disableNetwork = 18;
        static final int TRANSACTION_disconnect = 21;
        static final int TRANSACTION_enableNetwork = 17;
        static final int TRANSACTION_enableTdls = 54;
        static final int TRANSACTION_enableTdlsWithMacAddress = 55;
        static final int TRANSACTION_enableVerboseLogging = 57;
        static final int TRANSACTION_enableWifiConnectivityManager = 59;
        static final int TRANSACTION_factoryReset = 61;
        static final int TRANSACTION_getAllMatchingWifiConfigs = 7;
        static final int TRANSACTION_getConfiguredNetworks = 4;
        static final int TRANSACTION_getConnectionInfo = 24;
        static final int TRANSACTION_getCountryCode = 31;
        static final int TRANSACTION_getCurrentNetwork = 62;
        static final int TRANSACTION_getCurrentNetworkWpsNfcConfigurationToken = 56;
        static final int TRANSACTION_getDhcpInfo = 34;
        static final int TRANSACTION_getMatchingOsuProviders = 8;
        static final int TRANSACTION_getMatchingWifiConfig = 6;
        static final int TRANSACTION_getPasspointConfigurations = 12;
        static final int TRANSACTION_getPrivilegedConfiguredNetworks = 5;
        static final int TRANSACTION_getScanResults = 20;
        static final int TRANSACTION_getSupportedFeatures = 1;
        static final int TRANSACTION_getVerboseLoggingLevel = 58;
        static final int TRANSACTION_getWifiApConfiguration = 51;
        static final int TRANSACTION_getWifiApEnabledState = 50;
        static final int TRANSACTION_getWifiEnabledState = 26;
        static final int TRANSACTION_getWifiServiceMessenger = 53;
        static final int TRANSACTION_initializeMulticastFiltering = 39;
        static final int TRANSACTION_isDualBandSupported = 32;
        static final int TRANSACTION_isMulticastEnabled = 40;
        static final int TRANSACTION_isScanAlwaysAvailable = 35;
        static final int TRANSACTION_matchProviderWithCurrentNetwork = 14;
        static final int TRANSACTION_needs5GHzToAnyApBandConversion = 33;
        static final int TRANSACTION_queryPasspointIcon = 13;
        static final int TRANSACTION_reassociate = 23;
        static final int TRANSACTION_reconnect = 22;
        static final int TRANSACTION_registerSoftApCallback = 67;
        static final int TRANSACTION_releaseMulticastLock = 42;
        static final int TRANSACTION_releaseWifiLock = 38;
        static final int TRANSACTION_removeNetwork = 16;
        static final int TRANSACTION_removePasspointConfiguration = 11;
        static final int TRANSACTION_reportActivityInfo = 2;
        static final int TRANSACTION_requestActivityInfo = 3;
        static final int TRANSACTION_restoreBackupData = 64;
        static final int TRANSACTION_restoreSupplicantBackupData = 65;
        static final int TRANSACTION_retrieveBackupData = 63;
        static final int TRANSACTION_setCountryCode = 30;
        static final int TRANSACTION_setVoWifiDetectMode = 69;
        static final int TRANSACTION_setWifiApConfiguration = 52;
        static final int TRANSACTION_setWifiApStateByManual = 28;
        static final int TRANSACTION_setWifiEnableForP2p = 29;
        static final int TRANSACTION_setWifiEnabled = 25;
        static final int TRANSACTION_setWifiStateByManual = 27;
        static final int TRANSACTION_startLocalOnlyHotspot = 46;
        static final int TRANSACTION_startScan = 19;
        static final int TRANSACTION_startSoftAp = 44;
        static final int TRANSACTION_startSubscriptionProvisioning = 66;
        static final int TRANSACTION_startWatchLocalOnlyHotspot = 48;
        static final int TRANSACTION_stopLocalOnlyHotspot = 47;
        static final int TRANSACTION_stopSoftAp = 45;
        static final int TRANSACTION_stopWatchLocalOnlyHotspot = 49;
        static final int TRANSACTION_unregisterSoftApCallback = 68;
        static final int TRANSACTION_updateInterfaceIpState = 43;
        static final int TRANSACTION_updateWifiLockWorkSource = 37;

        private static class Proxy implements IWifiManager {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public int getSupportedFeatures() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public WifiActivityEnergyInfo reportActivityInfo() throws RemoteException {
                WifiActivityEnergyInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = WifiActivityEnergyInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void requestActivityInfo(ResultReceiver result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (result != null) {
                        _data.writeInt(1);
                        result.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public ParceledListSlice getConfiguredNetworks() throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getPrivilegedConfiguredNetworks() throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public WifiConfiguration getMatchingWifiConfig(ScanResult scanResult) throws RemoteException {
                WifiConfiguration _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (scanResult != null) {
                        _data.writeInt(1);
                        scanResult.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = WifiConfiguration.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<WifiConfiguration> getAllMatchingWifiConfigs(ScanResult scanResult) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (scanResult != null) {
                        _data.writeInt(1);
                        scanResult.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(WifiConfiguration.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<OsuProvider> getMatchingOsuProviders(ScanResult scanResult) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (scanResult != null) {
                        _data.writeInt(1);
                        scanResult.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(OsuProvider.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int addOrUpdateNetwork(WifiConfiguration config, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (config != null) {
                        _data.writeInt(1);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean addOrUpdatePasspointConfiguration(PasspointConfiguration config, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (config != null) {
                        _data.writeInt(1);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean removePasspointConfiguration(String fqdn, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(fqdn);
                    _data.writeString(packageName);
                    boolean _result = false;
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<PasspointConfiguration> getPasspointConfigurations() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(PasspointConfiguration.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void queryPasspointIcon(long bssid, String fileName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(bssid);
                    _data.writeString(fileName);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int matchProviderWithCurrentNetwork(String fqdn) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(fqdn);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deauthenticateNetwork(long holdoff, boolean ess) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(holdoff);
                    _data.writeInt(ess);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean removeNetwork(int netId, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    _data.writeString(packageName);
                    boolean _result = false;
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean enableNetwork(int netId, boolean disableOthers, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    _data.writeInt(disableOthers);
                    _data.writeString(packageName);
                    boolean _result = false;
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean disableNetwork(int netId, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    _data.writeString(packageName);
                    boolean _result = false;
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean startScan(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = false;
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<ScanResult> getScanResults(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(ScanResult.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void disconnect(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reconnect(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reassociate(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public WifiInfo getConnectionInfo(String callingPackage) throws RemoteException {
                WifiInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = WifiInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setWifiEnabled(String packageName, boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(enable);
                    boolean _result = false;
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getWifiEnabledState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setWifiStateByManual(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable);
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setWifiApStateByManual(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable);
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setWifiEnableForP2p(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable);
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setCountryCode(String country) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(country);
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getCountryCode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isDualBandSupported() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean needs5GHzToAnyApBandConversion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(33, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public DhcpInfo getDhcpInfo() throws RemoteException {
                DhcpInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(34, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (DhcpInfo) DhcpInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isScanAlwaysAvailable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(35, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean acquireWifiLock(IBinder lock, int lockType, String tag, WorkSource ws) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(lock);
                    _data.writeInt(lockType);
                    _data.writeString(tag);
                    boolean _result = true;
                    if (ws != null) {
                        _data.writeInt(1);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(36, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateWifiLockWorkSource(IBinder lock, WorkSource ws) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(lock);
                    if (ws != null) {
                        _data.writeInt(1);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(37, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean releaseWifiLock(IBinder lock) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(lock);
                    boolean _result = false;
                    this.mRemote.transact(38, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void initializeMulticastFiltering() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(39, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isMulticastEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(40, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void acquireMulticastLock(IBinder binder, String tag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(binder);
                    _data.writeString(tag);
                    this.mRemote.transact(41, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void releaseMulticastLock() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(42, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateInterfaceIpState(String ifaceName, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ifaceName);
                    _data.writeInt(mode);
                    this.mRemote.transact(43, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean startSoftAp(WifiConfiguration wifiConfig) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (wifiConfig != null) {
                        _data.writeInt(1);
                        wifiConfig.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(44, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean stopSoftAp() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(45, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int startLocalOnlyHotspot(Messenger messenger, IBinder binder, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (messenger != null) {
                        _data.writeInt(1);
                        messenger.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(binder);
                    _data.writeString(packageName);
                    this.mRemote.transact(46, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopLocalOnlyHotspot() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(47, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startWatchLocalOnlyHotspot(Messenger messenger, IBinder binder) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (messenger != null) {
                        _data.writeInt(1);
                        messenger.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(binder);
                    this.mRemote.transact(48, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopWatchLocalOnlyHotspot() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(49, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getWifiApEnabledState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(50, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public WifiConfiguration getWifiApConfiguration() throws RemoteException {
                WifiConfiguration _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(51, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = WifiConfiguration.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setWifiApConfiguration(WifiConfiguration wifiConfig, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (wifiConfig != null) {
                        _data.writeInt(1);
                        wifiConfig.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    this.mRemote.transact(52, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Messenger getWifiServiceMessenger(String packageName) throws RemoteException {
                Messenger _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(53, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Messenger.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void enableTdls(String remoteIPAddress, boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(remoteIPAddress);
                    _data.writeInt(enable);
                    this.mRemote.transact(54, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void enableTdlsWithMacAddress(String remoteMacAddress, boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(remoteMacAddress);
                    _data.writeInt(enable);
                    this.mRemote.transact(55, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getCurrentNetworkWpsNfcConfigurationToken() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(56, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void enableVerboseLogging(int verbose) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(verbose);
                    this.mRemote.transact(57, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getVerboseLoggingLevel() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(58, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void enableWifiConnectivityManager(boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enabled);
                    this.mRemote.transact(59, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void disableEphemeralNetwork(String SSID, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(SSID);
                    _data.writeString(packageName);
                    this.mRemote.transact(60, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void factoryReset(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(61, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Network getCurrentNetwork() throws RemoteException {
                Network _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(62, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Network) Network.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] retrieveBackupData() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(63, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createByteArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void restoreBackupData(byte[] data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(data);
                    this.mRemote.transact(64, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void restoreSupplicantBackupData(byte[] supplicantData, byte[] ipConfigData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(supplicantData);
                    _data.writeByteArray(ipConfigData);
                    this.mRemote.transact(65, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startSubscriptionProvisioning(OsuProvider provider, IProvisioningCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (provider != null) {
                        _data.writeInt(1);
                        provider.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(66, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerSoftApCallback(IBinder binder, ISoftApCallback callback, int callbackIdentifier) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(binder);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeInt(callbackIdentifier);
                    this.mRemote.transact(67, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterSoftApCallback(int callbackIdentifier) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(callbackIdentifier);
                    this.mRemote.transact(68, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setVoWifiDetectMode(WifiDetectConfInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(69, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IWifiManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IWifiManager)) {
                return new Proxy(obj);
            }
            return (IWifiManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v10, resolved type: android.net.wifi.ScanResult} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v14, resolved type: android.net.wifi.ScanResult} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v18, resolved type: android.net.wifi.ScanResult} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v22, resolved type: android.net.wifi.WifiConfiguration} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v26, resolved type: android.net.wifi.hotspot2.PasspointConfiguration} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v59, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v63, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v71, resolved type: android.net.wifi.WifiConfiguration} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v76, resolved type: android.os.Messenger} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v80, resolved type: android.os.Messenger} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v86, resolved type: android.net.wifi.WifiConfiguration} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v104, resolved type: android.net.wifi.hotspot2.OsuProvider} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v110, resolved type: android.net.wifi.WifiDetectConfInfo} */
        /* JADX WARNING: type inference failed for: r1v1 */
        /* JADX WARNING: type inference failed for: r1v4, types: [android.os.ResultReceiver] */
        /* JADX WARNING: type inference failed for: r1v115 */
        /* JADX WARNING: type inference failed for: r1v116 */
        /* JADX WARNING: type inference failed for: r1v117 */
        /* JADX WARNING: type inference failed for: r1v118 */
        /* JADX WARNING: type inference failed for: r1v119 */
        /* JADX WARNING: type inference failed for: r1v120 */
        /* JADX WARNING: type inference failed for: r1v121 */
        /* JADX WARNING: type inference failed for: r1v122 */
        /* JADX WARNING: type inference failed for: r1v123 */
        /* JADX WARNING: type inference failed for: r1v124 */
        /* JADX WARNING: type inference failed for: r1v125 */
        /* JADX WARNING: type inference failed for: r1v126 */
        /* JADX WARNING: type inference failed for: r1v127 */
        /* JADX WARNING: type inference failed for: r1v128 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                ? _arg0 = 0;
                boolean _arg1 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = getSupportedFeatures();
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        WifiActivityEnergyInfo _result2 = reportActivityInfo();
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ResultReceiver.CREATOR.createFromParcel(data);
                        }
                        requestActivityInfo(_arg0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result3 = getConfiguredNetworks();
                        reply.writeNoException();
                        if (_result3 != null) {
                            reply.writeInt(1);
                            _result3.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result4 = getPrivilegedConfiguredNetworks();
                        reply.writeNoException();
                        if (_result4 != null) {
                            reply.writeInt(1);
                            _result4.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ScanResult.CREATOR.createFromParcel(data);
                        }
                        WifiConfiguration _result5 = getMatchingWifiConfig(_arg0);
                        reply.writeNoException();
                        if (_result5 != null) {
                            reply.writeInt(1);
                            _result5.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ScanResult.CREATOR.createFromParcel(data);
                        }
                        List<WifiConfiguration> _result6 = getAllMatchingWifiConfigs(_arg0);
                        reply.writeNoException();
                        reply.writeTypedList(_result6);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ScanResult.CREATOR.createFromParcel(data);
                        }
                        List<OsuProvider> _result7 = getMatchingOsuProviders(_arg0);
                        reply.writeNoException();
                        reply.writeTypedList(_result7);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = WifiConfiguration.CREATOR.createFromParcel(data);
                        }
                        int _result8 = addOrUpdateNetwork(_arg0, data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = PasspointConfiguration.CREATOR.createFromParcel(data);
                        }
                        boolean _result9 = addOrUpdatePasspointConfiguration(_arg0, data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result10 = removePasspointConfiguration(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        List<PasspointConfiguration> _result11 = getPasspointConfigurations();
                        reply.writeNoException();
                        reply.writeTypedList(_result11);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        queryPasspointIcon(data.readLong(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        int _result12 = matchProviderWithCurrentNetwork(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        long _arg02 = data.readLong();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        deauthenticateNetwork(_arg02, _arg1);
                        reply.writeNoException();
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result13 = removeNetwork(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean _result14 = enableNetwork(_arg03, _arg1, data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result14);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result15 = disableNetwork(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result15);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result16 = startScan(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result16);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        List<ScanResult> _result17 = getScanResults(data.readString());
                        reply.writeNoException();
                        reply.writeTypedList(_result17);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        disconnect(data.readString());
                        reply.writeNoException();
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        reconnect(data.readString());
                        reply.writeNoException();
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        reassociate(data.readString());
                        reply.writeNoException();
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        WifiInfo _result18 = getConnectionInfo(data.readString());
                        reply.writeNoException();
                        if (_result18 != null) {
                            reply.writeInt(1);
                            _result18.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg04 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean _result19 = setWifiEnabled(_arg04, _arg1);
                        reply.writeNoException();
                        reply.writeInt(_result19);
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        int _result20 = getWifiEnabledState();
                        reply.writeNoException();
                        reply.writeInt(_result20);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setWifiStateByManual(_arg1);
                        reply.writeNoException();
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setWifiApStateByManual(_arg1);
                        reply.writeNoException();
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setWifiEnableForP2p(_arg1);
                        reply.writeNoException();
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        setCountryCode(data.readString());
                        reply.writeNoException();
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        String _result21 = getCountryCode();
                        reply.writeNoException();
                        reply.writeString(_result21);
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result22 = isDualBandSupported();
                        reply.writeNoException();
                        reply.writeInt(_result22);
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result23 = needs5GHzToAnyApBandConversion();
                        reply.writeNoException();
                        reply.writeInt(_result23);
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        DhcpInfo _result24 = getDhcpInfo();
                        reply.writeNoException();
                        if (_result24 != null) {
                            reply.writeInt(1);
                            _result24.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result25 = isScanAlwaysAvailable();
                        reply.writeNoException();
                        reply.writeInt(_result25);
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg05 = data.readStrongBinder();
                        int _arg12 = data.readInt();
                        String _arg2 = data.readString();
                        if (data.readInt() != 0) {
                            _arg0 = WorkSource.CREATOR.createFromParcel(data);
                        }
                        boolean _result26 = acquireWifiLock(_arg05, _arg12, _arg2, _arg0);
                        reply.writeNoException();
                        reply.writeInt(_result26);
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg06 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg0 = WorkSource.CREATOR.createFromParcel(data);
                        }
                        updateWifiLockWorkSource(_arg06, _arg0);
                        reply.writeNoException();
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result27 = releaseWifiLock(data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(_result27);
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        initializeMulticastFiltering();
                        reply.writeNoException();
                        return true;
                    case 40:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result28 = isMulticastEnabled();
                        reply.writeNoException();
                        reply.writeInt(_result28);
                        return true;
                    case 41:
                        data.enforceInterface(DESCRIPTOR);
                        acquireMulticastLock(data.readStrongBinder(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 42:
                        data.enforceInterface(DESCRIPTOR);
                        releaseMulticastLock();
                        reply.writeNoException();
                        return true;
                    case 43:
                        data.enforceInterface(DESCRIPTOR);
                        updateInterfaceIpState(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 44:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = WifiConfiguration.CREATOR.createFromParcel(data);
                        }
                        boolean _result29 = startSoftAp(_arg0);
                        reply.writeNoException();
                        reply.writeInt(_result29);
                        return true;
                    case 45:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result30 = stopSoftAp();
                        reply.writeNoException();
                        reply.writeInt(_result30);
                        return true;
                    case 46:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Messenger.CREATOR.createFromParcel(data);
                        }
                        int _result31 = startLocalOnlyHotspot(_arg0, data.readStrongBinder(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result31);
                        return true;
                    case 47:
                        data.enforceInterface(DESCRIPTOR);
                        stopLocalOnlyHotspot();
                        reply.writeNoException();
                        return true;
                    case 48:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Messenger.CREATOR.createFromParcel(data);
                        }
                        startWatchLocalOnlyHotspot(_arg0, data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 49:
                        data.enforceInterface(DESCRIPTOR);
                        stopWatchLocalOnlyHotspot();
                        reply.writeNoException();
                        return true;
                    case 50:
                        data.enforceInterface(DESCRIPTOR);
                        int _result32 = getWifiApEnabledState();
                        reply.writeNoException();
                        reply.writeInt(_result32);
                        return true;
                    case 51:
                        data.enforceInterface(DESCRIPTOR);
                        WifiConfiguration _result33 = getWifiApConfiguration();
                        reply.writeNoException();
                        if (_result33 != null) {
                            reply.writeInt(1);
                            _result33.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 52:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = WifiConfiguration.CREATOR.createFromParcel(data);
                        }
                        boolean _result34 = setWifiApConfiguration(_arg0, data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result34);
                        return true;
                    case 53:
                        data.enforceInterface(DESCRIPTOR);
                        Messenger _result35 = getWifiServiceMessenger(data.readString());
                        reply.writeNoException();
                        if (_result35 != null) {
                            reply.writeInt(1);
                            _result35.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 54:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg07 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        enableTdls(_arg07, _arg1);
                        reply.writeNoException();
                        return true;
                    case 55:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg08 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        enableTdlsWithMacAddress(_arg08, _arg1);
                        reply.writeNoException();
                        return true;
                    case 56:
                        data.enforceInterface(DESCRIPTOR);
                        String _result36 = getCurrentNetworkWpsNfcConfigurationToken();
                        reply.writeNoException();
                        reply.writeString(_result36);
                        return true;
                    case 57:
                        data.enforceInterface(DESCRIPTOR);
                        enableVerboseLogging(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 58:
                        data.enforceInterface(DESCRIPTOR);
                        int _result37 = getVerboseLoggingLevel();
                        reply.writeNoException();
                        reply.writeInt(_result37);
                        return true;
                    case 59:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        enableWifiConnectivityManager(_arg1);
                        reply.writeNoException();
                        return true;
                    case 60:
                        data.enforceInterface(DESCRIPTOR);
                        disableEphemeralNetwork(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 61:
                        data.enforceInterface(DESCRIPTOR);
                        factoryReset(data.readString());
                        reply.writeNoException();
                        return true;
                    case 62:
                        data.enforceInterface(DESCRIPTOR);
                        Network _result38 = getCurrentNetwork();
                        reply.writeNoException();
                        if (_result38 != null) {
                            reply.writeInt(1);
                            _result38.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 63:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result39 = retrieveBackupData();
                        reply.writeNoException();
                        reply.writeByteArray(_result39);
                        return true;
                    case 64:
                        data.enforceInterface(DESCRIPTOR);
                        restoreBackupData(data.createByteArray());
                        reply.writeNoException();
                        return true;
                    case 65:
                        data.enforceInterface(DESCRIPTOR);
                        restoreSupplicantBackupData(data.createByteArray(), data.createByteArray());
                        reply.writeNoException();
                        return true;
                    case 66:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = OsuProvider.CREATOR.createFromParcel(data);
                        }
                        startSubscriptionProvisioning(_arg0, IProvisioningCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 67:
                        data.enforceInterface(DESCRIPTOR);
                        registerSoftApCallback(data.readStrongBinder(), ISoftApCallback.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 68:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterSoftApCallback(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 69:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = WifiDetectConfInfo.CREATOR.createFromParcel(data);
                        }
                        boolean _result40 = setVoWifiDetectMode(_arg0);
                        reply.writeNoException();
                        reply.writeInt(_result40);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void acquireMulticastLock(IBinder iBinder, String str) throws RemoteException;

    boolean acquireWifiLock(IBinder iBinder, int i, String str, WorkSource workSource) throws RemoteException;

    int addOrUpdateNetwork(WifiConfiguration wifiConfiguration, String str) throws RemoteException;

    boolean addOrUpdatePasspointConfiguration(PasspointConfiguration passpointConfiguration, String str) throws RemoteException;

    void deauthenticateNetwork(long j, boolean z) throws RemoteException;

    void disableEphemeralNetwork(String str, String str2) throws RemoteException;

    boolean disableNetwork(int i, String str) throws RemoteException;

    void disconnect(String str) throws RemoteException;

    boolean enableNetwork(int i, boolean z, String str) throws RemoteException;

    void enableTdls(String str, boolean z) throws RemoteException;

    void enableTdlsWithMacAddress(String str, boolean z) throws RemoteException;

    void enableVerboseLogging(int i) throws RemoteException;

    void enableWifiConnectivityManager(boolean z) throws RemoteException;

    void factoryReset(String str) throws RemoteException;

    List<WifiConfiguration> getAllMatchingWifiConfigs(ScanResult scanResult) throws RemoteException;

    ParceledListSlice getConfiguredNetworks() throws RemoteException;

    WifiInfo getConnectionInfo(String str) throws RemoteException;

    String getCountryCode() throws RemoteException;

    Network getCurrentNetwork() throws RemoteException;

    String getCurrentNetworkWpsNfcConfigurationToken() throws RemoteException;

    DhcpInfo getDhcpInfo() throws RemoteException;

    List<OsuProvider> getMatchingOsuProviders(ScanResult scanResult) throws RemoteException;

    WifiConfiguration getMatchingWifiConfig(ScanResult scanResult) throws RemoteException;

    List<PasspointConfiguration> getPasspointConfigurations() throws RemoteException;

    ParceledListSlice getPrivilegedConfiguredNetworks() throws RemoteException;

    List<ScanResult> getScanResults(String str) throws RemoteException;

    int getSupportedFeatures() throws RemoteException;

    int getVerboseLoggingLevel() throws RemoteException;

    WifiConfiguration getWifiApConfiguration() throws RemoteException;

    int getWifiApEnabledState() throws RemoteException;

    int getWifiEnabledState() throws RemoteException;

    Messenger getWifiServiceMessenger(String str) throws RemoteException;

    void initializeMulticastFiltering() throws RemoteException;

    boolean isDualBandSupported() throws RemoteException;

    boolean isMulticastEnabled() throws RemoteException;

    boolean isScanAlwaysAvailable() throws RemoteException;

    int matchProviderWithCurrentNetwork(String str) throws RemoteException;

    boolean needs5GHzToAnyApBandConversion() throws RemoteException;

    void queryPasspointIcon(long j, String str) throws RemoteException;

    void reassociate(String str) throws RemoteException;

    void reconnect(String str) throws RemoteException;

    void registerSoftApCallback(IBinder iBinder, ISoftApCallback iSoftApCallback, int i) throws RemoteException;

    void releaseMulticastLock() throws RemoteException;

    boolean releaseWifiLock(IBinder iBinder) throws RemoteException;

    boolean removeNetwork(int i, String str) throws RemoteException;

    boolean removePasspointConfiguration(String str, String str2) throws RemoteException;

    WifiActivityEnergyInfo reportActivityInfo() throws RemoteException;

    void requestActivityInfo(ResultReceiver resultReceiver) throws RemoteException;

    void restoreBackupData(byte[] bArr) throws RemoteException;

    void restoreSupplicantBackupData(byte[] bArr, byte[] bArr2) throws RemoteException;

    byte[] retrieveBackupData() throws RemoteException;

    void setCountryCode(String str) throws RemoteException;

    boolean setVoWifiDetectMode(WifiDetectConfInfo wifiDetectConfInfo) throws RemoteException;

    boolean setWifiApConfiguration(WifiConfiguration wifiConfiguration, String str) throws RemoteException;

    void setWifiApStateByManual(boolean z) throws RemoteException;

    void setWifiEnableForP2p(boolean z) throws RemoteException;

    boolean setWifiEnabled(String str, boolean z) throws RemoteException;

    void setWifiStateByManual(boolean z) throws RemoteException;

    int startLocalOnlyHotspot(Messenger messenger, IBinder iBinder, String str) throws RemoteException;

    boolean startScan(String str) throws RemoteException;

    boolean startSoftAp(WifiConfiguration wifiConfiguration) throws RemoteException;

    void startSubscriptionProvisioning(OsuProvider osuProvider, IProvisioningCallback iProvisioningCallback) throws RemoteException;

    void startWatchLocalOnlyHotspot(Messenger messenger, IBinder iBinder) throws RemoteException;

    void stopLocalOnlyHotspot() throws RemoteException;

    boolean stopSoftAp() throws RemoteException;

    void stopWatchLocalOnlyHotspot() throws RemoteException;

    void unregisterSoftApCallback(int i) throws RemoteException;

    void updateInterfaceIpState(String str, int i) throws RemoteException;

    void updateWifiLockWorkSource(IBinder iBinder, WorkSource workSource) throws RemoteException;
}
