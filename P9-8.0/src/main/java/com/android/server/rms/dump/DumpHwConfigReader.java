package com.android.server.rms.dump;

import android.content.Context;
import android.rms.config.ResourceConfig;
import com.android.server.am.HwActivityManagerService;
import com.android.server.display.HwEyeProtectionDividedTimeControl;
import com.android.server.rms.config.HwConfigReader;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import java.io.PrintWriter;
import junit.framework.Assert;
import junit.framework.AssertionFailedError;

public final class DumpHwConfigReader extends Assert {
    private static int[] groupIDs = new int[]{10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24};
    private static HwConfigReader mConfig;

    public static final void dumpConfigReader(PrintWriter pw, Context context) {
        mConfig = new HwConfigReader();
        mConfig.loadResConfig();
        try {
            dumpGetGroupNum();
            pw.println("<I> getGroupNum --pass");
        } catch (AssertionFailedError e) {
            pw.println("<I> getGroupNum --fail " + e);
        }
        try {
            dumpGetGroupName();
            pw.println("<I> getGroupName --pass");
        } catch (AssertionFailedError e2) {
            pw.println("<I> getGroupName --fail " + e2);
        }
        try {
            dumpIsCount();
            pw.println("<I> isCount --pass");
        } catch (AssertionFailedError e22) {
            pw.println("<I> isCount --fail " + e22);
        }
        try {
            dumpGetGroupSampleCycleNum();
            pw.println("<I> getGroupSampleCycleNum --pass");
        } catch (AssertionFailedError e222) {
            pw.println("<I> getGroupSampleCycleNum --fail " + e222);
        }
        try {
            dumpGetResConfig();
            pw.println("<I> getResConfig --pass");
            pw.println("<I> getSubTypeName --pass");
            pw.println("<I> getResourceThreshold --pass");
            pw.println("<I> getResourceStrategy --pass");
            pw.println("<I> getResourceMaxPeroid --pass");
            pw.println("<I> getLoopInterval --pass");
            pw.println("<I> getNormalThreshold --pass");
            pw.println("<I> getWarningThreshold --pass");
            pw.println("<I> getUrgentThreshold --pass");
        } catch (AssertionFailedError e2222) {
            pw.println("<I> getResConfig --fail " + e2222);
        }
        try {
            dumpGetSubTypeNum();
            pw.println("<I> getSubTypeNum --pass");
        } catch (AssertionFailedError e22222) {
            pw.println("<I> getSubTypeNum --fail " + e22222);
        }
        try {
            dumpGetSubTypeLevels();
            pw.println("<I> getSubTypeLevels --pass");
        } catch (AssertionFailedError e222222) {
            pw.println("<I> getSubTypeLevels --fail " + e222222);
        }
        try {
            dumpCountGroupID();
            pw.println("<I> getCountGroupID --pass");
        } catch (AssertionFailedError e2222222) {
            pw.println("<I> getCountGroupID --fail " + e2222222);
        }
        try {
            dumpGetSaveInterval();
            pw.println("<I> getSaveInterval --pass");
        } catch (AssertionFailedError e22222222) {
            pw.println("<I> getSaveInterval --fail " + e22222222);
        }
        try {
            dumpGetCountInterval();
            pw.println("<I> getCountInterval --pass");
        } catch (AssertionFailedError e222222222) {
            pw.println("<I> getCountInterval --fail " + e222222222);
        }
        try {
            dumpGetSampleBasePeriod();
            pw.println("<I> getSampleBasePeriod --pass");
        } catch (AssertionFailedError e2222222222) {
            pw.println("<I> getSampleBasePeriod --fail " + e2222222222);
        }
        try {
            dumpGetMaxKeepFiles();
            pw.println("<I> getMaxKeepFiles --pass");
        } catch (AssertionFailedError e22222222222) {
            pw.println("<I> getMaxKeepFiles --fail " + e22222222222);
        }
        try {
            dumpGetWhiteList();
            pw.println("<I> getWhiteList --pass");
        } catch (AssertionFailedError e222222222222) {
            pw.println("<I> getWhiteList --fail " + e222222222222);
        }
    }

    private static final void dumpGetGroupNum() {
        assertEquals(21, mConfig.getGroupNum());
    }

    private static final void dumpGetGroupName() {
        String[] expect = new String[]{"NOTIFICATION", "BROADCAST", "RECEIVER", "ALARM", "APPOPS", "PIDS", "CURSOR", "APPSERVICE", "APP", "CPU", "IO", "SCHEDGROUP", "ANR", "DELAY", "FRAMELOST"};
        for (int i = 0; i < groupIDs.length; i++) {
            assertEquals(expect[i], mConfig.getGroupName(groupIDs[i]));
        }
    }

