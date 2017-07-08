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
import com.android.internal.R;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class VpnConfig implements Parcelable {
    public static final Creator<VpnConfig> CREATOR = null;
    public static final String DIALOGS_PACKAGE = "com.android.vpndialogs";
    public static final String LEGACY_VPN = "[Legacy VPN]";
    public static final String SERVICE_INTERFACE = "android.net.VpnService";
    public List<LinkAddress> addresses;
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
    public int mtu;
    public List<RouteInfo> routes;
    public List<String> searchDomains;
    public String session;
    public long startTime;
    public Network[] underlyingNetworks;
    public String user;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.net.VpnConfig.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.net.VpnConfig.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.net.VpnConfig.<clinit>():void");
    }

    public VpnConfig() {
        this.mtu = -1;
        this.addresses = new ArrayList();
        this.routes = new ArrayList();
        this.startTime = -1;
    }

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
        if (!routesStr.trim().equals("")) {
            for (String route : routesStr.trim().split(" ")) {
                RouteInfo info = new RouteInfo(new IpPrefix(route), null);
                this.routes.add(info);
                updateAllowedFamilies(info.getDestination().getAddress());
            }
        }
    }

    public void addLegacyAddresses(String addressesStr) {
        if (!addressesStr.trim().equals("")) {
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
