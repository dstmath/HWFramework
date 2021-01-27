package huawei.hiview;

import android.util.Log;

/* access modifiers changed from: package-private */
public class NativeWriter implements Writer {
    private static native int write2N(String str);

    NativeWriter() {
    }

    static {
        try {
            System.loadLibrary("hiview_jni");
        } catch (UnsatisfiedLinkError e) {
            Log.e("HiView.HiEvent", "libhiview_jni.so error:" + e.getMessage());
        }
    }

    @Override // huawei.hiview.Writer
    public int write(String src) throws UnsatisfiedLinkError {
        return write2N(src);
    }
}
