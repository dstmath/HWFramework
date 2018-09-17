package com.android.server.mtm.iaware.appmng;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AwareAppMngSortPolicy {
    Map<Integer, List<AwareProcessBlockInfo>> mSrcProcList = null;

    public AwareAppMngSortPolicy(Context context, Map<Integer, List<AwareProcessBlockInfo>> sortProcList) {
        this.mSrcProcList = sortProcList;
    }

    public List<AwareProcessBlockInfo> getAllowStopProcBlockList() {
        if (this.mSrcProcList == null) {
            return new ArrayList();
        }
        return (List) this.mSrcProcList.get(Integer.valueOf(2));
    }

    public List<AwareProcessBlockInfo> getShortageStopProcBlockList() {
        if (this.mSrcProcList == null) {
            return new ArrayList();
        }
        return (List) this.mSrcProcList.get(Integer.valueOf(1));
    }

    public List<AwareProcessBlockInfo> getForbidStopProcBlockList() {
        if (this.mSrcProcList == null) {
            return new ArrayList();
        }
        return (List) this.mSrcProcList.get(Integer.valueOf(0));
    }
}
