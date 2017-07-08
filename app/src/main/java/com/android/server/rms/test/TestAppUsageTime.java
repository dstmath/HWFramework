package com.android.server.rms.test;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import com.android.server.rms.record.AppUsageTime;
import com.android.server.rms.utils.Utils;

public class TestAppUsageTime {
    private static final String TAG = "RMS.TestAppUsageTime";
    private static final AppUsageTime mTest = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.test.TestAppUsageTime.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.test.TestAppUsageTime.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.test.TestAppUsageTime.<clinit>():void");
    }

    private static final boolean checkInputArgs(Context context, String[] args) {
        if (args.length != 2 || args[1] == null) {
            Log.e(TAG, "please input correct package name for AppUsageTime test!");
            return false;
        }
        try {
            if (context.getPackageManager().getApplicationInfo(args[1], 0) != null) {
                return true;
            }
            Log.e(TAG, "There is not such an application, please check the package name carefully!!!");
            return false;
        } catch (NameNotFoundException e) {
            Log.e(TAG, "getApplicationInfo failed. Please check the package name carefully!!!");
            return false;
        }
    }

    public static final void testGetUsageTime(Context context, String[] args) {
        if (checkInputArgs(context, args)) {
            String pkg = args[1];
            long time = mTest.getRealUsageTimeLocked(pkg) / 1000;
            if (Utils.HWFLOW) {
                Log.i(TAG, "pkg " + pkg + ", usageTime is " + time + "s");
            }
        }
    }

    public static final void testIsHistoryInstalledApp(Context context, String[] args) {
        if (checkInputArgs(context, args)) {
            String pkg = args[1];
            if (mTest.isHistoryInstalledApp(pkg)) {
                if (Utils.HWFLOW) {
                    Log.i(TAG, "pkg " + pkg + " is a history installed application");
                }
            } else if (Utils.HWFLOW) {
                Log.i(TAG, "pkg " + pkg + " is not a history installed application");
            }
        }
    }

    public static final void dumpRecordedAppUsageInfo() {
        mTest.dumpInfo(false);
    }
}
