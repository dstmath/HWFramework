package com.android.server.rms.test;

import android.content.Context;
import android.util.Pair;
import com.android.server.rms.statistic.HwResRecord;
import com.android.server.rms.statistic.HwResRecord.Aspect;
import java.io.PrintWriter;
import java.util.ArrayList;
import junit.framework.Assert;
import junit.framework.AssertionFailedError;

public final class TestHwResRecord extends Assert {
    public static final String TAG = "TestHwResRecord";

    private static final void testHandleAspectData() {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.test.TestHwResRecord.testHandleAspectData():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.test.TestHwResRecord.testHandleAspectData():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.test.TestHwResRecord.testHandleAspectData():void");
    }

    public static final void testResRecord(PrintWriter pw, Context context) {
        try {
            testHwResRecord();
            pw.println("<I> testHwResRecord --pass");
        } catch (AssertionFailedError e) {
            pw.println("<I> testHwResRecord --fail " + e);
        }
        try {
            testGetGroupName();
            pw.println("<I> getGroupName --pass");
        } catch (AssertionFailedError e2) {
            pw.println("<I> getGroupName --fail " + e2);
        }
        try {
            testGetSubTypeName();
            pw.println("<I> getSubTypeName --pass");
        } catch (AssertionFailedError e22) {
            pw.println("<I> getSubTypeName --fail " + e22);
        }
        try {
            testGetLevel();
            pw.println("<I> getLevel --pass");
        } catch (AssertionFailedError e222) {
            pw.println("<I> getLevel --fail " + e222);
        }
        try {
            testHandleAspectData();
            pw.println("<I> getAspectData --pass");
            pw.println("<I> updateAspectData --pass");
            pw.println("<I> resetAspectData --pass");
        } catch (AssertionFailedError e2222) {
            pw.println("<I> handleAspectData --fail " + e2222);
        }
    }

    private static final void testHwResRecord() {
        assertNotNull("case 1", new HwResRecord("testGroup", "testSubType", 1));
        assertNotNull("case 2", new HwResRecord(null, null, -1));
    }

    private static final void testGetGroupName() {
        assertEquals("case 1", "testGroup", new HwResRecord("testGroup", "testSubType", 1).getGroupName());
        assertEquals("case 2", null, new HwResRecord(null, null, -1).getGroupName());
    }

    private static final void testGetSubTypeName() {
        assertEquals("case 1", "testSubType", new HwResRecord("testGroup", "testSubType", 1).getSubTypeName());
        assertEquals("case 2", null, new HwResRecord(null, null, -1).getSubTypeName());
    }

    private static final void testGetLevel() {
        assertEquals("case 1", 1, new HwResRecord("testGroup", "testSubType", 1).getLevel());
        assertEquals("case 2", -1, new HwResRecord(null, null, -1).getLevel());
    }

    private static final void assertAspectDataEquals(String msg, ArrayList<Pair<String, Integer>> expect, ArrayList<Aspect> actual) {
        for (int i = 0; i < expect.size(); i++) {
            assertEquals(msg, (String) ((Pair) expect.get(i)).first, ((Aspect) actual.get(i)).name);
            assertEquals(msg, ((Integer) ((Pair) expect.get(i)).second).intValue(), ((Aspect) actual.get(i)).value);
        }
    }
}