    private static final void dumpIsCount() {
        boolean[] expect = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
        for (int i = 0; i < groupIDs.length; i++) {
            assertEquals(expect[i], mConfig.isCount(groupIDs[i]));
        }
    }

    private static final void dumpGetGroupSampleCycleNum() {
        int[] expect = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
        for (int i = 0; i < groupIDs.length; i++) {
            assertEquals(expect[i], mConfig.getGroupSampleCycleNum(groupIDs[i]));
        }
    }

    private static final void dumpGetResConfig() {
        int i;
        ResourceConfig[] expect = new ResourceConfig[]{new ResourceConfig(0, 20, 2, 0, 1000, "APP_THIRDPARTY", 30, 40, 50, 0), new ResourceConfig(1, 50, 2, 3, 1000, "APP_HW", 30, 40, 50, 0), new ResourceConfig(2, 200, 2, 1, 1000, "APP_SYS", 30, 40, 50, 0), new ResourceConfig(3, 300, 2, 2, 1000, "APP_TOTAL", 30, 40, 50, 0)};
        for (i = 0; i < 4; i++) {
            assertResConfigEquals("dump NOTIFICATION", expect[i], mConfig.getResConfig(groupIDs[0], i));
            assertSubTypeAttrEquals("dump NOTIFICATION", expect[i], groupIDs[0], i);
        }
        ResourceConfig[] expect1 = new ResourceConfig[]{new ResourceConfig(0, 30, 2, 3, 1000, "APP_THIRDPARTY", 30, 40, 50, 0), new ResourceConfig(1, HwActivityManagerService.SERVICE_ADJ, 2, 3, 1000, "APP_HW", 30, 40, 50, 0), new ResourceConfig(2, 800, 1, 5, 1000, "APP_SYS", 30, 40, 50, 0), new ResourceConfig(3, 1000, 1, 5, 1000, "APP_TOTAL", 30, 40, 50, 0)};
        for (i = 0; i < 4; i++) {
            assertResConfigEquals("dump BROADCAST", expect1[i], mConfig.getResConfig(groupIDs[1], i));
            assertSubTypeAttrEquals("dump BROADCAST", expect1[i], groupIDs[1], i);
        }
        ResourceConfig[] expect2 = new ResourceConfig[]{new ResourceConfig(0, 200, 2, 0, 1000, "APP_THIRDPARTY", 30, 40, 50, 0), new ResourceConfig(1, HwActivityManagerService.SERVICE_ADJ, 2, 0, 1000, "APP_HW", 30, 40, 50, 0), new ResourceConfig(2, 1000, 1, 0, 0, "APP_SYS", 30, 40, 50, 0), new ResourceConfig(3, 3000, 2, 0, 0, "APP_TOTAL", 30, 40, 50, 0)};
        for (i = 0; i < 4; i++) {
            assertResConfigEquals("dump RECEIVER", expect2[i], mConfig.getResConfig(groupIDs[2], i));
            assertSubTypeAttrEquals("dump RECEIVER", expect2[i], groupIDs[2], i);
        }
        ResourceConfig[] expect3 = new ResourceConfig[]{new ResourceConfig(0, 50, 1, 0, 5000, "APP_THIRDPARTY", 30, 40, 50, 0), new ResourceConfig(1, 100, 1, 0, 5000, "APP_HW", 30, 40, 50, 0), new ResourceConfig(2, 200, 1, 0, 5000, "APP_SYS", 30, 40, 50, 0), new ResourceConfig(3, 800, 1, 0, 5000, "APP_TOTAL", 30, 40, 50, 0)};
        for (i = 0; i < 4; i++) {
            assertResConfigEquals("dump ALARM", expect3[i], mConfig.getResConfig(groupIDs[3], i));
            assertSubTypeAttrEquals("dump ALARM", expect3[i], groupIDs[3], i);
        }
        ResourceConfig[] expect4 = new ResourceConfig[]{new ResourceConfig(0, 200, 1, 0, 0, "APP_THIRDPARTY", -1, -1, -1, 0)};
        for (i = 0; i < 1; i++) {
            assertResConfigEquals("dump APPOPS", expect4[i], mConfig.getResConfig(groupIDs[4], i));
            assertSubTypeAttrEquals("dump APPOPS", expect4[i], groupIDs[4], i);
        }
        ResourceConfig[] expect6 = new ResourceConfig[]{new ResourceConfig(0, 300, HwActivityManagerService.SERVICE_ADJ, 0, 0, "APP_THIRDPARTY", 30, 40, 50, 0)};
        for (i = 0; i < 1; i++) {
            assertResConfigEquals("dump PIDS", expect6[i], mConfig.getResConfig(groupIDs[6], i));
            assertSubTypeAttrEquals("dump PIDS", expect6[i], groupIDs[5], i);
        }
        ResourceConfig[] expect7 = new ResourceConfig[]{new ResourceConfig(0, 50, 2, 0, AwareAppAssociate.ASSOC_REPORT_MIN_TIME, "APP_THIRDPARTY", 30, 40, 50, 0), new ResourceConfig(1, 50, 2, 0, AwareAppAssociate.ASSOC_REPORT_MIN_TIME, "APP_HW", 30, 40, 50, 0), new ResourceConfig(2, 200, 1, 0, AwareAppAssociate.ASSOC_REPORT_MIN_TIME, "APP_SYS", 30, 40, 50, 0), new ResourceConfig(3, 1000, 1, 0, AwareAppAssociate.ASSOC_REPORT_MIN_TIME, "APP_TOTAL", 30, 40, 50, 0)};
        for (i = 0; i < 4; i++) {
            assertResConfigEquals("dump CURSOR", expect7[i], mConfig.getResConfig(groupIDs[7], i));
            assertSubTypeAttrEquals("dump CURSOR", expect7[i], groupIDs[6], i);
        }
        ResourceConfig[] expect8 = new ResourceConfig[]{new ResourceConfig(0, 1, 2, 10, 2000, "APP_THIRDPARTY", 30, 40, 50, 0), new ResourceConfig(1, 1, 2, 10, 2000, "APP_HW", 30, 40, 50, 0), new ResourceConfig(2, 1, 2, 10, 2000, "APP_SYS", 30, 40, 50, 0), new ResourceConfig(3, 1, 2, 10, 2000, "APP_TOTAL", 30, 40, 50, 0)};
        for (i = 0; i < 4; i++) {
            assertResConfigEquals("dump APPSERVICE", expect8[i], mConfig.getResConfig(groupIDs[8], i));
            assertSubTypeAttrEquals("dump APPSERVICE", expect8[i], groupIDs[7], i);
        }
        ResourceConfig[] expect9 = new ResourceConfig[]{new ResourceConfig(0, 1, 2, 10, 2000, "APP_THIRDPARTY", 30, 40, 50, 0), new ResourceConfig(1, 1, 2, 10, 2000, "APP_HW", 30, 40, 50, 0), new ResourceConfig(2, 1, 2, 10, 2000, "APP_SYS", 30, 40, 50, 0), new ResourceConfig(3, 1, 2, 10, 2000, "APP_TOTAL", 30, 40, 50, 0)};
        for (i = 0; i < 4; i++) {
            assertResConfigEquals("dump APP", expect9[i], mConfig.getResConfig(groupIDs[9], i));
            assertSubTypeAttrEquals("dump APP", expect9[i], groupIDs[8], i);
        }
        assertEquals("dump CPU", null, mConfig.getResConfig(groupIDs[9], 0));
        assertEquals("dump IO", null, mConfig.getResConfig(groupIDs[10], 0));
        assertEquals("dump SCHEDGROUP", null, mConfig.getResConfig(groupIDs[11], 0));
        assertEquals("dump ANR", null, mConfig.getResConfig(groupIDs[12], 0));
        assertEquals("dump DELAY", null, mConfig.getResConfig(groupIDs[13], 0));
        assertEquals("dump FRAMELOST", null, mConfig.getResConfig(groupIDs[14], 0));
    }

