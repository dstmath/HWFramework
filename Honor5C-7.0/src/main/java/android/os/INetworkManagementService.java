package android.os;

import android.net.INetworkManagementEventObserver;
import android.net.InterfaceConfiguration;
import android.net.Network;
import android.net.NetworkStats;
import android.net.RouteInfo;
import android.net.UidRange;
import android.net.wifi.WifiConfiguration;
import java.util.List;

public interface INetworkManagementService extends IInterface {

    public static abstract class Stub extends Binder implements INetworkManagementService {
        private static final String DESCRIPTOR = "android.os.INetworkManagementService";
        static final int TRANSACTION_addIdleTimer = 55;
        static final int TRANSACTION_addInterfaceToLocalNetwork = 88;
        static final int TRANSACTION_addInterfaceToNetwork = 78;
        static final int TRANSACTION_addLegacyRouteForNetId = 80;
        static final int TRANSACTION_addRoute = 13;
        static final int TRANSACTION_addUpstreamV6Interface = 31;
        static final int TRANSACTION_addVpnUidRanges = 67;
        static final int TRANSACTION_allowProtect = 86;
        static final int TRANSACTION_attachPppd = 34;
        static final int TRANSACTION_clearDefaultNetId = 82;
        static final int TRANSACTION_clearInterfaceAddresses = 6;
        static final int TRANSACTION_clearPermission = 85;
        static final int TRANSACTION_createPhysicalNetwork = 75;
        static final int TRANSACTION_createVirtualNetwork = 76;
        static final int TRANSACTION_denyProtect = 87;
        static final int TRANSACTION_detachPppd = 35;
        static final int TRANSACTION_disableIpv6 = 10;
        static final int TRANSACTION_disableNat = 30;
        static final int TRANSACTION_enableIpv6 = 11;
        static final int TRANSACTION_enableNat = 29;
        static final int TRANSACTION_getDnsForwarders = 26;
        static final int TRANSACTION_getInterfaceConfig = 4;
        static final int TRANSACTION_getIpForwardingEnabled = 17;
        static final int TRANSACTION_getNetworkStatsDetail = 42;
        static final int TRANSACTION_getNetworkStatsSummaryDev = 40;
        static final int TRANSACTION_getNetworkStatsSummaryXt = 41;
        static final int TRANSACTION_getNetworkStatsTethering = 44;
        static final int TRANSACTION_getNetworkStatsUidDetail = 43;
        static final int TRANSACTION_isBandwidthControlEnabled = 54;
        static final int TRANSACTION_isClatdStarted = 71;
        static final int TRANSACTION_isFirewallEnabled = 60;
        static final int TRANSACTION_isNetworkActive = 74;
        static final int TRANSACTION_isTetheringStarted = 21;
        static final int TRANSACTION_listInterfaces = 3;
        static final int TRANSACTION_listTetheredInterfaces = 24;
        static final int TRANSACTION_listTtys = 33;
        static final int TRANSACTION_registerNetworkActivityListener = 72;
        static final int TRANSACTION_registerObserver = 1;
        static final int TRANSACTION_removeIdleTimer = 56;
        static final int TRANSACTION_removeInterfaceAlert = 48;
        static final int TRANSACTION_removeInterfaceFromLocalNetwork = 89;
        static final int TRANSACTION_removeInterfaceFromNetwork = 79;
        static final int TRANSACTION_removeInterfaceQuota = 46;
        static final int TRANSACTION_removeNetwork = 77;
        static final int TRANSACTION_removeRoute = 14;
        static final int TRANSACTION_removeUpstreamV6Interface = 32;
        static final int TRANSACTION_removeVpnUidRanges = 68;
        static final int TRANSACTION_setAccessPoint = 39;
        static final int TRANSACTION_setAllowOnlyVpnForUids = 90;
        static final int TRANSACTION_setDataSaverModeEnabled = 52;
        static final int TRANSACTION_setDefaultNetId = 81;
        static final int TRANSACTION_setDnsConfigurationForNetwork = 57;
        static final int TRANSACTION_setDnsForwarders = 25;
        static final int TRANSACTION_setDnsServersForNetwork = 58;
        static final int TRANSACTION_setFirewallChainEnabled = 66;
        static final int TRANSACTION_setFirewallEgressDestRule = 63;
        static final int TRANSACTION_setFirewallEgressSourceRule = 62;
        static final int TRANSACTION_setFirewallEnabled = 59;
        static final int TRANSACTION_setFirewallInterfaceRule = 61;
        static final int TRANSACTION_setFirewallUidRule = 64;
        static final int TRANSACTION_setFirewallUidRules = 65;
        static final int TRANSACTION_setGlobalAlert = 49;
        static final int TRANSACTION_setInterfaceAlert = 47;
        static final int TRANSACTION_setInterfaceConfig = 5;
        static final int TRANSACTION_setInterfaceDown = 7;
        static final int TRANSACTION_setInterfaceIpv6NdOffload = 12;
        static final int TRANSACTION_setInterfaceIpv6PrivacyExtensions = 9;
        static final int TRANSACTION_setInterfaceQuota = 45;
        static final int TRANSACTION_setInterfaceUp = 8;
        static final int TRANSACTION_setIpForwardingEnabled = 18;
        static final int TRANSACTION_setMtu = 15;
        static final int TRANSACTION_setNetworkPermission = 83;
        static final int TRANSACTION_setPermission = 84;
        static final int TRANSACTION_setUidCleartextNetworkPolicy = 53;
        static final int TRANSACTION_setUidMeteredNetworkBlacklist = 50;
        static final int TRANSACTION_setUidMeteredNetworkWhitelist = 51;
        static final int TRANSACTION_shutdown = 16;
        static final int TRANSACTION_startAccessPoint = 37;
        static final int TRANSACTION_startClatd = 69;
        static final int TRANSACTION_startInterfaceForwarding = 27;
        static final int TRANSACTION_startTethering = 19;
        static final int TRANSACTION_stopAccessPoint = 38;
        static final int TRANSACTION_stopClatd = 70;
        static final int TRANSACTION_stopInterfaceForwarding = 28;
        static final int TRANSACTION_stopTethering = 20;
        static final int TRANSACTION_tetherInterface = 22;
        static final int TRANSACTION_unregisterNetworkActivityListener = 73;
        static final int TRANSACTION_unregisterObserver = 2;
        static final int TRANSACTION_untetherInterface = 23;
        static final int TRANSACTION_wifiFirmwareReload = 36;

