package com.android.server.am;

import android.content.IntentFilter;
import com.huawei.annotation.HwSystemApi;
import java.util.ArrayList;
import java.util.List;

@HwSystemApi
public class BroadcastFilterEx {
    private BroadcastFilter mBroadcastFilter;

    public BroadcastFilterEx() {
    }

    public BroadcastFilterEx(BroadcastFilter bf) {
        this.mBroadcastFilter = bf;
    }

    public static List<BroadcastFilterEx> getBfExList(List<BroadcastFilter> bfList) {
        List<BroadcastFilterEx> bfExList = new ArrayList<>();
        if (bfList == null) {
            return bfExList;
        }
        for (BroadcastFilter bf : bfList) {
            bfExList.add(new BroadcastFilterEx(bf));
        }
        return bfExList;
    }

    public String getPackageName() {
        return this.mBroadcastFilter.packageName;
    }

    public ReceiverListEx getReceiverList() {
        return new ReceiverListEx(this.mBroadcastFilter.receiverList);
    }

    public int countActionFilters() {
        return this.mBroadcastFilter.countActionFilters();
    }

    public String getIdentifier() {
        return this.mBroadcastFilter.getIdentifier();
    }

    public boolean isFilterNull() {
        return this.mBroadcastFilter == null;
    }

    public static BroadcastFilterEx getBroadcastFilterEx(Object target) {
        if (!(target instanceof BroadcastFilter)) {
            return null;
        }
        return new BroadcastFilterEx((BroadcastFilter) target);
    }

    public BroadcastFilter getBroadcastFilter() {
        return this.mBroadcastFilter;
    }

    public IntentFilter getFilter() {
        return this.mBroadcastFilter;
    }

    public static boolean isSameReceivers(BroadcastFilterEx filterSrc, BroadcastFilterEx filterTar) {
        if (filterSrc == null || filterTar == null || filterSrc.isFilterNull() || filterSrc.isFilterNull() || filterSrc.getBroadcastFilter().receiverList != filterTar.getBroadcastFilter().receiverList) {
            return false;
        }
        return true;
    }
}
