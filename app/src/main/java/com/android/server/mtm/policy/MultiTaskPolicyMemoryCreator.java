package com.android.server.mtm.policy;

import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.mtm.MultiTaskPolicy;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import com.android.server.am.HwActivityManagerService;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.mtm.condition.ConditionMatchor;
import com.android.server.mtm.policy.MultiTaskPolicyList.MultiTaskResourceConfig;
import com.android.server.mtm.policy.MultiTaskPolicyList.PolicyConditionConfig;
import com.android.server.mtm.policy.MultiTaskPolicyList.PolicyConfig;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.rms.IScene;
import com.android.server.rms.collector.ProcMemInfoReader;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.resource.MemoryInnerResource;
import com.android.server.rms.scene.NonIdleScene;
import com.android.server.security.trustcircle.lifecycle.LifeCycleStateMachine;
import com.android.server.wifipro.WifiProCommonDefs;
import com.android.server.wifipro.WifiProCommonUtils;
import huawei.com.android.server.policy.HwGlobalActionsData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class MultiTaskPolicyMemoryCreator extends MultiTaskPolicyCreatorImp {
    private static boolean DEBUG = false;
    public static final int MAX_TASK_NUM = 32;
    public static final String POLICY_PROCESSFORCESTOP_PARAM = "pidsneedtoforcestop";
    public static final String POLICY_PROCESSHRINK_PARAM = "pidsneedtoshrink";
    public static final String POLICY_PROCESSKILL_PARAM = "pidsneedtokill";
    private static final String TAG = "MTM.MultiTaskPolicyMemoryCreator";
    private static Map<Integer, Integer> mLRU;
    private static int mMaxRecentTaskPos;
    private static Map<Integer, Integer> mPackageType;
    private static MultiTaskPolicyMemoryCreator mPolicyCreator;
    private static Map<String, Integer> mRecentTasks;
    private static Map<Integer, Integer> mSchedGroup;
    private IActivityManager mActivityManager;
    private MultiTaskManagerService mMtms;
    private PackageManager mPackageManager;
    private final ProcMemInfoReader mProcMemInfoReader;
    private IScene mScene;
    Comparator<ProcessInfo> processComparator;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.mtm.policy.MultiTaskPolicyMemoryCreator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.mtm.policy.MultiTaskPolicyMemoryCreator.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.mtm.policy.MultiTaskPolicyMemoryCreator.<clinit>():void");
    }

    public MultiTaskPolicyMemoryCreator() {
        this.mProcMemInfoReader = new ProcMemInfoReader();
        this.mMtms = null;
        this.mPackageManager = null;
        this.mActivityManager = null;
        this.mScene = null;
        this.processComparator = new Comparator<ProcessInfo>() {
            public int compare(ProcessInfo arg0, ProcessInfo arg1) {
                if (arg0.mCurSchedGroup != arg1.mCurSchedGroup) {
                    return MultiTaskPolicyMemoryCreator.this.getMapIntValue(MultiTaskPolicyMemoryCreator.mSchedGroup, arg0.mCurSchedGroup) - MultiTaskPolicyMemoryCreator.this.getMapIntValue(MultiTaskPolicyMemoryCreator.mSchedGroup, arg1.mCurSchedGroup);
                }
                int pos0 = MultiTaskPolicyMemoryCreator.this.getPositionInRecentTasks(arg0.mPackageName);
                int pos1 = MultiTaskPolicyMemoryCreator.this.getPositionInRecentTasks(arg1.mPackageName);
                if (pos0 != pos1) {
                    return pos0 - pos1;
                }
                int lru0 = MultiTaskPolicyMemoryCreator.this.getLRU(arg0.mPid);
                int lru1 = MultiTaskPolicyMemoryCreator.this.getLRU(arg1.mPid);
                if (lru0 != lru1) {
                    return lru1 - lru0;
                }
                if (arg0.mType != arg1.mType) {
                    return MultiTaskPolicyMemoryCreator.this.getMapIntValue(MultiTaskPolicyMemoryCreator.mPackageType, arg0.mType) - MultiTaskPolicyMemoryCreator.this.getMapIntValue(MultiTaskPolicyMemoryCreator.mPackageType, arg1.mType);
                }
                return arg0.mCurAdj - arg1.mCurAdj;
            }
        };
        this.mMtms = MultiTaskManagerService.self();
        if (this.mMtms == null || this.mMtms.context() == null) {
            Log.e(TAG, "multitask handle or context is null");
            return;
        }
        Context context = this.mMtms.context();
        this.mPackageManager = context.getPackageManager();
        this.mActivityManager = ActivityManagerNative.getDefault();
        this.mScene = new NonIdleScene(context);
    }

    public static synchronized MultiTaskPolicyCreatorImp getInstance() {
        MultiTaskPolicyCreatorImp multiTaskPolicyCreatorImp;
        synchronized (MultiTaskPolicyMemoryCreator.class) {
            if (mPolicyCreator == null) {
                mPolicyCreator = new MultiTaskPolicyMemoryCreator();
                if (DEBUG) {
                    Log.d(TAG, "getInstance create new resource");
                }
            }
            multiTaskPolicyCreatorImp = mPolicyCreator;
        }
        return multiTaskPolicyCreatorImp;
    }

    public MultiTaskPolicy getResourcePolicy(int resourcetype, String resourceextend, int resourcestatus, Bundle args) {
        if (resourcetype != 20) {
            return null;
        }
        long memNeedToReclaim = args.getLong(MemoryInnerResource.MEMORY_PARAM_MEMNEEDTORECLAIM);
        int policy = 0;
        Bundle policyData = new Bundle();
        Log.d(TAG, "getResourcePolicy resourcetype:" + resourcetype + " resourceextend:" + resourceextend + " resourcestatus:" + resourcestatus + " memNeedToReclaim:" + memNeedToReclaim);
        MultiTaskResourceConfig staticPolicy = MultiTaskPolicyList.getInstance().getStaticPolicy(resourcetype, resourceextend, resourcestatus);
        if (staticPolicy == null || staticPolicy.getPolicy().size() <= 0) {
            Log.d(TAG, "no policy found in static policy!");
            return null;
        }
        ArrayList<PolicyConfig> policyConfigs = staticPolicy.getPolicy();
        if (DEBUG) {
            Log.d(TAG, "policyConfigs.size: " + policyConfigs.size());
        }
        Iterator iter = policyConfigs.iterator();
        while (iter.hasNext()) {
            PolicyConfig policyConfig = (PolicyConfig) iter.next();
            int policyType = policyConfig.policytype;
            Log.d(TAG, "policytype: " + policyType);
            if (meetTheConditions(policyConfig)) {
                policy |= policyType;
                if ((policyType & MAX_TASK_NUM) != 0) {
                    if (DEBUG) {
                        Log.d(TAG, "MultiTask_POLICY_ProcessKill");
                    }
                    ArrayList<ProcessInfo> procList = getProcessInfoListSorted(resourcestatus);
                    ArrayList<ProcessInfo> procNeedToForcestop = new ArrayList();
                    ArrayList<ProcessInfo> procNeedToKill = new ArrayList();
                    getProcessNeedToClean(policyConfig, procList, memNeedToReclaim, procNeedToForcestop, procNeedToKill);
                    policyData.putIntArray(POLICY_PROCESSFORCESTOP_PARAM, getPidArray(procNeedToForcestop));
                    policyData.putIntArray(POLICY_PROCESSKILL_PARAM, getPidArray(procNeedToKill));
                } else if ((policyType & HwGlobalActionsData.FLAG_SILENTMODE_NORMAL) != 0) {
                    if (DEBUG) {
                        Log.d(TAG, "MultiTask_POLICY_ProcessShrink");
                    }
                    policyData.putIntArray(POLICY_PROCESSHRINK_PARAM, getProcessNeedToReclaim(args));
                }
            } else {
                Log.d(TAG, "policytype: " + policyType + " does not meet the condition!");
            }
        }
        return new MultiTaskPolicy(policy, policyData);
    }

    private boolean meetTheConditions(PolicyConfig policyConfig) {
        boolean z = false;
        ArrayList<PolicyConditionConfig> conditionList = policyConfig.getPolicycondition();
        if (conditionList == null || conditionList.size() <= 0) {
            return true;
        }
        Iterator iter = conditionList.iterator();
        while (iter.hasNext()) {
            PolicyConditionConfig condition = (PolicyConditionConfig) iter.next();
            if (DEBUG) {
                Log.d(TAG, "conditiontype: " + condition.conditiontype + " conditionname: " + condition.conditionname);
            }
            if (ConditionMatchor.SCREENOFF == condition.conditiontype) {
                if (DEBUG) {
                    Log.d(TAG, "isScreenOn: " + (this.mScene == null ? false : this.mScene.identify(null)));
                }
                if (!(this.mScene == null || this.mScene.identify(null))) {
                    z = true;
                }
                return z;
            }
        }
        return true;
    }

    private ArrayList<ProcessInfo> getProcessInfoListSorted(int resourcestatus) {
        ArrayList<ProcessInfo> procList = ProcessInfoCollector.getInstance().getProcessInfoList();
        if (procList == null) {
            return new ArrayList();
        }
        if (DEBUG) {
            Log.d(TAG, "ProcessInfoList.size: " + procList.size());
        }
        dumpProcessList(procList, null);
        sortProcess(procList, resourcestatus);
        if (DEBUG) {
            Log.d(TAG, "after sort: ");
        }
        dumpProcessList(procList, null);
        return procList;
    }

    private void getProcessNeedToClean(PolicyConfig policyConfig, ArrayList<ProcessInfo> procList, long memNeedToReclaim, ArrayList<ProcessInfo> procNeedToForcestop, ArrayList<ProcessInfo> procNeedToKill) {
        ArrayList<ProcessInfo> procCanKill = processCanKill(procList);
        Map<Integer, Long> procPss = new HashMap();
        long totalMem = 0;
        if (DEBUG) {
            Log.d(TAG, "procCanKill.size: " + procCanKill.size());
        }
        ListIterator iter = procCanKill.listIterator(procCanKill.size());
        while (iter.hasPrevious()) {
            ProcessInfo processInfo = (ProcessInfo) iter.previous();
            if (totalMem >= memNeedToReclaim) {
                break;
            } else if (!packageNameInWhiteList(policyConfig, processInfo.mPackageName)) {
                long pss = this.mProcMemInfoReader.getProcessPssByPID(processInfo.mPid);
                totalMem += pss;
                Log.d(TAG, "mPid: " + processInfo.mPid + " mProcessName: " + processInfo.mProcessName + " pss: " + pss + " totalMem:" + totalMem);
                if (ProcessInfoCollector.getInstance().hasForegroundDeps(processInfo.mPackageName)) {
                    procNeedToKill.add(processInfo);
                } else {
                    procNeedToForcestop.add(processInfo);
                }
                procPss.put(Integer.valueOf(processInfo.mPid), Long.valueOf(pss));
            }
        }
        if (DEBUG) {
            Log.d(TAG, "procNeedToForcestop.size: " + procNeedToForcestop.size());
        }
        dumpProcessList(procNeedToForcestop, procPss);
        if (DEBUG) {
            Log.d(TAG, "procNeedToKill.size: " + procNeedToKill.size());
        }
        dumpProcessList(procNeedToKill, procPss);
    }

    private boolean packageNameInWhiteList(PolicyConfig policyConfig, ArrayList<String> packageName) {
        ArrayList<PolicyConditionConfig> conditionList = policyConfig.getPolicycondition();
        if (conditionList == null || conditionList.size() <= 0) {
            return true;
        }
        Iterator iter = conditionList.iterator();
        while (iter.hasNext()) {
            PolicyConditionConfig condition = (PolicyConditionConfig) iter.next();
            if (condition != null && ConditionMatchor.PACKAGENAMECONTAINS == condition.conditiontype && 2 == condition.conditionattribute) {
                int i = 0;
                while (i < packageName.size()) {
                    String pkgName = (String) packageName.get(i);
                    if (pkgName == null || !pkgName.contains(condition.conditionextend)) {
                        i++;
                    } else {
                        Log.d(TAG, "package: " + pkgName + " is in the white list!");
                        return true;
                    }
                }
                continue;
            }
        }
        return false;
    }

    private int[] getPidArray(ArrayList<ProcessInfo> procList) {
        if (procList == null || procList.size() <= 0) {
            return new int[0];
        }
        int[] pids = new int[procList.size()];
        for (int i = 0; i < procList.size(); i++) {
            pids[i] = ((ProcessInfo) procList.get(i)).mPid;
        }
        return pids;
    }

    private ArrayList<ProcessInfo> processCanKill(ArrayList<ProcessInfo> procList) {
        ArrayList<ProcessInfo> procInfoList = new ArrayList();
        if (DEBUG) {
            Log.d(TAG, "processCanKill !!");
        }
        Iterator iter = procList.iterator();
        while (iter.hasNext()) {
            ProcessInfo procInfo = (ProcessInfo) iter.next();
            dumpProcessInfo(procInfo, 0);
            if (!(procInfo.mCurSchedGroup != 0 || procInfo.mCurAdj == HwActivityManagerService.PREVIOUS_APP_ADJ || procInfo.mCurAdj == 100 || procInfo.mCurAdj == WifiProCommonUtils.HTTP_REACHALBE_HOME || procInfo.mCurAdj == 0 || procInfo.mCurAdj == WifiProCommonUtils.RESP_CODE_TIMEOUT)) {
                if (DEBUG) {
                    Log.d(TAG, "can kill process: " + procInfo.mPid);
                }
                procInfoList.add(procInfo);
            }
        }
        return procInfoList;
    }

    private int[] getProcessNeedToReclaim(Bundle args) {
        int[] mainServicePids = args.getIntArray(MemoryInnerResource.MEMORY_PARAM_PROCNEEDTORECLAIM);
        if (mainServicePids != null && DEBUG) {
            Log.d(TAG, "getProcessNeedToReclaim mainServicePids.size: " + mainServicePids.length);
        }
        return mainServicePids;
    }

    private void sortProcess(ArrayList<ProcessInfo> processList, int resourcestatus) {
        readRecentTasks();
        readLRU(processList);
        Collections.sort(processList, this.processComparator);
    }

    private int getMapIntValue(Map<Integer, Integer> map, int key) {
        Integer value = (Integer) map.get(Integer.valueOf(key));
        return value == null ? -1 : value.intValue();
    }

    private void readRecentTasks() {
        mRecentTasks.clear();
        mMaxRecentTaskPos = 0;
        if (this.mActivityManager != null) {
            List recentTaskInfos = null;
            try {
                recentTaskInfos = this.mActivityManager.getRecentTasks(MAX_TASK_NUM, 6, UserHandle.myUserId()).getList();
            } catch (RemoteException e) {
            }
            if (recentTaskInfos != null) {
                mMaxRecentTaskPos = recentTaskInfos.size();
                if (DEBUG) {
                    Log.d(TAG, "recent task size: " + mMaxRecentTaskPos);
                }
                for (int i = 0; i < mMaxRecentTaskPos; i++) {
                    RecentTaskInfo info = (RecentTaskInfo) recentTaskInfos.get(i);
                    if (info != null) {
                        Intent intent = new Intent(info.baseIntent);
                        if (info.origActivity != null) {
                            intent.setComponent(info.origActivity);
                        }
                        intent.setFlags((intent.getFlags() & -2097153) | 268435456);
                        intent.addFlags(1048576);
                        ResolveInfo resolveInfo = this.mPackageManager.resolveActivity(intent, HwGlobalActionsData.FLAG_REBOOT);
                        if (!(resolveInfo == null || resolveInfo.activityInfo == null || resolveInfo.activityInfo.packageName == null)) {
                            if (DEBUG) {
                                Log.d(TAG, "recent task packageName: " + resolveInfo.activityInfo.packageName + " index: " + i);
                            }
                            mRecentTasks.put(resolveInfo.activityInfo.packageName, Integer.valueOf(i));
                        }
                    }
                }
                if (DEBUG) {
                    Log.d(TAG, "recent task size in map: " + mMaxRecentTaskPos);
                }
            }
        }
    }

    private int getPositionInRecentTasks(ArrayList<String> packageName) {
        if (packageName == null) {
            return mRecentTasks.values().size();
        }
        for (int i = 0; i < packageName.size(); i++) {
            String pkgName = (String) packageName.get(i);
            if (pkgName != null) {
                Integer value = (Integer) mRecentTasks.get(pkgName);
                if (DEBUG) {
                    Log.d(TAG, "getPositionInRecentTasks packageName: " + pkgName + " index: " + value);
                }
                if (value != null) {
                    return value.intValue();
                }
            }
        }
        return mMaxRecentTaskPos;
    }

    private void readLRU(ArrayList<ProcessInfo> processList) {
        mLRU.clear();
        Iterator iter = processList.iterator();
        while (iter.hasNext()) {
            ProcessInfo processInfo = (ProcessInfo) iter.next();
            mLRU.put(Integer.valueOf(processInfo.mPid), Integer.valueOf(ProcessInfoCollector.getInstance().getAMSLruBypid(processInfo.mPid)));
        }
    }

    private int getLRU(int pid) {
        return ((Integer) mLRU.get(Integer.valueOf(pid))).intValue();
    }

    private void dumpProcessList(ArrayList<ProcessInfo> processList, Map<Integer, Long> procPss) {
        Iterator iter = processList.iterator();
        while (iter.hasNext()) {
            ProcessInfo procInfo = (ProcessInfo) iter.next();
            dumpProcessInfo(procInfo, procPss != null ? ((Long) procPss.get(Integer.valueOf(procInfo.mPid))).longValue() : 0);
        }
    }

    private void dumpProcessInfo(ProcessInfo Info, long pss) {
        if (DEBUG) {
            Log.d(TAG, "pid=" + Info.mPid + ", uid=" + Info.mUid + ", PackageName=" + Info.mPackageName + ", ProcessName=" + Info.mProcessName + ", Group=" + getSchedGroupName(Info.mCurSchedGroup) + ", oom_Adj=" + Info.mCurAdj + ", package type=" + getPkgTypeName(Info.mType) + ", pss=" + pss + ", LRU=" + ProcessInfoCollector.getInstance().getAMSLruBypid(Info.mPid) + ", PositionInRecentTask=" + getPositionInRecentTasks(Info.mPackageName));
        }
    }

    private String getSchedGroupName(int group) {
        switch (group) {
            case WifiProCommonUtils.HISTORY_ITEM_UNCHECKED /*-1*/:
                return MemoryConstant.MEM_SCENE_DEFAULT;
            case WifiProCommonUtils.HISTORY_ITEM_NO_INTERNET /*0*/:
                return "backgroud";
            case LifeCycleStateMachine.DELETE_ACCOUNT /*6*/:
                return "perceptible";
            default:
                return AppHibernateCst.INVALID_PKG;
        }
    }

    private String getPkgTypeName(int type) {
        switch (type) {
            case HwGlobalActionsData.FLAG_AIRPLANEMODE_ON /*1*/:
                return "SYSTEM_SERVER";
            case HwGlobalActionsData.FLAG_AIRPLANEMODE_OFF /*2*/:
                return "SYSTEM_APP";
            case WifiProCommonDefs.WIFI_SECURITY_PHISHING_FAILED /*3*/:
                return "HW_INSTALL";
            case HwGlobalActionsData.FLAG_AIRPLANEMODE_TRANSITING /*4*/:
                return "THIRDPARTY";
            default:
                return AppHibernateCst.INVALID_PKG;
        }
    }

    public static void enableDebug() {
        DEBUG = true;
    }

    public static void disableDebug() {
        DEBUG = false;
    }
}
