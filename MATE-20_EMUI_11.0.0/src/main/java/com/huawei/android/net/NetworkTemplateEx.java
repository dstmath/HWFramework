package com.huawei.android.net;

import android.net.NetworkTemplate;

public class NetworkTemplateEx {
    public static final int MATCH_WIFI_WILDCARD = 7;
    private NetworkTemplate mNetworkTemplate;

    public NetworkTemplateEx(NetworkTemplate networkTemplate) {
        this.mNetworkTemplate = networkTemplate;
    }

    public static NetworkTemplateEx buildTemplateWifiWildcard() {
        return new NetworkTemplateEx(NetworkTemplate.buildTemplateWifiWildcard());
    }

    public static NetworkTemplateEx buildTemplateMobileAll(String subscriberId) {
        return new NetworkTemplateEx(NetworkTemplate.buildTemplateMobileAll(subscriberId));
    }

    public NetworkTemplate getNetworkTemplate() {
        return this.mNetworkTemplate;
    }

    public int getMatchRule() {
        NetworkTemplate networkTemplate = this.mNetworkTemplate;
        if (networkTemplate == null) {
            return 0;
        }
        return networkTemplate.getMatchRule();
    }
}
