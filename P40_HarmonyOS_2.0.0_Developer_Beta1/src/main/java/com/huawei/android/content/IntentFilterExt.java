package com.huawei.android.content;

import android.content.ActionFilterEntry;
import android.content.IntentFilter;
import java.util.Iterator;

public class IntentFilterExt {
    public static Iterator<ActionFilterEntry> actionFilterIterator(IntentFilter filter) {
        if (filter == null) {
            return null;
        }
        return filter.actionFilterIterator();
    }

    public static String getIdentifier(IntentFilter filter) {
        if (filter == null) {
            return null;
        }
        return filter.getIdentifier();
    }

    public static int countActionFilters(IntentFilter filter) {
        if (filter == null) {
            return 0;
        }
        return filter.countActionFilters();
    }
}
