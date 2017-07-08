package android.net;

import android.app.PendingIntent;
import android.app.Service;
import android.common.HwFrameworkMonitor;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageManager.Stub;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.security.keymaster.KeymasterDefs;
import android.system.OsConstants;
import android.util.Log;
import com.android.internal.net.VpnConfig;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class VpnService extends Service {
    public static final String SERVICE_INTERFACE = "android.net.VpnService";

    public class Builder {
        private final List<LinkAddress> mAddresses;
        private final VpnConfig mConfig;
        private final List<RouteInfo> mRoutes;

        public Builder() {
            this.mConfig = new VpnConfig();
            this.mAddresses = new ArrayList();
            this.mRoutes = new ArrayList();
            this.mConfig.user = VpnService.this.getClass().getName();
        }

        public Builder setSession(String session) {
            this.mConfig.session = session;
            return this;
        }

        public Builder setConfigureIntent(PendingIntent intent) {
            this.mConfig.configureIntent = intent;
            return this;
        }

        public Builder setMtu(int mtu) {
            if (mtu <= 0) {
                throw new IllegalArgumentException("Bad mtu");
            }
            this.mConfig.mtu = mtu;
            return this;
        }

        public Builder addAddress(InetAddress address, int prefixLength) {
            VpnService.check(address, prefixLength);
            if (address.isAnyLocalAddress()) {
                throw new IllegalArgumentException("Bad address");
            }
            this.mAddresses.add(new LinkAddress(address, prefixLength));
            this.mConfig.updateAllowedFamilies(address);
            return this;
        }

        public Builder addAddress(String address, int prefixLength) {
            return addAddress(InetAddress.parseNumericAddress(address), prefixLength);
        }

        public Builder addRoute(InetAddress address, int prefixLength) {
            VpnService.check(address, prefixLength);
            int offset = prefixLength / 8;
            byte[] bytes = address.getAddress();
            if (offset < bytes.length) {
                bytes[offset] = (byte) (bytes[offset] << (prefixLength % 8));
                while (offset < bytes.length) {
                    if (bytes[offset] != null) {
                        throw new IllegalArgumentException("Bad address");
                    }
                    offset++;
                }
            }
            this.mRoutes.add(new RouteInfo(new IpPrefix(address, prefixLength), null));
            this.mConfig.updateAllowedFamilies(address);
            return this;
        }

        public Builder addRoute(String address, int prefixLength) {
            return addRoute(InetAddress.parseNumericAddress(address), prefixLength);
        }

        public Builder addDnsServer(InetAddress address) {
            if (address.isLoopbackAddress() || address.isAnyLocalAddress()) {
                throw new IllegalArgumentException("Bad address");
            }
            if (this.mConfig.dnsServers == null) {
                this.mConfig.dnsServers = new ArrayList();
            }
            this.mConfig.dnsServers.add(address.getHostAddress());
            return this;
        }

        public Builder addDnsServer(String address) {
            return addDnsServer(InetAddress.parseNumericAddress(address));
        }

        public Builder addSearchDomain(String domain) {
            if (this.mConfig.searchDomains == null) {
                this.mConfig.searchDomains = new ArrayList();
            }
            this.mConfig.searchDomains.add(domain);
            return this;
        }

        public Builder allowFamily(int family) {
            if (family == OsConstants.AF_INET) {
                this.mConfig.allowIPv4 = true;
            } else if (family == OsConstants.AF_INET6) {
                this.mConfig.allowIPv6 = true;
            } else {
                throw new IllegalArgumentException(family + " is neither " + OsConstants.AF_INET + " nor " + OsConstants.AF_INET6);
            }
            return this;
        }

        private void verifyApp(String packageName) throws NameNotFoundException {
            try {
                Stub.asInterface(ServiceManager.getService(HwFrameworkMonitor.KEY_PACKAGE)).getApplicationInfo(packageName, 0, UserHandle.getCallingUserId());
            } catch (RemoteException e) {
                throw new IllegalStateException(e);
            }
        }

        public Builder addAllowedApplication(String packageName) throws NameNotFoundException {
            if (this.mConfig.disallowedApplications != null) {
                throw new UnsupportedOperationException("addDisallowedApplication already called");
            }
            verifyApp(packageName);
            if (this.mConfig.allowedApplications == null) {
                this.mConfig.allowedApplications = new ArrayList();
            }
            this.mConfig.allowedApplications.add(packageName);
            return this;
        }

        public Builder addDisallowedApplication(String packageName) throws NameNotFoundException {
            if (this.mConfig.allowedApplications != null) {
                throw new UnsupportedOperationException("addAllowedApplication already called");
            }
            verifyApp(packageName);
            if (this.mConfig.disallowedApplications == null) {
                this.mConfig.disallowedApplications = new ArrayList();
            }
            this.mConfig.disallowedApplications.add(packageName);
            return this;
        }

        public Builder allowBypass() {
            this.mConfig.allowBypass = true;
            return this;
        }

        public Builder setBlocking(boolean blocking) {
            this.mConfig.blocking = blocking;
            return this;
        }

        public Builder setUnderlyingNetworks(Network[] networks) {
            Network[] networkArr = null;
            VpnConfig vpnConfig = this.mConfig;
            if (networks != null) {
                networkArr = (Network[]) networks.clone();
            }
            vpnConfig.underlyingNetworks = networkArr;
            return this;
        }

        public ParcelFileDescriptor establish() {
            this.mConfig.addresses = this.mAddresses;
            this.mConfig.routes = this.mRoutes;
            try {
                return VpnService.getService().establishVpn(this.mConfig);
            } catch (RemoteException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private class Callback extends Binder {
        private Callback() {
        }

        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) {
            if (code != IBinder.LAST_CALL_TRANSACTION) {
                return false;
            }
            VpnService.this.onRevoke();
            return true;
        }
    }

    private static IConnectivityManager getService() {
        return IConnectivityManager.Stub.asInterface(ServiceManager.getService(Context.CONNECTIVITY_SERVICE));
    }

    public static Intent prepare(Context context) {
        try {
            if (getService().prepareVpn(context.getPackageName(), null, UserHandle.myUserId())) {
                return null;
            }
        } catch (RemoteException e) {
        }
        return VpnConfig.getIntentForConfirmation();
    }

    public static void prepareAndAuthorize(Context context) {
        IConnectivityManager cm = getService();
        String packageName = context.getPackageName();
        try {
            int userId = UserHandle.myUserId();
            if (!cm.prepareVpn(packageName, null, userId)) {
                cm.prepareVpn(null, packageName, userId);
            }
            cm.setVpnPackageAuthorization(packageName, userId, true);
        } catch (RemoteException e) {
        }
    }

    public boolean protect(int socket) {
        if (SystemProperties.get("persist.sys.cc_mode", WifiEnterpriseConfig.ENGINE_DISABLE).equals(WifiEnterpriseConfig.ENGINE_DISABLE)) {
            return NetworkUtils.protectFromVpn(socket);
        }
        Log.d("VpnService", "tunneling-split has been prohibited");
        return false;
    }

    public boolean protect(Socket socket) {
        return protect(socket.getFileDescriptor$().getInt$());
    }

    public boolean protect(DatagramSocket socket) {
        return protect(socket.getFileDescriptor$().getInt$());
    }

    public boolean addAddress(InetAddress address, int prefixLength) {
        check(address, prefixLength);
        try {
            return getService().addVpnAddress(address.getHostAddress(), prefixLength);
        } catch (RemoteException e) {
            throw new IllegalStateException(e);
        }
    }

    public boolean removeAddress(InetAddress address, int prefixLength) {
        check(address, prefixLength);
        try {
            return getService().removeVpnAddress(address.getHostAddress(), prefixLength);
        } catch (RemoteException e) {
            throw new IllegalStateException(e);
        }
    }

    public boolean setUnderlyingNetworks(Network[] networks) {
        try {
            return getService().setUnderlyingNetworksForVpn(networks);
        } catch (RemoteException e) {
            throw new IllegalStateException(e);
        }
    }

    public IBinder onBind(Intent intent) {
        if (intent == null || !SERVICE_INTERFACE.equals(intent.getAction())) {
            return null;
        }
        return new Callback();
    }

    public void onRevoke() {
        stopSelf();
    }

    private static void check(InetAddress address, int prefixLength) {
        if (address.isLoopbackAddress()) {
            throw new IllegalArgumentException("Bad address");
        } else if (address instanceof Inet4Address) {
            if (prefixLength < 0 || prefixLength > 32) {
                throw new IllegalArgumentException("Bad prefixLength");
            }
        } else if (!(address instanceof Inet6Address)) {
            throw new IllegalArgumentException("Unsupported family");
        } else if (prefixLength < 0 || prefixLength > KeymasterDefs.KM_ALGORITHM_HMAC) {
            throw new IllegalArgumentException("Bad prefixLength");
        }
    }
}
