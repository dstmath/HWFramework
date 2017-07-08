package com.android.server.rms.test;

import android.content.Context;
import android.util.Log;
import com.android.server.rms.io.IOStatsService;
import com.android.server.rms.utils.Utils;

public class TestIOResourceService {
    private static final String SPLIT = ",";
    private static final String TAG = "IO.TestIOResourceService";
    private static IOStatsService mIOStatsService;
    private static TestIOResourceService mTestIOResourceService;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.test.TestIOResourceService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.test.TestIOResourceService.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.test.TestIOResourceService.<clinit>():void");
    }

    private TestIOResourceService() {
    }

    public static synchronized TestIOResourceService getInstance(Context context) {
        TestIOResourceService testIOResourceService;
        synchronized (TestIOResourceService.class) {
            if (mTestIOResourceService == null) {
                mTestIOResourceService = new TestIOResourceService();
            }
            if (mIOStatsService == null) {
                mIOStatsService = IOStatsService.getInstance(null, null);
            }
            testIOResourceService = mTestIOResourceService;
        }
        return testIOResourceService;
    }

    public void testPeriodMonitorTask() {
        if (Utils.DEBUG) {
            Log.d(TAG, "testPeriodMonitorTask");
        }
        try {
            mIOStatsService.periodMonitorTask();
        } catch (Exception ex) {
            Log.e(TAG, "testPeriodMonitorTask,fail due to Exception");
            if (Utils.DEBUG) {
                ex.printStackTrace();
            }
        }
    }

    public void testPeriodReadTask() {
        if (Utils.DEBUG) {
            Log.d(TAG, "testPeriodReadTask");
        }
        try {
            mIOStatsService.periodReadTask();
        } catch (Exception ex) {
            Log.e(TAG, "testReadStatsFromKernel,fail due to Exception");
            if (Utils.DEBUG) {
                ex.printStackTrace();
            }
        }
    }

    public void testRefreshAddUidMonitored(String uidPkg) {
        try {
            String[] splitArray = uidPkg.split(SPLIT);
            mIOStatsService.refreshMonitoredUids(false, Integer.parseInt(splitArray[0]), splitArray[1]);
        } catch (Exception ex) {
            Log.e(TAG, "testRefreshAddUidMonitored,uidPkg parameter is invalid");
            if (Utils.DEBUG) {
                ex.printStackTrace();
            }
        }
    }

    public void testRefreshRemoveUidMonitored(String uidPkg) {
        try {
            mIOStatsService.refreshMonitoredUids(true, Integer.parseInt(uidPkg.split(SPLIT)[0]), null);
        } catch (Exception ex) {
            Log.e(TAG, "testRefreshRemoveUidMonitored,uidPkg parameter is invalid");
            if (Utils.DEBUG) {
                ex.printStackTrace();
            }
        }
    }

    public void testShutdown() {
        if (Utils.DEBUG) {
            Log.d(TAG, "testShutdown");
        }
        try {
            mIOStatsService.saveIOStatsAndLatestUids(true);
        } catch (Exception ex) {
            Log.e(TAG, "saveIOStatsAndLatestUids,fail due to Exception");
            if (Utils.DEBUG) {
                ex.printStackTrace();
            }
        }
    }
}
