package huawei.hiview;

import android.util.Log;

public class NativeWriter implements Writer {
    private static native int write2N(String str);

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
