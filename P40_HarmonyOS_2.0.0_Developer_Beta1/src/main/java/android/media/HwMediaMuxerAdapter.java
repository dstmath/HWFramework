package android.media;

public final class HwMediaMuxerAdapter {
    private static native void nativeSetUserTag(long j, String str);

    static {
        System.loadLibrary("media_jni.huawei");
    }

    public static void setUserTag(long nativeObject, String userTag) {
        nativeSetUserTag(nativeObject, userTag);
    }
}
