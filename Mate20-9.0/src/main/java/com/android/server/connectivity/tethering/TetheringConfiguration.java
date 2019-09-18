package com.android.server.connectivity.tethering;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.util.SharedLog;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.StringJoiner;

public class TetheringConfiguration {
    private static final String[] DHCP_DEFAULT_RANGE = {"192.168.42.2", "192.168.42.254", "192.168.43.2", "192.168.43.254", "192.168.44.2", "192.168.44.254", "192.168.45.2", "192.168.45.254", "192.168.46.2", "192.168.46.254", "192.168.47.2", "192.168.47.254", "192.168.48.2", "192.168.48.254", "192.168.49.2", "192.168.49.254", "192.168.50.2", "192.168.50.254"};
    @VisibleForTesting
    public static final int DUN_NOT_REQUIRED = 0;
    public static final int DUN_REQUIRED = 1;
    public static final int DUN_UNSPECIFIED = 2;
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final boolean INIT_PDN_WIFI = SystemProperties.getBoolean("ro.config.forbid_roam_dun_wifi", false);
    private static final String TAG = TetheringConfiguration.class.getSimpleName();
    private final String[] DEFAULT_IPV4_DNS = {"8.8.4.4", "8.8.8.8"};
    public final String[] defaultIPv4DNS;
    public final String[] dhcpRanges;
    public final int dunCheck;
    public final boolean isDunRequired;
    public final Collection<Integer> preferredUpstreamIfaceTypes;
    public final String[] provisioningApp;
    public final String provisioningAppNoUi;
    public final String[] tetherableBluetoothRegexs;
    public final String[] tetherableUsbRegexs;
    public final String[] tetherableWifiRegexs;

