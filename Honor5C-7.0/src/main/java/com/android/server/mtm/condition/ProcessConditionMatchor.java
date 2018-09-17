package com.android.server.mtm.condition;

import android.os.Bundle;
import android.util.Log;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.rms.shrinker.ProcessStopShrinker;
import com.android.server.security.trustcircle.IOTController;

public class ProcessConditionMatchor extends ConditionMatchorImp {
    private static boolean DEBUG = false;
    private static final String TAG = "ProcessConditionMatchor";
    private static ProcessConditionMatchor mConditionMatchor;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.mtm.condition.ProcessConditionMatchor.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.mtm.condition.ProcessConditionMatchor.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.mtm.condition.ProcessConditionMatchor.<clinit>():void");
    }

    private boolean compareProcessName(String processname, String conditionname, int conditiontype) {
        if (processname == null || conditionname == null) {
            return false;
        }
        if (ConditionMatchor.PROCESSNAMECONTAINS == conditiontype) {
            if (processname.contains(conditionname)) {
                return true;
            }
        } else if (processname.equals(conditionname)) {
            return true;
        }
        return false;
    }

    public static synchronized ProcessConditionMatchor getInstance() {
        ProcessConditionMatchor processConditionMatchor;
        synchronized (ProcessConditionMatchor.class) {
            if (mConditionMatchor == null) {
                mConditionMatchor = new ProcessConditionMatchor();
                if (DEBUG) {
                    Log.d(TAG, "getInstance create new resource");
                }
            }
            processConditionMatchor = mConditionMatchor;
        }
        return processConditionMatchor;
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
        int conditionattribute = args.getInt("conditionattribute", -1);
        String conditionname = args.getString("conditionextend", null);
        if (conditionname == null || conditionname.isEmpty()) {
            if (DEBUG) {
                Log.d(TAG, "conditiondescribe is not set ");
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
        ProcessInfo myproc = ProcessInfoCollector.getInstance().getProcessInfo(pid);
        if (myproc == null) {
            if (DEBUG) {
                Log.d(TAG, "process not exist pid:" + pid);
            }
            return 0;
        }
        boolean matched = compareProcessName(myproc.mProcessName, conditionname, conditiontype);
        String combinedcondition = args.getString("combinedcondition", null);
        if (DEBUG) {
            Log.d(TAG, "combinedcondition is " + combinedcondition);
        }
        if (!(combinedcondition == null || combinedcondition.isEmpty())) {
            ConditionMatchor mConditionMatchor = ConditionMatchorImp.getConditionMatchor(IOTController.TYPE_MASTER);
            if (mConditionMatchor == null) {
                return 0;
            }
            if (mConditionMatchor.conditionMatch(IOTController.TYPE_MASTER, args) == 0) {
                if (DEBUG) {
                    Log.d(TAG, "combinedcondition is UNMATCHED");
                }
                return 0;
            }
        }
        if (matched && 2 == conditionattribute) {
            return 2;
        }
        if (matched) {
            return 1;
        }
        return 0;
    }
}
