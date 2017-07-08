package com.android.server.pm;

import com.android.server.IntentResolver;

public class PersistentPreferredIntentResolver extends IntentResolver<PersistentPreferredActivity, PersistentPreferredActivity> {
    protected PersistentPreferredActivity[] newArray(int size) {
        return new PersistentPreferredActivity[size];
    }

    protected boolean isPackageForFilter(String packageName, PersistentPreferredActivity filter) {
        return packageName.equals(filter.mComponent.getPackageName());
    }
}
