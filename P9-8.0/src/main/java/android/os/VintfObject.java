package android.os;

import java.util.Map;

public class VintfObject {
    public static native String[] getHalNamesAndVersions();

    public static native String getSepolicyVersion();

    public static native Map<String, String[]> getVndkSnapshots();

    public static native String[] report();

    public static native int verify(String[] strArr);
}
