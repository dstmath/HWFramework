package com.android.server.rms.test;

import android.content.Context;
import com.android.server.rms.config.HwConfigReader;
import com.android.server.rms.statistic.HwResRecord;
import com.android.server.rms.statistic.RamStatistic;
import java.io.PrintWriter;
import java.util.Map;
import junit.framework.Assert;
import junit.framework.AssertionFailedError;

public final class TestRamStatistic extends Assert {
    public static final String TAG = "TestRamStatistic";

    public static final void testRamStatistic(PrintWriter pw, Context context) {
        try {
            testGetInstance();
            pw.println("<I> getInstance --pass");
        } catch (AssertionFailedError e) {
            pw.println("<I> getInstance --fail " + e);
        }
        try {
            pw.println("<I> sample data = " + RamStatistic.getInstance().sample(20));
            pw.println("<I> statistic --result = " + testStatistic());
        } catch (AssertionFailedError e2) {
            pw.println("<I> fail " + e2);
        }
    }

    private static final void testGetInstance() {
        assertSame("case 1", RamStatistic.getInstance(), RamStatistic.getInstance());
    }

    private static final Map<String, HwResRecord> testStatistic() {
        HwConfigReader config = new HwConfigReader();
        config.loadResConfig();
        RamStatistic obj = RamStatistic.getInstance();
        obj.init(config);
        obj.statistic(obj.sample(20));
        return obj.obtainResRecordMap();
    }
}
