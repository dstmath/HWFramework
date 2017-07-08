package com.android.server.rms.test;

import android.content.Context;
import com.android.server.rms.utils.Utils;
import java.io.PrintWriter;

public final class TestCase {
    public static final boolean test(Context context, PrintWriter pw, String[] args) {
        if (Utils.scanArgs(args, "--test-ProcessShrinker")) {
            TestShrinker.testProcessShrinker();
            return true;
        } else if (Utils.scanArgs(args, "--test-SystemShrinker")) {
            TestShrinker.testSystemShrinker();
            return true;
        } else if (Utils.scanArgs(args, "--test-DownloadScene")) {
            TestScene.testDownloadScene(context);
            return true;
        } else if (Utils.scanArgs(args, "--test-MediaScene")) {
            TestScene.testMediaScene(context);
            return true;
        } else if (Utils.scanArgs(args, "--test-NonIdleScene")) {
            TestScene.testNonIdleScene(context);
            return true;
        } else if (Utils.scanArgs(args, "--test-PhoneScene")) {
            TestScene.testPhoneScene(context);
            return true;
        } else if (Utils.scanArgs(args, "--test-MemQuery")) {
            TestMemory.testMemQuery(context);
            return true;
        } else if (Utils.scanArgs(args, "--test-MemAcquire")) {
            TestMemory.testMemAcquire(context);
            return true;
        } else if (Utils.scanArgs(args, "--test-MemAppAcquire")) {
            TestMemory.testMemAppAcquire(args);
            return true;
        } else if (Utils.scanArgs(args, "--test-MemInterrupt")) {
            TestMemory.testMemInterrupt(context);
            return true;
        } else if (Utils.scanArgs(args, "DisableMemLog")) {
            TestMemory.testDisableMemLog(context);
            return true;
        } else if (Utils.scanArgs(args, "EnableMemLog")) {
            TestMemory.testEnableMemLog(context);
            return true;
        } else if (Utils.scanArgs(args, "--test-MemoryInfoReader")) {
            TestMemInfo.testMemoryInfoReader(context);
            return true;
        } else if (Utils.scanArgs(args, "--test-MemoryFragReader")) {
            TestMemInfo.testMemoryFragReader(context);
            return true;
        } else if (Utils.scanArgs(args, "--test-ProcMemReader")) {
            TestMemInfo.testProcMemoryReader(context, args);
            return true;
        } else if (Utils.scanArgs(args, "--test-HwConfigReader")) {
            TestHwConfigReader.testConfigReader(pw, context);
            return true;
        } else if (Utils.scanArgs(args, "--test-NotificationWhiteList")) {
            TestResource.testNotificationWhiteList(context);
            return true;
        } else if (Utils.scanArgs(args, "--test-ContentObserver")) {
            TestResource.testContentObserver(context, args);
            return true;
        } else if (Utils.scanArgs(args, "--test-ActivityResource")) {
            TestActivityResource.testActivity(context, args);
            return true;
        } else if (Utils.scanArgs(args, "--test-HwResRecord")) {
            TestHwResRecord.testResRecord(pw, context);
            return true;
        } else if (Utils.scanArgs(args, "--test-HwTimeStatistic")) {
            TestHwTimeStatistic.testTimeStatistic(pw, context);
            return true;
        } else if (Utils.scanArgs(args, "--test-HwResStatisticImpl")) {
            TestHwResStatisticImpl.testResStatisticImpl(pw, context);
            return true;
        } else if (Utils.scanArgs(args, "--test-HwStatisticCtl")) {
            TestHwStatisticCtl.testStatisticCtl(pw, context);
            return true;
        } else if (Utils.scanArgs(args, "--test-RamStatistic")) {
            TestRamStatistic.testRamStatistic(pw, context);
            return true;
        } else if (Utils.scanArgs(args, "--test-IO-readstats")) {
            TestIOResourceService.getInstance(context).testPeriodReadTask();
            return true;
        } else {
            String adduidParam = Utils.scanArgsWithParam(args, "--test-IO-write-adduid");
            if (adduidParam != null) {
                TestIOResourceService.getInstance(context).testRefreshAddUidMonitored(adduidParam);
                return true;
            }
            String removeUidParam = Utils.scanArgsWithParam(args, "--test-IO-write-removeuid");
            if (removeUidParam != null) {
                TestIOResourceService.getInstance(context).testRefreshRemoveUidMonitored(removeUidParam);
                return true;
            } else if (Utils.scanArgs(args, "--test-IO-write-shutdown")) {
                TestIOResourceService.getInstance(context).testShutdown();
                return true;
            } else if (Utils.scanArgs(args, "--test-IO-monitor")) {
                TestIOResourceService.getInstance(context).testPeriodMonitorTask();
                return true;
            } else if (Utils.scanArgs(args, "--test-AppAssociate")) {
                TestAppAssociate.testAppAssociate(pw, context, args);
                return true;
            } else if (Utils.scanArgs(args, "--test-AppImportance")) {
                TestAppKeyBackgroup.testAppImportance(pw, context, args);
                return true;
            } else if (Utils.scanArgs(args, "--test-AppWhitelist")) {
                TestAppWhiteList.testAppWhiteList(pw, context, args);
                return true;
            } else if (Utils.scanArgs(args, "--test-AwareUserHabit")) {
                TestAwareUserHabit.testAwareUserHabit(pw, context, args);
                return true;
            } else if (Utils.scanArgs(args, "--test-AppUsageTime-getUsageTime")) {
                TestAppUsageTime.testGetUsageTime(context, args);
                return true;
            } else if (Utils.scanArgs(args, "--test-AppUsageTime-historyApp")) {
                TestAppUsageTime.testIsHistoryInstalledApp(context, args);
                return true;
            } else if (Utils.scanArgs(args, "--test-AppUsageTime-dumpRecorded")) {
                TestAppUsageTime.dumpRecordedAppUsageInfo();
                return true;
            } else if (Utils.scanArgs(args, "--test-AppResource-getPolicyType")) {
                TestAppResource.getPolicyType(context, args);
                return true;
            } else if (!Utils.scanArgs(args, "--test-AppResource-testDispatchAQV")) {
                return false;
            } else {
                TestAppResource.testDispatchAQV(context, args);
                return true;
            }
        }
    }
}
