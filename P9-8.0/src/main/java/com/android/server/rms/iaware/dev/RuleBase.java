package com.android.server.rms.iaware.dev;

import android.rms.iaware.AwareConfig.SubItem;

public abstract class RuleBase {
    private static final String TAG = "RuleBase";
    protected long mMode = 0;
    public int mPriority = 0;

    public abstract boolean fillRuleInfo(SubItem subItem);

    public abstract boolean isMatch(Object... objArr);

    public int getPriority() {
        return this.mPriority;
    }

    public long getMode() {
        return this.mMode;
    }
}
