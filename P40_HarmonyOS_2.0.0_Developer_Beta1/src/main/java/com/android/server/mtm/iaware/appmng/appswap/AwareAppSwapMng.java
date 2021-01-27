package com.android.server.mtm.iaware.appmng.appswap;

import android.app.mtm.iaware.appmng.AppMngConstant;
import android.rms.iaware.AwareLog;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.mtm.iaware.appmng.appclean.CleanSource;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.mtm.utils.AppStatusUtils;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class AwareAppSwapMng extends CleanSource {
    private static final Object LOCK = new Object();
    private static final String TAG = "AwareAppSwapMng";
    private static AwareAppSwapMng sAwareAppSwapMng = null;
    private static boolean sDebug = false;
    private static boolean sEnabled = false;
    private AtomicBoolean mIsInitialized = new AtomicBoolean(false);

    public static AwareAppSwapMng getInstance() {
        AwareAppSwapMng awareAppSwapMng;
        synchronized (LOCK) {
            if (sAwareAppSwapMng == null) {
                sAwareAppSwapMng = new AwareAppSwapMng();
            }
            awareAppSwapMng = sAwareAppSwapMng;
        }
        return awareAppSwapMng;
    }

    private AwareAppSwapMng() {
    }

    private void initialize() {
        if (!this.mIsInitialized.get()) {
            this.mIsInitialized.set(true);
        }
    }

    private void deInitialize() {
        if (this.mIsInitialized.get()) {
            this.mIsInitialized.set(false);
        }
    }

    public static void enable() {
        AwareLog.d(TAG, "AwareAppSwapMng feature enable");
        sEnabled = true;
        AwareAppSwapMng awareAppSwapMng = sAwareAppSwapMng;
        if (awareAppSwapMng != null) {
            awareAppSwapMng.initialize();
        }
    }

    public static void disable() {
        AwareLog.d(TAG, "AwareAppSwapMng feature disabled");
        sEnabled = false;
        AwareAppSwapMng awareAppSwapMng = sAwareAppSwapMng;
        if (awareAppSwapMng != null) {
            awareAppSwapMng.deInitialize();
        }
    }

    public static void enableDebug() {
        sDebug = true;
    }

    public static void disableDebug() {
        sDebug = false;
    }

    public Map<String, Integer> getAllAppSwapIndex() {
        Map<String, Integer> swapMap = new HashMap<>();
        List<AwareProcessBlockInfo> processList = getAllAppSwapPercent();
        if (processList == null || processList.isEmpty()) {
            return swapMap;
        }
        for (AwareProcessBlockInfo prcBlkInfo : processList) {
            swapMap.put(prcBlkInfo.procPackageName, Integer.valueOf(prcBlkInfo.procSwapIndex));
        }
        return swapMap;
    }

    private List<AwareProcessBlockInfo> getAllAppSwapPercent() {
        List<AwareProcessBlockInfo> awareProcessBlockInfos = new ArrayList<>();
        List<AwareProcessInfo> awareProcList = AppStatusUtils.getInstance().getAllProcNeedSort();
        if (awareProcList == null || awareProcList.isEmpty()) {
            AwareLog.d(TAG, "no pid exit in mtm");
            return awareProcessBlockInfos;
        }
        List<AwareProcessBlockInfo> awareProcessBlockInfos2 = DecisionMaker.getInstance().decideAll(awareProcList, 0, AppMngConstant.AppMngFeature.APP_CLEAN, (AppMngConstant.EnumWithDesc) AppMngConstant.AppCleanSource.APP_SWAP);
        if (awareProcessBlockInfos2 == null || awareProcessBlockInfos2.isEmpty()) {
            AwareLog.w(TAG, "no pid need to swap");
            return awareProcessBlockInfos2;
        }
        List<AwareProcessBlockInfo> awareProcessBlockInfos3 = mergeBlock(awareProcessBlockInfos2);
        if (awareProcessBlockInfos3 == null) {
            return new ArrayList();
        }
        return awareProcessBlockInfos3;
    }

    private List<AwareProcessInfo> getProcListByPkgName(String pkgName) {
        return ProcessInfoCollector.getInstance().getAwareProcessInfosFromPackage(pkgName, AwareAppAssociate.getInstance().getCurUserId());
    }

    public int getAppSwapIndex(String pkgName) {
        if (pkgName == null) {
            AwareLog.e(TAG, "pkg name is null");
            return -1;
        }
        List<AwareProcessInfo> processList = getProcListByPkgName(pkgName);
        if (processList == null || processList.size() == 0) {
            AwareLog.e(TAG, "can not get process list by pkg name" + pkgName);
            return -1;
        }
        List<AwareProcessBlockInfo> procBlockList = DecisionMaker.getInstance().decideAll(processList, 0, AppMngConstant.AppMngFeature.APP_CLEAN, (AppMngConstant.EnumWithDesc) AppMngConstant.AppCleanSource.APP_SWAP);
        if (sDebug) {
            for (AwareProcessBlockInfo temp : procBlockList) {
                AwareLog.w(TAG, temp.toString());
            }
        }
        List<AwareProcessBlockInfo> procBlockList2 = mergeBlock(procBlockList);
        if (procBlockList2 == null || procBlockList2.isEmpty()) {
            return -1;
        }
        return procBlockList2.get(0).procSwapIndex;
    }

    public void dumpAppSwap(PrintWriter pw) {
        synchronized (LOCK) {
            List<AwareProcessBlockInfo> processList = getAllAppSwapPercent();
            pw.println("Application swap policy: ");
            for (AwareProcessBlockInfo temp : processList) {
                pw.println("App name: " + temp.procPackageName + ", swap policy:" + temp.procSwapIndex + ", policy:" + temp.procCleanType + ", procReason:" + temp.procReason);
                for (AwareProcessInfo process : temp.getProcessList()) {
                    pw.println("    proc id: " + process.procPid + ", swap policy:" + process.procSwapIndex + ", policy:" + process.procCleanType);
                }
            }
        }
    }

    public void dumpAppSwapByPkg(PrintWriter pw, String pkgName) {
        synchronized (LOCK) {
            int result = getAppSwapIndex(pkgName);
            pw.println("Application swap policy: ");
            pw.println("App name: " + pkgName + ", swap policy:" + result);
        }
    }
}
