package com.android.server.mtm.iaware.appmng;

import android.content.Context;
import android.os.Bundle;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import java.util.List;

public class AwareAppCleanerForSm extends AwareAppCleaner {
    private static final Object LOCK = new Object();
    private static volatile AwareAppCleanerForSm sInstance;

    private AwareAppCleanerForSm(Context context) {
        super(context);
    }

    public static AwareAppCleanerForSm getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = new AwareAppCleanerForSm(context);
                }
            }
        }
        return sInstance;
    }

    @Override // com.android.server.mtm.iaware.appmng.AwareAppCleaner
    public int execute(AwareAppMngSortPolicy policy, Bundle extras) {
        int killedCount = 0;
        if (policy == null) {
            return 0;
        }
        List<AwareProcessBlockInfo> procGroups = policy.getAllowStopProcBlockList();
        if (procGroups != null && !procGroups.isEmpty()) {
            for (AwareProcessBlockInfo procGroup : procGroups) {
                AwareIntelligentRecg.getInstance().reportAllowAppStartClean(procGroup);
                killedCount += ProcessCleaner.getInstance(this.mContext).uniformClean(procGroup, extras, "SystemManager");
            }
        }
        return killedCount;
    }
}
