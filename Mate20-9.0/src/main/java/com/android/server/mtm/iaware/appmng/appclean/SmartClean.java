package com.android.server.mtm.iaware.appmng.appclean;

import android.app.mtm.iaware.appmng.AppMngConstant;
import android.content.Context;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import com.android.server.mtm.iaware.appmng.AwareAppCleanerForSC;
import com.android.server.mtm.iaware.appmng.AwareAppMngSortPolicy;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.mtm.iaware.appmng.rule.RuleParserUtil;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.mtm.utils.AppStatusUtils;
import com.android.server.rms.algorithm.AwareUserHabit;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SmartClean extends CleanSource {
    private static final String TAG = "SmartClean";
    private Context mContext;

    public SmartClean(Context context) {
        this.mContext = context;
    }

    public void clean() {
        List<AwareProcessInfo> allAwareProcNeedProcess = AppStatusUtils.getInstance().getAllProcNeedSort();
        if (allAwareProcNeedProcess == null || allAwareProcNeedProcess.isEmpty()) {
            AwareLog.e(TAG, "getAllProcNeedSort failed!");
            return;
        }
        List<AwareProcessBlockInfo> rawInfo = DecisionMaker.getInstance().decideAll(allAwareProcNeedProcess, 0, AppMngConstant.AppMngFeature.APP_CLEAN, AppMngConstant.AppCleanSource.SMART_CLEAN);
        List<AwareProcessBlockInfo> needClean = getSmartCleanList(rawInfo);
        if (needClean != null && !needClean.isEmpty()) {
            Map<Integer, List<AwareProcessBlockInfo>> srcProcList = new ArrayMap<>();
            srcProcList.put(2, needClean);
            AwareAppCleanerForSC.getInstance(this.mContext).execute(new AwareAppMngSortPolicy(null, srcProcList), null);
            rawInfo.addAll(needClean);
        }
        for (AwareProcessBlockInfo block : rawInfo) {
            if (block != null && !ProcessCleaner.CleanType.NONE.equals(block.mCleanType)) {
                AwareLog.i(TAG, "pkg = " + block.mPackageName + ", uid = " + block.mUid + ", policy = " + block.mCleanType + ", reason = " + block.mReason);
            }
            updateHistory(AppMngConstant.AppCleanSource.SMART_CLEAN, block);
            uploadToBigData(AppMngConstant.AppCleanSource.SMART_CLEAN, block);
        }
    }

    public List<AwareProcessBlockInfo> getSmartCleanList(List<AwareProcessBlockInfo> rawInfo) {
        if (rawInfo == null) {
            List<AwareProcessInfo> allAwareProcNeedProcess = AppStatusUtils.getInstance().getAllProcNeedSort();
            if (allAwareProcNeedProcess == null || allAwareProcNeedProcess.isEmpty()) {
                AwareLog.e(TAG, "getAllProcNeedSort failed!");
                return null;
            }
            rawInfo = DecisionMaker.getInstance().decideAll(allAwareProcNeedProcess, 0, AppMngConstant.AppMngFeature.APP_CLEAN, AppMngConstant.AppCleanSource.SMART_CLEAN);
        }
        List<AwareProcessBlockInfo> needClean = mergeBlockForMemory(rawInfo, DecisionMaker.getInstance().getProcessList(AppMngConstant.AppMngFeature.APP_CLEAN, AppMngConstant.AppCleanSource.SMART_CLEAN));
        if (needClean == null || needClean.isEmpty()) {
            AwareLog.e(TAG, "no available process info or no process need to clean");
            return null;
        }
        removeNoUiBlock(rawInfo, needClean);
        return needClean;
    }

    private void removeNoUiBlock(List<AwareProcessBlockInfo> rawInfo, List<AwareProcessBlockInfo> needClean) {
        AwareUserHabit habit = AwareUserHabit.getInstance();
        if (habit == null) {
            AwareLog.e(TAG, "AwareUserHabit is null");
            return;
        }
        LinkedHashMap<String, Long> lru = habit.getLruCache();
        if (lru != null) {
            Iterator<AwareProcessBlockInfo> iter = needClean.iterator();
            while (iter.hasNext()) {
                AwareProcessBlockInfo block = iter.next();
                if (block != null && lru.get(block.mPackageName) == null) {
                    iter.remove();
                    block.mCleanType = ProcessCleaner.CleanType.NONE;
                    block.mReason = AppMngConstant.CleanReason.INVISIBLE.getCode();
                    ArrayMap<String, Integer> detailedReason = new ArrayMap<>();
                    detailedReason.put(RuleParserUtil.AppMngTag.POLICY.getDesc(), Integer.valueOf(ProcessCleaner.CleanType.NONE.ordinal()));
                    detailedReason.put("spec", Integer.valueOf(AppMngConstant.CleanReason.INVISIBLE.ordinal()));
                    block.mDetailedReason = detailedReason;
                    rawInfo.add(block);
                }
            }
        }
    }
}
