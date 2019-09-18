package com.android.server.pm;

import com.android.server.IntentResolver;

public class PersistentPreferredIntentResolver extends IntentResolver<PersistentPreferredActivity, PersistentPreferredActivity> {
    /* access modifiers changed from: protected */
    public PersistentPreferredActivity[] newArray(int size) {
        return new PersistentPreferredActivity[size];
    }

    /* access modifiers changed from: protected */
    public boolean isPackageForFilter(String packageName, PersistentPreferredActivity filter) {
        return packageName.equals(filter.mComponent.getPackageName());
    }
}
