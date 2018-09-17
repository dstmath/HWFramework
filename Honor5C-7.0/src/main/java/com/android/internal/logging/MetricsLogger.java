package com.android.internal.logging;

import android.content.Context;
import android.os.Build;

public class MetricsLogger {
    public static final int VIEW_UNKNOWN = 0;

    public static void visible(Context context, int category) throws IllegalArgumentException {
        if (Build.IS_DEBUGGABLE && category == 0) {
            throw new IllegalArgumentException("Must define metric category");
        }
        EventLogTags.writeSysuiViewVisibility(category, 100);
    }

    public static void hidden(Context context, int category) throws IllegalArgumentException {
        if (Build.IS_DEBUGGABLE && category == 0) {
            throw new IllegalArgumentException("Must define metric category");
        }
        EventLogTags.writeSysuiViewVisibility(category, 0);
    }

    public static void visibility(Context context, int category, boolean visibile) throws IllegalArgumentException {
        if (visibile) {
            visible(context, category);
        } else {
            hidden(context, category);
        }
    }

    public static void visibility(Context context, int category, int vis) throws IllegalArgumentException {
        boolean z = false;
        if (vis == 0) {
            z = true;
        }
        visibility(context, category, z);
    }

    public static void action(Context context, int category) {
        action(context, category, "");
    }

    public static void action(Context context, int category, int value) {
        action(context, category, Integer.toString(value));
    }

    public static void action(Context context, int category, boolean value) {
        action(context, category, Boolean.toString(value));
    }

    public static void action(Context context, int category, String pkg) {
        if (Build.IS_DEBUGGABLE && category == 0) {
            throw new IllegalArgumentException("Must define metric category");
        }
        EventLogTags.writeSysuiAction(category, pkg);
    }

    public static void count(Context context, String name, int value) {
        EventLogTags.writeSysuiCount(name, value);
    }

    public static void histogram(Context context, String name, int bucket) {
        EventLogTags.writeSysuiHistogram(name, bucket);
    }
}
