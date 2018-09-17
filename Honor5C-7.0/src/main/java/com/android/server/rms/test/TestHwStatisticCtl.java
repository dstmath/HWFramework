package com.android.server.rms.test;

import android.content.Context;
import com.android.server.rms.config.HwConfigReader;
import com.android.server.rms.statistic.HwStatisticCtl;
import java.io.PrintWriter;

public final class TestHwStatisticCtl {
    public static final String TAG = "TestHwStatisticCtl";

    public static final void testStatisticCtl(PrintWriter pw, Context context) {
        try {
            testInit();
            pw.println("<I> init --pass");
        } catch (Exception e) {
            pw.println("<I> init --fail " + e);
        }
        try {
            testStatisticGroups();
            pw.println("<I> collectGroups --pass");
        } catch (Exception e2) {
            pw.println("<I> collectGroups --fail " + e2);
        }
    }

    private static final void testInit() {
        HwConfigReader config = new HwConfigReader();
        config.loadResConfig();
        new HwStatisticCtl(config).init();
        new HwStatisticCtl(null).init();
    }

    public static final void testStatisticGroups() {
        HwConfigReader config = new HwConfigReader();
        config.loadResConfig();
        HwStatisticCtl sc = new HwStatisticCtl(config);
        sc.init();
        sc.statisticGroups();
        HwStatisticCtl sc1 = new HwStatisticCtl(null);
        sc1.init();
        sc1.statisticGroups();
        new HwStatisticCtl(null).statisticGroups();
    }
}
