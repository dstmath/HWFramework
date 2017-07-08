package com.android.server.rms.test;

import android.content.Context;
import com.android.server.rms.statistic.HwResStatisticImpl;
import com.android.server.security.trustcircle.IOTController;
import java.io.PrintWriter;
import junit.framework.Assert;
import junit.framework.AssertionFailedError;

public final class TestHwResStatisticImpl extends Assert {
    public static final String TAG = "TestHwResStatisticImpl";

    public static final void testResStatisticImpl(PrintWriter pw, Context context) {
        try {
            testGetResStatistic();
            pw.println("<I> getResStatistic --pass");
        } catch (AssertionFailedError e) {
            pw.println("<I> getResStatistic --fail");
        }
        try {
            testInit();
            pw.println("<I> init --pass");
        } catch (AssertionFailedError e2) {
            pw.println("<I> init --fail");
        }
        try {
            testSample();
            pw.println("<I> sample --pass");
        } catch (AssertionFailedError e3) {
            pw.println("<I> sample --fail");
        }
        try {
            testStatistic();
            pw.println("<I> statistic --pass");
        } catch (AssertionFailedError e4) {
            pw.println("<I> statistic --fail");
        }
        try {
            testObtainResRecordMap();
            pw.println("<I> obtainResRecordMap --pass");
        } catch (AssertionFailedError e5) {
            pw.println("<I> obtainResRecordMap --fail");
        }
        try {
            testResetResRecordMap();
            pw.println("<I> resetResRecordMap --pass");
        } catch (AssertionFailedError e6) {
            pw.println("<I> resetResRecordMap --fail");
        }
    }

    private static final void testGetResStatistic() {
        assertSame("case1", HwResStatisticImpl.getResStatistic(20), HwResStatisticImpl.getResStatistic(20));
        assertEquals("case2", null, HwResStatisticImpl.getResStatistic(-1));
    }

    public static final void testInit() {
        assertTrue("case 1", new HwResStatisticImpl().init(null));
    }

    public static final void testSample() {
        HwResStatisticImpl s = new HwResStatisticImpl();
        assertNotNull("case 1", s.sample(20));
        assertNull("case 2", s.sample(-1));
        assertNull("case 3", s.sample(IOTController.TYPE_MASTER));
    }

    public static final void testStatistic() {
        assertTrue("case 1", new HwResStatisticImpl().statistic(null));
    }

    public static final void testObtainResRecordMap() {
        assertNull(new HwResStatisticImpl().obtainResRecordMap());
    }

    public static final void testResetResRecordMap() {
        assertTrue(new HwResStatisticImpl().resetResRecordMap(null));
    }
}
