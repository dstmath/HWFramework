package com.android.internal.util;

import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Binder;
import android.os.Handler;
import android.text.TextUtils;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.function.Predicate;

public final class DumpUtils {
    private static final boolean DEBUG = false;
    private static final String TAG = "DumpUtils";

    public interface Dump {
        void dump(PrintWriter printWriter, String str);
    }

    private DumpUtils() {
    }

    public static void dumpAsync(Handler handler, final Dump dump, PrintWriter pw, final String prefix, long timeout) {
        final StringWriter sw = new StringWriter();
        if (handler.runWithScissors(new Runnable() {
            public void run() {
                PrintWriter lpw = new FastPrintWriter((Writer) sw);
                dump.dump(lpw, prefix);
                lpw.close();
            }
        }, timeout)) {
            pw.print(sw.toString());
        } else {
            pw.println("... timed out");
        }
    }

    private static void logMessage(PrintWriter pw, String msg) {
        pw.println(msg);
    }

    public static boolean checkDumpPermission(Context context, String tag, PrintWriter pw) {
        if (context.checkCallingOrSelfPermission("android.permission.DUMP") == 0) {
            return true;
        }
        logMessage(pw, "Permission Denial: can't dump " + tag + " from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " due to missing android.permission.DUMP permission");
        return false;
    }

    public static boolean checkUsageStatsPermission(Context context, String tag, PrintWriter pw) {
        int uid = Binder.getCallingUid();
        if (uid == 0 || uid == 1000 || uid == 1067 || uid == 2000) {
            return true;
        }
        if (context.checkCallingOrSelfPermission("android.permission.PACKAGE_USAGE_STATS") != 0) {
            logMessage(pw, "Permission Denial: can't dump " + tag + " from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " due to missing android.permission.PACKAGE_USAGE_STATS permission");
            return false;
        }
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(AppOpsManager.class);
        String[] pkgs = context.getPackageManager().getPackagesForUid(uid);
        if (pkgs != null) {
            for (String pkg : pkgs) {
                int noteOpNoThrow = appOps.noteOpNoThrow(43, uid, pkg);
                if (noteOpNoThrow == 0 || noteOpNoThrow == 3) {
                    return true;
                }
            }
        }
        logMessage(pw, "Permission Denial: can't dump " + tag + " from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " due to android:get_usage_stats app-op not allowed");
        return false;
    }

    public static boolean checkDumpAndUsageStatsPermission(Context context, String tag, PrintWriter pw) {
        return checkDumpPermission(context, tag, pw) && checkUsageStatsPermission(context, tag, pw);
    }

    public static boolean isPlatformPackage(String packageName) {
        return packageName != null && (packageName.equals("android") || packageName.startsWith("android.") || packageName.startsWith("com.android."));
    }

    public static boolean isPlatformPackage(ComponentName cname) {
        return cname != null && isPlatformPackage(cname.getPackageName());
    }

    public static boolean isPlatformPackage(ComponentName.WithComponentName wcn) {
        return wcn != null && isPlatformPackage(wcn.getComponentName());
    }

    public static boolean isNonPlatformPackage(String packageName) {
        return packageName != null && !isPlatformPackage(packageName);
    }

    public static boolean isNonPlatformPackage(ComponentName cname) {
        return cname != null && isNonPlatformPackage(cname.getPackageName());
    }

    public static boolean isNonPlatformPackage(ComponentName.WithComponentName wcn) {
        return wcn != null && !isPlatformPackage(wcn.getComponentName());
    }

    public static <TRec extends ComponentName.WithComponentName> Predicate<TRec> filterRecord(String filterString) {
        if (TextUtils.isEmpty(filterString)) {
            return $$Lambda$DumpUtils$D1OlZP6xIpu72ypnJd0fzx0wd6I.INSTANCE;
        }
        if ("all".equals(filterString)) {
            return $$Lambda$DumpUtils$eRa1rlfDk6Og2yFeXGHqUGPzRF0.INSTANCE;
        }
        if ("all-platform".equals(filterString)) {
            return $$Lambda$kVylv1rl9MOSbHFZoVyK5dl1kfY.INSTANCE;
        }
        if ("all-non-platform".equals(filterString)) {
            return $$Lambda$JwOUSWW2Jzu15y4Kn4JuPh8tWM.INSTANCE;
        }
        ComponentName filterCname = ComponentName.unflattenFromString(filterString);
        if (filterCname != null) {
            return new Predicate(filterCname) {
                private final /* synthetic */ ComponentName f$0;

                {
                    this.f$0 = r1;
                }

                public final boolean test(Object obj) {
                    return DumpUtils.lambda$filterRecord$1(this.f$0, (ComponentName.WithComponentName) obj);
                }
            };
        }
        return new Predicate(ParseUtils.parseIntWithBase(filterString, 16, -1), filterString) {
            private final /* synthetic */ int f$0;
            private final /* synthetic */ String f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final boolean test(Object obj) {
                return DumpUtils.lambda$filterRecord$2(this.f$0, this.f$1, (ComponentName.WithComponentName) obj);
            }
        };
    }

    static /* synthetic */ boolean lambda$filterRecord$0(ComponentName.WithComponentName rec) {
        return false;
    }

    static /* synthetic */ boolean lambda$filterRecord$1(ComponentName filterCname, ComponentName.WithComponentName rec) {
        return rec != null && filterCname.equals(rec.getComponentName());
    }

    static /* synthetic */ boolean lambda$filterRecord$2(int id, String filterString, ComponentName.WithComponentName rec) {
        return (id != -1 && System.identityHashCode(rec) == id) || rec.getComponentName().flattenToString().toLowerCase().contains(filterString.toLowerCase());
    }
}
