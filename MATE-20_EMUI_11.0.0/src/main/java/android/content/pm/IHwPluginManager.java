package android.content.pm;

public interface IHwPluginManager {
    public static final int VERSION_APIMAJOR_POS = 10000000;
    public static final int VERSION_API_POS = 10000;

    static int compareAPIMajorVersion(int versionA, int versionB) {
        return Integer.compare(versionA / VERSION_APIMAJOR_POS, versionB / VERSION_APIMAJOR_POS);
    }

    static int compareAPIMajorVersion(long versionA, long versionB) {
        return Long.compare(versionA / 10000000, versionB / 10000000);
    }
}
