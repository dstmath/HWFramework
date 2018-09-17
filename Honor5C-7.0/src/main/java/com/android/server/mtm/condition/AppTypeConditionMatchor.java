package com.android.server.mtm.condition;

import android.os.Bundle;
import android.util.Log;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.rms.iaware.memory.utils.EventTracker;
import com.android.server.rms.shrinker.ProcessStopShrinker;
import com.android.server.security.trustcircle.IOTController;
import com.android.server.wifipro.WifiProCommonDefs;
import huawei.com.android.server.policy.HwGlobalActionsData;

public class AppTypeConditionMatchor extends ConditionMatchorImp {
    private static boolean DEBUG = false;
    private static final String TAG = "AppTypeConditionMatchor";
    private static AppTypeConditionMatchor mConditionMatchor;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.mtm.condition.AppTypeConditionMatchor.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.mtm.condition.AppTypeConditionMatchor.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.mtm.condition.AppTypeConditionMatchor.<clinit>():void");
    }

    private boolean compareAppType(int apptype, int conditiontype) {
        boolean z = true;
        switch (apptype) {
            case HwGlobalActionsData.FLAG_AIRPLANEMODE_ON /*1*/:
                if (EventTracker.TRACK_TYPE_END != conditiontype) {
                    z = false;
                }
                return z;
            case HwGlobalActionsData.FLAG_AIRPLANEMODE_OFF /*2*/:
                if (EventTracker.TRACK_TYPE_TRIG != conditiontype) {
                    z = false;
                }
                return z;
            case WifiProCommonDefs.WIFI_SECURITY_PHISHING_FAILED /*3*/:
                if (EventTracker.TRACK_TYPE_KILL != conditiontype) {
                    z = false;
                }
                return z;
            case HwGlobalActionsData.FLAG_AIRPLANEMODE_TRANSITING /*4*/:
                if (IOTController.TYPE_SLAVE != conditiontype) {
                    z = false;
                }
                return z;
            default:
                return false;
        }
    }

    public static synchronized AppTypeConditionMatchor getInstance() {
        AppTypeConditionMatchor appTypeConditionMatchor;
        synchronized (AppTypeConditionMatchor.class) {
            if (mConditionMatchor == null) {
                mConditionMatchor = new AppTypeConditionMatchor();
                if (DEBUG) {
                    Log.d(TAG, "getInstance create new resource");
                }
            }
            appTypeConditionMatchor = mConditionMatchor;
        }
        return appTypeConditionMatchor;
    }

    public static void enableDebug() {
        DEBUG = true;
    }

    public static void disableDebug() {
        DEBUG = false;
    }

    public int conditionMatch(int conditiontype, Bundle args) {
        if (args == null) {
            if (DEBUG) {
                Log.d(TAG, "condition args is not exist ");
            }
            return 0;
        }
        Bundle rsbundle = args.getBundle("resourcebundle");
        if (rsbundle == null) {
            if (DEBUG) {
                Log.d(TAG, "resourcebundle is not set ");
            }
            return 0;
        }
        int pid = rsbundle.getInt(ProcessStopShrinker.PID_KEY, -1);
        int conditionattribute = args.getInt("conditionattribute", -1);
        ProcessInfo myproc = ProcessInfoCollector.getInstance().getProcessInfo(pid);
        if (myproc == null) {
            if (DEBUG) {
                Log.d(TAG, "process not exist pid:" + pid);
            }
            return 0;
        }
        boolean matched = compareAppType(myproc.mType, conditiontype);
        if (matched && 2 == conditionattribute) {
            return 2;
        }
        if (matched) {
            return 1;
        }
        return 0;
    }
}
