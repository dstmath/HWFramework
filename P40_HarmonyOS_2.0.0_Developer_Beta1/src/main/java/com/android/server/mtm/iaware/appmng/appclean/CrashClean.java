package com.android.server.mtm.iaware.appmng.appclean;

import android.app.mtm.iaware.appmng.AppMngConstant;
import android.content.Context;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import android.util.ERecovery;
import android.util.ERecoveryEvent;
import com.android.server.mtm.iaware.appmng.AwareAppCleanerForCrash;
import com.android.server.mtm.iaware.appmng.AwareAppMngSortPolicy;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CrashClean extends CleanSource {
    private static final long CRASH_CLEAN_ERECOVERYID = 421001022;
    private static final long CRASH_CLEAN_FAULTID = 905100000;
    private static final String TAG = "CrashClean";
    private int mCleanCount;
    private Context mContext;
    private int mLevel;
    private String mPkgName;
    private int mUserId;

    public CrashClean(int userId, int level, String pkgName, Context context) {
        this.mUserId = userId;
        this.mLevel = level;
        this.mPkgName = pkgName;
        this.mContext = context;
    }

    public int getCleanCount() {
        return this.mCleanCount;
    }

    @Override // com.android.server.mtm.iaware.appmng.appclean.CleanSource
    public void clean() {
        String str = this.mPkgName;
        if (str != null) {
            ArrayList<AwareProcessInfo> procList = AwareProcessInfo.getAwareProcInfosFromPackage(str, this.mUserId);
            if (procList.isEmpty()) {
                procList.add(getDeadAwareProcInfo(this.mPkgName, this.mUserId));
            }
            List<AwareProcessBlockInfo> info = mergeBlock(DecisionMaker.getInstance().decideAll((List<AwareProcessInfo>) procList, this.mLevel, AppMngConstant.AppMngFeature.APP_CLEAN, (AppMngConstant.EnumWithDesc) AppMngConstant.AppCleanSource.CRASH));
            if (info == null || info.isEmpty()) {
                AwareLog.e(TAG, "info is empty");
                return;
            }
            Map<Integer, List<AwareProcessBlockInfo>> srcProcList = new ArrayMap<>();
            srcProcList.put(2, info);
            this.mCleanCount = AwareAppCleanerForCrash.getInstance(this.mContext).execute(new AwareAppMngSortPolicy(null, srcProcList), null);
            for (AwareProcessBlockInfo block : info) {
                if (block != null) {
                    AwareLog.i(TAG, "pkg = " + block.procPackageName + ", uid = " + block.procUid + ", policy = " + block.procCleanType + ", reason = " + block.procReason);
                    updateHistory(AppMngConstant.AppCleanSource.CRASH, block);
                    uploadToBigData(AppMngConstant.AppCleanSource.CRASH, block);
                }
            }
            endCrashClean();
            AwareLog.d(TAG, "crash clean eRecovery report");
        }
    }

    private void endCrashClean() {
        ERecoveryEvent event = new ERecoveryEvent();
        event.setERecoveryID((long) CRASH_CLEAN_ERECOVERYID);
        event.setFaultID((long) CRASH_CLEAN_FAULTID);
        event.setState(3);
        event.setResult(0);
        ERecovery.eRecoveryReport(event);
    }
}