    private static final void assertResConfigEquals(String info, ResourceConfig expect, ResourceConfig actual) {
        assertEquals(info, expect.getResourceID(), actual.getResourceID());
        assertEquals(info, expect.getResourceThreshold(), actual.getResourceThreshold());
        assertEquals(info, expect.getResourceStrategy(), actual.getResourceStrategy());
        assertEquals(info, expect.getResourceMaxPeroid(), actual.getResourceMaxPeroid());
        assertEquals(info, expect.getLoopInterval(), actual.getLoopInterval());
        assertEquals(info, expect.getResouceName(), actual.getResouceName());
        assertEquals(info, expect.getResouceNormalThreshold(), actual.getResouceNormalThreshold());
        assertEquals(info, expect.getResouceWarningThreshold(), actual.getResouceWarningThreshold());
        assertEquals(info, expect.getResouceUrgentThreshold(), actual.getResouceUrgentThreshold());
    }

    private static final void assertSubTypeAttrEquals(String info, ResourceConfig expect, int groupID, int subType) {
        assertEquals("dump getSubTypeName " + info, expect.getResouceName(), mConfig.getSubTypeName(groupID, subType));
        assertEquals("dump getResourceThreshold " + info, expect.getResourceThreshold(), mConfig.getResourceThreshold(groupID, subType));
        assertEquals("dump getResourceStrategy " + info, expect.getResourceStrategy(), mConfig.getResourceStrategy(groupID, subType));
        assertEquals("dump getResourceMaxPeroid " + info, expect.getResourceMaxPeroid(), mConfig.getResourceMaxPeroid(groupID, subType));
        assertEquals("dump getLoopInterval " + info, expect.getLoopInterval(), mConfig.getLoopInterval(groupID, subType));
        assertEquals("dump getNormalThreshold " + info, expect.getResouceNormalThreshold(), mConfig.getNormalThreshold(groupID, subType));
        assertEquals("dump getWarningThreshold " + info, expect.getResouceWarningThreshold(), mConfig.getWarningThreshold(groupID, subType));
        assertEquals("dump getUrgentThreshold " + info, expect.getResouceUrgentThreshold(), mConfig.getUrgentThreshold(groupID, subType));
    }

