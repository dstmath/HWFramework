package com.android.server.connectivity.tethering;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.StringJoiner;

public class TetheringConfiguration {
    private static final String[] DHCP_DEFAULT_RANGE = new String[]{"192.168.42.2", "192.168.42.254", "192.168.43.2", "192.168.43.254", "192.168.44.2", "192.168.44.254", "192.168.45.2", "192.168.45.254", "192.168.46.2", "192.168.46.254", "192.168.47.2", "192.168.47.254", "192.168.48.2", "192.168.48.254", "192.168.49.2", "192.168.49.254", "192.168.50.2", "192.168.50.254"};
    public static final int DUN_NOT_REQUIRED = 0;
    public static final int DUN_REQUIRED = 1;
    public static final int DUN_UNSPECIFIED = 2;
    private static final boolean INIT_PDN_WIFI = SystemProperties.getBoolean("ro.config.forbid_roam_dun_wifi", false);
    private static final String TAG = TetheringConfiguration.class.getSimpleName();
    private final String[] DEFAULT_IPV4_DNS = new String[]{"8.8.4.4", "8.8.8.8"};
    public final String[] defaultIPv4DNS;
    public final String[] dhcpRanges;
    public final boolean isDunRequired;
    public final Collection<Integer> preferredUpstreamIfaceTypes;
    public final String[] tetherableBluetoothRegexs;
    public final String[] tetherableUsbRegexs;
    public final String[] tetherableWifiRegexs;

    public TetheringConfiguration(Context ctx) {
        this.tetherableUsbRegexs = ctx.getResources().getStringArray(17236041);
        this.tetherableWifiRegexs = ctx.getResources().getStringArray(17236042);
        this.tetherableBluetoothRegexs = ctx.getResources().getStringArray(17236038);
        this.preferredUpstreamIfaceTypes = getUpstreamIfaceTypes(ctx, checkDunRequired(ctx));
        this.isDunRequired = this.preferredUpstreamIfaceTypes.contains(Integer.valueOf(4));
        this.dhcpRanges = getDhcpRanges(ctx);
        this.defaultIPv4DNS = copy(this.DEFAULT_IPV4_DNS);
    }

    public boolean isUsb(String iface) {
        return matchesDownstreamRegexs(iface, this.tetherableUsbRegexs);
    }

    public boolean isWifi(String iface) {
        return matchesDownstreamRegexs(iface, this.tetherableWifiRegexs);
    }

    public boolean isBluetooth(String iface) {
        return matchesDownstreamRegexs(iface, this.tetherableBluetoothRegexs);
    }

    public void dump(PrintWriter pw) {
        dumpStringArray(pw, "tetherableUsbRegexs", this.tetherableUsbRegexs);
        dumpStringArray(pw, "tetherableWifiRegexs", this.tetherableWifiRegexs);
        dumpStringArray(pw, "tetherableBluetoothRegexs", this.tetherableBluetoothRegexs);
        pw.print("isDunRequired: ");
        pw.println(this.isDunRequired);
        String[] upstreamTypes = null;
        if (this.preferredUpstreamIfaceTypes != null) {
            upstreamTypes = new String[this.preferredUpstreamIfaceTypes.size()];
            int i = 0;
            for (Integer netType : this.preferredUpstreamIfaceTypes) {
                upstreamTypes[i] = ConnectivityManager.getNetworkTypeName(netType.intValue());
                i++;
            }
        }
        dumpStringArray(pw, "preferredUpstreamIfaceTypes", upstreamTypes);
        dumpStringArray(pw, "dhcpRanges", this.dhcpRanges);
        dumpStringArray(pw, "defaultIPv4DNS", this.defaultIPv4DNS);
    }

    private static void dumpStringArray(PrintWriter pw, String label, String[] values) {
        pw.print(label);
        pw.print(": ");
        if (values != null) {
            StringJoiner sj = new StringJoiner(", ", "[", "]");
            for (String value : values) {
                sj.add(value);
            }
            pw.print(sj.toString());
        } else {
            pw.print("null");
        }
        pw.println();
    }

    private static int checkDunRequired(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService("phone");
        if (INIT_PDN_WIFI && tm != null && tm.isNetworkRoaming()) {
            return 0;
        }
        return tm != null ? tm.getTetherApnRequired() : 2;
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static Collection<Integer> getUpstreamIfaceTypes(Context ctx, int dunCheck) {
        boolean z = true;
        int[] ifaceTypes = ctx.getResources().getIntArray(17236040);
        ArrayList<Integer> upstreamIfaceTypes = new ArrayList(ifaceTypes.length);
        for (int i : ifaceTypes) {
            switch (i) {
                case 0:
                case 5:
                    if (dunCheck == 1) {
                        break;
                    }
                case 4:
                    if (dunCheck == 0) {
                        break;
                    }
                default:
                    upstreamIfaceTypes.add(Integer.valueOf(i));
                    break;
            }
        }
        if (dunCheck == 1) {
            if (!upstreamIfaceTypes.contains(Integer.valueOf(4))) {
                upstreamIfaceTypes.add(Integer.valueOf(4));
            }
        } else if (dunCheck == 0) {
            if (!upstreamIfaceTypes.contains(Integer.valueOf(0))) {
                upstreamIfaceTypes.add(Integer.valueOf(0));
            }
            if (!upstreamIfaceTypes.contains(Integer.valueOf(5))) {
                upstreamIfaceTypes.add(Integer.valueOf(5));
            }
        } else {
            if (!(upstreamIfaceTypes.contains(Integer.valueOf(4)) || upstreamIfaceTypes.contains(Integer.valueOf(0)))) {
                z = upstreamIfaceTypes.contains(Integer.valueOf(5));
            }
            if (!z) {
                upstreamIfaceTypes.add(Integer.valueOf(0));
                upstreamIfaceTypes.add(Integer.valueOf(5));
            }
        }
        return upstreamIfaceTypes;
    }

    private static boolean matchesDownstreamRegexs(String iface, String[] regexs) {
        for (String regex : regexs) {
            if (iface.matches(regex)) {
                return true;
            }
        }
        return false;
    }

    private static String[] getDhcpRanges(Context ctx) {
        String[] fromResource = ctx.getResources().getStringArray(17236039);
        if (fromResource.length <= 0 || fromResource.length % 2 != 0) {
            return copy(DHCP_DEFAULT_RANGE);
        }
        return fromResource;
    }

    private static String[] copy(String[] strarray) {
        return (String[]) Arrays.copyOf(strarray, strarray.length);
    }
}