    public TetheringConfiguration(Context ctx, SharedLog log) {
        SharedLog configLog = log.forSubComponent("config");
        this.tetherableUsbRegexs = getResourceStringArray(ctx, 17236046);
        this.tetherableWifiRegexs = getResourceStringArray(ctx, 17236047);
        this.tetherableBluetoothRegexs = getResourceStringArray(ctx, 17236043);
        this.dunCheck = checkDunRequired(ctx);
        configLog.log("DUN check returned: " + dunCheckString(this.dunCheck));
        this.preferredUpstreamIfaceTypes = getUpstreamIfaceTypes(ctx, this.dunCheck);
        this.isDunRequired = this.preferredUpstreamIfaceTypes.contains(4);
        this.dhcpRanges = getDhcpRanges(ctx);
        this.defaultIPv4DNS = copy(this.DEFAULT_IPV4_DNS);
        this.provisioningApp = getResourceStringArray(ctx, 17236019);
        this.provisioningAppNoUi = getProvisioningAppNoUi(ctx);
        configLog.log(toString());
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

    public boolean hasMobileHotspotProvisionApp() {
        return !TextUtils.isEmpty(this.provisioningAppNoUi);
    }

    public void dump(PrintWriter pw) {
        dumpStringArray(pw, "tetherableUsbRegexs", this.tetherableUsbRegexs);
        dumpStringArray(pw, "tetherableWifiRegexs", this.tetherableWifiRegexs);
        dumpStringArray(pw, "tetherableBluetoothRegexs", this.tetherableBluetoothRegexs);
        pw.print("isDunRequired: ");
        pw.println(this.isDunRequired);
        dumpStringArray(pw, "preferredUpstreamIfaceTypes", preferredUpstreamNames(this.preferredUpstreamIfaceTypes));
        dumpStringArray(pw, "dhcpRanges", this.dhcpRanges);
        dumpStringArray(pw, "defaultIPv4DNS", this.defaultIPv4DNS);
        dumpStringArray(pw, "provisioningApp", this.provisioningApp);
        pw.print("provisioningAppNoUi: ");
        pw.println(this.provisioningAppNoUi);
    }

    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add(String.format("tetherableUsbRegexs:%s", new Object[]{makeString(this.tetherableUsbRegexs)}));
        sj.add(String.format("tetherableWifiRegexs:%s", new Object[]{makeString(this.tetherableWifiRegexs)}));
        sj.add(String.format("tetherableBluetoothRegexs:%s", new Object[]{makeString(this.tetherableBluetoothRegexs)}));
        sj.add(String.format("isDunRequired:%s", new Object[]{Boolean.valueOf(this.isDunRequired)}));
        sj.add(String.format("preferredUpstreamIfaceTypes:%s", new Object[]{makeString(preferredUpstreamNames(this.preferredUpstreamIfaceTypes))}));
        sj.add(String.format("provisioningApp:%s", new Object[]{makeString(this.provisioningApp)}));
        sj.add(String.format("provisioningAppNoUi:%s", new Object[]{this.provisioningAppNoUi}));
        return String.format("TetheringConfiguration{%s}", new Object[]{sj.toString()});
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

    private static String makeString(String[] strings) {
        if (strings == null) {
            return "null";
        }
        StringJoiner sj = new StringJoiner(",", "[", "]");
        for (String s : strings) {
            sj.add(s);
        }
        return sj.toString();
    }

    private static String[] preferredUpstreamNames(Collection<Integer> upstreamTypes) {
        String[] upstreamNames = null;
        if (upstreamTypes != null) {
            upstreamNames = new String[upstreamTypes.size()];
            int i = 0;
            for (Integer netType : upstreamTypes) {
                upstreamNames[i] = ConnectivityManager.getNetworkTypeName(netType.intValue());
                i++;
            }
        }
        return upstreamNames;
    }

    public static int checkDunRequired(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService("phone");
        if (INIT_PDN_WIFI && tm != null && tm.isNetworkRoaming()) {
            return 0;
        }
        return tm != null ? tm.getTetherApnRequired() : 2;
    }

    private static String dunCheckString(int dunCheck2) {
        switch (dunCheck2) {
            case 0:
                return "DUN_NOT_REQUIRED";
            case 1:
                return "DUN_REQUIRED";
            case 2:
                return "DUN_UNSPECIFIED";
            default:
                return String.format("UNKNOWN (%s)", new Object[]{Integer.valueOf(dunCheck2)});
        }
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    private static Collection<Integer> getUpstreamIfaceTypes(Context ctx, int dunCheck2) {
        int[] ifaceTypes = ctx.getResources().getIntArray(17236045);
        ArrayList<Integer> upstreamIfaceTypes = new ArrayList<>(ifaceTypes.length);
        for (int i : ifaceTypes) {
            if (i != 0) {
                switch (i) {
                    case 4:
                        break;
                    case 5:
                        break;
                }
            }
            if (dunCheck2 == 1) {
            }
            upstreamIfaceTypes.add(Integer.valueOf(i));
            continue;
        }
        Slog.d(TAG, "upstreamIfaceTypes " + upstreamIfaceTypes + ", dunCheck is " + dunCheck2);
        if (dunCheck2 == 1) {
            appendIfNotPresent(upstreamIfaceTypes, 4);
        } else if (dunCheck2 == 0) {
            appendIfNotPresent(upstreamIfaceTypes, 0);
            appendIfNotPresent(upstreamIfaceTypes, 5);
        } else {
            if (!containsOneOf(upstreamIfaceTypes, 4, 0, 5)) {
                upstreamIfaceTypes.add(0);
                upstreamIfaceTypes.add(5);
            }
        }
        prependIfNotPresent(upstreamIfaceTypes, 9);
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
        String[] fromResource = getResourceStringArray(ctx, 17236044);
        if (fromResource.length <= 0 || fromResource.length % 2 != 0) {
            return copy(DHCP_DEFAULT_RANGE);
        }
        return fromResource;
    }

    private static String getProvisioningAppNoUi(Context ctx) {
        try {
            return ctx.getResources().getString(17039831);
        } catch (Resources.NotFoundException e) {
            return BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        }
    }

    private static String[] getResourceStringArray(Context ctx, int resId) {
        try {
            String[] strArray = ctx.getResources().getStringArray(resId);
            return strArray != null ? strArray : EMPTY_STRING_ARRAY;
        } catch (Resources.NotFoundException e) {
            return EMPTY_STRING_ARRAY;
        }
    }

    private static String[] copy(String[] strarray) {
        return (String[]) Arrays.copyOf(strarray, strarray.length);
    }

    private static void prependIfNotPresent(ArrayList<Integer> list, int value) {
        if (!list.contains(Integer.valueOf(value))) {
            list.add(0, Integer.valueOf(value));
        }
    }

    private static void appendIfNotPresent(ArrayList<Integer> list, int value) {
        if (!list.contains(Integer.valueOf(value))) {
            list.add(Integer.valueOf(value));
        }
    }

    private static boolean containsOneOf(ArrayList<Integer> list, Integer... values) {
        for (Integer value : values) {
            if (list.contains(value)) {
                return true;
            }
        }
        return false;
    }
}
