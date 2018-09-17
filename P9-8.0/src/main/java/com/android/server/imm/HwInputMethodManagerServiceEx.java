package com.android.server.imm;

import android.content.Context;

public final class HwInputMethodManagerServiceEx implements IHwInputMethodManagerServiceEx {
    static final String TAG = "HwInputMethodManagerServiceEx";
    IHwInputMethodManagerInner mIImsInner = null;

    public HwInputMethodManagerServiceEx(IHwInputMethodManagerInner iims, Context context) {
        this.mIImsInner = iims;
    }

    public void setDefaultIme(String imeId) {
        this.mIImsInner.setInputMethodLockedByInner(imeId);
    }

    public void setKeyguardEnable() {
        this.mIImsInner.sendEnableKeyguardBroadcastByInner();
    }
}
