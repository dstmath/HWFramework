package org.apache.harmony.luni.internal.util;

public abstract class TimezoneGetter {
    private static TimezoneGetter instance;

    public abstract String getId();

    public static TimezoneGetter getInstance() {
        return instance;
    }

    public static void setInstance(TimezoneGetter getter) {
        if (instance != null) {
            throw new UnsupportedOperationException("TimezoneGetter instance already set");
        }
        instance = getter;
    }
}
