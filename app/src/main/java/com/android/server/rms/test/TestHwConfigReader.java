package com.android.server.rms.test;

import android.content.Context;
import android.rms.config.ResourceConfig;
import com.android.server.HwNetworkPropertyChecker;
import com.android.server.am.HwActivityManagerService;
import com.android.server.display.HwEyeProtectionDividedTimeControl;
import com.android.server.rms.config.HwConfigReader;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.security.trustcircle.IOTController;
import com.android.server.wifipro.WifiProCommonDefs;
import com.android.server.wifipro.WifiProCommonUtils;
import huawei.com.android.server.policy.HwGlobalActionsView;
import java.io.File;
import java.io.PrintWriter;
import junit.framework.Assert;
import junit.framework.AssertionFailedError;

public final class TestHwConfigReader extends Assert {
    private static int[] groupIDs;
    private static HwConfigReader mConfig;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.test.TestHwConfigReader.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.test.TestHwConfigReader.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.test.TestHwConfigReader.<clinit>():void");
    }

    public static final void testConfigReader(PrintWriter pw, Context context) {
        mConfig = new HwConfigReader();
        mConfig.loadResConfig();
        try {
            testGetGroupNum();
            pw.println("<I> getGroupNum --pass");
        } catch (AssertionFailedError e) {
            pw.println("<I> getGroupNum --fail " + e);
        }
        try {
            testGetGroupName();
            pw.println("<I> getGroupName --pass");
        } catch (AssertionFailedError e2) {
            pw.println("<I> getGroupName --fail " + e2);
        }
        try {
            testIsCount();
            pw.println("<I> isCount --pass");
        } catch (AssertionFailedError e22) {
            pw.println("<I> isCount --fail " + e22);
        }
        try {
            testGetGroupSampleCycleNum();
            pw.println("<I> getGroupSampleCycleNum --pass");
        } catch (AssertionFailedError e222) {
            pw.println("<I> getGroupSampleCycleNum --fail " + e222);
        }
        try {
            testGetResConfig();
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
            testGetSubTypeNum();
            pw.println("<I> getSubTypeNum --pass");
        } catch (AssertionFailedError e22222) {
            pw.println("<I> getSubTypeNum --fail " + e22222);
        }
        try {
            testGetSubTypeLevels();
            pw.println("<I> getSubTypeLevels --pass");
        } catch (AssertionFailedError e222222) {
            pw.println("<I> getSubTypeLevels --fail " + e222222);
        }
        try {
            testCountGroupID();
            pw.println("<I> getCountGroupID --pass");
        } catch (AssertionFailedError e2222222) {
            pw.println("<I> getCountGroupID --fail " + e2222222);
        }
        try {
            testGetSaveInterval();
            pw.println("<I> getSaveInterval --pass");
        } catch (AssertionFailedError e22222222) {
            pw.println("<I> getSaveInterval --fail " + e22222222);
        }
        try {
            testGetCountInterval();
            pw.println("<I> getCountInterval --pass");
        } catch (AssertionFailedError e222222222) {
            pw.println("<I> getCountInterval --fail " + e222222222);
        }
        try {
            testGetSampleBasePeriod();
            pw.println("<I> getSampleBasePeriod --pass");
        } catch (AssertionFailedError e2222222222) {
            pw.println("<I> getSampleBasePeriod --fail " + e2222222222);
        }
        try {
            testGetMaxKeepFiles();
            pw.println("<I> getMaxKeepFiles --pass");
        } catch (AssertionFailedError e22222222222) {
            pw.println("<I> getMaxKeepFiles --fail " + e22222222222);
        }
        try {
            testGetWhiteList();
            pw.println("<I> getWhiteList --pass");
        } catch (AssertionFailedError e222222222222) {
            pw.println("<I> getWhiteList --fail " + e222222222222);
        }
    }

    private static final void testGetGroupNum() {
        assertEquals(19, mConfig.getGroupNum());
    }

    private static final void testGetGroupName() {
        String[] expect = new String[]{"NOTIFICATION", "BROADCAST", "RECEIVER", "ALARM", "APPOPS", "PROVIDER", "PIDS", "CURSOR", "APPSERVICE", "APP", "MEMORY", "CPU", "IO", "SCHEDGROUP", "ANR", "DELAY", "FRAMELOST", "BUDDYINFO", "MAINSERVICES"};
        for (int i = 0; i < groupIDs.length; i++) {
            assertEquals(expect[i], mConfig.getGroupName(groupIDs[i]));
        }
    }

    private static final void testIsCount() {
        boolean[] expect = new boolean[]{false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, false, true, true};
        for (int i = 0; i < groupIDs.length; i++) {
            assertEquals(expect[i], mConfig.isCount(groupIDs[i]));
        }
    }

    private static final void testGetGroupSampleCycleNum() {
        int[] expect = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 10, -1, -1, -1, -1, -1, -1, -1, -1};
        for (int i = 0; i < groupIDs.length; i++) {
            assertEquals(expect[i], mConfig.getGroupSampleCycleNum(groupIDs[i]));
        }
    }

    private static final void testGetResConfig() {
        int i;
        ResourceConfig[] expect = new ResourceConfig[]{new ResourceConfig(0, 20, 2, 0, IOTController.TYPE_MASTER, "APP_THIRDPARTY", -1, -1, -1), new ResourceConfig(1, 50, 2, 3, IOTController.TYPE_MASTER, "APP_HW", -1, -1, -1), new ResourceConfig(2, WifiProCommonUtils.HTTP_REACHALBE_HOME, 2, 1, IOTController.TYPE_MASTER, "SYS", -1, -1, -1), new ResourceConfig(3, HwGlobalActionsView.VIBRATE_DELAY, 2, 2, IOTController.TYPE_MASTER, "TOTAL", -1, -1, -1)};
        for (i = 0; i < 4; i++) {
            assertResConfigEquals("test NOTIFICATION", expect[i], mConfig.getResConfig(groupIDs[0], i));
            assertSubTypeAttrEquals("test NOTIFICATION", expect[i], groupIDs[0], i);
        }
        ResourceConfig[] expect1 = new ResourceConfig[]{new ResourceConfig(0, 30, 2, 3, IOTController.TYPE_MASTER, "APP_THIRDPARTY", -1, -1, -1), new ResourceConfig(1, HwActivityManagerService.SERVICE_ADJ, 2, 3, IOTController.TYPE_MASTER, "APP_HW", -1, -1, -1), new ResourceConfig(2, HwActivityManagerService.SERVICE_B_ADJ, 1, 5, IOTController.TYPE_MASTER, "SYS", -1, -1, -1), new ResourceConfig(3, IOTController.TYPE_MASTER, 1, 5, IOTController.TYPE_MASTER, "TOTAL", -1, -1, -1)};
        for (i = 0; i < 4; i++) {
            assertResConfigEquals("test BROADCAST", expect1[i], mConfig.getResConfig(groupIDs[1], i));
            assertSubTypeAttrEquals("test BROADCAST", expect1[i], groupIDs[1], i);
        }
        ResourceConfig[] expect2 = new ResourceConfig[]{new ResourceConfig(0, HwActivityManagerService.SERVICE_ADJ, 2, 0, IOTController.TYPE_MASTER, "APP_THIRDPARTY", -1, -1, -1), new ResourceConfig(1, HwActivityManagerService.SERVICE_ADJ, 2, 0, IOTController.TYPE_MASTER, "APP_HW", -1, -1, -1), new ResourceConfig(2, IOTController.TYPE_MASTER, 1, 0, 0, "SYS", -1, -1, -1), new ResourceConfig(3, 3000, 2, 0, 0, "TOTAL", -1, -1, -1)};
        for (i = 0; i < 4; i++) {
            assertResConfigEquals("test RECEIVER", expect2[i], mConfig.getResConfig(groupIDs[2], i));
            assertSubTypeAttrEquals("test RECEIVER", expect2[i], groupIDs[2], i);
        }
        ResourceConfig[] expect3 = new ResourceConfig[]{new ResourceConfig(0, 50, 2, 0, 5000, "APP_THIRDPARTY", -1, -1, -1), new ResourceConfig(1, 100, 2, 0, 5000, "APP_HW", -1, -1, -1), new ResourceConfig(2, WifiProCommonUtils.HTTP_REACHALBE_HOME, 1, 0, 5000, "SYS", -1, -1, -1), new ResourceConfig(3, HwActivityManagerService.SERVICE_B_ADJ, 1, 0, 5000, "TOTAL", -1, -1, -1)};
        for (i = 0; i < 4; i++) {
            assertResConfigEquals("test ALARM", expect3[i], mConfig.getResConfig(groupIDs[3], i));
            assertSubTypeAttrEquals("test ALARM", expect3[i], groupIDs[3], i);
        }
        ResourceConfig[] expect4 = new ResourceConfig[]{new ResourceConfig(0, WifiProCommonUtils.HTTP_REACHALBE_HOME, 2, 0, 0, "APP_THIRDPARTY", -1, -1, -1)};
        for (i = 0; i < 1; i++) {
            assertResConfigEquals("test APPOPS", expect4[i], mConfig.getResConfig(groupIDs[4], i));
            assertSubTypeAttrEquals("test APPOPS", expect4[i], groupIDs[4], i);
        }
        ResourceConfig[] expect5 = new ResourceConfig[]{new ResourceConfig(0, 100, 2, 0, 0, "APP_THIRDPARTY", -1, -1, -1)};
        for (i = 0; i < 1; i++) {
            assertResConfigEquals("test PROVIDER", expect5[i], mConfig.getResConfig(groupIDs[5], i));
            assertSubTypeAttrEquals("test PROVIDER", expect5[i], groupIDs[5], i);
        }
        ResourceConfig[] expect6 = new ResourceConfig[]{new ResourceConfig(0, HwGlobalActionsView.VIBRATE_DELAY, HwActivityManagerService.SERVICE_ADJ, 0, 0, "APP_THIRDPARTY", -1, -1, -1)};
        for (i = 0; i < 1; i++) {
            assertResConfigEquals("test PIDS", expect6[i], mConfig.getResConfig(groupIDs[6], i));
            assertSubTypeAttrEquals("test PIDS", expect6[i], groupIDs[6], i);
        }
        ResourceConfig[] expect7 = new ResourceConfig[]{new ResourceConfig(0, 50, 2, 0, AwareAppAssociate.ASSOC_REPORT_MIN_TIME, "APP_THIRDPARTY", -1, -1, -1), new ResourceConfig(1, 50, 2, 0, AwareAppAssociate.ASSOC_REPORT_MIN_TIME, "APP_HW", -1, -1, -1), new ResourceConfig(2, WifiProCommonUtils.HTTP_REACHALBE_HOME, 1, 0, AwareAppAssociate.ASSOC_REPORT_MIN_TIME, "APP_SYS", -1, -1, -1), new ResourceConfig(3, IOTController.TYPE_MASTER, 1, 0, AwareAppAssociate.ASSOC_REPORT_MIN_TIME, "APP_TOTAL", -1, -1, -1)};
        for (i = 0; i < 4; i++) {
            assertResConfigEquals("test CURSOR", expect7[i], mConfig.getResConfig(groupIDs[7], i));
            assertSubTypeAttrEquals("test CURSOR", expect7[i], groupIDs[7], i);
        }
        ResourceConfig[] expect8 = new ResourceConfig[]{new ResourceConfig(0, 1, 2, 10, HwNetworkPropertyChecker.HW_DEFAULT_REEVALUATE_DELAY_MS, "APP_THIRDPARTY", -1, -1, -1), new ResourceConfig(1, 1, 2, 10, HwNetworkPropertyChecker.HW_DEFAULT_REEVALUATE_DELAY_MS, "APP_HW", -1, -1, -1), new ResourceConfig(2, 1, 2, 10, HwNetworkPropertyChecker.HW_DEFAULT_REEVALUATE_DELAY_MS, "APP_SYS", -1, -1, -1), new ResourceConfig(3, 1, 2, 10, HwNetworkPropertyChecker.HW_DEFAULT_REEVALUATE_DELAY_MS, "APP_TOTAL", -1, -1, -1)};
        for (i = 0; i < 4; i++) {
            assertResConfigEquals("test APPSERVICE", expect8[i], mConfig.getResConfig(groupIDs[8], i));
            assertSubTypeAttrEquals("test APPSERVICE", expect8[i], groupIDs[8], i);
        }
        ResourceConfig[] expect9 = new ResourceConfig[]{new ResourceConfig(0, 1, 2, 10, HwNetworkPropertyChecker.HW_DEFAULT_REEVALUATE_DELAY_MS, "APP_THIRDPARTY", -1, -1, -1), new ResourceConfig(1, 1, 2, 10, HwNetworkPropertyChecker.HW_DEFAULT_REEVALUATE_DELAY_MS, "APP_HW", -1, -1, -1), new ResourceConfig(2, 1, 2, 10, HwNetworkPropertyChecker.HW_DEFAULT_REEVALUATE_DELAY_MS, "APP_SYS", -1, -1, -1), new ResourceConfig(3, 1, 2, 10, HwNetworkPropertyChecker.HW_DEFAULT_REEVALUATE_DELAY_MS, "APP_TOTAL", -1, -1, -1)};
        for (i = 0; i < 4; i++) {
            assertResConfigEquals("test APP", expect9[i], mConfig.getResConfig(groupIDs[9], i));
            assertSubTypeAttrEquals("test APP", expect9[i], groupIDs[9], i);
        }
        ResourceConfig[] expect10 = new ResourceConfig[]{new ResourceConfig(0, -1, -1, -1, -1, "Total", -1, -1, -1), new ResourceConfig(1, -1, -1, -1, -1, "MemFree", 10, 6, 4), new ResourceConfig(2, -1, -1, -1, -1, "Buffers", -1, -1, -1), new ResourceConfig(3, -1, -1, -1, -1, "Cached", 30, 35, 40), new ResourceConfig(4, -1, -1, -1, -1, "SwapTotal", -1, -1, -1), new ResourceConfig(5, -1, -1, -1, -1, "SwapFree", 50, 40, 25), new ResourceConfig(6, -1, -1, -1, -1, "Slab", -1, -1, -1), new ResourceConfig(7, -1, -1, -1, -1, "SUnreclaim", -1, -1, -1)};
        for (i = 0; i < 7; i++) {
            assertResConfigEquals("test MEMORY", expect10[i], mConfig.getResConfig(groupIDs[10], i));
            assertSubTypeAttrEquals("test MEMORY", expect10[i], groupIDs[10], i);
        }
        assertEquals("test CPU", null, mConfig.getResConfig(groupIDs[11], 0));
        assertEquals("test IO", null, mConfig.getResConfig(groupIDs[12], 0));
        assertEquals("test SCHEDGROUP", null, mConfig.getResConfig(groupIDs[13], 0));
        assertEquals("test ANR", null, mConfig.getResConfig(groupIDs[14], 0));
        assertEquals("test DELAY", null, mConfig.getResConfig(groupIDs[15], 0));
        assertEquals("test FRAMELOST", null, mConfig.getResConfig(groupIDs[16], 0));
        ResourceConfig[] expect18 = new ResourceConfig[]{new ResourceConfig(0, -1, -1, -1, -1, "order0", 7, 6, 3), new ResourceConfig(1, -1, -1, -1, -1, "order1", -1, -1, -1), new ResourceConfig(2, -1, -1, -1, -1, "order2", -1, -1, -1), new ResourceConfig(3, -1, -1, -1, -1, "order3", -1, -1, -1), new ResourceConfig(4, -1, -1, -1, -1, "order4", -1, -1, -1), new ResourceConfig(5, -1, -1, -1, -1, "order5", -1, -1, -1), new ResourceConfig(6, -1, -1, -1, -1, "order6", -1, -1, -1), new ResourceConfig(7, -1, -1, -1, -1, "order7", -1, -1, -1), new ResourceConfig(8, -1, -1, -1, -1, "order8", -1, -1, -1), new ResourceConfig(9, -1, -1, -1, -1, "order9", -1, -1, -1), new ResourceConfig(10, -1, -1, -1, -1, "order10", -1, -1, -1)};
        for (i = 0; i < 11; i++) {
            assertResConfigEquals("test BUDDYINFO", expect18[i], mConfig.getResConfig(groupIDs[17], i));
            assertSubTypeAttrEquals("test BUDDYINFO", expect18[i], groupIDs[17], i);
        }
        ResourceConfig[] expect19;
        if (new File("/system/bin", "HwCamCfgSvr").exists()) {
            expect19 = new ResourceConfig[]{new ResourceConfig(0, -1, -1, -1, -1, "/system/bin/mediaserver", 7, 10, 15), new ResourceConfig(1, -1, -1, -1, -1, "system_server", 80, 100, 150), new ResourceConfig(2, -1, -1, -1, -1, "/system/bin/HwCamCfgSvr", 100, 150, WifiProCommonUtils.HTTP_REACHALBE_HOME), new ResourceConfig(3, -1, -1, -1, -1, "com.huawei.camera", 100, 150, WifiProCommonUtils.HTTP_REACHALBE_HOME), new ResourceConfig(4, -1, -1, -1, -1, "com.android.keyguard", 80, 100, 150), new ResourceConfig(5, -1, -1, -1, -1, "com.android.systemui", 80, 100, 150), new ResourceConfig(6, -1, -1, -1, -1, "com.huawei.android.launcher", 80, 100, 150)};
            for (i = 0; i < 7; i++) {
                assertResConfigEquals("test MAINSERVICES", expect19[i], mConfig.getResConfig(groupIDs[18], i));
                assertSubTypeAttrEquals("test MAINSERVICES", expect19[i], groupIDs[18], i);
            }
            return;
        }
        expect19 = new ResourceConfig[]{new ResourceConfig(0, -1, -1, -1, -1, "/system/bin/mediaserver", 7, 10, 15), new ResourceConfig(1, -1, -1, -1, -1, "system_server", 80, 100, 150), new ResourceConfig(2, -1, -1, -1, -1, "/vendor/bin/HwCamCfgSvr", 100, 150, WifiProCommonUtils.HTTP_REACHALBE_HOME), new ResourceConfig(3, -1, -1, -1, -1, "com.huawei.camera", 100, 150, WifiProCommonUtils.HTTP_REACHALBE_HOME), new ResourceConfig(4, -1, -1, -1, -1, "com.android.keyguard", 80, 100, 150), new ResourceConfig(5, -1, -1, -1, -1, "com.android.systemui", 80, 100, 150), new ResourceConfig(6, -1, -1, -1, -1, "com.huawei.android.launcher", 80, 100, 150)};
        for (i = 0; i < 7; i++) {
            assertResConfigEquals("test MAINSERVICES", expect19[i], mConfig.getResConfig(groupIDs[18], i));
            assertSubTypeAttrEquals("test MAINSERVICES", expect19[i], groupIDs[18], i);
        }
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
        assertEquals("test getSubTypeName " + info, expect.getResouceName(), mConfig.getSubTypeName(groupID, subType));
        assertEquals("test getResourceThreshold " + info, expect.getResourceThreshold(), mConfig.getResourceThreshold(groupID, subType));
        assertEquals("test getResourceStrategy " + info, expect.getResourceStrategy(), mConfig.getResourceStrategy(groupID, subType));
        assertEquals("test getResourceMaxPeroid " + info, expect.getResourceMaxPeroid(), mConfig.getResourceMaxPeroid(groupID, subType));
        assertEquals("test getLoopInterval " + info, expect.getLoopInterval(), mConfig.getLoopInterval(groupID, subType));
        assertEquals("test getNormalThreshold " + info, expect.getResouceNormalThreshold(), mConfig.getNormalThreshold(groupID, subType));
        assertEquals("test getWarningThreshold " + info, expect.getResouceWarningThreshold(), mConfig.getWarningThreshold(groupID, subType));
        assertEquals("test getUrgentThreshold " + info, expect.getResouceUrgentThreshold(), mConfig.getUrgentThreshold(groupID, subType));
    }

    private static final void testGetSubTypeNum() {
        int[] expect = new int[]{4, 4, 4, 4, 1, 1, 1, 4, 4, 4, 8, 0, 0, 0, 0, 0, 0, 11, 7};
        for (int i = 0; i < expect.length; i++) {
            assertEquals(expect[i], mConfig.getSubTypeNum(groupIDs[i]));
        }
    }

    private static final void testGetSubTypeLevels() {
        int i;
        for (i = 0; i < 4; i++) {
            assertEquals("test NOTIFICATION", 0, mConfig.getSubTypeLevels(groupIDs[0], i).size());
        }
        for (i = 0; i < 4; i++) {
            assertEquals("test BROADCAST", 0, mConfig.getSubTypeLevels(groupIDs[1], i).size());
        }
        for (i = 0; i < 4; i++) {
            assertEquals("test RECEIVER", 0, mConfig.getSubTypeLevels(groupIDs[2], i).size());
        }
        for (i = 0; i < 4; i++) {
            assertEquals("test ALARM", 0, mConfig.getSubTypeLevels(groupIDs[3], i).size());
        }
        for (i = 0; i < 1; i++) {
            assertEquals("test APPOPS", 0, mConfig.getSubTypeLevels(groupIDs[4], i).size());
        }
        for (i = 0; i < 1; i++) {
            assertEquals("test PROVIDER", 0, mConfig.getSubTypeLevels(groupIDs[5], i).size());
        }
        for (i = 0; i < 1; i++) {
            assertEquals("test PIDS", 0, mConfig.getSubTypeLevels(groupIDs[6], i).size());
        }
        for (i = 0; i < 4; i++) {
            assertEquals("test CURSOR", 0, mConfig.getSubTypeLevels(groupIDs[7], i).size());
        }
        for (i = 0; i < 4; i++) {
            assertEquals("test APPSERVICE", 0, mConfig.getSubTypeLevels(groupIDs[8], i).size());
        }
        for (i = 0; i < 4; i++) {
            assertEquals("test APPSERVICE", 0, mConfig.getSubTypeLevels(groupIDs[9], i).size());
        }
        int[] expcet = new int[]{4, 15, 15, 15, 15, 15, 15};
        for (i = 0; i < 7; i++) {
            assertEquals("test MEMORY " + i, expcet[i], mConfig.getSubTypeLevels(groupIDs[10], i).size());
        }
        assertEquals("test CPU", null, mConfig.getSubTypeLevels(groupIDs[11], 0));
        assertEquals("test IO", null, mConfig.getSubTypeLevels(groupIDs[12], 0));
        assertEquals("test SCHEDGROUP", null, mConfig.getSubTypeLevels(groupIDs[13], 0));
        assertEquals("test ANR", null, mConfig.getSubTypeLevels(groupIDs[14], 0));
        assertEquals("test DELAY", null, mConfig.getSubTypeLevels(groupIDs[15], 0));
        assertEquals("test FRAMELOST", null, mConfig.getSubTypeLevels(groupIDs[16], 0));
        int[] expcet1 = new int[]{13, 13, 13, 13, 13, 13, 12, 12, 12, 12, 10};
        for (i = 0; i < 11; i++) {
            assertEquals("test BUDDYINFO " + i, expcet1[i], mConfig.getSubTypeLevels(groupIDs[17], i).size());
        }
        int[] expcet2 = new int[]{14, 14, 14, 14, 14, 14, 14};
        for (i = 0; i < 7; i++) {
            assertEquals("test MAINSERVICES " + i, expcet2[i], mConfig.getSubTypeLevels(groupIDs[18], i).size());
        }
    }

    private static final void testCountGroupID() {
        int[] group = new int[]{20, 100, WifiProCommonDefs.TYEP_HAS_INTERNET};
        assertEquals("group num ", 3, mConfig.getCountGroupID().size());
        for (int i = 0; i < 3; i++) {
            assertEquals(group[i], ((Integer) mConfig.getCountGroupID().get(i)).intValue());
        }
    }

    private static final void testGetSaveInterval() {
        assertEquals(30, mConfig.getSaveInterval());
    }

    private static final void testGetCountInterval() {
        assertEquals(10080, mConfig.getCountInterval(false));
        assertEquals(HwEyeProtectionDividedTimeControl.DAY_IN_MINUTE, mConfig.getCountInterval(true));
    }

    private static final void testGetSampleBasePeriod() {
        assertEquals(AwareAppAssociate.ASSOC_REPORT_MIN_TIME, mConfig.getSampleBasePeriod());
    }

    private static final void testGetMaxKeepFiles() {
        assertEquals(10, mConfig.getMaxKeepFiles());
    }

    private static final void testGetWhiteList() {
        String[] expect = new String[]{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null};
        for (int i = 0; i < groupIDs.length; i++) {
            assertEquals("case " + i, expect[i], mConfig.getWhiteList(groupIDs[i]));
        }
    }
}
