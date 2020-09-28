package android.os;

public class VintfRuntimeInfo {
    public static native String getBootAvbVersion();

    public static native String getBootVbmetaAvbVersion();

    public static native String getCpuInfo();

    public static native String getHardwareId();

    public static native long getKernelSepolicyVersion();

    public static native String getKernelVersion();

    public static native String getNodeName();

    public static native String getOsName();

    public static native String getOsRelease();

    public static native String getOsVersion();

    private VintfRuntimeInfo() {
    }
}
