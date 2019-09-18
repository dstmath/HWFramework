package com.android.server.net;

import android.content.Context;

public final class HwNetworkStatsServiceEx implements IHwNetworkStatsServiceEx {
    static final String TAG = "HwNetworkStatsServiceEx";
    final Context mContext;
    IHwNetworkStatsInner mINetworkStatsInner = null;

    public HwNetworkStatsServiceEx(IHwNetworkStatsInner ins, Context context) {
        this.mINetworkStatsInner = ins;
        this.mContext = context;
    }
}
