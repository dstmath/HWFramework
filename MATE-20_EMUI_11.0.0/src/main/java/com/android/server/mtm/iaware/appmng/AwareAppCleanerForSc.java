package com.android.server.mtm.iaware.appmng;

import android.content.Context;
import android.os.Bundle;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import java.util.List;

public class AwareAppCleanerForSc extends AwareAppCleaner {
    private static final Object LOCK = new Object();
    private static volatile AwareAppCleanerForSc sInstance;

    private AwareAppCleanerForSc(Context context) {
        super(context);
    }

    public static AwareAppCleanerForSc getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = new AwareAppCleanerForSc(context);
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
                killedCount += ProcessCleaner.getInstance(this.mContext).uniformClean(procGroup, extras, "SmartClean");
            }
        }
        return killedCount;
    }
}
