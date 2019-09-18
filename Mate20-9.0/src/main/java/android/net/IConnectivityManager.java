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
        static final int TRANSACTION_addVpnAddress = 69;
        static final int TRANSACTION_checkMobileProvisioning = 50;
        static final int TRANSACTION_establishVpn = 41;
        static final int TRANSACTION_factoryReset = 72;
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
        static final int TRANSACTION_getAlwaysOnVpnPackage = 49;
        static final int TRANSACTION_getCaptivePortalServerUrl = 75;
        static final int TRANSACTION_getDefaultNetworkCapabilitiesForUser = 10;
        static final int TRANSACTION_getGlobalProxy = 36;
        static final int TRANSACTION_getLastTetherError = 22;
        static final int TRANSACTION_getLegacyVpnInfo = 44;
        static final int TRANSACTION_getLinkProperties = 14;
        static final int TRANSACTION_getLinkPropertiesForType = 13;
        static final int TRANSACTION_getMobileProvisioningUrl = 51;
        static final int TRANSACTION_getMultipathPreference = 67;
        static final int TRANSACTION_getNetworkCapabilities = 15;
        static final int TRANSACTION_getNetworkForType = 8;
        static final int TRANSACTION_getNetworkInfo = 5;
        static final int TRANSACTION_getNetworkInfoForUid = 6;
        static final int TRANSACTION_getNetworkWatchlistConfigHash = 76;
        static final int TRANSACTION_getProxyForNetwork = 38;
        static final int TRANSACTION_getRestoreDefaultNetworkDelay = 68;
        static final int TRANSACTION_getTetherableBluetoothRegexs = 32;
        static final int TRANSACTION_getTetherableIfaces = 26;
        static final int TRANSACTION_getTetherableUsbRegexs = 30;
        static final int TRANSACTION_getTetherableWifiRegexs = 31;
        static final int TRANSACTION_getTetheredDhcpRanges = 29;
        static final int TRANSACTION_getTetheredIfaces = 27;
        static final int TRANSACTION_getTetheringErroredIfaces = 28;
        static final int TRANSACTION_getVpnConfig = 42;
        static final int TRANSACTION_isActiveNetworkMetered = 18;
        static final int TRANSACTION_isAlwaysOnVpnPackageSupported = 47;
        static final int TRANSACTION_isNetworkSupported = 11;
        static final int TRANSACTION_isTetheringSupported = 23;
        static final int TRANSACTION_listenForNetwork = 61;
        static final int TRANSACTION_pendingListenForNetwork = 62;
        static final int TRANSACTION_pendingRequestForNetwork = 59;
        static final int TRANSACTION_prepareVpn = 39;
        static final int TRANSACTION_registerNetworkAgent = 57;
        static final int TRANSACTION_registerNetworkFactory = 54;
        static final int TRANSACTION_releaseNetworkRequest = 63;
        static final int TRANSACTION_releasePendingNetworkRequest = 60;
        static final int TRANSACTION_removeVpnAddress = 70;
        static final int TRANSACTION_reportInetCondition = 34;
        static final int TRANSACTION_reportNetworkConnectivity = 35;
        static final int TRANSACTION_requestBandwidthUpdate = 55;
        static final int TRANSACTION_requestNetwork = 58;
        static final int TRANSACTION_requestRouteToHostAddress = 19;
        static final int TRANSACTION_setAcceptUnvalidated = 64;
        static final int TRANSACTION_setAirplaneMode = 53;
        static final int TRANSACTION_setAlwaysOnVpnPackage = 48;
        static final int TRANSACTION_setAvoidUnvalidated = 65;
        static final int TRANSACTION_setGlobalProxy = 37;
        static final int TRANSACTION_setProvisioningNotificationVisible = 52;
        static final int TRANSACTION_setUnderlyingNetworksForVpn = 71;
        static final int TRANSACTION_setUsbTethering = 33;
        static final int TRANSACTION_setVpnPackageAuthorization = 40;
        static final int TRANSACTION_startCaptivePortalApp = 66;
        static final int TRANSACTION_startLegacyVpn = 43;
        static final int TRANSACTION_startNattKeepalive = 73;
        static final int TRANSACTION_startTethering = 24;
        static final int TRANSACTION_stopKeepalive = 74;
        static final int TRANSACTION_stopTethering = 25;
        static final int TRANSACTION_tether = 20;
        static final int TRANSACTION_turnOffVpn = 77;
        static final int TRANSACTION_unregisterNetworkFactory = 56;
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
                Network _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Network.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Network getActiveNetworkForUid(int uid, boolean ignoreBlocked) throws RemoteException {
                Network _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(ignoreBlocked);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Network.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NetworkInfo getActiveNetworkInfo() throws RemoteException {
                NetworkInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NetworkInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NetworkInfo getActiveNetworkInfoForUid(int uid, boolean ignoreBlocked) throws RemoteException {
                NetworkInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(ignoreBlocked);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NetworkInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NetworkInfo getNetworkInfo(int networkType) throws RemoteException {
                NetworkInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(networkType);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NetworkInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NetworkInfo getNetworkInfoForUid(Network network, int uid, boolean ignoreBlocked) throws RemoteException {
                NetworkInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (network != null) {
                        _data.writeInt(1);
                        network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(uid);
                    _data.writeInt(ignoreBlocked);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NetworkInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NetworkInfo[] getAllNetworkInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    return (NetworkInfo[]) _reply.createTypedArray(NetworkInfo.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Network getNetworkForType(int networkType) throws RemoteException {
                Network _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(networkType);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Network.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Network[] getAllNetworks() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    return (Network[]) _reply.createTypedArray(Network.CREATOR);
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
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    return (NetworkCapabilities[]) _reply.createTypedArray(NetworkCapabilities.CREATOR);
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

            public LinkProperties getActiveLinkProperties() throws RemoteException {
                LinkProperties _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = LinkProperties.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public LinkProperties getLinkPropertiesForType(int networkType) throws RemoteException {
                LinkProperties _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(networkType);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = LinkProperties.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public LinkProperties getLinkProperties(Network network) throws RemoteException {
                LinkProperties _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (network != null) {
                        _data.writeInt(1);
                        network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = LinkProperties.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NetworkCapabilities getNetworkCapabilities(Network network) throws RemoteException {
                NetworkCapabilities _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (network != null) {
                        _data.writeInt(1);
                        network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NetworkCapabilities.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NetworkState[] getAllNetworkState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    return (NetworkState[]) _reply.createTypedArray(NetworkState.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NetworkQuotaInfo getActiveNetworkQuotaInfo() throws RemoteException {
                NetworkQuotaInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NetworkQuotaInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isActiveNetworkMetered() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
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

            public boolean requestRouteToHostAddress(int networkType, byte[] hostAddress) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(networkType);
                    _data.writeByteArray(hostAddress);
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

            public int tether(String iface, String callerPkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    _data.writeString(callerPkg);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int untether(String iface, String callerPkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    _data.writeString(callerPkg);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
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
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isTetheringSupported(String callerPkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPkg);
                    boolean _result = false;
                    this.mRemote.transact(23, _data, _reply, 0);
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

            public void startTethering(int type, ResultReceiver receiver, boolean showProvisioningUi, String callerPkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    if (receiver != null) {
                        _data.writeInt(1);
                        receiver.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(showProvisioningUi);
                    _data.writeString(callerPkg);
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopTethering(int type, String callerPkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeString(callerPkg);
                    this.mRemote.transact(25, _data, _reply, 0);
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
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
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
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
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
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
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
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
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
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
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
                    this.mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
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
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setUsbTethering(boolean enable, String callerPkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable);
                    _data.writeString(callerPkg);
                    this.mRemote.transact(33, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
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
                    this.mRemote.transact(34, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reportNetworkConnectivity(Network network, boolean hasConnectivity) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (network != null) {
                        _data.writeInt(1);
                        network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(hasConnectivity);
                    this.mRemote.transact(35, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ProxyInfo getGlobalProxy() throws RemoteException {
                ProxyInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(36, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ProxyInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
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
                        _data.writeInt(1);
                        p.writeToParcel(_data, 0);
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

            public ProxyInfo getProxyForNetwork(Network nework) throws RemoteException {
                ProxyInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (nework != null) {
                        _data.writeInt(1);
                        nework.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(38, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ProxyInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
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
                    boolean _result = false;
                    this.mRemote.transact(39, _data, _reply, 0);
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

            public void setVpnPackageAuthorization(String packageName, int userId, boolean authorized) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    _data.writeInt(authorized);
                    this.mRemote.transact(40, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParcelFileDescriptor establishVpn(VpnConfig config) throws RemoteException {
                ParcelFileDescriptor _result;
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
                    this.mRemote.transact(41, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public VpnConfig getVpnConfig(int userId) throws RemoteException {
                VpnConfig _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(42, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (VpnConfig) VpnConfig.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
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
                        _data.writeInt(1);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(43, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public LegacyVpnInfo getLegacyVpnInfo(int userId) throws RemoteException {
                LegacyVpnInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(44, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (LegacyVpnInfo) LegacyVpnInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public VpnInfo[] getAllVpnInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(45, _data, _reply, 0);
                    _reply.readException();
                    return (VpnInfo[]) _reply.createTypedArray(VpnInfo.CREATOR);
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
                    boolean _result = false;
                    this.mRemote.transact(46, _data, _reply, 0);
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

            public boolean isAlwaysOnVpnPackageSupported(int userId, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeString(packageName);
                    boolean _result = false;
                    this.mRemote.transact(47, _data, _reply, 0);
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

            public boolean setAlwaysOnVpnPackage(int userId, String packageName, boolean lockdown) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeString(packageName);
                    _data.writeInt(lockdown);
                    boolean _result = false;
                    this.mRemote.transact(48, _data, _reply, 0);
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

            public String getAlwaysOnVpnPackage(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(49, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
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
                    this.mRemote.transact(50, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
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
                    this.mRemote.transact(51, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setProvisioningNotificationVisible(boolean visible, int networkType, String action) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(visible);
                    _data.writeInt(networkType);
                    _data.writeString(action);
                    this.mRemote.transact(52, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAirplaneMode(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable);
                    this.mRemote.transact(53, _data, _reply, 0);
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
                        _data.writeInt(1);
                        messenger.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(name);
                    this.mRemote.transact(54, _data, _reply, 0);
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
                    boolean _result = true;
                    if (network != null) {
                        _data.writeInt(1);
                        network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(55, _data, _reply, 0);
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

            public void unregisterNetworkFactory(Messenger messenger) throws RemoteException {
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
                    this.mRemote.transact(56, _data, _reply, 0);
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
                        _data.writeInt(1);
                        messenger.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (ni != null) {
                        _data.writeInt(1);
                        ni.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (lp != null) {
                        _data.writeInt(1);
                        lp.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (nc != null) {
                        _data.writeInt(1);
                        nc.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(score);
                    if (misc != null) {
                        _data.writeInt(1);
                        misc.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(57, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NetworkRequest requestNetwork(NetworkCapabilities networkCapabilities, Messenger messenger, int timeoutSec, IBinder binder, int legacy) throws RemoteException {
                NetworkRequest _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (networkCapabilities != null) {
                        _data.writeInt(1);
                        networkCapabilities.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (messenger != null) {
                        _data.writeInt(1);
                        messenger.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(timeoutSec);
                    _data.writeStrongBinder(binder);
                    _data.writeInt(legacy);
                    this.mRemote.transact(58, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NetworkRequest.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NetworkRequest pendingRequestForNetwork(NetworkCapabilities networkCapabilities, PendingIntent operation) throws RemoteException {
                NetworkRequest _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (networkCapabilities != null) {
                        _data.writeInt(1);
                        networkCapabilities.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (operation != null) {
                        _data.writeInt(1);
                        operation.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(59, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NetworkRequest.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
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
                        _data.writeInt(1);
                        operation.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(60, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NetworkRequest listenForNetwork(NetworkCapabilities networkCapabilities, Messenger messenger, IBinder binder) throws RemoteException {
                NetworkRequest _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (networkCapabilities != null) {
                        _data.writeInt(1);
                        networkCapabilities.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (messenger != null) {
                        _data.writeInt(1);
                        messenger.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(binder);
                    this.mRemote.transact(61, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NetworkRequest.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
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
                        _data.writeInt(1);
                        networkCapabilities.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (operation != null) {
                        _data.writeInt(1);
                        operation.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(62, _data, _reply, 0);
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
                        _data.writeInt(1);
                        networkRequest.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(63, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAcceptUnvalidated(Network network, boolean accept, boolean always) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (network != null) {
                        _data.writeInt(1);
                        network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(accept);
                    _data.writeInt(always);
                    this.mRemote.transact(64, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAvoidUnvalidated(Network network) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (network != null) {
                        _data.writeInt(1);
                        network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(65, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startCaptivePortalApp(Network network) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (network != null) {
                        _data.writeInt(1);
                        network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(66, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getMultipathPreference(Network Network) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (Network != null) {
                        _data.writeInt(1);
                        Network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(67, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
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
                    this.mRemote.transact(68, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
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
                    boolean _result = false;
                    this.mRemote.transact(69, _data, _reply, 0);
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

            public boolean removeVpnAddress(String address, int prefixLength) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(prefixLength);
                    boolean _result = false;
                    this.mRemote.transact(70, _data, _reply, 0);
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

            public boolean setUnderlyingNetworksForVpn(Network[] networks) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    _data.writeTypedArray(networks, 0);
                    this.mRemote.transact(71, _data, _reply, 0);
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

            public void factoryReset() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(72, _data, _reply, 0);
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
                        _data.writeInt(1);
                        network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(intervalSeconds);
                    if (messenger != null) {
                        _data.writeInt(1);
                        messenger.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(binder);
                    _data.writeString(srcAddr);
                    _data.writeInt(srcPort);
                    _data.writeString(dstAddr);
                    this.mRemote.transact(73, _data, _reply, 0);
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
                        _data.writeInt(1);
                        network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(slot);
                    this.mRemote.transact(74, _data, _reply, 0);
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
                    this.mRemote.transact(75, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] getNetworkWatchlistConfigHash() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(76, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createByteArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean turnOffVpn(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    boolean _result = false;
                    this.mRemote.transact(77, _data, _reply, 0);
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
            Messenger _arg0;
            NetworkInfo _arg1;
            LinkProperties _arg2;
            NetworkCapabilities _arg3;
            NetworkCapabilities _arg02;
            NetworkCapabilities _arg03;
            NetworkCapabilities _arg04;
            NetworkCapabilities _arg05;
            Network _arg06;
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                boolean _arg22 = false;
                VpnConfig _arg07 = null;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        Network _result = getActiveNetwork();
                        reply.writeNoException();
                        if (_result != null) {
                            parcel2.writeInt(1);
                            _result.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        Network _result2 = getActiveNetworkForUid(data.readInt(), data.readInt() != 0);
                        reply.writeNoException();
                        if (_result2 != null) {
                            parcel2.writeInt(1);
                            _result2.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        NetworkInfo _result3 = getActiveNetworkInfo();
                        reply.writeNoException();
                        if (_result3 != null) {
                            parcel2.writeInt(1);
                            _result3.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        NetworkInfo _result4 = getActiveNetworkInfoForUid(data.readInt(), data.readInt() != 0);
                        reply.writeNoException();
                        if (_result4 != null) {
                            parcel2.writeInt(1);
                            _result4.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        NetworkInfo _result5 = getNetworkInfo(data.readInt());
                        reply.writeNoException();
                        if (_result5 != null) {
                            parcel2.writeInt(1);
                            _result5.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = (Network) Network.CREATOR.createFromParcel(parcel);
                        }
                        NetworkInfo _result6 = getNetworkInfoForUid(_arg07, data.readInt(), data.readInt() != 0);
                        reply.writeNoException();
                        if (_result6 != null) {
                            parcel2.writeInt(1);
                            _result6.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        NetworkInfo[] _result7 = getAllNetworkInfo();
                        reply.writeNoException();
                        parcel2.writeTypedArray(_result7, 1);
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        Network _result8 = getNetworkForType(data.readInt());
                        reply.writeNoException();
                        if (_result8 != null) {
                            parcel2.writeInt(1);
                            _result8.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        Network[] _result9 = getAllNetworks();
                        reply.writeNoException();
                        parcel2.writeTypedArray(_result9, 1);
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        NetworkCapabilities[] _result10 = getDefaultNetworkCapabilitiesForUser(data.readInt());
                        reply.writeNoException();
                        parcel2.writeTypedArray(_result10, 1);
                        return true;
                    case 11:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result11 = isNetworkSupported(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result11);
                        return true;
                    case 12:
                        parcel.enforceInterface(DESCRIPTOR);
                        LinkProperties _result12 = getActiveLinkProperties();
                        reply.writeNoException();
                        if (_result12 != null) {
                            parcel2.writeInt(1);
                            _result12.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 13:
                        parcel.enforceInterface(DESCRIPTOR);
                        LinkProperties _result13 = getLinkPropertiesForType(data.readInt());
                        reply.writeNoException();
                        if (_result13 != null) {
                            parcel2.writeInt(1);
                            _result13.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 14:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = (Network) Network.CREATOR.createFromParcel(parcel);
                        }
                        LinkProperties _result14 = getLinkProperties(_arg07);
                        reply.writeNoException();
                        if (_result14 != null) {
                            parcel2.writeInt(1);
                            _result14.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 15:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = (Network) Network.CREATOR.createFromParcel(parcel);
                        }
                        NetworkCapabilities _result15 = getNetworkCapabilities(_arg07);
                        reply.writeNoException();
                        if (_result15 != null) {
                            parcel2.writeInt(1);
                            _result15.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 16:
                        parcel.enforceInterface(DESCRIPTOR);
                        NetworkState[] _result16 = getAllNetworkState();
                        reply.writeNoException();
                        parcel2.writeTypedArray(_result16, 1);
                        return true;
                    case 17:
                        parcel.enforceInterface(DESCRIPTOR);
                        NetworkQuotaInfo _result17 = getActiveNetworkQuotaInfo();
                        reply.writeNoException();
                        if (_result17 != null) {
                            parcel2.writeInt(1);
                            _result17.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 18:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result18 = isActiveNetworkMetered();
                        reply.writeNoException();
                        parcel2.writeInt(_result18);
                        return true;
                    case 19:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result19 = requestRouteToHostAddress(data.readInt(), data.createByteArray());
                        reply.writeNoException();
                        parcel2.writeInt(_result19);
                        return true;
                    case 20:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result20 = tether(data.readString(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result20);
                        return true;
                    case 21:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result21 = untether(data.readString(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result21);
                        return true;
                    case 22:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result22 = getLastTetherError(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result22);
                        return true;
                    case 23:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result23 = isTetheringSupported(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result23);
                        return true;
                    case 24:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg08 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg07 = (ResultReceiver) ResultReceiver.CREATOR.createFromParcel(parcel);
                        }
                        if (data.readInt() != 0) {
                            _arg22 = true;
                        }
                        startTethering(_arg08, _arg07, _arg22, data.readString());
                        reply.writeNoException();
                        return true;
                    case 25:
                        parcel.enforceInterface(DESCRIPTOR);
                        stopTethering(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 26:
                        parcel.enforceInterface(DESCRIPTOR);
                        String[] _result24 = getTetherableIfaces();
                        reply.writeNoException();
                        parcel2.writeStringArray(_result24);
                        return true;
                    case 27:
                        parcel.enforceInterface(DESCRIPTOR);
                        String[] _result25 = getTetheredIfaces();
                        reply.writeNoException();
                        parcel2.writeStringArray(_result25);
                        return true;
                    case 28:
                        parcel.enforceInterface(DESCRIPTOR);
                        String[] _result26 = getTetheringErroredIfaces();
                        reply.writeNoException();
                        parcel2.writeStringArray(_result26);
                        return true;
                    case 29:
                        parcel.enforceInterface(DESCRIPTOR);
                        String[] _result27 = getTetheredDhcpRanges();
                        reply.writeNoException();
                        parcel2.writeStringArray(_result27);
                        return true;
                    case 30:
                        parcel.enforceInterface(DESCRIPTOR);
                        String[] _result28 = getTetherableUsbRegexs();
                        reply.writeNoException();
                        parcel2.writeStringArray(_result28);
                        return true;
                    case 31:
                        parcel.enforceInterface(DESCRIPTOR);
                        String[] _result29 = getTetherableWifiRegexs();
                        reply.writeNoException();
                        parcel2.writeStringArray(_result29);
                        return true;
                    case 32:
                        parcel.enforceInterface(DESCRIPTOR);
                        String[] _result30 = getTetherableBluetoothRegexs();
                        reply.writeNoException();
                        parcel2.writeStringArray(_result30);
                        return true;
                    case 33:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg22 = true;
                        }
                        int _result31 = setUsbTethering(_arg22, data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result31);
                        return true;
                    case 34:
                        parcel.enforceInterface(DESCRIPTOR);
                        reportInetCondition(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 35:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = (Network) Network.CREATOR.createFromParcel(parcel);
                        }
                        if (data.readInt() != 0) {
                            _arg22 = true;
                        }
                        reportNetworkConnectivity(_arg07, _arg22);
                        reply.writeNoException();
                        return true;
                    case 36:
                        parcel.enforceInterface(DESCRIPTOR);
                        ProxyInfo _result32 = getGlobalProxy();
                        reply.writeNoException();
                        if (_result32 != null) {
                            parcel2.writeInt(1);
                            _result32.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 37:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = (ProxyInfo) ProxyInfo.CREATOR.createFromParcel(parcel);
                        }
                        setGlobalProxy(_arg07);
                        reply.writeNoException();
                        return true;
                    case 38:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = (Network) Network.CREATOR.createFromParcel(parcel);
                        }
                        ProxyInfo _result33 = getProxyForNetwork(_arg07);
                        reply.writeNoException();
                        if (_result33 != null) {
                            parcel2.writeInt(1);
                            _result33.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 39:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result34 = prepareVpn(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result34);
                        return true;
                    case 40:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg09 = data.readString();
                        int _arg12 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg22 = true;
                        }
                        setVpnPackageAuthorization(_arg09, _arg12, _arg22);
                        reply.writeNoException();
                        return true;
                    case 41:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = (VpnConfig) VpnConfig.CREATOR.createFromParcel(parcel);
                        }
                        ParcelFileDescriptor _result35 = establishVpn(_arg07);
                        reply.writeNoException();
                        if (_result35 != null) {
                            parcel2.writeInt(1);
                            _result35.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 42:
                        parcel.enforceInterface(DESCRIPTOR);
                        VpnConfig _result36 = getVpnConfig(data.readInt());
                        reply.writeNoException();
                        if (_result36 != null) {
                            parcel2.writeInt(1);
                            _result36.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 43:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = (VpnProfile) VpnProfile.CREATOR.createFromParcel(parcel);
                        }
                        startLegacyVpn(_arg07);
                        reply.writeNoException();
                        return true;
                    case 44:
                        parcel.enforceInterface(DESCRIPTOR);
                        LegacyVpnInfo _result37 = getLegacyVpnInfo(data.readInt());
                        reply.writeNoException();
                        if (_result37 != null) {
                            parcel2.writeInt(1);
                            _result37.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 45:
                        parcel.enforceInterface(DESCRIPTOR);
                        VpnInfo[] _result38 = getAllVpnInfo();
                        reply.writeNoException();
                        parcel2.writeTypedArray(_result38, 1);
                        return true;
                    case 46:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result39 = updateLockdownVpn();
                        reply.writeNoException();
                        parcel2.writeInt(_result39);
                        return true;
                    case 47:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result40 = isAlwaysOnVpnPackageSupported(data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result40);
                        return true;
                    case 48:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg010 = data.readInt();
                        String _arg13 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = true;
                        }
                        boolean _result41 = setAlwaysOnVpnPackage(_arg010, _arg13, _arg22);
                        reply.writeNoException();
                        parcel2.writeInt(_result41);
                        return true;
                    case 49:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result42 = getAlwaysOnVpnPackage(data.readInt());
                        reply.writeNoException();
                        parcel2.writeString(_result42);
                        return true;
                    case 50:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result43 = checkMobileProvisioning(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result43);
                        return true;
                    case 51:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result44 = getMobileProvisioningUrl();
                        reply.writeNoException();
                        parcel2.writeString(_result44);
                        return true;
                    case 52:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg22 = true;
                        }
                        setProvisioningNotificationVisible(_arg22, data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 53:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg22 = true;
                        }
                        setAirplaneMode(_arg22);
                        reply.writeNoException();
                        return true;
                    case 54:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = (Messenger) Messenger.CREATOR.createFromParcel(parcel);
                        }
                        registerNetworkFactory(_arg07, data.readString());
                        reply.writeNoException();
                        return true;
                    case 55:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = Network.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result45 = requestBandwidthUpdate(_arg07);
                        reply.writeNoException();
                        parcel2.writeInt(_result45);
                        return true;
                    case 56:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = (Messenger) Messenger.CREATOR.createFromParcel(parcel);
                        }
                        unregisterNetworkFactory(_arg07);
                        reply.writeNoException();
                        return true;
                    case 57:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (Messenger) Messenger.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg1 = NetworkInfo.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg1 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg2 = LinkProperties.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg2 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg3 = NetworkCapabilities.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg3 = null;
                        }
                        int _arg4 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg07 = NetworkMisc.CREATOR.createFromParcel(parcel);
                        }
                        int _result46 = registerNetworkAgent(_arg0, _arg1, _arg2, _arg3, _arg4, _arg07);
                        reply.writeNoException();
                        parcel2.writeInt(_result46);
                        return true;
                    case 58:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = NetworkCapabilities.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg02 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg07 = (Messenger) Messenger.CREATOR.createFromParcel(parcel);
                        }
                        NetworkRequest _result47 = requestNetwork(_arg02, _arg07, data.readInt(), data.readStrongBinder(), data.readInt());
                        reply.writeNoException();
                        if (_result47 != null) {
                            parcel2.writeInt(1);
                            _result47.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 59:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = NetworkCapabilities.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg03 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg07 = PendingIntent.CREATOR.createFromParcel(parcel);
                        }
                        NetworkRequest _result48 = pendingRequestForNetwork(_arg03, _arg07);
                        reply.writeNoException();
                        if (_result48 != null) {
                            parcel2.writeInt(1);
                            _result48.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 60:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = PendingIntent.CREATOR.createFromParcel(parcel);
                        }
                        releasePendingNetworkRequest(_arg07);
                        reply.writeNoException();
                        return true;
                    case 61:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = NetworkCapabilities.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg04 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg07 = (Messenger) Messenger.CREATOR.createFromParcel(parcel);
                        }
                        NetworkRequest _result49 = listenForNetwork(_arg04, _arg07, data.readStrongBinder());
                        reply.writeNoException();
                        if (_result49 != null) {
                            parcel2.writeInt(1);
                            _result49.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 62:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = NetworkCapabilities.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg05 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg07 = PendingIntent.CREATOR.createFromParcel(parcel);
                        }
                        pendingListenForNetwork(_arg05, _arg07);
                        reply.writeNoException();
                        return true;
                    case 63:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = NetworkRequest.CREATOR.createFromParcel(parcel);
                        }
                        releaseNetworkRequest(_arg07);
                        reply.writeNoException();
                        return true;
                    case 64:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = Network.CREATOR.createFromParcel(parcel);
                        }
                        boolean _arg14 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg22 = true;
                        }
                        setAcceptUnvalidated(_arg07, _arg14, _arg22);
                        reply.writeNoException();
                        return true;
                    case 65:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = Network.CREATOR.createFromParcel(parcel);
                        }
                        setAvoidUnvalidated(_arg07);
                        reply.writeNoException();
                        return true;
                    case 66:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = Network.CREATOR.createFromParcel(parcel);
                        }
                        startCaptivePortalApp(_arg07);
                        reply.writeNoException();
                        return true;
                    case 67:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = Network.CREATOR.createFromParcel(parcel);
                        }
                        int _result50 = getMultipathPreference(_arg07);
                        reply.writeNoException();
                        parcel2.writeInt(_result50);
                        return true;
                    case 68:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result51 = getRestoreDefaultNetworkDelay(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result51);
                        return true;
                    case 69:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result52 = addVpnAddress(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result52);
                        return true;
                    case 70:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result53 = removeVpnAddress(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result53);
                        return true;
                    case 71:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result54 = setUnderlyingNetworksForVpn((Network[]) parcel.createTypedArray(Network.CREATOR));
                        reply.writeNoException();
                        parcel2.writeInt(_result54);
                        return true;
                    case 72:
                        parcel.enforceInterface(DESCRIPTOR);
                        factoryReset();
                        reply.writeNoException();
                        return true;
                    case 73:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = Network.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg06 = null;
                        }
                        int _arg15 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg07 = (Messenger) Messenger.CREATOR.createFromParcel(parcel);
                        }
                        startNattKeepalive(_arg06, _arg15, _arg07, data.readStrongBinder(), data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 74:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = Network.CREATOR.createFromParcel(parcel);
                        }
                        stopKeepalive(_arg07, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 75:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result55 = getCaptivePortalServerUrl();
                        reply.writeNoException();
                        parcel2.writeString(_result55);
                        return true;
                    case 76:
                        parcel.enforceInterface(DESCRIPTOR);
                        byte[] _result56 = getNetworkWatchlistConfigHash();
                        reply.writeNoException();
                        parcel2.writeByteArray(_result56);
                        return true;
                    case 77:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result57 = turnOffVpn(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result57);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    boolean addVpnAddress(String str, int i) throws RemoteException;

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

    String getMobileProvisioningUrl() throws RemoteException;

    int getMultipathPreference(Network network) throws RemoteException;

    NetworkCapabilities getNetworkCapabilities(Network network) throws RemoteException;

    Network getNetworkForType(int i) throws RemoteException;

    NetworkInfo getNetworkInfo(int i) throws RemoteException;

    NetworkInfo getNetworkInfoForUid(Network network, int i, boolean z) throws RemoteException;

    byte[] getNetworkWatchlistConfigHash() throws RemoteException;

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

    boolean isAlwaysOnVpnPackageSupported(int i, String str) throws RemoteException;

    boolean isNetworkSupported(int i) throws RemoteException;

    boolean isTetheringSupported(String str) throws RemoteException;

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

    void setAvoidUnvalidated(Network network) throws RemoteException;

    void setGlobalProxy(ProxyInfo proxyInfo) throws RemoteException;

    void setProvisioningNotificationVisible(boolean z, int i, String str) throws RemoteException;

    boolean setUnderlyingNetworksForVpn(Network[] networkArr) throws RemoteException;

    int setUsbTethering(boolean z, String str) throws RemoteException;

    void setVpnPackageAuthorization(String str, int i, boolean z) throws RemoteException;

    void startCaptivePortalApp(Network network) throws RemoteException;

    void startLegacyVpn(VpnProfile vpnProfile) throws RemoteException;

    void startNattKeepalive(Network network, int i, Messenger messenger, IBinder iBinder, String str, int i2, String str2) throws RemoteException;

    void startTethering(int i, ResultReceiver resultReceiver, boolean z, String str) throws RemoteException;

    void stopKeepalive(Network network, int i) throws RemoteException;

    void stopTethering(int i, String str) throws RemoteException;

    int tether(String str, String str2) throws RemoteException;

    boolean turnOffVpn(String str, int i) throws RemoteException;

    void unregisterNetworkFactory(Messenger messenger) throws RemoteException;

    int untether(String str, String str2) throws RemoteException;

    boolean updateLockdownVpn() throws RemoteException;
}
