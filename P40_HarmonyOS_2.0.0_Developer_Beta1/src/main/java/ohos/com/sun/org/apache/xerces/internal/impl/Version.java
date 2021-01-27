package ohos.com.sun.org.apache.xerces.internal.impl;

public class Version {
    private static final String fImmutableVersion = "Xerces-J 2.7.1";
    public static final String fVersion = getVersion();

    public static String getVersion() {
        return fImmutableVersion;
    }

    public static void main(String[] strArr) {
        System.out.println(fVersion);
    }
}
