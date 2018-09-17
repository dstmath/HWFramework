package com.android.server.mtm.condition;

import android.os.Bundle;
import android.util.Log;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.pm.HwPackageManagerService;
import com.android.server.rms.iaware.memory.utils.EventTracker;
import com.android.server.rms.shrinker.ProcessStopShrinker;
import com.android.server.security.trustcircle.lifecycle.LifeCycleStateMachine;
import com.android.server.wifipro.WifiProCommonUtils;

public class GroupConditionMatchor extends ConditionMatchorImp {
    private static boolean DEBUG = false;
    private static final String TAG = "GroupConditionMatchor";
    private static GroupConditionMatchor mConditionMatchor;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.mtm.condition.GroupConditionMatchor.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.mtm.condition.GroupConditionMatchor.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.mtm.condition.GroupConditionMatchor.<clinit>():void");
    }

    private boolean compareGroupType(int grouptype, int conditiontype) {
        boolean z = true;
        switch (grouptype) {
            case WifiProCommonUtils.HISTORY_ITEM_UNCHECKED /*-1*/:
                if (HwPackageManagerService.TRANSACTION_CODE_CHECK_GMS_IS_UNINSTALLED != conditiontype) {
                    z = false;
                }
                return z;
            case WifiProCommonUtils.HISTORY_ITEM_NO_INTERNET /*0*/:
                if (HwPackageManagerService.transaction_sendLimitedPackageBroadcast != conditiontype) {
                    z = false;
                }
                return z;
            case LifeCycleStateMachine.LOGOUT /*5*/:
                if (EventTracker.TRACK_TYPE_STOP != conditiontype) {
                    z = false;
                }
                return z;
            case LifeCycleStateMachine.DELETE_ACCOUNT /*6*/:
                if (HwPackageManagerService.TRANSACTION_CODE_GET_PREINSTALLED_APK_LIST != conditiontype) {
                    z = false;
                }
                return z;
            default:
                return false;
        }
    }

    public static synchronized GroupConditionMatchor getInstance() {
        GroupConditionMatchor groupConditionMatchor;
        synchronized (GroupConditionMatchor.class) {
            if (mConditionMatchor == null) {
                mConditionMatchor = new GroupConditionMatchor();
                if (DEBUG) {
                    Log.d(TAG, "getInstance create new resource");
                }
            }
            groupConditionMatchor = mConditionMatchor;
        }
        return groupConditionMatchor;
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
        int myschedgroup = myproc.mCurSchedGroup;
        if (WifiProCommonUtils.HTTP_REACHALBE_HOME == myproc.mCurAdj && myproc.mCurSchedGroup != 6) {
            if (myproc.mCurSchedGroup == 0) {
                if (DEBUG) {
                    Log.d(TAG, "group is adjust to :6");
                }
                myschedgroup = 6;
            } else if (myproc.mCurSchedGroup == 5 && (AwareAppMngSort.FG_SERVICE.equals(myproc.mAdjType) || "force-fg".equals(myproc.mAdjType))) {
                if (DEBUG) {
                    Log.d(TAG, "group is just to :6");
                }
                myschedgroup = 6;
            }
        }
        if (DEBUG) {
            Log.d(TAG, "process schedgroup is :" + myschedgroup + " conditiontype is:" + conditiontype);
        }
        boolean matched = compareGroupType(myschedgroup, conditiontype);
        if (matched && 2 == conditionattribute) {
            return 2;
        }
        if (matched) {
            return 1;
        }
        return 0;
    }
}