        private static class Proxy implements INetworkManagementService {
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

            public void registerObserver(INetworkManagementEventObserver obs) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (obs != null) {
                        iBinder = obs.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_registerObserver, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterObserver(INetworkManagementEventObserver obs) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (obs != null) {
                        iBinder = obs.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_unregisterObserver, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] listInterfaces() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_listInterfaces, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public InterfaceConfiguration getInterfaceConfig(String iface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    InterfaceConfiguration interfaceConfiguration;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    this.mRemote.transact(Stub.TRANSACTION_getInterfaceConfig, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        interfaceConfiguration = (InterfaceConfiguration) InterfaceConfiguration.CREATOR.createFromParcel(_reply);
                    } else {
                        interfaceConfiguration = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return interfaceConfiguration;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setInterfaceConfig(String iface, InterfaceConfiguration cfg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    if (cfg != null) {
                        _data.writeInt(Stub.TRANSACTION_registerObserver);
                        cfg.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setInterfaceConfig, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearInterfaceAddresses(String iface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    this.mRemote.transact(Stub.TRANSACTION_clearInterfaceAddresses, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setInterfaceDown(String iface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    this.mRemote.transact(Stub.TRANSACTION_setInterfaceDown, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setInterfaceUp(String iface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    this.mRemote.transact(Stub.TRANSACTION_setInterfaceUp, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setInterfaceIpv6PrivacyExtensions(String iface, boolean enable) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    if (enable) {
                        i = Stub.TRANSACTION_registerObserver;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setInterfaceIpv6PrivacyExtensions, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void disableIpv6(String iface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    this.mRemote.transact(Stub.TRANSACTION_disableIpv6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void enableIpv6(String iface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    this.mRemote.transact(Stub.TRANSACTION_enableIpv6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setInterfaceIpv6NdOffload(String iface, boolean enable) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    if (enable) {
                        i = Stub.TRANSACTION_registerObserver;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setInterfaceIpv6NdOffload, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addRoute(int netId, RouteInfo route) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    if (route != null) {
                        _data.writeInt(Stub.TRANSACTION_registerObserver);
                        route.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_addRoute, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeRoute(int netId, RouteInfo route) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    if (route != null) {
                        _data.writeInt(Stub.TRANSACTION_registerObserver);
                        route.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_removeRoute, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setMtu(String iface, int mtu) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    _data.writeInt(mtu);
                    this.mRemote.transact(Stub.TRANSACTION_setMtu, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void shutdown() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_shutdown, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getIpForwardingEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getIpForwardingEnabled, _data, _reply, 0);
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

            public void setIpForwardingEnabled(boolean enabled) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (enabled) {
                        i = Stub.TRANSACTION_registerObserver;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setIpForwardingEnabled, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startTethering(String[] dhcpRanges) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(dhcpRanges);
                    this.mRemote.transact(Stub.TRANSACTION_startTethering, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopTethering() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_stopTethering, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isTetheringStarted() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isTetheringStarted, _data, _reply, 0);
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

            public void tetherInterface(String iface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    this.mRemote.transact(Stub.TRANSACTION_tetherInterface, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void untetherInterface(String iface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    this.mRemote.transact(Stub.TRANSACTION_untetherInterface, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] listTetheredInterfaces() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_listTetheredInterfaces, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDnsForwarders(Network network, String[] dns) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (network != null) {
                        _data.writeInt(Stub.TRANSACTION_registerObserver);
                        network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStringArray(dns);
                    this.mRemote.transact(Stub.TRANSACTION_setDnsForwarders, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getDnsForwarders() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getDnsForwarders, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startInterfaceForwarding(String fromIface, String toIface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(fromIface);
                    _data.writeString(toIface);
                    this.mRemote.transact(Stub.TRANSACTION_startInterfaceForwarding, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopInterfaceForwarding(String fromIface, String toIface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(fromIface);
                    _data.writeString(toIface);
                    this.mRemote.transact(Stub.TRANSACTION_stopInterfaceForwarding, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void enableNat(String internalInterface, String externalInterface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(internalInterface);
                    _data.writeString(externalInterface);
                    this.mRemote.transact(Stub.TRANSACTION_enableNat, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void disableNat(String internalInterface, String externalInterface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(internalInterface);
                    _data.writeString(externalInterface);
                    this.mRemote.transact(Stub.TRANSACTION_disableNat, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addUpstreamV6Interface(String iface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    this.mRemote.transact(Stub.TRANSACTION_addUpstreamV6Interface, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeUpstreamV6Interface(String iface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    this.mRemote.transact(Stub.TRANSACTION_removeUpstreamV6Interface, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] listTtys() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_listTtys, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void attachPppd(String tty, String localAddr, String remoteAddr, String dns1Addr, String dns2Addr) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(tty);
                    _data.writeString(localAddr);
                    _data.writeString(remoteAddr);
                    _data.writeString(dns1Addr);
                    _data.writeString(dns2Addr);
                    this.mRemote.transact(Stub.TRANSACTION_attachPppd, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void detachPppd(String tty) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(tty);
                    this.mRemote.transact(Stub.TRANSACTION_detachPppd, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void wifiFirmwareReload(String wlanIface, String mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(wlanIface);
                    _data.writeString(mode);
                    this.mRemote.transact(Stub.TRANSACTION_wifiFirmwareReload, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startAccessPoint(WifiConfiguration wifiConfig, String iface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (wifiConfig != null) {
                        _data.writeInt(Stub.TRANSACTION_registerObserver);
                        wifiConfig.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(iface);
                    this.mRemote.transact(Stub.TRANSACTION_startAccessPoint, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopAccessPoint(String iface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    this.mRemote.transact(Stub.TRANSACTION_stopAccessPoint, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAccessPoint(WifiConfiguration wifiConfig, String iface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (wifiConfig != null) {
                        _data.writeInt(Stub.TRANSACTION_registerObserver);
                        wifiConfig.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(iface);
                    this.mRemote.transact(Stub.TRANSACTION_setAccessPoint, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NetworkStats getNetworkStatsSummaryDev() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    NetworkStats networkStats;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getNetworkStatsSummaryDev, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        networkStats = (NetworkStats) NetworkStats.CREATOR.createFromParcel(_reply);
                    } else {
                        networkStats = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return networkStats;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NetworkStats getNetworkStatsSummaryXt() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    NetworkStats networkStats;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getNetworkStatsSummaryXt, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        networkStats = (NetworkStats) NetworkStats.CREATOR.createFromParcel(_reply);
                    } else {
                        networkStats = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return networkStats;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NetworkStats getNetworkStatsDetail() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    NetworkStats networkStats;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getNetworkStatsDetail, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        networkStats = (NetworkStats) NetworkStats.CREATOR.createFromParcel(_reply);
                    } else {
                        networkStats = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return networkStats;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NetworkStats getNetworkStatsUidDetail(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    NetworkStats networkStats;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_getNetworkStatsUidDetail, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        networkStats = (NetworkStats) NetworkStats.CREATOR.createFromParcel(_reply);
                    } else {
                        networkStats = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return networkStats;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NetworkStats getNetworkStatsTethering() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    NetworkStats networkStats;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getNetworkStatsTethering, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        networkStats = (NetworkStats) NetworkStats.CREATOR.createFromParcel(_reply);
                    } else {
                        networkStats = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return networkStats;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setInterfaceQuota(String iface, long quotaBytes) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    _data.writeLong(quotaBytes);
                    this.mRemote.transact(Stub.TRANSACTION_setInterfaceQuota, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeInterfaceQuota(String iface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    this.mRemote.transact(Stub.TRANSACTION_removeInterfaceQuota, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setInterfaceAlert(String iface, long alertBytes) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    _data.writeLong(alertBytes);
                    this.mRemote.transact(Stub.TRANSACTION_setInterfaceAlert, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeInterfaceAlert(String iface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    this.mRemote.transact(Stub.TRANSACTION_removeInterfaceAlert, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setGlobalAlert(long alertBytes) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(alertBytes);
                    this.mRemote.transact(Stub.TRANSACTION_setGlobalAlert, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setUidMeteredNetworkBlacklist(int uid, boolean enable) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    if (enable) {
                        i = Stub.TRANSACTION_registerObserver;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setUidMeteredNetworkBlacklist, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setUidMeteredNetworkWhitelist(int uid, boolean enable) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    if (enable) {
                        i = Stub.TRANSACTION_registerObserver;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setUidMeteredNetworkWhitelist, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setDataSaverModeEnabled(boolean enable) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (enable) {
                        i = Stub.TRANSACTION_registerObserver;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setDataSaverModeEnabled, _data, _reply, 0);
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

            public void setUidCleartextNetworkPolicy(int uid, int policy) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(policy);
                    this.mRemote.transact(Stub.TRANSACTION_setUidCleartextNetworkPolicy, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isBandwidthControlEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isBandwidthControlEnabled, _data, _reply, 0);
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

            public void addIdleTimer(String iface, int timeout, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    _data.writeInt(timeout);
                    _data.writeInt(type);
                    this.mRemote.transact(Stub.TRANSACTION_addIdleTimer, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeIdleTimer(String iface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    this.mRemote.transact(Stub.TRANSACTION_removeIdleTimer, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDnsConfigurationForNetwork(int netId, String[] servers, String domains) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    _data.writeStringArray(servers);
                    _data.writeString(domains);
                    this.mRemote.transact(Stub.TRANSACTION_setDnsConfigurationForNetwork, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDnsServersForNetwork(int netId, String[] servers, String domains) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    _data.writeStringArray(servers);
                    _data.writeString(domains);
                    this.mRemote.transact(Stub.TRANSACTION_setDnsServersForNetwork, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setFirewallEnabled(boolean enabled) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (enabled) {
                        i = Stub.TRANSACTION_registerObserver;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setFirewallEnabled, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isFirewallEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isFirewallEnabled, _data, _reply, 0);
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

            public void setFirewallInterfaceRule(String iface, boolean allow) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    if (allow) {
                        i = Stub.TRANSACTION_registerObserver;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setFirewallInterfaceRule, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setFirewallEgressSourceRule(String addr, boolean allow) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(addr);
                    if (allow) {
                        i = Stub.TRANSACTION_registerObserver;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setFirewallEgressSourceRule, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setFirewallEgressDestRule(String addr, int port, boolean allow) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(addr);
                    _data.writeInt(port);
                    if (allow) {
                        i = Stub.TRANSACTION_registerObserver;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setFirewallEgressDestRule, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setFirewallUidRule(int chain, int uid, int rule) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(chain);
                    _data.writeInt(uid);
                    _data.writeInt(rule);
                    this.mRemote.transact(Stub.TRANSACTION_setFirewallUidRule, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setFirewallUidRules(int chain, int[] uids, int[] rules) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(chain);
                    _data.writeIntArray(uids);
                    _data.writeIntArray(rules);
                    this.mRemote.transact(Stub.TRANSACTION_setFirewallUidRules, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setFirewallChainEnabled(int chain, boolean enable) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(chain);
                    if (enable) {
                        i = Stub.TRANSACTION_registerObserver;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setFirewallChainEnabled, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addVpnUidRanges(int netId, UidRange[] ranges) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    _data.writeTypedArray(ranges, 0);
                    this.mRemote.transact(Stub.TRANSACTION_addVpnUidRanges, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeVpnUidRanges(int netId, UidRange[] ranges) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    _data.writeTypedArray(ranges, 0);
                    this.mRemote.transact(Stub.TRANSACTION_removeVpnUidRanges, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startClatd(String interfaceName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(interfaceName);
                    this.mRemote.transact(Stub.TRANSACTION_startClatd, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopClatd(String interfaceName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(interfaceName);
                    this.mRemote.transact(Stub.TRANSACTION_stopClatd, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isClatdStarted(String interfaceName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(interfaceName);
                    this.mRemote.transact(Stub.TRANSACTION_isClatdStarted, _data, _reply, 0);
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

            public void registerNetworkActivityListener(INetworkActivityListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_registerNetworkActivityListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterNetworkActivityListener(INetworkActivityListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_unregisterNetworkActivityListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isNetworkActive() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isNetworkActive, _data, _reply, 0);
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

            public void createPhysicalNetwork(int netId, String permission) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    _data.writeString(permission);
                    this.mRemote.transact(Stub.TRANSACTION_createPhysicalNetwork, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void createVirtualNetwork(int netId, boolean hasDNS, boolean secure) throws RemoteException {
                int i = Stub.TRANSACTION_registerObserver;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    int i2;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    if (hasDNS) {
                        i2 = Stub.TRANSACTION_registerObserver;
                    } else {
                        i2 = 0;
                    }
                    _data.writeInt(i2);
                    if (!secure) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_createVirtualNetwork, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeNetwork(int netId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    this.mRemote.transact(Stub.TRANSACTION_removeNetwork, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addInterfaceToNetwork(String iface, int netId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    _data.writeInt(netId);
                    this.mRemote.transact(Stub.TRANSACTION_addInterfaceToNetwork, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeInterfaceFromNetwork(String iface, int netId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    _data.writeInt(netId);
                    this.mRemote.transact(Stub.TRANSACTION_removeInterfaceFromNetwork, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addLegacyRouteForNetId(int netId, RouteInfo routeInfo, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    if (routeInfo != null) {
                        _data.writeInt(Stub.TRANSACTION_registerObserver);
                        routeInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_addLegacyRouteForNetId, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDefaultNetId(int netId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    this.mRemote.transact(Stub.TRANSACTION_setDefaultNetId, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearDefaultNetId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_clearDefaultNetId, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setNetworkPermission(int netId, String permission) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    _data.writeString(permission);
                    this.mRemote.transact(Stub.TRANSACTION_setNetworkPermission, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setPermission(String permission, int[] uids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(permission);
                    _data.writeIntArray(uids);
                    this.mRemote.transact(Stub.TRANSACTION_setPermission, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearPermission(int[] uids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(uids);
                    this.mRemote.transact(Stub.TRANSACTION_clearPermission, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void allowProtect(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_allowProtect, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void denyProtect(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_denyProtect, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addInterfaceToLocalNetwork(String iface, List<RouteInfo> routes) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    _data.writeTypedList(routes);
                    this.mRemote.transact(Stub.TRANSACTION_addInterfaceToLocalNetwork, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeInterfaceFromLocalNetwork(String iface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    this.mRemote.transact(Stub.TRANSACTION_removeInterfaceFromLocalNetwork, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAllowOnlyVpnForUids(boolean enable, UidRange[] uidRanges) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (enable) {
                        i = Stub.TRANSACTION_registerObserver;
                    }
                    _data.writeInt(i);
                    _data.writeTypedArray(uidRanges, 0);
                    this.mRemote.transact(Stub.TRANSACTION_setAllowOnlyVpnForUids, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INetworkManagementService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INetworkManagementService)) {
                return new Proxy(obj);
            }
            return (INetworkManagementService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            String[] _result;
            int _arg0;
            RouteInfo routeInfo;
            boolean _result2;
            WifiConfiguration wifiConfiguration;
            NetworkStats _result3;
            switch (code) {
                case TRANSACTION_registerObserver /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    registerObserver(android.net.INetworkManagementEventObserver.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_unregisterObserver /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    unregisterObserver(android.net.INetworkManagementEventObserver.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_listInterfaces /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = listInterfaces();
                    reply.writeNoException();
                    reply.writeStringArray(_result);
                    return true;
                case TRANSACTION_getInterfaceConfig /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    InterfaceConfiguration _result4 = getInterfaceConfig(data.readString());
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_registerObserver);
                        _result4.writeToParcel(reply, TRANSACTION_registerObserver);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_setInterfaceConfig /*5*/:
                    InterfaceConfiguration interfaceConfiguration;
                    data.enforceInterface(DESCRIPTOR);
                    String _arg02 = data.readString();
                    if (data.readInt() != 0) {
                        interfaceConfiguration = (InterfaceConfiguration) InterfaceConfiguration.CREATOR.createFromParcel(data);
                    } else {
                        interfaceConfiguration = null;
                    }
                    setInterfaceConfig(_arg02, interfaceConfiguration);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_clearInterfaceAddresses /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    clearInterfaceAddresses(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setInterfaceDown /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    setInterfaceDown(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setInterfaceUp /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    setInterfaceUp(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setInterfaceIpv6PrivacyExtensions /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    setInterfaceIpv6PrivacyExtensions(data.readString(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_disableIpv6 /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    disableIpv6(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_enableIpv6 /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    enableIpv6(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setInterfaceIpv6NdOffload /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    setInterfaceIpv6NdOffload(data.readString(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_addRoute /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        routeInfo = (RouteInfo) RouteInfo.CREATOR.createFromParcel(data);
                    } else {
                        routeInfo = null;
                    }
                    addRoute(_arg0, routeInfo);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removeRoute /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        routeInfo = (RouteInfo) RouteInfo.CREATOR.createFromParcel(data);
                    } else {
                        routeInfo = null;
                    }
                    removeRoute(_arg0, routeInfo);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setMtu /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    setMtu(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_shutdown /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    shutdown();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getIpForwardingEnabled /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getIpForwardingEnabled();
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_registerObserver : 0);
                    return true;
                case TRANSACTION_setIpForwardingEnabled /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    setIpForwardingEnabled(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_startTethering /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    startTethering(data.createStringArray());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_stopTethering /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    stopTethering();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isTetheringStarted /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = isTetheringStarted();
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_registerObserver : 0);
                    return true;
                case TRANSACTION_tetherInterface /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    tetherInterface(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_untetherInterface /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    untetherInterface(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_listTetheredInterfaces /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = listTetheredInterfaces();
                    reply.writeNoException();
                    reply.writeStringArray(_result);
                    return true;
                case TRANSACTION_setDnsForwarders /*25*/:
                    Network network;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        network = (Network) Network.CREATOR.createFromParcel(data);
                    } else {
                        network = null;
                    }
                    setDnsForwarders(network, data.createStringArray());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getDnsForwarders /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getDnsForwarders();
                    reply.writeNoException();
                    reply.writeStringArray(_result);
                    return true;
                case TRANSACTION_startInterfaceForwarding /*27*/:
                    data.enforceInterface(DESCRIPTOR);
                    startInterfaceForwarding(data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_stopInterfaceForwarding /*28*/:
                    data.enforceInterface(DESCRIPTOR);
                    stopInterfaceForwarding(data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_enableNat /*29*/:
                    data.enforceInterface(DESCRIPTOR);
                    enableNat(data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_disableNat /*30*/:
                    data.enforceInterface(DESCRIPTOR);
                    disableNat(data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_addUpstreamV6Interface /*31*/:
                    data.enforceInterface(DESCRIPTOR);
                    addUpstreamV6Interface(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removeUpstreamV6Interface /*32*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeUpstreamV6Interface(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_listTtys /*33*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = listTtys();
                    reply.writeNoException();
                    reply.writeStringArray(_result);
                    return true;
                case TRANSACTION_attachPppd /*34*/:
                    data.enforceInterface(DESCRIPTOR);
                    attachPppd(data.readString(), data.readString(), data.readString(), data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_detachPppd /*35*/:
                    data.enforceInterface(DESCRIPTOR);
                    detachPppd(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_wifiFirmwareReload /*36*/:
                    data.enforceInterface(DESCRIPTOR);
                    wifiFirmwareReload(data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_startAccessPoint /*37*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        wifiConfiguration = (WifiConfiguration) WifiConfiguration.CREATOR.createFromParcel(data);
                    } else {
                        wifiConfiguration = null;
                    }
                    startAccessPoint(wifiConfiguration, data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_stopAccessPoint /*38*/:
                    data.enforceInterface(DESCRIPTOR);
                    stopAccessPoint(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setAccessPoint /*39*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        wifiConfiguration = (WifiConfiguration) WifiConfiguration.CREATOR.createFromParcel(data);
                    } else {
                        wifiConfiguration = null;
                    }
                    setAccessPoint(wifiConfiguration, data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getNetworkStatsSummaryDev /*40*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getNetworkStatsSummaryDev();
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_registerObserver);
                        _result3.writeToParcel(reply, TRANSACTION_registerObserver);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getNetworkStatsSummaryXt /*41*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getNetworkStatsSummaryXt();
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_registerObserver);
                        _result3.writeToParcel(reply, TRANSACTION_registerObserver);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getNetworkStatsDetail /*42*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getNetworkStatsDetail();
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_registerObserver);
                        _result3.writeToParcel(reply, TRANSACTION_registerObserver);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getNetworkStatsUidDetail /*43*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getNetworkStatsUidDetail(data.readInt());
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_registerObserver);
                        _result3.writeToParcel(reply, TRANSACTION_registerObserver);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getNetworkStatsTethering /*44*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getNetworkStatsTethering();
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_registerObserver);
                        _result3.writeToParcel(reply, TRANSACTION_registerObserver);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_setInterfaceQuota /*45*/:
                    data.enforceInterface(DESCRIPTOR);
                    setInterfaceQuota(data.readString(), data.readLong());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removeInterfaceQuota /*46*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeInterfaceQuota(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setInterfaceAlert /*47*/:
                    data.enforceInterface(DESCRIPTOR);
                    setInterfaceAlert(data.readString(), data.readLong());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removeInterfaceAlert /*48*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeInterfaceAlert(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setGlobalAlert /*49*/:
                    data.enforceInterface(DESCRIPTOR);
                    setGlobalAlert(data.readLong());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setUidMeteredNetworkBlacklist /*50*/:
                    data.enforceInterface(DESCRIPTOR);
                    setUidMeteredNetworkBlacklist(data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setUidMeteredNetworkWhitelist /*51*/:
                    data.enforceInterface(DESCRIPTOR);
                    setUidMeteredNetworkWhitelist(data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setDataSaverModeEnabled /*52*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = setDataSaverModeEnabled(data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_registerObserver : 0);
                    return true;
                case TRANSACTION_setUidCleartextNetworkPolicy /*53*/:
                    data.enforceInterface(DESCRIPTOR);
                    setUidCleartextNetworkPolicy(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isBandwidthControlEnabled /*54*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = isBandwidthControlEnabled();
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_registerObserver : 0);
                    return true;
                case TRANSACTION_addIdleTimer /*55*/:
                    data.enforceInterface(DESCRIPTOR);
                    addIdleTimer(data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removeIdleTimer /*56*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeIdleTimer(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setDnsConfigurationForNetwork /*57*/:
                    data.enforceInterface(DESCRIPTOR);
                    setDnsConfigurationForNetwork(data.readInt(), data.createStringArray(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setDnsServersForNetwork /*58*/:
                    data.enforceInterface(DESCRIPTOR);
                    setDnsServersForNetwork(data.readInt(), data.createStringArray(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setFirewallEnabled /*59*/:
                    data.enforceInterface(DESCRIPTOR);
                    setFirewallEnabled(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isFirewallEnabled /*60*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = isFirewallEnabled();
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_registerObserver : 0);
                    return true;
                case TRANSACTION_setFirewallInterfaceRule /*61*/:
                    data.enforceInterface(DESCRIPTOR);
                    setFirewallInterfaceRule(data.readString(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setFirewallEgressSourceRule /*62*/:
                    data.enforceInterface(DESCRIPTOR);
                    setFirewallEgressSourceRule(data.readString(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setFirewallEgressDestRule /*63*/:
                    data.enforceInterface(DESCRIPTOR);
                    setFirewallEgressDestRule(data.readString(), data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setFirewallUidRule /*64*/:
                    data.enforceInterface(DESCRIPTOR);
                    setFirewallUidRule(data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setFirewallUidRules /*65*/:
                    data.enforceInterface(DESCRIPTOR);
                    setFirewallUidRules(data.readInt(), data.createIntArray(), data.createIntArray());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setFirewallChainEnabled /*66*/:
                    data.enforceInterface(DESCRIPTOR);
                    setFirewallChainEnabled(data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_addVpnUidRanges /*67*/:
                    data.enforceInterface(DESCRIPTOR);
                    addVpnUidRanges(data.readInt(), (UidRange[]) data.createTypedArray(UidRange.CREATOR));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removeVpnUidRanges /*68*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeVpnUidRanges(data.readInt(), (UidRange[]) data.createTypedArray(UidRange.CREATOR));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_startClatd /*69*/:
                    data.enforceInterface(DESCRIPTOR);
                    startClatd(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_stopClatd /*70*/:
                    data.enforceInterface(DESCRIPTOR);
                    stopClatd(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isClatdStarted /*71*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = isClatdStarted(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_registerObserver : 0);
                    return true;
                case TRANSACTION_registerNetworkActivityListener /*72*/:
                    data.enforceInterface(DESCRIPTOR);
                    registerNetworkActivityListener(android.os.INetworkActivityListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_unregisterNetworkActivityListener /*73*/:
                    data.enforceInterface(DESCRIPTOR);
                    unregisterNetworkActivityListener(android.os.INetworkActivityListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isNetworkActive /*74*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = isNetworkActive();
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_registerObserver : 0);
                    return true;
                case TRANSACTION_createPhysicalNetwork /*75*/:
                    data.enforceInterface(DESCRIPTOR);
                    createPhysicalNetwork(data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_createVirtualNetwork /*76*/:
                    data.enforceInterface(DESCRIPTOR);
                    createVirtualNetwork(data.readInt(), data.readInt() != 0, data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removeNetwork /*77*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeNetwork(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_addInterfaceToNetwork /*78*/:
                    data.enforceInterface(DESCRIPTOR);
                    addInterfaceToNetwork(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removeInterfaceFromNetwork /*79*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeInterfaceFromNetwork(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_addLegacyRouteForNetId /*80*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        routeInfo = (RouteInfo) RouteInfo.CREATOR.createFromParcel(data);
                    } else {
                        routeInfo = null;
                    }
                    addLegacyRouteForNetId(_arg0, routeInfo, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setDefaultNetId /*81*/:
                    data.enforceInterface(DESCRIPTOR);
                    setDefaultNetId(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_clearDefaultNetId /*82*/:
                    data.enforceInterface(DESCRIPTOR);
                    clearDefaultNetId();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setNetworkPermission /*83*/:
                    data.enforceInterface(DESCRIPTOR);
                    setNetworkPermission(data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setPermission /*84*/:
                    data.enforceInterface(DESCRIPTOR);
                    setPermission(data.readString(), data.createIntArray());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_clearPermission /*85*/:
                    data.enforceInterface(DESCRIPTOR);
                    clearPermission(data.createIntArray());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_allowProtect /*86*/:
                    data.enforceInterface(DESCRIPTOR);
                    allowProtect(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_denyProtect /*87*/:
                    data.enforceInterface(DESCRIPTOR);
                    denyProtect(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_addInterfaceToLocalNetwork /*88*/:
                    data.enforceInterface(DESCRIPTOR);
                    addInterfaceToLocalNetwork(data.readString(), data.createTypedArrayList(RouteInfo.CREATOR));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removeInterfaceFromLocalNetwork /*89*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeInterfaceFromLocalNetwork(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setAllowOnlyVpnForUids /*90*/:
                    data.enforceInterface(DESCRIPTOR);
                    setAllowOnlyVpnForUids(data.readInt() != 0, (UidRange[]) data.createTypedArray(UidRange.CREATOR));
                    reply.writeNoException();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void addIdleTimer(String str, int i, int i2) throws RemoteException;

    void addInterfaceToLocalNetwork(String str, List<RouteInfo> list) throws RemoteException;

    void addInterfaceToNetwork(String str, int i) throws RemoteException;

    void addLegacyRouteForNetId(int i, RouteInfo routeInfo, int i2) throws RemoteException;

    void addRoute(int i, RouteInfo routeInfo) throws RemoteException;

    void addUpstreamV6Interface(String str) throws RemoteException;

    void addVpnUidRanges(int i, UidRange[] uidRangeArr) throws RemoteException;

    void allowProtect(int i) throws RemoteException;

    void attachPppd(String str, String str2, String str3, String str4, String str5) throws RemoteException;

    void clearDefaultNetId() throws RemoteException;

    void clearInterfaceAddresses(String str) throws RemoteException;

    void clearPermission(int[] iArr) throws RemoteException;

    void createPhysicalNetwork(int i, String str) throws RemoteException;

    void createVirtualNetwork(int i, boolean z, boolean z2) throws RemoteException;

    void denyProtect(int i) throws RemoteException;

    void detachPppd(String str) throws RemoteException;

    void disableIpv6(String str) throws RemoteException;

    void disableNat(String str, String str2) throws RemoteException;

    void enableIpv6(String str) throws RemoteException;

    void enableNat(String str, String str2) throws RemoteException;

    String[] getDnsForwarders() throws RemoteException;

    InterfaceConfiguration getInterfaceConfig(String str) throws RemoteException;

    boolean getIpForwardingEnabled() throws RemoteException;

    NetworkStats getNetworkStatsDetail() throws RemoteException;

    NetworkStats getNetworkStatsSummaryDev() throws RemoteException;

    NetworkStats getNetworkStatsSummaryXt() throws RemoteException;

    NetworkStats getNetworkStatsTethering() throws RemoteException;

    NetworkStats getNetworkStatsUidDetail(int i) throws RemoteException;

    boolean isBandwidthControlEnabled() throws RemoteException;

    boolean isClatdStarted(String str) throws RemoteException;

    boolean isFirewallEnabled() throws RemoteException;

    boolean isNetworkActive() throws RemoteException;

    boolean isTetheringStarted() throws RemoteException;

    String[] listInterfaces() throws RemoteException;

    String[] listTetheredInterfaces() throws RemoteException;

    String[] listTtys() throws RemoteException;

    void registerNetworkActivityListener(INetworkActivityListener iNetworkActivityListener) throws RemoteException;

    void registerObserver(INetworkManagementEventObserver iNetworkManagementEventObserver) throws RemoteException;

    void removeIdleTimer(String str) throws RemoteException;

    void removeInterfaceAlert(String str) throws RemoteException;

    void removeInterfaceFromLocalNetwork(String str) throws RemoteException;

    void removeInterfaceFromNetwork(String str, int i) throws RemoteException;

    void removeInterfaceQuota(String str) throws RemoteException;

    void removeNetwork(int i) throws RemoteException;

    void removeRoute(int i, RouteInfo routeInfo) throws RemoteException;

    void removeUpstreamV6Interface(String str) throws RemoteException;

    void removeVpnUidRanges(int i, UidRange[] uidRangeArr) throws RemoteException;

    void setAccessPoint(WifiConfiguration wifiConfiguration, String str) throws RemoteException;

    void setAllowOnlyVpnForUids(boolean z, UidRange[] uidRangeArr) throws RemoteException;

    boolean setDataSaverModeEnabled(boolean z) throws RemoteException;

    void setDefaultNetId(int i) throws RemoteException;

    void setDnsConfigurationForNetwork(int i, String[] strArr, String str) throws RemoteException;

    void setDnsForwarders(Network network, String[] strArr) throws RemoteException;

    void setDnsServersForNetwork(int i, String[] strArr, String str) throws RemoteException;

    void setFirewallChainEnabled(int i, boolean z) throws RemoteException;

    void setFirewallEgressDestRule(String str, int i, boolean z) throws RemoteException;

    void setFirewallEgressSourceRule(String str, boolean z) throws RemoteException;

    void setFirewallEnabled(boolean z) throws RemoteException;

    void setFirewallInterfaceRule(String str, boolean z) throws RemoteException;

    void setFirewallUidRule(int i, int i2, int i3) throws RemoteException;

    void setFirewallUidRules(int i, int[] iArr, int[] iArr2) throws RemoteException;

    void setGlobalAlert(long j) throws RemoteException;

    void setInterfaceAlert(String str, long j) throws RemoteException;

    void setInterfaceConfig(String str, InterfaceConfiguration interfaceConfiguration) throws RemoteException;

    void setInterfaceDown(String str) throws RemoteException;

    void setInterfaceIpv6NdOffload(String str, boolean z) throws RemoteException;

    void setInterfaceIpv6PrivacyExtensions(String str, boolean z) throws RemoteException;

    void setInterfaceQuota(String str, long j) throws RemoteException;

    void setInterfaceUp(String str) throws RemoteException;

    void setIpForwardingEnabled(boolean z) throws RemoteException;

    void setMtu(String str, int i) throws RemoteException;

    void setNetworkPermission(int i, String str) throws RemoteException;

    void setPermission(String str, int[] iArr) throws RemoteException;

    void setUidCleartextNetworkPolicy(int i, int i2) throws RemoteException;

    void setUidMeteredNetworkBlacklist(int i, boolean z) throws RemoteException;

    void setUidMeteredNetworkWhitelist(int i, boolean z) throws RemoteException;

    void shutdown() throws RemoteException;

    void startAccessPoint(WifiConfiguration wifiConfiguration, String str) throws RemoteException;

    void startClatd(String str) throws RemoteException;

    void startInterfaceForwarding(String str, String str2) throws RemoteException;

    void startTethering(String[] strArr) throws RemoteException;

    void stopAccessPoint(String str) throws RemoteException;

    void stopClatd(String str) throws RemoteException;

    void stopInterfaceForwarding(String str, String str2) throws RemoteException;

    void stopTethering() throws RemoteException;

    void tetherInterface(String str) throws RemoteException;

    void unregisterNetworkActivityListener(INetworkActivityListener iNetworkActivityListener) throws RemoteException;

    void unregisterObserver(INetworkManagementEventObserver iNetworkManagementEventObserver) throws RemoteException;

    void untetherInterface(String str) throws RemoteException;

    void wifiFirmwareReload(String str, String str2) throws RemoteException;
}
