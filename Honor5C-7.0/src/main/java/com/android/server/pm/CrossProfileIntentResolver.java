package com.android.server.pm;

import com.android.server.IntentResolver;
import java.util.List;

class CrossProfileIntentResolver extends IntentResolver<CrossProfileIntentFilter, CrossProfileIntentFilter> {
    CrossProfileIntentResolver() {
    }

    protected CrossProfileIntentFilter[] newArray(int size) {
        return new CrossProfileIntentFilter[size];
    }

    protected boolean isPackageForFilter(String packageName, CrossProfileIntentFilter filter) {
        return false;
    }

    protected void sortResults(List<CrossProfileIntentFilter> list) {
    }
}
