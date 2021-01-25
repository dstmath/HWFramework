package com.android.server.connectivity.tethering;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.util.SharedLog;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
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
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final boolean INIT_PDN_WIFI = SystemProperties.getBoolean("ro.config.forbid_roam_dun_wifi", false);
    private static final String[] LEGACY_DHCP_DEFAULT_RANGE = {"192.168.42.2", "192.168.42.254", "192.168.43.2", "192.168.43.254", "192.168.44.2", "192.168.44.254", "192.168.45.2", "192.168.45.254", "192.168.46.2", "192.168.46.254", "192.168.47.2", "192.168.47.254", "192.168.48.2", "192.168.48.254", "192.168.49.2", "192.168.49.254", "192.168.50.2", "192.168.50.254"};
    private static final String TAG = TetheringConfiguration.class.getSimpleName();
    private final String[] DEFAULT_IPV4_DNS = {"8.8.4.4", "8.8.8.8"};
    public final boolean chooseUpstreamAutomatically;
    public final String[] defaultIPv4DNS;
    public final boolean enableLegacyDhcpServer;
    public final boolean isDunRequired;
    public final String[] legacyDhcpRanges;
    public final Collection<Integer> preferredUpstreamIfaceTypes;
    public final String[] provisioningApp;
    public final String provisioningAppNoUi;
    public final int provisioningCheckPeriod;
    public final int subId;
    public final String[] tetherableBluetoothRegexs;
    public final String[] tetherableUsbRegexs;
    public final String[] tetherableWifiRegexs;

    public TetheringConfiguration(Context ctx, SharedLog log, int id) {
        SharedLog configLog = log.forSubComponent("config");
        this.subId = id;
        Resources res = getResources(ctx, this.subId);
        this.tetherableUsbRegexs = getResourceStringArray(res, 17236071);
        this.tetherableWifiRegexs = getResourceStringArray(res, 17236072);
        this.tetherableBluetoothRegexs = getResourceStringArray(res, 17236068);
        this.isDunRequired = checkDunRequired(ctx);
        this.chooseUpstreamAutomatically = getResourceBoolean(res, 17891552);
        this.preferredUpstreamIfaceTypes = getUpstreamIfaceTypes(res, this.isDunRequired);
        this.legacyDhcpRanges = getLegacyDhcpRanges(res);
        this.defaultIPv4DNS = copy(this.DEFAULT_IPV4_DNS);
        this.enableLegacyDhcpServer = getEnableLegacyDhcpServer(ctx);
        this.provisioningApp = getResourceStringArray(res, 17236038);
        this.provisioningAppNoUi = getProvisioningAppNoUi(res);
        this.provisioningCheckPeriod = getResourceInteger(res, 17694845, 0);
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
        pw.print("subId: ");
        pw.println(this.subId);
        dumpStringArray(pw, "tetherableUsbRegexs", this.tetherableUsbRegexs);
        dumpStringArray(pw, "tetherableWifiRegexs", this.tetherableWifiRegexs);
        dumpStringArray(pw, "tetherableBluetoothRegexs", this.tetherableBluetoothRegexs);
        pw.print("isDunRequired: ");
        pw.println(this.isDunRequired);
        pw.print("chooseUpstreamAutomatically: ");
        pw.println(this.chooseUpstreamAutomatically);
        dumpStringArray(pw, "preferredUpstreamIfaceTypes", preferredUpstreamNames(this.preferredUpstreamIfaceTypes));
        dumpStringArray(pw, "legacyDhcpRanges", this.legacyDhcpRanges);
        dumpStringArray(pw, "defaultIPv4DNS", this.defaultIPv4DNS);
        dumpStringArray(pw, "provisioningApp", this.provisioningApp);
        pw.print("provisioningAppNoUi: ");
        pw.println(this.provisioningAppNoUi);
        pw.print("enableLegacyDhcpServer: ");
        pw.println(this.enableLegacyDhcpServer);
    }

    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add(String.format("subId:%d", Integer.valueOf(this.subId)));
        sj.add(String.format("tetherableUsbRegexs:%s", makeString(this.tetherableUsbRegexs)));
        sj.add(String.format("tetherableWifiRegexs:%s", makeString(this.tetherableWifiRegexs)));
        sj.add(String.format("tetherableBluetoothRegexs:%s", makeString(this.tetherableBluetoothRegexs)));
        sj.add(String.format("isDunRequired:%s", Boolean.valueOf(this.isDunRequired)));
        sj.add(String.format("chooseUpstreamAutomatically:%s", Boolean.valueOf(this.chooseUpstreamAutomatically)));
        sj.add(String.format("preferredUpstreamIfaceTypes:%s", makeString(preferredUpstreamNames(this.preferredUpstreamIfaceTypes))));
        sj.add(String.format("provisioningApp:%s", makeString(this.provisioningApp)));
        sj.add(String.format("provisioningAppNoUi:%s", this.provisioningAppNoUi));
        sj.add(String.format("enableLegacyDhcpServer:%s", Boolean.valueOf(this.enableLegacyDhcpServer)));
        return String.format("TetheringConfiguration{%s}", sj.toString());
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

    public static boolean checkDunRequired(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService("phone");
        if ((!INIT_PDN_WIFI || tm == null || !tm.isNetworkRoaming()) && tm != null) {
            return tm.getTetherApnRequired();
        }
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:6:0x001e, code lost:
        if (r8 != 5) goto L_0x0025;
     */
    private static Collection<Integer> getUpstreamIfaceTypes(Resources res, boolean dunRequired) {
        int[] ifaceTypes = res.getIntArray(17236070);
        ArrayList<Integer> upstreamIfaceTypes = new ArrayList<>(ifaceTypes.length);
        int length = ifaceTypes.length;
        for (int i = 0; i < length; i++) {
            int i2 = ifaceTypes[i];
            if (i2 != 0) {
                if (i2 == 4) {
                }
            }
            if (dunRequired) {
            }
            upstreamIfaceTypes.add(Integer.valueOf(i2));
        }
        Slog.d(TAG, "upstreamIfaceTypes " + upstreamIfaceTypes + ", dunCheck is " + dunRequired);
        if (dunRequired) {
            appendIfNotPresent(upstreamIfaceTypes, 4);
        } else if (!containsOneOf(upstreamIfaceTypes, 0, 5)) {
            upstreamIfaceTypes.add(0);
            upstreamIfaceTypes.add(5);
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

    private static String[] getLegacyDhcpRanges(Resources res) {
        String[] fromResource = getResourceStringArray(res, 17236069);
        if (fromResource.length <= 0 || fromResource.length % 2 != 0) {
            return copy(LEGACY_DHCP_DEFAULT_RANGE);
        }
        return fromResource;
    }

    private static String getProvisioningAppNoUi(Resources res) {
        try {
            return res.getString(17039876);
        } catch (Resources.NotFoundException e) {
            return "";
        }
    }

    private static boolean getResourceBoolean(Resources res, int resId) {
        try {
            return res.getBoolean(resId);
        } catch (Resources.NotFoundException e) {
            return false;
        }
    }

    private static String[] getResourceStringArray(Resources res, int resId) {
        try {
            String[] strArray = res.getStringArray(resId);
            return strArray != null ? strArray : EMPTY_STRING_ARRAY;
        } catch (Resources.NotFoundException e) {
            return EMPTY_STRING_ARRAY;
        }
    }

    private static int getResourceInteger(Resources res, int resId, int defaultValue) {
        try {
            return res.getInteger(resId);
        } catch (Resources.NotFoundException e) {
            return defaultValue;
        }
    }

    private static boolean getEnableLegacyDhcpServer(Context ctx) {
        if (Settings.Global.getInt(ctx.getContentResolver(), "tether_enable_legacy_dhcp_server", 1) != 0) {
            return true;
        }
        return false;
    }

    private Resources getResources(Context ctx, int subId2) {
        if (subId2 != -1) {
            return getResourcesForSubIdWrapper(ctx, subId2);
        }
        return ctx.getResources();
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public Resources getResourcesForSubIdWrapper(Context ctx, int subId2) {
        return SubscriptionManager.getResourcesForSubId(ctx, subId2);
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
