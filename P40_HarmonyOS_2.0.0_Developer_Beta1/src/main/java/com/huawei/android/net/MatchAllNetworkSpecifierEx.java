package com.huawei.android.net;

import android.net.MatchAllNetworkSpecifier;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class MatchAllNetworkSpecifierEx {
    private MatchAllNetworkSpecifier mMatchAllNetworkSpecifier = new MatchAllNetworkSpecifier();

    public MatchAllNetworkSpecifier getMatchAllNetworkSpecifier() {
        return this.mMatchAllNetworkSpecifier;
    }
}
