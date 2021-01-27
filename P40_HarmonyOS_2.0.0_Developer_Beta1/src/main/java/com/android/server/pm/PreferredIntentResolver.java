package com.android.server.pm;

import com.android.server.IntentResolver;
import java.io.PrintWriter;

public class PreferredIntentResolver extends IntentResolver<PreferredActivity, PreferredActivity> {
    /* access modifiers changed from: protected */
    @Override // com.android.server.IntentResolver
    public PreferredActivity[] newArray(int size) {
        return new PreferredActivity[size];
    }

    /* access modifiers changed from: protected */
    public boolean isPackageForFilter(String packageName, PreferredActivity filter) {
        return packageName.equals(filter.mPref.mComponent.getPackageName());
    }

    /* access modifiers changed from: protected */
    public void dumpFilter(PrintWriter out, String prefix, PreferredActivity filter) {
        filter.mPref.dump(out, prefix, filter);
    }
}
