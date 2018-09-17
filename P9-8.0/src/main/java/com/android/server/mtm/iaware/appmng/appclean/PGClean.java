package com.android.server.mtm.iaware.appmng.appclean;

import android.app.mtm.iaware.appmng.AppCleanParam;
import android.app.mtm.iaware.appmng.AppCleanParam.Builder;
import android.app.mtm.iaware.appmng.AppMngConstant.AppCleanSource;
import android.app.mtm.iaware.appmng.AppMngConstant.AppMngFeature;
import android.app.mtm.iaware.appmng.IAppCleanCallback;
import android.content.Context;
import android.os.RemoteException;
import android.rms.iaware.AwareLog;
import com.android.server.mtm.iaware.appmng.AwareAppCleanerForPG;
import com.android.server.mtm.iaware.appmng.AwareAppMngSortPolicy;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PGClean extends CleanSource {
    private static final String TAG = "PGClean";
    private IAppCleanCallback mCallback;
    private Context mContext;
    private AppCleanParam mParam;

    public PGClean(AppCleanParam param, IAppCleanCallback callback, Context context) {
        this.mParam = param;
        this.mContext = context;
        this.mCallback = callback;
    }

    public void clean() {
        if (this.mParam != null) {
            List<String> pkgList = this.mParam.getStringList();
            List<Integer> intList = this.mParam.getIntList();
            if (!pkgList.isEmpty()) {
                if (pkgList.size() != intList.size()) {
                    AwareLog.e(TAG, "size of pkglist should same to intlist");
                    return;
                }
                ArrayList<AwareProcessInfo> proclist = new ArrayList();
                int listSize = pkgList.size();
                for (int i = 0; i < listSize; i++) {
                    String packageName = (String) pkgList.get(i);
                    int userId = ((Integer) intList.get(i)).intValue();
                    ArrayList<AwareProcessInfo> procInfo = AwareProcessInfo.getAwareProcInfosFromPackage(packageName, userId);
                    if (procInfo.isEmpty()) {
                        proclist.add(CleanSource.getDeadAwareProcInfo(packageName, userId));
                    } else {
                        proclist.addAll(procInfo);
                    }
                }
                List<AwareProcessBlockInfo> info = CleanSource.mergeBlock(DecisionMaker.getInstance().decideAll(proclist, this.mParam.getLevel(), AppMngFeature.APP_CLEAN, AppCleanSource.POWER_GENIE));
                if (info == null || info.isEmpty()) {
                    AwareLog.e(TAG, "info is empty");
                    return;
                }
                Map<Integer, List<AwareProcessBlockInfo>> srcProcList = new HashMap();
                srcProcList.put(Integer.valueOf(2), info);
                int cleanCount = AwareAppCleanerForPG.getInstance(this.mContext).execute(new AwareAppMngSortPolicy(null, srcProcList), null);
                AppCleanParam result = new Builder(this.mParam.getSource()).timeStamp(this.mParam.getTimeStamp()).killedCount(cleanCount).build();
                if (this.mCallback != null) {
                    try {
                        this.mCallback.onCleanFinish(result);
                    } catch (RemoteException e) {
                        AwareLog.e(TAG, "RemoteExcption e = " + e);
                    }
                }
                for (AwareProcessBlockInfo block : info) {
                    if (block != null) {
                        AwareLog.i(TAG, "cleanCount = " + cleanCount + ", pkg = " + block.mPackageName + ", uid = " + block.mUid + ", policy = " + block.mCleanType + ", reason = " + block.mReason);
                        if (block.mDetailedReason == null) {
                            AwareLog.e(TAG, "detailedReason is null, not upload to bigData");
                        } else {
                            block.mDetailedReason.put(MemoryConstant.MEM_FILECACHE_ITEM_LEVEL, Integer.valueOf(this.mParam.getLevel()));
                            updateHistory(AppCleanSource.POWER_GENIE, block);
                            uploadToBigData(AppCleanSource.POWER_GENIE, block);
                        }
                    }
                }
            }
        }
    }
}
