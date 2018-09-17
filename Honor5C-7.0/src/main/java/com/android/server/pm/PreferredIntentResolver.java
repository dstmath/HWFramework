package com.android.server.pm;

import com.android.server.IntentResolver;
import java.io.PrintWriter;

public class PreferredIntentResolver extends IntentResolver<PreferredActivity, PreferredActivity> {
    protected PreferredActivity[] newArray(int size) {
        return new PreferredActivity[size];
    }

    protected boolean isPackageForFilter(String packageName, PreferredActivity filter) {
        return packageName.equals(filter.mPref.mComponent.getPackageName());
    }

    protected void dumpFilter(PrintWriter out, String prefix, PreferredActivity filter) {
        filter.mPref.dump(out, prefix, filter);
    }
}
