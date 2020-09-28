package com.huawei.android.app;

public final class HiLogLabel {
    protected android.util.HiLogLabel label;

    public HiLogLabel(int type, int domain, String tag) {
        this.label = new android.util.HiLogLabel(type, domain, tag);
    }
}
