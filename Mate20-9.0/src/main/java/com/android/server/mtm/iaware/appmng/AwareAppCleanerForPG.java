package com.android.server.mtm.iaware.appmng;

import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import com.android.server.mtm.iaware.appmng.appstart.AwareAppStartupPolicy;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
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
        if (procGroups != null && !procGroups.isEmpty()) {
            for (AwareProcessBlockInfo procGroup : procGroups) {
                reportAppStart(procGroup);
                int count = ProcessCleaner.getInstance(this.mContext).uniformClean(procGroup, extras, "PowerGenie");
                killedCount += count;
                addHwStop(procGroup, count);
            }
        }
        return killedCount;
    }

    private void reportAppStart(AwareProcessBlockInfo procGroup) {
        if (procGroup != null && !ProcessCleaner.CleanType.NONE.equals(procGroup.mCleanType)) {
            AwareAppStartupPolicy awareAppStartupPolicy = AwareAppStartupPolicy.self();
            if (awareAppStartupPolicy != null) {
                awareAppStartupPolicy.reportPgClean(procGroup.mPackageName);
            }
        }
    }

    private void addHwStop(AwareProcessBlockInfo procGroup, int count) {
        if (!(procGroup == null || count == 0 || !ProcessCleaner.CleanType.FORCESTOP.equals(procGroup.mCleanType))) {
            AwareIntelligentRecg.getInstance().setHwStopFlag(UserHandle.getUserId(procGroup.mUid), procGroup.mPackageName, true);
        }
    }
}
