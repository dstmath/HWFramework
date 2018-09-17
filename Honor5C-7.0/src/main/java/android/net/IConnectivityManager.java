package android.net;

import android.app.PendingIntent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Messenger;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ResultReceiver;
import com.android.internal.net.LegacyVpnInfo;
import com.android.internal.net.VpnConfig;
import com.android.internal.net.VpnInfo;
import com.android.internal.net.VpnProfile;

public interface IConnectivityManager extends IInterface {

    public static abstract class Stub extends Binder implements IConnectivityManager {
        private static final String DESCRIPTOR = "android.net.IConnectivityManager";
        static final int TRANSACTION_addVpnAddress = 65;
        static final int TRANSACTION_checkLteConnectState = 73;
        static final int TRANSACTION_checkMobileProvisioning = 49;
        static final int TRANSACTION_establishVpn = 41;
        static final int TRANSACTION_factoryReset = 68;
        static final int TRANSACTION_getActiveLinkProperties = 12;
        static final int TRANSACTION_getActiveNetwork = 1;
        static final int TRANSACTION_getActiveNetworkForUid = 2;
        static final int TRANSACTION_getActiveNetworkInfo = 3;
        static final int TRANSACTION_getActiveNetworkInfoForUid = 4;
        static final int TRANSACTION_getActiveNetworkQuotaInfo = 17;
        static final int TRANSACTION_getAllNetworkInfo = 7;
        static final int TRANSACTION_getAllNetworkState = 16;
        static final int TRANSACTION_getAllNetworks = 9;
        static final int TRANSACTION_getAllVpnInfo = 45;
        static final int TRANSACTION_getAlwaysOnVpnPackage = 48;
        static final int TRANSACTION_getCaptivePortalServerUrl = 71;
        static final int TRANSACTION_getDefaultNetworkCapabilitiesForUser = 10;
        static final int TRANSACTION_getGlobalProxy = 36;
        static final int TRANSACTION_getLastTetherError = 22;
        static final int TRANSACTION_getLegacyVpnInfo = 44;
        static final int TRANSACTION_getLinkProperties = 14;
        static final int TRANSACTION_getLinkPropertiesForType = 13;
        static final int TRANSACTION_getLteTotalRxBytes = 74;
        static final int TRANSACTION_getLteTotalTxBytes = 75;
        static final int TRANSACTION_getMobileProvisioningUrl = 50;
        static final int TRANSACTION_getNetworkCapabilities = 15;
        static final int TRANSACTION_getNetworkForType = 8;
        static final int TRANSACTION_getNetworkInfo = 5;
        static final int TRANSACTION_getNetworkInfoForUid = 6;
        static final int TRANSACTION_getProxyForNetwork = 38;
        static final int TRANSACTION_getRestoreDefaultNetworkDelay = 64;
        static final int TRANSACTION_getTetherableBluetoothRegexs = 32;
        static final int TRANSACTION_getTetherableIfaces = 26;
        static final int TRANSACTION_getTetherableUsbRegexs = 30;
        static final int TRANSACTION_getTetherableWifiRegexs = 31;
        static final int TRANSACTION_getTetheredDhcpRanges = 29;
        static final int TRANSACTION_getTetheredIfaces = 27;
        static final int TRANSACTION_getTetheringErroredIfaces = 28;
        static final int TRANSACTION_getVpnConfig = 42;
        static final int TRANSACTION_isActiveNetworkMetered = 18;
        static final int TRANSACTION_isNetworkSupported = 11;
        static final int TRANSACTION_isTetheringSupported = 23;
        static final int TRANSACTION_listenForNetwork = 60;
        static final int TRANSACTION_pendingListenForNetwork = 61;
        static final int TRANSACTION_pendingRequestForNetwork = 58;
        static final int TRANSACTION_prepareVpn = 39;
        static final int TRANSACTION_registerNetworkAgent = 56;
        static final int TRANSACTION_registerNetworkFactory = 53;
        static final int TRANSACTION_releaseNetworkRequest = 62;
        static final int TRANSACTION_releasePendingNetworkRequest = 59;
        static final int TRANSACTION_removeVpnAddress = 66;
        static final int TRANSACTION_reportInetCondition = 34;
        static final int TRANSACTION_reportNetworkConnectivity = 35;
        static final int TRANSACTION_requestBandwidthUpdate = 54;
        static final int TRANSACTION_requestNetwork = 57;
        static final int TRANSACTION_requestRouteToHostAddress = 19;
        static final int TRANSACTION_setAcceptUnvalidated = 63;
        static final int TRANSACTION_setAirplaneMode = 52;
        static final int TRANSACTION_setAlwaysOnVpnPackage = 47;
        static final int TRANSACTION_setGlobalProxy = 37;
        static final int TRANSACTION_setLteMobileDataEnabled = 72;
        static final int TRANSACTION_setProvisioningNotificationVisible = 51;
        static final int TRANSACTION_setUnderlyingNetworksForVpn = 67;
        static final int TRANSACTION_setUsbTethering = 33;
        static final int TRANSACTION_setVpnPackageAuthorization = 40;
        static final int TRANSACTION_startLegacyVpn = 43;
        static final int TRANSACTION_startNattKeepalive = 69;
        static final int TRANSACTION_startTethering = 24;
        static final int TRANSACTION_stopKeepalive = 70;
        static final int TRANSACTION_stopTethering = 25;
        static final int TRANSACTION_tether = 20;
        static final int TRANSACTION_unregisterNetworkFactory = 55;
        static final int TRANSACTION_untether = 21;
        static final int TRANSACTION_updateLockdownVpn = 46;

        private static class Proxy implements IConnectivityManager {
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

            public Network getActiveNetwork() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Network network;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getActiveNetwork, _data, _reply, 0);
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

            public Network getActiveNetworkForUid(int uid, boolean ignoreBlocked) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Network network;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    if (ignoreBlocked) {
                        i = Stub.TRANSACTION_getActiveNetwork;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_getActiveNetworkForUid, _data, _reply, 0);
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

