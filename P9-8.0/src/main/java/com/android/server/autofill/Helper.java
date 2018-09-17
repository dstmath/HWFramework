package com.android.server.autofill;

import android.os.Bundle;
import android.util.ArraySet;
import android.view.autofill.AutofillId;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

public final class Helper {
    public static boolean sDebug = false;
    static int sPartitionMaxCount = 10;
    public static boolean sVerbose = false;

    private Helper() {
        throw new UnsupportedOperationException("contains static members only");
    }

    static void append(StringBuilder builder, Bundle bundle) {
        if (bundle == null || (sVerbose ^ 1) != 0) {
            builder.append("null");
            return;
        }
        Set<String> keySet = bundle.keySet();
        builder.append("[Bundle with ").append(keySet.size()).append(" extras:");
        for (String key : keySet) {
            Object obj = bundle.get(key);
            builder.append(' ').append(key).append('=');
            if (obj instanceof Object[]) {
                obj = Arrays.toString((Objects[]) obj);
            }
            builder.append(obj);
        }
        builder.append(']');
    }

    static String bundleToString(Bundle bundle) {
        StringBuilder builder = new StringBuilder();
        append(builder, bundle);
        return builder.toString();
    }

    static AutofillId[] toArray(ArraySet<AutofillId> set) {
        if (set == null) {
            return null;
        }
        AutofillId[] array = new AutofillId[set.size()];
        for (int i = 0; i < set.size(); i++) {
            array[i] = (AutofillId) set.valueAt(i);
        }
        return array;
    }
}
