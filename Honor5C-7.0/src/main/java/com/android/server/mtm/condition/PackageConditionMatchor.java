package com.android.server.mtm.condition;

import android.os.Bundle;
import android.util.Log;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.mtm.utils.InnerUtils;
import com.android.server.rms.shrinker.ProcessStopShrinker;
import com.android.server.security.trustcircle.IOTController;

public class PackageConditionMatchor extends ConditionMatchorImp {
    private static boolean DEBUG = false;
    private static final String TAG = "PackageConditionMatchor";
    private static PackageConditionMatchor mConditionMatchor;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.mtm.condition.PackageConditionMatchor.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.mtm.condition.PackageConditionMatchor.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.mtm.condition.PackageConditionMatchor.<clinit>():void");
    }

    private boolean comparePackageName(int pid, int uid, String conditionname, int conditiontype) {
        if (conditionname == null) {
            return false;
        }
        ProcessInfo myproc = ProcessInfoCollector.getInstance().getProcessInfo(pid);
        if (uid <= 0) {
            if (myproc == null) {
                if (DEBUG) {
                    Log.d(TAG, "process is not exit pid:" + pid);
                }
                return false;
            }
            uid = myproc.mUid;
        }
        if (ConditionMatchor.PACKAGENAMECONTAINS == conditiontype) {
            if (InnerUtils.checkPackageNameContainsByUid(uid, conditionname)) {
                return true;
            }
        } else if (InnerUtils.checkPackageNameByUid(uid, conditionname)) {
            return true;
        }
        if (myproc != null) {
            int list_size = myproc.mPackageName.size();
            for (int i = 0; i < list_size; i++) {
                String pkgname = (String) myproc.mPackageName.get(i);
                if (DEBUG) {
                    Log.d(TAG, "process packagename include:" + pkgname + " and pid is:" + pid);
                }
                if (ConditionMatchor.PACKAGENAMECONTAINS == conditiontype) {
                    if (pkgname != null && pkgname.contains(conditionname)) {
                        return true;
                    }
                } else if (pkgname != null && pkgname.equals(conditionname)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static synchronized PackageConditionMatchor getInstance() {
        PackageConditionMatchor packageConditionMatchor;
        synchronized (PackageConditionMatchor.class) {
            if (mConditionMatchor == null) {
                mConditionMatchor = new PackageConditionMatchor();
                if (DEBUG) {
                    Log.d(TAG, "getInstance create new resource");
                }
            }
            packageConditionMatchor = mConditionMatchor;
        }
        return packageConditionMatchor;
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
        boolean matched = comparePackageName(rsbundle.getInt(ProcessStopShrinker.PID_KEY, -1), rsbundle.getInt("uid", -1), conditionname, conditiontype);
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
