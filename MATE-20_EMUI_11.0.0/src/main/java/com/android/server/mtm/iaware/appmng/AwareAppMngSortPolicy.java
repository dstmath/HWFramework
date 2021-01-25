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

    public AwareAppMngSortPolicy() {
    }

    public List<AwareProcessBlockInfo> getAllowStopProcBlockList() {
        Map<Integer, List<AwareProcessBlockInfo>> map = this.mSrcProcList;
        if (map == null) {
            return new ArrayList();
        }
        return map.get(2);
    }

    public List<AwareProcessBlockInfo> getShortageStopProcBlockList() {
        Map<Integer, List<AwareProcessBlockInfo>> map = this.mSrcProcList;
        if (map == null) {
            return new ArrayList();
        }
        return map.get(1);
    }

    public List<AwareProcessBlockInfo> getForbidStopProcBlockList() {
        Map<Integer, List<AwareProcessBlockInfo>> map = this.mSrcProcList;
        if (map == null) {
            return new ArrayList();
        }
        return map.get(0);
    }
}
