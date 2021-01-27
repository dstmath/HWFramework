package com.android.server.notification;

import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import android.service.notification.ConditionProviderService;
import android.service.notification.IConditionProvider;
import android.util.TimeUtils;
import com.android.server.notification.NotificationManagerService;
import java.io.PrintWriter;
import java.util.Date;

public abstract class SystemConditionProviderService extends ConditionProviderService {
    public abstract IConditionProvider asInterface();

    public abstract void attachBase(Context context);

    public abstract void dump(PrintWriter printWriter, NotificationManagerService.DumpFilter dumpFilter);

    public abstract ComponentName getComponent();

    public abstract boolean isValidConditionId(Uri uri);

    public abstract void onBootComplete();

    protected static String ts(long time) {
        return new Date(time) + " (" + time + ")";
    }

    protected static String formatDuration(long millis) {
        StringBuilder sb = new StringBuilder();
        TimeUtils.formatDuration(millis, sb);
        return sb.toString();
    }

    protected static void dumpUpcomingTime(PrintWriter pw, String var, long time, long now) {
        pw.print("      ");
        pw.print(var);
        pw.print('=');
        if (time > 0) {
            pw.printf("%s, in %s, now=%s", ts(time), formatDuration(time - now), ts(now));
        } else {
            pw.print(time);
        }
        pw.println();
    }
}