    private static final void dumpGetSubTypeNum() {
        int[] expect = new int[]{4, 4, 4, 4, 4, 1, 4, 4, 4, 0, 0, 0, 0, 0, 0};
        for (int i = 0; i < expect.length; i++) {
            assertEquals(expect[i], mConfig.getSubTypeNum(groupIDs[i]));
        }
    }

    private static final void dumpGetSubTypeLevels() {
        int i;
        for (i = 0; i < 4; i++) {
            assertEquals("dump NOTIFICATION", 0, mConfig.getSubTypeLevels(groupIDs[0], i).size());
        }
        for (i = 0; i < 4; i++) {
            assertEquals("dump BROADCAST", 0, mConfig.getSubTypeLevels(groupIDs[1], i).size());
        }
        for (i = 0; i < 4; i++) {
            assertEquals("dump RECEIVER", 0, mConfig.getSubTypeLevels(groupIDs[2], i).size());
        }
        for (i = 0; i < 4; i++) {
            assertEquals("dump ALARM", 0, mConfig.getSubTypeLevels(groupIDs[3], i).size());
        }
        for (i = 0; i < 1; i++) {
            assertEquals("dump APPOPS", 0, mConfig.getSubTypeLevels(groupIDs[4], i).size());
        }
        for (i = 0; i < 1; i++) {
            assertEquals("dump PIDS", 0, mConfig.getSubTypeLevels(groupIDs[5], i).size());
        }
        for (i = 0; i < 4; i++) {
            assertEquals("dump CURSOR", 0, mConfig.getSubTypeLevels(groupIDs[6], i).size());
        }
        for (i = 0; i < 4; i++) {
            assertEquals("dump APPSERVICE", 0, mConfig.getSubTypeLevels(groupIDs[7], i).size());
        }
        for (i = 0; i < 4; i++) {
            assertEquals("dump APPSERVICE", 0, mConfig.getSubTypeLevels(groupIDs[8], i).size());
        }
        assertEquals("dump CPU", null, mConfig.getSubTypeLevels(groupIDs[9], 0));
        assertEquals("dump IO", null, mConfig.getSubTypeLevels(groupIDs[10], 0));
        assertEquals("dump SCHEDGROUP", null, mConfig.getSubTypeLevels(groupIDs[11], 0));
        assertEquals("dump ANR", null, mConfig.getSubTypeLevels(groupIDs[12], 0));
        assertEquals("dump DELAY", null, mConfig.getSubTypeLevels(groupIDs[13], 0));
        assertEquals("dump FRAMELOST", null, mConfig.getSubTypeLevels(groupIDs[14], 0));
    }

    private static final void dumpCountGroupID() {
        int[] group = new int[]{26, 27, 28};
        assertEquals("group num ", 3, mConfig.getCountGroupID().size());
        for (int i = 0; i < 3; i++) {
            assertEquals(group[i], ((Integer) mConfig.getCountGroupID().get(i)).intValue());
        }
    }

    private static final void dumpGetSaveInterval() {
        assertEquals(30, mConfig.getSaveInterval());
    }

    private static final void dumpGetCountInterval() {
        assertEquals(10080, mConfig.getCountInterval(false));
        assertEquals(HwEyeProtectionDividedTimeControl.DAY_IN_MINUTE, mConfig.getCountInterval(true));
    }

    private static final void dumpGetSampleBasePeriod() {
        assertEquals(AwareAppAssociate.ASSOC_REPORT_MIN_TIME, mConfig.getSampleBasePeriod());
    }

    private static final void dumpGetMaxKeepFiles() {
        assertEquals(10, mConfig.getMaxKeepFiles());
    }

    private static final void dumpGetWhiteList() {
        String[] expect = new String[]{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null};
        for (int i = 0; i < groupIDs.length; i++) {
            assertEquals("case " + i, expect[i], mConfig.getWhiteList(groupIDs[i]));
        }
    }
}
