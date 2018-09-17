package android.net.wifi;

import android.net.DhcpInfo;
import android.net.Network;
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
        static final int TRANSACTION_addOrUpdateNetwork = 7;
        static final int TRANSACTION_addPasspointManagementObject = 8;
        static final int TRANSACTION_addToBlacklist = 48;
        static final int TRANSACTION_buildWifiConfig = 46;
        static final int TRANSACTION_clearBlacklist = 49;
        static final int TRANSACTION_deauthenticateNetwork = 12;
        static final int TRANSACTION_disableEphemeralNetwork = 65;
        static final int TRANSACTION_disableNetwork = 15;
        static final int TRANSACTION_disconnect = 19;
        static final int TRANSACTION_enableAggressiveHandover = 57;
        static final int TRANSACTION_enableNetwork = 14;
        static final int TRANSACTION_enableTdls = 52;
        static final int TRANSACTION_enableTdlsWithMacAddress = 53;
        static final int TRANSACTION_enableVerboseLogging = 55;
        static final int TRANSACTION_enableWifiConnectivityManager = 63;
        static final int TRANSACTION_factoryReset = 66;
        static final int TRANSACTION_getAggressiveHandover = 58;
        static final int TRANSACTION_getAllowScansWithTraffic = 60;
        static final int TRANSACTION_getConfigFile = 51;
        static final int TRANSACTION_getConfiguredNetworks = 4;
        static final int TRANSACTION_getConnectionInfo = 22;
        static final int TRANSACTION_getConnectionStatistics = 64;
        static final int TRANSACTION_getCountryCode = 29;
        static final int TRANSACTION_getCurrentNetwork = 67;
        static final int TRANSACTION_getDhcpInfo = 34;
        static final int TRANSACTION_getEnableAutoJoinWhenAssociated = 62;
        static final int TRANSACTION_getFrequencyBand = 31;
        static final int TRANSACTION_getMatchingWifiConfig = 6;
        static final int TRANSACTION_getPrivilegedConfiguredNetworks = 5;
        static final int TRANSACTION_getScanResults = 18;
        static final int TRANSACTION_getSupportedFeatures = 1;
        static final int TRANSACTION_getVerboseLoggingLevel = 56;
        static final int TRANSACTION_getWifiApConfiguration = 45;
        static final int TRANSACTION_getWifiApEnabledState = 44;
        static final int TRANSACTION_getWifiEnabledState = 24;
        static final int TRANSACTION_getWifiServiceMessenger = 50;
        static final int TRANSACTION_getWpsNfcConfigurationToken = 54;
        static final int TRANSACTION_initializeMulticastFiltering = 39;
        static final int TRANSACTION_isDualBandSupported = 32;
        static final int TRANSACTION_isMulticastEnabled = 40;
        static final int TRANSACTION_isScanAlwaysAvailable = 35;
        static final int TRANSACTION_matchProviderWithCurrentNetwork = 11;
        static final int TRANSACTION_modifyPasspointManagementObject = 9;
        static final int TRANSACTION_pingSupplicant = 16;
        static final int TRANSACTION_queryPasspointIcon = 10;
        static final int TRANSACTION_reassociate = 21;
        static final int TRANSACTION_reconnect = 20;
        static final int TRANSACTION_releaseMulticastLock = 42;
        static final int TRANSACTION_releaseWifiLock = 38;
        static final int TRANSACTION_removeNetwork = 13;
        static final int TRANSACTION_reportActivityInfo = 2;
        static final int TRANSACTION_requestActivityInfo = 3;
        static final int TRANSACTION_saveConfiguration = 33;
        static final int TRANSACTION_setAllowScansWithTraffic = 59;
        static final int TRANSACTION_setCountryCode = 28;
        static final int TRANSACTION_setEnableAutoJoinWhenAssociated = 61;
        static final int TRANSACTION_setFrequencyBand = 30;
        static final int TRANSACTION_setVoWifiDetectMode = 68;
        static final int TRANSACTION_setWifiApConfiguration = 47;
        static final int TRANSACTION_setWifiApEnabled = 43;
        static final int TRANSACTION_setWifiApStateByManual = 26;
        static final int TRANSACTION_setWifiEnableForP2p = 27;
        static final int TRANSACTION_setWifiEnabled = 23;
        static final int TRANSACTION_setWifiStateByManual = 25;
        static final int TRANSACTION_startScan = 17;
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
                    this.mRemote.transact(Stub.TRANSACTION_getSupportedFeatures, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public WifiActivityEnergyInfo reportActivityInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    WifiActivityEnergyInfo wifiActivityEnergyInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_reportActivityInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        wifiActivityEnergyInfo = (WifiActivityEnergyInfo) WifiActivityEnergyInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        wifiActivityEnergyInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return wifiActivityEnergyInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void requestActivityInfo(ResultReceiver result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (result != null) {
                        _data.writeInt(Stub.TRANSACTION_getSupportedFeatures);
                        result.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_requestActivityInfo, _data, null, Stub.TRANSACTION_getSupportedFeatures);
                } finally {
                    _data.recycle();
                }
            }

            public List<WifiConfiguration> getConfiguredNetworks() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getConfiguredNetworks, _data, _reply, 0);
                    _reply.readException();
                    List<WifiConfiguration> _result = _reply.createTypedArrayList(WifiConfiguration.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<WifiConfiguration> getPrivilegedConfiguredNetworks() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getPrivilegedConfiguredNetworks, _data, _reply, 0);
                    _reply.readException();
                    List<WifiConfiguration> _result = _reply.createTypedArrayList(WifiConfiguration.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public WifiConfiguration getMatchingWifiConfig(ScanResult scanResult) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    WifiConfiguration wifiConfiguration;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (scanResult != null) {
                        _data.writeInt(Stub.TRANSACTION_getSupportedFeatures);
                        scanResult.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getMatchingWifiConfig, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        wifiConfiguration = (WifiConfiguration) WifiConfiguration.CREATOR.createFromParcel(_reply);
                    } else {
                        wifiConfiguration = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return wifiConfiguration;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int addOrUpdateNetwork(WifiConfiguration config) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (config != null) {
                        _data.writeInt(Stub.TRANSACTION_getSupportedFeatures);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_addOrUpdateNetwork, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int addPasspointManagementObject(String mo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(mo);
                    this.mRemote.transact(Stub.TRANSACTION_addPasspointManagementObject, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int modifyPasspointManagementObject(String fqdn, List<PasspointManagementObjectDefinition> mos) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(fqdn);
                    _data.writeTypedList(mos);
                    this.mRemote.transact(Stub.TRANSACTION_modifyPasspointManagementObject, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_queryPasspointIcon, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_matchProviderWithCurrentNetwork, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deauthenticateNetwork(long holdoff, boolean ess) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(holdoff);
                    if (ess) {
                        i = Stub.TRANSACTION_getSupportedFeatures;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_deauthenticateNetwork, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean removeNetwork(int netId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    this.mRemote.transact(Stub.TRANSACTION_removeNetwork, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean enableNetwork(int netId, boolean disableOthers) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    if (disableOthers) {
                        i = Stub.TRANSACTION_getSupportedFeatures;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_enableNetwork, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean disableNetwork(int netId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    this.mRemote.transact(Stub.TRANSACTION_disableNetwork, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean pingSupplicant() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_pingSupplicant, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startScan(ScanSettings requested, WorkSource ws) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (requested != null) {
                        _data.writeInt(Stub.TRANSACTION_getSupportedFeatures);
                        requested.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (ws != null) {
                        _data.writeInt(Stub.TRANSACTION_getSupportedFeatures);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_startScan, _data, _reply, 0);
                    _reply.readException();
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
                    this.mRemote.transact(Stub.TRANSACTION_getScanResults, _data, _reply, 0);
                    _reply.readException();
                    List<ScanResult> _result = _reply.createTypedArrayList(ScanResult.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void disconnect() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_disconnect, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reconnect() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_reconnect, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reassociate() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_reassociate, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public WifiInfo getConnectionInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    WifiInfo wifiInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getConnectionInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        wifiInfo = (WifiInfo) WifiInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        wifiInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return wifiInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setWifiEnabled(boolean enable) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (enable) {
                        i = Stub.TRANSACTION_getSupportedFeatures;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setWifiEnabled, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getWifiEnabledState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getWifiEnabledState, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setWifiStateByManual(boolean enable) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (enable) {
                        i = Stub.TRANSACTION_getSupportedFeatures;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setWifiStateByManual, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setWifiApStateByManual(boolean enable) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (enable) {
                        i = Stub.TRANSACTION_getSupportedFeatures;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setWifiApStateByManual, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setWifiEnableForP2p(boolean enable) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (enable) {
                        i = Stub.TRANSACTION_getSupportedFeatures;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setWifiEnableForP2p, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setCountryCode(String country, boolean persist) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(country);
                    if (persist) {
                        i = Stub.TRANSACTION_getSupportedFeatures;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setCountryCode, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getCountryCode, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setFrequencyBand(int band, boolean persist) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(band);
                    if (persist) {
                        i = Stub.TRANSACTION_getSupportedFeatures;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setFrequencyBand, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getFrequencyBand() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getFrequencyBand, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_isDualBandSupported, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean saveConfiguration() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_saveConfiguration, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public DhcpInfo getDhcpInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    DhcpInfo dhcpInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getDhcpInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        dhcpInfo = (DhcpInfo) DhcpInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        dhcpInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return dhcpInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isScanAlwaysAvailable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isScanAlwaysAvailable, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
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
                    if (ws != null) {
                        _data.writeInt(Stub.TRANSACTION_getSupportedFeatures);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_acquireWifiLock, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
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
                        _data.writeInt(Stub.TRANSACTION_getSupportedFeatures);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_updateWifiLockWorkSource, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_releaseWifiLock, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void initializeMulticastFiltering() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_initializeMulticastFiltering, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_isMulticastEnabled, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
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
                    this.mRemote.transact(Stub.TRANSACTION_acquireMulticastLock, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_releaseMulticastLock, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setWifiApEnabled(WifiConfiguration wifiConfig, boolean enable) throws RemoteException {
                int i = Stub.TRANSACTION_getSupportedFeatures;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (wifiConfig != null) {
                        _data.writeInt(Stub.TRANSACTION_getSupportedFeatures);
                        wifiConfig.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!enable) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setWifiApEnabled, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getWifiApEnabledState, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public WifiConfiguration getWifiApConfiguration() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    WifiConfiguration wifiConfiguration;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getWifiApConfiguration, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        wifiConfiguration = (WifiConfiguration) WifiConfiguration.CREATOR.createFromParcel(_reply);
                    } else {
                        wifiConfiguration = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return wifiConfiguration;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public WifiConfiguration buildWifiConfig(String uriString, String mimeType, byte[] data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    WifiConfiguration wifiConfiguration;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uriString);
                    _data.writeString(mimeType);
                    _data.writeByteArray(data);
                    this.mRemote.transact(Stub.TRANSACTION_buildWifiConfig, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        wifiConfiguration = (WifiConfiguration) WifiConfiguration.CREATOR.createFromParcel(_reply);
                    } else {
                        wifiConfiguration = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return wifiConfiguration;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setWifiApConfiguration(WifiConfiguration wifiConfig) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (wifiConfig != null) {
                        _data.writeInt(Stub.TRANSACTION_getSupportedFeatures);
                        wifiConfig.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setWifiApConfiguration, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addToBlacklist(String bssid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(bssid);
                    this.mRemote.transact(Stub.TRANSACTION_addToBlacklist, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearBlacklist() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_clearBlacklist, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Messenger getWifiServiceMessenger() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Messenger messenger;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getWifiServiceMessenger, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        messenger = (Messenger) Messenger.CREATOR.createFromParcel(_reply);
                    } else {
                        messenger = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return messenger;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getConfigFile() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getConfigFile, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void enableTdls(String remoteIPAddress, boolean enable) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(remoteIPAddress);
                    if (enable) {
                        i = Stub.TRANSACTION_getSupportedFeatures;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_enableTdls, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void enableTdlsWithMacAddress(String remoteMacAddress, boolean enable) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(remoteMacAddress);
                    if (enable) {
                        i = Stub.TRANSACTION_getSupportedFeatures;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_enableTdlsWithMacAddress, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getWpsNfcConfigurationToken(int netId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    this.mRemote.transact(Stub.TRANSACTION_getWpsNfcConfigurationToken, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_enableVerboseLogging, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getVerboseLoggingLevel, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void enableAggressiveHandover(int enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enabled);
                    this.mRemote.transact(Stub.TRANSACTION_enableAggressiveHandover, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getAggressiveHandover() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getAggressiveHandover, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAllowScansWithTraffic(int enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enabled);
                    this.mRemote.transact(Stub.TRANSACTION_setAllowScansWithTraffic, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getAllowScansWithTraffic() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getAllowScansWithTraffic, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setEnableAutoJoinWhenAssociated(boolean enabled) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (enabled) {
                        i = Stub.TRANSACTION_getSupportedFeatures;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setEnableAutoJoinWhenAssociated, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getEnableAutoJoinWhenAssociated() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getEnableAutoJoinWhenAssociated, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void enableWifiConnectivityManager(boolean enabled) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (enabled) {
                        i = Stub.TRANSACTION_getSupportedFeatures;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_enableWifiConnectivityManager, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public WifiConnectionStatistics getConnectionStatistics() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    WifiConnectionStatistics wifiConnectionStatistics;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getConnectionStatistics, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        wifiConnectionStatistics = (WifiConnectionStatistics) WifiConnectionStatistics.CREATOR.createFromParcel(_reply);
                    } else {
                        wifiConnectionStatistics = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return wifiConnectionStatistics;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void disableEphemeralNetwork(String SSID) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(SSID);
                    this.mRemote.transact(Stub.TRANSACTION_disableEphemeralNetwork, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void factoryReset() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_factoryReset, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Network getCurrentNetwork() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Network network;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getCurrentNetwork, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        network = (Network) Network.CREATOR.createFromParcel(_reply);
                    } else {
                        network = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return network;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setVoWifiDetectMode(WifiDetectConfInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (info != null) {
                        _data.writeInt(Stub.TRANSACTION_getSupportedFeatures);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setVoWifiDetectMode, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
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

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _result;
            List<WifiConfiguration> _result2;
            WifiConfiguration _result3;
            WifiConfiguration wifiConfiguration;
            boolean _result4;
            WorkSource workSource;
            String _result5;
            IBinder _arg0;
            switch (code) {
                case TRANSACTION_getSupportedFeatures /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getSupportedFeatures();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_reportActivityInfo /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    WifiActivityEnergyInfo _result6 = reportActivityInfo();
                    reply.writeNoException();
                    if (_result6 != null) {
                        reply.writeInt(TRANSACTION_getSupportedFeatures);
                        _result6.writeToParcel(reply, TRANSACTION_getSupportedFeatures);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_requestActivityInfo /*3*/:
                    ResultReceiver resultReceiver;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        resultReceiver = (ResultReceiver) ResultReceiver.CREATOR.createFromParcel(data);
                    } else {
                        resultReceiver = null;
                    }
                    requestActivityInfo(resultReceiver);
                    return true;
                case TRANSACTION_getConfiguredNetworks /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getConfiguredNetworks();
                    reply.writeNoException();
                    reply.writeTypedList(_result2);
                    return true;
                case TRANSACTION_getPrivilegedConfiguredNetworks /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getPrivilegedConfiguredNetworks();
                    reply.writeNoException();
                    reply.writeTypedList(_result2);
                    return true;
                case TRANSACTION_getMatchingWifiConfig /*6*/:
                    ScanResult scanResult;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        scanResult = (ScanResult) ScanResult.CREATOR.createFromParcel(data);
                    } else {
                        scanResult = null;
                    }
                    _result3 = getMatchingWifiConfig(scanResult);
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_getSupportedFeatures);
                        _result3.writeToParcel(reply, TRANSACTION_getSupportedFeatures);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_addOrUpdateNetwork /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        wifiConfiguration = (WifiConfiguration) WifiConfiguration.CREATOR.createFromParcel(data);
                    } else {
                        wifiConfiguration = null;
                    }
                    _result = addOrUpdateNetwork(wifiConfiguration);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_addPasspointManagementObject /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = addPasspointManagementObject(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_modifyPasspointManagementObject /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = modifyPasspointManagementObject(data.readString(), data.createTypedArrayList(PasspointManagementObjectDefinition.CREATOR));
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_queryPasspointIcon /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    queryPasspointIcon(data.readLong(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_matchProviderWithCurrentNetwork /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = matchProviderWithCurrentNetwork(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_deauthenticateNetwork /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    deauthenticateNetwork(data.readLong(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removeNetwork /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = removeNetwork(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result4 ? TRANSACTION_getSupportedFeatures : 0);
                    return true;
                case TRANSACTION_enableNetwork /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = enableNetwork(data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result4 ? TRANSACTION_getSupportedFeatures : 0);
                    return true;
                case TRANSACTION_disableNetwork /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = disableNetwork(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result4 ? TRANSACTION_getSupportedFeatures : 0);
                    return true;
                case TRANSACTION_pingSupplicant /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = pingSupplicant();
                    reply.writeNoException();
                    reply.writeInt(_result4 ? TRANSACTION_getSupportedFeatures : 0);
                    return true;
                case TRANSACTION_startScan /*17*/:
                    ScanSettings scanSettings;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        scanSettings = (ScanSettings) ScanSettings.CREATOR.createFromParcel(data);
                    } else {
                        scanSettings = null;
                    }
                    if (data.readInt() != 0) {
                        workSource = (WorkSource) WorkSource.CREATOR.createFromParcel(data);
                    } else {
                        workSource = null;
                    }
                    startScan(scanSettings, workSource);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getScanResults /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    List<ScanResult> _result7 = getScanResults(data.readString());
                    reply.writeNoException();
                    reply.writeTypedList(_result7);
                    return true;
                case TRANSACTION_disconnect /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    disconnect();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_reconnect /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    reconnect();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_reassociate /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    reassociate();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getConnectionInfo /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    WifiInfo _result8 = getConnectionInfo();
                    reply.writeNoException();
                    if (_result8 != null) {
                        reply.writeInt(TRANSACTION_getSupportedFeatures);
                        _result8.writeToParcel(reply, TRANSACTION_getSupportedFeatures);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_setWifiEnabled /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = setWifiEnabled(data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result4 ? TRANSACTION_getSupportedFeatures : 0);
                    return true;
                case TRANSACTION_getWifiEnabledState /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getWifiEnabledState();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_setWifiStateByManual /*25*/:
                    data.enforceInterface(DESCRIPTOR);
                    setWifiStateByManual(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setWifiApStateByManual /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    setWifiApStateByManual(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setWifiEnableForP2p /*27*/:
                    data.enforceInterface(DESCRIPTOR);
                    setWifiEnableForP2p(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setCountryCode /*28*/:
                    data.enforceInterface(DESCRIPTOR);
                    setCountryCode(data.readString(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getCountryCode /*29*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result5 = getCountryCode();
                    reply.writeNoException();
                    reply.writeString(_result5);
                    return true;
                case TRANSACTION_setFrequencyBand /*30*/:
                    data.enforceInterface(DESCRIPTOR);
                    setFrequencyBand(data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getFrequencyBand /*31*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getFrequencyBand();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_isDualBandSupported /*32*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = isDualBandSupported();
                    reply.writeNoException();
                    reply.writeInt(_result4 ? TRANSACTION_getSupportedFeatures : 0);
                    return true;
                case TRANSACTION_saveConfiguration /*33*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = saveConfiguration();
                    reply.writeNoException();
                    reply.writeInt(_result4 ? TRANSACTION_getSupportedFeatures : 0);
                    return true;
                case TRANSACTION_getDhcpInfo /*34*/:
                    data.enforceInterface(DESCRIPTOR);
                    DhcpInfo _result9 = getDhcpInfo();
                    reply.writeNoException();
                    if (_result9 != null) {
                        reply.writeInt(TRANSACTION_getSupportedFeatures);
                        _result9.writeToParcel(reply, TRANSACTION_getSupportedFeatures);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_isScanAlwaysAvailable /*35*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = isScanAlwaysAvailable();
                    reply.writeNoException();
                    reply.writeInt(_result4 ? TRANSACTION_getSupportedFeatures : 0);
                    return true;
                case TRANSACTION_acquireWifiLock /*36*/:
                    WorkSource workSource2;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readStrongBinder();
                    int _arg1 = data.readInt();
                    String _arg2 = data.readString();
                    if (data.readInt() != 0) {
                        workSource2 = (WorkSource) WorkSource.CREATOR.createFromParcel(data);
                    } else {
                        workSource2 = null;
                    }
                    _result4 = acquireWifiLock(_arg0, _arg1, _arg2, workSource2);
                    reply.writeNoException();
                    reply.writeInt(_result4 ? TRANSACTION_getSupportedFeatures : 0);
                    return true;
                case TRANSACTION_updateWifiLockWorkSource /*37*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readStrongBinder();
                    if (data.readInt() != 0) {
                        workSource = (WorkSource) WorkSource.CREATOR.createFromParcel(data);
                    } else {
                        workSource = null;
                    }
                    updateWifiLockWorkSource(_arg0, workSource);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_releaseWifiLock /*38*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = releaseWifiLock(data.readStrongBinder());
                    reply.writeNoException();
                    reply.writeInt(_result4 ? TRANSACTION_getSupportedFeatures : 0);
                    return true;
                case TRANSACTION_initializeMulticastFiltering /*39*/:
                    data.enforceInterface(DESCRIPTOR);
                    initializeMulticastFiltering();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isMulticastEnabled /*40*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = isMulticastEnabled();
                    reply.writeNoException();
                    reply.writeInt(_result4 ? TRANSACTION_getSupportedFeatures : 0);
                    return true;
                case TRANSACTION_acquireMulticastLock /*41*/:
                    data.enforceInterface(DESCRIPTOR);
                    acquireMulticastLock(data.readStrongBinder(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_releaseMulticastLock /*42*/:
                    data.enforceInterface(DESCRIPTOR);
                    releaseMulticastLock();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setWifiApEnabled /*43*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        wifiConfiguration = (WifiConfiguration) WifiConfiguration.CREATOR.createFromParcel(data);
                    } else {
                        wifiConfiguration = null;
                    }
                    setWifiApEnabled(wifiConfiguration, data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getWifiApEnabledState /*44*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getWifiApEnabledState();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_getWifiApConfiguration /*45*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getWifiApConfiguration();
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_getSupportedFeatures);
                        _result3.writeToParcel(reply, TRANSACTION_getSupportedFeatures);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_buildWifiConfig /*46*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = buildWifiConfig(data.readString(), data.readString(), data.createByteArray());
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_getSupportedFeatures);
                        _result3.writeToParcel(reply, TRANSACTION_getSupportedFeatures);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_setWifiApConfiguration /*47*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        wifiConfiguration = (WifiConfiguration) WifiConfiguration.CREATOR.createFromParcel(data);
                    } else {
                        wifiConfiguration = null;
                    }
                    setWifiApConfiguration(wifiConfiguration);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_addToBlacklist /*48*/:
                    data.enforceInterface(DESCRIPTOR);
                    addToBlacklist(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_clearBlacklist /*49*/:
                    data.enforceInterface(DESCRIPTOR);
                    clearBlacklist();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getWifiServiceMessenger /*50*/:
                    data.enforceInterface(DESCRIPTOR);
                    Messenger _result10 = getWifiServiceMessenger();
                    reply.writeNoException();
                    if (_result10 != null) {
                        reply.writeInt(TRANSACTION_getSupportedFeatures);
                        _result10.writeToParcel(reply, TRANSACTION_getSupportedFeatures);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getConfigFile /*51*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result5 = getConfigFile();
                    reply.writeNoException();
                    reply.writeString(_result5);
                    return true;
                case TRANSACTION_enableTdls /*52*/:
                    data.enforceInterface(DESCRIPTOR);
                    enableTdls(data.readString(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_enableTdlsWithMacAddress /*53*/:
                    data.enforceInterface(DESCRIPTOR);
                    enableTdlsWithMacAddress(data.readString(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getWpsNfcConfigurationToken /*54*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result5 = getWpsNfcConfigurationToken(data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result5);
                    return true;
                case TRANSACTION_enableVerboseLogging /*55*/:
                    data.enforceInterface(DESCRIPTOR);
                    enableVerboseLogging(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getVerboseLoggingLevel /*56*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getVerboseLoggingLevel();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_enableAggressiveHandover /*57*/:
                    data.enforceInterface(DESCRIPTOR);
                    enableAggressiveHandover(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getAggressiveHandover /*58*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getAggressiveHandover();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_setAllowScansWithTraffic /*59*/:
                    data.enforceInterface(DESCRIPTOR);
                    setAllowScansWithTraffic(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getAllowScansWithTraffic /*60*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getAllowScansWithTraffic();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_setEnableAutoJoinWhenAssociated /*61*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = setEnableAutoJoinWhenAssociated(data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result4 ? TRANSACTION_getSupportedFeatures : 0);
                    return true;
                case TRANSACTION_getEnableAutoJoinWhenAssociated /*62*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getEnableAutoJoinWhenAssociated();
                    reply.writeNoException();
                    reply.writeInt(_result4 ? TRANSACTION_getSupportedFeatures : 0);
                    return true;
                case TRANSACTION_enableWifiConnectivityManager /*63*/:
                    data.enforceInterface(DESCRIPTOR);
                    enableWifiConnectivityManager(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getConnectionStatistics /*64*/:
                    data.enforceInterface(DESCRIPTOR);
                    WifiConnectionStatistics _result11 = getConnectionStatistics();
                    reply.writeNoException();
                    if (_result11 != null) {
                        reply.writeInt(TRANSACTION_getSupportedFeatures);
                        _result11.writeToParcel(reply, TRANSACTION_getSupportedFeatures);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_disableEphemeralNetwork /*65*/:
                    data.enforceInterface(DESCRIPTOR);
                    disableEphemeralNetwork(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_factoryReset /*66*/:
                    data.enforceInterface(DESCRIPTOR);
                    factoryReset();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getCurrentNetwork /*67*/:
                    data.enforceInterface(DESCRIPTOR);
                    Network _result12 = getCurrentNetwork();
                    reply.writeNoException();
                    if (_result12 != null) {
                        reply.writeInt(TRANSACTION_getSupportedFeatures);
                        _result12.writeToParcel(reply, TRANSACTION_getSupportedFeatures);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_setVoWifiDetectMode /*68*/:
                    WifiDetectConfInfo wifiDetectConfInfo;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        wifiDetectConfInfo = (WifiDetectConfInfo) WifiDetectConfInfo.CREATOR.createFromParcel(data);
                    } else {
                        wifiDetectConfInfo = null;
                    }
                    _result4 = setVoWifiDetectMode(wifiDetectConfInfo);
                    reply.writeNoException();
                    reply.writeInt(_result4 ? TRANSACTION_getSupportedFeatures : 0);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void acquireMulticastLock(IBinder iBinder, String str) throws RemoteException;

    boolean acquireWifiLock(IBinder iBinder, int i, String str, WorkSource workSource) throws RemoteException;

    int addOrUpdateNetwork(WifiConfiguration wifiConfiguration) throws RemoteException;

    int addPasspointManagementObject(String str) throws RemoteException;

    void addToBlacklist(String str) throws RemoteException;

    WifiConfiguration buildWifiConfig(String str, String str2, byte[] bArr) throws RemoteException;

    void clearBlacklist() throws RemoteException;

    void deauthenticateNetwork(long j, boolean z) throws RemoteException;

    void disableEphemeralNetwork(String str) throws RemoteException;

    boolean disableNetwork(int i) throws RemoteException;

    void disconnect() throws RemoteException;

    void enableAggressiveHandover(int i) throws RemoteException;

    boolean enableNetwork(int i, boolean z) throws RemoteException;

    void enableTdls(String str, boolean z) throws RemoteException;

    void enableTdlsWithMacAddress(String str, boolean z) throws RemoteException;

    void enableVerboseLogging(int i) throws RemoteException;

    void enableWifiConnectivityManager(boolean z) throws RemoteException;

    void factoryReset() throws RemoteException;

    int getAggressiveHandover() throws RemoteException;

    int getAllowScansWithTraffic() throws RemoteException;

    String getConfigFile() throws RemoteException;

    List<WifiConfiguration> getConfiguredNetworks() throws RemoteException;

    WifiInfo getConnectionInfo() throws RemoteException;

    WifiConnectionStatistics getConnectionStatistics() throws RemoteException;

    String getCountryCode() throws RemoteException;

    Network getCurrentNetwork() throws RemoteException;

    DhcpInfo getDhcpInfo() throws RemoteException;

    boolean getEnableAutoJoinWhenAssociated() throws RemoteException;

    int getFrequencyBand() throws RemoteException;

    WifiConfiguration getMatchingWifiConfig(ScanResult scanResult) throws RemoteException;

    List<WifiConfiguration> getPrivilegedConfiguredNetworks() throws RemoteException;

    List<ScanResult> getScanResults(String str) throws RemoteException;

    int getSupportedFeatures() throws RemoteException;

    int getVerboseLoggingLevel() throws RemoteException;

    WifiConfiguration getWifiApConfiguration() throws RemoteException;

    int getWifiApEnabledState() throws RemoteException;

    int getWifiEnabledState() throws RemoteException;

    Messenger getWifiServiceMessenger() throws RemoteException;

    String getWpsNfcConfigurationToken(int i) throws RemoteException;

    void initializeMulticastFiltering() throws RemoteException;

    boolean isDualBandSupported() throws RemoteException;

    boolean isMulticastEnabled() throws RemoteException;

    boolean isScanAlwaysAvailable() throws RemoteException;

    int matchProviderWithCurrentNetwork(String str) throws RemoteException;

    int modifyPasspointManagementObject(String str, List<PasspointManagementObjectDefinition> list) throws RemoteException;

    boolean pingSupplicant() throws RemoteException;

    void queryPasspointIcon(long j, String str) throws RemoteException;

    void reassociate() throws RemoteException;

    void reconnect() throws RemoteException;

    void releaseMulticastLock() throws RemoteException;

    boolean releaseWifiLock(IBinder iBinder) throws RemoteException;

    boolean removeNetwork(int i) throws RemoteException;

    WifiActivityEnergyInfo reportActivityInfo() throws RemoteException;

    void requestActivityInfo(ResultReceiver resultReceiver) throws RemoteException;

    boolean saveConfiguration() throws RemoteException;

    void setAllowScansWithTraffic(int i) throws RemoteException;

    void setCountryCode(String str, boolean z) throws RemoteException;

    boolean setEnableAutoJoinWhenAssociated(boolean z) throws RemoteException;

    void setFrequencyBand(int i, boolean z) throws RemoteException;

    boolean setVoWifiDetectMode(WifiDetectConfInfo wifiDetectConfInfo) throws RemoteException;

    void setWifiApConfiguration(WifiConfiguration wifiConfiguration) throws RemoteException;

    void setWifiApEnabled(WifiConfiguration wifiConfiguration, boolean z) throws RemoteException;

    void setWifiApStateByManual(boolean z) throws RemoteException;

    void setWifiEnableForP2p(boolean z) throws RemoteException;

    boolean setWifiEnabled(boolean z) throws RemoteException;

    void setWifiStateByManual(boolean z) throws RemoteException;

    void startScan(ScanSettings scanSettings, WorkSource workSource) throws RemoteException;

    void updateWifiLockWorkSource(IBinder iBinder, WorkSource workSource) throws RemoteException;
}
