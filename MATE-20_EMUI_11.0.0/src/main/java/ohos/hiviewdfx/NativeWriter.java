package ohos.hiviewdfx;

class NativeWriter implements Writer {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218115329, "NativeWriter");

    private static native int write2N(String str);

    NativeWriter() {
    }

    static {
        try {
            System.loadLibrary("hiview_jni.z");
        } catch (UnsatisfiedLinkError e) {
            HiLog.error(LABEL, "libhiview_jni.so error:%{public}s", e.getMessage());
        }
    }

    @Override // ohos.hiviewdfx.Writer
    public int write(String str) throws UnsatisfiedLinkError {
        return write2N(str);
    }
}
