package com.android.server.rms.iaware.dev;

import android.rms.iaware.AwareConfig;
import android.util.ArrayMap;
import java.util.Map;

public abstract class RuleBase {
    private static final String TAG = "RuleBase";
    protected final Map<String, String> mItemValue = new ArrayMap();
    protected long mMode = 0;
    public int mPriority = 0;

    public abstract boolean fillRuleInfo(AwareConfig.SubItem subItem);

    public abstract boolean isMatch(Object... objArr);

    public int getPriority() {
        return this.mPriority;
    }

    public long getMode() {
        return this.mMode;
    }

    public String getItemValue(String key) {
        return this.mItemValue.get(key);
    }
}
