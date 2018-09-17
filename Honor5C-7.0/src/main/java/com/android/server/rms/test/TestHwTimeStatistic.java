package com.android.server.rms.test;

import android.content.Context;
import android.os.SystemClock;
import com.android.server.net.HwNetworkStatsService;
import com.android.server.rms.statistic.HwTimeStatistic;
import java.io.PrintWriter;
import junit.framework.Assert;
import junit.framework.AssertionFailedError;

public final class TestHwTimeStatistic extends Assert {
    public static final String TAG = "TestHwTimeStatistic";

    public static final void testTimeStatistic(PrintWriter pw, Context context) {
        try {
            testInit();
            pw.println("<I> init --pass");
        } catch (Exception e) {
            pw.println("<I> init --fail " + e);
        }
        try {
            testAccumulateTime();
            pw.println("<I> accumulateTime --pass");
        } catch (AssertionFailedError e2) {
            pw.println("<I> accumulateTime --fail " + e2);
        }
        try {
            testIsTimeToSave();
            pw.println("<I> isTimeToSave --pass");
        } catch (AssertionFailedError e22) {
            pw.println("<I> isTimeToSave --fail " + e22);
        }
        try {
            testGetCurrentTime();
            pw.println("<I> getCurrentTime --pass");
        } catch (AssertionFailedError e222) {
            pw.println("<I> getCurrentTime --fail " + e222);
        }
        try {
            testUpdateSaveTime();
            pw.println("<I> updateSaveTime --pass");
        } catch (AssertionFailedError e2222) {
            pw.println("<I> updateSaveTime --fail " + e2222);
        }
        try {
            testGetAccumulateTime();
            pw.println("<I> getAccumulateTime --pass");
        } catch (AssertionFailedError e22222) {
            pw.println("<I> getAccumulateTime --fail " + e22222);
        }
        try {
            testGetCurrentWeek();
            pw.println("<I> getCurrentWeek --pass");
        } catch (AssertionFailedError e222222) {
            pw.println("<I> getCurrentWeek --fail " + e222222);
        }
        try {
            testIsNewWeek();
            pw.println("<I> isNewWeek --pass");
        } catch (AssertionFailedError e2222222) {
            pw.println("<I> isNewWeek --fail " + e2222222);
        }
        try {
            testUpdateWeek();
            pw.println("<I> updateWeek --pass");
        } catch (AssertionFailedError e22222222) {
            pw.println("<I> updateWeek --fail " + e22222222);
        }
    }

    private static final void testInit() {
        HwTimeStatistic ts = new HwTimeStatistic();
        ts.init(0, 0, 0);
        ts.init(-1, -1, -1);
        ts.init(100, 100, 100);
    }

    private static final void testAccumulateTime() {
        boolean z;
        boolean z2 = false;
        HwTimeStatistic ts = new HwTimeStatistic();
        ts.init(0, 0, 0);
        SystemClock.sleep(1000);
        long time = ts.accumulateTime();
        String str = "case 1 ";
        if (time <= 995 || time >= 1005) {
            z = false;
        } else {
            z = true;
        }
        assertTrue(str, z);
        ts.init(0, 0, -1000);
        SystemClock.sleep(1000);
        time = ts.accumulateTime();
        str = "case 2 ";
        if (time <= 995 || time >= 1005) {
            z = false;
        } else {
            z = true;
        }
        assertTrue(str, z);
        ts.init(0, 0, 1000);
        SystemClock.sleep(1000);
        time = ts.accumulateTime();
        String str2 = "case 3 ";
        if (time > 1995 && time < 2005) {
            z2 = true;
        }
        assertTrue(str2, z2);
    }

    private static final void testIsTimeToSave() {
        boolean z;
        HwTimeStatistic ts = new HwTimeStatistic();
        ts.init(1000, 0, 0);
        long beginTime = SystemClock.elapsedRealtime();
        assertTrue("case 1 ", ts.isTimeToSave(1001 + beginTime));
        assertTrue("case 2 ", !ts.isTimeToSave(995 + beginTime));
        assertTrue("case 3 ", !ts.isTimeToSave(0));
        ts.init(-1000, 0, 0);
        beginTime = SystemClock.elapsedRealtime();
        assertTrue("case 4 ", ts.isTimeToSave((HwNetworkStatsService.UPLOAD_INTERVAL + beginTime) + 1));
        assertTrue("case 5 ", !ts.isTimeToSave((HwNetworkStatsService.UPLOAD_INTERVAL + beginTime) - 5));
        assertTrue("case 6 ", !ts.isTimeToSave(0));
        assertTrue("case 7 ", !ts.isTimeToSave(-1));
        ts.init(0, 0, 0);
        beginTime = SystemClock.elapsedRealtime();
        assertTrue("case 8 ", ts.isTimeToSave((HwNetworkStatsService.UPLOAD_INTERVAL + beginTime) + 1));
        assertTrue("case 9 ", (ts.isTimeToSave((HwNetworkStatsService.UPLOAD_INTERVAL + beginTime) - 5) ? null : 1) != null);
        assertTrue("case 10 ", !ts.isTimeToSave(0));
        String str = "case 11 ";
        if (ts.isTimeToSave(-1)) {
            z = false;
        } else {
            z = true;
        }
        assertTrue(str, z);
    }

