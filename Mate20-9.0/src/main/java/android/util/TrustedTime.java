package android.util;

public interface TrustedTime {
    long currentTimeMillis();

    boolean forceRefresh();

    long getCacheAge();

    long getCacheCertainty();

    boolean hasCache();
}
