package com.android.server.mtm.policy;

import android.app.mtm.MultiTaskPolicy;
import android.os.Bundle;
import android.util.Log;
import com.android.server.location.HwGnssLogHandlerMsgID;
import com.android.server.mtm.condition.ConditionMatchor;
import com.android.server.mtm.condition.ConditionMatchorImp;
import com.android.server.mtm.policy.MultiTaskPolicyList.MultiTaskResourceConfig;
import com.android.server.mtm.policy.MultiTaskPolicyList.PolicyConditionConfig;
import com.android.server.mtm.policy.MultiTaskPolicyList.PolicyConfig;
import java.util.ArrayList;

public class MultiTaskPolicyCreatorImp implements MultiTaskPolicyCreator {
    private static boolean DEBUG = false;
    private static final String TAG = "MultiTaskPolicyCreatorImp";
    private static MultiTaskPolicyCreatorImp mBasePolicyCreator;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.mtm.policy.MultiTaskPolicyCreatorImp.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.mtm.policy.MultiTaskPolicyCreatorImp.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.mtm.policy.MultiTaskPolicyCreatorImp.<clinit>():void");
    }

    public static MultiTaskPolicyCreator getPolicyCreator(int resourceType) {
        switch (resourceType) {
            case HwGnssLogHandlerMsgID.UPDATEBINDERRORTIME /*20*/:
                return MultiTaskPolicyMemoryCreator.getInstance();
            default:
                return getInstance();
        }
    }

    public static synchronized MultiTaskPolicyCreatorImp getInstance() {
        MultiTaskPolicyCreatorImp multiTaskPolicyCreatorImp;
        synchronized (MultiTaskPolicyCreatorImp.class) {
            if (mBasePolicyCreator == null) {
                mBasePolicyCreator = new MultiTaskPolicyCreatorImp();
                if (DEBUG) {
                    Log.d(TAG, "getInstance create new resource");
                }
            }
            multiTaskPolicyCreatorImp = mBasePolicyCreator;
        }
        return multiTaskPolicyCreatorImp;
    }

    private int conditionMatch(ArrayList<PolicyConditionConfig> conditions, Bundle args, int attributeflag) {
        if (conditions == null || conditions.size() == 0) {
            return 1;
        }
        int N = conditions.size() - 1;
        Bundle rsBundle = new Bundle();
        rsBundle.putBundle("resourcebundle", args);
        for (int i = N; i >= 0; i--) {
            PolicyConditionConfig mcondition = (PolicyConditionConfig) conditions.get(i);
            if (mcondition != null && mcondition.conditionattribute == attributeflag) {
                rsBundle.putInt("conditiontype", mcondition.conditiontype);
                rsBundle.putInt("conditionattribute", mcondition.conditionattribute);
                rsBundle.putString("conditionextend", mcondition.conditionextend);
                rsBundle.putString("combinedcondition", mcondition.combinedcondition);
                ConditionMatchor mConditionMatchor = ConditionMatchorImp.getConditionMatchor(mcondition.conditiontype);
                if (mConditionMatchor != null) {
                    int conditionmatch = mConditionMatchor.conditionMatch(mcondition.conditiontype, rsBundle);
                    if (conditionmatch == attributeflag) {
                        return conditionmatch;
                    }
                } else {
                    continue;
                }
            }
        }
        return 0;
    }

    public MultiTaskPolicy getResourcePolicy(int resourcetype, String resourceextend, int resourcestatus, Bundle args) {
        MultiTaskPolicyList policylist = MultiTaskPolicyList.getInstance();
        if (policylist == null) {
            if (DEBUG) {
                Log.d(TAG, "static policylist not exist");
            }
            return null;
        }
        MultiTaskResourceConfig resourcepolicy = policylist.getStaticPolicy(resourcetype, resourceextend, resourcestatus);
        if (resourcepolicy == null) {
            if (DEBUG) {
                Log.d(TAG, "no policy set for resource type:" + resourcetype + " resourceextend:" + resourceextend + " status:" + resourcestatus);
            }
            return null;
        }
        ArrayList<PolicyConfig> policyconfig = resourcepolicy.getPolicy();
        if (policyconfig == null || policyconfig.size() <= 0) {
            if (DEBUG) {
                Log.d(TAG, "no policy config set for resource type:" + resourcetype + " resourceextend:" + resourceextend + " status:" + resourcestatus);
            }
            return null;
        }
        int N = policyconfig.size() - 1;
        int policy = 0;
        Bundle policydata = new Bundle();
        for (int i = N; i >= 0; i--) {
            PolicyConfig mypolicy = (PolicyConfig) policyconfig.get(i);
            if (mypolicy != null) {
                ArrayList<PolicyConditionConfig> conditions = mypolicy.getPolicycondition();
                if (conditions == null || conditions.size() == 0) {
                    policy |= mypolicy.policytype;
                    if (DEBUG) {
                        Log.d(TAG, "policytype=" + mypolicy.policytype + " policyvalueflag=" + mypolicy.policyvalueflag + " policyvalue=" + mypolicy.policyvalue);
                    }
                    if (mypolicy.policyvalueflag) {
                        policydata.putInt("policyvalue" + mypolicy.policytype, mypolicy.policyvalue);
                    }
                } else if (conditionMatch(conditions, args, 2) != 2 && conditionMatch(conditions, args, 1) == 1) {
                    if (DEBUG) {
                        Log.d(TAG, "policytype=" + mypolicy.policytype + " policyvalueflag=" + mypolicy.policyvalueflag + " policyvalue=" + mypolicy.policyvalue);
                    }
                    policy |= mypolicy.policytype;
                    if (mypolicy.policyvalueflag) {
                        policydata.putInt("policyvalue" + mypolicy.policytype, mypolicy.policyvalue);
                    }
                }
            }
        }
        if (policy > 0) {
            return new MultiTaskPolicy(policy, policydata);
        }
        return null;
    }

    public static void enableDebug() {
        DEBUG = true;
    }

    public static void disableDebug() {
        DEBUG = false;
    }
}
