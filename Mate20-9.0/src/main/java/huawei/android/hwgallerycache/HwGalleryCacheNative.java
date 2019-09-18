package huawei.android.hwgallerycache;

import android.util.Log;
import java.io.FileDescriptor;

public class HwGalleryCacheNative {
    private static final String TAG = "HwGalleryCacheNative";
    private static boolean sLoadLibraryFailed;

    private static native String nativeFD2ID(FileDescriptor fileDescriptor);

    private static native String nativeFD2Path(FileDescriptor fileDescriptor);

    static {
        try {
            System.loadLibrary("hwgallerycache_jni");
        } catch (UnsatisfiedLinkError e) {
            sLoadLibraryFailed = true;
            Log.e(TAG, "loadLibrary hwgallerycache_jni failed");
        }
        sLoadLibraryFailed = false;
    }

    public String getFilePath(FileDescriptor fd) {
        if (sLoadLibraryFailed || fd == null) {
            return null;
        }
        return nativeFD2Path(fd);
    }

    public String getFileID(FileDescriptor fd) {
        if (sLoadLibraryFailed || fd == null) {
            return null;
        }
        return nativeFD2ID(fd);
    }
}
