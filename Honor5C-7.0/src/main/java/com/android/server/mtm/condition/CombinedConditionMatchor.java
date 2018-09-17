package com.android.server.mtm.condition;

import android.os.Bundle;
import android.util.Log;
import com.android.server.pm.HwPackageManagerService;
import com.android.server.rms.iaware.memory.utils.EventTracker;
import com.android.server.security.trustcircle.IOTController;

public class CombinedConditionMatchor extends ConditionMatchorImp {
    private static boolean DEBUG = false;
    private static final String TAG = "CombinedConditionMatchor";
    private static CombinedConditionMatchor mConditionMatchor;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.mtm.condition.CombinedConditionMatchor.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.mtm.condition.CombinedConditionMatchor.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.mtm.condition.CombinedConditionMatchor.<clinit>():void");
    }

    private boolean checkSupportedCondition(int condtiontype) {
        switch (condtiontype) {
            case IOTController.TYPE_MASTER /*1000*/:
                return false;
            case IOTController.TYPE_SLAVE /*1001*/:
            case EventTracker.TRACK_TYPE_KILL /*1002*/:
            case EventTracker.TRACK_TYPE_TRIG /*1003*/:
            case EventTracker.TRACK_TYPE_END /*1004*/:
            case EventTracker.TRACK_TYPE_STOP /*1005*/:
            case HwPackageManagerService.transaction_sendLimitedPackageBroadcast /*1006*/:
            case HwPackageManagerService.TRANSACTION_CODE_GET_PREINSTALLED_APK_LIST /*1007*/:
            case HwPackageManagerService.TRANSACTION_CODE_CHECK_GMS_IS_UNINSTALLED /*1008*/:
                return true;
            default:
                return false;
        }
    }

    public static synchronized CombinedConditionMatchor getInstance() {
        CombinedConditionMatchor combinedConditionMatchor;
        synchronized (CombinedConditionMatchor.class) {
            if (mConditionMatchor == null) {
                mConditionMatchor = new CombinedConditionMatchor();
                if (DEBUG) {
                    Log.d(TAG, "getInstance create new resource");
                }
            }
            combinedConditionMatchor = mConditionMatchor;
        }
        return combinedConditionMatchor;
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
        String combinedcondition = args.getString("combinedcondition", null);
        if (DEBUG) {
            Log.d(TAG, "combinedcondition is:" + combinedcondition);
        }
        if (combinedcondition == null || combinedcondition.isEmpty()) {
            if (DEBUG) {
                Log.d(TAG, "combinedcondition null or empty: " + combinedcondition);
            }
            return 0;
        }
        String[] conditions = combinedcondition.split("\\|");
        if (conditions.length == 0) {
            if (DEBUG) {
                Log.d(TAG, "combinedcondition lengthe 0");
            }
            return 0;
        }
        int i = 0;
        while (i < conditions.length) {
            try {
                if (DEBUG) {
                    Log.d(TAG, "condition type is " + conditions[i]);
                }
                int mconditionType = Integer.parseInt(conditions[i]);
                if (checkSupportedCondition(mconditionType)) {
                    ConditionMatchor mConditionMatchor = ConditionMatchorImp.getConditionMatchor(mconditionType);
                    if (mConditionMatchor == null) {
                        if (DEBUG) {
                            Log.d(TAG, "combinedcondition type " + mconditionType + " do not exist ");
                        }
                        return 0;
                    }
                    int conditionmatch = mConditionMatchor.conditionMatch(mconditionType, args);
                    if (DEBUG) {
                        Log.d(TAG, "conditionmatch result is " + conditionmatch + " type is " + mconditionType);
                    }
                    if (conditionmatch == 0) {
                        if (DEBUG) {
                            Log.d(TAG, "combinedcondition do not match");
                        }
                        return 0;
                    }
                    i++;
                } else {
                    if (DEBUG) {
                        Log.d(TAG, "combinedcondition do not support");
                    }
                    return 0;
                }
            } catch (NumberFormatException e) {
                if (DEBUG) {
                    Log.d(TAG, "NumberFormatException");
                }
                return 0;
            }
        }
        if (2 == conditionattribute) {
            return 2;
        }
        return 1;
    }
}