            public NetworkInfo getActiveNetworkInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    NetworkInfo networkInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getActiveNetworkInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        networkInfo = (NetworkInfo) NetworkInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        networkInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return networkInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NetworkInfo getActiveNetworkInfoForUid(int uid, boolean ignoreBlocked) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    NetworkInfo networkInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    if (ignoreBlocked) {
                        i = Stub.TRANSACTION_getActiveNetwork;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_getActiveNetworkInfoForUid, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        networkInfo = (NetworkInfo) NetworkInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        networkInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return networkInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NetworkInfo getNetworkInfo(int networkType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    NetworkInfo networkInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(networkType);
                    this.mRemote.transact(Stub.TRANSACTION_getNetworkInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        networkInfo = (NetworkInfo) NetworkInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        networkInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return networkInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NetworkInfo getNetworkInfoForUid(Network network, int uid, boolean ignoreBlocked) throws RemoteException {
                int i = Stub.TRANSACTION_getActiveNetwork;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    NetworkInfo networkInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (network != null) {
                        _data.writeInt(Stub.TRANSACTION_getActiveNetwork);
                        network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(uid);
                    if (!ignoreBlocked) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_getNetworkInfoForUid, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        networkInfo = (NetworkInfo) NetworkInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        networkInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return networkInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NetworkInfo[] getAllNetworkInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getAllNetworkInfo, _data, _reply, 0);
                    _reply.readException();
                    NetworkInfo[] _result = (NetworkInfo[]) _reply.createTypedArray(NetworkInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Network getNetworkForType(int networkType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Network network;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(networkType);
                    this.mRemote.transact(Stub.TRANSACTION_getNetworkForType, _data, _reply, 0);
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

            public Network[] getAllNetworks() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getAllNetworks, _data, _reply, 0);
                    _reply.readException();
                    Network[] _result = (Network[]) _reply.createTypedArray(Network.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NetworkCapabilities[] getDefaultNetworkCapabilitiesForUser(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getDefaultNetworkCapabilitiesForUser, _data, _reply, 0);
                    _reply.readException();
                    NetworkCapabilities[] _result = (NetworkCapabilities[]) _reply.createTypedArray(NetworkCapabilities.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isNetworkSupported(int networkType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(networkType);
                    this.mRemote.transact(Stub.TRANSACTION_isNetworkSupported, _data, _reply, 0);
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

            public LinkProperties getActiveLinkProperties() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    LinkProperties linkProperties;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getActiveLinkProperties, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        linkProperties = (LinkProperties) LinkProperties.CREATOR.createFromParcel(_reply);
                    } else {
                        linkProperties = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return linkProperties;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public LinkProperties getLinkPropertiesForType(int networkType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    LinkProperties linkProperties;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(networkType);
                    this.mRemote.transact(Stub.TRANSACTION_getLinkPropertiesForType, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        linkProperties = (LinkProperties) LinkProperties.CREATOR.createFromParcel(_reply);
                    } else {
                        linkProperties = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return linkProperties;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public LinkProperties getLinkProperties(Network network) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    LinkProperties linkProperties;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (network != null) {
                        _data.writeInt(Stub.TRANSACTION_getActiveNetwork);
                        network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getLinkProperties, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        linkProperties = (LinkProperties) LinkProperties.CREATOR.createFromParcel(_reply);
                    } else {
                        linkProperties = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return linkProperties;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NetworkCapabilities getNetworkCapabilities(Network network) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    NetworkCapabilities networkCapabilities;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (network != null) {
                        _data.writeInt(Stub.TRANSACTION_getActiveNetwork);
                        network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getNetworkCapabilities, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        networkCapabilities = (NetworkCapabilities) NetworkCapabilities.CREATOR.createFromParcel(_reply);
                    } else {
                        networkCapabilities = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return networkCapabilities;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NetworkState[] getAllNetworkState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getAllNetworkState, _data, _reply, 0);
                    _reply.readException();
                    NetworkState[] _result = (NetworkState[]) _reply.createTypedArray(NetworkState.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NetworkQuotaInfo getActiveNetworkQuotaInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    NetworkQuotaInfo networkQuotaInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getActiveNetworkQuotaInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        networkQuotaInfo = (NetworkQuotaInfo) NetworkQuotaInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        networkQuotaInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return networkQuotaInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isActiveNetworkMetered() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isActiveNetworkMetered, _data, _reply, 0);
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

            public boolean requestRouteToHostAddress(int networkType, byte[] hostAddress) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(networkType);
                    _data.writeByteArray(hostAddress);
                    this.mRemote.transact(Stub.TRANSACTION_requestRouteToHostAddress, _data, _reply, 0);
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

            public int tether(String iface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    this.mRemote.transact(Stub.TRANSACTION_tether, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int untether(String iface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    this.mRemote.transact(Stub.TRANSACTION_untether, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getLastTetherError(String iface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    this.mRemote.transact(Stub.TRANSACTION_getLastTetherError, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isTetheringSupported() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isTetheringSupported, _data, _reply, 0);
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

            public void startTethering(int type, ResultReceiver receiver, boolean showProvisioningUi) throws RemoteException {
                int i = Stub.TRANSACTION_getActiveNetwork;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    if (receiver != null) {
                        _data.writeInt(Stub.TRANSACTION_getActiveNetwork);
                        receiver.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!showProvisioningUi) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_startTethering, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopTethering(int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    this.mRemote.transact(Stub.TRANSACTION_stopTethering, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getTetherableIfaces() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getTetherableIfaces, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getTetheredIfaces() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getTetheredIfaces, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getTetheringErroredIfaces() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getTetheringErroredIfaces, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getTetheredDhcpRanges() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getTetheredDhcpRanges, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getTetherableUsbRegexs() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getTetherableUsbRegexs, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getTetherableWifiRegexs() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getTetherableWifiRegexs, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getTetherableBluetoothRegexs() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getTetherableBluetoothRegexs, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setUsbTethering(boolean enable) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (enable) {
                        i = Stub.TRANSACTION_getActiveNetwork;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setUsbTethering, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reportInetCondition(int networkType, int percentage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(networkType);
                    _data.writeInt(percentage);
                    this.mRemote.transact(Stub.TRANSACTION_reportInetCondition, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reportNetworkConnectivity(Network network, boolean hasConnectivity) throws RemoteException {
                int i = Stub.TRANSACTION_getActiveNetwork;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (network != null) {
                        _data.writeInt(Stub.TRANSACTION_getActiveNetwork);
                        network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!hasConnectivity) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_reportNetworkConnectivity, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ProxyInfo getGlobalProxy() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ProxyInfo proxyInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getGlobalProxy, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        proxyInfo = (ProxyInfo) ProxyInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        proxyInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return proxyInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setGlobalProxy(ProxyInfo p) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (p != null) {
                        _data.writeInt(Stub.TRANSACTION_getActiveNetwork);
                        p.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setGlobalProxy, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ProxyInfo getProxyForNetwork(Network nework) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ProxyInfo proxyInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (nework != null) {
                        _data.writeInt(Stub.TRANSACTION_getActiveNetwork);
                        nework.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getProxyForNetwork, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        proxyInfo = (ProxyInfo) ProxyInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        proxyInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return proxyInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean prepareVpn(String oldPackage, String newPackage, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(oldPackage);
                    _data.writeString(newPackage);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_prepareVpn, _data, _reply, 0);
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

            public void setVpnPackageAuthorization(String packageName, int userId, boolean authorized) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    if (authorized) {
                        i = Stub.TRANSACTION_getActiveNetwork;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setVpnPackageAuthorization, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParcelFileDescriptor establishVpn(VpnConfig config) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParcelFileDescriptor parcelFileDescriptor;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (config != null) {
                        _data.writeInt(Stub.TRANSACTION_getActiveNetwork);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_establishVpn, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parcelFileDescriptor = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(_reply);
                    } else {
                        parcelFileDescriptor = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parcelFileDescriptor;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public VpnConfig getVpnConfig(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    VpnConfig vpnConfig;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getVpnConfig, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        vpnConfig = (VpnConfig) VpnConfig.CREATOR.createFromParcel(_reply);
                    } else {
                        vpnConfig = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return vpnConfig;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startLegacyVpn(VpnProfile profile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (profile != null) {
                        _data.writeInt(Stub.TRANSACTION_getActiveNetwork);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_startLegacyVpn, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public LegacyVpnInfo getLegacyVpnInfo(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    LegacyVpnInfo legacyVpnInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getLegacyVpnInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        legacyVpnInfo = (LegacyVpnInfo) LegacyVpnInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        legacyVpnInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return legacyVpnInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public VpnInfo[] getAllVpnInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getAllVpnInfo, _data, _reply, 0);
                    _reply.readException();
                    VpnInfo[] _result = (VpnInfo[]) _reply.createTypedArray(VpnInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean updateLockdownVpn() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_updateLockdownVpn, _data, _reply, 0);
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

            public boolean setAlwaysOnVpnPackage(int userId, String packageName, boolean lockdown) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeString(packageName);
                    if (lockdown) {
                        i = Stub.TRANSACTION_getActiveNetwork;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setAlwaysOnVpnPackage, _data, _reply, 0);
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

            public String getAlwaysOnVpnPackage(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getAlwaysOnVpnPackage, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int checkMobileProvisioning(int suggestedTimeOutMs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(suggestedTimeOutMs);
                    this.mRemote.transact(Stub.TRANSACTION_checkMobileProvisioning, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getMobileProvisioningUrl() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getMobileProvisioningUrl, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setProvisioningNotificationVisible(boolean visible, int networkType, String action) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (visible) {
                        i = Stub.TRANSACTION_getActiveNetwork;
                    }
                    _data.writeInt(i);
                    _data.writeInt(networkType);
                    _data.writeString(action);
                    this.mRemote.transact(Stub.TRANSACTION_setProvisioningNotificationVisible, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAirplaneMode(boolean enable) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (enable) {
                        i = Stub.TRANSACTION_getActiveNetwork;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setAirplaneMode, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerNetworkFactory(Messenger messenger, String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (messenger != null) {
                        _data.writeInt(Stub.TRANSACTION_getActiveNetwork);
                        messenger.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(name);
                    this.mRemote.transact(Stub.TRANSACTION_registerNetworkFactory, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean requestBandwidthUpdate(Network network) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (network != null) {
                        _data.writeInt(Stub.TRANSACTION_getActiveNetwork);
                        network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_requestBandwidthUpdate, _data, _reply, 0);
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

            public void unregisterNetworkFactory(Messenger messenger) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (messenger != null) {
                        _data.writeInt(Stub.TRANSACTION_getActiveNetwork);
                        messenger.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_unregisterNetworkFactory, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int registerNetworkAgent(Messenger messenger, NetworkInfo ni, LinkProperties lp, NetworkCapabilities nc, int score, NetworkMisc misc) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (messenger != null) {
                        _data.writeInt(Stub.TRANSACTION_getActiveNetwork);
                        messenger.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (ni != null) {
                        _data.writeInt(Stub.TRANSACTION_getActiveNetwork);
                        ni.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (lp != null) {
                        _data.writeInt(Stub.TRANSACTION_getActiveNetwork);
                        lp.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (nc != null) {
                        _data.writeInt(Stub.TRANSACTION_getActiveNetwork);
                        nc.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(score);
                    if (misc != null) {
                        _data.writeInt(Stub.TRANSACTION_getActiveNetwork);
                        misc.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_registerNetworkAgent, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NetworkRequest requestNetwork(NetworkCapabilities networkCapabilities, Messenger messenger, int timeoutSec, IBinder binder, int legacy) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    NetworkRequest networkRequest;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (networkCapabilities != null) {
                        _data.writeInt(Stub.TRANSACTION_getActiveNetwork);
                        networkCapabilities.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (messenger != null) {
                        _data.writeInt(Stub.TRANSACTION_getActiveNetwork);
                        messenger.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(timeoutSec);
                    _data.writeStrongBinder(binder);
                    _data.writeInt(legacy);
                    this.mRemote.transact(Stub.TRANSACTION_requestNetwork, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        networkRequest = (NetworkRequest) NetworkRequest.CREATOR.createFromParcel(_reply);
                    } else {
                        networkRequest = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return networkRequest;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NetworkRequest pendingRequestForNetwork(NetworkCapabilities networkCapabilities, PendingIntent operation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    NetworkRequest networkRequest;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (networkCapabilities != null) {
                        _data.writeInt(Stub.TRANSACTION_getActiveNetwork);
                        networkCapabilities.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (operation != null) {
                        _data.writeInt(Stub.TRANSACTION_getActiveNetwork);
                        operation.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_pendingRequestForNetwork, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        networkRequest = (NetworkRequest) NetworkRequest.CREATOR.createFromParcel(_reply);
                    } else {
                        networkRequest = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return networkRequest;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void releasePendingNetworkRequest(PendingIntent operation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (operation != null) {
                        _data.writeInt(Stub.TRANSACTION_getActiveNetwork);
                        operation.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_releasePendingNetworkRequest, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NetworkRequest listenForNetwork(NetworkCapabilities networkCapabilities, Messenger messenger, IBinder binder) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    NetworkRequest networkRequest;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (networkCapabilities != null) {
                        _data.writeInt(Stub.TRANSACTION_getActiveNetwork);
                        networkCapabilities.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (messenger != null) {
                        _data.writeInt(Stub.TRANSACTION_getActiveNetwork);
                        messenger.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(binder);
                    this.mRemote.transact(Stub.TRANSACTION_listenForNetwork, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        networkRequest = (NetworkRequest) NetworkRequest.CREATOR.createFromParcel(_reply);
                    } else {
                        networkRequest = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return networkRequest;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void pendingListenForNetwork(NetworkCapabilities networkCapabilities, PendingIntent operation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (networkCapabilities != null) {
                        _data.writeInt(Stub.TRANSACTION_getActiveNetwork);
                        networkCapabilities.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (operation != null) {
                        _data.writeInt(Stub.TRANSACTION_getActiveNetwork);
                        operation.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_pendingListenForNetwork, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void releaseNetworkRequest(NetworkRequest networkRequest) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (networkRequest != null) {
                        _data.writeInt(Stub.TRANSACTION_getActiveNetwork);
                        networkRequest.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_releaseNetworkRequest, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAcceptUnvalidated(Network network, boolean accept, boolean always) throws RemoteException {
                int i = Stub.TRANSACTION_getActiveNetwork;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (network != null) {
                        _data.writeInt(Stub.TRANSACTION_getActiveNetwork);
                        network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(accept ? Stub.TRANSACTION_getActiveNetwork : 0);
                    if (!always) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setAcceptUnvalidated, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getRestoreDefaultNetworkDelay(int networkType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(networkType);
                    this.mRemote.transact(Stub.TRANSACTION_getRestoreDefaultNetworkDelay, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean addVpnAddress(String address, int prefixLength) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(prefixLength);
                    this.mRemote.transact(Stub.TRANSACTION_addVpnAddress, _data, _reply, 0);
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

            public boolean removeVpnAddress(String address, int prefixLength) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(prefixLength);
                    this.mRemote.transact(Stub.TRANSACTION_removeVpnAddress, _data, _reply, 0);
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

            public boolean setUnderlyingNetworksForVpn(Network[] networks) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedArray(networks, 0);
                    this.mRemote.transact(Stub.TRANSACTION_setUnderlyingNetworksForVpn, _data, _reply, 0);
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

            public void startNattKeepalive(Network network, int intervalSeconds, Messenger messenger, IBinder binder, String srcAddr, int srcPort, String dstAddr) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (network != null) {
                        _data.writeInt(Stub.TRANSACTION_getActiveNetwork);
                        network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(intervalSeconds);
                    if (messenger != null) {
                        _data.writeInt(Stub.TRANSACTION_getActiveNetwork);
                        messenger.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(binder);
                    _data.writeString(srcAddr);
                    _data.writeInt(srcPort);
                    _data.writeString(dstAddr);
                    this.mRemote.transact(Stub.TRANSACTION_startNattKeepalive, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopKeepalive(Network network, int slot) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (network != null) {
                        _data.writeInt(Stub.TRANSACTION_getActiveNetwork);
                        network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(slot);
                    this.mRemote.transact(Stub.TRANSACTION_stopKeepalive, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getCaptivePortalServerUrl() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getCaptivePortalServerUrl, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setLteMobileDataEnabled(boolean enable) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (enable) {
                        i = Stub.TRANSACTION_getActiveNetwork;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setLteMobileDataEnabled, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int checkLteConnectState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_checkLteConnectState, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getLteTotalRxBytes() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getLteTotalRxBytes, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getLteTotalTxBytes() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getLteTotalTxBytes, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
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

        public static IConnectivityManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IConnectivityManager)) {
                return new Proxy(obj);
            }
            return (IConnectivityManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Network _result;
            NetworkInfo _result2;
            Network network;
            boolean _result3;
            LinkProperties _result4;
            int _result5;
            String[] _result6;
            ProxyInfo _result7;
            String _result8;
            Messenger messenger;
            NetworkCapabilities networkCapabilities;
            Messenger messenger2;
            NetworkRequest _result9;
            PendingIntent pendingIntent;
            long _result10;
            switch (code) {
                case TRANSACTION_getActiveNetwork /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getActiveNetwork();
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_getActiveNetwork);
                        _result.writeToParcel(reply, TRANSACTION_getActiveNetwork);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getActiveNetworkForUid /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getActiveNetworkForUid(data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_getActiveNetwork);
                        _result.writeToParcel(reply, TRANSACTION_getActiveNetwork);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getActiveNetworkInfo /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getActiveNetworkInfo();
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_getActiveNetwork);
                        _result2.writeToParcel(reply, TRANSACTION_getActiveNetwork);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getActiveNetworkInfoForUid /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getActiveNetworkInfoForUid(data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_getActiveNetwork);
                        _result2.writeToParcel(reply, TRANSACTION_getActiveNetwork);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getNetworkInfo /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getNetworkInfo(data.readInt());
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_getActiveNetwork);
                        _result2.writeToParcel(reply, TRANSACTION_getActiveNetwork);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getNetworkInfoForUid /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        network = (Network) Network.CREATOR.createFromParcel(data);
                    } else {
                        network = null;
                    }
                    _result2 = getNetworkInfoForUid(network, data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_getActiveNetwork);
                        _result2.writeToParcel(reply, TRANSACTION_getActiveNetwork);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getAllNetworkInfo /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    NetworkInfo[] _result11 = getAllNetworkInfo();
                    reply.writeNoException();
                    reply.writeTypedArray(_result11, TRANSACTION_getActiveNetwork);
                    return true;
                case TRANSACTION_getNetworkForType /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getNetworkForType(data.readInt());
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_getActiveNetwork);
                        _result.writeToParcel(reply, TRANSACTION_getActiveNetwork);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getAllNetworks /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    Network[] _result12 = getAllNetworks();
                    reply.writeNoException();
                    reply.writeTypedArray(_result12, TRANSACTION_getActiveNetwork);
                    return true;
                case TRANSACTION_getDefaultNetworkCapabilitiesForUser /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    NetworkCapabilities[] _result13 = getDefaultNetworkCapabilitiesForUser(data.readInt());
                    reply.writeNoException();
                    reply.writeTypedArray(_result13, TRANSACTION_getActiveNetwork);
                    return true;
                case TRANSACTION_isNetworkSupported /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isNetworkSupported(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getActiveNetwork : 0);
                    return true;
                case TRANSACTION_getActiveLinkProperties /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getActiveLinkProperties();
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_getActiveNetwork);
                        _result4.writeToParcel(reply, TRANSACTION_getActiveNetwork);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getLinkPropertiesForType /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getLinkPropertiesForType(data.readInt());
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_getActiveNetwork);
                        _result4.writeToParcel(reply, TRANSACTION_getActiveNetwork);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getLinkProperties /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        network = (Network) Network.CREATOR.createFromParcel(data);
                    } else {
                        network = null;
                    }
                    _result4 = getLinkProperties(network);
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_getActiveNetwork);
                        _result4.writeToParcel(reply, TRANSACTION_getActiveNetwork);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getNetworkCapabilities /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        network = (Network) Network.CREATOR.createFromParcel(data);
                    } else {
                        network = null;
                    }
                    NetworkCapabilities _result14 = getNetworkCapabilities(network);
                    reply.writeNoException();
                    if (_result14 != null) {
                        reply.writeInt(TRANSACTION_getActiveNetwork);
                        _result14.writeToParcel(reply, TRANSACTION_getActiveNetwork);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getAllNetworkState /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    NetworkState[] _result15 = getAllNetworkState();
                    reply.writeNoException();
                    reply.writeTypedArray(_result15, TRANSACTION_getActiveNetwork);
                    return true;
                case TRANSACTION_getActiveNetworkQuotaInfo /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    NetworkQuotaInfo _result16 = getActiveNetworkQuotaInfo();
                    reply.writeNoException();
                    if (_result16 != null) {
                        reply.writeInt(TRANSACTION_getActiveNetwork);
                        _result16.writeToParcel(reply, TRANSACTION_getActiveNetwork);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_isActiveNetworkMetered /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isActiveNetworkMetered();
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getActiveNetwork : 0);
                    return true;
                case TRANSACTION_requestRouteToHostAddress /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = requestRouteToHostAddress(data.readInt(), data.createByteArray());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getActiveNetwork : 0);
                    return true;
                case TRANSACTION_tether /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result5 = tether(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result5);
                    return true;
                case TRANSACTION_untether /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result5 = untether(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result5);
                    return true;
                case TRANSACTION_getLastTetherError /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result5 = getLastTetherError(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result5);
                    return true;
                case TRANSACTION_isTetheringSupported /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = isTetheringSupported();
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getActiveNetwork : 0);
                    return true;
                case TRANSACTION_startTethering /*24*/:
                    ResultReceiver resultReceiver;
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        resultReceiver = (ResultReceiver) ResultReceiver.CREATOR.createFromParcel(data);
                    } else {
                        resultReceiver = null;
                    }
                    startTethering(_arg0, resultReceiver, data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_stopTethering /*25*/:
                    data.enforceInterface(DESCRIPTOR);
                    stopTethering(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getTetherableIfaces /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result6 = getTetherableIfaces();
                    reply.writeNoException();
                    reply.writeStringArray(_result6);
                    return true;
                case TRANSACTION_getTetheredIfaces /*27*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result6 = getTetheredIfaces();
                    reply.writeNoException();
                    reply.writeStringArray(_result6);
                    return true;
                case TRANSACTION_getTetheringErroredIfaces /*28*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result6 = getTetheringErroredIfaces();
                    reply.writeNoException();
                    reply.writeStringArray(_result6);
                    return true;
                case TRANSACTION_getTetheredDhcpRanges /*29*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result6 = getTetheredDhcpRanges();
                    reply.writeNoException();
                    reply.writeStringArray(_result6);
                    return true;
                case TRANSACTION_getTetherableUsbRegexs /*30*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result6 = getTetherableUsbRegexs();
                    reply.writeNoException();
                    reply.writeStringArray(_result6);
                    return true;
                case TRANSACTION_getTetherableWifiRegexs /*31*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result6 = getTetherableWifiRegexs();
                    reply.writeNoException();
                    reply.writeStringArray(_result6);
                    return true;
                case TRANSACTION_getTetherableBluetoothRegexs /*32*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result6 = getTetherableBluetoothRegexs();
                    reply.writeNoException();
                    reply.writeStringArray(_result6);
                    return true;
                case TRANSACTION_setUsbTethering /*33*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result5 = setUsbTethering(data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result5);
                    return true;
                case TRANSACTION_reportInetCondition /*34*/:
                    data.enforceInterface(DESCRIPTOR);
                    reportInetCondition(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_reportNetworkConnectivity /*35*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        network = (Network) Network.CREATOR.createFromParcel(data);
                    } else {
                        network = null;
                    }
                    reportNetworkConnectivity(network, data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getGlobalProxy /*36*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result7 = getGlobalProxy();
                    reply.writeNoException();
                    if (_result7 != null) {
                        reply.writeInt(TRANSACTION_getActiveNetwork);
                        _result7.writeToParcel(reply, TRANSACTION_getActiveNetwork);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_setGlobalProxy /*37*/:
                    ProxyInfo proxyInfo;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        proxyInfo = (ProxyInfo) ProxyInfo.CREATOR.createFromParcel(data);
                    } else {
                        proxyInfo = null;
                    }
                    setGlobalProxy(proxyInfo);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getProxyForNetwork /*38*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        network = (Network) Network.CREATOR.createFromParcel(data);
                    } else {
                        network = null;
                    }
                    _result7 = getProxyForNetwork(network);
                    reply.writeNoException();
                    if (_result7 != null) {
                        reply.writeInt(TRANSACTION_getActiveNetwork);
                        _result7.writeToParcel(reply, TRANSACTION_getActiveNetwork);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_prepareVpn /*39*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = prepareVpn(data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getActiveNetwork : 0);
                    return true;
                case TRANSACTION_setVpnPackageAuthorization /*40*/:
                    data.enforceInterface(DESCRIPTOR);
                    setVpnPackageAuthorization(data.readString(), data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_establishVpn /*41*/:
                    VpnConfig vpnConfig;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        vpnConfig = (VpnConfig) VpnConfig.CREATOR.createFromParcel(data);
                    } else {
                        vpnConfig = null;
                    }
                    ParcelFileDescriptor _result17 = establishVpn(vpnConfig);
                    reply.writeNoException();
                    if (_result17 != null) {
                        reply.writeInt(TRANSACTION_getActiveNetwork);
                        _result17.writeToParcel(reply, TRANSACTION_getActiveNetwork);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getVpnConfig /*42*/:
                    data.enforceInterface(DESCRIPTOR);
                    VpnConfig _result18 = getVpnConfig(data.readInt());
                    reply.writeNoException();
                    if (_result18 != null) {
                        reply.writeInt(TRANSACTION_getActiveNetwork);
                        _result18.writeToParcel(reply, TRANSACTION_getActiveNetwork);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_startLegacyVpn /*43*/:
                    VpnProfile vpnProfile;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        vpnProfile = (VpnProfile) VpnProfile.CREATOR.createFromParcel(data);
                    } else {
                        vpnProfile = null;
                    }
                    startLegacyVpn(vpnProfile);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getLegacyVpnInfo /*44*/:
                    data.enforceInterface(DESCRIPTOR);
                    LegacyVpnInfo _result19 = getLegacyVpnInfo(data.readInt());
                    reply.writeNoException();
                    if (_result19 != null) {
                        reply.writeInt(TRANSACTION_getActiveNetwork);
                        _result19.writeToParcel(reply, TRANSACTION_getActiveNetwork);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getAllVpnInfo /*45*/:
                    data.enforceInterface(DESCRIPTOR);
                    VpnInfo[] _result20 = getAllVpnInfo();
                    reply.writeNoException();
                    reply.writeTypedArray(_result20, TRANSACTION_getActiveNetwork);
                    return true;
                case TRANSACTION_updateLockdownVpn /*46*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = updateLockdownVpn();
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getActiveNetwork : 0);
                    return true;
                case TRANSACTION_setAlwaysOnVpnPackage /*47*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setAlwaysOnVpnPackage(data.readInt(), data.readString(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getActiveNetwork : 0);
                    return true;
                case TRANSACTION_getAlwaysOnVpnPackage /*48*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result8 = getAlwaysOnVpnPackage(data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result8);
                    return true;
                case TRANSACTION_checkMobileProvisioning /*49*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result5 = checkMobileProvisioning(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result5);
                    return true;
                case TRANSACTION_getMobileProvisioningUrl /*50*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result8 = getMobileProvisioningUrl();
                    reply.writeNoException();
                    reply.writeString(_result8);
                    return true;
                case TRANSACTION_setProvisioningNotificationVisible /*51*/:
                    data.enforceInterface(DESCRIPTOR);
                    setProvisioningNotificationVisible(data.readInt() != 0, data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setAirplaneMode /*52*/:
                    data.enforceInterface(DESCRIPTOR);
                    setAirplaneMode(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_registerNetworkFactory /*53*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        messenger = (Messenger) Messenger.CREATOR.createFromParcel(data);
                    } else {
                        messenger = null;
                    }
                    registerNetworkFactory(messenger, data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_requestBandwidthUpdate /*54*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        network = (Network) Network.CREATOR.createFromParcel(data);
                    } else {
                        network = null;
                    }
                    _result3 = requestBandwidthUpdate(network);
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getActiveNetwork : 0);
                    return true;
                case TRANSACTION_unregisterNetworkFactory /*55*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        messenger = (Messenger) Messenger.CREATOR.createFromParcel(data);
                    } else {
                        messenger = null;
                    }
                    unregisterNetworkFactory(messenger);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_registerNetworkAgent /*56*/:
                    NetworkInfo networkInfo;
                    LinkProperties linkProperties;
                    NetworkCapabilities networkCapabilities2;
                    NetworkMisc networkMisc;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        messenger = (Messenger) Messenger.CREATOR.createFromParcel(data);
                    } else {
                        messenger = null;
                    }
                    if (data.readInt() != 0) {
                        networkInfo = (NetworkInfo) NetworkInfo.CREATOR.createFromParcel(data);
                    } else {
                        networkInfo = null;
                    }
                    if (data.readInt() != 0) {
                        linkProperties = (LinkProperties) LinkProperties.CREATOR.createFromParcel(data);
                    } else {
                        linkProperties = null;
                    }
                    if (data.readInt() != 0) {
                        networkCapabilities2 = (NetworkCapabilities) NetworkCapabilities.CREATOR.createFromParcel(data);
                    } else {
                        networkCapabilities2 = null;
                    }
                    int _arg4 = data.readInt();
                    if (data.readInt() != 0) {
                        networkMisc = (NetworkMisc) NetworkMisc.CREATOR.createFromParcel(data);
                    } else {
                        networkMisc = null;
                    }
                    _result5 = registerNetworkAgent(messenger, networkInfo, linkProperties, networkCapabilities2, _arg4, networkMisc);
                    reply.writeNoException();
                    reply.writeInt(_result5);
                    return true;
                case TRANSACTION_requestNetwork /*57*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        networkCapabilities = (NetworkCapabilities) NetworkCapabilities.CREATOR.createFromParcel(data);
                    } else {
                        networkCapabilities = null;
                    }
                    if (data.readInt() != 0) {
                        messenger2 = (Messenger) Messenger.CREATOR.createFromParcel(data);
                    } else {
                        messenger2 = null;
                    }
                    _result9 = requestNetwork(networkCapabilities, messenger2, data.readInt(), data.readStrongBinder(), data.readInt());
                    reply.writeNoException();
                    if (_result9 != null) {
                        reply.writeInt(TRANSACTION_getActiveNetwork);
                        _result9.writeToParcel(reply, TRANSACTION_getActiveNetwork);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_pendingRequestForNetwork /*58*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        networkCapabilities = (NetworkCapabilities) NetworkCapabilities.CREATOR.createFromParcel(data);
                    } else {
                        networkCapabilities = null;
                    }
                    if (data.readInt() != 0) {
                        pendingIntent = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        pendingIntent = null;
                    }
                    _result9 = pendingRequestForNetwork(networkCapabilities, pendingIntent);
                    reply.writeNoException();
                    if (_result9 != null) {
                        reply.writeInt(TRANSACTION_getActiveNetwork);
                        _result9.writeToParcel(reply, TRANSACTION_getActiveNetwork);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_releasePendingNetworkRequest /*59*/:
                    PendingIntent pendingIntent2;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        pendingIntent2 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        pendingIntent2 = null;
                    }
                    releasePendingNetworkRequest(pendingIntent2);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_listenForNetwork /*60*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        networkCapabilities = (NetworkCapabilities) NetworkCapabilities.CREATOR.createFromParcel(data);
                    } else {
                        networkCapabilities = null;
                    }
                    if (data.readInt() != 0) {
                        messenger2 = (Messenger) Messenger.CREATOR.createFromParcel(data);
                    } else {
                        messenger2 = null;
                    }
                    _result9 = listenForNetwork(networkCapabilities, messenger2, data.readStrongBinder());
                    reply.writeNoException();
                    if (_result9 != null) {
                        reply.writeInt(TRANSACTION_getActiveNetwork);
                        _result9.writeToParcel(reply, TRANSACTION_getActiveNetwork);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_pendingListenForNetwork /*61*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        networkCapabilities = (NetworkCapabilities) NetworkCapabilities.CREATOR.createFromParcel(data);
                    } else {
                        networkCapabilities = null;
                    }
                    if (data.readInt() != 0) {
                        pendingIntent = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        pendingIntent = null;
                    }
                    pendingListenForNetwork(networkCapabilities, pendingIntent);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_releaseNetworkRequest /*62*/:
                    NetworkRequest networkRequest;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        networkRequest = (NetworkRequest) NetworkRequest.CREATOR.createFromParcel(data);
                    } else {
                        networkRequest = null;
                    }
                    releaseNetworkRequest(networkRequest);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setAcceptUnvalidated /*63*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        network = (Network) Network.CREATOR.createFromParcel(data);
                    } else {
                        network = null;
                    }
                    setAcceptUnvalidated(network, data.readInt() != 0, data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getRestoreDefaultNetworkDelay /*64*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result5 = getRestoreDefaultNetworkDelay(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result5);
                    return true;
                case TRANSACTION_addVpnAddress /*65*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = addVpnAddress(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getActiveNetwork : 0);
                    return true;
                case TRANSACTION_removeVpnAddress /*66*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = removeVpnAddress(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getActiveNetwork : 0);
                    return true;
                case TRANSACTION_setUnderlyingNetworksForVpn /*67*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setUnderlyingNetworksForVpn((Network[]) data.createTypedArray(Network.CREATOR));
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getActiveNetwork : 0);
                    return true;
                case TRANSACTION_factoryReset /*68*/:
                    data.enforceInterface(DESCRIPTOR);
                    factoryReset();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_startNattKeepalive /*69*/:
                    Messenger messenger3;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        network = (Network) Network.CREATOR.createFromParcel(data);
                    } else {
                        network = null;
                    }
                    int _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        messenger3 = (Messenger) Messenger.CREATOR.createFromParcel(data);
                    } else {
                        messenger3 = null;
                    }
                    startNattKeepalive(network, _arg1, messenger3, data.readStrongBinder(), data.readString(), data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_stopKeepalive /*70*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        network = (Network) Network.CREATOR.createFromParcel(data);
                    } else {
                        network = null;
                    }
                    stopKeepalive(network, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getCaptivePortalServerUrl /*71*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result8 = getCaptivePortalServerUrl();
                    reply.writeNoException();
                    reply.writeString(_result8);
                    return true;
                case TRANSACTION_setLteMobileDataEnabled /*72*/:
                    data.enforceInterface(DESCRIPTOR);
                    setLteMobileDataEnabled(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_checkLteConnectState /*73*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result5 = checkLteConnectState();
                    reply.writeNoException();
                    reply.writeInt(_result5);
                    return true;
                case TRANSACTION_getLteTotalRxBytes /*74*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result10 = getLteTotalRxBytes();
                    reply.writeNoException();
                    reply.writeLong(_result10);
                    return true;
                case TRANSACTION_getLteTotalTxBytes /*75*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result10 = getLteTotalTxBytes();
                    reply.writeNoException();
                    reply.writeLong(_result10);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    boolean addVpnAddress(String str, int i) throws RemoteException;

    int checkLteConnectState() throws RemoteException;

    int checkMobileProvisioning(int i) throws RemoteException;

    ParcelFileDescriptor establishVpn(VpnConfig vpnConfig) throws RemoteException;

    void factoryReset() throws RemoteException;

    LinkProperties getActiveLinkProperties() throws RemoteException;

    Network getActiveNetwork() throws RemoteException;

    Network getActiveNetworkForUid(int i, boolean z) throws RemoteException;

    NetworkInfo getActiveNetworkInfo() throws RemoteException;

    NetworkInfo getActiveNetworkInfoForUid(int i, boolean z) throws RemoteException;

    NetworkQuotaInfo getActiveNetworkQuotaInfo() throws RemoteException;

    NetworkInfo[] getAllNetworkInfo() throws RemoteException;

    NetworkState[] getAllNetworkState() throws RemoteException;

    Network[] getAllNetworks() throws RemoteException;

    VpnInfo[] getAllVpnInfo() throws RemoteException;

    String getAlwaysOnVpnPackage(int i) throws RemoteException;

    String getCaptivePortalServerUrl() throws RemoteException;

    NetworkCapabilities[] getDefaultNetworkCapabilitiesForUser(int i) throws RemoteException;

    ProxyInfo getGlobalProxy() throws RemoteException;

    int getLastTetherError(String str) throws RemoteException;

    LegacyVpnInfo getLegacyVpnInfo(int i) throws RemoteException;

    LinkProperties getLinkProperties(Network network) throws RemoteException;

    LinkProperties getLinkPropertiesForType(int i) throws RemoteException;

    long getLteTotalRxBytes() throws RemoteException;

    long getLteTotalTxBytes() throws RemoteException;

    String getMobileProvisioningUrl() throws RemoteException;

    NetworkCapabilities getNetworkCapabilities(Network network) throws RemoteException;

    Network getNetworkForType(int i) throws RemoteException;

    NetworkInfo getNetworkInfo(int i) throws RemoteException;

    NetworkInfo getNetworkInfoForUid(Network network, int i, boolean z) throws RemoteException;

    ProxyInfo getProxyForNetwork(Network network) throws RemoteException;

    int getRestoreDefaultNetworkDelay(int i) throws RemoteException;

    String[] getTetherableBluetoothRegexs() throws RemoteException;

    String[] getTetherableIfaces() throws RemoteException;

    String[] getTetherableUsbRegexs() throws RemoteException;

    String[] getTetherableWifiRegexs() throws RemoteException;

    String[] getTetheredDhcpRanges() throws RemoteException;

    String[] getTetheredIfaces() throws RemoteException;

    String[] getTetheringErroredIfaces() throws RemoteException;

    VpnConfig getVpnConfig(int i) throws RemoteException;

    boolean isActiveNetworkMetered() throws RemoteException;

    boolean isNetworkSupported(int i) throws RemoteException;

    boolean isTetheringSupported() throws RemoteException;

    NetworkRequest listenForNetwork(NetworkCapabilities networkCapabilities, Messenger messenger, IBinder iBinder) throws RemoteException;

    void pendingListenForNetwork(NetworkCapabilities networkCapabilities, PendingIntent pendingIntent) throws RemoteException;

    NetworkRequest pendingRequestForNetwork(NetworkCapabilities networkCapabilities, PendingIntent pendingIntent) throws RemoteException;

    boolean prepareVpn(String str, String str2, int i) throws RemoteException;

    int registerNetworkAgent(Messenger messenger, NetworkInfo networkInfo, LinkProperties linkProperties, NetworkCapabilities networkCapabilities, int i, NetworkMisc networkMisc) throws RemoteException;

    void registerNetworkFactory(Messenger messenger, String str) throws RemoteException;

    void releaseNetworkRequest(NetworkRequest networkRequest) throws RemoteException;

    void releasePendingNetworkRequest(PendingIntent pendingIntent) throws RemoteException;

    boolean removeVpnAddress(String str, int i) throws RemoteException;

    void reportInetCondition(int i, int i2) throws RemoteException;

    void reportNetworkConnectivity(Network network, boolean z) throws RemoteException;

    boolean requestBandwidthUpdate(Network network) throws RemoteException;

    NetworkRequest requestNetwork(NetworkCapabilities networkCapabilities, Messenger messenger, int i, IBinder iBinder, int i2) throws RemoteException;

    boolean requestRouteToHostAddress(int i, byte[] bArr) throws RemoteException;

    void setAcceptUnvalidated(Network network, boolean z, boolean z2) throws RemoteException;

    void setAirplaneMode(boolean z) throws RemoteException;

    boolean setAlwaysOnVpnPackage(int i, String str, boolean z) throws RemoteException;

    void setGlobalProxy(ProxyInfo proxyInfo) throws RemoteException;

    void setLteMobileDataEnabled(boolean z) throws RemoteException;

    void setProvisioningNotificationVisible(boolean z, int i, String str) throws RemoteException;

    boolean setUnderlyingNetworksForVpn(Network[] networkArr) throws RemoteException;

    int setUsbTethering(boolean z) throws RemoteException;

    void setVpnPackageAuthorization(String str, int i, boolean z) throws RemoteException;

    void startLegacyVpn(VpnProfile vpnProfile) throws RemoteException;

    void startNattKeepalive(Network network, int i, Messenger messenger, IBinder iBinder, String str, int i2, String str2) throws RemoteException;

    void startTethering(int i, ResultReceiver resultReceiver, boolean z) throws RemoteException;

    void stopKeepalive(Network network, int i) throws RemoteException;

    void stopTethering(int i) throws RemoteException;

    int tether(String str) throws RemoteException;

    void unregisterNetworkFactory(Messenger messenger) throws RemoteException;

    int untether(String str) throws RemoteException;

    boolean updateLockdownVpn() throws RemoteException;
}
