package android.util;

import android.annotation.UnsupportedAppUsage;

public interface TrustedTime {
    @UnsupportedAppUsage
    long currentTimeMillis();

    @UnsupportedAppUsage
    boolean forceRefresh();

    @UnsupportedAppUsage
    long getCacheAge();

    long getCacheCertainty();

    @UnsupportedAppUsage
    boolean hasCache();
}
