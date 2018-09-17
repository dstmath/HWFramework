package com.android.server.mtm.iaware.appmng;

import android.content.Context;
import android.os.Bundle;
import com.android.server.mtm.iaware.appmng.appstart.AwareAppStartupPolicy;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.mtm.taskstatus.ProcessCleaner.CleanType;
import java.util.List;

public class AwareAppCleanerForPG extends AwareAppCleaner {
    private static volatile AwareAppCleanerForPG instance;

    private AwareAppCleanerForPG(Context context) {
        super(context);
    }

    public static AwareAppCleanerForPG getInstance(Context context) {
        if (instance == null) {
            synchronized (AwareAppCleanerForPG.class) {
                if (instance == null) {
                    instance = new AwareAppCleanerForPG(context);
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
                reportAppStart(procGroup);
                killedCount += ProcessCleaner.getInstance(this.mContext).uniformClean(procGroup, extras, "PowerGenie");
            }
        }
        return killedCount;
    }

    private void reportAppStart(AwareProcessBlockInfo procGroup) {
        if (procGroup != null && (CleanType.NONE.equals(procGroup.mCleanType) ^ 1) != 0) {
            AwareAppStartupPolicy awareAppStartupPolicy = AwareAppStartupPolicy.self();
            if (awareAppStartupPolicy != null) {
                awareAppStartupPolicy.reportPgClean(procGroup.mPackageName);
            }
        }
    }
}
