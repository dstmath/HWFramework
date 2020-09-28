package android.telephony;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.BatteryStats;
import android.os.ParcelUuid;
import com.android.internal.util.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AnomalyReporter {
    private static final String TAG = "AnomalyReporter";
    private static Context sContext = null;
    private static String sDebugPackageName = null;
    private static Map<UUID, Integer> sEvents = new ConcurrentHashMap();

    private AnomalyReporter() {
    }

    public static void reportAnomaly(UUID eventId, String description) {
        if (sContext == null) {
            Rlog.w(TAG, "AnomalyReporter not yet initialized, dropping event=" + eventId);
            return;
        }
        Integer count = Integer.valueOf(sEvents.containsKey(eventId) ? sEvents.get(eventId).intValue() + 1 : 1);
        sEvents.put(eventId, count);
        if (count.intValue() <= 1 && sDebugPackageName != null) {
            Intent dbgIntent = new Intent(TelephonyManager.ACTION_ANOMALY_REPORTED);
            dbgIntent.putExtra(TelephonyManager.EXTRA_ANOMALY_ID, new ParcelUuid(eventId));
            if (description != null) {
                dbgIntent.putExtra(TelephonyManager.EXTRA_ANOMALY_DESCRIPTION, description);
            }
            dbgIntent.setPackage(sDebugPackageName);
            sContext.sendBroadcast(dbgIntent, Manifest.permission.READ_PRIVILEGED_PHONE_STATE);
        }
    }

    public static void initialize(Context context) {
        List<ResolveInfo> packages;
        if (context != null) {
            context.enforceCallingOrSelfPermission(Manifest.permission.MODIFY_PHONE_STATE, "This app does not have privileges to send debug events");
            sContext = context;
            PackageManager pm = sContext.getPackageManager();
            if (!(pm == null || (packages = pm.queryBroadcastReceivers(new Intent(TelephonyManager.ACTION_ANOMALY_REPORTED), BatteryStats.HistoryItem.MOST_INTERESTING_STATES)) == null || packages.isEmpty())) {
                if (packages.size() > 1) {
                    Rlog.e(TAG, "Multiple Anomaly Receivers installed.");
                }
                for (ResolveInfo r : packages) {
                    if (r.activityInfo != null && pm.checkPermission(Manifest.permission.READ_PRIVILEGED_PHONE_STATE, r.activityInfo.packageName) == 0) {
                        Rlog.d(TAG, "Found a valid package " + r.activityInfo.packageName);
                        sDebugPackageName = r.activityInfo.packageName;
                        return;
                    } else if (r.activityInfo != null) {
                        Rlog.w(TAG, "Found package without proper permissions or no activity" + r.activityInfo.packageName);
                    }
                }
                return;
            }
            return;
        }
        throw new IllegalArgumentException("AnomalyReporter needs a non-null context.");
    }

    public static void dump(FileDescriptor fd, PrintWriter printWriter, String[] args) {
        if (sContext != null) {
            IndentingPrintWriter pw = new IndentingPrintWriter(printWriter, "  ");
            sContext.enforceCallingOrSelfPermission(Manifest.permission.DUMP, "Requires DUMP");
            StringBuilder sb = new StringBuilder();
            sb.append("Initialized=");
            sb.append(sContext != null ? "Yes" : "No");
            pw.println(sb.toString());
            pw.println("Debug Package=" + sDebugPackageName);
            pw.println("Anomaly Counts:");
            pw.increaseIndent();
            for (UUID event : sEvents.keySet()) {
                pw.println(event + ": " + sEvents.get(event));
            }
            pw.decreaseIndent();
            pw.flush();
        }
    }
}
