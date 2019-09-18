package com.android.server.policy;

import android.app.ActivityManager;
import android.content.Context;
import android.provider.Settings;
import android.util.ArraySet;
import android.util.HwPCUtils;
import android.util.Slog;
import android.view.WindowManager;
import com.android.server.policy.WindowManagerPolicy;
import java.io.PrintWriter;
import java.io.StringWriter;

public class PolicyControl {
    private static boolean DEBUG = false;
    private static final String NAME_IMMERSIVE_FULL = "immersive.full";
    private static final String NAME_IMMERSIVE_NAVIGATION = "immersive.navigation";
    private static final String NAME_IMMERSIVE_PRECONFIRMATIONS = "immersive.preconfirms";
    private static final String NAME_IMMERSIVE_STATUS = "immersive.status";
    private static String TAG = "PolicyControl";
    private static Filter sImmersiveNavigationFilter;
    private static Filter sImmersivePreconfirmationsFilter;
    private static Filter sImmersiveStatusFilter;
    private static String sSettingValue;

    private static class Filter {
        private static final String ALL = "*";
        private static final String APPS = "apps";
        private final ArraySet<String> mBlacklist;
        private final ArraySet<String> mWhitelist;

        private Filter(ArraySet<String> whitelist, ArraySet<String> blacklist) {
            this.mWhitelist = whitelist;
            this.mBlacklist = blacklist;
        }

        /* access modifiers changed from: package-private */
        public boolean matches(WindowManager.LayoutParams attrs) {
            if (attrs == null) {
                return false;
            }
            boolean isApp = attrs.type >= 1 && attrs.type <= 99;
            if ((isApp && this.mBlacklist.contains(APPS)) || onBlacklist(attrs.packageName)) {
                return false;
            }
            if (!isApp || !this.mWhitelist.contains(APPS)) {
                return onWhitelist(attrs.packageName);
            }
            return true;
        }

        /* access modifiers changed from: package-private */
        public boolean matches(String packageName) {
            return !onBlacklist(packageName) && onWhitelist(packageName);
        }

        private boolean onBlacklist(String packageName) {
            return this.mBlacklist.contains(packageName) || this.mBlacklist.contains(ALL);
        }

        private boolean onWhitelist(String packageName) {
            return this.mWhitelist.contains(ALL) || this.mWhitelist.contains(packageName);
        }

        /* access modifiers changed from: package-private */
        public void dump(PrintWriter pw) {
            pw.print("Filter[");
            dump("whitelist", this.mWhitelist, pw);
            pw.print(',');
            dump("blacklist", this.mBlacklist, pw);
            pw.print(']');
        }

        private void dump(String name, ArraySet<String> set, PrintWriter pw) {
            pw.print(name);
            pw.print("=(");
            int n = set.size();
            for (int i = 0; i < n; i++) {
                if (i > 0) {
                    pw.print(',');
                }
                pw.print(set.valueAt(i));
            }
            pw.print(')');
        }

        public String toString() {
            StringWriter sw = new StringWriter();
            dump(new PrintWriter(sw, true));
            return sw.toString();
        }

        static Filter parse(String value) {
            if (value == null) {
                return null;
            }
            ArraySet<String> whitelist = new ArraySet<>();
            ArraySet<String> blacklist = new ArraySet<>();
            for (String token : value.split(",")) {
                String token2 = token.trim();
                if (!token2.startsWith("-") || token2.length() <= 1) {
                    whitelist.add(token2);
                } else {
                    blacklist.add(token2.substring(1));
                }
            }
            return new Filter(whitelist, blacklist);
        }
    }

