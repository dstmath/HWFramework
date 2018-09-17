package com.android.server.mtm.iaware.appmng.appclean;

import android.app.mtm.iaware.appmng.AppMngConstant.AppCleanSource;
import android.app.mtm.iaware.appmng.AppMngConstant.AppMngFeature;
import android.app.mtm.iaware.appmng.AppMngConstant.CleanReason;
import android.content.Context;
import android.rms.iaware.AwareLog;
import com.android.server.mtm.iaware.appmng.AwareAppCleanerForSC;
import com.android.server.mtm.iaware.appmng.AwareAppMngSortPolicy;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.AppMngTag;
import com.android.server.mtm.taskstatus.ProcessCleaner.CleanType;
import com.android.server.mtm.utils.AppStatusUtils;
import com.android.server.rms.algorithm.AwareUserHabit;
import java.util.HashMap;
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
        List<AwareProcessBlockInfo> rawInfo = DecisionMaker.getInstance().decideAll(allAwareProcNeedProcess, 0, AppMngFeature.APP_CLEAN, AppCleanSource.SMART_CLEAN);
        List<AwareProcessBlockInfo> needClean = getSmartCleanList(rawInfo);
        if (!(needClean == null || needClean.isEmpty())) {
            Map<Integer, List<AwareProcessBlockInfo>> srcProcList = new HashMap();
            srcProcList.put(Integer.valueOf(2), needClean);
            AwareAppCleanerForSC.getInstance(this.mContext).execute(new AwareAppMngSortPolicy(null, srcProcList), null);
            rawInfo.addAll(needClean);
        }
        for (AwareProcessBlockInfo block : rawInfo) {
            if (!(block == null || (CleanType.NONE.equals(block.mCleanType) ^ 1) == 0)) {
                AwareLog.i(TAG, "pkg = " + block.mPackageName + ", uid = " + block.mUid + ", policy = " + block.mCleanType + ", reason = " + block.mReason);
            }
            updateHistory(AppCleanSource.SMART_CLEAN, block);
            uploadToBigData(AppCleanSource.SMART_CLEAN, block);
        }
    }

    public List<AwareProcessBlockInfo> getSmartCleanList(List<AwareProcessBlockInfo> rawInfo) {
        if (rawInfo == null) {
            List<AwareProcessInfo> allAwareProcNeedProcess = AppStatusUtils.getInstance().getAllProcNeedSort();
            if (allAwareProcNeedProcess == null || allAwareProcNeedProcess.isEmpty()) {
                AwareLog.e(TAG, "getAllProcNeedSort failed!");
                return null;
            }
            rawInfo = DecisionMaker.getInstance().decideAll(allAwareProcNeedProcess, 0, AppMngFeature.APP_CLEAN, AppCleanSource.SMART_CLEAN);
        }
        List<AwareProcessBlockInfo> needClean = CleanSource.mergeBlockForMemory(rawInfo);
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
                AwareProcessBlockInfo block = (AwareProcessBlockInfo) iter.next();
                if (block != null && ((Long) lru.get(block.mPackageName)) == null) {
                    iter.remove();
                    block.mCleanType = CleanType.NONE;
                    block.mReason = CleanReason.INVISIBLE.getCode();
                    HashMap<String, Integer> detailedReason = new HashMap();
                    detailedReason.put(AppMngTag.POLICY.getDesc(), Integer.valueOf(CleanType.NONE.ordinal()));
                    detailedReason.put("spec", Integer.valueOf(CleanReason.INVISIBLE.ordinal()));
                    block.mDetailedReason = detailedReason;
                    rawInfo.add(block);
                }
            }
        }
    }
}
