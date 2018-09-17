package com.android.internal.net;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.IpPrefix;
import android.net.LinkAddress;
import android.net.Network;
import android.net.RouteInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.UserHandle;
import android.util.LogException;
import com.android.internal.R;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class VpnConfig implements Parcelable {
    public static final Creator<VpnConfig> CREATOR = new Creator<VpnConfig>() {
        public VpnConfig createFromParcel(Parcel in) {
            boolean z;
            boolean z2 = true;
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
            config.legacy = in.readInt() != 0;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            config.blocking = z;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            config.allowBypass = z;
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            config.allowIPv4 = z;
            if (in.readInt() == 0) {
                z2 = false;
            }
            config.allowIPv6 = z2;
            config.underlyingNetworks = (Network[]) in.createTypedArray(Network.CREATOR);
            return config;
        }

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
    public boolean legacy;
    public int mtu = -1;
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

    public static CharSequence getVpnLabel(Context context, String packageName) throws NameNotFoundException {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(SERVICE_INTERFACE);
        intent.setPackage(packageName);
        List<ResolveInfo> services = pm.queryIntentServices(intent, 0);
        if (services == null || services.size() != 1) {
            return pm.getApplicationInfo(packageName, 0).loadLabel(pm);
        }
        return ((ResolveInfo) services.get(0)).loadLabel(pm);
    }

    public void updateAllowedFamilies(InetAddress address) {
        if (address instanceof Inet4Address) {
            this.allowIPv4 = true;
        } else {
            this.allowIPv6 = true;
        }
    }

    public void addLegacyRoutes(String routesStr) {
        if (!routesStr.trim().equals(LogException.NO_VALUE)) {
            for (String route : routesStr.trim().split(" ")) {
                RouteInfo info = new RouteInfo(new IpPrefix(route), null);
                this.routes.add(info);
                updateAllowedFamilies(info.getDestination().getAddress());
            }
        }
    }

    public void addLegacyAddresses(String addressesStr) {
        if (!addressesStr.trim().equals(LogException.NO_VALUE)) {
            for (String address : addressesStr.trim().split(" ")) {
                LinkAddress addr = new LinkAddress(address);
                this.addresses.add(addr);
                updateAllowedFamilies(addr.getAddress());
            }
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        int i;
        int i2 = 1;
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
        if (this.blocking) {
            i = 1;
        } else {
            i = 0;
        }
        out.writeInt(i);
        if (this.allowBypass) {
            i = 1;
        } else {
            i = 0;
        }
        out.writeInt(i);
        if (this.allowIPv4) {
            i = 1;
        } else {
            i = 0;
        }
        out.writeInt(i);
        if (!this.allowIPv6) {
            i2 = 0;
        }
        out.writeInt(i2);
        out.writeTypedArray(this.underlyingNetworks, flags);
    }
}
