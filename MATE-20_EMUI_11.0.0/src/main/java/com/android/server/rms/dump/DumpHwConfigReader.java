package com.android.server.rms.dump;

import android.content.Context;
import android.rms.config.ResourceConfig;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.rms.config.HwConfigReader;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import java.io.PrintWriter;
import junit.framework.Assert;
import junit.framework.AssertionFailedError;

public final class DumpHwConfigReader extends Assert {
    private static int[] groupIDs = {10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24};
    private static HwConfigReader mConfig;

    public static final void dumpConfigReader(PrintWriter pw, Context context) {
        mConfig = new HwConfigReader();
        mConfig.loadResConfig(context);
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
        } catch (AssertionFailedError e3) {
            pw.println("<I> isCount --fail " + e3);
        }
        try {
            dumpGetGroupSampleCycleNum();
            pw.println("<I> getGroupSampleCycleNum --pass");
        } catch (AssertionFailedError e4) {
            pw.println("<I> getGroupSampleCycleNum --fail " + e4);
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
        } catch (AssertionFailedError e5) {
            pw.println("<I> getResConfig --fail " + e5);
        }
        try {
            dumpGetSubTypeNum();
            pw.println("<I> getSubTypeNum --pass");
        } catch (AssertionFailedError e6) {
            pw.println("<I> getSubTypeNum --fail " + e6);
        }
        try {
            dumpGetSubTypeLevels();
            pw.println("<I> getSubTypeLevels --pass");
        } catch (AssertionFailedError e7) {
            pw.println("<I> getSubTypeLevels --fail " + e7);
        }
        try {
            dumpCountGroupID();
            pw.println("<I> getCountGroupID --pass");
        } catch (AssertionFailedError e8) {
            pw.println("<I> getCountGroupID --fail " + e8);
        }
        try {
            dumpGetSaveInterval();
            pw.println("<I> getSaveInterval --pass");
        } catch (AssertionFailedError e9) {
            pw.println("<I> getSaveInterval --fail " + e9);
        }
        try {
            dumpGetCountInterval();
            pw.println("<I> getCountInterval --pass");
        } catch (AssertionFailedError e10) {
            pw.println("<I> getCountInterval --fail " + e10);
        }
        try {
            dumpGetSampleBasePeriod();
            pw.println("<I> getSampleBasePeriod --pass");
        } catch (AssertionFailedError e11) {
            pw.println("<I> getSampleBasePeriod --fail " + e11);
        }
        try {
            dumpGetMaxKeepFiles();
            pw.println("<I> getMaxKeepFiles --pass");
        } catch (AssertionFailedError e12) {
            pw.println("<I> getMaxKeepFiles --fail " + e12);
        }
        try {
            dumpGetWhiteList();
            pw.println("<I> getWhiteList --pass");
        } catch (AssertionFailedError e13) {
            pw.println("<I> getWhiteList --fail " + e13);
        }
    }

    private static final void dumpGetGroupNum() {
        assertEquals(21, mConfig.getGroupNum());
    }

    private static final void dumpGetGroupName() {
        String[] expect = {"NOTIFICATION", "BROADCAST", "RECEIVER", "ALARM", "APPOPS", "PIDS", "CURSOR", "APPSERVICE", "APP", "CPU", "IO", "SCHEDGROUP", "ANR", "DELAY", "FRAMELOST"};
        int i = 0;
        while (true) {
            int[] iArr = groupIDs;
            if (i < iArr.length) {
                assertEquals(expect[i], mConfig.getGroupName(iArr[i]));
                i++;
            } else {
                return;
            }
        }
    }

    private static final void dumpIsCount() {
        boolean[] expect = {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
        int i = 0;
        while (true) {
            int[] iArr = groupIDs;
            if (i < iArr.length) {
                assertEquals(expect[i], mConfig.isCount(iArr[i]));
                i++;
            } else {
                return;
            }
        }
    }

    private static final void dumpGetGroupSampleCycleNum() {
        int[] expect = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
        int i = 0;
        while (true) {
            int[] iArr = groupIDs;
            if (i < iArr.length) {
                assertEquals(expect[i], mConfig.getGroupSampleCycleNum(iArr[i]));
                i++;
            } else {
                return;
            }
        }
    }

    /* JADX INFO: Multiple debug info for r3v6 android.rms.config.ResourceConfig[]: [D('i' int), D('expect1' android.rms.config.ResourceConfig[])] */
    /* JADX INFO: Multiple debug info for r7v9 android.rms.config.ResourceConfig[]: [D('i' int), D('expect2' android.rms.config.ResourceConfig[])] */
    /* JADX INFO: Multiple debug info for r8v11 android.rms.config.ResourceConfig[]: [D('i' int), D('expect3' android.rms.config.ResourceConfig[])] */
    /* JADX INFO: Multiple debug info for r9v13 android.rms.config.ResourceConfig[]: [D('i' int), D('expect4' android.rms.config.ResourceConfig[])] */
    /* JADX INFO: Multiple debug info for r10v12 android.rms.config.ResourceConfig[]: [D('i' int), D('expect6' android.rms.config.ResourceConfig[])] */
    /* JADX INFO: Multiple debug info for r11v13 android.rms.config.ResourceConfig[]: [D('i' int), D('expect7' android.rms.config.ResourceConfig[])] */
    private static final void dumpGetResConfig() {
        ResourceConfig[] expect = {new ResourceConfig(0, 20, 2, 0, 1000, "APP_THIRDPARTY", 30, 40, 50, 0), new ResourceConfig(1, 50, 2, 3, 1000, "APP_HW", 30, 40, 50, 0), new ResourceConfig(2, (int) MultiTaskManagerService.MSG_POLICY_BR, 2, 1, 1000, "APP_SYS", 30, 40, 50, 0), new ResourceConfig(3, 300, 2, 2, 1000, "APP_TOTAL", 30, 40, 50, 0)};
        for (int i = 0; i < 4; i++) {
            assertResConfigEquals("dump NOTIFICATION", expect[i], mConfig.getResConfig(groupIDs[0], i));
            assertSubTypeAttrEquals("dump NOTIFICATION", expect[i], groupIDs[0], i);
        }
        ResourceConfig[] expect1 = {new ResourceConfig(0, 30, 2, 3, 1000, "APP_THIRDPARTY", 30, 40, 50, 0), new ResourceConfig(1, 500, 2, 3, 1000, "APP_HW", 30, 40, 50, 0), new ResourceConfig(2, 800, 1, 5, 1000, "APP_SYS", 30, 40, 50, 0), new ResourceConfig(3, 1000, 1, 5, 1000, "APP_TOTAL", 30, 40, 50, 0)};
        for (int i2 = 0; i2 < 4; i2++) {
            assertResConfigEquals("dump BROADCAST", expect1[i2], mConfig.getResConfig(groupIDs[1], i2));
            assertSubTypeAttrEquals("dump BROADCAST", expect1[i2], groupIDs[1], i2);
        }
        ResourceConfig[] expect2 = {new ResourceConfig(0, (int) MultiTaskManagerService.MSG_POLICY_BR, 2, 0, 1000, "APP_THIRDPARTY", 30, 40, 50, 0), new ResourceConfig(1, 500, 2, 0, 1000, "APP_HW", 30, 40, 50, 0), new ResourceConfig(2, 1000, 1, 0, 0, "APP_SYS", 30, 40, 50, 0), new ResourceConfig(3, 3000, 2, 0, 0, "APP_TOTAL", 30, 40, 50, 0)};
        for (int i3 = 0; i3 < 4; i3++) {
            assertResConfigEquals("dump RECEIVER", expect2[i3], mConfig.getResConfig(groupIDs[2], i3));
            assertSubTypeAttrEquals("dump RECEIVER", expect2[i3], groupIDs[2], i3);
        }
        ResourceConfig[] expect3 = {new ResourceConfig(0, 50, 1, 0, 5000, "APP_THIRDPARTY", 30, 40, 50, 0), new ResourceConfig(1, 100, 1, 0, 5000, "APP_HW", 30, 40, 50, 0), new ResourceConfig(2, (int) MultiTaskManagerService.MSG_POLICY_BR, 1, 0, 5000, "APP_SYS", 30, 40, 50, 0), new ResourceConfig(3, 800, 1, 0, 5000, "APP_TOTAL", 30, 40, 50, 0)};
        for (int i4 = 0; i4 < 4; i4++) {
            assertResConfigEquals("dump ALARM", expect3[i4], mConfig.getResConfig(groupIDs[3], i4));
            assertSubTypeAttrEquals("dump ALARM", expect3[i4], groupIDs[3], i4);
        }
        ResourceConfig[] expect4 = {new ResourceConfig(0, (int) MultiTaskManagerService.MSG_POLICY_BR, 1, 0, 0, "APP_THIRDPARTY", -1, -1, -1, 0)};
        for (int i5 = 0; i5 < 1; i5++) {
            assertResConfigEquals("dump APPOPS", expect4[i5], mConfig.getResConfig(groupIDs[4], i5));
            assertSubTypeAttrEquals("dump APPOPS", expect4[i5], groupIDs[4], i5);
        }
        ResourceConfig[] expect6 = {new ResourceConfig(0, 300, 500, 0, 0, "APP_THIRDPARTY", 30, 40, 50, 0)};
        for (int i6 = 0; i6 < 1; i6++) {
            assertResConfigEquals("dump PIDS", expect6[i6], mConfig.getResConfig(groupIDs[6], i6));
            assertSubTypeAttrEquals("dump PIDS", expect6[i6], groupIDs[5], i6);
        }
        ResourceConfig[] expect7 = {new ResourceConfig(0, 50, 2, 0, (int) AwareAppAssociate.ASSOC_REPORT_MIN_TIME, "APP_THIRDPARTY", 30, 40, 50, 0), new ResourceConfig(1, 50, 2, 0, (int) AwareAppAssociate.ASSOC_REPORT_MIN_TIME, "APP_HW", 30, 40, 50, 0), new ResourceConfig(2, (int) MultiTaskManagerService.MSG_POLICY_BR, 1, 0, (int) AwareAppAssociate.ASSOC_REPORT_MIN_TIME, "APP_SYS", 30, 40, 50, 0), new ResourceConfig(3, 1000, 1, 0, (int) AwareAppAssociate.ASSOC_REPORT_MIN_TIME, "APP_TOTAL", 30, 40, 50, 0)};
        for (int i7 = 0; i7 < 4; i7++) {
            assertResConfigEquals("dump CURSOR", expect7[i7], mConfig.getResConfig(groupIDs[7], i7));
            assertSubTypeAttrEquals("dump CURSOR", expect7[i7], groupIDs[6], i7);
        }
        ResourceConfig[] expect8 = {new ResourceConfig(0, 1, 2, 10, 2000, "APP_THIRDPARTY", 30, 40, 50, 0), new ResourceConfig(1, 1, 2, 10, 2000, "APP_HW", 30, 40, 50, 0), new ResourceConfig(2, 1, 2, 10, 2000, "APP_SYS", 30, 40, 50, 0), new ResourceConfig(3, 1, 2, 10, 2000, "APP_TOTAL", 30, 40, 50, 0)};
        for (int i8 = 0; i8 < 4; i8++) {
            assertResConfigEquals("dump APPSERVICE", expect8[i8], mConfig.getResConfig(groupIDs[8], i8));
            assertSubTypeAttrEquals("dump APPSERVICE", expect8[i8], groupIDs[7], i8);
        }
        ResourceConfig[] expect9 = {new ResourceConfig(0, 1, 2, 10, 2000, "APP_THIRDPARTY", 30, 40, 50, 0), new ResourceConfig(1, 1, 2, 10, 2000, "APP_HW", 30, 40, 50, 0), new ResourceConfig(2, 1, 2, 10, 2000, "APP_SYS", 30, 40, 50, 0), new ResourceConfig(3, 1, 2, 10, 2000, "APP_TOTAL", 30, 40, 50, 0)};
        for (int i9 = 0; i9 < 4; i9++) {
            assertResConfigEquals("dump APP", expect9[i9], mConfig.getResConfig(groupIDs[9], i9));
            assertSubTypeAttrEquals("dump APP", expect9[i9], groupIDs[8], i9);
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
        int[] expect = {4, 4, 4, 4, 4, 1, 4, 4, 4, 0, 0, 0, 0, 0, 0};
        for (int i = 0; i < expect.length; i++) {
            assertEquals(expect[i], mConfig.getSubTypeNum(groupIDs[i]));
        }
    }

    private static final void dumpGetSubTypeLevels() {
        for (int i = 0; i < 4; i++) {
            assertEquals("dump NOTIFICATION", 0, mConfig.getSubTypeLevels(groupIDs[0], i).size());
        }
        for (int i2 = 0; i2 < 4; i2++) {
            assertEquals("dump BROADCAST", 0, mConfig.getSubTypeLevels(groupIDs[1], i2).size());
        }
        for (int i3 = 0; i3 < 4; i3++) {
            assertEquals("dump RECEIVER", 0, mConfig.getSubTypeLevels(groupIDs[2], i3).size());
        }
        for (int i4 = 0; i4 < 4; i4++) {
            assertEquals("dump ALARM", 0, mConfig.getSubTypeLevels(groupIDs[3], i4).size());
        }
        for (int i5 = 0; i5 < 1; i5++) {
            assertEquals("dump APPOPS", 0, mConfig.getSubTypeLevels(groupIDs[4], i5).size());
        }
        for (int i6 = 0; i6 < 1; i6++) {
            assertEquals("dump PIDS", 0, mConfig.getSubTypeLevels(groupIDs[5], i6).size());
        }
        for (int i7 = 0; i7 < 4; i7++) {
            assertEquals("dump CURSOR", 0, mConfig.getSubTypeLevels(groupIDs[6], i7).size());
        }
        for (int i8 = 0; i8 < 4; i8++) {
            assertEquals("dump APPSERVICE", 0, mConfig.getSubTypeLevels(groupIDs[7], i8).size());
        }
        for (int i9 = 0; i9 < 4; i9++) {
            assertEquals("dump APPSERVICE", 0, mConfig.getSubTypeLevels(groupIDs[8], i9).size());
        }
        assertEquals("dump CPU", null, mConfig.getSubTypeLevels(groupIDs[9], 0));
        assertEquals("dump IO", null, mConfig.getSubTypeLevels(groupIDs[10], 0));
        assertEquals("dump SCHEDGROUP", null, mConfig.getSubTypeLevels(groupIDs[11], 0));
        assertEquals("dump ANR", null, mConfig.getSubTypeLevels(groupIDs[12], 0));
        assertEquals("dump DELAY", null, mConfig.getSubTypeLevels(groupIDs[13], 0));
        assertEquals("dump FRAMELOST", null, mConfig.getSubTypeLevels(groupIDs[14], 0));
    }

    private static final void dumpCountGroupID() {
        int[] group = {26, 27, 28};
        assertEquals("group num ", 3, mConfig.getCountGroupID().size());
        for (int i = 0; i < 3; i++) {
            assertEquals(group[i], mConfig.getCountGroupID().get(i).intValue());
        }
    }

    private static final void dumpGetSaveInterval() {
        assertEquals(30, mConfig.getSaveInterval());
    }

    private static final void dumpGetCountInterval() {
        assertEquals(10080, mConfig.getCountInterval(false));
        assertEquals(1440, mConfig.getCountInterval(true));
    }

    private static final void dumpGetSampleBasePeriod() {
        assertEquals(AwareAppAssociate.ASSOC_REPORT_MIN_TIME, mConfig.getSampleBasePeriod());
    }

    private static final void dumpGetMaxKeepFiles() {
        assertEquals(10, mConfig.getMaxKeepFiles());
    }

    private static final void dumpGetWhiteList() {
        String[] expect = {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null};
        for (int i = 0; i < groupIDs.length; i++) {
            assertEquals("case " + i, expect[i], mConfig.getWhiteList(groupIDs[i]));
        }
    }
}
