package huawei.android.hwgallerycache;

import android.util.Log;
import java.io.FileDescriptor;

public class HwGalleryCacheNative {
    private static final String TAG = "HwGalleryCacheNative";
    private static boolean sIsLoadLibraryFailed;

    private static native String nativeFD2ID(FileDescriptor fileDescriptor);

    private static native String nativeFD2Path(FileDescriptor fileDescriptor);

    static {
        try {
            System.loadLibrary("hwgallerycache_jni");
        } catch (UnsatisfiedLinkError e) {
            sIsLoadLibraryFailed = true;
            Log.e(TAG, "loadLibrary hwgallerycache_jni failed");
        }
        sIsLoadLibraryFailed = false;
    }

    public String getFilePath(FileDescriptor fd) {
        if (sIsLoadLibraryFailed || fd == null) {
            return null;
        }
        return nativeFD2Path(fd);
    }

    public String getFileId(FileDescriptor fd) {
        if (sIsLoadLibraryFailed || fd == null) {
            return null;
        }
        return nativeFD2ID(fd);
    }
}
