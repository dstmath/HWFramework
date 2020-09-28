package com.android.internal.net;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.IpPrefix;
import android.net.LinkAddress;
import android.net.Network;
import android.net.ProxyInfo;
import android.net.RouteInfo;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.UserHandle;
import com.android.internal.R;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VpnConfig implements Parcelable {
    public static final Parcelable.Creator<VpnConfig> CREATOR = new Parcelable.Creator<VpnConfig>() {
        /* class com.android.internal.net.VpnConfig.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public VpnConfig createFromParcel(Parcel in) {
            VpnConfig config = new VpnConfig();
            config.user = in.readString();
            config.interfaze = in.readString();
            config.session = in.readString();
            config.mtu = in.readInt();
            in.readTypedList(config.addresses, LinkAddress.CREATOR);
            in.readTypedList(config.routes, RouteInfo.CREATOR);
            config.dnsServers = in.createStringArrayList();
            config.searchDomains = in.createStringArrayList();
            config.allowedApplications = in.createStringArrayList();
            config.disallowedApplications = in.createStringArrayList();
            config.configureIntent = (PendingIntent) in.readParcelable(null);
            config.startTime = in.readLong();
            boolean z = true;
            config.legacy = in.readInt() != 0;
            config.blocking = in.readInt() != 0;
            config.allowBypass = in.readInt() != 0;
            config.allowIPv4 = in.readInt() != 0;
            config.allowIPv6 = in.readInt() != 0;
            if (in.readInt() == 0) {
                z = false;
            }
            config.isMetered = z;
            config.underlyingNetworks = (Network[]) in.createTypedArray(Network.CREATOR);
            config.proxyInfo = (ProxyInfo) in.readParcelable(null);
            return config;
        }

        @Override // android.os.Parcelable.Creator
        public VpnConfig[] newArray(int size) {
            return new VpnConfig[size];
        }
    };
    public static final String DIALOGS_PACKAGE = "com.android.vpndialogs";
    public static final String LEGACY_VPN = "[Legacy VPN]";
    public static final String SERVICE_INTERFACE = "android.net.VpnService";
    public List<LinkAddress> addresses = new ArrayList();
    public boolean allowBypass;
    public boolean allowIPv4;
    public boolean allowIPv6;
    public List<String> allowedApplications;
    public boolean blocking;
    public PendingIntent configureIntent;
    public List<String> disallowedApplications;
    public List<String> dnsServers;
    public String interfaze;
    public boolean isMetered = true;
    public boolean legacy;
    public int mtu = -1;
    public ProxyInfo proxyInfo;
    public List<RouteInfo> routes = new ArrayList();
    public List<String> searchDomains;
    public String session;
    public long startTime = -1;
    public Network[] underlyingNetworks;
    public String user;

    public static Intent getIntentForConfirmation() {
        Intent intent = new Intent();
        ComponentName componentName = ComponentName.unflattenFromString(Resources.getSystem().getString(R.string.config_customVpnConfirmDialogComponent));
        intent.setClassName(componentName.getPackageName(), componentName.getClassName());
        return intent;
    }

    public static PendingIntent getIntentForStatusPanel(Context context) {
        Intent intent = new Intent();
        intent.setClassName(DIALOGS_PACKAGE, "com.android.vpndialogs.ManageDialog");
        intent.addFlags(1350565888);
        return PendingIntent.getActivityAsUser(context, 0, intent, 0, null, UserHandle.CURRENT);
    }

    public static CharSequence getVpnLabel(Context context, String packageName) throws PackageManager.NameNotFoundException {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent("android.net.VpnService");
        intent.setPackage(packageName);
        List<ResolveInfo> services = pm.queryIntentServices(intent, 0);
        if (services == null || services.size() != 1) {
            return pm.getApplicationInfo(packageName, 0).loadLabel(pm);
        }
        return services.get(0).loadLabel(pm);
    }

    public void updateAllowedFamilies(InetAddress address) {
        if (address instanceof Inet4Address) {
            this.allowIPv4 = true;
        } else {
            this.allowIPv6 = true;
        }
    }

    public void addLegacyRoutes(String routesStr) {
        if (!routesStr.trim().equals("")) {
            for (String route : routesStr.trim().split(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER)) {
                RouteInfo info = new RouteInfo(new IpPrefix(route), (InetAddress) null);
                this.routes.add(info);
                updateAllowedFamilies(info.getDestination().getAddress());
            }
        }
    }

    public void addLegacyAddresses(String addressesStr) {
        if (!addressesStr.trim().equals("")) {
            for (String address : addressesStr.trim().split(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER)) {
                LinkAddress addr = new LinkAddress(address);
                this.addresses.add(addr);
                updateAllowedFamilies(addr.getAddress());
            }
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.user);
        out.writeString(this.interfaze);
        out.writeString(this.session);
        out.writeInt(this.mtu);
        out.writeTypedList(this.addresses);
        out.writeTypedList(this.routes);
        out.writeStringList(this.dnsServers);
        out.writeStringList(this.searchDomains);
        out.writeStringList(this.allowedApplications);
        out.writeStringList(this.disallowedApplications);
        out.writeParcelable(this.configureIntent, flags);
        out.writeLong(this.startTime);
        out.writeInt(this.legacy ? 1 : 0);
        out.writeInt(this.blocking ? 1 : 0);
        out.writeInt(this.allowBypass ? 1 : 0);
        out.writeInt(this.allowIPv4 ? 1 : 0);
        out.writeInt(this.allowIPv6 ? 1 : 0);
        out.writeInt(this.isMetered ? 1 : 0);
        out.writeTypedArray(this.underlyingNetworks, flags);
        out.writeParcelable(this.proxyInfo, flags);
    }

    public String toString() {
        return "VpnConfig" + "{ user=" + this.user + ", interface=" + this.interfaze + ", session=" + this.session + ", mtu=" + this.mtu + ", addresses=" + toString(this.addresses) + ", routes=" + toString(this.routes) + ", dns=" + toString(this.dnsServers) + ", searchDomains=" + toString(this.searchDomains) + ", allowedApps=" + toString(this.allowedApplications) + ", disallowedApps=" + toString(this.disallowedApplications) + ", configureIntent=" + this.configureIntent + ", startTime=" + this.startTime + ", legacy=" + this.legacy + ", blocking=" + this.blocking + ", allowBypass=" + this.allowBypass + ", allowIPv4=" + this.allowIPv4 + ", allowIPv6=" + this.allowIPv6 + ", underlyingNetworks=" + Arrays.toString(this.underlyingNetworks) + ", proxyInfo=" + this.proxyInfo + "}";
    }

    static <T> String toString(List<T> ls) {
        if (ls == null) {
            return "null";
        }
        return Arrays.toString(ls.toArray());
    }
}
