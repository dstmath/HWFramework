package android.content.pm;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import java.util.Collections;
import java.util.List;

public final class AuxiliaryResolveInfo {
    public final Intent failureIntent;
    public final List<AuxiliaryFilter> filters;
    public final ComponentName installFailureActivity;
    public final boolean needsPhaseTwo;
    public final String token;

    public static final class AuxiliaryFilter extends IntentFilter {
        public final Bundle extras;
        public final String packageName;
        public final InstantAppResolveInfo resolveInfo;
        public final String splitName;
        public final long versionCode;

        public AuxiliaryFilter(IntentFilter orig, InstantAppResolveInfo resolveInfo2, String splitName2, Bundle extras2) {
            super(orig);
            this.resolveInfo = resolveInfo2;
            this.packageName = resolveInfo2.getPackageName();
            this.versionCode = resolveInfo2.getLongVersionCode();
            this.splitName = splitName2;
            this.extras = extras2;
        }

        public AuxiliaryFilter(InstantAppResolveInfo resolveInfo2, String splitName2, Bundle extras2) {
            this.resolveInfo = resolveInfo2;
            this.packageName = resolveInfo2.getPackageName();
            this.versionCode = resolveInfo2.getLongVersionCode();
            this.splitName = splitName2;
            this.extras = extras2;
        }

        public AuxiliaryFilter(String packageName2, long versionCode2, String splitName2) {
            this.resolveInfo = null;
            this.packageName = packageName2;
            this.versionCode = versionCode2;
            this.splitName = splitName2;
            this.extras = null;
        }

        public String toString() {
            return "AuxiliaryFilter{packageName='" + this.packageName + '\'' + ", versionCode=" + this.versionCode + ", splitName='" + this.splitName + '\'' + '}';
        }
    }

    public AuxiliaryResolveInfo(String token2, boolean needsPhase2, Intent failureIntent2, List<AuxiliaryFilter> filters2) {
        this.token = token2;
        this.needsPhaseTwo = needsPhase2;
        this.failureIntent = failureIntent2;
        this.filters = filters2;
        this.installFailureActivity = null;
    }

    public AuxiliaryResolveInfo(ComponentName failureActivity, Intent failureIntent2, List<AuxiliaryFilter> filters2) {
        this.installFailureActivity = failureActivity;
        this.filters = filters2;
        this.token = null;
        this.needsPhaseTwo = false;
        this.failureIntent = failureIntent2;
    }

    public AuxiliaryResolveInfo(ComponentName failureActivity, String packageName, long versionCode, String splitName) {
        this(failureActivity, null, Collections.singletonList(new AuxiliaryFilter(packageName, versionCode, splitName)));
    }
}
