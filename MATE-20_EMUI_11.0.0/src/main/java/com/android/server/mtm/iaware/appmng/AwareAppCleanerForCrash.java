package com.android.server.mtm.iaware.appmng;

import android.content.Context;
import android.os.Bundle;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.huawei.android.os.UserHandleEx;
import java.util.List;

public class AwareAppCleanerForCrash extends AwareAppCleaner {
    private static final Object LOCK = new Object();
    private static volatile AwareAppCleanerForCrash sInstance;

    private AwareAppCleanerForCrash(Context context) {
        super(context);
    }

    public static AwareAppCleanerForCrash getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = new AwareAppCleanerForCrash(context);
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
                AwareIntelligentRecg.getInstance().reportAbnormalClean(procGroup);
                int count = ProcessCleaner.getInstance(this.mContext).uniformClean(procGroup, extras, "CrashClean");
                killedCount += count;
                addHwStop(procGroup, count);
            }
        }
        return killedCount;
    }

    private void addHwStop(AwareProcessBlockInfo procGroup, int count) {
        if (procGroup != null && count != 0 && ProcessCleaner.CleanType.FORCE_STOP.equals(procGroup.procCleanType)) {
            AwareIntelligentRecg.getInstance().setHwStopFlag(UserHandleEx.getUserId(procGroup.procUid), procGroup.procPackageName, true);
        }
    }
}
