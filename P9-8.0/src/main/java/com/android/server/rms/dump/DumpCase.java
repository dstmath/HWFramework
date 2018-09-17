package com.android.server.rms.dump;

import android.content.Context;
import android.rms.utils.Utils;
import java.io.PrintWriter;

public final class DumpCase {
    public static final boolean dump(Context context, PrintWriter pw, String[] args) {
        if (!Utils.DEBUG) {
            return false;
        }
        if (Utils.scanArgs(args, "--dump-ProcessShrinker")) {
            DumpShrinker.dumpProcessShrinker();
            return true;
        } else if (Utils.scanArgs(args, "--dump-SystemShrinker")) {
            DumpShrinker.dumpSystemShrinker();
            return true;
        } else if (Utils.scanArgs(args, "--dump-DownloadScene")) {
            DumpScene.dumpDownloadScene(context);
            return true;
        } else if (Utils.scanArgs(args, "--dump-MediaScene")) {
            DumpScene.dumpMediaScene(context);
            return true;
        } else if (Utils.scanArgs(args, "--dump-NonIdleScene")) {
            DumpScene.dumpNonIdleScene(context);
            return true;
        } else if (Utils.scanArgs(args, "--dump-PhoneScene")) {
            DumpScene.dumpPhoneScene(context);
            return true;
        } else if (Utils.scanArgs(args, "--dump-HwConfigReader")) {
            DumpHwConfigReader.dumpConfigReader(pw, context);
            return true;
        } else if (Utils.scanArgs(args, "--dump-NotificationWhiteList")) {
            DumpResource.dumpNotificationWhiteList(context);
            return true;
        } else if (Utils.scanArgs(args, "--dump-ContentObserver")) {
            DumpResource.dumpContentObserver(context, args);
            return true;
        } else if (Utils.scanArgs(args, "--dump-ActivityResource")) {
            DumpActivityResource.dumpActivity(context, args);
            return true;
        } else if (Utils.scanArgs(args, "--dump-AppAssociate")) {
            DumpAppAssociate.dumpAppAssociate(pw, context, args);
            return true;
        } else if (Utils.scanArgs(args, "--dump-AppImportance")) {
            DumpAppKeyBackgroup.dumpAppImportance(pw, context, args);
            return true;
        } else if (Utils.scanArgs(args, "--dump-AppWhitelist")) {
            DumpAppWhiteList.dumpAppWhiteList(pw, context, args);
            return true;
        } else if (Utils.scanArgs(args, "--dump-AwareUserHabit")) {
            DumpAwareUserHabit.dumpAwareUserHabit(pw, args);
            return true;
        } else if (Utils.scanArgs(args, "--dump-AppUsageTime-getUsageTime")) {
            DumpAppUsageTime.dumpGetUsageTime(context, args);
            return true;
        } else if (Utils.scanArgs(args, "--dump-AppUsageTime-historyApp")) {
            DumpAppUsageTime.dumpIsHistoryInstalledApp(context, args);
            return true;
        } else if (Utils.scanArgs(args, "--dump-AppUsageTime-dumpRecorded")) {
            DumpAppUsageTime.dumpRecordedAppUsageInfo();
            return true;
        } else if (Utils.scanArgs(args, "--dump-AppResource-getPolicyType")) {
            DumpAppResource.getPolicyType(context, args);
            return true;
        } else if (!Utils.scanArgs(args, "--dump-AppResource-dumpDispatchAQV")) {
            return false;
        } else {
            DumpAppResource.dumpDispatchAQV(context, args);
            return true;
        }
    }
}
