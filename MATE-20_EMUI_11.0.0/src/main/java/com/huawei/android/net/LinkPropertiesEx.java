package com.huawei.android.net;

import android.net.LinkProperties;

public class LinkPropertiesEx {
    private LinkProperties mLinkProperties;

    public LinkPropertiesEx(LinkProperties source) {
        this.mLinkProperties = new LinkProperties(source);
    }

    public static LinkProperties creatLinkProperties(LinkProperties source) {
        return new LinkProperties(source);
    }
}
