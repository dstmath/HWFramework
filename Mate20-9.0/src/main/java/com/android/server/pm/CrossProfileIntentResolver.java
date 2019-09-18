package com.android.server.pm;

import com.android.server.IntentResolver;
import java.util.List;

class CrossProfileIntentResolver extends IntentResolver<CrossProfileIntentFilter, CrossProfileIntentFilter> {
    CrossProfileIntentResolver() {
    }

    /* access modifiers changed from: protected */
    public CrossProfileIntentFilter[] newArray(int size) {
        return new CrossProfileIntentFilter[size];
    }

    /* access modifiers changed from: protected */
    public boolean isPackageForFilter(String packageName, CrossProfileIntentFilter filter) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void sortResults(List<CrossProfileIntentFilter> list) {
    }
}