    public static int getSystemUiVisibility(WindowManagerPolicy.WindowState win, WindowManager.LayoutParams attrs) {
        int vis;
        if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer()) {
            return 67043332;
        }
        WindowManager.LayoutParams attrs2 = attrs != null ? attrs : win.getAttrs();
        if (win != null) {
            vis = win.getSystemUiVisibility();
        } else {
            vis = attrs2.systemUiVisibility | attrs2.subtreeSystemUiVisibility;
        }
        if (sImmersiveStatusFilter != null && sImmersiveStatusFilter.matches(attrs2)) {
            vis = (vis | 5124) & -1073742081;
        }
        if (sImmersiveNavigationFilter != null && sImmersiveNavigationFilter.matches(attrs2)) {
            vis = (vis | 4610) & 2147483391;
        }
        return vis;
    }

    public static int getWindowFlags(WindowManagerPolicy.WindowState win, WindowManager.LayoutParams attrs) {
        WindowManager.LayoutParams attrs2 = attrs != null ? attrs : win.getAttrs();
        int flags = attrs2.flags;
        if (sImmersiveStatusFilter != null && sImmersiveStatusFilter.matches(attrs2)) {
            flags = (flags | 1024) & -67110913;
        }
        if (sImmersiveNavigationFilter == null || !sImmersiveNavigationFilter.matches(attrs2)) {
            return flags;
        }
        return flags & -134217729;
    }

    public static int adjustClearableFlags(WindowManagerPolicy.WindowState win, int clearableFlags) {
        WindowManager.LayoutParams attrs = win != null ? win.getAttrs() : null;
        if (sImmersiveStatusFilter == null || !sImmersiveStatusFilter.matches(attrs)) {
            return clearableFlags;
        }
        return clearableFlags & -5;
    }

    public static boolean disableImmersiveConfirmation(String pkg) {
        return (sImmersivePreconfirmationsFilter != null && sImmersivePreconfirmationsFilter.matches(pkg)) || ActivityManager.isRunningInTestHarness();
    }

    public static void reloadFromSetting(Context context) {
        if (DEBUG) {
            Slog.d(TAG, "reloadFromSetting()");
        }
        try {
            String value = Settings.Global.getStringForUser(context.getContentResolver(), "policy_control", -2);
            if (sSettingValue == null || !sSettingValue.equals(value)) {
                setFilters(value);
                sSettingValue = value;
            }
        } catch (Throwable t) {
            String str = TAG;
            Slog.w(str, "Error loading policy control, value=" + null, t);
        }
    }

    public static void dump(String prefix, PrintWriter pw) {
        dump("sImmersiveStatusFilter", sImmersiveStatusFilter, prefix, pw);
        dump("sImmersiveNavigationFilter", sImmersiveNavigationFilter, prefix, pw);
        dump("sImmersivePreconfirmationsFilter", sImmersivePreconfirmationsFilter, prefix, pw);
    }

    private static void dump(String name, Filter filter, String prefix, PrintWriter pw) {
        pw.print(prefix);
        pw.print("PolicyControl.");
        pw.print(name);
        pw.print('=');
        if (filter == null) {
            pw.println("null");
            return;
        }
        filter.dump(pw);
        pw.println();
    }

    private static void setFilters(String value) {
        if (DEBUG) {
            Slog.d(TAG, "setFilters: " + value);
        }
        sImmersiveStatusFilter = null;
        sImmersiveNavigationFilter = null;
        sImmersivePreconfirmationsFilter = null;
        if (value != null) {
            for (String nvp : value.split(":")) {
                int i = nvp.indexOf(61);
                if (i != -1) {
                    String n = nvp.substring(0, i);
                    String v = nvp.substring(i + 1);
                    if (n.equals(NAME_IMMERSIVE_FULL)) {
                        Filter f = Filter.parse(v);
                        sImmersiveNavigationFilter = f;
                        sImmersiveStatusFilter = f;
                        if (sImmersivePreconfirmationsFilter == null) {
                            sImmersivePreconfirmationsFilter = f;
                        }
                    } else if (n.equals(NAME_IMMERSIVE_STATUS)) {
                        sImmersiveStatusFilter = Filter.parse(v);
                    } else if (n.equals(NAME_IMMERSIVE_NAVIGATION)) {
                        Filter f2 = Filter.parse(v);
                        sImmersiveNavigationFilter = f2;
                        if (sImmersivePreconfirmationsFilter == null) {
                            sImmersivePreconfirmationsFilter = f2;
                        }
                    } else if (n.equals(NAME_IMMERSIVE_PRECONFIRMATIONS)) {
                        sImmersivePreconfirmationsFilter = Filter.parse(v);
                    }
                }
            }
        }
        if (DEBUG) {
            Slog.d(TAG, "immersiveStatusFilter: " + sImmersiveStatusFilter);
            Slog.d(TAG, "immersiveNavigationFilter: " + sImmersiveNavigationFilter);
            Slog.d(TAG, "immersivePreconfirmationsFilter: " + sImmersivePreconfirmationsFilter);
        }
    }
}
