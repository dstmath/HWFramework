package com.android.server.mtm.iaware.appmng;

import android.content.Context;
import android.os.Bundle;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import java.util.List;

public class AwareAppCleanerForSM extends AwareAppCleaner {
    private static final String TAG = "AwareSM_Kill";
    private static volatile AwareAppCleanerForSM instance;

    private AwareAppCleanerForSM(Context context) {
        super(context);
    }

    public static AwareAppCleanerForSM getInstance(Context context) {
        if (instance == null) {
            synchronized (AwareAppCleanerForSM.class) {
                if (instance == null) {
                    instance = new AwareAppCleanerForSM(context);
                }
            }
        }
        return instance;
    }

    public int execute(AwareAppMngSortPolicy policy, Bundle extras) {
        int killedCount = 0;
        if (policy == null) {
            return 0;
        }
        List<AwareProcessBlockInfo> procGroups = policy.getAllowStopProcBlockList();
        if (!(procGroups == null || (procGroups.isEmpty() ^ 1) == 0)) {
            for (AwareProcessBlockInfo procGroup : procGroups) {
                killedCount += ProcessCleaner.getInstance(this.mContext).uniformClean(procGroup, extras, "SystemManager");
            }
        }
        return killedCount;
    }
}
