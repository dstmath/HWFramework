package sun.misc;

public class SharedSecrets {
    private static JavaIOFileDescriptorAccess javaIOFileDescriptorAccess;

    public static void setJavaIOFileDescriptorAccess(JavaIOFileDescriptorAccess jiofda) {
        javaIOFileDescriptorAccess = jiofda;
    }

    public static JavaIOFileDescriptorAccess getJavaIOFileDescriptorAccess() {
        return javaIOFileDescriptorAccess;
    }
}