    private static final void testGetCurrentTime() {
        HwTimeStatistic ts = new HwTimeStatistic();
        ts.init(0, 0, 0);
        long beginTime = SystemClock.elapsedRealtime();
        SystemClock.sleep(1000);
        ts.accumulateTime();
        long diff = ts.getCurrentTime() - beginTime;
        String str = "case 1 " + diff;
        boolean z = diff > 995 && diff < 1005;
        assertTrue(str, z);
        ts.init(0, 0, -1000);
        beginTime = SystemClock.elapsedRealtime();
        SystemClock.sleep(1000);
        ts.accumulateTime();
        diff = ts.getCurrentTime() - beginTime;
        str = "case 2 ";
        z = diff > 995 && diff < 1005;
        assertTrue(str, z);
        ts.init(0, 0, 1000);
        beginTime = SystemClock.elapsedRealtime();
        SystemClock.sleep(1000);
        ts.accumulateTime();
        diff = ts.getCurrentTime() - beginTime;
        str = "case 3 ";
        if (diff <= 995 || diff >= 1005) {
            z = false;
        } else {
            z = true;
        }
        assertTrue(str, z);
    }

    private static final void testUpdateSaveTime() {
        HwTimeStatistic ts = new HwTimeStatistic();
        ts.init(1000, 0, 0);
        assertEquals("case 1", 1000, ts.updateSaveTime(1000));
        ts.init(-1000, 0, 0);
        assertEquals("case 2", 0, ts.updateSaveTime(0));
        ts.init(0, 0, 0);
        assertEquals("case 3", 0, ts.updateSaveTime(-1000));
    }

    private static final void testGetAccumulateTime() {
        HwTimeStatistic ts = new HwTimeStatistic();
        ts.init(0, 0, 1000);
        assertEquals("case 1 ", 1000, ts.getAccumulateTime());
        SystemClock.sleep(1000);
        ts.accumulateTime();
        long time = ts.getAccumulateTime();
        String str = "case 2 ";
        boolean z = time > 1995 && time < 2005;
        assertTrue(str, z);
        ts.init(0, 0, 0);
        assertEquals("case 3 ", 0, ts.getAccumulateTime());
        SystemClock.sleep(1000);
        ts.accumulateTime();
        time = ts.getAccumulateTime();
        str = "case 4 ";
        z = time > 995 && time < 1005;
        assertTrue(str, z);
        ts.init(0, 0, -1000);
        assertEquals("case 5 ", 0, ts.getAccumulateTime());
        SystemClock.sleep(1000);
        ts.accumulateTime();
        time = ts.getAccumulateTime();
        str = "case 6 ";
        if (time <= 995 || time >= 1005) {
            z = false;
        } else {
            z = true;
        }
        assertTrue(str, z);
    }

    private static final void testGetCurrentWeek() {
        HwTimeStatistic ts = new HwTimeStatistic();
        ts.init(0, 0, 10079999);
        assertEquals("case 1", 1, ts.getCurrentWeek());
        ts.init(0, 0, 10080000);
        assertEquals("case 2", 2, ts.getCurrentWeek());
        ts.init(0, 100, 99);
        assertEquals("case 3", 1, ts.getCurrentWeek());
        ts.init(0, 100, 100);
        assertEquals("case 4", 2, ts.getCurrentWeek());
        ts.init(0, 100, 101);
        assertEquals("case 5", 2, ts.getCurrentWeek());
        ts.init(0, 100, 101);
        ts.updateWeek(250);
        assertEquals("case 6", 3, ts.getCurrentWeek());
    }

    private static final void testIsNewWeek() {
        boolean z;
        boolean z2 = false;
        HwTimeStatistic ts = new HwTimeStatistic();
        ts.init(0, 0, 10079999);
        assertTrue("case 1", ts.isNewWeek(10080000));
        ts.init(0, 0, 10080000);
        String str = "case 2";
        if (ts.isNewWeek(10080000)) {
            z = false;
        } else {
            z = true;
        }
        assertTrue(str, z);
        ts.init(0, 0, 10080000);
        str = "case 3";
        if (ts.isNewWeek(10080010)) {
            z = false;
        } else {
            z = true;
        }
        assertTrue(str, z);
        ts.init(0, 100, 99);
        assertTrue("case 4", ts.isNewWeek(100));
        ts.init(0, 100, 99);
        String str2 = "case 5";
        if (!ts.isNewWeek(98)) {
            z2 = true;
        }
        assertTrue(str2, z2);
    }

    private static final void testUpdateWeek() {
        boolean z;
        boolean z2 = false;
        HwTimeStatistic ts = new HwTimeStatistic();
        ts.init(0, 0, 0);
        String str = "case 1";
        if (ts.isNewWeek(10079999)) {
            z = false;
        } else {
            z = true;
        }
        assertTrue(str, z);
        ts.init(0, 0, 0);
        assertTrue("case 2", ts.isNewWeek(10080000));
        ts.init(0, 0, 10080000);
        str = "case 3";
        if (ts.isNewWeek(-1)) {
            z = false;
        } else {
            z = true;
        }
        assertTrue(str, z);
        ts.init(0, 100, 0);
        str = "case 4";
        if (ts.isNewWeek(99)) {
            z = false;
        } else {
            z = true;
        }
        assertTrue(str, z);
        ts.init(0, 100, 0);
        assertTrue("case 5", ts.isNewWeek(101));
        ts.init(0, 100, 0);
        String str2 = "case 6";
        if (!ts.isNewWeek(-1)) {
            z2 = true;
        }
        assertTrue(str2, z2);
    }
}
