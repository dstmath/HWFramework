package com.android.internal.telephony.cat;

public class DefaultHwCatServiceEx implements IHwCatServiceEx {
    private ICatServiceInner mCatServiceInner;

    public DefaultHwCatServiceEx() {
    }

    public DefaultHwCatServiceEx(ICatServiceInner catServiceInner) {
        this.mCatServiceInner = catServiceInner;
    }
}
