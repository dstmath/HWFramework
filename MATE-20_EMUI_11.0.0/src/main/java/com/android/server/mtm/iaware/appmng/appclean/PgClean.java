package com.android.server.mtm.iaware.appmng.appclean;

import android.app.mtm.iaware.appmng.AppCleanParam;
import android.app.mtm.iaware.appmng.AppMngConstant;
import android.app.mtm.iaware.appmng.IAppCleanCallback;
import android.content.Context;
import android.os.RemoteException;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import com.android.server.mtm.iaware.appmng.AwareAppCleanerForPg;
import com.android.server.mtm.iaware.appmng.AwareAppMngSortPolicy;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PgClean extends CleanSource {
    private static final String TAG = "PGClean";
    private IAppCleanCallback mCallback;
    private Context mContext;
    private AppCleanParam mParam;

    public PgClean(AppCleanParam param, IAppCleanCallback callback, Context context) {
        this.mParam = param;
        this.mContext = context;
        this.mCallback = callback;
    }

    @Override // com.android.server.mtm.iaware.appmng.appclean.CleanSource
    public void clean() {
        AppCleanParam appCleanParam = this.mParam;
        if (appCleanParam != null) {
            List<String> pkgList = appCleanParam.getStringList();
            List<Integer> intList = this.mParam.getIntList();
            if (!pkgList.isEmpty()) {
                if (pkgList.size() != intList.size()) {
                    AwareLog.e(TAG, "size of pkglist should same to intlist");
                } else {
                    executeClean(pkgList, intList);
                }
            }
        }
    }

    private void executeClean(List<String> pkgList, List<Integer> intList) {
        ArrayList<AwareProcessInfo> proclist = new ArrayList<>();
        int listSize = pkgList.size();
        for (int i = 0; i < listSize; i++) {
            String packageName = pkgList.get(i);
            int userId = intList.get(i).intValue();
            ArrayList<AwareProcessInfo> procInfo = AwareProcessInfo.getAwareProcInfosFromPackage(packageName, userId);
            if (procInfo.isEmpty()) {
                proclist.add(getDeadAwareProcInfo(packageName, userId));
            } else {
                proclist.addAll(procInfo);
            }
        }
        List<AwareProcessBlockInfo> info = mergeBlock(DecisionMaker.getInstance().decideAll((List<AwareProcessInfo>) proclist, this.mParam.getLevel(), AppMngConstant.AppMngFeature.APP_CLEAN, (AppMngConstant.EnumWithDesc) AppMngConstant.AppCleanSource.POWER_GENIE));
        if (info == null || info.isEmpty()) {
            AwareLog.e(TAG, "info is empty");
            return;
        }
        Map<Integer, List<AwareProcessBlockInfo>> srcProcList = new ArrayMap<>();
        srcProcList.put(2, info);
        int cleanCount = AwareAppCleanerForPg.getInstance(this.mContext).execute(new AwareAppMngSortPolicy(null, srcProcList), null);
        AppCleanParam result = new AppCleanParam.Builder(this.mParam.getSource()).timeStamp(this.mParam.getTimeStamp()).killedCount(cleanCount).build();
        IAppCleanCallback iAppCleanCallback = this.mCallback;
        if (iAppCleanCallback != null) {
            try {
                iAppCleanCallback.onCleanFinish(result);
            } catch (RemoteException e) {
                AwareLog.e(TAG, "RemoteExcption e = " + e);
            }
        }
        for (AwareProcessBlockInfo block : info) {
            if (block != null) {
                AwareLog.i(TAG, "cleanCount = " + cleanCount + ", pkg = " + block.procPackageName + ", uid = " + block.procUid + ", policy = " + block.procCleanType + ", reason = " + block.procReason);
                if (block.procDetailedReason == null) {
                    AwareLog.e(TAG, "detailedReason is null, not upload to bigData");
                } else {
                    block.procDetailedReason.put(MemoryConstant.MEM_FILECACHE_ITEM_LEVEL, Integer.valueOf(this.mParam.getLevel()));
                    updateHistory(AppMngConstant.AppCleanSource.POWER_GENIE, block);
                    uploadToBigData(AppMngConstant.AppCleanSource.POWER_GENIE, block);
                }
            }
        }
    }
}
