package android.content.res;

import android.content.pm.ApplicationInfo;

public class CompatibilityInfoEx {
    private CompatibilityInfo mCompatibilityInfo;

    public void setCompatibilityInfo(CompatibilityInfo compatibilityInfo) {
        this.mCompatibilityInfo = compatibilityInfo;
    }

    public boolean isEmpty() {
        return this.mCompatibilityInfo == null;
    }

    public ApplicationInfo getApplicationInfo() {
        CompatibilityInfo compatibilityInfo = this.mCompatibilityInfo;
        if (compatibilityInfo != null) {
            return compatibilityInfo.mAppInfo;
        }
        return null;
    }
}
